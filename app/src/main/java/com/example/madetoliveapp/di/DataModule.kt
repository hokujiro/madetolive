package com.example.madetoliveapp.di

import android.util.Log
import org.koin.dsl.module
import androidx.room.Room
import com.example.madetoliveapp.data.mapper.RemoteMapper
import com.example.madetoliveapp.data.mapper.RemoteMapperImpl
import com.example.madetoliveapp.data.repository.auth.AuthRepository
import com.example.madetoliveapp.data.repository.auth.AuthRepositoryImpl
import com.example.madetoliveapp.data.repository.projects.ProjectRepository
import com.example.madetoliveapp.data.repository.projects.ProjectRepositoryImpl
import com.example.madetoliveapp.data.repository.tasks.TaskRepository
import com.example.madetoliveapp.data.repository.tasks.TaskRepositoryImpl
import com.example.madetoliveapp.data.repository.user.UserRepository
import com.example.madetoliveapp.data.repository.user.UserRepositoryImpl
import com.example.madetoliveapp.data.source.local.bbdd.AppDatabase
import com.example.madetoliveapp.data.source.remote.api.ProjectApi
import com.example.madetoliveapp.data.source.remote.api.TaskApi
import com.example.madetoliveapp.data.source.remote.api.UserApi
import com.example.madetoliveapp.data.source.remote.auth.AuthApi
import com.example.madetoliveapp.data.source.remote.auth.RefreshTokenRequest
import com.example.madetoliveapp.presentation.auth.TokenManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


val dataModule = module {

    single { TokenManager(androidContext()) }

    // Room Database
    single {
        Room.databaseBuilder(get(), AppDatabase::class.java, "app_database")
            .fallbackToDestructiveMigration()
            .build()
    }

    //http client
    single(named("unauthenticated")) {
        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)

        OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    //http client
    single(named("authenticated")) {
        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor(TokenInterceptor(get(), get())) // ✅ Pass authApi
            .build()
    }

    // Retrofit without token (AuthApi)
    single(named("unauthenticatedRetrofit")) {
        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8080/")
           // .baseUrl("https://server-m6em.onrender.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(get(named("unauthenticated")))
            .build()
    }

// Retrofit with token (TaskApi)
    single(named("authenticatedRetrofit")) {
        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8080/")
           // .baseUrl("https://server-m6em.onrender.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(get(named("authenticated")))
            .build()
    }

    // DAO
    single { get<AppDatabase>().taskDao() }

    // AuthApi
    single<AuthApi> { get<Retrofit>(named("unauthenticatedRetrofit")).create(AuthApi::class.java) }
    single<TaskApi> { get<Retrofit>(named("authenticatedRetrofit")).create(TaskApi::class.java) }
    single<UserApi> { get<Retrofit>(named("authenticatedRetrofit")).create(UserApi::class.java) }
    single<ProjectApi> { get<Retrofit>(named("authenticatedRetrofit")).create(ProjectApi::class.java) }

    // Repository
    single<TaskRepository> { TaskRepositoryImpl(get(), get()) }
    single<ProjectRepository> { ProjectRepositoryImpl(get(), get()) }
    single<AuthRepository> { AuthRepositoryImpl(get()) }
    single<UserRepository> { UserRepositoryImpl(get(), get()) }
    single<RemoteMapper> { RemoteMapperImpl() }
}

class TokenInterceptor(
    private val tokenManager: TokenManager,
    private val authApi: AuthApi // ✅ Inject AuthApi (unauthenticated)
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requestBuilder = originalRequest.newBuilder()

        val accessToken = tokenManager.getAccessToken()
        if (!accessToken.isNullOrEmpty()) {
            requestBuilder.addHeader("Authorization", "Bearer $accessToken")
        }

        val response = chain.proceed(requestBuilder.build())

        // If 401 — try refreshing token
        if (response.code == 401) {
            response.close() // 👈 Important to close the original response
            val refreshToken = tokenManager.getRefreshToken()

            if (!refreshToken.isNullOrEmpty()) {
                try {
                    val refreshResponse = runBlocking {
                        authApi.refreshToken(RefreshTokenRequest(refreshToken))
                    }

                    if (refreshResponse.isSuccessful) {
                        val newTokens = refreshResponse.body()
                        if (newTokens != null) {
                            tokenManager.saveTokens(newTokens.accessToken, newTokens.refreshToken)

                            // Retry original request with new token
                            val newRequest = originalRequest.newBuilder()
                                .removeHeader("Authorization")
                                .addHeader("Authorization", "Bearer ${newTokens.accessToken}")
                                .build()

                            return chain.proceed(newRequest)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("TokenInterceptor", "Token refresh failed: ${e.message}")
                }
            }

            // Refresh failed → logout
            tokenManager.clearTokens()
            tokenManager.notifySessionExpired()
        }

        return response
    }
}
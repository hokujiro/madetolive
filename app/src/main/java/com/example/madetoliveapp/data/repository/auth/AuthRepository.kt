package com.example.madetoliveapp.data.repository.auth

import com.example.madetoliveapp.data.source.remote.auth.AuthRequest
import com.example.madetoliveapp.data.source.remote.auth.AuthResponse
import com.example.madetoliveapp.data.source.remote.auth.TokenResponse
import com.example.madetoliveapp.data.source.remote.googleauth.GoogleAuthRequest
import com.example.madetoliveapp.data.source.remote.googleauth.GoogleAuthResponse
import retrofit2.Response

interface AuthRepository {
    suspend fun login(authRequest: AuthRequest): Response<AuthResponse> // Fetch all tasks
    suspend fun register(authRequest: AuthRequest): Response<AuthResponse> // Fetch all tasks
    suspend fun googleSignIn(authRequest: GoogleAuthRequest): Response<GoogleAuthResponse>  // Fetch all tasks
    suspend fun refreshToken(refreshToken: String): Response<TokenResponse>
}

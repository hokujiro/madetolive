package com.example.madetoliveapp.data.repository.tasks

import com.example.madetoliveapp.data.mapper.RemoteMapper
import com.example.madetoliveapp.data.source.remote.api.TaskApi
import com.example.madetoliveapp.domain.model.DailyPointsSummaryModel
import com.example.madetoliveapp.domain.model.TaskModel

class TaskRepositoryImpl(
    //private val taskDao: TaskDao,
    private val mapper: RemoteMapper,
    private val taskApi: TaskApi
) : TaskRepository {

/*    // Fetch all tasks from the database
    override suspend fun getAllTasks(): List<TaskModel> {
        return taskDao.getAll().map { mapper.toModel(it) }
    } */

    override suspend fun getAllTasks(): List<TaskModel> {
        val response = taskApi.getAllTasks()
        if (response.isSuccessful) {
            return response.body()?.map { mapper.toTaskDomainModel(it) } ?: emptyList()
        } else {
            throw Exception("Failed to load tasks: ${response.errorBody()?.string()}")
        }
    }
    /*
        // Fetch all tasks from the database
        override suspend fun getTasksForDay(date: Long): List<TaskModel> {
            return taskDao.getTasksForDay(date).map { mapper.toModel(it) }
        }
     */

    override suspend fun getTasksForDay(date: Long): List<TaskModel> {
        val response = taskApi.getTasksForDay(date)
        if (response.isSuccessful) {
            return response.body()?.map { mapper.toTaskDomainModel(it) } ?: emptyList()
        } else {
            throw Exception("Failed to load tasks for date: ${response.errorBody()?.string()}")
        }
    }

    /*
        override suspend fun insertTask(task: TaskModel) {
            taskDao.insert(mapper.toEntity(task))
        }
        */

    override suspend fun insertTask(task: TaskModel) {
        taskApi.insertTask(mapper.toTaskEntity(task))
    }

    /*
        // Delete a task from the database
        override suspend fun deleteTask(task: TaskModel) {
            taskDao.delete(mapper.toEntity(task))
        }
        */

    override suspend fun deleteTask(id: String) {
        taskApi.deleteTask(id)
    }

    /*
        override suspend fun updateTask(task: TaskModel) {
            taskDao.update(mapper.toEntity(task))
        }*/

    override suspend fun updateTask(task: TaskModel) {
        taskApi.updateTask(task.uid, mapper.toTaskEntity(task))
    }

    override suspend fun getPointsForDay(date: Long): DailyPointsSummaryModel {
        val response = taskApi.getPointsForDay(date)
        if (response.isSuccessful) {
            return mapper.toDailyPointsSummaryModel(response.body())
        } else {
            throw Exception("Failed to load daily points: ${response.errorBody()?.string()}")
        }
    }

}
package com.example.madetoliveapp.presentation.tasks

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.madetoliveapp.data.source.remote.api.UserApi
import com.example.madetoliveapp.domain.model.ProjectModel
import com.example.madetoliveapp.domain.model.TaskModel
import com.example.madetoliveapp.domain.usecase.AddProjectUseCase
import com.example.madetoliveapp.domain.usecase.AddTaskUseCase
import com.example.madetoliveapp.domain.usecase.DeleteTaskUseCase
import com.example.madetoliveapp.domain.usecase.GetProjectsUseCase
import com.example.madetoliveapp.domain.usecase.GetTasksForDayUseCase
import com.example.madetoliveapp.domain.usecase.GetTasksUseCase
import com.example.madetoliveapp.domain.usecase.GetUserPointsUseCase
import com.example.madetoliveapp.domain.usecase.UpdateTaskUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TaskViewModel(
    private val getAllTasksUseCase: GetTasksUseCase,
    private val getTasksForDayUseCase: GetTasksForDayUseCase,
    private val addTaskUseCase: AddTaskUseCase,
    private val updateTaskUseCase: UpdateTaskUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase,
    private val addProjectUseCase: AddProjectUseCase,
    private val getAllProjectsUseCase: GetProjectsUseCase,
    private val getUserPointsUseCase: GetUserPointsUseCase
)  : ViewModel() {

    // Lista de tareas como StateFlow (mejor integración con Compose)
    private val _tasks = MutableStateFlow<List<TaskModel>>(emptyList())
    val tasks: StateFlow<List<TaskModel>> = _tasks.asStateFlow()

    // Lista de tareas como StateFlow (mejor integración con Compose)
    private val _projects = MutableStateFlow<List<ProjectModel>>(emptyList())
    val projects: StateFlow<List<ProjectModel>> = _projects.asStateFlow()

    private val _totalPoints = MutableStateFlow(0)
    val totalPoints: StateFlow<Int> = _totalPoints

    private val _taskFilter = MutableStateFlow(TaskFilter.ALL)
    val taskFilter = _taskFilter.asStateFlow()

    private val _sortMode = MutableStateFlow(SortMode.BY_POINTS)
    val sortMode = _sortMode.asStateFlow()

    val visibleTasks = combine(_tasks, _taskFilter, _sortMode) { allTasks, filter, sort ->
        val filtered = when (filter) {
            TaskFilter.ALL -> allTasks
            TaskFilter.POSITIVE -> allTasks.filter { (it.points ?: 0) > 0 }
            TaskFilter.NEGATIVE -> allTasks.filter { (it.points ?: 0) < 0 }
        }

        val sorted = filtered.sortedWith(
            when (sort) {
                SortMode.BY_POINTS -> compareBy<TaskModel> { it.checked }
                    .thenByDescending { it.points ?: 0 }

                SortMode.BY_CREATION -> compareBy<TaskModel> { it.checked }
                    .thenBy { it.date }
            }
        )

        sorted
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val dailyPoints = combine(tasks, taskFilter) { allTasks, filter ->
        val filtered = when (filter) {
            TaskFilter.ALL -> allTasks
            TaskFilter.POSITIVE -> allTasks.filter { (it.points ?: 0) > 0 }
            TaskFilter.NEGATIVE -> allTasks.filter { (it.points ?: 0) < 0 }
        }

        filtered.filter { it.checked }.sumOf { it.points ?: 0 }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    fun setFilter(filter: TaskFilter) {
        _taskFilter.value = filter
    }

    fun toggleSortMode() {
        _sortMode.value = if (_sortMode.value == SortMode.BY_POINTS) SortMode.BY_CREATION else SortMode.BY_POINTS
    }


    // Método para marcar una tarea como completada
    fun toggleTaskCompletion(taskId: String) {
        viewModelScope.launch {
            val taskToUpdate = _tasks.value.find { it.uid == taskId }
            taskToUpdate?.let { task ->
                val updatedTask = task.copy(checked = !task.checked)
                updateTaskUseCase.execute(updatedTask) // Update task in the database
                // Update the state with only the modified task
                _tasks.value = _tasks.value.map { if (it.uid == taskId) updatedTask else it }
                loadUserPoints()
            }
        }
    }
//testing new repo
fun getAllTasks() {
    viewModelScope.launch {
        try {
            val result = getAllTasksUseCase.execute()
            _tasks.value = result
        } catch (e: Exception) {
            // Handle the error gracefully, log it, or update UI state
            Log.e("TaskViewModel", "Error loading tasks", e)
            // Optionally show error state to UI
        }
    }
}

    fun getTasksForDay(date: Long) {
        viewModelScope.launch {
            val result = getTasksForDayUseCase.execute(date)
            _tasks.value = result
        }
    }

    // Add tasks using the use case
    fun addTask(task: TaskModel) {
        viewModelScope.launch {
            addTaskUseCase.execute(task)
        }
        _tasks.value += task
    }

    // Add tasks using the use case
    fun addProject(project: ProjectModel) {
        viewModelScope.launch {
            addProjectUseCase.execute(project)
        }
        _projects.value += project
    }

    fun getAllProjects() {
        viewModelScope.launch {
            try {
                val result = getAllProjectsUseCase.execute()
                _projects.value = result
            } catch (e: Exception) {
                // Handle the error gracefully, log it, or update UI state
                Log.e("TaskViewModel", "Error loading projects", e)
                // Optionally show error state to UI
            }
        }
    }

    // Delete tasks using the use case
    fun deleteTask(task: TaskModel) {
        viewModelScope.launch {
            deleteTaskUseCase.execute(task)
        }
    }

    fun loadUserPoints() {
        viewModelScope.launch {
            try {
                val points = getUserPointsUseCase.execute()
                _totalPoints.value = points.toInt()
            } catch (e: Exception) {
                // Handle error (optional: show toast or log)
            }
        }
    }
}

enum class TaskFilter { ALL, POSITIVE, NEGATIVE }
enum class SortMode { BY_POINTS, BY_CREATION }
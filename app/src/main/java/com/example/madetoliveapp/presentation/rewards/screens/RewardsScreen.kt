package com.example.madetoliveapp.presentation.rewards.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.madetoliveapp.domain.model.TaskModel
import com.example.madetoliveapp.presentation.TaskViewModel
import com.example.madetoliveapp.presentation.components.BottomNavigationBar
import com.example.madetoliveapp.presentation.components.CalendarHeader
import com.example.madetoliveapp.presentation.components.TaskComponent
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date

@Composable
fun RewardsScreen(taskViewModel: TaskViewModel = koinViewModel()) {
    // Obtener las tareas desde el ViewModel
    val tasks by taskViewModel.tasks.collectAsState()
    val dateFormat = SimpleDateFormat("d-MM-yyyy")

    LaunchedEffect(Unit) {
        taskViewModel.getAllTasks()
    }

    Scaffold(
        bottomBar = { BottomNavigationBar(selectedRoute = "rewards") },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val parsedDate: Date = dateFormat.parse("2-10-2013") as Date
                    val newTask = TaskModel(
                        title = "Nueva tarea",
                        checked = false,
                        subTasks = listOf(),
                        category = null,
                        finishingDate = parsedDate,
                        points = null
                    )
                    taskViewModel.addTask(newTask)
                },
                modifier = Modifier.padding(16.dp) // Adjust padding to prevent overlap
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar tarea")
            }
        }
    ) { paddingValues ->
        val outerScrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(outerScrollState)
        ) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(text = "Rewards Screen", style = MaterialTheme.typography.h4)
            }
            CalendarHeader(modifier = Modifier.fillMaxWidth())

            TaskComponent(
                tasks = tasks,
                onTaskClick = taskViewModel::toggleTaskCompletion,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
            )
        }
    }
}






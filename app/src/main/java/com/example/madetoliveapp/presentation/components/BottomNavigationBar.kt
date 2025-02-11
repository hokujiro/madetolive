package com.example.madetoliveapp.presentation.components

import android.app.Activity
import android.content.Intent
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.example.madetoliveapp.presentation.daily.MainActivity
import com.example.madetoliveapp.presentation.home.HomeActivity
import com.example.madetoliveapp.presentation.rewards.RewardsActivity

@Composable
fun BottomNavigationBar(selectedRoute: String) {
    val context = LocalContext.current

    BottomNavigation {
        BottomNavigationItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") },
            selected = selectedRoute == "home",
            onClick = {
                if (selectedRoute != "home") {
                    context.startActivity(Intent(context, HomeActivity::class.java))
                    (context as Activity).overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                }
            }
        )
        BottomNavigationItem(
            icon = { Icon(Icons.Default.Person, contentDescription = "Tasks") },
            label = { Text("Tasks") },
            selected = selectedRoute == "tasks",
            onClick = {
                if (selectedRoute != "tasks") {
                    context.startActivity(Intent(context, MainActivity::class.java))
                    (context as Activity).overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                }
            }
        )
        BottomNavigationItem(
            icon = { Icon(Icons.Default.Settings, contentDescription = "Rewards") },
            label = { Text("Rewards") },
            selected = selectedRoute == "rewards",
            onClick = {
                if (selectedRoute != "rewards") {
                    context.startActivity(Intent(context, RewardsActivity::class.java))
                    (context as Activity).overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                }
            }
        )
    }
}
package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.WorkoutViewModel
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.NutritionScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.ProgressScreen
import com.example.ui.screens.WorkoutSessionScreen
import com.example.ui.screens.WorkoutSummaryScreen
import com.example.ui.screens.WorkoutsListScreen
import com.example.ui.theme.LimePrimary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NavyBackground
import com.example.ui.theme.NavySurface
import com.example.ui.theme.NavySurfaceVariant
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

enum class AppScreen {
    HOME,
    WORKOUTS,
    PROGRESS,
    NUTRITION,
    PROFILE,
    ACTIVE_SESSION,
    SESSION_SUMMARY
}

class MainActivity : ComponentActivity() {
    private val workoutViewModel: WorkoutViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                WorkoutJourneyApp(viewModel = workoutViewModel)
            }
        }
    }
}

@Composable
fun WorkoutJourneyApp(viewModel: WorkoutViewModel) {
    var currentScreen by remember { mutableStateOf(AppScreen.HOME) }
    var activeSummarySessionId by remember { mutableLongStateOf(0L) }

    // Physical Back button management
    BackHandler(enabled = currentScreen != AppScreen.HOME) {
        if (currentScreen == AppScreen.ACTIVE_SESSION) {
            // Handled within WorkoutSessionScreen exit confirmation dialog
        } else {
            currentScreen = AppScreen.HOME
        }
    }

    val isFullscreenWorkout = currentScreen == AppScreen.ACTIVE_SESSION || currentScreen == AppScreen.SESSION_SUMMARY

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (!isFullscreenWorkout) {
                WorkoutBottomNavBar(
                    currentScreen = currentScreen,
                    onNavigate = { screen -> currentScreen = screen }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(NavyBackground)
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                AppScreen.HOME -> {
                    HomeScreen(
                        viewModel = viewModel,
                        onStartWorkout = { dayOfWeek ->
                            viewModel.startWorkout(dayOfWeek)
                            currentScreen = AppScreen.ACTIVE_SESSION
                        },
                        onNavigateToWorkouts = { currentScreen = AppScreen.WORKOUTS },
                        onNavigateToProgress = { currentScreen = AppScreen.PROGRESS },
                        onNavigateToProfile = { currentScreen = AppScreen.PROFILE }
                    )
                }

                AppScreen.WORKOUTS -> {
                    WorkoutsListScreen(
                        viewModel = viewModel,
                        onStartWorkout = { dayOfWeek ->
                            viewModel.startWorkout(dayOfWeek)
                            currentScreen = AppScreen.ACTIVE_SESSION
                        }
                    )
                }

                AppScreen.PROGRESS -> {
                    ProgressScreen(viewModel = viewModel)
                }

                AppScreen.NUTRITION -> {
                    NutritionScreen(viewModel = viewModel)
                }

                AppScreen.PROFILE -> {
                    ProfileScreen(viewModel = viewModel)
                }

                AppScreen.ACTIVE_SESSION -> {
                    WorkoutSessionScreen(
                        viewModel = viewModel,
                        onWorkoutFinished = { sessionId ->
                            activeSummarySessionId = sessionId
                            currentScreen = AppScreen.SESSION_SUMMARY
                        },
                        onExitWorkout = {
                            currentScreen = AppScreen.HOME
                        }
                    )
                }

                AppScreen.SESSION_SUMMARY -> {
                    WorkoutSummaryScreen(
                        sessionId = activeSummarySessionId,
                        viewModel = viewModel,
                        onReturnToDashboard = {
                            viewModel.discardOrExitWorkout()
                            currentScreen = AppScreen.HOME
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun WorkoutBottomNavBar(
    currentScreen: AppScreen,
    onNavigate: (AppScreen) -> Unit
) {
    NavigationBar(
        containerColor = NavySurface,
        tonalElevation = 8.dp,
        modifier = Modifier.testTag("bottom_nav_bar")
    ) {
        val navItems = listOf(
            NavItem(AppScreen.HOME, "Home", Icons.Default.Home, "nav_home"),
            NavItem(AppScreen.WORKOUTS, "Workouts", Icons.Default.FitnessCenter, "nav_workouts"),
            NavItem(AppScreen.PROGRESS, "Progress", Icons.AutoMirrored.Filled.ShowChart, "nav_progress"),
            NavItem(AppScreen.NUTRITION, "Nutrition", Icons.Default.Restaurant, "nav_nutrition"),
            NavItem(AppScreen.PROFILE, "Profile", Icons.Default.Person, "nav_profile")
        )

        navItems.forEach { item ->
            val isSelected = currentScreen == item.screen
            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(item.screen) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = if (isSelected) LimePrimary else TextSecondary
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        color = if (isSelected) LimePrimary else TextSecondary,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = NavySurfaceVariant
                ),
                modifier = Modifier.testTag(item.testTag)
            )
        }
    }
}

private data class NavItem(
    val screen: AppScreen,
    val label: String,
    val icon: ImageVector,
    val testTag: String
)

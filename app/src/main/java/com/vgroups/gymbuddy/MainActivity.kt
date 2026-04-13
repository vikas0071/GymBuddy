package com.vgroups.gymbuddy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.vgroups.gymbuddy.presentation.celebration.CelebrationScreen
import com.vgroups.gymbuddy.presentation.exercises.ExerciseListScreen
import com.vgroups.gymbuddy.presentation.home.HomeScreen
import com.vgroups.gymbuddy.presentation.timer.WorkoutTimerScreen
import com.vgroups.gymbuddy.ui.theme.GymBuddyTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        val startTime = System.currentTimeMillis()
        splashScreen.setKeepOnScreenCondition {
            System.currentTimeMillis() - startTime < 1200L
        }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GymBuddyTheme {
                GymBuddyNavGraph()
            }
        }
    }
}

@Composable
private fun GymBuddyNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {

        // ── Screen 1: Split Selector ─────────────────────────────────────
        composable("home") {
            HomeScreen(
                onSplitClick = { splitId, dayIndex ->
                    navController.navigate("exercises/$splitId/$dayIndex")
                }
            )
        }

        // ── Screen 2: Exercise List ──────────────────────────────────────
        composable(
            route = "exercises/{splitId}/{dayIndex}",
            arguments = listOf(
                navArgument("splitId") { type = NavType.StringType },
                navArgument("dayIndex") { type = NavType.IntType }
            )
        ) {
            ExerciseListScreen(
                onBack = { navController.popBackStack() },
                onStartWorkout = { sid, dIdx ->
                    navController.navigate("timer/$sid/$dIdx")
                }
            )
        }

        // ── Screen 3: Workout Timer ──────────────────────────────────────
        composable(
            route = "timer/{splitId}/{dayIndex}",
            arguments = listOf(
                navArgument("splitId") { type = NavType.StringType },
                navArgument("dayIndex") { type = NavType.IntType }
            )
        ) {
            WorkoutTimerScreen(
                onWorkoutComplete = { splitId, dayIndex, durationSeconds, exerciseCount, splitName, dayLabel ->
                    // URL-encode dayLabel to handle spaces and special chars in nav route
                    val encodedDay = java.net.URLEncoder.encode(dayLabel, "UTF-8")
                    val encodedSplit = java.net.URLEncoder.encode(splitName, "UTF-8")
                    navController.navigate(
                        "celebration/$encodedSplit/$encodedDay/$durationSeconds/$exerciseCount"
                    ) {
                        // Remove timer + exercise list from back stack
                        popUpTo("home") { inclusive = false }
                    }
                }
            )
        }

        // ── Screen 4: Celebration ────────────────────────────────────────
        composable(
            route = "celebration/{splitName}/{dayLabel}/{durationSeconds}/{exerciseCount}",
            arguments = listOf(
                navArgument("splitName") { type = NavType.StringType },
                navArgument("dayLabel") { type = NavType.StringType },
                navArgument("durationSeconds") { type = NavType.LongType },
                navArgument("exerciseCount") { type = NavType.IntType }
            )
        ) { backStack ->
            val splitName = java.net.URLDecoder.decode(
                backStack.arguments?.getString("splitName") ?: "", "UTF-8"
            )
            val dayLabel = java.net.URLDecoder.decode(
                backStack.arguments?.getString("dayLabel") ?: "", "UTF-8"
            )
            val duration = backStack.arguments?.getLong("durationSeconds") ?: 0L
            val exerciseCount = backStack.arguments?.getInt("exerciseCount") ?: 0

            CelebrationScreen(
                durationSeconds = duration,
                exerciseCount = exerciseCount,
                splitName = splitName,
                dayLabel = dayLabel,
                onBackHome = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }
    }
}
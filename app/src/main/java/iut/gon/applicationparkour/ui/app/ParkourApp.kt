package iut.gon.applicationparkour.ui.app

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import iut.gon.applicationparkour.ArbitrageScreen
import iut.gon.applicationparkour.CompetitionArbitrageScreen
import iut.gon.applicationparkour.CompetitionCompetitorsScreen
import iut.gon.applicationparkour.CompetitionCoursesScreen
import iut.gon.applicationparkour.CompetitionResultsScreen
import iut.gon.applicationparkour.ui.screens.CompetitionScreen
import iut.gon.applicationparkour.ui.screens.CompetitorScreen
import iut.gon.applicationparkour.CourseObstaclesScreen
import iut.gon.applicationparkour.ui.screens.ObstaclesScreen
import iut.gon.applicationparkour.ResultScreen
import iut.gon.applicationparkour.ui.screens.WelcomeScreen

@Composable
fun ParkourApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "welcome"
    ) {
        composable("welcome") {
            WelcomeScreen(navController)
        }
        composable("competitors") {
            CompetitorScreen(navController)
        }
        composable("competitions") {
            CompetitionScreen(navController)
        }
        composable("obstacles") {
            ObstaclesScreen(navController)
        }
        composable("competitors/{competitionId}") { backStackEntry ->
            val competitionId = backStackEntry.arguments?.getString("competitionId") ?: "0"
            CompetitionCompetitorsScreen(navController, competitionId.toInt())
        }

        composable("results/{competitionId}") { backStackEntry ->
            val competitionId = backStackEntry.arguments?.getString("competitionId") ?: "0"
            CompetitionResultsScreen(navController, competitionId)
        }

        composable("arbitrage/{competitionId}") { backStackEntry ->
            val competitionId = backStackEntry.arguments?.getString("competitionId") ?: "0"
            CompetitionArbitrageScreen(navController, competitionId)
        }

        composable("competitionArbitrage/{competitionId}") { backStackEntry ->
            val competitionId = backStackEntry.arguments?.getString("competitionId") ?: ""
            CompetitionArbitrageScreen(navController, competitionId)
        }
        composable("resultScreen/{competitionId}/{courseId}") { backStackEntry ->
            val competitionId = backStackEntry.arguments?.getString("competitionId") ?: ""
            val courseId = backStackEntry.arguments?.getString("courseId") ?: ""
            ResultScreen(navController, competitionId, courseId)
        }
        composable("arbitrageScreen/{competitionId}/{courseId}") { backStackEntry ->
            val competitionId = backStackEntry.arguments?.getString("competitionId") ?: ""
            val courseId = backStackEntry.arguments?.getString("courseId") ?: ""
            ArbitrageScreen(navController, competitionId, courseId)
        }

        composable("competitionCourses/{competitionId}") { backStackEntry ->
            val competitionId = backStackEntry.arguments?.getString("competitionId") ?: "0"
            CompetitionCoursesScreen(navController, competitionId, onFinalSave = {
                navController.popBackStack()
            })
        }

        composable("courseObstacles/{courseId}") { backStackEntry ->
            val courseId = backStackEntry.arguments?.getString("courseId")?.toIntOrNull() ?: 0
            CourseObstaclesScreen(navController, courseId)
        }
    }
}
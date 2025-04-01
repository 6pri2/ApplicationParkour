package iut.gon.applicationparkour.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import iut.gon.applicationparkour.ui.components.scaffold.ScreenScaffold

/**
 * page d'affichage des résultats d'une compétition
 */

@Composable
fun CompetitionResultsScreen(navController: NavController, competitionId: String) {
    ScreenScaffold(
        title = "Résultats de la compétition",
        navController = navController
    ) {
        Box(
            modifier = Modifier.Companion.fillMaxSize(),
            contentAlignment = Alignment.Companion.Center
        ) {
            Text("Résultats de la compétition: $competitionId")
        }
    }
}
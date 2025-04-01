package iut.gon.applicationparkour.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import iut.gon.applicationparkour.ui.components.scaffold.ScreenScaffold

/**
 * Page d'arbitrage d'un parcours
 */

@Composable
fun ArbitrageScreen(navController: NavController, competitionId: String, courseId: String) {
    ScreenScaffold(
        title = "Arbitrage du parcours",
        navController = navController
    ) {
        Box(
            modifier = Modifier.Companion.fillMaxSize(),
            contentAlignment = Alignment.Companion.Center
        ) {
            Column(horizontalAlignment = Alignment.Companion.CenterHorizontally) {
                Text(
                    "Arbitrage du parcours: ${courseId}",
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    "Compétition: ${competitionId}",
                    style = MaterialTheme.typography.headlineSmall
                )
                // Ajoutez ici les fonctionnalités d'arbitrage
            }
        }
    }
}
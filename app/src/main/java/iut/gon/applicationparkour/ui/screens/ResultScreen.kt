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
 * page d'affichage des résultat d'un parcours
 */

@Composable
fun ResultScreen(navController: NavController, competitionId: String, courseId: String) {
    ScreenScaffold(
        title = "Résultat du parcours",
        navController = navController
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Résultat du parcours: ${courseId}", style = MaterialTheme.typography.headlineMedium)
                Text("Compétition: ${competitionId}", style = MaterialTheme.typography.headlineSmall)
                // Ajoutez ici les détails des résultats
            }
        }
    }
}
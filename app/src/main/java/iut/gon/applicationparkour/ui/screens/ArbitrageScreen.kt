package iut.gon.applicationparkour.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import iut.gon.applicationparkour.ui.components.competitor.CompetiteurArbitrageItem
import iut.gon.applicationparkour.data.api.ApiClient
import iut.gon.applicationparkour.data.model.Competitor
import iut.gon.applicationparkour.ui.components.scaffold.ScreenScaffold
import kotlinx.coroutines.launch

/**
 * Page d'arbitrage d'un parcours
 */


@Composable
fun ArbitrageScreen(navController: NavController, competitionId: String, courseId: String) {
    val token = "Bearer 1ofD5tbAoC0Xd0TCMcQG3U214MqUo7JzUWrQFWt1ugPuiiDmwQCImm9Giw7fwR0Y"
    var inscriptions by remember { mutableStateOf<List<Competitor>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    // Chargement des inscriptions de la compétition
    LaunchedEffect(competitionId) {
        scope.launch {
            try {
                inscriptions = ApiClient.apiService.getCompetitionInscriptions(
                    competitionId.toInt()
                )
                isLoading = false
            } catch (e: Exception) {
                error = "Erreur de chargement: ${e.message}"
                isLoading = false
            }
        }
    }

    ScreenScaffold(
        title = "Arbitrage du parcours",
        navController = navController
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                error != null -> {
                    Text(
                        text = error!!,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                inscriptions.isEmpty() -> {
                    Text("Aucun compétiteur inscrit", modifier = Modifier.align(Alignment.Center))
                }

                else -> {
                    LazyColumn(modifier = Modifier.padding(16.dp)) {
                        items(inscriptions) { inscription ->
                            CompetiteurArbitrageItem(
                                competitor = inscription,
                                navController = navController,
                                competitionId = competitionId,
                                courseId = courseId
                            )
                        }
                    }
                }
            }
        }
    }
}
package iut.gon.applicationparkour.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import iut.gon.applicationparkour.data.api.ApiClient
import iut.gon.applicationparkour.data.model.Competition
import iut.gon.applicationparkour.data.model.Competitor
import iut.gon.applicationparkour.data.model.Courses
import iut.gon.applicationparkour.data.model.PerformanceObstacle
import iut.gon.applicationparkour.ui.components.classement.ObstacleCard
import iut.gon.applicationparkour.ui.components.scaffold.ScreenScaffold
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

@Composable
fun CompetitorDetailsScreen(
    navController: NavController,
    performanceId: String,
    competitionId: String,
    courseId: String,
    competitorId: String,
    rank: String
) {
    val token = "Bearer 1ofD5tbAoC0Xd0TCMcQG3U214MqUo7JzUWrQFWt1ugPuiiDmwQCImm9Giw7fwR0Y"
    val scope = rememberCoroutineScope()

    var competition by remember { mutableStateOf<Competition?>(null) }
    var course by remember { mutableStateOf<Courses?>(null) }
    var competitor by remember { mutableStateOf<Competitor?>(null) }
    var obstacles by remember { mutableStateOf<List<PerformanceObstacle>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val deferredCompetition = async {
                    ApiClient.apiService.getCompetitionDetails(competitionId.toInt())
                }
                val deferredCourse = async {
                    ApiClient.apiService.getCourseById(courseId.toInt())
                }
                val deferredCompetitor = async {
                    ApiClient.apiService.getAllCompetitors()
                        .firstOrNull { it.id == competitorId.toInt() }
                }
                val deferredObstacles = async {
                    ApiClient.apiService.getPerformanceObstacles(performanceId.toInt())
                }

                competition = deferredCompetition.await()
                course = deferredCourse.await()
                competitor = deferredCompetitor.await()
                obstacles = deferredObstacles.await()
                loading = false
            } catch (e: Exception) {
                error = "Erreur de chargement: ${e.message}"
                loading = false
            }
        }
    }

    ScreenScaffold(
        title = "Détails participant",
        navController = navController
    ) {
        if (loading) {
            Box(
                modifier = Modifier.Companion.fillMaxSize(),
                contentAlignment = Alignment.Companion.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (error != null) {
            Box(
                modifier = Modifier.Companion.fillMaxSize(),
                contentAlignment = Alignment.Companion.Center
            ) {
                Text(text = error!!, color = MaterialTheme.colorScheme.error)
            }
        } else {
            Column(modifier = Modifier.Companion.fillMaxSize().fillMaxWidth().padding(16.dp)) {
                Card(
                    modifier = Modifier.Companion.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0x808080)),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.Companion.padding(16.dp)) {
                        Text(
                            text = "Compétition: ${competition?.name ?: "Non disponible"}",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "Parcours: ${course?.name ?: "Non disponible"}",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(text = "Participant: ${competitor?.let { "${it.first_name} ${it.last_name}" } ?: "Non disponible"}",
                            style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = "Classement: $rank",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "ID Performance: $performanceId",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.Companion.height(16.dp))
                Text(text = "Obstacles", style = MaterialTheme.typography.titleLarge)
                Divider()

                if (obstacles.isEmpty()) {
                    Box(
                        modifier = Modifier.Companion.fillMaxSize(),
                        contentAlignment = Alignment.Companion.Center
                    ) {
                        Text(text = "Aucun obstacle trouvé")
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(obstacles) { obstacle ->
                            Card(
                                modifier = Modifier.Companion.fillMaxWidth(),
                                elevation = CardDefaults.cardElevation(2.dp)
                            ) {
                                ObstacleCard(obstacle = obstacle)
                            }
                        }
                    }
                }
            }
        }
    }
}



package iut.gon.applicationparkour.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import iut.gon.applicationparkour.ui.components.scaffold.ScreenScaffold
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.gson.annotations.SerializedName
import iut.gon.applicationparkour.data.api.ApiClient
import iut.gon.applicationparkour.data.model.Competition
import iut.gon.applicationparkour.data.model.Competitor
import iut.gon.applicationparkour.data.model.Courses
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

/**
 * page d'affichage des résultat d'un parcours
 */

data class Performance(
    val id: Int,
    @SerializedName("competitor_id")
    val competitorId: Int,
    @SerializedName("course_id")
    val courseId: Int,
    val status: String,
    @SerializedName("total_time")
    val totalTime: Int,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String
)

data class PerformanceObstacle(
    val id: Int,
    val obstacle_id: Int,
    val performance_id: Int,
    val time: Int,
    val has_fell: Int,
    val to_verify: Int
)

@Composable
fun ResultScreen(navController: NavController, competitionId: String, courseId: String) {
    val token = "Bearer 1ofD5tbAoC0Xd0TCMcQG3U214MqUo7JzUWrQFWt1ugPuiiDmwQCImm9Giw7fwR0Y" // Remplacez par votre vrai token
    val scope = rememberCoroutineScope()

    var competition by remember { mutableStateOf<Competition?>(null) }
    var course by remember { mutableStateOf<Courses?>(null) }
    var competitors by remember { mutableStateOf<Map<Int, Competitor>?>(null) }
    var performances by remember { mutableStateOf<List<Performance>?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val deferredCompetition = async {
                    ApiClient.apiService.getCompetitionDetails(token, competitionId.toInt())
                }

                val deferredCourse = async {
                    ApiClient.apiService.getCourseDetails(token, courseId.toInt())
                }

                val deferredCompetitors = async {
                    ApiClient.apiService.getAllCompetitors(token)
                        .associateBy { it.id }
                }

                val deferredPerformances = async {
                    ApiClient.apiService.getPerformances(token)
                        .filter { it.courseId == courseId.toInt() }
                }

                competition = deferredCompetition.await()
                course = deferredCourse.await()
                competitors = deferredCompetitors.await()
                performances = deferredPerformances.await()
                loading = false

            } catch (e: Exception) {
                error = "Erreur de chargement: ${e.message}"
                loading = false
            }
        }
    }

    ScreenScaffold(
        title = "Résultats du parcours",
        navController = navController
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            when {
                loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))

                error != null -> Text(
                    text = error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )

                else -> SuccessContent(
                    competition = competition!!,
                    course = course!!,
                    competitors = competitors!!,
                    performances = performances!!,
                    onCompetitorClick = { perf, rank ->
                        navController.navigate("competitorDetails/${competitionId}/${courseId}/${perf.competitorId}/$rank/${perf.id}")
                    }
                )
            }
        }
    }
}

@Composable
private fun SuccessContent(
    competition: Competition,
    course: Courses,
    competitors: Map<Int, Competitor>,
    performances: List<Performance>,
    onCompetitorClick: (Performance, Int) -> Unit
) {
    val sortedPerformances = remember(performances) {
        performances.sortedWith(
            compareBy(
                { when (it.status.lowercase()) {
                    "to_verify" -> 0
                    "over" -> 0
                    else -> 1
                } },
                { performance ->
                    when (performance.status.lowercase()) {
                        in listOf("to_verify", "over") -> performance.totalTime
                        else -> -performance.totalTime
                    }
                }
            )
        )
    }

    Column(Modifier.padding(16.dp)) {
        // En-tête amélioré
        Text(
            "Classement - ${competition.name}",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            textAlign = TextAlign.Center
        )

        Text(
            "Parcours: ${course.name}",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            textAlign = TextAlign.Center
        )

        if (sortedPerformances.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Aucun résultat disponible")
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                itemsIndexed(sortedPerformances) { index, performance ->
                    val rank = index + 1
                    PodiumCard(
                        rank = rank,
                        performance = performance,
                        competitor = competitors[performance.competitorId],
                        onClick = {onCompetitorClick(performance, rank)}
                    )
                }
            }
        }
    }
}

@Composable
private fun PodiumCard(
    rank: Int,
    performance: Performance,
    competitor: Competitor?,
    onClick: () -> Unit
) {
    val (cardColor, rankColor) = when (rank) {
        1 -> Pair(Color(0xFFFFD700), Color(0xFFC98910)) // Or
        2 -> Pair(Color(0xFFC0C0C0), Color(0xFFA8A8A8)) // Argent
        3 -> Pair(Color(0xFFCD7F32), Color(0xFFA46628)) // Bronze
        else -> Pair(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.onSurface)
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Badge de classement
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = rankColor,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$rank",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Infos du participant
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = competitor?.let { "${it.first_name} ${it.last_name}" } ?: "Participant inconnu",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Row {
                    Text(
                        text = when (performance.status.lowercase()) {
                            "defection" -> "Chute"
                            else -> "Terminé"
                        },
                        modifier = Modifier.padding(end = 8.dp)
                    )

                    Text("${performance.totalTime / 1000}s")
                }
            }

            // Date
            Text(
                text = performance.createdAt.substring(0..9),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

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
                    ApiClient.apiService.getCompetitionDetails(token, competitionId.toInt())
                }
                val deferredCourse = async {
                    ApiClient.apiService.getCourseDetails(token, courseId.toInt())
                }
                val deferredCompetitor = async {
                    ApiClient.apiService.getAllCompetitors(token)
                        .firstOrNull { it.id == competitorId.toInt() }
                }
                val deferredObstacles = async {
                    ApiClient.apiService.getPerformanceObstacles(token,performanceId.toInt())
                }



                competition = deferredCompetition.await()
                course = deferredCourse.await()
                competitor = deferredCompetitor.await()
                obstacles = deferredObstacles.await()
                loading = false
                println(obstacles.size)
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
            CircularProgressIndicator()
        } else if (error != null) {
            Text(
                text = error!!,
                color = MaterialTheme.colorScheme.error)
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                // Section informations principales
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Compétition: ${competition?.name ?: "Non disponible"}",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "Parcours: ${course?.name ?: "Non disponible"}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Participant: ${competitor?.let { "${it.first_name} ${it.last_name}" } ?: "Non disponible"}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Classement: $rank",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "ID Performance: $performanceId",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // Section obstacles
                Text(
                    text = "Obstacles",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                if (obstacles.isEmpty()) {
                    Text(
                        text = "Aucun obstacle trouvé",
                        modifier = Modifier.padding(16.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(obstacles) { obstacle ->
                            ObstacleCard(obstacle = obstacle)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ObstacleCard(obstacle: PerformanceObstacle) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Obstacle #${obstacle.obstacle_id}",
                style = MaterialTheme.typography.titleMedium
            )
            Text(text = "Temps: ${obstacle.time} secondes")
            Text(text = "Chute: ${if (obstacle.has_fell == 1) "Oui" else "Non"}")
            Text(text = "À vérifier: ${if (obstacle.to_verify == 1) "Oui" else "Non"}")
        }
    }
}
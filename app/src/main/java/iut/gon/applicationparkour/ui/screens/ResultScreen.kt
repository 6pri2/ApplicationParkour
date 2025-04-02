package iut.gon.applicationparkour.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import iut.gon.applicationparkour.ui.components.scaffold.ScreenScaffold
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.gson.annotations.SerializedName
import iut.gon.applicationparkour.data.api.ApiClient
import iut.gon.applicationparkour.data.model.Courses

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

@Composable
fun ResultScreen(navController: NavController, competitionId: String, courseId: String) {
    val token = "Bearer 1ofD5tbAoC0Xd0TCMcQG3U214MqUo7JzUWrQFWt1ugPuiiDmwQCImm9Giw7fwR0Y"
    var performances by remember { mutableStateOf<List<Performance>?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(true) {
        try {
            val result = ApiClient.apiService.getPerformances(token)
            performances = result.filter { it.courseId == courseId.toInt() }
            loading = false
        } catch (e: Exception) {
            error = "Erreur lors du chargement des résultats"
            loading = false
        }
    }

    ScreenScaffold(
        title = "Résultats du parcours",
        navController = navController
    ) {
        Box(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                loading -> CircularProgressIndicator()
                error != null -> Text(error!!, color = MaterialTheme.colorScheme.error)
                performances.isNullOrEmpty() -> Text("Aucun résultat disponible")
                else -> {
                    Column {
                        Text(
                            "Compétition: $competitionId",
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        LazyColumn(modifier = Modifier.weight(1f)) {
                            items(performances!!) { performance ->
                                PerformanceCard(performance)
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PerformanceCard(performance: Performance) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                "Participant #${performance.competitorId}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text("Statut : ${performance.status.replaceFirstChar { it.uppercase() }}")
            Text("Temps total : ${performance.totalTime /10} secondes")
            Text("Date : ${performance.createdAt.substring(0, 10)}")
        }
    }
}
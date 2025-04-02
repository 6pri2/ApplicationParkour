package iut.gon.applicationparkour.ui.components.classement

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import iut.gon.applicationparkour.data.api.ApiClient
import iut.gon.applicationparkour.data.model.Competition
import iut.gon.applicationparkour.data.model.Competitor
import iut.gon.applicationparkour.data.model.Courses
import iut.gon.applicationparkour.data.model.Performance
import iut.gon.applicationparkour.data.model.PerformanceObstacle


@Composable
fun ObstacleCard(obstacle: PerformanceObstacle) {
    var name by remember { mutableStateOf<String?>(null) }
    val token = "Bearer 1ofD5tbAoC0Xd0TCMcQG3U214MqUo7JzUWrQFWt1ugPuiiDmwQCImm9Giw7fwR0Y"
    LaunchedEffect(obstacle.obstacle_id) {
        try {
            val obstacleDetails =
                ApiClient.apiService.getObstacleDetails(token, obstacle.obstacle_id)
            name = obstacleDetails.name
        } catch (e: Exception) {
            name = "Obstacle inconnu"
        }
    }



    Card(
        modifier = Modifier.Companion.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.Companion.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = name ?: "Chargement...",
                style = MaterialTheme.typography.titleMedium
            )
            Text(text = "Temps: ${obstacle.time} secondes")
            Text(text = "Chute: ${if (obstacle.has_fell == 1) "Oui" else "Non"}")
            Text(text = "À vérifier: ${if (obstacle.to_verify == 1) "Oui" else "Non"}")
        }
    }
}
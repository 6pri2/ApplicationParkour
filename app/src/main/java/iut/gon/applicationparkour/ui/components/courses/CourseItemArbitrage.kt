package iut.gon.applicationparkour.ui.components.courses

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Sports
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import iut.gon.applicationparkour.data.api.ApiClient
import iut.gon.applicationparkour.data.model.Courses
import kotlinx.coroutines.launch

@Composable
public fun CourseItemArbitrage(
    course: Courses,
    competitionId: String,
    navController: NavController
) {
    val scope = rememberCoroutineScope()
    var hasEveryTime by remember { mutableStateOf(true) }
    var hasEveryTimePrevious by remember { mutableStateOf(true) }

    LaunchedEffect(hasEveryTime) {
        scope.launch {
            println(course.id)
            val perfs = ApiClient.apiService.getPerformanceByCourse( course.id)
            val personne =
                ApiClient.apiService.getCompetitionInscriptions( competitionId.toInt())
            hasEveryTime = (perfs.size == personne.size) && hasEveryTimePrevious
            if (hasEveryTime) {
                hasEveryTimePrevious = true
            } else {
                hasEveryTimePrevious = false
            }


        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Nom: ${course.name}",
                style = MaterialTheme.typography.bodyLarge,
            )

            Text(text = "Durée max: ${course.max_duration} sec")
            Text(text = "Position: ${course.position}")
            Text(text = "Statut: ${if (course.is_over == 1) "Terminé" else "En cours"}")
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                IconButton(
                    onClick = {
                        navController.navigate("resultScreen/${competitionId}/${course.id}")
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "Résultats"
                    )
                }
                IconButton(
                    onClick = {
                        navController.navigate("arbitrageScreen/${competitionId}/${course.id}")
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Sports,
                        contentDescription = "Arbitrage"
                    )
                }
            }
        }
    }
}
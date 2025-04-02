package iut.gon.applicationparkour.ui.components.classement

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import iut.gon.applicationparkour.data.model.Competition
import iut.gon.applicationparkour.data.model.Competitor
import iut.gon.applicationparkour.data.model.Courses
import iut.gon.applicationparkour.data.model.Performance


@Composable
fun SuccessContent(
    competition: Competition,
    course: Courses,
    competitors: Map<Int, Competitor>,
    performances: List<Performance>,
    onCompetitorClick: (Performance, Int) -> Unit
) {
    val sortedPerformances = remember(performances) {
        performances.sortedWith(
            compareBy(
                {
                    when (it.status.lowercase()) {
                        "to_verify" -> 0
                        "over" -> 0
                        else -> 1
                    }
                },
                { performance ->
                    when (performance.status.lowercase()) {
                        in listOf("to_verify", "over") -> performance.totalTime
                        else -> -performance.totalTime
                    }
                }
            )
        )
    }

    Column(Modifier.Companion.padding(16.dp)) {
        // En-tête amélioré
        Text(
            "Classement - ${competition.name}",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.Companion
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            textAlign = TextAlign.Companion.Center
        )

        Text(
            "Parcours: ${course.name}",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.Companion
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            textAlign = TextAlign.Companion.Center
        )

        if (sortedPerformances.isEmpty()) {
            Box(
                modifier = Modifier.Companion
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Companion.Center
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
                        onClick = { onCompetitorClick(performance, rank) }
                    )
                }
            }
        }
    }
}

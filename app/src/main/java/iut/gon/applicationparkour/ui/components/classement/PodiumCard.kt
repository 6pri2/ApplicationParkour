package iut.gon.applicationparkour.ui.components.classement

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import iut.gon.applicationparkour.data.model.Competitor
import iut.gon.applicationparkour.data.model.Performance

/**
 * affiche des cartes avec couleur de podium pour les premiers
 */

@Composable
fun PodiumCard(
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
        modifier = Modifier.Companion.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.Companion.padding(16.dp),
            verticalAlignment = Alignment.Companion.CenterVertically
        ) {
            // Badge de classement
            Box(
                modifier = Modifier.Companion
                    .size(40.dp)
                    .background(
                        color = rankColor,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Companion.Center
            ) {
                Text(
                    text = "$rank",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.Companion.White,
                    fontWeight = FontWeight.Companion.Bold
                )
            }

            Spacer(modifier = Modifier.Companion.width(16.dp))

            // Infos du participant
            Column(modifier = Modifier.Companion.weight(1f)) {
                Text(
                    text = competitor?.let { "${it.first_name} ${it.last_name}" }
                        ?: "Participant inconnu",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Companion.Bold
                )

                Row {
                    Text(
                        text = when (performance.status.lowercase()) {
                            "defection" -> "Chute"
                            else -> "Terminé"
                        },
                        modifier = Modifier.Companion.padding(end = 8.dp)
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
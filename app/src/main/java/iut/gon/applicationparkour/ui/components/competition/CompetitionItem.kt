package iut.gon.applicationparkour.ui.components.competition

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Sports
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import iut.gon.applicationparkour.data.model.Competition

/**
 * affichage d'une competition
 */

@Composable
fun CompetitionItem(
    competition: Competition,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onCompetitors: () -> Unit,
    onResults: () -> Unit,
    onArbitrage: () -> Unit,
    onCourses: () -> Unit
) {
    Card(
        modifier = Modifier.Companion
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.Companion.padding(16.dp)) {
            Text(
                text = "Nom: ${competition.name}",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(text = "Âge: ${competition.age_min} - ${competition.age_max} ans")
            Text(text = "Genre: ${if (competition.gender == "H") "Homme" else "Femme"}")
            Text(text = "Retry: ${if (competition.has_retry == 1) "Oui" else "Non"}")
            Text(text = "Statut: ${competition.status}")

            Row(
                modifier = Modifier.Companion.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, "Modifier")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, "Supprimer")
                }
                IconButton(onClick = onCompetitors) {
                    Icon(Icons.Default.Person, "Compétiteurs")
                }
                IconButton(onClick = onResults) {
                    Icon(Icons.Default.EmojiEvents, contentDescription = "Résultats")
                }
                IconButton(onClick = onArbitrage) {
                    Icon(Icons.Default.Sports, "Arbitrage")
                }
            }
        }
    }
}
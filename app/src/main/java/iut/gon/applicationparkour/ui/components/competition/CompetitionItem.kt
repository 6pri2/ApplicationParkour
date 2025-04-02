package iut.gon.applicationparkour.ui.components.competition

import androidx.compose.foundation.layout.*

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Sports
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import iut.gon.applicationparkour.data.model.Competition

@Composable
fun CompetitionItem(
    competition: Competition,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onCompetitors: () -> Unit,
    onResults: () -> Unit,
    onArbitrage: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .shadow(4.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Titre de la compétition
            Text(
                text = competition.name,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Informations de la compétition
            Surface(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.2f)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(text = "Âge: ${competition.age_min} - ${competition.age_max} ans", style = MaterialTheme.typography.bodyMedium)
                    Text(text = "Genre: ${if (competition.gender == "H") "Homme" else "Femme"}", style = MaterialTheme.typography.bodyMedium)
                    Text(text = "Retry: ${if (competition.has_retry == 1) "Oui" else "Non"}", style = MaterialTheme.typography.bodyMedium)
                    Text(text = "Statut: ${if (competition.status == "not_ready") "Pas prêt" else "Prêt"}", style = MaterialTheme.typography.bodyMedium)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Ligne d'actions (modifier, supprimer, compétiteurs, résultats, arbitrage)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Modifier",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Supprimer",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
                IconButton(onClick = onCompetitors) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Compétiteurs",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onResults) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "Résultats",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onArbitrage) {
                    Icon(
                        imageVector = Icons.Default.Sports,
                        contentDescription = "Arbitrage",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

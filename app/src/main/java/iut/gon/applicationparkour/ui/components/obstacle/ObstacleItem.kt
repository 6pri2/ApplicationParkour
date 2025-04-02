package iut.gon.applicationparkour.ui.components.obstacle

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import iut.gon.applicationparkour.data.model.ObstacleCourse

/**
 * élément pour afficher un obstacle
 */

@Composable
fun ObstacleItem(
    obstacle: ObstacleCourse,
    onMoveUp: (() -> Unit)? = null,
    onMoveDown: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    onAdd: (() -> Unit)? = null,
    isFirst: Boolean = false,
    isLast: Boolean = false,
    isOnlyObstacle: Boolean = false
) {
    Card(
        modifier = Modifier.Companion
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.Companion.padding(16.dp)) {
            Text(
                text = obstacle.obstacle_name ?: "Nom inconnu",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Companion.Bold
            )


            Row(
                modifier = Modifier.Companion.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Boutons de déplacement (seulement pour les obstacles du parcours)
                if (onMoveUp != null && onMoveDown != null) {
                    Row {
                        IconButton(
                            onClick = onMoveUp,
                            enabled = !isFirst
                        ) {
                            Icon(
                                Icons.Default.ArrowUpward,
                                contentDescription = "Monter",
                                tint = if (isFirst) Color.Companion.Gray else MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(
                            onClick = onMoveDown,
                            enabled = !isLast
                        ) {
                            Icon(
                                Icons.Default.ArrowDownward,
                                contentDescription = "Descendre",
                                tint = if (isLast) Color.Companion.Gray else MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // Bouton d'action (suppression ou ajout)
                Row {
                    if (onDelete != null) {
                        IconButton(
                            onClick = onDelete ?: {},
                            enabled = !isOnlyObstacle  // Désactiver si c'est le dernier obstacle
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                "Supprimer",
                                tint = if (isOnlyObstacle) Color.Companion.Gray else MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    if (onAdd != null) {
                        IconButton(onClick = onAdd) {
                            Icon(
                                Icons.Default.Add,
                                "Ajouter",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}
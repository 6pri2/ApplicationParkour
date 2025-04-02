package iut.gon.applicationparkour.ui.components.courses

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import iut.gon.applicationparkour.data.model.Courses

/**
 * fonction pour afficher la modification des courses
 */

@Composable
fun CourseItemModif(
    course: Courses,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    isFirst: Boolean,
    isLast: Boolean,
    isOnlyCourse: Boolean
) {
    Card(
        modifier = Modifier.Companion
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.Companion.padding(16.dp)) {
            Text(
                text = course.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Companion.Bold
            )
            Spacer(modifier = Modifier.Companion.height(8.dp))
            Text(text = "Durée max: ${course.max_duration} secondes")

            Text(text = "Statut: ${if (course.is_over == 1) "Terminé" else "En cours"}")

            Row(
                modifier = Modifier.Companion.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Boutons de déplacement
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
                            tint = if (isLast) Color.Companion.Gray else MaterialTheme.colorScheme.primary,
                        )
                    }
                }

                // Boutons d'édition/suppression
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, "Modifier")
                    }
                    IconButton(
                        onClick = onDelete,
                        enabled = !isOnlyCourse
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            "Supprimer",
                            tint = if (isOnlyCourse) Color.Companion.Gray else MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}
package iut.gon.applicationparkour.ui.components.courses

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Sports
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import iut.gon.applicationparkour.data.model.Courses

@Composable
fun CourseItem(
    course: Courses,
    competitionId: String,
    navController: NavController
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .shadow(4.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        onClick = {
            navController.navigate("arbitrageScreen/${competitionId}/${course.id}")
        }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Nom de la course
            Text(
                text = course.name,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Informations de la course
            Surface(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.2f)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(text = "Durée max: ${course.max_duration} sec", style = MaterialTheme.typography.bodyMedium)

                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Ligne d'actions (résultats, arbitrage)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        navController.navigate("resultScreen/${competitionId}/${course.id}")
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "Résultats",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.width(8.dp)) // Espacement entre les icônes

                IconButton(
                    onClick = {
                        navController.navigate("arbitrageScreen/${competitionId}/${course.id}")
                    }
                ) {
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

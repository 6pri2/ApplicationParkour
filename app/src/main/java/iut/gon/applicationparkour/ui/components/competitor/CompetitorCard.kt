package iut.gon.applicationparkour.ui.components.competitor

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import iut.gon.applicationparkour.calculateAge
import iut.gon.applicationparkour.data.model.Competitor

/**
 * fonction pour afficher un competiteur
 */

@Composable
fun CompetitorCard(competitor: Competitor) {
    val fullName = "${competitor.first_name} ${competitor.last_name}"
    val age = calculateAge(competitor.born_at)

    Card(
        modifier = Modifier.Companion
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.Companion.padding(16.dp)) {
            Text(text = "Nom: $fullName", style = MaterialTheme.typography.bodyLarge)
            Text(text = "Âge: $age ans")
            Text(text = "Genre: ${if (competitor.gender == "H") "Homme" else "Femme"}")
        }
    }
}
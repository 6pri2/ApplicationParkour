package iut.gon.applicationparkour.ui.components.competitor

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sports
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import iut.gon.applicationparkour.data.api.ApiClient
import iut.gon.applicationparkour.data.model.Competitor

// Le composant CompetiteurArbitrageItem reste identique
@Composable
fun CompetiteurArbitrageItem(
    competitor: Competitor,
    navController: NavController,
    competitionId: String,
    courseId: String
) {
    var hasPerformance by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            println("\uD83D\uDD0D Vérification pour ${competitor.id}...")
            println("\uD83D\uDD0D Vérification pour ${competitionId}...")
            println("\uD83D\uDD0D Vérification pour ${competitor.id}...")

            // 1. Conversion sécurisée des IDs
            val compId = competitionId.toIntOrNull()
                ?: throw NumberFormatException("ID Compétition invalide")
            val crsId = courseId.toIntOrNull() ?: throw NumberFormatException("ID Course invalide")
            val competitorId = competitor.id

            // 2. Appel API avec vérification null
            val allPerformances = ApiClient.apiService.getPerformances()
            println("\uD83D\uDCE6 Réponse API (${allPerformances.size} éléments)")

            // 3. Filtrage sécurisé avec vérification null
            val matching = allPerformances.filter { response ->
                response?.let { perf ->
                    perf.courseId == crsId &&
                            perf.competitorId == competitorId
                } ?: false
            }

            // 4. Vérification des résultats
            println("\uD83D\uDD0E ${matching.size} performances valides trouvées")
            hasPerformance = matching.isNotEmpty()

        } catch (e: NumberFormatException) {
            errorMessage = "Erreur format ID: ${e.message}"
            println("\uD83D\uDD34 $errorMessage")
        } catch (e: Exception) {
            errorMessage = "Erreur technique: ${e.message?.take(200)}"
            println("\uD83D\uDD34 ${e.javaClass.simpleName}: ${e.message}")
        } finally {
            isLoading = false
            println("✅ Vérification terminée - Performance existante: $hasPerformance")
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${competitor.first_name} ${competitor.last_name}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text("Âge: ${calculateAge(competitor.born_at)} ans")
                Text("Genre: ${if (competitor.gender == "H") "Homme" else "Femme"}")
            }

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp))
            } else {
                IconButton(
                    onClick = {
                        navController.navigate("chronometre/$competitionId/$courseId/${competitor.id}")
                    },
                    enabled = !hasPerformance
                ) {
                    Icon(
                        imageVector = Icons.Default.Sports,
                        contentDescription = "Arbitrage",
                        tint = if (hasPerformance)
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        else
                            MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
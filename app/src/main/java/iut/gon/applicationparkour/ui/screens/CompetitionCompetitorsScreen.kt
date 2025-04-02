package iut.gon.applicationparkour.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import iut.gon.applicationparkour.ui.components.competitor.calculateAge
import iut.gon.applicationparkour.data.api.ApiClient
import iut.gon.applicationparkour.data.model.AddCompetitorRequest
import iut.gon.applicationparkour.data.model.Competition
import iut.gon.applicationparkour.data.model.Competitor
import iut.gon.applicationparkour.ui.components.scaffold.ScreenScaffold
import kotlinx.coroutines.launch
import kotlin.collections.forEach
import kotlin.collections.minus
import kotlin.collections.plus

/**
 * affichage des compétiteurs d'une compétition
 */

@Composable
fun CompetitionCompetitorsScreen(navController: NavController, competitionId: Int) {
    val token = "Bearer 1ofD5tbAoC0Xd0TCMcQG3U214MqUo7JzUWrQFWt1ugPuiiDmwQCImm9Giw7fwR0Y"
    var competitors by remember { mutableStateOf<List<Competitor>?>(null) }
    var allCompetitors by remember { mutableStateOf<List<Competitor>?>(null) }
    var competition by remember { mutableStateOf<Competition?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var competitorToDelete by remember { mutableStateOf<Competitor?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedCompetitors by remember { mutableStateOf<Set<Int>>(emptySet()) }
    val coroutineScope = rememberCoroutineScope()


    LaunchedEffect(competitionId) {
        try {
            val compId = competitionId
            competition = ApiClient.apiService.getCompetitionDetails(token, compId)
            competitors = ApiClient.apiService.getCompetitorsByCompetition(token, compId)
            allCompetitors = ApiClient.apiService.getAllCompetitors(token)
            isLoading = false
        } catch (e: Exception) {
            error = "Erreur lors du chargement: ${e.localizedMessage}"
            isLoading = false
        }
    }
    val eligibleCompetitors = allCompetitors?.filter { competitor ->
        val age = calculateAge(competitor.born_at)
        val genderMatch = competition?.gender == "Tous" || competitor.gender == competition?.gender
        val ageMatch = age in (competition?.age_min ?: 0)..(competition?.age_max ?: 100)
        val notRegistered = competitors?.none { it.id == competitor.id } ?: true

        genderMatch && ageMatch && notRegistered
    }
    if (competitorToDelete != null) {
        val competitor = competitorToDelete!!
        AlertDialog(
            onDismissRequest = { competitorToDelete = null },
            title = { Text("Confirmer la retrait du compétiteur dans la compétition ?") },
            text = { Text("Voulez-vous vraiment supprimer ${competitor.first_name} ${competitor.last_name} ?") },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            try {
                                ApiClient.apiService.removeCompetitorFromCompetition(
                                    token,
                                    competitionId,
                                    competitor.id
                                )
                                competitors = competitors?.filter { it.id != competitor.id }
                            } catch (e: Exception) {
                                error = "Erreur lors de la suppression: ${e.localizedMessage}"
                            } finally {
                                competitorToDelete = null
                            }
                        }
                    }
                ) {
                    Text("Supprimer")
                }
            },
            dismissButton = {
                Button(onClick = { competitorToDelete = null }) {
                    Text("Annuler")
                }
            }
        )
    }
    if (showAddDialog && eligibleCompetitors != null) {
        AlertDialog(
            onDismissRequest = {
                selectedCompetitors = emptySet()
                showAddDialog = false
            },
            title = { Text("Ajouter des compétiteurs") },
            text = {
                Column(modifier = Modifier.Companion.verticalScroll(rememberScrollState())) {
                    eligibleCompetitors.forEach { competitor ->
                        Row(
                            verticalAlignment = Alignment.Companion.CenterVertically,
                            modifier = Modifier.Companion.fillMaxWidth().padding(4.dp)
                        ) {
                            Checkbox(
                                checked = selectedCompetitors.contains(competitor.id),
                                onCheckedChange = { checked ->
                                    selectedCompetitors = if (checked) {
                                        selectedCompetitors + competitor.id
                                    } else {
                                        selectedCompetitors - competitor.id
                                    }
                                }
                            )
                            Column(modifier = Modifier.Companion.weight(1f)) {
                                Text("${competitor.last_name} ${competitor.first_name}")
                                Text(
                                    "Âge: ${calculateAge(competitor.born_at)} ans",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            try {
                                val newCompetitors = mutableListOf<Competitor>()

                                selectedCompetitors.forEach { competitorId ->
                                    val response = ApiClient.apiService.addCompetitorToCompetition(
                                        token,
                                        competitionId,
                                        AddCompetitorRequest(competitorId)
                                    )

                                    if (response.isSuccessful) {
                                        allCompetitors?.find { it.id == competitorId }
                                            ?.let { newCompetitor ->
                                                newCompetitors.add(newCompetitor)
                                            }
                                    }
                                }
                                competitors = newCompetitors + (competitors ?: emptyList())

                                selectedCompetitors = emptySet()
                                showAddDialog = false
                            } catch (e: Exception) {
                                error = "Erreur critique: ${e.localizedMessage}"
                            }
                        }
                    },
                    enabled = selectedCompetitors.isNotEmpty()
                ) {
                    Text("Ajouter (${selectedCompetitors.size})")
                }
            },
            dismissButton = {
                Button(onClick = {
                    selectedCompetitors = emptySet()
                    showAddDialog = false
                }) {
                    Text("Annuler")
                }
            }
        )
    }

    ScreenScaffold(
        title = "Compétiteurs de la compétition",
        navController = navController
    ) {
        Box(
            modifier = Modifier.Companion.fillMaxSize(),
            contentAlignment = Alignment.Companion.Center
        ) {
            when {
                isLoading -> CircularProgressIndicator()
                error != null -> Text(
                    text = error!!,
                    color = MaterialTheme.colorScheme.error
                )

                else -> {
                    Column(
                        modifier = Modifier.Companion.fillMaxSize()
                    ) {
                        if (competitors.isNullOrEmpty()) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Aucun compétiteur trouvé")
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.Companion.weight(1f)
                            ) {
                                items(competitors!!) { competitor ->
                                    Card(
                                        modifier = Modifier.Companion
                                            .fillMaxWidth()
                                            .padding(8.dp),
                                        elevation = CardDefaults.cardElevation(4.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.Companion.fillMaxWidth(),
                                            verticalAlignment = Alignment.Companion.CenterVertically
                                        ) {
                                            Column(
                                                modifier = Modifier.Companion
                                                    .weight(1f)
                                                    .padding(16.dp)
                                            ) {
                                                Text("Nom: ${competitor.last_name}")
                                                Text("Prénom: ${competitor.first_name}")
                                                Text("Genre: ${if (competitor.gender == "H") "Homme" else "Femme"}")
                                            }

                                            IconButton(
                                                onClick = { competitorToDelete = competitor }
                                            ) {
                                                Icon(
                                                    Icons.Default.Remove,
                                                    contentDescription = "Retirer de la compétition",
                                                    tint = MaterialTheme.colorScheme.error
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Button(
                            onClick = { showAddDialog = true },
                            modifier = Modifier.Companion
                                .fillMaxWidth()
                                .padding(16.dp),
                            enabled = !isLoading
                        ) {
                            Text("Ajouter un compétiteur")
                        }
                    }
                }
            }
        }
    }
}
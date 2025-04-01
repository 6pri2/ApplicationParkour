package iut.gon.applicationparkour.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import iut.gon.applicationparkour.ui.components.competitor.AddCompetitorDialog
import iut.gon.applicationparkour.ui.components.competitor.calculateAge
import iut.gon.applicationparkour.data.api.ApiClient
import iut.gon.applicationparkour.data.model.Competitor
import iut.gon.applicationparkour.ui.components.scaffold.ScreenScaffold
import kotlinx.coroutines.launch
import kotlin.collections.sortedByDescending


/**
 * Affichage de tous les compétiteurs
 */

@Composable
fun CompetitorScreen(navController: NavController) {
    ScreenScaffold(
        title = "Compétiteurs",
        navController = navController
    ) {
        val token = "Bearer 1ofD5tbAoC0Xd0TCMcQG3U214MqUo7JzUWrQFWt1ugPuiiDmwQCImm9Giw7fwR0Y"
        var competitors by remember { mutableStateOf<List<Competitor>?>(null) }
        var showDialog by remember { mutableStateOf(false) }
        var competitorToEdit by remember { mutableStateOf<Competitor?>(null) }  // Pour l'édition
        var showDeleteDialog by remember { mutableStateOf(false) }  // Pour la confirmation de suppression
        var competitorToDelete by remember { mutableStateOf<Competitor?>(null) } // Compétiteur à supprimer

        val scope = rememberCoroutineScope()

        // Fonction pour mettre à jour la liste des compétiteurs
        val updateCompetitors = {
            scope.launch {
                try {
                    competitors = ApiClient.apiService.getCompetitors(token)
                } catch (e: Exception) {
                    println("Erreur lors du chargement des compétiteurs : ${e.message}")
                }
            }
        }

        // Charger les compétiteurs au démarrage de l'écran
        LaunchedEffect(true) {
            updateCompetitors() // Charger les compétiteurs lorsque l'écran est chargé
        }

        // Affichage du dialog pour ajouter ou modifier un compétiteur
        if (showDialog) {
            AddCompetitorDialog(
                token = token,
                competitor = competitorToEdit, // Passer le compétiteur à modifier (null si ajout)
                onDismiss = { showDialog = false },
                onCompetitorsUpdated = {
                    updateCompetitors() // Mettre à jour la liste des compétiteurs
                    showDialog = false
                }
            )
        }

        // Affichage du dialog de suppression
        if (showDeleteDialog && competitorToDelete != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Confirmer la suppression") },
                text = {
                    Text("Voulez-vous vraiment supprimer ${competitorToDelete!!.first_name} ${competitorToDelete!!.last_name} ?")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            // Supprimer le compétiteur via l'API
                            scope.launch {
                                try {
                                    ApiClient.apiService.deleteCompetitor(
                                        token,
                                        competitorToDelete!!.id
                                    ) // Suppression via l'API
                                    updateCompetitors() // Mise à jour de la liste après suppression
                                    showDeleteDialog = false
                                } catch (e: Exception) {
                                    println("Erreur lors de la suppression : ${e.message}")
                                }
                            }
                        }
                    ) {
                        Text("Supprimer")
                    }
                },
                dismissButton = {
                    Button(onClick = { showDeleteDialog = false }) { Text("Annuler") }
                }
            )
        }

        // Affichage de la liste des compétiteurs
        Column(
            modifier = Modifier.Companion
                .fillMaxSize()
                .padding(16.dp)
        ) {
            when {
                competitors == null -> {
                    CircularProgressIndicator(modifier = Modifier.Companion.align(Alignment.Companion.CenterHorizontally)) // Si les compétiteurs sont en cours de chargement
                }

                competitors!!.isEmpty() -> {
                    Text(
                        "Aucun compétiteur trouvé.",
                        modifier = Modifier.Companion.align(Alignment.Companion.CenterHorizontally)
                    ) // Si aucune donnée n'est disponible
                }

                else -> {
                    LazyColumn(modifier = Modifier.Companion.weight(1f)) { // Donne de l'espace au LazyColumn
                        items(competitors!!.sortedByDescending {
                            // Vous pourriez utiliser une date d'ajout si disponible
                            // Ici on utilise simplement l'ordre d'ajout dans la liste
                            competitors!!.indexOf(it)
                        }) { competitor ->

                            val fullName = "${competitor.first_name} ${competitor.last_name}"
                            val birthDate = competitor.born_at // Format : "yyyy-MM-dd"
                            // Calculer l'âge
                            val age = calculateAge(birthDate)


                            Card(
                                modifier = Modifier.Companion
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                elevation = CardDefaults.cardElevation(4.dp)
                            ) {
                                Column(modifier = Modifier.Companion.padding(16.dp)) {
                                    Text(
                                        text = "Nom: $fullName",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(text = "Âge: $age ans")
                                    Text(text = "Genre: ${if (competitor.gender == "H") "Homme" else "Femme"}")

                                    // Icônes pour modifier et supprimer
                                    Row(
                                        horizontalArrangement = Arrangement.End,
                                        modifier = Modifier.Companion.fillMaxWidth()
                                    ) {
                                        IconButton(onClick = {
                                            competitorToEdit = competitor // Lancer l'édition
                                            showDialog = true // Afficher le dialog d'édition
                                        }) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Modifier"
                                            )
                                        }

                                        IconButton(onClick = {
                                            competitorToDelete = competitor // Lancer la suppression
                                            showDeleteDialog =
                                                true // Afficher la confirmation de suppression
                                        }) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Supprimer"
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.Companion.height(16.dp)) // Un espacement entre la liste et le bouton

            // Le bouton "Ajouter un compétiteur"
            Button(
                onClick = {
                    competitorToEdit = null
                    showDialog = true
                },
                modifier = Modifier.Companion.fillMaxWidth()
            ) {
                Text("Ajouter un compétiteur")
            }
        }
    }
}
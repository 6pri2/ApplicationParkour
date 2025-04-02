package iut.gon.applicationparkour.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow

import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import iut.gon.applicationparkour.ui.components.competitor.AddCompetitorDialog
import iut.gon.applicationparkour.ui.components.competitor.calculateAge
import iut.gon.applicationparkour.data.api.ApiClient
import iut.gon.applicationparkour.data.model.Competitor
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompetitorScreen(navController: NavController) {

    var competitors by remember { mutableStateOf<List<Competitor>?>(null) }
    var filteredCompetitors by remember { mutableStateOf<List<Competitor>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    var competitorToEdit by remember { mutableStateOf<Competitor?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var competitorToDelete by remember { mutableStateOf<Competitor?>(null) }
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    // Fonction pour filtrer les compétiteurs
    fun filterCompetitors(query: String) {
        if (query.isEmpty()) {
            filteredCompetitors = competitors ?: emptyList()
        } else {
            filteredCompetitors = competitors?.filter {
                it.first_name.contains(query, ignoreCase = true) ||
                        it.last_name.contains(query, ignoreCase = true) ||
                        "${it.first_name} ${it.last_name}".contains(query, ignoreCase = true)
            } ?: emptyList()
        }
    }

    // Fonction pour mettre à jour la liste des compétiteurs
    val updateCompetitors = {
        scope.launch {
            try {
                competitors = ApiClient.apiService.getCompetitors()
                filterCompetitors(searchQuery) // Appliquer le filtre après le chargement
            } catch (e: Exception) {
                println("Erreur lors du chargement des compétiteurs : ${e.message}")
            }
        }
    }

    // Charger les compétiteurs au démarrage de l'écran
    LaunchedEffect(true) {
        updateCompetitors()
    }

    // Mettre à jour les compétiteurs filtrés lorsque la requête change
    LaunchedEffect(searchQuery, competitors) {
        filterCompetitors(searchQuery)
    }

    // Dialog pour ajouter/modifier un compétiteur
    if (showDialog) {
        AddCompetitorDialog(
            competitor = competitorToEdit,
            onDismiss = { showDialog = false },
            onCompetitorsUpdated = {
                updateCompetitors()
                showDialog = false
            }
        )
    }

    // Dialog de suppression
    if (showDeleteDialog && competitorToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Confirmer la suppression", style = MaterialTheme.typography.headlineSmall) },
            text = {
                Text("Voulez-vous vraiment supprimer ${competitorToDelete!!.first_name} ${competitorToDelete!!.last_name} ?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                ApiClient.apiService.deleteCompetitor(competitorToDelete!!.id)
                                updateCompetitors()
                                showDeleteDialog = false
                            } catch (e: Exception) {
                                println("Erreur lors de la suppression : ${e.message}")
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Supprimer")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showDeleteDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text("Annuler")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Compétiteurs", style = MaterialTheme.typography.headlineMedium) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Barre de recherche
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    filterCompetitors(it)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp)),
                placeholder = { Text("Rechercher un compétiteur...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Rechercher"
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done,
                    keyboardType = KeyboardType.Text
                ),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                ),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            when {
                competitors == null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                filteredCompetitors.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (searchQuery.isEmpty()) "Aucun compétiteur trouvé" else "Aucun résultat pour \"$searchQuery\"",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredCompetitors.sortedByDescending { it.id }) { competitor ->
                            CompetitorCard(
                                competitor = competitor,
                                onEdit = {
                                    competitorToEdit = competitor
                                    showDialog = true
                                },
                                onDelete = {
                                    competitorToDelete = competitor
                                    showDeleteDialog = true
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    competitorToEdit = null
                    showDialog = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Ajouter un compétiteur", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
fun CompetitorCard(
    competitor: Competitor,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val fullName = "${competitor.first_name} ${competitor.last_name}"
    val age = calculateAge(competitor.born_at)
    val gender = if (competitor.gender == "H") "Homme" else "Femme"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = fullName,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row {
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
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                modifier = Modifier.clip(RoundedCornerShape(8.dp)),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.2f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Text(
                        text = "Âge: $age ans",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Genre: $gender",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
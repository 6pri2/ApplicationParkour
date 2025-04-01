package iut.gon.applicationparkour.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import iut.gon.applicationparkour.data.api.ApiClient
import iut.gon.applicationparkour.data.model.Competition
import iut.gon.applicationparkour.ui.components.competition.CompetitionEditDialog
import iut.gon.applicationparkour.ui.components.competition.CompetitionItem
import iut.gon.applicationparkour.ui.components.competition.DeleteCompetitionDialog
import iut.gon.applicationparkour.ui.components.scaffold.ScreenScaffold
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompetitionScreen(navController: NavController) {
    ScreenScaffold(
        title = "Compétitions",
        navController = navController
    ) {
        val token = "Bearer 1ofD5tbAoC0Xd0TCMcQG3U214MqUo7JzUWrQFWt1ugPuiiDmwQCImm9Giw7fwR0Y"
        var competitions by remember { mutableStateOf<List<Competition>?>(null) }
        var searchQuery by remember { mutableStateOf("") }
        var filteredCompetitions by remember { mutableStateOf<List<Competition>?>(null) }
        var showAddDialog by remember { mutableStateOf(false) }
        var isLoading by remember { mutableStateOf(false) }
        var showSuccessMessage by remember { mutableStateOf<String?>(null) }
        var showErrorMessage by remember { mutableStateOf<String?>(null) }

        val scope = rememberCoroutineScope()

        // Fonction pour charger les compétitions
        fun loadCompetitions() {
            scope.launch {
                isLoading = true
                try {
                    competitions = ApiClient.apiService.getCompetitions(token)
                    isLoading = false
                } catch (e: Exception) {
                    isLoading = false
                    showErrorMessage = "Erreur lors du chargement: ${e.message}"
                }
            }
        }

        // Fonction pour créer une compétition et gérer les courses
        fun createCompetitionAndManageCourses(baseCompetition: Competition) {
            scope.launch {
                isLoading = true
                try {
                    val createdCompetition =
                        ApiClient.apiService.addCompetition(token, baseCompetition)
                    showSuccessMessage = "Compétition créée"
                    navController.navigate("competitionCourses/${createdCompetition.id}") {
                        popUpTo("competitions") { inclusive = false }
                    }
                } catch (e: Exception) {
                    showErrorMessage = "Erreur création: ${e.message}"
                } finally {
                    isLoading = false
                    showAddDialog = false
                }
            }
        }

        // Chargement initial
        LaunchedEffect(Unit) {
            loadCompetitions()
        }

        // Filtrage des compétitions
        LaunchedEffect(competitions, searchQuery) {
            competitions?.let {
                filteredCompetitions = if (searchQuery.isEmpty()) {
                    it
                } else {
                    it.filter { competition ->
                        competition.name.contains(searchQuery, ignoreCase = true)
                    }
                }
            }
        }

        // Gestion des messages toast
        LaunchedEffect(showSuccessMessage, showErrorMessage) {
            if (showSuccessMessage != null) {
                delay(3000)
                showSuccessMessage = null
            }
            if (showErrorMessage != null) {
                delay(5000)
                showErrorMessage = null
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Barre de recherche
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Effacer")
                            }
                        }
                    },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    ),
                    placeholder = { Text("Rechercher une compétition...") },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { /* Handle keyboard done */ })
                )

                when {
                    isLoading && competitions == null -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(48.dp),
                                    strokeWidth = 3.dp
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Chargement en cours...", style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }

                    filteredCompetitions.isNullOrEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "Aucune compétition trouvée",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                Button(
                                    onClick = { showAddDialog = true },
                                    shape = MaterialTheme.shapes.large
                                ) {
                                    Text("Créer une compétition", modifier = Modifier.padding(horizontal = 24.dp))
                                }
                            }
                        }
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp)
                        ) {
                            items(filteredCompetitions!!) { competition ->
                                Card(
                                    modifier = Modifier
                                        .padding(vertical = 8.dp)
                                        .fillMaxWidth(),
                                    shape = MaterialTheme.shapes.medium,
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    CompetitionItem(
                                        competition = competition,
                                        onEdit = {},
                                        onDelete = {},
                                        onCompetitors = {},
                                        onResults = {},
                                        onArbitrage = {},
                                        onCourses = {},

                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Bouton Ajouter en bas à droite
            FloatingActionButton(
                onClick = { showAddDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp),
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(Icons.Default.Add, contentDescription = "Ajouter")
            }

            // Affichage du message de succès
            showSuccessMessage?.let { message ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    action = {
                        IconButton(onClick = { showSuccessMessage = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Fermer")
                        }
                    }
                ) {
                    Text(message)
                }
            }

            // Affichage du message d'erreur
            showErrorMessage?.let { message ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    action = {
                        IconButton(onClick = { showErrorMessage = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Fermer")
                        }
                    }
                ) {
                    Text(message)
                }
            }
        }

        // Dialog de création de compétition
        if (showAddDialog) {
            CompetitionEditDialog(
                competition = null,
                navController = navController,
                onDismiss = { showAddDialog = false },
                onSave = ::createCompetitionAndManageCourses,
                onManageCourses = {} // Pas utilisé en création
            )
        }
    }
}
package iut.gon.applicationparkour.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
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
import iut.gon.applicationparkour.CompetitionEditDialog
import iut.gon.applicationparkour.CompetitionItem
import iut.gon.applicationparkour.DeleteCompetitionDialog
import iut.gon.applicationparkour.data.api.ApiClient
import iut.gon.applicationparkour.data.model.Competition
import iut.gon.applicationparkour.ui.components.scaffold.ScreenScaffold
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Affichage de toutes les compétitions
 */

@Composable
fun CompetitionScreen(navController: NavController) {
    ScreenScaffold(
        title = "Compétitions",
        navController = navController
    ) {
        val token = "Bearer 1ofD5tbAoC0Xd0TCMcQG3U214MqUo7JzUWrQFWt1ugPuiiDmwQCImm9Giw7fwR0Y"
        var competitions by remember { mutableStateOf<List<Competition>?>(null) }
        var showEditDialog by remember { mutableStateOf(false) }
        var showDeleteDialog by remember { mutableStateOf(false) }
        var showAddDialog by remember { mutableStateOf(false) }
        var selectedCompetition by remember { mutableStateOf<Competition?>(null) }
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


        if (showEditDialog && selectedCompetition != null) {
            CompetitionEditDialog(
                competition = selectedCompetition,
                navController = navController,
                onDismiss = { showEditDialog = false },
                onSave = { updatedCompetition ->
                    scope.launch {
                        isLoading = true
                        try {
                            ApiClient.apiService.updateCompetition(
                                token,
                                selectedCompetition!!.id,
                                updatedCompetition
                            )
                            showSuccessMessage = "Compétition mise à jour"
                            loadCompetitions()
                        } catch (e: Exception) {
                            showErrorMessage = "Erreur mise à jour: ${e.message}"
                        } finally {
                            isLoading = false
                            showEditDialog = false
                        }
                    }
                },
                onManageCourses = {
                    navController.navigate("competitionCourses/${selectedCompetition!!.id}")
                }
            )
        }

        if (showAddDialog) {
            CompetitionEditDialog(
                competition = null,
                navController = navController,
                onDismiss = { showAddDialog = false },
                onSave = ::createCompetitionAndManageCourses,
                onManageCourses = {} // Pas utilisé en création
            )
        }

        if (showDeleteDialog && selectedCompetition != null) {
            DeleteCompetitionDialog(
                competition = selectedCompetition!!,
                onDismiss = { showDeleteDialog = false },
                onConfirm = {
                    scope.launch {
                        isLoading = true
                        try {
                            ApiClient.apiService.deleteCompetition(token, selectedCompetition!!.id)
                            showSuccessMessage = "Compétition supprimée"
                            loadCompetitions()
                        } catch (e: Exception) {
                            showErrorMessage = "Erreur suppression: ${e.message}"
                        } finally {
                            isLoading = false
                            showDeleteDialog = false
                        }
                    }
                }
            )
        }

        Box(
            modifier = Modifier.Companion
                .fillMaxSize()
        ) {

            when {
                isLoading && competitions == null -> {
                    Box(
                        Modifier.Companion.fillMaxSize(),
                        contentAlignment = Alignment.Companion.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                competitions.isNullOrEmpty() -> {
                    Box(
                        Modifier.Companion.fillMaxSize(),
                        contentAlignment = Alignment.Companion.Center
                    ) {
                        Column(horizontalAlignment = Alignment.Companion.CenterHorizontally) {
                            Text("Aucune compétition trouvée")
                            Spacer(Modifier.Companion.height(16.dp))
                            Button(
                                onClick = { showAddDialog = true },
                                modifier = Modifier.Companion.padding(horizontal = 32.dp)
                            ) {
                                Text("Créer une compétition")
                            }
                        }
                    }
                }

                else -> {
                    Column(modifier = Modifier.Companion.fillMaxSize()) {
                        LazyColumn(modifier = Modifier.Companion.weight(1f)) {
                            items(competitions!!) { competition ->
                                CompetitionItem(
                                    competition = competition,
                                    onEdit = {
                                        selectedCompetition = competition
                                        showEditDialog = true
                                    },
                                    onDelete = {
                                        selectedCompetition = competition
                                        showDeleteDialog = true
                                    },
                                    onCompetitors = {
                                        navController.navigate("competitors/${competition.id}")
                                    },
                                    onResults = {
                                        navController.navigate("results/${competition.id}")
                                    },
                                    onArbitrage = {
                                        navController.navigate("arbitrage/${competition.id}")
                                    },
                                    onCourses = {
                                        navController.navigate("competitionCourses/${competition.id}")
                                    }
                                )
                            }
                        }

                        Button(
                            onClick = { showAddDialog = true },
                            modifier = Modifier.Companion
                                .fillMaxWidth()
                                .padding(16.dp),
                            enabled = !isLoading
                        ) {
                            Text("Ajouter une compétition")
                        }
                    }
                }
            }

            if (isLoading && competitions != null) {
                Box(
                    modifier = Modifier.Companion
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Companion.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            Box(Modifier.Companion.align(Alignment.Companion.BottomCenter)) {
                showSuccessMessage?.let { message ->
                    Snackbar(
                        modifier = Modifier.Companion.padding(16.dp),
                        action = {
                            IconButton({
                                showSuccessMessage = null
                            }) { Icon(Icons.Default.Close, "Fermer") }
                        }
                    ) { Text(message, color = MaterialTheme.colorScheme.onPrimaryContainer) }
                }
                showErrorMessage?.let { message ->
                    Snackbar(
                        modifier = Modifier.Companion.padding(16.dp),
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        action = {
                            IconButton({
                                showErrorMessage = null
                            }) { Icon(Icons.Default.Close, "Fermer") }
                        }
                    ) { Text(message, color = MaterialTheme.colorScheme.onErrorContainer) }
                }
            }
        }
    }
}
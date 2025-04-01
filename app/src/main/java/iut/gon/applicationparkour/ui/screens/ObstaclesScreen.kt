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
import iut.gon.applicationparkour.ui.components.obstacle.AddObstacleDialog
import iut.gon.applicationparkour.data.api.ApiClient
import iut.gon.applicationparkour.data.model.Obstacles
import iut.gon.applicationparkour.ui.components.scaffold.ScreenScaffold
import kotlinx.coroutines.launch

/**
 * Affichage de tous les obstacles
 */

@Composable
fun ObstaclesScreen(navController: NavController) {
    ScreenScaffold(
        title = "Obstacles",
        navController = navController
    ) {
        val token = "Bearer 1ofD5tbAoC0Xd0TCMcQG3U214MqUo7JzUWrQFWt1ugPuiiDmwQCImm9Giw7fwR0Y"
        var obstacles by remember { mutableStateOf<List<Obstacles>?>(null) }
        var showDialog by remember { mutableStateOf(false) }
        var obstacleToEdit by remember { mutableStateOf<Obstacles?>(null) }
        var showDeleteDialog by remember { mutableStateOf(false) }
        var obstacleToDelete by remember { mutableStateOf<Obstacles?>(null) }

        val scope = rememberCoroutineScope()

        val updateObstacles = {
            scope.launch {
                try {
                    obstacles = ApiClient.apiService.getObstacles(token)
                } catch (e: Exception) {
                    println("Erreur lors du chargement des obstacles : ${e.message}")
                }
            }
        }

        LaunchedEffect(true) {
            updateObstacles()
        }

        if (showDialog) {
            AddObstacleDialog(
                token = token,
                obstacle = obstacleToEdit,
                onDismiss = { showDialog = false },
                onObstaclesUpdated = {
                    updateObstacles()
                    showDialog = false
                }
            )
        }

        if (showDeleteDialog && obstacleToDelete != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Confirmer la suppression") },
                text = { Text("Voulez-vous vraiment supprimer ${obstacleToDelete!!.name} ?") },
                confirmButton = {
                    Button(
                        onClick = {
                            scope.launch {
                                try {
                                    ApiClient.apiService.deleteObstacles(
                                        token,
                                        obstacleToDelete!!.id
                                    )
                                    updateObstacles()
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

        Column(
            modifier = Modifier.Companion
                .fillMaxSize()
                .padding(16.dp)
        ) {
            when {
                obstacles == null -> CircularProgressIndicator(
                    modifier = Modifier.Companion.align(
                        Alignment.Companion.CenterHorizontally
                    )
                )

                obstacles!!.isEmpty() -> Text(
                    "Aucun obstacle trouvé.",
                    modifier = Modifier.Companion.align(Alignment.Companion.CenterHorizontally)
                )

                else -> {
                    LazyColumn(modifier = Modifier.Companion.weight(1f)) {
                        items(obstacles!!) { obstacle ->
                            Card(
                                modifier = Modifier.Companion
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                elevation = CardDefaults.cardElevation(4.dp)
                            ) {
                                Column(modifier = Modifier.Companion.padding(16.dp)) {
                                    Text(
                                        text = "Nom: ${obstacle.name}",
                                        style = MaterialTheme.typography.bodyLarge
                                    )


                                    Row(
                                        horizontalArrangement = Arrangement.End,
                                        modifier = Modifier.Companion.fillMaxWidth()
                                    ) {
                                        IconButton(onClick = {
                                            obstacleToEdit = obstacle
                                            showDialog = true
                                        }) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Modifier"
                                            )
                                        }

                                        IconButton(onClick = {
                                            obstacleToDelete = obstacle
                                            showDeleteDialog = true
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

            Spacer(modifier = Modifier.Companion.height(16.dp))

            Button(
                onClick = {
                    obstacleToEdit = null
                    showDialog = true
                },
                modifier = Modifier.Companion.fillMaxWidth()
            ) {
                Text("Ajouter un obstacle")
            }
        }
    }
}
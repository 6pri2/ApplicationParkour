package iut.gon.applicationparkour.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import iut.gon.applicationparkour.ui.components.obstacle.AddObstacleDialog
import iut.gon.applicationparkour.data.api.ApiClient
import iut.gon.applicationparkour.data.model.Obstacles
import iut.gon.applicationparkour.ui.components.scaffold.ScreenScaffold
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ObstaclesScreen(navController: NavController) {
    val token = "Bearer 1ofD5tbAoC0Xd0TCMcQG3U214MqUo7JzUWrQFWt1ugPuiiDmwQCImm9Giw7fwR0Y"
    var obstacles by remember { mutableStateOf<List<Obstacles>?>(null) }
    var filteredObstacles by remember { mutableStateOf<List<Obstacles>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    var obstacleToEdit by remember { mutableStateOf<Obstacles?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var obstacleToDelete by remember { mutableStateOf<Obstacles?>(null) }
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    fun filterObstacles(query: String) {
        if (query.isEmpty()) {
            filteredObstacles = obstacles ?: emptyList()
        } else {
            filteredObstacles = obstacles?.filter {
                it.name.contains(query, ignoreCase = true)
            } ?: emptyList()
        }
    }

    val updateObstacles = {
        scope.launch {
            try {
                obstacles = ApiClient.apiService.getObstacles(token)
                filterObstacles(searchQuery)
            } catch (e: Exception) {
                println("Erreur lors du chargement des obstacles : ${e.message}")
            }
        }
    }

    LaunchedEffect(true) {
        updateObstacles()
    }

    LaunchedEffect(searchQuery, obstacles) {
        filterObstacles(searchQuery)
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
            title = { Text("Confirmer la suppression", style = MaterialTheme.typography.headlineSmall) },
            text = {
                Text("Voulez-vous vraiment supprimer ${obstacleToDelete!!.name} ?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                ApiClient.apiService.deleteObstacles(token, obstacleToDelete!!.id)
                                updateObstacles()
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

    ScreenScaffold(
        title = "Obstacles",
        navController = navController
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    filterObstacles(it)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp)),
                placeholder = { Text("Rechercher un obstacle...") },
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
                obstacles == null -> {
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

                filteredObstacles.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (searchQuery.isEmpty()) "Aucun obstacle trouvé" else "Aucun résultat pour \"$searchQuery\"",
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
                        items(filteredObstacles.sortedByDescending { it.id }) { obstacle ->
                            ObstacleCard(
                                obstacle = obstacle,
                                onEdit = {
                                    obstacleToEdit = obstacle
                                    showDialog = true
                                },
                                onDelete = {
                                    obstacleToDelete = obstacle
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
                    obstacleToEdit = null
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
                Text("Ajouter un obstacle", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
fun ObstacleCard(
    obstacle: Obstacles,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
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
                    text = obstacle.name,
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

        }
    }
}
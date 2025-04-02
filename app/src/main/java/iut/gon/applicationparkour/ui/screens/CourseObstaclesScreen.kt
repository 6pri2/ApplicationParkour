package iut.gon.applicationparkour.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import iut.gon.applicationparkour.data.api.ApiClient
import iut.gon.applicationparkour.data.model.AddObstacleRequest
import iut.gon.applicationparkour.data.model.ObstacleCourse
import iut.gon.applicationparkour.data.model.Obstacles
import iut.gon.applicationparkour.data.model.UpdateObstaclePositionRequest
import iut.gon.applicationparkour.ui.components.obstacle.ObstacleItem
import iut.gon.applicationparkour.ui.components.scaffold.ScreenScaffold
import kotlinx.coroutines.launch

/**
 * affichage des obstacles d'une course
 */

@Composable
fun CourseObstaclesScreen(
    navController: NavController,
    courseId: Int,
    onFinalSave: () -> Unit = { navController.popBackStack() }
) {
    println("courseObstaclesScreen - courseID : $courseId")
    val token = "Bearer 1ofD5tbAoC0Xd0TCMcQG3U214MqUo7JzUWrQFWt1ugPuiiDmwQCImm9Giw7fwR0Y"
    var courseObstacles by remember { mutableStateOf<List<ObstacleCourse>>(emptyList()) }
    var unusedObstacles by remember { mutableStateOf<List<Obstacles>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var tempPositions by remember { mutableStateOf<Map<Int, Int>>(emptyMap()) }
    var showAddObstacleDialog by remember { mutableStateOf(false) }
    var obstacleName by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    fun loadObstacles() {
        scope.launch {
            isLoading = true
            try {
                val updatedObstacles = ApiClient.apiService.getCourseObstacles(courseId)
                val updatedUnused = ApiClient.apiService.getUnusedObstacles(courseId)
                courseObstacles = updatedObstacles.sortedBy { it.position }
                unusedObstacles = updatedUnused
            } catch (e: Exception) {
                errorMessage = "Erreur de chargement: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(courseId) {
        loadObstacles()
    }

    fun moveObstacleUp(obstacle: ObstacleCourse) {
        val currentIndex = courseObstacles.indexOfFirst { it.obstacle_id == obstacle.obstacle_id }
        println("DEBUG - Moving up from index $currentIndex")
        if (currentIndex > 0) {
            scope.launch {
                try {
                    val request = UpdateObstaclePositionRequest(
                        obstacleId = obstacle.obstacle_id,
                        position = currentIndex
                    )
                    println("DEBUG - Sending request: $request")
                    // Envoyer la nouvelle position (currentIndex car les indices commencent à 0)
                    val response = ApiClient.apiService.updateObstaclePosition(
                        courseId,
                        request
                    )
                    println("DEBUG - Response: ${response.code()} ${response.message()}")
                    println("DEBUG - Response body: ${response.errorBody()?.string()}")
                    if (response.isSuccessful) {
                        // Recharger les obstacles après mise à jour
                        loadObstacles()
                    } else {
                        errorMessage = "Échec de la mise à jour"
                    }
                } catch (e: Exception) {
                    println("DEBUG - Exception: ${e.stackTraceToString()}")
                    errorMessage = "Erreur: ${e.message}"
                }
            }
        }
    }

    fun moveObstacleDown(obstacle: ObstacleCourse) {
        val currentIndex = courseObstacles.indexOfFirst { it.obstacle_id == obstacle.obstacle_id }
        if (currentIndex < courseObstacles.size - 1) {
            scope.launch {
                try {
                    // Envoyer la nouvelle position (currentIndex + 2 si l'API attend des positions à partir de 1)
                    val response = ApiClient.apiService.updateObstaclePosition(
                        courseId,
                        UpdateObstaclePositionRequest(
                            obstacleId = obstacle.obstacle_id,
                            position = currentIndex+1
                        )
                    )

                    if (response.isSuccessful) {
                        loadObstacles()
                    } else {
                        errorMessage = "Échec de la mise à jour"
                    }
                } catch (e: Exception) {
                    errorMessage = "Erreur: ${e.message}"
                }
            }
        }
    }
        if (showAddObstacleDialog) {
            AlertDialog(
                onDismissRequest = {
                    showAddObstacleDialog = false
                    obstacleName = ""
                },
                title = { Text("Créer un nouvel obstacle") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = obstacleName,
                            onValueChange = { obstacleName = it },
                            label = { Text("Nom de l'obstacle") },
                            modifier = Modifier.Companion.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            scope.launch {
                                try {
                                    // Créer le nouvel obstacle
                                    val newObstacle = ApiClient.apiService.addObstacles(
                                        Obstacles(id = 0, name = obstacleName)
                                    )
                                    // Ajouter l'obstacle au parcours
                                    ApiClient.apiService.addObstacleToCourse(
                                        courseId,
                                        AddObstacleRequest(newObstacle.id)
                                    )
                                    loadObstacles()
                                    showAddObstacleDialog = false
                                    obstacleName = ""
                                } catch (e: Exception) {
                                    errorMessage = "Erreur lors de la création: ${e.message}"
                                }
                            }
                        },
                        enabled = obstacleName.isNotBlank()
                    ) {
                        Text("Créer")
                    }
                },
                dismissButton = {
                    Button(onClick = {
                        showAddObstacleDialog = false
                        obstacleName = ""
                    }) {
                        Text("Annuler")
                    }
                }
            )
        }

    ScreenScaffold(
        title = "Gestion des obstacles",
        navController = navController,
        //onBackPressed = {onBackPressed()}
    ) {
        Box(modifier = Modifier.Companion.fillMaxSize()) {
            Column(modifier = Modifier.Companion.fillMaxSize()) {
                // Liste des obstacles du parcours
                LazyColumn(modifier = Modifier.Companion.weight(2f)) {
                    items(courseObstacles, key = { it.obstacle_id }) { obstacle ->
                        ObstacleItem(
                            obstacle = obstacle,
                            onMoveUp = { moveObstacleUp(obstacle) },
                            onMoveDown = { moveObstacleDown(obstacle) },
                            onDelete = {
                                scope.launch {
                                    try {
                                        ApiClient.apiService.removeObstacleFromCourse(
                                            courseId,
                                            obstacle.obstacle_id
                                        )
                                        loadObstacles()
                                    } catch (e: Exception) {
                                        errorMessage = "Erreur de suppression: ${e.message}"
                                    }
                                }
                            },
                            isFirst = courseObstacles.firstOrNull()?.obstacle_id == obstacle.obstacle_id,
                            isLast = courseObstacles.lastOrNull()?.obstacle_id == obstacle.obstacle_id,
                            isOnlyObstacle = courseObstacles.size == 1
                        )
                    }
                }

                // Liste des obstacles disponibles
                Text("Obstacles disponibles:", modifier = Modifier.Companion.padding(8.dp))
                LazyColumn(modifier = Modifier.Companion.weight(1f)) {
                    items(unusedObstacles) { obstacle ->
                        ObstacleItem(
                            obstacle = ObstacleCourse(
                                obstacle_id = obstacle.id,
                                obstacle_name = obstacle.name,
                                duration = 0,
                                position = 0
                            ),
                            onAdd = {
                                scope.launch {
                                    try {
                                        ApiClient.apiService.addObstacleToCourse(
                                            courseId,
                                            AddObstacleRequest(obstacle.id)
                                        )
                                        loadObstacles()
                                    } catch (e: Exception) {
                                        errorMessage = "Erreur d'ajout: ${e.message}"
                                    }
                                }
                            }
                        )
                    }
                }

                Button(
                    onClick = { showAddObstacleDialog = true },
                    modifier = Modifier.Companion
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.Companion.width(8.dp))
                    Text("Créer un nouvel obstacle")
                }
                Button(
                    onClick = {
                        scope.launch {
                            onFinalSave()
                        }
                    },
                    modifier = Modifier.Companion
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Valider le parcours")
                }
            }

            // Gestion des états de chargement/erreur
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.Companion
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.7f)),
                        contentAlignment = Alignment.Companion.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                errorMessage != null -> {
                    Snackbar(
                        modifier = Modifier.Companion.padding(16.dp),
                        action = {
                            Button(onClick = { errorMessage = null }) {
                                Text("OK")
                            }
                        }
                    ) {
                        Text(errorMessage!!)
                    }
                }
            }
        }
    }
}
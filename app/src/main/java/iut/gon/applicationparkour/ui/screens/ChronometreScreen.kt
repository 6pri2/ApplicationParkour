package iut.gon.applicationparkour.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
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
import iut.gon.applicationparkour.data.model.Competition
import iut.gon.applicationparkour.data.model.Courses
import iut.gon.applicationparkour.data.model.ObstacleResult
import iut.gon.applicationparkour.data.model.ObstacleResultAPI
import iut.gon.applicationparkour.data.model.Obstacles
import iut.gon.applicationparkour.data.model.Obstaclesv2
import iut.gon.applicationparkour.data.model.PerformanceAPI
import iut.gon.applicationparkour.data.model.PerformanceResponse
import iut.gon.applicationparkour.ui.components.scaffold.ScreenScaffold
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@SuppressLint("UnrememberedMutableState")
@Composable
fun ChronometreScreen(
    navController: NavController,
    competitionId: String,
    courseId: String,
    competitorId: String
) {
    val scope = rememberCoroutineScope()

    var obstacles by remember { mutableStateOf<List<Obstaclesv2>>(emptyList()) }
    var currentObstacleIndex by remember { mutableStateOf(0) }
    var recordedResults by remember { mutableStateOf<List<ObstacleResult>>(emptyList()) }
    var isRunning by remember { mutableStateOf(false) }
    var timeMillis by remember { mutableStateOf(0L) }
    var startTime by remember { mutableStateOf(0L) }
    var error by remember { mutableStateOf<String?>(null) }
    var currentObstacleStartTime by remember { mutableStateOf(0L) }
    var competition by remember { mutableStateOf<Competition?>(null) }
    val listState = rememberLazyListState()
    var hasRecordedFallForCurrentAttempt by remember { mutableStateOf(false) }
    var currentAttemptStartTime by remember { mutableStateOf(0L) }
    var remainingRetries by remember { mutableStateOf(1) } // Nombre de réessais restants
    var hasRecordedFall by remember { mutableStateOf(false) }
    var resetTrigger by remember { mutableStateOf(0) }
    var isCourseCompleted by remember { mutableStateOf(false) }
    var performanceStatus by remember { mutableStateOf<PerformanceResponse?>(null) }
    var course by remember { mutableStateOf<Courses?>(null) }
    // Ajouter ces états
    var savedPerformance by remember { mutableStateOf<PerformanceResponse?>(null) }

    // Ajouter ces 3 variables
    var isSaving by remember { mutableStateOf(false) }
    var showPerformanceDialog by remember { mutableStateOf(false) }
    var apiErrorMessage by remember { mutableStateOf<String?>(null) }

    var isLoadingObstacles by remember { mutableStateOf(true) }

    fun handleSaveResults() {
        scope.launch {
            isSaving = true
            apiErrorMessage = null

            try {

                // Calculer le statut global de la performance
                val totalTenths = recordedResults.sumOf { it.time }

                // Déterminer le statut selon les règles métier
                val globalStatus = when {
                    recordedResults.any { it.status == "defection" } -> "defection"
                    else -> "over" // Tous les obstacles terminés
                }
                println(competitorId.toInt())
                println(courseId.toInt())
                val insert = PerformanceAPI(
                    competitorId.toInt(),
                    courseId.toInt(),
                    globalStatus,
                    totalTenths
                )
                // Créer la performance avec le bon statut
                val performanceResponse = ApiClient.apiService.createPerformance(
                    insert
                )

                // Sauvegarder les résultats d'obstacles d'abord
                recordedResults.forEach {
                    // Convertir le statut des obstacles si nécessaire
                    val obstacleStatus = when(it.status) {
                        "failed" -> "defection" // Ou la valeur appropriée
                        else -> "to_verify" // Valeur par défaut
                    }

                    runBlocking { launch { delay(1000) } }

                    var p = ApiClient.apiService.getPerformancesv2()
                    println(p.size)
                    p = p.filter { it.competitor_id == competitorId.toInt()
                            && it.course_id == courseId.toInt()  }
                    println(p.size)
                    val pi = p.get(0)

                    val insertion = ObstacleResultAPI(
                        obstacle_id = it.obstacle_id,
                        performance_id = pi.id,
                        has_fell = hasRecordedFall,
                        to_verify = false,
                        time = it.time,
                    )
                    println("id obstacle" + it.obstacle_id)
                    println("id performance : " + pi.id)
                    println("has_fell : " + hasRecordedFall)
                    println("temps : "+ it.time)
                    println("result" + ApiClient.apiService.saveObstacleResult(
                        insertion
                    ))
                }




                savedPerformance = performanceResponse
                showPerformanceDialog = true
                resetTrigger++

            } catch (e: Exception) {
                apiErrorMessage = "Erreur API : ${e.message?.substringBefore("\n") ?: "Code statut invalide"}"
            } finally {
                isSaving = false
            }
        }
    }

    LaunchedEffect(courseId) {
        try {
            course = ApiClient.apiService.getCourseById(courseId.toInt())
        } catch (e: Exception) {
            error = "Erreur de chargement du parcours"
        }
    }

    // Logique de gestion du temps maximum
    LaunchedEffect(isRunning, timeMillis) {
        if (isRunning) {
            course?.max_duration?.let { maxDuration ->
                // Convertir la durée max en millisecondes (maxDuration est en dixièmes de seconde)
                val maxDurationMillis = maxDuration * 100L // Ex: 120 dixièmes (12.0s) → 12000ms

                if (timeMillis >= maxDurationMillis) {
                    // Arrêter le chrono
                    isRunning = false

                    val currentObstacle = obstacles.getOrNull(currentObstacleIndex)
                    currentObstacle?.let {
                        // Calculer le temps en dixièmes de seconde (Int)
                        val timeTenths = ((timeMillis - currentAttemptStartTime) / 100).toInt()

                        val result = ObstacleResult(
                            competition_id = competitionId.toInt(),
                            course_id = courseId.toInt(),
                            competitor_id = competitorId.toInt(),
                            obstacle_id = it.id,
                            time = timeTenths, // Envoyé en Int
                            status = "failed"
                        )
                        recordedResults = recordedResults + result
                        isCourseCompleted = true
                        hasRecordedFall = true
                    }
                }
            }
        }
    }

    LaunchedEffect(resetTrigger) {
        currentObstacleIndex = 0
        recordedResults = emptyList()
        timeMillis = 0L
        isRunning = false
        remainingRetries = if (competition?.has_retry == 1) 1 else 0
        hasRecordedFall = false
        currentAttemptStartTime = 0L
        isCourseCompleted = false
    }

    LaunchedEffect(recordedResults.size) {
        if (recordedResults.isNotEmpty()) {
            listState.animateScrollToItem(recordedResults.size - 1)
        }
    }

    LaunchedEffect(currentObstacleIndex) {
        currentObstacleStartTime = timeMillis
        currentAttemptStartTime = timeMillis
        remainingRetries = if (competition?.has_retry == 1) 1 else 0
        hasRecordedFall = false
    }

    // Calcul de l'état désactivé pour le bouton Démarrer
    val isStartButtonDisabled = isCourseCompleted ||
            (competition?.has_retry == 1 && remainingRetries == 0) ||
            (competition?.has_retry != 1 && hasRecordedFall)


    LaunchedEffect(courseId) {
        isLoadingObstacles = true
        try {
            obstacles = ApiClient.apiService.getCourseObstaclesv2(courseId.toInt())
        } catch (e: Exception) {
            error = "Erreur d'obstacles"
        } finally {
            isLoadingObstacles = false
        }
    }

    LaunchedEffect(competitionId) {
        try {
            competition = ApiClient.apiService.getCompetitionById(competitionId.toInt())
            remainingRetries = if (competition?.has_retry == 1) 1 else 0
        } catch (e: Exception) {
            error = "Erreur de compétition"
            remainingRetries = 0
        }
    }

    val formattedTime by derivedStateOf {
        val tenths = (timeMillis / 100).toInt() // Conversion en dixièmes de seconde
        val seconds = tenths / 10
        val tenth = tenths % 10
        "$seconds.${tenth}s"
    }

    LaunchedEffect(isRunning) {
        if (isRunning) {
            startTime = System.currentTimeMillis() - timeMillis
            while (isRunning) {
                timeMillis = System.currentTimeMillis() - startTime
                delay(10)
            }
        }
    }

    fun recordObstacleTime() {
        val currentObstacle = obstacles.getOrNull(currentObstacleIndex)
        currentObstacle?.let {
            // Convertir en dixièmes de seconde (ex: 1234ms → 12.3s → 123)
            val timeTenths = ((timeMillis - currentObstacleStartTime) / 100).toInt()

            val result = ObstacleResult(
                competitionId.toInt(),
                courseId.toInt(),
                competitorId.toInt(),
                it.id,
                timeTenths, // Stocké en Int
                "completed"
            )
            recordedResults = recordedResults + result
            currentObstacleIndex++
            currentObstacleStartTime = timeMillis

            if (currentObstacleIndex >= obstacles.size) {
                isCourseCompleted = true
                isRunning = false
            }
        }
    }

    ScreenScaffold(
        title = "Chronomètre",
        navController = navController
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "$formattedTime ",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            if (obstacles.isNotEmpty() && currentObstacleIndex < obstacles.size) {
                val obstacle = obstacles[currentObstacleIndex]
                Button(
                    onClick = { recordObstacleTime() },
                    enabled = isRunning,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Obstacle ${currentObstacleIndex + 1}/${obstacles.size}: ${obstacle.obstacle_name}",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            if (recordedResults.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = MaterialTheme.shapes.medium
                        )
                        .padding(8.dp)
                ) {
                    Text(
                        "Temps enregistrés :",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        itemsIndexed(recordedResults) { index, result ->
                            val obstacle = obstacles.find { it.id == result.obstacle_id }
                            val seconds = result.time / 10
                            val tenth = result.time % 10

                            ListItem(
                                headlineContent = {
                                    Text(
                                        "${index + 1}. ${obstacle?.obstacle_name ?: "Inconnu"} : " +
                                                "$seconds.${tenth}s (${result.status})",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                },
                                leadingContent = {
                                    Icon(
                                        Icons.Default.AccessTime,
                                        contentDescription = null
                                    )
                                },
                            )
                        }
                    }
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Bouton Start/Stop
                Button(
                    onClick = {
                        if (!isRunning && !isCourseCompleted) {
                            // Nouveau démarrage uniquement si course non terminée
                            isRunning = true
                        } else {
                            // Arrêt possible à tout moment
                            isRunning = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRunning) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoadingObstacles && when {
                        isCourseCompleted -> false
                        isRunning -> true
                        else -> !isStartButtonDisabled
                    }
                )  {
                    if (isLoadingObstacles) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    } else {
                        Text(if (isRunning) "Arrêter" else "Démarrer")
                    }
                }

                // Bouton Chute
                Button(
                    onClick = {
                        val currentObstacle = obstacles.getOrNull(currentObstacleIndex)
                        currentObstacle?.let {
                            // Convertir en dixièmes de seconde (Int)
                            val timeTenths = ((timeMillis - currentAttemptStartTime) / 100).toInt()

                            val result = ObstacleResult(
                                competition_id = competitionId.toInt(),
                                course_id = courseId.toInt(),
                                competitor_id = competitorId.toInt(),
                                obstacle_id = it.id,
                                time = timeTenths, // Stocké en Int
                                status = "failed"
                            )
                            recordedResults = recordedResults + result

                            if (competition?.has_retry == 1) {
                                if (remainingRetries > 0) {
                                    // Réessai - Réinitialiser le chrono pour cet obstacle
                                    remainingRetries--
                                    timeMillis = currentAttemptStartTime
                                    currentAttemptStartTime = timeMillis // Nouveau départ
                                } else {
                                    // Plus de réessais - Arrêt définitif
                                    isRunning = false
                                    hasRecordedFall = true
                                    isCourseCompleted = true
                                }
                            } else {
                                // Pas de réessai - Arrêt immédiat
                                isRunning = false
                                hasRecordedFall = true
                                isCourseCompleted = true
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = isRunning && !isCourseCompleted && when {
                        competition?.has_retry == 1 -> remainingRetries > 0
                        else -> !hasRecordedFall
                    }
                ) {
                    Text(
                        "Chute ${
                            if (competition?.has_retry == 1) "($remainingRetries)"
                            else ""
                        }"
                    )
                }

                LaunchedEffect(currentObstacleIndex) {
                    hasRecordedFallForCurrentAttempt = false
                }

                Button(
                    onClick = { handleSaveResults() }, // Utiliser la nouvelle fonction
                    modifier = Modifier.fillMaxWidth(),
                    enabled = recordedResults.isNotEmpty() && !isRunning && !isSaving
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Sauvegarder les résultats")
                    }
                }

                // Ajouter ce composant pour les erreurs
                if (apiErrorMessage != null) {
                    AlertDialog(
                        onDismissRequest = { apiErrorMessage = null },
                        title = { Text("Erreur de sauvegarde") },
                        text = { Text(apiErrorMessage!!) },
                        confirmButton = {
                            Button(onClick = { apiErrorMessage = null }) {
                                Text("OK")
                            }
                        }
                    )
                }

                // Ajouter cette boîte de dialogue
                if (showPerformanceDialog) {
                    AlertDialog(
                        onDismissRequest = { showPerformanceDialog = false },
                        title = { Text("Performance sauvegardée !") },
                        text = {
                            Text("Sauvegarde réussie")
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    showPerformanceDialog = false
                                    resetTrigger++ // Réinitialiser après confirmation
                                }
                            ) {
                                Text("OK")
                            }
                        }
                    )
                }

                // Modifier le bouton Réinitialiser
                Button(
                    onClick = {
                        resetTrigger++ // Incrémenter pour déclencher la réinitialisation
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Réinitialiser")
                }
            }

            if (currentObstacleIndex >= obstacles.size) {
                Text(
                    text = "Course terminée !",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}
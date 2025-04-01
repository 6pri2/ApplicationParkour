package iut.gon.applicationparkour.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
import iut.gon.applicationparkour.ui.components.courses.CourseItem
import iut.gon.applicationparkour.data.api.ApiClient
import iut.gon.applicationparkour.data.model.Courses
import iut.gon.applicationparkour.ui.components.scaffold.ScreenScaffold
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun CompetitionArbitrageScreen(navController: NavController, competitionId: String) {
    val token = "Bearer 1ofD5tbAoC0Xd0TCMcQG3U214MqUo7JzUWrQFWt1ugPuiiDmwQCImm9Giw7fwR0Y"
    var courses by remember { mutableStateOf<List<Courses>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var showSuccessMessage by remember { mutableStateOf<String?>(null) }
    var showErrorMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    // Fonction pour charger les courses
    fun loadCourses() {
        scope.launch {
            isLoading = true
            try {
                courses = ApiClient.apiService.getCompetitionCourses(token, competitionId.toInt())
                isLoading = false
            } catch (e: Exception) {
                isLoading = false
                error = "Erreur de connexion: ${e.message}"
                showErrorMessage = error
            }
        }
    }

    // Chargement initial
    LaunchedEffect(competitionId) {
        loadCourses()
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

    ScreenScaffold(
        title = "Arbitrage de la compétition",
        navController = navController
    ) {
        Box(modifier = Modifier.Companion.fillMaxSize()) {
            Column(modifier = Modifier.Companion.fillMaxSize()) {
                when {
                    isLoading && courses.isEmpty() -> {
                        Box(
                            modifier = Modifier.Companion.fillMaxSize(),
                            contentAlignment = Alignment.Companion.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    error != null -> {
                        Box(
                            modifier = Modifier.Companion.fillMaxSize(),
                            contentAlignment = Alignment.Companion.Center
                        ) {
                            Text(
                                text = error!!,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    courses.isEmpty() -> {
                        Box(
                            modifier = Modifier.Companion.fillMaxSize(),
                            contentAlignment = Alignment.Companion.Center
                        ) {
                            Text("Aucun parcours trouvé pour cette compétition")
                        }
                    }

                    else -> {
                        LazyColumn(modifier = Modifier.Companion.weight(1f)) {
                            items(courses) { course ->
                                CourseItem(
                                    course = course,
                                    competitionId = competitionId,
                                    navController = navController
                                )
                            }
                        }
                    }
                }
            }

            // Indicateur de chargement pour les opérations
            if (isLoading && courses.isNotEmpty()) {
                Box(
                    modifier = Modifier.Companion
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Companion.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            // Messages toast
            Box(
                modifier = Modifier.Companion
                    .align(Alignment.Companion.BottomCenter)
                    .padding(bottom = 16.dp)
            ) {
                showSuccessMessage?.let { message ->
                    Snackbar(
                        modifier = Modifier.Companion.padding(16.dp),
                        action = {
                            IconButton(onClick = { showSuccessMessage = null }) {
                                Icon(Icons.Default.Close, "Fermer")
                            }
                        }
                    ) {
                        Text(
                            text = message,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                showErrorMessage?.let { message ->
                    Snackbar(
                        modifier = Modifier.Companion.padding(16.dp),
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        action = {
                            IconButton(onClick = { showErrorMessage = null }) {
                                Icon(Icons.Default.Close, "Fermer")
                            }
                        }
                    ) {
                        Text(
                            text = message,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
    }
}
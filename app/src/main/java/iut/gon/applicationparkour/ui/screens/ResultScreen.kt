package iut.gon.applicationparkour.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import iut.gon.applicationparkour.ui.components.scaffold.ScreenScaffold
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import iut.gon.applicationparkour.data.api.ApiClient
import iut.gon.applicationparkour.data.model.Competition
import iut.gon.applicationparkour.data.model.Competitor
import iut.gon.applicationparkour.data.model.Courses
import iut.gon.applicationparkour.data.model.Performance
import iut.gon.applicationparkour.ui.components.classement.SuccessContent
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

/**
 * page d'affichage des résultat d'un parcours
 */


@Composable
fun ResultScreen(navController: NavController, competitionId: String, courseId: String) {
    val token = "Bearer 1ofD5tbAoC0Xd0TCMcQG3U214MqUo7JzUWrQFWt1ugPuiiDmwQCImm9Giw7fwR0Y" // Remplacez par votre vrai token
    val scope = rememberCoroutineScope()

    var competition by remember { mutableStateOf<Competition?>(null) }
    var course by remember { mutableStateOf<Courses?>(null) }
    var competitors by remember { mutableStateOf<Map<Int, Competitor>?>(null) }
    var performances by remember { mutableStateOf<List<Performance>?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val deferredCompetition = async {
                    ApiClient.apiService.getCompetitionDetails(competitionId.toInt())
                }

                val deferredCourse = async {
                    ApiClient.apiService.getCourseById(courseId.toInt())
                }

                val deferredCompetitors = async {
                    ApiClient.apiService.getAllCompetitors(token)
                        .associateBy { it.id }
                }

                val deferredPerformances = async {
                    ApiClient.apiService.getPerformances(token)
                        .filter { it.courseId == courseId.toInt() }
                }

                competition = deferredCompetition.await()
                course = deferredCourse.await()
                competitors = deferredCompetitors.await()
                performances = deferredPerformances.await()
                loading = false

            } catch (e: Exception) {
                error = "Erreur de chargement: ${e.message}"
                loading = false
            }
        }
    }

    ScreenScaffold(
        title = "Résultats du parcours",
        navController = navController
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            when {
                loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))

                error != null -> Text(
                    text = error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )

                else -> SuccessContent(
                    competition = competition!!,
                    course = course!!,
                    competitors = competitors!!,
                    performances = performances!!,
                    onCompetitorClick = { perf, rank ->
                        navController.navigate("competitorDetails/${competitionId}/${courseId}/${perf.competitorId}/$rank/${perf.id}")
                    }
                )
            }
        }
    }
}



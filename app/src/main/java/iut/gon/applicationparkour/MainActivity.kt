package iut.gon.applicationparkour

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import iut.gon.applicationparkour.ui.theme.ApplicationParkourTheme
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.Period
import androidx.compose.material.icons.filled.Remove
import iut.gon.applicationparkour.data.api.ApiClient
import iut.gon.applicationparkour.data.model.*
import iut.gon.applicationparkour.ui.app.ParkourApp
import iut.gon.applicationparkour.ui.components.courses.CourseAddDialog
import iut.gon.applicationparkour.ui.components.courses.CourseEditDialog
import iut.gon.applicationparkour.ui.components.courses.CourseItemModif
import iut.gon.applicationparkour.ui.components.scaffold.ScreenScaffold

// --- Activité principale ---

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ApplicationParkourTheme {
                ParkourApp()
            }
        }
    }
}


@Composable
fun CompetitionCompetitorsScreen(navController: NavController, competitionId: Int) {
    val token = "Bearer 1ofD5tbAoC0Xd0TCMcQG3U214MqUo7JzUWrQFWt1ugPuiiDmwQCImm9Giw7fwR0Y"
    var competitors by remember { mutableStateOf<List<Competitor>?>(null) }
    var allCompetitors by remember { mutableStateOf<List<Competitor>?>(null) }
    var competition by remember { mutableStateOf<Competition?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var competitorToDelete by remember { mutableStateOf<Competitor?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedCompetitors by remember { mutableStateOf<Set<Int>>(emptySet()) }
    val coroutineScope = rememberCoroutineScope()


    LaunchedEffect(competitionId) {
        try {
            val compId = competitionId
            competition = ApiClient.apiService.getCompetitionDetails(token, compId)
            competitors = ApiClient.apiService.getCompetitorsByCompetition(token, compId)
            allCompetitors = ApiClient.apiService.getAllCompetitors(token)
            isLoading = false
        } catch (e: Exception) {
            error = "Erreur lors du chargement: ${e.localizedMessage}"
            isLoading = false
        }
    }
    val eligibleCompetitors = allCompetitors?.filter { competitor ->
        val age = calculateAge(competitor.born_at)
        val genderMatch = competition?.gender == "Tous" || competitor.gender == competition?.gender
        val ageMatch = age in (competition?.age_min ?: 0)..(competition?.age_max ?: 100)
        val notRegistered = competitors?.none { it.id == competitor.id } ?: true

        genderMatch && ageMatch && notRegistered
    }
    if (competitorToDelete != null) {
        val competitor = competitorToDelete!!
        AlertDialog(
            onDismissRequest = { competitorToDelete = null },
            title = { Text("Confirmer la retrait du compétiteur dans la compétition ?") },
            text = { Text("Voulez-vous vraiment supprimer ${competitor.first_name} ${competitor.last_name} ?") },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            try {
                                ApiClient.apiService.removeCompetitorFromCompetition(
                                    token,
                                    competitionId,
                                    competitor.id
                                )
                                competitors = competitors?.filter { it.id != competitor.id }
                            } catch (e: Exception) {
                                error = "Erreur lors de la suppression: ${e.localizedMessage}"
                            } finally {
                                competitorToDelete = null
                            }
                        }
                    }
                ) {
                    Text("Supprimer")
                }
            },
            dismissButton = {
                Button(onClick = { competitorToDelete = null }) {
                    Text("Annuler")
                }
            }
        )
    }
    if (showAddDialog && eligibleCompetitors != null) {
        AlertDialog(
            onDismissRequest = {
                selectedCompetitors = emptySet()
                showAddDialog = false
            },
            title = { Text("Ajouter des compétiteurs") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    eligibleCompetitors.forEach { competitor ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().padding(4.dp)
                        ) {
                            Checkbox(
                                checked = selectedCompetitors.contains(competitor.id),
                                onCheckedChange = { checked ->
                                    selectedCompetitors = if (checked) {
                                        selectedCompetitors + competitor.id
                                    } else {
                                        selectedCompetitors - competitor.id
                                    }
                                }
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text("${competitor.last_name} ${competitor.first_name}")
                                Text(
                                    "Âge: ${calculateAge(competitor.born_at)} ans",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            try {
                                val newCompetitors = mutableListOf<Competitor>()

                                selectedCompetitors.forEach { competitorId ->
                                    val response = ApiClient.apiService.addCompetitorToCompetition(
                                        token,
                                        competitionId,
                                        AddCompetitorRequest(competitorId)
                                    )

                                    if (response.isSuccessful) {
                                        allCompetitors?.find { it.id == competitorId }?.let { newCompetitor ->
                                            newCompetitors.add(newCompetitor)
                                        }
                                    }
                                }
                                competitors = newCompetitors + (competitors ?: emptyList())

                                selectedCompetitors = emptySet()
                                showAddDialog = false
                            } catch (e: Exception) {
                                error = "Erreur critique: ${e.localizedMessage}"
                            }
                        }
                    },
                    enabled = selectedCompetitors.isNotEmpty()
                ) {
                    Text("Ajouter (${selectedCompetitors.size})")
                }
            },
            dismissButton = {
                Button(onClick = {
                    selectedCompetitors = emptySet()
                    showAddDialog = false
                }) {
                    Text("Annuler")
                }
            }
        )
    }

    ScreenScaffold(
        title = "Compétiteurs de la compétition",
        navController = navController
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading -> CircularProgressIndicator()
                error != null -> Text(
                    text = error!!,
                    color = MaterialTheme.colorScheme.error
                )

                competitors.isNullOrEmpty() -> Text("Aucun compétiteur trouvé")
                else -> {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        LazyColumn(
                            modifier = Modifier.weight(1f)
                        ) {
                            items(competitors!!) { competitor ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    elevation = CardDefaults.cardElevation(4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .weight(1f)
                                                .padding(16.dp)
                                        ) {
                                            Text("Nom: ${competitor.last_name}")
                                            Text("Prénom: ${competitor.first_name}")
                                            Text("Genre: ${if (competitor.gender == "H") "Homme" else "Femme"}")
                                        }

                                        IconButton(
                                            onClick = { competitorToDelete = competitor }
                                        ) {
                                            Icon(
                                                Icons.Default.Remove,
                                                contentDescription = "Retirer de la compétition",
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Button(
                            onClick = { showAddDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            enabled = !isLoading
                        ) {
                            Text("Ajouter un compétiteur")
                        }
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompetitionCoursesScreen(navController: NavController, competitionId: String, onFinalSave: () -> Unit) {
    val token = "Bearer 1ofD5tbAoC0Xd0TCMcQG3U214MqUo7JzUWrQFWt1ugPuiiDmwQCImm9Giw7fwR0Y"
    var courses by remember { mutableStateOf<List<Courses>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var courseToEdit by remember { mutableStateOf<Courses?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var courseToDelete by remember { mutableStateOf<Courses?>(null) }
    var tempPositions by remember { mutableStateOf<Map<Int, Int>>(emptyMap()) }

    val scope = rememberCoroutineScope()

    // Fonction pour charger les parcours
    fun loadCourses() {
        scope.launch {
            isLoading = true
            try {
                courses = ApiClient.apiService.getCompetitionCourses(token, competitionId.toInt())
                    .sortedBy { it.position }
                isLoading = false
            } catch (e: Exception) {
                error = "Erreur de chargement: ${e.message}"
                isLoading = false
            }
        }
    }

    // Chargement initial
    LaunchedEffect(competitionId) {
        loadCourses()
    }

    // Fonction pour mettre à jour un parcours
    fun updateCourse(course: Courses) {
        scope.launch {
            try {
                val updatedCourse = CourseUpdateRequest(
                    name = course.name,
                    max_duration = course.max_duration,
                    position = course.position,
                    is_over = course.is_over
                )
                // Note: Tu devras ajouter cette méthode dans ton interface ApiService
                ApiClient.apiService.updateCourse(token, course.id, updatedCourse)
                loadCourses()
            } catch (e: Exception) {
                error = "Erreur de mise à jour: ${e.message}"
            }
        }
    }

    // Fonction pour supprimer un parcours
    fun deleteCourse(courseId: Int) {
        scope.launch {
            try {
                ApiClient.apiService.deleteCourse(token, courseId)
                loadCourses()
            } catch (e: Exception) {
                error = "Erreur de suppression: ${e.message}"
            }
        }
    }
    // Fonction pour changer la position d'un parcours
    fun moveCourseUp(course: Courses) {
        val currentIndex = courses.indexOfFirst { it.id == course.id }
        if (currentIndex > 0) {
            val newPosition = courses[currentIndex - 1].position
            val temp = courses.toMutableList()
            temp[currentIndex] = temp[currentIndex].copy(position = newPosition)
            temp[currentIndex - 1] = temp[currentIndex - 1].copy(position = course.position)
            courses = temp.sortedBy { it.position }
            tempPositions = tempPositions + mapOf(
                temp[currentIndex].id to newPosition,
                temp[currentIndex - 1].id to course.position
            )
        }
    }

    fun moveCourseDown(course: Courses) {
        val currentIndex = courses.indexOfFirst { it.id == course.id }
        if (currentIndex < courses.size - 1) {
            val newPosition = courses[currentIndex + 1].position
            val temp = courses.toMutableList()
            temp[currentIndex] = temp[currentIndex].copy(position = newPosition)
            temp[currentIndex + 1] = temp[currentIndex + 1].copy(position = course.position)
            courses = temp.sortedBy { it.position }
            tempPositions = tempPositions + mapOf(
                temp[currentIndex].id to newPosition,
                temp[currentIndex + 1].id to course.position
            )
        }
    }

    // Fonction pour sauvegarder les nouvelles positions
    fun savePositions() {
        if(courses.isEmpty()){
            error = "Vous devez ajouter au moins un parcours."
            return
        }
        scope.launch {
            try {
                tempPositions.forEach { (courseId, newPosition) ->
                    val course = courses.find { it.id == courseId }!!
                    val updatedCourse = CourseUpdateRequest(
                        name = course.name,
                        max_duration = course.max_duration,
                        position = newPosition,
                        is_over = course.is_over
                    )
                    ApiClient.apiService.updateCourse(token, courseId, updatedCourse)
                }
                tempPositions = emptyMap()
                loadCourses()
            } catch (e: Exception) {
                error = "Erreur lors de la sauvegarde des positions: ${e.message}"
            }
        }
    }

    // Dialogue d'édition
    if (showEditDialog && courseToEdit != null) {
        val currentCourse = courseToEdit!!
        CourseEditDialog(
            course = currentCourse,
            onDismiss = { showEditDialog = false },
            onSave = { updatedCourse ->
                updateCourse(updatedCourse)
                showEditDialog = false
            },
            onManageObstacles = {
                navController.navigate("courseObstacles/${currentCourse.id}") {
                    launchSingleTop = true
                }
                showEditDialog = false // Fermer le dialogue
            }
        )
    }

    // Dialogue d'ajout
    if (showAddDialog) {
        CourseAddDialog(
            competitionId = competitionId,
            onDismiss = { showAddDialog = false },
            onSave = { newCourse ->
                scope.launch {
                    try {
                        // 1. Créer le parcours
                        val createdCourse = ApiClient.apiService.addCourse(
                            token,
                            CreateCourseRequest(
                                name = newCourse.name,
                                max_duration = newCourse.max_duration,
                                competitionId = competitionId.toInt()
                            )
                        )

                        // 2. Rediriger vers la gestion des obstacles
                        navController.navigate("courseObstacles/${createdCourse.id}") {
                            launchSingleTop = true
                        }

                        // 3. Fermer le dialogue et recharger la liste
                        showAddDialog = false
                        loadCourses()
                    } catch (e: Exception) {
                        error = "Erreur lors de l'ajout: ${e.message}"
                    }
                }
            },
            defaultPosition = courses.maxOfOrNull { it.position }?.plus(1) ?: 1
        )
    }

    // Dialogue de suppression
    if (showDeleteDialog && courseToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Confirmer la suppression") },
            text = {
                if (courses.size <= 1) {
                    Text("Vous devez conserver au moins un parcours")
                } else {
                    Text("Voulez-vous vraiment supprimer le parcours ${courseToDelete?.name} ?")
                }
            },confirmButton = {
                Button(
                    onClick = {
                        if (courses.size > 1) {
                            courseToDelete?.id?.let { deleteCourse(it) }
                            showDeleteDialog = false
                        }
                    },
                    enabled = courses.size > 1
                ) {
                    Text("Supprimer")
                }
            },
            dismissButton = {
                Button(onClick = { showDeleteDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    ScreenScaffold(
        title = "Parcours de la compétition",
        navController = navController
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {

            Column(modifier = Modifier.fillMaxSize()) {
                // Liste des parcours
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(courses, key = { it.id }) { course ->
                        CourseItemModif(
                            course = course,
                            onEdit = { courseToEdit = course; showEditDialog = true },
                            onDelete = {
                                if (courses.size > 1) {
                                    courseToDelete = course;
                                    showDeleteDialog = true
                                } else {
                                    error = "Vous devez conserver au moins un parcours"
                                }
                            },
                            onMoveUp = { moveCourseUp(course) },
                            onMoveDown = { moveCourseDown(course) },
                            isFirst = courses.firstOrNull()?.id == course.id,
                            isLast = courses.lastOrNull()?.id == course.id,
                            isOnlyCourse = courses.size == 1
                        )
                    }
                }

                // Bouton Ajouter un parcours (toujours visible)
                Button(
                    onClick = { showAddDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text("Ajouter un parcours")
                }

                // Bouton Valider la compétition
                Button(
                    onClick = {
                        if (courses.isEmpty()) {
                            error = "Vous devez ajouter au moins un parcours avant de valider"
                        } else {
                            scope.launch {
                                if (tempPositions.isNotEmpty()) {
                                    savePositions()
                                }
                                onFinalSave()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    enabled = courses.isNotEmpty()
                ) {
                    Text("Valider la compétition")
                }
            }

            // Gestion des états de chargement/erreur
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.7f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                error != null -> {
                    Snackbar(
                        modifier = Modifier.padding(16.dp),
                        action = {
                            Button(onClick = { error = null }) {
                                Text("OK")
                            }
                        }
                    ) {
                        Text(error!!)
                    }
                }
            }
        }
    }
}


fun calculateAge(bornAt: String): Int {
    // Convertir la date de naissance en LocalDate
    val birthDate = LocalDate.parse(bornAt)
    val currentDate = LocalDate.now()

    // Calculer l'âge en années
    val age = Period.between(birthDate, currentDate).years
    return age
}


@Preview(showBackground = true)
@Composable
fun PreviewApp() {
    ApplicationParkourTheme {
        ParkourApp()
    }
}

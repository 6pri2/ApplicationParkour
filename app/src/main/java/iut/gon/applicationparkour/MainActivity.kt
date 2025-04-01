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
import androidx.compose.material.icons.filled.Add
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
import iut.gon.applicationparkour.ui.components.obstacle.ObstacleItem
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
                val updatedObstacles = ApiClient.apiService.getCourseObstacles(token, courseId)
                val updatedUnused = ApiClient.apiService.getUnusedObstacles(token, courseId)
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
                        position = currentIndex  // Essayez avec et sans +1
                    )
                    println("DEBUG - Sending request: $request")
                    // Envoyer la nouvelle position (currentIndex car les indices commencent à 0)
                    val response = ApiClient.apiService.updateObstaclePosition(
                        token,
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
                        token,
                        courseId,
                        UpdateObstaclePositionRequest(
                            obstacleId = obstacle.obstacle_id,
                            position = currentIndex + 1
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
                onDismissRequest = { showAddObstacleDialog = false
                                   obstacleName=""},
                title = { Text("Créer un nouvel obstacle") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = obstacleName,
                            onValueChange = { obstacleName = it },
                            label = { Text("Nom de l'obstacle") },
                            modifier = Modifier.fillMaxWidth()
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
                                        token,
                                        Obstacles(id = 0, name = obstacleName)
                                    )
                                    // Ajouter l'obstacle au parcours
                                    ApiClient.apiService.addObstacleToCourse(
                                        token,
                                        courseId,
                                        AddObstacleRequest(newObstacle.id)
                                    )
                                    loadObstacles()
                                    showAddObstacleDialog = false
                                    obstacleName=""
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
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Liste des obstacles du parcours
                LazyColumn(modifier = Modifier.weight(2f)) {
                    items(courseObstacles, key = { it.obstacle_id }) { obstacle ->
                        ObstacleItem(
                            obstacle = obstacle,
                            onMoveUp = { moveObstacleUp(obstacle) },
                            onMoveDown = { moveObstacleDown(obstacle) },
                            onDelete = {
                                scope.launch {
                                    try {
                                        ApiClient.apiService.removeObstacleFromCourse(
                                            token,
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
                Text("Obstacles disponibles:", modifier = Modifier.padding(8.dp))
                LazyColumn(modifier = Modifier.weight(1f)) {
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
                                            token,
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Créer un nouvel obstacle")
                }
                Button(
                    onClick = {
                        scope.launch {
                            onFinalSave()
                        }
                    },
                    modifier = Modifier
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
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.7f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                errorMessage != null -> {
                    Snackbar(
                        modifier = Modifier.padding(16.dp),
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

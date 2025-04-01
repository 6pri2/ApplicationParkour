package com.example.applicationparkour

import android.graphics.Picture
import android.os.Bundle
import android.os.Debug
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.applicationparkour.ui.theme.ApplicationParkourTheme
import kotlinx.coroutines.launch
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import java.time.LocalDate
import java.time.Period
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import androidx.compose.material.icons.filled.Sports
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Remove
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.zIndex
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.coroutineScope


// --- Configuration Retrofit ---

// Data class pour représenter une compétition
data class Competition(
    val id: Int,
    val name: String,
    val age_min: Int,
    val age_max: Int,
    val gender: String,
    val has_retry: Int,
    val status: String
)

data class Competitor(
    val id: Int,
    val first_name: String,
    val last_name: String,
    val email: String,
    val gender: String,
    val phone: String,
    val born_at: String // Format "yyyy-MM-dd"
)

data class Courses(
    val id: Int,
    val name: String,
    val max_duration: Int,
    val position: Int,
    val is_over: Int,
    val competition_id: Int
)

data class Obstacles(
    val id: Int,
    val name: String,
)

// Interface de l'API
interface ApiService {
    @GET("competitions")
    suspend fun getCompetitions(@Header("Authorization") token: String): List<Competition>

    @GET("competitors")
    suspend fun getCompetitors(@Header("Authorization") token: String): List<Competitor>

    @GET("courses")
    suspend fun getCourses(@Header("Authorization") token: String): List<Courses>

    @GET("obstacles")
    suspend fun getObstacles(@Header("Authorization") token: String): List<Obstacles>

    @POST("obstacles")
    suspend fun addObstacles(
        @Header("Authorization") token: String,
        @Body obstacles: Obstacles
    ): Obstacles

    @POST("competitors")
    suspend fun addCompetitor(
        @Header("Authorization") token: String,
        @Body competitor: Competitor
    ): Competitor

    @DELETE("competitors/{id}")
    suspend fun deleteCompetitor(
        @Header("Authorization") token: String,
        @Path("id") competitorId: Int
    ): Response<Unit>

    @DELETE("obstacles/{id}")
    suspend fun deleteObstacles(
        @Header("Authorization") token: String,
        @Path("id") obstaclesId: Int
    ): Response<Unit>

    @PUT("competitors/{id}")
    suspend fun updateCompetitor(
        @Header("Authorization") token: String,
        @Path("id") competitorId: Int,
        @Body competitor: Competitor
    ): Competitor

    @PUT("obstacles/{id}")
    suspend fun updateObstacles(
        @Header("Authorization") token: String,
        @Path("id") obstaclesId: Int,
        @Body obstacles: Obstacles
    ): Obstacles

    @POST("competitions")
    suspend fun addCompetition(
        @Header("Authorization") token: String,
        @Body competition: Competition
    ): Competition

    @PUT("competitions/{id}")
    suspend fun updateCompetition(
        @Header("Authorization") token: String,
        @Path("id") competitionId: Int,
        @Body competition: Competition
    ): Competition

    @DELETE("competitions/{id}")
    suspend fun deleteCompetition(
        @Header("Authorization") token: String,
        @Path("id") competitionId: Int
    ): Response<Unit>

    @GET("competitions/{id}/inscriptions")
    suspend fun getCompetitorsByCompetition(
        @Header("Authorization") token: String,
        @Path("id") competitionId: Int
    ): List<Competitor>

        @DELETE("competitions/{id}/remove_competitor/{id_competitor}")
        suspend fun removeCompetitorFromCompetition(
            @Header("Authorization") token: String,
            @Path("id") competitionId: Int,
            @Path("id_competitor") competitorId: Int
        ): Response<Unit>

    @POST("competitions/{id}/add_competitor")
    suspend fun addCompetitorToCompetition(
        @Header("Authorization") token: String,
        @Path("id") competitionId: Int,
        @Body competitorId: AddCompetitorRequest
    ): Response<Unit>

    @GET("competitions/{id}")
    suspend fun getCompetitionDetails(
        @Header("Authorization") token: String,
        @Path("id") competitionId: Int
    ): Competition

    @GET("competitors")
    suspend fun getAllCompetitors(
        @Header("Authorization") token: String
    ): List<Competitor>

    @GET("competitions/{id}/courses")
    suspend fun getCompetitionCourses(
        @Header("Authorization") token: String,
        @Path("id") competitionId: Int
    ): List<Courses>

    // Ajoute ces méthodes dans ton interface ApiService
    @PUT("courses/{id}")
    suspend fun updateCourse(
        @Header("Authorization") token: String,
        @Path("id") courseId: Int,
        @Body course: CourseUpdateRequest
    ): Courses

    @POST("courses")
    suspend fun addCourse(
        @Header("Authorization") token: String,
        @Body course: CreateCourseRequest
    ): Courses

    @DELETE("courses/{id}")
    suspend fun deleteCourse(
        @Header("Authorization") token: String,
        @Path("id") courseId: Int
    ): Response<Unit>

    @GET("courses/{id}/obstacles")
    suspend fun getCourseObstacles(
        @Header("Authorization") token: String,
        @Path("id") courseId: Int
    ): List<ObstacleCourse>

    @GET("courses/{id}/unused_obstacles")
    suspend fun getUnusedObstacles(
        @Header("Authorization") token: String,
        @Path("id") courseId: Int
    ): List<Obstacles>

    @POST("courses/{courseId}/add_obstacle")
    suspend fun addObstacleToCourse(
        @Header("Authorization") token: String,
        @Path("courseId") courseId: Int,
        @Body request: AddObstacleRequest
    ): Response<Unit>

    @DELETE("courses/{courseId}/remove_obstacle/{obstacleId}")
    suspend fun removeObstacleFromCourse(
        @Header("Authorization") token: String,
        @Path("courseId") courseId: Int,
        @Path("obstacleId") obstacleId: Int
    ): Response<Unit>

    @POST("courses/{courseId}/update_obstacle_position")
    suspend fun updateObstaclePosition(
        @Header("Authorization") token: String,
        @Path("courseId") courseId: Int,
        @Body request: UpdateObstaclePositionRequest
    ): Response<Unit>

    @PUT("courses/{courseId}/update_obstacle_position")
    suspend fun updateSingleObstaclePosition(
        @Header("Authorization") token: String,
        @Path("courseId") courseId: Int,
        @Body request: ObstaclePositionUpdate
    ): Response<Unit>

}
data class AddCompetitorRequest(
    @SerializedName("competitor_id")
    val competitorId: Int
)

data class CourseUpdateRequest(
    val name: String,
    val max_duration: Int,
    val position: Int,
    val is_over: Int
)

data class CreateCourseRequest(
    val name: String,
    val max_duration: Int,
    @SerializedName("competition_id")
    val competitionId: Int
)

data class ObstacleCourse(
    val obstacle_id: Int,
    val obstacle_name: String,
    val duration: Int, // À confirmer selon l'API
    val position: Int
)

data class AddObstacleRequest(
    @SerializedName("obstacle_id")
    val obstacleId: Int
)

data class ObstaclePositionUpdate(
    @SerializedName("obstacle_id")
    val obstacleId: Int,
    @SerializedName("position")
    val position: Int
)

// Data class pour la requête
data class UpdateObstaclePositionRequest(
    @SerializedName("obstacle_id")
    val obstacleId: Int,
    @SerializedName("position")
    val position: Int
)


// Objet singleton pour créer l'instance Retrofit
object ApiClient {
    private const val BASE_URL = "http://92.222.217.100/api/"

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}

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
fun ParkourApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "welcome"
    ) {
        composable("welcome") {
            WelcomeScreen(navController)
        }
        composable("competitors") {
            CompetitorScreen(navController)
        }
        composable("competitions") {
            CompetitionScreen(navController)
        }
        composable("courses") {
            CoursesScreen(navController)
        }
        composable("obstacles") {
            ObstaclesScreen(navController)
        }
        composable("arbitrage") {
            ArbitragesScreen(navController)
        }
        composable("competitors/{competitionId}") { backStackEntry ->
            val competitionId = backStackEntry.arguments?.getString("competitionId") ?: "0"
            CompetitionCompetitorsScreen(navController, competitionId.toInt())
        }

        composable("results/{competitionId}") { backStackEntry ->
            val competitionId = backStackEntry.arguments?.getString("competitionId") ?: "0"
            CompetitionResultsScreen(navController, competitionId)
        }

        composable("arbitrage/{competitionId}") { backStackEntry ->
            val competitionId = backStackEntry.arguments?.getString("competitionId") ?: "0"
            CompetitionArbitrageScreen(navController, competitionId)
        }

        composable("competitionArbitrage/{competitionId}") { backStackEntry ->
            val competitionId = backStackEntry.arguments?.getString("competitionId") ?: ""
            CompetitionArbitrageScreen(navController, competitionId)
        }
        composable("resultScreen/{competitionId}/{courseId}") { backStackEntry ->
            val competitionId = backStackEntry.arguments?.getString("competitionId") ?: ""
            val courseId = backStackEntry.arguments?.getString("courseId") ?: ""
            ResultScreen(navController, competitionId, courseId)
        }
        composable("arbitrageScreen/{competitionId}/{courseId}") { backStackEntry ->
            val competitionId = backStackEntry.arguments?.getString("competitionId") ?: ""
            val courseId = backStackEntry.arguments?.getString("courseId") ?: ""
            ArbitrageScreen(navController, competitionId, courseId)
        }

        composable("competitionCourses/{competitionId}") { backStackEntry ->
            val competitionId = backStackEntry.arguments?.getString("competitionId") ?: "0"
            CompetitionCoursesScreen(navController, competitionId, onFinalSave = {
                navController.popBackStack()
            })
        }

        composable("courseObstacles/{courseId}") { backStackEntry ->
            val courseId = backStackEntry.arguments?.getString("courseId")?.toIntOrNull() ?: 0
            CourseObstaclesScreen(navController, courseId)
        }
    }
}

@Composable
fun WelcomeScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Application Parkour",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 48.dp)
        )

        val buttonModifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .height(60.dp)

        Button(
            onClick = { navController.navigate("competitors") },
            modifier = buttonModifier
        ) {
            Text("Compétiteurs")
        }

        Button(
            onClick = { navController.navigate("competitions") },
            modifier = buttonModifier
        ) {
            Text("Compétitions")
        }

        Button(
            onClick = {navController.navigate("obstacles")},
            modifier = buttonModifier
        ){
            Text("Obstacles")
        }

        // Ajoutez les autres boutons de la même manière
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenScaffold(
    title: String,
    navController: NavController,
    content: @Composable () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            content()
        }
    }
}

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
                    val createdCompetition = ApiClient.apiService.addCompetition(token, baseCompetition)
                    showSuccessMessage = "Compétition créée"
                    navController.navigate("competitionCourses/${createdCompetition.id}"){
                        popUpTo("competitions") {inclusive = false}
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

        Box(modifier = Modifier
            .fillMaxSize()) {

            when {
                isLoading && competitions == null -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                competitions.isNullOrEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Aucune compétition trouvée")
                            Spacer(Modifier.height(16.dp))
                            Button(
                                onClick = { showAddDialog = true },
                                modifier = Modifier.padding(horizontal = 32.dp)
                            ) {
                                Text("Créer une compétition")
                            }
                        }
                    }
                }
                else -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        LazyColumn(modifier = Modifier.weight(1f)) {
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
                            modifier = Modifier
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
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            Box(Modifier.align(Alignment.BottomCenter)) {
                showSuccessMessage?.let { message ->
                    Snackbar(
                        modifier = Modifier.padding(16.dp),
                        action = { IconButton({ showSuccessMessage = null }) { Icon(Icons.Default.Close, "Fermer") } }
                    ) { Text(message, color = MaterialTheme.colorScheme.onPrimaryContainer) }
                }
                showErrorMessage?.let { message ->
                    Snackbar(
                        modifier = Modifier.padding(16.dp),
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        action = { IconButton({ showErrorMessage = null }) { Icon(Icons.Default.Close, "Fermer") } }
                    ) { Text(message, color = MaterialTheme.colorScheme.onErrorContainer) }
                }
            }
        }
    }
}


@Composable
fun CompetitionItem(
    competition: Competition,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onCompetitors: () -> Unit,
    onResults: () -> Unit,
    onArbitrage: () -> Unit,
    onCourses: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Nom: ${competition.name}",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(text = "Âge: ${competition.age_min} - ${competition.age_max} ans")
            Text(text = "Genre: ${if (competition.gender == "H") "Homme" else "Femme"}")
            Text(text = "Retry: ${if (competition.has_retry == 1) "Oui" else "Non"}")
            Text(text = "Statut: ${competition.status}")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, "Modifier")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, "Supprimer")
                }
                IconButton(onClick = onCompetitors) {
                    Icon(Icons.Default.Person, "Compétiteurs")
                }
                IconButton(onClick = onResults) {
                    Icon(Icons.Default.EmojiEvents, contentDescription = "Résultats")
                }
                IconButton(onClick = onArbitrage) {
                    Icon(Icons.Default.Sports, "Arbitrage")
                }
            }
        }
    }
}

@Composable
fun DeleteCompetitionDialog(
    competition: Competition,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirmer la suppression") },
        text = { Text("Voulez-vous vraiment supprimer ${competition.name} ?") },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Supprimer")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}


@Composable
fun CompetitionEditDialog(
    competition: Competition?,
    navController: NavController,
    onDismiss: () -> Unit,
    onSave: (Competition) -> Unit,
    onManageCourses: () -> Unit
) {
    var name by remember { mutableStateOf(competition?.name ?: "") }
    var ageMin by remember { mutableStateOf(competition?.age_min?.toString() ?: "") }
    var ageMax by remember { mutableStateOf(competition?.age_max?.toString() ?: "") }
    var gender by remember { mutableStateOf(competition?.gender ?: "H") }
    var hasRetry by remember { mutableStateOf(competition?.has_retry == 1) }
    var status by remember { mutableStateOf(competition?.status ?: "pending") }
    val token = "Bearer 1ofD5tbAoC0Xd0TCMcQG3U214MqUo7JzUWrQFWt1ugPuiiDmwQCImm9Giw7fwR0Y"
    val scope = rememberCoroutineScope()
    // Validation
    val ageMinError = remember(ageMin) {
        ageMin.toIntOrNull()?.takeIf { it <= 0 }?.let { "L'âge minimum doit être positif" }
    }
    val ageMaxError = remember(ageMax, ageMin) {
        when {
            ageMax.toIntOrNull() == null -> "Veuillez entrer un nombre valide"
            ageMax.toInt() <= 0 -> "L'âge maximum doit être positif"
            ageMin.toIntOrNull()?.let { min -> ageMax.toInt() < min } == true ->
                "L'âge max doit être ≥ âge min"
            else -> null
        }
    }
    val nameError = remember(name) {
        if (name.isBlank()) "Le nom est obligatoire" else null
    }
    val isValid = ageMinError == null && ageMaxError == null && nameError == null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (competition == null) "Ajouter une compétition" else "Modifier la compétition") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Nom
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nom") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = nameError != null,
                    supportingText = { nameError?.let { Text(it) } }
                )

                // Âges
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = ageMin,
                        onValueChange = { if (it.all { c -> c.isDigit() }) ageMin = it },
                        label = { Text("Âge min") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = ageMinError != null,
                        supportingText = { ageMinError?.let { Text(it) } }
                    )

                    OutlinedTextField(
                        value = ageMax,
                        onValueChange = { if (it.all { c -> c.isDigit() }) ageMax = it },
                        label = { Text("Âge max") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = ageMaxError != null,
                        supportingText = { ageMaxError?.let { Text(it) } }
                    )
                }

                // Genre
                Text("Genre:", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = gender == "H",
                            onClick = { gender = "H" }
                        )
                        Text("Homme", modifier = Modifier.padding(start = 4.dp))
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = gender == "F",
                            onClick = { gender = "F" }
                        )
                        Text("Femme", modifier = Modifier.padding(start = 4.dp))
                    }
                }

                // Recommencer
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = hasRetry,
                        onCheckedChange = { hasRetry = it }
                    )
                    Text("Possibilité de recommencer", modifier = Modifier.padding(start = 4.dp))
                }
            }
        },
        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (competition == null){
                    Column {
                        Button(
                            onClick = {
                                val newCompetition = Competition(
                                    id = 0,
                                    name = name.trim(),
                                    age_min = ageMin.toIntOrNull() ?: 0,
                                    age_max = ageMax.toIntOrNull() ?: 0,
                                    gender = gender,
                                    has_retry = if (hasRetry) 1 else 0,
                                    status = "pending"
                                )
                                scope.launch {
                                    try {
                                        val createdCompetition = ApiClient.apiService.addCompetition(
                                            token,
                                            newCompetition
                                        )
                                        navController.navigate("competitionCourses/${createdCompetition.id}") {
                                            popUpTo("competitions") { inclusive = false }
                                        }
                                    } catch (e: Exception) {
                                        // Gérer l'erreur ici
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = isValid
                        ) {
                            Text("Créer et gérer les parcours")
                        }
                        if (nameError != null || ageMinError != null || ageMaxError != null) {
                            Text(
                                text = "Veuillez corriger les erreurs avant de continuer",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
                else {

                    if (competition != null) {
                        Button(
                            onClick = {
                                onDismiss()
                                onManageCourses()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Sports, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Modifier les parcours")
                        }
                    }

                    Button(
                        onClick = {
                            val updatedCompetition = Competition(
                                id = competition?.id ?: 0,
                                name = name.trim(),
                                age_min = ageMin.toIntOrNull() ?: 0,
                                age_max = ageMax.toIntOrNull() ?: 0,
                                gender = gender,
                                has_retry = if (hasRetry) 1 else 0,
                                status = competition?.status ?: "pending"
                            )

                            // Debug
                            println("Données envoyées: $updatedCompetition")

                            scope.launch {
                                try {
                                    if (competition == null) {
                                        val response = ApiClient.apiService.addCompetition(
                                            token,
                                            updatedCompetition
                                        )
                                        println("Réponse: $response")
                                    } else {
                                        val response = ApiClient.apiService.updateCompetition(
                                            token,
                                            competition.id,
                                            updatedCompetition
                                        )
                                        println("Réponse: $response")
                                    }
                                    onSave(updatedCompetition)
                                } catch (e: retrofit2.HttpException) {
                                    val errorBody = e.response()?.errorBody()?.string()
                                    println("Erreur 422: $errorBody")
                                    // Affichez un message à l'utilisateur
                                } catch (e: Exception) {
                                    println("Autre erreur: ${e.message}")
                                }
                            }
                        },
                        enabled = isValid,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Enregistrer")
                    }

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        Text("Annuler")
                    }
                }
            }
        }
    )
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
                navController.navigate("courseObstacles/${currentCourse.id}"){
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
                        navController.navigate("courseObstacles/${createdCourse.id}"){
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
        Box(modifier = Modifier
            .fillMaxSize()) {

            Column(modifier = Modifier.fillMaxSize()) {
                // Liste des parcours
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(courses, key = { it.id }) { course ->
                        CourseItemModif(
                            course = course,
                            onEdit = { courseToEdit = course; showEditDialog = true },
                            onDelete = {
                                if(courses.size > 1) {
                                    courseToDelete = course;
                                    showDeleteDialog = true
                                }else{
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
                        if(courses.isEmpty()){
                            error = "Vous devez ajouter au moins un parcours avant de valider"
                        }else {
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
fun CourseEditDialog(
    course: Courses,
    onDismiss: () -> Unit,
    onSave: (Courses) -> Unit,
    onManageObstacles : () -> Unit
) {
    var name by remember { mutableStateOf(course.name) }
    var maxDuration by remember { mutableStateOf(course.max_duration.toString()) }
    var isOver by remember { mutableStateOf(course.is_over == 1) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Modifier le parcours") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nom") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = maxDuration,
                    onValueChange = { if (it.all { c -> c.isDigit() }) maxDuration = it },
                    label = { Text("Durée maximale (secondes)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onManageObstacles,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Gérer les Obstacles")
                    Icon(Icons.Default.Settings, contentDescription = null)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isOver,
                        onCheckedChange = { isOver = it }
                    )
                    Text("Parcours terminé")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        course.copy(
                            name = name,
                            max_duration = maxDuration.toIntOrNull() ?: course.max_duration,
                            is_over = if (isOver) 1 else 0
                        )
                    )
                }
            ) {
                Text("Enregistrer")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
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
                            isOnlyObstacle = courseObstacles.size ==1
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


@Composable
fun ObstacleItem(
    obstacle: ObstacleCourse,
    onMoveUp: (() -> Unit)? = null,
    onMoveDown: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    onAdd: (() -> Unit)? = null,
    isFirst: Boolean = false,
    isLast: Boolean = false,
    isOnlyObstacle: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = obstacle.obstacle_name ?: "Nom inconnu",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            if (obstacle.position > 0) {
                Text(text = "Position: ${obstacle.position}")
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Boutons de déplacement (seulement pour les obstacles du parcours)
                if (onMoveUp != null && onMoveDown != null) {
                    Row {
                        IconButton(
                            onClick = onMoveUp,
                            enabled = !isFirst
                        ) {
                            Icon(
                                Icons.Default.ArrowUpward,
                                contentDescription = "Monter",
                                tint = if (isFirst) Color.Gray else MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(
                            onClick = onMoveDown,
                            enabled = !isLast
                        ) {
                            Icon(
                                Icons.Default.ArrowDownward,
                                contentDescription = "Descendre",
                                tint = if (isLast) Color.Gray else MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // Bouton d'action (suppression ou ajout)
                Row {
                    if (onDelete != null) {
                        IconButton(
                            onClick = onDelete ?: {},
                            enabled = !isOnlyObstacle  // Désactiver si c'est le dernier obstacle
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                "Supprimer",
                                tint = if (isOnlyObstacle) Color.Gray else MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    if (onAdd != null) {
                        IconButton(onClick = onAdd) {
                            Icon(
                                Icons.Default.Add,
                                "Ajouter",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun CourseAddDialog(
    competitionId: String,
    onDismiss: () -> Unit,
    onSave: (Courses) -> Unit,
    defaultPosition: Int
) {
    var name by remember { mutableStateOf("") }
    var maxDuration by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ajouter un parcours") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nom") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = maxDuration,
                    onValueChange = { if (it.all { c -> c.isDigit() }) maxDuration = it },
                    label = { Text("Durée maximale (secondes)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newCourse = Courses(
                        id = 0,  // L'ID sera généré par le serveur
                        name = name,
                        max_duration = maxDuration.toIntOrNull() ?: 0,
                        position = defaultPosition,
                        is_over = 0,
                        competition_id = competitionId.toInt()
                    )
                    onSave(newCourse)
                },
                enabled = name.isNotBlank() && maxDuration.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ajouter et gérer les obstacles")
            }
        }

    )
}

@Composable
fun CourseItemModif(
    course: Courses,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    isFirst: Boolean,
    isLast: Boolean,
    isOnlyCourse: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = course.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Durée max: ${course.max_duration} secondes")
            Text(text = "Position: ${course.position}")
            Text(text = "Statut: ${if (course.is_over == 1) "Terminé" else "En cours"}")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Boutons de déplacement
                Row {
                    IconButton(
                        onClick = onMoveUp,
                        enabled = !isFirst
                    ) {
                        Icon(
                            Icons.Default.ArrowUpward,
                            contentDescription = "Monter",
                            tint = if (isFirst) Color.Gray else MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = onMoveDown,
                        enabled = !isLast
                    ) {
                        Icon(
                            Icons.Default.ArrowDownward,
                            contentDescription = "Descendre",
                            tint = if (isLast) Color.Gray else MaterialTheme.colorScheme.primary,
                        )
                    }
                }

                // Boutons d'édition/suppression
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, "Modifier")
                    }
                    IconButton(
                        onClick = onDelete,
                        enabled = !isOnlyCourse
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            "Supprimer",
                            tint = if (isOnlyCourse ) Color.Gray else MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CompetitionResultsScreen(navController: NavController, competitionId: String) {
    ScreenScaffold(
        title = "Résultats de la compétition",
        navController = navController
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Résultats de la compétition: $competitionId")
        }
    }
}

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
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                when {
                    isLoading && courses.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    error != null -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = error!!,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    courses.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Aucun parcours trouvé pour cette compétition")
                        }
                    }
                    else -> {
                        LazyColumn(modifier = Modifier.weight(1f)) {
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
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            // Messages toast
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
            ) {
                showSuccessMessage?.let { message ->
                    Snackbar(
                        modifier = Modifier.padding(16.dp),
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
                        modifier = Modifier.padding(16.dp),
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

@Composable
private fun CourseItem(
    course: Courses,
    competitionId: String,
    navController: NavController
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Nom: ${course.name}",
                style = MaterialTheme.typography.bodyLarge,
            )

            Text(text = "Durée max: ${course.max_duration} sec")
            Text(text = "Position: ${course.position}")
            Text(text = "Statut: ${if (course.is_over == 1) "Terminé" else "En cours"}")
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                IconButton(
                    onClick = {
                        navController.navigate("resultScreen/${competitionId}/${course.id}")
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "Résultats"
                    )
                }
                IconButton(
                    onClick = {
                        navController.navigate("arbitrageScreen/${competitionId}/${course.id}")
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Sports,
                        contentDescription = "Arbitrage"
                    )
                }
            }
        }
    }
}

// Écran de résultat (à ajouter à votre graphe de navigation)
@Composable
fun ResultScreen(navController: NavController, competitionId: String, courseId: String) {
    ScreenScaffold(
        title = "Résultat du parcours",
        navController = navController
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Résultat du parcours: ${courseId}", style = MaterialTheme.typography.headlineMedium)
                Text("Compétition: ${competitionId}", style = MaterialTheme.typography.headlineSmall)
                // Ajoutez ici les détails des résultats
            }
        }
    }
}

// Écran d'arbitrage (à ajouter à votre graphe de navigation)
@Composable
fun ArbitrageScreen(navController: NavController, competitionId: String, courseId: String) {
    ScreenScaffold(
        title = "Arbitrage du parcours",
        navController = navController
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Arbitrage du parcours: ${courseId}", style = MaterialTheme.typography.headlineMedium)
                Text("Compétition: ${competitionId}", style = MaterialTheme.typography.headlineSmall)
                // Ajoutez ici les fonctionnalités d'arbitrage
            }
        }
    }
}

@Composable
fun CompetitorScreen(navController: NavController) {
    ScreenScaffold(
        title = "Compétiteurs",
        navController = navController
    ) {
        val token = "Bearer 1ofD5tbAoC0Xd0TCMcQG3U214MqUo7JzUWrQFWt1ugPuiiDmwQCImm9Giw7fwR0Y"
        var competitors by remember { mutableStateOf<List<Competitor>?>(null) }
        var showDialog by remember { mutableStateOf(false) }
        var competitorToEdit by remember { mutableStateOf<Competitor?>(null) }  // Pour l'édition
        var showDeleteDialog by remember { mutableStateOf(false) }  // Pour la confirmation de suppression
        var competitorToDelete by remember { mutableStateOf<Competitor?>(null) } // Compétiteur à supprimer

        val scope = rememberCoroutineScope()

        // Fonction pour mettre à jour la liste des compétiteurs
        val updateCompetitors = {
            scope.launch {
                try {
                    competitors = ApiClient.apiService.getCompetitors(token)
                } catch (e: Exception) {
                    println("Erreur lors du chargement des compétiteurs : ${e.message}")
                }
            }
        }

        // Charger les compétiteurs au démarrage de l'écran
        LaunchedEffect(true) {
            updateCompetitors() // Charger les compétiteurs lorsque l'écran est chargé
        }

        // Affichage du dialog pour ajouter ou modifier un compétiteur
        if (showDialog) {
            AddCompetitorDialog(
                token = token,
                competitor = competitorToEdit, // Passer le compétiteur à modifier (null si ajout)
                onDismiss = { showDialog = false },
                onCompetitorsUpdated = {
                    updateCompetitors() // Mettre à jour la liste des compétiteurs
                    showDialog = false
                }
            )
        }

        // Affichage du dialog de suppression
        if (showDeleteDialog && competitorToDelete != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Confirmer la suppression") },
                text = {
                    Text("Voulez-vous vraiment supprimer ${competitorToDelete!!.first_name} ${competitorToDelete!!.last_name} ?")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            // Supprimer le compétiteur via l'API
                            scope.launch {
                                try {
                                    ApiClient.apiService.deleteCompetitor(
                                        token,
                                        competitorToDelete!!.id
                                    ) // Suppression via l'API
                                    updateCompetitors() // Mise à jour de la liste après suppression
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

        // Affichage de la liste des compétiteurs
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)) {
            when {
                competitors == null -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally)) // Si les compétiteurs sont en cours de chargement
                }

                competitors!!.isEmpty() -> {
                    Text(
                        "Aucun compétiteur trouvé.",
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) // Si aucune donnée n'est disponible
                }

                else -> {
                    LazyColumn(modifier = Modifier.weight(1f)) { // Donne de l'espace au LazyColumn
                        items(competitors!!.sortedByDescending {
                            // Vous pourriez utiliser une date d'ajout si disponible
                            // Ici on utilise simplement l'ordre d'ajout dans la liste
                            competitors!!.indexOf(it)
                        }) { competitor ->
                            
                            val fullName = "${competitor.first_name} ${competitor.last_name}"
                            val birthDate = competitor.born_at // Format : "yyyy-MM-dd"
                            // Calculer l'âge
                            val age = calculateAge(birthDate)


                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                elevation = CardDefaults.cardElevation(4.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "Nom: $fullName",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(text = "Âge: $age ans")
                                    Text(text = "Genre: ${if (competitor.gender == "H") "Homme" else "Femme"}")

                                    // Icônes pour modifier et supprimer
                                    Row(
                                        horizontalArrangement = Arrangement.End,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        IconButton(onClick = {
                                            competitorToEdit = competitor // Lancer l'édition
                                            showDialog = true // Afficher le dialog d'édition
                                        }) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Modifier"
                                            )
                                        }

                                        IconButton(onClick = {
                                            competitorToDelete = competitor // Lancer la suppression
                                            showDeleteDialog =
                                                true // Afficher la confirmation de suppression
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

            Spacer(modifier = Modifier.height(16.dp)) // Un espacement entre la liste et le bouton

            // Le bouton "Ajouter un compétiteur"
            Button(
                onClick = {
                    competitorToEdit = null
                    showDialog = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ajouter un compétiteur")
            }
        }
    }
}


@Composable
fun AddCompetitorDialog(
    token: String,
    competitor: Competitor? = null, // Si un compétiteur est passé, on le modifie
    onDismiss: () -> Unit,
    onCompetitorsUpdated: () -> Unit // Callback pour mettre à jour la liste des compétiteurs
) {
    val scope = rememberCoroutineScope()

    // Si nous sommes en mode modification, pré-remplir les champs
    var firstName by remember { mutableStateOf(competitor?.first_name ?: "") }
    var lastName by remember { mutableStateOf(competitor?.last_name ?: "") }
    var email by remember { mutableStateOf(competitor?.email ?: "") }
    var phone by remember { mutableStateOf(competitor?.phone ?: "") }
    var gender by remember { mutableStateOf(competitor?.gender ?: "H") }
    var birthDate by remember { mutableStateOf(competitor?.born_at ?: "") }

    // Reset des valeurs quand la fenêtre est fermée
    LaunchedEffect(competitor) {
        if (competitor == null) {
            // Si aucun compétiteur n'est passé, réinitialiser les champs
            firstName = ""
            lastName = ""
            email = ""
            phone = ""
            gender = "H"
            birthDate = ""
        }
    }

    // Variable pour afficher le message d'erreur
    var errorMessage by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (competitor == null) "Ajouter un compétiteur" else "Modifier un compétiteur") },
        text = {
            Column {
                OutlinedTextField(value = firstName, onValueChange = { firstName = it }, label = { Text("Prénom") })
                OutlinedTextField(value = lastName, onValueChange = { lastName = it }, label = { Text("Nom") })
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Téléphone") })
                OutlinedTextField(value = birthDate, onValueChange = { birthDate = it }, label = { Text("Date de naissance (YYYY-MM-DD)") })

                Text("Genre:")
                Row {
                    RadioButton(
                        selected = gender == "H",
                        onClick = { gender = "H" }
                    )
                    Text("Homme", modifier = Modifier.padding(start = 8.dp))

                    Spacer(modifier = Modifier.width(16.dp))

                    RadioButton(
                        selected = gender == "F",
                        onClick = { gender = "F" }
                    )
                    Text("Femme", modifier = Modifier.padding(start = 8.dp))
                }

                // Affichage du message d'erreur si les champs ne sont pas remplis
                if (errorMessage.isNotEmpty()) {
                    Text(text = errorMessage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    // Validation des champs
                    if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || phone.isEmpty() || birthDate.isEmpty()) {
                        errorMessage = "Tous les champs doivent être remplis"
                    } else {
                        errorMessage = ""

                        // Créer un nouveau compétiteur sans l'ID
                        val updatedCompetitor = Competitor(
                            id = competitor?.id ?: 0, // Si modification, utiliser l'ID existant
                            first_name = firstName,
                            last_name = lastName,
                            email = email,
                            gender = gender,
                            phone = phone,
                            born_at = birthDate
                        )

                        scope.launch {
                            try {
                                if (competitor == null) {
                                    // Ajout d'un nouveau compétiteur
                                    ApiClient.apiService.addCompetitor(token, updatedCompetitor)

                                } else {
                                    // Mise à jour du compétiteur
                                    ApiClient.apiService.updateCompetitor(token, updatedCompetitor.id, updatedCompetitor)
                                }
                                onCompetitorsUpdated() // Mettre à jour la liste des compétiteurs
                                onDismiss() // Fermer la fenêtre

                            } catch (e: Exception) {
                                println("Erreur : ${e.message}")
                            }
                        }
                    }
                }
            ) {
                Text(if (competitor == null) "Ajouter" else "Modifier")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) { Text("Annuler") }
        }
    )
}


@Composable
fun CompetitorCard(competitor: Competitor) {
    val fullName = "${competitor.first_name} ${competitor.last_name}"
    val age = calculateAge(competitor.born_at)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Nom: $fullName", style = MaterialTheme.typography.bodyLarge)
            Text(text = "Âge: $age ans")
            Text(text = "Genre: ${if (competitor.gender == "H") "Homme" else "Femme"}")
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


@Composable
fun CoursesScreen(navController: NavController) {
    ScreenScaffold(
        title = "Courses/Parcours",
        navController = navController
    ) {
        val token = "Bearer 1ofD5tbAoC0Xd0TCMcQG3U214MqUo7JzUWrQFWt1ugPuiiDmwQCImm9Giw7fwR0Y"

        val courses by produceState<List<Courses>?>(initialValue = null) {
            try {
                val response = ApiClient.apiService.getCourses(token)
                value = response
                println("✅ Réponse API Courses: $response")
            } catch (e: Exception) {
                value = emptyList()
                println("❌ Erreur API Courses: ${e.message}")
            }
        }

        when {
            courses == null -> CircularProgressIndicator()
            courses!!.isEmpty() -> Text("Aucune course trouvée")
            else -> LazyColumn(modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)) {
                items(courses!!) { course ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Nom: ${course.name}",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(text = "Durée max: ${course.max_duration} sec")
                            Text(text = "Position: ${course.position}")
                            Text(text = "Terminée: ${if (course.is_over == 1) "Oui" else "Non"}")
                        }
                    }
                }
            }
        }
    }
}

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

        Column(modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)) {
            when {
                obstacles == null -> CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                obstacles!!.isEmpty() -> Text(
                    "Aucun obstacle trouvé.",
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                else -> {
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(obstacles!!) { obstacle ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                elevation = CardDefaults.cardElevation(4.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "Nom: ${obstacle.name}",
                                        style = MaterialTheme.typography.bodyLarge
                                    )


                                    Row(
                                        horizontalArrangement = Arrangement.End,
                                        modifier = Modifier.fillMaxWidth()
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

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    obstacleToEdit = null
                    showDialog = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ajouter un obstacle")
            }
        }
    }
}


@Composable
fun AddObstacleDialog(
    token: String,
    obstacle: Obstacles? = null,
    onDismiss: () -> Unit,
    onObstaclesUpdated: () -> Unit
) {
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf(obstacle?.name ?: "") }
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(obstacle) {
        if (obstacle == null) {
            name = ""
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (obstacle == null) "Ajouter un obstacle" else "Modifier un obstacle") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nom") })

                if (errorMessage.isNotEmpty()) {
                    Text(text = errorMessage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isEmpty()) {
                        errorMessage = "Le nom est requis"
                    } else {
                        errorMessage = ""

                        val updatedObstacle = Obstacles(
                            id = obstacle?.id ?: 0,
                            name = name
                        )

                        scope.launch {
                            try {
                                if (obstacle == null) {
                                    ApiClient.apiService.addObstacles(token, updatedObstacle)
                                } else {
                                    ApiClient.apiService.updateObstacles(token, updatedObstacle.id, updatedObstacle)
                                }
                                onObstaclesUpdated()
                                onDismiss()
                            } catch (e: Exception) {
                                println("Erreur : ${e.message}")
                            }
                        }
                    }
                }
            ) {
                Text(if (obstacle == null) "Ajouter" else "Modifier")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) { Text("Annuler") }
        }
    )
}


@Composable
fun ArbitragesScreen(navController: NavController){
    ScreenScaffold(
        title = "Arbitrage",
        navController = navController
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "À faire", style = MaterialTheme.typography.headlineMedium)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewApp() {
    ApplicationParkourTheme {
        ParkourApp()
    }
}

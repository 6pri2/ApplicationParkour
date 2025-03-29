package com.example.applicationparkour

import android.annotation.SuppressLint
import android.graphics.Picture
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
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
import com.google.firebase.perf.util.Timer
import com.google.type.Date
import kotlinx.coroutines.delay
import java.util.Locale


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
    val is_over: Int
)

data class Obstacles(
    val id: Int,
    val name: String,
)

data class Inscription(
    val id: Int,
    val competitor: Competitor,
    val competition_id: Int
)

data class ObstacleResult(
    val competition_id: Int,
    val course_id: Int,
    val competitor_id: Int,
    val obstacle_id: Int,
    val time: Float, // Temps en secondes avec décimales
    val status: String // "completed" ou "failed"
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

    @GET("competitions/{id}/courses")
    suspend fun getCompetitionCourses(
        @Header("Authorization") token: String,
        @Path("id") competitionId: Int
    ): List<Courses>

    @GET("competitions/{competitionId}/inscriptions")
    suspend fun getCompetitionInscriptions(
        @Header("Authorization") token: String,
        @Path("competitionId") competitionId: Int
    ): List<Competitor>

    @GET("courses/{courseId}/obstacles")
    suspend fun getCourseObstacles(
        @Header("Authorization") token: String,
        @Path("courseId") courseId: Int
    ): List<Obstacles>

    @POST("results")
    suspend fun saveObstacleResult(
        @Header("Authorization") token: String,
        @Body result: ObstacleResult
    ): Response<Unit>
}


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
            CompetitionCompetitorsScreen(navController, competitionId)
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
        composable("chronometre/{competitionId}/{courseId}/{competitorId}") { backStackEntry ->
            val competitionId = backStackEntry.arguments?.getString("competitionId") ?: ""
            val courseId = backStackEntry.arguments?.getString("courseId") ?: ""
            val competitorId = backStackEntry.arguments?.getString("competitorId") ?: ""
            ChronometreScreen(
                navController = navController,
                competitionId = competitionId,
                courseId = courseId,
                competitorId = competitorId
            )
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
                    println(showErrorMessage)
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

        // Dialogue d'ajout/modification
        if (showEditDialog || showAddDialog) {
            CompetitionEditDialog(
                competition = if (showEditDialog) selectedCompetition else null,
                onDismiss = {
                    showEditDialog = false
                    showAddDialog = false
                },
                onSave = { updatedCompetition ->
                    scope.launch {
                        isLoading = true
                        try {
                            if (showEditDialog && selectedCompetition != null) {
                                ApiClient.apiService.updateCompetition(
                                    token,
                                    selectedCompetition!!.id,
                                    updatedCompetition
                                )
                                showSuccessMessage = "Compétition mise à jour avec succès"
                            } else {
                                ApiClient.apiService.addCompetition(token, updatedCompetition)
                                showSuccessMessage = "Compétition ajoutée avec succès"
                            }
                            loadCompetitions()
                        } catch (e: Exception) {
                            showErrorMessage = "Erreur lors de la sauvegarde: ${e.message}"
                            println(showErrorMessage)
                        } finally {
                            isLoading = false
                            showEditDialog = false
                            showAddDialog = false
                        }
                    }
                }
            )
        }

        // Dialogue de suppression
        if (showDeleteDialog && selectedCompetition != null) {
            DeleteCompetitionDialog(
                competition = selectedCompetition!!,
                onDismiss = { showDeleteDialog = false },
                onConfirm = {
                    scope.launch {
                        isLoading = true
                        try {
                            ApiClient.apiService.deleteCompetition(token, selectedCompetition!!.id)
                            showSuccessMessage = "Compétition supprimée avec succès"
                            loadCompetitions()
                        } catch (e: Exception) {
                            showErrorMessage = "Erreur lors de la suppression: ${e.message}"
                            println(showErrorMessage)
                        } finally {
                            isLoading = false
                            showDeleteDialog = false
                        }
                    }
                }
            )
        }

        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                when {
                    isLoading && competitions == null -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    competitions == null -> {
                        // Already handled by isLoading case
                    }
                    competitions!!.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Aucune compétition trouvée")
                        }
                    }
                    else -> {
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
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

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

            // Indicateur de chargement pour les opérations
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
fun CompetitionItem(
    competition: Competition,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onCompetitors: () -> Unit,
    onResults: () -> Unit,
    onArbitrage: () -> Unit
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
                    Icon(Icons.Default.Star, "Résultats")
                }
                IconButton(onClick = onArbitrage) {
                    Icon(Icons.Default.Settings, "Arbitrage")
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
    onDismiss: () -> Unit,
    onSave: (Competition) -> Unit
) {
    var name by remember { mutableStateOf(competition?.name ?: "") }
    var ageMin by remember { mutableStateOf(competition?.age_min?.toString() ?: "") }
    var ageMax by remember { mutableStateOf(competition?.age_max?.toString() ?: "") }
    var gender by remember { mutableStateOf(competition?.gender ?: "H") }
    var hasRetry by remember { mutableStateOf(competition?.has_retry == 1) }
    var status by remember { mutableStateOf(competition?.status ?: "pending") }

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
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nom") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = nameError != null,
                    supportingText = { nameError?.let { Text(it) } }
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row {
                    OutlinedTextField(
                        value = ageMin,
                        onValueChange = { if (it.all { c -> c.isDigit() }) ageMin = it },
                        label = { Text("Âge min") },
                        modifier = Modifier.weight(1f),
                        isError = ageMinError != null,
                        supportingText = { ageMinError?.let { Text(it) } },
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Number
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = ageMax,
                        onValueChange = { if (it.all { c -> c.isDigit() }) ageMax = it },
                        label = { Text("Âge max") },
                        modifier = Modifier.weight(1f),
                        isError = ageMaxError != null,
                        supportingText = { ageMaxError?.let { Text(it) } },
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Number
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Genre:", style = MaterialTheme.typography.labelLarge)
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

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = hasRetry,
                        onCheckedChange = { hasRetry = it }
                    )
                    Text("Possibilité de recommencer")
                }

                /*Spacer(modifier = Modifier.height(16.dp))

                Text("Statut:", style = MaterialTheme.typography.labelLarge)
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = status == "pending",
                            onClick = { status = "pending" }
                        )
                        Text("En attente", modifier = Modifier.padding(start = 8.dp))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = status == "ongoing",
                            onClick = { status = "ongoing" }
                        )
                        Text("En cours", modifier = Modifier.padding(start = 8.dp))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = status == "completed",
                            onClick = { status = "completed" }
                        )
                        Text("Terminée", modifier = Modifier.padding(start = 8.dp))
                    }
                }*/
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updatedCompetition = Competition(
                        id = competition?.id ?: 0,
                        name = name,
                        age_min = ageMin.toIntOrNull() ?: 0,
                        age_max = ageMax.toIntOrNull() ?: 0,
                        gender = gender,
                        has_retry = if (hasRetry) 1 else 0,
                        status = status
                    )
                    onSave(updatedCompetition)
                },
                enabled = isValid
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
fun CompetitionCompetitorsScreen(navController: NavController, competitionId: String) {
    ScreenScaffold(
        title = "Compétiteurs de la compétition",
        navController = navController
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Compétiteurs de la compétition: $competitionId")
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

@Composable
fun ArbitrageScreen(navController: NavController, competitionId: String, courseId: String) {
    val token = "Bearer 1ofD5tbAoC0Xd0TCMcQG3U214MqUo7JzUWrQFWt1ugPuiiDmwQCImm9Giw7fwR0Y"
    var inscriptions by remember { mutableStateOf<List<Competitor>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    // Chargement des inscriptions de la compétition
    LaunchedEffect(competitionId) {
        scope.launch {
            try {
                inscriptions = ApiClient.apiService.getCompetitionInscriptions(
                    token,
                    competitionId.toInt()
                )
                isLoading = false
            } catch (e: Exception) {
                error = "Erreur de chargement: ${e.message}"
                isLoading = false
            }
        }
    }

    ScreenScaffold(
        title = "Arbitrage du parcours",
        navController = navController
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                error != null -> {
                    Text(
                        text = error!!,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                inscriptions.isEmpty() -> {
                    Text("Aucun compétiteur inscrit", modifier = Modifier.align(Alignment.Center))
                }

                else -> {
                    LazyColumn(modifier = Modifier.padding(16.dp)) {
                        items(inscriptions) { inscription ->
                            CompetiteurArbitrageItem(
                                competitor = inscription,
                                navController = navController,
                                competitionId = competitionId,
                                courseId = courseId
                            )
                        }
                    }
                }
            }
        }
    }
}

// Le composant CompetiteurArbitrageItem reste identique
@Composable
fun CompetiteurArbitrageItem(
    competitor: Competitor,
    navController: NavController,
    competitionId: String,
    courseId: String
) {
    val fullName = "${competitor.first_name} ${competitor.last_name}"
    val age = calculateAge(competitor.born_at)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = fullName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text("Âge: $age ans")
                Text("Genre: ${if (competitor.gender == "H") "Homme" else "Femme"}")
            }

            IconButton(
                onClick = {
                    navController.navigate("chronometre/$competitionId/$courseId/${competitor.id}")
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

@SuppressLint("UnrememberedMutableState")
@Composable
fun ChronometreScreen(
    navController: NavController,
    competitionId: String,
    courseId: String,
    competitorId: String
) {
    val token = "Bearer 1ofD5tbAoC0Xd0TCMcQG3U214MqUo7JzUWrQFWt1ugPuiiDmwQCImm9Giw7fwR0Y"
    var obstacles by remember { mutableStateOf<List<Obstacles>>(emptyList()) }
    var currentObstacleIndex by remember { mutableStateOf(0) }
    var recordedTimes by remember { mutableStateOf<Map<Int, Float>>(emptyMap()) }
    var isRunning by remember { mutableStateOf(false) }
    var timeMillis by remember { mutableStateOf(0L) }
    var startTime by remember { mutableStateOf(0L) }
    var error by remember { mutableStateOf<String?>(null) }
    var isResultsExpanded by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    LaunchedEffect(recordedTimes.size) {
        if (recordedTimes.isNotEmpty()) {
            listState.animateScrollToItem(recordedTimes.size - 1)
        }
    }

    // Chargement des obstacles
    LaunchedEffect(courseId) {
        try {
            obstacles = ApiClient.apiService.getCourseObstacles(token, courseId.toInt())
        } catch (e: Exception) {
            error = "Erreur de chargement des obstacles"
        }
    }

    val formattedTime by derivedStateOf {
        val totalSeconds = timeMillis / 1000f
        String.format("%.1f", totalSeconds)
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
            recordedTimes = recordedTimes + (it.id to (timeMillis / 1000f))
            currentObstacleIndex++

            // Arrêt automatique si dernier obstacle
            if (currentObstacleIndex >= obstacles.size) {
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
            // Affichage du temps
            Text(
                text = "$formattedTime s",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            // Bouton obstacle actuel
            if (obstacles.isNotEmpty() && currentObstacleIndex < obstacles.size) {
                val obstacle = obstacles[currentObstacleIndex]
                Button(
                    onClick = { recordObstacleTime() },
                    enabled = isRunning,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Obstacle ${currentObstacleIndex + 1}/${obstacles.size}: ${obstacle.name}",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            // Liste des résultats avec hauteur fixe
            if (recordedTimes.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = MaterialTheme.shapes.medium)
                        .padding(8.dp)
                ) {
                    Text(
                        "Temps enregistrés :",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    LazyColumn(
                        state = listState, // État de défilement
                        modifier = Modifier.fillMaxSize()
                    ) {
                        itemsIndexed(recordedTimes.toList()) { index, (id, time) ->
                            val obstacle = obstacles.find { it.id == id }
                            ListItem(
                                headlineContent = {
                                    Text(
                                        "${index + 1}. ${obstacle?.name ?: "Inconnu"} : ${"%.1f".format(time)}s",
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

            // Contrôles en bas de l'écran
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = { isRunning = !isRunning },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRunning) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (isRunning) "Arrêter" else "Démarrer")
                }

                Button(
                    onClick = {
                        isRunning = false
                        timeMillis = 0L
                        currentObstacleIndex = 0
                        recordedTimes = emptyMap()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Réinitialiser")
                }
            }

            // Message de fin
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
                        items(competitors!!) { competitor ->
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

package com.example.applicationparkour

import android.graphics.Picture
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
import retrofit2.http.Path


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
    val picture: String?,
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

    @POST("competitors")
    suspend fun addCompetitor(
        @Header("Authorization") token: String,
        @Body competitor: Competitor
    ): Competitor

    @DELETE("competitors/{id}")
    suspend fun deleteCompetitor(
        @Header("Authorization") token: String,
        @Path("id") competitorId: Int
    ): Response<Unit>  // Unité vide pour indiquer que rien n'est renvoyé après la suppression
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
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                var currentPage by remember { mutableStateOf(0) }
                val scope = rememberCoroutineScope()

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        SlidingMenu(setPage = { page ->
                            currentPage = page
                            scope.launch { drawerState.close() }
                        })
                    }
                ) {
                    MainContent(
                        currentPage = currentPage,
                        openDrawer = { scope.launch { drawerState.open() } }
                    )
                }
            }
        }
    }
}

@Composable
fun MainContent(currentPage: Int, openDrawer: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        IconButton(onClick = { openDrawer() }) {
            Icon(imageVector = Icons.Default.Menu, contentDescription = "Open Drawer")
        }
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            when (currentPage) {
                0 -> CompetitionScreen()
                1 -> CompetitorScreen()
                2 -> CoursesScreen()
                3 -> ObstaclesScreen()
                4 -> ArbitragesScreen()
            }
        }
    }
}

@Composable
fun SlidingMenu(setPage: (Int) -> Unit) {
    val menuItems = listOf("Compétition", "Compétiteur", "Courses", "Obstacles", "Arbitrage")
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Text(text = "Menu")
        Spacer(modifier = Modifier.height(20.dp))
        menuItems.forEachIndexed { index, title ->
            TextButton(onClick = { setPage(index) }) {
                Text(title)
            }
        }
    }
}

@Composable
fun CompetitionScreen() {
    val token = "Bearer 1ofD5tbAoC0Xd0TCMcQG3U214MqUo7JzUWrQFWt1ugPuiiDmwQCImm9Giw7fwR0Y"
    val competitions by produceState<List<Competition>?>(initialValue = null) {
        try {
            val response = ApiClient.apiService.getCompetitions(token)
            value = response
            println("✅ Réponse API : $response") // Log dans la console
        } catch (e: Exception) {
            value = emptyList()
            println("❌ Erreur API : ${e.message}") // Log erreur
        }
    }


    when {
        competitions == null -> CircularProgressIndicator()
        competitions!!.isEmpty() -> Text("Aucune compétition trouvée")
        else -> LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            items(competitions!!) { competition ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Nom: ${competition.name}", style = MaterialTheme.typography.bodyLarge)
                        Text(text = "Âge: ${competition.age_min} - ${competition.age_max} ans")
                        Text(text = "Genre: ${if (competition.gender == "H") "Homme" else "Femme"}")
                        Text(text = "Retry: ${if (competition.has_retry == 1) "Oui" else "Non"}")
                        Text(text = "Statut: ${competition.status}")
                    }
                }
            }
        }
    }
}

@Composable
fun CompetitorScreen() {
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

    // Affichage du dialog pour ajouter un compétiteur
    if (showDialog) {
        AddCompetitorDialog(
            token = token,
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
                                ApiClient.apiService.deleteCompetitor(token, competitorToDelete!!.id) // Suppression via l'API
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
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Afficher la liste des compétiteurs
        when {
            competitors == null -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally)) // Si les compétiteurs sont en cours de chargement
            }
            competitors!!.isEmpty() -> {
                Text("Aucun compétiteur trouvé.", modifier = Modifier.align(Alignment.CenterHorizontally)) // Si aucune donnée n'est disponible
            }
            else -> {
                LazyColumn(modifier = Modifier.weight(1f)) { // Donne de l'espace au LazyColumn
                    items(competitors!!) { competitor ->
                        val fullName = "${competitor.first_name} ${competitor.last_name}"
                        val birthDate = competitor.born_at // Format : "yyyy-MM-dd"
                        // Calculer l'âge
                        val age = calculateAge(birthDate)

                        Card(
                            modifier = Modifier.fillMaxWidth().padding(8.dp),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(text = "Nom: $fullName", style = MaterialTheme.typography.bodyLarge)
                                Text(text = "Âge: $age ans")
                                Text(text = "Genre: ${if (competitor.gender == "H") "Homme" else "Femme"}")

                                // Icônes pour modifier et supprimer
                                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                                    IconButton(onClick = {
                                        competitorToEdit = competitor // Lancer l'édition
                                        showDialog = true // Afficher le dialog d'édition
                                    }) {
                                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Modifier")
                                    }

                                    IconButton(onClick = {
                                        competitorToDelete = competitor // Lancer la suppression
                                        showDeleteDialog = true // Afficher la confirmation de suppression
                                    }) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Supprimer")
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
            onClick = { showDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ajouter un compétiteur")
        }
    }
}

@Composable
fun AddCompetitorDialog(
    token: String,
    onDismiss: () -> Unit,
    onCompetitorsUpdated: () -> Unit // Callback pour mettre à jour la liste des compétiteurs
) {
    val scope = rememberCoroutineScope()

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("H") }
    var birthDate by remember { mutableStateOf("") }

    // Variable pour afficher le message d'erreur
    var errorMessage by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ajouter un compétiteur") },
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
                        val newCompetitor = Competitor(
                            id = 0, // Ne pas utiliser l'ID dans la requête
                            first_name = firstName,
                            last_name = lastName,
                            email = email,
                            gender = gender,
                            phone = phone,
                            born_at = birthDate
                        )

                        scope.launch {
                            try {
                                // Envoie du compétiteur à l'API
                                ApiClient.apiService.addCompetitor(token, newCompetitor)
                                onCompetitorsUpdated() // Mettre à jour la liste des compétiteurs
                                onDismiss() // Fermer la fenêtre
                            } catch (e: Exception) {
                                println("Erreur : ${e.message}")
                            }
                        }
                    }
                }
            ) {
                Text("Ajouter")
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
        modifier = Modifier.fillMaxWidth().padding(8.dp),
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
fun CoursesScreen() {
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
        else -> LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            items(courses!!) { course ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Nom: ${course.name}", style = MaterialTheme.typography.bodyLarge)
                        Text(text = "Durée max: ${course.max_duration} sec")
                        Text(text = "Position: ${course.position}")
                        Text(text = "Terminée: ${if (course.is_over == 1) "Oui" else "Non"}")
                    }
                }
            }
        }
    }
}

@Composable
fun ObstaclesScreen() {
    val token = "Bearer 1ofD5tbAoC0Xd0TCMcQG3U214MqUo7JzUWrQFWt1ugPuiiDmwQCImm9Giw7fwR0Y"

    val obstacles by produceState<List<Obstacles>?>(initialValue = null) {
        try {
            val response = ApiClient.apiService.getObstacles(token)
            value = response
            println("✅ Réponse API Obstacles: $response")
        } catch (e: Exception) {
            value = emptyList()
            println("❌ Erreur API Obstacles: ${e.message}")
        }
    }

    when {
        obstacles == null -> CircularProgressIndicator()
        obstacles!!.isEmpty() -> Text("Aucun obstacle trouvé")
        else -> LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            items(obstacles!!) { obstacle ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Nom: ${obstacle.name}", style = MaterialTheme.typography.bodyLarge)
                        if (obstacle.picture != null) {
                            // Ici, on pourrait afficher une image si l'URL est valide
                            // AsyncImage(model = obstacle.picture, contentDescription = "Image de l'obstacle")
                        } else {
                            Text(text = "Aucune image disponible", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ArbitragesScreen(){
   Box(
       modifier = Modifier.fillMaxSize(),
       contentAlignment = Alignment.Center
   ) {
       Text(text = "À faire", style = MaterialTheme.typography.headlineMedium)
   }
}

@Preview(showBackground = true)
@Composable
fun PreviewApp() {
    ApplicationParkourTheme {
        MainContent(currentPage = 0, openDrawer = {})
    }
}

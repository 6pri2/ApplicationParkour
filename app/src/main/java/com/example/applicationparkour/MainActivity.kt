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
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.applicationparkour.ui.theme.ApplicationParkourTheme
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import java.time.LocalDate
import java.time.Period


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

    val competitors by produceState<List<Competitor>?>(initialValue = null) {
        try {
            val response = ApiClient.apiService.getCompetitors(token)
            value = response
            println("✅ Réponse API : $response") // Log dans la console
        } catch (e: Exception) {
            value = emptyList()
            println("❌ Erreur API : ${e.message}") // Log erreur
        }
    }

    when {
        competitors == null -> CircularProgressIndicator()
        competitors!!.isEmpty() -> Text("Aucun compétiteur trouvé")
        else -> LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            items(competitors!!) { competitor ->
                val fullName = "${competitor.first_name} ${competitor.last_name}"
                val birthDate = competitor.born_at // Format : "yyyy-MM-dd"

                // Calcul de l'âge
                val age = calculateAge(birthDate)

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

package com.example.applicationparkour

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
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
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Icone menu hamburger
        IconButton(onClick = { openDrawer() }) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Open Drawer"
            )
        }

        // Affichage de la page en fonction de currentPage
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            when (currentPage) {
                0 -> CompetitionScreen()
                1 -> CompetitorScreen()
                2 -> CoursesScreen()
                3 -> ObstaclesScreen()
            }
        }
    }
}

@Composable
fun SlidingMenu(setPage: (Int) -> Unit) {
    val menuItems = listOf("Compétition", "Compétiteur", "Courses", "Obstacles")

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
    Text("Écran Compétition")
}

@Composable
fun CompetitorScreen() {
    Text("Écran Compétiteur")
}

@Composable
fun CoursesScreen() {
    Text("Écran Courses")
}

@Composable
fun ObstaclesScreen() {
    Text("Écran Obstacles")
}

@Preview(showBackground = true)
@Composable
fun PreviewApp() {
    ApplicationParkourTheme {
        MainContent(currentPage = 0, openDrawer = {})
    }
}
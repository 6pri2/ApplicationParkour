package iut.gon.applicationparkour.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

/**
 * Page de bienvenue
 */

@Composable
fun WelcomeScreen(navController: NavController) {
    Column(
        modifier = Modifier.Companion
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Companion.CenterHorizontally
    ) {
        Text(
            text = "Application Parkour",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.Companion.padding(bottom = 48.dp)
        )

        val buttonModifier = Modifier.Companion
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
            onClick = { navController.navigate("obstacles") },
            modifier = buttonModifier
        ) {
            Text("Obstacles")
        }
    }
}
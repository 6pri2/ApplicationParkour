package iut.gon.applicationparkour.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import iut.gon.applicationparkour.R


/**
 * Page de bienvenue
 */

@Composable
fun WelcomeScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Titre
        Text(
            text = "Application Parkour",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Logo de l'application
        Image(
            painter = painterResource(id = R.mipmap.ic_launcher_adaptive_fore), // Image dans res/drawable
            contentDescription = "Logo de l'application",
            modifier = Modifier
                .size(346.dp)
                .padding(bottom = 24.dp)
        )

        // Définition de la taille des boutons
        val buttonModifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .height(60.dp)

        // Premier bouton : Compétiteurs
        Button(
            onClick = { navController.navigate("competitors") },
            modifier = buttonModifier,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Icon(Icons.Filled.Person, contentDescription = "Compétiteurs", tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Compétiteurs", color = Color.White)
        }

        // Deuxième bouton : Compétitions
        Button(
            onClick = { navController.navigate("competitions") },
            modifier = buttonModifier,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Icon(Icons.Filled.EmojiEvents, contentDescription = "Compétitions", tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Compétitions", color = Color.White)
        }

        // Troisième bouton : Obstacles
        Button(
            onClick = { navController.navigate("obstacles") },
            modifier = buttonModifier,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary) // Si la couleur tertiaire existe
        ) {
            Icon(Icons.Filled.FitnessCenter, contentDescription = "Obstacles", tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Obstacles", color = Color.White)
        }
    }
}

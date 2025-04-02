package iut.gon.applicationparkour.ui.components.obstacle

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import iut.gon.applicationparkour.data.api.ApiClient
import iut.gon.applicationparkour.data.model.Obstacles
import kotlinx.coroutines.launch

/**
 * dialogue pour ajouter un obstacle
 */

@Composable
fun AddObstacleDialog(
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
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nom") })

                if (errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
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
                                    ApiClient.apiService.addObstacles( updatedObstacle)
                                } else {
                                    ApiClient.apiService.updateObstacles(
                                        updatedObstacle.id,
                                        updatedObstacle
                                    )
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
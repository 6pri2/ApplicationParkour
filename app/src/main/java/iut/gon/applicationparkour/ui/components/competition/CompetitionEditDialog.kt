package iut.gon.applicationparkour.ui.components.competition

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sports
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import iut.gon.applicationparkour.data.api.ApiClient
import iut.gon.applicationparkour.data.model.Competition
import kotlinx.coroutines.launch
import retrofit2.HttpException

/**
 * dialogue pour ajouter une competition
 */

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
                modifier = Modifier.Companion.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Nom
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nom") },
                    modifier = Modifier.Companion.fillMaxWidth(),
                    isError = nameError != null,
                    supportingText = { nameError?.let { Text(it) } }
                )

                // Âges
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = ageMin,
                        onValueChange = { if (it.all { c -> c.isDigit() }) ageMin = it },
                        label = { Text("Âge min") },
                        modifier = Modifier.Companion.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Companion.Number),
                        isError = ageMinError != null,
                        supportingText = { ageMinError?.let { Text(it) } }
                    )

                    OutlinedTextField(
                        value = ageMax,
                        onValueChange = { if (it.all { c -> c.isDigit() }) ageMax = it },
                        label = { Text("Âge max") },
                        modifier = Modifier.Companion.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Companion.Number),
                        isError = ageMaxError != null,
                        supportingText = { ageMaxError?.let { Text(it) } }
                    )
                }

                // Genre
                Text("Genre:", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.Companion.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.Companion.CenterVertically) {
                        RadioButton(
                            selected = gender == "H",
                            onClick = { gender = "H" }
                        )
                        Text("Homme", modifier = Modifier.Companion.padding(start = 4.dp))
                    }

                    Row(verticalAlignment = Alignment.Companion.CenterVertically) {
                        RadioButton(
                            selected = gender == "F",
                            onClick = { gender = "F" }
                        )
                        Text("Femme", modifier = Modifier.Companion.padding(start = 4.dp))
                    }
                }

                // Recommencer
                Row(verticalAlignment = Alignment.Companion.CenterVertically) {
                    Checkbox(
                        checked = hasRetry,
                        onCheckedChange = { hasRetry = it }
                    )
                    Text(
                        "Possibilité de recommencer",
                        modifier = Modifier.Companion.padding(start = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            Column(
                modifier = Modifier.Companion.fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (competition == null) {
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
                                        val createdCompetition =
                                            ApiClient.apiService.addCompetition(
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
                            modifier = Modifier.Companion.fillMaxWidth(),
                            enabled = isValid
                        ) {
                            Text("Créer et gérer les parcours")
                        }
                        if (nameError != null || ageMinError != null || ageMaxError != null) {
                            Text(
                                text = "Veuillez corriger les erreurs avant de continuer",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.Companion.padding(top = 8.dp)
                            )
                        }
                    }
                } else {

                    if (competition != null) {
                        Button(
                            onClick = {
                                onDismiss()
                                onManageCourses()
                            },
                            modifier = Modifier.Companion.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Sports, contentDescription = null)
                            Spacer(Modifier.Companion.width(8.dp))
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
                                } catch (e: HttpException) {
                                    val errorBody = e.response()?.errorBody()?.string()
                                    println("Erreur 422: $errorBody")
                                    // Affichez un message à l'utilisateur
                                } catch (e: Exception) {
                                    println("Autre erreur: ${e.message}")
                                }
                            }
                        },
                        enabled = isValid,
                        modifier = Modifier.Companion.fillMaxWidth()
                    ) {
                        Text("Enregistrer")
                    }

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.Companion.fillMaxWidth(),
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
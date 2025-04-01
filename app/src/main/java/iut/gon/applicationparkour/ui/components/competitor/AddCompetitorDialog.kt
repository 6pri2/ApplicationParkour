package iut.gon.applicationparkour.ui.components.competitor

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import iut.gon.applicationparkour.data.api.ApiClient
import iut.gon.applicationparkour.data.model.Competitor
import kotlinx.coroutines.launch

/**
 * dialogue pour ajouter un competiteur
 */

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
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("Prénom") })
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("Nom") })
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") })
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Téléphone") })
                OutlinedTextField(
                    value = birthDate,
                    onValueChange = { birthDate = it },
                    label = { Text("Date de naissance (YYYY-MM-DD)") })

                Text("Genre:")
                Row {
                    RadioButton(
                        selected = gender == "H",
                        onClick = { gender = "H" }
                    )
                    Text("Homme", modifier = Modifier.Companion.padding(start = 8.dp))

                    Spacer(modifier = Modifier.Companion.width(16.dp))

                    RadioButton(
                        selected = gender == "F",
                        onClick = { gender = "F" }
                    )
                    Text("Femme", modifier = Modifier.Companion.padding(start = 8.dp))
                }

                // Affichage du message d'erreur si les champs ne sont pas remplis
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
                                    ApiClient.apiService.updateCompetitor(
                                        token,
                                        updatedCompetitor.id,
                                        updatedCompetitor
                                    )
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
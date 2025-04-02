package iut.gon.applicationparkour.ui.components.courses

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import iut.gon.applicationparkour.data.model.Courses

/**
 * dialogue pour ajouter une course
 */

@Composable
fun CourseAddDialog(
    competitionId: String,
    onDismiss: () -> Unit,
    onSave: (Courses) -> Unit,
    defaultPosition: Int
) {
    var name by remember { mutableStateOf("") }
    var maxDuration by remember { mutableStateOf("") }
        maxDuration.toIntOrNull()?.takeIf { it > 240 }?.let { "Le parcours est trop long" }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ajouter un parcours") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nom") },
                    modifier = Modifier.Companion.fillMaxWidth()
                )
                Spacer(modifier = Modifier.Companion.height(8.dp))
                OutlinedTextField(
                    value = maxDuration,
                    onValueChange = { if (it.all { c -> c.isDigit() }) maxDuration = it },
                    label = { Text("Durée maximale (secondes)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Companion.Number),
                    modifier = Modifier.Companion.fillMaxWidth()
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
                modifier = Modifier.Companion.fillMaxWidth()
            ) {
                Text("Ajouter et gérer les obstacles")
            }
        }

    )
}
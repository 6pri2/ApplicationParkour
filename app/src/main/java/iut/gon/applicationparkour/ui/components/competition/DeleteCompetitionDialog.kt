package iut.gon.applicationparkour.ui.components.competition

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import iut.gon.applicationparkour.data.model.Competition

/**
 * dialogue pour supprimer une competition
 */

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
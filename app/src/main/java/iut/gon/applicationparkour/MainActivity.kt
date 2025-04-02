package iut.gon.applicationparkour

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import iut.gon.applicationparkour.ui.theme.ApplicationParkourTheme
import iut.gon.applicationparkour.ui.app.ParkourApp

// --- Activité principale ---

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ApplicationParkourTheme {
                ParkourApp()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewApp() {
    ApplicationParkourTheme {
        ParkourApp()
    }
}

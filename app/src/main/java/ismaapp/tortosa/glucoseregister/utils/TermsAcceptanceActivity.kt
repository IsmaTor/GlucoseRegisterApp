package ismaapp.tortosa.glucoseregister.utils

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class TermsAcceptanceActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            TermsAcceptanceScreen {
                // Usuario acepta los términos
                setResult(RESULT_OK)
                finish() // Cerrar la actividad y volver a MainActivity
            }
        }
    }
}

@Composable
fun TermsAcceptanceScreen(onAccept: () -> Unit) {
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Por favor, acepta los términos y condiciones para continuar.",
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Button(
                onClick = onAccept,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text(text = "Aceptar")
            }

            Button(
                onClick = { /* No se aceptan los términos */ },
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text(text = "Rechazar")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewTermsAcceptanceScreen() {
    TermsAcceptanceScreen(onAccept = { /* Preview action */ })
}
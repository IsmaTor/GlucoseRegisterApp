package ismaapp.tortosa.glucoseregister

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.DarkGray
                ) {
                    GlucoseMeasurementScreen()
                }
            }
        }
    }
}

@Composable
fun GlucoseMeasurementScreen() {
    var glucoseValue by remember { mutableStateOf(0f) }
    var isMeasurementSuccessful by remember { mutableStateOf(false) }
    var showMessage by remember { mutableStateOf(false) }

    val date = DateUtils.getFormattedDate()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(100.dp)
    ) {
        Text( //text = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(currentDate.value),
            "Indique la medición:",
            style = TextStyle(color = Color.White)
        )
        Spacer(modifier = Modifier.height(8.dp))

        var isError by remember { mutableStateOf(false) }
        var message by remember { mutableStateOf("") }

        OutlinedTextField(
            value = glucoseValue.toString(),
            onValueChange = {
                isError = false
                glucoseValue = it.toFloatOrNull() ?: 0f
            },
            label = { Text("Ingrese el valor de glucosa", modifier = Modifier.align(Alignment.CenterHorizontally)) },
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Number
            ),
            isError = isError,
            textStyle = TextStyle(color = Color.White)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = {
                    if (glucoseValue >= 80 && glucoseValue <= 120) {
                        isMeasurementSuccessful = true
                        showMessage = true
                        message = "Medición registrada correctamente"
                    } else {
                        isMeasurementSuccessful = false
                        showMessage = true
                        message = "Medición no registrada correctamente"
                    }
                },
                modifier = Modifier.fillMaxWidth()
                    .padding(4.dp)
                    .heightIn(min = 48.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Registrar")
            }
            Button(
                onClick = {
                //lógica para código de estadísticas.
                },
                modifier = Modifier.fillMaxWidth()
                    .padding(4.dp)
                    .heightIn(min = 48.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                Icon(Icons.Default.DateRange, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Historial")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (showMessage) {
            val icon = if (isMeasurementSuccessful) Icons.Default.Check else Icons.Default.Clear
            val color = if (isMeasurementSuccessful) Color.Green else Color.Red

            Row(
                modifier = Modifier
                    .background(color)
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(4.dp))
                Text(message, color = Color.White)
            }
        }

        Text(
            "Fecha: $date",
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(16.dp)
                .background(Color.LightGray)
        )

    }
}

@Preview(showBackground = true)
@Composable
fun GlucoseMeasurementScreenPreview() {
    MaterialTheme {
        GlucoseMeasurementScreen()
    }
}
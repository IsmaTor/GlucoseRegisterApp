package ismaapp.tortosa.glucoseregister.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import ismaapp.tortosa.glucoseregister.services.IGlucoseServices
import kotlinx.coroutines.delay

@Composable
fun GlucoseMeasurementScreen(glucoseService: IGlucoseServices, navController: NavController) {
    var glucoseValue by remember { mutableStateOf(0) }
    var isMeasurementSuccessful by remember { mutableStateOf(false) }
    var showMessage by remember { mutableStateOf(false) }
    var lastMeasurement: Int? by remember { mutableStateOf(null) }
    var message by remember { mutableStateOf("") }

    // Obtener la última medición de la base de datos.
    LaunchedEffect(Unit) {
        lastMeasurement = glucoseService.lastGlucoseMeasurement
    }

    // El mensaje desaparecerá después del tiempo indicado.
    LaunchedEffect(showMessage) {
        if (showMessage) {
            delay(5000) // 5 segundos.
            showMessage = false // false para que desaparezca.
        }
    }

    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(100.dp)
    ) {
        //Ingresar mediciones
        GlucoseInput(
            glucoseValue = glucoseValue,
            onValueChange = { newValue ->
                glucoseValue = newValue
            }
        )

        buttonsHome(
            glucoseService = glucoseService,
            navController = navController,
            glucoseValue = glucoseValue,
            keyboardController = keyboardController,
            onMeasurementRegistered = { newSuccessful, newMessage, newGlucoseValue ->
                isMeasurementSuccessful = newSuccessful
                showMessage = true
                message = newMessage
                glucoseValue = newGlucoseValue
            },
            onLastMeasurementUpdated = { newLastMeasurement ->
                lastMeasurement = newLastMeasurement
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            if (showMessage) {
                // Muestra el mensaje
                val icon = if (isMeasurementSuccessful) Icons.Default.Check else Icons.Default.Clear
                val color = if (isMeasurementSuccessful) Color.Green else Color.Red

                Row(
                    modifier = Modifier
                        .background(color)
                        .fillMaxWidth()
                        .padding(20.dp)
                        .zIndex(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(icon, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(message, color = Color.White)
                }
            }
            // Muestra la última medición
            lastMeasure(lastMeasurement = lastMeasurement)
        }

    }
}

@Composable
fun buttonsHome(
    glucoseService: IGlucoseServices,
    navController: NavController,
    glucoseValue: Int,
    keyboardController: SoftwareKeyboardController?,
    onMeasurementRegistered: (Boolean, String, Int) -> Unit,
    onLastMeasurementUpdated: (Int?) -> Unit
) {
    // Valores para ajustar el recuadro
    val xOffset = 0.dp
    val yOffset = 60.dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .offset(x = xOffset, y = yOffset),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = {
                // Insertar medición en la base de datos
                glucoseService.insertGlucoseMeasurement(glucoseValue)

                val isInsertSuccessful = glucoseService.isInsertSuccess

                if (isInsertSuccessful) {
                    onMeasurementRegistered(true, "Medición registrada correctamente", 0)
                } else {
                    onMeasurementRegistered(false, "Medición no registrada correctamente", glucoseValue)
                }
                keyboardController?.hide()
            },
            modifier = Modifier
                .fillMaxWidth()
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
                glucoseService.getPaginatedGlucoseMeasurements(0, 12, true, true, "")
                navController.navigate("historial/1") {
                    launchSingleTop = true
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .heightIn(min = 48.dp)
                .clip(RoundedCornerShape(8.dp))
        ) {
            Icon(Icons.Default.DateRange, contentDescription = null)
            Spacer(modifier = Modifier.width(4.dp))
            Text("Historial")
        }
        Button(
            onClick = {
                // Obtener la última medición de la base de datos
                val updatedLastMeasurement = glucoseService.lastGlucoseMeasurement
                onLastMeasurementUpdated(updatedLastMeasurement)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .heightIn(min = 48.dp)
                .clip(RoundedCornerShape(8.dp))
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(4.dp))
            Text("Actualizar")
        }
    }
}

@Composable
private fun GlucoseInput(glucoseValue: Int, onValueChange: (Int) -> Unit) {
    val scale = 1.15f // Porcentaje de escala en este es un 15% más grande de 1.0

    var isError by remember { mutableStateOf(false) }

    val backgroundColor = Color(0xFF808080).copy(alpha = 0.9f)

    Box(
        modifier = Modifier.scale(scale)
    ) {
        OutlinedTextField(
            value = glucoseValue.takeIf { it != 0 }?.toString() ?: "",
            onValueChange = { newValue ->
                isError = false

                // Permitir solo un punto decimal y números y tres digitos.
                val regex = Regex("""^-?\d{0,3}$""")
                if (newValue.isBlank() || regex.matches(newValue)) {
                    onValueChange(newValue.toIntOrNull() ?: 0)
                }
            },
            label = { Text("Ingrese el valor de glucosa") },
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Number
            ),
            isError = isError,
            textStyle = TextStyle(color = Color.White, fontSize = 24.sp * scale),
            modifier = Modifier
                .background(backgroundColor)
                .fillMaxWidth()
        )
    }

    Spacer(modifier = Modifier.height(16.dp * scale))
}



@Composable
fun lastMeasure(lastMeasurement: Int?) {

    lastMeasurement?.let { measurement ->
        val textColor = when (measurement) {
            in 80..130 -> Color.Green // Si la medición está entre 80 y 130
            else -> Color.Red.copy(alpha = 0.8f) // Por defecto
        }

        // Valores para ajustar el recuadro
        val xOffset = 0.dp
        val yOffset = 100.dp

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .offset(x = xOffset, y = yOffset)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color.LightGray
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Text(
                        text = "Última medición:",
                        color = Color.DarkGray,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Text(
                        text = "$measurement",
                        color = textColor,
                        fontSize = 8.em,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
}

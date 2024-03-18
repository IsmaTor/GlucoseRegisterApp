package ismaapp.tortosa.glucoseregister.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.navigation.NavController
import ismaapp.tortosa.glucoseregister.services.IGlucoseServices
import kotlinx.coroutines.delay

@Composable
fun GlucoseMeasurementScreen(glucoseService: IGlucoseServices, navController: NavController) {
    var glucoseValue by remember { mutableStateOf(0f) }
    var isMeasurementSuccessful by remember { mutableStateOf(false) }
    var showMessage by remember { mutableStateOf(false) }
    var lastMeasurement: Float? by remember { mutableStateOf(null) }
    var message by remember { mutableStateOf("") }

    // Obtener la última medición de la base de datos.
    LaunchedEffect(Unit) {
        lastMeasurement = glucoseService.getLastGlucoseMeasurement()
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

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = {
                    // Insertar medición en la base de datos
                    glucoseService.insertGlucoseMeasurement(glucoseValue)

                    val isInsertSuccessful = glucoseService.isInsertSuccess // Asume que hay un método en glucoseService para verificar si la inserción fue exitosa

                    if (isInsertSuccessful) {
                        isMeasurementSuccessful = true
                        showMessage = true
                        message = "Medición registrada correctamente"
                        glucoseValue = 0f
                    } else {
                        isMeasurementSuccessful = false
                        showMessage = true
                        message = "Medición no registrada correctamente"
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
        // Última medición guardada
        lastMeasure(lastMeasurement = lastMeasurement)
    }
}

@Composable
private fun GlucoseInput(glucoseValue: Float, onValueChange: (Float) -> Unit) {
    Text(
        "Indique la medición:",
        style = TextStyle(color = Color.White)
    )
    Spacer(modifier = Modifier.height(8.dp))

    var isError by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = glucoseValue.takeIf { it != 0f }?.toString() ?: "",
        onValueChange = { newValue ->
            isError = false

            // Permitir solo un punto decimal y números y tres digitos.
            val regex = Regex("""^-?\d{0,3}(\.\d{0,1})?$""")
            if (newValue.isBlank() || regex.matches(newValue)) {
                onValueChange(newValue.toFloatOrNull() ?: 0f)
            }
        },
        label = { Text("Ingrese el valor de glucosa") },
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.Decimal
        ),
        isError = isError,
        textStyle = TextStyle(color = Color.White),
    )

    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
fun lastMeasure(lastMeasurement: Float?) { //Float? porqué puede ser nulo

    lastMeasurement?.let { measurement ->
        val textColor = when {
            measurement in 80.0..130.0 -> Color.Green // Si la medición está entre 80 y 130
            measurement < 80.0 || measurement > 130.0 -> Color.Red.copy(alpha = 0.8f) // Si la medición es menor que 80 o mayor que 130
            else -> Color.White // Por defecto
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(8.dp),
            color = Color.LightGray
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
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
                    fontSize = 8.em, // Doble de grande que el tamaño de fuente por defecto
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

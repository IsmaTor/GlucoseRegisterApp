package ismaapp.tortosa.glucoseregister.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import ismaapp.tortosa.glucoseregister.repository.GlucoseRepository
import ismaapp.tortosa.glucoseregister.services.GlucoseLevelsImp
import ismaapp.tortosa.glucoseregister.services.IGlucoseService
import ismaapp.tortosa.glucoseregister.ui.theme.BackgroundGrey
import ismaapp.tortosa.glucoseregister.ui.theme.SoftYellow
import ismaapp.tortosa.glucoseregister.ui.theme.SoftGreen
import ismaapp.tortosa.glucoseregister.ui.theme.SoftRed
import ismaapp.tortosa.glucoseregister.utils.SuccessfulMessage
import kotlinx.coroutines.delay

@Composable
fun GlucoseMeasurementScreen(
    glucoseService: IGlucoseService,
    glucoseRepository: GlucoseRepository,
    navController: NavController
) {
    var glucoseValue by remember { mutableIntStateOf(0) }
    var isMeasurementSuccessful by remember { mutableStateOf(false) }
    var showMessage by remember { mutableStateOf(false) }
    var lastMeasurement by remember { mutableStateOf<Int?>(null) }
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

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.End, // Alinea los elementos al final derecha.
        verticalAlignment = Alignment.Top // Alinea los elementos en la parte superior derecha.
    ) {
        IconButton(
            onClick = {
                navController.navigate("configuration") {
                    launchSingleTop = true
                }
            },
            modifier = Modifier
                .size(60.dp) // tamaño del recuadro del icono.
                .shadow(4.dp) // agrega sombra al icono.
        ) {
            Icon(
                Icons.Default.Settings,
                contentDescription = "Configuración",
                modifier = Modifier.size(60.dp),
                tint = SoftYellow
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(100.dp)
    ) {
        // Ingresar mediciones
        GlucoseInput(
            glucoseValue = glucoseValue,
            onValueChange = { newValue ->
                glucoseValue = newValue
            }
        )

        ButtonsHome(
            glucoseService = glucoseService,
            navController = navController,
            glucoseValue = glucoseValue,
            keyboardController = keyboardController,
            onMeasurementRegistered = { newSuccessful, newMessage, newGlucoseValue ->
                isMeasurementSuccessful = newSuccessful
                showMessage = true
                message = newMessage
                glucoseValue = newGlucoseValue

                // Obtener la última medición de la base de datos actualizada
                lastMeasurement = glucoseService.lastGlucoseMeasurement
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
                SuccessfulMessage(showMessage = showMessage, isMeasurementSuccessful = isMeasurementSuccessful, message = message)
            }
            // Muestra la última medición
            LastMeasure(lastMeasurement = lastMeasurement, glucoseRepository = glucoseRepository)
        }
    }
}

@Composable
fun ButtonsHome(
    glucoseService: IGlucoseService,
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

                val isInsertSuccessful = glucoseService.isDeleteSuccess

                if (isInsertSuccessful) {
                    onMeasurementRegistered(true, "Medición registrada", 0)

                    val updatedLastMeasurement = glucoseService.lastGlucoseMeasurement
                    onLastMeasurementUpdated(updatedLastMeasurement)
                } else {
                    onMeasurementRegistered(false, "Medición no registrada", glucoseValue)
                }
                keyboardController?.hide()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .heightIn(min = 48.dp)
                .clip(RoundedCornerShape(50.dp, 50.dp, 0.dp, 0.dp)), //botón ovalado desde arriba.
            colors = ButtonDefaults.buttonColors(SoftYellow)
        ) {
            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "register")
            Spacer(modifier = Modifier.width(4.dp))
            Text("  Registrar")
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
                .clip(RoundedCornerShape(8.dp)), //botón con bordes redondeados.
            colors = ButtonDefaults.buttonColors(SoftYellow)
        ) {
            Icon(Icons.Default.DateRange, contentDescription = "historical")
            Spacer(modifier = Modifier.width(4.dp))
            Text("  Historial")
        }
        Button(
            onClick = {
                navController.navigate("graphic") {
                    launchSingleTop = true
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .heightIn(min = 48.dp)
                .clip(RoundedCornerShape(0.dp, 0.dp, 50.dp, 50.dp)), //botón ovalado desde abajo.
            colors = ButtonDefaults.buttonColors(SoftYellow)
        ) {
            Icon(Icons.Default.Info, contentDescription = "graphical")
            Spacer(modifier = Modifier.width(4.dp))
            Text("  Gráficas")
        }
    }
}

@Composable
fun GlucoseInput(glucoseValue: Int, onValueChange: (Int) -> Unit) {
    val scale = 1.15f // Porcentaje de escala en este es un 15% más grande de 1.0

    var isError by remember { mutableStateOf(false) }

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
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            isError = isError,
            textStyle = TextStyle(color = Color.White, fontSize = 24.sp * scale),
            modifier = Modifier
                .background(BackgroundGrey)
                .fillMaxWidth()
        )
    }

    Spacer(modifier = Modifier.height(16.dp * scale))
}

@Composable
fun LastMeasure(lastMeasurement: Int?, glucoseRepository: GlucoseRepository) {
    val textColor = remember { mutableStateOf(Color.Red.copy(alpha = 0.8f)) }
    val glucoseLevelsImp = remember { GlucoseLevelsImp(glucoseRepository) }

    val levelMax = glucoseLevelsImp.getLevelMaxDB()
    val levelMin = glucoseLevelsImp.getLevelMinDB()

    lastMeasurement?.let { measurement ->
        if (levelMin < levelMax) {
            textColor.value = when {
                measurement in levelMin..levelMax -> SoftGreen
                else -> SoftRed
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .offset(x = 0.dp, y = 100.dp)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
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
                        color = textColor.value,
                        fontSize = 8.em,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
}

package ismaapp.tortosa.glucoseregister.ui.screen

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import ismaapp.tortosa.glucoseregister.entities.GlucoseLevels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ismaapp.tortosa.glucoseregister.repository.GlucoseRepository
import ismaapp.tortosa.glucoseregister.services.GlucoseLevelsImp
import ismaapp.tortosa.glucoseregister.services.IGlucoseLevels
import ismaapp.tortosa.glucoseregister.utils.SuccessfulMessage
import kotlinx.coroutines.delay

@Composable
fun GlucoseConfigurationScreen(glucoseLevels: IGlucoseLevels, glucoseRepository: GlucoseRepository) {
    var glucoseLevelsUpdate by remember { mutableStateOf(glucoseLevels.levels ?: GlucoseLevels(130, 80)) }
    var showMessage by remember { mutableStateOf(false) }
    var isOperationSuccessful by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

    // El mensaje desaparecerá después del tiempo indicado.
    LaunchedEffect(showMessage) {
        if (showMessage) {
            delay(5000) // 5 segundos.
            showMessage = false // false para que desaparezca.
        }
    }

    GlucoseConfiguration(glucoseLevelsUpdate) { updatedLevels ->
        //Actualiza los niveles de glucosa con los nuevos valores.
        glucoseLevelsUpdate = updatedLevels

        //Actualiza los valores en la base de datos.
        val glucoseLevelsImp = GlucoseLevelsImp(glucoseRepository)
        glucoseLevelsImp.updateLevels(glucoseLevelsUpdate)

        isOperationSuccessful = glucoseLevelsImp.isLevelSuccess
        message = if (isOperationSuccessful) "Valores añadidos correctamente" else "ERROR: Valores no añadidos"
        showMessage = true

    }

    //Muestra el mensaje de confirmación.
    if (showMessage) {
        SuccessfulMessage(showMessage = showMessage, isMeasurementSuccessful = isOperationSuccessful, message = message)
    }

    //Obtiene y muestra los niveles de la tabla de niveles.
    val allGlucoseLevels = glucoseLevels.allGlucoseLevels
    for (levels in allGlucoseLevels) {
        Log.d("GlucoseConfigurationScreen", "ID: ${levels.id}, Max: ${levels.levelMax}, Min: ${levels.levelMin}")
    }
}

@Composable
fun GlucoseConfiguration(
    glucoseLevels: GlucoseLevels,
    onValuesChanged: (GlucoseLevels) -> Unit
) {
    var newLevelMax by remember { mutableIntStateOf(glucoseLevels.levelMax) }
    var newLevelMin by remember { mutableIntStateOf(glucoseLevels.levelMin) }
    var showMessage by remember { mutableStateOf(false) }
    var isOperationSuccessful by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

    val paddingSpace = 8.dp

    // El mensaje desaparecerá después del tiempo indicado.
    LaunchedEffect(showMessage) {
        if (showMessage) {
            delay(5000) // 5 segundos.
            showMessage = false // false para que desaparezca.
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(paddingSpace)
    ) {
        Text(
            "CONFIGURACIÓN DE LOS NIVELES",
            style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold),
            color = Color.White,
            modifier = Modifier.padding(bottom = paddingSpace) //Espacio inferior.
        )

        Text(
            "Configuración nivel máximo: ",
            style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold),
            color = Color.White,
            modifier = Modifier.padding(bottom = paddingSpace) //Espacio inferior.
        )

        Box(modifier = Modifier
            .fillMaxWidth(0.6f) //Ocupa el 80% del ancho disponible.
            .padding(horizontal = 16.dp)) {
            GlucoseInput(
                glucoseValue = newLevelMax,
                onValueChange = { newValue ->
                    newLevelMax = newValue
                }
            )
        }

        Text(
            "Configuración nivel mínimo: ",
            style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold),
            color = Color.White,
            modifier = Modifier.padding(bottom = paddingSpace) //Espacio inferior.
        )

        Box(modifier = Modifier
            .fillMaxWidth(0.6f)
            .padding(horizontal = 16.dp)) {
            GlucoseInput(
                glucoseValue = newLevelMin,
                onValueChange = { newValue ->
                    newLevelMin = newValue
                }
            )
        }

        //Separación adicional solo para el botón.
        Spacer(modifier = Modifier.height(paddingSpace))

        //Botón para confirmar los cambios
        Button(
            onClick = {
                try {
                    glucoseLevels.levelMax = newLevelMax
                    glucoseLevels.levelMin = newLevelMin
                    glucoseLevels.setLevels(newLevelMax, newLevelMin)
                    onValuesChanged(glucoseLevels)
                } catch (e: IllegalArgumentException) {
                    isOperationSuccessful = false
                    message = e.message ?: "Error desconocido"
                }
                showMessage = true
            },
            modifier = Modifier.align(Alignment.CenterHorizontally)
                .width(200.dp) //ancho del botón.
                .height(50.dp) //alto del botón.
        ) {
            Text("Guardar cambios", style = TextStyle(fontSize = 18.sp))
        }
        // Muestra el mensaje de confirmación o error.
        if (showMessage) {
            SuccessfulMessage(showMessage = showMessage, isMeasurementSuccessful = isOperationSuccessful, message = message)
        }
    }
}

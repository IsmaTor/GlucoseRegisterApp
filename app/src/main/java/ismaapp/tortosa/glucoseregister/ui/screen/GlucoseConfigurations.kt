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
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ismaapp.tortosa.glucoseregister.repository.GlucoseRepository
import ismaapp.tortosa.glucoseregister.services.GlucoseLevelsImp
import ismaapp.tortosa.glucoseregister.services.IGlucoseLevels
import ismaapp.tortosa.glucoseregister.ui.theme.SoftYellow
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

    GlucoseConfiguration(glucoseLevelsUpdate, glucoseRepository) { updatedLevels ->
        //Actualiza los niveles de glucosa con los nuevos valores.
        glucoseLevelsUpdate = updatedLevels

        //Actualiza los valores en la base de datos.
        val glucoseLevelsImp = GlucoseLevelsImp(glucoseRepository)
        glucoseLevelsImp.updateLevels(glucoseLevelsUpdate)

        isOperationSuccessful = glucoseLevelsImp.isLevelSuccess
        message = if (isOperationSuccessful) "Valores añadidos correctamente" else "ERROR: Valores no añadidos"
        Log.d("GlucoseConfigurationScreen", "Update Result - Success: $isOperationSuccessful, Message: $message")
        showMessage = true

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
    glucoseRepository: GlucoseRepository,
    onValuesChanged: (GlucoseLevels) -> Unit
) {
    var newLevelMax by remember { mutableStateOf(glucoseLevels.levelMax) }
    var newLevelMin by remember { mutableStateOf(glucoseLevels.levelMin) }
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
            modifier = Modifier.padding(bottom = paddingSpace)
        )

        Text(
            "Configuración nivel máximo: ",
            style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold),
            color = Color.White,
            modifier = Modifier.padding(bottom = paddingSpace)
        )

        Box(modifier = Modifier
            .fillMaxWidth(0.6f)
            .padding(horizontal = 16.dp)) {
            GlucoseInput(
                glucoseValue = newLevelMax,
                onValueChange = { newValue ->
                    newLevelMax = newValue
                    Log.d("GlucoseConfiguration", "Nivel máximo actualizado a: $newLevelMax")
                }
            )
        }

        Text(
            "Configuración nivel mínimo: ",
            style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold),
            color = Color.White,
            modifier = Modifier.padding(bottom = paddingSpace)
        )

        Box(modifier = Modifier
            .fillMaxWidth(0.6f)
            .padding(horizontal = 16.dp)) {
            GlucoseInput(
                glucoseValue = newLevelMin,
                onValueChange = { newValue ->
                    newLevelMin = newValue
                    Log.d("GlucoseConfiguration", "Nivel mínimo actualizado a: $newLevelMin")
                }
            )
        }

        Spacer(modifier = Modifier.height(paddingSpace))

        Button(
            onClick = {
                try {
                    // Crear una nueva instancia de GlucoseLevels con los valores actualizados
                    val updatedLevels = GlucoseLevels(newLevelMax, newLevelMin).apply {
                        id = glucoseLevels.id // Si es necesario
                    }

                    Log.d("GlucoseConfiguration", "Intentando guardar - Max: $newLevelMax, Min: $newLevelMin")

                    val glucoseLevelsImp = GlucoseLevelsImp(glucoseRepository)
                    glucoseLevelsImp.updateLevels(updatedLevels)

                    isOperationSuccessful = glucoseLevelsImp.isLevelSuccess
                    message = if (isOperationSuccessful) "Valores añadidos correctamente" else "ERROR: Valores no añadidos"
                    Log.d("GlucoseConfiguration", "Update Result - Success: $isOperationSuccessful, Message: $message")

                    onValuesChanged(updatedLevels)
                } catch (e: IllegalArgumentException) {
                    isOperationSuccessful = false
                    message = e.message ?: "Error desconocido"
                    Log.e("GlucoseConfiguration", "Error al guardar niveles: ${e.message}", e)
                }
                showMessage = true
            },
            modifier = Modifier.align(Alignment.CenterHorizontally)
                .width(200.dp)
                .height(50.dp)
                .clip(CutCornerShape(20.dp, 20.dp, 20.dp, 20.dp)),
            colors = ButtonDefaults.buttonColors(Color.Yellow) // Cambiar a SoftYellow si está definido en otro lugar.
        ) {
            Text("Guardar cambios", style = TextStyle(fontSize = 18.sp))
        }

        if (showMessage) {
            SuccessfulMessage(showMessage = showMessage, isMeasurementSuccessful = isOperationSuccessful, message = message)
        }
    }
}


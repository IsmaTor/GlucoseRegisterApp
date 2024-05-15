package ismaapp.tortosa.glucoseregister.ui.screen

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import ismaapp.tortosa.glucoseregister.entities.GlucoseLevels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
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

@Composable
fun GlucoseConfigurationScreen() {
    var glucoseLevels by remember { mutableStateOf(GlucoseLevels()) }

    // Log para verificar los valores iniciales de glucoseLevels
    Log.d("GlucoseConfigurationScreen", "GlucoseLevels inicial: $glucoseLevels")

    GlucoseConfiguration(glucoseLevels) { updatedLevels ->
        // Actualiza los niveles de glucosa con los nuevos valores
        glucoseLevels = updatedLevels

    // Log para verificar los niveles de glucosa actualizados
    Log.d("GlucoseConfigurationScreen", "GlucoseLevels actualizados: $updatedLevels")
    }
}


@Composable
fun GlucoseConfiguration(
    glucoseLevels: GlucoseLevels,
    onValuesChanged: (GlucoseLevels) -> Unit
) {
    var newLevelMax by remember { mutableIntStateOf(glucoseLevels.levelMax) }
    var newLevelMin by remember { mutableIntStateOf(glucoseLevels.levelMin) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "CONFIGURACIÓN DE LOS NIVELES",
            style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold),
            color = Color.White,
            modifier = Modifier.padding(bottom = 20.dp) //Espacio inferior.
        )

        Text(
            "Configuración nivel máximo: ",
            style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold),
            color = Color.White,
            modifier = Modifier.padding(bottom = 20.dp) //Espacio inferior.
        )

        GlucoseInput(
            glucoseValue = newLevelMax,
            onValueChange = { newValue ->
                newLevelMax = newValue
            }
        )

        Text(
            "Configuración nivel mínimo: ",
            style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold),
            color = Color.White,
            modifier = Modifier.padding(bottom = 20.dp) //Espacio inferior.
        )

        GlucoseInput(
            glucoseValue = newLevelMin,
            onValueChange = { newValue ->
                newLevelMin = newValue
            }
        )

        //Botón para confirmar los cambios
        Button(
            onClick = {
                glucoseLevels.levelMax = newLevelMax
                glucoseLevels.levelMin = newLevelMin

                // Log para verificar los valores antes de llamar a onValuesChanged
                Log.d("GlucoseConfiguration", "Nuevos niveles de glucosa: $glucoseLevels")

                onValuesChanged(glucoseLevels)

                // Log para verificar cuándo se llama a onValuesChanged y qué valores se pasan
                Log.d("GlucoseConfiguration", "Llamado a onValuesChanged con: $glucoseLevels")
            },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Guardar cambios")
        }
    }
}


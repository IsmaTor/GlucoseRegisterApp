package ismaapp.tortosa.glucoseregister.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ismaapp.tortosa.glucoseregister.entities.GlucoseLevels
import ismaapp.tortosa.glucoseregister.services.TimeRangeServiceImp
import ismaapp.tortosa.glucoseregister.services.IGlucoseService
import ismaapp.tortosa.glucoseregister.utils.buttonModifier

@Composable
fun GlucoseTimeRangeScreen(glucoseService: IGlucoseService, glucoseLevels: GlucoseLevels) {
    val calculator = remember {
        TimeRangeServiceImp(
            glucoseService,
            glucoseLevels
        )
    }
    val (intervalHours, setIntervalHours) = remember { mutableStateOf(24) }
    val (glucosePercentagesByCategory, setGlucosePercentagesByCategory) = remember { mutableStateOf<Map<String, Double>>(emptyMap()) }
    val (showMessage, setShowMessage) = remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "Elegir Tiempo de Rango",
            style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold),
            color = Color.White
        )

        Button(
            onClick = {
                val currentInterval = 1
                setIntervalHours(currentInterval)
                val percentages = calculator.calculateGlucosePercentageByCategory(currentInterval)
                setGlucosePercentagesByCategory(percentages)
                setShowMessage(percentages.isEmpty())
            },
            buttonModifier(selected = intervalHours == 1)
        ) {
            Text("1 HORA", color = Color.White)
        }

        Button(
            onClick = {
                val currentInterval = 24
                setIntervalHours(currentInterval)
                val percentages = calculator.calculateGlucosePercentageByCategory(currentInterval)
                setGlucosePercentagesByCategory(percentages)
                setShowMessage(percentages.isEmpty())
            },
            buttonModifier(selected = intervalHours == 24)
        ) {
            Text("24 HORAS", color = Color.White)
        }

        Button(
            onClick = {
                val currentInterval = 168
                setIntervalHours(currentInterval)
                val percentages = calculator.calculateGlucosePercentageByCategory(currentInterval)
                setGlucosePercentagesByCategory(percentages)
                setShowMessage(percentages.isEmpty())
            },
            buttonModifier(selected = intervalHours == 168)
        ) {
            Text("7 DÍAS", color = Color.White)
        }

        Button(
            onClick = {
                val currentInterval = 720
                setIntervalHours(currentInterval)
                val percentages = calculator.calculateGlucosePercentageByCategory(currentInterval)
                setGlucosePercentagesByCategory(percentages)
                setShowMessage(percentages.isEmpty())
            },
            buttonModifier(selected = intervalHours == 720)
        ) {
            Text("30 DÍAS", color = Color.White)
        }

        // Mostrar mensaje cuando no hay mediciones en el rango de tiempo escogido.
        if (showMessage) {
            Text(
                "No hay mediciones en este rango de tiempo.",
                style = TextStyle(fontSize = 16.sp),
                color = Color.White
            )
        }

        // Mostrar los resultados de los porcentajes solo cuando el estado cambia.
        if (glucosePercentagesByCategory.isNotEmpty()) {
            GlucosePercentageList(glucosePercentagesByCategory)
        }  else {
            Text(" ", color = Color.White)
        }
    }
}

@Composable
fun GlucosePercentageList(glucosePercentagesByCategory: Map<String, Double>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            "Resultados de Porcentajes",
            style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold),
            color = Color.White
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Mostrar cada categoría con su porcentaje correspondiente.
        glucosePercentagesByCategory.forEach { (category, percentage) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$category:",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(120.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Barra de progreso para mejor visualización.
                LinearProgressIndicator(
                    progress = percentage.toFloat() / 100f, // Convertir el porcentaje a una fracción.
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Mostrar el porcentaje.
                Text(
                    text = "${"%.2f".format(percentage)}%",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Divider(color = Color.Gray, thickness = 1.dp) // Separador entre elementos.
        }
    }
}

package ismaapp.tortosa.glucoseregister.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ismaapp.tortosa.glucoseregister.repository.GlucoseRepository
import ismaapp.tortosa.glucoseregister.services.GlucosePercentageCalculator

@Composable
fun GlucoseTimeRangeScreen(glucoseRepository: GlucoseRepository) {
    val calculator = remember { GlucosePercentageCalculator(glucoseRepository) }
    val (intervalHours, setIntervalHours) = remember { mutableStateOf(24) }
    val (glucosePercentagesByCategory, setGlucosePercentagesByCategory) = remember { mutableStateOf<Map<String, Double>>(emptyMap()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "Configurar Tiempo de Rango",
            style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold),
            color = Color.White
        )

        Button(
            onClick = {
                setIntervalHours(1)
                val percentages = calculator.calculateGlucosePercentageByCategory(1)
                setGlucosePercentagesByCategory(percentages)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .heightIn(min = 24.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(color = if (intervalHours == 1) Color.Blue else Color.Gray)
        ) {
            Text("Cada 1 minuto", color = Color.White)
        }

        Button(
            onClick = {
                setIntervalHours(24)
                val percentages = calculator.calculateGlucosePercentageByCategory(24)
                setGlucosePercentagesByCategory(percentages)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .heightIn(min = 24.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(color = if (intervalHours == 24) Color.Blue else Color.Gray)
        ) {
            Text("Cada 24 horas", color = Color.White)
        }

        // Mostrar los resultados de los porcentajes solo cuando el estado cambia
        if (glucosePercentagesByCategory.isNotEmpty()) {
            GlucosePercentageList(glucosePercentagesByCategory)
        }
    }
}


@Composable
fun GlucosePercentageList(glucosePercentagesByCategory: Map<String, Double>) {
    Column {
        glucosePercentagesByCategory.forEach { (category, percentage) ->
            Text(
                "Porcentaje de $category: ${"%.2f".format(percentage)}%",
                color = Color.White,
                fontSize = 16.sp
            )
        }
    }
}

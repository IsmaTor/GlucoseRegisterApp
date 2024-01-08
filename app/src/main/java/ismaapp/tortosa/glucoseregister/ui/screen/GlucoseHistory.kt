package ismaapp.tortosa.glucoseregister.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import ismaapp.tortosa.glucoseregister.entity.GlucoseMeasurement
import ismaapp.tortosa.glucoseregister.services.IGlucoseServices

@Composable
fun GlucoseHistoryScreen(glucoseService: IGlucoseServices, pageNumber: Int, navController: NavController) {
    var glucoseMeasurements by remember { mutableStateOf<List<GlucoseMeasurement>>(emptyList()) }
    var orderByLatest by remember { mutableStateOf(true) }
    var orderByDateDescending by remember { mutableStateOf(true) }

    val darkRed = Color(0xFF800000)
    val pageSize = 20
    val startIndex = (pageNumber - 1) * pageSize
    val endIndex = startIndex + pageSize

    // Obtener las mediciones al cargar la página actual
    glucoseMeasurements = glucoseService.getPaginatedGlucoseMeasurements(startIndex, endIndex, orderByLatest)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Historial de Glucosa", style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold), color = Color.White)

        // Botón para borrar todas las mediciones
        Button(
            onClick = {
                glucoseService.deleteAllGlucoseMeasurements()
                glucoseMeasurements = emptyList()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp)
                .heightIn(min = 24.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(color = darkRed)
        ) {
            Text("Borrar Todas las Mediciones", color = darkRed)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = {
                    if (pageNumber > 1) {
                        navController.navigate("historialPaginado/${pageNumber - 1}")
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 24.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                Text("Anterior")
            }

            Button(
                onClick = {
                    navController.navigate("historialPaginado/${pageNumber + 1}")
                },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 24.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                Text("Siguiente")
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.LightGray)
                    .padding(4.dp)
            ) {
                // Botón de ordenamiento para Fecha
                Text(
                    text = "Fecha",
                    modifier = Modifier
                        .weight(1f)
                        .padding(4.dp)
                        .clickable {
                            orderByLatest = !orderByLatest
                            orderByDateDescending = !orderByDateDescending
                            glucoseMeasurements = glucoseService.getPaginatedGlucoseMeasurements(
                                startIndex,
                                endIndex,
                                orderByLatest
                            )
                        },
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        color = LocalContentColor.current
                    )
                )

                Text("ID", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text("Registro", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            }

            glucoseMeasurements.forEach { measurement ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                ) {
                    Text(measurement.date, modifier = Modifier.weight(1f), color = Color.White)
                    Text(measurement.id.toString(), modifier = Modifier.weight(1f), color = Color.White)
                    Text(measurement.glucoseValue.toString(), modifier = Modifier.weight(1f), color = Color.White)
                }
            }
        }
    }
}
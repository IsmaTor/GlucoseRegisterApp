package ismaapp.tortosa.glucoseregister.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import ismaapp.tortosa.glucoseregister.entities.GlucoseMeasurement
import ismaapp.tortosa.glucoseregister.services.IGlucoseService
import ismaapp.tortosa.glucoseregister.ui.theme.SoftYellow

@Composable
fun GlucoseHistoryScreen(
    glucoseService: IGlucoseService,
    pageNumber: Int,
    navController: NavController,
    orderByLatest: Boolean,
    orderByOldest: Boolean,
    orderByHighestGlucose: Boolean,
    orderByLowestGlucose: Boolean,
    onOrderByLatestChanged: (Boolean) -> Unit,
    onOrderByOldestChanged: (Boolean) -> Unit,
    onOrderByHighestGlucoseChanged: (Boolean) -> Unit,
    onOrderByLowestGlucoseChanged: (Boolean) -> Unit
) {

    var userSelection by remember { mutableStateOf("FECHA") }
    var glucoseMeasurements by remember { mutableStateOf<List<GlucoseMeasurement>>(emptyList()) }
    val pageSize = 12
    val startIndex = (pageNumber - 1) * pageSize
    val calculatedPageNumber = (startIndex / pageSize) + 1

    // Obtener las mediciones al cargar la página actual
    glucoseMeasurements = glucoseService.getPaginatedGlucoseMeasurements(calculatedPageNumber, pageSize, orderByLatest, orderByHighestGlucose, userSelection)

    //Obtener el número total de registros.
    val totalRecords = glucoseService.totalGlucoseMeasurements
    val totalPages = (totalRecords + pageSize -1) / pageSize

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "Historial de Glucosa",
            style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold),
            color = Color.White
        )

        //Botón para mostrar opciones de borrar o imprimir.
        Button(
            onClick = {
                navController.navigate("options")
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(2.dp)
                .heightIn(min = 24.dp)
                .clip(RoundedCornerShape(50.dp, 50.dp, 0.dp, 0.dp)),
            colors = ButtonDefaults.buttonColors(SoftYellow)
        ) {
            Icon(Icons.Filled.Menu, contentDescription = "Menú")
            Text("   OPCIONES", color = Color.White)

        }

        //Botón para el tiempo de rango.
        Button(
            onClick = {
                navController.navigate("rangeTime")
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(2.dp)
                .heightIn(min = 24.dp)
                .clip(RoundedCornerShape(0.dp, 0.dp, 50.dp, 50.dp)),
            colors = ButtonDefaults.buttonColors(SoftYellow)
        ) {
            Text("   VER TIEMPO DE RANGO", color = Color.White)
        }

        LazyColumn {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.LightGray)
                        .padding(4.dp)
                ) {
                    // Botón de ordenamiento para Fecha
                    Text(
                        text = "   ",
                        modifier = Modifier
                            .weight(0.5f))

                    Text(
                        text = "FECHA",
                        modifier = Modifier
                            .weight(1f)
                            .padding(4.dp)
                            .clickable {
                                userSelection = "FECHA"
                                onOrderByLatestChanged(!orderByLatest)
                                onOrderByOldestChanged(!orderByOldest)
                            },
                        style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            color = LocalContentColor.current
                        )
                    )

                    Text(
                        text = "REGISTRO",
                        modifier = Modifier
                            .weight(1f)
                            .padding(4.dp)
                            .clickable {
                                userSelection = "REGISTRO"
                                onOrderByLatestChanged(false)
                                onOrderByOldestChanged(false)
                                onOrderByHighestGlucoseChanged(!orderByHighestGlucose)
                                onOrderByLowestGlucoseChanged(!orderByLowestGlucose)
                            },
                        style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            color = LocalContentColor.current
                        )
                    )

                }
                Divider(color = Color.Gray, thickness = 1.dp, modifier = Modifier.fillMaxWidth())
            }

            itemsIndexed(glucoseMeasurements) { index, measurement ->
                GlucoseRow(startIndex + index + 1, measurement)
                Divider(color = Color.Gray, thickness = 1.dp, modifier = Modifier.fillMaxWidth())
            }
        }
        //llamada a los botones de navegación.
        NavigationButtons(pageNumber = pageNumber, navController = navController, totalPages = totalPages)
    }
}

@Composable
fun NavigationButtons(
    pageNumber: Int,
    navController: NavController,
    totalPages: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Button(
            onClick = {
                if (pageNumber > 1) {
                    navController.navigate("historial/${pageNumber - 1}")
                }
            },
            modifier = Modifier
                .weight(1.1f)
                .heightIn(min = 24.dp)
                .padding(7.dp)
                .clip(RoundedCornerShape(8.dp)),
            colors = ButtonDefaults.buttonColors(SoftYellow)
        ) {
            Text("ANTERIOR",
                fontSize = 14.sp) //Tamaño del texto.
        }

        Button(
            onClick = {
                navController.popBackStack("glucoseMeasurement", inclusive = false)
            },
            modifier = Modifier
                .weight(0.7f)
                .heightIn(min = 24.dp)
                .padding(8.dp)
                .clip(RoundedCornerShape(8.dp)),
            colors = ButtonDefaults.buttonColors(SoftYellow)
        ) {
            Icon(Icons.Filled.Home, contentDescription = "Home")
        }

        Button(
            onClick = {
                navController.navigate("historial/${pageNumber + 1}")
            },
            modifier = Modifier
                .weight(1.1f)
                .heightIn(min = 24.dp)
                .padding(7.dp)
                .clip(RoundedCornerShape(8.dp)),
            colors = ButtonDefaults.buttonColors(SoftYellow),
            enabled = pageNumber < totalPages
        ) {
            Text("SIGUIENTE",
                fontSize = 14.sp) //Tamaño del texto.
        }
    }
}


@Composable
fun GlucoseRow(positionNumber: Int, measurement: GlucoseMeasurement) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text(
            positionNumber.toString(),
            modifier = Modifier.weight(0.4f), //Modificador del peso de posición.
            color = Color.White
        )
        Spacer(modifier = Modifier.width(8.dp)) //Modificador del espacio entre posición y Fecha.
        Text(
            measurement.date,
            modifier = Modifier.weight(2f), //Modificador del peso de Fecha.
            color = Color.White
        )
        Spacer(modifier = Modifier.width(16.dp)) //Modificador del espacio entre Fecha y Registro.
        Text(
            measurement.glucoseValue.toString(),
            modifier = Modifier.weight(1.5f), //Modificador del peso de Registro.
            color = Color.White
        )
    }
}

package ismaapp.tortosa.glucoseregister

import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ismaapp.tortosa.glucoseregister.entity.GlucoseMeasurement
import ismaapp.tortosa.glucoseregister.helpers.GlucoseDBHelper
import ismaapp.tortosa.glucoseregister.repository.GlucoseRepository
import ismaapp.tortosa.glucoseregister.services.GlucoseServicesImp
import ismaapp.tortosa.glucoseregister.services.IGlucoseServices
import ismaapp.tortosa.glucoseregister.utils.DateUtils

class MainActivity : ComponentActivity() {
    private lateinit var databaseGlucose: SQLiteDatabase
    private lateinit var glucoseRepository: GlucoseRepository
    private lateinit var glucoseService: IGlucoseServices

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        databaseGlucose = GlucoseDBHelper(this).writableDatabase
        glucoseRepository = GlucoseRepository(databaseGlucose)
        glucoseService = GlucoseServicesImp(glucoseRepository)

        setContent {
            MaterialTheme {
                LoadingScreen(onLoadingComplete = {
                    setContent {

                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = Color.DarkGray
                        ) {
                            val navController = rememberNavController()

                            NavHost(
                                navController = navController,
                                startDestination = "glucoseMeasurement"
                            ) {
                                composable("glucoseMeasurement") {
                                    GlucoseMeasurementScreen(glucoseService, navController)
                                }
                                composable("historial/{pageNumber}") { backStackEntry ->
                                    val pageNumber = backStackEntry.arguments?.getString("pageNumber")?.toInt() ?: 1
                                    GlucoseHistoryScreen(glucoseService, pageNumber, navController)
                                }
                                composable("historialPaginado/{pageNumber}") { backStackEntry ->
                                    val pageNumber = backStackEntry.arguments?.getString("pageNumber")?.toInt() ?: 1
                                    GlucoseHistoryScreen(glucoseService, pageNumber, navController)
                                }
                            }
                        }
                    }
                })
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        databaseGlucose.close()
    }
}


@Composable
fun LoadingScreen(onLoadingComplete: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Cargando...", color = Color.White, fontSize = 18.sp)

            CircularProgressIndicator(
                modifier = Modifier
                    .padding(8.dp)
                    .size(36.dp),
                color = Color.White
            )
        }
    }

    onLoadingComplete()
}

@Composable
fun GlucoseMeasurementScreen(glucoseService: IGlucoseServices, navController: NavController) {
    var glucoseValue by remember { mutableStateOf(0f) }
    var isMeasurementSuccessful by remember { mutableStateOf(false) }
    var showMessage by remember { mutableStateOf(false) }

    val date = DateUtils.getFormattedDate()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(100.dp)
    ) {
        Text(
            "Indique la medición:",
            style = TextStyle(color = Color.White)
        )
        Spacer(modifier = Modifier.height(8.dp))

        var isError by remember { mutableStateOf(false) }
        var message by remember { mutableStateOf("") }

        OutlinedTextField(
            value = glucoseValue.takeIf { it != 0f }?.toString() ?: "",
            onValueChange = { newValue ->
                isError = false

                // Permitir solo un punto decimal y números
                val regex = Regex("""^-?\d*\.?\d*$""")
                if (newValue.isBlank() || regex.matches(newValue)) {
                    glucoseValue = newValue.toFloatOrNull() ?: 0f
                }
            },
            label = { Text("Ingrese el valor de glucosa", modifier = Modifier.align(Alignment.CenterHorizontally)) },
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Decimal
            ),
            isError = isError,

            textStyle = TextStyle(color = Color.White)
        )

        Spacer(modifier = Modifier.height(16.dp))

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

                    //ejemplo para más adelante añadir un registro correcto
                    if (glucoseValue >= 80 && glucoseValue <= 120) {
                        isMeasurementSuccessful = true
                        showMessage = true
                        message = "Medición registrada correctamente"
                    } else {
                        isMeasurementSuccessful = false
                        showMessage = true
                        message = "Medición no registrada correctamente"
                    }
                },
                modifier = Modifier.fillMaxWidth()
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
                    //lógica para código de estadísticas.
                    glucoseService.getPaginatedGlucoseMeasurements(0, 20, true)
                    navController.navigate("historial/1") {
                        launchSingleTop = true
                    }
                },
                modifier = Modifier.fillMaxWidth()
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

        Text(
            "Fecha: $date",
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(16.dp)
                .background(Color.LightGray)
        )
    }
}

@Composable
fun GlucoseHistoryScreen(glucoseService: IGlucoseServices, pageNumber: Int, navController: NavController) {
    var glucoseMeasurements by remember { mutableStateOf<List<GlucoseMeasurement>>(emptyList()) }
    var orderByLatest by remember { mutableStateOf(true) }
    var orderByDateDescending by remember { mutableStateOf(true) }
    val GranateColor = Color(0xFF800000)

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
                // Lógica para borrar todas las mediciones
                glucoseService.deleteAllGlucoseMeasurements()
                // Limpiar la lista local de mediciones
                glucoseMeasurements = emptyList()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp)
                .heightIn(min = 24.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(color = GranateColor)
        ) {
            Text("Borrar Todas las Mediciones", color = GranateColor)
        }

        // Botón para cargar más mediciones
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = {
                    // Navegar a la página anterior con la página anterior
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
                    // Navegar a la siguiente página con la página siguiente
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

        // Tabla para mostrar las mediciones
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            // Encabezado de la tabla
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
                            // Lógica para el clic en "Fecha" (si es necesario)
                            orderByLatest = !orderByLatest
                            orderByDateDescending = !orderByDateDescending
                            // Actualizar la lista al cambiar la orientación de la ordenación
                            glucoseMeasurements = glucoseService.getPaginatedGlucoseMeasurements(
                                startIndex,
                                endIndex,
                                orderByLatest
                            )
                        },
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        color = LocalContentColor.current // para mantener el color del tema
                    )
                )

                Text("ID", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text("Registro", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            }

            // Filas de la tabla
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







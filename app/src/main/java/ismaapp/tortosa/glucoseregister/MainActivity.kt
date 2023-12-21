package ismaapp.tortosa.glucoseregister

import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

class MainActivity : ComponentActivity() {
    private lateinit var databaseGlucose: SQLiteDatabase
    private lateinit var glucoseRepository: GlucoseRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        databaseGlucose = GlucoseDBHelper(this).writableDatabase
        glucoseRepository = GlucoseRepository(databaseGlucose)

        setContent {
            MaterialTheme {
                LoadingScreen(onLoadingComplete = {
                    setContent {
                        val navController = rememberNavController()

                        NavHost(
                            navController = navController,
                            startDestination = "glucoseMeasurement"
                        ) {
                            composable("glucoseMeasurement") {
                                GlucoseMeasurementScreen(glucoseRepository, navController)
                            }
                            composable("historial") {
                                val glucoseMeasurements = glucoseRepository.getPaginatedGlucoseMeasurements(0, 20)
                                GlucoseHistoryScreen(glucoseMeasurements)
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
fun GlucoseMeasurementScreen(glucoseRepository: GlucoseRepository, navController: NavController) {
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
            value = glucoseValue.toString(),
            onValueChange = {
                isError = false
                glucoseValue = it.toFloatOrNull() ?: 0f
            },
            label = { Text("Ingrese el valor de glucosa", modifier = Modifier.align(Alignment.CenterHorizontally)) },
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Number
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
                    glucoseRepository.insertGlucoseMeasurement(glucoseValue)
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
                   glucoseRepository.getPaginatedGlucoseMeasurements(0, 20)
                    navController.navigate("historial") {
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
fun GlucoseHistoryScreen(glucoseMeasurements: List<GlucoseMeasurement>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Historial de Glucosa", style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold))

        LazyColumn {
            items(glucoseMeasurements.sortedByDescending { it.date }) { measurement ->
                Text("ID: ${measurement.id}, Glucosa: ${measurement.glucoseValue}, Fecha: ${measurement.date}")
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}




package ismaapp.tortosa.glucoseregister

import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ismaapp.tortosa.glucoseregister.helpers.GlucoseDBHelper
import ismaapp.tortosa.glucoseregister.repository.GlucoseRepository
import ismaapp.tortosa.glucoseregister.services.GlucoseServicesImp
import ismaapp.tortosa.glucoseregister.services.IGlucoseServices
import ismaapp.tortosa.glucoseregister.ui.screen.GlucoseHistoryScreen
import ismaapp.tortosa.glucoseregister.ui.screen.GlucoseMeasurementScreen
import ismaapp.tortosa.glucoseregister.ui.screen.LoadingScreen

class MainActivity : ComponentActivity() {
    private lateinit var databaseGlucose: SQLiteDatabase
    private lateinit var glucoseRepository: GlucoseRepository
    private lateinit var glucoseService: IGlucoseServices

    private var orderByLatest by mutableStateOf(true)
    private var orderByDateDescending by mutableStateOf(true)

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
                                    GlucoseHistoryScreen(
                                        glucoseService,
                                        pageNumber,
                                        navController,
                                        orderByLatest,
                                        orderByDateDescending,
                                        onOrderByLatestChanged = { orderByLatest = it },
                                        onOrderByDateDescendingChanged = { orderByDateDescending = it }
                                    )
                                }
                                composable("historialPaginado/{pageNumber}") { backStackEntry ->
                                    val pageNumber = backStackEntry.arguments?.getString("pageNumber")?.toInt() ?: 1
                                    GlucoseHistoryScreen(
                                        glucoseService,
                                        pageNumber,
                                        navController,
                                        orderByLatest,
                                        orderByDateDescending,
                                        onOrderByLatestChanged = { orderByLatest = it },
                                        onOrderByDateDescendingChanged = { orderByDateDescending = it }
                                    )
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

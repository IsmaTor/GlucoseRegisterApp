package ismaapp.tortosa.glucoseregister

import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.Manifest
import android.util.Log
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
import ismaapp.tortosa.glucoseregister.services.GlucoseServiceImp
import ismaapp.tortosa.glucoseregister.services.IGlucoseService
import ismaapp.tortosa.glucoseregister.ui.screen.GlucoseHistoryScreen
import ismaapp.tortosa.glucoseregister.ui.screen.GlucoseMeasurementScreen
import ismaapp.tortosa.glucoseregister.ui.screen.LoadingScreen
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import ismaapp.tortosa.glucoseregister.entities.GlucoseLevels
import ismaapp.tortosa.glucoseregister.services.GlucoseLevelsImp
import ismaapp.tortosa.glucoseregister.services.IGlucoseLevels
import ismaapp.tortosa.glucoseregister.services.IPrintService
import ismaapp.tortosa.glucoseregister.services.PrintServiceImp
import ismaapp.tortosa.glucoseregister.ui.screen.GlucoseConfigurationScreen
import ismaapp.tortosa.glucoseregister.ui.screen.GlucoseOptionsScreen
import ismaapp.tortosa.glucoseregister.ui.screen.GraphicDetailScreen
import ismaapp.tortosa.glucoseregister.ui.screen.GlucoseTimeRangeScreen
import ismaapp.tortosa.glucoseregister.ui.screen.GraphicsScreen

class MainActivity : ComponentActivity() {
    private lateinit var databaseGlucose: SQLiteDatabase
    private lateinit var glucoseRepository: GlucoseRepository
    private lateinit var glucoseService: IGlucoseService
    private lateinit var printService: IPrintService
    private lateinit var glucoseLevelsImp: IGlucoseLevels
    private lateinit var glucoseLevels: GlucoseLevels

    private var orderByLatest by mutableStateOf(true)
    private var orderByOldest by mutableStateOf(true)
    private var orderByHighestGlucose by mutableStateOf(true)
    private var orderByLowestGlucose by mutableStateOf(true)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        databaseGlucose = GlucoseDBHelper(this).writableDatabase
        glucoseRepository = GlucoseRepository(databaseGlucose)
        glucoseService = GlucoseServiceImp(glucoseRepository)
        printService = PrintServiceImp(glucoseService)
        glucoseLevelsImp = GlucoseLevelsImp(glucoseRepository)
        glucoseLevels = glucoseLevelsImp.levels

        val dbHelper = GlucoseDBHelper(this)
        dbHelper.createTablesIfNotExists(databaseGlucose)

        // Verificar que la tabla 'levels' se ha creado correctamente
        if (isTableExists(databaseGlucose, GlucoseDBHelper.LEVELS_TABLE_NAME)) {
            Log.d("MainActivity", "La tabla 'levels' existe.")
        } else {
            Log.e("MainActivity", "La tabla 'levels' no existe.")
        }

        // Insertar valores iniciales solo si no existen
        if (glucoseLevelsImp.levels == null) {
            val initialLevels = GlucoseLevels(130, 80)
            glucoseRepository.insertInitialLevels(initialLevels)
        }

        setContent {
            MaterialTheme {
                LoadingScreen(onLoadingComplete = {
                    setContent {

                        Image(
                            painter = painterResource(id = R.drawable.wallpaper),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        val navController = rememberNavController()

                        // Solicitar permisos de descarga si no están concedidos
                        if (!isDownloadPermissionGranted()) {
                            requestDownloadPermission()
                        }

                        NavHost(
                            navController = navController,
                            startDestination = "glucoseMeasurement"
                        ) {
                            composable("glucoseMeasurement") {
                                GlucoseMeasurementScreen(glucoseService, glucoseRepository, navController)
                            }
                            composable("historial/{pageNumber}") { backStackEntry ->
                                val pageNumber =
                                    backStackEntry.arguments?.getString("pageNumber")?.toInt() ?: 1
                                Surface(color = Color.DarkGray) {
                                    GlucoseHistoryScreen(
                                        glucoseService,
                                        pageNumber,
                                        navController,
                                        orderByLatest,
                                        orderByOldest,
                                        orderByHighestGlucose,
                                        orderByLowestGlucose,
                                        onOrderByLatestChanged = { orderByLatest = it },
                                        onOrderByOldestChanged = { orderByOldest = it },
                                        onOrderByHighestGlucoseChanged = {
                                            orderByHighestGlucose = it
                                        },
                                        onOrderByLowestGlucoseChanged = {
                                            orderByLowestGlucose = it
                                        }
                                    )
                                }
                            }
                            composable("rangeTime") {
                                Surface(color = Color.DarkGray) {
                                    GlucoseTimeRangeScreen(glucoseService = glucoseService, glucoseLevels = glucoseLevels)
                                }
                            }
                            composable("graphic") {
                                Surface(color = Color.DarkGray) {
                                    GraphicsScreen(glucoseService = glucoseService, glucoseLevels = glucoseLevels, navController = navController)
                                }
                            }
                            composable("graphicDetail/{intervalHours}") { navBackStackEntry ->
                                val intervalHours = navBackStackEntry.arguments?.getString("intervalHours")?.toInt() ?: 0
                                Surface(
                                    color = Color.DarkGray
                                    ) {
                                    GraphicDetailScreen(glucoseService, glucoseLevels, intervalHours, onNavigateBack = {
                                        navController.popBackStack()
                                    })
                                }
                            }
                            composable("options") {
                                Surface(color = Color.DarkGray) {
                                    GlucoseOptionsScreen(glucoseService = glucoseService, printService = printService, context = applicationContext)
                                }
                            }
                            composable("configuration") {
                                Surface(color = Color.DarkGray) {
                                    GlucoseConfigurationScreen(glucoseLevels = glucoseLevelsImp, glucoseRepository = glucoseRepository)
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

    private fun isDownloadPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestDownloadPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            REQUEST_DOWNLOAD_PERMISSION_CODE
        )
    }

    companion object {
        private const val REQUEST_DOWNLOAD_PERMISSION_CODE = 100
    }

    private fun isTableExists(db: SQLiteDatabase, tableName: String): Boolean {
        val cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name=?", arrayOf(tableName))
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

}

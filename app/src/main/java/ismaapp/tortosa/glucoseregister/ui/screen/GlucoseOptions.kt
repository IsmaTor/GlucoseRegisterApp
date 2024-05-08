package ismaapp.tortosa.glucoseregister.ui.screen

import android.Manifest
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import ismaapp.tortosa.glucoseregister.entities.GlucoseMeasurement
import ismaapp.tortosa.glucoseregister.services.IGlucoseService
import ismaapp.tortosa.glucoseregister.services.IPrintService
import ismaapp.tortosa.glucoseregister.utils.params.DialogOptionsParams
import kotlinx.coroutines.delay

@Composable
fun GlucoseOptionsScreen(
    glucoseService: IGlucoseService,
    printService: IPrintService,
    context: Context
) {
    val deleteAll = " BORRAR REGISTROS"
    val deleteLast = " BORRAR ÚLTIMA"
    val print = " DESCARGAR"
    val darkRed by remember { mutableStateOf(Color(0xFF800000)) }

    var showMessage by remember { mutableStateOf(false) }
    var isMeasurementSuccessful by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

    var glucoseMeasurements by remember { mutableStateOf<List<GlucoseMeasurement>>(emptyList()) }
    var showDialog by remember { mutableStateOf(false) }
    var userSelection by remember { mutableStateOf(deleteAll) }

    // El mensaje desaparecerá después del tiempo indicado.
    LaunchedEffect(showMessage) {
        if (showMessage) {
            delay(5000) // 5 segundos.
            showMessage = false // false para que desaparezca.
        }
    }

    // Función para realizar la descarga después de verificar permisos
    fun performDownload(printService: IPrintService, context: Context) {
        // Realizar la descarga (generar PDF)
        printService.generatePDF(context, glucoseMeasurements)

        // Configurar el mensaje de éxito
        showMessage = true
        isMeasurementSuccessful = printService.isDownloadSuccess
        message = if (isMeasurementSuccessful) "Descarga exitosa" else "Error en la descarga"
    }

    // RequestPermissionLauncher para solicitar WRITE_EXTERNAL_STORAGE
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isPermissionGranted ->
        if (isPermissionGranted) {
            // Permiso concedido, proceder con la descarga
            performDownload(printService, context)
        } else {
            // Permiso denegado, mostrar mensaje de error o realizar alguna acción
            showMessage = true
            isMeasurementSuccessful = false
            message = "Permiso denegado, acepte el permiso de almacenamiento."
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "MENÚ DE OPCIONES",
            style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold),
            color = Color.White,
            modifier = Modifier.padding(bottom = 20.dp) //Espacio inferior.
        )

        Text(
            "Opciones de borrar",
            style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold),
            color = Color.White,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        //Botón para borrar todas las mediciones.
        OptionButtons(
            color = darkRed,
            text = deleteAll,
            onClick = {
                showDialog = true
                userSelection = deleteAll
            } )

        //Botón para borrar última medición.
        OptionButtons(
            color = darkRed,
            text = deleteLast,
            onClick = {
                showDialog = true
                userSelection = deleteLast
            } )

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            "Opciones de descarga",
            style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold),
            color = Color.White,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        //Botón para descargar las mediciones.
        OptionButtons(
            color = Color.DarkGray,
            text = print,
            onClick = {
                showDialog = true
                userSelection = print
            } )

        val dialogOptionsParams = DialogOptionsParams(
            glucoseService = glucoseService,
            printService = printService,
            showDialog = showDialog,
            onDismiss = { showDialog = false },
            onConfirm = {
                when (userSelection) {
                    deleteAll -> {
                        glucoseService.deleteAllGlucoseMeasurements()
                        glucoseMeasurements = emptyList()
                    }
                    deleteLast -> {
                        glucoseService.deleteLastMeasure()
                    }
                    print -> {
                        // Verificar permiso WRITE_EXTERNAL_STORAGE antes de descargar
                        if (ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                        ) {
                            // Permiso concedido, proceder con la descarga
                            performDownload(printService, context)
                        } else {
                            // Permiso no concedido, solicitar permiso al usuario
                            requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        }
                        printService.generatePDF(context, glucoseMeasurements)
                    }
                }
                showDialog = false
            },
            userSelection = userSelection,
            isDatabaseEmptyOrNull = glucoseService::isDatabaseEmptyOrNull,
            onMeasurementsDeleted = { isSuccess, newMessage ->
                isMeasurementSuccessful = isSuccess
                showMessage = true
                message = newMessage
            }
        )
        
        ConfirmDeleteDialogOptions(dialogOptionsParams = dialogOptionsParams)

        SuccessfulMessage(showMessage = showMessage, isMeasurementSuccessful = isMeasurementSuccessful, message = message)

    }
}



@Composable
fun OptionButtons(
    color: Color,
    text: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(2.dp)
            .heightIn(min = 24.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(color = color)
    ) {
        Icon(Icons.Filled.Delete, contentDescription = text)
        Text(text, color = Color.White)
    }
}

@Composable
fun SuccessfulMessage(
    showMessage: Boolean,
    isMeasurementSuccessful: Boolean,
    message: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        if (showMessage) {
            //Muestra el mensaje.
            val icon = if (isMeasurementSuccessful) Icons.Default.Check else Icons.Default.Clear
            val color = if (isMeasurementSuccessful) Color.Green else Color.Red

            Row(
                modifier = Modifier
                    .background(color)
                    .fillMaxWidth()
                    .padding(20.dp)
                    .zIndex(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, contentDescription = "successfulMessage", tint = Color.White)
                Spacer(modifier = Modifier.width(4.dp))
                Text(message, color = Color.White)
            }
        }
    }
}

@Composable
fun ConfirmDeleteDialogOptions(
    dialogOptionsParams: DialogOptionsParams
) {
    with(dialogOptionsParams) {
        if (showDialog) {
            val emptyOrNull = isDatabaseEmptyOrNull()
            if (emptyOrNull) {
                AlertDialog(
                    onDismissRequest = onDismiss,
                    title = { Text(text = "ERROR") },
                    text = { Text("La base de datos está vacía o nula.") },
                    confirmButton = {
                        Button(onClick = onDismiss) {
                            Text("OK")
                        }
                    }
                )
            } else {
                val confirmationMessage = when (userSelection) {
                    " BORRAR REGISTROS" -> "¿Estás seguro de que quieres borrar todas las mediciones?"
                    " BORRAR ÚLTIMA" -> "¿Estás seguro de que quieres borrar la última medición registrada?"
                    " DESCARGAR" -> "¿Descargar los últimos 30 días?"
                    else -> ""
                }

                AlertDialog(
                    onDismissRequest = onDismiss,
                    title = { Text(text = "Confirmación") },
                    text = { Text(confirmationMessage) },
                    confirmButton = {
                        Button(onClick = {
                            onConfirm()
                            val isSuccessfulDelet = glucoseService.isDeleteSuccess
                            val isSuccessfulPrint = printService.isDownloadSuccess

                            if (isSuccessfulDelet || isSuccessfulPrint) {
                                onMeasurementsDeleted(true, "Proceso correcto.")
                            } else {
                                onMeasurementsDeleted(false, "ERROR al realizar el proceso.")
                            }
                        }) {
                            Text("Sí")
                        }
                    },
                    dismissButton = {
                        Button(onClick = onDismiss) {
                            Text("No")
                        }
                    }
                )
            }
        }
    }
}

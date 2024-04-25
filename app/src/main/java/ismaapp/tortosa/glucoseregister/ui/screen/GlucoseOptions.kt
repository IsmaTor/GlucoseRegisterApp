package ismaapp.tortosa.glucoseregister.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
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
import ismaapp.tortosa.glucoseregister.entities.GlucoseMeasurement
import ismaapp.tortosa.glucoseregister.services.IGlucoseService

@Composable
fun GlucoseOptionsScreen(
    glucoseService: IGlucoseService,
) {
    val deleteAll = " BORRAR REGISTROS"
    val deleteLast = " BORRAR ÚLTIMA"
    val print = " IMPRIMIR"

    var glucoseMeasurements by remember { mutableStateOf<List<GlucoseMeasurement>>(emptyList()) }
    var showDialog by remember { mutableStateOf(false) }
    val darkRed by remember { mutableStateOf(Color(0xFF800000)) }
    var userSelection by remember { mutableStateOf(deleteAll) }

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

        //Botón para borrar.
        Button(
            onClick = {
                showDialog = true
                userSelection = deleteAll
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(2.dp)
                .heightIn(min = 24.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(color = darkRed)
        ) {
            Icon(Icons.Filled.Delete, contentDescription = "deleteAll")
            Text(deleteAll, color = Color.White)
        }

        //Botón para borrar última medición.
        Button(
            onClick = {
                showDialog = true
                userSelection = deleteLast
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(2.dp)
                .heightIn(min = 24.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(color = darkRed)
        ) {
            Icon(Icons.Filled.Delete, contentDescription = "deleteLast")
            Text(deleteLast, color = Color.White)
        }

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            "Opciones de imprimir",
            style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold),
            color = Color.White,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        //Botón para mostrar opciones de imprimir.
        Button(
            onClick = {
                showDialog = true
                userSelection = print
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(2.dp)
                .heightIn(min = 24.dp)
                .clip(RoundedCornerShape(8.dp))
        ) {
            Icon(Icons.Filled.ArrowDropDown, contentDescription = "print")
            Text(print, color = Color.White)
        }

        ConfirmDeleteDialogOptions(
            showDialog = showDialog,
            onDismiss = { showDialog = false },
            onConfirm = {
                when (userSelection) {
                    deleteAll -> {
                        glucoseService.deleteAllGlucoseMeasurements()
                        glucoseMeasurements = emptyList()
                    }
                    deleteLast -> {
                        if (glucoseMeasurements.isNotEmpty()) {
                            //implementar lógica para eliminar el último registro por hacer en el service.
                            glucoseMeasurements = glucoseMeasurements.dropLast(1)
                        }
                    }
                    print -> {
                        if (glucoseMeasurements.isNotEmpty()) {
                            //implementar lógica para imprimir
                            glucoseMeasurements = glucoseMeasurements.dropLast(2)
                        }
                    }
                }
                showDialog = false
            },
            userSelection = userSelection,
            isDatabaseEmptyOrNull = glucoseService::isDatabaseEmptyOrNull
        )
    }
}

@Composable
fun ConfirmDeleteDialogOptions(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    userSelection: String,
    isDatabaseEmptyOrNull: () -> Boolean
) {
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
                " IMPRIMIR" -> "¿Imprimir los últimos 30 días?"
                else -> ""
            }

            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text(text = "Confirmación") },
                text = { Text(confirmationMessage) },
                confirmButton = {
                    Button(onClick = onConfirm) {
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

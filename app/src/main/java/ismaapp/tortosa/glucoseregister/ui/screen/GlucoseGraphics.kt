package ismaapp.tortosa.glucoseregister.ui.screen

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.Button
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ismaapp.tortosa.glucoseregister.services.IGlucoseService
import ismaapp.tortosa.glucoseregister.services.TimeRangeServiceImp
import ismaapp.tortosa.glucoseregister.utils.buttonModifier

@Composable
fun GraphicsScreen(glucoseService: IGlucoseService) {
    val lastDaysService = remember {
        TimeRangeServiceImp(glucoseService)
    }
    val (intervalHours, setIntervalHours) = remember { mutableStateOf(0) }
    val (showMessage, setShowMessage) = remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "Gráfica",
            style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold),
            color = Color.White
        )

        // Botones para seleccionar diferentes rangos de tiempo
        Button(
            onClick = {
                val currentInterval = 48
                setIntervalHours(currentInterval)
                val result = lastDaysService.getLastDays(currentInterval)
                setShowMessage(result.isEmpty())
            },
            modifier = buttonModifier(intervalHours == 48)
        ) {
            Text("48 HORAS", color = Color.White)
        }

        Button(
            onClick = {
                val currentInterval = 168
                Log.d("GraphicsScreen", "Seleccionado intervalo: 1 semana")
                setIntervalHours(currentInterval)
                val result = lastDaysService.getLastDays(currentInterval)
                setShowMessage(result.isEmpty())
                Log.d("GraphicsScreen", "Resultado de getLastDays: $result")
            },
            modifier = buttonModifier(intervalHours == 168)
        ) {
            Text("1 SEMANA", color = Color.White)
        }

        Button(
            onClick = {
                val currentInterval = 720
                setIntervalHours(currentInterval)
                val result = lastDaysService.getLastDays(currentInterval)
                setShowMessage(result.isEmpty())
            },
            modifier = buttonModifier(intervalHours == 720)
        ) {
            Text("1 MES", color = Color.White)
        }

        Button(
            onClick = {
                val currentInterval = 8760
                setIntervalHours(currentInterval)
                val result = lastDaysService.getLastDays(currentInterval)
                setShowMessage(result.isEmpty())
            },
            modifier = buttonModifier(intervalHours == 8760)
        ) {
            Text("1 AÑO", color = Color.White)
        }

        // Mostrar mensaje cuando no hay mediciones en el rango de tiempo seleccionado
        if (showMessage) {
            Text(
                "No hay mediciones en este rango de tiempo.",
                style = TextStyle(fontSize = 16.sp),
                color = Color.White
            )
        }

        // Obtener las mediciones para el intervalo seleccionado
        val measurementsToShow = remember(intervalHours) {
            lastDaysService.getLastDays(intervalHours)
        }

        if (measurementsToShow.isNotEmpty()) {
            LineChartComponent(
                glucoseValues = measurementsToShow.map { it.glucoseValue },
                timestamps = measurementsToShow.map { it.date }
            )
        } else {
            Text(" ", color = Color.White)
        }

    }
}

//FUNCIONA PERO AL CAMBIAR DE INTERVALO DEJA DE FUNCIONAR
@Composable
fun LineChartComponent(glucoseValues: List<Int>, timestamps: List<String>) {
    var selectedTimestamp by remember { mutableStateOf<String?>(null) }

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val maxValue = glucoseValues.maxOrNull() ?: 0
                    val distance = size.width / (timestamps.size + 1)
                    val xTolerance = 16f
                    val yTolerance = 16f

                    val closestIndex = glucoseValues.indexOfFirst { value ->
                        val index = glucoseValues.indexOf(value)
                        val x = (index + 1) * distance
                        val y = size.height - (value.toFloat() / maxValue.toFloat()) * size.height
                        offset.x in (x - xTolerance)..(x + xTolerance) &&
                                offset.y in (y - yTolerance)..(y + yTolerance)
                    }

                    // Revisa si closestIndex está dentro de los límites válidos de timestamps
                    selectedTimestamp = if (closestIndex != -1 && closestIndex < timestamps.size) {
                        timestamps[closestIndex]
                    } else {
                        null
                    }
                }
            }
    ) {
        drawIntoCanvas { canvas ->
            val maxValue = glucoseValues.maxOrNull() ?: 0
            val distance = size.width / (timestamps.size + 1)

            // Draw smooth lines connecting data points
            val points = mutableListOf<Offset>()
            glucoseValues.forEachIndexed { index, value ->
                val y = size.height - (value.toFloat() / maxValue.toFloat()) * size.height
                val x = (index + 1) * distance
                points.add(Offset(x, y))
            }
            for (i in 1 until points.size) {
                val midPoint = Offset((points[i - 1].x + points[i].x) / 2, points[i - 1].y)
                drawLine(
                    start = points[i - 1],
                    end = midPoint,
                    color = Color.Blue,
                    strokeWidth = 4f
                )
                drawLine(
                    start = midPoint,
                    end = points[i],
                    color = Color.Blue,
                    strokeWidth = 4f
                )
            }

            // Dibujar líneas horizontales en el eje Y
            val stepSizeLine = size.height / 5
            for (indexLine in 0..5) {
                val yPosition = stepSizeLine * indexLine
                canvas.drawLine(
                    Offset(0f, yPosition),
                    Offset(size.width, yPosition),
                    Paint().apply {
                        color = Color.Gray // Color de las líneas horizontales
                        strokeWidth = 1f // Ancho de las líneas
                    }
                )

                // Dibujar etiquetas de valores en el eje Y
                val stepSizeLabel = maxValue / 5
                for (indexLabel in 0..5) {
                    val labelText = (stepSizeLabel * indexLabel).toString()
                    canvas.nativeCanvas.drawText(
                        labelText,
                        5f,
                        size.height - (size.height / 5) * indexLabel - 5f, // Ajuste de posición para centrar verticalmente
                        android.graphics.Paint().apply {
                            color = Color.White.toArgb()
                            textSize = 12f
                        }
                    )
                }
            }

            // Draw larger markers at data points
            points.forEachIndexed { index, point ->
                val isHovered =
                    selectedTimestamp != null && selectedTimestamp == timestamps[index]
                canvas.drawCircle(point, 8f, Paint().apply {
                    color = if (isHovered) Color.Red else Color.Blue
                })

                // Show timestamp on hover
                if (isHovered) {
                    canvas.nativeCanvas.drawText(
                        timestamps[index],
                        point.x,
                        point.y - 20f,
                        android.graphics.Paint().apply {
                            color = Color.Black.toArgb()
                            textSize = 12f // Use a specific text size here
                        }
                    )
                }
            }
        }
    }
}


//MUESTRA SIEMPRE LAS FECHAS
/*@Composable
fun LineChartComponent(glucoseValues: List<Int>, timestamps: List<String>) {
    var selectedTimestamp by remember { mutableStateOf<String?>(null) }

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    val maxValue = glucoseValues.maxOrNull() ?: 0
                    val distance = size.width / (timestamps.size + 1)
                    val xTolerance = 16f
                    val yTolerance = 16f

                    val currentPos = change.position

                    val closestIndex = glucoseValues.indexOfFirst { value ->
                        val y = size.height - (value.toFloat() / maxValue.toFloat()) * size.height
                        val x = (glucoseValues.indexOf(value) + 1) * distance
                        currentPos.x in (x - xTolerance)..(x + xTolerance) &&
                                currentPos.y in (y - yTolerance)..(y + yTolerance)
                    }

                    if (closestIndex != -1 && closestIndex < timestamps.size) {
                        selectedTimestamp = timestamps[closestIndex]
                    } else {
                        selectedTimestamp = null
                    }
                }
            }
    ) {
        drawIntoCanvas { canvas ->
            val maxValue = glucoseValues.maxOrNull() ?: 0
            val distance = size.width / (timestamps.size + 1)

            // Draw grid lines and Y-axis labels
            for (i in 0..5) {
                val y = size.height - (size.height / 5) * i
                canvas.run {
                    drawLine(
                        start = Offset(45f, y),
                        end = Offset(size.width, y),
                        color = Color.LightGray,
                        strokeWidth = 1f
                    )
                }
            }

            // Draw smooth lines connecting data points
            val points = mutableListOf<Offset>()
            glucoseValues.forEachIndexed { index, value ->
                val y = size.height - (value.toFloat() / maxValue.toFloat()) * size.height
                val x = (index + 1) * distance
                points.add(Offset(x, y))
            }
            for (i in 1 until points.size) {
                val midPoint = Offset((points[i - 1].x + points[i].x) / 2, points[i - 1].y)
                canvas.run {
                    drawLine(
                        start = points[i - 1],
                        end = midPoint,
                        color = Color.Blue,
                        strokeWidth = 4f
                    )
                    drawLine(
                        start = midPoint,
                        end = points[i],
                        color = Color.Blue,
                        strokeWidth = 4f
                    )
                }
            }

            // Draw larger markers at data points
            points.forEach { point ->
                canvas.drawCircle(point, 8f, Paint().apply {
                    color = Color.Blue
                })
            }

            // Show the selected timestamp
            selectedTimestamp?.let { timestamp ->
                canvas.nativeCanvas.drawText(
                    timestamp,
                    10f,
                    size.height + 20f,
                    android.graphics.Paint().apply {
                        color = Color.Black.toArgb()
                        textSize = 12.sp.toPx()
                    }
                )
            }

            // Dibujar fechas encima de los puntos
            glucoseValues.forEachIndexed { index, value ->
                val y = size.height - (value.toFloat() / maxValue.toFloat()) * size.height
                val x = (index + 1) * distance

                // Dibujar círculo
                canvas.drawCircle(Offset(x, y), 8f, Paint().apply {
                    color = Color.Blue
                })

                // Calcular posición en y para el texto de la fecha
                val textY = y - 15f // Desplazar hacia arriba para no superponer el punto

                // Dibujar texto de la fecha
                canvas.nativeCanvas.drawText(
                    timestamps[index],
                    x.toFloat(),
                    textY,
                    android.graphics.Paint().apply {
                        color = Color.Black.toArgb()
                        textSize = 10.sp.toPx()
                    }
                )
            }
        }


    }
}*/



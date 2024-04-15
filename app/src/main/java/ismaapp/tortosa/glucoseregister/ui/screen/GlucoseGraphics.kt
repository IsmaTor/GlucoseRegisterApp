package ismaapp.tortosa.glucoseregister.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ismaapp.tortosa.glucoseregister.services.IGlucoseService
import ismaapp.tortosa.glucoseregister.services.TimeRangeServiceImp

@Composable
fun GraphicsScreen(glucoseService: IGlucoseService) {
    val lastDaysService = remember {
        TimeRangeServiceImp(glucoseService)
    }
    val (intervalHours, setIntervalHours) = remember { mutableStateOf(24) }
    val (showMessage, setShowMessage) = remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "Elegir Tiempo de Rango",
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
                setIntervalHours(currentInterval)
                val result = lastDaysService.getLastDays(currentInterval)
                setShowMessage(result.isEmpty())
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

@Composable
private fun buttonModifier(selected: Boolean): Modifier {
    return Modifier
        .fillMaxWidth()
        .padding(8.dp)
        .heightIn(min = 24.dp)
        .clip(RoundedCornerShape(8.dp))
        .background(color = if (selected) Color.Blue else Color.DarkGray)
}

@Composable
fun LineChartComponent(glucoseValues: List<Int>, timestamps: List<String>) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {
        drawIntoCanvas { canvas ->
            val maxValue = glucoseValues.maxOrNull() ?: 0
            val distance = size.width / (timestamps.size + 1)
            var currentX = 0f

            // Dibujar líneas de la cuadrícula y etiquetas del eje Y
            for (i in 0..5) {
                val y = size.height - (size.height / 5) * i
                canvas.run {
                    drawLine(
                                start = Offset(45f, y),
                                end = Offset(size.width, y),
                                color = Color.LightGray,
                                strokeWidth = 1f
                            )
                    nativeCanvas.drawText(
                                (maxValue / 5 * i).toString(),
                                10f,
                                y + 6f,
                                android.graphics.Paint().apply {
                                    color = Color.Black.toArgb()
                                    textSize = 12.sp.toPx()
                                }
                            )
                }
            }

            // Dibujar etiquetas del eje X (días)
            timestamps.forEachIndexed { index, labelText ->
                val x = currentX + distance
                canvas.nativeCanvas.drawText(
                    labelText,
                    x - (distance / 2),
                    size.height + 20f,
                    android.graphics.Paint().apply {
                        color = Color.Black.toArgb()
                        textSize = 12.sp.toPx()
                    }
                )
                currentX += distance
            }

            // Dibujar curvas suaves que conectan los puntos de datos
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

            // Dibujar marcadores en los puntos de datos
            points.forEach { point ->
                canvas.drawCircle(point, 4f, Paint().apply {
                    color = Color.Blue
                })
            }
        }
    }
}

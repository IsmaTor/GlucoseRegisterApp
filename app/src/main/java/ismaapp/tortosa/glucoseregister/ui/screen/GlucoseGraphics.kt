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
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import ismaapp.tortosa.glucoseregister.services.IGlucoseService
import ismaapp.tortosa.glucoseregister.services.TimeRangeServiceImp
import ismaapp.tortosa.glucoseregister.utils.buttonModifier

@Composable
fun GraphicsScreen(glucoseService: IGlucoseService, navController: NavController) {
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

        //Botones para seleccionar diferentes rangos de tiempo.
        Button(
            onClick = {
                val currentInterval = 48
                setIntervalHours(currentInterval)
                val result = lastDaysService.getLastDays(currentInterval)
                setShowMessage(result.isEmpty())
                navController.navigate("graphicDetail/$currentInterval")
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
                navController.navigate("graphicDetail/$currentInterval")
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
                navController.navigate("graphicDetail/$currentInterval")
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
                navController.navigate("graphicDetail/$currentInterval")
            },
            modifier = buttonModifier(intervalHours == 8760)
        ) {
            Text("1 AÑO", color = Color.White)
        }

        // Mostrar mensaje cuando no hay mediciones en el intervalo de tiempo seleccionado.
        if (showMessage) {
            Text(
                "No hay mediciones en este intervalo de tiempo.",
                style = TextStyle(fontSize = 16.sp),
                color = Color.White
            )
        }

        // Obtener las mediciones para el intervalo seleccionado.
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
fun GraphicDetailScreen(
    glucoseService: IGlucoseService,
    intervalHours: Int,
    onNavigateBack: () -> Unit
) {
    val measurementsToShow = remember(intervalHours) {
        TimeRangeServiceImp(glucoseService).getLastDays(intervalHours)
    }

    Surface(
        color = Color.DarkGray,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = { onNavigateBack() },
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                Text("Volver")
            }

            Box(
                modifier = Modifier
                    .size(400.dp) // Tamaño para la gráfica.
                    .padding(16.dp)
                    .background(Color.DarkGray) //Color de fondo de la gráfica.
                    .border(
                        width = 2.dp, //Grosor del borde.
                        color = Color.Black, //Color del borde.
                        shape = RectangleShape //Forma del borde.
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (measurementsToShow.isNotEmpty()) {
                    LineChartComponent(
                        glucoseValues = measurementsToShow.map { it.glucoseValue },
                        timestamps = measurementsToShow.map { it.date }
                    )
                } else {
                    Text(
                        text = "No hay mediciones en este rango de tiempo.",
                        color = Color.Black,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

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

                    //Revisa si closestIndex está dentro de los límites válidos de timestamps.
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

            //Dibujar líneas que conectan los puntos de datos.
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

            //Dibujar líneas horizontales en el eje Y.
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

                //Dibujar etiquetas de valores en el eje Y.
                val stepSizeLabel = maxValue / 5
                for (indexLabel in 0..5) {
                    val labelText = (stepSizeLabel * indexLabel).toString()
                    canvas.nativeCanvas.drawText(
                        labelText,
                        20f, //Separación de la etiquetas.
                        size.height - (size.height / 5) * indexLabel - 5f, //Ajuste de posición para centrar verticalmente.
                        android.graphics.Paint().apply {
                            color = Color.White.toArgb()
                            textSize = 40f //Tamaño de las etiquetas.
                        }
                    )
                }
            }

            //Dibujar marcadores más grandes en los puntos de datos.
            points.forEachIndexed { index, point ->
                val isHovered = selectedTimestamp != null && selectedTimestamp == timestamps[index]
                canvas.drawCircle(point, 8f, Paint().apply {
                    color = if (isHovered) Color.Red else Color.Blue
                })

                //Mostrar la marca de tiempo al pasar el cursor.
                if (isHovered) {
                    //Coordenadas fijas para mostrar la etiqueta debajo del gráfico.
                    val labelX = 20f
                    val labelY = size.height + 50f  //Posición debajo del gráfico.

                    val text = timestamps[index]
                    val textPaint = android.graphics.Paint().apply {
                        color = Color.Black.toArgb()
                        textSize = 50f //Tamaño del texto.
                    }

                    val textWidth = textPaint.measureText(text)
                    val textHeight = textPaint.fontSpacing
                    val verticalY = 20f //Numeración para el ajuste vertical.

                    //Definir el área del rectángulo para el fondo detrás del texto.
                    val rectLeft = labelX
                    val rectTop = labelY - textHeight + verticalY  //Ajustar el rectángulo según la altura del texto y bajarlo.
                    val rectRight = rectLeft + textWidth
                    val rectBottom = labelY + verticalY //Ajustar la posición vertical del fondo.

                    // Dibujar el rectángulo como fondo detrás del texto
                    canvas.nativeCanvas.drawRect(
                        rectLeft,
                        rectTop,
                        rectRight,
                        rectBottom,
                        android.graphics.Paint().apply {
                            color = Color.White.toArgb() //Color del fondo.
                            alpha = 200 //Transparencia del fondo (0-255).
                        }
                    )

                    //Alinear el texto ligeramente por debajo de la parte inferior izquierda del rectángulo.
                    val textX = rectLeft
                    val textY = labelY - textPaint.descent() + verticalY //Ajustar la posición vertical del texto.

                    //Dibujar el texto encima del fondo.
                    canvas.nativeCanvas.drawText(
                        text,
                        textX,
                        textY,
                        textPaint
                    )
                }
            }
        }
    }
}

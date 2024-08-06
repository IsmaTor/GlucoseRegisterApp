package ismaapp.tortosa.glucoseregister.utils

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import ismaapp.tortosa.glucoseregister.ui.theme.SoftGreen
import ismaapp.tortosa.glucoseregister.ui.theme.SoftRed

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
            val color = if (isMeasurementSuccessful) SoftGreen else SoftRed

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
                Text(message, color = Color.White, style = TextStyle(fontSize = 19.sp)) //mensaje, color y tamaño.
            }
        } else {
            Log.d("SuccessfulMessage", "No message to show.")
        }
    }
}

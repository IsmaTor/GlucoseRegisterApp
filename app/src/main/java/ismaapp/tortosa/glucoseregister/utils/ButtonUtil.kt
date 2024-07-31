package ismaapp.tortosa.glucoseregister.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import ismaapp.tortosa.glucoseregister.ui.theme.SoftYellow

@Composable
fun buttonModifier(selected: Boolean): Modifier {
    return Modifier
        .fillMaxWidth()
        .padding(8.dp)
        .heightIn(min = 24.dp)
        .clip(RoundedCornerShape(8.dp))
        .background(color = if (selected) Color.Blue else Color.DarkGray)
}

@Composable
fun OptionButtons(
    color: Color,
    text: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(2.dp)
            .heightIn(min = 24.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(color = color),
        colors = ButtonDefaults.buttonColors(SoftYellow)
    ) {
        Icon(icon, contentDescription = text)
        Text(text, color = Color.White)
    }
}



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
import androidx.compose.ui.unit.dp

@Composable
fun buttonModifier(selected: Boolean): Modifier {
    return Modifier
        .fillMaxWidth()
        .padding(8.dp)
        .heightIn(min = 24.dp)
        .clip(RoundedCornerShape(8.dp))
        .background(color = if (selected) Color.Blue else Color.DarkGray)
}


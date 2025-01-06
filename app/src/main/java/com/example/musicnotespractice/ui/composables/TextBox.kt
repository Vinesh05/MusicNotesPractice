package com.example.musicnotespractice.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.example.musicnotespractice.ui.theme.ButtonColor

@Composable
fun TextBox(
    modifier: Modifier = Modifier,
    swar: String,
    fontSize: TextUnit,
    isHighlighted: Boolean
) {
    Box(
        modifier = modifier
            .background(
                color = if (isHighlighted) ButtonColor else Color.White,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = swar,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            color = if (isHighlighted) Color.White else Color.Black
        )
    }
}

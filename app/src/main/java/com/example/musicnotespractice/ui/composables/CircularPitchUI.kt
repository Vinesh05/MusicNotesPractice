package com.example.musicnotespractice.ui.composables

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CircularPitchUI(
    modifier: Modifier,
    note: String,
){
    val textMeasurer = rememberTextMeasurer()
    val notesCosSinValues = remember { hashMapOf(
        "Dha" to Pair(0.866f, 0.5f),
        "TDha" to Pair(0.5f, 0.866f),
        "Ni" to Pair(0f, 1f),
        "Sa" to Pair(-0.5f, 0.866f),
        "TSa" to Pair(-0.866f, 0.5f),
        "Re" to Pair(-1f, 0f),
        "TRe" to Pair(-0.866f, -0.5f),
        "Ga" to Pair(-0.5f, -0.86f),
        "Ma" to Pair(0f, -1f),
        "TMa" to Pair(0.5f, -0.866f),
        "Pa" to Pair(0.866f, -0.5f),
        "TPa" to Pair(1f, 0f)
    ) }
    val notesOffsets = remember { HashMap<String, Offset>() }

    Canvas(
        modifier = modifier.fillMaxWidth()
            .padding(8.dp)
    ){

        val circleCenter = if(note!=" " && notesOffsets[note]!=null){
            notesOffsets[note]!!
        }
        else{
            size.center
        }

        drawCircle(
            brush = Brush.radialGradient(
                radius = size.width/10,
                center = circleCenter,
                colorStops = arrayOf(
                    Pair(0f, Color(0xFFF1C25D)), Pair(0.60f, Color(0xFF897447)), Pair(1f, Color(0xFF2B2B2B))
                )
            ),
            radius = size.width/10,
            center = circleCenter
        )
        val radius = size.width/2.5f
        notesCosSinValues.forEach { (noteText, cosSinValues) ->
            val textLayoutResult = textMeasurer.measure(
                text = noteText,
                style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Light)
            )
            notesOffsets[noteText] = Offset(
                (size.center.x+(radius*cosSinValues.first)),
                (size.center.y+(radius*cosSinValues.second))
            )
            if(notesOffsets[noteText]!=null) {
                drawText(
                    textMeasurer = textMeasurer,
                    text = noteText,
                    topLeft = Offset(
                        notesOffsets[noteText]!!.x - (textLayoutResult.size.width / 2f),
                        notesOffsets[noteText]!!.y - (textLayoutResult.size.height / 2f)
                    ),
                    style = TextStyle(
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Light
                    )
                )
            }
        }

        Log.d("CircularPitchUI", "Note: $note")
    }

}

@Preview
@Composable
fun CircularPitchUIPreview(){
    CircularPitchUI(
        modifier = Modifier.width(400.dp).height(600.dp),
        note = "Sa"
    )
}
package com.example.musicnotespractice.ui.composables

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import com.example.musicnotespractice.ui.theme.ButtonColor
import com.example.musicnotespractice.ui.theme.MusicNotesPracticeTheme
import com.example.musicnotespractice.ui.theme.TextColor
import kotlinx.coroutines.launch

@Composable
fun AlankarPractice(
    alankars: List<List<String>>,
    isAlankarPlaying: MutableState<Boolean>,
    currentAlankarIndex: MutableIntState,
    alankarCurrentIndex: MutableIntState,
    lazyListState: LazyListState,
) {
    val scope = rememberCoroutineScope()
    val dropDownOptions = List(alankars.size) { index -> "Alankar ${index+1}" }
    val dropDownExpanded = remember { mutableStateOf(false) }
    val selectedAlankarText = remember { mutableStateOf(dropDownOptions[currentAlankarIndex.intValue])}

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AlankarDropDown(
            Modifier
                .align(Alignment.CenterHorizontally)
                .padding(16.dp),
            dropDownOptions,
            dropDownExpanded,
            selectedAlankarText,
            currentAlankarIndex
        )

        LazyRow(
            modifier = Modifier
                .fillMaxWidth(),
            state = lazyListState,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
             items(alankars[currentAlankarIndex.intValue].size){ index ->
                SwarBox(
                    swar = alankars[currentAlankarIndex.intValue][index],
                    isHighlighted = index == alankarCurrentIndex.intValue
                )
            }
            Log.d("AlankarPractice", "Current Index: ${alankarCurrentIndex.intValue}")
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { isAlankarPlaying.value = !isAlankarPlaying.value },
                modifier = Modifier.padding(8.dp),
                colors = ButtonColors(
                    containerColor = ButtonColor,
                    contentColor = TextColor,
                    disabledContainerColor = Color.DarkGray,
                    disabledContentColor = Color.LightGray
                )
            ) {
                Text(if (isAlankarPlaying.value) "Pause" else "Play")
            }

            Button(
                onClick = {
                    isAlankarPlaying.value = false
                    alankarCurrentIndex.intValue = 0
                    scope.launch {
                        lazyListState.animateScrollToItem(0)
                    }
                },
                modifier = Modifier.padding(8.dp),
                colors = ButtonColors(
                    containerColor = ButtonColor,
                    contentColor = TextColor,
                    disabledContainerColor = Color.DarkGray,
                    disabledContentColor = Color.LightGray
                )
            ) {
                Text("Reset")
            }
        }
    }
}

@Composable
fun SwarBox(
    swar: String,
    isHighlighted: Boolean
) {
    Box(
        modifier = Modifier
            .background(
                color = if (isHighlighted) ButtonColor else Color.White,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = swar,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = if (isHighlighted) Color.White
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun AlankarDropDown(
    modifier: Modifier,
    dropDownOptions: List<String>,
    dropDownExpanded: MutableState<Boolean>,
    selectedAlankarText: MutableState<String>,
    currentAlankarIndex: MutableIntState
){
    val textfieldSize = remember { mutableStateOf(Size(0f, 0f))}
    Box(
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedAlankarText.value,
            readOnly = true,
            onValueChange = {},
            modifier = Modifier
                .onGloballyPositioned { coordinates ->
                    textfieldSize.value = coordinates.size.toSize()
                },
            label = {},
            trailingIcon = {
                Icon(
                    if(dropDownExpanded.value) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                    "contentDescription",
                    Modifier.clickable { dropDownExpanded.value = !dropDownExpanded.value })
            }
        )
        DropdownMenu(
            expanded = dropDownExpanded.value,
            onDismissRequest = { dropDownExpanded.value = false },
            modifier = Modifier
                .width(with(LocalDensity.current){textfieldSize.value.width.toDp()})
        ) {
            dropDownOptions.forEachIndexed { alankarIndex, alankarText->
                DropdownMenuItem(
                    text = { Text(text = alankarText) },
                    onClick = {
                        currentAlankarIndex.intValue = alankarIndex
                        selectedAlankarText.value = alankarText
                        dropDownExpanded.value = false
                    }
                )
            }
        }
    }
}

@Preview
@Composable
fun AlankarPracticePreview(){
    MusicNotesPracticeTheme {
        val isAlankarPlaying = remember{ mutableStateOf(false) }
        val currentAlankarIndex = remember{ mutableIntStateOf(0)}
        val alankarCurrentIndex = remember{ mutableIntStateOf(0)}
        val lazyListState = rememberLazyListState()
        AlankarPractice(
            listOf(
                listOf("Sa", "_", "_", "_", "Re", "_", "_", "_", "Ga", "_", "_", "_", "Ma", "_", "_", "_", "Pa", "_", "_", "_", "Dha", "_", "_", "_", "Ni", "_", "_", "_", "Sa","_", "_", "_", "Sa", "_", "_", "_", "Ni", "_", "_", "_", "Dha", "_", "_", "_", "Pa", "_", "_", "_", "Ma", "_", "_", "_", "Ga", "_", "_", "_", "Re", "_", "_", "_", "Sa", "_", "_", "_"),
        listOf("Sa", "_", "Sa", "_", "Re", "_", "Re", "_", "Ga", "_", "Ga", "_", "Ma", "_", "Ma", "_", "Pa", "_", "Pa", "_", "Dha", "_", "Dha", "_", "Ni", "_", "Ni", "_", "Sa", "_", "Sa", "_", "Sa", "_", "Sa", "_", "Ni", "_", "Ni", "_", "Dha", "_", "Dha", "_", "Pa", "_", "Pa", "_", "Ma", "_", "Ni", "_", "Ga", "_", "Ga", "_", "Re", "_", "Re", "_", "Sa", "_", "Sa", "_"),
        listOf("Sa", "Sa", "Sa", "_", "Re", "Re", "Re", "_", "Ga", "Ga", "Ga", "_", "Ma", "Ma", "Ma", "_", "Pa", "Pa", "Pa", "_", "Dha", "Dha", "Dha", "_", "Ni", "Ni", "Ni", "_", "Sa", "Sa", "Sa", "_", "Sa", "Sa", "Sa", "_", "Ni", "Ni", "Ni", "_", "Dha", "Dha", "Dha", "_", "Pa", "Pa", "Pa", "_", "Ma", "Ma", "Ma", "_", "Ga", "Ga", "Ga", "_", "Re", "Re", "Re", "_", "Sa", "Sa", "Sa", "_"),
        listOf("Sa", "Sa", "Sa", "Sa", "Re", "Re", "Re", "Re", "Ga", "Ga", "Ga", "Ga", "Ma", "Ma", "Ma", "Ma", "Pa", "Pa", "Pa", "Pa", "Dha", "Dha", "Dha", "Dha", "Ni", "Ni", "Ni", "Ni", "Sa", "Sa", "Sa", "Sa", "Sa", "Sa", "Sa", "Sa", "Ni", "Ni", "Ni", "Ni", "Dha", "Dha", "Dha", "Dha", "Pa", "Pa", "Pa", "Pa", "Ma", "Ma", "Ma", "Ma", "Ga", "Ga", "Ga", "Ga", "Re", "Re", "Re", "Re", "Sa", "Sa", "Sa", "Sa")
        ),
            isAlankarPlaying,
            currentAlankarIndex,
            alankarCurrentIndex,
            lazyListState
        )
    }
}
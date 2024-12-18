package com.nasahacker.nasavolumecontrol.ui.widget

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


@Composable
fun VolumeItem(
    title: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: (() -> Unit)? = null
) {
    Column {
        Text(
            text = title, modifier = Modifier
                .fillMaxWidth()
                .padding(start = 18.dp, end = 18.dp, bottom = 4.dp)
        )
        Slider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 18.dp, end = 18.dp, bottom = 4.dp),
            value = value,
            onValueChange = onValueChange,
            valueRange = 0f..100f,
            onValueChangeFinished = {
                onValueChangeFinished?.invoke()
            }
        )
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewVolumeItem() {
    var volume by remember { mutableFloatStateOf(50f) }
    VolumeItem(
        title = "Volume Control",
        value = volume,
        onValueChange = { newValue -> volume = newValue },
        onValueChangeFinished = {

        }
    )
}
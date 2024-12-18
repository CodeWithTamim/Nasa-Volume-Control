package com.nasahacker.nasavolumecontrol.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nasahacker.nasavolumecontrol.ui.widget.ActionButton
import com.nasahacker.nasavolumecontrol.ui.widget.SettingSwitch
import com.nasahacker.nasavolumecontrol.util.AppUtil
import com.nasahacker.nasavolumecontrol.util.Constant

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    onStartService: () -> Unit,
    onStopService: () -> Unit,
    onRequestOverlayPermission: () -> Unit,
) {
    val context = LocalContext.current
    var bootState by remember { mutableStateOf(AppUtil.getIsStartOnBoot(context)) }
    var advancedState by remember { mutableStateOf(AppUtil.getIsAdvancedMode(context)) }
    var sliderPosition by remember { mutableFloatStateOf(AppUtil.getLayoutOpacity(context) * 100) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Welcome To Nasa Volume Control",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "Start the window to get started",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                SettingSwitch(
                    title = "Start on Boot",
                    checked = bootState,
                    onCheckedChange = {
                        bootState = it
                        AppUtil.setIsStartOnBoot(context, it)
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                SettingSwitch(
                    title = "Advanced Controls",
                    checked = advancedState,
                    onCheckedChange = {
                        advancedState = it
                        AppUtil.setIsAdvancedMode(context, it)
                        AppUtil.restartService(context)
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Window Opacity",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Slider(
                    value = sliderPosition,
                    onValueChange = { value ->
                        if (value >= Constant.SEEK_BAR_MIN_PROGRESS) {
                            sliderPosition = value
                            AppUtil.setLayoutOpacity(context, AppUtil.getFloatProgress(value.toInt()))
                        }
                    },
                    valueRange = Constant.SEEK_BAR_MIN_PROGRESS.toFloat()..100f,
                    onValueChangeFinished = { AppUtil.restartService(context) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                ActionButton(
                    text = "Start Floating Window",
                    onClick = {
                        if (AppUtil.canDrawOverlay(context)) {
                            onStartService()
                        } else {
                            onRequestOverlayPermission()
                        }
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                ActionButton(
                    text = "Stop Floating Window",
                    onClick = onStopService
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewMainScreen() {
    MainScreen(
        onStartService = {},
        onStopService = {},
        onRequestOverlayPermission = {}
    )
}

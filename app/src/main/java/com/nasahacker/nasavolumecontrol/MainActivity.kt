package com.nasahacker.nasavolumecontrol

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.nasahacker.nasavolumecontrol.ui.screen.MainScreen
import com.nasahacker.nasavolumecontrol.ui.theme.AppTheme
import com.nasahacker.nasavolumecontrol.util.AppUtil

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            AppTheme {

                MainScreen(
                    modifier = Modifier.fillMaxSize(),
                    onStartService = { AppUtil.startService(context) },
                    onStopService = { AppUtil.stopService(context) },
                    onRequestOverlayPermission = { AppUtil.requestOverlayPermission(context) }
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewMainScreen() {
    val context = LocalContext.current
    MainScreen(
        modifier = Modifier.fillMaxSize(),
        onStartService = { AppUtil.startService(context) },
        onStopService = { AppUtil.stopService(context) },
        onRequestOverlayPermission = { AppUtil.requestOverlayPermission(context) }
    )
}


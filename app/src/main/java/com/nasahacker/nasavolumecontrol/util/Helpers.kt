package com.nasahacker.nasavolumecontrol.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat
import com.nasahacker.nasavolumecontrol.util.Constants.APP_PREF
import com.nasahacker.nasavolumecontrol.util.Constants.START_ON_BOOT

object Helpers {
    fun canDrawOverlay(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) true
        else Settings.canDrawOverlays(context)
    }

    fun requestOverlayPermission(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!canDrawOverlay(context)) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${context.packageName}")
                )
                context.startActivity(intent)
            }
        }
    }

    fun requestNotificationPermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    101
                )
            }

        }

    }

    private fun getSharedPreferences(context: Context) =
        context.getSharedPreferences(APP_PREF, Context.MODE_PRIVATE)

    fun setIsStartOnBoot(context: Context, isStartOnBoot: Boolean) {
        getSharedPreferences(context)
            .edit()
            .putBoolean(START_ON_BOOT, isStartOnBoot)
            .apply()
    }

    fun getIsStartOnBoot(context: Context): Boolean {
        return getSharedPreferences(context)
            .getBoolean(START_ON_BOOT, false)
    }
}

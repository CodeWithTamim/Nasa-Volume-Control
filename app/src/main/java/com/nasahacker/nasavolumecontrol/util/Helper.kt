package com.nasahacker.nasavolumecontrol.util

import android.Manifest
import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity.NOTIFICATION_SERVICE
import androidx.core.app.ActivityCompat
import com.nasahacker.nasavolumecontrol.R
import com.nasahacker.nasavolumecontrol.util.Constant.ADVANCED_MODE_ON
import com.nasahacker.nasavolumecontrol.util.Constant.APP_PREF
import com.nasahacker.nasavolumecontrol.util.Constant.START_ON_BOOT


object Helper {
    fun canDrawOverlay(context: Context): Boolean {
        return Settings.canDrawOverlays(context)
    }

    fun requestOverlayPermission(context: Context) {
        if (!canDrawOverlay(context)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse(context.getString(R.string.label_package, context.packageName))
            )
            context.startActivity(intent)
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

    fun requestDoNotDisturbAccess(context: Context) {
        val notificationManager =
            context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (!notificationManager.isNotificationPolicyAccessGranted) {
            Toast.makeText(
                context,
                context.getString(R.string.label_grant_dnd_access_please), Toast.LENGTH_SHORT
            ).show()
            // Request permission
            val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
            context.startActivity(intent)
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

    fun setIsAdvancedMode(context: Context, isStartOnBoot: Boolean) {
        getSharedPreferences(context)
            .edit()
            .putBoolean(ADVANCED_MODE_ON, isStartOnBoot)
            .apply()
    }

    fun getIsAdvancedMode(context: Context): Boolean {
        return getSharedPreferences(context)
            .getBoolean(ADVANCED_MODE_ON, false)
    }
}

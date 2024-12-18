package com.nasahacker.nasavolumecontrol.util


import android.Manifest
import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.nasahacker.nasavolumecontrol.R
import com.nasahacker.nasavolumecontrol.service.VolumeControlService
import com.nasahacker.nasavolumecontrol.util.Constant.ADVANCED_MODE_ON
import com.nasahacker.nasavolumecontrol.util.Constant.APP_PREF
import com.nasahacker.nasavolumecontrol.util.Constant.LAYOUT_OPACITY
import com.nasahacker.nasavolumecontrol.util.Constant.PERMISSION_REQUEST_CODE
import com.nasahacker.nasavolumecontrol.util.Constant.START_ON_BOOT


object AppUtil {
    /**
     * Checks the overlay permission
     * @param context
     */
    fun canDrawOverlay(context: Context): Boolean {
        return Settings.canDrawOverlays(context)
    }

    /**
     * Requests the overlay permission
     * @param context
     */
    fun requestOverlayPermission(context: Context) {
        if (!canDrawOverlay(context)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse(context.getString(R.string.label_package, context.packageName))
            )
            context.startActivity(intent)
        }
    }

    /**
     * Request notification permission
     * @param activity
     */
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
                    PERMISSION_REQUEST_CODE
                )
            }

        }
    }

    /**
     * Request do not disturb access
     * @param context
     */
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

    /**
     * Return SharedPrefs instance
     * @param context
     */
    private fun getSharedPreferences(context: Context) =
        context.getSharedPreferences(APP_PREF, Context.MODE_PRIVATE)

    /**
     * Sets flag value for boot
     * @param context
     * @param isStartOnBoot
     */
    fun setIsStartOnBoot(context: Context, isStartOnBoot: Boolean) {
        getSharedPreferences(context)
            .edit()
            .putBoolean(START_ON_BOOT, isStartOnBoot)
            .apply()
    }

    /**
     * Returns flag value for boot
     * @param context
     */
    fun getIsStartOnBoot(context: Context): Boolean {
        return getSharedPreferences(context)
            .getBoolean(START_ON_BOOT, false)
    }

    /**
     * Sets flag value for advanced mode
     * @param context
     * @param isStartOnBoot
     */
    fun setIsAdvancedMode(context: Context, isStartOnBoot: Boolean) {
        getSharedPreferences(context)
            .edit()
            .putBoolean(ADVANCED_MODE_ON, isStartOnBoot)
            .apply()
    }

    /**
     * Sets flag value for opacity mode
     * @param context
     * @param opacity
     */
    fun setLayoutOpacity(context: Context, opacity: Float) {
        getSharedPreferences(context)
            .edit()
            .putFloat(LAYOUT_OPACITY, opacity)
            .apply()
    }

    /**
     * Returns flag value for opacity
     * @param context
     */
    fun getLayoutOpacity(context: Context): Float {
        return getSharedPreferences(context)
            .getFloat(LAYOUT_OPACITY, 1F)
    }

    /**
     * Returns flag value for advanced mode
     * @param context
     */
    fun getIsAdvancedMode(context: Context): Boolean {
        return getSharedPreferences(context)
            .getBoolean(ADVANCED_MODE_ON, false)
    }

    /**
     * Restarts the service
     * @param context
     */

    fun restartService(context: Context) {
        if (VolumeControlService.isServiceRunning) {
            if (canDrawOverlay(context)) {
                context.stopService(Intent(context, VolumeControlService::class.java))
                context.startService(Intent(context, VolumeControlService::class.java))
            }
        }
    }

    /**
     * Starts the service
     * @param context
     */

    fun startService(context: Context) {
        if (!VolumeControlService.isServiceRunning) {
            if (canDrawOverlay(context)) {
                val intent = Intent(context, VolumeControlService::class.java)
                context.startService(intent)
            }
        }
    }


    /**
     * Stops the service
     * @param context
     */


    fun stopService(context: Context) {
        if (VolumeControlService.isServiceRunning) {
            context.stopService(Intent(context, VolumeControlService::class.java))
            Toast.makeText(context, "Success", Toast.LENGTH_SHORT).show()
            return
        }
        Toast.makeText(context, "Not running currently", Toast.LENGTH_SHORT).show()
    }

    /**
     * Send the progress in float
     * @param progress
     */

    fun getFloatProgress(progress: Int): Float = progress / 100F
}
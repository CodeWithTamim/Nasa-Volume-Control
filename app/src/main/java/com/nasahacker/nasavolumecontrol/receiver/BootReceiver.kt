package com.nasahacker.nasavolumecontrol.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.nasahacker.nasavolumecontrol.service.VolumeControlService
import com.nasahacker.nasavolumecontrol.util.Helpers

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (Intent.ACTION_BOOT_COMPLETED == intent?.action) {
            if (Helpers.canDrawOverlay(context!!) && Helpers.getIsStartOnBoot(context)) {
                context.startService(Intent(context, VolumeControlService::class.java))
            }
        }
    }
}
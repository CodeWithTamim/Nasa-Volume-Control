package com.nasahacker.nasavolumecontrol.util

object Constant {
    /**
     * Notification
     */
    const val NOTIFICATION_CHANNEL_NAME = "Nasa Volume Control"
    const val NOTIFICATION_CHANNEL_ID = "Volume Notifications"

    /**
     * Service
     */
    const val NOTIFICATION_FOREGROUND_ID = 1

    /**
     * Prefs
     */
    const val APP_PREF = "NASA_VOLUME_CONTROL"
    const val START_ON_BOOT = "isStartOnBoot"
    const val ADVANCED_MODE_ON = "advancedModeOn"
    const val LAYOUT_OPACITY = "layoutOpacity"

    /**
     * Actions
     */
    const val VOL_CHANGE_ACTION = "android.media.VOLUME_CHANGED_ACTION"

    /**
     * Request codes
     */
    const val PERMISSION_REQUEST_CODE = 101

    /**
     * Constants for opacity
     */
    const val LAYOUT_MAX_OPACITY = 1
    const val LAYOUT_MIN_OPACITY = 0.2

    /**
     * Seekbar progress
     */
    const val SEEK_BAR_MIN_PROGRESS = 30

}
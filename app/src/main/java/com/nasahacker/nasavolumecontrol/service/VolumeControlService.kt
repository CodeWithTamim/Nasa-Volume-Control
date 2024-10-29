package com.nasahacker.nasavolumecontrol.service

import android.app.Notification
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.media.AudioManager
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.nasahacker.nasavolumecontrol.R
import com.nasahacker.nasavolumecontrol.util.Constant
import com.nasahacker.nasavolumecontrol.util.Constant.NOTIFICATION_CHANNEL_ID
import com.nasahacker.nasavolumecontrol.util.Constant.NOTIFICATION_FOREGROUND_ID
import com.nasahacker.nasavolumecontrol.util.AppUtils

class VolumeControlService : Service() {

    // Service lifecycle method
    override fun onBind(intent: Intent?): IBinder? = null

    // Class-level variables
    private lateinit var windowManager: WindowManager
    private lateinit var floatingView: View
    private lateinit var audioManager: AudioManager

    private var initialX: Int = 0
    private var initialY: Int = 0
    private var initialTouchX: Float = 0f
    private var initialTouchY: Float = 0f
    private var isDragging: Boolean = false
    private var isVisible: Boolean = false
    private var isMuted: Boolean = false

    private lateinit var muteButton: ImageView
    private lateinit var pgCall: SeekBar
    private lateinit var pgRingtone: SeekBar
    private lateinit var pgNotification: SeekBar
    private lateinit var pgAlarm: SeekBar
    private lateinit var pgMedia: SeekBar

    companion object { var isServiceRunning: Boolean = false }

    private val volumeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            updateSeekBars()
        }
    }

    // Called when the service is created
    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_FOREGROUND_ID,
                getNotification(),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(
                NOTIFICATION_FOREGROUND_ID,
                getNotification(),
            )
        }
        isServiceRunning = true

        // Initialize WindowManager and AudioManager
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager

        // Register volume change receiver
        registerReceiver(volumeReceiver, IntentFilter(Constant.VOL_CHANGE_ACTION))

        // Inflate the floating view layout
        if (!AppUtils.getIsAdvancedMode(this)) {
            floatingView =
                LayoutInflater.from(this).inflate(R.layout.floting_vol_layout_normal, null)
            floatingView.findViewById<ImageView>(R.id.upButton).setOnClickListener {
                increaseSound(AudioManager.STREAM_MUSIC)
            }

            floatingView.findViewById<ImageView>(R.id.downButton).setOnClickListener {
                decreaseSound(AudioManager.STREAM_MUSIC)
            }

        } else {
            floatingView =
                LayoutInflater.from(this).inflate(R.layout.floting_vol_layout_advanced, null)
            pgMedia = floatingView.findViewById(R.id.pgMedia)
            pgCall = floatingView.findViewById(R.id.pgCall)
            pgAlarm = floatingView.findViewById(R.id.pgAlarm)
            pgRingtone = floatingView.findViewById(R.id.pgRingtone)
            pgNotification = floatingView.findViewById(R.id.pgNotification)

            pgMedia.max = getMaxVol(AudioManager.STREAM_MUSIC)
            pgCall.max = getMaxVol(AudioManager.STREAM_VOICE_CALL)
            pgAlarm.max = getMaxVol(AudioManager.STREAM_ALARM)
            pgRingtone.max = getMaxVol(AudioManager.STREAM_RING)
            pgNotification.max = getMaxVol(AudioManager.STREAM_NOTIFICATION)

            updateSeekBars() // Initialize SeekBar values

            pgMedia.setOnSeekBarChangeListener(createSeekBarChangeListener(AudioManager.STREAM_MUSIC))
            pgCall.setOnSeekBarChangeListener(createSeekBarChangeListener(AudioManager.STREAM_VOICE_CALL))
            pgAlarm.setOnSeekBarChangeListener(createSeekBarChangeListener(AudioManager.STREAM_ALARM))
            pgRingtone.setOnSeekBarChangeListener(createSeekBarChangeListener(AudioManager.STREAM_RING))
            pgNotification.setOnSeekBarChangeListener(createSeekBarChangeListener(AudioManager.STREAM_NOTIFICATION))
        }
        muteButton = floatingView.findViewById(R.id.muteButton)

        floatingView.alpha = AppUtils.getLayoutOpacity(this)

        // Set up click listeners for buttons
        floatingView.findViewById<ImageView>(R.id.minMax).setOnClickListener {
            toggleVisibility()
        }
        muteButton.setOnClickListener {
            toggleMute()
        }

        // Check if the audio is initially muted
        isMuted = (audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) == 0)

        // Set up layout parameters for the floating view
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
            x = 0
            y = 100
        }

        // Add the floating view to the window manager
        windowManager.addView(floatingView, params)
        isVisible = true

        // Handle touch events for dragging the floating view
        floatingView.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Record initial touch position
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isDragging = false
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    // Update the position of the floating view
                    params.x = initialX + (event.rawX - initialTouchX).toInt()
                    params.y = initialY + (event.rawY - initialTouchY).toInt()
                    windowManager.updateViewLayout(floatingView, params)
                    isDragging = true
                    true
                }

                MotionEvent.ACTION_UP -> {
                    // Simulate a click event if not dragging
                    if (!isDragging) {
                        floatingView.performClick()
                    }
                    true
                }

                else -> false
            }
        }
    }

    // Increase the media volume and show a toast message with volume percentage
    private fun increaseSound(type: Int) {
        audioManager.adjustStreamVolume(type, AudioManager.ADJUST_RAISE, 0)
        updateSeekBars() // Update SeekBars to reflect the change
        showVolumeToast()
    }

    // Decrease the media volume and show a toast message with volume percentage
    private fun decreaseSound(type: Int) {
        audioManager.adjustStreamVolume(type, AudioManager.ADJUST_LOWER, 0)
        updateSeekBars() // Update SeekBars to reflect the change
        showVolumeToast()
    }


    private fun getMaxVol(type: Int): Int {
        return audioManager.getStreamMaxVolume(type)
    }

    // Toggle the visibility of the floating view
    private fun toggleVisibility() {
        val mainLayout = floatingView.findViewById<LinearLayout>(R.id.mainLayout)
        mainLayout.visibility = if (isVisible) View.GONE else View.VISIBLE
        isVisible = !isVisible
    }

    // Toggle mute state
    private fun toggleMute() {
        isMuted = !isMuted
        audioManager.setStreamVolume(
            AudioManager.STREAM_MUSIC,
            if (isMuted) 0 else audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
            0
        )
        updateSeekBars() // Update SeekBars to reflect the change
        muteButton.setImageResource(if (isMuted) R.drawable.baseline_volume_off_24 else R.drawable.baseline_volume_mute_24)
    }

    // Update SeekBars to reflect the current system volume
    private fun updateSeekBars() {
        if (this::pgMedia.isInitialized) {
            pgMedia.progress = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            pgCall.progress = audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL)
            pgAlarm.progress = audioManager.getStreamVolume(AudioManager.STREAM_ALARM)
            pgRingtone.progress = audioManager.getStreamVolume(AudioManager.STREAM_RING)
            pgNotification.progress = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION)
        }
    }

    // Create a SeekBar change listener for updating volume
    private fun createSeekBarChangeListener(type: Int) = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            if (fromUser) {
                try {
                    audioManager.setStreamVolume(type, progress, 0)
                } catch (e: SecurityException) {
                    e.printStackTrace()
                    AppUtils.requestDoNotDisturbAccess(this@VolumeControlService)
                }
            }
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {}

        override fun onStopTrackingTouch(seekBar: SeekBar?) {}
    }

    // Show a toast message with the current volume percentage
    private fun showVolumeToast() {
        val volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val volumePercentage = (volume / maxVolume.toDouble() * 100).toInt()
        Toast.makeText(this, getString(R.string.label_volume, volumePercentage), Toast.LENGTH_SHORT)
            .show()
    }

    // Get the notification for the foreground service
    private fun getNotification(): Notification {
        val builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
        builder.setContentTitle(getString(R.string.label_volume_control_service))
            .setContentText(getString(R.string.label_running_in_background))
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
        return builder.build()
    }


    // Called when the service is destroyed
    override fun onDestroy() {
        super.onDestroy()
        windowManager.removeView(floatingView)
        unregisterReceiver(volumeReceiver)
        isServiceRunning = false
    }
}

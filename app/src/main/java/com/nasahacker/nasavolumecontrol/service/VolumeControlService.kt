package com.nasahacker.nasavolumecontrol.service

import android.app.Notification
import android.app.Service
import android.content.Intent
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
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import com.nasahacker.nasavolumecontrol.R
import com.nasahacker.nasavolumecontrol.util.Constants.NOTIFICATION_CHANNEL_ID
import com.nasahacker.nasavolumecontrol.util.Constants.NOTIFICATION_FOREGROUND_ID

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

        // Initialize WindowManager and AudioManager
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager

        // Inflate the floating view layout
        floatingView = LayoutInflater.from(this).inflate(R.layout.floting_volume_layout, null)

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

        // Set up click listeners for buttons
        floatingView.findViewById<ImageView>(R.id.minMax).setOnClickListener {
            toggleVisibility()
        }

        floatingView.findViewById<ImageView>(R.id.upButton).setOnClickListener {
            increaseSound()
        }

        floatingView.findViewById<ImageView>(R.id.downButton).setOnClickListener {
            decreaseSound()
        }

        floatingView.findViewById<ImageView>(R.id.muteButton).setOnClickListener {
            toggleMute()
        }
    }

    // Increase the media volume and show a toast message with volume percentage
    private fun increaseSound() {
        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, 0)
        showVolumeToast()
    }

    // Decrease the media volume and show a toast message with volume percentage
    private fun decreaseSound() {
        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, 0)
        showVolumeToast()
    }

    // Toggle mute status and show a toast message
    private fun toggleMute() {
        isMuted = !isMuted
        audioManager.setStreamVolume(
            AudioManager.STREAM_MUSIC,
            if (isMuted) 0 else audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
            0
        )
        showToast(if (isMuted) "Muted" else "Unmuted")
    }

    // Show the volume percentage in a toast message
    private fun showVolumeToast() {
        val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val volumePercentage = (currentVolume / maxVolume.toFloat() * 100).toInt()
        showToast("Volume: $volumePercentage%")
    }

    // Toggle the visibility of the floating view
    private fun toggleVisibility() {
        val mainLayout = floatingView.findViewById<LinearLayout>(R.id.mainLayout)
        mainLayout.visibility = if (isVisible) View.GONE else View.VISIBLE
        isVisible = !isVisible
    }

    // Show a toast message
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    //get notification
    private fun getNotification(): Notification {
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher) // Ensure this is a valid drawable resource
            .setContentTitle("Volume Control Service Running")
            .setContentText("Volume Control Service is running in the background.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
    }


    // Called when the service is destroyed
    override fun onDestroy() {
        super.onDestroy()
        if (::floatingView.isInitialized) {
            windowManager.removeView(floatingView)
        }
    }
}

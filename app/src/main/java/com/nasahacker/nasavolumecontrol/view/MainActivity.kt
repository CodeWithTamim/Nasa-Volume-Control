package com.nasahacker.nasavolumecontrol.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.nasahacker.nasavolumecontrol.R
import com.nasahacker.nasavolumecontrol.databinding.ActivityMainBinding
import com.nasahacker.nasavolumecontrol.service.VolumeControlService
import com.nasahacker.nasavolumecontrol.util.Helpers

class MainActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setupInitialState()
        setupListeners()
    }

    private fun setupInitialState() {
        // Set initial state of the boot start switch
        binding.switchBootStart.isChecked = Helpers.getIsStartOnBoot(this)
        Helpers.requestNotificationPermission(this)
    }

    private fun setupListeners() {
        // Listener for the boot start switch
        binding.switchBootStart.setOnCheckedChangeListener { _, isChecked ->
            Helpers.setIsStartOnBoot(this, isChecked)
        }

        // Listener for the stop button
        binding.btnStop.setOnClickListener {
            stopService(Intent(this, VolumeControlService::class.java))
        }

        // Listener for the start button
        binding.btnStart.setOnClickListener {
            if (Helpers.canDrawOverlay(this)) {
                startService(Intent(this, VolumeControlService::class.java))
            } else {
                Helpers.requestOverlayPermission(this)
                Toast.makeText(
                    this,
                    getString(R.string.label_please_grant_the_permission_to_draw_over_other_apps),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}

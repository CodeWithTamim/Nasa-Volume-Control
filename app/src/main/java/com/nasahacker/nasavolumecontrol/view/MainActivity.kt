package com.nasahacker.nasavolumecontrol.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.nasahacker.nasavolumecontrol.R
import com.nasahacker.nasavolumecontrol.databinding.ActivityMainBinding
import com.nasahacker.nasavolumecontrol.service.VolumeControlService
import com.nasahacker.nasavolumecontrol.util.Constant
import com.nasahacker.nasavolumecontrol.util.Helper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


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
        binding.switchBootStart.isChecked = Helper.getIsStartOnBoot(this)
        binding.switchAdvancedControls.isChecked = Helper.getIsAdvancedMode(this)
        binding.sbOpacity.progress = (Helper.getLayoutOpacity(this) * 100).toInt()
        Helper.requestNotificationPermission(this)
    }

    private fun setupListeners() {
        binding.sbOpacity.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (progress < Constant.SEEK_BAR_MIN_PROGRESS) {
                    Toast.makeText(
                        this@MainActivity,
                        getString(R.string.label_min_progress_is, Constant.SEEK_BAR_MIN_PROGRESS),
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.sbOpacity.progress = Constant.SEEK_BAR_MIN_PROGRESS
                } else {
                    lifecycleScope.launch(Dispatchers.IO) {
                        Helper.setLayoutOpacity(this@MainActivity, Helper.getFloatProgress(progress))
                        Helper.restartService(this@MainActivity)
                    }
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }

        })
        // Listener for the boot start switch
        binding.switchBootStart.setOnCheckedChangeListener { _, isChecked ->
            Helper.setIsStartOnBoot(this, isChecked)
        }

        //Listener for advanced controls
        binding.switchAdvancedControls.setOnCheckedChangeListener { _, isChecked ->
            Helper.setIsAdvancedMode(this, isChecked)
            Helper.restartService(this)
        }

        // Listener for the stop button
        binding.btnStop.setOnClickListener {
            Helper.stopService(this)
        }

        // Listener for the start button
        binding.btnStart.setOnClickListener {
            if (Helper.canDrawOverlay(this)) {
                Helper.startService(this)
            } else {
                Helper.requestOverlayPermission(this)
                Toast.makeText(
                    this,
                    getString(R.string.label_please_grant_the_permission_to_draw_over_other_apps),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}

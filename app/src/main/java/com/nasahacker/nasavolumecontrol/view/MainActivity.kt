package com.nasahacker.nasavolumecontrol.view

import android.os.Bundle
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.nasahacker.nasavolumecontrol.R
import com.nasahacker.nasavolumecontrol.databinding.ActivityMainBinding
import com.nasahacker.nasavolumecontrol.util.Constant
import com.nasahacker.nasavolumecontrol.util.AppUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        setupInitialState()
        setupListeners()
    }

    private fun setupInitialState() {
        // Set initial state of the boot start switch
        binding.switchBootStart.isChecked = AppUtils.getIsStartOnBoot(this)
        binding.switchAdvancedControls.isChecked = AppUtils.getIsAdvancedMode(this)
        binding.sbOpacity.progress = (AppUtils.getLayoutOpacity(this) * 100).toInt()
        AppUtils.requestNotificationPermission(this)
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
                        AppUtils.setLayoutOpacity(this@MainActivity, AppUtils.getFloatProgress(progress))
                        AppUtils.restartService(this@MainActivity)
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
            AppUtils.setIsStartOnBoot(this, isChecked)
        }

        //Listener for advanced controls
        binding.switchAdvancedControls.setOnCheckedChangeListener { _, isChecked ->
            AppUtils.setIsAdvancedMode(this, isChecked)
            AppUtils.restartService(this)
        }

        // Listener for the stop button
        binding.btnStop.setOnClickListener {
            AppUtils.stopService(this)
        }

        // Listener for the start button
        binding.btnStart.setOnClickListener {
            if (AppUtils.canDrawOverlay(this)) {
                AppUtils.startService(this)
            } else {
                AppUtils.requestOverlayPermission(this)
                Toast.makeText(
                    this,
                    getString(R.string.label_please_grant_the_permission_to_draw_over_other_apps),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}

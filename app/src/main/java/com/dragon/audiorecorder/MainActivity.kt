package com.dragon.audiorecorder

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import com.dragon.audiorecorder.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private var isGranted = false
    private val recorder = AudioRecorder()
    lateinit var binding: ActivityMainBinding

    private val preference by lazy {
        getPreferences(MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        recorder.isRecording.observe(this) {
            binding.startOrStopButton.text = if (it) "stop record" else "start record"
        }
        preference.getString("ip", null)?.let {
            binding.ipEditText.text = SpannableStringBuilder(it)
        }
        binding.startOrStopButton.setOnClickListener {
            recorder.isRecording.value?.let {
                if (it)
                    recorder.stop()
                else {
                    if (!isGranted) {
                        requestAudioPermission()
                        return@setOnClickListener
                    }
                    val ip = binding.ipEditText.editableText.toString()
                    if (ip.isNotEmpty())
                        preference.edit {
                            putString("ip", ip)
                        }
                    recorder.start(ip)
                }
            }
        }
        requestAudioPermission()
    }

    override fun onDestroy() {
        super.onDestroy()
        recorder.stop()
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101) {
            if (permissions.firstOrNull()?.contains(Manifest.permission.RECORD_AUDIO) == true) {
                grantResults.firstOrNull()?.let {
                    if (it == PackageManager.PERMISSION_GRANTED) {
                        isGranted = true
                        Toast.makeText(this, "audio grant success!!!", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this, "audio grant fail!!!", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun requestAudioPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 101)
        } else {
            isGranted = true
        }
    }
}
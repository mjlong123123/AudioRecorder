package com.dragon.audiorecorder

import android.os.Bundle
import android.text.SpannableStringBuilder
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.dragon.audiorecorder.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
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
                    val ip = binding.ipEditText.editableText.toString()
                    if (ip.isNotEmpty())
                        preference.edit {
                            putString("ip", ip)
                        }
                    recorder.start(ip)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        recorder.stop()
    }
}
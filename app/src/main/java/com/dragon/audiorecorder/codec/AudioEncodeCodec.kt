package com.dragon.audiorecorder.codec

import android.media.*
import android.media.AudioFormat.CHANNEL_IN_MONO
import android.media.AudioFormat.CHANNEL_IN_STEREO
import android.util.Log
import com.dragon.audiorecorder.background.runInBackground
import java.lang.Exception

/**
 * @author dragon
 */
abstract class AudioEncodeCodec(mediaFormat: MediaFormat) : BufferEncodeCodec("Encode audio", mediaFormat) {
    private lateinit var audioRecord: AudioRecord
    private val sampleRate = mediaFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE)
    private val channelCount = mediaFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
    private val minBufferSize = AudioRecord.getMinBufferSize(sampleRate, if (channelCount == 1) CHANNEL_IN_MONO else CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT);


    init {
        runInBackground {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                if (channelCount == 1) CHANNEL_IN_MONO else CHANNEL_IN_STEREO,
                AudioFormat.ENCODING_PCM_16BIT,
                2 * minBufferSize
            )
            audioRecord.startRecording()
        }
    }

    override fun releaseInternal() {
        audioRecord.stop()
        audioRecord.release()
        super.releaseInternal()
    }


    override fun onInputBufferAvailable(codec: MediaCodec, index: Int) {
        try {
            codec.getInputBuffer(index)?.let { bb ->
                var startTime = System.currentTimeMillis();
                var readSize = audioRecord.read(bb, bb.capacity())
                log { "read time ${System.currentTimeMillis() - startTime} read size $readSize" }
                if (readSize < 0) {
                    readSize = 0
                }
                codec.queueInputBuffer(index, 0, readSize, System.nanoTime() / 1000, 0)
            }
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    fun log(block: () -> String) {
        Log.d("AudioEncodeCodec[${Thread.currentThread().name}]", block.invoke())
    }
}
package com.dragon.audiorecorder.codec

import android.media.MediaCodec
import android.media.MediaFormat
import android.util.Log
import com.dragon.audiorecorder.background.BackgroundScope
import com.dragon.audiorecorder.background.HandlerThreadScope
import com.dragon.audiorecorder.background.quit
import com.dragon.audiorecorder.background.runInBackground

/**
 * @author dragon
 */
abstract class BaseCodec(
    val name: String = "BaseCodec",
    val mediaFormat: MediaFormat
) : MediaCodec.Callback(), BackgroundScope by HandlerThreadScope() {

    lateinit var mediaCodec: MediaCodec

    init {
        runInBackground {
            mediaCodec = onCreateMediaCodec(mediaFormat)
            onConfigMediaCodec(mediaCodec)
            mediaCodec.setCallback(object : MediaCodec.Callback() {
                override fun onInputBufferAvailable(codec: MediaCodec, index: Int) {
                    runInBackground {
                        this@BaseCodec.onInputBufferAvailable(codec, index)
                    }
                }

                override fun onOutputBufferAvailable(codec: MediaCodec, index: Int, info: MediaCodec.BufferInfo) {
                    runInBackground {
                        this@BaseCodec.onOutputBufferAvailable(codec, index, info)
                    }
                }

                override fun onError(codec: MediaCodec, e: MediaCodec.CodecException) {
                    runInBackground {
                        this@BaseCodec.onError(codec, e)
                    }
                }

                override fun onOutputFormatChanged(codec: MediaCodec, format: MediaFormat) {
                    runInBackground {
                        this@BaseCodec.onOutputFormatChanged(codec, format)
                    }
                }
            })
            mediaCodec.start()
        }
    }


    fun release(afterBlock: () -> Unit = {}) = runInBackground {
        releaseInternal()
        afterBlock.invoke()
        quit()
    }

    protected open fun releaseInternal() {
        mediaCodec.flush()
        mediaCodec.stop()
        mediaCodec.release()
    }

    protected abstract fun onCreateMediaCodec(mediaFormat: MediaFormat): MediaCodec

    protected abstract fun onConfigMediaCodec(mediaCodec: MediaCodec)

    override fun onError(codec: MediaCodec, e: MediaCodec.CodecException) {
        Log.d("name", e.message ?: "")
    }
}
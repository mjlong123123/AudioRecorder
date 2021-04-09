package com.dragon.audiorecorder

import android.media.*
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dragon.audiorecorder.codec.AudioEncodeCodec
import com.dragon.audiorecorder.codec.BaseCodec
import com.dragon.rtplib.RtpWrapper

class AudioRecorder {

    private val _isRecording = MutableLiveData<Boolean>()
    val isRecording: LiveData<Boolean> = _isRecording


    private val audioPayloadType = 97;
    private val audioRtpPort = 40020;
    private val audioBitRate = 128 * 1024;
    private val audioChannelCount = 1;
    private val audioSampleRate = 44100;
    private val audioMinBufferSize =
        AudioRecord.getMinBufferSize(
            audioSampleRate,
            if (audioChannelCount == 1) AudioFormat.CHANNEL_IN_MONO else AudioFormat.CHANNEL_IN_STEREO,
            AudioFormat.ENCODING_PCM_16BIT
        );
    private val audioMaxBufferSize = audioMinBufferSize * 3;
    private val audioBuffer = ByteArray(audioMaxBufferSize)
    private var lastSendAudioTime: Long = 0

    private val audioProfile = 1

    /**
     *  97000, 88200, 64000, 48000,44100, 32000, 24000, 22050,16000, 12000, 11025, 8000,7350, 0, 0, 0
     */
    private val audioIndex = 4
    private val audioSpecificConfig = ByteArray(2).apply {
        this[0] = ((audioProfile + 1).shl(3).and(0xff)).or(audioIndex.ushr(1).and(0xff)).toByte()
        this[1] = ((audioIndex.shl(7).and(0xff)).or(audioChannelCount.shl(3).and(0xff))).toByte()
    }

    private val auHeaderLength = ByteArray(2).apply {
        this[0] = 0
        this[1] = 0x10
    }

    private fun auHeader(len: Int): ByteArray {
        return ByteArray(2).apply {
            this[0] = (len and 0x1fe0 shr 5).toByte()
            this[1] = (len and 0x1f shl 3).toByte()
        }
    }

    private var audioRtpWrapper: RtpWrapper? = null;
    private var audioCodec: BaseCodec? = null
    private var bufferArray = ByteArray(audioMinBufferSize * 2)

    init {
        _isRecording.value = false
    }

    fun start(ip: String) {
        _isRecording.value ?: return
        if (_isRecording.value!!) {
            return
        }
        _isRecording.value = true
        val mediaFormat = MediaFormat.createAudioFormat(
            MediaFormat.MIMETYPE_AUDIO_AAC,
            audioSampleRate,
            audioChannelCount
        )
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, audioBitRate)
        mediaFormat.setInteger(
            MediaFormat.KEY_AAC_PROFILE,
            MediaCodecInfo.CodecProfileLevel.AACObjectLC
        )
        mediaFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, audioMaxBufferSize)
        audioCodec = object : AudioEncodeCodec(mediaFormat) {
            override fun onOutputBufferAvailable(
                codec: MediaCodec,
                index: Int,
                info: MediaCodec.BufferInfo
            ) {
                try {
                    val buffer = codec.getOutputBuffer(index) ?: return
                    if (lastSendAudioTime == 0L) {
                        lastSendAudioTime = info.presentationTimeUs;
                    }
                    val increase =
                        (info.presentationTimeUs - lastSendAudioTime) * audioSampleRate / 1000 / 1000
                    buffer.position(info.offset)
                    buffer.get(bufferArray, 0, info.size)
                    audioRtpWrapper?.sendData(bufferArray, info.size, 97, true, increase.toInt())
                    lastSendAudioTime = info.presentationTimeUs
                    codec.releaseOutputBuffer(index, false)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onOutputFormatChanged(codec: MediaCodec, format: MediaFormat) {
                audioRtpWrapper = RtpWrapper()
                audioRtpWrapper?.open(audioRtpPort, audioPayloadType, audioSampleRate)
                audioRtpWrapper?.addDestinationIp(ip)
            }
        }

    }

    fun stop() {
        _isRecording.value ?: return
        if (_isRecording.value!!.not()) {
            return
        }
        _isRecording.value = false
        audioCodec?.release {
            audioRtpWrapper?.close()
        }
    }
}
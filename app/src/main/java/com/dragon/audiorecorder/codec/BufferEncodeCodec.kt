package com.dragon.audiorecorder.codec

import android.media.MediaCodec
import android.media.MediaCodecList
import android.media.MediaFormat

/**
 * @author dragon
 */
abstract class BufferEncodeCodec(name: String = "Encode buffer",mediaFormat: MediaFormat) : BaseCodec(name, mediaFormat) {

    override fun onCreateMediaCodec(mediaFormat: MediaFormat): MediaCodec {
        val mediaCodecList = MediaCodecList(MediaCodecList.ALL_CODECS)
        val codecName = mediaCodecList.findEncoderForFormat(mediaFormat)
        check(!codecName.isNullOrEmpty()) { throw RuntimeException("not find the matched codec!!!!!!!") }
        return MediaCodec.createByCodecName(codecName)
    }

    override fun onConfigMediaCodec(mediaCodec: MediaCodec) {
        mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
    }
}
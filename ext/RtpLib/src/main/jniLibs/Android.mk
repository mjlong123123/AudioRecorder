LOCAL_PATH := $(call my-dir)



include $(CLEAR_VARS)
LOCAL_SRC_FILES:= \
                libjthread/src/jmutex.cpp \
                libjthread/src/jthread.cpp \
LOCAL_CFLAGS +=  -DHAVE_PTHREAD \
                 -DENABLE_TRACE \
LOCAL_C_INCLUDES:= $(LOCAL_PATH)/libjthread/include $(LOCAL_PATH)/libjthread/src 
LOCAL_EXPORT_C_INCLUDES:=$(LOCAL_C_INCLUDES)
LOCAL_PRELINK_MODULE := false
LOCAL_LDLIBS +=   -ldl -llog
LOCAL_MODULE:= libjthread
include $(BUILD_STATIC_LIBRARY)


include $(CLEAR_VARS)
LOCAL_SRC_FILES:=libjrtp/tools/gettypes.cpp \
                libjrtp/src/extratransmitters/rtpfaketransmitter.cpp \
                libjrtp/src/rtcpapppacket.cpp \
                libjrtp/src/rtcpbyepacket.cpp \
                libjrtp/src/rtcpcompoundpacket.cpp \
                libjrtp/src/rtcpcompoundpacketbuilder.cpp \
                libjrtp/src/rtcppacket.cpp \
                libjrtp/src/rtcppacketbuilder.cpp \
                libjrtp/src/rtcprrpacket.cpp \
                libjrtp/src/rtcpscheduler.cpp \
                libjrtp/src/rtcpsdesinfo.cpp \
                libjrtp/src/rtcpsdespacket.cpp \
                libjrtp/src/rtcpsrpacket.cpp \
                libjrtp/src/rtpcollisionlist.cpp \
                libjrtp/src/rtpdebug.cpp \
                libjrtp/src/rtperrors.cpp \
                libjrtp/src/rtpinternalsourcedata.cpp \
                libjrtp/src/rtpipv4address.cpp \
                libjrtp/src/rtpipv6address.cpp \
                libjrtp/src/rtplibraryversion.cpp \
                libjrtp/src/rtppacket.cpp \
                libjrtp/src/rtppacketbuilder.cpp \
                libjrtp/src/rtppollthread.cpp \
                libjrtp/src/rtprandom.cpp \
                libjrtp/src/rtpsession.cpp \
                libjrtp/src/rtpsessionparams.cpp \
                libjrtp/src/rtpsessionsources.cpp \
                libjrtp/src/rtpsourcedata.cpp \
                libjrtp/src/rtpsources.cpp \
                libjrtp/src/rtptimeutilities.cpp \
                libjrtp/src/rtpudpv4transmitter.cpp \
                libjrtp/src/rtpudpv6transmitter.cpp
LOCAL_CFLAGS += -DHAVE_TIME_H \-DHAVE_FCNTL_H \
                                -DHAVE_SYS_SELECT_H \
                                -DENABLE_TRACE \
                                -DHAVE_RTP_SUPPORT_IPV4MULTICAST\
                                -DOSIP_MT
LOCAL_C_INCLUDES:= $(LOCAL_PATH)/libjrtp/include $(LOCAL_PATH)/libjrtp/src $(LOCAL_PATH)/libjrtp/tools
LOCAL_EXPORT_C_INCLUDES:=$(LOCAL_C_INCLUDES)
LOCAL_STATIC_LIBRARIES :=\
           libjthread \
           
#LOCAL_LDLIBS += -lpthread -ldl
LOCAL_PRELINK_MODULE := false
LOCAL_MODULE:= libjrtp
include $(BUILD_STATIC_LIBRARY)
#$(call import-module,../../speex/jni)


include $(CLEAR_VARS)
LOCAL_MODULE    := rtp-jni
LOCAL_SRC_FILES := com_android_localcall_jni_rtp.cpp \
customrtpsession.cpp \
customspeex.cpp \
customrtp.cpp \
speex_jni.cpp
LOCAL_CFLAGS +=   -DENABLE_TRACE
LOCAL_C_INCLUDES:= $(LOCAL_PATH)/libjthread/include $(LOCAL_PATH)/libjthread/src $(LOCAL_PATH)/libjrtp/include $(LOCAL_PATH)/libjrtp/src $(LOCAL_PATH)/libjrtp/tools \
$(LOCAL_PATH)/speex/include
LOCAL_EXPORT_C_INCLUDES:=$(LOCAL_C_INCLUDES)
LOCAL_STATIC_LIBRARIES :=\
           libjthread libjrtp libspeex
LOCAL_LDLIBS += -llog
include $(BUILD_SHARED_LIBRARY)


include $(call all-makefiles-under,$(LOCAL_PATH))

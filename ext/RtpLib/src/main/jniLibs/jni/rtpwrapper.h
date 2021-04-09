//
// Created by 30562 on 2021/1/9.
//

#ifndef COMMONLIBDEMO_RTPWRAPPER_H
#define COMMONLIBDEMO_RTPWRAPPER_H

#include <jni.h>
#include <rtpsessionparams.h>
#include <rtpudpv4transmitter.h>
#include "rtpsession.h"
#include "logger.h"

struct RtpWrapperFields {
    jfieldID nativeObject;
    jmethodID callback;
};

class RtpWrapper : public RTPSession {

public:
    RtpWrapper(JavaVM *javaVm, jobject obj);

    virtual ~RtpWrapper();

    bool open(int port, int payloadType, int sampleRate);

    bool close();

    int getPortBase() {
        return transParams.GetPortbase();
    }

protected:
    virtual void OnPollThreadError(int errcode);

    virtual void OnPollThreadStep();

private:
    void onProcessData(const RTPSourceData *srcData, const RTPPacket *rtpPacket);

    void callJavaMethod(const char *buffer, const int size);

    void checkRtpError(int code) {
        if (code >= 0) {
            return;
        }
        log(TAG, "checkRtpError %s\n", RTPGetErrorString(code).c_str());
    }

private:
    const char *TAG = "RtpWrapper";
    JavaVM *m_javaVM = nullptr;
    jobject m_javaObj = nullptr;
    RTPSessionParams sessionParams;
    RTPUDPv4TransmissionParams transParams;
};


#endif //COMMONLIBDEMO_RTPWRAPPER_H

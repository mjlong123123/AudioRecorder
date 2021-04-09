//
// Created by 30562 on 2021/1/9.
//

#include "rtpwrapper.h"
#include <jni.h>
#include <android/log.h>
#include <rtpsessionparams.h>
#include "rtpudpv4transmitter.h"
#include "rtpipv4address.h"
#include "rtperrors.h"
#include "define.h"
#include "logger.h"
#include "rtpsession.h"
#include <rtpsessionparams.h>
#include <rtpudpv4transmitter.h>
#include <rtppacket.h>
#include <rtpsourcedata.h>

extern RtpWrapperFields rtpWrapperFields;

RtpWrapper::RtpWrapper(JavaVM *javaVm, jobject obj) : m_javaVM(javaVm), m_javaObj(obj) {
    log(TAG, "RtpWrapper");
    sessionParams.SetJavaVM(javaVm);
    sessionParams.SetAcceptOwnPackets(true);
    sessionParams.SetUsePollThread(true);
    sessionParams.SetMaximumPacketSize(65535 - 100);
}

RtpWrapper::~RtpWrapper() {
    log(TAG, "~RtpWrapper");
}

bool RtpWrapper::open(int port, int payloadType, int sampleRate) {
    sessionParams.SetOwnTimestampUnit(1.0 / sampleRate);
    transParams.SetPortbase(port);
    checkRtpError(Create(sessionParams, &transParams));
    checkRtpError(SetDefaultPayloadType(payloadType));
    checkRtpError(SetDefaultMark(true));
    checkRtpError(SetDefaultTimestampIncrement(sampleRate / 30));
    return true;
}


bool RtpWrapper::close() {
    BYEDestroy(RTPTime(1, 0), 0, 0);
    return true;
}

void RtpWrapper::onProcessData(const RTPSourceData *srcData, const RTPPacket *rtpPacket) {
    callJavaMethod((const char *) rtpPacket->GetPayloadData(), rtpPacket->GetPayloadLength());
}

void RtpWrapper::callJavaMethod(const char *buffer, const int size) {
    JNIEnv *env;
    if (m_javaVM->GetEnv((void **) &env, JNI_VERSION_1_4) != JNI_OK) {
        log(TAG, "RtpWrapper::callJavaMethod m_javaVM->GetEnv error");
        return;
    }
    if (env == NULL) {
        log(TAG, "RtpWrapper::callJavaMethod env is null");
        return;
    }
    jbyteArray bufret;
    bufret = env->NewByteArray(size);
    env->SetByteArrayRegion(bufret, 0, size, (const jbyte *) buffer);
    env->CallVoidMethod(m_javaObj, rtpWrapperFields.callback, bufret, size);
    env->DeleteLocalRef(bufret);
}

void RtpWrapper::OnPollThreadStep() {
    BeginDataAccess();
    if (GotoFirstSourceWithData()) {
        do {
            RTPPacket *pack;
            while ((pack = GetNextPacket()) != nullptr) {
                onProcessData(GetCurrentSourceInfo(), pack);
                DeletePacket(pack);
            }
        } while (GotoNextSourceWithData());
    }
    EndDataAccess();
}

void RtpWrapper::OnPollThreadError(int errorCode) {
    log(TAG, "OnPollThreadError errorCode:%d", errorCode);
}


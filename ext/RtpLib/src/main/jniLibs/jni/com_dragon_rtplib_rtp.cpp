#include <jni.h>


#ifndef WIN32

#include <netinet/in.h>
#include <arpa/inet.h>

#else
#include <winsock2.h>
#endif // WIN32

#include <cstdlib>
#include <cstdio>
#include <iostream>
#include <string>
#include "rtpwrapper.h"
#include "rtpipv4address.h"

const char *const LOG_TAG = "RTP_JNI";

//throw java exception.
void jniThrowException(JNIEnv *env, const char *exc, const char *msg = NULL) {
    jclass excClazz = env->FindClass(exc);
    env->ThrowNew(excClazz, msg);
}

RtpWrapperFields rtpWrapperFields;


inline RtpWrapper *getRtpWrapperFromJavaObject(JNIEnv *env, jobject obj) {
    return (RtpWrapper *) env->GetLongField(obj, rtpWrapperFields.nativeObject);
}


extern "C"
JNIEXPORT void JNICALL
Java_com_dragon_rtplib_RtpWrapper_initLib(JNIEnv *env, jclass clazz) {
    rtpWrapperFields.nativeObject = env->GetFieldID(clazz, "nativeObject", "J");
    if (rtpWrapperFields.nativeObject == nullptr) {
        jniThrowException(env, "java/lang/RuntimeException", "Can't find nativeObject");
        return;
    }

    rtpWrapperFields.callback = env->GetMethodID(clazz, "receivedData", "([BI)V");
    if (rtpWrapperFields.callback == nullptr) {
        jniThrowException(env, "java/lang/RuntimeException", "Can't find receivedData");
        return;
    }
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_dragon_rtplib_RtpWrapper_init(JNIEnv *env, jobject thiz) {
    RtpWrapper *rtpWrapper = getRtpWrapperFromJavaObject(env, thiz);
    if (rtpWrapper != nullptr) {
        delete rtpWrapper;
    }
    JavaVM *javaVm;
    env->GetJavaVM(&javaVm);
    env->SetLongField(thiz, rtpWrapperFields.nativeObject, (jlong) new RtpWrapper(javaVm, env->NewGlobalRef(thiz)));
    return true;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_dragon_rtplib_RtpWrapper_open(JNIEnv *env, jobject thiz, jint port, jint payloadType, jint sampleRate) {
    RtpWrapper *rtpWrapper = getRtpWrapperFromJavaObject(env, thiz);
    if (rtpWrapper == nullptr) {
        log(LOG_TAG, "RtpWrapper_open rtpWrapper == nullptr");
        return false;
    }
    rtpWrapper->open(port, payloadType, sampleRate);
    return true;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_dragon_rtplib_RtpWrapper_sendData(JNIEnv *env, jobject thiz, jbyteArray buffer, jint len, jint pt, jboolean mark, jint increase) {
    RtpWrapper *rtpWrapper = getRtpWrapperFromJavaObject(env, thiz);
    if (rtpWrapper == nullptr) {
        log(LOG_TAG, "RtpWrapper_sendData rtpWrapper == nullptr");
        return 0;
    }
    int size = env->GetArrayLength(buffer);
    if (size < len) {
        log(LOG_TAG, "RtpWrapper_sendData size < len");
        return 0;
    }
    jboolean copy = false;
    jbyte *data = env->GetByteArrayElements(buffer, &copy);
    rtpWrapper->SendPacket(data, len, pt, mark, increase);
    env->ReleaseByteArrayElements(buffer, data, 0);
    return 1;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_dragon_rtplib_RtpWrapper_close(JNIEnv *env, jobject thiz) {
    RtpWrapper *rtpWrapper = getRtpWrapperFromJavaObject(env, thiz);
    if (rtpWrapper == nullptr) {
        log(LOG_TAG, "RtpWrapper_close rtpWrapper == nullptr");
        return false;
    }
    rtpWrapper->close();
    return true;
}extern "C"
JNIEXPORT jboolean JNICALL
Java_com_dragon_rtplib_RtpWrapper_release(JNIEnv *env, jobject thiz) {
    RtpWrapper *rtpWrapper = getRtpWrapperFromJavaObject(env, thiz);
    if (rtpWrapper != nullptr) {
        delete rtpWrapper;
    }
    return true;
}extern "C"
JNIEXPORT jboolean JNICALL
Java_com_dragon_rtplib_RtpWrapper_addDestinationIp(JNIEnv *env, jobject thiz, jstring ip) {
    RtpWrapper *rtpWrapper = getRtpWrapperFromJavaObject(env, thiz);
    if (rtpWrapper == nullptr) {
        log(LOG_TAG, "RtpWrapper_addDestinationIp rtpWrapper == nullptr");
        return false;
    }
    jboolean copy = false;
    jchar const *ipchar = nullptr;
    int lenth = 0;
    int i = 0;
    char buf[50];
    std::string ipstr;
    uint32_t destip;
    ipchar = env->GetStringChars(ip, &copy);
    lenth = env->GetStringLength(ip);
    memset(buf, 0, sizeof(buf));
    for (i = 0; i < lenth; i++) {
        buf[i] = (char) ipchar[i];
    }
    env->ReleaseStringChars(ip, ipchar);
    ipstr = std::string((char *) buf);
    destip = inet_addr(ipstr.c_str());
    destip = ntohl(destip);
    RTPIPv4Address addr(destip, rtpWrapper->getPortBase());
    rtpWrapper->AddDestination(addr);
    return true;
}
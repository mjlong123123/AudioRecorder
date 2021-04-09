//
// Created by 30562 on 2021/1/9.
//

#ifndef COMMONLIBDEMO_LOGGER_H
#define COMMONLIBDEMO_LOGGER_H
#include <android/log.h>
#define log(tag, ...)   __android_log_print(ANDROID_LOG_DEBUG, tag,__VA_ARGS__)
#endif //COMMONLIBDEMO_LOGGER_H

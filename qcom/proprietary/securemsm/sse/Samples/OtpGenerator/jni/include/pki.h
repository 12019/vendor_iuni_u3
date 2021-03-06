/*
 * Copyright (c) 2013 Qualcomm Technologies, Inc.  All Rights Reserved.
 * Qualcomm Technologies Proprietary and Confidential.
 */
#ifndef PKI_H
#define PKI_H

#include <cryptoki.h>
#include <android/log.h>
#include <stdlib.h>

#define DEBUG    true
#define PKI_LOG_TAG "------PKI------"

#if DEBUG
  #define LOGV(...) __android_log_print(ANDROID_LOG_VERBOSE, PKI_LOG_TAG, __VA_ARGS__)
  #define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,   PKI_LOG_TAG, __VA_ARGS__)
  #define LOGI(...) __android_log_print(ANDROID_LOG_INFO,    PKI_LOG_TAG, __VA_ARGS__)
  #define LOGW(...) __android_log_print(ANDROID_LOG_WARN,    PKI_LOG_TAG, __VA_ARGS__)
#else
  #define LOGV(...) do {} while (0)
  #define LOGD(...) do {} while (0)
  #define LOGI(...) do {} while (0)
  #define LOGW(...) do {} while (0)
#endif

#define LOGE(fmt, args...) LOGe("%s, line %d :" fmt , __FILE__, __LINE__ , ## args)
#define LOGe(...) __android_log_print(ANDROID_LOG_ERROR, PKI_LOG_TAG, __VA_ARGS__)

// helpers
#define ENTER   LOGD("%s+",__FUNCTION__)
#define EXIT    LOGD("%s-",__FUNCTION__)

#define EXITV(x)   LOGD("%s-: %08X",__FUNCTION__,(x)); return x


class Pki {
  private:
    static const char*  userName = "User";       // A String holds the user name.
    static const char*  userPin  = "01234567";   // A String holds the user pin.
    static char*  salt;                          // A variable of type String holds the salt value of OTP function.

    static Pki* instance;                       // A static variable of type Pki holds the instance of this class.
    static bool pkcsModule;                     // A variable of type Module holds the module of the Pki.

    static CK_FUNCTION_LIST_PTR pFunctionList;
    static CK_SLOT_ID currentSlotId;

    static bool isTimeBased;           // A variable of type boolean holds if time based OTP is selected.

  private:
    Pki(const char* label);
    ~Pki();
    bool initModule(const char* intStr);

  public:
    static Pki* getPkiInstance();
    static Pki* initPkiInstance(const char* initPki);
    static void  closePki();

    static const char* getSalt();
    static void  setSalt(const char* salt);

    bool obtainToken (const char* tokenLabel);
    bool createOTPKey(const char* password, const char* keyLabel);
    CK_OBJECT_HANDLE retrieveKey(const char* keyLabel);
    bool deleteOTPKey(const char* keyLabel);
    long generateOTP (CK_OBJECT_HANDLE key);
    bool setTimeBasedOTP(bool val);

    static CK_RV create_mutex(void **mutex);
    static CK_RV destroy_mutex(void *mutex);
    static CK_RV lock_mutex(void *mutex);
    static CK_RV unlock_mutex(void *mutex);

  private:

};

#endif

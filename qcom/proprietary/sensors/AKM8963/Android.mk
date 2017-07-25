#gionee liujiang 2013-10-17 add for akm8963 pdc start
ifeq ($(GN_Q_BSP_AKM8963_PDC_SUPPORT), no)
LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_PREBUILT_LIBS := libAK8963/libAK8963.a
LOCAL_MODULE_TAGS := optional
include $(BUILD_MULTI_PREBUILT)

include $(CLEAR_VARS)
LOCAL_C_INCLUDES += $(LOCAL_PATH)
LOCAL_C_INCLUDES += $(LOCAL_PATH)/libAK8963
LOCAL_SRC_FILES += \
	AKMD_APIs.c \
	FileIO.c \
	Measure.c

LOCAL_WHOLE_STATIC_LIBRARIES += libAK8963
LOCAL_SHARED_LIBRARIES += liblog
LOCAL_MODULE:=libAKM8963
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE_PATH := $(TARGET_OUT_VENDOR_SHARED_LIBRARIES)
LOCAL_PRELINK_MODULE := false
include $(BUILD_SHARED_LIBRARY)
endif
#gionee liujiang 2013-10-17 add for akm8963 pdc end
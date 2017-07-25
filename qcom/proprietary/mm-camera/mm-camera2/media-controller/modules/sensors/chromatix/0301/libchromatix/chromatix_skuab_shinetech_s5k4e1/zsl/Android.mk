SKUAB_ST_s5k4e1_CHROMATIX_zsl_PATH := $(call my-dir)

# ---------------------------------------------------------------------------
#                      Make the shared library (libchromatix_SKUAB_ST_s5k4e1_zsl)
# ---------------------------------------------------------------------------

include $(CLEAR_VARS)
LOCAL_PATH := $(SKUAB_ST_s5k4e1_CHROMATIX_zsl_PATH)
LOCAL_MODULE_TAGS := optional

LOCAL_CFLAGS:= -DAMSS_VERSION=$(AMSS_VERSION) \
        $(mmcamera_debug_defines) \
        $(mmcamera_debug_cflags) \
        -include camera_defs_i.h

LOCAL_C_INCLUDES := $(LOCAL_PATH)/../../../
LOCAL_C_INCLUDES += $(LOCAL_PATH)/../../../../../module/
LOCAL_C_INCLUDES += $(LOCAL_PATH)/../../../../../../../../../common/
LOCAL_C_INCLUDES += chromatix_s5k4e1_zsl.h

LOCAL_SRC_FILES:= chromatix_s5k4e1_zsl.c

LOCAL_MODULE           := libchromatix_SKUAB_ST_s5k4e1_zsl

LOCAL_SHARED_LIBRARIES := libcutils
include $(LOCAL_PATH)/../../../../../../../../../local_additional_dependency.mk

ifeq ($(MM_DEBUG),true)
LOCAL_SHARED_LIBRARIES += liblog
endif

LOCAL_MODULE_OWNER := qcom
LOCAL_PROPRIETARY_MODULE := true

include $(BUILD_SHARED_LIBRARY)

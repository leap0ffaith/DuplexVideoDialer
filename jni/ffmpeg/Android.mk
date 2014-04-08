
LOCAL_PATH := $(call my-dir)

#declare the prebuilt library

include $(CLEAR_VARS)

LOCAL_MODULE := ffmpeg

LOCAL_SRC_FILES := android-ffmpeg/libffmpeg.so

LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/android-ffmpeg/include

#LOCAL_EXPORT_LDLIBS := -llog -lz $(LOCAL_PATH)/android-ffmpeg/lib/libx264.a $(LOCAL_PATH)/android-ffmpeg/libffmpeg.so
LOCAL_EXPORT_LDLIBS := -llog -lz $(LOCAL_PATH)/android-ffmpeg/libffmpeg.so

LOCAL_PRELINK_MODULE := true

include $(PREBUILT_SHARED_LIBRARY)

#the ffmpeg-test-jni library
 
include $(CLEAR_VARS)

LOCAL_ALLOW_UNDEFINED_SYMBOLS=false

LOCAL_MODULE := ffmpeg_decoder

LOCAL_SRC_FILES := ffmpeg_decoder.c

LOCAL_C_INCLUDES := $(LOCAL_PATH)/android-ffmpeg/include

LOCAL_SHARED_LIBRARY := ffmpeg

#LOCAL_STATIC_LIBRARIES := libavcodec libavdevice libavfilter libavformat libavutil libswresample libswscale

#LOCAL_LDLIBS    := -llog -lz -lm $(LOCAL_PATH)/android-ffmpeg/lib/libx264.a $(LOCAL_PATH)/android-ffmpeg/libffmpeg.so
LOCAL_LDLIBS    := -llog -lz -lm $(LOCAL_PATH)/android-ffmpeg/libffmpeg.so

include $(BUILD_SHARED_LIBRARY)

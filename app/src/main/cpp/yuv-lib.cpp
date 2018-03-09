#include <jni.h>
#include <string>
#include "libyuv.h"

uint8 *argb;
uint8 *dstArgb;

extern "C"
JNIEXPORT jint JNICALL nativeNV21ToNV12(JNIEnv *env, jclass type, jbyteArray nv21_,
                                        jbyteArray nv12_, jint width, jint height) {
    jbyte *nv21 = env->GetByteArrayElements(nv21_, NULL);
    jbyte *nv12 = env->GetByteArrayElements(nv12_, NULL);

    uint8 *i420 = (uint8 *) new char[width * height * 3 / 2];

    int y_stride = width;
    int u_stride = width >> 1;
    int v_stride = u_stride;

    size_t ySize = (size_t) (y_stride * height);

    libyuv::NV21ToI420((uint8 *) nv21, y_stride, (uint8 *) (nv21 + ySize),
                       y_stride,
                       i420, y_stride, (i420 + ySize), u_stride,
                       i420 + ySize + ySize / 4, v_stride,
                       width, height);

    libyuv::I420ToNV12(i420, y_stride, i420 + ySize, u_stride,
                       i420 + ySize + ySize / 4, v_stride, (uint8 *) nv12,
                       y_stride,
                       (uint8 *) (nv12 + ySize), y_stride, width, height);

    delete i420;

    env->ReleaseByteArrayElements(nv21_, nv21, 0);
    env->ReleaseByteArrayElements(nv12_, nv12, 0);

    return 0;
}

extern "C"
JNIEXPORT jint JNICALL nativeNV21ToI420(JNIEnv *env, jclass type, jbyteArray nv21_,
                                        jbyteArray i420_, jint width, jint height) {
    jbyte *nv21 = env->GetByteArrayElements(nv21_, NULL);
    jbyte *i420 = env->GetByteArrayElements(i420_, NULL);

    int y_stride = width;
    int u_stride = width >> 1;
    int v_stride = u_stride;

    size_t ySize = (size_t) (y_stride * height);

    libyuv::NV21ToI420((uint8 *) nv21, y_stride, (uint8 *) (nv21 + ySize),
                       y_stride,
                       (uint8 *) i420, y_stride, (uint8 *) (i420 + ySize), u_stride,
                       (uint8 *) (i420 + ySize + ySize / 4), v_stride,
                       width, height);

    env->ReleaseByteArrayElements(nv21_, nv21, 0);
    env->ReleaseByteArrayElements(i420_, i420, 0);

    return 0;
}

extern "C"
JNIEXPORT jint JNICALL nativeNV21ToYV12(JNIEnv *env, jclass type, jbyteArray nv21_,
                                        jbyteArray yv12_, jint width, jint height) {
    jbyte *nv21 = env->GetByteArrayElements(nv21_, NULL);
    jbyte *yv12 = env->GetByteArrayElements(yv12_, NULL);

    // TODO
    int y_stride = width;
    int u_stride = width >> 1;
    int v_stride = u_stride;

    size_t ySize = (size_t) (y_stride * height);

    libyuv::NV12ToI420((uint8 *) nv21, y_stride, (uint8 *) (nv21 + ySize),
                       y_stride,
                       (uint8 *) yv12, y_stride, (uint8 *) (yv12 + ySize), u_stride,
                       (uint8 *) (yv12 + ySize + ySize / 4), v_stride,
                       width, height);

    env->ReleaseByteArrayElements(nv21_, nv21, 0);
    env->ReleaseByteArrayElements(yv12_, yv12, 0);

    return 0;
}

extern "C"
JNIEXPORT jint JNICALL nativeNV21ToRGB24(JNIEnv *env, jclass type,
                                         jbyteArray yuvBytes_, jbyteArray rgb24Bytes_,
                                         jintArray hw_, jint orientation) {
    jbyte *yuvBytes = env->GetByteArrayElements(yuvBytes_, NULL);
    jbyte *rgb24Bytes = env->GetByteArrayElements(rgb24Bytes_, NULL);
    jint *hw = env->GetIntArrayElements(hw_, NULL);

    int width = hw[0];
    int height = hw[1];

    int y_stride = width;
    int u_stride = width >> 1;
    int v_stride = u_stride;

    int dst_y_stride = 0;
    int dst_u_stride = 0;
    int dst_v_stride = 0;

    size_t ySize = (size_t) (y_stride * height);
    size_t dst_ysize = 0;

    uint8 *i420 = new uint8[width * height * 3 / 2];
    uint8 *dst_i420 = new uint8[width * height * 3 / 2];

    if (dstArgb == NULL) {
        dstArgb = new uint8[width * height * 4];
    }

    libyuv::RotationMode rotationMode = libyuv::RotationMode::kRotate0;

    // TODO
    libyuv::NV21ToI420((uint8 *) yuvBytes, y_stride, (uint8 *) (yuvBytes + ySize),
                       y_stride,
                       i420, y_stride, i420 + ySize, u_stride,
                       i420 + ySize + ySize / 4, v_stride,
                       width, height);

    // rotate I420
    switch (orientation) {
        case 0:
            rotationMode = libyuv::RotationMode::kRotate0;
            break;
        case 90:
            hw[0] = height;
            hw[1] = width;
            rotationMode = libyuv::RotationMode::kRotate90;
            break;
        case 180:
            rotationMode = libyuv::RotationMode::kRotate180;
            break;
        case 270:
            hw[0] = height;
            hw[1] = width;
            rotationMode = libyuv::RotationMode::kRotate270;
            break;
        default:
            break;
    }

    dst_y_stride = hw[0];
    dst_u_stride = hw[0] >> 1;
    dst_v_stride = dst_u_stride;

    dst_ysize = (size_t) (hw[0] * hw[1]);

    libyuv::I420Rotate(i420, y_stride, i420 + ySize, u_stride, i420 + ySize + ySize / 4, v_stride,
                       dst_i420, dst_y_stride, dst_i420 + dst_ysize, dst_u_stride,
                       dst_i420 + dst_ysize + dst_ysize / 4, dst_v_stride, width, height,
                       rotationMode);

    libyuv::I420ToABGR(dst_i420, dst_y_stride, dst_i420 + dst_ysize, dst_u_stride,
                       dst_i420 + dst_ysize + dst_ysize / 4, dst_v_stride, dstArgb, hw[0] * 4,
                       hw[0], hw[1]);

    libyuv::ARGBToRGB24(dstArgb, hw[0] * 4, (uint8 *) rgb24Bytes, hw[0] * 3, hw[0], hw[1]);

    delete[] i420;
    delete[] dst_i420;

    env->ReleaseByteArrayElements(yuvBytes_, yuvBytes, 0);
    env->ReleaseByteArrayElements(rgb24Bytes_, rgb24Bytes, 0);
    env->ReleaseIntArrayElements(hw_, hw, 0);
    return 0;
}

extern "C"
JNIEXPORT jint JNICALL nativeNV21ToBGR24(JNIEnv *env, jclass type, jbyteArray yuvBytes_,
                                         jbyteArray rgb24Bytes_, jintArray hw_,
                                         jint orientation) {
    jbyte *yuvBytes = env->GetByteArrayElements(yuvBytes_, NULL);
    jbyte *rgb24Bytes = env->GetByteArrayElements(rgb24Bytes_, NULL);
    jint *hw = env->GetIntArrayElements(hw_, NULL);

    int width = hw[0];
    int height = hw[1];

    int y_stride = width;
    int u_stride = width >> 1;
    int v_stride = u_stride;

    int dst_y_stride = 0;
    int dst_u_stride = 0;
    int dst_v_stride = 0;

    size_t ySize = (size_t) (y_stride * height);
    size_t dst_ysize = 0;

    uint8 *i420 = new uint8[width * height * 3 / 2];
    uint8 *dst_i420 = new uint8[width * height * 3 / 2];

    libyuv::RotationMode rotationMode = libyuv::RotationMode::kRotate0;

    // TODO
    libyuv::NV21ToI420((uint8 *) yuvBytes, y_stride, (uint8 *) (yuvBytes + ySize),
                       y_stride,
                       i420, y_stride, i420 + ySize, u_stride,
                       i420 + ySize + ySize / 4, v_stride,
                       width, height);

    // rotate I420
    switch (orientation) {
        case 0:
            rotationMode = libyuv::RotationMode::kRotate0;
            break;
        case 90:
            hw[0] = height;
            hw[1] = width;
            rotationMode = libyuv::RotationMode::kRotate90;
            break;
        case 180:
            rotationMode = libyuv::RotationMode::kRotate180;
            break;
        case 270:
            hw[0] = height;
            hw[1] = width;
            rotationMode = libyuv::RotationMode::kRotate270;
            break;
        default:
            break;
    }

    dst_y_stride = hw[0];
    dst_u_stride = hw[0] >> 1;
    dst_v_stride = dst_u_stride;

    dst_ysize = (size_t) (hw[0] * hw[1]);

    libyuv::I420Rotate(i420, y_stride, i420 + ySize, u_stride, i420 + ySize + ySize / 4, v_stride,
                       dst_i420, dst_y_stride, dst_i420 + dst_ysize, dst_u_stride,
                       dst_i420 + dst_ysize + dst_ysize / 4, dst_v_stride, width, height,
                       rotationMode);

    libyuv::I420ToRGB24(dst_i420, dst_y_stride, dst_i420 + dst_ysize, dst_u_stride,
                        dst_i420 + dst_ysize + dst_ysize / 4, dst_v_stride,
                        (uint8 *) rgb24Bytes, hw[0] * 3, hw[0], hw[1]);

    delete[] i420;
    delete[] dst_i420;

    env->ReleaseByteArrayElements(yuvBytes_, yuvBytes, 0);
    env->ReleaseByteArrayElements(rgb24Bytes_, rgb24Bytes, 0);
    env->ReleaseIntArrayElements(hw_, hw, 0);

    return 0;
}

void unInit() {
    if (argb != NULL) {
        delete argb;
        argb = NULL;
    }

    if (dstArgb != NULL) {
        delete dstArgb;
        dstArgb = NULL;
    }
}

extern "C"
JNIEXPORT void nativeUnInit(JNIEnv *env, jclass type) {

    // TODO
    unInit();
}

static JNINativeMethod methods[]{
        {"nativeNV21ToRGB24", "([B[B[II)I", (void *) nativeNV21ToRGB24},
        {"nativeNV21ToBGR24", "([B[B[II)I", (void *) nativeNV21ToBGR24},
        {"nativeNV21ToNV12",  "([B[BII)I",  (void *) nativeNV21ToNV12},
        {"nativeNV21ToI420",  "([B[BII)I",  (void *) nativeNV21ToI420},
        {"nativeNV21ToYV12",  "([B[BII)I",  (void *) nativeNV21ToYV12},
        {"nativeUnInit",      "()V",        (void *) nativeUnInit}
};

jint registerNativeMethods(JNIEnv *env, const char *class_name, JNINativeMethod *methods,
                           int num_methods) {
    jclass clazz = env->FindClass(class_name);
    int result = env->RegisterNatives(clazz, methods, num_methods);
    return result;
}

int registerNativeYuv(JNIEnv *env) {
    const char *className = "com/xiaozhi/jni/ImageNdk";
    return registerNativeMethods(env, className, methods,
                                 ((int) sizeof(methods) / sizeof(methods[0])));
}

//当动态库被加载时这个函数被系统调用
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env;
    if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR;
    }
    // register native methods
    if (registerNativeYuv(env)) {
        return JNI_ERR;
    }
    // return jni version
    return JNI_VERSION_1_6;
}

//当动态库被卸载时这个函数被系统调用
JNIEXPORT void JNICALL JNI_OnUnload(JavaVM *vm, void *reserved) {
    unInit();
}


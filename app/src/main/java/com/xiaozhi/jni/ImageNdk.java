package com.xiaozhi.jni;

/**
 * Created by wangzhi on 2017/9/28.
 */

public class ImageNdk {

  static {
    System.loadLibrary("yuv-lib");
  }

  /**
   * A native method that is implemented by the 'native-lib' native library,
   * which is packaged with this application.
   */
  public static native int nativeNV21ToRGB24(byte[] nv21, byte[] rgb24Bytes, int[] hw, int orientation);

  public static native int nativeNV21ToBGR24(byte[] nv21, byte[] rgb24Bytes, int[] hw, int orientation);

  public static native int nativeNV21ToNV12(byte[] nv21, byte[] nv12, int width, int height);

  public static native int nativeNV21ToI420(byte[] nv21, byte[] i420, int width, int height);

  public static native int nativeNV21ToYV12(byte[] nv21, byte[] yv12, int width, int height);

  public static native void nativeUnInit();
}

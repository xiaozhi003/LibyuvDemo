# LibyuvDemo
Demo中使用libyuv库对camera返回的nv21数据进行rgb24的转换，目前测试中基本是速度最快的一种方式

# ImageNdk API
目前jni中提供如下接口供参考，当然libyuv不仅仅是这么几个接口，有更过需求的可以详细去研究
```java
 public static native int nativeNV21ToRGB24(byte[] nv21, byte[] rgb24Bytes, int[] hw, int orientation);

  public static native int nativeNV21ToBGR24(byte[] nv21, byte[] rgb24Bytes, int[] hw, int orientation);

  public static native int nativeNV21ToNV12(byte[] nv21, byte[] nv12, int width, int height);

  public static native int nativeNV21ToI420(byte[] nv21, byte[] i420, int width, int height);

  public static native int nativeNV21ToYV12(byte[] nv21, byte[] yv12, int width, int height);

  public static native void nativeUnInit();
```

# Libyuv官方地址
[https://chromium.googlesource.com/libyuv/libyuv/][1]


  [1]: https://chromium.googlesource.com/libyuv/libyuv/

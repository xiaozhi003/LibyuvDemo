package com.xiaozhi.libyuvdemo;

import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.xiaozhi.camera.CameraInterface;
import com.xiaozhi.jni.ImageNdk;
import com.xiaozhi.widget.CameraView;

public class CameraActivity extends AppCompatActivity {

  private static final String TAG = CameraActivity.class.getSimpleName();

  CameraView mCameraView;
  ImageView mImageView;
  TextView mHintTv;

  @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_camera);

    mCameraView = findViewById(R.id.cameraView);
    mImageView = findViewById(R.id.imageV);
    mHintTv = findViewById(R.id.hintTv);
    CameraInterface.getInstance().setCameraId(0);
    CameraInterface.getInstance().setDisplayOrientation(90);
    new Handler().postDelayed(() -> new YuvToRgbTask().start(), 2000);
  }

  @Override
  protected void onResume() {
    super.onResume();
    mCameraView.onResume();
  }

  @Override
  protected void onPause() {
    super.onPause();
    mCameraView.onPause();
  }

  byte[] rgbBytes = new byte[640 * 480 * 3];

  class YuvToRgbTask extends Thread {

    @Override
    public void run() {
      super.run();

      while (CameraInterface.getInstance().isPreviewing()) {
        byte[] yuvBytes = CameraInterface.getInstance().getCameraBytes();
        if (yuvBytes == null) {
          continue;
        }
        long start = System.currentTimeMillis();

        int[] hw = {640, 480};
        ImageNdk.nativeNV21ToRGB24(yuvBytes, rgbBytes, hw, 90);

        Log.d(TAG, "yuv to rgb时间：" + (System.currentTimeMillis() - start) + "ms" + " hw[0]:" + hw[0] + " hw[1]:" + hw[1]);

        Bitmap bitmap = rgb2Bitmap(rgbBytes, hw[0], hw[1]);

        long end = System.currentTimeMillis();
        final long spendTime = end - start;
        Log.i(TAG, "w：" + hw[0] + " h:" + hw[1]);
        Log.d(TAG, "yuv to bitmap时间：" + spendTime + "ms" + " rgb bytes size:" + rgbBytes.length);
        runOnUiThread(() -> {
          mImageView.setImageBitmap(bitmap);
          mHintTv.setText(spendTime + "ms");
        });
      }
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    ImageNdk.nativeUnInit();
  }

  /**
   * @方法描述 将RGB字节数组转换成Bitmap，
   */
  static public Bitmap rgb2Bitmap(byte[] data, int width, int height) {
    int[] colors = convertByteToColor(data);    //取RGB值转换为int数组
    if (colors == null) {
      return null;
    }

    Bitmap bmp = Bitmap.createBitmap(colors, 0, width, width, height,
        Bitmap.Config.ARGB_8888);
    return bmp;
  }

  // 将一个byte数转成int
  // 实现这个函数的目的是为了将byte数当成无符号的变量去转化成int
  public static int convertByteToInt(byte data) {
    int heightBit = (int) ((data >> 4) & 0x0F);
    int lowBit = (int) (0x0F & data);
    return heightBit * 16 + lowBit;
  }


  // 将纯RGB数据数组转化成int像素数组
  public static int[] convertByteToColor(byte[] data) {
    int size = data.length;
    if (size == 0) {
      return null;
    }

    int arg = 0;
    if (size % 3 != 0) {
      arg = 1;
    }

    // 一般RGB字节数组的长度应该是3的倍数，
    // 不排除有特殊情况，多余的RGB数据用黑色0XFF000000填充
    int[] color = new int[size / 3 + arg];
    int red, green, blue;
    int colorLen = color.length;
    if (arg == 0) {
      for (int i = 0; i < colorLen; ++i) {
        red = convertByteToInt(data[i * 3]);
        green = convertByteToInt(data[i * 3 + 1]);
        blue = convertByteToInt(data[i * 3 + 2]);

        // 获取RGB分量值通过按位或生成int的像素值
        color[i] = (red << 16) | (green << 8) | blue | 0xFF000000;
      }
    } else {
      for (int i = 0; i < colorLen - 1; ++i) {
        red = convertByteToInt(data[i * 3]);
        green = convertByteToInt(data[i * 3 + 1]);
        blue = convertByteToInt(data[i * 3 + 2]);
        color[i] = (red << 16) | (green << 8) | blue | 0xFF000000;
      }

      color[colorLen - 1] = 0xFF000000;
    }
    return color;
  }
}

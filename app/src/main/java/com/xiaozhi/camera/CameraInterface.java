package com.xiaozhi.camera;

import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.ShutterCallback;
import android.view.SurfaceHolder;

import com.xiaozhi.utils.Logs;

import java.io.IOException;
import java.util.List;

/**
 * Created by wangzhi on 2017/8/24.
 */
public class CameraInterface {
  private static final String TAG = CameraInterface.class.getSimpleName();
  private Camera mCamera;
  private Parameters mParams;
  private boolean isPreviewing = false;
  private float mPreviwRate = -1f;
  private int mDisplayOrientation = 0;
  private int mCameraId = 0;
  private static CameraInterface mCameraInterface;

  public interface CamOpenOverCallback {
    public void cameraHasOpened();

    public void cameraOpenError();
  }

  private CameraInterface() {

  }

  public Camera getCamera() {
    return mCamera;
  }

  public static synchronized CameraInterface getInstance() {
    if (mCameraInterface == null) {
      mCameraInterface = new CameraInterface();
    }
    return mCameraInterface;
  }

  /**
   * 设置旋转角度
   *
   * @param displayOrientation
   */
  public void setDisplayOrientation(int displayOrientation) {
    mDisplayOrientation = displayOrientation;
  }

  /**
   * 设置打开摄像头号（0：后置，1：前置）
   *
   * @param cameraId
   */
  public void setCameraId(int cameraId) {
    mCameraId = cameraId;
  }

  /**
   * 打开Camera
   *
   * @param callback
   */
  public void doOpenCamera(CamOpenOverCallback callback) {
    Logs.i(TAG, "Camera open....");
    if (mCamera == null) {
      mCamera = new GingerbreadOpenCameraInterface().open(mCameraId);
      if (mCamera == null) {
        if (callback != null) {
          callback.cameraOpenError();
        }
      } else {
        Logs.i(TAG, "Camera open over....");
        if (callback != null) {
          callback.cameraHasOpened();
        }
      }
    } else {
      Logs.i(TAG, "Camera open 异常!!!");
      doStopCamera();
    }
  }

  /**
   * 摄像头打开状态
   *
   * @return
   */
  public synchronized boolean isOpen() {
    return mCamera != null;
  }

  /**
   * 使用Surfaceview开启预览
   *
   * @param holder
   * @param previewRate
   */
  public void doStartPreview(SurfaceHolder holder, float previewRate) {
    Logs.i(TAG, "doStartPreview...");
    if (isPreviewing) {
      mCamera.stopPreview();
      return;
    }
    if (mCamera != null) {
      try {
        mCamera.setPreviewDisplay(holder);
        mCamera.setPreviewCallback(mPreviewCallback);
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      initCamera(previewRate);
    }
  }

  /**
   * 使用TextureView预览Camera
   *
   * @param surface
   * @param previewRate
   */
  public void doStartPreview(SurfaceTexture surface, float previewRate) {
    Logs.i(TAG, "doStartPreview...");
    if (isPreviewing) {
      mCamera.stopPreview();
      return;
    }
    if (mCamera != null) {
      try {
        mCamera.setPreviewTexture(surface);
        mCamera.setPreviewCallback(mPreviewCallback);
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      initCamera(previewRate);
    }

  }

  /**
   * 停止预览，释放Camera
   */
  public void doStopCamera() {
    if (null != mCamera) {
      Parameters parameters = mCamera.getParameters();
//      parameters.setFlashMode(Parameters.FLASH_MODE_OFF);
      mCamera.setPreviewCallback(null);
      mCamera.stopPreview();
      isPreviewing = false;
      mPreviwRate = -1f;
      mCamera.release();
      mCamera = null;
    }
  }

  /**
   * 是否正在预览
   *
   * @return
   */
  public boolean isPreviewing() {
    return isPreviewing;
  }

  private void initCamera(float previewRate) {
    if (mCamera != null) {

      mParams = mCamera.getParameters();
      mParams.setPictureFormat(PixelFormat.JPEG);// 设置拍照后存储的图片格式
      // CamParaUtil.getInstance().printSupportPictureSize(mParams);
      // CamParaUtil.getInstance().printSupportPreviewSize(mParams);
      // 设置PreviewSize和PictureSize
      mParams.setPreviewSize(640, 480);
      mParams.setPreviewFormat(ImageFormat.NV21);

      mCamera.setDisplayOrientation(mDisplayOrientation);

      // CamParaUtil.getInstance().printSupportFocusMode(mParams);
      List<String> focusModes = mParams.getSupportedFocusModes();
      if (focusModes.contains("continuous-video")) {
        mParams.setFocusMode(Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
      }
//      mParams.setFlashMode(Parameters.FLASH_MODE_TORCH);
      mCamera.setParameters(mParams);
      mCamera.startPreview();// 开启预览

      isPreviewing = true;
      mPreviwRate = previewRate;

      mParams = mCamera.getParameters(); // 重新get一次
      Logs.i(TAG,
          "最终设置:PreviewSize--With = "
              + mParams.getPreviewSize().width + "Height = "
              + mParams.getPreviewSize().height);
      Logs.i(TAG,
          "最终设置:PictureSize--With = "
              + mParams.getPictureSize().width + "Height = "
              + mParams.getPictureSize().height);
    }
  }

  /* 为了实现拍照的快门声音及拍照保存照片需要下面三个回调变量 */
  ShutterCallback mShutterCallback = new ShutterCallback()
      // 快门按下的回调，在这里我们可以设置类似播放“咔嚓”声之类的操作。默认的就是咔嚓。
  {
    public void onShutter() {
      // TODO Auto-generated method stub
      Logs.i(TAG, "myShutterCallback:onShutter...");
    }
  };
  PictureCallback mRawCallback = new PictureCallback()
      // 拍摄的未压缩原数据的回调,可以为null
  {

    public void onPictureTaken(byte[] data, Camera camera) {
      // TODO Auto-generated method stub
      Logs.i(TAG, "myRawCallback:onPictureTaken...");

    }
  };

  PreviewCallback mPreviewCallback = new PreviewCallback() {

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
      Parameters parameters = camera.getParameters();
      Camera.Size previewSize = parameters.getPreviewSize();
//      Logs.i(TAG, "帧率：" + Utils.fps() + " w:" + previewSize.width + " h:" + previewSize.height);
      mCameraBytes = data;
    }
  };

  private byte[] mCameraBytes = null;

  /**
   * 获取一帧图像数据（yuv格式）
   *
   * @return
   */
  public byte[] getCameraBytes() {
    return mCameraBytes;
  }
}

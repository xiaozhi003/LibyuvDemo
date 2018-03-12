package com.xiaozhi.widget;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import com.xiaozhi.camera.CameraInterface;
import com.xiaozhi.utils.Logs;

/**
 * Created by wangzhi on 2017/9/28.
 */

public class CameraView extends SurfaceView implements SurfaceHolder.Callback, CameraInterface.CamOpenOverCallback {

  private static final String TAG = CameraView.class.getSimpleName();

  private Context mContext;
  SurfaceHolder mSurfaceHolder;
  private Handler mHandler;

  private boolean hasSurface; // 是否存在摄像头显示层

  public CameraView(Context context) {
    super(context);
    init(context);
  }

  public CameraView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context);
  }

  public CameraView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context);
  }

  private void init(Context context) {
    mContext = context;
    mHandler = new Handler(context.getMainLooper());
    mSurfaceHolder = getHolder();
    mSurfaceHolder.setFormat(PixelFormat.TRANSPARENT);//translucent半透明 transparent透明
    mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    mSurfaceHolder.addCallback(this);
  }

  @Override
  public void surfaceCreated(SurfaceHolder holder) {
    Logs.i(TAG, "surfaceCreated..." + hasSurface);
    if (!hasSurface && holder != null) {
      hasSurface = true;
      initCamera(holder);
    }
  }

  @Override
  public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

  }

  @Override
  public void surfaceDestroyed(SurfaceHolder holder) {

  }

  public SurfaceHolder getSurfaceHolder() {
    return mSurfaceHolder;
  }

  @Override
  public void cameraHasOpened() {
    CameraInterface.getInstance().doStartPreview(getSurfaceHolder(), 1);
  }

  @Override
  public void cameraOpenError() {
    mHandler.post(new Runnable() {
      @Override
      public void run() {
        Toast.makeText(mContext, "摄像头打开失败", Toast.LENGTH_SHORT).show();
      }
    });
  }

  public void onResume() {
    if (hasSurface) {
      // 当activity暂停，但是并未停止的时候，surface仍然存在，所以 surfaceCreated()
      // 并不会调用，需要在此处初始化摄像头
      initCamera(mSurfaceHolder);
    } else {
      // 设置回调，等待 surfaceCreated() 初始化摄像头
      mSurfaceHolder.addCallback(this);
      mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }
  }

  public void onPause() {
    CameraInterface.getInstance().doStopCamera();
    Logs.i(TAG, "onPause..." + hasSurface);
    if (!hasSurface) {
      mSurfaceHolder.removeCallback(this);
    }
  }

  /**
   * 初始化摄像头，较为关键的内容
   *
   * @param surfaceHolder
   */
  private void initCamera(SurfaceHolder surfaceHolder) {
    if (surfaceHolder == null) {
      throw new IllegalStateException("SurfaceHolder is null");
    }
    if (CameraInterface.getInstance().isOpen()) {
      Logs.w(TAG, "Camera is opened！");
      return;
    }
    new Thread(() -> CameraInterface.getInstance().doOpenCamera(CameraView.this)).start();
  }


}

package com.xiaozhi.camera;

import android.annotation.SuppressLint;
import android.hardware.Camera;

import com.xiaozhi.utils.Logs;

@SuppressLint("NewApi")
public final class GingerbreadOpenCameraInterface implements OpenCameraInterface {

	private String TAG = "GingerbreadOpenCamera";

	public static int sCameraId = 0;

	@Override
	public Camera open(int indexid) {
		int numCameras = Camera.getNumberOfCameras();
		Logs.d(TAG, "摄像头数目:" + numCameras);
		if (numCameras == 0) {
			Logs.e(TAG, "No cameras!");
			return null;
		}
		Camera camera = null;
		if (numCameras == 1) {
			Logs.d(TAG, "only one camera#0");
			Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
			Camera.getCameraInfo(0, cameraInfo);
			camera = Camera.open(0);
			return camera;
		}

		if (indexid < numCameras) {
			Logs.d(TAG, "Opening camera #" + indexid);
			if (indexid == 1) {// 前置
				Logs.d(TAG, "打开前置摄像头");
				Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
				Camera.getCameraInfo(1, cameraInfo);
				camera = Camera.open(1);
				sCameraId = 1;
			} else if (indexid == 0) {// 后置
				Logs.d(TAG, "打开后置摄像头");
				Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
				Camera.getCameraInfo(0, cameraInfo);
				camera = Camera.open(0);
				sCameraId = 0;
			} else if(indexid == 2) {
				Logs.d(TAG, "打开虹膜摄像头");
				Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
				Camera.getCameraInfo(2, cameraInfo);
				camera = Camera.open(2);
				sCameraId = 2;
			}
		} else {
			Logs.e(TAG, "No camera facing back; returning camera #0");
			Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
			Camera.getCameraInfo(0, cameraInfo);
			camera = Camera.open(0);
			sCameraId = 0;
		}
		return camera;
	}
}

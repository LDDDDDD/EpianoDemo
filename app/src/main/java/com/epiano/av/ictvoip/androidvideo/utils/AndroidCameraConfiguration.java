package com.epiano.av.ictvoip.androidvideo.utils;

import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Size;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class AndroidCameraConfiguration {

	private static AndroidCamera[] camerasCache;
	public static String TAG = "VIDEO_SYSTEM";

	public static int getCamerasCount() {
		initCamerasCache();
		return camerasCache.length;
	}

	public static AndroidCamera[] retrieveCameras() {
		initCamerasCache();
		return camerasCache;
	}

	public static boolean hasSeveralCameras() {
		initCamerasCache();
		return camerasCache.length > 1;
	}

	public static boolean hasFrontCamera() {
		initCamerasCache();
		for (AndroidCamera cam : camerasCache) {
			if (cam.frontFacing)
				return true;
		}
		return false;
	}

	private static void initCamerasCache() {
		// cache already filled ?
		if (camerasCache != null)
			return;

		try {
			camerasCache = AndroidCameraConfiguration.probeCamerasSDK();
		} catch (Exception exc) {
			Log.e(TAG,
					"AndroidCameraConfiguration	Error: cannot retrieve cameras information (busy ?)",
					exc);
			exc.printStackTrace();
			camerasCache = new AndroidCamera[0];
		}
	}

	static AndroidCamera[] probeCamerasSDK() {
		return probeCameras();
	}

	static public AndroidCamera[] probeCameras() {
		List<AndroidCamera> cam = new ArrayList<AndroidCamera>(
				Camera.getNumberOfCameras());

		for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
			CameraInfo info = new CameraInfo();
			Camera.getCameraInfo(i, info);
			Camera c = Camera.open(i);
			cam.add(new AndroidCamera(i,
					info.facing == CameraInfo.CAMERA_FACING_FRONT,
					info.orientation, c.getParameters()
							.getSupportedPreviewSizes()));
			c.release();
		}

		AndroidCamera[] result = new AndroidCamera[cam.size()];
		result = cam.toArray(result);

		return result;
	}

	/**
	 * Default: no front; rear=0; default=rear
	 * 
	 * @author Guillaume Beraudo
	 * 
	 */
	static public class AndroidCamera {
		public AndroidCamera(int i, boolean f, int o, List<Size> r) {
			this.id = i;
			this.frontFacing = f;
			this.orientation = o;
			this.resolutions = r;
		}

		public int id;
		public boolean frontFacing; // false => rear facing
		public int orientation;
		public List<Size> resolutions;
	}

}

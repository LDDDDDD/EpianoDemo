package com.epiano.uvc;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

// JNI
@SuppressWarnings("ALL")
public class UvcWebCam{

	private static final String LOG_TAG = "USBCAM";
	private Context mycontext;

	public UvcWebCam(Context context) {

		Log.e(LOG_TAG, "Loading libuvcwebcam.so...");
		System.loadLibrary("uvcwebcam");
		Log.e(LOG_TAG, "Loaded libuvcwebcam.so.");

		mycontext = context;
	}

	public UvcWebCam() {

		Log.e(LOG_TAG, "Loading libuvcwebcam.so...");
		System.loadLibrary("uvcwebcam");
		Log.e(LOG_TAG, "Loaded libuvcwebcam.so.");

		//mycontext = context;
	}

	// API Native

	public native int open(byte[] devname);

	public native int init(int width, int height, int numbuf, int ctype);

	public native int streamon();

	public native int pixeltobmp(Bitmap bmp);

	public native int yuvtorgb(byte[] yuvdata, int[] rgbdata);

	public native int dqbuf(byte[] videodata);

	public native int qbuf(int index);

	public native int streamoff(int index);

	public native int release();

	// 一次性打开摄像头，取一图后，立刻关闭摄像头
	// 不好用, 卡, 崩溃, 暂时不用
	public native int GetOneFrame(byte[] devname,
								  int width, int height, int numbuf, int ctype,
								  Bitmap bmp
	);
}
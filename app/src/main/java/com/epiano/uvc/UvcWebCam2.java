package com.epiano.uvc;
 
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

// JNI 
public class UvcWebCam2{
	
	private static final String LOG_TAG = "USBCAM";
	private Context mycontext;
	
	public UvcWebCam2(Context context) {

		Log.e(LOG_TAG, "Loading libuvcwebcam2.so...");
		System.loadLibrary("uvcwebcam2");		
		Log.e(LOG_TAG, "Loaded libuvcwebcam2.so.");

		mycontext = context;
	}
	
	public UvcWebCam2() {

		Log.e(LOG_TAG, "Loading libuvcwebcam2.so...");
		System.loadLibrary("uvcwebcam2");		
		Log.e(LOG_TAG, "Loaded libuvcwebcam2.so.");

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
 
}
package com.epiano.av.ictvoip.androidvideo.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Iterator;
import java.util.List;

@SuppressWarnings("JniMissingFunction")
public class AndroidVideoApiJniWrapper {

	public static boolean isRecording = false;
	public static String TAG = "VIDEO_SYSTEM";


	public static native void putImage(long nativePtr, byte[] buffer);
	
	
	static public int detectCameras(int[] indexes, int[] frontFacing,
			int[] orientation) {
		Log.d(TAG, "detectCameras\n");
		AndroidCameraConfiguration.AndroidCamera[] cameras = AndroidCameraConfiguration.retrieveCameras();

		int nextIndex = 0;
		for (AndroidCameraConfiguration.AndroidCamera androidCamera : cameras) {
			if (nextIndex == 2) {
				Log.w(TAG,
						"detectCameras	Returning only the 2 first cameras (increase buffer size to retrieve all)");
				break;
			}
			// skip already added cameras
			indexes[nextIndex] = androidCamera.id;
			frontFacing[nextIndex] = androidCamera.frontFacing ? 1 : 0;
			orientation[nextIndex] = androidCamera.orientation;
			nextIndex++;
		}
		return nextIndex;
	} 

	/** 
	 * Return the hw-available available resolution best matching the requested
	 * one. Best matching meaning : - try to find the same one - try to find one
	 * just a little bigger (ex: CIF when asked QVGA) - as a fallback the
	 * nearest smaller one
	 * 
	 * @param requestedW
	 *            Requested video size width
	 * @param requestedH
	 *            Requested video size height
	 * @return int[width, height] of the chosen resolution, may be null if no
	 *         resolution can possibly match the requested one
	 */
	static public int[] selectNearestResolutionAvailable(int cameraId,
			int requestedW, int requestedH) {
		Log.d(TAG, "selectNearestResolutionAvailable: " + cameraId + ", "
				+ requestedW + "x" + requestedH);
		return selectNearestResolutionAvailableForCamera(cameraId, requestedW,
				requestedH);
	}

	static public void activateAutoFocus(Object cam) {
		Log.d(TAG, "activateAutoFocus	Turning on autofocus on camera " + cam);
		Camera camera = (Camera) cam;
		if (camera != null
				&& (camera.getParameters().getFocusMode() == Parameters.FOCUS_MODE_AUTO || camera
						.getParameters().getFocusMode() == Parameters.FOCUS_MODE_MACRO))
			camera.autoFocus(null); // We don't need to do anything after the
									// focus finished, so we don't need a
									// callback
	}
 
	public static Object startRecording(int cameraId, int width, int height,
			int fps, int rotation, final long nativePtr) {

		Log.i(TAG, "startRecording(" + cameraId + ", " + width + ", " + height
				+ ", " + fps + ", " + rotation + ", " + nativePtr + ")");

		try {
			//cameraId = 0; // test
			
			Camera camera = Camera.open(cameraId);		

			Parameters params = camera.getParameters();

			params.setPreviewSize(width, height);
			int[] chosenFps = findClosestEnclosingFpsRange(fps * 1000,
					params.getSupportedPreviewFpsRange());
			params.setPreviewFpsRange(chosenFps[0], chosenFps[1]);
			
			
			//params.setPreviewFpsRange(5, 15);
			
			
			camera.setParameters(params);
			
			
			Log.i(TAG, "camera.setParameters(params)" );

			int bufferSize = (width * height * ImageFormat
					.getBitsPerPixel(params.getPreviewFormat())) / 8;
			camera.addCallbackBuffer(new byte[bufferSize]);
			camera.addCallbackBuffer(new byte[bufferSize]);
			
//			Camera.PictureCallback(new Camera.PictureCallback() {
//				
//				@Override
//				public void onPictureTaken(byte[] arg0, Camera arg1) {
//					// TODO Auto-generated method stub
//					
//				}
//			});
			
			camera.setPreviewCallbackWithBuffer(new Camera.PreviewCallback() {
				int frcount = 0;

				public void onPreviewFrame(byte[] data, Camera camera) {
					// forward image data to JNI

					//Log.i(TAG, "onPreviewFrame" );

					if (data == null) {
						// It appears that there is a bug in the camera driver
						// that is asking for a buffer size bigger than it
						// should
						Parameters params = camera.getParameters();
						Size size = params.getPreviewSize();
						int bufferSize = (size.width * size.height * ImageFormat
								.getBitsPerPixel(params.getPreviewFormat())) / 8;
						bufferSize += bufferSize / 20;
						camera.addCallbackBuffer(new byte[bufferSize]);
					} else if (isRecording) {

						//if(frcount % 10 == 0)
						//	Log.i(TAG, "Get a frame data,  count:" + (frcount++));
									

						//MainGUI.mWindows.requestRender();
				
						//decToBitMap(data, camera);
						
						// xxxx
						//putImage(nativePtr, data);
						camera.addCallbackBuffer(data);
					}
				}
			});

			setCameraDisplayOrientation(rotation, cameraId, camera);
			camera.startPreview();
			
			Log.d(TAG, "startPreview " );
			
			isRecording = true;
			Log.d(TAG, "Returning camera object: " + camera);
			return camera;
		} catch (Exception exc) {
			exc.printStackTrace();
			return null;
		}



	}

	private static void setCameraDisplayOrientation(int rotationDegrees,
			int cameraId, Camera camera) {
		Camera.CameraInfo info = new Camera.CameraInfo();
		Camera.getCameraInfo(cameraId, info);

		int result;
		if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
			result = (info.orientation + rotationDegrees) % 360;
			result = (360 - result) % 360; // compensate the mirror
		} else { // back-facing
			result = (info.orientation - rotationDegrees + 360) % 360;
		}

		Log.w(TAG, "Camera preview orientation: " + result);
		try {
			camera.setDisplayOrientation(result);
		} catch (Exception exc) {
			Log.e(TAG, "Failed to execute: camera[" + camera
					+ "].setDisplayOrientation(" + result + ")");
			exc.printStackTrace();
		}
	}

	
	public static void decToBitMap(byte[] data, Camera mCamera) {
		Size size = mCamera.getParameters().getPreviewSize();
		try {
			YuvImage image = new YuvImage(data, ImageFormat.NV21, size.width,
					size.height, null);
			Log.i(TAG, size.width + " " + size.height);
			if (image != null) {

 

				ByteArrayOutputStream stream = new ByteArrayOutputStream();

				image.compressToJpeg(new Rect(0, 0, size.width, size.height),
						80, stream);

				Bitmap bmp = BitmapFactory.decodeByteArray(
						stream.toByteArray(), 0, stream.size());
				
				Log.i(TAG, bmp.getWidth() + " " + bmp.getHeight());

//				
//				
//				Canvas canvas = ((SurfaceHolder) MainGUI.mRenderSurfaceView).lockCanvas(null);
//				canvas.drawBitmap(bmp, 0, 0, null);
//				((SurfaceHolder) MainGUI.mRenderSurfaceView).unlockCanvasAndPost(canvas);			
//				
//				
				
				stream.close();




			}
		} catch (Exception ex) {
			Log.e(TAG, "Error:" + ex.getMessage());
		}
	}
	
	
	
	
	
	public static void decodeToBitMap(byte[] data, Camera mCamera, int frcount) {
		Size size = mCamera.getParameters().getPreviewSize();
		try {
			YuvImage image = new YuvImage(data, ImageFormat.NV21, size.width,
					size.height, null);
			Log.i(TAG, size.width + " " + size.height);
			if (image != null) {
				File file = new File("mnt/sdcard/videotest/");
				if (!file.exists()) {
					file.mkdir();
				}

				FileOutputStream out = null;
				try {
					out = new FileOutputStream("mnt/sdcard/videotest/"
							+ frcount + ".png");
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Log.e(TAG, "FileNotFoundException", e);
				}

				ByteArrayOutputStream stream = new ByteArrayOutputStream();

				image.compressToJpeg(new Rect(0, 0, size.width, size.height),
						80, stream);

				Bitmap bmp = BitmapFactory.decodeByteArray(
						stream.toByteArray(), 0, stream.size());
				
				Log.i(TAG, bmp.getWidth() + " " + bmp.getHeight());
				Log.w(TAG,
						(bmp.getPixel(100, 100) & 0xff) + "  "
								+ ((bmp.getPixel(100, 100) >> 8) & 0xff) + "  "
								+ ((bmp.getPixel(100, 100) >> 16) & 0xff));
				
				
				
				bmp.compress(Bitmap.CompressFormat.PNG, 100, out);

				stream.close();
				out.flush();
				out.close();



			}
		} catch (Exception ex) {
			Log.e(TAG, "Error:" + ex.getMessage());
		}
	}

	public static void stopRecording(Object cam) {
		isRecording = false;
		Log.d(TAG, "stopRecording(" + cam + ")");
		Camera camera = (Camera) cam;

		if (camera != null) {
			camera.setPreviewCallback(null);
			camera.stopPreview();
			camera.release();
		} else {
			Log.i(TAG, "stopRecording	Cannot stop recording ('camera' is null)");
		}
	}
	
	
	
	
	

	public static void setPreviewDisplaySurface(Object cam, Object surf) {
		Log.d(TAG, "setPreviewDisplaySurface(" + cam + ", " + surf + ")");
		Camera camera = (Camera) cam;
		SurfaceView surface = (SurfaceView) surf;
		try {

			SurfaceHolder mySurfaceHolder = null;
			mySurfaceHolder = surface.getHolder();
			mySurfaceHolder.setFormat(PixelFormat.TRANSLUCENT);// translucent闁告锕敓浠嬪及閿熺但ransparent闂侇偄绻戝Σ锟�?
			// mySurfaceHolder.addCallback(this);
			mySurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

			camera.setPreviewDisplay(mySurfaceHolder);
		} catch (Exception exc) {
			exc.printStackTrace();
		}

	}

	protected static void applyCameraParameters(Camera camera, int width,
			int height, int requestedFps) {
		Parameters params = camera.getParameters();

		params.setPreviewSize(width, height);

		List<int[]> supported = params.getSupportedPreviewFpsRange();
		if (supported != null) {
			for (int[] curRange : supported) {
				Log.d(TAG, "fpsRanges: w=" + curRange[0] + ",h=" + curRange[1]
						+ ", size:" + supported.size());
			}
		}

		Log.d(TAG, "range:" + supported.size());
		for (int j = 0; j < supported.size(); j++) {
			int[] r = supported.get(j);
			for (int k = 0; k < r.length; k++) {
				Log.d(TAG, "supported FPS:" + r[k]);
			}
		}

		if (supported != null) {
			int fps[] = findClosestEnclosingFpsRange(requestedFps, supported);

			int trfps = requestedFps * 1000;
			Log.d(TAG, "fpsRanges: fps[0]=" + fps[0] + ",fps[1]=" + fps[1]
					+ ", trfps:" + trfps);

			if ((trfps <= fps[1]) && (trfps >= fps[0])) {
				params.setPreviewFpsRange(trfps, trfps);

			} else {
				Log.e(TAG, "requestedFps error");

			}

			int[] tfps = new int[2];
			params.getPreviewFpsRange(tfps);
			Log.d(TAG, "Preview framerate set:" + tfps[0] + "," + tfps[1]);

		}

		camera.setParameters(params);

		// Log.i(TAG, "applyCameraParameters:   requestedFps: " + requestedFps);
		//
		// //params.setPreviewFrameRate(requestedFps);
		//
		// List<Integer> supported = params.getSupportedPreviewFrameRates();
		// if (supported != null) {
		// int nearest = Integer.MAX_VALUE;
		//
		// for (Integer fr : supported) {
		//
		// Log.i(TAG, "applyCameraParameters:   PreviewFrameRates  fr:"
		// + fr.intValue());
		//
		// int diff = Math.abs(fr.intValue() - requestedFps);
		// if (diff < nearest) {
		// nearest = diff;
		// params.setPreviewFrameRate(fr.intValue());
		// }
		// }
		// Log.i(TAG,
		// "applyCameraParameters:	Preview framerate set:"
		// + params.getPreviewFrameRate());
		// }
		//
		// camera.setParameters(params);

	}
	
	
	protected static int[] selectNearestResolutionAvailableForCamera(int id,
			int requestedW, int requestedH) {
		// inversing resolution since webcams only support landscape ones
		if (requestedH > requestedW) {
			int t = requestedH;
			requestedH = requestedW;
			requestedW = t;
		}

		AndroidCameraConfiguration.AndroidCamera[] cameras = AndroidCameraConfiguration.retrieveCameras();
		List<Size> supportedSizes = null;
		for (AndroidCameraConfiguration.AndroidCamera c : cameras) {
			//if (c.id == id)
				//supportedSizes = c.resolutions;
			Iterator<Size> it = c.resolutions.iterator();
			while (it.hasNext()) {
				//System.out.println(it.next());
				Size s = it.next(); 
				if (s.width == requestedW
						&& s.height == requestedH)
				{
					supportedSizes = c.resolutions;
					break;
				}
			}			
		}
		
		if (supportedSizes == null) {
			Log.e(TAG, "Failed to retrieve supported resolutions.");
			return null;
		}
		
		Log.d(TAG, supportedSizes.size() + " supported resolutions :");
		for (Size s : supportedSizes) {
			Log.d(TAG, "\t" + s.width + "x" + s.height);
		}
		int r[] = null;

		int rW = Math.max(requestedW, requestedH);
		int rH = Math.min(requestedW, requestedH);

		try {
			// look for nearest size
			Size result = null;
			int req = rW * rH;
			int minDist = Integer.MAX_VALUE;
			int useDownscale = 0;
			for (Size s : supportedSizes) {
				int dist = Math.abs(req - s.width * s.height);
				if (dist < minDist) {
					minDist = dist;
					result = s;
					useDownscale = 0;
				}

				/* MS2 has a NEON downscaler, so we test this too */
				int downScaleDist = Math.abs(req - s.width * s.height / 4);
				if (downScaleDist < minDist) {
					minDist = downScaleDist;
					result = s;
					useDownscale = 1;
				}
				if (s.width == rW && s.height == rH) {
					result = s;
					useDownscale = 0;
					break;
				}
			}
			r = new int[] { result.width, result.height, useDownscale };
			Log.d(TAG, "resolution selection done (" + r[0] + ", " + r[1]
					+ ", " + r[2] + ")");
			return r;
		} catch (Exception exc) {
			Log.e(TAG, "resolution selection failed");
			exc.printStackTrace();
			return null;
		}
	}

	private static int[] findClosestEnclosingFpsRange(int expectedFps,
			List<int[]> fpsRanges) {
		Log.d(TAG, "Searching for closest fps range from " + expectedFps);
		// init with first element
		int[] closestRange = fpsRanges.get(0);
		int measure = Math.abs(closestRange[0] - expectedFps)
				+ Math.abs(closestRange[1] - expectedFps);
		for (int[] curRange : fpsRanges) {

			if (curRange[0] > expectedFps || curRange[1] < expectedFps)
				continue;
			int curMeasure = Math.abs(curRange[0] - expectedFps)
					+ Math.abs(curRange[1] - expectedFps);
			if (curMeasure < measure) {
				closestRange = curRange;
				measure = curMeasure;
				Log.d(TAG, "a better range has been found: w="
						+ closestRange[0] + ",h=" + closestRange[1]);
			}
		}
		Log.d(TAG, "The closest fps range is w=" + closestRange[0] + ",h="
				+ closestRange[1]);
		return closestRange;
	}

}

package com.epiano.av.ictvoip.androidvideo.display;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.Surface;
import android.view.Surface.OutOfResourcesException;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class AndroidVideoWindowImpl {
	private SurfaceView mVideoRenderingView;
	private SurfaceView mVideoPreviewView;

	private boolean useGLrendering;
	private Bitmap mBitmap;

	private Surface mSurface;
	private VideoWindowListener mListener;
	private Renderer renderer;

	private static String TAG = "VIDEO";

//	public void test() {
//
//		Log.i(TAG, "test");
//	}

	/**
	 * Utility listener interface providing callback for Android events useful
	 * to Mediastreamer.
	 */
	public static interface VideoWindowListener {
		void onVideoRenderingSurfaceReady(AndroidVideoWindowImpl vw,
										  SurfaceView surface);

		void onVideoRenderingSurfaceDestroyed(AndroidVideoWindowImpl vw);

		void onVideoPreviewSurfaceReady(AndroidVideoWindowImpl vw,
										SurfaceView surface);

		void onVideoPreviewSurfaceDestroyed(AndroidVideoWindowImpl vw);
	};

	/**
	 * @param renderingSurface
	 *            Surface created by the application that will be used to render
	 *            decoded video stream
	 * @param previewSurface
	 *            Surface created by the application used by Android's Camera
	 *            preview framework
	 */
	public AndroidVideoWindowImpl(SurfaceView renderingSurface,
			SurfaceView previewSurface) {
		mVideoRenderingView = renderingSurface;
		mVideoPreviewView = previewSurface;

		useGLrendering = (renderingSurface instanceof GLSurfaceView);

		mBitmap = null;
		mSurface = null;
		mListener = null;
	}

	public void init() {
		// register callback for rendering surface events
		mVideoRenderingView.getHolder().addCallback(new Callback() {
			public void surfaceChanged(SurfaceHolder holder, int format,
					int width, int height) {
				Log.i(TAG, "Video display surface is being changed.");
				if (!useGLrendering) {
					synchronized (AndroidVideoWindowImpl.this) {
						mBitmap = Bitmap.createBitmap(width, height,
								Config.RGB_565);
						mSurface = holder.getSurface();
					}
				}
				if (mListener != null)
					mListener.onVideoRenderingSurfaceReady(
							AndroidVideoWindowImpl.this, mVideoRenderingView);
				Log.w(TAG, "Video display surface changed");
			}

			public void surfaceCreated(SurfaceHolder holder) {
				Log.w(TAG, "Video display surface created");
			}

			public void surfaceDestroyed(SurfaceHolder holder) {
				if (!useGLrendering) {
					synchronized (AndroidVideoWindowImpl.this) {
						mSurface = null;
						mBitmap = null;
					}
				}
				if (mListener != null)
				{
					mListener.onVideoRenderingSurfaceDestroyed(AndroidVideoWindowImpl.this);
				}
				Log.d(TAG, "Video display surface destroyed");
			}
		});
		
		// register callback for preview surface events
		if (mVideoPreviewView != null) {
			//if (false)	// wg mod
			{
				mVideoPreviewView.getHolder().addCallback(new Callback() {
					public void surfaceChanged(SurfaceHolder holder, int format,
							int width, int height) {
						Log.i(TAG, "Video preview surface is being changed.");
						if (mListener != null)
							mListener.onVideoPreviewSurfaceReady(
									AndroidVideoWindowImpl.this, mVideoPreviewView);
						Log.w(TAG, "Video preview surface changed");
					}

					public void surfaceCreated(SurfaceHolder holder) {
						Log.w(TAG, "Video preview surface created");
					}

					public void surfaceDestroyed(SurfaceHolder holder) {
						if (mListener != null)
							mListener.onVideoPreviewSurfaceDestroyed(AndroidVideoWindowImpl.this);
						Log.d(TAG, "Video preview surface destroyed");
					}
				});
			}
		}

		if (useGLrendering) {
			renderer = new Renderer();
			((GLSurfaceView) mVideoRenderingView).setRenderer(renderer);
			((GLSurfaceView) mVideoRenderingView).setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
		}
	}

	public void release() {
		// mSensorMgr.unregisterListener(this);
	}

	public void setListener(VideoWindowListener l) {
		mListener = l;
	}

	public Surface getSurface() {
		if (useGLrendering)
			Log.e(TAG, "View class does not match Video display filter used (you must use a non-GL View)");
		return mVideoRenderingView.getHolder().getSurface();
	}

	public Bitmap getBitmap() {
		if (useGLrendering)
			Log.e(TAG, "View class does not match Video display filter used (you must use a non-GL View)");
		return mBitmap;
	}

	public void setOpenGLESDisplay(int ptr) {
		if (!useGLrendering)
			Log.e(TAG, "View class does not match Video display filter used (you must use a GL View)");
		renderer.setOpenGLESDisplay(ptr);
	}

	public void requestRender() {
		((GLSurfaceView) mVideoRenderingView).requestRender();
	}

	// Called by the mediastreamer2 android display filter
	public synchronized void update() {
		if (mSurface != null) {
			try {
				Canvas canvas = mSurface.lockCanvas(null);
				canvas.drawBitmap(mBitmap, 0, 0, null);
				mSurface.unlockCanvasAndPost(canvas);

			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (OutOfResourcesException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private static class Renderer implements GLSurfaceView.Renderer {
		int ptr;
		boolean initPending;
		int width, height;
		int flag = 0;
		int counter = 0;

		long timestart = 0, timecurr = 0;

		// wg mod
		//MainGUI appMain = new MainGUI();
		// appMain = new AudioVideo();

		public Renderer() {
			ptr = 0;
			initPending = false;
		}

		public void setOpenGLESDisplay(int ptr) {
			/*
			 * Synchronize this with onDrawFrame: - they are called from
			 * different threads (Rendering thread and Linphone's one) -
			 * setOpenGLESDisplay can modify ptr while onDrawFrame is using it
			 */
			synchronized (this) {
				if (this.ptr != 0 && ptr != this.ptr) {
					initPending = true;
				}
				this.ptr = ptr;
			}
		}

		public void onDrawFrame(GL10 gl) {
			/*
			 * See comment in setOpenGLESDisplay
			 */

			Log.e(TAG, "ogl onDrawFrame(), counter:" + counter++); // + appMain.getVideoDisplayFlag());

			synchronized (this) {
				if (ptr == 0)
					return;
				if (initPending) {
					OpenGLESDisplay.init(ptr, width, height);
					initPending = false;

					gl.glClearColor(0, 0, 0, 1);
					gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
				}

				/*
				if (appMain.getVideoDisplayFlag() == 0) {
					
					Log.e(TAG, "onDrawFrame() called." + appMain.getVideoDisplayFlag());
					
					flag = 0;
					if (timestart == 0) {
						timestart = System.currentTimeMillis();
						timecurr = timestart;
					}	
				}
				
				
				if (flag == 0){	
					timecurr = System.currentTimeMillis();
					if(timecurr - timestart < 5000){				
						flag = 0;					
					}else{
						timestart = 0;					
						flag = appMain.getVideoDisplayFlag();
					}
			
					gl.glClearColor(0, 0, 0, 1);
					gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
				} else {
					OpenGLESDisplay.render(ptr);
				}
				*/

				OpenGLESDisplay.render(ptr);
			}
		}

		public void onSurfaceChanged(GL10 gl, int width, int height) {
			/* delay init until ptr is set */
			this.width = width;
			this.height = height;
			initPending = true;
		}

		public void onSurfaceCreated(GL10 gl, EGLConfig config) {

		}
	}

	public static int rotationToAngle(int r) {
		switch (r) {
		case Surface.ROTATION_0:
			return 0;
		case Surface.ROTATION_90:
			return 90;
		case Surface.ROTATION_180:
			return 180;
		case Surface.ROTATION_270:
			return 270;
		}
		return 0;
	}
}

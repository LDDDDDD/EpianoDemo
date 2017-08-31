package com.epiano.eLearning;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Button;
import android.widget.ImageView;

import java.io.IOException;
import java.util.List;


public class NativeCam
{
	//private SurfaceView mImag;
	public int width = 0; //WIDTH;
	public int height = 0; //HEIGHT;

	Size PreViewSize;

	public byte[] mdata = new byte[640 * 480 * 4];
	public byte[] mdatattmp = new byte[320 * 240 * 3 / 2];
	public byte[] mdata2 = new byte[320 * 240 * 3 / 2];
	private Handler mHandler;

	private int numbuf = 0;
	private int index = 0;
	private int ret = 0;

	public Button mcap;
	private Bitmap bitmap;
	private Bitmap bmp;
	private int[] rgb;
	Context mContext;
	int mCamIdx = 0;
	public boolean mRuning = true;

	boolean BMP_Switch = false;	// cfg
	GLFrameRenderer mGLFRendererPreview;

	boolean ImgFetched = true;	// 图像取走标记

	int swapUV = 0;		// camera 数据，交换UV

	long NativeCamFrmNumInPeriod = 0;		// 当前周期内nativecam采集帧率统计

	AVCom avcom;

	/////////////

	private Camera mCamera;// Camera对象
	//private Button mButton, mStopButton;// 右侧条框，点击出发保存图像（拍照）的事件
	private SurfaceView mSurfaceView;// 显示图像的surfaceView
	private SurfaceHolder holder;// SurfaceView的控制器
	//    private AutoFocusCallback mAutoFocusCallback = new AutoFocusCallback();// AutoFocusCallback自动对焦的回调对象
	private ImageView sendImageIv;// 发送图片的imageview，位于右侧条框

	private String strCaptureFilePath = Environment
			.getExternalStorageDirectory() + "/DCIM/Camera/";// 保存图像的路径

	private static int IMAGE_FORMAT = ImageFormat.NV21; // YV12;
	//private static final int IMAGE_FORMAT = ImageFormat.YV12; // YV12;

	String DeviceMode = android.os.Build.MODEL;

	// devnameIn: /dev/video9
	public NativeCam(Context ctx, SurfaceView Imag, SurfaceHolder holderIn,
					 int CamIdx, int w, int h, GLFrameRenderer GLFRendererPreview, AVCom avcomIn)
	{
		mContext = ctx;

		mSurfaceView = Imag;

		mCamIdx = CamIdx;

		width = w;
		height = h;

		mGLFRendererPreview = GLFRendererPreview;

		holder = holderIn;

		avcom = avcomIn;

		//YU12和YV12属于YUV420格 式，也是一种Plane模式，将Y、U、V分量分别打包，依次存储
		//NV12和NV21属于YUV420格式，是一种two-plane模式，即Y和UV分为两个Plane，但是UV（CbCr）为交错存储，而不是分为三个plane

		// Device model
// 		String DeviceMode = android.os.Build.MODEL;
//		String newmode = null;
//		if(DeviceMode.contains(" "))
//			newmode = DeviceMode.replace(" ", "_");
//		else
//			newmode = DeviceMode;
		if (DeviceMode.equals("HUAWEI MT1-U06"))
		{
			IMAGE_FORMAT = ImageFormat.YV12;
			swapUV = 0; // 1;
		}
		else if (DeviceMode.equals("H60-L01"))
		{
			//IMAGE_FORMAT = ImageFormat.YV12;
			IMAGE_FORMAT = ImageFormat.NV21;
			swapUV = 1; // 1;
		}
		else if (DeviceMode.equals("U8860"))
		{
			//IMAGE_FORMAT = ImageFormat.YV12;
			IMAGE_FORMAT = ImageFormat.YV12;
			swapUV = 1; // 1;
		}
	}

	public void setavcom(AVCom avcomIn)
	{
		avcom = avcomIn;
	}

	public void Release()
	{
		if (mCamera != null)
		{
			stopCamera();

			mCamera.setPreviewCallback(null);

//	    	try {
//		    	mCamera.setPreviewDisplay(null);
//	    	} catch (IOException exception) {
//	        }

			mCamera.release();
			mCamera = null;
		}
	}

//	// 连续取图
//  	final Runnable mUpdateUI = new Runnable() {
//
//        @Override
//        public void run() {
//            // TODO Auto-generated method stub
//            mImag.setImageBitmap(bitmap);	// draw
//
//        }
//    };
//  	class StartThread extends Thread {
//
//        @Override
//        public void run() {
//            // TODO Auto-generated method stub
//            //super.run();
//        	int count = 0;
//
//            while(mRuning) {
//
//            	try {
//        			Thread.sleep(1); // 10);
//        		} catch (InterruptedException e) {
//
//        			e.printStackTrace();
//        		}
//
//            	byte[] data = mdata;
//
//                {	// uvc cam
//
//                	// 本地显示
//                	if ((count++ % 2) == 0)
//                	{
//	                    if((index < 0) || (mdata == null)) {
//	                        //onDestroy();
//	                        break;
//	                    }
//
////	                    if (BMP_Switch)	// cfg
////	                    {
////		                    if (mCamIdx == 0)
////		                	{
////		                		Fimcgzsd.pixeltobmp(bmp);
////		                	}
////
////		                    mHandler.post(mUpdateUI);
////		                    bitmap = bmp;
////		                }
//
//	                    if (mGLFRendererPreview != null)
//	                    {
//	                    	mGLFRendererPreview.update(width, height);
//	                    	mGLFRendererPreview.updateYVV(mdata, 0, width, height);
//	                    }
//                	}
//
//                }
//            }
//
//            Release();
//        }
//    }

	/* 停止相机的method */
	private void stopCamera() {
		if (mCamera != null) {
			try {
                /* 停止预览 */
				mCamera.stopPreview();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	// 检测摄像头是否存在的私有方法
	boolean checkCameraHardware(Context context) {
		if (context.getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_CAMERA)) {
			// 摄像头存在
			return true;
		} else {
			// 摄像头不存在
			return false;
		}
	}

	// 每次cam采集到新图像时调用的回调方法，前提是必须开启预览
	class priviewCallBack implements Camera.PreviewCallback {

		@Override
		public void onPreviewFrame(byte[] data, Camera camera) {
			// TODO Auto-generated method stub
			// Log.w("wwwwwwwww", data[5] + "");
			// Log.w("支持格式", mCamera.getParameters().getPreviewFormat()+"");

			NativeCamFrmNumInPeriod++;

			decodeToBitMap(data, camera);
		}
	}

	int counter = 0;
	public void decodeToBitMap(byte[] data, Camera _camera) {

		if (_camera == null)
		{
			return;
		}

		Camera.Parameters parameters = mCamera.getParameters();
		//int w = parameters.getInt();
		PreViewSize = parameters.getPreviewSize();
		int len = data.length;
		int z = len;



		if (ImgFetched)
		{
			byte d[];

			//synchronized(mdata2)
			{
				if (DeviceMode.equals("U8860"))
				{
					GetSmallImg2(data, mdatattmp, PreViewSize.width, PreViewSize.height, 320, 240, swapUV); // (counter++)%2); //
				}
				else
					GetSmallImg(data, mdatattmp, PreViewSize.width, PreViewSize.height, 320, 240, swapUV); // (counter++)%2); //
			}
			synchronized(mdata2)
			{
				System.arraycopy(mdatattmp, 0, mdata2, 0, 320 * 240 * 3 / 2);
			}

			//System.arraycopy(data, 0, mdata2, 0, 320 * 240 * 3 / 2);

//			//synchronized(mdata2)
//			//if (false)
//			{
//				if (avcom != null)
//				{
//					d = avcom.GetSmallImgJni(data, PreViewSize.width, PreViewSize.height, 320, 240, swapUV);
//					synchronized(mdata2)
//					{
//						System.arraycopy(d, 0, mdata2, 0, 320 * 240 * 3 / 2);
//					}
//				}
//			}



			//System.arraycopy(data, 0, mdata, 0, len);

			ImgFetched = false;
		}

		YuvImage image;
//        Size size = _camera.getParameters().getPreviewSize();
//        try {
//            YuvImage image = new YuvImage(data, ImageFormat.NV21, size.width,
//                    size.height, null);
//            Log.w("wwwwwwwww", size.width + " " + size.height);
//            if (image != null) {
//                ByteArrayOutputStream stream = new ByteArrayOutputStream();
//                image.compressToJpeg(new Rect(0, 0, size.width, size.height),
//                        80, stream);
//                Bitmap bmp = BitmapFactory.decodeByteArray(
//                        stream.toByteArray(), 0, stream.size());
//                Log.w("wwwwwwwww", bmp.getWidth() + " " + bmp.getHeight());
//                Log.w("wwwwwwwww",
//                        (bmp.getPixel(100, 100) & 0xff) + "  "
//                                + ((bmp.getPixel(100, 100) >> 8) & 0xff) + "  "
//                                + ((bmp.getPixel(100, 100) >> 16) & 0xff));
//
//                stream.close();
//            }
//        } catch (Exception ex) {
//            Log.e("Sys", "Error:" + ex.getMessage());
//        }
	}

//   public void StartCamera() {
//
//    	if (mCamera != null)
//    	{
//	    	//stopCamera();
//	        //mCamera.release();
//	        //mCamera = null;
//
//            try {
//                Camera.Parameters parameters = mCamera.getParameters();
//                /*
//                 * 设定相片大小为1024*768， 格式为JPG
//                 */
//                // parameters.setPictureFormat(PixelFormat.JPEG);
//
////                parameters.setPictureSize(1024, 768);
//                parameters.setPictureSize(width, height);
////                parameters.setPreviewSize(width, height); // 此句导致itop4418摄像头预览花屏
//                parameters.setPreviewFormat(IMAGE_FORMAT); // setting preview format：YV12
//                mCamera.setParameters(parameters);
//
//                /* 打开预览画面 */
//                mCamera.startPreview();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }


//    /* 相机初始化的method */
//    public void RestartCamera() {
//
//    	if (mCamera != null)
//    	{
//	    	stopCamera();
//	        //mCamera.release();
//	        //mCamera = null;
//
//	    	StartCamera();
//        }
//    }

	public static String cameraSizeToSting(Iterable<Size> sizes)
	{
		StringBuilder s = new StringBuilder();
		for (Size size : sizes)
		{
			if (s.length() != 0)
				s.append(",");
			s.append(size.width).append('x').append(size.height);
		}
		return s.toString();
	}

	public int OnCreateNatvieCamVideoView()
	{
		int ret = 0;
//
//        if (checkCameraHardware(this)) {
//            Log.e("============", "摄像头存在");// 验证摄像头是否存在
//        }
//        else
//        {
//        	return 0;
//        }
//
//        //mSurfaceView = (SurfaceView) findViewById(R.id.mSurfaceView);
//        holder = mSurfaceView.getHolder();
//        holder.addCallback(this);

		//mImag = (ImageView)findViewById(R.id.video_Image_local);
		//mcap = (Button)findViewById(R.id.mcap);
		bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		numbuf  = 4;
		//mdata = new byte[width * height * numbuf];
		rgb = new int[width * height * numbuf];

		// open android camera
		{
			Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
			int camnum = Camera.getNumberOfCameras();
			camnum = 1;
			for(int cameraId=0; cameraId < camnum; cameraId++)
			{
				Camera.getCameraInfo( cameraId, cameraInfo);
				Camera camera = Camera.open(cameraId);
				Camera.Parameters params = camera.getParameters();
				List<Size> previewSizes = params.getSupportedVideoSizes();
				if (previewSizes != null)
				{
					Log.e("WG", "Video supported sizes: " + cameraSizeToSting( previewSizes));
				}
				camera.release();
			}

			try {
				mCamera = null;
				try {
					if (DeviceMode.equals("H60-L01"))
					{
						mCamera = Camera.open(1);
					}
					else
						mCamera = Camera.open(0);//打开相机；在低版本里，只有open（）方法；高级版本加入此方法的意义是具有打开多个
					//摄像机的能力，其中输入参数为摄像机的编号
					//在manifest中设定的最小版本会影响这里方法的调用，如果最小版本设定有误（版本过低），在ide里将不允许调用有参的
					//open方法;
					//如果模拟器版本较高的话，无参的open方法将会获得null值!所以尽量使用通用版本的模拟器和API；
				} catch (Exception e) {
					Log.e("============", "摄像头被占用");
				}
				if (mCamera == null) {
					Log.e("============", "摄像机为空");
					System.exit(0);
				}

				Camera.Parameters parameters = mCamera.getParameters();
//                parameters.setPictureSize(1024, 768);
				//width = 1920;
				//height = 1080;
				parameters.setPictureSize(width, height);
				if (DeviceMode.equals("AOSP on drone2"))
				{
					// 4418
				}
				else
				{
					parameters.setPreviewSize(width, height);	// 此句导致itop4418摄像头预览花屏
				}
				parameters.setPreviewFormat(IMAGE_FORMAT); // setting preview format：YV12
				//parameters.setPreviewFrameRate(30000);
				//parameters.setPreviewFpsRange(15000, 20000);

				mCamera.setParameters(parameters);

				mCamera.setPreviewDisplay(holder);//设置显示面板控制器
				//mCamera.setPreviewDisplay(null);//设置显示面板控制器

				priviewCallBack pre = new priviewCallBack();//建立预览回调对象
				mCamera.setPreviewCallback(pre); //设置预览回调对象
				//mCamera.getParameters().setPreviewFormat(ImageFormat.JPEG);
				mCamera.startPreview();//开始预览，这步操作很重要
			}
			catch (IOException exception) {
				mCamera.release();
				mCamera = null;
			}


			// 不添加显示面板的代码：
            /*
             * 打开相机， mCamera = null; try { mCamera = Camera.open(0); } catch
             * (Exception e) { Log.e("============", "摄像头被占用"); } if (mCamera ==
             * null) { Log.e("============", "返回结果为空"); System.exit(0); } //
             * mCamera.setPreviewDisplay(holder); priviewCallBack pre = new
             * priviewCallBack(); mCamera.setPreviewCallback(pre); Log.w("wwwwwwww",
             * mCamera.getParameters().getPreviewFormat() + "");
             * mCamera.startPreview();
             */
		}

		try {
			Thread.sleep(500); // 10);
		} catch (InterruptedException e) {

			e.printStackTrace();
		}

//        mHandler = new Handler();
//        new StartThread().start();

		return 1;
	}



	public static void GetSmallImg(byte[] dataIn, byte[] dataOut, int w_org, int h_org, int w_dst, int h_dst, int swapUV)
	{
		int pO = 0;
		int pI = 0;
		int OrgLineLen = w_org;
		float scale = w_org / w_dst;
		float scaley = h_org / h_dst;
		//int OrgLineLen2 = OrgLineLen / scale;

		for(int y = 0; y < h_dst; y++)
		{
			for(int x = 0; x < w_dst; x++)
			{
				dataOut[pO] = dataIn[pI];
				pO++;
				pI += scale;
			}
			pI+=OrgLineLen;
		}

		// nv12
		int h2 = (int)(h_dst / scale);
		int w2 = (int)(w_dst / scaley);
//		for(int y = 0; y < h2; y++)
//		{
//			for(int x = 0; x < w2; x++)
//			{
//				dataOut[pO+swapUV] = dataIn[pI];
//				pO++;
//				pI++;
//				dataOut[pO-swapUV] = dataIn[pI];
//				pO++;
//				pI += 1 + 2 * (scale - 1);
//			}
//
//			pI += OrgLineLen * (scale - 1);
//		}
		if (swapUV == 0)
		{
			for(int y = 0; y < h2; y++)
			{
				for(int x = 0; x < w2; x++)
				{
					dataOut[pO] = dataIn[pI];
					pO++;
					pI++;
					dataOut[pO] = dataIn[pI];
					pO++;
					pI += 1 + 2 * (scale - 1);
				}

				pI += OrgLineLen * (scale - 1);
			}
		}
		else
		{
			for(int y = 0; y < h2; y++)
			{
				for(int x = 0; x < w2; x++)
				{
					dataOut[pO+swapUV] = dataIn[pI];
					pO++;
					pI++;
					dataOut[pO-swapUV] = dataIn[pI];
					pO++;
					pI += 1 + 2 * (scale - 1);
				}

				pI += OrgLineLen * (scaley - 1);
			}
		}
	}

	public static void GetSmallImg2(byte[] dataIn, byte[] dataOut, int w_org, int h_org, int w_dst, int h_dst, int swapUV)
	{
		int pO = 0;
		int pI = 0;
		int OrgLineLen = w_org;
		float scale = w_org / w_dst;
		float scaley = h_org / h_dst;
		//int OrgLineLen2 = OrgLineLen / scale;

		for(int y = 0; y < h_dst; y++)
		{
			for(int x = 0; x < w_dst; x++)
			{
				dataOut[pO] = dataIn[pI];
				pO++;
				pI += scale;
			}
			pI+=OrgLineLen;
		}

		// nv12
		int h2 = (int)(h_dst / scale);
		int w2 = (int)(w_dst / scaley);

		int pO2 = (int)(w_dst * h_dst / 4) + pO;
		if (swapUV == 0)
		{
			for(int y = 0; y < h2; y++)
			{
				for(int x = 0; x < w2; x++)
				{
					dataOut[pO] = dataIn[pI];
					pO++;
					pI++;
					dataOut[pO2] = dataIn[pI];
					pO2++;
					pI += 1 + 2 * (scale - 1);
				}

				pI += OrgLineLen * (scale - 1);
			}
		}
		else
		{
			for(int y = 0; y < h2; y++)
			{
				for(int x = 0; x < w2; x++)
				{
					dataOut[pO] = dataIn[pI+swapUV];
					pO++;
					pI++;
					dataOut[pO2] = dataIn[pI-swapUV];
					pO2++;
					pI += 1 + 2 * (scale - 1);
				}

				pI += OrgLineLen * (scaley - 1);
			}
		}
	}
}

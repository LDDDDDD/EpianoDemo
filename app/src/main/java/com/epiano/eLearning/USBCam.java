package com.epiano.eLearning;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.epiano.uvc.UvcWebCam;


public class USBCam
{
	private ImageView mImag;
	private int width = 0; //WIDTH;
	private int height = 0; //HEIGHT;
	private String devname; // = "/dev/video9"; //3";
	public byte[] mdata;	/////////////////////////////////////
	private Handler mHandler;
	private int numbuf = 0;
	private int index = 0;
	private int ret = 0;
	private int ctype = 1;//0 is zc301 1 is uvc camera
	public Button mcap;
	private Bitmap bitmap;
	private Bitmap bmp;
	private int[] rgb;
	Context mContext;
	int mCamIdx = 0;
	public int CamOpenOK = 0;
	public boolean mRuning = true;

	UvcWebCam Fimcgzsd;
	UvcWebCam Fimcgzsd2;

	boolean BMP_Switch = false;	// cfg
	GLFrameRenderer mGLFRendererPreview;

	// devnameIn: /dev/video9
	public USBCam(Context ctx, String devnameIn, ImageView Imag,
				  int CamIdx, int w, int h, GLFrameRenderer GLFRendererPreview)
	{
		mContext = ctx;

		devname = devnameIn;

		mImag = Imag;

		mCamIdx = CamIdx;

		width = w;
		height = h;

		mGLFRendererPreview = GLFRendererPreview;

		OnCreateUSBVideoView();
	}

	public void Release()
	{
		if (Fimcgzsd != null)
		{
			Fimcgzsd.release();
		}

		if (Fimcgzsd2 != null)
		{
			Fimcgzsd2.release();
		}
	}

	// 连续取图
	final Runnable mUpdateUI = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			mImag.setImageBitmap(bitmap);	// draw

		}
	};
	class StartThread extends Thread {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			//super.run();
			int count = 0;

			while(mRuning) {

				try {
					Thread.sleep(1); // 10);
				} catch (InterruptedException e) {

					e.printStackTrace();
				}

				if(ctype == 1){	// uvc cam

					if (mCamIdx == 0)
					{
						index = Fimcgzsd.dqbuf(mdata);
					}
					else
					{
						index = Fimcgzsd2.dqbuf(mdata);
					}
					byte[] data = mdata;

					// 本地显示
					if ((count++ % 2) == 0)
					{
						if((index < 0) || (mdata == null)) {
							//onDestroy();
							break;
						}

						if (BMP_Switch)	// cfg
						{
							if (mCamIdx == 0)
							{
								Fimcgzsd.pixeltobmp(bmp);
							}
							else
							{
								Fimcgzsd2.pixeltobmp(bmp);
							}

							mHandler.post(mUpdateUI);
							bitmap = bmp;
						}

						if (mGLFRendererPreview != null)
						{
							mGLFRendererPreview.update(width, height);
							mGLFRendererPreview.updateYVV(mdata, 0, width, height);
						}
					}

					if (mCamIdx == 0)
					{
						Fimcgzsd.qbuf(index);
					}
					else
					{
						Fimcgzsd2.qbuf(index);
					}
					//Fimcgzsd.yuvtorgb(mdata, rgb);
					//mHandler.post(mUpdateUI);
					//bitmap = Bitmap.createBitmap(rgb,width,height,Bitmap.Config.ARGB_8888);
					//Fimcgzsd.qbuf(index);
				} else {

					if (mCamIdx == 0)
					{
						index = Fimcgzsd.dqbuf(mdata);
					}
					else
					{
						index = Fimcgzsd2.dqbuf(mdata);
					}
					if(index < 0) {
						//onDestroy();
						break;
					}
					mHandler.post(mUpdateUI);
					bitmap = BitmapFactory.decodeByteArray(mdata, 0, width * height);
					if (mCamIdx == 0)
					{
						Fimcgzsd.qbuf(index);
					}
					else
					{
						Fimcgzsd2.qbuf(index);
					}

				}
			}

			Release();
		}
	}
	public void OnCreateUSBVideoView()
	{
		int ret = 0;

		if (mCamIdx == 0)
		{
			Fimcgzsd = new UvcWebCam(mContext);
		}
		else
		{
			Fimcgzsd2 = null; //new UvcWebCam2(mContext);
		}

		//mImag = (ImageView)findViewById(R.id.video_capture_surface_usbcam);
		//mcap = (Button)findViewById(R.id.mcap);
		bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		numbuf  = 4;
		mdata = new byte[width * height * 3 / 2]; // numbuf];
		rgb = new int[width * height * numbuf];

		if (mCamIdx == 0)
		{
			ret = Fimcgzsd.open(devname.getBytes());
			CamOpenOK = ret;
		}
		else
		{
			ret = Fimcgzsd2.open(devname.getBytes());
		}
		if(ret < 0)
		{
			Toast.makeText(mContext, "Can't open uvc camera.", Toast.LENGTH_SHORT).show();
			//finish();
			return;
		}

		if (mCamIdx == 0)
		{
			ret = Fimcgzsd.init(width, height, numbuf,ctype);
		}
		else
		{
			ret = Fimcgzsd2.init(width, height, numbuf,ctype);
		}
		if(ret < 0)
		{
			Toast.makeText(mContext, "Can't set uvc camera.", Toast.LENGTH_SHORT).show();
			//finish();
			return;
		}

		if (mCamIdx == 0)
		{
			ret = Fimcgzsd.streamon();
		}
		else
		{
			ret = Fimcgzsd2.streamon();
		}
		if(ret < 0)
		{
			Toast.makeText(mContext, "Can't open camera stream.", Toast.LENGTH_SHORT).show();
			//finish();
			return;
		}

		try {
			Thread.sleep(500); // 10);
		} catch (InterruptedException e) {

			e.printStackTrace();
		}

		mHandler = new Handler();
		new StartThread().start();
		//mcap.setOnClickListener(new CaptureListener());
	}

	// 一次取一图
	private void OnCreateUSBVideoView_OneFrameMode()
	{
		int ret = 0;

		Fimcgzsd = new UvcWebCam(mContext);

		bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		numbuf  = 4;
		mdata = new byte[width * height * numbuf];
		rgb = new int[width * height * numbuf];

		mHandler = new Handler();
		new StartThread_OneFrameMode().start();
	}
	private void GetOneFrameThenCloseCamera()
	{
		int ret = 0;

		if (mCamIdx == 0)
		{
			ret = Fimcgzsd.GetOneFrame(devname.getBytes(), width, height, numbuf,ctype, bmp);
		}
		else
		{
			ret = Fimcgzsd.GetOneFrame(devname.getBytes(), width, height, numbuf,ctype, bmp);
		}

		if(ret < 0)
		{
			Toast.makeText(mContext, "Can't set camera.", Toast.LENGTH_SHORT).show();
			//finish();
			return;
		}

	}
	class StartThread_OneFrameMode extends Thread {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			//super.run();
			while(mRuning) {
				if(ctype == 1){	// uvc cam
					GetOneFrameThenCloseCamera();

					mHandler.post(mUpdateUI);
					bitmap = bmp;
				} else {

				}

				try {
					Thread.sleep(100); // 10);
				} catch (InterruptedException e) {

					e.printStackTrace();
				}
			}
		}
	}
}

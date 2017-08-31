package com.epiano.commutil;

//import android.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.epiano.av.encode.androidencode.AvcHWDecoder;
import com.epiano.av.encode.androidencode.AvcHWEncoder;
import com.epiano.av.ictvoip.androidvideo.display.AndroidVideoWindowImpl;
import com.epiano.eLearning.AVCom;
import com.epiano.eLearning.GLFrameRenderer;
import com.epiano.eLearning.NativeCam;
import com.epiano.eLearning.USBCam;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.Timer;
import java.util.TimerTask;

//import android.content.DialogInterface;
//import android.content.pm.ResolveInfo;
//import android.epiano.com.commutil.R;
//import android.epiano.com.commutil.R;
//import android.graphics.Canvas;
//import android.graphics.Bitmap.Config;
//import android.media.AudioFormat;
//import android.media.AudioManager;
//import android.media.AudioTrack;
//import android.os.AsyncTask;
//import com.example.myogl2.R;


//import com.example.android.apis.view.Grid1;

//import com.example.android.apis.R;
//import com.example.android.apis.view.Grid1;
//import com.example.android.apis.view.Grid1.AppsAdapter;


@SuppressWarnings("JniMissingFunction")
public class PianoELearning extends Activity  implements SurfaceHolder.Callback  { // implements OnClickListener {

	private static String TAG = "Video";

	// MScore
	PianoELearning_MScore MScore = null;
	// camera
//	UvcWebCam Fimcgzsd;	// jni
//	UvcWebCam2 Fimcgzsd2;	// jni
	USBCam UsbCam;
	USBCam UsbCam2;
	// AVCom
//	AudioVideo AVCom;

	//Button mBtnSetting;

	//VEnc venc;
	//VDec vdec;
//	EncCtl venc;
//	DecCtl vdec;
	AVCom avcom;

	//	enum VIDEO_ENC_MODE
//	{
//		VIDEO_ENC_MODE_X264,
//		VIDEO_ENC_MODE_HW264,
//	};
//	VIDEO_ENC_MODE VideoEncMode = VIDEO_ENC_MODE.VIDEO_ENC_MODE_X264; // cfg...
	int UseHw264Enc = 1; // cfg...

	/////////////////////////////

	final int WIDTH = 320;
	final int HEIGHT = 240;
	//	final int WIDTH = 640;
//	final int HEIGHT = 480;
	final int BR = 25000; //40000; //30000; //200000; // 300000; // 500000;	// cfg Bps


//	Config BmpConfig = Bitmap.Config.ARGB_8888;
//	//Config BmpConfig = Bitmap.Config.RGB_565;
//	String ImgFileSurfix = ".bmp";

	public static AndroidVideoWindowImpl mWindows = null;

	// peer video
	LinearLayout LL3D;
	public static GLSurfaceView mRenderSurfaceView = null;
	//	public static ImageView mRenderSurfaceView2 = null;
//	OglDisp ogldisp;
	GLFrameRenderer mPeerViewGLFRenderer;

	// local video
	LinearLayout LLusbcamView;
	HorizontalScrollView StatSrollView;
	public static GLSurfaceView mRenderPreviewSurfaceView = null;
	//	public static ImageView mRenderSurfaceView2 = null;
//	OglDisp ogldisp;
	GLFrameRenderer mPreViewGLFRenderer;
	//
	LinearLayout LLnativecamView;

	private SeekBar mSeekBarBR;
	EditText EditIP;
	String remoteipvalue = "127.0.0.1";

	public static SurfaceView mPreviewSurfaceView = null;

	float whK = (float)1.6;						// 图像长宽比

	String openfilename;

	ProgressBar pb;

//	public static native int AVComInit();				// jni: fec 264等编解码等
//	public static native void AVComDestroy();		// jni: fec 264等编解码等
//
//	// fec编码
//	public static native int FECEncode(
//			byte [] H264data,
//		    int H264datalen,
//		    int MaxPkSize_L,
//		    int OrgPkNum_N,
//		    int FecPkNum_n);
//	// 获取fec包数据
//	public static native byte[] GetFecPk(int FecPkId);
//	public static native int PutPk(
//      	int pkid,
//	    byte H264data[],
//	    int MaxPkSize_L,
//	    int AllPkNum_N
//		);
//
//	public static native int VideoDecode(
//		int NulCount,
//	    int H264Frmdatalen,		// 264原始数据长度， 不包括fec包
//	    byte HTransFrmdata[],	// 原数据 + FEC数据, 可整除MaxPkSize_L
//	    int MaxPkSize_L,
//	    int OrgPkNum_N,
//	    int FecPkNum_n,
//	    int jlostPk[],
//	    int lostPkNum
//		);
//
//	// 调试输出到voiceshow
//	public static native int IntTestOut(String paraname, String paravalue);
//	public static native int IntPrint(int level, String jprintStr); // ICTVOIP_LOG_ERROR:6
//	public static native byte[] GetDecPic();
//	public static native byte[] GetDecPicYVU();
//	public static native void SetPicBufIdle();
//	public static native int GetDecPic2(Bitmap bmp);
//	public static native int GetPicWidth();
//	public static native int GetPicHeight();

	////////////////////////////////////////////
	//
	//private static native void setwindows(Object mRenderSurfaceView, Object mPreviewSurfaceView);
	//private static native void setwindows(Object PeerSurfaceView, Object PreviewSurfaceView);
	private static native void setPreviewWindow(Object PreviewSurfaceView);
	private static native void setPeerViewWindow(Object PeerSurfaceView);
	private static native void setPreviewView(Object mWinViewID);
	private static native void setVideoWinImpl(Object mWinImpID);
	//
	private static native void init();
	//
	private static native void start();
	private static native void startAudio();
	private static native void muteAudio();
	private static native void unMuteAudio();
	private static native void startVideo();
	private static native void startAV();
	private static native void stop();
	//
	private static native void setCamID(int index);
	//
	private static native void swichcam();
	//
	private static native void setPhoneMode(String mode);
	//
	private static native void setEncFps(int fps);
	private static native void setResolution(int index);
	private static native void setEncBitrate(int bitrate);
	private static native void setUseHwcode(int flag);
	private static native void setUseImgCut(int flag);
	private static native void setAdaptiveRes(int flag);
	//
	private static native void setSendFileName(String filepath);
	private static native void startFileSend();
	private static native int getFileSendValue();
	private static native int getFileRecvValue();
	private static native int getFileSendEndFlag();
	private static native int getFileRecvEndFlag();
	//
	private static native void setServerIP(String destip);
	private static native void setServerPort(int port);
	private static native void setRemoteIP(String destip);
	private static native void setRemotePort(int port);
	private static native void setRemoteName(String name);

	private static native void setLocalIP(String ipAdress);
	private static native void setLocalPort(int port);
	private static native void setLocalName(String name);
	private static native void initSocket();
	//
	private static native int getVideoStartFlag();
	private static native void startVideoThread(int flag);
	public static native int getVideoDisplayFlag();
	// wg add
	public static native int OnMainThread();

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

	@Override
	protected void onDestroy() {

		{
			remoteipvalue = EditIP.getText().toString();
			configInfo.edit().putString(REMOTEIP, remoteipvalue).commit();
		}

		if (avcom != null)
		{
			avcom.Destory(); // jni: fec 264等编解码等
		}

		if (UsbCam != null)
		{
			UsbCam.mRuning = false;
		}

		if (UsbCam2 != null)
		{
			UsbCam2.mRuning = false;
		}

		if (nativeCam != null)
		{
			nativeCam.Release();
		}

		onDestroyCamera();

//	    if (venc != null)
//	    {
//	    	venc.mRuning = false;
//	    }
//
//	    if (vdec != null)
//	    {
//	    	vdec.mRuning = false;
//	    }

//	    if (audioTrack != null
//	    		&& StartToPlay == true)
//	    {
//	    	StartToPlay = false;
////	    	audioTrack.stop();//停止播放
////			audioTrack.release();//释放底层资源
//	    }

		super.onDestroy();
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		// TODO Auto-generated method stub
		super.onWindowFocusChanged(hasFocus);

		if (hasFocus) {

			//do something
			OnActivityLoaded();
		}
	}

	public void fixZOrder(SurfaceView video, SurfaceView preview) {
		video.setZOrderOnTop(false);
		if (preview != null)
		{
			preview.setZOrderOnTop(true);
			preview.setZOrderMediaOverlay(true); // Needed to be able to display
		}												// control layout over
	}

	//Get wifi IP Address
	private String getWifiIPAddress() {
		WifiManager wifiManager = (WifiManager) this.getSystemService(this.WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();

		int ipAddress = wifiInfo.getIpAddress();

		if(ipAddress != 0)
			//
			return String.format("%d.%d.%d.%d",
					(ipAddress & 0xff), (ipAddress >> 8 & 0xff),
					(ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
		else
			return "127.0.0.1";
	}


	// 3G IP  Address
	public static String get3GIPAddress() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
				 en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()
							&& inetAddress instanceof Inet4Address) {
						// if (!inetAddress.isLoopbackAddress() && inetAddress
						// instanceof Inet6Address) {
						return inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private void onStartClickEvent() {
		// TODO Auto-generated method stub
		mBtnStart.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {

				Log.d(TAG, "onStartClickEvent");

				//initViewConfig();

//				AVCom.start();
				avcom.Start();
				Log.d(TAG, "onStartClickEvent done.");


			}
		});
	}

	private void onStopClickEvent() {
		// TODO Auto-generated method stub
		mBtnStop.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {

				Log.d(TAG, "onStopClickEvent");

//				AVCom.stop();
//				AVCom.statThreadRuning = false;
				// AndroidVideoApiJniWrapper.stopRecording(mCamera);
				System.exit(0); // tbd...

			}
		});
	}

	private void onSettingClickEvent() {
		// TODO Auto-generated method stub
		mBtnSetting.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {

//				Log.d(TAG, "onSettingClickEvent");
//				openOptionsMenu();
				Configuration config = getResources().getConfiguration();
				if ((config.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) > Configuration.SCREENLAYOUT_SIZE_LARGE) {
					int originalScreenLayout = config.screenLayout;
					config.screenLayout = Configuration.SCREENLAYOUT_SIZE_LARGE;
					openOptionsMenu();
					config.screenLayout = originalScreenLayout;
				} else {
					openOptionsMenu();
				}

			}
		});
	}

	private void onChooseCamClickEvent() {
		// TODO Auto-generated method stub
		mBtnChooseCam.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {

				avcom.venc.SetCamSrc(-1);
			}
		});
	}

	// 在系统完成activity加载后，执行与窗口相关的操作
	void OnActivityLoaded()
	{
		if (mRenderSurfaceView == null)
		{
			return;
		}

		if (MScore != null)
		{
			return;
		}

		if (UsbCam != null)
		{
			//UsbCam.OnCreateUSBVideoView();  // usb camera

			// layout
			if (UsbCam.CamOpenOK >= 0)
			{
				int comW = 100; //LLusbcamView.getWidth();
				int comH = 75;
				LayoutParams ll = new LayoutParams(comW, comH);
				LLusbcamView.setLayoutParams(ll);//设置布局参数
			}
		}
		if (UsbCam2 != null)
		{
			UsbCam2.OnCreateUSBVideoView();  // usb camera
		}
		// 一次性打开摄像头，取一图后，立刻关闭摄像头
		// 不好用, 卡, 崩溃, 暂时不用
//		if (UsbCam != null)
//		{
//			UsbCam.OnCreateUSBVideoView_OneFrameMode();  // usb camera
//		}

		if (nativeCam != null)
		{
			nativeCam.OnCreateNatvieCamVideoView();

			// layout
			{
				int comW = 100; //
				int comH = 75;
				LayoutParams ll = new LayoutParams(comW, comH);
				LLnativecamView.setLayoutParams(ll);//设置布局参数
			}
		}

//		AVCom.OnActivityLoaded();
		LayerOutVideoCom();

		// 通信窗口
		{
			int comW = LL3D.getWidth();
			int comH = comW * 3 / 4;
			LayoutParams ll = new LayoutParams(comW, comH);
			LL3D.setLayoutParams(ll);//设置布局参数
		}


		// 曲谱窗口
		Context context = this;
		AttributeSet attrs = null;
//		String openfilenameIn = openfilename;
		LinearLayout ll = (LinearLayout) findViewById(R.id.ll_left);
		GridView GridV = (GridView) findViewById(R.id.GridView1);
//	    int w = ll.getWidth();
//	    int h = (int)(w * whK);
		MScore = new PianoELearning_MScore(context, attrs, openfilename, ll, GridV); //w, h);

		// yyyy 显示曲谱
		MScore.OnActivityLoaded();

//
//		if (true)
//		{
//			// 渲染music score窗口
//			RenderViews();
//
//			// 布局视频通信相关窗口
//			LayerOutVideoCom();
//		}
	}

	public class h264test extends Thread{
		AvcHWEncoder venc;
		AvcHWDecoder vdec;

		int width = WIDTH; //WIDTH;
		int height = HEIGHT; //HEIGHT;
		int framerate = 15;
		int bitrate = 500000;	// 100000
		boolean mRuning = true;
		byte nv12[] = new byte[(int)(WIDTH * HEIGHT * 1.5)];
		byte nv12_out[] = new byte[(int)(WIDTH * HEIGHT * 10)];
		byte encOut[] = new byte[(int)(WIDTH * HEIGHT * 1.5)];

		public h264test()
		{
			venc = new AvcHWEncoder(width, height, framerate, bitrate);
			vdec = new AvcHWDecoder(width, height, null);
		}

		public int genimg(int random)
		{
			for(int y=0; y < height; y++)
			{
				int pos1 = y * width;
				for(int x=0; x < width; x++)
				{
					int pos = pos1 + x;
					nv12[pos + 0] = (byte)(x + random);
					nv12[pos + 1] = (byte)(y + random);
					nv12[pos + 2] = (byte)(x + y + random);
				}
			}

			return 1;
		}

		public void run()
		{
			int counter = 0;
			int c = 0;
			int c2 = 0;
			int [] nalcount = new int[2];
			int [] nalpos = new int[100];
			while(mRuning)
			{
				genimg(counter * 3);
				c = venc.offerEncoder(nv12, encOut, width, height, nalcount, nalpos);
				if (c > 0)
				{
					c2 = vdec.offerDecoder(c, encOut, 0, nv12_out);
					if (c2 > 0)
					{
						Log.i(TAG, "offerDecoder(), dec datalen:" + c2);
					}
				}
				counter++;
			}
		}
	}

	/**
	 * SeekBarListener
	 * 定义一个监听器,该监听器负责监听进度条进度的改变
	 */
	private SeekBar.OnSeekBarChangeListener seekBarListener = new SeekBar.OnSeekBarChangeListener() {

		/**
		 * 当用户结束对滑块滑动时,调用该方法
		 */
		public void onStopTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
			//System.out.println("stop: > "+seekBar.getProgress());
		}

		/**
		 * 当用户开始滑动滑块时调用该方法
		 */
		public void onStartTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
			//System.out.println("start: => "+seekBar.getProgress());
		}

		/**
		 * 当进度条发生变化时调用该方法
		 */
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
		{
			// TODO Auto-generated method stub
			//System.out.println(progress);
			if (avcom != null)
			{
				avcom.DstSendBw = progress + avcom.mMinByteRate;
				//avcom.venc.mDelay2Reset = 1;
				avcom.Soft264setbitrateEncap((int)((avcom.DstSendBw - avcom.DstSendBwAudio) * 0.9));

				//avcom.pushval_BRSet(avcom.DstSendBw);
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		int r = 0;

//		{
//			Log.e(TAG, "Loading libavcom.so librsfec.so...");
//
//			//System.loadLibrary("ICTIntTest");
//			//System.loadLibrary("rsfec");
//			//System.loadLibrary("avcom");
//			//System.loadLibrary("ogl");
//
//			Log.e(TAG, "Loaded libavcom.so librsfec.so.");
//		}

		//设置为横屏
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

		requestWindowFeature(Window.FEATURE_NO_TITLE);//隐藏标题栏
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);//设置全屏

		super.onCreate(savedInstanceState);
		setContentView(R.layout.pianoelearning); //activity_main);

		// Device model
		String DeviceMode = android.os.Build.MODEL;
		String newmode = null;
		if(DeviceMode.contains(" "))
			newmode = DeviceMode.replace(" ", "_");
		else
			newmode = DeviceMode;

		// 软硬编码选择 cfg...
		if (DeviceMode.equals("iTOP-4412")
				||DeviceMode.equals("H60-L01")
				)
		{
			UseHw264Enc = 0;
		}
		UseHw264Enc = 0;


		// 编解码对接测试
//		if (true)
//		{
//			new h264test().start();
//
//			return;
//		}

		//pb = (ProgressBar)findViewById(R.id.pb);
		//pb.setMax(1000);

		// 获取文件名
		Intent intent=getIntent();
		openfilename = intent.getStringExtra("openfilename");



		{
			// 视频通信窗口
			//mPreviewSurfaceView = (SurfaceView) findViewById(R.id.video_capture_surface_nativecam);

			// peer view
			{
				//mRenderSurfaceView = (GLSurfaceView) findViewById(R.id.video_Image_peer);
				LL3D = (LinearLayout)findViewById(R.id.LinearLayout3D);
				mRenderSurfaceView = new GLSurfaceView(this);
				LL3D.addView(mRenderSurfaceView);
				//fixZOrder(mRenderSurfaceView, mPreviewSurfaceView);
				//			ogldisp = new OglDisp(); //(null, mRenderSurfaceView);
				//mGLSurface = (GLFrameSurface) findViewById(R.id.glsurface);
				mRenderSurfaceView.setEGLContextClientVersion(2);
				mPeerViewGLFRenderer = new GLFrameRenderer(this, mRenderSurfaceView);
				mRenderSurfaceView.setRenderer(mPeerViewGLFRenderer);
				//mPeerViewGLFRenderer.update(320, 240);
				//			mPeerViewGLFRenderer.update(ydata, udata, vdata);
			}

			// local usb camera preview
			LLusbcamView = (LinearLayout)findViewById(R.id.video_capture_surface_2usbcam);
			mRenderPreviewSurfaceView = new GLSurfaceView(this);
			LLusbcamView.addView(mRenderPreviewSurfaceView);
			mRenderPreviewSurfaceView.setEGLContextClientVersion(2);
			mPreViewGLFRenderer = new GLFrameRenderer(this, mRenderPreviewSurfaceView);
			mRenderPreviewSurfaceView.setRenderer(mPreViewGLFRenderer);
			ImageView Imag = (ImageView)findViewById(R.id.video_capture_surface_usbcam);
			//UsbCam = new USBCam(this, "/dev/video9", Imag, 0, WIDTH, HEIGHT, mPreViewGLFRenderer);
			UsbCam = new USBCam(this, "/dev/video4", Imag, 0, WIDTH, HEIGHT, mPreViewGLFRenderer);
//			if (UsbCam.CamOpenOK < 0)
//			{
//				UsbCam = null;
//			}
			//ImageView Imag2 = (ImageView)findViewById(R.id.video_Image_local2);
			//UsbCam2 = new USBCam(this, "/dev/video9", Imag2, 1);
			//
			LLnativecamView = (LinearLayout)findViewById(R.id.llvideo_capture_surface_nativecam);

			// native camera
			if (DeviceMode.equals("iTOP-4412") == false)
			{
				onCreateCamera();
			}


			//AVComInit(); // jni: fec 264等编解码等
			InitAVComCtl();

			{
				EditIP = (EditText)findViewById(R.id.EditTextIP);
				remoteipvalue = configInfo.getString(REMOTEIP, "127.0.0.1");
				EditIP.setText(remoteipvalue);
				//configInfo.edit().putString(REMOTEIP, remoteipvalue).commit();
			}
		}


		// 视频编解码器
		// xxxx
		//ImageView ImagePeer = (ImageView)findViewById(R.id.video_Image_peer);
		ImageView ImagePeer = (ImageView)findViewById(R.id.video_Image_peer2);
		ImageView ImageStat = (ImageView)findViewById(R.id.statview);
		StatSrollView = (HorizontalScrollView)findViewById(R.id.horizontalScrollViewStat);
		avcom = new AVCom(this, UsbCam, nativeCam, BR, WIDTH, HEIGHT,
				ImagePeer, mPeerViewGLFRenderer, StatSrollView, ImageStat, UseHw264Enc, remoteipvalue);
		if (nativeCam != null)
			nativeCam.setavcom(avcom);

		mSeekBarBR = (SeekBar)findViewById(R.id.seekBarBR);
		mSeekBarBR.setMax(avcom.mMaxByteRate - avcom.mMinByteRate);
		mSeekBarBR.setProgress(avcom.DstSendBw - avcom.mMinByteRate);
		mSeekBarBR.setOnSeekBarChangeListener(seekBarListener);

		//获取CheckBox实例
		CheckBox checkBoxAudio = (CheckBox)this.findViewById(R.id.checkBoxAudio);
		checkBoxAudio.setChecked(true);
		//绑定监听器
		checkBoxAudio.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
										 boolean isChecked) {
				// TODO Auto-generated method stub
				if(isChecked){
					//editText1.setText(buttonView.getText()+"选中");
					avcom.FreezeAudio = 0;
				}else{
					//editText1.setText(buttonView.getText()+"取消选中");
					avcom.FreezeAudio = 1;
				}
			}
		});
		CheckBox checkBoxVideo = (CheckBox)this.findViewById(R.id.checkBoxVideo);
		checkBoxVideo.setChecked(true);
		//绑定监听器
		checkBoxVideo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
										 boolean isChecked) {
				// TODO Auto-generated method stub
				if(isChecked){
					//editText1.setText(buttonView.getText()+"选中");
					avcom.FreezeVideo = 0;
				}else{
					//editText1.setText(buttonView.getText()+"取消选中");
					avcom.FreezeVideo = 1;
				}
			}
		});
		CheckBox checkBoxNetAuto = (CheckBox)this.findViewById(R.id.checkBoxNetAuto);
		checkBoxNetAuto.setChecked(true);
		//绑定监听器
		checkBoxNetAuto.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
										 boolean isChecked) {
				// TODO Auto-generated method stub
				if(isChecked){
					//editText1.setText(buttonView.getText()+"选中");
					avcom.BwSelfAdaptSwitch = 1;
				}else{
					//editText1.setText(buttonView.getText()+"取消选中");
					avcom.BwSelfAdaptSwitch = 0;
				}
			}
		});

		ImageButton buttonPlay = (ImageButton)findViewById(R.id.imageButtonPlay);
		/* 监听button的事件信息 */
		buttonPlay.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {

				//////////////////////

				// Call Play API..

				return;
			}
		});

		ImageButton buttonPause = (ImageButton)findViewById(R.id.imageButtonPause);
		/* 监听button的事件信息 */
		buttonPause.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {

				/////////////

//            	// Call Pause API..
//            	if (PausePlay == false)
//            	{
//            		PausePlay = true;
//            	}
//            	else
//            	{
//            		PausePlay = false;
//            	}

				return;
			}
		});

		ImageButton buttonStop = (ImageButton)findViewById(R.id.imageButtonStop);
  		/* 监听button的事件信息 */
		buttonStop.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {

				/////////////

				// Call Stop API..
//            	 StartToPlay = false;

			}
		});

		ImageButton buttonStereo = (ImageButton)findViewById(R.id.ImageButtonStereo);
  		/* 监听button的事件信息 */
		buttonStereo.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {

				/////////////

				// Call Stop API..
//            	  StereoOn = 1 - StereoOn;

			}
		});

		ImageButton buttonVolume = (ImageButton)findViewById(R.id.imageButtonVolume);
		/* 监听button的事件信息 */
		buttonVolume.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {

				/////////////

				return;
			}
		});

		//OnActivityLoaded()
	}

	// 布局视频通信相关窗口
	public boolean LayerOutVideoCom()
	{
		LayoutParams ll;
		//ll = new LinearLayout.LayoutParams(PicViewWidthOrg, PicViewWidthOrg * 3 / 4);
		LinearLayout ll_right = (LinearLayout) findViewById(R.id.ll_right);
		int w = ll_right.getWidth();
		int h = w * 12 / 16;
		ll = new LayoutParams(w, h);
		if (mRenderSurfaceView != null)
		{
			mRenderSurfaceView.setLayoutParams(ll);//设置布局参数
			mRenderSurfaceView.invalidate();
			//		VideoPeer.setLayoutParams(ll);//设置布局参数
			//		VideoPeer.invalidate();
		}

		return true;
	}


	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		float x = event.getRawX();
		float y = event.getRawY();
		float w = MScore.WinWidthCur; //getWidth();
		float h = MScore.WinHeightCur; //getHeight();

		if (event.getPointerCount() == 2) {
			float x0, x1, y0, y1;
			x0 = event.getX(0);
			x1 = event.getX(1);
			y0 = event.getY(0);
			y1 = event.getY(1);

			if (x0 < w && x1 < w &&
					y0 < h && y1 < h)
			{
				MScore.dispatchTouchEvent(event);
			}
		}
		else
		{
			if (x < w && y < h)
			{
				MScore.dispatchTouchEvent(event);
			}
		}

		return super.dispatchTouchEvent(event);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		int action = event.getAction();
		switch (action) {
			case MotionEvent.ACTION_DOWN:
				//Log.i("MainActivity", "MainActivity-onTouchEvent action = action down");
				break;

			case MotionEvent.ACTION_UP:
				//Log.i("MainActivity", "MainActivity-onTouchEvent action = action up");
				break;

			default:
				break;
		}

		return super.onTouchEvent(event);
	}

	// wg add
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

//	    // Checks the orientation of the screen
//	    if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
//	        Toast.makeText(this, "横屏模式", Toast.LENGTH_SHORT).show();
//	    } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
//	        Toast.makeText(this, "竖屏模式", Toast.LENGTH_SHORT).show();
//	    }
//
//	    onConfigurationChangedflag = 1;
//
//	    // 改变view的尺寸，mGrid.getNumColumns()保持不变
//	    WindowManager m = getWindowManager();
//		Display d = m.getDefaultDisplay(); // 为获取屏幕宽、高
//		WinWidthCur = d.getWidth();
//		WinHeightCur = d.getHeight();
//		PicViewWidthCur = (int)(WinWidthCur / mGrid.getNumColumns());
//		PicViewHeightCur = (int) (PicViewWidthCur * whK);
//		if (PicViewWidthCur == 0)
//		{
//			Toast.makeText(this, "Caution, win width is 0.", Toast.LENGTH_SHORT).show();
//		}
//		mGrid.setColumnWidth(PicViewWidthCur);
//		float Scale = (float)PicViewWidthCur / PicViewWidthOrg;
//
////		lp = new GridView.LayoutParams(destPageW, destPageH);
//		for (int j = 0; j < PicViewCount; j++) {
//			pv[j].mScale = Scale;
//			lp = (GridView.LayoutParams)pv[j].getLayoutParams();
//			if (lp == null)
//			{
//				lp = new GridView.LayoutParams(PicViewWidthCur, PicViewHeightCur);
//			}
//			lp.width = PicViewWidthCur;
//			lp.height = PicViewHeightCur;
//			pv[j].setLayoutParams(lp);// 设置布局参数
//
//			pv[j].mBitmapPeerRctDst.right =  PicViewWidthCur;
//			pv[j].mBitmapPeerRctDst.bottom =  PicViewHeightCur;
//			//pv[j].zoombmp = null;
//
//			pv[j].OffsetX = 0;
//		}
////		for (int j = 0; j < PicViewCount; j++) {
////			pv[j].OffsetX = 0;
////		}
//		for (int j = 0; j < PicViewCount; j++) {
//			pv[j].postInvalidate(); //0, 0, pv[j].mBitmapPeerRctDst.right, pv[j].mBitmapPeerRctDst.bottom);
//		}

	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main_gui, menu);	// main
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		return super.onOptionsItemSelected(item);
	}


	//////////////////////// AVComSetting///////////////////////
	private static String BITRATE = "Bitrate";
	private static String FPS = "FPS";
	private static String RADIORESID = "RadioResID";
	private static String RESID = "ResID";

	private static String SERVERIP = "Server IP";
	private static String SERVERPORT = "Server Port";

	private static String REMOTEIP = "Remote IP";
	private static String REMOTEPORT = "Remote Port";
	private static String REMOTENAME = "Remote name";

	private static String LOCALIP = "Local IP";
	private static String LOCALPORT = "Local Port";
	private static String LOCALNAME = "Local name";

	private static String RADIOCAMID = "RadioCamID";

	private static String USINGIMGCUT = "Using ImgCut";
	private static String USINGHWCODE = "Using HardCoding";
	private static String ADAPTIVERES = "Adaptive Resolution";

	//private static String ENCBITRATE = "Enc Bitrate";
	//private static String CAPFPS = "Capture FPS";
	//private static String ENCFPS = "Encode FPS";

	private TextView textViewCapFps = null;
	private TextView textViewDisplayFps = null;
	private TextView textViewSendBitrate = null;
	private TextView textViewRecvBitrate = null;

	private SharedPreferences configInfo;

	private EditText bitrateEditText;
	private EditText capfps_EditText, encfps_EditText;
	private EditText RemaoteNameEditText;

	private RadioGroup codecinfo_RadioGroup;
	private RadioButton qvga_RadioButton, cif_RadioButton, vga_RadioButton, stHD_RadioButton;
	private EditText bitrate_EditText;
	private EditText fps_EditText;

	private Builder codecinfo_builder;
	private Builder networkinfo_builder;

	private EditText serverip_EditText, serverport_EditText;
	private EditText remoteip_EditText, remoteport_EditText, remotename_EditText;
	private EditText localip_EditText, localport_EditText, localname_EditText;

	private RadioGroup camera_RadioGroup;
	private RadioButton frontcamera_RadioButton, backcamera_RadioButton;
	private Builder camera_builder;

	private Builder fileTrans_builder;

	private CheckBox hwcode_CheckBox, imgCut_CheckBox, AdaptiveRes_CheckBox;

	private static TextView textViewProgressSend = null;
	private static ProgressBar progressBarSend;

	private static TextView textViewProgressRecv = null;
	private static ProgressBar progressBarRecv;

	private Button mBtnStart, mBtnStop, mBtnSetting, mBtnChooseCam;
	private AlertDialog res_dialog, cam_dialog;
	private Builder bitrate_builder, fps_builder, destip_builder, serverip_builder, remote_builder;

	private int gres_selected = 0, gcam_selected = 0;

	private static Timer mTimer;
	private static TimerTask mTimerTask, mVideoTimeTask;
	private static TimerTask mMainthreadTimerTask;
	private static Handler mHandler;
	protected static final int UPDATE_FILETRANS_TEXT = 1;
	protected static final int UPDATE_VIDEO_TEXT = 2;
	protected static final int MAINTHREADTIMER = 3;

//	public static AndroidVideoWindowImpl mWindows = null;

	///////////////////////////////////////////
	public void InitAVComCtl(){


		mBtnStart = (Button) findViewById(R.id.btn_start);
		mBtnStop = (Button) findViewById(R.id.btn_stop);
		mBtnSetting = (Button) findViewById(R.id.btn_setting);
		mBtnChooseCam = (Button) findViewById(R.id.btn_choosecam);

		onStartClickEvent();
		onStopClickEvent();
		onSettingClickEvent();
		getStatToDisplay();

		onChooseCamClickEvent();

		configInfo = getSharedPreferences("AdvideoConfig", Context.MODE_PRIVATE);
		initViewConfig();
	}

	public void initViewConfig(){

		int bitrate = configInfo.getInt(BITRATE, 100);
		//int capfps = configInfo.getInt(CAPFPS, 10);
		int encfps = configInfo.getInt(FPS, 15);

		String remoteip = configInfo.getString(REMOTEIP, "127.0.0.1");
		int remoteport = configInfo.getInt(REMOTEPORT, 30004);
		String remotename = configInfo.getString(REMOTENAME, "Ad2");

		String serverip = configInfo.getString(SERVERIP, "192.168.0.101");
		int serverport = configInfo.getInt(SERVERPORT, 9520);


		//String localip = configInfo.getString(LOCALIP, "127.0.0.1");

		String localip = getWifiIPAddress();
		int localport = configInfo.getInt(LOCALPORT, 30003);
		String localname = configInfo.getString(LOCALNAME, "Ad1");


		int iUsingImgCut = configInfo.getInt(USINGIMGCUT, 0);
		int iUsingHWCode = configInfo.getInt(USINGHWCODE, 0);
		int iAdaptiveRes = configInfo.getInt(ADAPTIVERES, 0);


//		int resid = configInfo.getInt(RESID, 1);
//		AVCom.setResolution(resid);
//
//		AVCom.setRemoteIP(remoteip);
//		AVCom.setRemotePort(remoteport);
//		AVCom.setRemoteName(remotename);
//
//		AVCom.setServerIP(serverip);
//		AVCom.setServerPort(serverport);
//
//		AVCom.setLocalIP(localip);
//		AVCom.setLocalPort(localport);
//		AVCom.setLocalName(localname);
//
//		AVCom.setUseHwcode(iUsingHWCode);
//		AVCom.setUseImgCut(iUsingImgCut);
//		AVCom.setAdaptiveRes(iAdaptiveRes);
	}

//	public void getStatDataToDisplay() {
//
//		textViewCapFps = (TextView) findViewById(R.id.textView_capFps);
//		textViewDisplayFps = (TextView) findViewById(R.id.textView_displayFps);
//		textViewSendBitrate = (TextView) findViewById(R.id.textView_sendBitrate);
//		textViewRecvBitrate = (TextView) findViewById(R.id.textView_recvBitrate);
//
//		mTimer = new Timer();
//		mHandler = new Handler() {
//			public void handleMessage(Message msg) {
//				switch (msg.what) {
//				case UPDATE_TEXT:
//
//					String capFps = "Capture FPS: " + getCapFps();
//					String displayFps = "Display FPS: " + getDisplayFps();
//					String sendBitrate = "Send Bitrate: " + getSendBitrate();
//					String rcevBitrate = "Rcev Bitrate: " + getRcevBitrate();
//
//					textViewCapFps.setText(capFps);
//					textViewDisplayFps.setText(displayFps);
//					textViewSendBitrate.setText(sendBitrate);
//					textViewRecvBitrate.setText(rcevBitrate);
//
//					textViewDisplayFps.setText(displayFps);
//					textViewSendBitrate.setText(sendBitrate);
//					textViewRecvBitrate.setText(rcevBitrate);
//
//					break;
//				}
//				super.handleMessage(msg);
//
//			}
//		};
//	}


	public void getStatToDisplay() {

		mTimer = new Timer();
		mHandler = new Handler() {
			public void handleMessage(Message msg) {
//				switch (msg.what) {
//
//				case UPDATE_VIDEO_TEXT:
//
//					int iVideoStart = AVCom.getVideoStartFlag();
//					if(iVideoStart > 0)
//						AVCom.startVideoThread(iVideoStart);
//
//					int iImgShowFlag = AVCom.getVideoDisplayFlag();
//					if(iImgShowFlag == 0)
//					{
//						Toast.makeText(PianoELearning.this, "Display error.", Toast.LENGTH_SHORT).show();
//					}
//
//					break;
//
//				case MAINTHREADTIMER:
//
//					// to be done ...
//					/* */
//					int r = 0;
//					r = AVCom.OnMainThread(); //&resChg);
//					if ((r & 3) == 1)
//					{
//						Toast.makeText(PianoELearning.this, "320x240", Toast.LENGTH_SHORT).show();
//					}
//					else if ((r & 3) == 2)
//					{
//						Toast.makeText(PianoELearning.this, "WIDTHxHEIGHT", Toast.LENGTH_SHORT).show();
//					}
//					else if ((r & 3) == 3)
//					{
//						Toast.makeText(PianoELearning.this, "Unkown resolution", Toast.LENGTH_SHORT).show();
//					}
//
//					break;
//
//				case UPDATE_FILETRANS_TEXT:
//
//					int iFileSendEndFlag = AVCom.getFileSendEndFlag();
//					int iFileSendProgValue = AVCom.getFileSendValue();
//					String sFileSendProgValue = "Send: " + iFileSendProgValue + "%";
//
//					if((iFileSendEndFlag == 0)&&(iFileSendProgValue > 0))
//					{
////						int iflag = progressBarSend.getVisibility();
////						if(iflag != View.VISIBLE)
////							progressBarSend.setVisibility(View.VISIBLE);
//
//						progressBarSend.setProgress(iFileSendProgValue);
////
////						iflag = textViewProgressSend.getVisibility();
////						if(iflag != View.VISIBLE)
////							textViewProgressSend.setVisibility(View.VISIBLE);
//
//						textViewProgressSend.setText(sFileSendProgValue);
//					}
//
//					int iFileRecvEndFlag = AVCom.getFileRecvEndFlag();
//					int iFileRecvProgValue = AVCom.getFileRecvValue();
//					String sFileRecvProgValue = "Recv: " + iFileRecvProgValue+ "%";
//
//					if((iFileRecvEndFlag == 0)&&(iFileRecvProgValue > 0))
//					{
//
////						int iflag = progressBarRecv.getVisibility();
////						if(iflag != View.VISIBLE)
////							progressBarRecv.setVisibility(View.VISIBLE);
//
//						progressBarRecv.setProgress(iFileRecvProgValue);
//
////						iflag = textViewProgressRecv.getVisibility();
////						if(iflag != View.VISIBLE)
////							textViewProgressRecv.setVisibility(View.VISIBLE);
//
//						textViewProgressRecv.setText(sFileRecvProgValue);
//					}
//
//					// Rmv by wanggeng, 閿熸枻鎷锋椂
//					/*
//					Log.i(TAG, sFileRecvProgValue + ",  "+ sFileRecvProgValue
//							+ ", iFileSendEndFlag = " + iFileSendEndFlag
//							+ ", iFileRecvEndFlag = " + iFileRecvEndFlag);
//					*/
//
//					String sFileSendProgEnd = null ;
//					String sFileRecvProgEnd = null ;
//
//					if(iFileSendEndFlag == 1)
//					{
//						if((iFileSendProgValue == 100) &&(progressBarSend.getProgress() != 0))
//						{
//							sFileSendProgEnd = "File Send OK!";
//							Toast.makeText(PianoELearning.this, sFileSendProgEnd, Toast.LENGTH_SHORT).show();
//						}
//						else if((iFileSendProgValue > 0) &&(progressBarSend.getProgress() != 0))
//						{
//							sFileSendProgEnd = "File Send Stop!";
//							Toast.makeText(PianoELearning.this, sFileSendProgEnd, Toast.LENGTH_SHORT).show();
//						}
//
//						progressBarSend.setProgress(0);
//						textViewProgressSend.setText(" ");
//					}
//
//					if(iFileRecvEndFlag == 1)
//					{
//						if((iFileRecvProgValue == 100)&&(progressBarSend.getProgress() != 0))
//						{
//							sFileRecvProgEnd = "File Recv OK!";
//							Toast.makeText(PianoELearning.this, sFileRecvProgEnd, Toast.LENGTH_SHORT).show();
//						}
//						else if((iFileRecvProgValue > 0)&&(progressBarSend.getProgress() != 0))
//						{
//							sFileRecvProgEnd = "File Recv Stop!";
//							Toast.makeText(PianoELearning.this, sFileRecvProgEnd, Toast.LENGTH_SHORT).show();
//						}
//
//						progressBarRecv.setProgress(0);
//						textViewProgressRecv.setText(" ");
//					}
//
//				break;
//
//				}
				super.handleMessage(msg);
			}
		};
	}

//	public void createMenuCodecSettingDialog() {
//
//		LayoutInflater inflaterbr = (LayoutInflater) getSystemService(this.LAYOUT_INFLATER_SERVICE);
//		View codecSetting_view = inflaterbr.inflate(R.layout.avseting_codec_setting, null);
//
//		codecinfo_RadioGroup = (RadioGroup) codecSetting_view.findViewById(R.id.radioGroup_resolution);
//		qvga_RadioButton = (RadioButton) codecSetting_view.findViewById(R.id.radio_qvga);
//		cif_RadioButton = (RadioButton) codecSetting_view.findViewById(R.id.radio_cif);
//		vga_RadioButton = (RadioButton) codecSetting_view.findViewById(R.id.radio_vga);
//		stHD_RadioButton = (RadioButton) codecSetting_view.findViewById(R.id.radio_720p);
//
//		bitrate_EditText = (EditText) codecSetting_view.findViewById(R.id.editText_bitrate);
//		fps_EditText = (EditText) codecSetting_view.findViewById(R.id.editText_fps);
//
//		hwcode_CheckBox = (CheckBox) codecSetting_view.findViewById(R.id.checkBox_hardcoding);
//		imgCut_CheckBox = (CheckBox) codecSetting_view.findViewById(R.id.checkBox_imgcuting);
//
//		AdaptiveRes_CheckBox = (CheckBox) codecSetting_view.findViewById(R.id.CheckBox_AdaptiveRes);
//
//		int bitrate = configInfo.getInt(BITRATE, 100);
//		bitrate_EditText.setText("" + bitrate);
//
//		int fps = configInfo.getInt(FPS, 15);
//		fps_EditText.setText("" + fps);
//
//
//		int radioid = configInfo.getInt(RADIORESID, R.id.radio_qvga);
//		codecinfo_RadioGroup.check(radioid);
//
//		int resid = configInfo.getInt(RESID, 1);
//		AVCom.setResolution(resid);
//
//		codecinfo_RadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener(){
//			@Override
//			public void onCheckedChanged(RadioGroup arg0,
//					int checkedId) {
//				// TODO Auto-generated method stub
//
//				if (checkedId == qvga_RadioButton.getId()) {
//					Toast.makeText(PianoELearning.this,"QVGA selected",Toast.LENGTH_SHORT).show();
//					//setSelectRes(1);
//					AVCom.setResolution(1);
//					configInfo.edit().putInt(RADIORESID, R.id.radio_qvga).commit();
//					configInfo.edit().putInt(RESID, 1).commit();
//				}
//
//				if (checkedId == cif_RadioButton.getId()) {
//					Toast.makeText(PianoELearning.this,"CIF selected",Toast.LENGTH_SHORT).show();
//					//setSelectRes(2);
//					AVCom.setResolution(2);
//					configInfo.edit().putInt(RADIORESID, R.id.radio_cif).commit();
//					configInfo.edit().putInt(RESID, 2).commit();
//				}
//
//				if (checkedId == vga_RadioButton.getId()) {
//					Toast.makeText(PianoELearning.this,"VGA selected",Toast.LENGTH_SHORT).show();
//					//setSelectRes(3);
//					AVCom.setResolution(3);
//					configInfo.edit().putInt(RADIORESID, R.id.radio_vga).commit();
//					configInfo.edit().putInt(RESID, 3).commit();
//				}
//
//				if (checkedId == stHD_RadioButton.getId()) {
//					Toast.makeText(PianoELearning.this,"720P selected",Toast.LENGTH_SHORT).show();
//					//setSelectRes(4);
//					AVCom.setResolution(4);
//					configInfo.edit().putInt(RADIORESID, R.id.radio_720p).commit();
//					configInfo.edit().putInt(RESID, 4).commit();
//				}
//			}
//		});
//
//		int iHwCode = configInfo.getInt(USINGHWCODE, 0);
//		if(iHwCode == 1)
//			hwcode_CheckBox.setChecked(true);
//		else
//			hwcode_CheckBox.setChecked(false);
//
//		hwcode_CheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                // TODO Auto-generated method stub
//            	if(isChecked){
//            		Toast.makeText(PianoELearning.this,"Using Hard-Coding",Toast.LENGTH_SHORT).show();
//            		AVCom.setUseHwcode(1);
//            		configInfo.edit().putInt(USINGHWCODE, 1).commit();
//            	}else{
//            		AVCom.setUseHwcode(0);
//            		configInfo.edit().putInt(USINGHWCODE, 0).commit();
//            	}
//            }
//        });
//
//		int iCutImg = configInfo.getInt(USINGIMGCUT, 0);
//		if(iCutImg == 1)
//			imgCut_CheckBox.setChecked(true);
//		else
//			imgCut_CheckBox.setChecked(false);
//
//		imgCut_CheckBox.setOnCheckedChangeListener(new android.widget.CompoundButton.OnCheckedChangeListener(){
//
//			@Override
//			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//				// TODO Auto-generated method stub
//            	if(isChecked){
//            		Toast.makeText(PianoELearning.this,"Using ImgCut, Res > QVGA",Toast.LENGTH_SHORT).show();
//            		AVCom.setUseImgCut(1);
//            		configInfo.edit().putInt(USINGIMGCUT, 1).commit();
//            	}else{
//            		AVCom.setUseImgCut(0);
//            		configInfo.edit().putInt(USINGIMGCUT, 0).commit();
//            	}
//			}});
//
//		// adpative resolution
//		int iAdaptRes = configInfo.getInt(ADAPTIVERES, 0);
//		if(iAdaptRes == 1)
//			AdaptiveRes_CheckBox.setChecked(true);
//		else
//			AdaptiveRes_CheckBox.setChecked(false);
//		AdaptiveRes_CheckBox.setOnCheckedChangeListener(new android.widget.CompoundButton.OnCheckedChangeListener(){
//			@Override
//			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//				// TODO Auto-generated method stub
//            	if(isChecked){
//            		Toast.makeText(PianoELearning.this,"Appying Adaptive Resolution",Toast.LENGTH_SHORT).show();
//            		AVCom.setAdaptiveRes(1);
//            		configInfo.edit().putInt(ADAPTIVERES, 1).commit();
//            	}else{
//            		AVCom.setAdaptiveRes(0);
//            		configInfo.edit().putInt(ADAPTIVERES, 0).commit();
//            	}
//			}});
//
//		//Log.i(TAG, "createMenuCodecSettingDialog: codecinfo_builder");
//		codecinfo_builder = new AlertDialog.Builder(this)
//				.setTitle("Codec Setting").setView(codecSetting_view)
//				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//
//					@Override
//					public void onClick(DialogInterface arg0, int arg1) {
//						// TODO Auto-generated method stub
//
//						//Log.i(TAG, "createMenuCodecSettingDialog: codecinfo_builder onClick");
//
//						String sbitrate = bitrate_EditText.getText().toString().trim();
//
//						int encbitrate = Integer.parseInt(sbitrate);
//						configInfo.edit().putInt(BITRATE, encbitrate).commit();
//
//						Log.i(TAG, "createMenuCodecSettingDialog  encbitrate: "+ encbitrate);
//
//						if (sbitrate.length() != 0) {
//							AVCom.setEncBitrate(encbitrate);
//						} else
//							AVCom.setEncBitrate(0);
//
//						String sfps = fps_EditText.getText().toString().trim();
//
//						int ifps = Integer.parseInt(sfps);
//						configInfo.edit().putInt(FPS, ifps).commit();
//
//						Log.i(TAG, "createMenuCodecSettingDialog  ifps: "+ ifps);
//
//						if (sfps.length() != 0) {
//							AVCom.setEncFps(ifps);
//						} else
//							AVCom.setEncFps(0);
//
////						int resindex = getSelectRes();
////						if(resindex > 0)
////							setResolution(resindex);
//					}
//				});
//	}
//
//	public void createMenuNetworkSettingDialog(){
//
//		//Log.i(TAG, "Enter createMenuNetworkSettingDialog");
//
//		LayoutInflater inflaterbr = (LayoutInflater) getSystemService(this.LAYOUT_INFLATER_SERVICE);
//		View netinfoSetting_view = inflaterbr.inflate(R.layout.avseting_network_setting, null);
//
//		serverip_EditText = (EditText) netinfoSetting_view.findViewById(R.id.editText_serverip);
//		serverport_EditText = (EditText) netinfoSetting_view.findViewById(R.id.editText_serverport);
//
//		remoteip_EditText = (EditText) netinfoSetting_view.findViewById(R.id.editText_remoteip);
//		remoteport_EditText = (EditText) netinfoSetting_view.findViewById(R.id.editText_remoteport);
//		remotename_EditText = (EditText) netinfoSetting_view.findViewById(R.id.editText_remotename);
//
//		localip_EditText = (EditText) netinfoSetting_view.findViewById(R.id.editText_localip);
//		localport_EditText = (EditText) netinfoSetting_view.findViewById(R.id.editText_localport);
//		localname_EditText = (EditText) netinfoSetting_view.findViewById(R.id.editText_localname);
//
//		//
//		String serverip = configInfo.getString(SERVERIP, "192.168.0.101");
//		int serverport = configInfo.getInt(SERVERPORT, 9520);
//
//		serverip_EditText.setText(serverip);
//		serverport_EditText.setText("" + serverport);
//
//		//
//		String remoteip = configInfo.getString(REMOTEIP, "127.0.0.1");
//		int remoteport = configInfo.getInt(REMOTEPORT, 50000);
//		String remotename = configInfo.getString(REMOTENAME, "AD2");
//
//		remoteip_EditText.setText(remoteip);
//		remoteport_EditText.setText("" + remoteport);
//		remotename_EditText.setText(remotename);
//
//		//
//		//String localip = configInfo.getString(LOCALIP, "127.0.0.1");
//		//String localip = configInfo.getString(LOCALIP, getWifiIPAddress());
//
//		String localip = getWifiIPAddress();
//
//		int localport = configInfo.getInt(LOCALPORT, 50000);
//		String localname = configInfo.getString(LOCALNAME, "AD1");
//
//		localip_EditText.setText(localip);
//		localport_EditText.setText("" + localport);
//		localname_EditText.setText(localname);
//
//		//Log.i(TAG, "createMenuNetworkSettingDialog  networkinfo_builder");
//		networkinfo_builder = new AlertDialog.Builder(this)
//				.setTitle("Network Setting").setView(netinfoSetting_view)
//				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//
//					@Override
//					public void onClick(DialogInterface arg0, int arg1) {
//						// TODO Auto-generated method stub
//
//						String serveripvalue = serverip_EditText.getText().toString().trim();
//						String serverportvalue = serverport_EditText.getText().toString().trim();
//
//						// Log.d(TAG, "destportvalue: " + destportvalue);
//						int iserverportvalue = Integer.parseInt(serverportvalue);
//
//						configInfo.edit().putString(SERVERIP, serveripvalue).commit();
//						configInfo.edit().putInt(SERVERPORT, iserverportvalue).commit();
//
//						if (serveripvalue.length() != 0)
//							AVCom.setServerIP(serveripvalue);
//						else
//							AVCom.setServerIP(null);
//
//						if (serverportvalue.length() != 0)
//							AVCom.setServerPort(iserverportvalue);
//						else
//							AVCom.setServerPort(0);
//
//						String remoteipvalue = remoteip_EditText.getText().toString().trim();
//						String remoteportvalue = remoteport_EditText.getText().toString().trim();
//						String remoteNamevalue = remotename_EditText.getText().toString().trim();
//
//						// Log.d(TAG, "destportvalue: " + destportvalue);
//						int iremoteportvalue = Integer.parseInt(remoteportvalue);
//
//						configInfo.edit().putString(REMOTEIP, remoteipvalue).commit();
//						configInfo.edit().putInt(REMOTEPORT, iremoteportvalue).commit();
//						configInfo.edit().putString(REMOTENAME, remoteNamevalue).commit();
//
//						if (remoteipvalue.length() != 0)
//							AVCom.setRemoteIP(remoteipvalue);
//
//						if (remoteportvalue.length() != 0)
//							AVCom.setRemotePort(iremoteportvalue);
//
//						if (remoteNamevalue.length() != 0)
//							AVCom.setRemoteName(remoteNamevalue);
//
//
//						String localipvalue = localip_EditText.getText().toString().trim();
//						String localportvalue = localport_EditText.getText().toString().trim();
//						String localNamevalue = localname_EditText.getText().toString().trim();
//
//						// Log.d(TAG, "destportvalue: " + destportvalue);
//						int ilocalportvalue = Integer.parseInt(remoteportvalue);
//
//						//configInfo.edit().putString(LOCALIP, localipvalue).commit();
//						configInfo.edit().putInt(LOCALPORT, ilocalportvalue).commit();
//						configInfo.edit().putString(LOCALNAME, localNamevalue).commit();
//
//						if (localipvalue.length() != 0)
//							AVCom.setLocalIP(localipvalue);
//
//						if (localportvalue.length() != 0)
//							AVCom.setLocalPort(ilocalportvalue);
//
//
//						if (localNamevalue.length() != 0)
//							AVCom.setLocalName(localNamevalue);
//					}
//
//				});
//		//Log.i(TAG, "Leave createMenuNetworkSettingDialog");
//	}
//
//	public void createMenuCameraSettingDialog(){
//
//		LayoutInflater inflaterbr = (LayoutInflater) getSystemService(this.LAYOUT_INFLATER_SERVICE);
//		View cameraSetting_view = inflaterbr.inflate(R.layout.avseting_camera_setting, null);
//
//		camera_RadioGroup = (RadioGroup) cameraSetting_view.findViewById(R.id.radioGroup_camersetting);
//		frontcamera_RadioButton = (RadioButton) cameraSetting_view.findViewById(R.id.radio_frontcamera);
//		backcamera_RadioButton = (RadioButton) cameraSetting_view.findViewById(R.id.radio_backcamera);
//
//		int radiocamid = configInfo.getInt(RADIOCAMID, R.id.radio_frontcamera);
//		camera_RadioGroup.check(radiocamid);
//
//		camera_RadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener(){
//			@Override
//			public void onCheckedChanged(RadioGroup arg0,
//					int checkedId) {
//				// TODO Auto-generated method stub
//
//				if (checkedId == frontcamera_RadioButton.getId()) {
//					Toast.makeText(PianoELearning.this,"Front-facing Camera Selected",Toast.LENGTH_SHORT).show();
//
//					//setSelectCam(1);
//					AVCom.setCamID(1);
//					configInfo.edit().putInt(RADIOCAMID, R.id.radio_frontcamera).commit();
//				}
//
//				if (checkedId == backcamera_RadioButton.getId()) {
//					Toast.makeText(PianoELearning.this,"Back-facing Camera Selected",Toast.LENGTH_SHORT).show();
//
//					//setSelectCam(0);
//
//					AVCom.setCamID(0);
//					configInfo.edit().putInt(RADIOCAMID, R.id.radio_backcamera).commit();
//				}
//			}
//		});
//
//		camera_builder = new AlertDialog.Builder(this)
//			.setTitle("Camera Setting").setView(cameraSetting_view)
//			.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//
//			@Override
//			public void onClick(DialogInterface arg0, int arg1) {
//				// TODO Auto-generated method stub
//				//setCamID(getSelectCam());
//			}
//		});
//
//	}
//
//	public void createMenuFileSeleteSettingDialog(){
//
//		//LayoutInflater inflaterbr = (LayoutInflater) MainGUI.this.getSystemService(LAYOUT_INFLATER_SERVICE);
//		//View cameraSetting_view = inflaterbr.inflate(R.layout.camera_setting, null);
//
//		fileTrans_builder = new AlertDialog.Builder(this)
//			.setTitle("File Selecting")
//			.setView(new FileSelectView(this, 0, new CallbackBundle(){
//
//				@Override
//				public void callback(Bundle bundle) {
//					// TODO Auto-generated method stub
//					String filepath = bundle.getString("path");
//
//					Log.i(TAG, "createMenuFileSeleteSettingDialog: "+filepath);
//
//					AVCom.setSendFileName(filepath);
//				}}))
//			.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//
//			@Override
//			public void onClick(DialogInterface arg0, int arg1) {
//				// TODO Auto-generated method stub
//				//setCamID(getSelectCam());
//
//				initProgressBar(0);
//				//setProgressBar(0, 0);
//				AVCom.startFileSend();
//			}
//		});
//	}
//	public static  void initProgressBar(int type){
//		if(type == 0){
//			progressBarSend.setVisibility(View.VISIBLE);
//			progressBarSend.setMax(100);
//			progressBarSend.setProgress(0);
//			textViewProgressSend.setVisibility(View.VISIBLE);
//			textViewProgressSend.setText("Send:"+ 0+"%");
//		}
//
//		if(type == 1){
//			progressBarRecv.setVisibility(View.VISIBLE);
//			progressBarRecv.setMax(100);
//			progressBarRecv.setProgress(0);
//			textViewProgressRecv.setVisibility(View.VISIBLE);
//			textViewProgressRecv.setText("Recv:"+ 0+"%");
//		}
//	}
//
//	public static void setProgressBar(int type, int value){
//
//		if(type == 0){
//
//			if(textViewProgressSend.getVisibility() != View.VISIBLE)
//				textViewProgressSend.setVisibility(View.VISIBLE);
//
//			if(progressBarSend.getVisibility() == View.VISIBLE)
//				progressBarSend.setVisibility(View.VISIBLE);
//
//			progressBarSend.setProgress(value);
//			textViewProgressSend.setText("Send: "+value+"%");
//
//			//Log.i(TAG, "setProgressBar: type0 , value:"+value);
//		}
//		if(type == 1){
//
//			if(textViewProgressRecv.getVisibility() != View.VISIBLE)
//				textViewProgressRecv.setVisibility(View.VISIBLE);
//
//			if(progressBarRecv.getVisibility() != View.VISIBLE)
//				progressBarRecv.setVisibility(View.VISIBLE);
//
//			progressBarRecv.setProgress(value);
//			textViewProgressRecv.setText("Recv: "+value+"%");
//
//			//Log.i(TAG, "setProgressBar: type1 , value:"+value);
//		}
//	}
//
//	public static void stopProgressBar(int type){
//		if(type == 0){
//			textViewProgressSend.setText("");
//			progressBarSend.setVisibility(View.GONE);
//
//			Log.i(TAG, "stopProgressBar: type0 ");
//			//Toast.makeText(this, "File Send OK!", Toast.LENGTH_SHORT).show();;
//		}
//		if(type == 1){
//			textViewProgressRecv.setText("");
//			progressBarRecv.setVisibility(View.GONE);;
//
//
//			Log.i(TAG, "stopProgressBar: type1 ");
//			//Toast.makeText(this, "File Recv OK!", Toast.LENGTH_SHORT).show();
//		}
//	}
//
//	public void setSelectRes(int index) {
//		gres_selected = index;
//
//		//Log.i(TAG, "setSelectRes:  "+ index);
//	}
//
//	public int getSelectRes() {
//		return gres_selected;
//
//	}
//
//	public void setSelectCam(int index) {
//		gcam_selected = index;
//	}
//
//	public int getSelectCam() {
//		return gcam_selected;
//
//	}

	@Override protected
	void onResume() {
		super.onResume();

//		mTimerTask = new TimerTask() {
//			@Override
//			public void run() {
//				Message message = new Message();
//				message.what = UPDATE_FILETRANS_TEXT;
//				mHandler.sendMessage(message);
//				// mHandler.sendEmptyMessage(UPDATE_TEXT);
//
//	 		}
//		};
//
//		mVideoTimeTask  = new TimerTask() {
//
//			@Override
//			public void run() {
//				// TODO Auto-generated method stub
//
//				Message message = new Message();
//				message.what = UPDATE_VIDEO_TEXT;
//				mHandler.sendMessage(message);
//				// mHandler.sendEmptyMessage(UP
//
//			}
//
//		};
//
//		mMainthreadTimerTask  = new TimerTask() {
//
//			@Override
//			public void run() {
//				// TODO Auto-generated method stub
//
//				Message message = new Message();
//				message.what = MAINTHREADTIMER;
//				mHandler.sendMessage(message);
//			}
//		};
//
//	 	mTimer.schedule(mTimerTask, 1000, 500);
//	 	mTimer.schedule(mVideoTimeTask, 1000, 50);
//	 	mTimer.schedule(mMainthreadTimerTask, 1000, 50);
	}

	//////////////////////// AVComSetting///////////////////////

	//////////////////////// EncCtl ////////////////////////
	//
	//
	//////////////////////// EncCtl ////////////////////////

	//////////////////////// DecCtl ////////////////////////
	//
	////////////////////////video decode output ////////////////////////


	public void onPlayStart(){

	}



	public class ISimplePlayer {

		public void onPlayStart(){

		}
	}


	/////////////////////////// native camera /////////////////
	//
	private Camera mCamera;// Camera对象
	private Button mButton, mStopButton;// 右侧条框，点击出发保存图像（拍照）的事件
	//    private SurfaceView video_capture_surface_nativecam;// 显示图像的surfaceView
	private SurfaceHolder holder;// SurfaceView的控制器
	//    private AutoFocusCallback mAutoFocusCallback = new AutoFocusCallback();// AutoFocusCallback自动对焦的回调对象
	private ImageView sendImageIv;// 发送图片的imageview，位于右侧条框

	private String strCaptureFilePath = Environment
			.getExternalStorageDirectory() + "/DCIM/Camera/";// 保存图像的路径

	private static final int IMAGE_FORMAT = ImageFormat.NV21; // YV12;

	NativeCam nativeCam;

	public void onCreateCamera() {

		if (checkCameraHardware(this)) {
			Log.e("============", "摄像头存在");// 验证摄像头是否存在
		}

        /* SurfaceHolder设置 */
		mPreviewSurfaceView = (SurfaceView) findViewById(R.id.video_capture_surface_nativecam);
		holder = mPreviewSurfaceView.getHolder();
		holder.addCallback(this);
		// holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        /* 设置拍照Button的OnClick事件处理 */
		int w = 640;
		int h = 480;
//        w = 320;
//        h = 240;
		nativeCam = new NativeCam(this, mPreviewSurfaceView, holder, 0, w, h, null, avcom);
		//nativeCam = new NativeCam(this, mPreviewSurfaceView, holder, 1, w, h, null, avcom);
	}

	// ///////----------重写SurfaceHolder.Callback接口的方法，
	// 在创建面板的时候调用的方法
	@Override
	public void surfaceCreated(SurfaceHolder surfaceholder) {

		if (nativeCam != null)
		{
			//nativeCam.OnCreateNatvieCamVideoView();
		}

//        try {
//            mCamera = null;
//            try {
//                mCamera = Camera.open(0);//打开相机；在低版本里，只有open（）方法；高级版本加入此方法的意义是具有打开多个
//                //摄像机的能力，其中输入参数为摄像机的编号
//                //在manifest中设定的最小版本会影响这里方法的调用，如果最小版本设定有误（版本过低），在ide里将不允许调用有参的
//                //open方法;
//                //如果模拟器版本较高的话，无参的open方法将会获得null值!所以尽量使用通用版本的模拟器和API；
//            } catch (Exception e) {
//                Log.e("============", "摄像头被占用");
//            }
//            if (mCamera == null) {
//                Log.e("============", "摄像机为空");
//                System.exit(0);
//            }
//
//            Camera.Parameters parameters = mCamera.getParameters();
////            parameters.setPictureSize(1024, 768);
//            parameters.setPictureSize(640, 480);
////            parameters.setPreviewSize(640, 480);	// 此句导致itop4418摄像头预览花屏
//            parameters.setPreviewFormat(IMAGE_FORMAT); // setting preview format：YV12
//            mCamera.setParameters(parameters);
//
//            mCamera.setPreviewDisplay(holder);//设置显示面板控制器
//            priviewCallBack pre = new priviewCallBack();//建立预览回调对象
//            mCamera.setPreviewCallback(pre); //设置预览回调对象
//            //mCamera.getParameters().setPreviewFormat(ImageFormat.JPEG);
//            mCamera.startPreview();//开始预览，这步操作很重要
//        } catch (IOException exception) {
//            mCamera.release();
//            mCamera = null;
//        }

	}

	// 在面板改变的时候调用的方法
	@Override
	public void surfaceChanged(SurfaceHolder surfaceholder, int format, int w,
							   int h) {
        /* 相机初始化 */
//        initCamera();

//    	if (nativeCam != null)
//    	{
//    		nativeCam.RestartCamera();
//    	}
	}

	// 销毁面板时的方法
	@Override
	public void surfaceDestroyed(SurfaceHolder surfaceholder) {
		if (mCamera != null)
		{
			onDestroyCamera();
		}
	}

	//@Override
	public void onDestroyCamera()
	{
		if (mCamera != null)
		{
			stopCamera();

			mCamera.setPreviewCallback(null);

			mCamera.release();
			mCamera = null;
		}

		//super.onDestroy();
	}

	/* 拍照的method */
	private void takePicture() {
		if (mCamera != null) {
			mCamera.takePicture(shutterCallback, rawCallback, jpegCallback);
		}
	}

	private ShutterCallback shutterCallback = new ShutterCallback() {
		public void onShutter() {
            /* 按下快门瞬间会调用这里的程序 */
		}
	};

	private PictureCallback rawCallback = new PictureCallback() {
		public void onPictureTaken(byte[] _data, Camera _camera) {
            /* 要处理raw data?写?否 */
		}
	};

	//在takepicture中调用的回调方法之一，接收jpeg格式的图像
	private PictureCallback jpegCallback = new PictureCallback() {
		public void onPictureTaken(byte[] _data, Camera _camera) {

            /*
             * if (Environment.getExternalStorageState().equals(
             * Environment.MEDIA_MOUNTED)) // 判断SD卡是否存在，并且可以可以读写 {
             *
             * } else { Toast.makeText(EX07_16.this, "SD卡不存在或写保护",
             * Toast.LENGTH_LONG) .show(); }
             */
			// Log.w("============", _data[55] + "");

			try {
                /* 取得相片 */
				Bitmap bm = BitmapFactory.decodeByteArray(_data, 0,
						_data.length);

                /* 创建文件 */
				File myCaptureFile = new File(strCaptureFilePath, "1.jpg");
				BufferedOutputStream bos = new BufferedOutputStream(
						new FileOutputStream(myCaptureFile));
                /* 采用压缩转档方法 */
				bm.compress(Bitmap.CompressFormat.JPEG, 100, bos);

                /* 调用flush()方法，更新BufferStream */
				bos.flush();

                /* 结束OutputStream */
				bos.close();

                /* 让相片显示3秒后圳重设相机 */
				// Thread.sleep(2000);
                /* 重新设定Camera */
				stopCamera();
				initCamera();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	/* 自定义class AutoFocusCallback */
	public final class AutoFocusCallback implements
			Camera.AutoFocusCallback {
		public void onAutoFocus(boolean focused, Camera camera) {

            /* 对到焦点拍照 */
			if (focused) {
				takePicture();
			}
		}
	};

	/* 相机初始化的method */
	private void initCamera() {

		if (mCamera != null)
		{
			stopCamera();
			//mCamera.release();
			//mCamera = null;

			try {
				Camera.Parameters parameters = mCamera.getParameters();
                /*
                 * 设定相片大小为1024*768， 格式为JPG
                 */
				// parameters.setPictureFormat(PixelFormat.JPEG);

//                parameters.setPictureSize(1024, 768);
				parameters.setPictureSize(640, 480);
//                parameters.setPreviewSize(640, 480);
				parameters.setPreviewFormat(IMAGE_FORMAT); // setting preview format：YV12
				mCamera.setParameters(parameters);

                /* 打开预览画面 */
				mCamera.startPreview();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

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
	private boolean checkCameraHardware(Context context) {
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

			if (mCamera == null)
			{
				return;
			}

			Size size = mCamera.getParameters().getPreviewSize();
			Log.w("支持格式", mCamera.getParameters().getPreviewFormat()+
					", w: " + size.width +
					", h: " + size.height);

			//decodeToBitMap(data, camera);

		}
	}

	public void decodeToBitMap(byte[] data, Camera _camera) {
		Size size = mCamera.getParameters().getPreviewSize();
		try {
			YuvImage image = new YuvImage(data, ImageFormat.NV21, size.width,
					size.height, null);
			Log.w("wwwwwwwww", size.width + " " + size.height);
			if (image != null) {
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				image.compressToJpeg(new Rect(0, 0, size.width, size.height),
						80, stream);
				Bitmap bmp = BitmapFactory.decodeByteArray(
						stream.toByteArray(), 0, stream.size());
				Log.w("wwwwwwwww", bmp.getWidth() + " " + bmp.getHeight());
				Log.w("wwwwwwwww",
						(bmp.getPixel(100, 100) & 0xff) + "  "
								+ ((bmp.getPixel(100, 100) >> 8) & 0xff) + "  "
								+ ((bmp.getPixel(100, 100) >> 16) & 0xff));

				stream.close();
			}
		} catch (Exception ex) {
			Log.e("Sys", "Error:" + ex.getMessage());
		}
	}
	//
	/////////////////////////// native camera /////////////////
}

package com.epiano.av.ictvoip.androidvideo.capture;

import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.huawei.AudioDeviceAndroid;
import com.huawei.AudioDeviceAndroidService;

@SuppressWarnings("JniMissingFunction")
public class AudioVideo extends View {

//	// peer video
//	public static SurfaceView mPeerSurfaceView = null;
//	// local video
//	public static SurfaceView mPreviewSurfaceView = null;  
//	public static ImageView mPreviewUsbCamView = null;  
//
//	public static AndroidVideoWindowImpl mWindows = null; 
 
	// Camera
	private Camera mCamera;

	public boolean statThreadRuning;
 
	private static String TAG = "Video";
	
	
	
	Context mContext;
	
	private LinearLayout ll;
	
	//static private int openfileDialogId = 0;
	
	// 在 系统完成activity加载后，执行与窗口相关的操作
	public void OnActivityLoaded()
	{
		int i = 0;
	}

//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
	public AudioVideo(Context context, AttributeSet attrs, LinearLayout llIn, 
			SurfaceView PreviewSurfaceView, 
			SurfaceView PeerSurfaceView, 
			ImageView PreviewUsbCamView,
			Button BtnStart, 
			Button BtnStop,
			Button BtnSetting,
			ProgressBar progressBarSendIn, 
			TextView textViewProgressSendIn,
			ProgressBar progressBarRecvIn,
			TextView textViewProgressRecvIn) //, int screenWidth, int screenHeight)
	{
		super(context, attrs);
		
		mContext = context;
		
		ll = llIn;
		
//		mPeerSurfaceView = PeerSurfaceView;
//		mPreviewSurfaceView = PreviewSurfaceView;
//		mPreviewUsbCamView = PreviewUsbCamView;
//		mBtnStart = BtnStart;
//		mBtnStop = BtnStop;
//		mBtnSetting = BtnSetting;
//		progressBarSend=progressBarSendIn;
//		textViewProgressSend = textViewProgressSendIn;		
//		progressBarRecv=progressBarRecvIn;
//		textViewProgressRecv = textViewProgressRecvIn;
		
//		super.onCreate(savedInstanceState);

//		requestWindowFeature(Window.FEATURE_NO_TITLE);
//		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//				WindowManager.LayoutParams.FLAG_FULLSCREEN);
//		
//		//
//      this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//
//		setContentView(R.layout.avseting_activity_main_gui);
//		this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		

		// wg add
		loadlib();

	    Log.i(TAG,"Product Model 1: " + android.os.Build.MODEL + ","
	    		+ android.os.Build.VERSION.SDK
	    		+ android.os.Build.VERSION.RELEASE);
				
		String mode = android.os.Build.MODEL;	
		String newmode = null;
		
		if(mode.contains(" "))
			newmode = mode.replace(" ", "_");
		else
			newmode = mode;
			
		setPhoneMode(newmode);
		
		// wg rmv
//		Context context = mContext;
	    Intent intent = new Intent(context, AudioDeviceAndroidService.class);
	    context.startService(intent);
	    AudioDeviceAndroid.SetContext(context);
	    
		init();	

		//setLocalIP(getWifiIPAddress());


		
		initSocket();
	}	
	

	
	
 
	

	


	private void onSettingClickEvent() {
//		// TODO Auto-generated method stub
//		mBtnSetting.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View arg0) {
//
////				Log.d(TAG, "onSettingClickEvent");
////				openOptionsMenu();
//				Configuration config = getResources().getConfiguration();
//			    if ((config.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) > Configuration.SCREENLAYOUT_SIZE_LARGE) {
//			        int originalScreenLayout = config.screenLayout;
//			        config.screenLayout = Configuration.SCREENLAYOUT_SIZE_LARGE;
//			        openOptionsMenu();
//			        config.screenLayout = originalScreenLayout;
//			    } else {
//			        openOptionsMenu();
//			    }
//
//			}
//		});
	}



//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// Inflate the menu; this adds items to the action bar if it is present.
//
//		getMenuInflater().inflate(R.menu.main_gui, menu);
//		return true;
//	}

//	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		return true;
	}



	//
	public native void setwindows(Object PeerSurfaceView, Object PreviewSurfaceView); // 被下面两个接口替代
	public native void setPreviewWindow(Object PreviewSurfaceView);
	public native void setPeerViewWindow(Object PeerSurfaceView);
	
	public native void setPreviewView(Object mWinViewID);
	public native void setVideoWinImpl(Object mWinImpID);

	//
	public native void init();

	//
	public native void start();
	public native void startAudio();	
	public native void muteAudio();	
	public native void unMuteAudio();
	public native void startVideo();	
	public native void startAV();	
	public native void stop();	
	
	//
	public native void setCamID(int index);
	//
	public native void swichcam();    
	
	//
 	public native void setPhoneMode(String mode); 	
	
 	//
	public native void setEncFps(int fps);
	public native void setResolution(int index);
	public native void setEncBitrate(int bitrate);	
	
	public native void setUseHwcode(int flag);
	public native void setUseImgCut(int flag);

	public native void setAdaptiveRes(int flag);

 	//
 	public native void setSendFileName(String filepath);  
 	public native void startFileSend();  	
 	public native int getFileSendValue();  	
 	public native int getFileRecvValue(); 	
 	public native int getFileSendEndFlag();  	
 	public native int getFileRecvEndFlag();

 	//
	public native void setServerIP(String destip);
	public native void setServerPort(int port);	
	
	public native void setRemoteIP(String destip);
	public native void setRemotePort(int port);
	public native void setRemoteName(String name);
	
	
 	public native void setLocalIP(String ipAdress); 	
	public native void setLocalPort(int port);
	public native void setLocalName(String name);	
	
	public native void initSocket();
	
	
	//
 	public native int getVideoStartFlag(); 
	public native void startVideoThread(int flag);	
	
	public native int getVideoDisplayFlag();

	// wg add
	public native int OnMainThread();
	
//	static {
	public void loadlib(){

		System.loadLibrary("ICTIntTest");

		System.loadLibrary("IctSqlite3");
		System.loadLibrary("IctDb");
		System.loadLibrary("IctNetClip");
		
		System.loadLibrary("hwdecode");	
		System.loadLibrary("hwencode");
		System.loadLibrary("ffmpegdecode");	
		
		System.loadLibrary("VideoJB");
		
		System.loadLibrary("advideoctrl");	
			
		System.loadLibrary("HME-Audio");	
		System.loadLibrary("IctStream");		
		System.loadLibrary("IctFec");		
		System.loadLibrary("ICTFileTrans");		
		System.loadLibrary("ICTVoip_Trans_jni");	
	
		System.loadLibrary("advideo");		

	}

}

package com.epiano.av.ictvoip.androidvideo.capture;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.epiano.av.ictvoip.androidvideo.display.AndroidVideoWindowImpl;
import com.epiano.av.ictvoip.androidvideo.utils.CallbackBundle;
import com.epiano.av.ictvoip.androidvideo.utils.FileSelectView;
import com.epiano.commutil.R;
import com.huawei.AudioDeviceAndroid;
import com.huawei.AudioDeviceAndroidService;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.Timer;
import java.util.TimerTask;

@SuppressWarnings("JniMissingFunction")
public class MainGUI extends Activity {

	// 显示peer视频的窗口
	public static SurfaceView mRenderSurfaceView = null;
	// 显示本端视频的窗口
	public static SurfaceView mPreviewSurfaceView = null;  

	public static AndroidVideoWindowImpl mWindows = null;
 
	// Camera
	private Camera mCamera;

	private boolean statThreadRuning;
 
	private static String TAG = "Video";
	
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
		
	private Button mBtnStart, mBtnStop, mBtnSetting;
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
	
	//static private int openfileDialogId = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		//设置为横屏
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

		setContentView(R.layout.avseting_activity_main_gui);
		this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		

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
		Context context = getApplicationContext();
	    Intent intent = new Intent(context, AudioDeviceAndroidService.class);
	    context.startService(intent);
	    AudioDeviceAndroid.SetContext(context);
	    
		init();	

		setLocalIP(getWifiIPAddress());

		int screenWidth = getWindowManager().getDefaultDisplay().getWidth();
		int screenHeight = getWindowManager().getDefaultDisplay().getHeight();

		Log.e(TAG, "getDefaultDisplay   screenWidth=" + screenWidth
				+ "; screenHeight=" + screenHeight);

		// 閸掓繂顬婇崠鏈rfaceView
		mPreviewSurfaceView = (SurfaceView) findViewById(R.id.video_capture_surface);
		mRenderSurfaceView = (SurfaceView) findViewById(R.id.video_surface);
		fixZOrder(mRenderSurfaceView, mPreviewSurfaceView);

		mWindows = new AndroidVideoWindowImpl(mRenderSurfaceView,
				//null//
				mPreviewSurfaceView
				);
		mWindows.setListener(new AndroidVideoWindowImpl.VideoWindowListener() {
			public void onVideoRenderingSurfaceReady(AndroidVideoWindowImpl vw,
					SurfaceView surface) {
				setVideoWinImpl(vw);
				mRenderSurfaceView = surface;
			}

			public void onVideoRenderingSurfaceDestroyed(
					AndroidVideoWindowImpl vw) {
				setVideoWinImpl(null);
			}

			public void onVideoPreviewSurfaceReady(AndroidVideoWindowImpl vw,
					SurfaceView surface) {
				mPreviewSurfaceView = surface;
				setPreviewView(mPreviewSurfaceView);
			}

			public void onVideoPreviewSurfaceDestroyed(AndroidVideoWindowImpl vw) {
				// Remove references kept in jni code and restart camera
				setPreviewView(null);
			}
		});

		mWindows.init();

		configInfo = getSharedPreferences("AdvideoConfig", Context.MODE_PRIVATE);

		mBtnStart = (Button) findViewById(R.id.btn_start);
		mBtnStop = (Button) findViewById(R.id.btn_stop);
		mBtnSetting = (Button) findViewById(R.id.btn_setting);

		progressBarSend=(ProgressBar) findViewById(R.id.progressBar_fileSend);
		progressBarSend.setProgress(0);
		//progressBarSend.setVisibility(View.GONE);
		textViewProgressSend = (TextView)findViewById(R.id.textView_fileSend);
		
		progressBarRecv=(ProgressBar) findViewById(R.id.progressBar_fileRecv);
		progressBarRecv.setProgress(0);
		//progressBarRecv.setVisibility(View.GONE);
		textViewProgressRecv = (TextView)findViewById(R.id.textView_fileRecv);
			
		onStartClickEvent();
		onStopClickEvent();
		onSettingClickEvent();
		getStatToDisplay(); 
		//getStatDataToDisplay();	

		//createMenuResDialog();
		//createMenuCameraDialog();		
	
		initViewConfig();
		initSocket();
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

				
		int resid = configInfo.getInt(RESID, 1);
		setResolution(resid);	
		
//		setEncBitrate(bitrate);
//		setCapFps(capfps);
//		setEncFps(encfps);
		
		setRemoteIP(remoteip);
		setRemotePort(remoteport);	
		setRemoteName(remotename);
		
		setServerIP(serverip);
		setServerPort(serverport);	
			
		setLocalIP(localip);
		setLocalPort(localport);			
		setLocalName(localname);
		
		setUseHwcode(iUsingHWCode);
		setUseImgCut(iUsingImgCut);	
		setAdaptiveRes(iAdaptiveRes);	
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
				switch (msg.what) {
				
				case UPDATE_VIDEO_TEXT:
		
					int iVideoStart = getVideoStartFlag();
					if(iVideoStart > 0)
						startVideoThread(iVideoStart);
						
					
					int iImgShowFlag = getVideoDisplayFlag();
					if(iImgShowFlag == 0)
					{	
						Toast.makeText(getApplicationContext(), "Display error.", Toast.LENGTH_SHORT).show();
					}
									
					break;
					
				case MAINTHREADTIMER:

					// to be done ...
					/* */
					int r = 0;
					r = OnMainThread(); //&resChg);
					if ((r & 3) == 1)
					{
						Toast.makeText(getApplicationContext(), "320x240", Toast.LENGTH_SHORT).show();
					}
					else if ((r & 3) == 2)
					{
						Toast.makeText(getApplicationContext(), "640x480", Toast.LENGTH_SHORT).show();
					}
					else if ((r & 3) == 3)
					{
						Toast.makeText(getApplicationContext(), "Unkown resolution", Toast.LENGTH_SHORT).show();
					}					
					
					break;
					
				case UPDATE_FILETRANS_TEXT:	
						
					int iFileSendEndFlag = getFileSendEndFlag();							
					int iFileSendProgValue = getFileSendValue();
					String sFileSendProgValue = "Send: " + iFileSendProgValue + "%";					

					if((iFileSendEndFlag == 0)&&(iFileSendProgValue > 0))
					{						
//						int iflag = progressBarSend.getVisibility();
//						if(iflag != View.VISIBLE)	
//							progressBarSend.setVisibility(View.VISIBLE);
							
						progressBarSend.setProgress(iFileSendProgValue);
//						
//						iflag = textViewProgressSend.getVisibility();
//						if(iflag != View.VISIBLE)		
//							textViewProgressSend.setVisibility(View.VISIBLE);
						
						textViewProgressSend.setText(sFileSendProgValue);						
					}					
					
					int iFileRecvEndFlag = getFileRecvEndFlag();	
					int iFileRecvProgValue = getFileRecvValue();
					String sFileRecvProgValue = "Recv: " + iFileRecvProgValue+ "%";
					
					if((iFileRecvEndFlag == 0)&&(iFileRecvProgValue > 0))
					{	
						
//						int iflag = progressBarRecv.getVisibility();
//						if(iflag != View.VISIBLE)		
//							progressBarRecv.setVisibility(View.VISIBLE);
							
						progressBarRecv.setProgress(iFileRecvProgValue);
						
//						iflag = textViewProgressRecv.getVisibility();
//						if(iflag != View.VISIBLE)		
//							textViewProgressRecv.setVisibility(View.VISIBLE);
							
						textViewProgressRecv.setText(sFileRecvProgValue);												
					}

					// Rmv by wanggeng, 锟斤拷时
					/*
					Log.i(TAG, sFileRecvProgValue + ",  "+ sFileRecvProgValue 
							+ ", iFileSendEndFlag = " + iFileSendEndFlag
							+ ", iFileRecvEndFlag = " + iFileRecvEndFlag);
					*/					

					String sFileSendProgEnd = null ;
					String sFileRecvProgEnd = null ;			
										
					if(iFileSendEndFlag == 1)
					{					
						if((iFileSendProgValue == 100) &&(progressBarSend.getProgress() != 0))	
						{
							sFileSendProgEnd = "File Send OK!";							
							Toast.makeText(getApplicationContext(), sFileSendProgEnd, Toast.LENGTH_SHORT).show();
						}
						else if((iFileSendProgValue > 0) &&(progressBarSend.getProgress() != 0))	
						{
							sFileSendProgEnd = "File Send Stop!";	
							Toast.makeText(getApplicationContext(), sFileSendProgEnd, Toast.LENGTH_SHORT).show();
						}
		
						progressBarSend.setProgress(0);					
						textViewProgressSend.setText(" ");						
					}
					
					if(iFileRecvEndFlag == 1)
					{					
						if((iFileRecvProgValue == 100)&&(progressBarSend.getProgress() != 0))	
						{
							sFileRecvProgEnd = "File Recv OK!";							
							Toast.makeText(getApplicationContext(), sFileRecvProgEnd, Toast.LENGTH_SHORT).show();
						}
						else if((iFileRecvProgValue > 0)&&(progressBarSend.getProgress() != 0))	
						{
							sFileRecvProgEnd = "File Recv Stop!";	
							Toast.makeText(getApplicationContext(), sFileRecvProgEnd, Toast.LENGTH_SHORT).show();
						}
		
						progressBarRecv.setProgress(0);					
						textViewProgressRecv.setText(" ");						
					}
							
				break;
					
				}
				super.handleMessage(msg);
			}
		};
	}
		
	public void createMenuCodecSettingDialog() {

		LayoutInflater inflaterbr = (LayoutInflater) MainGUI.this.getSystemService(LAYOUT_INFLATER_SERVICE);
		View codecSetting_view = inflaterbr.inflate(R.layout.avseting_codec_setting, null);

		codecinfo_RadioGroup = (RadioGroup) codecSetting_view.findViewById(R.id.radioGroup_resolution);
		qvga_RadioButton = (RadioButton) codecSetting_view.findViewById(R.id.radio_qvga);
		cif_RadioButton = (RadioButton) codecSetting_view.findViewById(R.id.radio_cif);
		vga_RadioButton = (RadioButton) codecSetting_view.findViewById(R.id.radio_vga);
		stHD_RadioButton = (RadioButton) codecSetting_view.findViewById(R.id.radio_720p);

		bitrate_EditText = (EditText) codecSetting_view.findViewById(R.id.editText_bitrate);
		fps_EditText = (EditText) codecSetting_view.findViewById(R.id.editText_fps);
				
		hwcode_CheckBox = (CheckBox) codecSetting_view.findViewById(R.id.checkBox_hardcoding);
		imgCut_CheckBox = (CheckBox) codecSetting_view.findViewById(R.id.checkBox_imgcuting);
		
		AdaptiveRes_CheckBox = (CheckBox) codecSetting_view.findViewById(R.id.CheckBox_AdaptiveRes);

		int bitrate = configInfo.getInt(BITRATE, 100);
		bitrate_EditText.setText("" + bitrate);

		int fps = configInfo.getInt(FPS, 15);
		fps_EditText.setText("" + fps);


		int radioid = configInfo.getInt(RADIORESID, R.id.radio_qvga);
		codecinfo_RadioGroup.check(radioid);
		
		int resid = configInfo.getInt(RESID, 1);
		setResolution(resid);
		
		codecinfo_RadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener(){		
			@Override
			public void onCheckedChanged(RadioGroup arg0,
					int checkedId) {
				// TODO Auto-generated method stub
				
				if (checkedId == qvga_RadioButton.getId()) {
					Toast.makeText(getApplicationContext(),"QVGA selected",Toast.LENGTH_SHORT).show();
					//setSelectRes(1);	
					setResolution(1);	
					configInfo.edit().putInt(RADIORESID, R.id.radio_qvga).commit();
					configInfo.edit().putInt(RESID, 1).commit();
				}

				if (checkedId == cif_RadioButton.getId()) {
					Toast.makeText(getApplicationContext(),"CIF selected",Toast.LENGTH_SHORT).show();
					//setSelectRes(2);
					setResolution(2);
					configInfo.edit().putInt(RADIORESID, R.id.radio_cif).commit();
					configInfo.edit().putInt(RESID, 2).commit();
				}

				if (checkedId == vga_RadioButton.getId()) {
					Toast.makeText(getApplicationContext(),"VGA selected",Toast.LENGTH_SHORT).show();
					//setSelectRes(3);
					setResolution(3);
					configInfo.edit().putInt(RADIORESID, R.id.radio_vga).commit();
					configInfo.edit().putInt(RESID, 3).commit();
				}

				if (checkedId == stHD_RadioButton.getId()) {
					Toast.makeText(getApplicationContext(),"720P selected",Toast.LENGTH_SHORT).show();
					//setSelectRes(4);
					setResolution(4);
					configInfo.edit().putInt(RADIORESID, R.id.radio_720p).commit();
					configInfo.edit().putInt(RESID, 4).commit();
				}							
			}		
		});
					
		int iHwCode = configInfo.getInt(USINGHWCODE, 0);
		if(iHwCode == 1)
			hwcode_CheckBox.setChecked(true);	
		else
			hwcode_CheckBox.setChecked(false);
		
		hwcode_CheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){ 
            @Override 
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) { 
                // TODO Auto-generated method stub 
            	if(isChecked){
            		Toast.makeText(getApplicationContext(),"Using Hard-Coding",Toast.LENGTH_SHORT).show();
            		setUseHwcode(1);               		
            		configInfo.edit().putInt(USINGHWCODE, 1).commit();
            	}else{
            		setUseHwcode(0);     
            		configInfo.edit().putInt(USINGHWCODE, 0).commit();
            	}
            } 
        });
			
		int iCutImg = configInfo.getInt(USINGIMGCUT, 0);
		if(iCutImg == 1)
			imgCut_CheckBox.setChecked(true);	
		else
			imgCut_CheckBox.setChecked(false);		
		
		imgCut_CheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
            	if(isChecked){
            		Toast.makeText(getApplicationContext(),"Using ImgCut, Res > QVGA",Toast.LENGTH_SHORT).show();
            		setUseImgCut(1); 
            		configInfo.edit().putInt(USINGIMGCUT, 1).commit();
            	}else{
            		setUseImgCut(0);  
            		configInfo.edit().putInt(USINGIMGCUT, 0).commit();
            	}			
			}});

		// adpative resolution
		int iAdaptRes = configInfo.getInt(ADAPTIVERES, 0);
		if(iAdaptRes == 1)
			AdaptiveRes_CheckBox.setChecked(true);	
		else
			AdaptiveRes_CheckBox.setChecked(false);			
		AdaptiveRes_CheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
            	if(isChecked){
            		Toast.makeText(getApplicationContext(),"Appying Adaptive Resolution",Toast.LENGTH_SHORT).show();
            		setAdaptiveRes(1); 
            		configInfo.edit().putInt(ADAPTIVERES, 1).commit();
            	}else{
            		setAdaptiveRes(0);  
            		configInfo.edit().putInt(ADAPTIVERES, 0).commit();
            	}			
			}});
		
		//Log.i(TAG, "createMenuCodecSettingDialog: codecinfo_builder");		
		codecinfo_builder = new Builder(MainGUI.this)
				.setTitle("Codec Setting").setView(codecSetting_view)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub
						
						//Log.i(TAG, "createMenuCodecSettingDialog: codecinfo_builder onClick");
												
						String sbitrate = bitrate_EditText.getText().toString().trim();

						int encbitrate = Integer.parseInt(sbitrate);
						configInfo.edit().putInt(BITRATE, encbitrate).commit();

						Log.i(TAG, "createMenuCodecSettingDialog  encbitrate: "+ encbitrate);

						if (sbitrate.length() != 0) {
							setEncBitrate(encbitrate);
						} else
							setEncBitrate(0);

						String sfps = fps_EditText.getText().toString().trim();

						int ifps = Integer.parseInt(sfps);
						configInfo.edit().putInt(FPS, ifps).commit();

						Log.i(TAG, "createMenuCodecSettingDialog  ifps: "+ ifps);

						if (sfps.length() != 0) {
							setEncFps(ifps);
						} else
							setEncFps(0);											
						
//						int resindex = getSelectRes();
//						if(resindex > 0)
//							setResolution(resindex);								
					}
				});
	}
		
	public void createMenuNetworkSettingDialog(){
		
		//Log.i(TAG, "Enter createMenuNetworkSettingDialog");
		
		LayoutInflater inflaterbr = (LayoutInflater) MainGUI.this.getSystemService(LAYOUT_INFLATER_SERVICE);
		View netinfoSetting_view = inflaterbr.inflate(R.layout.avseting_network_setting, null);
			
		serverip_EditText = (EditText) netinfoSetting_view.findViewById(R.id.editText_serverip);
		serverport_EditText = (EditText) netinfoSetting_view.findViewById(R.id.editText_serverport);
		
		remoteip_EditText = (EditText) netinfoSetting_view.findViewById(R.id.editText_remoteip);
		remoteport_EditText = (EditText) netinfoSetting_view.findViewById(R.id.editText_remoteport);
		remotename_EditText = (EditText) netinfoSetting_view.findViewById(R.id.editText_remotename);
		
		localip_EditText = (EditText) netinfoSetting_view.findViewById(R.id.editText_localip);
		localport_EditText = (EditText) netinfoSetting_view.findViewById(R.id.editText_localport);
		localname_EditText = (EditText) netinfoSetting_view.findViewById(R.id.editText_localname);
						
		//	
		String serverip = configInfo.getString(SERVERIP, "192.168.0.101");
		int serverport = configInfo.getInt(SERVERPORT, 9520);

		serverip_EditText.setText(serverip);
		serverport_EditText.setText("" + serverport);		
		
		//
		String remoteip = configInfo.getString(REMOTEIP, "127.0.0.1");
		int remoteport = configInfo.getInt(REMOTEPORT, 50000);
		String remotename = configInfo.getString(REMOTENAME, "AD2");
		
		remoteip_EditText.setText(remoteip);
		remoteport_EditText.setText("" + remoteport);		
		remotename_EditText.setText(remotename);		
		
		//
		//String localip = configInfo.getString(LOCALIP, "127.0.0.1");
		//String localip = configInfo.getString(LOCALIP, getWifiIPAddress());

		String localip = getWifiIPAddress();
				
		int localport = configInfo.getInt(LOCALPORT, 50000);
		String localname = configInfo.getString(LOCALNAME, "AD1");
		
		localip_EditText.setText(localip);
		localport_EditText.setText("" + localport);		
		localname_EditText.setText(localname);	
				
		//Log.i(TAG, "createMenuNetworkSettingDialog  networkinfo_builder");	
		networkinfo_builder = new Builder(MainGUI.this)
				.setTitle("Network Setting").setView(netinfoSetting_view)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub

						String serveripvalue = serverip_EditText.getText().toString().trim();
						String serverportvalue = serverport_EditText.getText().toString().trim();

						// Log.d(TAG, "destportvalue: " + destportvalue);
						int iserverportvalue = Integer.parseInt(serverportvalue);

						configInfo.edit().putString(SERVERIP, serveripvalue).commit();
						configInfo.edit().putInt(SERVERPORT, iserverportvalue).commit();

						if (serveripvalue.length() != 0)
							setServerIP(serveripvalue);
						else
							setServerIP(null);

						if (serverportvalue.length() != 0)
							setServerPort(iserverportvalue);
						else
							setServerPort(0);
																		
						String remoteipvalue = remoteip_EditText.getText().toString().trim();
						String remoteportvalue = remoteport_EditText.getText().toString().trim();
						String remoteNamevalue = remotename_EditText.getText().toString().trim();

						// Log.d(TAG, "destportvalue: " + destportvalue);
						int iremoteportvalue = Integer.parseInt(remoteportvalue);

						configInfo.edit().putString(REMOTEIP, remoteipvalue).commit();
						configInfo.edit().putInt(REMOTEPORT, iremoteportvalue).commit();
						configInfo.edit().putString(REMOTENAME, remoteNamevalue).commit();

						if (remoteipvalue.length() != 0)
							setRemoteIP(remoteipvalue);

						if (remoteportvalue.length() != 0)
							setRemotePort(iremoteportvalue);
					
						if (remoteNamevalue.length() != 0)
							setRemoteName(remoteNamevalue);
		
						
						String localipvalue = localip_EditText.getText().toString().trim();
						String localportvalue = localport_EditText.getText().toString().trim();
						String localNamevalue = localname_EditText.getText().toString().trim();

						// Log.d(TAG, "destportvalue: " + destportvalue);
						int ilocalportvalue = Integer.parseInt(remoteportvalue);

						//configInfo.edit().putString(LOCALIP, localipvalue).commit();
						configInfo.edit().putInt(LOCALPORT, ilocalportvalue).commit();
						configInfo.edit().putString(LOCALNAME, localNamevalue).commit();

						if (localipvalue.length() != 0)
							setLocalIP(localipvalue);

						if (localportvalue.length() != 0)
							setLocalPort(ilocalportvalue);		
						

						if (localNamevalue.length() != 0)
							setLocalName(localNamevalue);
					}

				});
		//Log.i(TAG, "Leave createMenuNetworkSettingDialog");	
	}
		
	public void createMenuCameraSettingDialog(){
		
		LayoutInflater inflaterbr = (LayoutInflater) MainGUI.this.getSystemService(LAYOUT_INFLATER_SERVICE);
		View cameraSetting_view = inflaterbr.inflate(R.layout.avseting_camera_setting, null);

		camera_RadioGroup = (RadioGroup) cameraSetting_view.findViewById(R.id.radioGroup_camersetting);
		frontcamera_RadioButton = (RadioButton) cameraSetting_view.findViewById(R.id.radio_frontcamera);
		backcamera_RadioButton = (RadioButton) cameraSetting_view.findViewById(R.id.radio_backcamera);

		int radiocamid = configInfo.getInt(RADIOCAMID, R.id.radio_frontcamera);
		camera_RadioGroup.check(radiocamid);	
			
		camera_RadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener(){		
			@Override
			public void onCheckedChanged(RadioGroup arg0,
					int checkedId) {
				// TODO Auto-generated method stub
				
				if (checkedId == frontcamera_RadioButton.getId()) {
					Toast.makeText(getApplicationContext(),"Front-facing Camera Selected",Toast.LENGTH_SHORT).show();
						
					//setSelectCam(1);
					setCamID(1);
					configInfo.edit().putInt(RADIOCAMID, R.id.radio_frontcamera).commit();
				}

				if (checkedId == backcamera_RadioButton.getId()) {
					Toast.makeText(getApplicationContext(),"Back-facing Camera Selected",Toast.LENGTH_SHORT).show();
					
					//setSelectCam(0);
					
					setCamID(0);
					configInfo.edit().putInt(RADIOCAMID, R.id.radio_backcamera).commit();
				}
			}		
		});			
		
		camera_builder = new Builder(MainGUI.this)
			.setTitle("Camera Setting").setView(cameraSetting_view)
			.setPositiveButton("OK", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				// TODO Auto-generated method stub
				//setCamID(getSelectCam());
			}					
		});
		
	}	

	public void createMenuFileSeleteSettingDialog(){
		
		//LayoutInflater inflaterbr = (LayoutInflater) MainGUI.this.getSystemService(LAYOUT_INFLATER_SERVICE);
		//View cameraSetting_view = inflaterbr.inflate(R.layout.camera_setting, null);
		
		fileTrans_builder = new Builder(MainGUI.this)
			.setTitle("File Selecting")
			.setView(new FileSelectView(getApplicationContext(), 0, new CallbackBundle(){

				@Override
				public void callback(Bundle bundle) {
					// TODO Auto-generated method stub
					String filepath = bundle.getString("path");
					
					Log.i(TAG, "createMenuFileSeleteSettingDialog: "+filepath);
					
					setSendFileName(filepath);
				}}))
			.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				// TODO Auto-generated method stub
				//setCamID(getSelectCam());
				
				initProgressBar(0);
				//setProgressBar(0, 0);	
				startFileSend();				
			}					
		});
	}
	
	@Override
	protected void onResume() {
		super.onResume();

		mTimerTask = new TimerTask() {
			@Override
			public void run() {
				Message message = new Message();
				message.what = UPDATE_FILETRANS_TEXT;
				mHandler.sendMessage(message);
				// mHandler.sendEmptyMessage(UPDATE_TEXT);

	 		} 
		};          
		
		mVideoTimeTask  = new TimerTask() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				
				Message message = new Message();
				message.what = UPDATE_VIDEO_TEXT;
				mHandler.sendMessage(message);
				// mHandler.sendEmptyMessage(UP		
				
			}
			
		};
		
		mMainthreadTimerTask  = new TimerTask() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				
				Message message = new Message();
				message.what = MAINTHREADTIMER;
				mHandler.sendMessage(message);			
			}			
		};
		
	 	mTimer.schedule(mTimerTask, 1000, 500);
	 	mTimer.schedule(mVideoTimeTask, 1000, 50);
	 	mTimer.schedule(mMainthreadTimerTask, 1000, 50);		
	} 
 
	private void onStartClickEvent() {
		// TODO Auto-generated method stub
		mBtnStart.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {

				Log.d(TAG, "onStartClickEvent");
				
				//initViewConfig();

				start();
 
				// int cameraCount =
				// AndroidCameraConfiguration.getCamerasCount();
				// if (cameraCount != 0) {
				// Log.d(TAG, "camera exist: " + cameraCount);
				//
				// if (AndroidCameraConfiguration.hasFrontCamera()) {
				// Log.d(TAG, "hasFrontCamera");
				// } else {
				// Log.d(TAG, "hasRearCamera");
				// }
				//
				// if (AndroidVideoApiJniWrapper.isRecording)
				// AndroidVideoApiJniWrapper.stopRecording(mCamera);
				//
				// mCamera = (Camera) AndroidVideoApiJniWrapper
				// .startRecording(0, 352, 288, 6, 270, 0);
				// //
				// AndroidVideoApiJniWrapper.setPreviewDisplaySurface(mCamera,
				// // mSurfaceView);
				//
				// } else {
				// Log.w(TAG, "no camera");
				// }

			}
		});
	}

	
	private void onStopClickEvent() {
		// TODO Auto-generated method stub
		mBtnStop.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {

				Log.d(TAG, "onStopClickEvent");

				stop();
				statThreadRuning = false;
				// AndroidVideoApiJniWrapper.stopRecording(mCamera);
				System.exit(0);

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

	private void fixZOrder(SurfaceView video, SurfaceView preview) {
		video.setZOrderOnTop(false);
		preview.setZOrderOnTop(true);
		preview.setZOrderMediaOverlay(true); // Needed to be able to display
												// control layout over
	}

	//wifi IP Address
	private String getWifiIPAddress() {
		WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		// 获取IP
		int ipAddress = wifiInfo.getIpAddress();

		if(ipAddress != 0)	
			// 
			return String.format("%d.%d.%d.%d",
					(ipAddress & 0xff), (ipAddress >> 8 & 0xff),
					(ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
		else
			return "127.0.0.1";
	}


	//3G IP  Address
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.

		getMenuInflater().inflate(R.menu.main_gui, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		// 
//		case R.id.action_start:
//			// do something here
//
//			initViewConfig();
//			start();
//			break;
//		case R.id.action_stop:
//			// do something here
//			stop();
//			statThreadRuning = false;
//			//System.exit(0);
//			break;
//
//		case R.id.action_start_self:
//			// do something here
//			startself();
//			break;
		
		case R.id.codecinfo_settings:
			// do something here
			createMenuCodecSettingDialog();
			codecinfo_builder.show();
			break;
		
		case R.id.networkinfo_settings:
			// do something here
			createMenuNetworkSettingDialog();
			networkinfo_builder.show();
			break;		
			
		case R.id.action_swichcam:
			// do something here
			createMenuCameraSettingDialog();
			camera_builder.show();
			break;

		case R.id.action_start_audio:
			// do something here	
			startAudio();
			break;
			
		case R.id.action_mute_audio:
			// do something here
			muteAudio();
			break;	
			
			
		case R.id.action_unmute_audio:
			// do something here
			unMuteAudio();
			break;			
					
		case R.id.action_start_video:
			// do something here
			startVideo();
			break;		
			
		case R.id.action_start_av:
			// do something here
			startAV();
			break;

		case R.id.action_start_file:
			// do something here
			
			//showDialog(openfileDialogId);
			//showDialog(0);
			createMenuFileSeleteSettingDialog();
			fileTrans_builder.show();
			
			//initProgressBar(0);
			//setProgressBar(0, 0);	
			//startFileSend();
			
//			setProgressBar(0, 50);
//			
//			initProgressBar(1);
//			setProgressBar(1, 100);
//			
//			
//			stopProgressBar(0);
//			stopProgressBar(1);
			break;

//		case R.id.res_settings:
//			// do something here
//			res_dialog.show();
//			break;
//				
//			
//		case R.id.bitrate_settings:
//			// do something here
//			
//			//createMenuBitrateDialog();
//			//bitrate_builder.show();
//			break;
//
//		case R.id.fps_settings:
//			// do something here
//
//			//createMenuFpsDialog();			
//			//fps_builder.show();
//
//
//			break;			
//			
//		case R.id.destip_settings:
//			// do something here
//			createMenuDestipDialog();
//			destip_builder.show();
//
//			break;
//			
//		case R.id.serverip_settings:
//			// do something here
//			createMenuServerDialog();
//			serverip_builder.show();
//
//			break;			
//			

		default:
			// 
			return super.onOptionsItemSelected(item);
		}
		// 
		return true;
	}

	public static  void initProgressBar(int type){	
		if(type == 0){		
			progressBarSend.setVisibility(View.VISIBLE);
			progressBarSend.setMax(100);
			progressBarSend.setProgress(0);
			textViewProgressSend.setVisibility(View.VISIBLE);
			textViewProgressSend.setText("Send:"+ 0+"%");		
		}
		
		if(type == 1){		
			progressBarRecv.setVisibility(View.VISIBLE);
			progressBarRecv.setMax(100);
			progressBarRecv.setProgress(0);
			textViewProgressRecv.setVisibility(View.VISIBLE);
			textViewProgressRecv.setText("Recv:"+ 0+"%");		
		}		
	}

	public static void setProgressBar(int type, int value){	

		if(type == 0){		
			
			if(textViewProgressSend.getVisibility() != View.VISIBLE) 
				textViewProgressSend.setVisibility(View.VISIBLE);		
			
			if(progressBarSend.getVisibility() == View.VISIBLE) 
				progressBarSend.setVisibility(View.VISIBLE);	
			
			progressBarSend.setProgress(value);
			textViewProgressSend.setText("Send: "+value+"%");	
			
			//Log.i(TAG, "setProgressBar: type0 , value:"+value);
		}
		if(type == 1){		
			
			if(textViewProgressRecv.getVisibility() != View.VISIBLE) 
				textViewProgressRecv.setVisibility(View.VISIBLE);
			
			if(progressBarRecv.getVisibility() != View.VISIBLE) 
				progressBarRecv.setVisibility(View.VISIBLE);						
			
			progressBarRecv.setProgress(value);
			textViewProgressRecv.setText("Recv: "+value+"%");	
			
			//Log.i(TAG, "setProgressBar: type1 , value:"+value);
		}
	}

	public static void stopProgressBar(int type){	
		if(type == 0){	
			textViewProgressSend.setText("");
			progressBarSend.setVisibility(View.GONE);
			
			Log.i(TAG, "stopProgressBar: type0 ");
			//Toast.makeText(this, "File Send OK!", Toast.LENGTH_SHORT).show();;
		}
		if(type == 1){		
			textViewProgressRecv.setText("");
			progressBarRecv.setVisibility(View.GONE);;
			
			
			Log.i(TAG, "stopProgressBar: type1 ");
			//Toast.makeText(this, "File Recv OK!", Toast.LENGTH_SHORT).show();
		}	
	}
	
	public void setSelectRes(int index) {
		gres_selected = index;
		
		//Log.i(TAG, "setSelectRes:  "+ index);
	}

	public int getSelectRes() {
		return gres_selected;

	} 
 
	public void setSelectCam(int index) {
		gcam_selected = index;
	}

	public int getSelectCam() {
		return gcam_selected;

	}

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

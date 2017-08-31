package com.epiano.commutil;

//import android.R;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ConfigurationInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap.Config;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.TimerTask;

//import android.content.pm.ResolveInfo;
//import android.epiano.com.commutil.MySurfaceView.SCROLL_DIRECTION;
//import android.epiano.com.commutil.MySurfaceView.SCROLL_PAGE_MOD;
//import android.epiano.com.commutil.MySurfaceView.SCROLL_PAGE_STATUS;
//import android.epiano.com.commutil.R.string;
//import android.epiano.com.commutil.R;
//import android.epiano.com.commutil.R;
//import android.graphics.Canvas;
//import android.media.AudioFormat;
//import android.media.AudioManager;
//import android.media.AudioTrack;
//import android.view.View.OnClickListener;
//import android.widget.LinearLayout.LayoutParams;
//import android.widget.ImageView;
//import android.widget.RelativeLayout;
//import android.widget.TableLayout;
//import android.widget.TableRow;
//import android.widget.TextView;
//import java.io.BufferedInputStream;
//import java.io.DataInputStream;
//import java.util.List;
//import java.util.Timer;



//import com.example.android.apis.view.Grid1;

//import com.example.android.apis.R;
//import com.example.android.apis.view.Grid1;
//import com.example.android.apis.view.Grid1.AppsAdapter;


public class MusicScoreBook3DGL2 extends Activity { // implements OnClickListener {

	private MySurfaceViewGL2 mGLSurfaceView;

	Config BmpConfig = Config.ARGB_8888;
	//Config BmpConfig = Bitmap.Config.RGB_565;
	//Config BmpConfig = Bitmap.Config.ARGB_4444;
	String ImgFileSurfix = ".png";
	//String ImgFileSurfix = ".jpg";

	static int MAX_PICVIEW_COUNT = 300;	// cfg...

	int OpenGl_2N_switchIn = 0; // cfg...

	//private PicView PicView1;// = (PicView)this.findViewById(R.id.PicView1);
	//private Timer mTimer;
//	private MyTimerTask mTimerTask;
	private int timercounter;
	public PicView pv[];
	private int PicViewCount = 0;

	int PicViewWidthOrg = 0;					// 初始窗口/图像宽度
	int PicViewHeightOrg = 0;
	int PicViewWidthCur = PicViewWidthOrg;		// 当前视图窗口宽度
	int PicViewHeightCur = PicViewHeightOrg;
	int WinWidthCur = PicViewWidthOrg;			// 当前物理窗口宽度
	int WinHeightCur = PicViewHeightOrg;
	int ImgWidth = PicViewWidthOrg;				// JNI图像宽度
	int ImgHeight = PicViewHeightOrg;
	float ImgAndViewK = 1;						// JNI中图像宽度与View宽度的比值
	float whK = (float)1.6;						// 图像长宽比

	final float PI = (float)3.14159265;

	//int PicViewLastMouseMovePageId = -1;	// 上一个pageid，用于优化重图时减少重图的页数, -1无效

	int onConfigurationChangedflag = 0;

	int EPianoAndroidJavaAPI_inited = 0;
	EPianoAndroidJavaAPI ObEPianoAndroidJavaAPI;

	//	private LinearLayout ll;
//	LinearLayout.LayoutParams lp;
//	RelativeLayout.LayoutParams lp;
//	private TableLayout ll;
//	TableLayout.LayoutParams lp;
//	LinearLayout.LayoutParams lp2;
	GridView mGrid;
	View mView3D;
	GridLayout ll;
	GridView.LayoutParams lp;

	LinearLayout LL3D;
	LinearLayout LL;
	LinearLayout.LayoutParams LLp2;

	Context context = this;

	float BaseDistanceOf2Fingers = 0;
	float mScale = 1;
	float mCurrentScale = 1;
	float last_x = -1;
	float last_y = -1;
	float delta_x = 0;
	int zoomed = 0;
	int mZoomStatus = 0;
	int r = 0;

	String openfilename;

	CNoteImgSet mNoteImgSet;

	///////////////// play /////////////////////////////////

	CAudioSynthesisAndPlayGL2 AudioSynthesis;
	int StereoOn = 1;

	//CKeyAudio mKeyAudio[]; // = new CKeyAudio[88];
//  	static int MaxPlayKeyId = 3000;
//  	int PlayKeyCount = 0;
//  	//PlayKeyBarId_TSId[]
//	int PlayKeyVPId[] 			= new int [MaxPlayKeyId];	// 音符标记
//	int PlayKeyBarId[] 			= new int [MaxPlayKeyId];	// 音符标记
//	int PlayKeyTSId[] 			= new int [MaxPlayKeyId];	// 音符标记, 对应TIME_NODE.iTimeSliceId
//	int PlayKeyTick[] 			= new int [MaxPlayKeyId];	// 音符起始时刻
//	int PlayKeyId[] 			= new int [MaxPlayKeyId];	// 音高 , 1 based
//	int PlayKeyFsInt;
//	float PlaKeyFs[] 			= new float [MaxPlayKeyId];	// 频率
//	int PlayKeyDurationInt;
//	float PlayKeyDuration[] 	= new float [MaxPlayKeyId];	// 时长
//	int PlayKeyStrength[] 		= new int [MaxPlayKeyId];	// 强度

//	CAudioSythAndPlayThread mAudioSythAndPlayThread;

	CKeyAudio mKeyAudio[];

	ProgressBar pb;
	//  	private boolean StartToPlay = false; //false;
//  	private boolean PausePlay = false; //false;
//  	int PauseTime = 0;
//  	AudioTrack audioTrack = null;
//  	int AudioChannels = 2; 	// 1;
//  	int StereoOn = 1; 		// 0: close, 1: on;
	int AutoScrollPageOnPlay = 1;		// 播放时自动翻页, cfg...

	boolean ForceHoofSwitch = true;	// 强制踏板/延音, cfg...
//
//  	int CurPosIndicatorId = -1;
//  	int mPosIndicator[];		// Play位置指示
//  	long mStartPlayTime = -1;
//  	int mRealPlaycount = 0;
//  	int audioTrackBufferDelay = 0;
//  	int PosIndicatorRes = 100; // ms
//  	final int SAMPLE_RATE = 16000; // 11025;
//  	PosIndicateTask IndiTask;

	WindowManager winmanager; // = getWindowManager();
	Display display;// = winmanager.getDefaultDisplay();  //为获取屏幕宽、高

	// 文件有效判断
	public boolean isFileExist(String filePathName)
	{
		File file = new File(filePathName);
		if (file.isFile() && file.exists()) {
			return true;
		}
		return false;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		int r = 0;

		requestWindowFeature(Window.FEATURE_NO_TITLE);//隐藏标题栏
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);//设置全屏


		//设置为横屏
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

		super.onCreate(savedInstanceState);
		setContentView(R.layout.musicscore3dgl2); //activity_main);

		// 获取文件名
		Intent intent=getIntent();
		openfilename = intent.getStringExtra("openfilename");
		if (!isFileExist(openfilename))
		{
			Log.i("WG", "Error, file:" + openfilename + " does not exist.");
			Toast.makeText(MusicScoreBook3DGL2.this, "文件不存在.", 200).show();

			return;
		}

		context = this;
		mNoteImgSet = new CNoteImgSet(this);

		//-----------------------------------------------方法三
		LL = (LinearLayout)findViewById(R.id.LinearLayoutCtl);
		//mGrid = (GridView) findViewById(R.id.GridView1);
		//ViewTreeObserver vto2 = mGrid.getViewTreeObserver();
		//mView3D = (View) findViewById(R.id.view3D);


		LL3D = (LinearLayout)findViewById(R.id.LinearLayout3D);
		mGLSurfaceView = new MySurfaceViewGL2(context, this, ImgFileSurfix, OpenGl_2N_switchIn);
		LL3D.addView(mGLSurfaceView);

		// 检查是否支持ES20
		final ActivityManager activityManager =
				(ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		final ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
		final boolean supportsEs2 =
				configurationInfo.reqGlEsVersion >= 0x20000
						|| (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1
						&& (Build.FINGERPRINT.startsWith("generic")
						|| Build.FINGERPRINT.startsWith("unknown")
						|| Build.MODEL.contains("google_sdk")
						|| Build.MODEL.contains("Emulator")
						|| Build.MODEL.contains("Android SDK built for x86")));
		if (supportsEs2) {
			// Request an OpenGL ES 2.0 compatible context.
			mGLSurfaceView.setEGLContextClientVersion(2);
			// Assign our renderer.
			//mGLSurfaceView.setRenderer(new FirstOpenGLProjectRenderer());
			//mGLSurfaceView.setRenderer(new MySurfaceViewGL2(this));
			//rendererSet = true;
		} else {
			Toast.makeText(this, "This device does not support OpenGL ES 2.0.",
					Toast.LENGTH_LONG).show();
			return;
		}


		ViewTreeObserver vto2 = mGLSurfaceView.getViewTreeObserver();
		vto2.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				if (mGrid != null)
				{
					// no use

					//mGrid.getViewTreeObserver().removeGlobalOnLayoutListener(this);

					DisplayMetrics dm=new DisplayMetrics();
					//            	dm.widthPixels = mGrid.getWidth();
					//            	dm.heightPixels = mGrid.getHeight();
					//dm.widthPixels = mGrid.getWidth();
					dm.widthPixels = mView3D.getWidth();
					winmanager = getWindowManager();
					display = winmanager.getDefaultDisplay();  //为获取屏幕宽、高
					int screenH0 = PicViewHeightOrg;
					int screenH1 = display.getHeight();
					int LH = LL.getHeight();
					dm.heightPixels = display.getHeight() - LL.getHeight();
					//dm.heightPixels = display.getHeight() - 5 - 28;

					// 生成pv[]
					RenderViews(dm.widthPixels, dm.heightPixels);

					//mGLSurfaceView = new MySurfaceView(context, pv, PicViewCount, dm);
					mGLSurfaceView.SetParam(context, BmpConfig, ObEPianoAndroidJavaAPI, pv, PicViewCount, dm);

					lp = new GridView.LayoutParams(dm.widthPixels, dm.heightPixels);
					mGLSurfaceView.setLayoutParams(lp);//设置布局参数

					mGrid.setAdapter(new AppsAdapter());
				}
				else
				{
					mGLSurfaceView.getViewTreeObserver().removeGlobalOnLayoutListener(this);

					DisplayMetrics dm = new DisplayMetrics();
					//            	dm.widthPixels = mGrid.getWidth();
					//            	dm.heightPixels = mGrid.getHeight();
					//dm.widthPixels = mGrid.getWidth();
					winmanager = getWindowManager();
					display = winmanager.getDefaultDisplay();  //为获取屏幕宽、高
//	            	int screenH0 = PicViewHeightOrg;
//	            	int screenH1 = display.getHeight();
//	            	int LH = LL.getHeight();
					dm.widthPixels = display.getWidth();
					dm.heightPixels = display.getHeight() - LL.getHeight();
					//dm.heightPixels = display.getHeight() - 5 - 28;

					mGLSurfaceView.SetWin(dm);
//	            	int iPageWidth;

					// 生成平面图
					RenderViews(mGLSurfaceView.tp.GetPageWidth(), mGLSurfaceView.tp.GetPageHeight()); //  dm.heightPixels);

					// 创建3D场景渲染器, 启动旋转线程
					//mGLSurfaceView = new MySurfaceView(context, pv, PicViewCount, dm);
					mGLSurfaceView.SetParam(context, BmpConfig, ObEPianoAndroidJavaAPI, pv, PicViewCount, dm);

					//LLp2 = new LinearLayout.LayoutParams(dm.widthPixels, dm.heightPixels);
					//mGLSurfaceView.setLayoutParams(LLp2);//设置布局参数
				}


			}
		});


		// view create
		ViewCreate();

	}

	private void loadApps() {
		Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

		//mApps = getPackageManager().queryIntentActivities(mainIntent, 0);
	}

	//private List<ResolveInfo> mApps;
	public class AppsAdapter extends BaseAdapter {
		public AppsAdapter() {
		}

		int selectItem = -1;

		public View getView(int position, View convertView, ViewGroup parent) {
			PicView i;

//	            System.out.println("AppsAdapter, position is " + position + ", convertView is " + convertView + ", pv[position].iPageId is " + pv[position].iPageId + ".");

//	            if (convertView == null) {
////	                //i = new ImageView(Grid1.this);
////	            	//i = new PicView(MainActivity.this, position, PicViewWidthOrg, PicViewHeightOrg);
////	            	i = pv[position];
////	                //i.setScaleType(ImageView.ScaleType.FIT_CENTER);
////	                i.setLayoutParams(new GridView.LayoutParams(PicViewWidthOrg, PicViewHeightOrg));
//////	                i.setScaleX((float)0.5);
//////	                i.setScaleY((float)0.5);
////	            	i.setAdjustViewBounds(false);
////	                i.setScaleType(ImageView.ScaleType.CENTER_CROP);
//
//	            	i = new ImageView(MainActivity.this);
//	                i.setScaleType(ImageView.ScaleType.FIT_CENTER);
//	                i.setLayoutParams(new GridView.LayoutParams(50, 50));
//
//	            } else {
//	                i = (PicView) convertView;
//	            }
//
//	            ResolveInfo info = mApps.get(position);
//	            i.setImageDrawable(info.activityInfo.loadIcon(getPackageManager()));

//	            int iPageId = position;
//	            i = pv[iPageId];
//
//	            return i;

			//return  pv[position];
			return mGLSurfaceView;
		}

		public void setSelection(int position)
		{
			selectItem = position;
		}

		public final int getCount() {
			//return PicViewCount; // mApps.size();
			return 1;
		}

		public final Object getItem(int position) {
			return position; //mApps.get(position);
		}

		public final long getItemId(int position) {
			return position;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (AudioSynthesis != null)
		{
			if (AudioSynthesis.StartToPlay == true
				//	    		&& AudioSynthesis.audioTrack != null
					)
			{
				AudioSynthesis.StartToPlay = false;
				//		    	audioTrack.stop();//停止播放
				//				audioTrack.release();//释放底层资源
			}
		}

		if (mGLSurfaceView != null)
		{
			mGLSurfaceView.threadGoOn = false;
		}
	}


	// 加载所有key的音频数据，生成立体声数据
	int LoadKeyAudio(int AudioChannels) {
		int r = 0;

		mKeyAudio = new CKeyAudio[88];

		for (int i = 0; i < 88; i++) {
			mKeyAudio[i] = new CKeyAudio(AudioChannels);
			mKeyAudio[i].keyId = i;

			r = mKeyAudio[i].readkeyaudiofile();
			if (r == 0) {
				return 0;
			}

			// 缺省单声道
			mKeyAudio[i].SetStereoOn(false);

//			if (StereoOn > 0) {
//				mKeyAudio[i].SetStereoOn(true);
//			} else {
//				mKeyAudio[i].SetStereoOn(false);
//			}
		}

		return 1;
	}

	// 设置声道
	int SetStereo(int StereoOn) {
		int r = 0;

		//mKeyAudio = new CKeyAudio[88];

		for (int i = 0; i < 88; i++) {
			if (mKeyAudio[i] == null)
			{
				return 0;
			}

			if (StereoOn == 1)
			{
				if (!mKeyAudio[i].stereoOn)	// 防止重复设置
				{
					mKeyAudio[i].SetStereoOn(true);
				}
			}
			else if (StereoOn == 0)
			{
				if (mKeyAudio[i].stereoOn)
				{
					mKeyAudio[i].SetStereoOn(false);
				}
			}
		}

		return 1;
	}

	// view create
	public boolean ViewCreate()
	{
		pb = (ProgressBar)findViewById(R.id.pb);
		pb.setMax(1000);

		ImageButton buttonRewind = (ImageButton)findViewById(R.id.ImageButtonGoto1stPage);
		/* 监听button的事件信息 */
		buttonRewind.setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View arg0) {

				// 回到首页
				if (mGLSurfaceView != null)
				{
					mGLSurfaceView.Rewind();
				}
			}
		});

		ImageButton buttonPlay = (ImageButton)findViewById(R.id.imageButtonPlay);
		/* 监听button的事件信息 */
		buttonPlay.setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View arg0) {

				if (AudioSynthesis != null)
				{
					// stop
					AudioSynthesis.StartToPlay = false;
					AudioSynthesis.IndiTask.cancel(true);
				}

				// 创建合成器
				//if (AudioSynthesis == null) // 重新创建, 重用有点问题:不能二次播放
				{
					AudioSynthesis = new CAudioSynthesisAndPlayGL2(openfilename, pb, StereoOn);
				}

				// 加载所有key的音频数据，生成立体声数据
				if (mKeyAudio == null) {
					int r = LoadKeyAudio(AudioSynthesis.AudioChannels);
					if (r == 0) {
						// Toast.makeText(MusicScoreBook3D.this,
						// "Can't load key audio.", 200).show();

						Log.i("WG", "Error, fail to Load audio");

						return;
					}
				}
				// 设置声道
				{
					int r = SetStereo(AudioSynthesis.StereoOn);
					if (r == 0) {

						Log.i("WG", "Error, fail to SetStereo");

						return;
					}
				}

				if (AudioSynthesis.StartToPlay == true)
				{
					// 已经在播放

					return;
				}

				// 生成播放数据
				if (true)
				{
					int PlayNoteSet[] = ObEPianoAndroidJavaAPI.GetPlayNoteSet();
					int timeK = 40; // 48,

					int DataCheckTag = PlayNoteSet[0];
					int DataFieldNum = PlayNoteSet[2]; 		// 6
					int DataLen; // = PlayNoteSet.length;
					int hdrlen = 3;
					DataLen = PlayNoteSet[1]; // - DataFieldNum * 3;	// * 2 ?; // - DataFieldNum;
					AudioSynthesis.PlayKeyCount = DataLen / DataFieldNum;

					if (DataFieldNum == 0)
					{
						Toast.makeText(MusicScoreBook3DGL2.this, "DataFieldNum is 0.", Toast.LENGTH_SHORT).show();
						return;
					}
					else
					{
						if ((DataLen % DataFieldNum) != 0)
						{
							Toast.makeText(MusicScoreBook3DGL2.this, "DataLen % DataFieldNum" + String.valueOf(DataLen)
									+ "," + String.valueOf(DataFieldNum), Toast.LENGTH_SHORT).show();
							return;
						}
					}

					Toast.makeText(MusicScoreBook3DGL2.this, "Get Note Set, DatLen" + String.valueOf(DataLen), Toast.LENGTH_SHORT).show();

//	            	int PlayKeyBarId_TSId[] 	= new int [1000];	// 音符标记
//	            	int PlayKeyTick[] 			= new int [1000];	// 音符起始时刻
//	            	int PlayKeyId[] 		= new int [1000];	// 音高
//	            	int PlayKeyFsInt;
//	            	float PlaKeyFs[] 	= new float [1000];	// 频率
//	            	int PlayKeyDurationInt;
//	            	float PlayKeyDuration[] 	= new float [1000];	// 时长
//	            	int PlayKeyStrength[] 		= new int [1000];	// 强度

					int showKeyDlg = 0; // 显示key列表对话框

					// 注入合成原料音符
					int dataidx = 0;
					//string strText = "数据";
					CharSequence strText = "播放数据(" + DataLen / (DataFieldNum * 4)+"条)\n";
					int maxp = DataLen + hdrlen; // - DataFieldNum;
					for(int i = 3; i < maxp && dataidx < AudioSynthesis.MaxPlayKeyId; dataidx++)
					{
						//Log.i("wg", "i:" + i);

						AudioSynthesis.PlayKeyVPId[dataidx] 		= PlayNoteSet[i++];
						AudioSynthesis.PlayKeyBarId[dataidx] 		= PlayNoteSet[i++];
						AudioSynthesis.PlayKeyTSId[dataidx] 		= PlayNoteSet[i++];
						AudioSynthesis.PlayKeyTick[dataidx] 		= PlayNoteSet[i++] * timeK;
						AudioSynthesis.PlayKeyId[dataidx] 			= PlayNoteSet[i++];
						AudioSynthesis.PlayKeyFsInt					= PlayNoteSet[i++];
						AudioSynthesis.PlayKeyDurationInt			= PlayNoteSet[i++] * timeK;
						AudioSynthesis.PlayKeyStrength[dataidx]		= PlayNoteSet[i++];

						AudioSynthesis.PlaKeyFs[dataidx] 			= (float)(AudioSynthesis.PlayKeyFsInt) / 100;			// 原始数据被放大100倍传递
						AudioSynthesis.PlayKeyDuration[dataidx] 	= (float)(AudioSynthesis.PlayKeyDurationInt) / 100;	// 原始数据被放大100倍传递

						// 显示条数
						//if (dataidx < 15)
						if (showKeyDlg > 0)
						{
							strText = strText + "\nTick:"
									+ AudioSynthesis.PlayKeyTick[dataidx]+",\tF:"
									+ AudioSynthesis.PlayKeyId[dataidx]+",\tDur:"
									+ AudioSynthesis.PlayKeyDuration[dataidx];
						}
					}

					// 如果首个note的tick小于0, 则把整个序列的tick推到>=0
					if (AudioSynthesis.PlayKeyTick[0] < 0)
					{
						int tShift = AudioSynthesis.PlayKeyTick[0];
						for(int i = 0; i < dataidx; i++)
						{
							AudioSynthesis.PlayKeyTick[i] += -tShift;
						}
					}

					// 创建完后设置对话框的属性
					if (showKeyDlg > 0)
					{
						AlertDialog.Builder builder = new AlertDialog.Builder(MusicScoreBook3DGL2.this);
						builder.setMessage(strText);
						AlertDialog alert = builder.create();
						alert.show();
					}

					AudioSynthesis.StartToPlay = true;
					AudioSynthesis.PausePlay = false;

//					// 位置指示
//					PosIndicateTask IndiTask = new PosIndicateTask();
//					IndiTask.execute(10);
					// 位置指示任务
					AudioSynthesis.IndiTask = new PosIndicateTaskGL2();
					//IndiTask.execute(10);

					// 启动合成，播放线程
					if (false)
					{
//		            	DownloadTask dTask = new DownloadTask();
//						dTask.execute(10);
					}
					else
					{
						AudioSynthesis.StartAudioSynthesisThread(mKeyAudio);
					}

				}

				//////////////////////

				// Call Play API..

				return;
			}
		});


		ImageButton buttonPause = (ImageButton)findViewById(R.id.imageButtonPause);
		/* 监听button的事件信息 */
		buttonPause.setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View arg0) {

				/////////////

				// Call Pause API..
				if (AudioSynthesis.PausePlay == false)
				{
					AudioSynthesis.PausePlay = true;
				}
				else
				{
					AudioSynthesis.PausePlay = false;
				}

				return;
			}
		});

		ImageButton buttonStop = (ImageButton)findViewById(R.id.imageButtonStop);
  		/* 监听button的事件信息 */
		buttonStop.setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View arg0) {

				/////////////

				// Call Stop API..
				AudioSynthesis.StartToPlay = false;

			}
		});

		ImageButton buttonStereo = (ImageButton)findViewById(R.id.ImageButtonStereo);
  		/* 监听button的事件信息 */
		buttonStereo.setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View arg0) {

				/////////////

				// Call Stop API..
//              	if (AudioSynthesis == null) // 重新创建, 重用有点问题:不能二次播放
//              	{
//              		AudioSynthesis = new CAudioSynthesisAndPlay(openfilename, pb);
//              	}
//              	AudioSynthesis.StereoOn = 1 - AudioSynthesis.StereoOn;
				StereoOn = 1 - StereoOn;

			}
		});

		ImageButton buttonVolume = (ImageButton)findViewById(R.id.imageButtonVolume);
		/* 监听button的事件信息 */
		buttonVolume.setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View arg0) {

				/////////////

				return;
			}
		});


		//requestWindowFeature(Window.FEATURE_NO_TITLE);
		// 改在<application 中增加android:theme="@android:style/Theme.Black.NoTitleBar.Fullscreen" >




//		ll = (LinearLayout)findViewById(R.id.layout);
////		ll = (RelativeLayout)findViewById(R.id.layout);
////		ll = (TableLayout)findViewById(R.id.layout);

		//setContentView(R.layout.grid_1);
		//mGrid = (GridView) findViewById(R.id.gridView1);
		//mGrid.setAdapter(new AppsAdapter());

		//pv = new PicView[MAX_PICVIEW_COUNT];

		// 定时器启动
//		mTimer = new Timer(true);
//		if (mTimer != null ){
//			if (mTimerTask != null){
//				mTimerTask.cancel();  //将原任务从队列中移除
//			}
//			//if (mDrawTask != null){
//			//	mDrawTask.cancel();  //将原任务从队列中移除
//			//}
//			mTimerTask = new MyTimerTask();  // 定时器任务：Pingserver、PNPing
//			mTimer.schedule(mTimerTask, 100, 5000);
//		}

		if (EPianoAndroidJavaAPI_inited == 0)
		{
			EPianoAndroidJavaAPI_inited = 1;

			ObEPianoAndroidJavaAPI = new EPianoAndroidJavaAPI(this);
			//ObEPianoAndroidJavaAPI.StartEngine();

			// 启动引擎
			ObEPianoAndroidJavaAPI.StartEngine();
		}

		if (false)
		{
			WindowManager m = getWindowManager();
			Display d = m.getDefaultDisplay();  //为获取屏幕宽、高
			RenderViews(d.getWidth(), d.getHeight());
		}

		return true;
	}

	public int CreatePV(int pvId)
	{
		pv[pvId] = new PicView(MusicScoreBook3DGL2.this, BmpConfig, ImgFileSurfix, mNoteImgSet, pvId, PicViewWidthOrg, PicViewHeightOrg, ImgWidth, ImgHeight);
		if (pv[pvId] == null)
		{
			Log.i("WG", "CreatePV() fail, pvId:" + pvId);
			return 0;
		}
		pv[pvId].setLayoutParams(lp);//设置布局参数

		return 1;
	}

	public boolean RenderViews(int vw, int vh)
	{
		long tick = 0;

		//mGrid = (GridView) findViewById(R.id.GridView1);
		//mView3D = (View) findViewById(R.id.view3D);

		// 设置参数给引擎
//		WindowManager m = getWindowManager();
//	    Display d = m.getDefaultDisplay();  //为获取屏幕宽、高
//	    PicViewWidthOrg = d.getWidth();
//	    PicViewHeightOrg = d.getHeight();
		PicViewWidthOrg = vw;
		PicViewHeightOrg = vh;

//	    if (false)
//	    {
//	    	int w = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);
//	        int h = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);
//	        mGrid.measure(w, h);
//	        int height =mGrid.getMeasuredHeight();
//	        int width = mGrid.getMeasuredWidth();
//	        PicViewWidthOrg = width;
//	        PicViewHeightOrg = height;
//	    }

		//PicViewHeightOrg = (int)(PicViewWidthOrg * whK);

		PicViewWidthCur = PicViewWidthOrg;
		PicViewHeightCur = PicViewHeightOrg;
		WinWidthCur = PicViewWidthOrg;
		WinHeightCur = PicViewHeightOrg;

		ImgWidth = (int)(PicViewWidthOrg * ImgAndViewK);
		ImgHeight = (int)(PicViewHeightOrg * ImgAndViewK);

		//ImgWidth /= 2;

//		int width = display.getWidth();
//		int height = display.getHeight();
		int leftmagin = PicViewWidthOrg / 20; //28; //30; //35; //30;
		int topmagin = leftmagin * 2;
		int rightmagin = leftmagin;
		int bottommagin = leftmagin * 2;
		int backgroundclr = 0xfffae9;
		r = ObEPianoAndroidJavaAPI.SetWinInfo(ImgWidth, ImgHeight, leftmagin, topmagin, rightmagin, bottommagin, backgroundclr);
		if (r <= 0)
		{
			Toast.makeText(this, "SetWinInfo fail.", Toast.LENGTH_SHORT).show();
			return false;
		}

		// 引擎生成demo
		if (openfilename.equals("demo"))
		{
			r =ObEPianoAndroidJavaAPI.DemoFile();	// 生成demo
			if (r <= 0)
			{
				Toast.makeText(this, "Can't open demo.", Toast.LENGTH_SHORT).show();
				return false;
			}
		}
		else
		{
			//String openfilename = "/mnt/sdcard/ComLang1_raw.sco";

			tick = System.currentTimeMillis();

			r = ObEPianoAndroidJavaAPI.OpenFile(openfilename);		// 打开指定文件
			if (r <= 0)
			{
				Toast.makeText(this, "Can't open " + openfilename + ".", Toast.LENGTH_SHORT).show();
				return false;
			}

			tick = System.currentTimeMillis() - tick;
			Log.i("WG", "open file, take time:" + tick + "ms.");

		}

		System.out.println("Start Paint...");

		// 引擎生成绘制指令
		tick = System.currentTimeMillis();
		r = ObEPianoAndroidJavaAPI.NotifyPaint();
		if (r <= 0)
		{
			Toast.makeText(this, "Paint fail.", Toast.LENGTH_SHORT).show();
			return false;
		}
		tick = System.currentTimeMillis() - tick;
		Log.i("WG", "c paint, take time:" + tick + "ms.");

		// 查询页数
		PicViewCount = ObEPianoAndroidJavaAPI.QueryPageCount();
		pv = new PicView[PicViewCount + 2]; //MAX_PICVIEW_COUNT];
		System.out.println("PicViewCount = " + PicViewCount); // print

		//ll.setColumnStretchable(columnIndex, isStretchable);

		lp = new GridView.LayoutParams(PicViewWidthOrg, PicViewHeightOrg);

		// 3d view
//		DisplayMetrics dm=new DisplayMetrics();
//        getWindowManager().getDefaultDisplay().getMetrics(dm);
//    	mGLSurfaceView = new MySurfaceView(this, pv, PicViewCount, dm);
//    	mGLSurfaceView.setLayoutParams(lp);

		// 添加java页
		//int PicViewCountLocal = PicViewCount;
		for(int i = 0; i < PicViewCount; i++)
		//for(int i = 0; i < 2; i++)	// 先只创建两页, 加快启动速度, 后面的页在MySurfaceView中动态创建, 搜索位置"mMusicScoreBook3D.CreatePV(pvId);"
		{
			//pv[i] = new PicView(MusicScoreBook3D.this, mNoteImgSet, i, PicViewWidthOrg, PicViewHeightOrg, ImgWidth, ImgHeight);
			int r = CreatePV(i);
			if (r == 0)
			{
				PicViewCount = i;
				break;
			}
			//pv[i].setLayoutParams(lp);//设置布局参数

			System.out.println("the view " + i + " is clickable " + pv[i].isClickable());
		}

		if (false)
		{
			//setContentView(R.layout.grid_1);
			//mGrid = (GridView) findViewById(R.id.GridView1);
			//loadApps();
			//mGrid.setAdapter(new AppsAdapter());
			mGrid.setColumnWidth(PicViewWidthOrg);
			mGrid.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
					//Toast.makeText(MainActivity.this, "mGrid.setOnItemClickListener: pic" + (position + 1), Toast.LENGTH_SHORT).show();

					// process after picview onTouchEvent(){ iMouseInPage = 1;}

					//            	if (onConfigurationChangedflag == 1)
					//            	{
					//            		onConfigurationChangedflag = 0;
					//            		ProcessZoom(BaseDistanceOf2Fingers);
					//            	}

					// find Mouse In which Page: foundPv
					int j = position;
					int jbak = 0;
					int foundPv = -1;

					mScale = (float)pv[j].getWidth() / PicViewWidthOrg;

					if (j < PicViewCount)
					{
						System.out.println("onClick------->>iPageId: " + j);

						//pv[j].mScale = mScale;

						foundPv = j;
						//ObEPianoAndroidJavaAPI.OnMouseMove(j, pv[j].iXInPage, pv[j].iYInPage);
						if (pv[j].mScale > 0)
						{
							float scalesum = pv[j].mScale / ImgAndViewK;
							if (pv[j].mScale <= 1)
							{
								// 缩小的情况，正常处理
								ObEPianoAndroidJavaAPI.OnMouseMove(j, (int)(pv[j].iXInPage / scalesum), (int)(pv[j].iYInPage / scalesum));
							}
							else
							{
								// 放大的情况，因为左右滑动窗口并没有使用正规的scrollview功能，而是通过图像平移方式来实现的，所以下面计算时手工加入了pv[j].OffsetX(窗口x方向滑动量)因素。
								ObEPianoAndroidJavaAPI.OnMouseMove(j, (int)((pv[j].iXInPage + pv[j].OffsetX) / scalesum), (int)(pv[j].iYInPage / scalesum));
							}

						}
						else
						{
							System.out.println("setOnClickListener, error, pv[" + j + "].mScale is 0.");
						}
					}

					// 指标画鼠标线
					if (true)
					{
						int MouseLinePara[];
						MouseLinePara = ObEPianoAndroidJavaAPI.QueryMouseLine();
						if (MouseLinePara.length != 5)
						{
							System.out.println("setOnClickListener, error, MouseLinePara.leng is " + MouseLinePara.length);
						}
						else
						{
							// clear mouse line in all pages
							for(j = 0; j < PicViewCount; j++)
							{
								pv[j].mMouseTimeSlice_iPageId = -1;

								//pv[j].mScale = mScale;
							}

							// set mouseline in page
							int iPageId = MouseLinePara[0];
							if (iPageId >= 0 && iPageId < PicViewCount)
							{
								pv[iPageId].mMouseTimeSlice_iPageId 	= MouseLinePara[0];	// -1 无效
								pv[iPageId].mMouseTimeSlice_iBarId 		= MouseLinePara[1];
								pv[iPageId].mMouseTimeSlice_xInPage 	= MouseLinePara[2];
								pv[iPageId].mMouseTimeSlice_y0InPage 	= MouseLinePara[3];
								pv[iPageId].mMouseTimeSlice_y1InPage 	= MouseLinePara[4];

								pv[iPageId].postInvalidate(); //(0, 0, pv[iPageId]., PicViewHeightCur);
							}

							// refresh win
							for(j = 0; j < PicViewCount; j++)
							{
								pv[j].postInvalidate(); //0, 0, PicViewWidthCur, PicViewHeightCur);
							}
						}
					}

				}
			});
		}

		// 改为在线程中动态加载
		// 查询组图指令
//		if (PicViewCount > 0)
//		{
//			LoadPagesToPicView(0, PicViewCount - 1);
//		}

		// over
		System.out.println("fresh PicView over.");


		return true;
	}

//	// 加载页面
//	public int LoadPagesToPicView(int startpage, int endpage)
//	{
//		if (endpage >= PicViewCount)
//		{
//			Log.i("WG", "LoadPages(), error, endpage is too big, " + endpage);
//
//			return 0;
//		}
//
//		// 查询组图指令
//		byte drawoderset[];
//		for(int i = startpage; i <= endpage; i++)
//		{
//			drawoderset = ObEPianoAndroidJavaAPI.QueryDrawOderSet(i);
//			//if (drawoderset.length > 0)
//			{
//				//pv[i].DrawOderSet = new byte[drawoderset.length];
//				pv[i].DrawOderSet = drawoderset;
//			}
//			if (drawoderset.length > 0)
//			{
//				pv[i].ReDraw = 1;
//				//pv[i].drawwgFromMem();
//
//				// 通知java绘制
//				pv[i].render();
//				//pv[i].postInvalidate(); //0, 0, PicViewWidthOrg, PicViewHeightOrg);
//				pv[i].DrawOderSet = null;
//			}
//		}
//
//		return 1;
//	}

	public boolean ProcessZoom(float CurDistanceOf2Fingers)
	{
		float scale = CurDistanceOf2Fingers / BaseDistanceOf2Fingers;// 当前两点间的距离除以手指落下时两点间的距离就是需要缩放的比例。

		if (mGrid == null)
		{
			return false;
		}

		//float ScaleTatalOld = mScale;

		// 如果起始比例<1，则一次手势最多只进行一次比例阶梯变更
		// 如果起始比例<1，后续比例>=1，则
		// 如果起始比例>=1，后续比例<1，则在进入<1前先绘制1:1比例

//		if (ScaleTatalOld < 1)
//		{
//			if (mZoomStatus == 0)
//			{
//				//mZoomStatus = 1;
//			}
//		}

		if (PicViewCount <= 0) // && PicViewWidthCur > 0)
		{
			return false;
		}

		// 改变view的尺寸

		//if (PicViewCount > 0) // && PicViewWidthCur > 0)
		{
			int dstW, dstH; // view w and h.
			zoomed = 1;

			//float ScaleOld = (float)pv[0].mBitmapPeerRctDst.right / PicViewWidthCur;
			float ScaleTotal = 1;

			//PicViewWidthCur = pv[0].mBitmapPeerRctDst.right;
			dstW = (int) (pv[0].getWidth() + (CurDistanceOf2Fingers - BaseDistanceOf2Fingers));
			//dstW = (int) (PicViewWidthCur + (value - BaseDistanceOf2Fingers));
			if (dstW > WinWidthCur * 2)
			{
				dstW = WinWidthCur * 2;
			}
//			else if (dstW < (int)(PicViewWidthOrg * 0.5))
//			{
//				dstW = (int)(PicViewWidthOrg * 0.5);
//			}
			ScaleTotal = (float)dstW / WinWidthCur; //PicViewWidthOrg;

			// 修正
			if (ScaleTotal >= 1)
			{
				mGrid.setNumColumns(1);
			}
			else // < 1
			{
				int rows = 0;

				{
					// 取1/2, 1/3, 1/4, 1/5
					int i = 0;
					float wi_org = 1;
					float wi = 1;
					float wi_h = 1;
					for(i = 0; i < 4; i++)
					{
						wi = wi_org / (i + 2);
						wi_h = (float)(wi * 1.5);
						if (ScaleTotal >= wi_h) // e.g. 0.75
						{
							Log.i("MainActivity", "ScaleTotal( " + ScaleTotal + ") >= wi_h(" + wi_h + ")");

							ScaleTotal = wi * 2;
							mGrid.setNumColumns(i + 1);
							break;
						}
						else if (ScaleTotal >= wi)	// e.g. 0.5
						{
							Log.i("MainActivity", "ScaleTotal( " + ScaleTotal + ") >= wi(" + wi + ")");

							ScaleTotal = wi;

//							dstW = (int)(ScaleTotal * PicViewWidthOrg);
//							dstH = (int)(dstW * PicViewHeightOrg / PicViewWidthOrg);
//								rows = (int)(((float)(PicViewCount)) / mGrid.getNumColumns() + 0.9);
//							if (dstH * rows > PicViewHeightOrg) // 如果缩得太小，则窗口下部分区域有大片空白，为防止这种情况，不允许再缩小了.
//							{
//								mGrid.setNumColumns(i + 2);
//							}
//							else
//							{
//								ScaleTotal = wi_org / (i - 1 + 2);
//							}
							mGrid.setNumColumns(i + 2);

							break;
						}
					}
					if (i >= 4)
					{
						i = 3;

						ScaleTotal = wi_org / (i + 2);

						mGrid.setNumColumns(5);
					}

					// 修正：如果缩得太小，则窗口下部分区域有大片空白，为防止这种情况，不允许再缩小了.
					dstH = (int)(dstW * whK);
					rows = (int)(((float)(PicViewCount)) / mGrid.getNumColumns() + 0.9);
					if (dstH * rows < PicViewHeightOrg / 2)
					{
						if (i > 0)
						{
							i = i - 1;		// 回退/放大比例
							mGrid.setNumColumns(i + 2);
							ScaleTotal = wi_org / (i + 2);
						}
					}

					dstW = (int)(ScaleTotal * WinWidthCur); //PicViewWidthOrg);
					dstH = (int)(dstW * whK);
				}


			}

			//mScale = ScaleTotal;
			dstH = (int)(dstW * whK);

//			lp = new LinearLayout.LayoutParams(dstW, dstH);
//			lp = new TableLayout.LayoutParams(dstW, dstH);
//			lp = new GridView.LayoutParams(dstW, dstH);
			float Scale = (float)dstW / PicViewWidthOrg;

			// 比例 <1时，一次手势只允许变化一次缩放等级
			if (ScaleTotal <= 1)
			{
				if (mScale != Scale) // scale变化了
				{
					//BaseDistanceOf2Fingers = value;

					mZoomStatus = 1; // hold住，不响应后续move事件，直到松手
				}
			}

			mScale = Scale;
			PicViewWidthCur = dstW;
			for (int j = 0; j < PicViewCount; j++) {
				pv[j].mScale = mScale;

				lp = (GridView.LayoutParams)pv[j].getLayoutParams();
				if (lp == null)
				{
					lp = new GridView.LayoutParams(dstW, dstH);
				}
				lp.width = dstW;
				lp.height = dstH;
				pv[j].setLayoutParams(lp);	// 设置布局参数

				pv[j].mBitmapPeerRctDst.right =  dstW;
				pv[j].mBitmapPeerRctDst.bottom =  dstH;
			}
			for (int j = 0; j < PicViewCount; j++) {
				pv[j].postInvalidate(); //0, 0, pv[j].mBitmapPeerRctDst.right, pv[j].mBitmapPeerRctDst.bottom);
			}

		}


		return true;
	}

	public boolean ProcessRelayout(float CurDistanceOf2Fingers)
	{
		float scale = CurDistanceOf2Fingers / BaseDistanceOf2Fingers;// 当前两点间的距离除以手指落下时两点间的距离就是需要缩放的比例。

		if (PicViewCount <= 0) // && PicViewWidthCur > 0)
		{
			return false;
		}

		if (mGrid == null)
		{
			return false;
		}

		// 改变view的尺寸

		//if (PicViewCount > 0) // && PicViewWidthCur > 0)
		{
			int dstW, dstH; // view w and h.
			zoomed = 1;

			//float ScaleOld = (float)pv[0].mBitmapPeerRctDst.right / PicViewWidthCur;
			float ScaleTotal = 1;

			//PicViewWidthCur = pv[0].mBitmapPeerRctDst.right;
			dstW = (int) (pv[0].getWidth() + (CurDistanceOf2Fingers - BaseDistanceOf2Fingers));
			//dstW = (int) (PicViewWidthCur + (value - BaseDistanceOf2Fingers));
			if (dstW > WinWidthCur * 2)
			{
				dstW = WinWidthCur * 2;
			}
//			else if (dstW < (int)(PicViewWidthOrg * 0.5))
//			{
//				dstW = (int)(PicViewWidthOrg * 0.5);
//			}
			ScaleTotal = (float)dstW / WinWidthCur; //PicViewWidthOrg;

			// 修正
			if (ScaleTotal >= 1)
			{
				mGrid.setNumColumns(1);
			}
			else // < 1
			{
				int rows = 0;

				{
					// 取1/2, 1/3, 1/4, 1/5
					int i = 0;
					float wi_org = 1;
					float wi = 1;
					float wi_h = 1;
					for(i = 0; i < 4; i++)
					{
						wi = wi_org / (i + 2);
						wi_h = (float)(wi * 1.5);
						if (ScaleTotal >= wi_h) // e.g. 0.75
						{
							Log.i("WG", "ScaleTotal( " + ScaleTotal + ") >= wi_h(" + wi_h + ")");

							ScaleTotal = wi * 2;
							mGrid.setNumColumns(i + 1);
							break;
						}
						else if (ScaleTotal >= wi)	// e.g. 0.5
						{
							Log.i("MainActivity", "ScaleTotal( " + ScaleTotal + ") >= wi(" + wi + ")");

							ScaleTotal = wi;

//							dstW = (int)(ScaleTotal * PicViewWidthOrg);
//							dstH = (int)(dstW * PicViewHeightOrg / PicViewWidthOrg);
//								rows = (int)(((float)(PicViewCount)) / mGrid.getNumColumns() + 0.9);
//							if (dstH * rows > PicViewHeightOrg) // 如果缩得太小，则窗口下部分区域有大片空白，为防止这种情况，不允许再缩小了.
//							{
//								mGrid.setNumColumns(i + 2);
//							}
//							else
//							{
//								ScaleTotal = wi_org / (i - 1 + 2);
//							}
							mGrid.setNumColumns(i + 2);

							break;
						}
					}
					if (i >= 4)
					{
						i = 3;

						ScaleTotal = wi_org / (i + 2);

						mGrid.setNumColumns(5);
					}

					// 修正：如果缩得太小，则窗口下部分区域有大片空白，为防止这种情况，不允许再缩小了.
					dstH = (int)(dstW * whK);
					rows = (int)(((float)(PicViewCount)) / mGrid.getNumColumns() + 0.9);
					if (dstH * rows < PicViewHeightOrg / 2)
					{
						if (i > 0)
						{
							i = i - 1;		// 回退/放大比例
							mGrid.setNumColumns(i + 2);
							ScaleTotal = wi_org / (i + 2);
						}
					}

					dstW = (int)(ScaleTotal * WinWidthCur); //PicViewWidthOrg);
					dstH = (int)(dstW * whK);
				}


			}

			//mScale = ScaleTotal;
			dstH = (int)(dstW * whK);

//			lp = new LinearLayout.LayoutParams(dstW, dstH);
//			lp = new TableLayout.LayoutParams(dstW, dstH);
//			lp = new GridView.LayoutParams(dstW, dstH);
			float Scale = (float)dstW / PicViewWidthOrg;

			// 比例 <1时，一次手势只允许变化一次缩放等级
			if (ScaleTotal <= 1)
			{
				if (mScale != Scale) // scale变化了
				{
					//BaseDistanceOf2Fingers = value;

					mZoomStatus = 1; // hold住，不响应后续move事件，直到松手
				}
			}

			mScale = Scale;
			PicViewWidthCur = dstW;
			for (int j = 0; j < PicViewCount; j++) {
				pv[j].mScale = mScale;

				lp = (GridView.LayoutParams)pv[j].getLayoutParams();
				if (lp == null)
				{
					lp = new GridView.LayoutParams(dstW, dstH);
				}
				lp.width = dstW;
				lp.height = dstH;
				pv[j].setLayoutParams(lp);	// 设置布局参数

				pv[j].mBitmapPeerRctDst.right =  dstW;
				pv[j].mBitmapPeerRctDst.bottom =  dstH;
			}
			for (int j = 0; j < PicViewCount; j++) {
				pv[j].postInvalidate(); //0, 0, pv[j].mBitmapPeerRctDst.right, pv[j].mBitmapPeerRctDst.bottom);
			}

		}


		return true;
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub

		int action = event.getAction();

		switch (action) {
			case MotionEvent.ACTION_DOWN:
				//Log.i("MainActivity", "MainActivity-dispatchTouchEvent action = action down");

				// clear iMouseInPage of all PicView
				for(int j = 0; j < PicViewCount; j++)
				{
					pv[j].iMouseInPage = 0;
				}

				BaseDistanceOf2Fingers = 0;
				last_x = event.getRawX();
				delta_x = 0;
				zoomed = 0;

				break;
			case MotionEvent.ACTION_MOVE:
				//Log.i("MainActivity", "MainActivity-dispatchTouchEvent action = action move");
//        	// new
//			if (event.getPointerCount() == 2) {
//
//			}

				// new
				if (event.getPointerCount() == 2) {
					float x0, x1, y0, y1;
					x0 = event.getX(0);
					x1 = event.getX(1);
					y0 = event.getY(0);
					y1 = event.getY(1);
					float xx = x0 - x1;
					float yy = y0 - y1;
					float CurDistanceOf2Fingers = (float) Math.sqrt(xx * xx + yy * yy);// 计算两点的距离
					if (BaseDistanceOf2Fingers == 0) {
						BaseDistanceOf2Fingers = CurDistanceOf2Fingers;
					} else {
						if (true) // value - BaseDistanceOf2Fingers >= 10 || value - BaseDistanceOf2Fingers <= -10)
						{
							if (mZoomStatus == 0)
							{
								ProcessZoom(CurDistanceOf2Fingers);
								//BaseDistanceOf2Fingers = value;
							}
						}
					}
				} else if (event.getPointerCount() == 1) {
					float xx = event.getRawX();
					float yy = event.getRawY();
					int j = 0;
//				xx -= last_x;
//				yy -= last_y;
//				if (xx >= 10 || yy >= 10 || xx <= -10 || yy <= -10)
//					//img_transport(x, y); // 移动图片位置
					delta_x = xx - last_x;	// delta_x < 0, means move left
					//Log.i("MainActivity", "delta_x( " + delta_x + ") >= xx(" + xx + ")");

					// 左右滑屏
					if (mScale > 1)
					{
						// 放大的情况才需要更新各页的OffsetX

						int offsetx = pv[j].OffsetX;
//					if (pv[j].OffsetX)
						offsetx += -delta_x;
						if (offsetx < 0)
						{
							offsetx = 0;
						}
						else if (offsetx + WinWidthCur > pv[j].mBitmapPeerRctDst.right) // pv[j].getWidth())
						{
							offsetx = pv[j].mBitmapPeerRctDst.right - WinWidthCur;
						}

						for (j = 0; j < PicViewCount; j++) {
							pv[j].OffsetX = offsetx;
						}
						for (j = 0; j < PicViewCount; j++) {
							pv[j].postInvalidate(); //0, 0, pv[j].mBitmapPeerRctDst.right, pv[j].mBitmapPeerRctDst.bottom);
						}
					}
					else
					{
						// 正常情况，清OffsetX

						if (pv[j].OffsetX != 0)
						{
							for (j = 0; j < PicViewCount; j++) {
								pv[j].OffsetX = 0;
							}
							for (j = 0; j < PicViewCount; j++) {
								pv[j].postInvalidate(); //0, 0, pv[j].mBitmapPeerRctDst.right, pv[j].mBitmapPeerRctDst.bottom);
							}
						}
					}

					last_x = xx;
					last_y = yy;
				}
				break;
			case MotionEvent.ACTION_UP:
				//Log.i("MainActivity", "MainActivity-dispatchTouchEvent action = action up");

				mZoomStatus = 0;

				//if (mScale > 1)
				if (zoomed == 1)
				{
					int sI, sF;
					sI = (int)mScale;
					sF = (int)(mScale * 10) % 10;
					//Toast.makeText(this, "比例: " + sI + "." + sF, Toast.LENGTH_SHORT).show();
					Toast.makeText(this, "比例: " + sI + "." + sF, Toast.LENGTH_SHORT).show();
				}
				else
				{

				}

				// 暂时播放控制...
				if (false)
				{
					float xx = event.getRawX();
					float yy = event.getRawY();
					if (xx < 30 && yy < 30)
					{
						int PlayNoteSet[];
						PlayNoteSet = ObEPianoAndroidJavaAPI.GetPlayNoteSet();
					}
				}

				break;

			default:
				break;
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

//	@Override
//	public void onClick(View view) {
//		// TODO Auto-generated method stub
//		int id = view.getId();
////		switch (id) {
////		case R.id.view_btn:
////			Log.i("MainActivity", "MainActivity view_btn clicked");
////			break;
////
////		default:
////			break;
////		}
//	}


	// wg add
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		// Checks the orientation of the screen
		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			Toast.makeText(this, "横屏模式", Toast.LENGTH_SHORT).show();
		} else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
			Toast.makeText(this, "竖屏模式", Toast.LENGTH_SHORT).show();
		}

		onConfigurationChangedflag = 1;

		// 改变view的尺寸，mGrid.getNumColumns()保持不变
		if (false) // 此段代码存在问题, 暂时关闭, pv[j].mBitmapPeerRctDst.right的计算方法不正确
		{
			WindowManager m = getWindowManager();
			Display d = m.getDefaultDisplay(); // 为获取屏幕宽、高
			WinWidthCur = d.getWidth();
			WinHeightCur = d.getHeight();
			if (mGrid == null)
			{
				PicViewWidthCur = (int)(WinWidthCur / 1);
			}
			else
			{
				PicViewWidthCur = (int)(WinWidthCur / mGrid.getNumColumns());
			}
			PicViewHeightCur = (int) (PicViewWidthCur * whK);
			if (PicViewWidthCur == 0)
			{
				Toast.makeText(this, "Caution, win width is 0.", Toast.LENGTH_SHORT).show();
			}
			if (mGrid != null)
			{
				mGrid.setColumnWidth(PicViewWidthCur);
			}
			float Scale = (float)PicViewWidthCur / PicViewWidthOrg;

			//		lp = new GridView.LayoutParams(destPageW, destPageH);
			for (int j = 0; j < PicViewCount; j++) {
				pv[j].mScale = Scale;
				lp = (GridView.LayoutParams)pv[j].getLayoutParams();
				if (lp == null)
				{
					lp = new GridView.LayoutParams(PicViewWidthCur, PicViewHeightCur);
				}
				lp.width = PicViewWidthCur;
				lp.height = PicViewHeightCur;
				pv[j].setLayoutParams(lp);// 设置布局参数

				pv[j].mBitmapPeerRctDst.right =  PicViewWidthCur;
				pv[j].mBitmapPeerRctDst.bottom =  PicViewHeightCur;

				pv[j].OffsetX = 0;
			}
			//		for (int j = 0; j < PicViewCount; j++) {
			//			pv[j].OffsetX = 0;
			//		}
			for (int j = 0; j < PicViewCount; j++) {
				pv[j].postInvalidate(); //0, 0, pv[j].mBitmapPeerRctDst.right, pv[j].mBitmapPeerRctDst.bottom);
			}
		}
	}


//	@Override
//	// 对每个子View视图进行布局
//	protected void onLayout(boolean changed, int l, int t, int r, int b) {
//		// TODO Auto-generated method stub
//		// 通过init()方法,我们为该ViewGroup对象添加了三个视图
//		int childCount = getChildCount();
//		int startLeft = 0; // 设置每个子View的起始横坐标
//		int startTop = 10; // 每个子View距离父视图的位置, 简单设置为10px,可以理解为android:margin =
//							// 10px;
//
//		Log.i("WG", "**** onLayout start ****");
//		for (int i = 0; i < childCount; i++) {
//			View child = getChildAt(i);
//			child.layout(startLeft, startTop,
//					startLeft.child.getMeasuredWidth(),
//					startTop + child.getMeasuredHeight());
//			startLeft = startLeft + child.getMeasuredWidth() + 10; // 校准startLeft值,View之间的间距设为10px;
//			Log.i("WG", "**** onLayout startLeft ****" + startLeft);
//		}
//	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public static void readFileByBytes(String fileName) {

		File file = new File(fileName);
		InputStream in = null;

		try {
			System.out.println("以字节为单位读取文件内容，一次读一个字节：");
			// 一次读一个字节
			in = new FileInputStream(file);
			int tempbyte;
			while ((tempbyte = in.read()) != -1) {
				System.out.write(tempbyte);
			}
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		try {
			System.out.println("以字节为单位读取文件内容，一次读多个字节：");
			// 一次读多个字节
			byte[] tempbytes = new byte[100];
			int byteread = 0;
			in = new FileInputStream(fileName);
			//ReadFromFile.showAvailableBytes(in);
			// 读入多个字节到字节数组中，byteread为一次读入的字节数
			while ((byteread = in.read(tempbytes)) != -1) {
				System.out.write(tempbytes, 0, byteread);
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e1) {
				}
			}
		}
	}

	private class MyTimerTask extends TimerTask{
		@Override  public void run() {
			boolean pkraterewind = false;

			//Message msg = mHandler.obtainMessage(EVENT_PINGSERVER);
			//msg.sendToTarget();
			timercounter++;
			//Log.i("Mytimertask:", "timercounter="+timercounter);

//			if (false)
//			{
//				if (EPianoAndroidJavaAPI_inited == 1)
//				{
//					EPianoAndroidJavaAPI_inited = 2;
//
//					//ObEPianoAndroidJavaAPI = new EPianoAndroidJavaAPI(this);
//					ObEPianoAndroidJavaAPI.StartEngine();
//					WindowManager m = getWindowManager();
//				    Display d = m.getDefaultDisplay();  //为获取屏幕宽、高
//					int width = d.getWidth();
//					int height = d.getHeight();
////					int width = display.getWidth();
////					int height = display.getHeight();
//					int leftmagin = 30;
//					int topmagin = 30;
//					int rightmagin = 30;
//					int bottommagin = 30;
//					ObEPianoAndroidJavaAPI.SetWinInfo(width, height, leftmagin, topmagin, rightmagin, bottommagin);
//					ObEPianoAndroidJavaAPI.DemoFile();
//					ObEPianoAndroidJavaAPI.NotifyPaint();
//
//					PicViewCount = ObEPianoAndroidJavaAPI.QueryPageCount();
//					int PicViewCountLocal = PicViewCount;
//					for(int i = 0; i < PicViewCount; i++)
//					{
//						pv[i] = new PicView(MainActivity.this, i, width, height);
//						pv[i].setPadding(0, 20, 0, 20);
//						LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, height);
//						// View view =LayoutInflater.from(this).inflate(R.layout.view_item, null);//也可以从XML中加载布局
//				        //LinearLayout view = new LinearLayout(this);
//						ll.addView(pv[i]);
//						pv[i].setLayoutParams(lp);//设置布局参数
//					}
//					DrawMusicScore();
//					for(int i = 0; i < PicViewCount; i++)
//					{
//						pv[i].postInvalidate(0, 0, width, height);
//					}
//
//					//PicView1.postInvalidate(0, 0, 400, 1592);
//				}
//			}



			//ObEPianoAndroidJavaAPI.ipagecount;
			//Toast.makeText(MainActivity.this, "AppLayerAddAPage" + String.valueOf(ObEPianoAndroidJavaAPI.ipagecount), Toast.LENGTH_SHORT).show();

			/*
			// ping服务端/中控端
			try {
				//pingServer(0);
			} catch (IOException e) {
				e.printStackTrace();
			}


			//ImageView pictureView = (ImageView) convertView.findViewById(R.id.picture_view);
			ViewGroup.LayoutParams layoutParams = PicView1.getLayoutParams();
			int w = layoutParams.width - timercounter;
			if (w > 1)
			{
				layoutParams.width = w;
			}
			//layoutParams.height = 768;
			//layoutParams.
			PicView1.setLayoutParams(layoutParams);
			*/


		}
	}

	/**
	 * Play位置指示任务
	 * 注: 如果在DownloadTask中进行位置指示，有可能因为audioTrack.write()阻塞的原因，导致指示器工作不流畅
	 */
	class PosIndicateTaskGL2 extends AsyncTask<Integer, Integer, String>{
		@Override
		protected void onPreExecute() {
			//第一个执行方法
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(Integer... params) {

			long PlayTimeSumMinusBufDelay = 0;
			long CurT = System.currentTimeMillis();
			long StartT = AudioSynthesis.mStartPlayTime;

			if (StartT == -1)
			{
				return "空串";
			}

			while(AudioSynthesis.mStartPlayTime != -1 && AudioSynthesis.mPosIndicator != null)	// CurPosIndicatorId
			{
				{
					CurT = System.currentTimeMillis();

					//long PlayTimeSumMinusBufDelay = PlayTimeSum - audioTrackBufferDelay;
					PlayTimeSumMinusBufDelay = (CurT - AudioSynthesis.mStartPlayTime) - AudioSynthesis.audioTrackBufferDelay;
					PlayTimeSumMinusBufDelay -= 3 * 100;	// test, 指示线快于声音, 这里补偿对齐一下
					if (PlayTimeSumMinusBufDelay < 0)
					{
						PlayTimeSumMinusBufDelay = 0;
					}

					PlayTimeSumMinusBufDelay -= AudioSynthesis.PauseTime;	// 扣除暂停时间

					AudioSynthesis.CurPosIndicatorId = ((int) PlayTimeSumMinusBufDelay / AudioSynthesis.PosIndicatorRes);

					synchronized(AudioSynthesis.mPosIndicator)
					{
						DrawPosIndicator(AudioSynthesis.CurPosIndicatorId, AudioSynthesis.mPosIndicator);
					}
				}

				try {
					Thread.sleep(50); // 10);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			mGLSurfaceView.PlayPageId = -1;

			Log.i("WG", "PosIndicateTask over.");

			return "空串";
		}

		@Override
		protected void onProgressUpdate(Integer... progress) {
			super.onProgressUpdate(progress);
		}

		@Override
		protected void onPostExecute(String result) {
			//doInBackground返回时触发，换句话说，就是doInBackground执行完后触发
			//这里的result就是上面doInBackground执行后的返回值，所以这里是"执行完毕"
			//setTitle(result);
			super.onPostExecute(result);
		}

		// PosIndicator
		int DrawPosIndicator(int CurPosIndicatorId, int PosIndicator[]) {

			if (CurPosIndicatorId >= PosIndicator.length)
			{
				Log.i("WG", "Error, CurPosIndicatorId:" + CurPosIndicatorId + " > PosIndicator.length:" + PosIndicator.length);

				return 0;
			}

			// 琶音情况下可能出现indicator短暂倒退的情况, 用此变量防止倒退现象
			if (AudioSynthesis.PrePosIndicatorId == -1)
			{
				AudioSynthesis.PrePosIndicatorId = CurPosIndicatorId;
			}
			else if (AudioSynthesis.PrePosIndicatorId > CurPosIndicatorId)
			{
				CurPosIndicatorId = AudioSynthesis.PrePosIndicatorId;
			}
			else
			{
				AudioSynthesis.PrePosIndicatorId = CurPosIndicatorId;
			}

			int playkeyid = PosIndicator[CurPosIndicatorId];

			if (playkeyid >= AudioSynthesis.PlayKeyCount)
			{
				Log.i("WG", "Error, playkeyid:" + playkeyid + " > PlayKeyCount:" + AudioSynthesis.PlayKeyCount);

				return 0;
			}

			int iVPId = AudioSynthesis.PlayKeyVPId[playkeyid];
			int iBarId = AudioSynthesis.PlayKeyBarId[playkeyid];
			int iTSId = AudioSynthesis.PlayKeyTSId[playkeyid];

//					Log.i("WG", "CurPosIndicatorId:" + CurPosIndicatorId
//							+ ", playkeyid:" + playkeyid
//							+ ", iVPId:" + iVPId
//							+ ", iBarId:" + iBarId
//							+ ", iTSId:" + iTSId);

			int PlayLinePara[];
			PlayLinePara = ObEPianoAndroidJavaAPI.QueryPlayLinePos(iVPId,
					iBarId, iTSId);
			{
				if (PlayLinePara.length != 5) {
					System.out.println("setOnClickListener, error, MouseLinePara.leng is "
							+ PlayLinePara.length);
				}
				else
				{
					// clear mouse line in all pages
//					 for(int j = 0; j < PicViewCount; j++)
//					 {
//						 pv[j].mPlayLine_iPageId = -1;
//						 //pv[j].mScale = mScale;
//					 }

					// set mouseline in page
					int iPageId = PlayLinePara[0];
					if (iPageId >= 0 && iPageId < PicViewCount) {
						pv[iPageId].mPlayLine_iPageId = PlayLinePara[0]; // -1
						// 无效
						pv[iPageId].mPlayLine_iBarId = PlayLinePara[1];
						pv[iPageId].mPlayLine_xInPage = PlayLinePara[2]-1;	// -1与mouseline错开
						pv[iPageId].mPlayLine_y0InPage = PlayLinePara[3];
						pv[iPageId].mPlayLine_y1InPage = PlayLinePara[4];

						pv[iPageId].postInvalidate(); // (0, 0, pv[iPageId].,
						// PicViewHeightCur);

//						mGLSurfaceView.PalyLineY0 = mGLSurfaceView.PageY2View3DY(pv[iPageId].mMouseTimeSlice_xInPage, pv[iPageId].mMouseTimeSlice_y0InPage);
//						mGLSurfaceView.PalyLineY1 = mGLSurfaceView.PageY2View3DY(pv[iPageId].mMouseTimeSlice_xInPage, pv[iPageId].mMouseTimeSlice_y0InPage);
//
//						mGLSurfaceView.MouseLineY0 = yMapFromView3D2PicView_R(tp, LeftOrRight, pv[iPageId].mMouseTimeSlice_xInPage, pv[iPageId].mMouseTimeSlice_y0InPage, pv[iPageId].mBitmapPeerRct.bottom);
//						MouseLineY1 = yMapFromView3D2PicView_R(tp, LeftOrRight, pv[iPageId].mMouseTimeSlice_xInPage, pv[iPageId].mMouseTimeSlice_y1InPage, pv[iPageId].mBitmapPeerRct.bottom);
//						MouseLineY0 = -(MouseLineY0 - (int)tp.height);	// "-()", 为什么要上下颠倒?
//						MouseLineY1 = -(MouseLineY1 - (int)tp.height);

						// 自动翻页控制
						if (AutoScrollPageOnPlay > 0)
						{
							// 检查: 本页是右页，且本页播放完毕，需要翻页
							if (mGLSurfaceView.PlayPageId >= 0
									&& mGLSurfaceView.PlayPageId == iPageId - 1
									&& mGLSurfaceView.PlayPageLeftOrRight == 1)
							//if (mGLSurfaceView.rightPaperId == iPageId - 1)
							{
								// 触发翻页

								mGLSurfaceView.OneTimeOnSingleModeCount = 0;
								//mGLSurfaceView.leftPaperId = mGLSurfaceView.PlayPaperId - 1;
								//mGLSurfaceView.rightPaperId = mGLSurfaceView.PlayPaperId;
								//mGLSurfaceView.GrabPaperId = mGLSurfaceView.rightPaperId;

								// 模拟向左划指操作, 触发翻页
								mGLSurfaceView.ScrollPageMode = MySurfaceViewGL2.SCROLL_PAGE_MOD.SCROLL_PAGE_MODE_SINGLE_PAGE;
								mGLSurfaceView.yAngleWg = -10;
							}

//							Log.i("WG", "AutoScrollPageOnPlay"
//									+ ",PlayPageId:" + mGLSurfaceView.PlayPageId
//									+ ",iPageId:" + iPageId
//									+ ",PlayPaperId:" + mGLSurfaceView.PlayPaperId
//									+ ",leftPaperId:" + mGLSurfaceView.leftPaperId
//									+ ",rightPaperId:" + mGLSurfaceView.rightPaperId
//									+ ",PlayPageLeftOrRight:" + mGLSurfaceView.PlayPageLeftOrRight
//									);
						}

						mGLSurfaceView.PlayPageId = iPageId;
					}
				}
			}

			// 绘图通知
//			for (int j = 0; j < PicViewCount; j++) {
//				pv[j].postInvalidate(); // 0, 0, PicViewWidthCur,
//										// PicViewHeightCur);
//			}

			return 1;
		}
	}




}

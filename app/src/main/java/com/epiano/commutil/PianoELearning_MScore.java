package com.epiano.commutil;

//import android.R;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;

//import android.epiano.com.commutil.UvcWebCamApp.CaptureListener;
//import android.epiano.com.commutil.UvcWebCamApp.StartThread;
//import android.epiano.com.commutil.av.ictvoip.androidvideo.capture.AudioVideo;
//import android.epiano.com.commutil.CNoteImgSet;
//import android.epiano.com.commutil.R;
//import android.epiano.com.commutil.R;


//import com.example.android.apis.view.Grid1;

//import com.example.android.apis.R;
//import com.example.android.apis.view.Grid1;
//import com.example.android.apis.view.Grid1.AppsAdapter;


public class PianoELearning_MScore extends View // extends Activity
{ // implements OnClickListener {

//	private ImageView mImag;
//	//  private int width = 320;
//	//  private int height = 240;
//	private int width = 640;
//	private int height = 480;
//	private String devname = "/dev/video9"; //3";
//	private byte[] mdata;
//	private Handler mHandler;
//	private int numbuf = 0;
//	private int index = 0;
//	private int ret = 0;
//	private int ctype = 1;//0 is zc301 1 is uvc camera
//	public Button mcap;
//	private Bitmap bitmap;
//	private Bitmap bmp;
//	private int[] rgb;

//	// camera
//	UvcWebCam Fimcgzsd;	// jni
//	// AVCom
//	AudioVideo AVCom;


	/////////////////////////////



	Config BmpConfig = Config.ARGB_8888;
	//Config BmpConfig = Bitmap.Config.RGB_565;
	String ImgFileSurfix = ".bmp";

	private PicView PicView1;// = (PicView)this.findViewById(R.id.PicView1);
	private Timer mTimer;
	//	private MyTimerTask mTimerTask;
	private int timercounter;
	private PicView pv[];
	private int PicViewCount = 0;

//	private SurfaceView VideoPeer;
//	private ImageView VideoPeerIV;
//	private SurfaceView VideoSelf;

	int PicViewWidthOrg = 0;					// 初始窗口/图像宽度
	int PicViewHeightOrg = 0;
	int PicViewWidthCur = PicViewWidthOrg;		// 当前视图窗口宽度
	int PicViewHeightCur = PicViewHeightOrg;
	public int WinWidthCur = PicViewWidthOrg;			// 当前物理窗口宽度
	public int WinHeightCur = PicViewHeightOrg;
	int ImgWidth = PicViewWidthOrg;				// JNI图像宽度
	int ImgHeight = PicViewHeightOrg;
	float ImgAndViewK = 1;						// JNI中图像宽度与View宽度的比值
	float whK = (float)1.6;						// 图像长宽比

	final float PI = (float)3.14159265;

	//int PicViewLastMouseMovePageId = -1;	// 上一个pageid，用于优化重图时减少重图的页数, -1无效

	int onConfigurationChangedflag = 0;

	int EPianoAndroidJavaAPI_inited = 0;
	EPianoAndroidJavaAPI ObEPianoAndroidJavaAPI;

	private LinearLayout ll;
	//	LinearLayout.LayoutParams lp;
//	RelativeLayout.LayoutParams lp;
//	private TableLayout ll;
//	TableLayout.LayoutParams lp;
//	LinearLayout.LayoutParams lp2;
	GridView mGrid;
	//    GridLayout ll;
	GridView.LayoutParams lp;

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
	//CKeyAudio mKeyAudio[]; // = new CKeyAudio[88];
	static int MaxPlayKeyId = 3000;
	int PlayKeyCount = 0;
	//PlayKeyBarId_TSId[]
	int PlayKeyVPId[] 			= new int [MaxPlayKeyId];	// 音符标记
	int PlayKeyBarId[] 			= new int [MaxPlayKeyId];	// 音符标记
	int PlayKeyTSId[] 			= new int [MaxPlayKeyId];	// 音符标记, 对应TIME_NODE.iTimeSliceId
	int PlayKeyTick[] 			= new int [MaxPlayKeyId];	// 音符起始时刻
	int PlayKeyId[] 			= new int [MaxPlayKeyId];	// 音高 , 1 based
	int PlayKeyFsInt;
	float PlaKeyFs[] 			= new float [MaxPlayKeyId];	// 频率
	int PlayKeyDurationInt;
	float PlayKeyDuration[] 	= new float [MaxPlayKeyId];	// 时长
	int PlayKeyStrength[] 		= new int [MaxPlayKeyId];	// 强度

	CAudioSythAndPlayThread mAudioSythAndPlayThread;

	ProgressBar pb;
	private boolean StartToPlay = false; //false;
	private boolean PausePlay = false; //false;
	int PauseTime = 0;
	AudioTrack audioTrack = null;
	int AudioChannels = 2; 	// 1;
	int StereoOn = 1; 		// 0: close, 1: on;

	int CurPosIndicatorId = -1;
	int mPosIndicator[];		// Play位置指示
	long mStartPlayTime = -1;
	int mRealPlaycount = 0;
	int audioTrackBufferDelay = 0;
	int PosIndicatorRes = 100; // ms
	final int SAMPLE_RATE = 16000; // 11025;
	PosIndicateTask IndiTask;

	Context mCtx;

	//WindowManager winmanager = getWindowManager();
	//Display display = winmanager.getDefaultDisplay();  //为获取屏幕宽、高

	public PianoELearning_MScore(Context context, AttributeSet attrs, String openfilenameIn, LinearLayout llIn, GridView GridV) {
		super(context, attrs);

		mCtx = context;
		openfilename = openfilenameIn;
		ll = llIn;
		mGrid = GridV;

		onCreate(null);
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

//            System.out.println("AppsAdapter, position is " + position + ", convertView is " + convertView + ", pv[position].iPageId is " + pv[position].iPageId + ".");

//            if (convertView == null) {
////                //i = new ImageView(Grid1.this);
////            	//i = new PicView(MainActivity.this, position, PicViewWidthOrg, PicViewHeightOrg);
////            	i = pv[position];
////                //i.setScaleType(ImageView.ScaleType.FIT_CENTER);
////                i.setLayoutParams(new GridView.LayoutParams(PicViewWidthOrg, PicViewHeightOrg));
//////                i.setScaleX((float)0.5);
//////                i.setScaleY((float)0.5);
////            	i.setAdjustViewBounds(false);
////                i.setScaleType(ImageView.ScaleType.CENTER_CROP);
//
//            	i = new ImageView(MainActivity.this);
//                i.setScaleType(ImageView.ScaleType.FIT_CENTER);
//                i.setLayoutParams(new GridView.LayoutParams(50, 50));
//
//            } else {
//                i = (PicView) convertView;
//            }
//
//            ResolveInfo info = mApps.get(position);
//            i.setImageDrawable(info.activityInfo.loadIcon(getPackageManager()));

//            int iPageId = position;
//            i = pv[iPageId];
//
//            return i;

			return  pv[position];
		}

		public void setSelection(int position)
		{
			selectItem = position;
		}

		public final int getCount() {
			return PicViewCount; // mApps.size();
		}

		public final Object getItem(int position) {
			return position; //mApps.get(position);
		}

		public final long getItemId(int position) {
			return position;
		}
	}

	//@Override
	protected void onDestroy() {
//	    super.onDestroy();

		if (audioTrack != null
				&& StartToPlay == true)
		{
			StartToPlay = false;
//	    	audioTrack.stop();//停止播放
//			audioTrack.release();//释放底层资源
		}
	}

//	@Override
//	public void onWindowFocusChanged(boolean hasFocus) {
//		// TODO Auto-generated method stub
//		super.onWindowFocusChanged(hasFocus);
//
//		if (hasFocus) {
//
//			//do something
//			OnActivityLoaded();
//		}
//	}

	// 在 系统完成activity加载后，执行与窗口相关的操作
	public void OnActivityLoaded()
	{
		mNoteImgSet = new CNoteImgSet(mCtx);

//		/////////////////
//		//VideoPeer = (SurfaceView)findViewById(R.id.video_Image_peer);
//		VideoPeerIV = (ImageView)findViewById(R.id.video_Image_peer);
//		//VideoSelf = (SurfaceView)findViewById(R.id.video_capture_surface_nativecam);
//		OnCreateVideoView(); ///////////////

//		// 加载key的audio数据
//		r = LoadKeyAudio();
//		if (r == 0)
//		{
//			Toast.makeText(MusicScore.this, "Can't load key audio.", 200).show();
//		}


		//requestWindowFeature(Window.FEATURE_NO_TITLE);
		// 改在<application 中增加android:theme="@android:style/Theme.Black.NoTitleBar.Fullscreen" >

//		// 获取文件名
//		Intent intent=getIntent();
//		openfilename = intent.getStringExtra("openfilename");
//		//LinearLayout ll = new LinearLayout(AQueryTest2.this);


//		ll = (LinearLayout)findViewById(R.id.layout);
////		ll = (RelativeLayout)findViewById(R.id.layout);
////		ll = (TableLayout)findViewById(R.id.layout);

		//setContentView(R.layout.grid_1);
		//mGrid = (GridView) findViewById(R.id.gridView1);
		//mGrid.setAdapter(new AppsAdapter());

		pv = new PicView[100];

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

			ObEPianoAndroidJavaAPI = new EPianoAndroidJavaAPI(mCtx);
			//ObEPianoAndroidJavaAPI.StartEngine();

			// 启动引擎
			ObEPianoAndroidJavaAPI.StartEngine();
		}


		if (true)
		{
			// 渲染music score窗口
			RenderViews();

//			// 布局视频通信相关窗口
//			LayerOutVideoCom();
		}
	}


	//	@Override
	protected void onCreate(Bundle savedInstanceState) {

		int r = 0;

//		//设置为横屏
//        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//
//		requestWindowFeature(Window.FEATURE_NO_TITLE);//隐藏标题栏
//		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//		WindowManager.LayoutParams.FLAG_FULLSCREEN);//设置全屏
//
//		super.onCreate(savedInstanceState);
//		setContentView(R.layout.pianoelearning); //activity_main);

//		pb = (ProgressBar)findViewById(R.id.pb);
//		pb.setMax(1000);
//
//		ImageButton buttonPlay = (ImageButton)findViewById(R.id.imageButtonPlay);
//		/* 监听button的事件信息 */
//        buttonPlay.setOnClickListener(new Button.OnClickListener(){
//            @Override
//            public void onClick(View arg0) {
//
//            	if (StartToPlay == true)
//            	{
//            		// 已经在播放
//
//            		return;
//            	}
//
//            	// 测试取播放数据
//            	if (true)
//            	{
//	            	int PlayNoteSet[] = ObEPianoAndroidJavaAPI.GetPlayNoteSet();
//	            	int timeK = 40; // 48,
//
//	            	int DataCheckTag = PlayNoteSet[0];
//	            	int DataFieldNum = PlayNoteSet[2]; 		// 6
//	            	int DataLen; // = PlayNoteSet.length;
//	            	int hdrlen = 3;
//	            	DataLen = PlayNoteSet[1]; // - DataFieldNum * 3;	// * 2 ?; // - DataFieldNum;
//	            	PlayKeyCount = DataLen / DataFieldNum;
//
//	            	if (DataFieldNum == 0)
//	            	{
//	            		Toast.makeText(mCtx, "DataFieldNum is 0.", 200).show();
//	            		return;
//	            	}
//	            	else
//	            	{
//	            		if ((DataLen % DataFieldNum) != 0)
//	            		{
//	            			Toast.makeText(mCtx, "DataLen % DataFieldNum" + String.valueOf(DataLen)
//	            					+ "," + String.valueOf(DataFieldNum), 200).show();
//	                		return;
//	            		}
//	            	}
//
//	            	Toast.makeText(mCtx, "Get Note Set, DatLen" + String.valueOf(DataLen), 200).show();
//
////	            	int PlayKeyBarId_TSId[] 	= new int [1000];	// 音符标记
////	            	int PlayKeyTick[] 			= new int [1000];	// 音符起始时刻
////	            	int PlayKeyId[] 		= new int [1000];	// 音高
////	            	int PlayKeyFsInt;
////	            	float PlaKeyFs[] 	= new float [1000];	// 频率
////	            	int PlayKeyDurationInt;
////	            	float PlayKeyDuration[] 	= new float [1000];	// 时长
////	            	int PlayKeyStrength[] 		= new int [1000];	// 强度
//
//	            	int showKeyDlg = 0; // 显示key列表对话框
//
//	            	int dataidx = 0;
//	            	//string strText = "数据";
//	            	CharSequence strText = "播放数据(" + DataLen / (DataFieldNum * 4)+"条)\n";
//	            	int maxp = DataLen + hdrlen; // - DataFieldNum;
//	            	for(int i = 3; i < maxp && dataidx < MaxPlayKeyId; dataidx++)
//	            	{
//	            		//Log.i("wg", "i:" + i);
//
//	            		PlayKeyVPId[dataidx] 		= PlayNoteSet[i++];
//	            		PlayKeyBarId[dataidx] 		= PlayNoteSet[i++];
//	            		PlayKeyTSId[dataidx] 		= PlayNoteSet[i++];
//	            		PlayKeyTick[dataidx] 		= PlayNoteSet[i++] * timeK;
//	            		PlayKeyId[dataidx] 			= PlayNoteSet[i++];
//	            		PlayKeyFsInt				= PlayNoteSet[i++];
//	            		PlayKeyDurationInt			= PlayNoteSet[i++] * timeK;
//	            		PlayKeyStrength[dataidx]	= PlayNoteSet[i++];
//
//	            		PlaKeyFs[dataidx] 			= (float)(PlayKeyFsInt) / 100;				// 原始数据被放大100倍传递
//	            		PlayKeyDuration[dataidx] 	= (float)(PlayKeyDurationInt) / 100;	// 原始数据被放大100倍传递
//
//	            		// 显示条数
//	            		//if (dataidx < 15)
//	            		if (showKeyDlg > 0)
//	            		{
//	            			strText = strText + "\nTick:"+PlayKeyTick[dataidx]+",\tF:"+PlayKeyId[dataidx]+",\tDur:"+PlayKeyDuration[dataidx];
//	            		}
//	            	}
//
//	            	// 创建完后设置对话框的属性
//	            	if (showKeyDlg > 0)
//	            	{
//		            	AlertDialog.Builder builder = new AlertDialog.Builder(mCtx);
//		            	builder.setMessage(strText);
//		            	AlertDialog alert = builder.create();
//		            	alert.show();
//	            	}
//
//	            	//
////	            	AudioSynthesisTask adTask = new AudioSynthesisTask();
////					adTask.execute();
//
////	    			final int SAMPLE_RATE = 16000; // 11025;
////	    			int minSize = AudioTrack.getMinBufferSize(SAMPLE_RATE, //
////	    					AudioFormat.CHANNEL_IN_MONO,// 常量： 16（0x00000010）
////	    					AudioFormat.ENCODING_PCM_16BIT);// PCM音频数据格式：16位，每样
////	    			AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,// 媒体音量
////	    					SAMPLE_RATE,//
////	    					AudioFormat.CHANNEL_OUT_MONO,// 常量： 16（0x00000010）
////	    					AudioFormat.ENCODING_PCM_16BIT, // PCM音频数据格式：每个采样点16比特
////	    					10 * minSize,//
////	    					// MODE_STATIC 音频数据放到一个固定的buffer, 然后一次写入
////	    					// MODE_STREAM 通过write方式把数据一次一次得写到audiotrack中
////	    					AudioTrack.MODE_STREAM // MODE_STATIC // MODE_STREAM//
////	    			);
//
//	            	StartToPlay = true;
//	            	PausePlay = false;
//
//	            	// 合成，播放
//	            	if (false)
//	            	{
////		            	DownloadTask dTask = new DownloadTask();
////						dTask.execute(10);
//	            	}
//	            	else
//	            	{
//	            		mAudioSythAndPlayThread = new CAudioSythAndPlayThread();
//	            		mAudioSythAndPlayThread.start();
//	            	}
//
////					// 位置指示
////					PosIndicateTask IndiTask = new PosIndicateTask();
////					IndiTask.execute(10);
//					// 位置指示任务
//					IndiTask = new PosIndicateTask();
//					//IndiTask.execute(10);
//            	}
//
//            	//////////////////////
//
//            	// Call Play API..
//
//            	return;
//            }
//        });
//
//
//        ImageButton buttonPause = (ImageButton)findViewById(R.id.imageButtonPause);
//		/* 监听button的事件信息 */
//        buttonPause.setOnClickListener(new Button.OnClickListener(){
//            @Override
//            public void onClick(View arg0) {
//
//            	/////////////
//
//            	// Call Pause API..
//            	if (PausePlay == false)
//            	{
//            		PausePlay = true;
//            	}
//            	else
//            	{
//            		PausePlay = false;
//            	}
//
//            	return;
//            }
//        });
//
//        ImageButton buttonStop = (ImageButton)findViewById(R.id.imageButtonStop);
//  		/* 监听button的事件信息 */
//        buttonStop.setOnClickListener(new Button.OnClickListener(){
//              @Override
//              public void onClick(View arg0) {
//
//              	/////////////
//
//            	// Call Stop API..
//            	 StartToPlay = false;
//
//              }
//          });
//
//       ImageButton buttonStereo = (ImageButton)findViewById(R.id.ImageButtonStereo);
//  		/* 监听button的事件信息 */
//       buttonStereo.setOnClickListener(new Button.OnClickListener(){
//              @Override
//              public void onClick(View arg0) {
//
//              	/////////////
//
//            	// Call Stop API..
//            	  StereoOn = 1 - StereoOn;
//
//              }
//          });
//
//      ImageButton buttonVolume = (ImageButton)findViewById(R.id.imageButtonVolume);
//		/* 监听button的事件信息 */
//      buttonVolume.setOnClickListener(new Button.OnClickListener(){
//            @Override
//            public void onClick(View arg0) {
//
//            	/////////////
//
//            	return;
//            }
//        });



		//OnActivityLoaded()

	}

//	// 布局视频通信相关窗口
//	public boolean LayerOutVideoCom()
//	{
//		LinearLayout.LayoutParams ll;
//		//ll = new LinearLayout.LayoutParams(PicViewWidthOrg, PicViewWidthOrg * 3 / 4);
//		LinearLayout ll_right = (LinearLayout) findViewById(R.id.ll_right);
//		int w = ll_right.getWidth();
//		int h = w * 12 / 16;
//		ll = new LinearLayout.LayoutParams(w, h);
//		VideoPeerIV.setLayoutParams(ll);//设置布局参数
//		VideoPeerIV.invalidate();
////		VideoPeer.setLayoutParams(ll);//设置布局参数
////		VideoPeer.invalidate();
//
//		return true;
//	}

	// 渲染music score窗口
	public boolean RenderViews()
	{
//		mGrid = (GridView) findViewById(R.id.GridView1);
//		LinearLayout ll = (LinearLayout) findViewById(R.id.ll_left);

		// 设置参数给引擎
//		WindowManager m = getWindowManager();
//	    Display d = m.getDefaultDisplay();  //为获取屏幕宽、高
//	    PicViewWidthOrg = d.getWidth();
//		PicViewHeightOrg = d.getHeight();

		PicViewWidthOrg = ll.getWidth();
		PicViewHeightOrg = (int)(PicViewWidthOrg * whK);
		PicViewWidthCur = PicViewWidthOrg;
		PicViewHeightCur = PicViewHeightOrg;
		WinWidthCur = PicViewWidthOrg;
		WinHeightCur = PicViewHeightOrg;

		ImgWidth = (int)(PicViewWidthOrg * ImgAndViewK);
		ImgHeight = (int)(PicViewHeightOrg * ImgAndViewK);

//		int width = display.getWidth();
//		int height = display.getHeight();
		int leftmagin = PicViewWidthOrg / 35; //30;
		int topmagin = leftmagin * 2;
		int rightmagin = leftmagin;
		int bottommagin = leftmagin * 2;
		int backgroundclr = 0xfffae9;
		r = ObEPianoAndroidJavaAPI.SetWinInfo(ImgWidth, ImgHeight, leftmagin, topmagin, rightmagin, bottommagin, backgroundclr);
		if (r <= 0)
		{
			Toast.makeText(mCtx, "SetWinInfo fail.", Toast.LENGTH_SHORT).show();
			return false;
		}

		// 引擎生成demo
		if (openfilename.equals("demo"))
		{
			r =ObEPianoAndroidJavaAPI.DemoFile();	// 生成demo
			if (r <= 0)
			{
				Toast.makeText(mCtx, "Can't open demo.", Toast.LENGTH_SHORT).show();
				return false;
			}
		}
		else
		{
			//String openfilename = "/mnt/sdcard/ComLang1_raw.sco";
			r = ObEPianoAndroidJavaAPI.OpenFile(openfilename);		// 打开指定文件
			if (r <= 0)
			{
				Toast.makeText(mCtx, "Can't open " + openfilename + ".", Toast.LENGTH_SHORT).show();
				return false;
			}
		}

		System.out.println("Start Paint...");

		// 引擎生成绘制指令
		r = ObEPianoAndroidJavaAPI.NotifyPaint();
		if (r <= 0)
		{
			Toast.makeText(mCtx, "Paint fail.", Toast.LENGTH_SHORT).show();
			return false;
		}

		// 查询页数
		PicViewCount = ObEPianoAndroidJavaAPI.QueryPageCount();
		//PicViewCount = 1; //ObEPianoAndroidJavaAPI.QueryPageCount();
		System.out.println("PicViewCount = " + PicViewCount); // print

		//ll.setColumnStretchable(columnIndex, isStretchable);

		// 添加java页
		int PicViewCountLocal = PicViewCount;
		lp = new GridView.LayoutParams(PicViewWidthOrg, PicViewHeightOrg);
		for(int i = 0; i < PicViewCount; i++)
		{
			pv[i] = new PicView(mCtx, BmpConfig, ImgFileSurfix, mNoteImgSet, i, PicViewWidthOrg, PicViewHeightOrg, ImgWidth, ImgHeight);
			//pv[i].setPadding(0, 20, 0, 20);
			pv[i].setLayoutParams(lp);//设置布局参数
			// pv[i].setClickable(true);

//			ll.addView(pv[i]);
//
////			lp = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, PicViewHeightOrg);
//			lp = new LinearLayout.LayoutParams(PicViewWidthOrg, PicViewHeightOrg);
////			lp = new RelativeLayout.LayoutParams(PicViewWidthOrg, PicViewHeightOrg);
////			lp2 = new LinearLayout.LayoutParams(PicViewWidthOrg, PicViewHeightOrg);
//
//			pv[i].setLayoutParams(lp);//设置布局参数

			//////////////
			if (i > 0)
			{
				//continue;
			}
			System.out.println("the view " + i + " is clickable " + pv[i].isClickable());

			//pv[i].setClickable(true);
		}

		//setContentView(R.layout.grid_1);

		//loadApps();
		mGrid.setAdapter(new AppsAdapter());
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
//				for(j = 0; j < PicViewCount; j++)
//				{
//					if (pv[j].iMouseInPage > 0)
//					{
//						break;
//					}
//				}

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


		// 查询组图指令
		byte drawoderset[];
		for(int i = 0; i < PicViewCount; i++)
		{
			drawoderset = ObEPianoAndroidJavaAPI.QueryDrawOderSet(i);
			//if (drawoderset.length > 0)
			{
				pv[i].DrawOderSet = new byte[drawoderset.length];
				pv[i].DrawOderSet = drawoderset;
			}
			if (drawoderset.length > 0)
			{
				pv[i].ReDraw = 1;
				//pv[i].drawwgFromMem();
			}
		}

		// 通知java绘制
		for(int i = 0; i < PicViewCount; i++) //
		{
			pv[i].render();
			pv[i].postInvalidate(); //0, 0, PicViewWidthOrg, PicViewHeightOrg);
		}

		// over
		System.out.println("fresh PicView over.");


		return true;
	}

	public boolean ProcessZoom(float CurDistanceOf2Fingers)
	{
		float scale = CurDistanceOf2Fingers / BaseDistanceOf2Fingers;// 当前两点间的距离除以手指落下时两点间的距离就是需要缩放的比例。

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
				//pv[j].zoombmp = null;
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
				//pv[j].zoombmp = null;
			}
			for (int j = 0; j < PicViewCount; j++) {
				pv[j].postInvalidate(); //0, 0, pv[j].mBitmapPeerRctDst.right, pv[j].mBitmapPeerRctDst.bottom);
			}

		}


		return true;
	}

	//	@Override
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
					Log.i("MainActivity", "delta_x( " + delta_x + ") >= xx(" + xx + ")");

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
					Toast.makeText(mCtx, "比例: " + sI + "." + sF, Toast.LENGTH_SHORT).show();
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

		//return super.dispatchTouchEvent(event);
		return true;
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

	// 作废
	public int DrawMusicScore(){

		Canvas mCanvas;


		//if (false)
		{
			String fileName = "/mnt/sdcard/DrawOdersFile.dat";
			File file = new File(fileName);
			InputStream in = null;

			try {

				in = new FileInputStream(fileName);
				int idx = 0;

				while(true)
				{
					int paralen = 0;
					int[] tempints = new int[4];
					byte[] tempbytes = new byte[100];

					//System.out.println("以字节为单位读取文件内容，一次读多个字节：");
					// 一次读多个字节

					int byteread;
					int bytereadread;
					//ReadFromFile.showAvailableBytes(in);
					// 读入多个字节到字节数组中，byteread为一次读入的字节数
					byteread = 1 * 4;
					bytereadread = in.read(tempbytes, 0, byteread);
					if (bytereadread != byteread) {
						break;
					}

					tempints[0] = tempbytes[0]&0xff;
					tempints[1] = tempbytes[1]&0xff;
					tempints[2] = tempbytes[2]&0xff;
					tempints[3] = tempbytes[3]&0xff;
					paralen = (tempints[3] << 24) + (tempints[2] << 16) + (tempints[1] << 8) + tempints[0];

					if (paralen > 50)
					{
						System.out.println("may be a error, draw cmd too long.");
						break;
					}
					byteread = paralen * 4 - 4;
					bytereadread = in.read(tempbytes, 0, byteread);
					if (bytereadread != byteread) {
						break;
					}

					int drawtype;
					tempints[0] = tempbytes[0]&0xff;
					tempints[1] = tempbytes[1]&0xff;
					tempints[2] = tempbytes[2]&0xff;
					tempints[3] = tempbytes[3]&0xff;
					drawtype = (tempints[3] << 24) + (tempints[2] << 16) + (tempints[1] << 8) + tempints[0];
					//System.out.println(String.valueOf(idx) + ": drawtype is " + String.valueOf(drawtype));
					idx++;

					if (drawtype == 5) //JFUNC_sAppLayerLineTo)
					{
						int iPageId;
						int x0, y0, x1, y1;
						int i;

						i = 4;

						tempints[0] = tempbytes[i++]&0xff;
						tempints[1] = tempbytes[i++]&0xff;
						tempints[2] = tempbytes[i++]&0xff;
						tempints[3] = tempbytes[i++]&0xff;
						iPageId = (tempints[3] << 24) + (tempints[2] << 16) + (tempints[1] << 8) + tempints[0];

						tempints[0] = tempbytes[i++]&0xff;
						tempints[1] = tempbytes[i++]&0xff;
						tempints[2] = tempbytes[i++]&0xff;
						tempints[3] = tempbytes[i++]&0xff;
						x0 = (tempints[3] << 24) + (tempints[2] << 16) + (tempints[1] << 8) + tempints[0];

						tempints[0] = tempbytes[i++]&0xff;
						tempints[1] = tempbytes[i++]&0xff;
						tempints[2] = tempbytes[i++]&0xff;
						tempints[3] = tempbytes[i++]&0xff;
						y0 = (tempints[3] << 24) + (tempints[2] << 16) + (tempints[1] << 8) + tempints[0];

						tempints[0] = tempbytes[i++]&0xff;
						tempints[1] = tempbytes[i++]&0xff;
						tempints[2] = tempbytes[i++]&0xff;
						tempints[3] = tempbytes[i++]&0xff;
						x1 = (tempints[3] << 24) + (tempints[2] << 16) + (tempints[1] << 8) + tempints[0];

						tempints[0] = tempbytes[i++]&0xff;
						tempints[1] = tempbytes[i++]&0xff;
						tempints[2] = tempbytes[i++]&0xff;
						tempints[3] = tempbytes[i++]&0xff;
						y1 = (tempints[3] << 24) + (tempints[2] << 16) + (tempints[1] << 8) + tempints[0];

						if (iPageId > 0)
						{
							break;
						}

						//pv[iPageId].AppLayerLineTo(pv[iPageId].piccanvasPeer, iPageId, x0, y0, x1, y1);
						pv[iPageId].ParaIntArray[0] = drawtype;
						pv[iPageId].ParaIntArray[1] = iPageId;
						pv[iPageId].ParaIntArray[2] = x0;
						pv[iPageId].ParaIntArray[3] = y0;
						pv[iPageId].ParaIntArray[4] = x1;
						pv[iPageId].ParaIntArray[5] = y1;
						pv[iPageId].postInvalidate();
					}
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

		return 1;
	}

	/**
	 * Play位置指标任务
	 * 注: 如果在DownloadTask中进行位置批示，有可能因为audioTrack.write()阻塞的原因，导致指示器工作不流畅
	 */
	class PosIndicateTask extends AsyncTask<Integer, Integer, String>{
		@Override
		protected void onPreExecute() {
			//第一个执行方法
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(Integer... params) {

			long PlayTimeSumMinusBufDelay = 0;
			long CurT = System.currentTimeMillis();
			long StartT = mStartPlayTime;

			if (StartT == -1)
			{
				return "空串";
			}

			while(mStartPlayTime != -1 && mPosIndicator != null)	// CurPosIndicatorId
			{
				{
					CurT = System.currentTimeMillis();

					//long PlayTimeSumMinusBufDelay = PlayTimeSum - audioTrackBufferDelay;
					PlayTimeSumMinusBufDelay = (CurT - mStartPlayTime) - audioTrackBufferDelay;
					PlayTimeSumMinusBufDelay -= 3 * 100;	// test, 指示线快于声音, 这里补偿对齐一下
					if (PlayTimeSumMinusBufDelay < 0)
					{
						PlayTimeSumMinusBufDelay = 0;
					}

					PlayTimeSumMinusBufDelay -= PauseTime;	// 扣除暂停时间

					CurPosIndicatorId = ((int) PlayTimeSumMinusBufDelay / PosIndicatorRes);

					synchronized(mPosIndicator)
					{
						DrawPosIndicator(CurPosIndicatorId, mPosIndicator);
					}
				}

				try {
					Thread.sleep(50); // 10);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

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

			int playkeyid = PosIndicator[CurPosIndicatorId];

			if (playkeyid >= PlayKeyCount)
			{
				Log.i("WG", "Error, playkeyid:" + playkeyid + " > PlayKeyCount:" + PlayKeyCount);

				return 0;
			}

			int iVPId = PlayKeyVPId[playkeyid];
			int iBarId = PlayKeyBarId[playkeyid];
			int iTSId = PlayKeyTSId[playkeyid];

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
					System.out
							.println("setOnClickListener, error, MouseLinePara.leng is "
									+ PlayLinePara.length);
				} else {
					// clear mouse line in all pages
					for(int j = 0; j < PicViewCount; j++)
					{
						pv[j].mPlayLine_iPageId = -1;
						//pv[j].mScale = mScale;
					}

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
					}
				}
			}

			// 绘图通知
			for (int j = 0; j < PicViewCount; j++) {
				pv[j].postInvalidate(); // 0, 0, PicViewWidthCur,
				// PicViewHeightCur);
			}

			return 1;
		}
	}

	//
	public class CAudioSythAndPlayThread extends Thread {

		CKeyAudio mKeyAudio[]; // = new CKeyAudio[88];

//		public static void main(String args[]){
//
//		}

		public void run() {
			while(StartToPlay) {

				// 检查Key数量
				if (PlayKeyCount <= 0)
				{
					Log.i("WG", "Error, PlayKeyCount:" + PlayKeyCount);

					return;
				}

				// 声道数量配置
				int AudioChannelsType;
				if (AudioChannels == 1)
				{
					AudioChannelsType = AudioFormat.CHANNEL_OUT_MONO;
				}
				else if (AudioChannels == 2)
				{
					AudioChannelsType = AudioFormat.CHANNEL_OUT_STEREO;
				}
				else
				{
					Log.i("WG", "Error, AudioChannels:" + AudioChannels);

					return;
				}

				// 加载所有key的音频数据，生成立体声数据
				r = LoadKeyAudio(AudioChannels);
				if (r == 0)
				{
					//Toast.makeText(MusicScore.this, "Can't load key audio.", 200).show();

					Log.i("WG", "Error, fail to Load audio");

					return;
				}

				// 打开系统音频播放模块
				//final int SAMPLE_RATE = 16000; // 11025;
				int minSize = AudioTrack.getMinBufferSize(SAMPLE_RATE, //
						AudioChannelsType, //AudioFormat.CHANNEL_IN_MONO,// 常量： 16（0x00000010）
						AudioFormat.ENCODING_PCM_16BIT);// PCM音频数据格式：16位，每样
				minSize *= 4; //4;
				audioTrackBufferDelay = minSize / SAMPLE_RATE * 1000 / AudioChannels;	// ms 缓冲造成的时延
				audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,// 媒体音量		AudioTrack
						SAMPLE_RATE,//
						AudioChannelsType, //AudioFormat.CHANNEL_OUT_MONO,// 常量： 16（0x00000010）
						AudioFormat.ENCODING_PCM_16BIT, // PCM音频数据格式：每个采样点16比特
						(int)(minSize),// 3 10 2
						// MODE_STATIC 音频数据放到一个固定的buffer, 然后一次写入
						// MODE_STREAM 通过write方式把数据一次一次得写到audiotrack中
						AudioTrack.MODE_STREAM // MODE_STATIC // MODE_STREAM//
				);

				int playstarted = 0;
				PauseTime = 0;

				// 保存wavefile, cfg...
				int SaveWaveCfg = 1;	// 保存wavefile开关
				boolean SaveWaved = false;		// 已保存?

				//audioTrack.play();

				// 合成、播放
				if (true)
				{
					int SynthesBufMaxLong = 600000;	// 300000 120000 10000 300 s
					int SynthesBufMaxLen = (SAMPLE_RATE / 1000) * SynthesBufMaxLong;	//
					int SynthesBufMaxLen_Channel = SynthesBufMaxLen * AudioChannels;
					int buffer_offset = 0;
					short[] buffer = new short[SynthesBufMaxLen_Channel];					// 合成数据缓冲区

					int PosIndicatorMaxSize = SynthesBufMaxLong / PosIndicatorRes;
					//int PosIndicatorSize = 0;
					int PosIndicator[] = new int[PosIndicatorMaxSize];						// play位置指示(PlayKeyTick[]数组索引)
					mPosIndicator = PosIndicator;

					int taillong = 20; //10; //20; //10; // ms cfg
					int tailLenInShorts = taillong * (SAMPLE_RATE / 1000);
					short[] buffer_tail = new short[tailLenInShorts * 5 * AudioChannels];	// Key尾部数据修剪缓冲区

					int actDataLenInBuffer = 0;
					int CurSynthKeyId = 0;													// 指向key集合中的当前合成位置

					long t0 = System.currentTimeMillis();
					long curT = t0;
					long buffer_baseT = t0;			// 缓冲区首成员对应的t
					long prepareT = t0;				// 数据准备到什么时刻
					long minPrepareDataLong = 1; 	// 1; //100; //2000;	// 最小准备数据长度, ms

					float tk = (float)1.2; 			// 时长伸缩系数, 1.5;

					int PlayedDataCountSum = 0;		// 累计播放的数据量
					long PlayTimeSum = 0;			// 累计播放的时间
					long TotalPlayTime = 0;			// 指定敬意播放时长
					TotalPlayTime = ratioT(tk, PlayKeyTick[PlayKeyCount-1])
							+ ratioT(tk, (int)PlayKeyDuration[PlayKeyCount-1]);
					int TotalPlayDataCount = (int)(TotalPlayTime * SAMPLE_RATE / 1000);
					float playedDataRatio = 0;

					// 计算耗时统计
					long Tsyt = 0;			// 合成耗时
					long Twriteaduio = 0;	// 写audio数据到驱动耗时
					long Tshiftaduio = 0;	// 移动缓冲区数据耗时

					int pause2resume = 0;	// 当从pause状态切换到play状态时，会产生尖声, 该变量用于控制消除尖声
					//int StopComfirmed = 0;	// stop确认, 当StopComfirmed>2时确认结束

					// 合成
					int lo = 0;
					while (StartToPlay)	// 结束判断
					{
						// 刷新当前时刻
						//curT = System.currentTimeMillis();

						lo++;

						// 合成，补充数据
						Tsyt = System.currentTimeMillis();
						//while(actDataLenInBuffer < minPrepareDataLong * (SAMPLE_RATE / 1000))
						int CurSythKeyTick = ratioT(tk, PlayKeyTick[CurSynthKeyId]);
						while(CurSythKeyTick < (curT - buffer_baseT) + SynthesBufMaxLong) //5000)
						{
							// 数据不足，增加数据

							if (CurSynthKeyId >= PlayKeyCount
									|| CurSynthKeyId >= MaxPlayKeyId) // MaxPlayKeyId)
							{
								// 合成完了

								//Log.i("WG", "syth break 1, CurSynthKeyId:" + CurSynthKeyId + ", PlayKeyCount:" + PlayKeyCount);

								break;
							}

							Log.i("WG", "CurSynthKeyId:" + CurSynthKeyId + ", PlayKeyCount:" + PlayKeyCount);

							// 把下一时刻所有的key都找到 StartKeyId ~ EndKeyId
							int StartKeyId = CurSynthKeyId;
							int EndKeyId = CurSynthKeyId;
							CurSythKeyTick = ratioT(tk, PlayKeyTick[CurSynthKeyId]); //PlayKeyTick[CurSynthKeyId];
							int CurSythKeyTick_nextkey = 0;
							while(true)
							{
								if (CurSynthKeyId >= PlayKeyCount
										|| CurSynthKeyId >= MaxPlayKeyId)
								{
									// 合成完了

									Log.i("WG", "syth break 2, CurSynthKeyId:" + CurSynthKeyId + ", PlayKeyCount:" + PlayKeyCount);

									break;
								}

								CurSythKeyTick_nextkey = ratioT(tk, PlayKeyTick[CurSynthKeyId]); //PlayKeyTick[CurSynthKeyId];
								if (CurSythKeyTick != CurSythKeyTick_nextkey)
								{
									//Log.i("WG", "syth break 3, CurSynthKeyId:" + CurSynthKeyId + ", PlayKeyCount:" + PlayKeyCount);

									break;
								}

								EndKeyId = CurSynthKeyId;
								CurSynthKeyId++;
							}

							// 合成
							if (StartKeyId < PlayKeyCount && StartKeyId < MaxPlayKeyId)
							{
								long StartKeyT = ratioT(tk, PlayKeyTick[StartKeyId]);
								long offsetTInBuf = StartKeyT - (buffer_baseT - t0);	// 当前Key相对于buf起始位置的时间偏移量
								if (offsetTInBuf < 0)
								{
									Log.i("WG", "syth break 4, CurSynthKeyId:" + CurSynthKeyId + ", PlayKeyCount:" + PlayKeyCount);

									break;
								}
								int StartPosInBuffer = (int)((SAMPLE_RATE / 1000) * offsetTInBuf);
								//long EndKeyIdT = ratioT(tk, PlayKeyTick[EndKeyId]);

								// PosIndicator
								int StartPosInPosIndicator = (int)(offsetTInBuf / PosIndicatorRes);
								long offsetEndTInBuf = offsetTInBuf + ratioT(tk, (int)PlayKeyDuration[StartKeyId]);
								if (EndKeyId + 1 < PlayKeyCount)
								{
									offsetEndTInBuf = ratioT(tk, PlayKeyTick[EndKeyId + 1]) - (buffer_baseT - t0);
								}
								int PosIndicatorLen = (int)((offsetEndTInBuf - offsetTInBuf + (PosIndicatorRes - 1)) / PosIndicatorRes);
								for(int p = 0; p < PosIndicatorLen; p++)
								{
									PosIndicator[StartPosInPosIndicator + p] = StartKeyId;
								}

								// 每个key
								for(int i = StartKeyId; i <= EndKeyId; i++)
								{
									int KeyDataCount = (int)((PlayKeyDuration[i]) * (SAMPLE_RATE / 1000) * tk   *1.3);	// *2 test

									// 强制踏板效果 cfg...
									if (EndKeyId != PlayKeyCount - 1)	// 最后一组不要延长， 否则造成结束判断异常
									{
										KeyDataCount *= 6; //3; //5;
									}

									if (StartPosInBuffer + KeyDataCount >= SynthesBufMaxLen)
									{
										Log.i("WG", "error. syth buf is too short. sp:" + StartPosInBuffer + ",KeyDataCount:" + KeyDataCount);
										KeyDataCount = SynthesBufMaxLen - StartPosInBuffer - 1;
									}

									int key = PlayKeyId[i] - 1;
									if (key >= 88)
									{
										Log.i("WG", "error. key >= 88, Key:" + key + ", i:" + i);
										continue;
									}
									if (mKeyAudio[key].keyAudio != null)
									{
										int c = KeyDataCount;
										// note数据长度溢出检查
										if (c > mKeyAudio[key].actDataLenInShorts)
										{
											c = mKeyAudio[key].actDataLenInShorts;
										}
										// score总长度举出检查
										if (c + StartPosInBuffer >= TotalPlayDataCount)
										{
											c = TotalPlayDataCount - StartPosInBuffer;
										}

//	 									for(int p = 0; p < c; p++)
//	 									{
//	 										buffer[StartPosInBuffer + p] += mKeyAudio[key].keyAudio[p];
//	 									}

										// audio

										// tail长度
										int tailLenInShortsTmp = tailLenInShorts;
										if (tailLenInShortsTmp > c)
										{
											tailLenInShortsTmp = c;
										}

										if (AudioChannels == 1)
										{
											// 头合成
											int hdrlen = c - tailLenInShortsTmp;
											for(int p = 0; p < hdrlen; p++)
											{
												buffer[StartPosInBuffer + p] += mKeyAudio[key].keyAudio[p];
											}

											// tail
											for(int p = 0; p < tailLenInShortsTmp; p++)
											{
												buffer_tail[p] = mKeyAudio[key].keyAudio[p + hdrlen];
											}
											//ClipKeyTail(buffer_tail, tailLenInShortsTmp, 1, AudioChannels);
											ClipKeyTail_cos(buffer_tail, tailLenInShortsTmp, 1, AudioChannels);
											for(int p = 0; p < tailLenInShortsTmp; p++)
											{
												buffer[StartPosInBuffer + hdrlen + p] += buffer_tail[p];
											}
										}
										else if (AudioChannels == 2)
										{
											// 头合成
											int hdrlen = c - tailLenInShortsTmp;
											int hdrlen2 = hdrlen * 2;
											int StartPosInBuffer2 = StartPosInBuffer * 2;
											for(int p = 0; p < hdrlen2; p++)
											{
												buffer[StartPosInBuffer2 + p] += mKeyAudio[key].keyAudio[p];
											}

											// tail
											int tailLenInShortsTmp2 = tailLenInShortsTmp * 2;
											for(int p = 0; p < tailLenInShortsTmp2; p++)
											{
												buffer_tail[p] = mKeyAudio[key].keyAudio[p + hdrlen2];
											}
											ClipKeyTail(buffer_tail, tailLenInShortsTmp, 1, AudioChannels);
											for(int p = 0; p < tailLenInShortsTmp2; p++)
											{
												buffer[StartPosInBuffer2 + hdrlen2 + p] += buffer_tail[p];
											}
										}
									}
									else
									{
										Log.i("WG", "key data is null, keyid(0 based):" + key);
									}

									// 刷新缓冲区数据实际长度: actDataLenInBuffer
									if (actDataLenInBuffer < StartPosInBuffer + KeyDataCount)
									{
										actDataLenInBuffer = StartPosInBuffer + KeyDataCount;
									}
								}
							}
						}
						Tsyt = System.currentTimeMillis() - Tsyt;

						if (playstarted == 0)
						{
							playstarted = 1;

							t0 = System.currentTimeMillis();
							curT = t0;
							buffer_baseT = t0;

							audioTrack.play();
						}

						// save test
						if (SaveWaveCfg > 0 && SaveWaved == false && actDataLenInBuffer > 0)
						{
							SaveWaved = true;

							CWaveFile cwf = new CWaveFile();

							String FileName = openfilename + ".wav";
							long totalAudioLenInShorts = actDataLenInBuffer * AudioChannels;
							long totalDataLenInShorts = totalAudioLenInShorts + 44;
							long longSampleRate = SAMPLE_RATE;
							int channels = AudioChannels;
							long byteRate = SAMPLE_RATE * 2 * AudioChannels;
							short AudioData[] = buffer;
							try {
								cwf.WriteWaveFile(FileName, totalAudioLenInShorts,
										totalDataLenInShorts, longSampleRate,
										channels, byteRate, AudioData);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}

						// 计算播放数据量
						long cT = System.currentTimeMillis();
						long pastT = cT - curT;
						//if (pastT < 10)
						if (false) //
						{
							try {
								//if (counter % 100 == 0)
								{
									Thread.sleep(10 - pastT); // 10);
								}
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

							continue;
						}
						curT = cT;

						if (PausePlay == true)	// 暂停play
						{
							pause2resume = 1;

							try {
								//if (counter % 100 == 0)
								{
									// 把t0向过去推
									long delayt = System.currentTimeMillis();
									Thread.sleep(10); // 10);
									delayt = System.currentTimeMillis() - delayt;
									//t0 -= delayt;
									t0 += delayt;
									PauseTime += delayt;
								}
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

							continue;
						}

						PlayTimeSum = curT - t0;							// 累计播放的时间

						//PlayTimeSum += 100; //300;
						int EstmatePlayDataSum = (int)(PlayTimeSum * (SAMPLE_RATE / 1000));	// 估计该播放的数据量
						//EstmatePlayDataSum += (100 * (SAMPLE_RATE / 1000)); // 多播放100ms的数据
						int TryPlaycount = (int)(EstmatePlayDataSum - PlayedDataCountSum);
						int RealPlaycount = 0;
						if (TryPlaycount > SynthesBufMaxLen)
						{
							TryPlaycount = SynthesBufMaxLen;
						}
						if (TryPlaycount > actDataLenInBuffer)
						{
							TryPlaycount = actDataLenInBuffer;
						}
						//TryPlaycount = actDataLenInBuffer; ///////////////////

						{
							// 进度条刷新
							{
								float playprogress = ((float)PlayTimeSum / TotalPlayTime) * 1000;
								pb.setProgress((int)playprogress);
								//publishProgress((int)playprogress);
							}

							// PosIndicator
							if (false)	// 性能原因，改由专门的任务处理
							{
								//long PlayTimeSumMinusBufDelay = PlayTimeSum - audioTrackBufferDelay;
								long PlayTimeSumMinusBufDelay = (buffer_baseT - t0  + (buffer_offset / (SAMPLE_RATE / 1000))) - audioTrackBufferDelay;
								PlayTimeSumMinusBufDelay -= 3 * 100;	// test, 指示线快于声音, 这里补偿对齐一下
								if (PlayTimeSumMinusBufDelay < 0)
								{
									PlayTimeSumMinusBufDelay = 0;
								}
								CurPosIndicatorId = ((int) PlayTimeSumMinusBufDelay / PosIndicatorRes);
								DrawPosIndicator(CurPosIndicatorId, PosIndicator);
							}

							if (TryPlaycount >= 16)
							{

								// play
								if (true)
								{
									if (pause2resume == 1)
									{
										pause2resume = 0;

										// hdr

										int tailLenInShortsTmp = tailLenInShorts * 3;
										if (tailLenInShortsTmp > TryPlaycount)
										{
											tailLenInShortsTmp = TryPlaycount;
										}
										for(int p = 0; p < tailLenInShortsTmp; p++)
										{
											buffer_tail[p] = buffer[p];
										}
										ClipKeyHdr(buffer_tail, tailLenInShortsTmp, 1);
										for(int p = 0; p < tailLenInShortsTmp; p++)
										{
											buffer[p] = buffer_tail[p];
										}
									}

									// 启动位置批示器: mStartPlayTime
									{
										if (mStartPlayTime == -1)
										{
											mStartPlayTime = System.currentTimeMillis();
											mPosIndicator = PosIndicator;
											mRealPlaycount = RealPlaycount;

											// 位置指示任务启动
											//PosIndicateTask IndiTask = new PosIndicateTask();
											IndiTask.execute(10);
										}
									}

									Twriteaduio = System.currentTimeMillis();
									RealPlaycount = audioTrack.write(buffer, buffer_offset * AudioChannels, TryPlaycount * AudioChannels); //buffer.length);
									RealPlaycount /= AudioChannels;
									buffer_offset += RealPlaycount;
									Twriteaduio = System.currentTimeMillis() - Twriteaduio;


									PlayedDataCountSum += RealPlaycount;
									playedDataRatio = ((float)PlayedDataCountSum * 100 / TotalPlayDataCount);
									float playprogress = ((float)PlayTimeSum * 100 / TotalPlayTime);
//	 								Log.i("WG", "RealPlayed:" + PlayedDataCountSum
//	 										+ ", TotalPlay:" + TotalPlayDataCount
//	 										+ ", playedRatio:" + playedDataRatio
//	 										+ ", progress:" + playprogress);

									if (false)
									{
										playedDataRatio = ((float)PlayTimeSum * 100 / TotalPlayTime);
										float CurPosIndiT = CurPosIndicatorId * 100;
										playprogress = ((float)PlayTimeSum * 100 / TotalPlayTime);
										Log.i("WG", "PlayTimeSum:" + PlayTimeSum
												+ ", TotalPlayTime:" + TotalPlayTime
												+ ", playedRatio:" + playedDataRatio
												+ ", CurPosIndiT:" + CurPosIndiT);
									}

								}
								else
								{
									short[] buffer1 = { //
											8130, 15752, 22389, 27625, 31134, 32695, 32210, 29711, 25354,
											19410,
											12253, //
											4329, -3865, -11818, -19032, -25055, -29511, -32121,
											-32722, -31276, -27874, -22728, -16160, -8582, -466 //
									};
									audioTrack.write(buffer1, 0, buffer1.length); //buffer.length);
								}

								// shift data
								Tshiftaduio = System.currentTimeMillis();
								{
									int maxdata = SynthesBufMaxLen; //min(SynthesBufMaxLen, actDataLenInBuffer);
									if (maxdata > actDataLenInBuffer)
									{
										maxdata = actDataLenInBuffer;
									}

									// 缓冲区数据快要耗尽时才移动
									float shiftKCfg = (float)0.9; // cfg...
									// 缓冲区中的数据占用率
									float r = (float)maxdata / SynthesBufMaxLen; //SynthesBufMaxLong;	// SynthesBufMaxLen
									// 缓冲区中的有效数据占用率
									float r2 = ((float)maxdata - buffer_offset) / actDataLenInBuffer;
									if (r > shiftKCfg && (r2 < 0.1 && r2 > 0))
									{
										// audio buf
										int tail = RealPlaycount % (PosIndicatorRes * (SAMPLE_RATE / 1000));
										int RealPlaycount_mod = RealPlaycount - tail; // RealPlaycount_mod整除100 * 16000 / 1000
										int shiftcount = maxdata - RealPlaycount_mod;
										if (RealPlaycount_mod > 0)
										{
											if (AudioChannels == 1)
											{
												//int shiftcount = maxdata - RealPlaycount;
												for(int i = 0; i < shiftcount; i++)
												{
													buffer[i] = buffer[i + RealPlaycount_mod];
												}

												for(int i = shiftcount; i < maxdata; i++)
												{
													buffer[i] = 0;
												}
											}
											else if (AudioChannels == 2)
											{
												//int shiftcount = maxdata - RealPlaycount;
												int shiftcount2 = shiftcount * 2;
												int RealPlaycount_mod2 = RealPlaycount_mod * 2;
												int maxdata2 = maxdata * 2;
												for(int i = 0; i < shiftcount2; i++)
												{
													buffer[i] = buffer[i + RealPlaycount_mod2];
												}

												for(int i = shiftcount2; i < maxdata2; i++)
												{
													buffer[i] = 0;
												}
											}
											buffer_offset = tail;
											buffer_baseT += RealPlaycount_mod / (SAMPLE_RATE / 1000);
											actDataLenInBuffer -= RealPlaycount_mod;

											// pos indicator buf
											{
												shiftcount = ((maxdata - RealPlaycount_mod) / (SAMPLE_RATE / 1000)) / PosIndicatorRes;
												int shiftdistance = (RealPlaycount_mod / (SAMPLE_RATE / 1000)) / PosIndicatorRes;
												for(int i = 0; i < shiftcount; i++)
												{
													PosIndicator[i] = PosIndicator[i + shiftdistance];
												}

												int maxindicatorData = ((maxdata) / (SAMPLE_RATE / 1000)) / PosIndicatorRes;
												for(int i = shiftcount; i < maxindicatorData; i++)
												{
													PosIndicator[i] = 0;
												}
											}
										}
									}
								}
								Tshiftaduio = System.currentTimeMillis() - Tshiftaduio;
								//Log.i("WG", "---");
							}
						}

						//if (TryPlaycount > 0)
						//if (false)
						{
							playedDataRatio = ((float)PlayTimeSum * 100 / TotalPlayTime);
							float CurPosIndiT = CurPosIndicatorId * 100;
							//float playprogress = ((float)PlayTimeSum * 100 / TotalPlayTime);

							Log.i("WG", "lo:" + lo
									+ ", pastT:" + pastT
									+ ", Tsyt:" + Tsyt
									+ ", Twa:" + Twriteaduio
									+ ", Tsa:" + Tshiftaduio
//	 								+ ", Tryms:" + TryPlaycount / (SAMPLE_RATE / 1000)
//	 								+ ", Realms:" + TryPlaycount / (SAMPLE_RATE / 1000)
//	 								+ ", KeyId:" + CurSynthKeyId
//	 								+ ", buflong:" + actDataLenInBuffer / (SAMPLE_RATE / 1000)
									+ ", PlayTimeSum:" + PlayTimeSum
									+ ", TotalPlayTime:" + TotalPlayTime
									+ ", playedRatio:" + playedDataRatio
									+ ", CurPosIndiT:" + CurPosIndiT);
						}

						// 结束判断
						if (true)
						{
							if ((CurSynthKeyId >= PlayKeyCount
									|| CurSynthKeyId >= MaxPlayKeyId)
									&& playedDataRatio > 100) // MaxPlayKeyId)
							{
								break;
							}
						}
					}

				}
				else
				{
					short[] buffer = { //
							8130, 15752, 22389, 27625, 31134, 32695, 32210, 29711, 25354,
							19410,
							12253, //
							4329, -3865, -11818, -19032, -25055, -29511, -32121,
							-32722, -31276, -27874, -22728, -16160, -8582, -466 //
					};

					//			while (StartToPlay) {
					//				audioTrack.write(buffer, 0, buffer.length);
					//			}
					while (StartToPlay) {

						if (PausePlay == false)
						{
							audioTrack.write(buffer, 0, buffer.length);
						}
						else
						{
							try {
								//if (counter % 100 == 0)
								{
									Thread.sleep(10); // 10);
								}
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}

				}


				// 关闭Play指示任务
				mStartPlayTime = -1;
				synchronized(mPosIndicator){
					mStartPlayTime = -1;
				}

				StartToPlay = false;

				audioTrack.stop();//停止播放
				audioTrack.release();//释放底层资源

				pb.setProgress(0);
				//publishProgress(0);

				return;

			}
		}

		// PosIndicator
		int DrawPosIndicator(int CurPosIndicatorId, int PosIndicator[]) {

			if (CurPosIndicatorId >= PosIndicator.length)
			{
				Log.i("WG", "Error, CurPosIndicatorId:" + CurPosIndicatorId + " > PosIndicator.length:" + PosIndicator.length);

				return 0;
			}

			int playkeyid = PosIndicator[CurPosIndicatorId];

			if (playkeyid >= PlayKeyCount)
			{
				Log.i("WG", "Error, playkeyid:" + playkeyid + " > PlayKeyCount:" + PlayKeyCount);

				return 0;
			}

			int iVPId = PlayKeyVPId[playkeyid];
			int iBarId = PlayKeyBarId[playkeyid];
			int iTSId = PlayKeyTSId[playkeyid];

//			Log.i("WG", "CurPosIndicatorId:" + CurPosIndicatorId
//					+ ", playkeyid:" + playkeyid
//					+ ", iVPId:" + iVPId
//					+ ", iBarId:" + iBarId
//					+ ", iTSId:" + iTSId);

			int PlayLinePara[];
			PlayLinePara = ObEPianoAndroidJavaAPI.QueryPlayLinePos(iVPId,
					iBarId, iTSId);
			{
				if (PlayLinePara.length != 5) {
					System.out
							.println("setOnClickListener, error, MouseLinePara.leng is "
									+ PlayLinePara.length);
				} else {
					// clear mouse line in all pages
					for(int j = 0; j < PicViewCount; j++)
					{
						pv[j].mPlayLine_iPageId = -1;
						//pv[j].mScale = mScale;
					}

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
					}
				}
			}

			// 绘图通知
			for (int j = 0; j < PicViewCount; j++) {
				pv[j].postInvalidate(); // 0, 0, PicViewWidthCur,
				// PicViewHeightCur);
			}

			return 1;
		}

		// 加载所有key的音频数据，生成立体声数据
		int LoadKeyAudio(int AudioChannels)
		{
			int r = 0;

			mKeyAudio = new CKeyAudio[88];

			for(int i = 0; i < 88; i++)
			{
				mKeyAudio[i] = new CKeyAudio(AudioChannels);
				mKeyAudio[i].keyId = i;

				r = mKeyAudio[i].readkeyaudiofile();
				if (r == 0)
				{
					return 0;
				}

				if (StereoOn > 0)
				{
					mKeyAudio[i].SetStereoOn(true);
				}
				else
				{
					mKeyAudio[i].SetStereoOn(false);
				}
			}

			return 1;
		}

		// 缩放t
		int ratioT(float k, int TickOrg)
		{
			int tickR = (int)(TickOrg * k);

			return tickR;
		}

		// 修剪key的tail
		// AudioData是tail数据, a二次函数系数, a超大尾越钝
		// DataLenInshorts是单声道对应的值，如果是stereo, 则实际值要*2
		int ClipKeyTail(short AudioData[], int DataLenInshorts, float a, int AudioChannels)
		{
			// 用二次函数进行修剪, 成以下形状")"

			if (AudioChannels < 0 || AudioChannels > 2)
			{
				return 0;
			}

			{
				for(int s = 0; s < AudioChannels; s++) // left or right side
				{
					// 找最大振幅
					short maxA = 0;
					int len = DataLenInshorts / 3;
					if (len == 0)
					{
						len = DataLenInshorts;
					}
					short v = 0;
					for(int i = 0; i < len; i++)
					{
						short av = AudioData[i * 2 + s];
						if (av > 0)
						{
							v = av;
						}
						else
						{
							v = (short)(0 - av);
						}
						//v = Math.abs(AudioData[i]);

						if (maxA < v)
						{
							maxA  = v;
						}
					}

					float k;
					int MaxAOrg = (int)Math.sqrt((DataLenInshorts - 1) / a);
					if (MaxAOrg == 0)
					{
						return 0;
					}
					k = (float)(maxA / MaxAOrg);

					// clip
					float A = 0;	// 动态振幅
					for(int i = 0; i < DataLenInshorts; i++)
					{
						A = (float)(Math.sqrt((DataLenInshorts - i * 2 - 1) / a) * k);
						AudioData[i * 2 + s] = (short)((float)AudioData[i * 2 + s] * A / maxA);
					}
				}
			}

			return 1;
		}

		// 修剪key的tail
		// AudioData是tail数据, a二次函数系数, a超大尾越钝
		// DataLenInshorts是单声道对应的值，如果是stereo, 则实际值要*2
		int ClipKeyTail_cos(short AudioData[], int DataLenInshorts, float a, int AudioChannels)
		{
			// 用二次函数进行修剪, 成以下形状")"

			if (AudioChannels < 0 || AudioChannels > 2)
			{
				return 0;
			}

			{
				for(int s = 0; s < AudioChannels; s++) // left or right side
				{
					// 找最大振幅
					short maxA = 0;
					int len = DataLenInshorts / 3;
					if (len == 0)
					{
						len = DataLenInshorts;
					}
					short v = 0;
					for(int i = 0; i < len; i++)
					{
						short av = AudioData[i * 2 + s];
						if (av > 0)
						{
							v = av;
						}
						else
						{
							v = (short)(0 - av);
						}
						//v = Math.abs(AudioData[i]);

						if (maxA < v)
						{
							maxA  = v;
						}
					}

					// 1.0 + cos(0, PI)
					float k = (float)(maxA / 2.0);

					// clip
					float A = 0;	// 动态振幅
					float angle = 0;
					for(int i = 0; i < DataLenInshorts; i++)
					{
						angle = PI * i / DataLenInshorts;
						A = (float)((1.0 + Math.cos(angle)) * k);
						AudioData[i * 2 + s] = (short)((float)AudioData[i * 2 + s] * A);
					}
				}
			}

			return 1;
		}

		// 修剪aduio的head
		// AudioData是tail数据, a二次函数系数, a超大尾越钝
		int ClipKeyHdr(short AudioData[], int DataLenInshorts, float a)
		{
			// 用二次函数进行修剪, 成以下形状")"

			// 找最大振幅
			short maxA = 0;
			int len = DataLenInshorts; // / 3;
			if (len == 0)
			{
				len = DataLenInshorts;
			}
			short v = 0;
			for(int i = 0; i < len; i++)
			{
				if (AudioData[i] > 0)
				{
					v = AudioData[i];
				}
				else
				{
					v = (short)(0 - AudioData[i]);
				}
				//v = Math.abs(AudioData[i]);

				if (maxA < v)
				{
					maxA  = v;
				}
			}

			float k;
			int MaxAOrg = (int)Math.sqrt((DataLenInshorts - 1) / a);
			if (MaxAOrg == 0)
			{
				return 0;
			}
			k = (float)(maxA / MaxAOrg);

			// clip
			float A = 0;	// 动态振幅
			for(int i = 0; i < DataLenInshorts; i++)
			{
				A = (float)(Math.sqrt((i) / a) * k);
				AudioData[i] = (short)((float)AudioData[i] * A / maxA);
			}

			return 1;
		}
	}




}

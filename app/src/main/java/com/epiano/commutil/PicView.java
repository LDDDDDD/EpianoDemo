package com.epiano.commutil;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

//import android.app.AlertDialog;
//import android.view.Display;


enum JAVA_DRAW_API
{
	JFUNC_sAppLayerAddAPage,		// 0
	JFUNC_sAppLayerDeleteAPage,		// 1
	JFUNC_sAppLayerDeleteAllPage,	// 2
	JFUNC_sAppLayerSelectPen,		// 3
	JFUNC_sAppLayerSelectFont,		// 4
	JFUNC_sAppLayerOutText,			// 5
	JFUNC_sAppLayerLineTo,			// 6
	JFUNC_sAppLayerSmoothLine,		// 7
	JFUNC_sAppLayerSetPixel,		// 8
	JFUNC_sAppLayerPaintWhite,		// 9
	JFUNC_sAppLayerLoadABmp,		// 10
	JFUNC_sAppLayerDrawParabola,	// 11	parabola
	JFUNC_sAppLayerDrawBCurve,		// 12	B curve

	JFUNC_BUTT,
};


public  class PicView extends ImageView //implements Runnable
{
	//Canvas mCanvas = null;
	int SCREEN_TYPE;//   0:MediaPad(1280*800)  1:U8860(854*480) 2:honor(960*540)
	Paint mPaint = null;
	Paint mPaint1 = null;
	Paint mPaintGray = null;
//	float x0=0,y0=0,x,y;  //上次画笔坐标、当前画笔坐标
//	int h1, h2;// = 240/2, h2 = 480/2;  //水平轴位置，本端和对端的相对高度起点
//	int yScale;
	//int h3 = 480, h4 = 600;  //收包图起始高度
	//int pPkRcv, pByteRcv, mPkRcv, mByteRcv;
	//int pPkRcv0, pByteRcv0, mPkRcv0, mByteRcv0;
//	float mBWinbyte, mBWinpk, pBWinbyte, pBWinpk, mJitterpknum, pJitterpknum, mPingRtl;  //m:本端，p:对端
//	float mpklstnum, ppklstnum;
//	float mBWinbyte0, mBWinpk0, pBWinbyte0, pBWinpk0, mJitterpknum0, pJitterpknum0,mpingrtl0;
//	float mpklstnum0, ppklstnum0;
//	float mMaxBWinbyte=0,mMaxBWinpk=0, pMaxBWinbyte=0, pMaxBWinpk=0, mMaxpingrtl=0; //绘制队列中的最大值，用于计算缩放比例
//	float mMinBWinbyte, pMinBWinbyte, mMaxLost, mMinLost, pMaxLost, pMinLost;  //统计信息

//	private List<Float> MListmBWinbyte = null;//显示统计曲线，队列长度为60
//	private List<Float> MListmBWinpk = null;
//	private List<Float> MListpBWinbyte = null;
//	private List<Float> MListmpklstnum = null;
//	private List<Float> MListppklstnum = null;
//	private List<Float> MListpingrtl = null;
//	private List<Float> MListjitter = null;

//	int ListBufferSize;  //绘制曲线的列表大小，影响着画曲线时横坐标轴方向绘制的长度。（将全部buffersize画到屏幕上）

	//	private List<Float> MListpBWinpk = null;
//	boolean startnewdraw = false; //控制画满后重新开始
//	boolean drawline = false;
//	boolean drawPk = false;
	// 收包图
	int gStripHigh = 30, gStripLen, gStripCount = 5;  //gStripLen = 400
	//	int gStartX = 20, gStartY = 50;
//	float xlast = 0;
	//double gDspUnit = gStripLen / 100;
//	boolean gDrawLastOnly = false;
//	boolean gDrawLastOnlyPeer = false;
//	boolean mJitterFlag = false;
//	private int mPeerTickDiff;
//	volatile int mStartTick = -1;  //两个线程可能会同时修改
//	private int mPkRate;
//	private int mLastSeq = -1;
//	private int mPeerTickDiffPeer;
//	private int mStartTickPeerForPkshow = -1;
//	private int mLastSeqPeer = -1;
//	private boolean mJitterFlagPeer = false;
	////ListIterator MitmPkRcvBuf;   //Iterator
	//ListIterator MitmPkRcvBufPeer;   //Iterator
	//public Bitmap mBitmap1;
	public Bitmap mBitmapPeer;
	public Bitmap mBitmapPeerBG;	// 暂时不用
	int BackGoundImgId = R.drawable.lambskin07;	// 23 20 2 6 7 cfg...
	//	public Bitmap mBitmapPeerScale;
//	public Bitmap zoombmp = null;
	Canvas piccanvasPeer;
	Canvas piccanvasPeerScale;
	//Matrix mMatrix;
	//Rect src1,src2,src3,src4,src5;
	//Config BmpConfig = Bitmap.Config.ARGB_8888;
	Config BmpConfig = Config.RGB_565;

	Path mPath0,mPath1,mPath2;
	Rect mBitmapPeerRct;
	public Rect mBitmapPeerRctDst;
	Rect mBitmapPeerRctMin;
	public int OffsetX = 0;				// 放大的情况，因为左右滑动窗口并没有使用正规的scrollview功能，而是通过图像平移方式来实现的，所以下面计算时手工加入了pv[j].OffsetX(窗口x方向滑动量)因素。
	Rect mBitmapPeerRctOffsetX;		// 加入了OffsetX后的RECT

	Canvas mDrawCanvas;

	public int [] ParaIntArray; // no use

	CNoteImgSet mNoteImgSet;

	public int iPageId = -1;

	public int iMouseInPage = 0;
	public int iXInPage;
	public int iYInPage;

	public byte DrawOderSet[];	// 组图指令集

	public int Drawed = 0;		// 初始化
	public int ReDraw = 0;	// 重画指示

	private int gAngle = 0;

	public float mScale = 1;

	public int BackgoundR = 0xff;
	public int BackgoundG = 0xfa;
	public int BackgoundB = 0xe9;

	private DashPathEffect pathEffect;

	float ImgAndViewK = 1;
	int ImgWidth = 0;
	int ImgHeight = 0;

	//int color = Color.GREEN;
	//int timecounter = 0;


	// 鼠标线
	public int mMouseTimeSlice_iPageId;		// -1无效
	public int mMouseTimeSlice_iBarId;
	public int mMouseTimeSlice_xInPage;
	public int mMouseTimeSlice_y0InPage;
	public int mMouseTimeSlice_y1InPage;

	// Play Line
	public int mPlayLine_iPageId;		// -1无效
	public int mPlayLine_iBarId;
	public int mPlayLine_xInPage;
	public int mPlayLine_y0InPage;
	public int mPlayLine_y1InPage;

	byte[] byteBuffer;	// 1000

	int mPvTextureId = -1;	// 本pv对应在mTextureSet[]中的索引

	String ImgFileSurfix = ".bmp";

	// 作废
	public PicView(Context context)
	{

		super(context);
	}

	// 使用
	public PicView(Context context, Config BmpConfigIn, String ImgFileSurfixIn, CNoteImgSet NoteImgSet, int PageId,
				   int ViewWidth, int ViewHeight, int ImgWidthIn, int ImgHeightIn)
	{
		super(context);

		BmpConfig = BmpConfigIn;

		ImgFileSurfix = ImgFileSurfixIn;

		mNoteImgSet = NoteImgSet;

		iPageId = PageId;

		mPaint = new Paint();
		mPaint1 = new Paint();
		mPaintGray = new Paint();
		mPaintGray.reset();
		mPaintGray.setStrokeWidth(1);
		int PenColor = 0xCCCCCC; //0x888888; // gray
		mPaintGray.setColor(PenColor | 0xFF000000);

		// 改为现用现生成
//		mBitmapPeer = Bitmap.createBitmap(ImgWidth, ImgHeight, Bitmap.Config.ARGB_8888);
//		piccanvasPeer = new Canvas(mBitmapPeer);  //绘制收包图

		mBitmapPeerRct = new Rect();
		mBitmapPeerRct.left = 0;
		mBitmapPeerRct.top = 0;
		mBitmapPeerRct.right = ViewWidth;
		mBitmapPeerRct.bottom = ViewHeight;
		mBitmapPeerRctDst = new Rect();
		mBitmapPeerRctDst = mBitmapPeerRct;
		mBitmapPeerRctMin = new Rect();
		mBitmapPeerRctMin = mBitmapPeerRct;
		mBitmapPeerRctOffsetX = new Rect();
		mBitmapPeerRctOffsetX = mBitmapPeerRct;

		ParaIntArray = new int[100];

		byteBuffer = new byte[1000];

		ImgWidth = ImgWidthIn;
		ImgHeight = ImgHeightIn;

		ImgAndViewK = ImgWidth / ViewWidth;

		mMouseTimeSlice_iPageId = -1;
	}

	// 作废
	public PicView(Context context,AttributeSet attr)
	{

		// 作废

		super(context,attr);
		setFocusable(true);

		setWillNotDraw(false);

		//mMatrix = new Matrix();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		int x, y;

		iMouseInPage = 1;
		iXInPage = (int)event.getX();
		iYInPage = (int)event.getY();

		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
//        	System.out.println("onTouch------->>ACTION_DOWN: iPageId " + String.valueOf(iPageId));
//        	x = (int)event.getRawX();
//        	y = (int)event.getRawY();
//        	System.out.println("PicView onTouchEvent ------->>ACTION_DOWN: Rawx " + String.valueOf(x) + "; Rawy " + String.valueOf(y));
//        	x = (int)event.getX();
//        	y = (int)event.getY();
//        	System.out.println("PicView onTouchEvent ------->>ACTION_DOWN: x " + String.valueOf(x) + "; y " + String.valueOf(y));
				break;
			case MotionEvent.ACTION_MOVE:
//        	x = (int)event.getRawX();
//        	y = (int)event.getRawY();
//        	//System.out.println("onTouch------->>ACTION_MOVE: x " + String.valueOf(x) + "; y " + String.valueOf(y));
				break;
			case MotionEvent.ACTION_UP:
				//System.out.println("onTouch------->>ACTION_UP");
				break;
		}


		return super.onTouchEvent(event);
	}

	// 图片可能被缓冲到/mnt/sdcard/ep_p0.bmp ...
	String BmpCachDir = "/mnt/sdcard/epcach/";
	//String BmpCachDir = "/data/data/android.epiano.com/files/matter/epcach/";
	// 删除缓冲
	public int InitCach()
	{
		// 存储路径
		File file = new File(BmpCachDir);
		if (!file.exists())
		{
			file.mkdirs();
		}

		return 1;
	}

	// 删除缓冲
	public int RemoveCach()
	{
		String filename = String.valueOf(iPageId);
		String pathFileName = BmpCachDir + filename + ImgFileSurfix;

		deleteFile(pathFileName);

		return 1;
	}

	// 检查缓冲
	public int IsCached()
	{
		String filename = String.valueOf(iPageId);
		String pathFileName = BmpCachDir + filename + ImgFileSurfix;

		File file = new File(pathFileName);
		if (file.isFile() && file.exists())
		{

		}
		else
		{
			return 0;
		}

		return 1;
	}

	// 加载缓冲
	public int LoadCach()
	{
		boolean v = isBmpValid();
		if (v)
		{
			return 1;
		}

		String filename = String.valueOf(iPageId);
		String pathFileName = BmpCachDir + filename + ImgFileSurfix;

//		File file = new File(pathFileName);
//		if (file.isFile() && file.exists())
//		{
//
//		}
//		else
//		{
//			return 0;
//		}

		if (true)
		//if (false)
		{
			Bitmap bmp = BitmapFactory.decodeFile(pathFileName, null); //获取位图

			mBitmapPeer = bmp;
		}
		else
		{
			FileInputStream fs = null;

			try {

				BitmapFactory.Options options = new BitmapFactory.Options();
				options.outWidth = 10;
				options.outHeight = 10;
				options.inSampleSize = 10;// 特别注意，这个值越大，相片质量越差，图像越小
				options.inPreferredConfig = Config.ARGB_4444;
				options.inPurgeable = true;
				options.inInputShareable = true;
				options.inDither = false;
				options.inTempStorage = new byte[12 * 1024];
				try {
					fs = new FileInputStream(new File(pathFileName));
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}

				mBitmapPeer = BitmapFactory.decodeFileDescriptor(fs.getFD(), null, options);

			} catch (Exception e) {
				return 0;
			} finally {
				if (fs != null) {
					try {
						fs.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}


//			try {
//				File file2 = new File(pathFileName);
//				BitmapFactory.Options o = new BitmapFactory.Options();
//				//o.inJustDecodeBounds = true;
//				mBitmapPeer = BitmapFactory.decodeStream(new FileInputStream(file2), null, o);
//			}
//			catch (FileNotFoundException e)
//			{
//				Log.i("WG", "Load file error.");
//			}
		}

		if (mBitmapPeer == null)
		{
			return 0;
		}

		if (BmpConfig == Config.ARGB_8888)
		{

		}
		else
		{
//			Bitmap bitmap = Bitmap.createBitmap(mBitmapPeer.getWidth(), mBitmapPeer.getHeight(), Bitmap.Config.RGB_565);
//
//			ByteBuffer buffer = ByteBuffer.wrap(data);
//
//			bitmap.copyPixelsFromBuffer(buffer);
//
//			Bitmap bitmapdel = mBitmapPeer;
//			mBitmapPeer = bitmap;
//
//			bitmapdel.recycle();
		}

		return 1;
	}
	// 缓冲
	public int Cach()
	{
		if (mBitmapPeer == null)
		{
			return 0;
		}

		String filename = String.valueOf(iPageId);
		String pathFileName = BmpCachDir + filename + ImgFileSurfix;
		//mBitmapPeer = BitmapFactory.decodeFile(pathFileName); //获取位图
		SaveBitmap(pathFileName, mBitmapPeer, ImgFileSurfix);

		if (mBitmapPeer == null)
		{
			return 0;
		}

		return 1;
	}

	/**
	 * 删除单个文件
	 * @param   filePath    被删除文件的文件名
	 * @return 文件删除成功返回true，否则返回false
	 */
	public boolean deleteFile(String filePath) {
		File file = new File(filePath);
		if (file.isFile() && file.exists()) {
			return file.delete();
		}
		return false;
	}

	//判断mBitmapPeer是否有效
	public boolean isBmpValid()
	{
		if (mBitmapPeer == null)
		{
			return false;
		}
		else if (mBitmapPeer.isRecycled())
		{
			return false;
		}

		return true;
	}

	// 创建主位图和画布:mBitmapPeer
	public int CreateBmp()
	{
		if (mBitmapPeer == null)
		{
			mBitmapPeer = Bitmap.createBitmap(ImgWidth, ImgHeight, BmpConfig);
			piccanvasPeer = new Canvas(mBitmapPeer);  //绘制收包图
		}
		else if (mBitmapPeer.isRecycled()) {
			mBitmapPeer = Bitmap.createBitmap(ImgWidth, ImgHeight, BmpConfig);
			piccanvasPeer = new Canvas(mBitmapPeer); // 绘制收包图
		}

		if (mBitmapPeer == null)
		{
			return 0;
		}

		// 背景
//    	if (mBitmapPeerBG == null)
//    	{
//    		mBitmapPeerBG = GetBackgoundBmp(BackGoundImgId, mBitmapPeerRct.right, mBitmapPeerRct.bottom);
//    	}

		return 1;
	}

	// 创建背景位图
	public Bitmap GetBackgoundBmp(int BackGoundImgId, int w, int h)
	{
		Bitmap zoombmplocal;
		zoombmplocal = BitmapFactory.decodeResource(this.getContext().getResources(), BackGoundImgId); //R.drawable.lambskin07);x
		//float w = mBitmapPeerRct.right; //this.getWidth(); //zoombmplocal.getWidth();
		//float h = mBitmapPeerRct.bottom; //this.getHeight();
		Bitmap BitmapPeerBG = zoomBitmap(zoombmplocal, (int)(w ), (int)(h));
		zoombmplocal.recycle();

		return BitmapPeerBG;
	}

	public void render()
	{
		//mCanvas = piccanvasPeer;

		CreateBmp();
		if (!this.isBmpValid())
		{
			return;
		}

		if (Drawed == 0) {
			Drawed = 1;

			double scale = 1.0;

			/*
			 * mPaint1.setColor(Color.GREEN); //int x = 100 * sin(gAngle / (2 *
			 * 3.14159);) int x = gAngle; gAngle += 1; gAngle = gAngle % 500;
			 * mPath2.moveTo(x, 20); mPath2.lineTo(300, 800);
			 * mCanvas.drawPath(mPath2, mPaint1);
			 */

			if (iPageId == -1) {
				System.out.println("PicView onDraw(), error, iPageId is -1.");

				return;
			}

			// drawwg(mCanvas);
			// drawwgFromFile(mCanvas);
			// mCanvas.drawBitmap(mBitmapPeer, left, top, paint);
			// drawwg3(mCanvas, ParaIntArray);
			//drawwgFromMem(mCanvas);
			//drawwgFromMem(piccanvasPeer);
		}

		//if (DrawOderSet.length > 0)
		if (ReDraw > 0)
		{
			//System.out.println("PicView onDraw(), iPageId " + iPageId + " is redrawed.");

			long tick = System.currentTimeMillis();

			drawwgFromMem(); // piccanvasPeer

			tick = System.currentTimeMillis() - tick;
			Log.i("WG", "draw picview:" + iPageId + ", take time:" + tick + "ms.");

			// 暂时补充画边框
			int x0, y0, x1, y1;
			x0 = 0 + 1;
			y0 = 0 + 1;
			x1 = mBitmapPeer.getWidth() - 2;
			y1 = mBitmapPeer.getHeight() - 2;
			mPaint1.reset();
			mPaint1.setStrokeWidth(3);
			int PenColor = 0x888888; // gray
			mPaint1.setColor(PenColor | 0xFF000000);
			AppLayerLineTo(piccanvasPeer, iPageId, x0, y0, x1, y0, mPaint1);
			AppLayerLineTo(piccanvasPeer, iPageId, x1, y0, x1, y1, mPaint1);
			AppLayerLineTo(piccanvasPeer, iPageId, x1, y1, x0, y1, mPaint1);
			AppLayerLineTo(piccanvasPeer, iPageId, x0, y1, x0, y0, mPaint1);
			//AppLayerLineTo(piccanvasPeer, iPageId, x0, y0, x1, y1);

			ReDraw = 0;
		}
	}

	@Override
	protected void onDraw(Canvas mCanvas)
	{
		//mCanvas = piccanvasPeer;

		super.onDraw(mCanvas);

		onDrawDir(mCanvas);
	}

	public void onDrawDir(Canvas mCanvas)
	{
		//mCanvas = piccanvasPeer;

		//super.onDraw(mCanvas);

		//render();

		// 显示
		if (true)
		{
			if (mBitmapPeerRctDst.right <=0 )
			{
				System.out.println("PicView onDraw(), error, mBitmapPeerRctDst.right is 0.");
				return;
			}

			if (mBitmapPeer.getWidth() == mBitmapPeerRctDst.width()
					&& mBitmapPeer.getHeight() == mBitmapPeerRctDst.height())
			{
				// 原始比例

				// 平移
				Matrix matrix = new Matrix();
				matrix.setTranslate(-OffsetX, 0);

//				//设置旋转30°，以图片中心
//				matrix.setRotate(30, zoombmp.getWidth()/2, zoombmp.getHeight()/2);
//				//设置算法，处理旋转后效果
//				mPaint.setAntiAlias(true);

				mCanvas.drawBitmap(mBitmapPeer, matrix, mPaint);
			}
			else
			{
				// 缩放, 利用matrix和createBitmap

				Bitmap zoombmp;
				//if (zoombmp == null)
				{
					zoombmp = zoomBitmap(mBitmapPeer, mBitmapPeerRctDst.width(), mBitmapPeerRctDst.height());
				}

//				mBitmapPeerRctMin.left = 0;
//				mBitmapPeerRctMin.top = 0;
//				mBitmapPeerRctMin.right = Math.min(mBitmapPeerRctDst.right, mBitmapPeerRct.right);
//				mBitmapPeerRctMin.bottom = Math.min(mBitmapPeerRctDst.bottom, mBitmapPeerRct.bottom);
//	            mCanvas.drawBitmap(zoombmp, mBitmapPeerRctDst, mBitmapPeerRctMin, mPaint);
//				mBitmapPeerRctOffsetX = mBitmapPeerRctDst;
//				mBitmapPeerRctOffsetX.left += 20;//OffsetX;
//				mBitmapPeerRctOffsetX.right += 20; //OffsetX;
//				mBitmapPeerRct.left += 20;//OffsetX;
//				mBitmapPeerRct.right += 20; //OffsetX;
//				mCanvas.drawBitmap(zoombmp, mBitmapPeerRctOffsetX, mBitmapPeerRct, mPaint);

				// 平移
				Matrix matrix = new Matrix();
				matrix.setTranslate(-OffsetX, 0);

//				//设置旋转30°，以图片中心
//				matrix.setRotate(30, zoombmp.getWidth()/2, zoombmp.getHeight()/2);
//				//设置算法，处理旋转后效果
//				mPaint.setAntiAlias(true);

				mCanvas.drawBitmap(zoombmp, matrix, mPaint);

				zoombmp.recycle();
			}
		}


		// 画鼠标
		if (mMouseTimeSlice_iPageId != -1)
		{
			mPaint1.reset();
			//mPaint1.setStyle(Paint.Style.STROKE);
			mPaint1.setStrokeWidth(2);
			mPaint1.setColor(0xff0000 | 0xFF000000); // 红
			int x = mMouseTimeSlice_xInPage - 2;
			float scalesum = mScale / ImgAndViewK;
			mCanvas.drawLine(x * scalesum - OffsetX, mMouseTimeSlice_y0InPage * scalesum, x * scalesum - OffsetX, mMouseTimeSlice_y1InPage * scalesum, mPaint1);// 画线
			//System.out.println("mMouseTimeSlice_y0InPage:" + mMouseTimeSlice_y0InPage + ". mScale: " + mScale + ". x: " + x + ". OffsetX: " + OffsetX + ". mMouseTimeSlice_y1InPage: " + mMouseTimeSlice_y1InPage);
		}

		// Play Line
		if (mPlayLine_iPageId != -1)
		{
			mPaint1.reset();
			//mPaint1.setStyle(Paint.Style.STROKE);
			mPaint1.setStrokeWidth(2);
			mPaint1.setColor(0x0000ff | 0xFF000000); // blue
			int x = mPlayLine_xInPage - 2;
			float scalesum = mScale / ImgAndViewK;
			mCanvas.drawLine(x * scalesum - OffsetX, mPlayLine_y0InPage * scalesum, x * scalesum - OffsetX, mPlayLine_y1InPage * scalesum, mPaint1);// 画线
			//System.out.println("mMouseTimeSlice_y0InPage:" + mMouseTimeSlice_y0InPage + ". mScale: " + mScale + ". x: " + x + ". OffsetX: " + OffsetX + ". mMouseTimeSlice_y1InPage: " + mMouseTimeSlice_y1InPage);
		}

	}

	public int PrepareNoteImg()
	{
		return 1;
	}

	// byte sting to int
	public static int byteArrayToInt(byte[] b, int offset)
	{
		int value = 0;

		for (int i = 0; i < 4; i++)
		{
			//int shift = (4 - 1 - i) * 8;
			int shift = i * 8;
			value += (b[i + offset] & 0x000000FF) << shift;
		}

		return value;
	}


	public int drawwgFromMem() //Canvas mCanvas)
	{

		int i = 0;

		if (mBitmapPeer == null)
		{
			return 0;
//			mBitmapPeer = Bitmap.createBitmap(ImgWidth, ImgHeight, Bitmap.Config.ARGB_8888);
//			piccanvasPeer = new Canvas(mBitmapPeer);  //绘制收包图
		}

		Canvas mCanvas = piccanvasPeer;


		// test
//		if (this.iPageId != 1)
//		{
//			return 1;
//		}

		// test


		//if (false)
		{
			int PageId = -1;

			int cmdcount = 0;

			//if (this.iPageId == 1)
			{
				System.out.println("PicView drawwgFromMem(), iPageId is " + iPageId + ", size is " + DrawOderSet.length);
			}

			int idx = 0;
			int bidx = 0;
			int bidxnext = 0;
			int maxlen = DrawOderSet.length;
			int[] tempints = new int[4];

			int[] counters = new int[100];		// 调用次数统计
			int[] tc = new int[100];	// 不同绘图操作时间消耗统计 timecounters
			for(i = 0; i < 100; i++)
			{
				counters[i] = 0;
				tc[i] = 0;
			}

			long curtick = 0;
			long curtick2 = 0;

			while(bidxnext < maxlen)
			{
				int paralen = 0;

				cmdcount++;

				//byte[] tempbytes= new byte[100];

				bidx = bidxnext;

				paralen = byteArrayToInt(DrawOderSet, bidx);
				bidx += 4;

				if (paralen > 100)
				{
					System.out.println("may be a error, draw cmd too long.");
					break;
				}

				bidxnext += paralen * 4;

				int drawtype;
				drawtype = byteArrayToInt(DrawOderSet, bidx);
				bidx += 4;
				//System.out.println(String.valueOf(idx) + ": drawtype is " + String.valueOf(drawtype));
				idx++;

				// test
//					if (idx > 40000)
//					{
//						break;
//					}

				////System.out.println("drawtype is " + drawtype + ", iPageId is " + iPageId + " bidxnext is " + bidxnext + "maxlen is " + maxlen);
				// inportant print
				//Log.e("WG", "cmdcount :" + cmdcount + ", drawtype is " + drawtype);

				// test
				if (drawtype < 100)
				{
					counters[drawtype]++;
				}

				curtick = System.currentTimeMillis();

				if (drawtype == (int) JAVA_DRAW_API.JFUNC_sAppLayerLineTo.ordinal())
				{
					int x0, y0, x1, y1;

					PageId = byteArrayToInt(DrawOderSet, bidx);
					bidx += 4;

					x0 = byteArrayToInt(DrawOderSet, bidx);
					bidx += 4;

					y0 = byteArrayToInt(DrawOderSet, bidx);
					bidx += 4;

					x1 = byteArrayToInt(DrawOderSet, bidx);
					bidx += 4;

					y1 = byteArrayToInt(DrawOderSet, bidx);
					bidx += 4;

					if (PageId != this.iPageId)
					{
						continue;
					}

					AppLayerLineTo(mCanvas, PageId, x0, y0, x1, y1, mPaint1);

					// 为了美化3D翻页的坚线闪烁问题，坚线加粗
					//if (x0 == x1)
					if (false)
					{
						//AppLayerLineTo(mCanvas, PageId, x0-1, y0, x1-1, y1, mPaintGray);
						AppLayerLineTo(mCanvas, PageId, x0+1, y0, x1+1, y1, mPaintGray);
					}
				}
				else if (drawtype == (int) JAVA_DRAW_API.JFUNC_sAppLayerSmoothLine.ordinal())
				{
					//int iPageId;
					int x0, y0, x1, y1, width, color;

					i = 4;

					PageId = byteArrayToInt(DrawOderSet, bidx);
					bidx += 4;

					x0= byteArrayToInt(DrawOderSet, bidx);
					bidx += 4;

					y0= byteArrayToInt(DrawOderSet, bidx);
					bidx += 4;

					x1 = byteArrayToInt(DrawOderSet, bidx);
					bidx += 4;

					y1 = byteArrayToInt(DrawOderSet, bidx);
					bidx += 4;

					width = byteArrayToInt(DrawOderSet, bidx);
					bidx += 4;

					color = byteArrayToInt(DrawOderSet, bidx);
					bidx += 4;

					if (PageId != this.iPageId)
					{
						continue;
					}

					AppLayerSmoothLine(mCanvas, PageId, x0, y0, x1, y1, width, color);
				}
				else if (drawtype ==(int) JAVA_DRAW_API.JFUNC_sAppLayerSetPixel.ordinal())
				{
					//int iPageId;
					int x0, y0, clrref;

					PageId = byteArrayToInt(DrawOderSet, bidx);
					bidx += 4;

					x0= byteArrayToInt(DrawOderSet, bidx);
					bidx += 4;

					y0= byteArrayToInt(DrawOderSet, bidx);
					bidx += 4;

					clrref= byteArrayToInt(DrawOderSet, bidx);
					bidx += 4;

					if (PageId != this.iPageId)
					{
						continue;
					}

					AppLayerSetPixel(mCanvas, PageId, x0, y0, clrref);
				}
				else if (drawtype == (int) JAVA_DRAW_API.JFUNC_sAppLayerLoadABmp.ordinal())
				{
					//int iPageId;
					int x0, y0, eNoteType, cNoteSelected;
					int zoomx;
					int zoomy;

					PageId = byteArrayToInt(DrawOderSet, bidx);
					bidx += 4;

					eNoteType = byteArrayToInt(DrawOderSet, bidx);
					bidx += 4;

					x0= byteArrayToInt(DrawOderSet, bidx);
					bidx += 4;

					y0= byteArrayToInt(DrawOderSet, bidx);
					bidx += 4;

					cNoteSelected= byteArrayToInt(DrawOderSet, bidx);
					bidx += 4;

					zoomx= byteArrayToInt(DrawOderSet, bidx);
					bidx += 4;

					zoomy= byteArrayToInt(DrawOderSet, bidx);
					bidx += 4;

					if (PageId != this.iPageId)
					{
						continue;
					}

					AppLayerLoadABmp(mCanvas, PageId, eNoteType, x0, y0, cNoteSelected, (float) zoomx / 100, (float) zoomy / 100);
				}
				else if (drawtype == (int) JAVA_DRAW_API.JFUNC_sAppLayerDrawParabola.ordinal())
				{
					//int iPageId;
					int x0_org, x1_org, x0, x1, xOffset, yOffset, width, cSelect;
					float a, b;
					int aS, bS; // 正负指示, 1:正，0:负
					float aI, aF, bI, bF; // 整数部分，小数部分

					PageId = byteArrayToInt(DrawOderSet, bidx);
					bidx += 4;

					x0_org= byteArrayToInt(DrawOderSet, bidx);
					bidx += 4;

					x1_org= byteArrayToInt(DrawOderSet, bidx);
					bidx += 4;

					x0= byteArrayToInt(DrawOderSet, bidx);
					bidx += 4;

					x1= byteArrayToInt(DrawOderSet, bidx);
					bidx += 4;

					xOffset= byteArrayToInt(DrawOderSet, bidx);
					bidx += 4;

					yOffset= byteArrayToInt(DrawOderSet, bidx);
					bidx += 4;

					aS= byteArrayToInt(DrawOderSet, bidx);
					bidx += 4;

					aI= (float)byteArrayToInt(DrawOderSet, bidx);
					bidx += 4;

					aF= (float)byteArrayToInt(DrawOderSet, bidx);
					bidx += 4;

					bS= byteArrayToInt(DrawOderSet, bidx);
					bidx += 4;

					bI= (float)byteArrayToInt(DrawOderSet, bidx);
					bidx += 4;

					bF= (float)byteArrayToInt(DrawOderSet, bidx);
					bidx += 4;

					width= byteArrayToInt(DrawOderSet, bidx);
					bidx += 4;

					cSelect= byteArrayToInt(DrawOderSet, bidx);
					bidx+= 4;

					a = aI + aF / 1000000;
					b = bI + bF / 1000000;

					if (aS == 0)
					{
						a = -a;
					}

					if (bS == 0)
					{
						b = -b;
					}


					if (PageId != this.iPageId)
					{
						continue;
					}

					AppLayerDrawParabola(mCanvas, iPageId, (float)x0_org, (float)x1_org, (float)x0, (float)x1, xOffset, yOffset, a, b, width, (char)cSelect);
				}
				else if (drawtype == (int) JAVA_DRAW_API.JFUNC_sAppLayerDrawBCurve.ordinal())
				{
					//int iPageId;
					//int x0_org, x1_org, x0, x1, xOffset, yOffset, width, cSelect;

					int x0_draw, x1_draw, xOffset, yOffset;
					int x0, x1, x2, x3;
					int y0, y1, y2, y3;
					int width, cSelect;

					PageId = byteArrayToInt(DrawOderSet, bidx);
					bidx += 4;

					x0_draw= byteArrayToInt(DrawOderSet, bidx);
					bidx += 4;

					x1_draw= byteArrayToInt(DrawOderSet, bidx);
					bidx += 4;

					xOffset= byteArrayToInt(DrawOderSet, bidx);
					bidx += 4;

					yOffset= byteArrayToInt(DrawOderSet, bidx);
					bidx += 4;

					x0= byteArrayToInt(DrawOderSet, bidx);
					bidx += 4;

					x1= byteArrayToInt(DrawOderSet, bidx);
					bidx += 4;

					x2= byteArrayToInt(DrawOderSet, bidx);
					bidx += 4;

					x3= byteArrayToInt(DrawOderSet, bidx);
					bidx += 4;

					y0= byteArrayToInt(DrawOderSet, bidx);
					bidx += 4;

					y1= byteArrayToInt(DrawOderSet, bidx);
					bidx += 4;

					y2= byteArrayToInt(DrawOderSet, bidx);
					bidx += 4;

					y3= byteArrayToInt(DrawOderSet, bidx);
					bidx += 4;

					width= byteArrayToInt(DrawOderSet, bidx);
					bidx += 4;

					cSelect= byteArrayToInt(DrawOderSet, bidx);
					bidx+= 4;

					if (PageId != this.iPageId)
					{
						continue;
					}

					AppLayerDrawBCurve(mCanvas, iPageId,
							x0_draw, x1_draw, xOffset, yOffset,
							x0, x1, x2, x3,
							y0, y1, y2, y3,
							width, (char)cSelect);
				}
				else if (drawtype == (int) JAVA_DRAW_API.JFUNC_sAppLayerOutText.ordinal())
				{
					//System.out.println("drawtype == 4 JFUNC_sAppLayerOutText, paralen is " + paralen);
					int FontId, x, y, charcount;
					String pstr;

					PageId = byteArrayToInt(DrawOderSet, bidx);
					bidx += 4;

					FontId= byteArrayToInt(DrawOderSet, bidx);
					bidx += 4;

					x= byteArrayToInt(DrawOderSet, bidx);
					x = getSignInt(x);
					bidx += 4;

					y= byteArrayToInt(DrawOderSet, bidx);
					y = getSignInt(y);
					bidx += 4;

					charcount= byteArrayToInt(DrawOderSet, bidx);
					bidx += 4;

					byte[] byBuffer;
					byBuffer = new byte[charcount + 1];
					for(i = 0; i < charcount; i++)
					{
						byBuffer[i] = DrawOderSet[bidx + i];
						if (byBuffer[i] == 0)
						{
							break;
						}
					}
					byBuffer[i] = 0;
					try {
						pstr = new String(byBuffer, "GBK");	// gb2312, UTF-8, ISO8859-1
						AppLayerOutText(mCanvas, iPageId, FontId, x, y, pstr);
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
				else if (drawtype == (int) JAVA_DRAW_API.JFUNC_sAppLayerSelectPen.ordinal())
				{
					//System.out.println("drawtype == 4 JFUNC_sAppLayerOutText, paralen is " + paralen);
					int PenColor, Width, ePenStyle;

					PageId = byteArrayToInt(DrawOderSet, bidx);
					bidx += 4;

					PenColor= byteArrayToInt(DrawOderSet, bidx);
					bidx += 4;

					Width= byteArrayToInt(DrawOderSet, bidx);
					bidx += 4;

					ePenStyle= byteArrayToInt(DrawOderSet, bidx);
					bidx += 4;

					AppLayerSelectPen(mCanvas, PageId, PenColor, Width, ePenStyle);
				}
				else if (drawtype == (int) JAVA_DRAW_API.JFUNC_sAppLayerPaintWhite.ordinal())
				{
					//System.out.println("drawtype == 8 JFUNC_sAppLayerPaintWhite, paralen is " + paralen);
					int left, top, right, bottom;


					PageId = byteArrayToInt(DrawOderSet, bidx);
					bidx += 4;

					left= byteArrayToInt(DrawOderSet, bidx);
					bidx += 4;

					top= byteArrayToInt(DrawOderSet, bidx);
					bidx += 4;

					right= byteArrayToInt(DrawOderSet, bidx);
					bidx += 4;

					bottom= byteArrayToInt(DrawOderSet, bidx);
					bidx += 4;

					AppLayerPaintWhite(mCanvas, PageId, left, top, right, bottom);

					{
						// 背景 background
						if (false)
						{
							// 有固定的mBitmapPeerBG, 浪费内存
							if (mBitmapPeerBG != null)
							{
								mBitmapPeerBG = GetBackgoundBmp(BackGoundImgId, mBitmapPeerRct.right, mBitmapPeerRct.bottom);

								if (mBitmapPeerBG != null)
								{
									//mBitmapPeer = mBitmapPeerBG.copy(config, isMutable);
									piccanvasPeer.drawBitmap(mBitmapPeerBG, 0, 0, null);
								}
							}
						}
						else
						{
							// 动态创建BitmapPeerBG，用完即回收, 节省内存, 注意速度
							Bitmap BitmapPeerBG;
							BitmapPeerBG = GetBackgoundBmp(BackGoundImgId, mBitmapPeerRct.right, mBitmapPeerRct.bottom);
							if (BitmapPeerBG != null)
							{
								piccanvasPeer.drawBitmap(BitmapPeerBG, 0, 0, null);
								BitmapPeerBG.recycle();
							}
						}
					}
				}

				curtick2 = System.currentTimeMillis();
				long tickdiff = curtick2 - curtick;
				tc[drawtype] += tickdiff;
			}



			// test
			//for(int i = 0; i < 11; i++)
			if (false)
			{
				System.out.println("DrawFromMem: times 0_" + counters[0] + ",1_" + counters[1] + ",2_" + counters[2] + ",3_" + counters[3] + ",4_" + counters[4] + ",5_" + counters[5] + ",6_" + counters[6] + ",7_" + counters[7] + ",8_" + counters[8] + ",9_" + counters[9] + ",10_" + counters[10]);
				System.out.println("DrawFromMem: Dur   0_" + tc[0] + ",1_" + tc[1] + ",2_" + tc[2] + ",3_" + tc[3] + ",4_" + tc[4] + ",5_" + tc[5] + ",6_" + tc[6] + ",7_" + tc[7] + ",8_" + tc[8] + ",9_" + tc[9] + ",10_" + tc[10]);
				System.out.println("DrawFromMem: DurP  0_" + tc[0]/(counters[0] + 0.1) + ",1_" + tc[1]/(counters[1] + 0.1) + ",2_" + tc[2]/(counters[2] + 0.1) + ",3_" + tc[3]/(counters[3] + 0.1) + ",4_" + tc[4]/(counters[4] + 0.1) + ",5_" + tc[5]/(counters[5] + 0.1) + ",6_" + tc[6]/(counters[6] + 0.1) + ",7_" + tc[7]/(counters[7] + 0.1) + ",8_" + tc[8]/(counters[8] + 0.1) + ",9_" + tc[9]/(counters[9] + 0.1) + ",10_" + tc[10]/(counters[10] + 0.1));
			}

		}


		return 1;
	}

	int getSignInt(int UnsigInt)
	{
		if (UnsigInt > 0x7F000000L)
		{
			long xx = 0xFFFFFFFFL / 2; // / 2;
			UnsigInt = -((int)xx - UnsigInt);
		}

		return UnsigInt;
	}

	// 作废 no use
	public int drawwg(Canvas mCanvas){

		if (true)
		{
			return 0;
		}

		int x0 = 0;
		int y0 = 0;
		mPaint1.reset();
		mPaint1.setStyle(Paint.Style.STROKE);
		mPaint1.setStrokeWidth(2);

		//pDC->SelectObject(myPen2);
		mPath2.reset();
		mPaint1.setColor(Color.GREEN);
		mPath2.moveTo(10, 20);
		mPath2.lineTo(100, 200);

		mCanvas.drawText("下行:BR:", x0, y0, mPaint1);

		// 创建画笔
		Paint p = new Paint();
		p.setColor(Color.RED);// 设置红色

		mCanvas.drawText("画圆：", 10, 20, p);// 画文本
		mCanvas.drawCircle(60, 20, 10, p);// 小圆
		p.setAntiAlias(true);// 设置画笔的锯齿效果。 true是去除，大家一看效果就明白了
		mCanvas.drawCircle(120, 20, 20, p);// 大圆

		mCanvas.drawText("画线及弧线：", 10, 60, p);
		p.setColor(Color.GREEN);// 设置绿色
		mCanvas.drawLine(60, 40, 100, 40, p);// 画线
		mCanvas.drawLine(110, 40, 190, 80, p);// 斜线
		//画笑脸弧线
		p.setStyle(Paint.Style.STROKE);//设置空心
		RectF oval1=new RectF(150,20,180,40);
		mCanvas.drawArc(oval1, 180, 180, false, p);//小弧形
		oval1.set(190, 20, 220, 40);
		mCanvas.drawArc(oval1, 180, 180, false, p);//小弧形
		oval1.set(160, 30, 210, 60);
		mCanvas.drawArc(oval1, 0, 180, false, p);//小弧形

		mCanvas.drawText("画矩形：", 10, 80, p);
		p.setColor(Color.GRAY);// 设置灰色
		p.setStyle(Paint.Style.FILL);//设置填满
		mCanvas.drawRect(60, 60, 80, 80, p);// 正方形
		mCanvas.drawRect(60, 90, 160, 100, p);// 长方形

		mCanvas.drawText("画扇形和椭圆:", 10, 120, p);
		/* 设置渐变色 这个正方形的颜色是改变的 */
		Shader mShader = new LinearGradient(0, 0, 100, 100,
				new int[] { Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW,
						Color.LTGRAY }, null, TileMode.REPEAT); // 一个材质,打造出一个线性梯度沿著一条线。
		p.setShader(mShader);
		// p.setColor(Color.BLUE);
		RectF oval2 = new RectF(60, 100, 200, 240);// 设置个新的长方形，扫描测量
		mCanvas.drawArc(oval2, 200, 130, true, p);
		// 画弧，第一个参数是RectF：该类是第二个参数是角度的开始，第三个参数是多少度，第四个参数是真的时候画扇形，是假的时候画弧线
		//画椭圆，把oval改一下
		oval2.set(210,100,250,130);
		mCanvas.drawOval(oval2, p);

		mCanvas.drawText("画三角形：", 10, 200, p);
		// 绘制这个三角形,你可以绘制任意多边形
		Path path = new Path();
		path.moveTo(80, 200);// 此点为多边形的起点
		path.lineTo(120, 250);
		path.lineTo(80, 250);
		path.close(); // 使这些点构成封闭的多边形
		mCanvas.drawPath(path, p);

		// 你可以绘制很多任意多边形，比如下面画六连形
		p.reset();//重置
		p.setColor(Color.LTGRAY);
		p.setStyle(Paint.Style.STROKE);//设置空心
		Path path1=new Path();
		path1.moveTo(180, 200);
		path1.lineTo(200, 200);
		path1.lineTo(210, 210);
		path1.lineTo(200, 220);
		path1.lineTo(180, 220);
		path1.lineTo(170, 210);
		path1.close();//封闭
		mCanvas.drawPath(path1, p);
		/*
		* Path类封装复合(多轮廓几何图形的路径
		* 由直线段*、二次曲线,和三次方曲线，也可画以油画。drawPath(路径、油漆),要么已填充的或抚摸
		* (基于油漆的风格),或者可以用于剪断或画画的文本在路径。
		*/

		//画圆角矩形
		p.setStyle(Paint.Style.FILL);//充满
		p.setColor(Color.LTGRAY);
		p.setAntiAlias(true);// 设置画笔的锯齿效果
		mCanvas.drawText("画圆角矩形:", 10, 260, p);
		RectF oval3 = new RectF(80, 260, 200, 300);// 设置个新的长方形
		mCanvas.drawRoundRect(oval3, 20, 15, p);//第二个参数是x半径，第三个参数是y半径

		//画贝塞尔曲线
		mCanvas.drawText("画贝塞尔曲线:", 10, 310, p);
		p.reset();
		p.setStyle(Paint.Style.STROKE);
		p.setColor(Color.GREEN);
		Path path2=new Path();
		path2.moveTo(100, 320);//设置Path的起点
		path2.quadTo(150, 310, 170, 400); //设置贝塞尔曲线的控制点坐标和终点坐标
		mCanvas.drawPath(path2, p);//画出贝塞尔曲线

		//画点
		p.setStyle(Paint.Style.FILL);
		mCanvas.drawText("画点：", 10, 390, p);

		int x = 60;
		for(x = 60; x < 120; x += 2)		{

			mCanvas.drawPoint(x, 390, p);//画一个点

			p.setColor(Color.RED);
			mCanvas.drawLine(x, 391, x+2, 391, p);// 画线

			p.setColor(Color.BLUE);
			mCanvas.drawLine(x, 392, x+2, 392, p);// 画线
		}

		//mCanvas.drawPoints(new float[]{60,400,65,400,70,400}, p);//画多个点

		//画图片，就是贴图
		Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
		mCanvas.drawBitmap(bitmap, 250,360, p);


		return 1;
	}

	public void SetScreenType(int i){
		this.SCREEN_TYPE = i;
	}


	///////////////////////////////////////////////////////////////////

	//选笔
	public int AppLayerSelectPen(Canvas mCanvas, int iPageId, int PenColor, int Width, int ePenStyle) // enum ENUM_PEN_STYLE
	{
		//	ENUM_PEN_STYLE_SOLID,			// 实绩
		// ENUM_PEN_STYLE_DASHED,			// 虚绩


		mPaint1.reset();
		//mPaint1.setStyle(Paint.Style.STROKE);
		mPaint1.setStrokeWidth(Width);
		if (ePenStyle == 0)
		{

		}
		else
		{
			mPaint1.setPathEffect(pathEffect);
		}
		//mPaint1.setPathEffect(pathEffect);
		int r = 0;
		mPaint1.setColor(PenColor | 0xFF000000);

		/*
		CPen * pPen = 0;
		enum ENUM_PEN Pen; // = ENUM_PEN_BUTT;

		CDC * pDC = mAppScorePage[iPageId]->pDC;
		if (!pDC)
		{
			WriteLog(USC_LOG_NOLOG, "AppLayerSelectPen(), error, pDC is null, page id:%d.", iPageId);
			return 0;
		}

		if (ePenStyle >= ENUM_PEN_STYLE_BUTT)
		{
			WriteLog(USC_LOG_NOLOG, "AppLayerSelectPen(), error, Low mem. page id:%d.", iPageId);
			return -1;
		}

		switch(PenColor)
		{
		case 0x1:
			Pen = ENUM_PEN_BLACK;
			break;
		case 0x888888:
			Pen = ENUM_PEN_GRAY;
			break;
		case 0xFF:
			Pen = ENUM_PEN_RED;
			break;
		case 0xFF00:
			Pen = ENUM_PEN_GREEN;
			break;
		case 0xFF0000:
			Pen = ENUM_PEN_BLUE;
			break;
		default:
			Pen = ENUM_PEN_OTHER;
			if (mpPenSet[0][ENUM_PEN_OTHER])
			{
				delete mpPenSet[0][ENUM_PEN_OTHER];
				mpPenSet[0][ENUM_PEN_BLACK] 	= new CPen(ePenStyle, 1, PenColor);
				if (!mpPenSet[0][ENUM_PEN_BLACK])
				{
					WriteLog(USC_LOG_NOLOG, "AppLayerSelectPen(), error, Low mem, page id:%d.", iPageId);
					return -1;
				}
			}
			break;
		}

		pDC->SelectObject(mpPenSet[ePenStyle][Pen]);
		*/

		return 1;
	}

	//选字体
	public int AppLayerSelectFont(Canvas mCanvas, int iPageId, int eFont) // enum ENUM_FONT
	{
		/*

		CDC * pDC = mAppScorePage[iPageId]->pDC;
		if (!pDC)
		{
			return 0;
		}

		pDC->SelectObject(&mpPenSet[eFont]);
		*/

		return 1;
	}


	//输出文字
	public int AppLayerOutText(Canvas mCanvas, int iPageId, int FontId, int x, int y, String pstr) // char *
	{
		/*
		CDC * pDC = mAppScorePage[iPageId]->pDC;
		if (!pDC)
		{
			return 0;
		}

		TCHAR strText[500];
		MultiByteToWideChar(CP_ACP, MB_ERR_INVALID_CHARS, pstr, strlen(pstr) + 1, strText, MAX_PATH);
		{
			CFont * def_font;
			//def_font = pDC->SelectObject(&mFont0);
			switch(FontId)
			{
			case 0:
				def_font = pDC->SelectObject(mpFontSet[ENUM_FONT_15_THIN_Arial]);
				break;
			case 1:
				def_font = pDC->SelectObject(mpFontSet[ENUM_FONT_25_BOLD_Stencil]);
				break;
			case 2:
				def_font = pDC->SelectObject(mpFontSet[ENUM_FONT_25_BOLD_Monotype_Corsiva]);
				break;
			default:
				ASSERT(FALSE);
				break;
		}
		pDC->TextOut(x, y, strText);
		//pDC->SelectObject(def_font);
		}
		*/

		switch(FontId)
		{
			case 0:
				mPaint.setTextSize(45);                          //设置画笔字体的大小
				break;
			case 1:
				mPaint.setTextSize(15);                          //设置画笔字体的大小
				break;
			default:
				mPaint.setTextSize(20);                          //设置画笔字体的大小
				break;
		}

		mCanvas.drawText(pstr, (float)x, (float)y, mPaint);

		return 1;
	}

	//画线
	public int AppLayerLineTo(Canvas mCanvas, int iPageId, int x0, int y0, int x1, int y1, Paint p)
	{
		/*
		CDC * pDC = mAppScorePage[iPageId]->pDC;
		if (!pDC)
		{
			return 0;
		}

		pDC->MoveTo(x0, y0);
		pDC->LineTo(x1, y1);
		*/

		// 创建画笔
//		Paint p = new Paint();
//		p.setColor(Color.BLACK);// 设置绿色
//		pathEffect = new DashPathEffect(new float[] { 1,2 }, 1);
//		p.setPathEffect(pathEffect);

		//mPaint1.reset();
		//mPaint1.setStyle(Paint.Style.STROKE);
		//mPaint1.setStrokeWidth(2);

		//mCanvas.drawLine(x0, y0, x1, y1, mPaint1);// 画线
		mCanvas.drawLine(x0, y0, x1, y1, p);// 画线


		return 1;
	}

	//画点
	public int AppLayerSetPixel(Canvas mCanvas, int iPageId, int x, int y, int ColorRef) // UINT
	{
		// 创建画笔
		Paint p0 = new Paint();
		p0.setStrokeWidth(1);
		int clr = ColorRef | 0xFF000000;
		p0.setColor(clr);
//		int alpha,red,green,blue;
//		alpha = 255;
//		red = 255;
//		green = 0;
//		blue = 0;
		//p0.setARGB(alpha,red,green,blue);
		mCanvas.drawPoint(x, y, p0);//画一个点
		//mCanvas.drawPoint(200, 390, p0);//画一个点
		/*
		CDC * pDC = mAppScorePage[iPageId]->pDC;
		if (!pDC)
		{
			return 0;
		}

		pDC->SetPixel(x, y, (COLORREF)ColorRef);
		*/

		return 1;
	}

	//刷白
	public int AppLayerPaintWhite(Canvas mCanvas, int iPageId, int left, int top, int right, int bottom)
	{
		/*
		CDC * pDC = mAppScorePage[iPageId]->pDC;
		if (!pDC)
		{
			return 0;
		}

		//CRect rct1(0,0,mWidth,mHeight);
		CRect rct1(0,0,mAppScorePage[iPageId]->pScorePage->iWidth,mAppScorePage[iPageId]->pScorePage->iHeight);
		HBRUSH hB = CreateSolidBrush(RGB(0xff,0xfa,0xe9));    //单色的画刷
		//FillRect ( pScorePage->pDC->m_hDC, &rct1, (HBRUSH) GetStockObject (WHITE_BRUSH));
		FillRect ( pDC->m_hDC, &rct1, hB);
		DeleteObject(hB);
		*/

//		Paint p0 = new Paint();
//		p0.setColor(Color.GRAY);// 设置灰色
//		p0.setStyle(Paint.Style.FILL);//设置填满
		mPaint1.setColor(Color.rgb(BackgoundR, BackgoundG, BackgoundB));// 设置灰色
		mPaint1.setStyle(Paint.Style.FILL);//设置填满
		mCanvas.drawRect(left, top, right, bottom, mPaint1);// 正方形

		return 1;
	}

	//画一行开始处的谱号、节奏号
	public int AppLayerLoadABmp(Canvas mCanvas, int iPageId, int eNoteType, int x, int y, int cNoteSelected, float zoomx, float zoomy) // enum ENUM_NOTE int actx, int acty,
	{
		//Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.pic180);    //获取位图

		Bitmap bmp = mNoteImgSet.mBmps[eNoteType]; //BitmapFactory.decodeFile(pathName); //获取位图
		if (bmp == null)
		{
			Log.i("WG", "AppLayerLoadABmp(), warning, Note missing:" + eNoteType);

			return 0;
		}

		int w = bmp.getWidth();
		int h = bmp.getHeight();

		//y -= h / 2;

		if (cNoteSelected > 0) {

			//Config config = Config.ARGB_8888;
			boolean isMutable = true;
			Bitmap bmpcopy = Bitmap.createBitmap(w, h, BmpConfig);
			bmpcopy.copy(BmpConfig, true);

//			Matrix matrix = new Matrix();
//			matrix.postScale(1, 1); // (0.2f, 0.2f);
//			Bitmap bmpcopy = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(),
//					bmp.getHeight(), matrix, true);

			for (int i = 0; i < h; i++) {
				for (int j = 0; j < w; j++) {

					int color = bmp.getPixel(j, i);
					if ((color & 0xFF) < 5) {
						bmpcopy.setPixel(j, i, Color.RED);
					}
				}
			}

			Paint redPaint;
			redPaint = new Paint();
//			redPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OVER)); //DST_OVER)); //DST_OUT)); x
			float alpha = 100; //255 * k;  //135
			redPaint.setAlpha( (int)alpha ); // 半透明

			if (zoomx == 1 && zoomy == 1)
			{
				mCanvas.drawBitmap(bmpcopy, x, y, redPaint); //null);
			}
			else
			{
//				int bmpw = bmpcopy.getWidth();
//				int bmph = bmpcopy.getHeight();
//				Matrix matrix = new Matrix();
//				float scaleWidth = ((float) bmpw / w);
//				float scaleHeight = ((float) bmph / h);
//				matrix.postScale(scaleWidth, scaleHeight);

				Bitmap zoombmplocal = zoomBitmap(bmpcopy, (int)(w * zoomx), (int)(h * zoomy));

//				mCanvas.drawBitmap(mBitmapPeer, matrix, mPaint);
				mCanvas.drawBitmap(zoombmplocal, x, y, redPaint); //null);

				zoombmplocal.recycle();
			}

			bmpcopy.recycle();
		}
		else
		{
			Paint redPaint;
			redPaint = new Paint();
			redPaint.setXfermode(new PorterDuffXfermode(Mode.DARKEN)); //DST_OVER)); //DST_OUT)); x
//			float alpha = 150; //255 * k;  //135
//			redPaint.setAlpha( (int)alpha ); // 半透明

			if (zoomx == 1 && zoomy == 1)
			{
				mCanvas.drawBitmap(bmp, x, y, redPaint);                       //显示位图
			}
			else
			{
				Bitmap zoombmplocal = zoomBitmap(bmp, (int)(bmp.getWidth() * zoomx), (int)(bmp.getHeight() * zoomy));

				mCanvas.drawBitmap(zoombmplocal, x, y, redPaint);

				zoombmplocal.recycle();
			}
		}

		//bmp.recycle();


		return 1;
	}

	public int AppNotifierSetPixel(int iPageId, int x, int y, int color, Paint paint0)
	{
		int clr = color | 0xFF000000;
		paint0.setColor(clr);
		//int alpha,red,green,blue;
		mDrawCanvas.drawPoint(x, y, paint0);//画一个点

		return 1;
	}

	public int AppLayerSmoothLine(Canvas mCanvas, int iPageId, int x0, int y0, int x1, int y1, int width, int color)
	{
//		if (!mShowDigitalScore)
//		{
//			return 0;
//		}

		float x00, y00, x11, y11;

		int r = color & 0xFF;
		int g = (color & 0xFF00) >> 8;
		int b = (color & 0xFF0000) >> 16;

		int color1;
		int color2;

//	#ifdef WIN32
//		Paint paint0 = 1;
//	#else		// for java
		// java模式, CMScore引擎负责逻辑, 画线的操作交给java应用层
//		AppNotifierSmoothLine(Page->iPageId, x0, y0, x1, y1, width, color);
//		return 1;

		mDrawCanvas = mCanvas;
//		int iPageId = 0;
		Paint paint0 = new Paint();
		paint0.setStrokeWidth(1);
		//int clr = color | 0xFF000000;
		//paint0.setColor(clr);
		//int alpha,red,green,blue;
		//mCanvas.drawPoint(x, y, paint0);//画一个点
//	#endif

		if (x0 < x1)
		{
			x00 = x0;
			y00 = y0;
			x11 = x1;
			y11 = y1;
		}
		else
		{
			x00 = x1;
			y00 = y1;
			x11 = x0;
			y11 = y0;
		}

		float k = 0;
		int w2 = width / 2;

		if (x11 == x00)
		{
			int x = (int)x00 - w2;
			for(int i = 0; i < width; i++, x++)
			{
				for(int y = (int)y00; y <= y11; y++)
				{
					AppNotifierSetPixel(iPageId, x, y, color, paint0);	//(COLORREF)0x0);
				}
			}
		}
		else if (y11 == y00)
		{
			int y = (int)y00 - w2;
			for(int i = 0; i < width; i++, y++)
			{
				for(int x = (int)x00; x <= x11; x++)
				{
					AppNotifierSetPixel(iPageId, x, y, color, paint0);	//(COLORREF)0x0);
				}
			}
		}
		else
		{
			k = (y11 - y00) / (x11 - x00);
			float k_abs = k;
			if (k_abs < 0)
			{
				k_abs = 0 - k_abs;
			}

			if (k_abs <= 1)
			{
				// 小于45度

				// test
				//return 1;

				float y;
				int width_h = width / 2;
				for(int x = 0; x <= x11 - x00; x++)
				{
					y = k_abs * x; // + y00;
					//y = k * x; // + y00;

					//y = y * 1000; // 保留小数点后3位
					//y = y / 1000;
					//float y_Int = (int)y;
					//float y_tail = y - y_Int;
					float y_tail = y - (int)y;

					int r1, r2;
					r1 = (int)(255 - ((255 - r) * (y_tail))); // 零头: 0~最白, 0.99999~最黑
					r2 = 255 - r1; //(255 - ((255 - r) * (1.0 - y_tail))); // 主体

					color1 = r1 + (r1 << 8) + (r1 << 16);
					color2 = r2 + (r2 << 8) + (r2 << 16);
					/*
					color1 = (255 - ((255 - r) * (y_Int / y)))
					+ ((int)(255 - ((255 - g) * (y_Int / y))) << 8)
					+ ((int)(255 - ((255 - b) * (y_Int / y))) << 16);
					color2 = (255 - ((255 - r) * (y_tail / y)))
					+ ((int)(255 - ((255 - g) * (y_tail / y))) << 8)
					+ ((int)(255 - ((255 - b) * (y_tail / y))) << 16);
					*/

					//char buf[300];
					//sprintf(buf, "LIne(), y %f,k %f, x %d, %f, %f, r:%d %d %d %d %d", y, k, x, y_Int, y_tail, r, r0, r1, color1, color2);
					//WriteLog(USC_LOG_NOLOG, "%s", buf);

					int yup;
					int ydown;
					if (k >= 0)
					{
						yup = (int)(y - width_h + y00);
						ydown = yup + width;

						if (width == 1)
						{
							AppNotifierSetPixel(iPageId, (int)(x + x00), (int)(yup), color2, paint0); // 主体
							AppNotifierSetPixel(iPageId, (int)(x + x00), (int)(ydown), color1, paint0);
						}
						else
						{
							AppNotifierSetPixel(iPageId, (int)(x + x00), (int)(yup), color2, paint0); // 主体
							for(int i = 1; i < width; i++)
							{
								AppNotifierSetPixel(iPageId, (int)(x + x00), (int)yup + i, color, paint0);
							}
							AppNotifierSetPixel(iPageId, (int)(x + x00), (int)(ydown), color1, paint0);
						}
					}
					else
					{
						yup = (int)(y - width_h + y00);
						int yBase = (int)( - width_h + y00);
						yup = yBase - (yup - yBase);
						ydown = yup + width;

						if (width == 1)
						{
							AppNotifierSetPixel(iPageId, (int)(x + x00), (int)(ydown), color2, paint0);
							AppNotifierSetPixel(iPageId, (int)(x + x00), (int)(yup), color1, paint0);
						}
						else
						{
							AppNotifierSetPixel(iPageId, (int)(x + x00), (int)(yup), color1, paint0); // 主体
							for(int i = 1; i < width; i++)
							{
								AppNotifierSetPixel(iPageId, (int)(x + x00), (int)yup + i, color, paint0);
							}
							AppNotifierSetPixel(iPageId, (int)(x + x00), (int)(ydown), color2, paint0);
						}
					}
				}
			}
			else
			{
				// >= 45度

				k_abs = (float)1.0 - k_abs;

				float x;
				int width_h = width / 2;
				for(int y = 0; y <= x11 - x00; y++)
				{
					x = k_abs * y; // + y00;
					//x = k * y; // + y00;

					//x = x * 1000; // 保留小数点后3位
					//x = x / 1000;
					//float y_Int = (int)x;
					//float y_tail = x - y_Int;
					float y_tail = x - (int)x;

					int r1, r2;
					r1 = (int)(255 - ((255 - r) * (y_tail))); // 零头: 0~最白, 0.99999~最黑
					r2 = 255 - r1; //(255 - ((255 - r) * (1.0 - y_tail))); // 主体

					color1 = r1 + (r1 << 8) + (r1 << 16);
					color2 = r2 + (r2 << 8) + (r2 << 16);
					/*
					color1 = (255 - ((255 - r) * (y_Int / x)))
					+ ((int)(255 - ((255 - g) * (y_Int / x))) << 8)
					+ ((int)(255 - ((255 - b) * (y_Int / x))) << 16);
					color2 = (255 - ((255 - r) * (y_tail / x)))
					+ ((int)(255 - ((255 - g) * (y_tail / x))) << 8)
					+ ((int)(255 - ((255 - b) * (y_tail / x))) << 16);
					*/

					//char buf[300];
					//sprintf(buf, "LIne(), x %f,k %f, y %d, %f, %f, r:%d %d %d %d %d", x, k, y, y_Int, y_tail, r, r0, r1, color1, color2);
					//WriteLog(USC_LOG_NOLOG, "%s", buf);

					int yup;
					int ydown;
					if (k >= 0)
					{
						yup = (int)(x - width_h + y00);
						ydown = yup + width;

						if (width == 1)
						{
							AppNotifierSetPixel(iPageId, (int)(y + x00), (int)(yup), color2, paint0); // 主体
							AppNotifierSetPixel(iPageId, (int)(y + x00), (int)(ydown), color1, paint0);
						}
						else
						{
							AppNotifierSetPixel(iPageId, (int)(y + x00), (int)(yup), color2, paint0); // 主体
							for(int i = 1; i < width; i++)
							{
								AppNotifierSetPixel(iPageId, (int)(y + x00), (int)yup + i, color, paint0);
							}
							AppNotifierSetPixel(iPageId, (int)(y + x00), (int)(ydown), color1, paint0);
						}
					}
					else
					{
						yup = (int)(x - width_h + y00);
						int yBase = (int)(- width_h + y00);
						yup = yBase - (yup - yBase);
						ydown = yup + width;

						if (width == 1)
						{
							AppNotifierSetPixel(iPageId, (int)(y + x00), (int)(ydown), color2, paint0);
							AppNotifierSetPixel(iPageId, (int)(y + x00), (int)(yup), color1, paint0);
						}
						else
						{
							AppNotifierSetPixel(iPageId, (int)(y + x00), (int)(yup), color1, paint0); // 主体
							for(int i = 1; i < width; i++)
							{
								AppNotifierSetPixel(iPageId, (int)(y + x00), (int)yup + i, color, paint0);
							}
							AppNotifierSetPixel(iPageId, (int)(y + x00), (int)(ydown), color2, paint0);
						}
					}
				}
			}
		}

		return 1;
	}
//	public int AppLayerSmoothLine(Canvas mCanvas, int iPageId, int x0, int y0, int x1, int y1, int width, int color)
//	//int CMusicScore::SmoothLine(SCORE_PAGE * Page, float x0, float y0, float x1, float y1, int width, COLORREF color)
//	{
//		float x00, y00, x11, y11;
//
//		int r = color & 0xFF;
//		int g = (color & 0xFF00) >> 8;
//		int b = (color & 0xFF0000) >> 16;
//
//		int color1;
//		int color2;
//
////	#ifdef WIN32
////		int iPageId = Page->iPageId;
////		Paint paint0 = 1;
////	#else		// for java
//		mDrawCanvas = mCanvas;
//		//int iPageId = 0;
//		Paint paint0 = new Paint();
//		paint0.setStrokeWidth(1);
//		//int clr = color | 0xFF000000;
//		//paint0.setColor(clr);
//		//int alpha,red,green,blue;
//		//mCanvas.drawPoint(x, y, paint0);//画一个点
//	//#endif
//
//		if (x0 < x1)
//		{
//			x00 = x0;
//			y00 = y0;
//			x11 = x1;
//			y11 = y1;
//		}
//		else
//		{
//			x00 = x1;
//			y00 = y1;
//			x11 = x0;
//			y11 = y0;
//		}
//
//		float k = 0;
//		int w2 = width / 2;
//
//		if (x11 == x00)
//		{
//			int x = (int)x00 - w2;
//			for(int i = 0; i < width; i++, x++)
//			{
//				for(int y = (int)y00; y <= y11; y++)
//				{
//					AppNotifierSetPixel(iPageId, x, y, color, paint0);	//(COLORREF)0x0);
//				}
//			}
//		}
//		else if (y11 == y00)
//		{
//			int y = (int)y00 - w2;
//			for(int i = 0; i < width; i++, y++)
//			{
//				for(int x = (int)x00; x <= x11; x++)
//				{
//					AppNotifierSetPixel(iPageId, x, y, color, paint0);	//(COLORREF)0x0);
//				}
//			}
//		}
//		else
//		{
//			k = (y11 - y00) / (x11 - x00);
//			float k_abs = k;
//			if (k_abs < 0)
//			{
//				k_abs = 0 - k_abs;
//			}
//
//			//if (k >= 0)
//			{
//				if (k_abs <= 1)
//				{
//					// 小于45度
//
//					// test
//					//return 1;
//
//					float y;
//					int width_h = width / 2;
//					for(int i = 0; i < width; i++)
//					{
//						for(int x = 0; x <= x11 - x00; x++)
//						{
//							y = k_abs * x; // + y00;
//
//							//y = y * 1000; // 保留小数点后3位
//							//y = y / 1000;
//							//float y_Int = (int)y;
//							//float y_tail = y - y_Int;
//							float y_tail = y - (int)y;
//							y += -width_h + i;
//
//							int r1, r2;
//							r1 = (int)(255 - ((255 - r) * (y_tail))); // 零头: 0~最白, 0.99999~最黑
//							r2 = 255 - r1; //(255 - ((255 - r) * (1.0 - y_tail))); // 主体
//
//							color1 = r1 + (r1 << 8) + (r1 << 16);
//							color2 = r2 + (r2 << 8) + (r2 << 16);
//							/*
//							color1 = (255 - ((255 - r) * (y_Int / y)))
//							+ ((int)(255 - ((255 - g) * (y_Int / y))) << 8)
//							+ ((int)(255 - ((255 - b) * (y_Int / y))) << 16);
//							color2 = (255 - ((255 - r) * (y_tail / y)))
//							+ ((int)(255 - ((255 - g) * (y_tail / y))) << 8)
//							+ ((int)(255 - ((255 - b) * (y_tail / y))) << 16);
//							*/
//
//							//char buf[300];
//							//sprintf(buf, "LIne(), y %f,k %f, x %d, %f, %f, r:%d %d %d %d %d", y, k, x, y_Int, y_tail, r, r0, r1, color1, color2);
//							//WriteLog(USC_LOG_NOLOG, "%s", buf);
//
//							if (k >= 0)
//							{
//								if (width == 1)
//								{
//									AppNotifierSetPixel(iPageId, (int)(x + x00), (int)(y + y00), color2, paint0); // 主体
//									AppNotifierSetPixel(iPageId, (int)(x + x00), (int)(y + 1 + y00), color1, paint0);
//								}
//								else
//								{
//									if (i == 0)
//									{
//										AppNotifierSetPixel(iPageId, (int)(x + x00), (int)(y + y00), color2, paint0); // 主体
//									}
//									else if (i < width - 1)
//									{
//										AppNotifierSetPixel(iPageId, (int)(x + x00), (int)(y + y00), color, paint0);
//									}
//									else	// i == width - 1
//									{
//										AppNotifierSetPixel(iPageId, (int)(x + x00), (int)(y + y00), color, paint0);
//										AppNotifierSetPixel(iPageId, (int)(x + x00), (int)(y + 1 + y00), color1, paint0);
//									}
//								}
//							}
//							else
//							{
//								if (width == 1)
//								{
//									AppNotifierSetPixel(iPageId, (int)(x + x00), (int)(0.0 -((int)y + 1) + y00), color1, paint0);
//									AppNotifierSetPixel(iPageId, (int)(x + x00), (int)(0.0 -(int)y + y00), color2, paint0);
//								}
//								else
//								{
//									if (i == 0)
//									{
//										AppNotifierSetPixel(iPageId, (int)(x + x00), (int)(0.0 - (int)y + y00), color2, paint0);
//									}
//									else if (i < width - 1)
//									{
//										AppNotifierSetPixel(iPageId, (int)(x + x00), (int)(0.0 - (int)y + y00), color, paint0);
//									}
//									else	// i == width - 1
//									{
//										AppNotifierSetPixel(iPageId, (int)(x + x00), (int)(0.0 - (int)y + y00), color, paint0);
//										AppNotifierSetPixel(iPageId, (int)(x + x00), (int)(0.0 -((int)y + 1) + y00), color1, paint0);
//									}
//								}
//
//								//char buf[300];
//								//sprintf(buf, "LIne(), x %d, y %f, x + x00 %f, 0.0 -y + y00 %f int %d, r0 %d", x, y, x + x00, 0.0 -y + y00, (int)( 0.0 -y + y00), r0);
//								//WriteLog(USC_LOG_NOLOG, "%s", buf);
//							}
//						}
//					}
//				}
//				else
//				{
//					// >= 45度
//
//					float x;
//					int width_h = width / 2;
//					int ydiff = (int)(y11 - y00);
//					if (ydiff < 0)
//					{
//						ydiff = -ydiff;
//					}
//					for(int i = 0; i < width; i++)
//					{
//						for(int y = 0; y <= ydiff; y++)
//						{
//							x = (float)1.0 / k_abs * y; // - y00) + x00;
//							//x = x * 1000; // 保留小数点后3位
//							//x = x / 1000;
//							//float x_Int = (int)x;
//							//float x_tail = x - x_Int;
//							float x_tail = x - (int)x;
//							x += -width_h + i;
//
//							int r1, r2;
//							r1 = (int)(255 - ((255 - r) * (x_tail)));		// 零头: 0~最白, 0.99999~最黑
//							r2 = 255- r1; //(255 - ((255 - r) * (1.0 - x_tail))); // 主体
//
//							color1 = r1 + (r1 << 8) + (r1 << 16);
//							color2 = r2 + (r2 << 8) + (r2 << 16);
//
//							if (k >= 0)
//							{
//								if (width == 1)
//								{
//									AppNotifierSetPixel(iPageId, (int)(x + x00), (int)(y + y00), color2, paint0);
//									AppNotifierSetPixel(iPageId, (int)(x + x00 + 1), (int)(y + y00), color1, paint0);
//								}
//								else
//								{
//									if (i == 0)
//									{
//										AppNotifierSetPixel(iPageId, (int)(x + x00), (int)(y + y00), color2, paint0);
//									}
//									else if (i < width - 1)
//									{
//										AppNotifierSetPixel(iPageId, (int)(x + x00), (int)(y + y00), color, paint0);
//									}
//									else	// i == width - 1
//									{
//										AppNotifierSetPixel(iPageId, (int)(x + x00), (int)(y + y00), color, paint0);
//										AppNotifierSetPixel(iPageId, (int)(x + x00 + 1), (int)(y + y00), color1, paint0);
//									}
//								}
//							}
//							else
//							{
//
//								if (width == 1)
//								{
//									AppNotifierSetPixel(iPageId, (int)(0.0 -(int)x + x00), (int)(y + y00), color2, paint0);			// 主体
//									AppNotifierSetPixel(iPageId, (int)(0.0 -((int)x + 1) + x00), (int)(y + y00), color1, paint0);
//								}
//								else
//								{
//									if (i == 0)
//									{
//										AppNotifierSetPixel(iPageId, (int)(0.0 -(int)x + x00), (int)(y + y00), color2, paint0);		// 主体
//									}
//									else if (i < width - 1)
//									{
//										AppNotifierSetPixel(iPageId, (int)(0.0 -(int)x + x00), (int)(y + y00), color, paint0);
//									}
//									else	// i == width - 1
//									{
//										AppNotifierSetPixel(iPageId, (int)(0.0 -(int)x + x00), (int)(y + y00), color, paint0);
//										AppNotifierSetPixel(iPageId, (int)(0.0 -((int)x + 1) + x00), (int)(y + y00), color1, paint0);
//									}
//								}
//							}
//						}
//					}
//				}
//			}
//		}
//
//		return 1;
//	}


//	// 根据两点(第二个点是顶点)确定抛物线系数(a,b,c)。置x0, y0固定为(0, 0), 所以c = 0
//	public int GetParabola2(float x1, float y1, OUTPUT float *a, float *b)
//	{
//		a[0] =  0.0 - y1 / (x1 * x1);
//		b[0] = 0.0 - 2 * a[0] * x1;
//
//		return 1;
//	}

	// 画一个抛物线
	// float x0_org, float x1_org:整个抛物线的x区间
	// float x0, float x1: 要绘制的x区间
	public int AppLayerDrawParabola(Canvas mCanvas, int iPageId, float x0_org, float x1_org, float x0, float x1, int xOffset, int yOffset, float a, float b, int width, char cSelect)
	{
		Paint p0 = new Paint();
		p0.setStrokeWidth(1);
		int clr;
//		int alpha,red,green,blue;
//		alpha = 255;
//		red = 255;
//		green = 0;
//		blue = 0;
//		//p0.setARGB(alpha,red,green,blue);
//		int clr = ColorRef | 0xFF000000;
//		p0.setColor(clr);
//		mCanvas.drawPoint(x, y, p0);//画一个点

		float y = 0;
		float y1 = 0;
		float y11 = 0;
		int col = 0x0; // COLORREF

//		// test
//		if (iPageId == 1 && x1 > 100)
//		{
//			y = 1;
//		}

		float L_max = (float)width / 2;
		float L = 1;

		int mod = 3;

		if (mod == 3)	// 按垂直方向控制宽度---------------------
		{
			int s = 1;
			float k = 0;
			float y_org;

			// 抛物线顶点
			int x_topspot = (int)(-b / (a * 2));
			int y_topspot = (int)(a * x_topspot * x_topspot + b * x_topspot);
			y_org = y_topspot;

			// 计算用于削尖抛物线参数, 非龙骨抛物线
			float a_Sharp_left;
			float b_Sharp_left;
			float a_Sharp_right;
			float b_Sharp_right;
			{
				//GetParabola2(x_topspot, L_max, &a_Sharp_left, &b_Sharp_left);
				a_Sharp_left = (float)(0.0 - L_max / (x_topspot * x_topspot));
				b_Sharp_left = (float)(0.0 - 2 * a_Sharp_left * x_topspot);
				//GetParabola2(x1_org - x_topspot, L_max, &a_Sharp_right, &b_Sharp_right);
				a_Sharp_right = (float)(0.0 - L_max / ((x1_org - x_topspot) * (x1_org - x_topspot)));
				b_Sharp_right = (float)(0.0 - 2 * a_Sharp_right * (x1_org - x_topspot));
			}

			for(int x = (int)x0; x < x1; x++)
			{
				y_org = a * x * x + b * x;
				//y11 = a * (x+s) * (x+s) + b * (x+s);
				//SmoothLine(Page, x + xOffset, (int)y + yOffset, x + s + xOffset, (int)y1 + yOffset, 1, col);
				//SmoothLine(Page, x + xOffset, (int)y + yOffset, x + 5 + xOffset, (int)y1 + yOffset, 1, col);
				k = a * 2 * x + b;

				{
					int color = col;
					int color1;
					int color2;
					int color_faint;
					//int width = 2;

					//float x,y;

					float x00 = xOffset;
					float y00 = yOffset;

					int r = color & 0xFF;
					int g = (color & 0xFF00) >> 8;
					int bc = (color & 0xFF0000) >> 16;

					//float k = 0;

					{
						//k = (y11 - y00) / (x11 - x00);
						float k_abs = k;
						if (k_abs < 0)
						{
							k_abs = (float)0.0 - k_abs;
						}

						//if (k >= 0)
						{
							if (true) //k_abs < 1)
							{
								// 小于45度

								// 削尖抛物线两头
								if (true)
								{
									// 抛物线模式宽度控制

									float dl= x_topspot;
									float dr= x_topspot;
									if (x < dl) // 左部削尖
									{
										int xx = (int)(x - x0_org);
										L = a_Sharp_left * xx * xx + b_Sharp_left * xx;
										L = L > 0?L: -L;

										//char buf[600];
										//sprintf(buf, "DrawParabola(), a_%.2f, b_%.2f, x_%.2f, X_%.2f, y_org_%.2f, Y_%.2f, L_%.2f, k_%.2f, kp_%.2f, B_%.2f.", a, b, (float)x, X, y_org, Y, L, k, kp, B);
										//WriteLog(USC_LOG_NOLOG, "%s", buf);
									}
									else if (x > dr) // 右部削尖
									{
										int xx = (int)((x1_org - dr) - (x - dr));
										L = a_Sharp_right * xx * xx + b_Sharp_right * xx;
										L = L > 0?L: -L;
										//L = L_max - L;
									}
									else
									{
										L = L_max;
									}
								}
//								else
//								{
//									// 线性等比抛物线宽度控制
//
//									//float yPlus;
//									float k_faint = 1;
//									float kd = 1; // 5
//									float dl= x_topspot / kd;
//									float dr= x_topspot + (x1_org - x_topspot) * (kd - 1) / kd;
//									if (x < dl) // 左部削尖
//									{
//										k_faint = (float)x / (dl - 0);
//										//L = L_max * sqrt(sqrt(sqrt(k_faint)));
//										L = L_max * k_faint;
//
//										//char buf[600];
//										//sprintf(buf, "DrawParabola(), a_%.2f, b_%.2f, x_%.2f, X_%.2f, y_org_%.2f, Y_%.2f, L_%.2f, k_%.2f, kp_%.2f, B_%.2f.", a, b, (float)x, X, y_org, Y, L, k, kp, B);
//										//WriteLog(USC_LOG_NOLOG, "%s", buf);
//									}
//									else if (x > dr) // 右部削尖
//									{
//										k_faint = (float)(1.0 - ((float)x - dr) / (x1_org - dr));
//										L = L_max * k_faint;
//									}
//									else
//									{
//										k_faint = 1;
//										L = L_max * k_faint;
//									}
//									L = L_max * k_faint;
//								}

								//float y;
								float yDown;
								float yUp;
								y = y_org >= 0?y_org:-y_org;
								yDown = y - L;
								yUp = y + L;
								for(y = yDown; y <= yUp;)
								{
									//y = y * 100; // 保留小数点后3位
									//y = y / 100;
									float y_Int = (int)y;
									float y_tail = y - y_Int;
									//y += -width_h + i;

									int r0, r1, rfaint;
									r0 = (int)(255 - ((255 - r) * (y_tail)));
									//r1 = (255 - ((255 - r) * (1.0 - y_tail)));
									r1 = 255 - r0;
									rfaint = r;

									color1 = r0 + (r0 << 8) + (r0 << 16);
									color2 = r1 + (r1 << 8) + (r1 << 16);
									//color2 = r1;
									color_faint = rfaint + (rfaint << 8) + (rfaint << 16);
									//color_faint = rfaint;
									/*
									color1 = (255 - ((255 - r) * (y_Int / y)))
									+ ((int)(255 - ((255 - g) * (y_Int / y))) << 8)
									+ ((int)(255 - ((255 - b) * (y_Int / y))) << 16);
									color2 = (255 - ((255 - r) * (y_tail / y)))
									+ ((int)(255 - ((255 - g) * (y_tail / y))) << 8)
									+ ((int)(255 - ((255 - b) * (y_tail / y))) << 16);
									*/

									//char buf[300];
									//sprintf(buf, "LIne(), y %f,k %f, x %d, %f, %f, r:%d %d %d %d %d", y, k, x, y_Int, y_tail, r, r0, r1, color1, color2);
									//WriteLog(USC_LOG_NOLOG, "%s", buf);

									if (true) //k >= 0)
									{
										//y += 0.001;

										if (cSelect > 0)
										{
											color1 &= 0xFF;
											color2 &= 0xFF;
											color_faint &= 0xFF;

											color1 = 0xFF - color1;
											color2 = 0xFF - color2;
											color_faint = 0xFF - color_faint;

											// 从蓝变为红
											color1 = color1 << 16;
											color2 = color2 << 16;
											color_faint = color_faint << 16;

											//color1 |= 0x00FFFF;
											//color2 |= 0x00FFFF;
											//color_faint |= 0x00FFFF;
										}

//										if (0) //L_max == 0.5)
//										{
//											AppNotifierSetPixel(Page->iPageId, x + x00, -y + y00, color2);
//											AppNotifierSetPixel(Page->iPageId, x + x00, -(y + 1) + y00, color1);
//										}
//										else
										{
											if (y == yDown) //下边线
											{
												//AppNotifierSetPixel(Page->iPageId, x + x00, -y + y00, color2);
												clr = color2 | 0xFF000000;
												p0.setColor(clr);
												piccanvasPeer.drawPoint( x + x00, -y + y00, p0);//画一个点
											}
											else if (y < yUp)
											{
												//AppNotifierSetPixel(Page->iPageId, x + x00, -y + y00, color_faint);
												clr = color_faint | 0xFF000000;
												p0.setColor(clr);
												piccanvasPeer.drawPoint(x + x00, -y + y00, p0);//画一个点
											}
											else	// y = yUp
											{
												if ((int)(-y + y00) == (int)(- yDown + y00))
												{
													// 线太细时,有可能和yDwon那条线重叠，此时跳过，不绘
													int z = 0;
												}
												else
												{
													//AppNotifierSetPixel(Page->iPageId, x + x00, -y + y00, color_faint);
													clr = color_faint | 0xFF000000;
													p0.setColor(clr);
													piccanvasPeer.drawPoint(x + x00, -y + y00, p0);//画一个点
												}

												//AppNotifierSetPixel(Page->iPageId, x + x00, -(y + 1) + y00, color1);
												clr = color1 | 0xFF000000;
												p0.setColor(clr);
												piccanvasPeer.drawPoint(x + x00, -(y + 1) + y00, p0);//画一个点
											}
										}

//										int px = (int)(x + x00); // + Page->iMaginLeft + xoffset;
//										int py = (int)(-y + y00); // + Page->iMaginTop;
//										if (px >= 0 && py >= 0)
//										{
//											MarkPointTag(pPicele, Page->mPicElementTagArray, px,  py, Page->iWidth);
//										}
									}

									if (y == yUp)
									{
										break;
									}
									y += 1;
									if (y > yUp)
									{
										//break;
										y = yUp;
									}
								}

							}
						}
					}

					//return 1;
				}
			}
		}

		return 1;
	}

	///////////////////////////////// B Curve //////////////////////////////////////////
//
	// 计算参数tt(0~1)对应的x坐标
	public float BTY_GetX(int byt_xy_count, float byt_xy_x[], int segIdx, float tt)
	{
		float Q_x;
		//for(int i=0;i<mbyt_xy->count-3;i++)
		int i = segIdx;

		if (i > byt_xy_count - 3)
		{
			//ASSERT(FALSE);
			return 0;
		}

		{
			Q_x=(byt_xy_x[i+0]*(-tt*tt*tt+3*tt*tt-3*tt+1)+
					byt_xy_x[i+1]*(3*tt*tt*tt-6*tt*tt+4)+
					byt_xy_x[i+2]*(-3*tt*tt*tt+3*tt*tt+3*tt+1)+
					byt_xy_x[i+3]*(tt*tt*tt))/6;
		}

		return Q_x;
	}

	// 计算参数tt(0~1)对应的y坐标
	public float BTY_GetY(int byt_xy_count, float byt_xy_y[], int segIdx, float tt)
	{
		float Q_y;
		//for(int i=0;i<mbyt_xy->count-3;i++)
		int i = segIdx;

		if (i > byt_xy_count - 3)
		{
			//ASSERT(FALSE);
			return 0;
		}

		{
			Q_y=(byt_xy_y[i+0]*(-tt*tt*tt+3*tt*tt-3*tt+1)+
					byt_xy_y[i+1]*(3*tt*tt*tt-6*tt*tt+4)+
					byt_xy_y[i+2]*(-3*tt*tt*tt+3*tt*tt+3*tt+1)+
					byt_xy_y[i+3]*(tt*tt*tt))/6;
		}

		return Q_y;
	}

	// 绘制b curve
	// input: 绘制区间:x0_draw, int x1_draw; 绘制偏移:xOffset, yOffset
	public int BYTRender(int count, int xCtl[], int yCtl[], Canvas mCanvas, Paint p0, int step, int x0_draw, int x1_draw, int xOffset, int yOffset)
	{
//		Paint p0 = new Paint();
//		p0.setStrokeWidth(1);
		int clr;
		int colordark = 1;
		colordark |= 0xFF000000;

		//CBytCurve mBytCurve;
//		BTYFeedPoints(4, x, y);
//		BYTRender(pDc, floor((double)(2000/byt_xy.count)),
//			x0_draw, x1_draw, xOffset, yOffset);


		/////////////////// B Curve /////////////
		//struct  BYT_xy  mbyt_xy;
		//struct  BYT_xy  mbyt_xy_2;
		int mbyt_xy_count = 0;          /* 坐标点数 */
		float mbyt_xy_x[] = new float[100];         /* X坐标  100*/
		float mbyt_xy_y[] = new float[100];         /* Y坐标 */
		int mbyt_xy_focus = 0;
		//int mbyt_xy_step;
		//int mbyt_xy_2_count;          /* 坐标点数 */
		float mbyt_xy_2_x[] = new float[100];         /* X坐标 */
		float mbyt_xy_2_y[] = new float[100];         /* Y坐标 */
		int mbyt_xy_2_focus = 0;
		//int mbyt_xy_2_step;
		//int mbytStep = step;

//		for(int i = 0; i < count; i++)
//		{
//			mbyt_xy_x[i] = (float)xCtl[i];
//			mbyt_xy_y[i] = (float)yCtl[i];
//			mbyt_xy_2_x[i] = (float)xCtl[i];
//			mbyt_xy_2_y[i] = (float)yCtl[i];
//		}

		{
			// 前三个点置相同值
			int i = 0;
			mbyt_xy_x[i]=(float)xCtl[0];
			mbyt_xy_y[i]=(float)yCtl[0];
			i = 1;
			mbyt_xy_x[i]=(float)xCtl[0];
			mbyt_xy_y[i]=(float)yCtl[0];

			for(i = 0; i < count; i++)
			{
				mbyt_xy_x[i + 2]=(float)xCtl[i];
				mbyt_xy_y[i + 2]=(float)yCtl[i];
			}

			// 后三个点置相同值
			i = count + 2;
			mbyt_xy_x[i]=(float)xCtl[count - 1];
			mbyt_xy_y[i]=(float)yCtl[count - 1];
			i++;
			mbyt_xy_x[i]=(float)xCtl[count - 1];
			mbyt_xy_y[i]=(float)yCtl[count - 1];


			mbyt_xy_count = count + 2 + 2;
			mbyt_xy_focus=0;

			for(i = 0; i < mbyt_xy_count; i++)
			{
				mbyt_xy_2_x[i]=(float)mbyt_xy_x[i];
				mbyt_xy_2_y[i]=(float)mbyt_xy_y[i];
			}

			// 下边沿
			int linew = 1; // 线厚度
			//mbyt_xy_2 = mbyt_xy;
			for(i = 2; i < mbyt_xy_count - 3; i++)
			{
				mbyt_xy_2_y[i] -= linew;
			}
		}

		/////////////////////


		int     i,j,k;
		int     t;
		float  Q_x=0,Q_y=0,tt=0,xx=0;
		float  Q_x2=0,Q_y2=0;

		int minx[] = new int [2]; // = {0};
		int maxx[] = new int [2]; // = {0};
		int Ycount[][] = new int [2][1000]; // = {0};
		float Y[][] = new float [2][1000]; // = {0};

		minx[0] = 0;
		minx[1] = 0;
		maxx[0] = 0;
		maxx[1] = 0;
//		for(i = 0; i < 2; i++)
//		{
//			for(j = 0; j < 1000; j++)
//			{
//				Ycount[i][j] = 0;
//				Y[i][j] = 0;
//			}
//		}

		j=mbyt_xy_count-1;               /* 线段数 */


//	#ifdef WIN32
//		if (1)
//		{
//			//setcolor(RED);
//			CPen redpen(PS_SOLID,1 ,RGB(0, 0, 255));
//			pDc->SelectObject(&redpen);
//			for(i=0;i<mbyt_xy.count;i++)
//			{
//				pDc->Rectangle(mbyt_xy.x[i]-2 + xOffset,mbyt_xy.y[i]-2 + yOffset,mbyt_xy.x[i]+2 + xOffset,mbyt_xy.y[i]+2 + yOffset);
//				//rectangle(mbyt_xy.x[i]-1,mbyt_xy.y[i]-1,mbyt_xy.x[i]+1,mbyt_xy.y[i]+1);
//			}
//			//setcolor(GREEN);
//			//rectangle(mbyt_xy.x[mbyt_xy.focus]-1,mbyt_xy.y[mbyt_xy.focus]-1,\
//			mbyt_xy.x[mbyt_xy.focus]+1,mbyt_xy.y[mbyt_xy.focus]+1);
//
//			CPen geenpen(PS_SOLID,1 ,RGB(255, 0, 0));
//			pDc->SelectObject(&geenpen);
//			pDc->Rectangle(mbyt_xy.x[mbyt_xy.focus]-2 + xOffset,mbyt_xy.y[mbyt_xy.focus]-2 + yOffset,\
//				mbyt_xy.x[mbyt_xy.focus]+2 + xOffset,mbyt_xy.y[mbyt_xy.focus]+2+ yOffset);
//		}
//	#endif

		for(i=0;i<(mbyt_xy_count-3);i++)
		{
			//if (i != 0) continue; // test

			for(t=0,xx=0;t<step;xx++,t++)
			{
				tt=xx/step;

				Q_x = BTY_GetX(mbyt_xy_count, mbyt_xy_x, i, tt);
				Q_y = BTY_GetY(mbyt_xy_count, mbyt_xy_y, i, tt);
				if (minx[0] == 0)
				{
					minx[0] = (int)Q_x;
				}
				if (!(Q_x>=minx[0]))
				{
					break;
				}
				//ASSERT((int)Q_x>=minx[0]);
				maxx[0] = (int)Q_x;
				Y[0][(int)(Q_x - minx[0])] += Q_y;
				Ycount[0][(int)(Q_x - minx[0])]++;
				//pDc->SetPixel(((Q_x)),((Q_y)),0);

				Q_x2 = BTY_GetX(mbyt_xy_count, mbyt_xy_2_x, i, tt);
				Q_y2 = BTY_GetY(mbyt_xy_count, mbyt_xy_2_y, i, tt);
				if (minx[1] == 0)
				{
					minx[1] = (int)Q_x2;
				}
				if (!(Q_x2>=minx[1]))
				{
					break;
				}
				//ASSERT((int)Q_x2>=minx[1]);
				maxx[1] = (int)Q_x2;
				Y[1][(int)(Q_x2 - minx[1])] += Q_y2;
				Ycount[1][(int)(Q_x2 - minx[1])]++;
				//pDc->SetPixel(((Q_x2)),((Q_y2)),0);
			}
		}

		int xcount = 0;
		xcount = maxx[0] - minx[0];
		int x;
		for(int c = 0; c < xcount; c++)
		{
			x = c + minx[0];

			// 绘制指定区域x0_draw~x1_draw
			if (x >= x0_draw && x <= x1_draw)
			{
			}
			else
			{
				continue;
			}

			float y[] = new float[2];
			for(i = 0; i < 2; i++)
			{
				y[i] = Y[i][c] / Ycount[i][c];

				int yint = (int)y[i];
				float yright = y[i] - yint;

				int colorint = (int)((1.0-yright) * 255);
				colorint = colorint + (colorint << 8) + (colorint << 16);
				int colorright = (int)((yright) * 255);
				colorright = colorright + (colorright << 8) + (colorright << 16);

				colorint |= 0xFF000000;
				colorright |= 0xFF000000;

				if (i == 0)
				{
					//pDc->SetPixel(x + xOffset,y[i]+1 + yOffset,colorint);
					p0.setColor(colorint);
					piccanvasPeer.drawPoint(x + xOffset,y[i]+1 + yOffset, p0);//画一个点
				}
				else if (i == 1)
				{
					//pDc->SetPixel(x + xOffset,y[i] + yOffset,colorright);
					p0.setColor(colorright);
					piccanvasPeer.drawPoint(x + xOffset,y[i] + yOffset, p0);//画一个点
				}
			}

			p0.setColor(colordark);
			for(int yt = (int)(y[1]+1); yt <= (int)y[0]; yt++)
			{
				//pDc->SetPixel(x + xOffset,yt + yOffset,0);
				p0.setColor(colordark);
				piccanvasPeer.drawPoint(x + xOffset,yt + yOffset, p0);//画一个点
			}
		}

		return 1;
	}

	// 画一个B样条
	public int AppLayerDrawBCurve(Canvas mCanvas, int iPageId,
								  int x0_draw, int x1_draw, int xOffset, int yOffset,
								  int x0, int x1, int x2, int x3,
								  int y0, int y1, int y2, int y3,
								  int width, char cSelect)
	{
		int x[] = new int [4];
		int y[] = new int [4];
		x[0] = x0;
		x[1] = x1;
		x[2] = x2;
		x[3] = x3;
		y[0] = y0;
		y[1] = y1;
		y[2] = y2;
		y[3] = y3;

		int step = (x[3] - x[1]) * 3;	// floor((double)(2000/mbyt_xy.count)
		if (step > 10000)
		{
			return 0;
		}

		BYTRender(4, x, y, mCanvas, mPaint, step, x0_draw, x1_draw, xOffset, yOffset);

		return 1;
	}
//
///////////////////////////////////////////////


	////////////////////////////////////////////////////////////////////////////
	public byte[] Bitmap2Bytes(Bitmap bm) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(CompressFormat.PNG, 100, baos);
		return baos.toByteArray();
	}

	public Bitmap Bytes2Bimap(byte[] b) {
		if (b.length != 0) {
			return BitmapFactory.decodeByteArray(b, 0, b.length);
		} else {
			return null;
		}
	}

	// Bitmap缩放
	public static Bitmap zoomBitmap(Bitmap bitmap, int width, int height) {
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();
		Matrix matrix = new Matrix();
		float scaleWidth = ((float) width / w);
		float scaleHeight = ((float) height / h);
		matrix.postScale(scaleWidth, scaleHeight);
		Bitmap newbmp = Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);
		return newbmp;
	}

	// 将Drawable转化为Bitmap
	public static Bitmap drawableToBitmap(Drawable drawable) {
		// 取 drawable 的长宽
		int w = drawable.getIntrinsicWidth();
		int h = drawable.getIntrinsicHeight();

		// 取 drawable 的颜色格式
		Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Config.ARGB_8888
				: Config.RGB_565;
		// 建立对应 bitmap
		Bitmap bitmap = Bitmap.createBitmap(w, h, config);
		// 建立对应 bitmap 的画布
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, w, h);
		// 把 drawable 内容画到画布中
		drawable.draw(canvas);
		return bitmap;
	}

	// 获得圆角图片
	public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, float roundPx, Config cfg) {
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();
		Bitmap output = Bitmap.createBitmap(w, h, cfg); //Config.ARGB_8888);
		Canvas canvas = new Canvas(output);
		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, w, h);
		final RectF rectF = new RectF(rect);
		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);

		return output;
	}

	// 获得带倒影的图片
	public static Bitmap createReflectionImageWithOrigin(Bitmap bitmap, Config cfg) {
		final int reflectionGap = 4;
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();

		Matrix matrix = new Matrix();
		matrix.preScale(1, -1);

		Bitmap reflectionImage = Bitmap.createBitmap(bitmap, 0, h / 2, w,
				h / 2, matrix, false);

		Bitmap bitmapWithReflection = Bitmap.createBitmap(w, (h + h / 2), cfg); //Config.ARGB_8888);

		Canvas canvas = new Canvas(bitmapWithReflection);
		canvas.drawBitmap(bitmap, 0, 0, null);
		Paint deafalutPaint = new Paint();
		canvas.drawRect(0, h, w, h + reflectionGap, deafalutPaint);

		canvas.drawBitmap(reflectionImage, 0, h + reflectionGap, null);

		Paint paint = new Paint();
		LinearGradient shader = new LinearGradient(0, bitmap.getHeight(), 0,
				bitmapWithReflection.getHeight() + reflectionGap, 0x70ffffff,
				0x00ffffff, TileMode.CLAMP);
		paint.setShader(shader);
		// Set the Transfer mode to be porter duff and destination in
		paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
		// Draw a rectangle using the paint with our linear gradient
		canvas.drawRect(0, h, w, bitmapWithReflection.getHeight()
				+ reflectionGap, paint);

		return bitmapWithReflection;
	}

	// Bitmap → String
	/**
	 * 图片转成string
	 *
	 * @param bitmap
	 * @return
	 */
	public static String convertIconToString(Bitmap bitmap) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();// outputstream
		bitmap.compress(CompressFormat.PNG, 100, baos);
		byte[] appicon = baos.toByteArray();// 转为byte数组
		return Base64.encodeToString(appicon, Base64.DEFAULT);
	}

	// String → Bitmap
	/**
	 * string转成bitmap
	 *
	 * @param st
	 */
	public static Bitmap convertStringToIcon(String st) {
		// OutputStream out;
		Bitmap bitmap = null;
		try {
			// out = new FileOutputStream("/sdcard/aa.jpg");
			byte[] bitmapArray;
			bitmapArray = Base64.decode(st, Base64.DEFAULT);
			bitmap = BitmapFactory.decodeByteArray(bitmapArray, 0,
					bitmapArray.length);
			// bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
			return bitmap;
		} catch (Exception e) {
			return null;
		}
	}

	// Bitmap转换成Drawable
	// Bitmap bm=xxx; //xxx根据你的情况获取
	// BitmapDrawable bd= new BitmapDrawable(getResource(), bm);
	// 因为BtimapDrawable是Drawable的子类，最终直接使用bd对象即可。

	// 暂时未用
	// Drawable缩放
	public static Drawable zoomDrawable(Drawable drawable, int w, int h) {
		int width = drawable.getIntrinsicWidth();
		int height = drawable.getIntrinsicHeight();
		// drawable转换成bitmap
		Bitmap oldbmp = drawableToBitmap(drawable);
		// 创建操作图片用的Matrix对象
		Matrix matrix = new Matrix();
		// 计算缩放比例
		float sx = ((float) w / width);
		float sy = ((float) h / height);
		// 设置缩放比例
		matrix.postScale(sx, sy);
		// 建立新的bitmap，其内容是对原bitmap的缩放后的图
		Bitmap newbmp = Bitmap.createBitmap(oldbmp, 0, 0, width, height,
				matrix, true);

		Drawable dr = new BitmapDrawable(newbmp);

		newbmp.recycle();
		oldbmp.recycle();

		return dr;
	}

	//保存到本地
	public void SaveBitmap(String PathFileName, Bitmap bmp, String ImgFileSurfix) {
//		Bitmap bitmap = Bitmap.createBitmap(800, 600, Config.ARGB_8888);
//		Canvas canvas = new Canvas(bitmap);
//		// 加载背景图片
//		Bitmap bmps = BitmapFactory.decodeResource(getResources(),
//				R.drawable.playerbackground);
//		canvas.drawBitmap(bmps, 0, 0, null);
//		// 加载要保存的画面
//		canvas.drawBitmap(bmp, 10, 100, null);
//		// 保存全部图层
//		canvas.save(Canvas.ALL_SAVE_FLAG);
//		canvas.restore();
		// 存储路径
//		File file = new File("/sdcard/song/");
//		if (!file.exists())
//			file.mkdirs();
		try {
			FileOutputStream fileOutputStream = new FileOutputStream(PathFileName);
			//bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
			//bmp.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
			if (ImgFileSurfix == ".png")
			{
				bmp.compress(CompressFormat.PNG, 100, fileOutputStream);
			}
			else if (ImgFileSurfix == ".jpg")
			{
				bmp.compress(CompressFormat.JPEG, 100, fileOutputStream);
			}
			if (ImgFileSurfix == ".bmp")
			{
				//bmp.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
				Log.i("WG", "Can't save bitmap.");
			}

			fileOutputStream.close();
			//System.out.println("saveBmp is here");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}




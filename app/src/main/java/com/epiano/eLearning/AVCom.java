package com.epiano.eLearning;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioFormat;
import android.os.Handler;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;

import com.epiano.av.encode.androidencode.AvcHWDecoder;
import com.epiano.av.encode.androidencode.AvcHWEncoder;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import java.nio.FloatBuffer;
//import android.epiano.com.commutil.DecCtl;
//import android.epiano.com.commutil.EncCtl;
//import android.epiano.com.commutil.PianoELearning.DecCtl;
//import android.epiano.com.commutil.PianoELearning.USBCam;
//import android.epiano.com.commutil.PianoELearning.EncCtl.GetImgAndEncodeThread;
//import android.epiano.com.commutil.eLearning.DecCtl.BStatThread;
//import android.epiano.com.commutil.eLearning.DecCtl.COne264Frm;
//import android.epiano.com.commutil.eLearning.DecCtl.DecodeThread;
//import android.epiano.com.commutil.eLearning.DecCtl.SimNet;
//import android.epiano.com.commutil.eLearning.DecCtl.StartDispThread;
//import android.epiano.com.commutil.eLearning.DecCtl.StartUdpRcvThread;
//import android.epiano.com.commutil.PianoELearning.GLProgram;

@SuppressWarnings("JniMissingFunction")
public class AVCom
{
	private static String TAG = "Video";

	enum UDP_ITEM
	{
		UDP_ITEM_AB,			// 0, A->B的正向消息
		UDP_ITEM_BA,			// 1, A->B的反馈消息
		UDP_ITEM_AUDIO,			// 2
		UDP_ITEM_AUDIO_FEC,		// 3
		UDP_ITEM_VIDEO,			// 4
		UDP_ITEM_FILL_DATA,		// 5
	};

	public EncCtl venc;		// 视频编码器
	DecCtl vdec;		// 视频解码器
	final int mVDecPicBufLen = (int)(640 * 480 * 1.5);
	ByteBuffer mVDecPicBuf;
	//ByteBuffer mVDecPicBuf = ByteBuffer.allocateDirect(mVDecPicBufLen); // 直接访问高效内存
	byte mVDecPicBufByte[];

	AudioCap audiocap;	// 语音信号采集
	int AudioEncBitRate = 20000;
	int AudioEncCplx = 2;

	public int DstSendBw = 25000; // 当前目标发送带宽, 可被自适应变更, 会被构造函数接口初始化
	public int mMaxByteRate = 80000;
	public int mMinByteRate = 10000;	// 20000
	public int DstSendBwAudio = 5000;		// cfg,
	int CompsentSendBw = 0; // 补偿发送带宽
	int fps = 15;					// cfg

	// 动态控制参数， voiceshow
	public int FreezeAudio = 0;
	public int FreezeVideo = 0;
	public int BwSelfAdaptSwitch = 1;

	boolean IntTestSwitch = false; // cfg, 向voiceshow输出曲线

	// 视频编码器选择
	int UseHw264Enc = 1;

	String remoteip = "127.0.0.1";

	public static native int NATDetect();

	public static native int AVComInit(Object VDecPicBuf,
									   //byte VDecPicBufByte[],
									   int AudioEncBitRate,
									   int AudioEncCplx);				// jni: fec 264等编解码等
	public static native void AVComDestroy();		// jni: fec 264等编解码等

	public static native byte[] GetFecPk(int FecPkId);
	//public static native byte[] GetFecPkAudio(int FecPkId);
	public static native int IntTestOut(String paraname, String paravalue);
	public static native int IntPrint(int level, String jprintStr); // ICTVOIP_LOG_ERROR:6
	public static native int GetCtlPara(String jParaName);

	public static native byte[] GetSmallImgJni(byte[] dataIn, int w_org, int h_org, int w_dst, int h_dst, int swapUV);

	// x264 encode, return nalcount
	public static native int X264Encode(
			byte[] jImgData,
			int w,
			int h,
			int frmType
			//jint frmid
	);
	// 从JNI获取nal，一次一个
	public static native byte[] X264EncodeGetNal(int NalIdx);
	public static native int Soft264Init(
			int width, int height, int ImgFmt
	);
	public static native int Soft264setbitrate(
			int byterate
	);
	public static native int Soft264OnNewIReq();

	// fec编码
	public static native int FECEncode(
			int frmId,
			byte [] H264data,
			int H264datalen,
			int MaxPkSize_L,
			int OrgPkNum_N,
			int FecPkNum_n);
	// 视频解码
	public static native int VideoDecode(
			int frmid,
			int NulCount,
			int H264Frmdatalen,		// 264原始数据长度， 不包括fec包
			byte HTransFrmdata[],	// 原数据 + FEC数据, 可整除MaxPkSize_L
			int MaxPkSize_L,
			int OrgPkNum_N,
			int FecPkNum_n,
			int jlostPk[],
			int lostPkNum,
			int RstVDec
	);
	// 语音编码
	public static native int PutPCMData(
			int datalen,
			short jAudioData[],		// PCM
			int frmid
			//,int offset
	);
	public static native byte[] GetAudioEncData();	// 取语音编码数据
	//public static native byte[] GetAudioEncFECData();		// 取语音FEC数据
	/*
	public static native int OpusEncode(	// no use
			int datalen,
		    short jAudioData[]		// PCM
		    //,int offset
			);
			*/
	// 语音解码
	// no use
	public static native int OpusDecode(
			int Frmdatalen,			// 本包原始数据长度， 不包括fec包
			byte HTransFrmdata[],	// 原数据 or FEC数据
			int MaxPkSize_L, 		// 包最大长度. fec包长度
			int OrgPkNum_N,			// 原数据包数
			int FecPkNum_n,			// fec包数
			int jlostPk[], 			// 丢包
			int lostPkNum			// 丢包数
	);
	// 获取语音解码数据
	public static native short[] GetAudioDecData();
	//public static native int OpusDestroy();
	public static native int PutOpusData(
			int datalen,
			byte jOpusData[],		// Opus
			int uiTs_Sor,
			int uiTs_Sink
			//,int offset
	);
	public static native int PutOpusFECData(
			int datalen,
			byte jOpusFECData[],		// Opus
			int uiTs_Sor,
			int uiTs_Sink
	);

	public static native byte[] GetDecPic();
	public static native byte[] GetDecPicYVU(byte YUVBuf[]);
	public static native void SetPicBufIdle();
	public static native int GetDecPic2(Bitmap bmp);
	public static native int GetPicWidth();
	public static native int GetPicHeight();

	Context mctx;
	USBCam usbcam;
	NativeCam nativecam;
	int mbr = 0;
	int mw = 0;
	int mh = 0;
	ImageView mImag;
	ImageView mImageStat; // 统计曲线窗口
	HorizontalScrollView mStatSrollView;
	//	GLFrameRenderer GLFRendererPreview,
	GLFrameRenderer mGLFRendererPeerview;


	// 在收端进行源和收的统计
	public CStat mStat;
	enum NETSTAT
	{
		NETSTAT_SendBRSet,		// 0
		NETSTAT_SendBR,		// 0
		NETSTAT_SendByte,		// 0
		NETSTAT_SendPks,		// 0

		NETSTAT_RcvByte,		// 0
		NETSTAT_RcvLostByte,	// 0
		NETSTAT_RcvPks,			// 0
		NETSTAT_RcvLostPks,		// 0
		NETSTAT_RcvOneWayDelay,	// 0
		NETSTAT_RcvRtt,			// 0
		NETSTAT_RcvBadCrcPks,	// 0

		NETSTAT_ImgCapNum,		// 0 源端摄像头采集量
		NETSTAT_ImgRcvNum,		// 0 收端解码输出量

		NETSTAT_BUTT,			// 0
	}
	enum NETSTAT_MOD
	{
		NETSTAT_MOD_VALUE,			// 原始值，针对时延类
		NETSTAT_MOD_COUNT_LIMIT,	// 原始值、回滚, 针对计数类
		NETSTAT_MOD_VALUE_2_SPEED,	// 原始值转化为速率，针对速率类
		NETSTAT_MOD_RATIO,			// 比上一相邻的比值，针对丢包率类
	}
	enum STAT_PEN
	{
		STAT_PEN_RED0,
		STAT_PEN_RED1,
		STAT_PEN_RED2,
		STAT_PEN_GREEN0,
		STAT_PEN_GREEN1,
		STAT_PEN_GREEN2,
		STAT_PEN_BLUE0,
		STAT_PEN_BLUE1,
		STAT_PEN_BLUE2,
		STAT_PEN_GRAY0,	// black
	}
	// 策略控制
	enum POLICY_STATUS
	{
		POLICY_STATUS_NULL,
		POLICY_STATUS_PRE_T_GOOD,
		POLICY_STATUS_PRE_JT_BIG,
		POLICY_STATUS_PRE_PKLOST,
	}
	// Net model
	enum NET_MODEL
	{
		NET_MODEL_NULL,				// 空，未知
		NET_MODEL_GOOD,				// 良好网络
		NET_MODEL_BWLIMT,			// 带宽限制
		NET_MODEL_BUFFER,			// 缓冲
		NET_MODEL_RANDLOST,			// 随机丢包
		NET_MODEL_BURST_JITTER,		// 突发抖动
		NET_MODEL_BURST_LOST,		// 突发丢包

		NET_MODEL_BUTT,				//
	}
	//	public void pushval_BRSet(long DstSendBw)
//	{
//		mStat.pushval(NETSTAT.NETSTAT_SendBRSet, DstSendBw);
//	}
	public class CStat
	{
		// T: 100ms

		int MAX_STAT_TCount = 600; 			// 总周期烽, 可能被因mXStep崦被小幅度调整，整倍数
		final int MAX_STAT_ITEM = 100;		// 最大招标项数

		int hdr;
		int size;
		int size_Undrawed;
		int tail;
		long stat[][] = new long[MAX_STAT_TCount][MAX_STAT_ITEM];	// 指标记录

		//boolean switches[] = new boolean[MAX_STAT_ITEM];			// 各指标项显示开关
		int switches[] = new int[MAX_STAT_ITEM];					// 各指标项显示开关, 1，计算加曲线， 2，仅计算
		double ks[] = new double[MAX_STAT_ITEM];					// 各指标显示比例
		NETSTAT_MOD StatMod[] = new NETSTAT_MOD[MAX_STAT_ITEM];
		Paint StatPaint[] = new Paint[MAX_STAT_ITEM];
		Paint StatTextPaint = null;
		Paint mPaintBG = new Paint();
		long v0[][] = new long[MAX_STAT_TCount][MAX_STAT_ITEM];
		//long v1[][] = new long[MAX_STAT_TCount][MAX_STAT_ITEM];
		//long v2[][] = new long[MAX_STAT_TCount][MAX_STAT_ITEM];

		int T;
		long curtick;
		int mXStep = 1; 	// x轴统计步长
		int mStatTNUM = 0;

		DecCtl decctl;

		public CStat(DecCtl decctlIn, long tick, int TIn, int widthInDP, int heightInDP, int XStep)
		{
			T = TIn;
			curtick = tick;

			decctl = decctlIn;

			//ImgWidth  = widthInDP; //Dp2Px(mctx, widthInDP); // 5000; // 200; //1000;
			ImgWidth  = Dp2Px(mctx, widthInDP); // 5000; // 200; //1000;
			ImgHeight = Dp2Px(mctx, heightInDP); //75);
			mXStep = XStep;
			mStatTNUM = MAX_STAT_TCount / XStep;
			MAX_STAT_TCount = mStatTNUM * mXStep;

			CreateBmp();

			// 统计项配置
			SetStatItem();

			reset();
		}

		void reset()
		{
			CurX = 0;
			lastDrawedIdx = 0;
			lastCalcedIdx = 0;
			size_Undrawed = 0;
			size = 0;
			hdr = 0;
			tail = 0;

			for(int i = 0; i < MAX_STAT_TCount; i++)
			{
				for(int j = 0; j < MAX_STAT_ITEM; j++)
				{
					v0[i][j] = 0;
				}
			}

			for(int i = 0; i < MAX_STAT_ITEM; i++)
			{
				//switches[i] = true; // 显示开关
				//ks[i] = 1;

				clearslice(i);
			}

			CreateBmp();
		}

		void drawrewind()
		{
			CurX = 0;
			CreateBmp();
		}

		void SetStatItem()
		{
//			NETSTAT_SendByte,		// 0
//			NETSTAT_SendPks,		// 0
//
//			NETSTAT_RcvByte,		// 0
//			NETSTAT_RcvPks,			// 0
//			NETSTAT_RcvLostByte,	// 0
//			NETSTAT_RcvLostPks,		// 0
//			NETSTAT_RcvOneWayDelay,	// 0
//			NETSTAT_RcvRtt,			// 0
//			NETSTAT_RcvBadCrcPks,	// 0

			switches[NETSTAT.NETSTAT_SendBRSet.ordinal()] 		= 1; // 各指标项显示开关, 1，计算加曲线， 2，仅计算
			switches[NETSTAT.NETSTAT_SendBR.ordinal()] 			=  0;
			switches[NETSTAT.NETSTAT_SendByte.ordinal()] 		= 1;
			switches[NETSTAT.NETSTAT_SendPks.ordinal()] 		=  0;
			switches[NETSTAT.NETSTAT_RcvByte.ordinal()] 		= 1;
			switches[NETSTAT.NETSTAT_RcvLostByte.ordinal()] 	=  0;
			switches[NETSTAT.NETSTAT_RcvPks.ordinal()] 			= 1;
			switches[NETSTAT.NETSTAT_RcvLostPks.ordinal()] 		= 1;
			switches[NETSTAT.NETSTAT_RcvOneWayDelay.ordinal()] 	= 1;
			switches[NETSTAT.NETSTAT_RcvRtt.ordinal()] 			= 2;
			switches[NETSTAT.NETSTAT_RcvBadCrcPks.ordinal()] 	=  0;
			switches[NETSTAT.NETSTAT_ImgCapNum.ordinal()] 		=  0;
			switches[NETSTAT.NETSTAT_ImgRcvNum.ordinal()] 		=  2;
/**/
			StatMod[NETSTAT.NETSTAT_SendBRSet.ordinal()] 		= NETSTAT_MOD.NETSTAT_MOD_VALUE;
			StatMod[NETSTAT.NETSTAT_SendBR.ordinal()] 			= NETSTAT_MOD.NETSTAT_MOD_VALUE;
			StatMod[NETSTAT.NETSTAT_SendByte.ordinal()] 		= NETSTAT_MOD.NETSTAT_MOD_VALUE_2_SPEED;
			StatMod[NETSTAT.NETSTAT_SendPks.ordinal()] 			= NETSTAT_MOD.NETSTAT_MOD_VALUE_2_SPEED;
			StatMod[NETSTAT.NETSTAT_RcvByte.ordinal()] 			= NETSTAT_MOD.NETSTAT_MOD_VALUE_2_SPEED;
			StatMod[NETSTAT.NETSTAT_RcvLostByte.ordinal()] 		= NETSTAT_MOD.NETSTAT_MOD_COUNT_LIMIT;
			StatMod[NETSTAT.NETSTAT_RcvPks.ordinal()] 			= NETSTAT_MOD.NETSTAT_MOD_VALUE_2_SPEED;
			StatMod[NETSTAT.NETSTAT_RcvLostPks.ordinal()] 		= NETSTAT_MOD.NETSTAT_MOD_VALUE_2_SPEED; //NETSTAT_MOD_RATIO;
			StatMod[NETSTAT.NETSTAT_RcvOneWayDelay.ordinal()] 	= NETSTAT_MOD.NETSTAT_MOD_VALUE;
			StatMod[NETSTAT.NETSTAT_RcvRtt.ordinal()] 			= NETSTAT_MOD.NETSTAT_MOD_VALUE;
			StatMod[NETSTAT.NETSTAT_RcvBadCrcPks.ordinal()] 	= NETSTAT_MOD.NETSTAT_MOD_COUNT_LIMIT;
			StatMod[NETSTAT.NETSTAT_ImgCapNum.ordinal()] 		= NETSTAT_MOD.NETSTAT_MOD_VALUE_2_SPEED;
			StatMod[NETSTAT.NETSTAT_ImgRcvNum.ordinal()] 		= NETSTAT_MOD.NETSTAT_MOD_VALUE_2_SPEED;

			ks[NETSTAT.NETSTAT_SendBRSet.ordinal()] 			= 0.0009;
			ks[NETSTAT.NETSTAT_SendBR.ordinal()] 				= 0.0009;
			ks[NETSTAT.NETSTAT_SendByte.ordinal()] 				= 0.0009;
			ks[NETSTAT.NETSTAT_SendPks.ordinal()] 				= 0.15;
			ks[NETSTAT.NETSTAT_RcvByte.ordinal()] 				= 0.0009;
			ks[NETSTAT.NETSTAT_RcvLostByte.ordinal()] 			= 1;
			ks[NETSTAT.NETSTAT_RcvPks.ordinal()] 				= 0.11;
			ks[NETSTAT.NETSTAT_RcvLostPks.ordinal()] 			= 0.5;
			ks[NETSTAT.NETSTAT_RcvOneWayDelay.ordinal()] 		= 0.05;
			ks[NETSTAT.NETSTAT_RcvRtt.ordinal()] 				= 0.05;
			ks[NETSTAT.NETSTAT_RcvBadCrcPks.ordinal()] 			= 1;
			ks[NETSTAT.NETSTAT_ImgCapNum.ordinal()] 			= 1;
			ks[NETSTAT.NETSTAT_ImgRcvNum.ordinal()] 			= 1;

			StatPaint[NETSTAT.NETSTAT_SendBRSet.ordinal()] 		= mPaint[STAT_PEN.STAT_PEN_GREEN0.ordinal()];
			StatPaint[NETSTAT.NETSTAT_SendBR.ordinal()] 		= mPaint[STAT_PEN.STAT_PEN_GREEN1.ordinal()];
			StatPaint[NETSTAT.NETSTAT_SendByte.ordinal()] 		= mPaint[STAT_PEN.STAT_PEN_GREEN0.ordinal()];
			StatPaint[NETSTAT.NETSTAT_SendPks.ordinal()] 		= mPaint[STAT_PEN.STAT_PEN_BLUE2.ordinal()];
			StatPaint[NETSTAT.NETSTAT_RcvByte.ordinal()] 		= mPaint[STAT_PEN.STAT_PEN_GREEN1.ordinal()];
			StatPaint[NETSTAT.NETSTAT_RcvLostByte.ordinal()] 	= mPaint[STAT_PEN.STAT_PEN_RED1.ordinal()];
			StatPaint[NETSTAT.NETSTAT_RcvPks.ordinal()] 		= mPaint[STAT_PEN.STAT_PEN_BLUE0.ordinal()];
			StatPaint[NETSTAT.NETSTAT_RcvLostPks.ordinal()] 	= mPaint[STAT_PEN.STAT_PEN_RED0.ordinal()];
			StatPaint[NETSTAT.NETSTAT_RcvOneWayDelay.ordinal()] = mPaint[STAT_PEN.STAT_PEN_RED1.ordinal()];
			StatPaint[NETSTAT.NETSTAT_RcvRtt.ordinal()] 		= null;
			StatPaint[NETSTAT.NETSTAT_RcvBadCrcPks.ordinal()] 	= mPaint[STAT_PEN.STAT_PEN_RED2.ordinal()];
			StatPaint[NETSTAT.NETSTAT_ImgCapNum.ordinal()]		 = null;
			StatPaint[NETSTAT.NETSTAT_ImgRcvNum.ordinal()] 		= null;
		}

		//
		void clearslice(int idx)
		{
			for(int i = 0; i < NETSTAT.NETSTAT_BUTT.ordinal(); i++)
			{
				stat[idx][i] = 0;
			}
		}

		void dida()
		{
			if (size < MAX_STAT_TCount)
			{
				size++;
			}
			else
			{
				hdr++;
				hdr %= MAX_STAT_TCount;
			}

			size_Undrawed++;

			tail = (hdr + size) % MAX_STAT_TCount;

			clearslice(tail);
		}

		void pushval(NETSTAT item, long val)
		{
			stat[tail][item.ordinal()] = val;
		}

		public Bitmap mBitmapPeer;
		Canvas piccanvasPeer;
		Bitmap.Config BmpConfig = Bitmap.Config.RGB_565;// ARGB_8888; // RGB_565;// ARGB_8888; //
		float ImgAndViewK = 1;
		int ImgWidth = 5000; // 200; //1000;
		int ImgHeight = Dp2Px(mctx, 75);
		final int MAX_PAINT = 20;
		Paint mPaint[] = new Paint[MAX_PAINT];
		final int MAX_LIMIT_VALUE = 30;

		public int BackgoundR = 0xff;
		public int BackgoundG = 0xfa;
		public int BackgoundB = 0xe9;

		public int Dp2Px(Context context, float dp) {
			final float scale = context.getResources().getDisplayMetrics().density;
			return (int) (dp * scale + 0.5f);
		}

		public int Px2Dp(Context context, float px) {
			final float scale = context.getResources().getDisplayMetrics().density;
			return (int) (px / scale + 0.5f);
		}

		void paintCreate()
		{
			//paint color init

			if (mPaint[0] != null)
			{
				return;
			}

			int PenColor = 0;
			int i = 0;

			for(i = 0; i < MAX_PAINT; i++)
			{
				mPaint[i] = new Paint();
				//mPaint[i].setStrokeWidth(1);
			}

			// R,G,B
			i = 0;
			PenColor = 0xFF << 16;
			mPaint[i++].setColor(PenColor | 0xFF000000);
			PenColor = 0xCC << 16;
			mPaint[i++].setColor(PenColor | 0xFF000000);
			PenColor = 0xAA << 16;
			mPaint[i++].setColor(PenColor | 0xFF000000);
			PenColor = 0xFF << 8;
			mPaint[i++].setColor(PenColor | 0xFF000000);
			PenColor = 0xCC << 8;
			mPaint[i++].setColor(PenColor | 0xFF000000);
			PenColor = 0xAA << 8;
			mPaint[i++].setColor(PenColor | 0xFF000000);
			PenColor = 0xFF << 0;
			mPaint[i++].setColor(PenColor | 0xFF000000);
			PenColor = 0xCC << 0;
			mPaint[i++].setColor(PenColor | 0xFF000000);
			PenColor = 0xAA << 0;
			mPaint[i++].setColor(PenColor | 0xFF000000);
			PenColor = 0x0;
			mPaint[i++].setColor(PenColor | 0xFF000000);

			// i = 7
			for( ; i < MAX_PAINT; i++)
			{
				PenColor = 0x427FCD + 0x5020 * i;
				mPaint[i].setColor(PenColor | 0xFF000000);
			}

			StatTextPaint = new Paint();
			PenColor = 0x333333;
			StatTextPaint.setColor(PenColor | 0xFF000000);
			StatTextPaint.setTextSize(12); // 15);
		}

		public int CreateBmp()
		{
			if (mBitmapPeer == null)
			{
				mBitmapPeer = Bitmap.createBitmap(ImgWidth, ImgHeight, BmpConfig);
				piccanvasPeer = new Canvas(mBitmapPeer);  //绘制收包图
			}

			if (mBitmapPeer != null)
			{
				// 刷背景
				mPaintBG.setColor(Color.rgb(BackgoundR, BackgoundG, BackgoundB));// 设置灰色
				mPaintBG.setStyle(Paint.Style.FILL);//设置填满
				piccanvasPeer.drawRect(0, 0, ImgWidth, ImgHeight, mPaintBG);// 正方形
			}

			paintCreate();

			return 1;
		}

		void DrawNetLine(Canvas pDC, int h, boolean drawflag, Paint pen, double yunit, long x0, long x1, long y0, long y1)
		{
			if (pDC == null)
			{
				return;
			}

			if (drawflag)
			{
				//pDC->SelectObject(pen);

				y0 *= yunit;
				y1 *= yunit;

				//pDC->MoveTo(x0, h - y0);
				//pDC->LineTo(x1, h - y1);

				pDC.drawLine(x0, h - y0, x1, h - y1, pen);

				// test
				//pDC.drawLine(0, 0, ImgWidth, 75, pen);
				//pDC.drawLine(0, 75, ImgWidth, 0, pen);
				//pDC.drawLine(x0, h - y0, ImgWidth, 75, pen);
				//pDC.drawLine(x1, h - y1, ImgWidth, 0, pen);
				//pDC.drawCircle(x0,  h - y0, 10, pen);
				//pDC.drawCircle(x1,  h - y1, 10, pen);
				//Paint p = new Paint();
				//p.setColor(Color.rgb(255, 0, 0)); // R
				//pDC.drawLine(x0, h - y0 - 5, x1, h - y1 - 5, p);
			}
		}

		int lastDrawedIdx = 0;
		int CurX = 0;
		float XScale = 4; // 2; cfg
		void Draw()
		{
			//mXStep = 1; // test

			//int id1 = 0;
			//if (size <= mXStep * 2 + 2)
			if (size_Undrawed <= mXStep * 2 + 1)
			{
				return;
			}

			//id1 = (hdr + size - mXStep * 2 - 1) % MAX_STAT_TCount;

			int x0 = 0;
			int x1;
			//long v0 = 0;
			//long v1 = 0;
			//long v2 = 0;

			int TT = T * mXStep;
			int Loop = size_Undrawed / mXStep - 1;
			if (size_Undrawed % mXStep == 0) // 最新一组可能为还没有统计值，不画最后一组
			{
				Loop--;
			}

			//for(int i = lastDrawedIdx; i != id1; i = (i + 1) % MAX_STAT_TCount)
			int i = lastDrawedIdx;

			//Log.e(TAG, "Draw: lastDrawedIdx:" + lastDrawedIdx + "----------");

			Paint pen = null;
			for(int c = 0; c < Loop; c++)
			{
//				if (i % mXStep != 0)
//				{
//					continue;
//				}

				//lastDrawedIdx = (lastDrawedIdx + mXStep) % MAX_STAT_TCount;

				x0 = (int)(CurX * XScale);
				x1 = (int)((CurX + 1) * XScale);
				for(int j = 0; j < NETSTAT.NETSTAT_BUTT.ordinal(); j++)
				{
					if (switches[j] == 0)
					{
						continue;
					}

					int preid = (i + MAX_STAT_TCount - mXStep) % MAX_STAT_TCount;

					if (StatPaint[j] != null)
					{
						pen = StatPaint[j];
					}
					else
					{
						pen = mPaint[j % MAX_PAINT];
					}

					//pen = mPaint[STAT_PEN.STAT_PEN_GRAY0.ordinal()];

					DrawNetLine(piccanvasPeer, ImgHeight, switches[j] == 1,
							pen, ks[j], x0, x1, (long)v0[preid][j], (long)v0[i][j]);

					//Log.e(TAG, "Draw: lastDrawedIdx:" + lastDrawedIdx +
					//	", p:" + pen +
					//	", x0:" + x0 +
					//	", x1:" + x1 +
					//	", v0:" + v0 +
					//	", v1:" + v1);
				}

				CurX++;
				i = (i + mXStep) % MAX_STAT_TCount;
				size_Undrawed -= mXStep;
			}
			lastDrawedIdx = i;

			// 文字化输出指标
//			piccanvasPeer.drawRect(5, 1, 350, 15, mPaintBG); // 清空长方形区域
			int lastid = (lastCalcedIdx + MAX_STAT_TCount - mXStep) % MAX_STAT_TCount;
//			String pstr = "" + 	(v0[lastid][NETSTAT.NETSTAT_SendBRSet.ordinal()]/1000)
//					+ ":" + 	(v0[lastid][NETSTAT.NETSTAT_SendByte.ordinal()]/1000)
//					+ ":" + 	(v0[lastid][NETSTAT.NETSTAT_RcvByte.ordinal()]/1000) + "kB"
//					//+ "," + 	(v0[lastid][NETSTAT.NETSTAT_SendBR.ordinal()]/1000) + "kB"
//					+ "," + 	(v0[lastid][NETSTAT.NETSTAT_RcvPks.ordinal()]) + "P/s"
//					+ ",:" + 	(v0[lastid][NETSTAT.NETSTAT_RcvLostPks.ordinal()]) + "%"
//					+ ",Jt:" + 	(v0[lastid][NETSTAT.NETSTAT_RcvOneWayDelay.ordinal()])
//					+ ",Rtt:" + (v0[lastid][NETSTAT.NETSTAT_RcvRtt.ordinal()]) + "ms"
//					+ ",:" + 	(v0[lastid][NETSTAT.NETSTAT_ImgCapNum.ordinal()])
//					+ ",:" + 	(v0[lastid][NETSTAT.NETSTAT_ImgRcvNum.ordinal()]) + "fps"
//					;
//			piccanvasPeer.drawText(pstr, 5, 10, StatTextPaint);
			piccanvasPeer.drawRect(5, 1, 350, 25, mPaintBG); // 清空长方形区域
			{
				String pstr = "" + 	(v0[lastid][NETSTAT.NETSTAT_SendBRSet.ordinal()]/1000)
						+ ":" + 	(v0[lastid][NETSTAT.NETSTAT_SendByte.ordinal()]/1000)
						+ ":" + 	(v0[lastid][NETSTAT.NETSTAT_RcvByte.ordinal()]/1000) + "kB"
						+ "," + 	(v0[lastid][NETSTAT.NETSTAT_RcvPks.ordinal()]) + "P/s"
						+ "," + 	(v0[lastid][NETSTAT.NETSTAT_RcvLostPks.ordinal()]) + "%"
						;
				piccanvasPeer.drawText(pstr, 5, 10, StatTextPaint);
			}
			{
				String pstr = ""
						+ "Jt:" + 	(v0[lastid][NETSTAT.NETSTAT_RcvOneWayDelay.ordinal()])
						+ ",Rtt:" + (v0[lastid][NETSTAT.NETSTAT_RcvRtt.ordinal()]) + "ms"
						+ ",:" + 	(v0[lastid][NETSTAT.NETSTAT_ImgCapNum.ordinal()])
						+ ",:" + 	(v0[lastid][NETSTAT.NETSTAT_ImgRcvNum.ordinal()]) + "fps"
						;
				piccanvasPeer.drawText(pstr, 5, 20, StatTextPaint);
			}


			if (x0 > ImgWidth * 0.99)
			{
				drawrewind();
			}

			//lastDrawedIdx = (hdr + size - 1) % MAX_STAT_TCount;
		}

		// 计算最新接收带宽, Bps
		// input: step, 周期数，超大, 带宽越平稳
		int GetRcvBw(int step)
		{
			int bw = 0;
			if (size <= step + 1)
			{
				return 0;
			}

			int id = NETSTAT.NETSTAT_RcvByte.ordinal();

			int t1 = tail + (MAX_STAT_TCount - 1);
			t1 %= MAX_STAT_TCount;
			int t0 = t1 + (MAX_STAT_TCount - step);
			t0 %= MAX_STAT_TCount;

			bw = (int)((stat[t1][id] - stat[t0][id]) * 1000 / (step * T));

			return bw;
		}

		// 计算v0[][] ...
		int lastCalcedIdx = 0; // 最新一个有效的计算周期，对应用的v0[][]有效，初始情况除外
		//int TCount = 0; // 有效周期数
		void Calc()
		{
			//mXStep = 1; // test

			int id1 = 0;
			//if (size <= mXStep * 2 + 2)
			//if (size_Undrawed <= mXStep * 2 + 1)
			//{
			//	return;
			//}

			if (size <= 0)
			{
				return;
			}

			//int s = size - (size % mXStep);
			int s = size - 1; // 最新一个项可能还没有统计完成，暂不用于计算

			id1 = (hdr + s) % MAX_STAT_TCount;

			//int x0 = 0;
			//int x1;
			//long v0 = 0;
			//long v1 = 0;
			//long v2 = 0;

			int TT = T * mXStep;

			int i = lastCalcedIdx;
			//for(; i != id1; i = (i + mXStep) % MAX_STAT_TCount)
			for(; i != id1; i = (i + 1) % MAX_STAT_TCount)
			{
				//x0 = (int)(CurX * XScale);
				//x1 = (int)((CurX + 1) * XScale);
				for(int j = 0; j < NETSTAT.NETSTAT_BUTT.ordinal(); j++)
				{
					if (switches[j] == 0)
					{
						continue;
					}

					if (StatMod[j] == NETSTAT_MOD.NETSTAT_MOD_VALUE_2_SPEED)
					{
						v0[i][j] = (stat[i][j] - stat[(i + MAX_STAT_TCount - mXStep) % MAX_STAT_TCount][j]) * 1000 / TT;
					}
					else if (StatMod[j] == NETSTAT_MOD.NETSTAT_MOD_COUNT_LIMIT)
					{
						v0[i][j] = stat[i][j] % MAX_LIMIT_VALUE;
					}
					else if (StatMod[j] == NETSTAT_MOD.NETSTAT_MOD_VALUE)
					{
						v0[i][j] = stat[i][j];
					}
					else if (StatMod[j] == NETSTAT_MOD.NETSTAT_MOD_RATIO)
					{
						if (stat[i][j-1] != 0 && stat[i][j] <= stat[i][j-1])
						{
							v0[i][j] = (stat[i][j] * 100) / stat[i][j-1];
						}
					}
					else
					{
						continue;
					}
				}

				// 丢包率
				if (v0[i][NETSTAT.NETSTAT_RcvPks.ordinal()] != 0)
				{
					v0[i][NETSTAT.NETSTAT_RcvLostPks.ordinal()]
							= Math.min(v0[i][NETSTAT.NETSTAT_RcvLostPks.ordinal()] * 100
							/ v0[i][NETSTAT.NETSTAT_RcvPks.ordinal()], 100);
				}
				/*
				if (v1[NETSTAT.NETSTAT_RcvPks.ordinal()] != 0)
				{
					v1[NETSTAT.NETSTAT_RcvLostPks.ordinal()]
						= v1[NETSTAT.NETSTAT_RcvLostPks.ordinal()] * 100
							/ v1[NETSTAT.NETSTAT_RcvPks.ordinal()];
				}
				*/

				//i = (i + mXStep) % MAX_STAT_TCount;
			}
			lastCalcedIdx = i;
		}

		/////////////////////////////////////////////////

		// 策略控制
		/* 三个周期关系，100ms基本参数测量Ｔ，参数统计Ｔ，策略Ｔ
		 T:100ms
		 mXStep:3
		 100 ms T: 0 1 2 3 4 5 6 7 8 9 0 1 2 ...
		 stat   T: 1 2 3 4 5 6 7 8 9 0 1 2 ...
		 stat   T: 0     1     3     4     5 ...
		 policy T:       0     1           2 ...
		 */
//		enum POLICY_STATUS
//		{
//			POLICY_STATUS_NULL,
//			POLICY_STATUS_PRE_T_GOOD,
//			POLICY_STATUS_PRE_JT_BIG,
//			POLICY_STATUS_PRE_PKLOST,
//		}
		POLICY_STATUS PStatusJT   = POLICY_STATUS.POLICY_STATUS_NULL;
		POLICY_STATUS PStatusLOST = POLICY_STATUS.POLICY_STATUS_NULL;
		//long PolicyRcvBR[];
		//long PolicyRcvJT[];
		//long PolicyRcvLOSTR[];
		int PolicyTLong = 1000 + 100; 		// 带宽控制周期, ms，如果遇到带宽下降，可提升提升周期 cfg
		int BWLimitUp   = 50000;		// 自动带宽控制上限，　cfg
		int BWLimitDown = 2000; // 10000;		// 自动带宽控制下限，　cfg
		int JTValThreshhold = 100;		// jt幅度异常门限, ms，cfg
		int JTLongThreshhold = 200;		// jt持续时长异常门限, ms，cfg
		int LostValThreshhold = 10;		// 丢包幅度异常门限, 百分之x，cfg
		int LostLongThreshhold = 200;	// 丢包持续时长异常门限, ms，cfg
		float BWGrownSpeed = 0.1f;  	// 带宽增长速率，在上一周期上增长的系数　cfg
		float BWDecreaseSpeed = 0.5f;  	// 带宽下降速率，在上一周期上下降的系数　cfg
		final int MaxPolicyTCunt = 20;	// cfg

		// dynamic params
		long JTAbnormalStart = 0;
		long LostAbnormalStart = 0;
		int PolicyTIdx[] = new int[MaxPolicyTCunt]; // 策略控制周期，索引对应v0[]
		int PolicyHdr = 0;
		int PolicySize = 0;
		long SmoothBRInT = 0;	// 周期内经平滑处理的带宽，用于下一同期的带宽估计
		long SmoothLostInT = 0;
		int LostParamNoChangeTimes = 0; // 为防止丢包状态下长时间未进行参数调整，主动调整

		//NET_MODEL netmodel[] = new NET_MODEL[NET_MODEL.NET_MODEL_BUTT.ordinal()];
		long netmodel[] = new long[NET_MODEL.NET_MODEL_BUTT.ordinal()];

		// 计算两个值的差异比
		float getk(float a, float b)
		{
			float fz = Math.abs(a - b);
			float fm = (float)(b + 0.001);
			float k = fz / fm;

			return k;
		}

		// important func: 网络策略控制
		public int GenPolicy(long tick)
		{
			/*
			如果无jt和丢包问题，取上一周期控制发端带宽乘以放大系数(e.g.110%)作为下一周期的源端发送带宽
			如果有jt异常，看持续时长，长于比如500ms,　则根据实际带宽快速下调(e.g.50%)发送带宽
			如果上一周期已经有异常，统计实际带宽，根据实际带宽快速下降发送带宽
			如果有丢包异常，先按实际接收带宽下调发送带宽，
			  若后续仍然有丢包，则根据带宽与丢包率的相关性判断是带宽受限型(带宽与丢包相关性大)还是随机丢包
			*/

			// TCount
			// lastCalcedIdx;

			// if T is ending
			int tail = (PolicyHdr + PolicySize) % MaxPolicyTCunt;
			int lastPolicyId = PolicyTIdx[tail];
			int statTpast = 0;
			// 计算本周期时长
			//int lastvalidid = (lastCalcedIdx + MAX_STAT_TCount - mXStep) % MAX_STAT_TCount;
			int lastvalidid = (lastCalcedIdx + MAX_STAT_TCount - 1) % MAX_STAT_TCount;
			if (lastCalcedIdx >= lastPolicyId)
			{
				statTpast = lastCalcedIdx - lastPolicyId;
			}
			else
			{
				// 回滚情况
				statTpast = lastCalcedIdx + MAX_STAT_TCount - lastPolicyId;
			}

//			long jtx   = (int)v0[lastvalidid][NETSTAT.NETSTAT_RcvOneWayDelay.ordinal()];
//			long jtx1   = (int)v0[(lastvalidid + MAX_STAT_TCount - 1) % MAX_STAT_TCount][NETSTAT.NETSTAT_RcvOneWayDelay.ordinal()];
//			long jtx2   = (int)v0[(lastvalidid + MAX_STAT_TCount - 2) % MAX_STAT_TCount][NETSTAT.NETSTAT_RcvOneWayDelay.ordinal()];
//			Log.e(TAG, "GenPolicy(), jtx: " + jtx
//					+ ", jtx1: " + jtx1
//					+ ", jtx2: " + jtx2);

			long br   = (int)v0[PolicyTIdx[tail]][NETSTAT.NETSTAT_RcvByte.ordinal()];
			long jt   = (int)v0[PolicyTIdx[tail]][NETSTAT.NETSTAT_RcvOneWayDelay.ordinal()];
			long lost = (int)v0[PolicyTIdx[tail]][NETSTAT.NETSTAT_RcvLostPks.ordinal()];
			long br_pre   = 0;
			long lost_pre = 0;
			long jt_pre   = 0;

			long jtlong   = 0;	// jt超过门限持续时长
			long lostlong = 0;	// 丢包超过门限持续时长

			// 取上一策略周期的数据
			if (PolicySize > 1)
			{
				int tailpre = (PolicyHdr + PolicySize - 1) % MaxPolicyTCunt;
				lost_pre = (int)v0[PolicyTIdx[tailpre]][NETSTAT.NETSTAT_RcvLostPks.ordinal()];
				jt_pre   = (int)v0[PolicyTIdx[tailpre]][NETSTAT.NETSTAT_RcvOneWayDelay.ordinal()];
				br_pre   = (int)v0[PolicyTIdx[tailpre]][NETSTAT.NETSTAT_RcvByte.ordinal()];
			}

			// 是否开始新的周期控制
			boolean startNewT = false;

			//int idx = PolicyTIdx[tail];
			//for(; idx != lastvalidid; idx++)
			{
				// jt lost 异常触发: 门限检测
				{
					// jt 异常出现检测，记录开始时刻
					if (jt >= JTValThreshhold)
					{
						if (JTAbnormalStart == 0)
						{
							JTAbnormalStart = tick;
						}
					}
					else
					{
						JTAbnormalStart = 0;
					}

					// lost 异常出现检测，记录开始时刻
					if (lost >= LostValThreshhold)
					{
						if (LostAbnormalStart == 0)
						{
							LostAbnormalStart = tick;
						}
					}
					else
					{
						LostAbnormalStart = 0;
					}

					// 异常持续时长计算
					if (JTAbnormalStart > 0)
					{
						jtlong = GetLongTickDiff(tick, JTAbnormalStart);
					}
					if (LostAbnormalStart > 0)
					{
						lostlong = GetLongTickDiff(tick, LostAbnormalStart);
					}
				}
			}

			// 平滑的丢包率计算
			SmoothLostInT = (long)(SmoothLostInT * 0.7 + lost * 0.3);

			int LostParamNoChangeTimes_org = LostParamNoChangeTimes;

			// 抖动和丢包异常处理，抖动优先
			if (jtlong >= JTLongThreshhold)
			{
				// jt 异常

				// 开始新周期
				startNewT = true;

				// 带宽估计, 以最新的带宽为准
				//SmoothBRInT = (long)(SmoothBRInT * 0.6 + br * 0.4);
				SmoothBRInT = (long)(br);
				// 快速下调带宽
				if (jt_pre < jt)
				{
					// jt在上升，带宽下降系数设置大一些
					SmoothBRInT = (long)(SmoothBRInT * (1.0f - BWDecreaseSpeed));
				}
				else
				{
					// jt在下降，带宽下降系数设置小一些
					SmoothBRInT = (long)(SmoothBRInT * (1.0f - BWDecreaseSpeed * 0.5f)); // 0.5f cfg
				}

				// 置前一周期JT异常标记
				PStatusJT = POLICY_STATUS.POLICY_STATUS_PRE_JT_BIG;

				decctl.ActLostRatio = (int)SmoothLostInT;
				int b = 0;
				if (decctl.BwCtl > 0)
				{
					b = Math.min((int)(decctl.BwCtl * (1.0 + BWGrownSpeed)), (int)SmoothBRInT);
				}
				else
				{
					b = (int)SmoothBRInT;
				}
				b = Math.min(b, BWLimitUp);
				if (b < BWLimitDown)
				{
					b = BWLimitDown;
				}
				decctl.BwCtl = b;

				Log.e(TAG, "GenPolicy(), new T begin for jt sake."
						+ ", BwCtl: " + decctl.BwCtl);
			}
			else if (lostlong >= LostLongThreshhold)
			{
				// lost 异常

				// 持续带宽估计，平滑方式
				SmoothBRInT   = (long)(SmoothBRInT * 0.7 + br * 0.3);
				//if (SmoothLostInT > 20)
				{
					// 开始新周期
					startNewT = true;

					if (SmoothBRInT < BWLimitDown)
					{
						SmoothBRInT = BWLimitDown;
					}

					//
					if (PStatusLOST != POLICY_STATUS.POLICY_STATUS_PRE_PKLOST)
					{
						// 上一周期正常，本周期丢包超过门限
						// 可能是带宽受限，先保守，把带宽下降到实际接收带宽，看后续丢包率是否下降
						decctl.BwCtl = Math.min(decctl.BwCtl, (int)(SmoothBRInT * 1.1));
						decctl.BwCtl = Math.min(decctl.BwCtl, BWLimitUp);
					}
					else
					{
						// 检测上一周期与本周期的带宽和丢包率，判断丢包是否与带宽相关
						// 相关性不大则不是带宽受限模型

						// 带宽变化率
						float kBR   = getk(br_pre, br);

						// 丢包变化率
						float kLost = getk(lost_pre, lost);

						// 带宽和丢包联合变化率
						float kBR_Lost = getk(kBR, kLost);

						if (kBR < 0.1f && kBR < 0.1f)
						{
							// 带宽和丢包率变化都太小，不适合进行模型判断
							// 策略, 带宽参数维持不变，等待接收参数发生变化

							// 防止长时间参数不变化
							LostParamNoChangeTimes++;
							if (LostParamNoChangeTimes > 10) // cfg
							{
								decctl.BwCtl = (int)(SmoothBRInT * 1.2);
								if (decctl.BwCtl > BWLimitUp)
								{
									decctl.BwCtl = BWLimitUp / 2;
								}
								LostParamNoChangeTimes = 0;
							}
						}
						else
						{
							// 比较粗的判别...
							int b = 0;

							if (kBR_Lost < 0.15f)
							{
								// 带宽和丢包同步变化，说明带宽和丢包是相关的, 可能是偏独享型的带宽限制
								// 策略, 下调带宽
								b = (int)(SmoothBRInT * 1.1);
							}
							else
							{
								// 带宽和丢包变化不同步，可能是带宽共享型竞争性丢包
								// 策略，不必下调带宽
								b = (int)(decctl.BwCtl * (1.0 + BWGrownSpeed));
							}

							if (b < BWLimitDown)
							{
								b = BWLimitDown;
							}
							if (b > BWLimitUp)
							{
								b = BWLimitUp;
							}

							decctl.BwCtl = b;
						}
					}

					decctl.ActLostRatio = (int)SmoothLostInT;

					// 置前一周期Lost异常标记
					PStatusLOST = POLICY_STATUS.POLICY_STATUS_PRE_PKLOST;

					Log.e(TAG, "GenPolicy(), new T begin for lost sake."
							+ ", BwCtl: " + decctl.BwCtl);
				}
			}
			else
			{
				// 网络正常情况

				// 持续带宽估计，平滑方式
				SmoothBRInT = (long)(SmoothBRInT * 0.7 + br * 0.3);

				// 周期正常结束: 因是正常情况，上调发端带宽
				if (statTpast * T >= PolicyTLong)
				{
					startNewT = true;

					SmoothBRInT = (long)(SmoothBRInT * (1.0 + BWGrownSpeed));

					decctl.ActLostRatio = (int)SmoothLostInT;

					// 如果是正常周期，原则上不主动下降带宽
					//decctl.BwCtl = Math.max(decctl.BwCtl, SmoothLostInT)
					long bw2 = (long)(decctl.BwCtl * (1.0 + BWGrownSpeed));
					//if (decctl.BwCtl <= SmoothLostInT)
					//{
					//	decctl.BwCtl = (int)SmoothBRInT;
					//}
					long bwOK = Math.max(bw2, SmoothBRInT);
					if (bwOK > BWLimitUp)
					{
						bwOK = BWLimitUp;
					}

					decctl.BwCtl = (int)bwOK;

					Log.e(TAG, "GenPolicy(), new T begin normally."
							+ ", BwCtl: " + decctl.BwCtl);

					PStatusJT   = POLICY_STATUS.POLICY_STATUS_PRE_T_GOOD;
					PStatusLOST = POLICY_STATUS.POLICY_STATUS_PRE_T_GOOD;
				}
			}

			// 丢包率超过20%，发送带宽控制在上限1/3以内
			if (lost >= 20) // cfg
			{
				decctl.BwCtl = Math.min(decctl.BwCtl, BWLimitUp / 3);
			}
			// jt超过300ms，发送带宽控制在上限1/3以内
			if (jt >= 300) // cfg
			{
				decctl.BwCtl = Math.min(decctl.BwCtl, BWLimitUp / 3);
			}

			if (LostParamNoChangeTimes_org == LostParamNoChangeTimes)
			{
				LostParamNoChangeTimes = 0;
			}

			if (startNewT)
			{
				// 新周期开始

				jtlong = 0;
				lostlong = 0;
				JTAbnormalStart = 0;
				LostAbnormalStart = 0;

				//SmoothBRInT = 0;
				//SmoothLostInT = 0;

				if (PolicySize < MaxPolicyTCunt)
				{
					PolicySize++;
				}
				else
				{
					PolicyHdr++;
					PolicyHdr %= MaxPolicyTCunt;
				}
				tail = (PolicyHdr + PolicySize) % MaxPolicyTCunt;
				PolicyTIdx[tail] = lastvalidid;

				Log.e(TAG, "GenPolicy(), tail: " + tail
						+ ", RcvBw: " + v0[PolicyTIdx[tail]][NETSTAT.NETSTAT_RcvByte.ordinal()]
						+ ", JT: " + v0[PolicyTIdx[tail]][NETSTAT.NETSTAT_RcvOneWayDelay.ordinal()]
						+ ", LostR: " + v0[PolicyTIdx[tail]][NETSTAT.NETSTAT_RcvLostPks.ordinal()]
				);
			}
//			else
//			{
//				Log.e(TAG, "statTpast: " + statTpast
//					+ ", lastCalcedIdx: " + lastCalcedIdx
//					+ ", lastCalcedIdx: " + lastPolicyId
//					+ ", x: " + MAX_STAT_TCount / mXStep
//					);
//			}

			return 1;
		}

	}

	/////////////////////////////////////////////////////////
	//
	public AVCom(Context ctx,
				 USBCam usbcamIn, NativeCam nativecamIn, int br, int w, int h,
				 ImageView Imag,
//			GLFrameRenderer GLFRendererPreview,
				 GLFrameRenderer GLFRendererPeerview,
				 HorizontalScrollView StatSrollView,
				 ImageView ImageStat,
				 int UseHw264EncIn,
				 String DstIp
	)
	{
		{
			Log.e(TAG, "Loading libavcom.so librsfec.so...");

			System.loadLibrary("stunclient");

			System.loadLibrary("ICTIntTest");
			System.loadLibrary("rsfec");		// video fec encoder
			System.loadLibrary("rsfecDec");		// video fec decoder
			System.loadLibrary("rsfecA");		// 音频FEC编码, 与解码分开，避免用锁
			System.loadLibrary("rsfecADec");	// 音频FEC解码
			System.loadLibrary("opus");

			System.loadLibrary("agcns");
			System.loadLibrary("ogl");
			System.loadLibrary("VideoJB");

			System.loadLibrary("avcom");


			Log.e(TAG, "Loaded libavcom.so librsfec.so.");
		}

		int natret = NATDetect();
		Log.e(TAG, "NATDetect: " + natret);

		remoteip = DstIp;

		//mVDecPicBuf = ByteBuffer.allocateDirect(mVDecPicBufLen); // 直接访问高效内存
		//mVDecPicBufByte = new byte[mVDecPicBufLen];
		String DeviceMode = android.os.Build.MODEL;
		if (DeviceMode.equals("H60-L01"))
		{
			Soft264Init(320, 240, 3); // 1: X264_CSP_I420: 3:X264_CSP_NV12
		}
		else
		{
			//if (DeviceMode.equals("iTOP-4412")
			Soft264Init(320, 240, 1); // 1: X264_CSP_I420: 3:X264_CSP_NV12
		}

		Soft264setbitrateEncap((int)((DstSendBw - DstSendBwAudio) * 0.9)); // Bps 40000
		AVComInit(mVDecPicBuf,
				//mVDecPicBufByte,
				AudioEncBitRate,
				AudioEncCplx); //mVDecPicBufLen); // video dec init

		mctx = ctx;
		usbcam = usbcamIn;
		nativecam = nativecamIn;
		mbr = br;
		mw = w;
		mh = h;
		mImag = Imag;
		mImageStat = ImageStat;
		mStatSrollView = StatSrollView;
//		GLFrameRenderer GLFRendererPreview,
		mGLFRendererPeerview = GLFRendererPeerview;

		UseHw264Enc = UseHw264EncIn;

//		vdec = new DecCtl(ctx, Imag, w, h, GLFRendererPeerview);
//		venc = new EncCtl(ctx, usbcamIn, vdec, br, w, h); //, GLFRendererPreview);
//		vdec.SetEncCtl(venc);
//
//		int freq = 16000;
//		int channel = 1;
//		int audiobits = AudioFormat.ENCODING_PCM_16BIT;
//		audiocap = new AudioCap(ctx, this, freq, channel, audiobits);
		audiocap = new AudioCap(mctx);
	}

	int Last264BRSet = 0;
	float Change264BRThreshhold = 0.2f; // cfg, 编码器码率调整启动门限
	public int Soft264setbitrateEncap(int byterate)
	{
		byterate *= 1.3;
		if (byterate < 25000) // cfg 20000
		{
			byterate = 25000;
		}

		//if (Last264BRSet > 0)
		{
			float delta = ((float)Math.abs(Last264BRSet - byterate)) / byterate;
			if (delta >= Change264BRThreshhold)
			{
				Last264BRSet = byterate;
				return Soft264setbitrate(byterate);
			}
		}

		return 0;
	}

	public int Start()
	{

		// 解码控制
		vdec = new DecCtl(mctx, mImag, mw, mh, mGLFRendererPeerview);

		// 统计类
		int w = mStatSrollView.getWidth(); // 2000; // 5000;
		int hInDP = 75;
		int XStep = 3; // 2;// 6;// 4; // 3; // 6;// 3; // 2; //6; // 3
		mStat = new CStat(vdec, System.currentTimeMillis(), 100, w, hInDP, XStep);

		// 编码控制
//		if (VideoEncMode == VIDEO_ENC_MODE.VIDEO_ENC_MODE_X264) // cfg...
//		{
//			UseHw264Enc = 0;
//		}
		venc = new EncCtl(mctx, usbcam, nativecam, vdec, mbr, mw, mh, UseHw264Enc); //, GLFRendererPreview);

		vdec.SetEncCtl(venc);

		vdec.Go();
		venc.Go();

		// 语音捕获
		int freq = 16000;
		int channel = 1;
		int audiobits = AudioFormat.ENCODING_PCM_16BIT;
		//audiocap = new AudioCap(mctx, this, freq, channel, audiobits);
		audiocap.Start(this, freq, channel, audiobits);

		return 1;
	}

	public void Destory()
	{
		// 关闭线程
		if (vdec != null)
		{
			vdec.mRuning0 = false;
		}

		try {
			Thread.sleep(5); // 10);
		} catch (InterruptedException e) {

			e.printStackTrace();
		}

		//vdec.mRuning = false;
		if (venc != null)
		{
			venc.mRuning = false;
		}

		audiocap.isRecording = false;

		//AVComDestroy();
	}

	long GetLongTickDiff(long t1, long t0)
	{
		long diff;

		diff = t1 - t0;

		return diff;
	}

	// 时差比较, 无符号
	int GetUsTickDiff(int t1, int t0)
	{
		int t00 = t0 & 0xFFFF;
		int t11 = t1 & 0xFFFF;

		int diff;

		if (t11 >= t00)
		{
			diff = t11 - t00;
		}
		else
		{
			diff = 0x10000 - (t00 - t11);
		}

		if (diff >= 32768)
		{
			return 0x10000 - diff;
		}

		return diff;
	}

	// 时差比较, 有符号
	long GetUiTickDiffSign(int t1, int t0)
	{
		long t00 = t0 & 0xFFFFFFFF;
		long t11 = t1 & 0xFFFFFFFF;

		long diff;

		if (t11 >= t00)
		{
			diff = t11 - t00;				// 正
			if (diff >= 0x80000000)
			{
				diff = -(0x100000000L - (diff & 0xFFFFFFFF));	// 负
			}
		}
		else
		{
			diff = -(t00 - t11);			// 负
			if (diff < -0x80000000)
			{
				diff = 0x100000000L - ((-diff) & 0xFFFFFFFF);		// 正
			}
		}

		return diff;
	}

	// 帧号比较，假设帧号间的实际间隔小于32768
	int GetUsFrmIdDiff(int t1, int t0)
	{
		int t00 = t0 & 0xFFFF;
		int t11 = t1 & 0xFFFF;

		int diff;

		if (t11 >= t00)
		{
			diff = t11 - t00;				// 正
			if (diff >= 32768)
			{
				diff = -(0x10000 - (diff & 0xFFFF));	// 负
			}
		}
		else
		{
			diff = -(t00 - t11);			// 负
			if (diff < -32768)
			{
				diff = 0x10000 - ((-diff) & 0xFFFF);		// 正
			}
		}

		return diff;
	}

	// 视频编码一帧
	public class VEncFrm
	{
		final int MAX_264_FRM_DATA_LEN = 100000;
		byte videoEncOutputData[] = new byte[MAX_264_FRM_DATA_LEN];
		int datacount = 0;	// 视频编码输出的数据量

		int mFrmType = 0;
		int mVFrmId = 0;

		int OrgPkNum_N = 0;
		int FecPkNum_n = 0;

		int nalcount[]  = new int[1];
		int nalpos[]  = new int[100];
		int mNalCount = 0;			// nal数量
		int mh264Frmlen = 0;

		int width = 0;
		int height = 0;

		int h264blklen = 0;
		int m264PkIdInGrp = 0;		// 当前视频编码发送包号

		int SendOut = 1;			// 发送完标记, 当新编码时置0， 发送完成后置1, 指标可以新编码

		EncCtl encctl;




		public VEncFrm(EncCtl encctlIn)
		{
			encctl = encctlIn;
		}

		// 本帧是否空闲
		public boolean Empty() // synchronized
		{
			if (OrgPkNum_N == 0)
			{
				return true;
			}

			if (m264PkIdInGrp >= OrgPkNum_N + FecPkNum_n)
			{
				return true;
			}

			return false;
		}

		int EncodeCount = 0;
		public int Encode(int VFrmId, AvcHWEncoder enc, byte ImgIn[], int w, int h, int blksize, int MinOrgPkNum_N, float FecRatio)
		{
			OrgPkNum_N = 0;		// 当前视频编码包数
			FecPkNum_n = 0;		// FEC组内包数

			//m264PkIdInGrp = 0; // bug

			mVFrmId = VFrmId;

			width = w;
			height = h;

			// test
//			int EncodeFrmLen = 0;
//			short buffer[] = new short[320];
//			OpusEncode(EncodeFrmLen, buffer); //, i * EncodeFrmLen);

//			int trytimes = 100;
//			int tryt = 0;
//			while(tryt++ < trytimes)
//			{
//				datacount = enc.offerEncoder(ImgIn, videoEncOutputData, width, height, nalcount, nalpos);
//				if (datacount > 0)
//				{
//					Log.i(TAG, "Encode trytimes: " + tryt);
//					break;
//				}
//
//				break;
//			}
//			if (datacount <= 0)
//			{
//				Log.i(TAG, "Encode trytimes: " + tryt + ", fail.");
//				return 0;
//			}

			// test
//			if (true)
//			{
//				Log.e(TAG, "EncodeCount:" + EncodeCount);
//				EncodeCount++;
//				return 0;
//			}

			if (enc == null) // VideoEncMode == VIDEO_ENC_MODE.VIDEO_ENC_MODE_X264) // cfg...
			{
				// soft 264 encode

				int frmType = 3;	// 3 EN_ICTVOIP_ENCCTRL_FRAMETYPE_PT, 注：0号帧会被JNI自动变更为2/I帧

				// 首帧或对端要求I帧
				if (encctl.IFrmCmd > 0)
				{
					frmType = 2; // I
					encctl.IFrmCmd = 0; // clear
				}

				// test
//				if (EncodeCount > 1)
//				{
//					frmType = 3;
//				}
//				EncodeCount++;

				// 每500帧发一I帧
				if ((VFrmId % 500) == 0)
				{
					frmType = 2; // I
				}

				nalcount[0] = X264Encode(ImgIn, width, height, frmType); //(ImgIn, videoEncOutputData, width, height, nalcount, nalpos);
				//datacount = encoutput.length;
				byte nal[] = null;
				datacount = 0;
				if (nalcount[0] > 0)
				{
					for(int i = 0; i < nalcount[0]; i++)
					{
						nal = X264EncodeGetNal(i);

						nalpos[i] = datacount;

						if (nal[2] == 0) // 00 00 00 1
						{
							System.arraycopy(nal, 0,
									videoEncOutputData, datacount,
									nal.length);

							datacount += nal.length;
						}
						else			// 00 00 1
						{
							System.arraycopy(nal, 0,
									videoEncOutputData, datacount + 1,
									nal.length);

							videoEncOutputData[datacount] = 0;

							datacount += nal.length + 1;
						}
					}
				}

				// test
				if (false)
				{
					for(int i = 0; i < nalcount[0]; i++)
					{
						byte t = (byte)(videoEncOutputData[nalpos[i] + 4] & 0xF);	// sps:6 pps:7 sei:8 I:5 P:1
						if (t == 5)
						{
							// 包类型， 1：I， 2：p
							mFrmType = 1;
							//IFrmCount++;

							//IFrmLastT = tick;	// 控制i帧间隔用

							// curve output
//							String s1 = String.valueOf(IFrmCount % 20);
//							if (IntTestSwitch) IntTestOut("IFrNum", String.valueOf(IFrmCount % 20)); // CURVE_TYPE_NUMBER,
//							String s2;
//							s2 = "IndicateEncoderWork(), Frm is I.";
//							IntPrint(6, s2); // ICTVOIP_LOG_ERROR:6

							break;
						}
						else if (t == 1)
						{
							// 包类型， 1：I， 2：p
							mFrmType = 2;
							//PtFrmCount++;

							// curve output
//							String s1 = String.valueOf(PtFrmCount % 20);
//							if (IntTestSwitch) IntTestOut("PtFrNum", String.valueOf(PtFrmCount % 20)); // CURVE_TYPE_NUMBER,
							break;
						}
					}

					String IorP = "N";
					if (mFrmType == 1)
					{
						IorP = "I";
					}
					if (mFrmType == 2)
					{
						IorP = "P";
					}
					Log.i(TAG, "(" + IorP + "), frmId:" + VFrmId + ", video encode: data len:" + datacount + ", nalcount:" + nalcount[0]);

					datacount = 0; // test
				}
			}
			else
			{
				// hardware encode
				datacount = enc.offerEncoder(ImgIn, videoEncOutputData, width, height, nalcount, nalpos);
			}

			if (datacount <= 0)
			{
				return 0;
			}

			//m264FrmId++;
			mh264Frmlen = datacount;
			mNalCount = nalcount[0];

			// 原始包数
			h264blklen = blksize;
			OrgPkNum_N = datacount / h264blklen;
			if (OrgPkNum_N < MinOrgPkNum_N)
			{
				h264blklen = datacount / MinOrgPkNum_N;
				if ((h264blklen % 2) == 1)
				{
					h264blklen++;
				}
				OrgPkNum_N = datacount / h264blklen;
			}
			if ((datacount % h264blklen) != 0) // MaxPkSize_L
			{
				OrgPkNum_N++;
			}
			// FEC包数计算
			FecPkNum_n = (int)(FecRatio * OrgPkNum_N + 0.5);
			if (FecPkNum_n == 0)
			{
				FecPkNum_n = 1;
			}

			/*
			int r = FECEncode(videoEncOutputData,
				    datacount,
				    h264blklen, //MaxPkSize_L,
				    OrgPkNum_N,
				    FecPkNum_n
				    );
			*/

			mFrmType = 0;
			for(int i = 0; i < nalcount[0]; i++)
			{
				byte t = (byte)(videoEncOutputData[nalpos[i] + 4] & 0xF);	// sps:6 pps:7 sei:8 I:5 P:1
				if (t == 5)
				{
					// 包类型， 1：I， 2：p
					mFrmType = 1;
					//IFrmCount++;

					//IFrmLastT = tick;	// 控制i帧间隔用

					// curve output
//					String s1 = String.valueOf(IFrmCount % 20);
//					if (IntTestSwitch) IntTestOut("IFrNum", String.valueOf(IFrmCount % 20)); // CURVE_TYPE_NUMBER,
//					String s2;
//					s2 = "IndicateEncoderWork(), Frm is I.";
//					IntPrint(6, s2); // ICTVOIP_LOG_ERROR:6

					break;
				}
				else if (t == 1)
				{
					// 包类型， 1：I， 2：p
					mFrmType = 2;
					//PtFrmCount++;

					// curve output
//					String s1 = String.valueOf(PtFrmCount % 20);
//					if (IntTestSwitch) IntTestOut("PtFrNum", String.valueOf(PtFrmCount % 20)); // CURVE_TYPE_NUMBER,
					break;
				}
			}

//			String IorP = "N";
//			if (mFrmType == 1)
//			{
//				IorP = "I";
//			}
//			if (mFrmType == 2)
//			{
//				IorP = "P";
//			}
//			Log.i(TAG, "(" + IorP + "), frmId:" + VFrmId + ", video encode: data len:" + datacount + ", nalcount:" + nalcount[0]);

			return datacount;
		}
	}

	public class EncCtl
	{
		Context mContext;

		public int mDelay2Reset = 0;			// 延迟reset

		USBCam usbcam;
		NativeCam nativeCam;
		int ChooseCam = 0; // 1; 	// 0: usbcam, 1:nativecam

		DecCtl decctl;					// 解码控制器向源端反馈数据时，需要利用编码控制器的发送通道

		//long DataLeftInByte = 0;		// 缓冲区剩余多少编码器输出的数据
		//int IFrmReq = 0;				// 对端I帧请求
		long IFrmLastT = 0;				// 上次编码IFrm的时刻, 用于控制I帧最小间隔
		long IFrmMinInterval = 500;		// cfg, 最小I帧间隔

		//int DstSendBw = 30000;	// 50000		// cfg, 当前目标发送带宽
		int ActSendBw = 0;				// 当前实际发送带宽
		int ActSendBwInPeriod = 0;		// 当前实际发送带宽，每周期(300ms)内从小到大动态变化
		long LastSendVFrmTick = 0;		// 上一次发送视频帧时刻，用于发送带宽控制
		long VFrmInterval = 40; // 100;		// 视频帧音间隔， ms, 100ms~10fps, 40ms~25fps
		int VideoEncTime = 0;			// 视频编码时长，ms
		int VideoEncTimeAvg = 0;

		int PeerIFrmReq = 0;			// 对端I帧请求
		int IFrmCmd = 1;				// 指示下一帧编码I帧
		int Wait2ResetVEnc = 0;			// Reset video enc
		int PeerIFrmReqCount = 0;		// 对端I帧请求总数
		int PeerBwReq = 0;				// 对端接收带宽
		int PeerLostRatio = 0;			// 对端接收丢包率
		int PeerLostBurst = 0;			// 对端接收连续丢包数
		int PeerAckFrmId = -1;			// 对端接收完成的帧号

		//float ReduRatio = 0;			// 冗余倍数, 0为不冗余
		long ByteSendToal = 0;			// 发送总字节数
		long ByteSendInPeriod = 0;		// 当前周期内已发送字节数
		long PksSendToal = 0;			// 发送总包数
		long PksSendInPeriod = 0;		// 当前周期内已发送包数
		long BWStat_lastTick = 0;		// 带宽统计上一次的时刻
		long VFrmNumSendTotal = 0;		// 发送总图片帧数
		long FrmNumInPeriod = 0;		// 当前周期内编码的帧数
		//long VFrmNumRcvTotal = 0;		// 接收总图片帧数 encctl.ActDecodeVFrmNum
		long IFrmCount = 0;				// 累计I帧数
		long PtFrmCount = 0;			// 累计PT帧数
		long FrmCount = 0;				// 累计帧数
		float ActEncodeFps = 0;			// 编码帧率
		long CutAckFrm = 0;				// 收端收齐包, 源端终止继续发送本包
		//long usCurFrameNo = 0;			// 编码帧号, 从0开始
		//long usRefFrameNo = 0;			// 参考帧号


		//long mAudioPkSndsCount = 0;		// 发送音频包
		//long mAudioPkRcvCount = 0;		// 接收音频包
		//long mAudioFECPkRcvCount = 0;	// 接收音频FEC包

		// trans ctl
		int mCRC16;
		int mSN;				// 包编号， 0 based
		//int mFrmType;			// 包类型， 1：I， 2：p
		//int mNalCount;			// nal数量
		//int mh264Frmlen;		// 264编码输出帧大小, 不含FEC
		//int mh264blklenInThisPk;		// 每块数据块大小, 最后一个原始包可能较小
		//int h264blklen = 0;		// 264分块大小 , fec大小
		int m264FrmId = 0; //-1;
		//int m264PkIdInGrp;		// 当前视频编码发送包号
		//int mFecIdInGrp;		// FEC组内编码， 0 based

		int TTL_Result = 0;			// ms, RTT测量结果

		int TTL_Sor;
		int TTL_Hold;


		// fec
		final int MinOrgPkNum_N = 10;	// cfg, 最小原始包数, 大一点，有利于冗余包计算
		//int OrgPkNum_N = 0;				// 当前视频编码包数
		//int FecPkNum_n = 0;				// FEC组内包数
		final int MaxPkSize_L = 500; 	// cfg,
		float FecRatio = 0.3f;			// cfg,

		// 发包缓冲区
		byte pk2send[] = new byte[MaxPkSize_L * 2];

		// 待发送包list
		//List<byte[]> PkList = new ArrayList<byte[]>();

		//int fps = 15;					// cfg
		//int byterate = 0; //BR; //700000; // 500000; // 300000;	// 100000
		int width = 0; //WIDTH;
		int height = 0; //HEIGHT;
		//byte InputImgData[] = null;
		//final int MAX_264_FRM_DATA_LEN = 100000;
		//byte videoEncOutputData[] = new byte[MAX_264_FRM_DATA_LEN];
		VEncFrm vencFrm1 = new VEncFrm(this);
		VEncFrm vencFrm2 = new VEncFrm(this);
		VEncFrm vencFrm_send = vencFrm1;		// 当前发送帧
		VEncFrm vencFrm_standby = null;		// 储备帧

		boolean g_video_encode_proc_runing = true;

		public boolean mRuning = true;

		private Handler mHandler;

		AvcHWEncoder enc = null;

		// test
		AvcHWDecoder dec = null;

		DatagramSocket socket = null;  //创建套接字
		InetAddress address; // = InetAddress.getByName("192.168.1.80");  //服务器地址

		GLFrameRenderer mGLFRendererPreview;

		VEncThread encThread = null;
		AVMainThread avmainThread = null;

		//////////////////////////////////////////////////////////////
		public EncCtl(Context Context, USBCam usbcamIn, NativeCam nativeCamIn, DecCtl decctlIn, int br, int w, int h, int UseHw264Enc)
//		, GLFrameRenderer GLFRendererPreview)
		{
			//AVComInit(); // jni: fec 264等编解码等

			mContext = Context;

			width = w;
			height = h;
			DstSendBw = br;

			usbcam = usbcamIn;
			//InputImgData = usbcam.mdata;

			nativeCam = nativeCamIn;

//			mGLFRendererPreview = GLFRendererPreview;

			decctl = decctlIn;

			IFrmLastT = System.currentTimeMillis();

			/////////////////////////////////////
			if (UseHw264Enc > 0)
			{
				enc = new AvcHWEncoder(width, height, fps, DstSendBw);
			}

			// test
			//dec = new DecCtl(Context, null);
			//dec = new AvcHWDecoder(width, height, null);

			// socket init
			try {
				socket = new DatagramSocket();  //创建套接字
				address = InetAddress.getByName(remoteip); // "127.0.0.1");  //服务器地址
				//			} catch (UnknownHostException e) {
				//	            e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			encThread = new VEncThread();
			avmainThread = new AVMainThread();

			//mHandler = new Handler();
			//encThread.start();
			//avmainThread.start();

			////new VEncThreadt().start();
			//new VEncThread().start();
			//new AVMainThread().start();
		}

		public void Go()
		{
			mHandler = new Handler();
			encThread.start();
			avmainThread.start();

			////new VEncThreadt().start();
			//new VEncThread().start();
			//new AVMainThread().start();
		}

		public void Release()
		{
		}

		// 视频编码线程
		//
	    /*
		int mVFrmId = 0;
	  	class VEncThreadt extends Thread {
	  		int counter = 0;

	        @Override
	        public void run() {
	            // TODO Auto-generated method stub
	            //super.run();

				//InitAVCom();

	            while(mRuning) {

					try {
						Thread.sleep(5); // 10);
					} catch (InterruptedException e) {

						e.printStackTrace();
					}

            		// wait camera
        			if (usbcam.mdata == null)
        			{
        				Log.i(TAG, "video encode: usbcam not ready, waiting.");

        				try {
    						Thread.sleep(100); // 10);
    					} catch (InterruptedException e) {

    						e.printStackTrace();
    					}

        				continue;
        			}

					// rest video enc by req
		    		if (Wait2ResetVEnc == 1)
		    		{
						IndicateEncoderReset();		////////////

						if (vencFrm1 != null)
						{
							vencFrm1.OrgPkNum_N = 0;
						}
						if (vencFrm2 != null)
						{
							vencFrm2.OrgPkNum_N = 0;
						}

						Wait2ResetVEnc = 0;
		    		}

					if (vencFrm_send.Empty())
					{

						// test
						//appendMethodA("/mnt/sdcard/hwenc.yuv", usbcam.mdata, (int)(width * height * 1.5));

						int datacount = vencFrm_send.Encode(mVFrmId++, enc, usbcam.mdata, width, height, MaxPkSize_L, MinOrgPkNum_N, FecRatio);
						//vencFrm1.Encode();
					}

	            }
	        }
	    }
	    */

		class VEncThread extends Thread {

			int counter = 0;

			@Override
			public void run() {
				// TODO Auto-generated method stub
				//super.run();

				//InitAVCom();

				while(mRuning) {

					long tick = System.currentTimeMillis();

					//mHandler.post(mUpdateUI);
					//bitmap = BitmapFactory.decodeByteArray(mdata, 0, width * height);

					// wait camera
					if (usbcam == null)
					{
						Log.i(TAG, "video encode: usbcam not ready, waiting.");

						try {
							Thread.sleep(100); // 10);
						} catch (InterruptedException e) {

							e.printStackTrace();
						}

						continue;
					}
					if (usbcam.mdata == null)
					{
						Log.i(TAG, "video encode: usbcam not ready, waiting 2.");

						try {
							Thread.sleep(100); // 10);
						} catch (InterruptedException e) {

							e.printStackTrace();
						}

						continue;
					}

					//synchronized(vencFrm_send)
					//synchronized(vencFrm_send.videoEncOutputData)
					{


						//	    				Log.i(TAG,"video_encodectl_and_send_proc(), counter:" + counter +
						//	    						" act bandwidth:" + ActSendBw +
						//	    						", dst bw:" + DstSendBw +
						//	    						", ByteSendToal:" + ByteSendToal);

						// test 测试功能
						int NewIFrame = 0;
						if (GetCtlPara("NewIFrame") > 0)
						{
							NewIFrame = 1;
						}

						// 如果收到对端的断链消息, 指示重新编码I帧
						int PeerIFrmReqx = PeerIFrmReq;
						int NewIFramex = NewIFrame;
						int mDelay2Resetx = mDelay2Reset;
						if (PeerIFrmReq > 0 || NewIFrame > 0 || mDelay2Reset > 0)
						{
							if (tick - IFrmLastT >= IFrmMinInterval
									|| mDelay2Reset > 0)
							{
								Log.e(TAG,"call IndicateEncoderReset"
										+ ", PeerIFrmReq: " + PeerIFrmReq
										+ ", NewIFrame: " + NewIFrame
										+ ", mDelay2Reset: " + mDelay2Reset
								);

								IndicateEncoderReset();		////////////
								//Wait2ResetVEnc = 1;

								IFrmLastT = tick;

								PeerIFrmReqCount++;
								// curve output
								//String s1 = String.valueOf(PeerIFrmReqCount % 20);
								if (IntTestSwitch) IntTestOut("IFrameRNum", String.valueOf(PeerIFrmReqCount % 20)); // CURVE_TYPE_NUMBER,
							}
						}
						PeerIFrmReq = 0; // 清标记
						//if (Wait2ResetVEnc == 1)
						//{
						//	continue;
						//}

						// 设置丢包率，影响FEC
						{
						}

						// 如果待发送数据不足, 指示编码器工作，产生数据
						//synchronized(vencFrm_send.videoEncOutputData)
						{
							if (DataSurffient() <= 0)
							{
								IndicateEncoderWork(counter);
							}
							// 如果对端反馈某帧已经齐备, 本端终止该帧发送，开始新的帧的编码和发送
							/*
							if (PeerAckFrmId == m264FrmId)
							{
								//vencFrm_send.SendOut = 1;
								IndicateEncoderWork(counter);

								CutAckFrm++;
								// curve output
								String s1 = String.valueOf(CutAckFrm % 20);
								if (IntTestSwitch) IntTestOut("CutAckFrm", String.valueOf(CutAckFrm % 20)); // CURVE_TYPE_NUMBER,
							}
							*/
						}
					}

					try {
						Thread.sleep(1); // 10);
					} catch (InterruptedException e) {

						e.printStackTrace();
					}

					counter++;
				}

				Release();
			}
		}

		// AV主线程
		class AVMainThread extends Thread {

			int counter = 0;
			int r = 0;
			long tick = 0;

			byte audiodata[];
			byte audioFECdata[];
			//int sendbyte = 0;

			int pkinited = 0;

			long T0 = System.currentTimeMillis();

			int audioPked = 0;
			int videoPked = 0;

			private void GetParaCtl()
			{
				//FreezeVideo = GetCtlPara("FreezeVideo");
				//int FreezeAudiot = GetCtlPara("FreezeAudio");
				//FreezeAudio = FreezeAudiot;

				if (venc != null)
				{
					int br = GetCtlPara("MaxBitRate");
					if (br != -1)
					{
						if (br != DstSendBw) //venc.byterate)
						{
							//venc.byterate = br;
							DstSendBw = br;
							venc.mDelay2Reset = 1;
							Soft264setbitrateEncap((int)((DstSendBw - DstSendBwAudio) * 0.9));
						}
					}
				}
			}

			// 根据带宽偏差调整帧间间隔
//			int AdjustVFrmInterval(int SetBR, int ActBR, int VFrmInterv)
//			{
//				int onefrmsendtime = 10; // 一帧的发送时长， ms
//				int FrmRateEst = 1000 / (VFrmInterv + 10);
//
//				int diff = SetBR - ActBR;
//				if (diff > 0)
//				{
//					VFrmInterval *= 0.25;
//				}
//				else
//			}

			@Override
			public void run() {
				// TODO Auto-generated method stub
				//super.run();

				//InitAVCom();

				/*
				User A allow A or V
				A-->Allow A or V-->B
				A<--req A or V-----B
				A--------AV------->B
				*/

				while(mRuning) {

					counter++;

					try {
						Thread.sleep(1); // 4); // 10);
					} catch (InterruptedException e) {

						e.printStackTrace();
					}

					// 获取动态控制参数
					if (counter % 100 == 1)
					{
						GetParaCtl();
					}

					pkinited = 0;	// 建包头

					/**/
					if (DstSendBw < DstSendBwAudio)
					{
						// 低带宽探测模式
						// 如果允许发送带宽低，不足以支撑语音和视频的发送，则发送构造包

						// 带宽过低，清语音数据，防止堆积
						while(true)
						{
							audiodata = GetAudioEncData();
							if (audiodata == null)
							{
								break;
							}
						}

						// udppk udppklen
						FromUdpPkt();
						pkinited = 1;

						// 填充数据

						// 探测包内容大小控制
						int len = 100;

						// 包率控制
						int pkslen = len + 12 + 28;
						int pkps = DstSendBw / pkslen;	// 每秒多少包
						int delayms = (int)1000 / pkps;	// 包间时间间隔ms
						if (delayms == 0)
						{
							delayms = 1;
						}
						try {
							Thread.sleep(delayms - 1);
						} catch (InterruptedException e) {

							e.printStackTrace();
						}

						AppendFillDataItem2UdpPkt(len);
					}
					else /////////////////////
					{
						// 正常A/V通信

						audioPked = 0;	// 语音包是否加入本包
						videoPked = 0;	// 视频包是否加入本包

						if (FreezeAudio == 0 && DstSendBw >= DstSendBwAudio)
						{
							// 取语音包+FEC包
							audiodata = GetAudioEncData();

							long tick = System.currentTimeMillis();

							//
							if (audiodata != null)
							{
								// 头两个short是语音压缩数据和FEC数据的长度
								int lenAudio;
								int lenFec;
								lenAudio = ((audiodata[1]&0xFF) << 8) + (audiodata[0]&0xFF);
								lenFec = ((audiodata[3]&0xFF) << 8) + (audiodata[2]&0xFF);

								// udppk udppklen
								FromUdpPkt();
								pkinited = 1;

								// FEC分组开始
								if ((audiocap.mAudioFrmId % audiocap.OrgPkNum_N) == 0)
								{
									// 发送FEC开始
									audiocap.m264PkIdInGrp = 0;
								}

								// 语音编码数据
								AppendAudioItem2UdpPkt(audiodata, 4, lenAudio, audiocap.mAudioFrmId, audiocap.m264PkIdInGrp);

								// FEC包
								if (audiocap.m264PkIdInGrp < audiocap.FecPkNum_n)
								{
									int PkIdInGrp = audiocap.m264PkIdInGrp;

									//audioFECdata = GetFecPkAudio(audiocap.m264PkIdInGrp); // mFecIdInGrp++);
									// 语音FEC数据
									//if (audioFECdata != null)
									if (lenFec > 0)
									{
										//audioFECdata
										AppendAudioFECItem2UdpPkt(audiodata, 4 + lenAudio, lenFec,
												audiocap.mAudioFrmId,
												audiocap.m264PkIdInGrp + audiocap.OrgPkNum_N);

										//audiocap.m264PkIdInGrp %= audiocap.OrgPkNum_N;
									}
								}

								//audiocap.mAudioEncCount++; // 累计帧数

								audiocap.m264PkIdInGrp++;

								audiocap.mAudioFrmId++;
								if (audiocap.mAudioFrmId >= 65530) // 整除OrgPkNum_N(10)
								{
									audiocap.mAudioFrmId = 0;
								}

								audioPked = 1;
							}
						}

						// 根据可用带宽发送数据
	/*
						if (false)
						{
							if (FreezeVideo == 0)
							{
								// 未被要求冻结画面

								if (vencFrm_send.m264PkIdInGrp >= vencFrm_send.OrgPkNum_N + vencFrm_send.FecPkNum_n)
		    					{
									// 无剩余发送数据，指示编码器编码

									// 时延控制
									// 基本时延, 25fps, 40ms
									int tickdiff = GetUsTickDiff((int)tick, (int)LastSendVFrmTick);

									if (counter % 10 == 0)
									{
										Log.e(TAG, "VFrmInterval:" + VFrmInterval
											+ ", VideoEncTimeAvg:" + VideoEncTimeAvg
											);
									}

									// 时延间隔扣除编码时长
									long interval = VFrmInterval;
									if (interval >= VideoEncTimeAvg)
									{
										interval -= VideoEncTimeAvg;
									}
									else
									{
										interval = 0;
									}

									if (tickdiff >= interval)
									{
										LastSendVFrmTick = tick;

			    						vencFrm_send.SendOut = 1;
									}
		    					}
		    					else
		    					{
									// 时延控制
									// 基本时延, 25fps, 40ms
									//int tickdiff = GetUsTickDiff((int)tick, (int)LastSendVFrmTick);
									//if (tickdiff >= VFrmInterval)
									{
										//LastSendVFrmTick = tick;

										// 创建包头
										if (pkinited == 0)
										{
											// udppk udppklen
								    		FromUdpPkt();
											pkinited = 1;
										}

										// 视频
										//synchronized(vencFrm_send)
										//synchronized(vencFrm_send.videoEncOutputData)
										{
											// 对端帧已收齐，后续fec包不必发送
											if (PeerAckFrmId == m264FrmId)
											{
												vencFrm_send.SendOut = 1;	// 指示编码器编码

												CutAckFrm++;
												// curve output
												String s1 = String.valueOf(CutAckFrm % 20);
												if (IntTestSwitch) IntTestOut("CutAckFrm", String.valueOf(CutAckFrm % 20)); // CURVE_TYPE_NUMBER,
											}
											else
											{
												r = AppendVideoItem2UdpPkt();
												if (r > 0)
												{
													vencFrm_send.m264PkIdInGrp++;
												}
											}
										}
									}
		    					}
							}
						}
						else /////////////////////////////////////////////////////
	*/
						{
							if (FreezeVideo == 0
									&& ActSendBwInPeriod < DstSendBw) // - DstSendBwAudio) // ActSendBw
							{
								if (vencFrm_send.SendOut == 1)
								{
									// 继续等待视频编码器输出数据
								}
								if (vencFrm_send.m264PkIdInGrp
										>= vencFrm_send.OrgPkNum_N + vencFrm_send.FecPkNum_n)
								{
									vencFrm_send.SendOut = 1;	// 指示编码器编码

									vencFrm_send.m264PkIdInGrp
											= 0x7FFFFFFF;
								}
								else
								{
									if (pkinited == 0)
									{
										// udppk udppklen
										FromUdpPkt();
										pkinited = 1;
									}

									// 视频
									//synchronized(vencFrm_send)
									//synchronized(vencFrm_send.videoEncOutputData)
									{
										// 对端帧已收齐，后续fec包不必发送
										if (PeerAckFrmId == m264FrmId)
										{
											vencFrm_send.SendOut = 1;	// 指示编码器编码

											vencFrm_send.m264PkIdInGrp
													= 0x7FFFFFFF; // vencFrm_send.OrgPkNum_N + vencFrm_send.FecPkNum_n; //bug

											CutAckFrm++;
											// curve output
											//String s1 = String.valueOf(CutAckFrm % 20);
											if (IntTestSwitch) IntTestOut("CutAckFrm", String.valueOf(CutAckFrm % 20)); // CURVE_TYPE_NUMBER,
										}
										else
										{
											r = AppendVideoItem2UdpPkt();
											if (r > 0)
											{
												vencFrm_send.m264PkIdInGrp++;
											}
										}
									}

									videoPked = 1;
								}
							}
						}
					}


					// 发送
					if (pkinited == 1)
					{
						long t = System.currentTimeMillis();

						// 反向信元
						//if (counter % 5 == 1 || decctl.IFrmReq > 0) // 频度控制
						{
							// 向udp包追加反向信元
							AppendReverseItem2UdpPkt(t);
						}

						// 打时间戳
						tick = t;
						SetUdpTick(t);// & 0xFFFF

						ByteSendToal += udppkLen + 28;
						ByteSendInPeriod += udppkLen + 28;
						PksSendToal++;
						PksSendInPeriod++;

						// 发送字节数 // send Report
						//setushort(udppk, 8, (int)ByteSendToal);
						setint(udppk, 8, (int)ByteSendToal / 10);

						// 计算包校验
						CRCUdpPkt();

						SendOnePkt(); // test rmv sendbyte =
					}

					ActSendBwInPeriod = (int)StatActBW(tick);
					//Log.e(TAG, "ActSendBw:" + ActSendBwInPeriod + "----------");
				}
			}

			// 统计实际带宽
			long BWStatPeriodInMS_count = 0;
			long BWStatPeriodInMS = 250; //300;	// cfg, 带宽统计周期ms, 一般为2秒
			int StatActBW(long tick)
			{
				//tick = System.currentTimeMillis();
				if (BWStat_lastTick == 0)
				{
					BWStat_lastTick = tick;
				}
				long TickPast = tick - BWStat_lastTick;

				//float bw = 0;
				float fStatFps = 0;
				float NativeCamfStatFps = 0;
				float audioSndPR = 0;

				if (TickPast > 0)
				{
					ActSendBwInPeriod = (int)(ByteSendInPeriod * 1000 / TickPast);
				}
				else
				{
					ActSendBwInPeriod = 0;
				}

				if (tick - T0 >= BWStatPeriodInMS * BWStatPeriodInMS_count)
				{
					BWStatPeriodInMS_count++;
					//}
					//// 新周期开始，更新周期起始时刻
					//if (TickPast >= BWStatPeriodInMS)
					//{
					//Log.i(TAG, "StatActBW(), s_counter:" + s_counter);

					BWStat_lastTick = tick;

					//if (TickPast > 0)
					{
						ActSendBw = (int)(ByteSendInPeriod * 1000 / BWStatPeriodInMS);

						fStatFps = FrmNumInPeriod * 1000 / BWStatPeriodInMS;
						if (audiocap != null)
							audioSndPR = audiocap.mAudioEncCount_Period * 1000 / BWStatPeriodInMS;

						if (nativeCam != null)
						{
							NativeCamfStatFps = nativeCam.NativeCamFrmNumInPeriod * 1000 / BWStatPeriodInMS;
						}
					}

//					Log.e(TAG, "idx:" + BWStatPeriodInMS_count
//						+ ", ByteSendInPeriod:" + ByteSendInPeriod
//						+ ", ActSendBwInPeriod:" + ActSendBwInPeriod
//						+ ", ActSendBw:" + ActSendBw
//						+ ", fStatFps:" + fStatFps);


					//ActEncodeFps = FrmCount * 1000 / TickPast;


					// 上一周期多发的数据要计入下一周期
					//				ByteSendInPeriod -=
					//					DstSendBw * BWStatPeriodInMS / 1000;
					//				if (ByteSendInPeriod < 0)
					//				{
					//					ByteSendInPeriod = 0;
					//				}
					ByteSendInPeriod = 0;
					PksSendInPeriod = 0;
					FrmNumInPeriod = 0;
					if (nativeCam != null)
					{
						nativeCam.NativeCamFrmNumInPeriod = 0;
					}
					if (audiocap != null)
					{
						audiocap.mAudioEncCount_Period = 0;
					}

					{
						//String s1 = String.valueOf((int)ActSendBwInPeriod);
						if (IntTestSwitch) IntTestOut("RealBitrate", String.valueOf((int)ActSendBwInPeriod)); // CURVE_TYPE_NUMBER,
					}
					{
						//String s1 = String.valueOf(fStatFps);
						if (IntTestSwitch) IntTestOut("RealFps", String.valueOf(fStatFps)); // CURVE_TYPE_NUMBER,
					}
					{
						//String s1 = String.valueOf(NativeCamfStatFps);
						if (IntTestSwitch) IntTestOut("NativeCamFps", String.valueOf(NativeCamfStatFps)); // CURVE_TYPE_NUMBER,
					}
					{
						//String s1 = String.valueOf(audioSndPR);
						if (IntTestSwitch) IntTestOut("AudioSndPR", String.valueOf(audioSndPR)); // CURVE_TYPE_NUMBER,
					}
				}

				//			char valueStr[50];
				//			sprintf(valueStr, "%.2f", bw );
				//			RemoteDisplayData(&gstVEncCtrlModule, "RealBitrate", CURVE_TYPE_NUMBER, valueStr);
				//
				//			sprintf(valueStr, "%.2f", fStatFps);
				//			RemoteDisplayData(&gstVEncCtrlModule, "RealFps", CURVE_TYPE_NUMBER, valueStr);


				return ActSendBwInPeriod;
			}
		}



		// 判断数据是否充足
		int DataSurffient()
		{
			// 264原始包和冗余包是否都发送完毕
			//if (m264PkIdInGrp >= OrgPkNum_N
			//		&& mFecIdInGrp >= FecPkNum_n)

			if (vencFrm_send == null)
			{
				return 0;
			}

			//if (vencFrm_send.m264PkIdInGrp >= vencFrm_send.OrgPkNum_N + vencFrm_send.FecPkNum_n)
			if (vencFrm_send.SendOut == 1)
			{
				return 0;
			}

			return 1;
		}

		final int MAX_UDP_SIZE = 2048;
		byte [] udppk = new byte[MAX_UDP_SIZE];
		int udppkLen = 0;
		// 把short数据value存入by + offset和by + offset + 1位置
//		void setushort(byte by[], int offset, int value)
//		{
//			by[offset] 		= (byte)(value); // & 0xFF);
//			by[offset + 1] 	= (byte)((value >> 8)); // & 0xFF);
//		}
		void setushort(byte by[], int offset, int value)
		{
			by[offset] 		= (byte)(value & 0xFF);
			by[offset + 1] 	= (byte)((value >> 8) & 0xFF);
		}
		void setint(byte by[], int offset, int value)
		{
			by[offset] 		= (byte)(value & 0xFF);
			by[offset + 1] 	= (byte)((value >> 8) & 0xFF);
			by[offset + 2] 	= (byte)((value >> 16) & 0xFF);
			by[offset + 3] 	= (byte)((value >> 24) & 0xFF);
		}
		int getushort(byte by[], int offset)
		{
//				short v1 = (short)(by[offset] & 0xFF);
//				short v2 = (short)(by[offset + 1] & 0xFF);
//				short value = (short)((v2 << 8) + v1);
//				int value = (int)(((by[offset + 1] & 0xff) << 8) | (by[offset] & 0xff));
//				int value = (int)(((int)((by[offset + 1]) << 8)) | (by[offset]));
			int targets = (by[offset] & 0xff) | ((by[offset + 1] << 8) & 0xff00);

			return targets;
		}
		int getint(byte by[], int offset)
		{
			int targets = (by[offset] & 0xff) | ((by[offset + 1] << 8) & 0xff00)
					| ((by[offset + 2] << 16) & 0xff0000) | ((by[offset + 3] << 24) & 0xff000000);
			return targets;
		}
		// 计算crc, bytelen必须为2的倍数
		short GetCRC16(byte data[], int offset, int bytelen)
		{
			short crc = 0;
			int shortd;

			for(int i = 0 + offset; i < bytelen; i += 2)
			{
				shortd = (((int)data[i]) << 8) + data[i+1];

				crc ^= shortd;
			}

			return crc;
		}

		// 生成一个udp包
		int FromUdpPkt()
		{
			//long tick = System.currentTimeMillis() & 0xFFFF;

			int i = 0;

			// Hdr
			{
				//			setushort(udppk, i, mCRC16);
				i += 2;

				//			setushort(udppk, i, tick);
				//			i += 2;
				//			setint(udppk, i, tick);
				i += 4;

				setushort(udppk, i, mSN++);				// 包序列号
				i += 2;

				// send Report
				// sendbyte
				//			setint(udppk, i, (int)ByteSendToal);
				i += 4;
			}

			udppkLen = i;

			// crc计算
			//i = 0;
			//mCRC16 = GetCRC16(udppk, 2, udppkLen - 2);
			//setushort(udppk, i, mCRC16);

			return udppkLen;
		}

		// 向udp包追加反向信元
		int AppendReverseItem2UdpPkt(long tick)
		{
			int offset = udppkLen;
			int i = offset;

			// B向A反馈
			{
				// T
				setushort(udppk, i, (int) UDP_ITEM.UDP_ITEM_BA.ordinal());
				i += 2;

				// L
				//udppk[udppkLen + i] = lenIn;
				i += 2;

				// V

				//setushort(udppk, i, decctl.IFrmReq);				// I帧请求
				//i += 2;
				//setushort(udppk, i, decctl.ActRcvBw);	//
				//i += 2;
				//setushort(udppk, i, decctl.ActLostRatio * 100);	//
				//i += 2;
				//setushort(udppk, i, decctl.ActLostBurst);	//
				//i += 2;
				udppk[i] = (byte)decctl.IFrmReq;
				i++;
				//int rcvbw = mStat.GetRcvBw(3) / 100;
				int rcvbw = decctl.BwCtl / 100;			// 控制发端带宽
				setushort(udppk, i, rcvbw); // decctl.ActRcvBw / 100);	//
				i += 2;
				udppk[i] = (byte)decctl.ActLostRatio;
				i++;
				udppk[i] = (byte)decctl.ActLostBurst;
				i++;

//				Log.e(TAG, "AppendReverseItem2UdpPkt: rcvbw: " + rcvbw
//					+ ", ActLostRatio: " + decctl.ActLostRatio
//					);

				setushort(udppk, i, decctl.mAckFrmId);	//
				i += 2;
				// TTL测量, 本端为B端, 向A端发送估计的A端时刻, A端用该时刻与最新时刻比较, 得到TTL
				if (decctl.TTL_Sor != -1)
				{
					int tick_hold = GetUsTickDiff((int)tick, (int)decctl.TTL_Local);
					int SorEstmate = (int)(decctl.TTL_Sor) + tick_hold;
					setint(udppk, i, SorEstmate);	//

					// important print
					//		    		Log.i(TAG, "SendOnePkt()"
					//						+ ", tickA:" + tick
					//						+ ", tickB_local:" + decctl.TTL_Local
					//						+ ", TTL_Sor:" + decctl.TTL_Sor
					//						+ ", tick_hold:" + tick_hold
					//						+ ", decctl.TTL_Local:" + decctl.TTL_Local
					//						+ ", SorEstmate:" + SorEstmate
					//						+ ", est ttl:" + (GetUsTickDiff((int)tick, SorEstmate))
					//						);

					// curve output
					//String s1 = String.valueOf(tick_hold);
					if (IntTestSwitch) IntTestOut("ttl_hold", String.valueOf(tick_hold)); // CURVE_TYPE_NUMBER,
				}
				else
				{
					setint(udppk, i, 0);	//
				}
				decctl.TTL_Sor = -1;
				i += 4;
			}

			int itemLen = i - offset; // 长度含T L V

			// L
			setushort(udppk, offset + 2, itemLen);

			udppkLen += itemLen;

			return itemLen;

		}

		// 向udp包追加Video信元
		int AppendVideoItem2UdpPkt()
		{
			if (vencFrm_send.m264PkIdInGrp >= vencFrm_send.OrgPkNum_N + vencFrm_send.FecPkNum_n)
			{
				return 0;
			}

			int offset = udppkLen;
			int i = offset;

			// T
			setushort(udppk, i, (int) UDP_ITEM.UDP_ITEM_VIDEO.ordinal());
			i += 2;

			// L
			//udppk[udppkLen + i] = lenIn;
			i += 2;

			// V
			{
				udppk[i] = (byte)vencFrm_send.mFrmType;
				i++;
				udppk[i] = (byte)vencFrm_send.mNalCount;
				i++;

				setushort(udppk, i, vencFrm_send.mh264Frmlen);		// 编码器输出数据总长度
				i += 2;
				setushort(udppk, i, vencFrm_send.h264blklen);	// 数据块大小
				i += 2;
				setushort(udppk, i, m264FrmId);			// 帧号
				i += 2;
				setushort(udppk, i, vencFrm_send.m264PkIdInGrp);	// 帧内包号, >=OrgPkNum_N的是fec组
				i += 2;
				//			setushort(udppk, i, mFecIdInGrp++);		// 帧内冗余包号
				//			i += 2;

				//setushort(udppk, i, OrgPkNum_N);		// 帧内包数
				//i += 2;
				//setushort(udppk, i, FecPkNum_n);		// 帧内冗余包数
				//i += 2;
				udppk[i] = (byte)vencFrm_send.OrgPkNum_N;
				i++;
				udppk[i] = (byte)vencFrm_send.FecPkNum_n;
				i++;

				// 保护
				int finallen = udppkLen + 2 + 2 + i + vencFrm_send.h264blklen; // T L V
				if (finallen > MAX_UDP_SIZE)
				{
					Log.i(TAG, "UDP too big: len" + finallen);

					return 0;
				}

				// 先发原始包， 后发fec包
				if (vencFrm_send.m264PkIdInGrp < vencFrm_send.OrgPkNum_N)
				{
//					Log.e(TAG, "1----- id " + vencFrm_send.m264PkIdInGrp
//						+ ", i: " + i
//						+ ", l: " + vencFrm_send.h264blklen);

					System.arraycopy(vencFrm_send.videoEncOutputData,
							vencFrm_send.m264PkIdInGrp * vencFrm_send.h264blklen,
							udppk, i,
							vencFrm_send.h264blklen); //MaxPkSize_L);

					// test
					//if (vencFrm_send.m264PkIdInGrp == vencFrm_send.OrgPkNum_N - 1)
					//{
					//	Log.i(TAG, "----- vencFrm_send.m264PkIdInGrp == vencFrm_send.OrgPkNum_N - 1: " + vencFrm_send.m264PkIdInGrp);
					//}
				}
				else if (vencFrm_send.m264PkIdInGrp < vencFrm_send.OrgPkNum_N + vencFrm_send.FecPkNum_n)
				{
					byte fecpk[] = GetFecPk(vencFrm_send.m264PkIdInGrp - vencFrm_send.OrgPkNum_N); // mFecIdInGrp++);

					if (fecpk != null)
					{
//			    		int len = fecpk.length;
//			    		if (len != vencFrm_send.h264blklen)
//			    		{
//			    			int z = 0;
//			    		}
//						for(int ii = 0; ii < fecpk.length; ii++)
//						{
//							fecpk[ii] = 1;
//						}

//						Log.e(TAG, "2----- :id: " + (vencFrm_send.m264PkIdInGrp) + ", orgn:" + vencFrm_send.OrgPkNum_N
//							//+ ", i: " + i
//							+ ", l: " + vencFrm_send.h264blklen);
//
//						if (fecpk.length != vencFrm_send.h264blklen)
//						{
//							Log.e(TAG, "3----- :id: " + (vencFrm_send.m264PkIdInGrp - vencFrm_send.OrgPkNum_N)
//									//+ ", i: " + i
//									+ ", l: " + vencFrm_send.h264blklen);
//						}

						// tbd, 可以对fecpk的长度进行判断，看是否等于vencFrm_send.h264blklen，不同则处理处理异常
						if (fecpk.length == vencFrm_send.h264blklen)
						{
							System.arraycopy(fecpk, 0,
									udppk, i,
									vencFrm_send.h264blklen); //MaxPkSize_L);
						}
						else
						{
							Log.e(TAG, "bug report: ----- :id: " + (vencFrm_send.m264PkIdInGrp - vencFrm_send.OrgPkNum_N)
									//+ ", i: " + i
									+ ", l: " + vencFrm_send.h264blklen);

							return 0;
						}
					}
					else
					{
						Log.i(TAG, "SendOnePkt(), can't get fec pk:" + (vencFrm_send.m264PkIdInGrp - vencFrm_send.OrgPkNum_N));

						return 0;
					}
				}

				i += vencFrm_send.h264blklen;

				int itemLen = i - offset;

				// L
				setushort(udppk, offset + 2, itemLen);

				udppkLen += itemLen;
			}

			return i - offset;
		}

		// 向udp包追加Audio信元
		// len: 2字节长度字段 + opus数据
		int AppendAudioItem2UdpPkt(byte AData[], int ADataOffset, int len, int frmid, int idInGrp)
		{
			int offset = udppkLen;
			int i = offset;

			// T
			setushort(udppk, i, (int) UDP_ITEM.UDP_ITEM_AUDIO.ordinal());
			i += 2;

			// L
			//udppk[udppkLen + i] = lenIn;
			i += 2;

			// V
			{
				setushort(udppk, i, len);							// 数据块大小, 数据区头两字节是后续数据区的大小
				i += 2;
				setushort(udppk, i, frmid); //audiocap.mAudioFrmId);			// 帧号
				i += 2;
				udppk[i] = (byte)idInGrp; //audiocap.m264PkIdInGrp;			// 帧内包号, >=OrgPkNum_N的是fec组
				i++;
				udppk[i] = (byte)audiocap.OrgPkNum_N;
				i++;
				udppk[i] = (byte)audiocap.FecPkNum_n;
				i++;

				System.arraycopy(AData,
						ADataOffset,
						udppk, i,
						len); //MaxPkSize_L);

				i += len;

				int itemLen = i - offset;

				// L
				setushort(udppk, offset + 2, itemLen);

				udppkLen += itemLen;
			}

			return i - offset;
		}

		// 向udp包追加Audio FEC信元
		int AppendAudioFECItem2UdpPkt(byte AData[], int ADataOffset, int len, int frmid, int idInGrp)
		{
			int offset = udppkLen;
			int i = offset;

			//byte fecpk[];

			// T
			setushort(udppk, i, (int) UDP_ITEM.UDP_ITEM_AUDIO_FEC.ordinal());
			i += 2;

			// L
			//udppk[udppkLen + i] = lenIn;
			i += 2;

			// V
			{
				// 收端根据帧号换算FEC组号
				//int fecgrpid = (audiocap.mAudioFrmId / audiocap.OrgPkNum_N - 1);

				setushort(udppk, i, len);							// 数据块大小
				i += 2;
				//setushort(udppk, i, fecgrpid);						// FEC组号
				setushort(udppk, i, frmid); //audiocap.mAudioFrmId);			// 帧号, 收端根据帧号换算FEC组号
				i += 2;
				udppk[i] = (byte)idInGrp; //audiocap.m264PkIdInGrp;			// 帧内包号, >=OrgPkNum_N的是fec组
				i++;
				udppk[i] = (byte)audiocap.OrgPkNum_N;
				i++;
				udppk[i] = (byte)audiocap.FecPkNum_n;
				i++;

				System.arraycopy(AData,
						ADataOffset,
						udppk, i,
						len); //MaxPkSize_L);

				i += len;

				int itemLen = i - offset;

				// L
				setushort(udppk, offset + 2, itemLen);

				udppkLen += itemLen;
			}

			return i - offset;
		}

		// 向udp包追加填充数据信元
		// len:本信元总长，含T、L、V
		int AppendFillDataItem2UdpPkt(int len)
		{
			int offset = udppkLen;
			int i = offset;

			//byte fecpk[];

			// T
			setushort(udppk, i, (int) UDP_ITEM.UDP_ITEM_FILL_DATA.ordinal());
			i += 2;

			// L
			//udppk[udppkLen + i] = lenIn;
			i += 2;

			// V
			{
				i += len - 4;
				int itemLen = i - offset;

				// L
				setushort(udppk, offset + 2, itemLen);

				udppkLen += itemLen;
			}

			return len; //i - offset;
		}

		// 向udp包追加信元
//		int AppendItem2UdpPkt(int ItemType, byte [] dataIn, int lenIn)
//		{
//
//			int finallen = udppkLen + 2 + 2 + lenIn; // T L V
//
//			if (finallen > MAX_UDP_SIZE)
//			{
//				Log.i(TAG, "UDP too big: len" + finallen);
//
//				return 0;
//			}
//
//			udppk[udppkLen] = ItemType;
//			udppk[udppkLen + 2] = lenIn;
//
//			System.arraycopy(dataIn, 0,
//					udppk, udppkLen + 4,
//					lenIn);
//
//			// crc计算
//			//int i = 0;
//			//int crc = GetCRC16(udppk, udppkLen, lenIn + 4);
//			//crc
//			//setushort(udppk, i, mCRC16);
//		}

		// 生成一个udp包
		void SetUdpTick(long tick)
		{
			//setushort(udppk, 2, (int)tick);
			setint(udppk, 2, (int)tick);
		}

		// 计算udp包crc
		void CRCUdpPkt()
		{
			int i = 0;

			// 保证长度是偶数
			if ((udppkLen % 2) == 1)
			{
				udppkLen++;
			}

			mCRC16 = GetCRC16(udppk, 2, udppkLen - 2);
			setushort(udppk, i, mCRC16);
		}

		// 生成一个udp包
/*
		int FromUdpPkt_old(byte [] OutUdpdata, byte [] data264In, int len264In)
		{
	//			int mSN;				// 包编号， 0 based
	//
	//			int m264PkIdInGrp;		// 当前视频编码发送包号
	//			int mFecIdInGrp;		// FEC组内编码， 0 based
	//
	//			int TTL_Sor;
	//			int TTL_Hold;
	//		    int OrgPkNum_N = 0;		// 当前视频编码包数
	//		    int FecPkNum_n = 0;		// FEC组内包数

			long tick = System.currentTimeMillis() & 0xFFFF;


			int i = 0;

			// A -> B
			{
	//			setushort(udppk, i, mCRC16);
				i += 2;

				setushort(udppk, i, mSN++);				// 包序列号
				i += 2;

				//setushort(udppk, i, mFrmType);			// 包类型， 1：I， 2：p
				//i += 2;
				udppk[i] = (byte)vencFrm_send.mFrmType;
				i++;
				udppk[i] = (byte)vencFrm_send.mNalCount;
				i++;

				setushort(udppk, i, vencFrm_send.mh264Frmlen);		// 编码器输出数据总长度
				i += 2;
				setushort(udppk, i, vencFrm_send.h264blklen);	// 数据块大小
				i += 2;
				setushort(udppk, i, m264FrmId);			// 帧号
				i += 2;
				setushort(udppk, i, vencFrm_send.m264PkIdInGrp++);	// 帧内包号, >=OrgPkNum_N的是fec组
				i += 2;
	//			setushort(udppk, i, mFecIdInGrp++);		// 帧内冗余包号
	//			i += 2;

				//setushort(udppk, i, OrgPkNum_N);		// 帧内包数
				//i += 2;
				//setushort(udppk, i, FecPkNum_n);		// 帧内冗余包数
				//i += 2;
				udppk[i] = (byte)vencFrm_send.OrgPkNum_N;
				i++;
				udppk[i] = (byte)vencFrm_send.FecPkNum_n;
				i++;

				setushort(udppk, i, (int)tick);			// TTL测量 TTL_Sor
				i += 2;
			}

			// B向A反馈
			{
				setushort(udppk, i, decctl.IFrmReq);				// I帧请求
				i += 2;
				setushort(udppk, i, decctl.ActRcvBw);	//
				i += 2;
				setushort(udppk, i, decctl.ActLostRatio * 100);	//
				i += 2;
				setushort(udppk, i, decctl.ActLostBurst);	//
				i += 2;
				setushort(udppk, i, decctl.mAckFrmId);	//
				i += 2;
				// TTL测量, 本端为B端, 向A端发送估计的A端时刻, A端用该时刻与最新时刻比较, 得到TTL
				if (decctl.TTL_Sor != -1)
				{
					int tick_hold = GetUsTickDiff((int)tick, (int)decctl.TTL_Local);
					int SorEstmate = (int)(decctl.TTL_Sor) + tick_hold;
					setint(udppk, i, SorEstmate);	//

					// important print
	//		    		Log.i(TAG, "SendOnePkt()"
	//						+ ", tickA:" + tick
	//						+ ", tickB_local:" + decctl.TTL_Local
	//						+ ", TTL_Sor:" + decctl.TTL_Sor
	//						+ ", tick_hold:" + tick_hold
	//						+ ", decctl.TTL_Local:" + decctl.TTL_Local
	//						+ ", SorEstmate:" + SorEstmate
	//						+ ", est ttl:" + (GetUsTickDiff((int)tick, SorEstmate))
	//						);

					// curve output
					//String s1 = String.valueOf(tick_hold);
					if (IntTestSwitch) IntTestOut("ttl_hold", String.valueOf(tick_hold)); // CURVE_TYPE_NUMBER,
				}
				else
				{
					setint(udppk, i, 0);	//
				}
				decctl.TTL_Sor = -1;
				i += 4;
			}

			// i必须是偶数
			//udppk[i++] = 0;
			if ((i % 2) == 1)
			{
				i++;
			}

			System.arraycopy(data264In, 0,
					udppk, i,
					vencFrm_send.h264blklen); //MaxPkSize_L);

			udppkLen = i + vencFrm_send.h264blklen;

			// crc计算
			i = 0;
			mCRC16 = GetCRC16(udppk, 2, udppkLen - 2);
			setushort(udppk, i, mCRC16);

			return udppkLen;
		}
*/

		int SendOnePkt()
		{
			int port = 50000;

			UdpSend(address, port, udppk, udppkLen);

			return udppkLen;
		}
		/*
		int SendOnePkt_old()
		{
			int sendbyte = 0;

//			if (vencFrm_send.OrgPkNum_N == 0) // && vencFrm_send.FecPkNum_n == 0)
//			{
//				return 0;
//			}

			if (vencFrm_send.SendOut == 1)
			{
				return 0;
			}

			//sendbyte = udppkLen; //MaxPkSize_L; // test

			byte fecpk[];

		    int port = 50000;

			// 先发原始包， 后发fec包
		    if (vencFrm_send.m264PkIdInGrp < vencFrm_send.OrgPkNum_N)
		    {
		    	//fecpk = GetFecPk(FecPkNum_n);
		    	System.arraycopy(vencFrm_send.videoEncOutputData, vencFrm_send.m264PkIdInGrp * vencFrm_send.h264blklen,
		    			pk2send, 0,
		    			vencFrm_send.h264blklen); //MaxPkSize_L);  //;pk2send videoEncOutputData;
	//		    	m264PkIdInGrp++;

		    	if (vencFrm_send.m264PkIdInGrp + 1 < vencFrm_send.OrgPkNum_N)
		    	{
		    		// 非最后一包
		    		//vencFrm_send.mh264blklenInThisPk = vencFrm_send.h264blklen;
		    	}
		    	else
		    	{
		    		// 最后一包
		    		//mh264blklenInThisPk = mh264Frmlen % h264blklen;
		    		//vencFrm_send.mh264blklenInThisPk = vencFrm_send.h264blklen;
		    	}

		    	sendbyte = FromUdpPkt_old(udppk, pk2send, vencFrm_send.h264blklen);

		    	UdpSend(address, port, udppk, sendbyte);
		    }
	//		    else if (mFecIdInGrp < FecPkNum_n)
		    else if (vencFrm_send.m264PkIdInGrp < vencFrm_send.OrgPkNum_N + vencFrm_send.FecPkNum_n)
		    {
		    	fecpk = GetFecPk(vencFrm_send.m264PkIdInGrp - vencFrm_send.OrgPkNum_N); // mFecIdInGrp++);

		    	if (fecpk != null)
		    	{
		    		sendbyte = FromUdpPkt_old(udppk, fecpk, vencFrm_send.h264blklen);
		    	}
		    	else
		    	{
		    		Log.i(TAG, "SendOnePkt(), can't get fec pk:" + (vencFrm_send.m264PkIdInGrp - vencFrm_send.OrgPkNum_N));
		    	}

		    	UdpSend(address, port, udppk, sendbyte);
		    }
			else
			{
				vencFrm_send.SendOut = 1;	// 指标编码器编码
			}



		    //UdpSend(InetAddress address, int port, byte data[], int datalen)

			return sendbyte;
		}
	*/
		//, int offset
		public void UdpSend(InetAddress address, int port, byte data[], int datalen) {
			//if (false)
			{
				try {
					//	            InetAddress address = InetAddress.getByName("192.168.1.80");  //服务器地址
					//	            int port = 8080;  //服务器的端口号
					//创建发送方的数据报信息
					DatagramPacket dataGramPacket = new DatagramPacket(data, datalen, address, port);

					//	            DatagramSocket socket = new DatagramSocket();  //创建套接字
					socket.send(dataGramPacket);  //通过套接字发送数据
					//	            socket.close();
					//	        } catch (UnknownHostException e) {
					//	            e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}

		// 设置cam源
		public void SetCamSrc(int ChooseCamIn)
		{
			if (ChooseCamIn == -1)
			{
				ChooseCam = 1 - ChooseCam;
			}
			else
			{
				ChooseCam = ChooseCamIn;
			}
		}

		// 指示编码器输出
		int IndicateEncoderWork(int counter)
		{
			//int nalcount[]  = new int[1];
			//int nalpos[]  = new int[100];


			if (usbcam.mdata == null)
			{
				Log.i(TAG, "video encode: usbcam not ready, waiting.");

				return 0;
			}

			if (vencFrm_send.SendOut == 0)
			{
				return 0;
			}

//			// test
//			//if (false)
//			{
//				if (GetCtlPara("FreezeVideo") > 0)
//	    		{
//					return 0;
//	    		}
//			}
			if (FreezeVideo > 0)
			{
				return 0;
			}

			long tick = System.currentTimeMillis();

			//synchronized(vencFrm_send.videoEncOutputData)
			{

				// test
				//appendMethodA("/mnt/sdcard/hwenc.yuv", usbcam.mdata, (int)(width * height * 1.5));


				int datacount = 0; // 视频编码器输出数据量
				int r = 0;
				//synchronized(vencFrm_send.videoEncOutputData)
				{
					//Log.e(TAG, "video encode: ChooseCam: " + ChooseCam);

					byte datain[];
					if (ChooseCam == 0)
					{
						if (usbcam != null)
						{
							datain = usbcam.mdata;
							if (datain != null)
							{
								datacount = vencFrm_send.Encode(m264FrmId, enc, datain, width, height, MaxPkSize_L, MinOrgPkNum_N, FecRatio);
							}
						}
					}
					else if (ChooseCam == 1)
					{
						if (nativeCam != null)
						{
							//synchronized(nativeCam.mdata2)
							synchronized(nativeCam.mdata2)
							{
								int swapUV = 0;
								//datain = GetSmallImgJni(nativeCam.mdata, nativeCam.width, nativeCam.height, 320, 240, swapUV);
								datain = nativeCam.mdata2;

								datacount = vencFrm_send.Encode(m264FrmId, enc, datain, width, height, MaxPkSize_L, MinOrgPkNum_N, FecRatio);
								nativeCam.ImgFetched = true;
							}
						}
					}

					if (datacount <= 0)
					{
						Log.e(TAG, "endcode error, frmid: " + m264FrmId);

						return 0;
					}

					//test xxxx
//					for(int ii = 0; ii < datacount; ii++)
//					{
//						vencFrm_send.videoEncOutputData[ii] = 1;
//					}

					//synchronized(vencFrm_send.videoEncOutputData)

//				Log.e(TAG, "endcode ok, frmid: " + m264FrmId
//					+ ", blklen: " + vencFrm_send.h264blklen
//					+ ", OrgPkNum_N: " + vencFrm_send.OrgPkNum_N
//					+ ", FecPkNum_n: " + vencFrm_send.FecPkNum_n
//					);

					m264FrmId++;

					r = FECEncode(
							m264FrmId,
							vencFrm_send.videoEncOutputData,
							datacount,
							vencFrm_send.h264blklen, //MaxPkSize_L,
							vencFrm_send.OrgPkNum_N,
							vencFrm_send.FecPkNum_n
					);
				}

				vencFrm_send.m264PkIdInGrp = 0;
				vencFrm_send.SendOut = 0; // 新编码数据, 等待发送

				long tick2 = System.currentTimeMillis();

				// 计算编码时长
				VideoEncTime = (int)(tick2 - tick);
				// 简单保护
				if (VideoEncTime < 0 || VideoEncTime > 1000)
				{
					VideoEncTime = 10; // ms
				}
				if (VideoEncTimeAvg == 0)
				{
					// init
					VideoEncTimeAvg = VideoEncTime;
				}
				else
				{
					// 权重分配
					VideoEncTimeAvg = (int)(VideoEncTimeAvg * 0.7 + VideoEncTime * 0.3);
				}

				// 编码后, 待发原包编码和fec包编码归0
				//			m264PkIdInGrp = 0;
				//			OrgPkNum_N = OrgPkNum_N;
				//			mFecIdInGrp = 0;
				//			FecPkNum_n = FecPkNum_n;


				// test
				//appendMethodA("/mnt/sdcard/hwenc.264", videoEncOutputData, datacount);

				// test
				/*
				if (datacount > 0)
				{
					int r = 0;
					for(int i = 0; i < nalcount[0]; i++)
					{
						dec.offerDecoder(nalpos[1 + i] - nalpos[i], videoEncOutputData, nalpos[i], null);
					}
				}
				*/

				FrmCount++; // 总帧数
				FrmNumInPeriod++;

				// 帧类型提取mFrmType
				//mFrmType = 0;
				if (vencFrm_send.mFrmType == 1)
				{
					// 包类型， 1：I， 2：p
					IFrmCount++;

					IFrmLastT = tick;	// 控制i帧间隔用

					// curve output
					//String s1 = String.valueOf(IFrmCount % 20);
					if (IntTestSwitch) IntTestOut("IFrNum", String.valueOf(IFrmCount % 20)); // CURVE_TYPE_NUMBER,
					String s2;
					//s2 = "IndicateEncoderWork(), Frm is I.";
					//IntPrint(3, s2); // ICTVOIP_LOG_DEBUG:3 ICTVOIP_LOG_ERROR:6
				}
				else if (vencFrm_send.mFrmType == 2)
				{
					// 包类型， 1：I， 2：p
					PtFrmCount++;

					// curve output
					//String s1 = String.valueOf(PtFrmCount % 20);
					if (IntTestSwitch) IntTestOut("PtFrNum", String.valueOf(PtFrmCount % 20)); // CURVE_TYPE_NUMBER,
				}

//				String IorP = "N";
//				if (vencFrm_send.mFrmType == 1)
//				{
//					IorP = "I";
//				}
//				if (vencFrm_send.mFrmType == 2)
//				{
//					IorP = "P";
//				}
//				Log.i(TAG, "(" + IorP + "), counter:" + counter + ", video encode: data len:" + datacount + ", nalcount:" + vencFrm_send.nalcount[0]);




				//	        int port = 50000;
				//	        if (datacount > 0)
				//	        {
				//	        	//UdpSend(address, port, videoEncOutputData, datacount);
				//	        }

				////////////////////////////////////////////////
				// test decode
//				if (false)
//				{
//					for(int i = 0; i < nalcount[0]; i++)
//				    {
//				    	byte videoEncOutputData_dup[] = vencFrm_send.videoEncOutputData;
//				    	int cutlen = 0;
//
//				    	//if (false)
//				    	{
//		//			    		videoEncOutputData_dup = new byte[100000];
//		//
//		//				    	if (nalcount[0] > 1)
//		//				    	{
//		//				    		cutlen = nalpos[nalcount[0] - 1];
//		//				    	}
//					    	System.arraycopy(vencFrm_send.videoEncOutputData, nalpos[i],
//					    			videoEncOutputData_dup, 0,
//					    			nalpos[i + 1] - nalpos[i]);
//				    	}
//
//				    	int mlost[] = new int[100];
//						r = VideoDecode(
//								vencFrm_send.mNalCount,
//								nalpos[i + 1] - nalpos[i],
//								videoEncOutputData_dup,
//								vencFrm_send.h264blklen,
//								vencFrm_send.OrgPkNum_N,
//								vencFrm_send.FecPkNum_n,
//								mlost,
//								0
//								);
//						if (r > 0)
//						{
//							// 解码成功
//							int z = 0;
//
//							int x = z;
//						}
//				    }
//
//				    // 模范重启编码器, IReq
//				    if (counter % 100 == 0)
//				    {
//				    	IndicateEncoderReset();
//				    }
//				}
//				if (false)
//				{
//				    {
//				    	byte videoEncOutputData_dup[] = vencFrm_send.videoEncOutputData;
//				    	int cutlen = 0;
//
//				    	if (false)
//				    	{
//				    		videoEncOutputData_dup = new byte[100000];
//
//					    	if (nalcount[0] > 1)
//					    	{
//					    		cutlen = nalpos[nalcount[0] - 1];
//					    	}
//					    	System.arraycopy(vencFrm_send.videoEncOutputData, cutlen,
//					    			videoEncOutputData_dup, 0,
//					    			vencFrm_send.h264blklen - cutlen);
//				    	}
//
//				    	int mlost[] = new int[100];
//						r = VideoDecode(
//								vencFrm_send.mNalCount,
//								vencFrm_send.mh264Frmlen - cutlen,
//								videoEncOutputData_dup,
//								vencFrm_send.h264blklen,
//								vencFrm_send.OrgPkNum_N,
//								vencFrm_send.FecPkNum_n,
//								mlost,
//								0
//								);
//						if (r > 0)
//						{
//							// 解码成功
//							int z = 0;
//
//							int x = z;
//						}
//				    }
//
//				    // 模范重启编码器, IReq
//				    if (counter % 100 == 0)
//				    {
//				    	IndicateEncoderReset();
//				    }
//				}
			}

			return 1;
		}

		// 指标编码器复位输出I帧
		int IndicateEncoderReset()
		{
			// 清延迟reset标记
			mDelay2Reset = 0;

			if (fps <= 0)
			{
				fps = 15;
			}

			if (DstSendBw <= 0)
			{
				DstSendBw = 30000;
			}

			//return 1; // test

			Log.i(TAG, "IndicateEncoderReset "
					+ ", width: " + width
					+ ", height: " + height
					+ ", fps: " + fps
					+ ", byterate: " + DstSendBw
			);

			if (enc != null)
			{
				enc.StartAvcHWEncoder(
						width,
						height,
						fps,
						DstSendBw * 8); //, javaclassStr_AvcHWEncoder);
			}
			else
			{
				//Soft264Init(320, 240, 1); // 1: X264_CSP_I420: 3:X264_CSP_NV12
				//Soft264setbitrateEncap(40000); // Bps
				IFrmCmd = 1;
			}

			String s2;
			s2 = "IndicateEncoderReset().";
			IntPrint(6, s2); // ICTVOIP_LOG_ERROR:6

			try {
				Thread.sleep(10); //0); // 10);
			} catch (InterruptedException e) {

				e.printStackTrace();
			}

			return 1;
		}


		int writefile(String FileName, byte data [], int datalen)
		{
			try {
				DataOutputStream fos = new DataOutputStream(
						new BufferedOutputStream(new FileOutputStream(FileName)));

				// 正式数据
				fos.write(data, 0, datalen);
				fos.close();

			} catch (Exception e) {
				e.printStackTrace();
			}

			return 1;
		}

		/**
		 * A方法追加文件：使用RandomAccessFile
		 */
		public void appendMethodA(String fileName, byte data [], int datalen) {
			try {
				// 打开一个随机访问文件流，按读写方式
				RandomAccessFile randomFile = new RandomAccessFile(fileName, "rw");
				// 文件长度，字节数
				long fileLength = randomFile.length();
				//将写文件指针移到文件尾。
				randomFile.seek(fileLength);
				//randomFile.writeBytes(data, datalen);
				randomFile.write(data, 0, datalen);
				randomFile.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		/**
		 * B方法追加文件：使用FileWriter
		 */
		public void appendMethodB(String fileName, char data [], int datalen) {
			try {
				//打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
				FileWriter writer = new FileWriter(fileName, true);
				writer.write(data, 0, datalen);
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		int GetUsTickDiff(int t1, int t0)
		{
			int t00 = t0 & 0xFFFF;
			int t11 = t1 & 0xFFFF;

			if (t11 >= t00)
			{
				return t11 - t00;
			}
			else
			{
				return 0xFFFF - (t00 - t11);
			}
		}

		//一帧图压缩后的数据， 仅含图像压缩数据和冗余数据
		class  OneFrm
		{
			int h264pknum;
			int fecpknum;

			int CurPkId;	// 当前处理包号

			List<byte[]> PkList = new ArrayList<byte[]>();
			//			List<short> PkCrcList = new ArrayList<short>();
		};

	}


	//////////////////////////////////////////
	//
	public class DecCtl
	{

		private ImageView mImag;
		private int width = 0; //WIDTH;
		private int height = 0; //HEIGHT;
		private byte[] mdata;
		private int GotData = 0;
		private Handler mHandler;
		public Button mcap;
		private Bitmap bitmap;
		//private Bitmap bmp;
		//private Bitmap decbmp;
		private int[] rgb;
		private int numbuf  = 4;
		Context mContext;
		public boolean mRuning0 = true;
		public boolean mRuning = true;

		//int ActRcvBw = 0;			// 当前实际接收带宽
		//int mRcvByte = 0;
		int mRcvByteInPeriod = 0;
		int ActLostRatio = 0;		// 当前实际接收丢包率
		int ActLostBurst = 0;		// 当前实际接收连续丢包
		int BwCtl = 0;				// 设置源端码率

		int ActRcvFps = 0;			// 接收帧率
		int ActRcvFpsInPeriod = 0;
		int ActDecodeVFrmNum = 0;			// 264解码帧率
		int ActDecodeFpsInPeriod = 0;
		int ActFecDecCount = 0;			// FEC解码次数


		byte IFrmReq = 1;			// 向源端请求I帧

		// TTL测试
		// TTLSor不可以重叠测试, 如果TTL_Sor == -1才可以
		long TTL_Sor = -1;			// 记录源端发过来的测量起始时刻(源端时间轴)
		long TTL_Local = 0;			// 收到源端消息时本端对应的时间(本端时间轴)

		AvcHWDecoder dec = null;

		EncCtl encctl = null;

		private int mOutofDateFrmId = -1; 		// 视频, 已经无用的包ID, <=此包可丢弃
		Map <Integer, COne264Frm>m264FrmMap = new HashMap<Integer, COne264Frm>();		// 帧接收线程用
		List <COne264Frm> m264FrmDecingList = new ArrayList<COne264Frm>();

		private int mOutofDateFrmId_Audio = -1; // 音频, 已经无用的包ID, <=此包可丢弃
		Map <Integer, COneAudioFrm>mAudioFrmMap = new HashMap<Integer, COneAudioFrm>();		// 帧接收线程用
		List <COneAudioFrm> mAudioFrmDecingList = new ArrayList<COneAudioFrm>();

		// Stats.
		int mLastSN = -1;
		long mLostPkNum = 0;
		int mLostPkNumInPeriod = 0;
		//int mLostPkRatio = 0;
		//COne264Frm
		int mAckFrmId = -1;
		long mBadCrcPkNum = 0;
		long mSorSendByte = 0;		// 源端发送总字节数
		long mSorSendPks = 0;		// 源端发送总包数
		long mSorSendPksSNRewindTime = 0; // 源端SN回滚资数
		long mRcvByte = 0;			// 收端接收总字节数
		long mRcvPks = 0;			// 收端接收总包数
		int  mCurJitter = 0;

		StartUdpRcvThread udpthread = null;
		StartDispThread dispThread = null;
		DecodeThread decThread = null;

		// 网络仿真
		SimNet simnet;

		GLFrameRenderer mGLFRenderer;

		// devnameIn: /dev/video9
		public DecCtl(Context ctx, ImageView Imag, int w, int h
				,GLFrameRenderer GLFRenderer)
		{
			mContext = ctx;

			width = w;
			height = h;

			mGLFRenderer = GLFRenderer;

			mImag = Imag;

			OnCreate();

			//
//			int framerate = 15;
//			int bitrate = 20000;
			SurfaceView sv = null;
			Surface s = null;
//			s = sv.getHolder();
//			s = sv.gets
			//dec = new AvcHWDecoder(width, height, s);

			// 网络仿真
			simnet = new SimNet(udpthread);
		}

		public void SetEncCtl(EncCtl encctlIn)
		{
			encctl = encctlIn;
		}

		private void OnCreate()
		{
			int ret = 0;

			mHandler = new Handler();
			dispThread = new StartDispThread();
			dispThread.setPriority(7);

			udpthread = new StartUdpRcvThread();
			udpthread.setPriority(8); //Thread.MAX_PRIORITY);

			new BStatThread().start();
			decThread = new DecodeThread();
			decThread.setPriority(7);

			//dispThread.start();
			//decThread.start();
			//mHandler.post(mUpdateUI);
			//udpthread.start();
		}

		public void Go()
		{
			dispThread.start();
			decThread.start();
			mHandler.post(mUpdateUI);
			udpthread.start();
		}

		public void Release()
		{
		}

		//public int udprcv(int port)

		// 一帧Audio压缩数据所有的包(含fec)
		public class COneAudioFrm
		{
			int mFrmId = 0;
			//int mFrmType = 0; // (I1,P2)
			//int mNalCount = 0;
			final int MAX_PKS_LEN_ONE_FRM = 1000;
			byte mpks[]; // = new byte[MAX_PKS_LEN_ONE_FRM];	// 一帧图的所有数据(含fec)
			//int mh264Frmlen = 0;		// 264编码数据长度, 不含fec
			int mh264blklen = 0;		// 块数据长度(一个包中)
			int mOrgPkNum_N = 0;		// 当前视频编码包数
			int mFecPkNum_n = 0;		// FEC组内包数
			int mpksnumgot = 0;
			//final int MAX_PKS_ONE_FRM = 100; //
			int mlost[];// = new int[MAX_PKS_ONE_FRM];	// 包丢失标记， 最多100包
			int mlostCount = 0;
			int mOrgPkCountRcv = 0;		// 接收到原数据包数量,  如果收齐可以不用FEC解码

			int mDecodeing = 0;			// 已经开始解码标记
			int mMayDecodeing = 0;		// 通知解码线程可以开始解码

			public COneAudioFrm()
			{

			}

			public int SetPara(int FrmId, int h264blklen, int OrgPkNum_N, int FecPkNum_n)
			{
				mFrmId = FrmId;
				mh264blklen = h264blklen;		// 一块264长度， 最后一块可能较小
				mOrgPkNum_N = OrgPkNum_N;
				mFecPkNum_n = FecPkNum_n;

				if (mOrgPkNum_N <= 0)
				{
					return 0;
				}

//				mh264Frmlen = (mOrgPkNum_N + mFecPkNum_n) * mdata264size;

				mpks = new byte[(mOrgPkNum_N + mFecPkNum_n) * h264blklen]; // xxxx
				mlost = new int[mOrgPkNum_N + mFecPkNum_n];

				// 全丢失
				for(int i = 0; i < mOrgPkNum_N + mFecPkNum_n; i++)
				{
					mlost[i] = 1;
				}
				mlostCount = mOrgPkNum_N + mFecPkNum_n;

				return 1;
			}

			// 检测是否足够完整，可以送解码
			public int CheckIntegral()
			{
				if (mOrgPkCountRcv == mOrgPkNum_N)
				{
					// 原始数据齐, 不用fec解码
					return 1;
				}

				if (mlostCount <= mFecPkNum_n)
				{
					// 原始数据不齐, 但fec解码可恢复
					return 2;
				}

				// 数据不齐
				return 0;
			}

			// 接收一个包（原始数据包或FEC包）
			public int SaveOnePk(int h264PkIdInGrp, byte pk[], int offset, int datalen)
			{
				// 保护
				if (h264PkIdInGrp >= mOrgPkNum_N + mFecPkNum_n)
				{
					return 0;
				}

				// 最后一个原始包有效数据大小可以不同
				if (mh264blklen != datalen && h264PkIdInGrp == mOrgPkNum_N - 1)
				{
					return 0;
				}

				// 重复接收包
				if (mlost[h264PkIdInGrp] == 0)
				{
					return 1;
				}

				if (h264PkIdInGrp < mOrgPkNum_N)
				{
					mOrgPkCountRcv++;
				}

				// 清丢失标记
				mlost[h264PkIdInGrp] = 0;
				mlostCount--;

				// 保存内容
				System.arraycopy(pk, offset,
						mpks, h264PkIdInGrp * mh264blklen,
						mh264blklen); //MaxPkSize_L);

				return 1;
			}
		}

		// 一帧264数据所有的包(含fec)
		public class COne264Frm
		{
			int mFrmId = 0;
			int mFrmType = 0; // (I1,P2)
			int mNalCount = 0;
			final int MAX_PKS_LEN_ONE_FRM = 100000;
			byte mpks[]; // = new byte[MAX_PKS_LEN_ONE_FRM];	// 一帧图的所有数据(含fec)
			int mh264Frmlen = 0;				// 264编码数据长度, 不含fec
			int mh264blklen = 0;		// 块数据长度(一个包中)
			int mOrgPkNum_N = 0;		// 当前视频编码包数
			int mFecPkNum_n = 0;		// FEC组内包数
			int mpksnumgot = 0;
			final int MAX_PKS_ONE_FRM = 100;
			int mlost[];// = new int[MAX_PKS_ONE_FRM];	// 包丢失标记， 最多100包
			int mlostCount = 0;
			int mOrgPkCountRcv = 0;		// 接收到原数据包数量,  如果收齐可以不用FEC解码

			int mDecodeing = 0;			// 已经开始解码标记
			int mMayDecodeing = 0;		// 通知解码线程可以开始解码

			public COne264Frm()
			{

			}

			public int SetPara(int FrmId, int FrmType, int NalCount, int h264Frmlen, int h264blklen, int OrgPkNum_N, int FecPkNum_n)
			{
				mFrmId = FrmId;
				mFrmType = FrmType; // I 1, P 2
				mNalCount = NalCount;
				mh264Frmlen = h264Frmlen;		// 整帧264编码数据长度
				mh264blklen = h264blklen;		// 一块264长度， 最后一块可能较小
				mOrgPkNum_N = OrgPkNum_N;
				mFecPkNum_n = FecPkNum_n;

				if (mOrgPkNum_N <= 0)
				{
					return 0;
				}

//				mh264Frmlen = (mOrgPkNum_N + mFecPkNum_n) * mdata264size;

				mpks = new byte[(mOrgPkNum_N + mFecPkNum_n) * h264blklen];
				mlost = new int[mOrgPkNum_N + mFecPkNum_n];

				// 全丢失
				for(int i = 0; i < mOrgPkNum_N + mFecPkNum_n; i++)
				{
					mlost[i] = 1;
				}
				mlostCount = mOrgPkNum_N + mFecPkNum_n;

				return 1;
			}

//			// 内存检测, 不通过不能进行addonepk()操作
//			public int CheckSize()
//			{
//				if ((mOrgPkNum_N + mFecPkNum_n) * mdata264size > MAX_PKS_LEN_ONE_FRM)
//				{
//					return 0;
//				}
//
//				return 1;
//			}

			// 检测是否足够完整，可以送解码
			public int CheckIntegral()
			{
				if (mOrgPkCountRcv == mOrgPkNum_N)
				{
					// 原始数据齐, 不用fec解码
					return 1;
				}

				if (mlostCount <= mFecPkNum_n)
				{
					// 原始数据不齐, 但fec解码可恢复
					return 2;
				}

				// 数据不齐
				return 0;
			}

			// 接收一个包（原始数据包或FEC包）
			public int SaveOnePk(int h264PkIdInGrp, byte pk[], int offset, int datalen)
			{
				// 保护
				if (h264PkIdInGrp >= mOrgPkNum_N + mFecPkNum_n)
				{
					return 0;
				}

//				if (h264PkIdInGrp >= mOrgPkNum_N)
//				{
//					int z = 0;
//					int x = z;
//				}

				// 最后一个原始包有效数据大小可以不同
				if (mh264blklen != datalen && h264PkIdInGrp == mOrgPkNum_N - 1)
				{
					return 0;
				}

				// 重复接收包
				if (mlost[h264PkIdInGrp] == 0)
				{
					return 1;
				}

				if (h264PkIdInGrp < mOrgPkNum_N)
				{
					mOrgPkCountRcv++;
				}

				// 清丢失标记
				mlost[h264PkIdInGrp] = 0;
				mlostCount--;

				// 保存内容
				System.arraycopy(pk, offset,
						mpks, h264PkIdInGrp * mh264blklen,
						mh264blklen); //MaxPkSize_L);

				return 1;
			}
		}

		class CJitter
		{
			// 基准包
			long baseT0 = 0;	// 源端发包时刻
			long baseR0 = 0;	// 本端收到此包的本机时刻
			long jitter = 0;	// ms

			long lastResetT = 0;
			final long RESETTIME = 120000; // 2分钟复位，解决时钟漂移问题

			public CJitter()
			{
			}

			void updateBase(long t0, long r0)
			{
				baseT0 = t0;
				baseR0 = r0;
			}

			int GetJitter(long t0, long r0)
			{
				int jt = 0;

				// 初始化
				if (baseT0 == 0)
				{
					lastResetT = t0;

					updateBase(t0, r0);

					return 0;
				}

				// 长时复位
				if (t0 - lastResetT >= RESETTIME)
				{
					lastResetT = t0;

					updateBase(t0, r0);

					return 0;
				}

				int r = (int)GetUiTickDiffSign((int)r0, (int)baseR0);

				int t = (int)GetUiTickDiffSign((int)t0, (int)baseT0);

				jt = r - t;

				// 更新参考包
				if (jt < 0)
				{
					updateBase(t0, r0);
				}

				jitter = jt;

				return jt;
			}
		}

		class SimNet
		{
			int mBWSet = 300000;			// BPS

			int mBWAct = 0;				// 实际带宽统计
			float mLostRatio_Pk = 0;		// 实际丢包率统计
			int mT = 400;				// ms
			int mByteRcvPeriod = 0;
			int mByteMaxPeriod = 0;

			//int mByteLostPeriod = 0;

			long lastTtick = System.currentTimeMillis();

			List<byte[]> PkList = new ArrayList<byte[]>();
			int mBufSize = 0;
			byte mBuf[];
			int mBufCurSize = 0;
			int mBufPkCount = 0;
			int mBufPos[] = new int[1000];

			StartUdpRcvThread mDec = null;

			boolean mRuning = true;

			public SimNet(StartUdpRcvThread dec)
			{
				mDec = dec;
				new DidaThread().start();

				// test
				//SetLostR(40);
				//SetLostR(100);
			}

			int pklost_cycle_ctl = 0;
			int pklostrate_1000_ctl = 0;	// 设置丢包率, 千分之x
			int pklost_or_bytelost = 0;		// 0:丢比特率模式

			long byte_sum = 0;
			long bytelost_sum = 0;
			int concatenate_lostpk_ctl = 0;

			int discarddata_in_byte_total = 0;
			int discarddata_in_pk_total = 0;
			int discarddata_in_pk_period = 0;

			int transmitdata_in_pk = 0;        // 统计周期内总的转发的包数


			int concatenate_lostpk = 0;
			int pklostnum = 0;
			int bytelost_overdiscardbyte = 0;
			int pklost_cycle_counter = 0;

			// 千分之x
			public void SetLostR(int lostR)
			{
				pklostrate_1000_ctl = lostR;
				bytelost_sum = 0;
				byte_sum = 0;
			}

			public void SetBW(int bw)
			{
				mBWSet = bw;

				mByteMaxPeriod = mBWSet * mT / 1000;
			}

			public void SetBufSize(int size)
			{
				mBufSize = size;
				mBuf = new byte[mBufSize];
			}

			public void SetT(int t)
			{
				mT = t;

				mByteMaxPeriod = mBWSet * mT / 1000;
			}

			// 版本2，修改连续丢包控制
			// layer: 0: 前景包，1: 背景包
			int __sim_pk_lost(int len) // 不需要定时器处理
			{
				long left, right;
				int discardflag = 0;

				if (pklostrate_1000_ctl == 0)
				{
					return 0;
				}

				// 参数保护
				if (pklost_cycle_ctl < 1000)
				{
					pklost_cycle_ctl = 1000;
				}
				if (pklostrate_1000_ctl > 500)
				{
					pklostrate_1000_ctl = 499;
				}

				if (pklost_or_bytelost == 0)
				{
					// 丢比特率模式

					// 保护
					if (byte_sum >= 0xFFFFFFFFL - 100000)
					{
						byte_sum = 0;
						bytelost_sum = 0;
					}

					if (concatenate_lostpk_ctl == 0)
					{
						// 随机丢比特;

						if (pklostrate_1000_ctl > 0 && pklost_cycle_ctl > 0)
						{
							left = 1000L * bytelost_sum;
							right = (long)byte_sum * pklostrate_1000_ctl;
							//left = 1000L * (wgrand() % pklost_cycle_ctl);
							//right = (1000L - pklostrate_1000_ctl) * pklost_cycle_ctl;
							if (left <= right)
							{
								//if ((wgrand() % 3) == 0)		// 加入一定的随机性
								{
									bytelost_sum += len;

									discardflag = 1;
								}
							}

							byte_sum += len;
						}
						else
						{
							; // 丢包率为0, 不丢包。
						}
					}
					else
					{
						// 如果在丢包处理中
						// 连续丢包数没达到，丢
						// 否则不丢
						// 在非丢包处理中

						if ( concatenate_lostpk < concatenate_lostpk_ctl)
						{
							// 连续丢包未达到预设值，继续丢包

							concatenate_lostpk++;
							pklostnum++;

							bytelost_sum += len;

							discardflag = 1;
						}
						else if ( concatenate_lostpk >= concatenate_lostpk_ctl)
						{
							// 本包不丢
							concatenate_lostpk++;

							//left = 1000L * pklostnum;
							//right = (u32)pklost_cycle_counter * pklostrate_1000_ctl;
							left = 1000L * bytelost_sum;
							right = (long)byte_sum * pklostrate_1000_ctl;
							if (left <= right)
							{
								// 如果执行丢包率达到预设丢包率，开始新一轮控制

								concatenate_lostpk = 0;
							}
						}

						byte_sum += len;
					}
				}
				else
				{
					// 丢包率模式

					if (concatenate_lostpk_ctl == 0)
					{
						// 随机丢包;

						if (pklostrate_1000_ctl > 0 && pklost_cycle_ctl > 0)
						{
							left = (long) (1000L * (Math.random() % pklost_cycle_ctl));
							right = (1000L - pklostrate_1000_ctl) * pklost_cycle_ctl;
							if (left >= right)
							{
								if (bytelost_overdiscardbyte >= len)
								{
									// 上轮丢多了字节，本轮还
									bytelost_overdiscardbyte -= len;
								}
								else
								{
									//
								}

								discardflag = 1;
							}
						}
						else
						{
							; // 丢包率为0, 不丢包。
						}
					}
					else
					{
						// 如果在丢包处理中
						// 连续丢包数没达到，丢
						// 否则不丢
						// 在非丢包处理中

						if (pklost_cycle_counter < 0x1FFFFF)
						{
							pklost_cycle_counter++; // 借用为总包数
						}
						else
						{
							// 保护
							pklost_cycle_counter = 0;
							pklostnum = 0;
							concatenate_lostpk = 0;
						}

						if ( concatenate_lostpk < concatenate_lostpk_ctl)
						{
							// 连续丢包未达到预设值，继续丢包

							concatenate_lostpk++;
							pklostnum++;

							discardflag = 1;
						}
						else if ( concatenate_lostpk >= concatenate_lostpk_ctl)
						{
							// 本包不丢
							concatenate_lostpk++;

							left = 1000L * pklostnum;
							right = (long)pklost_cycle_counter * pklostrate_1000_ctl;
							if (left <= right)
							{
								// 如果执行丢包率达到预设丢包率，开始新一轮控制

								concatenate_lostpk = 0;

								//br->ict_brctl_tmp[portno].pklostnum = 0;           // 无意义
								//br->ict_brctl[portno].pklost_cycle_counter = 0;    // 无意义
							}
						}
					}
				}

				if (discardflag > 0)
				{
					// 统计丢包

					discarddata_in_byte_total += len;
					discarddata_in_pk_total++;

					discarddata_in_pk_period++;

//					if (filtermod == STAT_ALL_LAYER)
//					{
//						// 统计不区分前、背景
//						discarddata_in_byte_total += skb->len;
//						discarddata_in_pk_total++;
//					}
//					else if (filtermod == STAT_ONlY_LAY0)
//					{
//						if (layer == 0)
//						{
//							// 统计丢包
//							discarddata_in_byte_total += skb->len;
//							discarddata_in_pk_total++;
//						}
//					}
//					else if (filtermod == STAT_ALL_LAYER_SEP)
//					{
//						// 统计区分前、背景
//
//						if (layer == 0)
//						{
//							// 统计丢包
//							discarddata_in_byte_total += skb->len;
//							discarddata_in_pk_total++;
//						}
//						else
//						{
//							discarddata_in_byte_total_bg += skb->len;
//							br->ict_brctl[channel][portno].discarddata_in_pk_total_bg++;
//						}
//					}

					//kfree_skb(skb);


					return 1;
				}

				// 未限制本包，继续转发处理
				return 0;
			}


			public void PkIn(byte in[], int len)
			{
				// 带宽逻辑

				int r;

				mByteRcvPeriod += len + 28; // IP HDR LEN

				// 丢包逻辑
				r = __sim_pk_lost(len);
				if (r > 0)
				{
					// 丢弃

					return;
				}

				// 带宽控制逻辑

				transmitdata_in_pk++;

				mDec.ProccessUdpPkt(in, len);
			}

			private void PkOut(byte in[])
			{

			}


			class DidaThread extends Thread {

				int r = 0;
				//int mRuning = 1;

				@Override
				public void run() {
					// TODO Auto-generated method stub
					//super.run();
					while(mRuning) {

						try {
							Thread.sleep(1); // 10);
						} catch (InterruptedException e) {

							e.printStackTrace();
						}

						long tick = System.currentTimeMillis();

						// 周期统计
						if (tick - lastTtick >= mT)
						{
							mBWAct = (int)(mByteRcvPeriod / (tick - lastTtick));

							//mLostRatio_Pk = (int)(discarddata_in_pk_period / (tick - lastTtick));
							mLostRatio_Pk = 0;
							if (transmitdata_in_pk > 0)
							{
								mLostRatio_Pk = ((float)discarddata_in_pk_period * 100 / transmitdata_in_pk);
								mLostRatio_Pk = (float)(((int)mLostRatio_Pk) * 1000) / 1000; // 保留一位小数
							}

							lastTtick = tick;

							// curve output
							//String s1 = String.valueOf((int)mLostRatio_Pk);
							if (IntTestSwitch) IntTestOut("ActLostR_PK", String.valueOf((int)mLostRatio_Pk)); // CURVE_TYPE_NUMBER,


							mByteRcvPeriod = 0;
							discarddata_in_pk_period = 0;
							transmitdata_in_pk = 0;
						}

					}
				}
			}
		}

		// udp接收线程
		public class StartUdpRcvThread extends Thread
		{
			InetAddress address;
			DatagramSocket socket = null;
			byte[] udpbuf = new byte[2048];  //定义byte数组
			byte[] opusbuf = new byte[1024];  //定义byte数组
			DatagramPacket packet = null;
			int UdpRcvPort = 50000;		// cfg

			CJitter jitter = new CJitter();


			//int recentPkNum

			void setushort(byte by[], int offset, int value)
			{
				by[offset] 		= (byte)(value & 0xFF);
				by[offset + 1] 	= (byte)((value >> 8) & 0xFF);
			}

			//
			int GetItemAB(byte Udpdata[], int offset, long tick)
			{
				int i = offset; // offset: item V的开始位置

				// 反馈: 本端B向A
				// TTL测量, 对端A发起TTL测量, 本端B记录A端时刻和本端收到消息的时刻
				if (TTL_Sor == -1)
				{
					//TTL_Sor = getushort(Udpdata, i); // & 0xFFFF;
					//i += 2;
					TTL_Sor = getint(Udpdata, i); // & 0xFFFF;
					i += 4;
					//TTL_Local = (int)((short)tick & 0xFFFF);
					//TTL_Local = (int)(tick & 0xFFFF);
					TTL_Local = tick;
				}
				else
				{
					i += 4;
				}

				return i - offset;
			}

			//
			int GetItemBA(byte Udpdata[], int offset, int ItemDataLen, long tick)
			{
				int i = offset; // offset: item V的开始位置

				// 反馈: 本端B向A
				{
					/*
					encctl.PeerIFrmReq = getushort(Udpdata, i);			// I帧请求
					i += 2;
					encctl.PeerBwReq = getushort(Udpdata, i);
					i += 2;
					encctl.PeerLostRatio = getushort(Udpdata, i);
					i += 2;
					encctl.PeerLostBurst = getushort(Udpdata, i);
					i += 2;
					*/
					encctl.PeerIFrmReq = Udpdata[i];			// I帧请求
					i++;

					encctl.PeerBwReq = getushort(Udpdata, i) * 100;
					i += 2;
					if (BwSelfAdaptSwitch > 0)
					{
						// 带宽自适应
						if (encctl.PeerBwReq > 0)
						{
							DstSendBw = encctl.PeerBwReq;
						}
					}

					encctl.PeerLostRatio = Udpdata[i];
					i++;
					encctl.PeerLostBurst = Udpdata[i];
					i++;

					encctl.PeerAckFrmId = getushort(Udpdata, i);
					i += 2;

					// ttl测量结果
					int ttl_est = getushort(Udpdata, i) & 0xFFFF;
					if (ttl_est > 0)
					{
						int t = (int)(tick & 0xFFFF);
						encctl.TTL_Result = GetUsTickDiff((t), ttl_est);

						// curve output
						//String s1 = String.valueOf((int)encctl.TTL_Result);
						if (IntTestSwitch) IntTestOut("ttl", String.valueOf((int)encctl.TTL_Result)); // CURVE_TYPE_NUMBER,
					}
					i += 2;

					//Log.e(TAG, "rcv udp pks, PeerBwReq: " + encctl.PeerBwReq
					//		+ ", PeerLostRatio: " + encctl.PeerLostRatio);
				}

				return i - offset;
			}


			//
			int GetAudioFECData(byte Udpdata[], int offset, int ItemDataLen, int tsSor, int tsSink)
			{
				/*
				int fecgrpid;
				int m264PkIdInGrp;
				int OrgPkNum_N;
				int FecPkNum_n;
				int Frmlen;
				int blk264Pos = 0;

				int i = offset; // offset: item V的开始位置

				Frmlen = getushort(Udpdata, i);			//
				i += 2;
				fecgrpid = (int)(getushort(Udpdata, i) & 0xFFFF);
				i += 2;
				m264PkIdInGrp = (int)(Udpdata[i] & 0xFFFF);
				i++;
				OrgPkNum_N = Udpdata[i];
				i++;
				FecPkNum_n = Udpdata[i];
				i++;

				blk264Pos = i;
				*/

				System.arraycopy(Udpdata, offset,
						opusbuf, 0,
						ItemDataLen); //MaxPkSize_L);

				PutOpusFECData(ItemDataLen, opusbuf, tsSor, tsSink);

				return 1;
			}

			//
			int GetAudioData(byte Udpdata[], int offset, int ItemDataLen, int tsSor, int tsSink)
			{
//				int mAudioFrmId;
//				int m264PkIdInGrp;
//				int OrgPkNum_N;
//				int FecPkNum_n;
//				int Frmlen;
//				int blk264Pos = 0;

				/*
				int i = offset; // offset: item V的开始位置

				Frmlen = getushort(Udpdata, i);			//
				i += 2;
				mAudioFrmId = (int)(getushort(Udpdata, i) & 0xFFFF);
				i += 2;
				m264PkIdInGrp = (int)(Udpdata[i] & 0xFFFF);
				i++;
				OrgPkNum_N = Udpdata[i];
				i++;
				FecPkNum_n = Udpdata[i];
				i++;

				blk264Pos = i;
				*/

				System.arraycopy(Udpdata, offset,
						opusbuf, 0,
						ItemDataLen); //MaxPkSize_L);

				PutOpusData(ItemDataLen, opusbuf, tsSor, tsSink);

				// test
				//if (m264PkIdInGrp == OrgPkNum_N - 1)
				//{
				//	Log.i(TAG, "xxx----- m264PkIdInGrp == OrgPkNum_N - 1: " + m264PkIdInGrp);
				//}

//				/*
//				map, key is frmid, value is data
//				如果包已太晚到达, 丢弃
//				不断扫描所有成员
//				//如果成员已经收齐, 打上收齐标记
//				如果有帧正在接收
//				  如果帧满足解码条件, 打收齐标记, 送解码, 刷新解码帧号
//				  如果不满足
//				    如果正在接收的帧数超过1帧, 丢弃最旧正在接收帧
//				      从头开始删除剩余下帧中的P帧, 直到I帧时终止, 刷新解码帧号;如果没有I帧, 发I帧请求
//				*/
//				//if (false)
//				{
//					// 包晚到, 丢弃
//					//if (isFrmTooOld(m264FrmId))
//					if (GetUsTickDiff(mAudioFrmId, mOutofDateFrmId_Audio) <= 0)
//					{
////						Log.i(TAG, "audio pks too old, discard"
////			            		+ ", mSN:" + mSN);
//
//						return 0;
//					}
//
//					// 找到frm或创建新的frm(存于m264FrmMap中)
//					COneAudioFrm frm = null;
//					Map <Integer, COneAudioFrm>h264FrmMap = mAudioFrmMap;
//					synchronized(mAudioFrmMap)
//					{
//						// 保存包
//
//						frm = mAudioFrmMap.get(mAudioFrmId);
//						if (frm == null)
//						{
//							frm = new COneAudioFrm();
//							frm.SetPara(mAudioFrmId, mh264blklenInThisPk, OrgPkNum_N, FecPkNum_n);
//							mAudioFrmMap.put(m264PkIdInGrp, frm);
//						}
//
//						// 本帧已经在解码了, 本包晚到, 丢弃
//						if (frm.mDecodeing > 0)
//						{
//							return 0;
//						}
//
//						// 保存本包
//						frm.SaveOnePk(m264PkIdInGrp, Udpdata, blk264Pos, ItemDataLen - (blk264Pos - offset));
//
//						// 检测帧完整性, 送解码
//						//if (frm.CheckIntegral() > 0)
//						//{
//							//frm.mMayDecodeing = 1;
//							//frm.mDecodeing = 1;
//							//synchronized(m264FrmDecingList)
//							//{
//							//	m264FrmDecingList.add(frm);
//							//}
//						//}
//
//						/////////////////////////////////////
//						SelectFrmAndDecode(h264FrmMap);
//					}
//				}

				return 1; //i - offset;
			}

			//
			int GetVideoData(int Sn, int jt, byte Udpdata[], int offset, int ItemDataLen)
			{
				int mFrmType;			// 包类型， 1：I， 2：p
				int mNalCount;			// nal数量
				int mh264Frmlen;		// 264编码输出帧大小
				int mh264blklenInThisPk;	// 数据块大小
				int m264FrmId;
				int m264PkIdInGrp;
//				int mFecIdInGrp;
				int OrgPkNum_N;
				int FecPkNum_n;
				int blk264Pos;

				int i = offset; // offset: item V的开始位置

				//mFrmType = getushort(Udpdata, i);
				//i += 2;
				mFrmType = Udpdata[i];
				if (mFrmType != 1 && mFrmType != 2) // I 1, P 2
				{
					//Log.i(TAG, "pks type error, I 1, P 2, this frm is :" + mSN);
					Log.i(TAG, "pks type error, I 1, P 2, mFrmType :" + mFrmType);

					return 0;
				}

				i++;
				mNalCount = Udpdata[i];
				i++;

				mh264Frmlen = getushort(Udpdata, i);			//
				i += 2;
				mh264blklenInThisPk = getushort(Udpdata, i);
				i += 2;
				m264FrmId = getushort(Udpdata, i) & 0xFFFF;
				i += 2;
				m264PkIdInGrp = getushort(Udpdata, i);
				i += 2;
//				mFecIdInGrp = getushort(Udpdata, i);
//				i += 2;

//				OrgPkNum_N = getushort(Udpdata, i);
//				i += 2;
//				FecPkNum_n = getushort(Udpdata, i);
//				i += 2;
				OrgPkNum_N = Udpdata[i];
				i++;
				FecPkNum_n = Udpdata[i];
				i++;

				blk264Pos = i;

				// test
				//if (m264PkIdInGrp == OrgPkNum_N - 1)
				//{
				//	Log.i(TAG, "xxx----- m264PkIdInGrp == OrgPkNum_N - 1: " + m264PkIdInGrp);
				//}

				// important printf
				// 264data: Udpdata + i;
//				Log.w(TAG, "rcv udp pks, video"
//	            		+ ", mSN:" + Sn
//	            		+ ", mFrmType(I1,P2):" + mFrmType
//	            		+ ", mNalCount:" + mNalCount
//	            		+ ", m264FrmId:" + m264FrmId
//	            		+ ", m264PkIdInGrp:" + m264PkIdInGrp
//	            		+ ", OrgPkNum_N:" + OrgPkNum_N
//	            		+ ", FecPkNum_n:" + FecPkNum_n
//	            		+ ", TTL_Sor:" + TTL_Sor
//	            		+ ", TTL_Local:" + TTL_Local
//	            		+ ", encctl.TTL_Result:" + encctl.TTL_Result
//	            		+ ", jt: " + jt
//	            		);

				/*
				map, key is frmid, value is data
				如果包已太晚到达, 丢弃
				不断扫描所有成员
				//如果成员已经收齐, 打上收齐标记
				如果有帧正在接收
				  如果帧满足解码条件, 打收齐标记, 送解码, 刷新解码帧号
				  如果不满足
				    如果正在接收的帧数超过1帧, 丢弃最旧正在接收帧
				      从头开始删除剩余下帧中的P帧, 直到I帧时终止, 刷新解码帧号;如果没有I帧, 发I帧请求
				*/
				//if (false)
				{
					// 包晚到, 丢弃
					//if (isFrmTooOld(m264FrmId))
					if (GetUsTickDiff(m264FrmId, mOutofDateFrmId) <= 0)
					{
//						Log.i(TAG, "pks too old, discard"
//			            		+ ", mSN:" + mSN);

						return 0;
					}

					// 找到frm或创建新的frm(存于m264FrmMap中)
					COne264Frm frm = null;
					Map <Integer, COne264Frm>h264FrmMap = m264FrmMap;
					synchronized(m264FrmMap)
					{
						// 保存包

						frm = m264FrmMap.get(m264FrmId);
						if (frm == null)
						{
							frm = new COne264Frm();
							frm.SetPara(m264FrmId, mFrmType, mNalCount, mh264Frmlen, mh264blklenInThisPk, OrgPkNum_N, FecPkNum_n);
							m264FrmMap.put(m264FrmId, frm);
						}

						// 本帧已经在解码了, 本包晚到, 丢弃
						if (frm.mDecodeing > 0)
						{
							return 0;
						}

						// 保存本包
						frm.SaveOnePk(m264PkIdInGrp, Udpdata, blk264Pos, ItemDataLen - (blk264Pos - offset));

						// 检测帧完整性, 送解码
						//if (frm.CheckIntegral() > 0)
						//{
						//frm.mMayDecodeing = 1;
						//frm.mDecodeing = 1;
						//synchronized(m264FrmDecingList)
						//{
						//	m264FrmDecingList.add(frm);
						//}
						//}

						/////////////////////////////////////
						SelectFrmAndDecode(h264FrmMap);
					}
				}

				//if (mFrmType == 1)
				//{
				// I
				//IFrmReq = 0;  // clear IReq
				//}

				return i - offset;
			}

			// 解码一个udp包
			int ProccessUdpPkt(byte [] Udpdata, int udplen)
			{
				int i = 0;

				short mCRC16;

				short mCRC16Calc;
				int mSN;
				long SorSendByte;

				// 计算包的crc值
				mCRC16Calc = GetCRC16(Udpdata, 2, udplen - 2);
				//byte xx[] = new byte[2];
				//setushort(xx, 0, mCRC16Calc);

				long tick = System.currentTimeMillis();
				//int tickcut0 = (int)((short)tick & 0xFFFF);
				//int tickcut = (int)(tick & 0xFFFF);
				long tickcut = tick & 0xFFFFFFFF;

				i = 0;

				int Tag = 0;
				int L = 0; // 含 T L V总长

				// 取发端crc
				mCRC16 = (short)getushort(Udpdata, i);
				i += 2;

				//int ts = getushort(Udpdata, i); // & 0xFFFF;
				int ts = getint(Udpdata, i); // & 0xFFFF;
				i += 4;

				// 反馈: 本端B向A
				// TTL测量, 对端A发起TTL测量, 本端B记录A端时刻和本端收到消息的时刻
				if (TTL_Sor == -1)
				{
					TTL_Sor = ts;
					TTL_Local = tickcut;
				}

				// test
//				{
//					int r = GetUsTickDiffSign(5, 65505);
//					int z = r;
//				}

				mSN = getushort(Udpdata, i); // & 0xFFFF;
				i += 2;

				// send report
				SorSendByte = getint(Udpdata, i); // & 0xFFFF;
				SorSendByte *= 10;
				i += 4;

				if (mCRC16Calc != mCRC16)
				{
					mBadCrcPkNum++;
					//mStat.pushval(NETSTAT.NETSTAT_RcvBadCrcPks, mBadCrcPkNum);

					Log.i(TAG, "rcv udp pks, crc check fail!, "
									+ ", mCRC16Calc:" + mCRC16Calc
									+ ", mCRC16:" + mCRC16
									+ ", mSN:" + mSN
//		            		+ ", m264FrmId:" + m264FrmId
//		            		+ ", m264PkIdInGrp:" + m264PkIdInGrp
////		            		+ ", mFecIdInGrp:" + mFecIdInGrp
//		            		+ ", OrgPkNum_N:" + OrgPkNum_N
//		            		+ ", FecPkNum_n:" + FecPkNum_n
					);

					return 0;
				}

				mRcvPks++;

				// jitter更新
				mCurJitter = jitter.GetJitter(ts, tickcut);
				// curve output
				//String s1 = String.valueOf((int)mCurJitter);
				if (IntTestSwitch) IntTestOut("Jitter", String.valueOf((int)mCurJitter)); // CURVE_TYPE_NUMBER,


				// 源端发送总字节数
				if (mSorSendByte < SorSendByte)
				{
					mSorSendByte = SorSendByte;
				}

				// mSN回滚计数
				if (mLastSN > mSN)
				{
					if (mLastSN - mSN >= 32768)
					{
						mSorSendPksSNRewindTime++;
					}
				}
				// 源端发送总包数
				mSorSendPks = mSorSendPksSNRewindTime * 65536 + mSN;

				// 丢包统计
				if (mLastSN == -1)
				{

				}
				else
				{
					int lostnum = 0;
					if (mSN > mLastSN) // 忽略回滚和乱序
					{
						lostnum = mSN - mLastSN;
						lostnum--;
					}
//					if (lostnum <= 0)
//					{
//						lostnum = -lostnum;
//					}
//					if (lostnum > 32768)
//					{
//						lostnum = 0xFFFF - lostnum;
//					}
					mLostPkNum += lostnum;
					mLostPkNumInPeriod += lostnum;
				}
				mLastSN = mSN;

				// 解包
				int dataleft = udplen - i;
				while(dataleft > 4)
				{
					Tag = getushort(Udpdata, i);
					i += 2;

					L = getushort(Udpdata, i);
					i += 2;
					if (L <= 0 || L > dataleft)
					{
						Log.i(TAG, "rcv udp pks, bad L: " + L + ", Tag: " + Tag);
						break;
					}

					// V

					if (Tag == (int) UDP_ITEM.UDP_ITEM_BA.ordinal())
					{
						GetItemBA(Udpdata, i, L, tick);
					}
//					else if (Tag == (int)UDP_ITEM.UDP_ITEM_AB.ordinal())
//					{
//						GetItemAB(Udpdata, i, L, tick);
//					}
					else if (Tag == (int) UDP_ITEM.UDP_ITEM_VIDEO.ordinal())
					{
						GetVideoData(mSN, mCurJitter, Udpdata, i, L - 4);
					}
					else if (Tag == (int) UDP_ITEM.UDP_ITEM_AUDIO.ordinal())
					{
						GetAudioData(Udpdata, i, L - 4, ts, (int)tickcut);
						audiocap.mAudioEncCount_Period++;
					}
					else if (Tag == (int) UDP_ITEM.UDP_ITEM_AUDIO_FEC.ordinal())
					{
						GetAudioFECData(Udpdata, i, L - 4, ts, (int)tickcut);
					}
					else if (Tag == (int) UDP_ITEM.UDP_ITEM_FILL_DATA.ordinal())
					{
						// 填充数据
					}
					else
					{
						Log.i(TAG, "rcv udp pks, bad T: " + Tag);
						break;
					}

					i += L - 4;	// T L V

					dataleft -= L;	// T L V
				}

				// important printf, may highly encrease delay
				// 264data: Udpdata + i;
//				if (mSN % 10 == 0)
//				{
//					Log.e(TAG, "rcv udp pks"
//		            		+ ", mSN:" + mSN
//		            		+ ", SorSendByte:" + SorSendByte
//	//	            		+ ", mFrmType(I1,P2):" + mFrmType
//	//	            		+ ", mNalCount:" + mNalCount
//	//	            		+ ", m264FrmId:" + m264FrmId
//	//	            		+ ", m264PkIdInGrp:" + m264PkIdInGrp
//	//	            		+ ", OrgPkNum_N:" + OrgPkNum_N
//	//	            		+ ", FecPkNum_n:" + FecPkNum_n
//	//	            		+ ", TTL_Sor:" + TTL_Sor
//	//	            		+ ", TTL_Local:" + TTL_Local
//		            		+ ", rtt:" + encctl.TTL_Result
//		            		+ ", jt:" + mCurJitter
//		            		+ ", lospks:" + mLostPkNum
//		            		);
//				}

				return 1;
			}


			// 解码一个udp包
			int ProccessUdpPkt_old(byte [] Udpdata, int udplen)
			{
				int i = 0;

				int mCRC16;
				short mCRC16Calc;
				int mSN;
				int mFrmType;			// 包类型， 1：I， 2：p
				int mNalCount;			// nal数量
				int mh264Frmlen;		// 264编码输出帧大小
				int mh264blklenInThisPk;	// 数据块大小
				int m264FrmId;
				int m264PkIdInGrp;
//				int mFecIdInGrp;
				int OrgPkNum_N;
				int FecPkNum_n;
				int blk264Pos;

				// 计算包的crc值
				mCRC16Calc = GetCRC16(Udpdata, 2, udplen - 2);
				byte xx[] = new byte[2];
				setushort(xx, 0, mCRC16Calc);

				long tick = System.currentTimeMillis();

				i = 0;

				{
					// 取发端crc
					mCRC16 = getushort(Udpdata, i);
					i += 2;
					mSN = getushort(Udpdata, i) & 0xFFFF;
					i += 2;

					//mFrmType = getushort(Udpdata, i);
					//i += 2;
					mFrmType = Udpdata[i];
					i++;
					mNalCount = Udpdata[i];
					i++;

					mh264Frmlen = getushort(Udpdata, i);			//
					i += 2;
					mh264blklenInThisPk = getushort(Udpdata, i);
					i += 2;
					m264FrmId = getushort(Udpdata, i) & 0xFFFF;
					i += 2;
					m264PkIdInGrp = getushort(Udpdata, i);
					i += 2;
//					mFecIdInGrp = getushort(Udpdata, i);
//					i += 2;

//					OrgPkNum_N = getushort(Udpdata, i);
//					i += 2;
//					FecPkNum_n = getushort(Udpdata, i);
//					i += 2;
					OrgPkNum_N = Udpdata[i];
					i++;
					FecPkNum_n = Udpdata[i];
					i++;

					// TTL测量, 对端A发起TTL测量, 本端B记录A端时刻和本端收到消息的时刻
					if (TTL_Sor == -1)
					{
						//TTL_Sor = getushort(Udpdata, i) & 0xFFFF;
						TTL_Sor = getint(Udpdata, i) & 0xFFFFFFFF;
						i += 4;
						//TTL_Local = (int)((short)tick & 0xFFFF);
						TTL_Local = tick;
					}
					else
					{
						i += 2;
					}
				}

				// 反馈: 本端B向A
				{
					encctl.PeerIFrmReq 		= getushort(Udpdata, i);			// I帧请求
					i += 2;
					encctl.PeerBwReq 		= getushort(Udpdata, i);
					i += 2;
					encctl.PeerLostRatio 	= getushort(Udpdata, i);
					i += 2;
					encctl.PeerLostBurst 	= getushort(Udpdata, i);
					i += 2;
					encctl.PeerAckFrmId 	= getushort(Udpdata, i);
					i += 2;

					// ttl测量结果
					int ttl_est = getushort(Udpdata, i) & 0xFFFF;
					if (ttl_est > 0)
					{
						int t = (int)(tick & 0xFFFF);
						encctl.TTL_Result = GetUsTickDiff((t), ttl_est);

						// curve output
						String s1 = String.valueOf((int)encctl.TTL_Result);
						if (IntTestSwitch) IntTestOut("ttl", s1); // CURVE_TYPE_NUMBER,
					}
					i += 2;
				}

				// i必须是偶数
				//udppk[i++] = 0;
				if ((i % 2) == 1)
				{
					i++;
				}

				// 264 data blk
				blk264Pos = i;

				// 丢包统计
				if (mLastSN == -1)
				{
					mLastSN = mSN;
				}
				else
				{
					int lostnum = mSN - mLastSN;
					if (lostnum < 0)
					{
						lostnum = -lostnum;
					}
					if (lostnum > 32768)
					{
						lostnum = 0xFFFF - lostnum;
					}
					mLostPkNum += lostnum;
					mLostPkNumInPeriod += lostnum;
				}

				if (mCRC16Calc != mCRC16)
				{
					Log.i(TAG, "rcv udp pks, crc check fail!, "
									+ ", mCRC16Calc:" + mCRC16Calc
									+ ", mCRC16:" + mCRC16
									+ ", mSN:" + mSN
									+ ", m264FrmId:" + m264FrmId
									+ ", m264PkIdInGrp:" + m264PkIdInGrp
//		            		+ ", mFecIdInGrp:" + mFecIdInGrp
									+ ", OrgPkNum_N:" + OrgPkNum_N
									+ ", FecPkNum_n:" + FecPkNum_n
					);

					return 0;
				}

				if (mFrmType != 1 && mFrmType != 2) // I 1, P 2
				{
					Log.i(TAG, "pks type error, I 1, P 2, this frm is :" + mSN);

					return 0;
				}

				// important printf
				// 264data: Udpdata + i;
//				Log.i(TAG, "rcv udp pks"
//	            		+ ", mSN:" + mSN
//	            		+ ", mFrmType(I1,P2):" + mFrmType
//	            		+ ", mNalCount:" + mNalCount
//	            		+ ", m264FrmId:" + m264FrmId
//	            		+ ", m264PkIdInGrp:" + m264PkIdInGrp
//	            		+ ", OrgPkNum_N:" + OrgPkNum_N
//	            		+ ", FecPkNum_n:" + FecPkNum_n
//	            		+ ", TTL_Sor:" + TTL_Sor
//	            		+ ", TTL_Local:" + TTL_Local
//	            		+ ", encctl.TTL_Result:" + encctl.TTL_Result
//	            		);

				/*
				map, key is frmid, value is data
				如果包已太晚到达, 丢弃
				不断扫描所有成员
				//如果成员已经收齐, 打上收齐标记
				如果有帧正在接收
				  如果帧满足解码条件, 打收齐标记, 送解码, 刷新解码帧号
				  如果不满足
				    如果正在接收的帧数超过1帧, 丢弃最旧正在接收帧
				      从头开始删除剩余下帧中的P帧, 直到I帧时终止, 刷新解码帧号;如果没有I帧, 发I帧请求
				*/
				//if (false)
				{
					// 包晚到, 丢弃
					//if (isFrmTooOld(m264FrmId))
					if (GetUsTickDiff(m264FrmId, mOutofDateFrmId) <= 0)
					{
//						Log.i(TAG, "pks too old, discard"
//			            		+ ", mSN:" + mSN);

						return 1;
					}

					// 找到frm或创建新的frm(存于m264FrmMap中)
					COne264Frm frm = null;
					Map <Integer, COne264Frm>h264FrmMap = m264FrmMap;
					synchronized(m264FrmMap)
					{
						// 保存包

						frm = m264FrmMap.get(m264FrmId);
						if (frm == null)
						{
							frm = new COne264Frm();
							frm.SetPara(m264FrmId, mFrmType, mNalCount, mh264Frmlen, mh264blklenInThisPk, OrgPkNum_N, FecPkNum_n);
							m264FrmMap.put(m264FrmId, frm);
						}

						// 本帧已经在解码了, 本包晚到, 丢弃
						if (frm.mDecodeing > 0)
						{
							return 0;
						}

						// 保存本包
						frm.SaveOnePk(m264PkIdInGrp, Udpdata, blk264Pos, udplen - blk264Pos);

						// 检测帧完整性, 送解码
						//if (frm.CheckIntegral() > 0)
						//{
						//frm.mMayDecodeing = 1;
						//frm.mDecodeing = 1;
						//synchronized(m264FrmDecingList)
						//{
						//	m264FrmDecingList.add(frm);
						//}
						//}

						/////////////////////////////////////
						SelectFrmAndDecode(h264FrmMap);
					}
				}

				if (mFrmType == 1)
				{
					// I
					IFrmReq = 0;  // clear IReq
				}

				return 1;
			}

			int SelectFrmAndDecode(Map <Integer, COne264Frm> h264FrmMap)
			{
				int r = 0;

				COne264Frm frm = null;


				// 包号排序, 找最旧包号 // map.remove(key);
				//mOutofDateFrmId
				int leastdiff = 0x7FFFFFFF;
				int keydiff1;
				int keyleast = 0;
				int key;
				int gotonextloop = 0;
				int i = 0;
				int s = 0; //h264FrmMap.size();

				while(h264FrmMap.size() > 0)
				{
					gotonextloop = 0;

					leastdiff = 0x7FFFFFFF;

					for (Map.Entry<Integer, COne264Frm> entry : h264FrmMap.entrySet()) {
						//System.out.println("Key = " + entry.getKey()); // + ", Value = " + entry.getValue());

						key = entry.getKey();

						// 找到最旧的key
						keydiff1 = GetUsFrmIdDiff(key, mOutofDateFrmId); //GetUsFrmIdDiff(key, mOutofDateFrmId);
						if (keydiff1 < 0)
						{
							// 本帧过旧, 删除
							gotonextloop = 1;
							h264FrmMap.remove(key);
							break;
						}

						// 刷新最小帧索引keyleast
						if (leastdiff > keydiff1)
						{
							leastdiff = keydiff1;
							keyleast = key;
						}
					}
					if (gotonextloop == 1)
					{
						continue;
					}

					// keyleast
					{
						frm = h264FrmMap.get(keyleast);
						if (frm.CheckIntegral() <= 0)
						{
							break;
						}
						else
						{
							// 数据齐备, 可送解码

							int decpass = 0; // 送解码的规则是否通过

							mAckFrmId = keyleast;	// 收齐的帧， 向源端反馈剩余数据不要再发送了， 可以编码下一帧了

							if (frm.mFrmType == 1)
							{
								// I帧可送解码
								decpass = 1;
							}
							else if (frm.mFrmType == 2)
							{
								// P帧要求参考关系是正常的
								//if (frm.mFrmId ==
								//	((mOutofDateFrmId + 1) & 0xFFFF))
								if (GetUsFrmIdDiff(frm.mFrmId, mOutofDateFrmId) == 1)
								{
									decpass = 1;
								}
							}
							else
							{
								// assert, 上面刚收到包是已经进行过FrmType的检测
								return 0;
							}

							if (decpass > 0)
							{
								frm.mMayDecodeing = 1;
								frm.mDecodeing = 1;
								synchronized(m264FrmDecingList)
								{
									m264FrmDecingList.add(frm);
								}
							}

							// 本帧处理完毕, 删除
							h264FrmMap.remove(keyleast);

							mOutofDateFrmId = keyleast;
						}
					}
				}

				//////
				// 接收队列过长， 删除旧的残缺帧
				// 如果帧数 > 2, 立即删除最旧的帧
				//   如果删除到I帧, 结束
				//   如果无I帧, 不断请求I帧, 拒绝所有后续P数据, 直到I帧为止
				int ireq = 0;
				s = h264FrmMap.size();
				if (s > 2)
				{
					ireq = 1;

					while(h264FrmMap.size() > 0)
					{
						leastdiff = 0x7FFFFFFF;

						// 找头元素keyleast
						leastdiff = 0x7FFFFFFF;
						for (Map.Entry<Integer, COne264Frm> entry : h264FrmMap.entrySet()) {
							//System.out.println("Key = " + entry.getKey()); // + ", Value = " + entry.getValue());

							key = entry.getKey();

							keydiff1 = GetUsFrmIdDiff(key, mOutofDateFrmId); //GetUsFrmIdDiff(key, mOutofDateFrmId);
							// 刷新最小帧索引keyleast
							if (leastdiff > keydiff1)
							{
								leastdiff = keydiff1;
								keyleast = key;
							}
						}

						frm = h264FrmMap.get(keyleast);
						if (frm.mFrmType == 1) // I
						{
							if (frm.CheckIntegral() > 0)
							{
								// 找到完整i帧, 中断删除无效包操作
								ireq = 0;
								break;
							}
						}

						// 删除旧的残缺帧
						mOutofDateFrmId = keyleast;
						h264FrmMap.remove(keyleast);
					}
				}
				if (ireq > 0)
				{
					IFrmReq = (byte)ireq;
					Log.i(TAG, "Broken Frm too much:" + s + ", IFrmReq: " + IFrmReq);
				}

				return 1;
			}

			@Override
			public void run() {

				int counter = 0;

				try {
					socket = new DatagramSocket(UdpRcvPort);
				} catch(IOException e) {
					e.printStackTrace();
				}

				while(mRuning)
				{
					if (simnet != null)
					{
						break;
					}


					try {
						Thread.sleep(10); // 10);
					} catch (InterruptedException e) {

						e.printStackTrace();
					}
				}


				while(mRuning)
				{
					counter++;

					try {
						packet = new DatagramPacket(udpbuf, 2048); // udpbuf.length);  //创建DatagramPacket对象
						if (packet == null)
						{
							Log.e(TAG, "packet null error. counter:" + counter);

							try {
								Thread.sleep(10); // 10);
							} catch (InterruptedException e) {

								e.printStackTrace();
							}

							continue;
							//return;
						}

						socket.receive(packet);  //通过套接字接收数据

						int datalen = packet.getLength();
						byte data[] = packet.getData();
						byte output[] = null;

						mRcvByte += datalen + 28;
						mRcvByteInPeriod += datalen + 28;

						// important print
//			            Log.i(TAG, "counter:" + counter + ", rcv udp pks id:" + counter + ", datalen:" + datalen
//			            		+ ", " + data[0]
//			            		+ " " + data[1]
//			            		+ " " + data[2]
//			    			    + " " + data[3]
//	    			    		+ " " + data[4]
//			            		);

						//offerDecoder(datalen, data, output);
//			            int r = dec.offerDecoder(datalen, data, 0, output);

						if (false)
						//if (true)
						{
							ProccessUdpPkt(data, datalen);
						}
						else
						{
							// test 测试功能
							//if (false)
							if (true)
							{
								int SimLostR = 0;
								if (counter % 100 == 0)
								{
									SimLostR = GetCtlPara("SimLostR");
									if (SimLostR * 10 != simnet.pklostrate_1000_ctl)
									{
										simnet.SetLostR(SimLostR * 10);
									}

									{
										String s1 = String.valueOf(SimLostR);
										if (IntTestSwitch) IntTestOut("SetLostR_PK", s1); // CURVE_TYPE_NUMBER,
									}
								}
							}
							else
							{
								int SimLostR = 10*10;
								if (simnet.pklostrate_1000_ctl != SimLostR)
								{
									simnet.SetLostR(SimLostR);
								}
							}

							// 加入网络仿真功能
							simnet.PkIn(data, datalen);
						}

					} catch (UnknownHostException e) {
						e.printStackTrace();
					} catch(IOException e) {
						e.printStackTrace();
					}

					//counter++;
				}

				socket.close();  //关闭套接字

//	            AVComDestroy();
			}


			// 判断当前帧号是否已经太旧
			private boolean isFrmTooOld(int FrmId)
			{
				if (FrmId > mOutofDateFrmId)
				{
					if (FrmId - mOutofDateFrmId < 32768)
					{
						return false;
					}
					else
					{
						return true;
					}
				}
				else
				{
					if (mOutofDateFrmId - FrmId < 32768)
					{
						return true;
					}
					else
					{
						return false;
					}
				}
			}

			// 把short数据value存入by + offset和by + offset + 1位置
//			short getushort(byte by[], int offset)
//			{
//				short v1 = (short)(by[offset] & 0xFF);
//				short v2 = (short)(by[offset + 1] & 0xFF);
//				short value = (short)((v2 << 8) + v1);
////				int value = (int)(((by[offset + 1] & 0xff) << 8) | (by[offset] & 0xff));
////				int value = (int)(((int)((by[offset + 1]) << 8)) | (by[offset]));
//
//				return value;
//			}
			int getushort(byte by[], int offset)
			{
//				short v1 = (short)(by[offset] & 0xFF);
//				short v2 = (short)(by[offset + 1] & 0xFF);
//				short value = (short)((v2 << 8) + v1);
//				int value = (int)(((by[offset + 1] & 0xff) << 8) | (by[offset] & 0xff));
//				int value = (int)(((int)((by[offset + 1]) << 8)) | (by[offset]));
				int targets = (by[offset] & 0xff) | ((by[offset + 1] << 8) & 0xff00);

				return targets;
			}
			int getint(byte by[], int offset)
			{
				int targets = (by[offset] & 0xff) | ((by[offset + 1] << 8) & 0xff00)
						| ((by[offset + 2] << 16) & 0xff0000) | ((by[offset + 3] << 24) & 0xff000000);
				return targets;
			}

			// 计算crc, bytelen必须为2的倍数
			short GetCRC16(byte data[], int offset, int bytelen)
			{
				short crc = 0;
//				int shortlen = bytelen / 2;
				int shortd;

				for(int i = 0 + offset; i < bytelen; i += 2)
				{
					shortd = (((int)data[i]) << 8) + data[i+1];

					crc ^= shortd;
				}

				return crc;
			}

		}

		// FEC和视频解码
		class DecodeThread extends Thread {

			int r = 0;

			int firstFrmChecked = 0;

			@Override
			public void run() {
				// TODO Auto-generated method stub
				//super.run();
				while(mRuning) {

					try {
						Thread.sleep(1); // 10);
					} catch (InterruptedException e) {

						e.printStackTrace();
					}

					// 检测包, 齐备了就送FEC触码和视频解码

					COne264Frm frm = null;
					synchronized(m264FrmDecingList)
					{
						if (m264FrmDecingList.size() == 0)
						{
							continue;
						}

						frm = m264FrmDecingList.get(0);
						m264FrmDecingList.remove(frm);
					}

					// 首帧丢失检测
					if (firstFrmChecked == 0)
					{
						firstFrmChecked = 1;

						if (frm.mFrmId != 1)
						{
							//Toast.makeText(mctx, "First frm lost.", 200).show();
						}
					}

					// fec解码调用统计
					{
						if (frm.CheckIntegral() == 2)	// 原始数据不齐, 但fec解码可恢复
						{
							ActFecDecCount++;

							//continue;
						}
					}

					// test
					int RstVDec = 0;
					if (GetCtlPara("RstVDec") > 0)
					{
						RstVDec = 1;
					}

					frm.mDecodeing = 1; // 置正在解码标记， 后续不再接收新包
					r = VideoDecode(
							frm.mFrmId,
							frm.mNalCount,
							frm.mh264Frmlen,
							frm.mpks,
							frm.mh264blklen,
							frm.mOrgPkNum_N,
							frm.mFecPkNum_n,
							frm.mlost,
							frm.mlostCount,
							RstVDec
					);
					if (r > 0)
					{
						// 解码成功
						int z = 0;

						ActDecodeVFrmNum++;			// 264解码帧率
						ActDecodeFpsInPeriod++;

						IFrmReq = 0;

						//Log.i(TAG, "VideoDecode ok: mFrmId:" + frm.mFrmId + ", mFrmType:" + frm.mFrmType + ", mNalCount:" + frm.mNalCount);
					}
					else
					{
						IFrmReq = 1;	// 向源端请求I帧

						Log.e(TAG, "VideoDecode fail, new I req: mFrmId:" + frm.mFrmId + ", mFrmType:" + frm.mFrmType + ", mNalCount:" + frm.mNalCount);
					}
				}

				AVComDestroy();
			}
		}

		// 连续取图， // 位图显示模式, 已被opengl模式替代
		Bitmap decbmp = null;
		byte decimg[] = null;
		byte YUVBuf[] = new byte[(int)(640 * 480 * 1.5) + 100];
		final Runnable mUpdateUI = new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub

				//while(mRuning)
				{
					if (decbmp != null)
					{
						synchronized(decbmp)
						{
//	                		int r = GetDecPic2(decbmp);
//	                		if (r > 0)
							{
								if (mImag != null)
								{
									mImag.setImageBitmap(decbmp);	// draw
								}
							}
						}
					}
				}
			}
		};

		// 显示输出
		class StartDispThread extends Thread {

			int r = 0;
			int w = 0;
			@Override
			public void run() {
				// TODO Auto-generated method stub
				//super.run();

				decbmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

				while(mRuning0) {

					// 检测包, 齐备了就送FEC触码和视频解码

					r = 0;
					// 位图显示模式, 已被opengl模式替代
					if (false)
					{
						Bitmap decbmpx = decbmp;
						if (decbmpx != null)
						{
							synchronized(decbmp)
							{
								w = GetPicWidth();
								if (w > 0)
								{
									r = GetDecPic2(decbmp);

									if (decbmp != null && r > 0)
									{
										mHandler.post(mUpdateUI);
									}
								}
							}
						}
					}

					//if (false)
					{
						decimg = GetDecPicYVU(null);
						if (decimg != null)
						{
							byte decimgx[] = decimg;
							int w = ((decimg[1]&0xFF) << 8) + (decimg[0]&0xFF);
							int h = ((decimg[3]&0xFF) << 8) + (decimg[2]&0xFF);
							if (w > 0)
							{
								mGLFRenderer.update(w, h);
								mGLFRenderer.update(decimgx, 4, w, h);
							}
						}
						SetPicBufIdle();
					}

					if (false)
					{
						decimg = YUVBuf;
						GetDecPicYVU(YUVBuf);
						if (decimg != null)
						{
							byte decimgx[] = decimg;
							int w = ((decimg[1]&0xFF) << 8) + (decimg[0]&0xFF);
							int h = ((decimg[3]&0xFF) << 8) + (decimg[2]&0xFF);
							if (w > 0)
							{
								mGLFRenderer.update(w, h);
								mGLFRenderer.update(decimgx, 4, w, h);
							}
						}
						SetPicBufIdle();
					}

					if (false)
					{
						mVDecPicBuf.get(YUVBuf, 0, (int)(320 * 240 * 1.5));

						decimg = YUVBuf;
						if (decimg != null)
						{
							byte decimgx[] = decimg;
							//int w = ((decimg[1]&0xFF) << 8) + (decimg[0]&0xFF);
							//int h = ((decimg[3]&0xFF) << 8) + (decimg[2]&0xFF);
							int w = 320;
							int h = 240;
							mGLFRenderer.update(w, h);
							mGLFRenderer.update(decimgx, 0, w, h);
						}
						SetPicBufIdle();
					}

					//Log.i(TAG, "w----------------------------------" + w);

					try {
						Thread.sleep(5); // 10);
					} catch (InterruptedException e) {

						e.printStackTrace();
					}
				}

				Release();
			}
		}

		// 显示输出
//	  	class StartDispThread extends Thread {
//
//	  		byte decimg[] = null;
//
//	        @Override
//	        public void run() {
//	            // TODO Auto-generated method stub
//	            //super.run();
//	        	decbmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888); // ARGB_8888
//
//	            while(mRuning) {
		//
//					// 检测包, 齐备了就送FEC触码和视频解码
//
//	            	//if (GotData > 0)
//	            	{
////		            		decimg = GetDecPic();
//	            		if (decbmp != null)
//	            		{
//							int w = GetPicWidth();
//							if (w != decbmp.getWidth())
//							{
//								// 分辨率发生变化，重新生成图像
//
//								decbmp.recycle();
//
//								width = w;
//								height = GetPicHeight();
//
//					        	decbmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888); // ARGB_8888
//							}
//	            			synchronized(decbmp)
//	                    	{
//	            				GetDecPic2(decbmp);
//	                    	}
//	            		}
//
//	                    mHandler.post(mUpdateUI);
//
//	                    //GotData = 0;
//	            	}
		//
//					try {
//						Thread.sleep(10); // 10);
//					} catch (InterruptedException e) {
//
//						e.printStackTrace();
//					}
//	            }
//
//	            Release();
//	        }
//	    }
		//
		// 收端统计
		class BStatThread extends Thread {

			// 统计实际带宽
			//int s_counter = 0;
			long BWStat_lastTick = 0;
			long BWStatPeriodInMS = 300;
			long BWStatPeriodInMS_count = 0;
			long StatPeriodInMS = 100;
			long StatPeriodInMS_count = 0;

			long T0 = System.currentTimeMillis();

			float RcvStat(long tick)
			{
				//tick = System.currentTimeMillis();
				//if (BWStat_lastTick == 0)
				//{
				//	BWStat_lastTick = tick;
				//}
				//long TickPast = tick - BWStat_lastTick;

				float rcvbw = 0;
				float lostratio = 0;
				float fStatFps = 0;
				float dec264Fps = 0;

				// 新周期开始，更新周期起始时刻
				//if (TickPast >= BWStatPeriodInMS)
				if (tick - T0 >= BWStatPeriodInMS * BWStatPeriodInMS_count)
				{
					BWStatPeriodInMS_count++;

					//Log.i(TAG, "StatActBW(), s_counter:" + s_counter);
					//s_counter++;

					//if (TickPast > 0)
					{
						rcvbw = mRcvByteInPeriod * 1000 / BWStatPeriodInMS;
						lostratio = mLostPkNumInPeriod * 1000 / BWStatPeriodInMS;
						dec264Fps = ActDecodeFpsInPeriod * 1000 / BWStatPeriodInMS;
						//fStatFps = FrmNumInPeriod * 1000 / TickPast; // tbd
					}

					//ActEncodeFps = FrmCount * 1000 / TickPast;

					//BWStat_lastTick = tick;

					mRcvByteInPeriod = 0;
					mLostPkNumInPeriod = 0;
					//FrmNumInPeriod = 0
					ActRcvFpsInPeriod = 0;
					ActDecodeFpsInPeriod = 0;

					//Log.i(TAG, "-----------------------------dec264Fps:" + dec264Fps + ", ActFecDecCount:" + ActFecDecCount);

					{
						String s1 = String.valueOf((int)rcvbw);
						if (IntTestSwitch) IntTestOut("RealRcvBR", s1); // CURVE_TYPE_NUMBER,
					}
					{
						String s1 = String.valueOf(lostratio * 100);
						if (IntTestSwitch) IntTestOut("LostR", s1); // CURVE_TYPE_NUMBER,
					}
					{
						String s1 = String.valueOf(dec264Fps);
						if (IntTestSwitch) IntTestOut("Dec264Fps", s1); // CURVE_TYPE_NUMBER,
					}
					{
						String s1 = String.valueOf(ActFecDecCount % 20);
						if (IntTestSwitch) IntTestOut("FecDecNum", s1); // CURVE_TYPE_NUMBER,
					}
				}

				// 在收端进行源和收的统计
				if (mStat != null
						&& tick - T0 >= StatPeriodInMS * StatPeriodInMS_count)
				{
					StatPeriodInMS_count++;

					mStat.pushval(NETSTAT.NETSTAT_SendBRSet, 	BwCtl); //DstSendBw);
					mStat.pushval(NETSTAT.NETSTAT_SendByte, 	mSorSendByte);
					mStat.pushval(NETSTAT.NETSTAT_SendPks, 		mSorSendPks);
					mStat.pushval(NETSTAT.NETSTAT_RcvByte, 		mRcvByte);
					mStat.pushval(NETSTAT.NETSTAT_RcvPks, 		mRcvPks);
					mStat.pushval(NETSTAT.NETSTAT_RcvLostPks, 	mLostPkNum);
					mStat.pushval(NETSTAT.NETSTAT_RcvOneWayDelay, mCurJitter);
					if (encctl != null)
					{
						mStat.pushval(NETSTAT.NETSTAT_SendBR, 		encctl.ActSendBw);
						mStat.pushval(NETSTAT.NETSTAT_RcvRtt, 		encctl.TTL_Result);
						mStat.pushval(NETSTAT.NETSTAT_ImgRcvNum, 	ActDecodeVFrmNum);
					}
					mStat.pushval(NETSTAT.NETSTAT_RcvBadCrcPks, mBadCrcPkNum);

					mStat.dida();

					// 绘图
					if (StatPeriodInMS_count % 3 == 0)
					{
						mStat.Calc();
						mStat.Draw();
						mHandler.post(mUpdateUI2);
					}

					// 策略控制
					{
						// 反馈发送带宽，丢包率
						mStat.GenPolicy(tick);
					}
				}

				return 1;
			}

			final Runnable mUpdateUI2 = new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub

//		        	Matrix matrix = new Matrix();
//		        	if (mStat.CurX > 100)
//		        	{
//		        		matrix.setTranslate(mStat.CurX - 100, 0);
//		        		mImageStat.setImageMatrix(matrix);
//		        	}

//		        	if (mStat.CurX > 150)
//		        	{
//		        		mStatSrollView.setScrollX(mStat.Px2Dp(mctx, (int)(mStat.XScale * (mStat.CurX - 150))));
//		        	}
					mImageStat.setImageBitmap(mStat.mBitmapPeer);	// draw


//		        	//while(mRuning)
//		        	{
//		                if (decbmp != null)
//		                {
//		                	synchronized(decbmp)
//		                	{
//	//	                		int r = GetDecPic2(decbmp);
//	//	                		if (r > 0)
//		                		{
//		                			if (mImag != null)
//		                			{
//		                				mImag.setImageBitmap(decbmp);	// draw
//		                			}
//		                		}
//		                	}
//		                }
//		        	}
				}
			};

			@Override
			public void run() {
				// TODO Auto-generated method stub
				//super.run();
				while(mRuning) {

					long tick = System.currentTimeMillis();
					RcvStat(tick);

					try {
						Thread.sleep(20); // 10);
					} catch (InterruptedException e) {

						e.printStackTrace();
					}
				}
			}
		}
	}

}

package com.epiano.commutil;

import android.content.Context;
import android.util.Log;

public class EPianoAndroidJavaAPI {

	private static final String LOG_TAG = "VC";
	static private Context mycontext;

	private int mAppScorePageCount = 0;
	private static int MAX_SCORE_PAGE_COUNT = 20;

	public int ipagecount = 0;

	public EPianoAndroidJavaAPI(Context context) {

		Log.e(LOG_TAG, "Loading EP_jni.so...");
		System.loadLibrary("EP_jni");
		Log.e(LOG_TAG, "Loaded EP_jni.so.");

		mycontext = context;
	}

	public EPianoAndroidJavaAPI() {

		Log.e(LOG_TAG, "Loading EP_jni.so...");
		System.loadLibrary("EP_jni");
		Log.e(LOG_TAG, "Loaded EP_jni.so.");

		//mycontext = context;
	}

	// API Native

	private native boolean NativeInit(Context context);

	// 开启引擎
	public native int StartEngine();

	// 关闭引擎
	public native int CloseEngine();

	// 向底层配置窗口信息
	public native int SetWinInfo(int width, int height, int leftmagin, int topmagin, int rightmagin, int bottommagin, int backgroundclr);		// 0xfffae9

	// 打开文件
	public native int OpenFile(String filename);

	// 试JNI参数格式化方式
	public native int TestParaFormat(char CharPara, int IntPara, long longPara, String StringPara, float Floatpara, int Intarray[], float Floatarray[], char Chararray[]);

	// demo文件
	public native int DemoFile();

	// 绘图
	public native int NotifyPaint();

	// 查询待绘图元数
	public native int QueryDrawElementCount();

	// 查询页数
	public native int QueryPageCount();

	// 查询页数
	public native byte[] QueryDrawOderSet(int iPageId);

	// “鼠标”移动
	public native int OnMouseMove(int iPageId, int x, int y);

	// 查询一个待绘图元
	//public native int GetDrawElement();

	// 查询页数
	public native int[] QueryMouseLine();

	// 查询页数
	public native int[] GetPlayNoteSet();

	// 查询play绘图位置信息
	public native int[] QueryPlayLinePos(int iVPId, int iBarId, int iTimeSliceId);

	// 通过声音识别单个琴键
	public native float[] KeyRecon(short Intarray[]);

	// FFT
	public native float[] RealFFT(float flotarray[]);



	////////////////////////////////////////////////////////////
	//jni, lib向上调用java, 暂时废弃

	//应用层增加一页
	public int AppLayerAddAPage(int iPageId)
	{

		//Toast.makeText(mycontext, "AppLayerAddAPage", Toast.LENGTH_SHORT).show();

		ipagecount++;

		return 1;
	}

	//应用层删除一页
	public int AppLayerDeleteAPage(int iPageId)
	{
		if (iPageId >= MAX_SCORE_PAGE_COUNT) // iPageId >= mAppScorePageCount ||
		{
			return 0;
		}

		if (mAppScorePageCount < 0)
		{
			Log.d(LOG_TAG, "AppLayerDeleteAPage(), error, mAppScorePageCount < 0"); //: %d", iPageId);
		}

		return 1;
	}

	//应用层删除所有一页
	public int AppLayerDeleteAllPage()
	{
		int i;
		for(i = 0; i < MAX_SCORE_PAGE_COUNT; i++)
		{
			AppLayerDeleteAPage(i);
		}

		return 1;
	}

	//选笔
	public int AppLayerSelectPen(int iPageId, int PenColor, int Width, int ePenStyle) // enum ENUM_PEN_STYLE
	{


		return 1;
	}

	//选字体
	public int AppLayerSelectFont(int iPageId, int eFont) // enum ENUM_FONT
	{


		return 1;
	}


	//输出文字
	public int AppLayerOutText(int iPageId, int FontId, int x, int y, String pstr) // char *
	{


		return 1;
	}

	//画线
	public int AppLayerLineTo(int iPageId, int x0, int y0, int x1, int y1)
	{


		return 1;
	}

	//画点
	public int AppLayerSetPixel(int iPageId, int x, int y, int ColorRef) // UINT
	{


		return 1;
	}

	//刷白
	public int AppLayerPaintWhite(int iPageId)
	{


		return 1;
	}

	//画一行开始处的谱号、节奏号
	public int AppLayerLoadABmp(int iPageId, int eNoteType, int x, int y, int cNoteSelected, float zoom) // enum ENUM_NOTE int actx, int acty,
	{


		return 1;
	}

}

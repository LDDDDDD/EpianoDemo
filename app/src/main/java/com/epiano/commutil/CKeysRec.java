package com.epiano.commutil;

import android.util.Log;
//import CCircleQueue;


public class CKeysRec {

	private static final String LOG_TAG = "VC";
	
	/*
	开始
	持续输入audio信号, 存到缓冲区
	长度达到1024时,进行fft处理,
	计算每次输入信号的fft
	提取
	*/

	//extern void RealFFT(float *x, unsigned long N);

//	public int mStart = 0;						// 开始状态
//	static int mListSize = 200;
//	public int mPreKeyList[] = new int[mListSize];		// 前面的Key列表
//	public int mPreKeyTick[] = new int[mListSize];		// 前面的Key开始时刻列表
//	public int mPreKeyLong[] = new int[mListSize];		// 前面的Key持续时长列表
//	public int mPreKeyCount = 0;					// 前面的Key数
	//private ArrayList<int[]> bufPCM = new ArrayList<int[]>();//原始录入数据

	//static CCircleQueue mCQ = new CCircleQueue();		// 保存auido数据的队列
	CLinearQueue mLQ = new CLinearQueue();

	static int DataNumPeek = (10 * 16000);	// 10秒
	short AudioDataPeek[] = new short[DataNumPeek];
	int PowerlineDataPeek[] = new int[DataNumPeek / mLQ.powern];

	float key88[] = new float[88];

	// 双缓冲区
	// 双缓冲区的作用是: 采集和分析分离，让采集数据不爱打扰
	// mCurBufChannel指向当前主channel
	// 如果主channel超过1/2时仍未检测到key，则准备切换channel（切换mCurBufChannel），（可适当copy一些前导数据，刷新DiscardDataNum）
	// 如果在主channel检测到一个key，则等待数据，
	// 直接到满足以下条件：
	//  1,已经等待了300ms: 则 强制识别（每增加100ms进行一次识别，防止太密集），结果处于待刷新状态（在后续得到更多数据时，有条件能得更好的结果），xx（后续数据导入到另一channel，（可适当copy一些前导数据））
	//  或 2，powerline能量值下降到1/n: 则 强制识别，后续数据导入到另一channel，（可适当copy一些前导数据）
	//  或3，检测到新的key: 则 选择最合适的fft位置进行识别，第二个key的数据以及后续数据导入到另一channel，（可适当copy一些前导数据）

//	static int MaxDataNumInBuf = 20 * 16000;	// 20秒
//
////	short AudioDataBuf_channel0[] = new short[MaxDataNumInBuf];
////	short AudioDataBuf_channel1[] = new short[MaxDataNumInBuf];
////	short AudioDataBuf[] = AudioDataBuf_channel0;
//	short AudioDataBuf[] = new short[MaxDataNumInBuf];
//
//	int mCurBufChannel = 0;
//	int mLastKeyConfirmed = 1;			// 上一key确认标记, 有些key的识别经历不确切到确切的过程
//	int AudioDataBuf_CurPos = 0;		// 待插入新数据位置
//	int AudioDataBuf_CurProcessPos = 0;	// 已分析完数据结束位置
//	int mDiscardDataNum = 0;
//
//	static int powern = 500;
//	static int powerlineMaxPoints = (MaxDataNumInBuf + powern - 1) / powern;
//	int powerline_CurPos = 0;	// powern *
//	int powerline[] = new int[powerlineMaxPoints]; //  / 500
//	int powerline_DrawedPos = 0;

	int OneFrameDataNum = 2 * 16000;	// 6秒的数据

	long mStartT0InMs = 0;
	static int MaxKeyCount = 90; // 100
	int KeyCount = 0; //[] = new int[1]; //0;						// 识别的kes数量
	int KeyHitPos[] = new int[MaxKeyCount]; // [1000] = {0};	// key在powerline中的开始位置
	int KeyHitFFTPos[] = new int[MaxKeyCount]; // [1000] = {0};	// key在powerline中的
	int KeyHitEndPos[] = new int[MaxKeyCount]; // [1000] = {0};	// key在powerline中的
	int KeyN[] = new int[MaxKeyCount]; // [1000] = {0};		// key的号1~88
	int KeyFFTSize[] = new int[MaxKeyCount]; // [1000] = {0};		// fft精度, 2048, 4096...
	long KeyT[] = new long[MaxKeyCount]; // [1000] = {0};	// key开始时刻
	int KeyLong[] = new int[MaxKeyCount]; // [1000] = {0};
	float KeyFs[] = new float[MaxKeyCount]; // [1000] = {0};
	static int harmnoicSeg = 1024;
	float KeyHarmnic[] = new float[MaxKeyCount * harmnoicSeg]; // [1000] = {0};

	short FFTInputBuf[] = new short[4096 + 10];

	int lineMember = 30; 	// 一行显示的key数量
	int mScroll = 0;
	int mScrollLineNum = 0;	// 滚动过多少行

	long mT0 = 0;

	public CKeysRec() {	// Context context

		Log.e(LOG_TAG, "Loading EP_jni.so for CKeys...");
		System.loadLibrary("EP_jni");
		Log.e(LOG_TAG, "Loaded EP_jni.so for CKeys.");

		//mycontext = context;

		InitKey88();

		if (EPianoAndroidJavaAPI_inited == 0)
		{
			EPianoAndroidJavaAPI_inited = 1;

			ObEPianoAndroidJavaAPI = new EPianoAndroidJavaAPI();
			//ObEPianoAndroidJavaAPI.StartEngine();

			// 启动引擎
			//ObEPianoAndroidJavaAPI.StartEngine();

			//ObEPianoAndroidJavaAPI.KeyRecon(null);
		}

		reset();
	}

	int EPianoAndroidJavaAPI_inited = 0;
	EPianoAndroidJavaAPI ObEPianoAndroidJavaAPI;
	int GetKeyFromLocalFile()
	{
		//if (EPianoAndroidJavaAPI_inited == 0)
		{
			//EPianoAndroidJavaAPI_inited = 1;

			//ObEPianoAndroidJavaAPI = new EPianoAndroidJavaAPI();
			//ObEPianoAndroidJavaAPI.StartEngine();

			// 启动引擎
			//ObEPianoAndroidJavaAPI.StartEngine();

			ObEPianoAndroidJavaAPI.KeyRecon(null);
		}

		return 1;
	}

	int GetAudioDataSize()
	{
		//return mCQ.GetBufSize();
		return 0;
	}

	int GetUnProcessDataSize()
	{
		int len = 0;

		synchronized(mLQ)
		{
			len = mLQ.AudioDataBuf_CurPos - mLQ.AudioDataBuf_CurProcessPos;
		}

		return len;
	}

	void reset()
	{
		mT0 = System.currentTimeMillis();
	}

	// 保存audio数据, call in RecordThread
	int FeedAudioData(short AudioData[], int DataNum)
	{
		//synchronized(mCQ);
		//int r = mCQ.FeedAudioData(AudioData, DataNum);

		int datafeed = DataNum;
		if (mLQ.AudioDataBuf_CurPos >= mLQ.MaxDataNumInBuf) //  - 1)
		{
			int len = mLQ.AudioDataBuf_CurPos - mLQ.MaxDataNumInBuf;
			Log.i("wg", "FeedAudioData(), error, buf overflow:" + String.valueOf(len));

			// 丢弃部分数据, 腾出位置
			int discarddata = mLQ.MaxDataNumInBuf / 2;
			discarddata = discarddata - (discarddata % 4);
			DiscardFrontAudioData(discarddata);

			return 0;
		}

		// lock
		//synchronized(AudioDataBuf)
		synchronized(mLQ)
		{
			//synchronized(powerline_CurPos)
			{
				if (mLQ.AudioDataBuf_CurPos + DataNum >= mLQ.MaxDataNumInBuf)
				{
					datafeed = mLQ.MaxDataNumInBuf - mLQ.AudioDataBuf_CurPos;
				}

				// cach audio data
				for(int i = 0; i < datafeed; i++)
				{
					mLQ.AudioDataBuf[mLQ.AudioDataBuf_CurPos + i] = AudioData[i];
				}

				mLQ.AudioDataBuf_CurPos += datafeed;

				// PowerLine
				{
					int plstart = mLQ.powerline_CurPos;
					int plend = mLQ.AudioDataBuf_CurPos / mLQ.powern - 1;
					//r = CalcPowerLine(AudioDataPeek, plstart, plend, powern, powerline);//AudioDataPeek
					int r = CalcPowerLine(mLQ.AudioDataBuf, plstart, plend, mLQ.powern, mLQ.powerline);//AudioDataPeek
					if (plend >= 0)
					{
						mLQ.powerline_CurPos = plend;
					}
				}
			}
		}

		return 1;
	}

	//  取audio数据, nu use
	int FetchAudioData(short AudioData[], int DataNum)
	{
		assert(false);

		int r = 0;
		//synchronized(mCQ);
		//r = mCQ.FetchAudioData(AudioData, DataNum);

		return r;
	}

	//  窥探audio数据, call in DrawThread, 	no use
	int PeekAudioData(short AudioData[], int PowerlineData[], int start, int DataNum)
	{
		//synchronized(mCQ);
		//int r = mCQ.PeekAudioData(AudioData, DataNum);

		int dn = DataNum;
		if (start + DataNum >= mLQ.AudioDataBuf_CurPos)
		{
			return 0;
		}

		// lock
		//synchronized(mLQ) // 在调用点同步
		{
			for(int i = 0; i < dn; i++)
			{
				AudioData[i] = mLQ.AudioDataBuf[start + i];
			}

			int plcount = dn / mLQ.powern;
			int s = start / mLQ.powern;
			for(int i = 0; i < plcount; i++)
			{
				PowerlineData[i] = mLQ.powerline[s + i];
			}
		}

		return 1;
	}

	//  弹出头部audio数据, call in DrawThread
	// DiscardDataNum需要是powern的倍数
	int DiscardFrontAudioData(int DiscardDataNum)
	{
		//synchronized(mCQ);
		//int r = mCQ.PeekAudioData(AudioData, DataNum);

		DiscardDataNum = DiscardDataNum - (DiscardDataNum % mLQ.powern);
		if (DiscardDataNum == 0)
		{
			return 0;
		}

		if (DiscardDataNum >= mLQ.AudioDataBuf_CurPos)
		{
			return 0;
		}

		// lock
		synchronized(mLQ) //AudioDataBuf)
		{
			int datamove = mLQ.AudioDataBuf_CurPos - DiscardDataNum;

			// audio data
			for(int i = 0; i < datamove; i++)
			{
				mLQ.AudioDataBuf[i] = mLQ.AudioDataBuf[DiscardDataNum + i]; // shift
			}

			// powerline
			int plmove = datamove / mLQ.powern;
			int plstart = DiscardDataNum / mLQ.powern;
			for(int i = 0; i < plmove; i++)
			{
				mLQ.powerline[i] = mLQ.powerline[plstart + i]; // shift
			}

			// update params
			mLQ.AudioDataBuf_CurPos -= DiscardDataNum;
			if (mLQ.AudioDataBuf_CurPos < 0)
			{
				mLQ.AudioDataBuf_CurPos = 0;
				Log.i("wg", "DiscardFrontAudioData, error 1.");
			}
			mLQ.powerline_CurPos -= DiscardDataNum / mLQ.powern;
			if (mLQ.powerline_CurPos < 0)
			{
				mLQ.powerline_CurPos = 0;
				Log.i("wg", "DiscardFrontAudioData, error 2.");
			}
			mLQ.powerline_DrawedPos -= DiscardDataNum / mLQ.powern;
			if (mLQ.powerline_DrawedPos < 0)
			{
				mLQ.powerline_DrawedPos = 0;
				Log.i("wg", "DiscardFrontAudioData, error 3.");
			}

			// 丢弃数据计数
			mLQ.mDiscardDataNum += DiscardDataNum;
		}

		return 1;
	}

	// 一帧数据收齐, no use
	int OneFrameOk()
	{
		//int minDataNum = 2 * 16000;	// 6秒的数据
		int audiodatanum = 0; //mCQ.GetBufSize();
		if (audiodatanum < OneFrameDataNum)
		{
			return 0;
		}

		return 1;
	}

	// 单线程处理模式
	int OnAudioData() //short AudioData[], int DataNum)
	{
		int r = 0;

		// 输入

		//Log.i("1 FeedAudioData in...num:" + String.valueOf(mCQ.GetBufSize()), "wg");

		// 窥视数据， copy到AudioDataPeek, PowerlineDataPeek  (效率低)
		// 效率太低，去除此peek操作
		int peekdatacount = 0;
		if (false)
		{
			r = 0;
			synchronized(mLQ)
			{
				int start = mLQ.AudioDataBuf_CurProcessPos;
				int end = mLQ.AudioDataBuf_CurPos;
				int datalen = end - start - 1;
				if (datalen > 0)
				{
					peekdatacount = DataNumPeek;	// 最多10秒数据
					if (peekdatacount > datalen)
					{
						peekdatacount = datalen;
					}
					peekdatacount = peekdatacount - (peekdatacount % mLQ.powern);

					if (peekdatacount > 0)
					{
						r = PeekAudioData(AudioDataPeek, PowerlineDataPeek, start, peekdatacount);
					}
				}
			}
			if (r == 0)
			{
				return 0;
			}
		}

		int len = GetUnProcessDataSize();
		if (len <= 16000 / 2)
		//if (peekdatacount < 16000 / 2)
		{
			//Log.i("wg", "3 too little UnProcessData, " + peekdatacount + "...");

			return 0;
		}

		//Log.i("wg", "4 OneFrameOk in...");

//		// PowerLine
//		{
//			int plstart = powerline_CurPos;
//			int plend = AudioDataBuf_CurPos / powern - 1;
//			//r = CalcPowerLine(AudioDataPeek, plstart, plend, powern, powerline);//AudioDataPeek
//			r = CalcPowerLine(AudioDataBuf, plstart, plend, powern, powerline);//AudioDataPeek
//			if (plend >= 0)
//			{
//				powerline_CurPos = plend;
//			}
//		}

		// Key recon
		//r = KeysProcess(AudioDataPeek, peekdatacount, mLQ.powern, PowerlineDataPeek);

		int c = mLQ.AudioDataBuf_CurPos;
		int AudioDataBufCount_int = c - (c % mLQ.powern);
		//long Tbase = (mLQ.mDiscardDataNum) / 16;
		r = KeysProcess(mLQ.AudioDataBuf, mLQ.AudioDataBuf_CurPos - 1, AudioDataBufCount_int,
				mLQ.powern, mLQ.powerline);
		if (r == 0)
		{
			//Log.i("wg", "5 OneFrameOk in... fail");

			return 0;
		}

		//Log.i("wg", "5 OneFrameOk in...");

		// 删除旧数据
		//mCQ.Reset();

		return 0;
	}

	// 多个key
	int CalcPowerLine(short pWaveData[], int plstart, int plend, int powern, int powerline[])
	{
		int r = 0;
		if (plend < plstart)
		{
			return 0;
		}
		r = GetPowerLine(pWaveData, plstart, plend, powern, powerline);	//pWaveData
		if (r == 0)
		{
			// ASSERT(FALSE);
			return 0;
		}

		return 1;
	}

	int InitKey88()
	{
		int i = 0;

		key88[i++] = (float)27.5        ;
		key88[i++] = (float)29.13523509 ;
		key88[i++] = (float)30.86770633 ;
		key88[i++] = (float)32.70319566 ;
		key88[i++] = (float)34.64782887 ;
		key88[i++] = (float)36.70809599 ;
		key88[i++] = (float)38.89087297 ;
		key88[i++] = (float)41.20344461 ;
		key88[i++] = (float)43.65352893 ;
		key88[i++] = (float)46.24930284 ;
		key88[i++] = (float)48.9994295  ;
		key88[i++] = (float)51.9130872  ;
		key88[i++] = (float)55          ;
		key88[i++] = (float)58.27047019 ;
		key88[i++] = (float)61.73541266 ;
		key88[i++] = (float)65.40639133 ;
		key88[i++] = (float)69.29565774 ;
		key88[i++] = (float)73.41619198 ;
		key88[i++] = (float)77.78174593 ;
		key88[i++] = (float)82.40688923 ;
		key88[i++] = (float)87.30705786 ;
		key88[i++] = (float)92.49860568 ;
		key88[i++] = (float)97.998859   ;
		key88[i++] = (float)103.8261744 ;
		key88[i++] = (float)110         ;
		key88[i++] = (float)116.5409404 ;
		key88[i++] = (float)123.4708253 ;
		key88[i++] = (float)130.8127827 ;
		key88[i++] = (float)138.5913155 ;
		key88[i++] = (float)146.832384  ;
		key88[i++] = (float)155.5634919 ;
		key88[i++] = (float)164.8137785 ;
		key88[i++] = (float)174.6141157 ;
		key88[i++] = (float)184.9972114 ;
		key88[i++] = (float)195.997718  ;
		key88[i++] = (float)207.6523488 ;
		key88[i++] = (float)220         ;
		key88[i++] = (float)233.0818808 ;
		key88[i++] = (float)246.9416506 ;
		key88[i++] = (float)261.6255653 ;
		key88[i++] = (float)277.182631  ;
		key88[i++] = (float)293.6647679 ;
		key88[i++] = (float)311.1269837 ;
		key88[i++] = (float)329.6275569 ;
		key88[i++] = (float)349.2282314 ;
		key88[i++] = (float)369.9944227 ;
		key88[i++] = (float)391.995436  ;
		key88[i++] = (float)415.3046976 ;
		key88[i++] = (float)440         ;
		key88[i++] = (float)466.1637615 ;
		key88[i++] = (float)493.8833013 ;
		key88[i++] = (float)523.2511306 ;
		key88[i++] = (float)554.365262  ;
		key88[i++] = (float)587.3295358 ;
		key88[i++] = (float)622.2539674 ;
		key88[i++] = (float)659.2551138 ;
		key88[i++] = (float)698.4564629 ;
		key88[i++] = (float)739.9888454 ;
		key88[i++] = (float)783.990872  ;
		key88[i++] = (float)830.6093952 ;
		key88[i++] = (float)880         ;
		key88[i++] = (float)932.327523  ;
		key88[i++] = (float)987.7666025 ;
		key88[i++] = (float)1046.502261 ;
		key88[i++] = (float)1108.730524 ;
		key88[i++] = (float)1174.659072 ;
		key88[i++] = (float)1244.507935 ;
		key88[i++] = (float)1318.510228 ;
		key88[i++] = (float)1396.912926 ;
		key88[i++] = (float)1479.977691 ;
		key88[i++] = (float)1567.981744 ;
		key88[i++] = (float)1661.21879  ;
		key88[i++] = (float)1760        ;
		key88[i++] = (float)1864.655046 ;
		key88[i++] = (float)1975.533205 ;
		key88[i++] = (float)2093.004522 ;
		key88[i++] = (float)2217.461048 ;
		key88[i++] = (float)2349.318143 ;
		key88[i++] = (float)2489.01587  ;
		key88[i++] = (float)2637.020455 ;
		key88[i++] = (float)2793.825851 ;
		key88[i++] = (float)2959.955382 ;
		key88[i++] = (float)3135.963488 ;
		key88[i++] = (float)3322.437581 ;
		key88[i++] = (float)3520        ;
		key88[i++] = (float)3729.310092 ;
		key88[i++] = (float)3951.06641  ;
		key88[i++] = (float)4186.009045 ;

		return 1;
	}

	// 频率到key号(1~88)的映射, 简单版本, 绝对比较
	// return 1~88;
	int KeyFs2N(float KeyFs)
	{
		int n = 0;
		float s = (float)1.05946309435929;

		float key;
		//float dl, dr;
		float d;
		float minFdiff = 10000;
		int minFN = -1;
		for(int i = 0; i < 88; i++)
		{
			d = abs(KeyFs - key88[i]);
			if (minFdiff > d)
			{
				minFdiff = d;
				minFN = i + 1;
			}
		}

		return minFN;
	}

	// 记录一个Key
	int RecordAKey(int idx, float result[], long keyT_tmp, int mDiscardPLNum, int HitPos[], int HitFFTPos[], int HitEndPos[])
	{
		if (idx >= MaxKeyCount)
		{
			return 0;
		}

		KeyFs[idx] = (float)result[1];
		KeyN[idx] = KeyFs2N(KeyFs[idx]); //(int)result[2];
		KeyT[idx] = keyT_tmp;
		KeyLong[idx] = (int)result[3];
		KeyFFTSize[idx] = (int)result[4];

		KeyHitPos[idx] = (int)HitPos[0] + mDiscardPLNum;
		KeyHitFFTPos[idx] = (int)HitFFTPos[0] + mDiscardPLNum;
		KeyHitEndPos[idx] = (int)HitEndPos[0] + mDiscardPLNum;

		// harmnic
		int harmOffset =  5 + 4096;
		int harmOfN = idx * harmnoicSeg;
		for(int h = 0; h < harmnoicSeg; h++)
		{
			KeyHarmnic[harmOfN + h] = result[harmOffset + h];
		}

		//KeyCount++;

		return 1;
	}

	// 弹出旧Key
	int PopFrontKey(int PopCount)
	{
		if (PopCount > KeyCount
				|| PopCount > MaxKeyCount
				|| KeyCount > MaxKeyCount)
		{
			return 0;
		}

		int c = KeyCount - PopCount;
		for(int i = 0; i < c; i++)
		{

			KeyFs[i] = KeyFs[i + PopCount];
			KeyN[i] = KeyN[i + PopCount];
			KeyT[i] = KeyT[i + PopCount];
			KeyLong[i] = KeyLong[i + PopCount];
			KeyFFTSize[i] = KeyFFTSize[i + PopCount];

			KeyHitPos[i] = KeyHitPos[i + PopCount];
			KeyHitFFTPos[i] = KeyHitFFTPos[i + PopCount];
			KeyHitEndPos[i] = KeyHitEndPos[i + PopCount];

			// harmnic
			int harmOfN = i * harmnoicSeg;
			int harmOfNSrc = (i + PopCount) * harmnoicSeg;
			for(int h = 0; h < harmnoicSeg; h++)
			{
				KeyHarmnic[harmOfN + h] = KeyHarmnic[harmOfNSrc + h];
			}
		}

		KeyCount -= PopCount;

		mScroll = 1; // 滚动标记

		return 1;
	}

	// 多个key
	int KeysProcess(short pWaveData[], int AudioDataCount, int AudioDataBufCountint, int powern, int powerline[])
	{
		int r = 0;
		float result[] = null;

		// hit
		int HitPos[] = new int[2]; // -1, 在powerline中的位置
		int HitMaxPower[] = new int[2]; // {0};
		int HitEndPos[] = new int[2]; // {-1};
		int HitFFTPos[] = new int[2]; // {0};
		int HitCount[] = new int[1];
		int inHit = 0;
		//float k = 0;
		float kHit = 2500;	// cfg...
		int PreHitPos[] = new int[1];
		PreHitPos[0] = -1;

		int mDiscardDataNum = mLQ.mDiscardDataNum;
		int mDiscardPLNum = mLQ.mDiscardDataNum / mLQ.powern;
		long Tbase = (mDiscardDataNum) / 16;

		int plstart = 0; //mLQ.AudioDataBuf_CurPos / powern;
		int plend = AudioDataCount / powern; // (AudioDataBufCountint - 1) / powern;
		plend -= 1;	// 尾部数据可能不保险, 暂时不用

		if (plend < plstart)
		{
			plend = plstart;
		}
		if (plend == plstart)
		{
			return 0;
		}

		if (true)
		{
			// find hit start
			GetHits(powerline, plstart, plend,
					PreHitPos, kHit,
					HitPos, HitEndPos, HitFFTPos, HitMaxPower, HitCount);
			//KeyCount = HitCount[0];

			if (true)
			{
				try
				{
					// find hit end
					if (HitCount[0] > 0)
					{
						int datanum = 0;

						if (HitCount[0] == 1)
						{
							// 本次检测到一个key

							//datanum = AudioDataBufCountint - HitFFTPos[0] * powern;
							//datanum = AudioDataCount - HitFFTPos[0] * powern;
							datanum = AudioDataCount - HitPos[0] * powern;
							if (datanum < 16000 / 4) // 2) // 2 * 16000)
							{
								//Log.i("wg", "GetHits, datanum:" + datanum + ".....................");

								// 若是最后一个key, 要求等待时间够长, 才算结束
								return 0;
							}
							else
							{
								//Log.i("wg", "GetHits, datanum:" + datanum);
							}
							if (HitFFTPos[0] == -1)
							{
								// 尚没有制定本hit的FFT位置
								return 0;
							}
							else if (KeyCount > 0)
							{
								// 本hit已经计算过fft
								if (KeyHitPos[KeyCount - 1] == HitPos[0] + mDiscardPLNum)
								{
									return 0;
								}
							}

						}
						else if (HitCount[0] >= 2)
						{
							// 多个key
							datanum = ((HitPos[1] - 1) - HitFFTPos[0]) * powern;
						}

						// 单/多个key
						{
							// 保护
							if (KeyCount >= MaxKeyCount)
							{
								//return 0;
								PopFrontKey(lineMember);
							}

							//for(int j = 0; j < HitCount[0]; j++)
							int j = 0;	// 仅取首个key
							{
								long keyT_tmp;	// key开始时刻, 相对
								keyT_tmp = (long)HitPos[0] * powern / 16000 * 1000 + Tbase;

								// prepare params
								int dataget = min(datanum, 4096 + 10);
								int posData = 0;	// 有效数据点
								if (false)
								{
									// 取fft数据方式一
									posData = HitFFTPos[j] * powern;
								}
								else
								{
									// 取fft数据方式二, 从powerline最高点附近计算fft
									short maxV = 0;
									int maxPos = 0;
									posData = HitPos[j] * powern;
									for(int k = 0; k < powern; k++)
									{
										if (maxV < pWaveData[k + posData])
										{
											maxV = pWaveData[k + posData];
											maxPos = k + posData;
										}
									}
									posData = maxPos + 160;	// + 160, 前移10ms
								}

								// copy fft数据, 准备FFT计算
								for(int k = 0; k < dataget; k++)
								{
									FFTInputBuf[k] = pWaveData[k + posData];
								}
//								for(int k = dataget; k < 4096 + 10; k++)
//								{
//									FFTInputBuf[k] = pWaveData[(k % dataget) + posData];
//								}

								////////////////////////////////////////////// ljh ////////////////////////////

								// copy fft数据, 数据不足, z方式补充copy数据
								if (dataget < 4096 + 10)
								{
									int dir = -1; // z方式copy数据
									int d = 0;
									for(int k = dataget; k < 4096 + 10; k++, d++)
									{
										if (dir == -1)
										{
											// 反向copy数据
											FFTInputBuf[k] = pWaveData[(k - d) + posData];
										}
										else
										{
											// 正向copy数据
											FFTInputBuf[k] = pWaveData[(k + d) + posData];
										}

										if (d >= dataget)
										{
											// 调转方向
											dir *= -1;

											d = 0;
										}
									}
								}

								// key recon
								result = ObEPianoAndroidJavaAPI.KeyRecon(FFTInputBuf);

								if (result != null)
								{
									if (result[0] > 0)
									{
										if (KeyCount == 0) // HitCount[0] > 0)
										{
											// 自识别开始以来首个key

											RecordAKey(KeyCount, result, keyT_tmp, mDiscardPLNum, HitPos, HitFFTPos, HitEndPos);

											KeyCount++;
										}
										else
										{
											// 非首个key

											int lastkeyid = KeyCount - 1;
											long tdiff = keyT_tmp - KeyT[lastkeyid];
											if (tdiff < 0)
											{
												tdiff = -tdiff;
											}

											if (tdiff < 100)
											{
												// update same key
												if (false)
												{
													RecordAKey(lastkeyid, result, keyT_tmp, mDiscardPLNum, HitPos, HitFFTPos, HitEndPos);
												}
											}
											else
											{
												// 上一key
												int HitFFTPost[] = new int[1];
												int HitEndPost[] = new int[1];
												HitFFTPost[0] = 0;
												HitEndPost[0] = 0;
												GetFFTHitPos(powerline, HitMaxPower[0],
														KeyHitPos[KeyCount - 1], HitPos[0],
														HitFFTPost, HitEndPost);
												//KeyN[KeyCount - 1] = (int)HitFFTPos[0] + plstart;
												KeyHitFFTPos[KeyCount - 1] = (int)HitFFTPost[0];

												RecordAKey(KeyCount, result, keyT_tmp, mDiscardPLNum, HitPos, HitFFTPos, HitEndPos);
												KeyCount++;
											}
										}
									}
								}
							}

							//CharSequence strText = "成功:" + (int)result[0] + ", F:" + (int)result[1] + ", Fn:" + (int)result[2] + ", L:" + (int)result[3] + ", n" + (int)result[4] + ".\n";
							//Toast.makeText(MusicScore.this, strText, 200).show();
						}

						// 老化数据
						if (HitCount[0] >= 2)
						{
							// 有两个以上hit, 删除头部hit的数据

							int DiscardDataNum = 0;
							int s = HitPos[1];
							s -= 2;
							if (s < 0)
							{
								s = 0;
							}
							if (s < HitPos[0])
							{
								s = HitPos[0] + 1;
							}

							DiscardDataNum = s * powern;
							if (DiscardDataNum > 0)
							{
								DiscardFrontAudioData(DiscardDataNum);
							}
						}
					}
					else
					{
						// 无hit, 删除头部数据

						int DiscardDataNum = 0;
						DiscardDataNum = (plend - 1) * powern;
						if (DiscardDataNum > 0)
						{
							DiscardFrontAudioData(DiscardDataNum);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		return 1;
	}

	// power line
	int GetPowerLine(short pWaveData[], int plstart, int plend, int powern, int powerline[])
	{
		//int powern = 200;
		int maxpower = 0;
		int pwId = 0;

		//int powerline[100000] = {0};					// 包络线
		//int powerLinePointNum = DataNum; // (DSize / 2 - n) / n;	// DSize / 2采样点数,
		int maxPowerInFile = 0;
		{
			int t = plstart * powern;
			int e = plend * powern;
			for(; t < e; t += powern)
			{
				maxpower = 0;

				for(int p = 0; p < powern; p++)		// n
				{
					if (maxpower < abs(pWaveData[p + t]))
					{
						maxpower = abs(pWaveData[p + t]);
					}
				}

				maxPowerInFile = max(maxPowerInFile, maxpower);

				pwId = t / powern;
				powerline[pwId] = maxpower;
			}

			// save
			/*
			{
				char FileName[] = "C:\\powerline.csv";
				FILE * pf;
				pf = fopen(FileName, "w+b");
				if (!pf)
				{
					return;
				}
				for(int i = 0; i < powerLinePointNum; i++)
				{
					pwId = i;
					sprintf(buf, "%d\n", powerline[pwId]);
					fwrite(buf, 1, strlen(buf), pf);
				}
				fclose(pf);
			}
			*/
		}

		return 1;
	}

	// 	找fft hits
	// plstart: 在powerline中的开始位置
	int GetFFTHitPos(int powerline[], int HitMaxPower,
					 int HitPos, int AfterHitEndPos,
					 int HitFFTPos[], int HitEndPos[]) // output
	{
		// cfg
		float kpos = (float)0.4; // 0.2 0.9; // 0.5; //0.9;
		int zoom = 6;

		//int lastHit = HitCount[0] - 1;
		//int i = lastHit;
		int j = 0;
		for(j = HitPos; j < AfterHitEndPos; j++)
		{
			if (powerline[j] * zoom < HitMaxPower)
			{
				HitEndPos[0] = j;
				break;
			}
		}

		if (j == AfterHitEndPos)
		{
			HitEndPos[0] = j;
		}

		HitFFTPos[0] = (int)((1.0 - kpos) * HitPos + kpos * HitEndPos[0]); // / 2;

		return 1;
	}

	// 找hits
	// plstart: 在powerline中的开始位置
	int GetHits(int powerline[], int plstart, int plend, int PreHitPos[], float kHit,
				int HitPos[], int HitEndPos[], int HitFFTPos[], int HitMaxPower[], int HitCount[])
	{
		// hit
		/*
		int HitPos[1000] = {-1};
		int HitMaxPower[1000] = {-1};
		int HitEndPos[1000] = {-1};
		int HitFFTPos[100] = {0};
		int HitCount = 0;
		int inHit = 0;
		float k = 0;
		float kHit = 1500;
		*/
		float k, k1, k2;

		HitCount[0] = 0;

		// cfg
		float kpos = (float)0.1; //0.4; // 0.2 0.9; // 0.5; //0.9;
		int zoom = 6;

		if (plend + 2 >= mLQ.powerlineMaxPoints)
		{
			plend =  mLQ.powerlineMaxPoints - 3;
		}

		//int PreHitPos = -1;
		{
			// find hit start
			for(int i = 0; i < (plend - plstart) - 3 - 1; ) // i++)
			{
				k = ((float)powerline[i + 1 + plstart] - powerline[i + plstart]) / 1;
				k1 = ((float)powerline[i + 2 + plstart] - powerline[i + plstart]) / 1; // 2;
				k2 = ((float)powerline[i + 3 + plstart] - powerline[i + plstart]) / 1; // 3;
				if (k > kHit
						|| k1 > kHit
						|| k2 > kHit)
				{
					// 防止两次hit start太近
					if (PreHitPos[0] >= 0)
					{
						int minKeyGap = 100; // ms
						if (i+1 - PreHitPos[0] <= minKeyGap * 16 / mLQ.powern) // 10  6 // cfg
						{
							i++;
							continue;
						}
					}

					// 找附近包络线的最高点
					int maxP = 0;
					int maxP_pos = -1;
					int j = 0;
					for(j = i + 1; j < i + 4 && j < plend; j++)
					{
						if (maxP < powerline[j + plstart])
						{
							maxP = powerline[j + plstart];
							maxP_pos = j;
						}
					}

					if (maxP_pos == -1)
					{
						//ASSERT(FALSE);
						return 0;
					}

					HitMaxPower[HitCount[0]] = maxP; //powerline[i + 1];
					HitPos[HitCount[0]] = maxP_pos; //i + 1;
					HitEndPos[HitCount[0]] = -1;
					HitFFTPos[HitCount[0]] = -1;

					PreHitPos[0] = HitPos[HitCount[0]];

					(HitCount[0])++;

					//
					if (HitCount[0] >= 2)
					{
						break;
					}

					i = j;
				}
				else
				{
					i++;
				}
			}

			// 无hit
			if (HitCount[0] == 0)
			{
				return 0;
			}

			// find hit end, fft pos
			int j = 0;
			for(int i = 0; i < HitCount[0] - 1; i++)
			{
				for(j = HitPos[i]; j < HitPos[i + 1]; j++)
				{
					if (powerline[j + plstart] * zoom < HitMaxPower[i]) // 3
					{
						HitEndPos[i] = j;
						break;
					}
				}
				if (j == HitPos[i + 1])
				{
					HitEndPos[i] = j;
				}

				//HitFFTPos[i] = (HitPos[i] + HitEndPos[i]) / 2;
				HitFFTPos[i] = (int)((1.0 - kpos) * HitPos[i] + kpos * HitEndPos[i]); // / 2;

				// 如果HitFFTPos离下一个Key太近, HitFFTPos向前推
				int dis2Next = (HitPos[i + 1] - HitFFTPos[i]) * mLQ.powern;
				int dis2End = (HitEndPos[i] - HitFFTPos[i]) * mLQ.powern;
				int dis2Begin = (HitFFTPos[i] - HitPos[i]) * mLQ.powern;
				if (dis2Next < 2048)
				{
					//HitFFTPos[i] = HitPos[i + 1] - 1024;
					Log.e("wg", "HitFFTPos离下一个Key太近, dis2Next:" + dis2Next + ", dis2Begin:" + dis2Begin + ", dis2End:" + dis2End + ".................");
				}
				else
				{
					//HitFFTPos[i] = HitPos[i + 1] - 1024;
					//Log.e("wg", "HitFFTPos离下一个Key, dis2Next:" + dis2Next + ", dis2Begin:" + dis2Begin + ", dis2End:" + dis2End + ".");
				}
			}

			// last HitEndPos and HitFFTPos
			if (true)
			{
				int lastHit = HitCount[0] - 1;
				int i = lastHit;
				j = 0;
				for(j = HitPos[i]; j < plend; j++)
				{
					if (powerline[j + plstart] * zoom < HitMaxPower[i])
					{
						HitEndPos[i] = j;

						HitEndPos[i] = plend - 1;
						HitFFTPos[i] = (int)((1.0 - kpos) * HitPos[i] + kpos * HitEndPos[i]); // / 2;

						break;
					}
				}
				if (j == plend)
				{
					//HitEndPos[i] = plend - 1;
					HitEndPos[i] = -1;
					HitFFTPos[i] = -1;
				}

			}
		}

		return 1;
	}

	/**
	 * 向上取最接近iint的2的幂次数.比如iint=320时,返回256
	 * @param iint
	 * @return
	 */
	private int up2int(int iint) {
		int ret = 1;
		while (ret<=iint) {
			ret = ret << 1;
		}
		return ret>>1;
	}


	//快速傅里叶变换
	public static final float pi= (float) 3.1415926;
	public void fft(Complex[] xin, int N)
	{
		int f,m,N2,nm,i,k,j,L;//L:运算级数
		float p;
		int e2,le,B,ip;
		Complex w = new Complex();
		Complex t = new Complex();
		N2 = N / 2;//每一级中蝶形的个数,同时也代表m位二进制数最高位的十进制权值
		f = N;//f是为了求流程的级数而设立的
		for(m = 1; (f = f / 2) != 1; m++);                             //得到流程图的共几级
		nm = N - 2;
		j = N2;
		/******倒序运算——雷德算法******/
		for(i = 1; i <= nm; i++)
		{
			if(i < j)//防止重复交换
			{
				t = xin[j];
				xin[j] = xin[i];
				xin[i] = t;
			}
			k = N2;
			while(j >= k)
			{
				j = j - k;
				k = k / 2;
			}
			j = j + k;
		}
		/******蝶形图计算部分******/
		for(L=1; L<=m; L++)                                    //从第1级到第m级
		{
			e2 = (int) Math.pow(2, L);
			//e2=(int)2.pow(L);
			le=e2+1;
			B=e2/2;
			for(j=0;j<B;j++)                                    //j从0到2^(L-1)-1
			{
				p=2*pi/e2;
				w.real = Math.cos(p * j);
				//w.real=Math.cos((double)p*j);                                   //系数W
				w.image = Math.sin(p*j) * -1;
				//w.imag = -sin(p*j);
				for(i=j;i<N;i=i+e2)                                //计算具有相同系数的数据
				{
					ip=i+B;                                           //对应蝶形的数据间隔为2^(L-1)
					t=xin[ip].cc(w);
					xin[ip] = xin[i].cut(t);
					xin[i] = xin[i].sum(t);
				}
			}
		}
	}



	int abs(int data)
	{
		if (data < 0)
			return -data;

		return data;
	}

	float abs(float data)
	{
		if (data < 0)
			return -data;

		return data;
	}

	int max(int a, int b)
	{
		if (a > b)
		{
			return a;
		}

		return b;
	}

	int min(int a, int b)
	{
		if (a < b)
		{
			return a;
		}

		return b;
	}



}




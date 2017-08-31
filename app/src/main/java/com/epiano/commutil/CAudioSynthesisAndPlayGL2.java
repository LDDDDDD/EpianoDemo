package com.epiano.commutil;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;
import android.widget.ProgressBar;

import java.io.IOException;


public class CAudioSynthesisAndPlayGL2 {

	enum BUF_STATUS			// 翻连续页状态控制
	{
		BUF_STATUS_NULL,
		BUF_STATUS_SYNTHED,		// 合成完
		BUF_STATUS_PLAYED,		// 播放完
	}


	final float PI = (float) 3.14159265;

	ProgressBar pb;					// play进度指示
	boolean StartToPlay = false; 	// 启动状态;
	boolean PausePlay = false; 		// 暂停状态;

	int PauseTime = 0;
	AudioTrack audioTrack = null;
	int AudioChannels = 2; // 1;		// 必须为2, 为处理方便, 即使是单声道也用双声道数据来表示
	int StereoOn = 1; // 0: close, 1: on;
	int AutoScrollPageOnPlay = 1; // 播放时自动翻页, cfg...

	boolean ForceHoofSwitch = true; // 强制踏板/延音, cfg...

	int CurPosIndicatorId = -1;
	int PrePosIndicatorId = -1;		// 琶音情况下可能出现indicator短暂倒退的情况, 用此变量防止倒退现象
	int mPosIndicator[]; // Play位置指示
	long mStartPlayTime = -1;
	int mRealPlaycount = 0;
	int audioTrackBufferDelay = 0;
	int PosIndicatorRes = 100; // ms
	final int SAMPLE_RATE = 16000; // 11025;
	MusicScoreBook3DGL2.PosIndicateTaskGL2 IndiTask;

	int MaxPlayKeyId = 30000;
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

	//public PicView pv[];

	String openfilename;

	CKeyAudio mKeyAudio[]; // = new CKeyAudio[88];

	CAudioSythThread mAudioSythThread;
	CAudioPlayThread mAudioPlayThread;

	int AudioChannelsType = AudioFormat.CHANNEL_OUT_MONO;
	int playstarted = 0;
	int SaveWaveCfg = 1; // 保存wavefile开关 cfg...
	boolean SaveWaved = false; // 已保存?

	static int MAX_SONG_LONG_IN_MS = 600 * 1000;	// 600 秒

	// 单合成缓冲时间长度(s)
	int SynthesBufMaxLong = 60000; // 600000 300000 120000 10000 300 s
	// 单合成缓冲单通道字节容量
	int SynthesBufMaxLen = (SAMPLE_RATE / 1000) * SynthesBufMaxLong; //
	// 单合成缓冲多通道字节容量
	int SynthesBufMaxLen_Channel = SynthesBufMaxLen	* AudioChannels;

	// 双缓冲机制
//	short[] buffer0 = new short[SynthesBufMaxLen_Channel]; // 合成数据缓冲区
//	short[] buffer1 = new short[SynthesBufMaxLen_Channel]; // 合成数据缓冲区
//	int buffer_offset = 0;
//	int MaxDataSizeInBuf[] = new int [2];
	CAudioBuf mAudioBuf[];

	// 当前合成缓冲
//	short[] CurSynthBuffer = buffer0;
	int CurSynthBufferId = -1;
	// 当前播放缓冲
//	short[] CurPlayBuffer = buffer0;
	int CurPlayBufferId = -1;

	// 指示器时间长度
	//int PosIndicatorMaxSize = SynthesBufMaxLong / PosIndicatorRes * 100;
	int PosIndicatorMaxSize = MAX_SONG_LONG_IN_MS / PosIndicatorRes; // * 100;
	// play位置指示(PlayKeyTick[]数组索引)
	//int PosIndicator[];

	float tk = (float) 1.2; // 时长伸缩系数, 1.5;

	int PlayedDataCountSum = 0; // 累计播放的数据量
	long PlayTimeSum = 0; // 累计播放的时间
	long TotalPlayTime = 0; // 指定区间播放时长
	int TotalPlayDataCount = 0;
	float playedDataRatio = 0;		// 0~100 进度, 按字节算
	float PlayprogressByTime = 0; 	// 0~1000 进度, 按时间算

	//float playprogress = 0;

	public class CAudioBuf {

		int id;

		short[] buffer; 			// 合成数据缓冲区

		int MaxDataSize = 0;		// 单通道的数据量

		int SynthDataSize = 0;		// 当前实际合成数据长度, 从idx 0算起, 单通道的, 全部数据, full数据后部的数据是不完整的数据
		int SynthDataSize_full = 0;	// 当前实际合成数据长度, 从idx 0算起, 单通道的, full指数据是完整的, full后的数据并不完整, 需要copy到下一缓冲区的首部
		int KeyId_full;				// full数据尾部对应的keyId;
		int PlayDataSize = 0;		// 当前实际播放数据长度, 从idx 0算起, 单通道的

		int SynthDone = 0;			// 完成合成的标记
		int PlayDone = 0;			// 完成播放的标记

		//long buffer_baseT = 0;	// 已经合成或播放的位置
		int buffer_KeyId = 0;		// buffer首位置对应的KeyId

		int AudioChannels = 1;


		BUF_STATUS BufStatus = BUF_STATUS.BUF_STATUS_NULL;

		// 构造函数
		public CAudioBuf()
		{

		}

		public int SetParam(int MaxDataSizeOneChannelIn, int AudioChannelsIn)
		{
			AudioChannels = AudioChannelsIn;
			MaxDataSize = MaxDataSizeOneChannelIn;
			buffer = new short[MaxDataSize * AudioChannels];

			return 1;
		}

		int reset()
		{
			SynthDataSize = 0;
			PlayDataSize = 0;
			//buffer_baseT = 0;
			buffer_KeyId = 0;

			int datac = MaxDataSize * AudioChannels;
			for(int i = 0; i < datac; i++)
			{
				buffer[i] = 0;
			}

			return 1;
		}

		// 计算尾部数据, (需要copy到下一个合成对象)
		int GetSynthTailDataCount()
		{
			int c = SynthDataSize - SynthDataSize_full;

			return c;
		}

		// 从一个有尾部数据的对象(AudioBuf_Tail)copy尾部数据
		int CopyTailData(CAudioBuf AudioBuf_Tail)
		{
			int c = AudioBuf_Tail.GetSynthTailDataCount() * AudioChannels;
			int offset = AudioBuf_Tail.SynthDataSize_full * AudioChannels;
			for(int i = 0; i < c; i++)
			{
				buffer[i] = AudioBuf_Tail.buffer[i + offset];
			}

			return 1;
		}

		int SynthDone()
		{
			if (SynthDataSize * 2.0 > MaxDataSize)	//
			{
				return 1;
			}

			return 0;
		}

		int PlayDone_full()
		{
//			Log.i("WG", "PlayDone(), PlayDataSize:" + PlayDataSize
//					+ ", SynthDataSize:" + SynthDataSize
//					+ ", t:" + PlayDataSize / (16));

			if (PlayDataSize >= SynthDataSize_full)	//
			{
				return 1;
			}

			return 0;
		}

		int PlayDone()
		{
//			Log.i("WG", "PlayDone(), PlayDataSize:" + PlayDataSize
//					+ ", SynthDataSize:" + SynthDataSize
//					+ ", t:" + PlayDataSize / (16));

			if (PlayDataSize >= SynthDataSize)	//
			{
				return 1;
			}

			return 0;
		}
	}

	// 构造函数
	public CAudioSynthesisAndPlayGL2(String openfilenameIn, ProgressBar pbIn, int StereoOnIn) //, CKeyAudio KeyAudio[])
	{
		openfilename = openfilenameIn;
		pb = pbIn;
		//mKeyAudio = KeyAudio;
		StereoOn = StereoOnIn;


		// 声道数量配置
		if (AudioChannels == 1) {
			AudioChannelsType = AudioFormat.CHANNEL_OUT_MONO;
		} else if (AudioChannels == 2) {
			AudioChannelsType = AudioFormat.CHANNEL_OUT_STEREO;
		} else {
			Log.i("WG", "Error, AudioChannels:" + AudioChannels);

			return;
		}

		// 加载所有key的音频数据，生成立体声数据
		if (mKeyAudio == null) {
//			int r = LoadKeyAudio(AudioChannels);
//			if (r == 0) {
//				// Toast.makeText(MusicScoreBook3D.this,
//				// "Can't load key audio.", 200).show();
//
//				Log.i("WG", "Error, fail to Load audio");
//
//				return;
//			}
			//return;
		}

		// 打开系统音频播放模块
		// final int SAMPLE_RATE = 16000; // 11025;
		int minSize = AudioTrack.getMinBufferSize(SAMPLE_RATE, //
				AudioChannelsType, // AudioFormat.CHANNEL_IN_MONO,// 常量：// 16（0x00000010）
				AudioFormat.ENCODING_PCM_16BIT);// PCM音频数据格式：16位，每样
		minSize *= 2; //3; //4; // 4;
		audioTrackBufferDelay = minSize / SAMPLE_RATE * 1000 / AudioChannels; // ms 缓冲造成的时延
		audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,// 媒体音量 AudioTrack
				SAMPLE_RATE,//
				AudioChannelsType, // AudioFormat.CHANNEL_OUT_MONO,//
				// 常量： 16（0x00000010）
				AudioFormat.ENCODING_PCM_16BIT, // PCM音频数据格式：每个采样点16比特
				(int) (minSize),// 3 10 2
				// MODE_STATIC 音频数据放到一个固定的buffer, 然后一次写入
				// MODE_STREAM 通过write方式把数据一次一次得写到audiotrack中
				AudioTrack.MODE_STREAM // MODE_STATIC // MODE_STREAM//
		);

		mAudioBuf = new CAudioBuf[2];
		mAudioBuf[0] = new CAudioBuf();
		mAudioBuf[1] = new CAudioBuf();
		mAudioBuf[0].id = 0;
		mAudioBuf[1].id = 1;
		mAudioBuf[0].SetParam(SynthesBufMaxLen_Channel / AudioChannels, AudioChannels);
		mAudioBuf[1].SetParam(SynthesBufMaxLen_Channel / AudioChannels, AudioChannels);
		CurSynthBufferId = -1;
		CurPlayBufferId = -1;

		mPosIndicator = new int[PosIndicatorMaxSize];
		//mPosIndicator = PosIndicator;

		PauseTime = 0;
	}

	// 启动合成与播放线程
	public void StartAudioSynthesisThread(CKeyAudio KeyAudio[])
	{
		mKeyAudio = KeyAudio;

		TotalPlayTime = ratioT(tk, PlayKeyTick[PlayKeyCount - 1])
				+ ratioT(tk, (int) PlayKeyDuration[PlayKeyCount - 1]);
		TotalPlayDataCount = (int) (TotalPlayTime * SAMPLE_RATE / 1000);

		// 检查Key数量
		if (PlayKeyCount <= 0) {
			Log.i("WG", "Error, PlayKeyCount:" + PlayKeyCount);

			return;
		}

		playstarted = 0;
//		PauseTime = 0;
//		mRealPlaycount = 0;
		PlayedDataCountSum = 0; // 累计播放的数据量
		PlayTimeSum = 0; // 累计播放的时间
		//TotalPlayTime = 0; // 指定区间播放时长
		//TotalPlayDataCount = 0;
		playedDataRatio = 0;

		mAudioBuf[0].reset();
		mAudioBuf[1].reset();

		mAudioSythThread = new CAudioSythThread();
		mAudioSythThread.start();

		mAudioPlayThread = new CAudioPlayThread();
		mAudioPlayThread.start();

	}

	// 缩放t
	int ratioT(float k, int TickOrg) {
		int tickR = (int) (TickOrg * k);

		return tickR;
	}


	// 播放线程
	public class CAudioPlayThread extends Thread {

		int taillong = 20; // 10; //20; //10; // ms cfg...
		int tailLenInShorts = taillong * (SAMPLE_RATE / 1000);
		short[] buffer_tail = new short[tailLenInShorts * 5 * AudioChannels]; // Key尾部数据修剪缓冲区

		int actDataLenInBuffer = 0;
		int CurSynthKeyId = 0; // 指向key集合中的当前合成位置

		long t0 = System.currentTimeMillis();
		long curT = t0;
		//long buffer_baseT = t0; // 缓冲区首成员对应的t
		long prepareT = t0; // 数据准备到什么时刻
		long minPrepareDataLong = 1; // 1; //100; //2000; //最小准备数据长度, ms

//		float tk = (float) 1.2; // 时长伸缩系数, 1.5;

//		int PlayedDataCountSum = 0; // 累计播放的数据量
//		long PlayTimeSum = 0; // 累计播放的时间
//		long TotalPlayTime = 0; // 指定区间播放时长
//
//		float playedDataRatio = 0;

		// 计算耗时统计
		long Tsyt = 0; // 合成耗时
		long Twriteaduio = 0; // 写audio数据到驱动耗时
		long Tshiftaduio = 0; // 移动缓冲区数据耗时
		//
		int pause2resume = 0; // 当从pause状态切换到play状态时，会产生尖声,

		// public static void main(String args[]){
		//
		// }

		CAudioBuf PlayBufObj;

		// 缩放t
		int ratioT(float k, int TickOrg) {
			int tickR = (int) (TickOrg * k);

			return tickR;
		}

		public void run() {

			while (StartToPlay)
			{

				//SynthBufObj.BufStatus = BUF_STATUS.BUF_STATUS_SYNTHED;


				// 从两个buf中选择一个用于播放, ->mAudioBuf[CurPlayBufferId]
				int foundSynBuf = 0;
				for(int i = 0; i < 2; i++)		// 两个buf
				{
					if (mAudioBuf[i].BufStatus == BUF_STATUS.BUF_STATUS_SYNTHED)
					{
						CurPlayBufferId = i;

						foundSynBuf = 1;

						break;
					}
				}
				if (foundSynBuf == 0)
				{
					try {
						// if (counter % 100 == 0)
						{
							Log.i("WG", "Warning, Can't get buf to play");
							Thread.sleep(50); // 10);
						}
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					continue;
				}
				PlayBufObj = mAudioBuf[CurPlayBufferId];

				// 另一个缓冲, 用于结尾判断
				CAudioBuf OtherBufObj = mAudioBuf[1 - CurPlayBufferId];

				// play
				if (playstarted == 0) {
					playstarted = 1;

					t0 = System.currentTimeMillis();
					curT = t0;
//					SynthBufObj.buffer_baseT = t0;

					audioTrack.play();
				}

//				Log.i("WG", "P-begin------------CurPlayBufferId:" + CurPlayBufferId
//						+ ", buffer_KeyId:" + PlayBufObj.buffer_KeyId);

				//while(PlayBufObj.PlayDone() == 0 && StartToPlay)
				while(StartToPlay)
				{

					// 计算播放数据量
					long cT = System.currentTimeMillis();
					long pastT = cT - curT;
					// if (pastT < 10)
					if (false) //
					{
						try {
							// if (counter % 100 == 0)
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

					if (PausePlay == true) // 暂停play
					{
						pause2resume = 1;

						try {
							// if (counter % 100 == 0)
							{
								// 把t0向过去推
								long delayt = System.currentTimeMillis();
								Thread.sleep(10); // 10);
								delayt = System.currentTimeMillis()
										- delayt;
								// t0 -= delayt;
								t0 += delayt;
								PauseTime += delayt;
							}
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						continue;
					}

					PlayTimeSum = curT - t0; // 累计播放的时间

					// PlayTimeSum += 100; //300;
					int EstmatePlayDataSum = (int) (PlayTimeSum * (SAMPLE_RATE / 1000)); // 估计该播放的数据量
					// EstmatePlayDataSum += (100 * (SAMPLE_RATE / 1000));
					// // 多播放100ms的数据
					int TryPlaycount = (int) (EstmatePlayDataSum - PlayedDataCountSum);
					int RealPlaycount = 0;
//					if (TryPlaycount > SynthesBufMaxLen) {
//						TryPlaycount = SynthesBufMaxLen;
//					}
//					if (TryPlaycount > actDataLenInBuffer) {
//						TryPlaycount = actDataLenInBuffer;
//					}
					if (OtherBufObj.BufStatus != BUF_STATUS.BUF_STATUS_SYNTHED)
					{
						// 已经到结尾部分

						if (TryPlaycount >= PlayBufObj.SynthDataSize - PlayBufObj.PlayDataSize) {
							TryPlaycount = PlayBufObj.SynthDataSize - PlayBufObj.PlayDataSize;
						}
					}
					else
					{
						if (TryPlaycount >= PlayBufObj.SynthDataSize_full - PlayBufObj.PlayDataSize) {
							TryPlaycount = PlayBufObj.SynthDataSize_full - PlayBufObj.PlayDataSize;
						}
					}
					// TryPlaycount = actDataLenInBuffer;
					// ///////////////////

					{
						// 进度条刷新
						{
							PlayprogressByTime = ((float) PlayTimeSum / TotalPlayTime) * 1000;
							pb.setProgress((int) PlayprogressByTime);
							// publishProgress((int)playprogress);
						}

						// PosIndicator
						// if (false) // 性能原因，改由专门的任务处理
						// {
						// // long PlayTimeSumMinusBufDelay = PlayTimeSum -
						// // audioTrackBufferDelay;
						// long PlayTimeSumMinusBufDelay = (buffer_baseT
						// - t0 + (buffer_offset / (SAMPLE_RATE / 1000)))
						// - audioTrackBufferDelay;
						// PlayTimeSumMinusBufDelay -= 3 * 100; // test,
						// // 指示线快于声音,
						// // 这里补偿对齐一下
						// if (PlayTimeSumMinusBufDelay < 0) {
						// PlayTimeSumMinusBufDelay = 0;
						// }
						// CurPosIndicatorId = ((int)
						// PlayTimeSumMinusBufDelay / PosIndicatorRes);
						// DrawPosIndicator(CurPosIndicatorId,
						// PosIndicator);
						// }

						if (TryPlaycount >= 1)
						{

							// play
							if (true) {
								if (pause2resume == 1) {
									pause2resume = 0;

									// hdr

									int tailLenInShortsTmp = tailLenInShorts * 3;
									if (tailLenInShortsTmp > TryPlaycount) {
										tailLenInShortsTmp = TryPlaycount;
									}
									for (int p = 0; p < tailLenInShortsTmp; p++) {
										buffer_tail[p] = PlayBufObj.buffer[p];
									}
									ClipKeyHdr(buffer_tail,
											tailLenInShortsTmp, 1);
									for (int p = 0; p < tailLenInShortsTmp; p++) {
										PlayBufObj.buffer[p] = buffer_tail[p];
									}
								}

								// 启动位置指示器: mStartPlayTime
								{
									if (mStartPlayTime == -1)
									{
										mStartPlayTime = System.currentTimeMillis();
										mStartPlayTime += 100; // 微调
										//mPosIndicator = PosIndicator;
										mRealPlaycount = RealPlaycount;

										PrePosIndicatorId = -1;

										// 位置指示任务启动
//										PosIndicateTask IndiTask = new PosIndicateTask();
										IndiTask.execute(10);
									}
								}

								// 注入音频数据到驱动层
								Twriteaduio = System.currentTimeMillis();
								RealPlaycount = audioTrack.write(PlayBufObj.buffer,
										PlayBufObj.PlayDataSize * AudioChannels,
										TryPlaycount * AudioChannels); // buffer.length);



								// 统计
								RealPlaycount /= AudioChannels;

//								Log.i("WG", "P-ING------------CurPlayBufferId:" + CurPlayBufferId
//										+ ", RealPlaycount:" + RealPlaycount
//										+ ", TryPlaycount:" + TryPlaycount
//										+ ", d:["
//										+ PlayBufObj.buffer[PlayBufObj.PlayDataSize] + ","
//										+ PlayBufObj.buffer[PlayBufObj.PlayDataSize + 1] + ","
//										+ PlayBufObj.buffer[PlayBufObj.PlayDataSize + 2] + "]"
//										);

								PlayBufObj.PlayDataSize += RealPlaycount;
								Twriteaduio = System.currentTimeMillis() - Twriteaduio;
								PlayedDataCountSum += RealPlaycount;
								playedDataRatio = ((float) PlayedDataCountSum * 100 / TotalPlayDataCount);
								//float playprogress = ((float) PlayTimeSum * 100 / TotalPlayTime);
								// Log.i("WG", "RealPlayed:" +
								// PlayedDataCountSum
								// + ", TotalPlay:" + TotalPlayDataCount
								// + ", playedRatio:" + playedDataRatio
								// + ", progress:" + playprogress);



								if (false) {
									playedDataRatio = ((float) PlayTimeSum * 100 / TotalPlayTime);
									float CurPosIndiT = CurPosIndicatorId * 100;
									//playprogress = ((float) PlayTimeSum * 100 / TotalPlayTime);
									Log.i("WG", "PlayTimeSum:"
											+ PlayTimeSum
											+ ", TotalPlayTime:"
											+ TotalPlayTime
											+ ", playedRatio:"
											+ playedDataRatio
											+ ", CurPosIndiT:"
											+ CurPosIndiT);
								}

							}
							else
							{
								short[] bufferx = { //
										8130, 15752, 22389, 27625, 31134, 32695,
										32210, 29711,
										25354,
										19410,
										12253, //
										4329, -3865, -11818, -19032,
										-25055, -29511, -32121, -32722,
										-31276, -27874, -22728, -16160,
										-8582, -466 //
								};
								audioTrack
										.write(bufferx, 0, bufferx.length); // buffer.length);
							}


							// Log.i("WG", "---");
						}


					}

					// if (TryPlaycount > 0)
					if (false) {
						playedDataRatio = ((float) PlayTimeSum * 100 / TotalPlayTime);
						float CurPosIndiT = CurPosIndicatorId * 100;
						// float playprogress = ((float)PlayTimeSum * 100 /
						// TotalPlayTime);

						Log.i("WG", "lo:"
								//+ lo
								+ ", pastT:"
								+ pastT
								+ ", Tsyt:"
								+ Tsyt
								+ ", Twa:"
								+ Twriteaduio
								+ ", Tsa:"
								+ Tshiftaduio
								// + ", Tryms:" + TryPlaycount /
								// (SAMPLE_RATE / 1000)
								// + ", Realms:" + TryPlaycount /
								// (SAMPLE_RATE / 1000)
								// + ", KeyId:" + CurSynthKeyId
								// + ", buflong:" + actDataLenInBuffer /
								// (SAMPLE_RATE / 1000)
								+ ", PlayTimeSum:" + PlayTimeSum
								+ ", TotalPlayTime:" + TotalPlayTime
								+ ", playedRatio:" + playedDataRatio
								+ ", CurPosIndiT:" + CurPosIndiT);
					}

					// 结束判断
					if (true) {
						if ((CurSynthKeyId >= PlayKeyCount || CurSynthKeyId >= MaxPlayKeyId)
								&& playedDataRatio > 100) // MaxPlayKeyId)
						{
							break;
						}
					}

					if (OtherBufObj.BufStatus != BUF_STATUS.BUF_STATUS_SYNTHED)
					{
						// 已经到结尾部分

						if (PlayBufObj.PlayDone() > 0 || PlayprogressByTime >= 1000)
						{
							playedDataRatio = 100;
							break;
						}
					}
					else
					{
						if (PlayBufObj.PlayDone_full() > 0)
						{
							break;
						}
					}

				}

//				Log.i("WG", "P-----------CurPlayBufferId:" + CurPlayBufferId + ", end");

				PlayBufObj.BufStatus = BUF_STATUS.BUF_STATUS_PLAYED;


				///////////////////////////////////////////////////////////////xxx

				// 关闭Play指示任务
				if (!StartToPlay
						|| playedDataRatio >= 100
						|| PlayprogressByTime >= 1000
						)
				{
					Log.i("WG", "Play over"
							+ ", playedDataRatio:" + playedDataRatio);

					mStartPlayTime = -1;
					synchronized (mPosIndicator) {
						mStartPlayTime = -1;
					}

					StartToPlay = false;

					// mGLSurfaceView.PlayPageId = -1;

					audioTrack.stop();// 停止播放
					audioTrack.release();// 释放底层资源

					pb.setProgress(0);
					// publishProgress(0);

					return;
				}
			}
		}

		// 修剪aduio的head
		// AudioData是tail数据, a二次函数系数, a超大尾越钝
		int ClipKeyHdr(short AudioData[], int DataLenInshorts, float a) {
			// 用二次函数进行修剪, 成以下形状")"

			// 找最大振幅
			short maxA = 0;
			int len = DataLenInshorts; // / 3;
			if (len == 0) {
				len = DataLenInshorts;
			}
			short v = 0;
			for (int i = 0; i < len; i++) {
				if (AudioData[i] > 0) {
					v = AudioData[i];
				} else {
					v = (short) (0 - AudioData[i]);
				}
				// v = Math.abs(AudioData[i]);

				if (maxA < v) {
					maxA = v;
				}
			}

			float k;
			int MaxAOrg = (int) Math.sqrt((DataLenInshorts - 1) / a);
			if (MaxAOrg == 0) {
				return 0;
			}
			k = (float) (maxA / MaxAOrg);

			// clip
			float A = 0; // 动态振幅
			for (int i = 0; i < DataLenInshorts; i++) {
				A = (float) (Math.sqrt((i) / a) * k);
				AudioData[i] = (short) ((float) AudioData[i] * A / maxA);
			}

			return 1;
		}
	}

	// 合成线程
	public class CAudioSythThread extends Thread {

		// audioTrack.play();

		public void run()
		{
			// 合成、播放


			int taillong = 20; // 10; //20; //10; // ms cfg...
			int tailLenInShorts = taillong * (SAMPLE_RATE / 1000);
			short[] buffer_tail = new short[tailLenInShorts * 5 * AudioChannels]; // Key尾部数据修剪缓冲区

			//int actDataLenInBuffer = 0;
			int CurSynthKeyId = 0; // 指向key集合中的当前合成位置

			long t0 = System.currentTimeMillis();
			//long curT = t0;
			//long buffer_baseT = t0; // 缓冲区首成员对应的t
			//long prepareT = t0; // 数据准备到什么时刻
			//long minPrepareDataLong = 1; // 1; //100; //2000; //最小准备数据长度, ms



			// 计算耗时统计
			long Tsyt = 0; // 合成耗时
			//long Twriteaduio = 0; // 写audio数据到驱动耗时
			//long Tshiftaduio = 0; // 移动缓冲区数据耗时

			//int pause2resume = 0; // 当从pause状态切换到play状态时，会产生尖声,
			// 该变量用于控制消除尖声
			// int StopComfirmed = 0; // stop确认, 当StopComfirmed>2时确认结束

			CAudioBuf SynthBufObj;
			//mAudioBuf[0].buffer_baseT = t0;
			//mAudioBuf[1].buffer_baseT = t0;
			mAudioBuf[0].buffer_KeyId = -1;
			mAudioBuf[1].buffer_KeyId = -1;
			// 合成
			int lo = 0;
			while (StartToPlay) // 结束判断
			{
				// 刷新当前时刻
				// curT = System.currentTimeMillis();

				lo++;

				if (CurSynthKeyId >= PlayKeyCount
					//|| CurSynthKeyId >= MaxPlayKeyId
						) // MaxPlayKeyId)
				{
					// 合成完了

					Log.i("WG", "syth break 0, CurSynthKeyId:" +
							CurSynthKeyId + ", PlayKeyCount:" +
							PlayKeyCount);

					break;
				}

				// 从两个buf中选择一个用于合成, ->mAudioBuf[CurSynthBufferId]
				int foundSynBuf = 0;
				for(int i = 0; i < 2; i++)		// 两个buf
				{
					if (mAudioBuf[i].BufStatus == BUF_STATUS.BUF_STATUS_NULL
							|| mAudioBuf[i].BufStatus == BUF_STATUS.BUF_STATUS_PLAYED)
					{
						CurSynthBufferId = i;

						foundSynBuf = 1;

						break;
					}
				}
				if (foundSynBuf == 0)
				{
					try {
						// if (counter % 100 == 0)
						{
							Thread.sleep(100); // 10);
						}
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					continue;
				}
				SynthBufObj = mAudioBuf[CurSynthBufferId];
				SynthBufObj.reset();
				SynthBufObj.buffer_KeyId = CurSynthKeyId;
				SynthBufObj.SynthDataSize = 0;
				SynthBufObj.KeyId_full = -1;
				SynthBufObj.SynthDataSize_full = 0;

				// 从前一个buf(TailObj)中继承尾部数据 ,
				CAudioBuf TailObj = mAudioBuf[1 - CurSynthBufferId];
				int taildatacount = TailObj.GetSynthTailDataCount();
				if (taildatacount > 0)
				{
					SynthBufObj.CopyTailData(TailObj);
					SynthBufObj.buffer_KeyId = TailObj.KeyId_full;
				}

//				Log.i("WG", "-S-begin------------CurSynthBufferId:" + CurSynthBufferId
//						+ ", buffer_KeyId:" + SynthBufObj.buffer_KeyId);

				// 合成，补充数据, 存到SynthBufObj
				Tsyt = System.currentTimeMillis();
				int CurSythKeyTick = 0;	// 当前待合成KeyId的Tick(0 based)
				//CurSythKeyTick = ratioT(tk, PlayKeyTick[CurSynthKeyId]);
				while(SynthBufObj.SynthDone() == 0 && StartToPlay)
				{

//				}
//
//				// 合成，补充数据
//				Tsyt = System.currentTimeMillis();
//				// while(actDataLenInBuffer < minPrepareDataLong *
//				// (SAMPLE_RATE / 1000))
//				int CurSythKeyTick = ratioT(tk, PlayKeyTick[CurSynthKeyId]);
//				while (CurSythKeyTick < (curT - buffer_baseT) + SynthesBufMaxLong) // 5000)
//				{
					// 数据不足，增加数据

					if (CurSynthKeyId >= PlayKeyCount
						//|| CurSynthKeyId >= MaxPlayKeyId
							) // MaxPlayKeyId)
					{
						// 合成完了

						Log.i("WG", "syth break 1, CurSynthKeyId:" +
								CurSynthKeyId + ", PlayKeyCount:" +
								PlayKeyCount);

						break;
					}

//					Log.i("WG", "CurSynthKeyId:" + CurSynthKeyId
//							+ ", PlayKeyCount:" + PlayKeyCount);

					// 把下一时刻所有的key都找到 StartKeyId ~ EndKeyId
					int StartKeyId = CurSynthKeyId;
					int EndKeyId = CurSynthKeyId;
					CurSythKeyTick = ratioT(tk, PlayKeyTick[CurSynthKeyId]); // PlayKeyTick[CurSynthKeyId];
					int CurSythKeyTick_nextkey = 0;
					while (true)
					{
						if (CurSynthKeyId >= PlayKeyCount
//								|| CurSynthKeyId >= MaxPlayKeyId
								)
						{
							// 合成完了

							Log.i("WG", "syth break 2, CurSynthKeyId:"
									+ CurSynthKeyId + ", PlayKeyCount:"
									+ PlayKeyCount);

							break;
						}

						CurSythKeyTick_nextkey = ratioT(tk, PlayKeyTick[CurSynthKeyId]); // PlayKeyTick[CurSynthKeyId];
						if (CurSythKeyTick != CurSythKeyTick_nextkey)
						{
							// Log.i("WG",
							// "syth break 3, CurSynthKeyId:" +
							// CurSynthKeyId + ", PlayKeyCount:" +
							// PlayKeyCount);

							break;
						}

						EndKeyId = CurSynthKeyId;
						CurSynthKeyId++;
					}

					// 合成
					if (StartKeyId < PlayKeyCount && StartKeyId < MaxPlayKeyId)
					{
						long StartKeyT = ratioT(tk, PlayKeyTick[StartKeyId]);
						//long offsetTInBuf = StartKeyT - (SynthBufObj.buffer_baseT - t0); // 当前Key相对于buf起始位置的时间偏移量
						long BufBaseT = ratioT(tk, PlayKeyTick[SynthBufObj.buffer_KeyId]);
						long offsetTInBuf = StartKeyT - BufBaseT; // 当前Key相对于buf起始位置的时间偏移量
						if (offsetTInBuf < 0)
						{
							Log.i("WG", "syth break 4, "
									+ ", StartKeyId:" + StartKeyId
									+ ", StartKeyT:" + StartKeyT
									+ ", BufBaseT:" + BufBaseT
									+ ", CurSynthKeyId:" + CurSynthKeyId
									+ ", PlayKeyCount:"	+ PlayKeyCount);

							break;
						}
						int StartPosInBuffer = (int) ((SAMPLE_RATE / 1000) * offsetTInBuf);
						// long EndKeyIdT = ratioT(tk,
						// PlayKeyTick[EndKeyId]);

						// PosIndicator
//						int StartPosInPosIndicator = (int) (offsetTInBuf / PosIndicatorRes);
//						long offsetEndTInBuf = offsetTInBuf + ratioT(tk, (int) PlayKeyDuration[StartKeyId]);
//						if (EndKeyId + 1 < PlayKeyCount)
//						{
//							offsetEndTInBuf = ratioT(tk, PlayKeyTick[EndKeyId + 1])
//									- (SynthBufObj.buffer_baseT - t0);
//						}
//						int PosIndicatorLen = (int) ((offsetEndTInBuf - offsetTInBuf + (PosIndicatorRes - 1)) / PosIndicatorRes);
//						for (int p = 0; p < PosIndicatorLen; p++)
//						{
//							PosIndicator[StartPosInPosIndicator + p] = StartKeyId;
//						}
//						//long StartKeyT_byT0 = StartKeyT + t0;
//						int StartPosInPosIndicator = (int) (StartKeyT / PosIndicatorRes);
//						long offsetEndTInBuf = StartKeyT + ratioT(tk, (int) PlayKeyDuration[StartKeyId]);
//						if (EndKeyId + 1 < PlayKeyCount)
//						{
//							offsetEndTInBuf = ratioT(tk, PlayKeyTick[EndKeyId + 1])
//									- (SynthBufObj.buffer_baseT - t0);
//						}
//						int PosIndicatorLen = (int) ((offsetEndTInBuf - offsetTInBuf + (PosIndicatorRes - 1)) / PosIndicatorRes);
						int StartPosInPosIndicator = (int) (StartKeyT / PosIndicatorRes);
						if (StartPosInPosIndicator < 0) // 保护
						{
							StartPosInPosIndicator = 0;
						}
						int EndPosInPosIndicator = StartPosInPosIndicator;
						if (EndKeyId + 1 < PlayKeyCount)
						{
							EndPosInPosIndicator = ratioT(tk, PlayKeyTick[EndKeyId + 1]) / PosIndicatorRes;
						}
						int PosIndicatorLen = EndPosInPosIndicator - StartPosInPosIndicator;
						if (PosIndicatorMaxSize < StartPosInPosIndicator + PosIndicatorLen)
						{
							PosIndicatorLen = PosIndicatorMaxSize - StartPosInPosIndicator - 1;
							Log.i("WG", "PosIndicatorLen is not long enough");
						}
						for (int p = 0; p <= PosIndicatorLen; p++)	// 长度检查...
						{
							mPosIndicator[StartPosInPosIndicator + p] = StartKeyId;	// PosIndicator
						}

						// 每个key
						for (int i = StartKeyId; i <= EndKeyId; i++)
						{
							int KeyDataCount = (int) ((PlayKeyDuration[i]) * (SAMPLE_RATE / 1000) * tk * 1.3); // *2 // test

							// 强制踏板效果 cfg...
							if (EndKeyId != PlayKeyCount - 1
									&& ForceHoofSwitch) // 最后一组不要延长， 否则造成结束判断异常
							{
								KeyDataCount *= 6; // 3; //5;
							}

//							Log.i("WG", "key:" + i
//									+ ", StartPosInBufferT:" + StartPosInBuffer / 32
//									+ ", KeyDataCountT:" + KeyDataCount / 32
//									+ ", SynthDataSize:" + SynthBufObj.SynthDataSize);

							if (StartPosInBuffer + KeyDataCount >= SynthesBufMaxLen)
							{
								Log.i("WG", "error. syth buf is too short. sp:"
										+ StartPosInBuffer
										+ ",KeyDataCount:"
										+ KeyDataCount);

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
								// score总长度溢出检查
								if (c + StartPosInBuffer >= TotalPlayDataCount)
								{
									c = TotalPlayDataCount - StartPosInBuffer;
								}

								// for(int p = 0; p < c; p++)
								// {
								// buffer[StartPosInBuffer + p] +=
								// mKeyAudio[key].keyAudio[p];
								// }

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
									for (int p = 0; p < hdrlen; p++)
									{
										SynthBufObj.buffer[StartPosInBuffer + p] += mKeyAudio[key].keyAudio[p];
									}

									// tail
									for (int p = 0; p < tailLenInShortsTmp; p++)
									{
										buffer_tail[p] = mKeyAudio[key].keyAudio[p + hdrlen];
									}
									// ClipKeyTail(buffer_tail,
									// tailLenInShortsTmp, 1,
									// AudioChannels);
									ClipKeyTail_cos(buffer_tail, tailLenInShortsTmp, 1, AudioChannels);
									for (int p = 0; p < tailLenInShortsTmp; p++)
									{
										SynthBufObj.buffer[StartPosInBuffer + hdrlen + p] += buffer_tail[p];
									}
								}
								else if (AudioChannels == 2)
								{
									// 头合成
									int hdrlen = c - tailLenInShortsTmp;
									int hdrlen2 = hdrlen * 2;
									int StartPosInBuffer2 = StartPosInBuffer * 2;
									for (int p = 0; p < hdrlen2; p++)
									{
										SynthBufObj.buffer[StartPosInBuffer2 + p] += mKeyAudio[key].keyAudio[p];
									}

									// tail
									int tailLenInShortsTmp2 = tailLenInShortsTmp * 2;
									for (int p = 0; p < tailLenInShortsTmp2; p++)
									{
										buffer_tail[p] = mKeyAudio[key].keyAudio[p + hdrlen2];
									}
									ClipKeyTail(buffer_tail, tailLenInShortsTmp, 1, AudioChannels);
									for (int p = 0; p < tailLenInShortsTmp2; p++)
									{
										SynthBufObj.buffer[StartPosInBuffer2 + hdrlen2 + p] += buffer_tail[p];
									}
								}
							}
							else
							{
								Log.i("WG", "key data is null, keyid(0 based):" + key);
							}

//							// 刷新缓冲区数据实际长度: actDataLenInBuffer
//							if (actDataLenInBuffer < StartPosInBuffer + KeyDataCount)
//							{
//								actDataLenInBuffer = StartPosInBuffer + KeyDataCount;
//							}

							// 刷新缓冲区数据实际长度: SynthDataSize,
							// 同时记录full数据结束位置KeyId_full(SynthDataSize_full), 供下一个合成缓冲区copy本次的尾部不完整数据
							if (SynthBufObj.SynthDataSize < StartPosInBuffer + KeyDataCount)
							{
								//int SynthDataSizeOld = SynthBufObj.SynthDataSize;

								SynthBufObj.SynthDataSize = StartPosInBuffer + KeyDataCount;

								// 记录有效数据长度SynthDataSize_full
								if (SynthBufObj.SynthDone() == 1 && SynthBufObj.KeyId_full == -1)
								{
									SynthBufObj.KeyId_full = StartKeyId;

//									long tdiff = PlayKeyTick[SynthBufObj.KeyId_full] - PlayKeyTick[SynthBufObj.buffer_KeyId];
//									int DataCount = (int) (tdiff * (SAMPLE_RATE / 1000) * tk * 1.3);
//									SynthBufObj.SynthDataSize_full = DataCount;
									SynthBufObj.SynthDataSize_full = StartPosInBuffer;
								}
							}

//							Log.i("WG", "key:" + i
//									+ ", StartPosInBufferT:" + StartPosInBuffer / 16
//									+ ", KeyDataCountT:" + KeyDataCount / 16
//									+ ", SynthDataSizeT:" + SynthBufObj.SynthDataSize / 16);
						}
					}


				} // end of while
				Tsyt = System.currentTimeMillis() - Tsyt;


				// save test
				if (SaveWaveCfg > 0 && SaveWaved == false
						&& SynthBufObj.SynthDataSize > 0)
				{
					SaveWaved = true;

					CWaveFile cwf = new CWaveFile();

					String FileName = openfilename + "_" + String.valueOf(SynthBufObj.buffer_KeyId) + ".wav";
					long totalAudioLenInShorts = SynthBufObj.SynthDataSize
							* AudioChannels;
					long totalDataLenInShorts = totalAudioLenInShorts + 44;

					Log.i("WG", "Save Wav, totalDataLenInShorts:" + totalDataLenInShorts);

					long longSampleRate = SAMPLE_RATE;
					int channels = AudioChannels;
					long byteRate = SAMPLE_RATE * 2 * AudioChannels;
					short AudioData[] = SynthBufObj.buffer;
					try {
						cwf.WriteWaveFile(FileName,
								totalAudioLenInShorts,
								totalDataLenInShorts, longSampleRate,
								channels, byteRate, AudioData);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				// 合成好
				SynthBufObj.BufStatus = BUF_STATUS.BUF_STATUS_SYNTHED;
				SynthBufObj.PlayDataSize = 0;	// 清空播放数

//				Log.i("WG", "-S-------------CurSynthBufferId:" + CurSynthBufferId + ", Ready");
			}

			Log.i("WG", "Synth thread over");
		}

		// 缩放t
		int ratioT(float k, int TickOrg) {
			int tickR = (int) (TickOrg * k);

			return tickR;
		}

		// 修剪key的tail
		// AudioData是tail数据, a二次函数系数, a超大尾越钝
		// DataLenInshorts是单声道对应的值，如果是stereo, 则实际值要*2
		int ClipKeyTail(short AudioData[], int DataLenInshorts, float a,
						int AudioChannels) {
			// 用二次函数进行修剪, 成以下形状")"

			if (AudioChannels < 0 || AudioChannels > 2) {
				return 0;
			}

			{
				for (int s = 0; s < AudioChannels; s++) // left or right side
				{
					// 找最大振幅
					short maxA = 0;
					int len = DataLenInshorts / 3;
					if (len == 0) {
						len = DataLenInshorts;
					}
					short v = 0;
					for (int i = 0; i < len; i++) {
						short av = AudioData[i * 2 + s];
						if (av > 0) {
							v = av;
						} else {
							v = (short) (0 - av);
						}
						// v = Math.abs(AudioData[i]);

						if (maxA < v) {
							maxA = v;
						}
					}

					float k;
					int MaxAOrg = (int) Math.sqrt((DataLenInshorts - 1) / a);
					if (MaxAOrg == 0) {
						return 0;
					}
					k = (float) (maxA / MaxAOrg);

					// clip
					float A = 0; // 动态振幅
					for (int i = 0; i < DataLenInshorts; i++) {
						A = (float) (Math.sqrt((DataLenInshorts - i * 2 - 1)
								/ a) * k);
						AudioData[i * 2 + s] = (short) ((float) AudioData[i * 2
								+ s]
								* A / maxA);
					}
				}
			}

			return 1;
		}

		// 修剪key的tail
		// AudioData是tail数据, a二次函数系数, a超大尾越钝
		// DataLenInshorts是单声道对应的值，如果是stereo, 则实际值要*2
		int ClipKeyTail_cos(short AudioData[], int DataLenInshorts, float a,
							int AudioChannels) {
			// 用二次函数进行修剪, 成以下形状")"

			if (AudioChannels < 0 || AudioChannels > 2) {
				return 0;
			}

			{
				for (int s = 0; s < AudioChannels; s++) // left or right side
				{
					// 找最大振幅
					short maxA = 0;
					int len = DataLenInshorts / 3;
					if (len == 0) {
						len = DataLenInshorts;
					}
					short v = 0;
					for (int i = 0; i < len; i++) {
						short av = AudioData[i * 2 + s];
						if (av > 0) {
							v = av;
						} else {
							v = (short) (0 - av);
						}
						// v = Math.abs(AudioData[i]);

						if (maxA < v) {
							maxA = v;
						}
					}

					// 1.0 + cos(0, PI)
					float k = (float) (maxA / 2.0);

					// clip
					float A = 0; // 动态振幅
					float angle = 0;
					for (int i = 0; i < DataLenInshorts; i++) {
						angle = PI * i / DataLenInshorts;
						A = (float) ((1.0 + Math.cos(angle)) * k);
						AudioData[i * 2 + s] = (short) ((float) AudioData[i * 2
								+ s] * A);
					}
				}
			}

			return 1;
		}


	}

}

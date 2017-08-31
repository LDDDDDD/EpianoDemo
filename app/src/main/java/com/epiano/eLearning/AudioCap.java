package com.epiano.eLearning;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.util.Log;

import com.epiano.commutil.Complex;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;


public class AudioCap
{
	private static String TAG = "Audio";

	public static final float pi= (float) 3.1415926;

	//boolean mRuning = true;

	int frequency = 16000;//4000/8000/11050/22050/44100
	int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
	int audioEncodeing = AudioFormat.ENCODING_PCM_16BIT;
	//static final int audioEncodeing = AudioFormat.ENCODING_PCM_8BIT;

	int EncodeFrmLen = 20; 	// 编码帧时间长度 ms
	int FrmDataLen = 0;		// 编码帧数据长度 byte
	int MaxWait2EncodeFrmNum = 10; // cfg, 最大待编码帧数, 防止待编码数据累积过多

	int minBufferSize = 0;//
	AudioRecord audioRecord = null;//

	boolean RepWidthFileData = false; //1; // 调试开关, 用文件中的数据代替mic数据
	public boolean isRecording = false;
	long bufPCMTick = 0;


	AVCom avcom;

	// test
	PlayTestThread playTestthread = new PlayTestThread();

	PlayThread playthread; // = new PlayThread();
	RecordThread recthread;

	long tick0 = 0; //System.currentTimeMillis();

	// 模式1: 录音时直接把PCM分割成比如20ms的片段
	CPCMBuf mPCMBuf;

	// 模式2: 录音时不分片段，比模式1效率可能高些
	final int mPCMDataBufMaxSize = 320; //10000;
	int mPCMDataBufSize = 0;
	short mPCMDataBuf[] = new short[mPCMDataBufMaxSize];

	public int mAudioEncCount = 0;		// 语音成功编码帧数
	public int mAudioEncCount_Period = 0;		// 语音成功编码帧数
	public int mAudioEncFECCount = 0;	// 语音成功编码FEC帧数

	int OrgPkNum_N = 10; // 注意与jni中的一致性OrgPkNum_N_A
	int FecPkNum_n = 3;
	//int mNalCount = 0;			// nal数量
	//int mh264Frmlen = 0;
	int h264blklen = 0;
	int m264PkIdInGrp = 0;		// 当前视频编码发送包号
	int mAudioFrmId = 0;

	boolean SaveAudioSwitch = false;

	//
	//public AudioCap(Context ctx, AVCom avcomIn, int freq, int channel, int audiobits)
	public AudioCap(Context ctx) // AVCom avcomIn, int freq, int channel, int audiobits)
	{
//		avcom = avcomIn;
//		frequency = freq;
//		channelConfiguration = channel;
//		audioEncodeing = audiobits;

		FrmDataLen = EncodeFrmLen * channelConfiguration * frequency / 1000;
		if (audioEncodeing == AudioFormat.ENCODING_PCM_16BIT)
		{
			//FrmDataLen *= 2;
		}
		else if (audioEncodeing == AudioFormat.ENCODING_PCM_8BIT)
		{
			FrmDataLen *= 1;
		}

		mPCMBuf = new CPCMBuf(FrmDataLen, 100);

		minBufferSize = AudioRecord.getMinBufferSize(frequency,
				channelConfiguration,
				audioEncodeing);
		Log.i(TAG,"-----minBufferSize--is:"+minBufferSize+" frequency:"+frequency+" audioEncodeing"+audioEncodeing);

	}

	public int Start(AVCom avcomIn, int freq, int channel, int audiobits)
	{
		avcom = avcomIn;
		frequency = freq;
		channelConfiguration = channel;
		audioEncodeing = audiobits;

		minBufferSize = 20 * minBufferSize;
		//minBufferSize = 2 * minBufferSize;
		audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,frequency,
				channelConfiguration,
				audioEncodeing,
				minBufferSize
		);

		isRecording = true;

		// test
		//playTestthread.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);
		//playTestthread.setOSPriority(-19); //Process.THREAD_PRIORITY_URGENT_AUDIO); // -19
		//playTestthread.setPriority(Thread.MIN_PRIORITY); // 1
		//playTestthread.start();

		playthread = new PlayThread();
		playthread.setPriority(9); //Thread.MAX_PRIORITY); // 1
		playthread.start();

		recthread = new RecordThread(audioRecord, minBufferSize);
		recthread.setPriority(9); //Thread.MAX_PRIORITY); // 1
		recthread.start();

		return 1;
	}

	public void Release()
	{

	}

	public void Amplify(short data[], int offset, int len, float Ratio)
	{
		int endp = offset + len;
		for(int i = offset; i < endp; i++)
		{
			data[i] *= Ratio;
		}
	}


	// 录音线程
	class RecordThread extends Thread {
		private AudioRecord audioRecord;
		private int minBufferSize;

		int length = 0;
		int r = 0;

		public RecordThread(AudioRecord audioRecord, int minBufferSize) {
			this.audioRecord = audioRecord;
			this.minBufferSize = minBufferSize;
		}

		void setushort(byte by[], int offset, int value)
		{
			by[offset] 		= (byte)(value & 0xFF);
			by[offset + 1] 	= (byte)((value >> 8) & 0xFF);
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

		public void run() {

			// play test
			AudioTrack audioTrack = null;
			final int SAMPLE_RATE = 16000; // 11025;
			//PosIndicateTask IndiTask;
			int AudioChannels = 1;
			int AudioChannelsType = AudioFormat.CHANNEL_OUT_MONO;;
			// 打开系统音频播放模块
			//final int SAMPLE_RATE = 16000; // 11025;
			int minSize = AudioTrack.getMinBufferSize(SAMPLE_RATE, //
					AudioChannelsType, //AudioFormat.CHANNEL_IN_MONO,// 常量： 16（0x00000010）
					AudioFormat.ENCODING_PCM_16BIT);// PCM音频数据格式：16位，每样
			//minSize *= 4; //4;	// cause delay
			//minSize = 320 * 10; // test cause crash
			//audioTrackBufferDelay = minSize / SAMPLE_RATE * 1000 / AudioChannels;	// ms 缓冲造成的时延
			audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,// 媒体音量		AudioTrack
					SAMPLE_RATE,//
					AudioChannelsType, //AudioFormat.CHANNEL_OUT_MONO,// 常量： 16（0x00000010）
					AudioFormat.ENCODING_PCM_16BIT, // PCM音频数据格式：每个采样点16比特
					(int)(minSize),// 3 10 2
					// MODE_STATIC 音频数据放到一个固定的buffer, 然后一次写入
					// MODE_STREAM 通过write方式把数据一次一次得写到audiotrack中
					AudioTrack.MODE_STREAM // MODE_STATIC // MODE_STREAM//
			);
			audioTrack.play();

			try {
				//short[] buffer = new short[minBufferSize * 2];

				int datanumSum = 0; // short的个数

				Log.i(TAG, "startRecording thread..."); // test

				audioRecord.startRecording();
				// FileOutputStream fos = mContext.openFileOutput("data.txt",
				// Context.MODE_PRIVATE);

				tick0 = System.currentTimeMillis();
				long tick1 = tick0;
				long tick2 = tick0;

				int counter = 0;

				while (isRecording) {

					counter++;

//					// test
//					// l: 233472, lx: 36864, ii: 36864, ix: 36864, s: -28672
//					{
//
//						long l = 0;
//						int ii = 0;
//						short s = 0;
//						byte by[] = new byte[2];
//						for(l = 0; l < 0x7FFFFFFF; l+=256)
//						{
//							setushort(by, 0, (int)l);
//							ii = getushort(by, 0);
//							s = (short)ii;
//							Log.e(TAG, "_____ : l: " + l
//								+ ", lx: " + (l & 0xFFFF)
//								+ ", ii: " + ii
//								+ ", ix: " + (ii & 0xFFFF)
//								+ ", s: " + s);
//							if((l & 0xFFFF) != (ii & 0xFFFF))
//							{
//								int z = 0;
//								int t = z;
//							}
//						}
//					}

					// 1ms
					try {
						if (counter % 100 == 0)
							Thread.sleep(1); // 10);
					} catch (InterruptedException e) {

						e.printStackTrace();
					}

					// test
					if (avcom.FreezeAudio > 0)
					{
						continue;
					}

					// 记录帧时刻
					if (bufPCMTick == 0) {
						bufPCMTick = System.currentTimeMillis();
					}

					//
					//Log.i(TAG, "1------------"); // test
					//int res = audioRecord.read(tmp, 0, minBufferSize);

					int res;
					//if (false)
					{
						int rcount = Math.min(minBufferSize, mPCMDataBufMaxSize);

						// read mic
						res = audioRecord.read(mPCMDataBuf, 0, rcount);

						// test play immediately
//						if (true)
//						{
//							int realplaynum =
//								audioTrack.write(mPCMDataBuf, 0, rcount); //640); //bufferlen);
//
//							continue;
//						}

						// opus fec encode
						r = avcom.PutPCMData(res, mPCMDataBuf, mAudioEncCount);
						if (r <= 0)
						{
							Log.e(TAG, "2------------PutPCMData error:");
						}
						else
						{
							mAudioEncCount++; // 成功编码包数
						}

						// test 取语音编码数据
//						if (true)
//						{
//							byte audiodata[];
//							while(true)
//							{
//								audiodata = avcom.GetAudioEncData();
//								if (audiodata == null)
//								{
//									break;
//								}
//
//								//mAudioEncCount++;
//							}
//						}

						// test save
						if (SaveAudioSwitch)
						{
							if (playthread.bufferlen + res >= mPCMDataBuf.length)
							{
								playthread.bufferlen = 0;
							}

							System.arraycopy(mPCMDataBuf, 0,
									playthread.bufferPCM, playthread.bufferlen,
									res);
							playthread.bufferlen += res;
						}

						// test save 1.pcm
//						if (true)
//						{
//							// copy
//							{
//								int l = res;
////								for(int i = 0; i < l; i++)
////								{
////									playTestthread.bufferPCM[playTestthread.bufferlen + i] =
////											mPCMDataBuf[i];
////								}
//								System.arraycopy(mPCMDataBuf, 0,
//										playTestthread.bufferPCM, playTestthread.bufferlen,
//										l);
//								playTestthread.bufferlen += res;
//							}
//
//							if (counter == 500)
//							{
//								String filen = "/mnt/sdcard/1.pcm";
//								PlayTestThread playthreadx = playTestthread;
//								writefileShort(filen, playTestthread.bufferPCM, playTestthread.bufferlen);
//
//								if (playTestthread.bufferPCM1 != null)
//								{
//									String filen2 = "/mnt/sdcard/2.pcm";
//	//								PlayTestThread playthreadx = playTestthread;
//									writefileShort(filen2, playTestthread.bufferPCM1, playTestthread.bufferlen1);
//								}
//							}
//						}
					}

					tick2 = System.currentTimeMillis();

					// important print
//					Log.e(TAG, "2------------read:" + res
//							+ ", df: " + (tick2 - tick1)
//							+ ", afrm: " + mAudioEncCount
//							+ ", hdr: " + mPCMBuf.hdr
//							+ ", cnt: " + mPCMBuf.count
//							);

					tick1 = tick2;

					datanumSum += res;	// 不清0

					//Log.i(TAG, "record datanumSum:" + datanumSum);
				}

			} catch (Exception e) {
				// TODO: handle exception
				Log.i("Rec E", e.toString());
			}

			audioRecord.stop();

			Log.i(TAG, "audio record thread end.------------"); // test

		}

//		public void runV0() {
//
//			try {
//				short[] buffer = new short[minBufferSize * 2];
//
//				int datanumSum = 0; // short的个数
//
//				Log.i(TAG, "startRecording thread..."); // test
//
//				audioRecord.startRecording();
//				// FileOutputStream fos = mContext.openFileOutput("data.txt",
//				// Context.MODE_PRIVATE);
//
//				tick0 = System.currentTimeMillis();
//				long tick1 = tick0;
//				long tick2 = tick0;
//
//				int counter = 0;
//
//				while (isRecording) {
//
//					counter++;
//
//					// 1ms
//		        	try {
//		        		if (counter % 100 == 0)
//		        			Thread.sleep(1); // 10);
//					} catch (InterruptedException e) {
//
//						e.printStackTrace();
//					}
//
//					// 记录帧时刻
//					if (bufPCMTick == 0) {
//						bufPCMTick = System.currentTimeMillis();
//					}
//
//					//
//					//Log.i(TAG, "1------------"); // test
//					//int res = audioRecord.read(tmp, 0, minBufferSize);
//
//					int res;
//					if (true)
//					{
//						int readx = mPCMBuf.GetPutSize();
//
//						//int res = audioRecord.read(buffer, datanum, readx); //minBufferSize);
//						short buft[] = mPCMBuf.GetTailBuf();
//						res = audioRecord.read(buft, mPCMBuf.tailsize, readx); //minBufferSize);
//
//						// test save 1.pcm
//						if (false)
//						{
//							// copy
//							{
//								int l = mPCMBuf.tailsize + res;
//								for(int i = mPCMBuf.tailsize; i < l; i++)
//								{
//									playTestthread.bufferPCM[playTestthread.bufferlen + i] =
//											buft[i];
//								}
//								playTestthread.bufferlen += res;
//							}
//
//							if (counter == 1000)
//							{
//								String filen = "/mnt/sdcard/1.pcm";
//								PlayTestThread playthreadx = playTestthread;
//								writefileShort(filen, playTestthread.bufferPCM, playTestthread.bufferlen);
//
//								{
//									String filen2 = "/mnt/sdcard/2.pcm";
//	//								PlayTestThread playthreadx = playTestthread;
//									writefileShort(filen2, playTestthread.bufferPCM1, playTestthread.bufferlen1);
//								}
//							}
//						}
//
//						mPCMBuf.put(null, res);
//					}
//
//					//Log.i(TAG, "2------------res:" + res); // test
//
//					tick2 = System.currentTimeMillis();
//					Log.e(TAG, "2------------read:" + res
//							+ ", df: " + (tick2 - tick1)
//							+ ", hdr: " + mPCMBuf.hdr
//							+ ", cnt: " + mPCMBuf.count
//							);
//					tick1 = tick2;
//
//					datanumSum += res;	// 不清0
//
//					//Log.i(TAG, "record datanumSum:" + datanumSum);
//				}
//
//			} catch (Exception e) {
//				// TODO: handle exception
//				Log.i("Rec E", e.toString());
//			}
//
//			audioRecord.stop();
//
//			Log.i(TAG, "audio record thread end.------------"); // test
//
//		}
	}

	// 播放线程
	class PlayThread extends Thread {

		//{
		AudioTrack audioTrack = null;
		int CurPosIndicatorId = -1;
		int mPosIndicator[];		// Play位置指示
		long mStartPlayTime = -1;
		int mRealPlaycount = 0;
		int audioTrackBufferDelay = 0;
		int PosIndicatorRes = 100; // ms
		final int SAMPLE_RATE = 16000; // 11025;
		//PosIndicateTask IndiTask;
		int AudioChannels = 1;
		int AudioChannelsType;

		short bufferPCM[]  = new short[500000];
		short bufferPCM1[] = new short[500000];
		int bufferlen = 0;
		int bufferlen1 = 0;
		int playpos = 0;

		//}

		int length = 0;
		int r = 0;

		public PlayThread() {

		}

		public int write(short data[], int len) {

			synchronized(bufferPCM)
			{
				System.arraycopy(data, 0, bufferPCM, bufferlen, len);
				bufferlen += len;
			}

			return len;
		}

		public void run() {

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

			// 打开系统音频播放模块
			//final int SAMPLE_RATE = 16000; // 11025;
			int minSize = AudioTrack.getMinBufferSize(SAMPLE_RATE, //
					AudioChannelsType, //AudioFormat.CHANNEL_IN_MONO,// 常量： 16（0x00000010）
					AudioFormat.ENCODING_PCM_16BIT);// PCM音频数据格式：16位，每样
			//minSize *= 4; //4; // cause much delay
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

//			// 延迟200ms
//        	try {
//				Thread.sleep(400); // 500 1000 2000// 10);
//			} catch (InterruptedException e) {
//
//				e.printStackTrace();
//			}
			audioTrack.play();

			long tick1 = System.currentTimeMillis();
			//long tick0 = tick1;
			long tick2 = tick1;
			long tick3 = 0;
			long td = 0;

			int playnum = 0;
			int realplaynum = 0;

			int counter = 0;

			while (isRecording) {

				// 1ms
				try {
					Thread.sleep(5); // 10);
				} catch (InterruptedException e) {

					e.printStackTrace();
				}

				tick1 = System.currentTimeMillis();


				// play
				//if (true)
				if (false)
				{
					// test

					// 收到tm秒后一起播放

					int tm = 5; // sec

					realplaynum = 0;
					playnum = 0;

					short audio[] = avcom.GetAudioDecData();
					if (audio != null)
					{
						// xxxx
						//audioTrack.play();

						playnum = audio.length;
						Amplify(audio, 0, playnum, 1f); // 3.3f);

						System.arraycopy(audio, 0, bufferPCM1, bufferlen1, playnum);
						bufferlen1 += playnum;
					}
					tick3 = System.currentTimeMillis();

					if (SaveAudioSwitch && bufferlen1 >= 320 * 50 * tm)
					{
						// test save 1.pcm 3.pcm
						{ // sor
							String filen = "/mnt/sdcard/1.pcm";
							writefileShort(filen, bufferPCM, bufferlen);
						}
						{ // sink
							String filen = "/mnt/sdcard/3.pcm";
							writefileShort(filen, bufferPCM1, bufferlen1);
						}

						realplaynum = audioTrack.write(bufferPCM1, 0, bufferlen1); //640); //bufferlen);
						bufferlen1 = 0;
					}
				}
				else
				{
					realplaynum = 0;
					playnum = 0;

					short audio[] = avcom.GetAudioDecData();
					if (audio != null)
					{
						// xxxx
						//audioTrack.play();

						playnum = audio.length;
						//Amplify(audio, 0, playnum, 3.3f);
						realplaynum = audioTrack.write(audio, 0, playnum); //640); //bufferlen);
					}
					tick3 = System.currentTimeMillis();
				}

//				tick2 = System.currentTimeMillis();
//				td = tick2 - tick1;
//				tick1 = tick2;
//				playnum = 16 * (int)td;
//				if (counter == 0)
//				{
//					playnum += 160;
//				}
//				if (playnum + playpos > bufferlen)
//				{
//					playnum = bufferlen - playpos;
//				}
//
//				//if (playnum > 0)
//				{
//					if (true)
//					{
//						short buft[];
//
//						for(int j = 0; j < 3; j++)
//						{
//							if (mPCMBuf.count == 1)
//							{
//								break;
//							}
//
//							buft = mPCMBuf.Get();
//							if (buft != null)
//							{
//								playnum = mPCMBuf.frmSizeInShort;
//
//								// test save 2.pcm
////										{
////											int l = playnum;
////											for(int i = 0; i < l; i++)
////											{
////												playTestthread.bufferPCM1[playTestthread.bufferlen1 + i] =
////														buft[i];
////											}
////											playTestthread.bufferlen1 += playnum;
////										}
//
//								Amplify(buft, 0, playnum, 3.3f);
//								realplaynum = audioTrack.write(buft, 0, playnum); //640); //bufferlen);
//							}
//						}
//
//					}
//				}

				// important print
//				Log.e(TAG,
//						"tick: " + tick1
//						+ ", dI: " + (tick1 - tick2)
//						+ ", dU: " + (tick3 - tick1)
//						+ ", realplaynum: " + realplaynum
//						+ ", playnum: " + playnum
//						+ ", td: " + td
//						//+ ", bufferlen: " + bufferlen
//						//+ ", playpos: " + playpos
//						); // test

				tick2 = tick1;

				counter++;
			}

			audioTrack.stop();
		}
	}

	////////////////////////////////////////////////////

	// test 播放测试
	class PlayTestThread extends Thread {

		// 播放测试
		//{
		AudioTrack audioTrack = null;
		int CurPosIndicatorId = -1;
		int mPosIndicator[];		// Play位置指示
		long mStartPlayTime = -1;
		int mRealPlaycount = 0;
		int audioTrackBufferDelay = 0;
		int PosIndicatorRes = 100; // ms
		final int SAMPLE_RATE = 16000; // 11025;
		//PosIndicateTask IndiTask;
		int AudioChannels = 1;
		int AudioChannelsType;

		short bufferPCM[] = new short[5000000];
		short bufferPCM1[] = new short[5000000];
		int bufferlen = 0;
		int bufferlen1 = 0;
		int playpos = 0;

		//}

		int length = 0;
		int r = 0;

		public PlayTestThread() {

		}

		public int write(short data[], int len) {

			synchronized(bufferPCM)
			{
				System.arraycopy(data, 0, bufferPCM, bufferlen, len);
				bufferlen += len;
			}

			return len;
		}

		public void run() {

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

			// 延迟200ms
			try {
				Thread.sleep(400); // 500 1000 2000// 10);
			} catch (InterruptedException e) {

				e.printStackTrace();
			}
			audioTrack.play();

			long tick1 = System.currentTimeMillis();
			//long tick0 = tick1;
			long tick2 = tick1;
			long td = 0;

			int counter = 0;

			while (isRecording) {

				// 1ms
				try {
					Thread.sleep(1); // 10);
				} catch (InterruptedException e) {

					e.printStackTrace();
				}

				if (audioTrack != null)
				{
					tick2 = System.currentTimeMillis();
					td = tick2 - tick1;
					tick1 = tick2;
					int playnum = 16 * (int)td;
					if (counter == 0)
					{
						playnum += 160;
					}
					if (playnum + playpos > bufferlen)
					{
						playnum = bufferlen - playpos;
					}

					int realplaynum = 0;
					//if (playnum > 0)
					{
						if (true)
						{
							short buft[];

							for(int j = 0; j < 3; j++)
							{
								if (mPCMBuf.count == 1)
								{
									break;
								}

								buft = mPCMBuf.Get();
								if (buft != null)
								{
									playnum = mPCMBuf.frmSizeInShort;

									// test save 2.pcm
//									{
//										int l = playnum;
//										for(int i = 0; i < l; i++)
//										{
//											playTestthread.bufferPCM1[playTestthread.bufferlen1 + i] =
//													buft[i];
//										}
//										playTestthread.bufferlen1 += playnum;
//									}

									Amplify(buft, 0, playnum, 3.3f);
									realplaynum = audioTrack.write(buft, 0, playnum); //640); //bufferlen);
								}
							}

						}

						Log.e(TAG, "realplaynum: " + realplaynum
										+ ", playnum: " + playnum
										+ ", td: " + td
								//+ ", bufferlen: " + bufferlen
								//+ ", playpos: " + playpos
						); // test
					}
				}

				counter++;
			}

			audioTrack.stop();
		}
	}


	///////////////////////////////////////////////////

	public int readaudiofile(short shortbuf[], int maxCount, int skipcount) {
		// String fileName = "/mnt/sdcard/audio_sample.dat";
		String fileName = "/mnt/sdcard2/audio_sample.dat";

		int maxdata = 0;
		try {
			DataInputStream in = new DataInputStream(
					new BufferedInputStream(new FileInputStream(fileName)));

			int skipcountseg = 1000000 * 2;
			byte b[] = new byte[maxCount * 2 + skipcountseg];	// skipcount
			int bc = 0;

			// 跳过一些数据
			if (skipcount > 0) {
				int left = skipcount * 2;
				while(left > 0)
				{
					if (left > skipcountseg)
					{
						bc = in.read(b, 0, skipcountseg);
					}
					else
					{
						bc = in.read(b, 0, left);
					}

					left -= bc;
				}
			}

			// 正式数据
			bc = in.read(b, 0, maxCount * 2);
			in.close();

			// int maxdata = min(maxCount, (int)(bc/2));
			maxdata = maxCount;
			if (maxdata > (int) (bc / 2)) {
				maxdata = (int) (bc / 2);
			}
			for (int j = 0; j < maxdata; j++) {
				int a = (int) (b[j * 2 + 1]);
				a &= 0xFF;
				a = a << 8;
				int a1 = (int) b[j * 2];
				a1 &= 0xFF;
				shortbuf[j] = (short) (a + a1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return maxdata;
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

	int writefileShort(String FileName, short data [], int datalen)
	{
		try {
			DataOutputStream fos = new DataOutputStream(
					new BufferedOutputStream(new FileOutputStream(FileName)));

			// 正式数据
			//fos.write(data, 0, datalen);
			byte b[] = new byte[datalen * 2];
//			for(int i = 0; i < datalen; i++)
//			{
//				fos.writeShort(data[i]);
//			}
			for(int i = 0; i < datalen; i++)
			{
				b[i*2] = (byte)(data[i] & 0xFF);
				b[i*2+1] = (byte)((data[i] >> 8) & 0xFF);
			}
			fos.write(b, 0, datalen * 2);
			fos.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return 1;
	}

	// no use
	class CPCMBuf
	{
		//private ArrayList<short[]> bufPCM = new ArrayList<short[]>();
		Map <Integer, short []>mPCMMap = new HashMap<Integer, short []>();
		int mPCMMapMaxSize = 50;
		int hdr = 0;
		int count = 0;
		int frmSizeInShort = 0;

		short putbuf[];

		int tailsize = 0; // tail项目的长度, 0表示满了，其它值表示未满

		public CPCMBuf(int frmSizeIn, int PCMMapMaxSize)
		{
			frmSizeInShort = frmSizeIn;
			mPCMMapMaxSize = PCMMapMaxSize;

			InitBufMap();
		}

		private int InitBufMap()
		{
			for(int i = 0; i < mPCMMapMaxSize; i++)
			{
				short buf[] = new short[frmSizeInShort];
				mPCMMap.put(i, buf);
			}

			putbuf = mPCMMap.get(0);

			return 1;
		}

		public short [] Get()
		{
			if (count == 0)
			{
				return null;
			}

			// 仅一项，且没有收齐
			if (count == 1 && tailsize < frmSizeInShort)
			{
				return null;
			}

			int id = hdr;
			hdr = (hdr + 1) % mPCMMapMaxSize;

			short data[] = mPCMMap.get(id);

			count--;

			return data;
		}

		public int GetPutSize()
		{
//				if (tailsize < frmSizeInShort)
//				{
//					return frmSizeInShort - tailsize;
//				}

			return frmSizeInShort - tailsize;
		}

		public short [] GetTailBuf()
		{
			int tail = (hdr + count) % mPCMMapMaxSize;
			putbuf = mPCMMap.get(tail);
			return putbuf;
		}

		/*
		public int put(short data[], int len)
		{
			if (data != null)
			{
				return 0;
			}

			int tail = (hdr + count) % mPCMMapMaxSize;

			int r = 1;
			short dataDst[] = null;

			if (tailsize == 0)
			{
				if (data != null)
				{
					dataDst = mPCMMap.get(tail);
					System.arraycopy(data, 0, dataDst, 0, len);
				}
				tailsize = len % frmSizeInShort;

//					if (tailsize != 0)
//					{
//						return 1;
//					}

				if (tailsize == 0)
				{
					//
					Log.e(TAG,"avcom.OpusEncode() 1");
					dataDst = mPCMMap.get(tail);
					avcom.OpusEncode(frmSizeInShort, dataDst);
				}

				if (count >= mPCMMapMaxSize)
				{
					// 缓冲区已满, 挤出旧数据
					hdr = (hdr + 1) % mPCMMapMaxSize;

					//return 2;	//
				}
				else
				{
					count++;
				}
			}
			else
			{
				// 已有半截数据

				if (data != null)
				{
					dataDst = mPCMMap.get(tail);
					System.arraycopy(data, 0, dataDst, tailsize, len);
				}
				//putbuf = dataDst;
				tailsize = (tailsize + len) % frmSizeInShort;

				if (tailsize == 0)
				{
					//
					Log.e(TAG,"avcom.OpusEncode() 2");
					dataDst = mPCMMap.get(tail);
					avcom.OpusEncode(frmSizeInShort, dataDst);
				}

				//return 1;
			}

			return 1;
		}
		*/
	}

}

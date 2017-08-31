package com.epiano.commutil;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.Rect;
import android.media.AudioRecord;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.util.ArrayList;

public class AudioProcess {
	public static final float pi= (float) 3.1415926;
	//应该把处理前后处理后的普线都显示出来
	//ljh 0307
	//private ArrayList<short[]> bufPCM = new ArrayList<short[]>();//原始录入数据
	long bufPCMTick = 0;
	private ArrayList<short[]> bufPCM = new ArrayList<short[]>();//原始录入数据
	//private ArrayList<int[]> bufFFT = new ArrayList<int[]>();//处理后的数据
	private ArrayList<float[]> bufFFT = new ArrayList<float[]>();//处理后的数据
	private boolean isRecording = false;
	private boolean isKeyRongizeing = false;	// 开始Key识别

	//Context mContext = this.getContext();
	//存储读取到的数据
	//FileOutputStream fos;
	//上下文
	Context mContext;
	String TAG = "AudioProcess";
	private int shift = 30;
	public int frequence = 0;

	private int length = 256;
	//y轴缩小的比例
	public int rateY = 100;   //default Y zoom in value
	//y轴基线
	public int baseLine = 0;
	public int baseLineHor = 0;

	private int Y_index = 0;// 当前画图所在屏幕X轴的坐标

	CKeysRec mKeys = new CKeysRec();

	TextView mTextView;
	CharSequence ResultText[];

	int RepWidthFileData = 0; //1; // 调试开关, 用文件中的数据代替mic数据

	//int lineMember = 30;

//	AudioProcess(TextView tv)
//	{
//		mTextView = tv;
//	}

	//初始化画图的一些参数   baseline:为surface的height
	public void initDraw(int rateY, int baseLine, int basLineHor, Context mContext, int frequence, CharSequence pResultText[]){
		this.mContext = mContext;
		this.rateY = rateY;
		this.baseLine = baseLine;
		this.baseLineHor = basLineHor;
		this.frequence = frequence;

		//mTextView = tv;
		ResultText = pResultText;
	}
	//启动程序
	public void start(AudioRecord audioRecord, int minBufferSize, SurfaceView sfvSurfaceView, SurfaceView sfvSurfaceViewHor, TextView tv) {
		if (isRecording)
		{
			Log.i(TAG, "already starting, stop first.");
			return;
		}

		isRecording = true;
		new RecordThread(audioRecord, minBufferSize).start();
		//new ProcessThread().start();
		new DrawThread(sfvSurfaceView, sfvSurfaceViewHor, tv).start();
	}
	//停止程序
	public void stop(SurfaceView sfvSurfaceView){
		isRecording = false;
		synchronized (bufPCM) {
			bufPCM.clear();
		}
		//sfvSurfaceView;
		//drawBuf.clear();
		//outBuf.clear();
	}
	//配置从mic或从文件读取audio数据
	public void ReadFromMic(int readfrommic) {
		RepWidthFileData = 1 - readfrommic;
	}

	//识别程序
	public void startKeyRec(AudioRecord audioRecord, int minBufferSize, SurfaceView sfvSurfaceView) {
		isKeyRongizeing = true;
//		new RecordThread(audioRecord, minBufferSize).start();
//		//new ProcessThread().start();
//		new DrawThread(sfvSurfaceView).start();
	}

	// 录音线程
	class RecordThread extends Thread {
		private AudioRecord audioRecord;
		private int minBufferSize;

		public RecordThread(AudioRecord audioRecord, int minBufferSize) {
			this.audioRecord = audioRecord;
			this.minBufferSize = minBufferSize;
		}

		// public void readaudiofile(short shortbuf[], int maxCount) {
		// try {
		// FileInputStream stream = new FileInputStream(
		// "/mnt/sdcard/audio_sample.dat");
		// //int c;
		// int i = 0;
		// byte b[] = new byte[maxCount * 2];
		// stream.read(); //(b, maxCount * 2);
		// // while ((shortbuf[i] = stream.read()) != -1) {
		// // //System.out.println(c);
		// // i++;
		// // }
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		// }

		// public int readaudiofile(short shortbuf[], int maxCount) {
		// String fileName = "/mnt/sdcard/audio_sample.dat";
		//
		// int i = 0;
		// try {
		// DataInputStream in = new DataInputStream(
		// new BufferedInputStream(new FileInputStream(fileName)));
		// int s;
		// int s1;
		// while(i < maxCount)
		// {
		// s = in.readShort();
		// s1 = (s & 0xff) << 8;
		// shortbuf[i] = (short)(((s & 0xff00) >> 8) + s1);
		// i++;
		// }
		// //System.out.println("The sum is:" + sum);
		// in.close();
		//
		// return i;
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		//
		// return i;
		// }

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

		public void run() {
			try {
				short[] buffer = new short[minBufferSize * 2];
				short[] tmp = new short[minBufferSize * 2];
				int datanum = 0;

				int datanumSum = 0;

				int fftDataNum = 2048; // 1024;
				length = up2int(fftDataNum);
				float fftInData[] = new float[length];
				float[] outFloat = new float[length];

				int switchfft = 0;

				// test
				//int RepWidthFileData = 1; // 调试开关, 用文件中的数据代替mic数据
				int maxdatacount = 12000000;
				//int maxdatacount = 12107100; // short data
				// int maxdatacount = 200000;

				int skipdatacount = 0; //5000000;
				//int skipdatacount = 5000000;
				//int skipdatacount = (int)((19402452 / 2) * 0.97);

				short dataInFile[] = null;
				int datacountInFile = 0;
				int dataIdxInFile = 0;
				if (RepWidthFileData > 0) {
					dataInFile = new short[maxdatacount];
					datacountInFile = readaudiofile(dataInFile, maxdatacount,
							skipdatacount);
					if (datacountInFile == 0) {
						Log.i(TAG, "Total shortdata is 0, data soruce is changed to mic.");
						RepWidthFileData = 0;
					}
					else
					{
						Log.i(TAG, "Total shortdata is " + datacountInFile);
					}
				}

				Log.i(TAG, "startRecording thread..."); // test

				audioRecord.startRecording();
				// FileOutputStream fos = mContext.openFileOutput("data.txt",
				// Context.MODE_PRIVATE);
				while (isRecording) {

					// 记录帧时刻
					if (bufPCMTick == 0) {
						bufPCMTick = System.currentTimeMillis();
					}

					//
					//Log.i(TAG, "1------------"); // test
					int res = audioRecord.read(tmp, 0, minBufferSize);
					//Log.i(TAG, "2------------res:" + res); // test

					if (true)
					{
						//continue; // test
					}

					//RepWidthFileData = 0; // test
					if (RepWidthFileData > 0) // test 用文件数据代替mic数据
					{
						// test data
						if (datacountInFile > 0) {
							// 数据回滚
							if (dataIdxInFile + res >= datacountInFile) {
								dataIdxInFile = 0;
							}

							for (int i = 0; i < res; i++) { // minBufferSize
								buffer[datanum + i] = dataInFile[dataIdxInFile];
								dataIdxInFile++;
							}
						}
					} else {
						// real data

//						for (int i = 0; i < res; i++) { // minBufferSize
//							buffer[datanum + i] = tmp[i];
//						}
						System.arraycopy(tmp, 0, buffer, datanum, res);
					}

					datanumSum += res;	// 不清0

					datanum += res;
					if (datanum < fftDataNum) {
						continue;
					}

					//Log.i(TAG, "record datanumSum:" + datanumSum); // test

					// wg add
					// jni fft
					if (isKeyRongizeing) {
						// int dataNum = mKeys.GetAudioDataSize();
						// float result[] = mKeys.OnAudioData(tmp, res); //
						// res:有效数据长度
						int r = mKeys.FeedAudioData(buffer, fftDataNum);
						if (r == 0) {
							// return null;
						}
					}


					switchfft = 1 - switchfft;
					if (true)
					//if (false)
					//if (switchfft > 0)
					{
						// java fft

						// 将录音结果存放到bufPCM中,以备画时域图使用
						//synchronized (bufPCM) {
						//	bufPCM.add(buffer);
						//}

						// 保证长度为2的幂次数
						//int l = up2int(fftDataNum);
						//length = l;
						int l = length;

						Complex[] complexs = new Complex[l];
						for (int i = 0; i < l; i++) {
							// Log.i(TAG, "shortdata is: " + i);
							double shortData = (double) buffer[i];
							complexs[i] = new Complex(shortData);
							// Log.i(TAG, "shortdata is : " + i);
						}

						//int[] outInt = new int[l];
						//float[] outFloat = new float[l];
						// if (false)
						if (true) {
							fft(complexs, l); // /////////////////////////////////
							int c = l / 2;
							for(int i = 0; i < c; i++) {
								//outInt[i] = complexs[i].getIntValue();
								outFloat[i] = complexs[i].getFloatValue();
								//outFloat[i] = complexs[i/2].getFloatValue();
							}
							synchronized (bufFFT) {
								//bufFFT.add(outInt);
								bufFFT.add(outFloat);
							}
						}
					}
					else
					{
						// jni fft

//						if (true)
//						{
//							datanum = 0;
//							continue; // test
//						}

						// 将录音结果存放到bufPCM中,以备画时域图使用
						//synchronized (bufPCM) {
						//	//bufPCM.clear(); // 0813
						//	bufPCM.add(buffer);
						//}

//						if (true)
//						{
//							datanum = 0;
//							continue; // test
//						}

						// 保证长度为2的幂次数
//						int l = up2int(fftDataNum);
//						length = l;
						int l = length;

						for (int i = 0; i < l; i++) {
							// Log.i(TAG, "shortdata is: " + i);
							fftInData[i] = (float) buffer[i];
							// Log.i(TAG, "shortdata is : " + i);
						}

						//Log.i(TAG, "3------------"); // test


						float result[] = null;
						synchronized (mKeys.ObEPianoAndroidJavaAPI)
						{
							result = mKeys.ObEPianoAndroidJavaAPI.RealFFT(fftInData);
						}
						//Log.i(TAG, "4------------"); // test
						if (result == null)
						{
							Log.i(TAG, "fft reutn null.");
							continue;
						}

//						if (true)
//						{
//							datanum = 0;
//							continue; // test
//						}


						//float[] outFloat = new float[l];
						int c = l; // l / 2;
						if (true)
						{
							for(int i = 0; i < c; i++)
							{
								outFloat[i] = result[i];
								if (outFloat[i] < 0)
								{
									outFloat[i] = -outFloat[i];
								}
							}
						}

						//Log.i(TAG, "5------------"); // test

						synchronized (bufFFT) {
							//bufFFT.add(outInt);
							bufFFT.add(outFloat);
						}

						//Log.i(TAG, "6------------"); // test
					}

					// 保留剩余的数据
					int dataleft = datanum - fftDataNum;
//					for (int i = 0; i < dataleft; i++) {
//						buffer[i] = buffer[fftDataNum + i];
//					}
					System.arraycopy(buffer, fftDataNum, buffer, 0, dataleft);
					datanum = dataleft;

					//Log.i(TAG, "7------------"); // test
				}

				audioRecord.stop();
			} catch (Exception e) {
				// TODO: handle exception
				Log.i("Rec E", e.toString());
			}

			Log.i(TAG, "audio record thread end.------------"); // test

		}
	}

	//绘图线程
	Bitmap mBitmapPeer;	// 显示spectrum powerline...
	Canvas piccanvasPeer;
	Bitmap mBitmapNote; // 缓冲识别的单个note
	Canvas piccanvasNote;
	long  LastKeyT = 0;	// 记录最新识别的key的对应的keyT[]时刻, 用于强调显示
	long  LastKeyTKeepTime = 4000;	// 显示延长时间 2000
	long  LastKeyShowT = 0;			// 开始显示时间

	class DrawThread extends Thread{
		//画板
		private SurfaceView sfvSurfaceView;
		private SurfaceView sfvSurfaceViewHor;
		private TextView textView;
		//当前画图所在屏幕x轴的坐标
		//画笔
		private Paint mPaint;
		private Paint tPaint;
		private Paint specPaint;
		private Paint dashPaint;
		public DrawThread(SurfaceView sfvSurfaceView, SurfaceView sfvSurfaceViewHor, TextView tv) {
			this.sfvSurfaceView = sfvSurfaceView;
			this.sfvSurfaceViewHor = sfvSurfaceViewHor;
			this.textView = tv;

			mBitmapPeer = Bitmap.createBitmap(sfvSurfaceView.getWidth(), sfvSurfaceView.getHeight(), Bitmap.Config.ARGB_8888);
			piccanvasPeer = new Canvas(mBitmapPeer);  //绘制spectrum

			mBitmapNote = Bitmap.createBitmap(400, 220, Bitmap.Config.ARGB_8888); // 260
			piccanvasNote = new Canvas(mBitmapNote);  //绘制note

			//设置画笔属性
			mPaint = new Paint();
			mPaint.setColor(Color.BLUE);
			mPaint.setStrokeWidth(2);
			mPaint.setAntiAlias(true);
			tPaint = new Paint();
			tPaint.setColor(Color.YELLOW);
			tPaint.setStrokeWidth(1);
			tPaint.setAntiAlias(true);
			specPaint = new Paint();
			specPaint.setColor(Color.YELLOW);
			specPaint.setStrokeWidth(1);
			specPaint.setAntiAlias(true);
			dashPaint = new Paint();
			dashPaint.setStyle(Paint.Style.STROKE);
			dashPaint.setColor(Color.GRAY);
			Path path = new Path();
			path.moveTo(0, 10);
			path.lineTo(480,10);
			PathEffect effects = new DashPathEffect(new float[]{5,5,5,5},1);
			dashPaint.setPathEffect(effects);
		}

		public void run() {
			int CurX = 0;
			int drawScorllSpec = 1;	// 画连续谱

			long mT0 = System.currentTimeMillis();
			long mCurT = mT0;
			float mXScale = (float)0.03; //0.03; //0.01;	// pixel per ms
			float mXScaleKeyList = (float)mXScale * 4; //0.03; //0.01;	// pixel per ms
			int winW = sfvSurfaceView.getWidth();
			int leftmargin = winW / 50;
			int rightmargin = leftmargin;
			int winXBase = leftmargin;
			int winW_act = winW - leftmargin - rightmargin;

			// 清屏
			Canvas canvas = sfvSurfaceView.getHolder().lockCanvas(
					new Rect(0, 0, sfvSurfaceView.getWidth(),sfvSurfaceView.getHeight()));
			tPaint.setColor(Color.BLACK);
			piccanvasPeer.drawRect(0, 0, sfvSurfaceView.getWidth(),sfvSurfaceView.getHeight(), tPaint);
			sfvSurfaceView.getHolder().unlockCanvasAndPost(canvas);

			while (isRecording) {

				// wg
				mKeys.OnAudioData();

				//ArrayList<int[]>buf = new ArrayList<int[]>();
				ArrayList<float[]>buf = new ArrayList<float[]>();
/*				synchronized (bufPCM) {
					if (bufPCM.size() == 0) {
						continue;
					}
					buf = (ArrayList<int[]>)bufPCM.clone();
					bufPCM.clear();
				}*/
				synchronized (bufFFT) {
					if (bufFFT.size() == 0) {
						continue;
					}
					buf = (ArrayList<float[]>)bufFFT.clone();
					bufFFT.clear();
				}
				//根据ArrayList中的short数组开始绘图
				float[]tmpBuf = new float[buf.size()];
				for(int i = 0; i < buf.size(); i++){
					tmpBuf = buf.get(i);
				}

				// 尺寸变更
				if (winW != sfvSurfaceView.getWidth())
				{
					winW = sfvSurfaceView.getWidth();
					leftmargin = winW / 50;
					rightmargin = leftmargin;
					winXBase = leftmargin;
					winW_act = winW - leftmargin - rightmargin;

					mBitmapPeer = Bitmap.createBitmap(sfvSurfaceView.getWidth(), sfvSurfaceView.getHeight(), Bitmap.Config.ARGB_8888);
					piccanvasPeer = new Canvas(mBitmapPeer);  //绘制收包图
				}

				long PreT = mCurT;
				//mCurT = System.currentTimeMillis();
				//mCurT = mT0 + (mKeys.mLQ.AudioDataBuf_CurPos + 0) / 16;	// 16000 / 1000
				long Tbase = (mKeys.mLQ.mDiscardDataNum) / 16;
				mCurT = mT0 + (mKeys.mLQ.AudioDataBuf_CurPos / 16) + Tbase;	// 16000 / 1000

				SimpleDraw(tmpBuf, rateY, baseLine, drawScorllSpec,
						mT0, Tbase, PreT, mCurT, mXScale, mXScaleKeyList, winW_act, leftmargin, piccanvasPeer); //, null); // canvas);

//				int PreX = CurX;
//				CurX = (int)((mCurT - mT0) * mXScale);
//				CurX = (CurX % winW_act) + leftmargin;
//				if (CurX > PreX)
//				{
//					// 回滚, clear screen
//
//				}
//				SimpleDraw(tmpBuf, rateY, baseLine, drawScorllSpec, PreX, CurX, piccanvasPeer); //, null); // canvas);


//				SimpleDraw(tmpBuf, rateY, baseLine, drawScorllSpec, CurX, piccanvasPeer); //, null); // canvas);
//				CurX++;
//				if (CurX > sfvSurfaceView.getWidth())
//				{
//					// 回滚
//					CurX = 0;
//					//canvas.drawColor(Color.BLACK);
//				}

				SimpleDrawHor(tmpBuf, rateY, baseLineHor);

				//根据ArrayList中的short数组计算基频
/*				int sampleRate = 11025;
				int nodesPerFrame = 1024;
				for(int i = 0; i < buf.size(); i++){
					calBaseFreq( buf.get(i), sampleRate,nodesPerFrame);
				}*/
				buf.clear();

				if (mKeys.KeyCount > 0)
				{
					//textView.setText("KeyCount is xxx");

					//textView.setText("KeyCount is " + String.valueOf(mKeys.KeyCount[0]));
					//textView.append("Hello,TextView!\n");

					//setContentView(textView);
					//textView.getHolder().unlockCanvasAndPost(canvas);
				}
			}

			Log.i(TAG, "audio drawing thread end.------------"); // test
		}

		//根据一个音频帧数据，估算基频
		private int calOneFrameACF(int[] wavFrame, int sampleRate) {
			// find the max one
			Float max = -999F;
			int index = 0;
			// calculate ACF
			for (int k = 0; k < wavFrame.length; k++) {
				Float sum = 0F;
				for (int i = 0; i < wavFrame.length - k; i++) {
					sum = sum + wavFrame[i] * wavFrame[i + k];
				}
				if (k > 25 && sum > max) {//？？？？25是干嘛的？？？？
					max = sum;
					index = k;
				}
			}
			return (int) sampleRate / index;
		}

		public  void calBaseFreq(int[] waveData, int sampleRate, int nodesPerFrame) {
			Log.i(TAG, "AudioProcess calBaseFreq" + waveData.length);
			int frames = 0;
			int dataLength = waveData.length;
			//while(frames < 2 * dataLength / nodesPerFrame-1)  //imp
			while (frames < dataLength / nodesPerFrame - 1) {
				int wavFrame2[] = new int[nodesPerFrame];
				int start = (int) (frames * nodesPerFrame);
				for (int j = start, k = 0; j < start + nodesPerFrame; j++, k++) {
					wavFrame2[k] = waveData[j];
				}
				// calculate the ACF of this frame
				float pitchFreqency = calOneFrameACF(wavFrame2, sampleRate);
				Log.i(TAG, "The pitch frequency is: " + pitchFreqency + " Hz");
				frames++;
			}
		}





		//HSV颜色空间转换为RGB空间颜色值
		// HSV (色相hue, 饱和度saturation, 明度value), 也称HSB (B指brightness) 是艺术家们常用的
		void Hsv2Rgb(float H, float S, float V, float R[], float G[], float B[])
		{
			int i;
			float f, p, q, t;

			if( S == 0 )
			{
				// achromatic (grey)
				R[0] = G[0] = B[0] = V;
				return;
			}

			H /= 60; // sector 0 to 5
			i = (int)H; //floor( H );
			f = H - i; // factorial part of h
			p = V * ( 1 - S );
			q = V * ( 1 - S * f );
			t = V * ( 1 - S * ( 1 - f ) );

			switch( i )
			{
				case 0:
					R[0] = V;
					G[0] = t;
					B[0] = p;
					break;
				case 1:
					R[0] = q;
					G[0] = V;
					B[0] = p;
					break;
				case 2:
					R[0] = p;
					G[0] = V;
					B[0] = t;
					break;
				case 3:
					R[0] = p;
					G[0] = q;
					B[0] = V;
					break;
				case 4:
					R[0] = t;
					G[0] = p;
					B[0] = V;
					break;
				default: // case 5:
					R[0] = V;
					G[0] = p;
					B[0] = q;
					break;
			}
		}

		//int sss[] = new int[1000];
		int MapColor (int s)
		{
			/*
		    if ( s < 16 )
		        return RGB(0, 0, 128);
		    else if ( s < 32)
		        return RGB(0, 0, 255);
		    else if ( s < 64 )
		        return RGB(0, 255, 0);
		    else if ( s < 128)
		        return RGB(255, 255, 0);
		    else if ( s < 256 )
		        return RGB(255, 128, 0);
		    else
		        return RGB(255, 0, 0);
		        */

			if (s < 1000)
			{
				//sss[s]++;
			}
			else
			{
				s = s+1;
			}


			float saturation;
		/*
			if ( s < 16 )
		        saturation = 1 / 6;
		    else if ( s < 32)
		        saturation = 2 / 6;
		    else if ( s < 64 )
		        saturation = 3 / 6;
		    else if ( s < 128)
		        saturation = 4 / 6;
		    else if ( s < 256 )
		        saturation = 5 / 6;
		    else
		        saturation = 6 / 6;
		*/
			float r[] = new float[1];
			float g[] = new float[1];
			float b[] = new float[1];
			float s0 = 20, s1 = 150, base = 50; // s0 第一段长
			float hue = 270;

			if (s < s0)
				Hsv2Rgb(hue, 1, base + (float)s * (255 - base) / s0, r, g, b);
			else if ( s < s1)
				Hsv2Rgb(hue, 1 - ((float)s - s0) / (s1 - s0), 255, r, g, b);
			else
				Hsv2Rgb(hue, 0, 255, r, g, b);
			//return RGB(r, g, b);
			int RGB;
			RGB = (((int)r[0]) << 16) + (((int)g[0]) << 8) + (((int)b[0]));

			return RGB;
		}

















		//String KeyString[] = new
		String keyId2Desc(int key)
		{
			String s;

			if (key > 0 && key <= 88)
			{
			}
			else
			{
				return null;
			}

			int k = key - 1;
			int m = (k - 3 + 12) % 12;

			String c = "";
			String sn = ""; // sing name do re mi
			switch(m)
			{
				case 0:  c = " C"; sn = "do"; break;
				case 1:  c = "#C"; sn = "do"; break;
				case 2:  c = " D"; sn = "re"; break;
				case 3:  c = "#D"; sn = "re"; break;
				case 4:  c = " E"; sn = "mi"; break;
				case 5:  c = " F"; sn = "fa"; break;
				case 6:  c = "#F"; sn = "fa"; break;
				case 7:  c = " G"; sn = "so"; break;
				case 8:  c = "#G"; sn = "so"; break;
				case 9:  c = " A"; sn = "la"; break;
				case 10: c = "#A"; sn = "la"; break;
				case 11: c = " B"; sn = "si"; break;
				default:break;
			}

			int grp = 0; // 组号, 0, 1, 2...
			if ( k < 3)
			{
				grp = 0;
			}
			else
			{
				grp = (k - 3) / 12;
				grp++;
			}

			// +"("+mKeys.KeyN[mKeys.KeyCount - 1]+")";
			//s = c + " " + String.valueOf(grp);
			//s = c + String.valueOf(grp) + "("+key+")"+"\r\n" + sn;
			s = c + String.valueOf(grp);
			s += "("+key+")";
			//s += "\r\n" + sn;
			//CharSequence ss = c + String.valueOf(grp) + "("+key+")"+"\r\n" + sn;

			return s;
		}


		/**
		 * 绘制指定区域
		 *
		 * @param start
		 *            X 轴开始的位置(全屏)
		 * @param buffer
		 *             缓冲区
		 * @param rate
		 *            Y 轴数据缩小的比例
		 * @param baseLine
		 *            Y 轴基线
		 */

		int FFTcolor[] = new int[4096];
		private void SimpleDraw(float[] buffer, int rate, int baseLine, int drawScorllSpec,
								long T0, long Tbase,
								long PreT,			// spectrum
								long CurT,
								float XScale,		// spectrum, powerline x方向比例
								float XScaleKeyList,	// key list x方向比例
								int winW_act, int leftmargin,
								Canvas canvasD){

			baseLine = sfvSurfaceView.getHeight() - 20;
			int powerline_h = sfvSurfaceView.getHeight() / 8;

			//Log.i(TAG, "SimpleDraw---------001");
//			Canvas canvas = sfvSurfaceView.getHolder().lockCanvas(
//					new Rect(0, 0, sfvSurfaceView.getWidth(),sfvSurfaceView.getHeight()));

			float x_end_margin = 20;                                         //x轴的尾部的margin
			float x_length = sfvSurfaceView.getWidth()-shift- x_end_margin;  //用来表示频率的x轴实际长度
			float y_length = sfvSurfaceView.getHeight()-shift- x_end_margin;  //用来表示频率的x轴实际长度
			float x_hz_max = 4096; // 5512;                                           //x轴最大频率
			float x_hz_line_num = 24;                                        //x轴上画的频率格线数
			float x_hz_step = x_length/x_hz_line_num;                        //最高频率表示到5512,
			float hz_per_pix = x_hz_max/x_length;                            //每个长度单位代表的频率
			float vt_per_pix = x_hz_max/y_length;                            //每个长度单位代表的频率

			//Log.i(TAG, "frequence="+frequence+ " buffer.length="+buffer.length);


			//Log.i(TAG, "SimpleDraw---------002");
			float x0, x1, y0, y1,x2,y2,y_db;
			float y_top = 20;

			//绘制频域谱线
			float maxA = 0;
			float hzOfMaxA = 0;

			int  nodesPerFrame = 800;
			int frames = 0;

			//Log.i(TAG, "SimpleDraw---------003");
			int xc = buffer.length; //

			int newkey = 0;

			try
			{
				if (drawScorllSpec > 0)
				{
					// 连续谱, 横向延伸


					// spectrum
					{
						tPaint.setStrokeWidth(1);
						tPaint.setColor(Color.YELLOW);
						canvasD.drawLine(1, y_length - 1, sfvSurfaceView.getWidth() - 1, y_length - 1, tPaint);//X轴箭头 (上)baseLine

						int CurX = (int)((CurT - T0) * XScale);
						int PreX = (int)((PreT - T0) * XScale);
						CurX = (CurX % winW_act) + leftmargin;
						PreX = (PreX % winW_act) + leftmargin;

						if (CurX < PreX)
						{
							// 回滚
							//CurX = 0;
							canvasD.drawColor(Color.BLACK);

							return;
						}

						float specH = y_length - 1 - powerline_h;
						float kh = specH / xc;
						for(int i = 0; i < xc; i++){
							y1 = specH - i * kh; // - x_end_margin; // shift;

							//canvas.drawLine(x1,y1,x2,y2,mPaint);
							int cc = (int)(buffer[i] / 400); // 200
							if (cc < 0)
							{
								cc = -cc;
							}
							//float k = (float)1.0 / 256;
							//int color = MapColor((int)(k * cc));
							int color = MapColor((int)(cc));
							FFTcolor[i] = color;
						}
//						for(int x = PreX; x < CurX; x++){	// 效率可优化, 不必重复计算MapColor
//							for(int i = 0; i < xc; i++){
//								y1 = specH - i * kh; // - x_end_margin; // shift;
//
//								//.setColor(0xff0000 | 0xFF000000); // 红
//								specPaint.setColor(FFTcolor[i] | 0xFF000000);
//								//canvas.drawLine(CurX,x1,CurX + 1,x1,specPaint);
//								canvasD.drawPoint(x,y1,specPaint);
//							}
//						}
						//for(int x = PreX; x < CurX; x++)
						{	// 效率可优化, 不必重复计算MapColor
							for(int i = 0; i < xc; i++){
								y1 = specH - i * kh; // - x_end_margin; // shift;

								//.setColor(0xff0000 | 0xFF000000); // 红
								specPaint.setColor(FFTcolor[i] | 0xFF000000);
								//canvas.drawLine(CurX,x1,CurX + 1,x1,specPaint);
								//canvasD.drawPoint(x,y1,specPaint);
								canvasD.drawLine(PreX,y1,CurX,y1,specPaint);
							}
						}
					}

					// powerline
					int powerline_CurPos = mKeys.mLQ.powerline_CurPos;
					if (true)
					{
						tPaint.setStrokeWidth(1);
						tPaint.setColor(Color.YELLOW);
						canvasD.drawLine(1, y_length - powerline_h - 1, sfvSurfaceView.getWidth() - 1, y_length - powerline_h - 1, tPaint);//X轴箭头 (上)baseLine

						// 修正
						int powerline_DrawedPos = mKeys.mLQ.powerline_DrawedPos - 3;
						if (powerline_DrawedPos < 0)
						{
							powerline_DrawedPos = 0;
						}

						//int powerline_CurPos = (int)(mKeys.mLQ.powerline_CurPos);
						int PointCount = powerline_CurPos - powerline_DrawedPos;

						tPaint.setColor(Color.YELLOW);

						float ky = (float)200.0 / (10000 * 5);
						//int shift = (int)(Tbase / mKeys.mLQ.powern);
						for(int c = 0; c < PointCount - 1; c++){	// 效率可优化, 不必重复计算MapColor
							//canvas.drawLine(CurX,x1,CurX + 1,x1,specPaint);
							float t0 = (powerline_DrawedPos + c) * mKeys.mLQ.powern / 16 + Tbase; // 16000 / 1000
							float t1 = (powerline_DrawedPos + c + 1) * mKeys.mLQ.powern / 16 + Tbase; // 16000 / 1000
							int CurX = (int)((t1) * XScale);
							int PreX = (int)((t0) * XScale);
							CurX = (CurX % winW_act) + leftmargin;
							PreX = (PreX % winW_act) + leftmargin;
							y0 = mKeys.mLQ.powerline[powerline_DrawedPos + c] * ky;
							y1 = mKeys.mLQ.powerline[powerline_DrawedPos + c + 1] * ky;
							if (CurX < PreX)
							{
								// 回滚
								break;
							}
							//canvasD.drawLine(PreX, y_length - 1 - y0, CurX, y_length - 1 - y1, tPaint);
							canvasD.drawLine(PreX, powerline_h - 1 - y0 + (y_length - powerline_h),
									CurX, powerline_h - 1 - y1 + (y_length - powerline_h), tPaint);
						}
						mKeys.mLQ.powerline_DrawedPos = powerline_CurPos;

						//mKeys.powerline_DrawedPos = 0;
					}

					// hits
					{
						int starthit = mKeys.KeyCount - 1;
						if (starthit < 0)
						{
							starthit = 0;
						}
						int yhit0 = (int)(powerline_h - 1 - 0 + (y_length - powerline_h));
						int yhit = (int)(powerline_h - 1 - powerline_h / 2 + (y_length - powerline_h));
						float t;
						int x;
						float t0;
						int CurX_PL;
						int hitpos;
						int hitfftpos;
						for(int i = starthit; i < mKeys.KeyCount; i++)
						{
							// hitpos
							{
								tPaint.setColor(Color.RED);

								hitpos = mKeys.KeyHitPos[i];
								t = (float)(hitpos * mKeys.mLQ.powern / 16); // + Tbase); // 相对t
								x = (int)(t * XScale);
								x = (x % winW_act) + leftmargin;
								//canvasD.drawLine(x, yhit, x, yhit0, tPaint);

								t0 = (powerline_CurPos) * mKeys.mLQ.powern / 16 + Tbase; // 16000 / 1000 // 绝对t
								CurX_PL = (int)((t0) * XScale);
								CurX_PL = (CurX_PL % winW_act) + leftmargin;

								if (x <= CurX_PL)
								{
									canvasD.drawLine(x, yhit, x, yhit0, tPaint);
								}
							}

							// fftpos
							{
								tPaint.setColor(Color.GREEN);

								hitfftpos = mKeys.KeyHitFFTPos[i];
								t = (float)(hitfftpos * mKeys.mLQ.powern / 16); // + Tbase);
								x = (int)(t * XScale);
								x = (x % winW_act) + leftmargin;
								//canvasD.drawLine(x, yhit, x, yhit0, tPaint);

								t0 = (powerline_CurPos) * mKeys.mLQ.powern / 16 + Tbase; // 16000 / 1000
								CurX_PL = (int)((t0) * XScale);
								CurX_PL = (CurX_PL % winW_act) + leftmargin;

								if (x <= CurX_PL)
								{
									// fft pos
									canvasD.drawLine(x, yhit, x, yhit0, tPaint);

									// select spectrum
									{
										int harmOffset =  5 + 4096;
										int harmOfN = i * mKeys.harmnoicSeg;
										if (i < mKeys.KeyCount)
										{
											int khh = mKeys.KeyFFTSize[i] / 2048;

											float specH = y_length - 1 - powerline_h;
											float kh = specH / xc;

											for(int h = 0; h < mKeys.harmnoicSeg; h++)
											{
												//if (mKeys.KeyHarmnic[harmOfN + h] == 0)
												if (mKeys.KeyHarmnic[harmOfN + h * khh] == 0)
												{
													continue;
												}

												y1 = specH - kh * h; // - x_end_margin; // shift;

												//											int cc = (int)(buffer[i] / 200);
												//											if (cc < 0)
												//											{
												//												cc = -cc;
												//											}
												//											float k = (float)1.0 / 256;
												//											int color = MapColor((int)(cc));
												//
												//											specPaint.setColor(color | 0xFF000000);
												canvasD.drawLine(x-3,y1,x + 3,y1,tPaint);
												//											canvasD.drawPoint(x,y1,specPaint);
											}
										}
									}
								}
							}
						}
						tPaint.setColor(Color.YELLOW);
					}

					// 删除数据区域指示
					{
						//if (Tbase > 1)
						{
							int CurX = (int)((Tbase) * XScale);	// 删除指示
							CurX = (CurX % winW_act) + leftmargin;

							float t0 = (powerline_CurPos) * mKeys.mLQ.powern / 16 + Tbase; // 16000 / 1000
							int CurX_PL = (int)((t0) * XScale);
							CurX_PL = (CurX_PL % winW_act) + leftmargin;

							if (CurX <= CurX_PL)
							{
								canvasD.drawLine(leftmargin, y_length / 2, CurX, y_length / 2, tPaint);
							}
						}
					}

					// key
					if (mKeys.KeyCount > 0)
					{
						int yGap = 15;

						tPaint.setColor(Color.YELLOW);

						// key滚动文字
						String s = "";
						if (true)
						{
							if (mKeys.mScroll > 0)
							{
								mKeys.mScroll = 0;

								//Paint paint=new Paint();
								tPaint.setColor(Color.BLACK);
								canvasD.drawRect(0, 0, x_length, 3 * yGap + yGap + 5, tPaint);
							}

							int lineid = 0;
							int c = 0;
							for(int l = 0; c < mKeys.KeyCount; l++)
							{
								s = "";
								for(int i = 0;
									c < mKeys.KeyCount
											&& i < mKeys.lineMember;
									i++, c++)
								{
									//s = s + " " + String.valueOf(mKeys.KeyFs[c]);
									s = s + " " + String.valueOf(mKeys.KeyN[c]);
								}

								canvasD.drawText(s, 0, s.length(), 2, (l + 2) * yGap, tPaint);
							}
						}
						else
						{
							int starthit = 0;
							if (starthit < 0)
							{
								starthit = 0;
							}
							float t;
							float t0;
							int CurX_key;
							int CurY_key;
							int hitpos;

							//显示所有测到的Key值
							for(int i = starthit; i < mKeys.KeyCount; i++)
							{
								// hitpos
								{
									//tPaint.setColor(Color.RED);

									hitpos = mKeys.KeyHitPos[i];
									t = (float)(hitpos * mKeys.mLQ.powern / 16); // + Tbase); // 相对t

									// key滚动文字
									{
										t0 = t; // (powerline_CurPos) * mKeys.mLQ.powern / 16 + Tbase; // 16000 / 1000 // 绝对t
										CurX_key = (int)((t0) * XScaleKeyList);
										int lineid = CurX_key / winW_act;
										lineid -= mKeys.mScrollLineNum;
										CurX_key = (CurX_key % winW_act) + leftmargin;

										String ss = String.valueOf(mKeys.KeyN[i]);
										canvasD.drawText(ss, 0, ss.length(), CurX_key, lineid * yGap, tPaint);
									}
								}
							}
						}

						// last key, 强调显示
						{
							if (LastKeyT != mKeys.KeyT[mKeys.KeyCount - 1])
							{
								// key idx: mKeys.KeyCount - 1

								LastKeyT = mKeys.KeyT[mKeys.KeyCount - 1];
								//						int xo = sfvSurfaceView.getWidth() / 2 - mBitmapNote.getWidth() / 2;
								//						int yo = sfvSurfaceView.getHeight() / 2 - mBitmapNote.getHeight() / 2;
								int xo = mBitmapNote.getWidth() / 2;
								int yo = mBitmapNote.getHeight() / 2;
								//String keyS = String.valueOf(mKeys.KeyN[mKeys.KeyCount - 1]);
								String keyS = keyId2Desc(mKeys.KeyN[mKeys.KeyCount - 1]);
								//String KeyFDeviation = String.valueOf(mKeys.key88[mKeys.KeyN[mKeys.KeyCount - 1]] - mKeys.KeyFs[mKeys.KeyCount - 1]);
								float FDeviation = mKeys.KeyFs[mKeys.KeyCount - 1] - mKeys.key88[mKeys.KeyN[mKeys.KeyCount - 1] - 1];
								//keyS = keyS+"("+mKeys.KeyN[mKeys.KeyCount - 1]+")";

								Paint mPaint = new Paint();
								mPaint.setStrokeWidth(3);

								// clear
								mPaint.setColor(Color.BLACK);
								piccanvasNote.drawRect(0, 0, mBitmapNote.getWidth(), mBitmapNote.getHeight(), mPaint);

								// key
								//{
								mPaint.setTextSize(80);
								mPaint.setColor(Color.WHITE);
								//mPaint.setTextAlign(Align.LEFT);
								mPaint.setTextAlign(Align.CENTER);
								Rect bounds = new Rect();
								mPaint.getTextBounds(keyS, 0, keyS.length(), bounds);
								int w = bounds.width();
								int h = bounds.height();
								//piccanvasNote.drawText(s, xo - w/2, yo - h/2, mPaint);
								//piccanvasNote.drawText(s, xo - w/2, yo - h/2, mPaint);
								piccanvasNote.drawText(keyS, xo, yo + h/5, mPaint);
								//}

								// key fs, deviation
								{
									mPaint.setTextSize(30);

									//								float key88 = mKeys.key88[mKeys.KeyN[mKeys.KeyCount - 1]];
									//								key88 = ((int)(key88 * 10));
									//								int key88_int = (int)key88 / 10;
									//								int key88_tail = ((int)key88) % 10;
									//								float Deviation = ((int)(FDeviation * 10));
									//								int Deviation_int = (int)Deviation / 10;
									//								int Deviation_tail = ((int)Deviation) % 10;
									//								String KeyFsS = key88_int + "." + key88_tail + "  " + Deviation_int + "." + Deviation_tail;
									int keyid = mKeys.KeyN[mKeys.KeyCount - 1] - 1;
									float key88 = mKeys.key88[keyid]; 	// 1.234
									float key88t = (float)((int)(key88 * 10)) / 10;				// 1.2

									float Deviation = (float)((int)(FDeviation * 10)) / 10;

									float DeviationRatio = Deviation / key88;
									DeviationRatio *= 100;
									float DeviationR = (float)((int)(DeviationRatio * 10)) / 10;

									String KeyFsS = key88t + "Hz  " + Deviation + "Hz(" + DeviationR + "%)";

									//Rect bounds = new Rect();
									mPaint.getTextBounds(KeyFsS, 0, KeyFsS.length(), bounds);
									int hFs = bounds.height();
									piccanvasNote.drawText(KeyFsS, xo, yo + h/4 + h * 3 / 5  + hFs / 4, mPaint);
								}

								LastKeyShowT = System.currentTimeMillis(); // 显示多长时长

								newkey = 1;
							}
						}
					}
				}

				//Log.i(TAG, "SimpleDraw---------005");
				//getBaseFreq(buffer);
				//Log.i(TAG, "MAX----Frequence is："+hzOfMaxA);	/////////

				Canvas canvas = sfvSurfaceView.getHolder().lockCanvas(
						new Rect(0, 0, sfvSurfaceView.getWidth(),sfvSurfaceView.getHeight()));

				Matrix matrix = new Matrix();
				matrix.setTranslate(1, 1);

				// 主图
				canvas.drawBitmap(mBitmapPeer, matrix, mPaint);

				// key显示
				if (LastKeyShowT > 0)
				{
					// 扣除时间
					float t = System.currentTimeMillis() - LastKeyShowT; // key过去了多少ms
					if (LastKeyTKeepTime > (long)t)	// 显示过时?
					{
						float k = (LastKeyTKeepTime - t) / LastKeyTKeepTime;	// 透明度系数
						k = k * k * k * k;

						float alpha = 255 * k;  //135
						mPaint.setAlpha( (int)alpha ); // 半透明

						int x = (sfvSurfaceView.getWidth() - mBitmapNote.getWidth()) / 2;
						canvas.drawBitmap(mBitmapNote, x, 100, mPaint); // 显示

						mPaint.setAlpha( 255 ); // 不透明
					}
					else
					{
						LastKeyShowT = 0;
					}
				}

				sfvSurfaceView.getHolder().unlockCanvasAndPost(canvas);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		private void SimpleDrawHor(float[] buffer, int rate, int baseLine){

			// test
			baseLine = sfvSurfaceViewHor.getHeight() - 20;

			//Log.i(TAG, "SimpleDraw---------001");
			Canvas canvas = sfvSurfaceViewHor.getHolder().lockCanvas(
					new Rect(0, 0, sfvSurfaceViewHor.getWidth(),sfvSurfaceViewHor.getHeight()));

			canvas.drawColor(Color.BLACK);

/*			Canvas canvas = sfvSurfaceView.getHolder().lockCanvas(
			               new Rect(0, 0, buffer.length,sfvSurfaceView.getHeight()));
			*/
			canvas.drawText("幅度值", 0, 3, 2, 15, tPaint);
			//canvas.drawText("原点(0,0)", 0, 7, 5, baseLine + 15, tPaint);
			canvas.drawText("0", 0, 1, shift - 5, baseLine + 15, tPaint);
			canvas.drawText("频率(HZ)", 0, 6, sfvSurfaceViewHor.getWidth() - 50, baseLine + 30, tPaint);
			canvas.drawLine(shift, 20, shift, baseLine, tPaint);  //绘制Y轴
			canvas.drawLine(shift, baseLine, sfvSurfaceViewHor.getWidth(), baseLine, tPaint); //绘制X轴
			canvas.save();
			canvas.rotate(30, shift, 20);
			canvas.drawLine(shift, 20, shift, 30, tPaint); //Y轴箭头 (左)
			canvas.rotate(-60, shift, 20);
			canvas.drawLine(shift, 20, shift, 30, tPaint); //Y轴箭头 (右)
			canvas.rotate(30, shift, 20);
			canvas.rotate(30, sfvSurfaceViewHor.getWidth()-1, baseLine);
			canvas.drawLine(sfvSurfaceViewHor.getWidth() - 1, baseLine, sfvSurfaceViewHor.getWidth() - 11, baseLine, tPaint);//X轴箭头 (上)
			canvas.rotate(-60, sfvSurfaceViewHor.getWidth()-1, baseLine);
			canvas.drawLine(sfvSurfaceViewHor.getWidth() - 1, baseLine, sfvSurfaceViewHor.getWidth() - 11, baseLine, tPaint);//X轴箭头 (下)
			canvas.restore();

			float x_end_margin = 20;                                         //x轴的尾部的margin
			float x_length = sfvSurfaceViewHor.getWidth()-shift- x_end_margin;  //用来表示频率的x轴实际长度
			float x_hz_max = 4500;                                           //x轴最大频率
			float x_hz_line_num = 10;                                        //x轴上画的频率格线数
			float x_hz_step = x_length/x_hz_line_num;                        //最高频率表示到5512,
			float hz_per_pix = x_hz_max/x_length;                            //每个长度单位代表的频率
			//Log.i(TAG, "frequence="+frequence+ " buffer.length="+buffer.length);
			//绘制频率提示格线
			for(int i = 1; i<= x_hz_line_num ;i++){
				canvas.drawLine(shift + x_hz_step*(float)i, baseLine, shift + x_hz_step*(float)i, 40, dashPaint);  //频率格线
				String str = String.valueOf((int)(x_hz_max / x_hz_line_num * i));
				canvas.drawText( str, 0, str.length(), shift + x_hz_step*(float)i - 15, baseLine + 15, tPaint);  //频率值
			}
			//Log.i(TAG, "SimpleDraw---------002");
			float x1,y1,x2,y2,y_db;
			float y_top = 20;

			//绘制频域谱线
			float maxA = 0;
			float hzOfMaxA = 0;

			int  nodesPerFrame = 800;
			int frames = 0;

			int xc = buffer.length; //
			//int f = frequence;
			//float k = (float)x_hz_max/(float)xc * frequence / 8000;
			float k = (float)1.0 / xc * (frequence / 2) / hz_per_pix;
			for(int i = 0; i < xc; i++){
				if(buffer[i] > maxA){
					maxA = buffer[i];
					hzOfMaxA = (float)k * (float)i;
				}
				//Log.i(TAG, "------i="+i+ "  freq="+(float)frequence/((float)buffer.length)*(float)i+ " A="+buffer[i]);
				x1 = (float)i * k + (float)shift;
				//x1 = k *(float)i/hz_per_pix + (float)shift;
				//x2 = k *(float)i/hz_per_pix + (float)shift ;
				x2 = x1;

				y1 = baseLine;

                /*计算Y2：方法1------------------------------------------------------------------------------*/
				/*计算Y2：方法1，普通算法*/
				//y2 = baseLine - buffer[i] / rateY ;  //常规计算，得出的谱线，Y值差距比较大
				y2 = baseLine - buffer[i] / 400 ;  //常规计算，得出的谱线，Y值差距比较大

				/*计算Y2：方法2------------------------------------------------------------------------------*/
				/*方法2，现将FFT结果进行20*lg(abs(Y)/Y0)表示成dB值，这时用Y0=1-------------------------------*/
				/*y_db= (float)20*(float)Math.log10((float)buffer[i]);  //计算FFT结果进行的DB值
				y2 = baseLine - y_db;*/
				//取了对数以后，差别太小了，看不出频率差异
               /*计算Y2：-----------------------------------------------------------------------------------*/

				if(y2 < y_top ){
					y2 = y_top;
				}
				//用20*lg(abs(Y)/Y0)表示成dB值，这时用Y0=1，这样得到的是相对值
				//canvas.drawLine(((float)frequence/(float)buffer.length)*(float)i/hz_per_pix + (float)shift, baseLine, (((float)frequence/(float)buffer.length)*(float)i/hz_per_pix) +(float)shift, y2, mPaint);
				canvas.drawLine(x1,y1,x2,y2,mPaint);
			}
			//Log.i(TAG, "SimpleDraw---------005");
			//getBaseFreq(buffer);
			//Log.i(TAG, "MAX----Frequence is："+hzOfMaxA);	/////////
			sfvSurfaceViewHor.getHolder().unlockCanvasAndPost(canvas);
		}
	}

	private float getBaseFreq(int[] fftBuf){
		int maxA = 0;
		int posOfMaxA = 0;
		float hzOfMaxA = 0;
		for(int i = 0; i <= fftBuf.length/2; i++) {
			if (fftBuf[i] > maxA) {
				maxA = fftBuf[i];
				hzOfMaxA = (float) frequence / ((float) fftBuf.length) * (float) i;
				posOfMaxA = i;
			}
		}
		Log.i(TAG, "getBaseFreq -->posOfMaxA"+posOfMaxA+"  hzOfMaxA="+hzOfMaxA);
		int tmpmaxPos = 0;
		int tmpmaxSum = 0;
		int freq_sum[]= new int[posOfMaxA];
		int lastStep=0;
		for(int i = 1; i<=posOfMaxA; i++){
			int step = posOfMaxA/i;
			if(lastStep == step){
				break;
			}else{
				lastStep=step;
			}
			//for(int index = step; index<= fftBuf.length/2; index+=step){
			for(int index = step; index<= posOfMaxA; index+=step){
				freq_sum[i-1] += fftBuf[index];
			}
			freq_sum[i-1] = freq_sum[i-1]*step;
			//Log.i(TAG, "step="+step+"freq_sum[ "+(i-1)+"]"+freq_sum[i-1]);
			if(freq_sum[i-1] > tmpmaxSum){
				tmpmaxSum = freq_sum[i-1];
				tmpmaxPos = i;
			}
		}
		Log.i(TAG, "base pos is "+tmpmaxPos);

		int change[] = new int[posOfMaxA];
		for(int i = 1; i<freq_sum.length-1; i++){
			change[i-1] = freq_sum[i-1] - freq_sum[i];
			//Log.i(TAG, "change["+(i-1)+"]"+change[i-1]);
		}

		int changeSum[] = new int[freq_sum.length];
		for(int i = 1; i<changeSum.length;i++){
			for(int index = i; index<changeSum.length; index+=i) {
				changeSum[i-1] += change[index];
			}
			changeSum[i-1] = changeSum[i-1]*i;
		}

		int maxChangeSum = 0;
		int maxChangePos = 0;
		for(int i = 0; i<changeSum.length;i++){
			if (changeSum[i] > maxChangeSum){
				maxChangeSum = changeSum[i];
				maxChangePos = i;
			}
		}
		Log.i(TAG, "maxChangePos is "+maxChangePos+"BASE FREQ IS "+hzOfMaxA / maxChangePos);
		return 0;
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
	public void fft(Complex[] xin,int N)
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
}

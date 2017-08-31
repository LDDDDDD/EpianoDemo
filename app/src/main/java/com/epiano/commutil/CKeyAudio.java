package com.epiano.commutil;


import android.util.Log;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;


// Audio素材加载, 立体声合成

public class CKeyAudio {

	private int maxCount = 160000;	// 10s
	public int actDataLenInShorts = 0;	// 单声道对应的值, 如果是stereo，则实际数据量 = actDataLenInShorts * 2

	public short keyAudio[];		// 对外提供
	//private short keyAudioMono[]; // = new short[maxCount];
	private short keyAudioStereo[]; // = new short[maxCount];
	int keyAudioLenInShort = 0;
	public int keyId = -1;

	// channels
	int AudioChannels = 1;		// 必须为2

	// mono or stereo
	boolean stereoOn = true;

	// 文件是单声道的, 但如果参数AudioChannelsCount是2，则加载后，把文件转换成双声道
	public CKeyAudio(int AudioChannelsCount)
	{
		AudioChannels = AudioChannelsCount;
	}

	// 设置stereo开关
	public int SetStereoOn(boolean SetstereoOn)
	{
		stereoOn = SetstereoOn;

		if (stereoOn)
		{
			keyAudio = keyAudioStereo;

			mono2stereo();
		}
		else
		{
			//keyAudio = keyAudioMono;
			keyAudio = keyAudioStereo;

			MakeMono();
		}

		return 1;
	}

	public int readkeyaudiofile() {
		// String fileName = "/mnt/sdcard/audio_sample.dat";
		String sounddir = "/mnt/sdcard/NoteBmp/sound/";
		String fileName = sounddir + String.valueOf(keyId) + ".raw";

		int maxdata = 0;
		try {
			DataInputStream in = new DataInputStream(
					new BufferedInputStream(new FileInputStream(fileName)));
			if (in == null)
			{
				Log.i("WG", "Low mem, in is null");
				return 0;
			}

			//int skipcountseg = 1000000 * 2;

			int bc = 0;
			byte b[];
			b = new byte[maxCount * 2 * AudioChannels];
			if (b == null)
			{
				Log.i("WG", "Low mem, b is null");
				return 0;
			}

			// 正式数据
			bc = in.read(b, 0, maxCount * 2);
			in.close();

			// int maxdata = min(maxCount, (int)(bc/2));
			actDataLenInShorts = maxCount;
			if (actDataLenInShorts > (int) (bc / 2)) {
				actDataLenInShorts = (int) (bc / 2);
			}

			// 立体声音
			keyAudioStereo = new short[actDataLenInShorts * AudioChannels];
			if (keyAudioStereo == null)
			{
				Log.i("WG", "Low mem, keyAudioStereo is null");
				return 0;
			}

			// 非立体声音，暂时还是用双通道播放，只是没立体效果
//			keyAudioMono = new short[actDataLenInShorts * AudioChannels];
//			if (keyAudioMono == null)
//			{
//				Log.i("WG", "Low mem, keyAudioMono is null");
//				return 0;
//			}

			// 缺省
			if (stereoOn)
			{
				keyAudio = keyAudioStereo;
			}
			else
			{
				//keyAudio = keyAudioMono;
				keyAudio = keyAudioStereo;
			}

			for (int j = 0; j < actDataLenInShorts; j++) {
				int a = (int) (b[j * 2 + 1]);
				a &= 0xFF;
				a = a << 8;
				int a1 = (int) b[j * 2];
				a1 &= 0xFF;
				//keyAudioMono[j] = (short) (a + a1);
				keyAudioStereo[j] = (short) (a + a1);
			}

			// 把单声道扩展成双声道，但是mono效果
			for (int j = 0; j < actDataLenInShorts; j++) {
				//int id = actDataLenInShorts2 - 2 - 2 * j;
				int id = actDataLenInShorts - 1 - j;
				int id2 = id * 2;
				//keyAudioMono[id2] 		= keyAudioMono[id];
				//keyAudioMono[id2 + 1] 	= keyAudioMono[id];
				keyAudioStereo[id2] 		= keyAudioStereo[id];
				keyAudioStereo[id2 + 1] 	= keyAudioStereo[id];
			}

//			// 转stereo
//			if (AudioChannels == 2)
//			{
//				mono2stereo();
//			}

		} catch (Exception e) {
			e.printStackTrace();

			Log.i("WG", "Low mem, keyAudio is null, id:" + keyId);

			return 0;
		}

		return actDataLenInShorts;
	}

	// 转stereo
	public int mono2stereo() {

		int actDataLenInShorts2 = 2 * actDataLenInShorts;

		// 修改双声道相位和能量
		// 左边的key先到左耳, 所有把右声道的数据向晚推, 能量降低
		//float phasek = 0;
		//float amplyk = 0;
		int DiffIdx = 0;				// 左右耳声音数据索引差, DiffIdx > 0表示是左边的声音用时更长，来得晚， 对应右边的key
		float DiffPower = 0;				// 能量衰减系数
		{
			float Speed  = 340;			//m/s ms/ms
			float PWidth = 3000; //2500;//3000; //3000; //4000; //3000; //2000; //5000 20000; //2000; 		//ms	琴宽
			float keyW = PWidth / 88;	// 单个Key的宽度
			float P_H_Distance = 800; //1000; 	//ms  琴板离头的距离
			float Headwidth = 300; //200; //400; //200;		//ms   头宽
			float sampleR = 16000;		//
//			float Fs =	8;				//kHz

			float LR_pos = (float)0.5; //0.1; //0.5;	// 听众位置 0~1, 0.5:坐在正中间
			float LR_X = 0;				// 听众头中心坐标
			float L_Ear_X = 0;			// 左耳X坐标
			float R_Ear_X = 0;			// 右耳X坐标

//			float Wl = PWidth * LR_pos;			// 从坐的位置看，左边琴的宽度
//			float Wr = PWidth * (1 - LR_pos);	// 右
//			float Keyl = Wl - (Headwidth / 2) - keyId * keyW;
			float dl = 0;	// key到左耳的距离
			float dr = 0;	// key到左耳的距离
			float pl = 0;	// key到左耳的能量剩余值
			float pr = 0;	// key到左耳的能量剩余值
			float Diff_dl_dr = 0;			// 左右耳声音距离差
			float Diff_T = 0;	// 左右耳声音时间差

//			float KeyX0 = 0;			// 设置KeyX0为x轴原点

			// 头x坐标设置在与琴的中部对齐的位置
			LR_X = (LR_pos * PWidth);	//
			L_Ear_X = LR_X - Headwidth / 2;
			R_Ear_X = LR_X + Headwidth / 2;

			float KeyX = 0;		// Key的X坐标
			KeyX = keyW * keyId;

			float dl_x = L_Ear_X - KeyX;	// key与左耳的X距离
			float dr_x = R_Ear_X - KeyX;

			dl = (float)Math.sqrt(dl_x * dl_x + P_H_Distance * P_H_Distance);	// key到左耳的距离
			dr = (float)Math.sqrt(dr_x * dr_x + P_H_Distance * P_H_Distance);	// key到左耳的距离

			pl = (float)Math.sqrt(dl);
			pr = (float)Math.sqrt(dr);
			DiffPower = pl / pr;
			if (DiffPower > 1)
			{
				DiffPower = (float)1.0 / DiffPower;
			}

			if (L_Ear_X > KeyX && R_Ear_X > KeyX)
			{
				// Key在左

				Diff_dl_dr = dl - dr;			// 左右耳声音距离差
				Diff_T = Diff_dl_dr / Speed;	// 左右耳声音时间差
			}
			else if (L_Ear_X < KeyX && R_Ear_X > KeyX)
			{
				// Key在中

				Diff_dl_dr = dl - dr;			// 左右耳声音距离差
				Diff_T = Diff_dl_dr / Speed;	// 左右耳声音时间差
			}
			else if (L_Ear_X < KeyX && R_Ear_X < KeyX)
			{
				// Key在右

				Diff_dl_dr = dl - dr;			// 左右耳声音距离差
				Diff_T = Diff_dl_dr / Speed;	// 左右耳声音时间差
			}

			DiffIdx = (int)(Diff_T * sampleR / 1000);

			// test
			Log.i("WG", "KeyId:" + keyId
					+ ", Diff_T:" + Diff_T
					+ ", DiffIdx:" + DiffIdx);
		}

//		// 把单声道扩展成双声道，但是mono效果
//		for (int j = 0; j < actDataLenInShorts; j++) {
//			//int id = actDataLenInShorts2 - 2 - 2 * j;
//			int id = actDataLenInShorts - 1 - j;
//			int id2 = id * 2;
//			//keyAudioMono[id2] 		= keyAudioMono[id];
//			//keyAudioMono[id2 + 1] 	= keyAudioMono[id];
//			keyAudioStereo[id2] 		= keyAudioStereo[id];
//			keyAudioStereo[id2 + 1] 	= keyAudioStereo[id];
//		}


		// stereo效果
//		for (int j = 0; j < actDataLenInShorts; j++) {
//			int id = (actDataLenInShorts - 1 - j) * 2;
//			keyAudioStereo[id] 		= keyAudioMono[id];
//			keyAudioStereo[id + 1] 	= keyAudioMono[id + 1];
//		}


		int phasemod = 1;	// 0: 无相位差，等同于mono; 1: 有相位
		if (phasemod == 0)
		{
		}
		else if (phasemod == 1)
		{
			if (DiffIdx > 0)	// == 0 不用处理
			{
				// DiffIdx > 0表示是左边的声音用时更长，来得晚， 对应右边的key


				if (false)
				{
					// 把左边的数据向后推
					int DiffIdx2 = DiffIdx * 2;
					int maxdata = actDataLenInShorts - DiffIdx;
					//for (int j = DiffIdx; j < actDataLenInShorts; j++) {
					for (int j = 0; j < maxdata; j++) {
						int id = (actDataLenInShorts - 1 - j) * 2;
						keyAudioStereo[id] = (short)(DiffPower * keyAudioStereo[id - DiffIdx2]);
					}
				}
				else
				{
					// 把右边的数据向前推
					int DiffIdx2 = DiffIdx * 2;
					int maxdata = actDataLenInShorts - DiffIdx;
					for (int j = 0; j < maxdata; j++) {
						int id = j * 2 + 1;
						keyAudioStereo[id] = (short)(DiffPower * keyAudioStereo[id + DiffIdx2]);
					}
				}

//				for (int j = 0; j < DiffIdx2; j+=2) {
//					keyAudio[j] = 0;
//				}
			}
			else if (DiffIdx < 0)
			{
				int DiffIdx2 = DiffIdx * 2;
				int maxdata = actDataLenInShorts - (-DiffIdx);
				//for (int j = -DiffIdx; j < actDataLenInShorts; j++) {
				for (int j = 0; j < maxdata; j++) {
					int id = (actDataLenInShorts - 1 - j) * 2    + 1;
					keyAudioStereo[id] = (short)(DiffPower * keyAudioStereo[id - (-DiffIdx2)]);
				}

//				for (int j = 1; j < DiffIdx2; j+=2) {
//					keyAudio[j] = 0;
//				}
			}
		}

		return 1;
	}

	// 转stereo
	public int MakeMono() {

		// 用左声道覆盖右声道
		for (int j = 0; j < actDataLenInShorts; j++) {
			int id = actDataLenInShorts - 1 - j;
			int id2 = id * 2;
			keyAudioStereo[id2 + 1] = keyAudioStereo[id2];
		}

		return 1;
	}

}

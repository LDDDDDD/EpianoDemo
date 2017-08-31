package com.epiano.commutil;

public class CLinearQueue {

//	static int mAudioBufCapacity = 20 * 16000;	// 20秒的数据
//	short mAudioBuf[] = new short [mAudioBufCapacity];
//	int mAudioBufSize = 0;	// 数据点数
//
//	int mDiscardAudioDataCount = 0;	// 因数据处理不过来丢弃的数据数量

	static int MaxDataNumInBuf = 20 * 16000;	// 20秒

	//	short AudioDataBuf_channel0[] = new short[MaxDataNumInBuf];
//	short AudioDataBuf_channel1[] = new short[MaxDataNumInBuf];
//	short AudioDataBuf[] = AudioDataBuf_channel0;
	short AudioDataBuf[] = new short[MaxDataNumInBuf];

	int mCurBufChannel = 0;
	int mLastKeyConfirmed = 1;			// 上一key确认标记, 有些key的识别经历不确切到确切的过程
	int AudioDataBuf_CurPos = 0;		// 待插入新数据位置, 行将于缓冲区中的数据个数
	int AudioDataBuf_CurProcessPos = 0;	// 已分析完数据结束位置
	int mDiscardDataNum = 0;

	static int powern = 250; //500;
	static int powerlineMaxPoints = (MaxDataNumInBuf + powern - 1) / powern;
	int powerline_CurPos = 0;	// powern *
	int powerline[] = new int[powerlineMaxPoints]; //  / 500
	int powerline_DrawedPos = 0;

//	synchronized int Reset()
//	{
//		mAudioBufSize = 0;
//
//		return 1;
//	}
//
//	int GetBufSize()
//	{
//		return mAudioBufSize;
//	}
//
//	//
//	synchronized int FeedAudioData(short AudioData[], int DataNum)
//	{
//		int Data;
//		if (DataNum > mAudioBufCapacity)
//		{
//			return 0;
//		}
//
//		// lock
//
//		if (mAudioBufSize + DataNum > mAudioBufCapacity)
//		{
//			// 缓冲满
//
//			if (true)
//			{
//				// 丢弃新数据, 直到老数据被消费, 腾出了空间
//				return 0;
//			}
//			else
//			{
//				// 头移动, 丢弃一部分数据
//				int datacut = mAudioBufSize + DataNum - mAudioBufCapacity;
//				mDiscardAudioDataCount += datacut;
//				mAudioBufHdr += datacut;
//				mAudioBufSize -= datacut;
//				if (mAudioBufSize < 0)
//				{
//					return 0;
//				}
//			}
//		}
//
//		// cach data
//		for(int i = 0; i < DataNum; i++)
//		{
//			mAudioBuf[mAudioBufTail] = AudioData[i];
//			mAudioBufTail++;
//			mAudioBufTail %= mAudioBufCapacity;
//		}
//		mAudioBufSize += DataNum;
//
//		//complexs[i] = new Complex(Data);
//
//		// unlock
//
//		return 1;
//	}
//
//	synchronized int FetchAudioData(short AudioData[], int DataNum)
//	{
//		int r = 0;
//		r = PeekAudioData(AudioData, DataNum);
//		if (r == 0)
//		{
//			return 0;
//		}
//
//		mAudioBufHdr += DataNum;
//		mAudioBufHdr %= mAudioBufCapacity;
//
//		mAudioBufSize -= DataNum;
//
//		return 1;
//	}
//
//	// 窥探audio数据
//	synchronized int PeekAudioData(short AudioData[], int DataNum)
//	{
//		if (mAudioBufSize < DataNum)
//		{
//			return 0;
//		}
//
//		// lock
//
////		if (mAudioBufSize + DataNum > mAudioBufCapacity)
////		{
////			// 缓冲满
////
////			// 头移动, 丢弃一部分数据
////			mAudioBufHdr += mAudioBufSize + DataNum - mAudioBufCapacity;
////			mAudioBufSize -= mAudioBufSize + DataNum - mAudioBufCapacity;
////
////			if (mAudioBufSize < 0)
////			{
////				return 0;
////			}
////		}
//
//		// save data
//		for(int i = 0; i < DataNum; i++)
//		{
//			AudioData[i] = mAudioBuf[(mAudioBufHdr + i) % mAudioBufCapacity];
//			//mAudioBufHdr++;
//		}
//		//mAudioBufSize -= DataNum;
//
//		// unlock
//
//		return 1;
//	}
//
//	// 删除头部数据
//	synchronized int DiscardFrontAudioData(int DataNum)
//	{
//		
//		if (DataNum > mAudioBufSize)
//		{
//			return 0;
//		}
//		
//		if (DataNum == mAudioBufSize)
//		{
//			Reset();
//			return 1;
//		}
//		
//		mAudioBufHdr += DataNum;
//		mAudioBufHdr %= mAudioBufCapacity;
//		
//		mAudioBufSize -= DataNum;
//
//		return 1;
//	}

}




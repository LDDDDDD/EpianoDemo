package com.epiano.eLearning;


// 处理来自服务器的消息
public class CUdpPacketFromServer extends CUdpPacket {

	private static String TAG = "Video";



	// 解码一个udp包
	// Stats.
	public int mLastSN = -1;
	public long mLostPkNum = 0;
	public int mLostPkNumInPeriod = 0;
	// int mLostPkRatio = 0;
	// COne264Frm
	int mAckFrmId = -1;
	public long mBadCrcPkNum = 0;
	public long mSorSendByte = 0; // 源端发送总字节数
	public long mSorSendPks = 0; // 源端发送总包数
	public long mSorSendPksSNRewindTime = 0; // 源端SN回滚资数
	public long mRcvByte = 0; // 收端接收总字节数
	public long mRcvPks = 0; // 收端接收总包数
	public int mCurJitter = 0;

	CMsgDef msgdef;

	public CUdpPacketFromServer() {
		msgdef = new CMsgDef();
	}

//	// no use
//	public int ProccessUdpPkt(byte[] Udpdata, int udplen)
//	{
//		int i = 0;
//
//		short mCRC16;
//
//		short mCRC16Calc;
//		int mSN;
//		int mMsgType;
//		long SorSendByte;
//
//		// 计算包的crc值
//		mCRC16Calc = GetCRC16(Udpdata, 2, udplen - 2);
//		// byte xx[] = new byte[2];
//		// setushort(xx, 0, mCRC16Calc);
//
//		long tick = System.currentTimeMillis();
//		// int tickcut0 = (int)((short)tick & 0xFFFF);
//		// int tickcut = (int)(tick & 0xFFFF);
////		long tickcut = tick & 0xFFFFFFFF;
//
//		i = 0;
//
//		int Tag = 0;
//		int L = 0; // 含 T L V总长
//
//		// 取发端crc
//		mCRC16 = (short) getushort(Udpdata, i);
//		i += 2;
//
//		// int ts = getushort(Udpdata, i); // & 0xFFFF;
//		int ts = getint(Udpdata, i); // & 0xFFFF;
//		i += 4;
//
//		// 反馈: 本端B向A
//		// TTL测量, 对端A发起TTL测量, 本端B记录A端时刻和本端收到消息的时刻
//		// if (TTL_Sor == -1)
//		// {
//		// TTL_Sor = ts;
//		// TTL_Local = tickcut;
//		// }
//
//		mSN = getushort(Udpdata, i); // & 0xFFFF;
//		i += 2;
//
//		mMsgType = getushort(Udpdata, i); // & 0xFFFF;
//		i += 2;
//
//		// send report
//		SorSendByte = getint(Udpdata, i); // & 0xFFFF;
//		SorSendByte *= 10;
//		i += 4;
//
//		if (mCRC16Calc != mCRC16) {
//			mBadCrcPkNum++;
//			// mStat.pushval(NETSTAT.NETSTAT_RcvBadCrcPks, mBadCrcPkNum);
//
//			Log.i(TAG, "rcv udp pks, crc check fail!, " + ", mCRC16Calc:"
//					+ mCRC16Calc + ", mCRC16:" + mCRC16 + ", mSN:" + mSN
//			// + ", m264FrmId:" + m264FrmId
//			// + ", m264PkIdInGrp:" + m264PkIdInGrp
//			// // + ", mFecIdInGrp:" + mFecIdInGrp
//			// + ", OrgPkNum_N:" + OrgPkNum_N
//			// + ", FecPkNum_n:" + FecPkNum_n
//			);
//
//			return 0;
//		}
//
//		mRcvPks++;
//
//		// // jitter更新
//		// mCurJitter = jitter.GetJitter(ts, tickcut);
//		// // curve output
//		// //String s1 = String.valueOf((int)mCurJitter);
//		// if (IntTestSwitch) IntTestOut("Jitter",
//		// String.valueOf((int)mCurJitter)); // CURVE_TYPE_NUMBER,
//
//		// 源端发送总字节数
//		if (mSorSendByte < SorSendByte) {
//			mSorSendByte = SorSendByte;
//		}
//
//		// mSN回滚计数
//		if (mLastSN > mSN) {
//			if (mLastSN - mSN >= 32768) {
//				mSorSendPksSNRewindTime++;
//			}
//		}
//		// 源端发送总包数
//		mSorSendPks = mSorSendPksSNRewindTime * 65536 + mSN;
//
//		// 丢包统计
//		if (mLastSN == -1) {
//
//		} else {
//			int lostnum = 0;
//			if (mSN > mLastSN) // 忽略回滚和乱序
//			{
//				lostnum = mSN - mLastSN;
//				lostnum--;
//			}
//			// if (lostnum <= 0)
//			// {
//			// lostnum = -lostnum;
//			// }
//			// if (lostnum > 32768)
//			// {
//			// lostnum = 0xFFFF - lostnum;
//			// }
//			mLostPkNum += lostnum;
//			mLostPkNumInPeriod += lostnum;
//		}
//		mLastSN = mSN;
//
//		// 解包
//		int dataleft = udplen - i;
//
//		if (mMsgType == msgdef.MSG_TYPE_REG_ACK) //MSG_TYPE.MSG_TYPE_REG_ACK.ordinal())
//		{
//			RegAckMsgHandler(i, Udpdata, udplen);
//		}
//
//
//		// important printf, may highly encrease delay
//		// 264data: Udpdata + i;
//		// if (mSN % 10 == 0)
//		// {
//		// Log.e(TAG, "rcv udp pks"
//		// + ", mSN:" + mSN
//		// + ", SorSendByte:" + SorSendByte
//		// // + ", mFrmType(I1,P2):" + mFrmType
//		// // + ", mNalCount:" + mNalCount
//		// // + ", m264FrmId:" + m264FrmId
//		// // + ", m264PkIdInGrp:" + m264PkIdInGrp
//		// // + ", OrgPkNum_N:" + OrgPkNum_N
//		// // + ", FecPkNum_n:" + FecPkNum_n
//		// // + ", TTL_Sor:" + TTL_Sor
//		// // + ", TTL_Local:" + TTL_Local
//		// + ", rtt:" + encctl.TTL_Result
//		// + ", jt:" + mCurJitter
//		// + ", lospks:" + mLostPkNum
//		// );
//		// }
//
//		return 1;
//	}


}
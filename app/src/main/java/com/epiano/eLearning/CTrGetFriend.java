package com.epiano.eLearning;

import android.util.Log;

import com.epiano.commutil.epdeamon;

import java.io.UnsupportedEncodingException;

public class CTrGetFriend extends CTr {

	private static String TAG = "Video";

	public CTrGetFriend()
	{

	}

	// (teacher, student,date,before/after,tuples), student可通配
	public int setQuery(long useridIn, String usernameIn, String passwordIn, int SessionIdIn, CWgUtils wgutilsIn)
	{
		wgutils = wgutilsIn;

		username = usernameIn;
		password = passwordIn;
		SessionId = SessionIdIn;
		userid = useridIn;

		return 1;
	}

	@Override
	public int rcv(int msgtype, byte data[], int offset, int datelen)
	{

		if (msgtype == msgdef.MSG_TYPE_GET_FRIEND_ACK) //MSG_TYPE.MSG_TYPE_REG_ACK.ordinal())
		{
			GetFriendAckMsgHandler(offset, data, datelen);
		}

		//
		kill();

		return 1;
	}

	@Override
	public int TimeOutNotify()
	{
		if (mFinished)
		{
			return 0;
		}

		Log.e(TAG, "Reg TimeOutNotify().");

		//mepd.SendRegAckMsg(msgdef.MSG_TYPE_REG_TIMEOUT, SessionId);

		return 1;
	}

	@Override
	public int Resend(long t1)
	{
		if (killed())
		{
			return 0;
		}

		long diff = t1 - T0;
		if (diff >= resendInterval * (retrytimes+1))
		{
			retrytimes++;

			FormRegMsg();

			UdpPk.UdpSend(mDstAddress, mDstPort, mepd.GetSN(), System.currentTimeMillis()); // UdpPk.udppk, UdpPk.udppkLen);

			Log.e(TAG, "CTrGetFriend, UdpPk, Resend: " + (retrytimes - 1) + ", diff: " + diff);

			return 1;
		}

		return 0;
	}

	public int Go()
	{
		// send msg

		UdpPk.SetSocket(mSocket);

		FormRegMsg();

		//mTrans.send(mDstAddress, mDstPort, UdpPk.udppk, UdpPk.udppkLen);
		//send(UdpPk.udppk, UdpPk.udppkLen);
		//UdpPk.UdpSend(mDstAddress, mDstPort, System.currentTimeMillis()); // UdpPk.udppk, UdpPk.udppkLen);
		new DelaySendThread().start();

		return 1;
	}

	class DelaySendThread extends Thread {

		//boolean mRuning = true;

		@Override
		public void run() {

			UdpPk.UdpSend(mDstAddress, mDstPort, mepd.GetSN(), System.currentTimeMillis()); // UdpPk.udppk, UdpPk.udppkLen);

		}
	}

	final int USRIDLEN = 20;
	final int USRNAMELEN = 20;
	public class CUdpGetFriendMsg extends CUdpPacket {

		//private static String TAG = "Video";

		// 注册消息

		//public byte usrid[] = new byte[USRIDLEN];
		long usrid = -1;
		public byte usrname[] = new byte[USRNAMELEN];
		public int RegAcked = 0;
		public int SessionId = 0;
		//public int DataVer = -1;

		public CUdpGetFriendMsg() {

		}

		// 获取朋友消息
		// byte usrname[], byte pwd[], byte localIP[], int localPort, byte
		// PhoneModel[], byte MAC[], int NatType
		public int AppendGetFriendMsg2UdpPkt() {
			int offset = udppkLen;
			int i = offset;
			int l = 0;

			{
				// T
				setushort(udppk, i, 0); // (int)UDP_ITEM.UDP_ITEM_REG.ordinal());
				i += 2;

				// L
				// udppk[udppkLen + i] = lenIn;
				i += 2;

				// V

				setint(udppk, i, SessionId);
				i += 4;

				// id, 用户名, PWD, Local IP, port, phone model, MAC, NAT type,
				//AddItem(udppk, usrid, USRIDLEN, i);
				//i += USRIDLEN;
				setint(udppk, i, (int)usrid);
				i += 4;

				AddItem(udppk, usrname, USRNAMELEN, i);
				i += USRNAMELEN;
			}

			int itemLen = i - offset; // 长度含T L V

			// L
			setushort(udppk, offset + 2, itemLen);

			udppkLen += itemLen;

			return itemLen;
		}
	}

	//long RegCount = 0;
	CUdpGetFriendMsg UdpPk = new CUdpGetFriendMsg();
	String localip = "0.0.0.0"; // new String(); // "127.0.0.1"; //getWifiIPAddress();
	//	int natret = 0;
//	String MAC;
	String username;
	long userid = -1;
	String password;
	int SessionId;
	int LoginNotified; // 是否通知过Login.java结果成功
	CWgUtils wgutils = null;
	CMsgDef msgdef = new CMsgDef();
	private int FormRegMsg()
	{
		//RegCount++;

		if (UdpPk == null)
		{
			UdpPk = new CUdpGetFriendMsg();

			//continue;
		}

		String localipt = wgutils.getWifiIPAddress();

		if (localipt.equals(localip) == false)
		{
			localip = localipt;
		}

		// send reg msg

		UdpPk.FromUdpPkt(msgdef.MSG_TYPE_GET_FRIEND, uniqueId.getBytes());

//		String DeviceMode = android.os.Build.MODEL;
//		String DeviceSerial = android.os.Build.SERIAL;

		// id, usrname, pwd, localIP, localPort, PhoneModel, MAC, NatType
		UdpPk.usrid = userid; //"1".getBytes();
		UdpPk.usrname = username.getBytes();
		UdpPk.SessionId = SessionId;

		//UdpPk.AppendUUId2UdpPkt(uniqueId);

		UdpPk.AppendGetFriendMsg2UdpPkt();

		//int port = 50000;
		//UdpPk.UdpSend(ServerAddress, serverPort, Tick); //, udppk, udppkLen);

		return 1;
	}

	String Byte2String(byte b[])
	{
		String s = "";

		int len = 0;
		for(len = 0; len < 10000; len++)
		{
			if (b[len] == 0)
			{
				break;
			}
		}

		byte bb[] = new byte[len];
		System.arraycopy(b, 0, bb, 0, len);

		try {
			s = new String(bb, "GB2312");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return s;
	}

	epdeamon mepd;
	public void Setepdeamon(epdeamon epd)
	{
		mepd = epd;
	}
	public int GetFriendAckMsgHandler(int i, byte Udpdata[], int udplen) //, byte PeerPubIp[], int PeerPubPort) //, struct sockaddr_in * from)
	{
		int dataleft = udplen - i;
		int Tag = 0;
		int L = 0; // 含 T L V总长
		int ii = 0;

		//printf("\nRegMsgHandler() called.");

		int RegRet = -1;

		while(dataleft > 4)
		{
			Tag = UdpPk.getushort(Udpdata, i);
			i += 2;

			L = UdpPk.getushort(Udpdata, i);
			i += 2;
			if (L <= 0 || L > dataleft)
			{
				//Log.i(TAG, "rcv udp pks, bad L: " + L + ", Tag: " + Tag);
				//printf("\nrcv udp pks, bad L: %d, Tag:%d.", L, Tag);
				break;
			}

			// V

			ii = i;

			if (Tag == 0)
			{
				//GetItemBA(Udpdata, i, L, tick);

				//char * d = (char *)Udpdata;
				//byte d[] = Udpdata;
				//RegRet = getushort(Udpdata, i);

				int userid = UdpPk.getint(Udpdata, ii);
				ii += 4;

				byte usernametmp[] = new byte[USRNAMELEN];
				UdpPk.getBytes(Udpdata, ii, usernametmp, USRNAMELEN);
				ii += USRNAMELEN;

				String UserName = "";
//				try {
//					UserName = new String(usernametmp, "GB2312");
//				} catch (UnsupportedEncodingException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
				UserName = Byte2String(usernametmp);

				Log.e(TAG, "id: " + userid + ", UserName: " + UserName);

			}
			else
			{
				//printf("\nrcv udp pks, bad T: %d.", Tag);
				break;
			}

			i += L - 4;	// T L V

			dataleft -= L;	// T L V
		}

		return RegRet;
	}
}
package com.epiano.eLearning;

import android.util.Log;

import com.epiano.commutil.epdeamon;


public class CTrReg extends CTr{

	private static String TAG = "Video";

	public CTrReg()
	{

	}

	// (teacher, student,date,before/after,tuples), student可通配
	public int setQuery(String usernameIn, String passwordIn, int SessionIdIn, CWgUtils wgutilsIn)
	{
		wgutils = wgutilsIn;

		username = usernameIn;
		password = passwordIn;
		SessionId = SessionIdIn;

		return 1;
	}

	@Override
	public int rcv(int msgtype, byte data[], int offset, int datelen)
	{
		//mFinished = true;

		if (msgtype == msgdef.MSG_TYPE_REG_ACK) //MSG_TYPE.MSG_TYPE_REG_ACK.ordinal())
		{
			RegAckMsgHandler(offset, data, datelen);
		}

		return 1;
	}

	boolean notified = false;
	@Override
	public int TimeOutNotify()
	{
		if (notified)
		{
			return 0;
		}

		notified = true;

		Log.e(TAG, "Reg TimeOutNotify().");

		mepd.SendRegAckMsg(msgdef.MSG_TYPE_REG_TIMEOUT, SessionId);

		return 1;
	}

	@Override
	public int Resend(long t1)
	{
		if (t1 - T0 >= resendInterval * (retrytimes+1))
		{
			retrytimes++;

			FormRegMsg();

			RegPk.UdpSend(mDstAddress, mDstPort, mepd.GetSN(), System.currentTimeMillis()); // RegPk.udppk, RegPk.udppkLen);

			Log.e(TAG, "RegPk, Resend: " + (retrytimes - 1));

			return 1;
		}

		return 0;
	}

	public int Go()
	{
		// send msg

		RegPk.SetSocket(mSocket);

		FormRegMsg();

		//mTrans.send(mDstAddress, mDstPort, RegPk.udppk, RegPk.udppkLen);
		//send(RegPk.udppk, RegPk.udppkLen);
		//RegPk.UdpSend(mDstAddress, mDstPort, System.currentTimeMillis()); // RegPk.udppk, RegPk.udppkLen);
		new DelaySendThread().start();

		return 1;
	}

	class DelaySendThread extends Thread {

		//boolean mRuning = true;

		@Override
		public void run() {

			RegPk.UdpSend(mDstAddress, mDstPort, mepd.GetSN(), System.currentTimeMillis()); // RegPk.udppk, RegPk.udppkLen);

		}
	}

	long RegCount = 0;
	CUdpRegMsg RegPk = new CUdpRegMsg();
	String localip = "0.0.0.0"; // new String(); // "127.0.0.1"; //getWifiIPAddress();
	int natret = 0;
	String MAC;
	String username;
	public long userid = -1;
	String password;
	int SessionId;
	int keepalive = 0;	// 0:注册, 1:已经完成了注册，keepalive
	//int UserId = -1;
	int LoginNotified; // 是否通知过Login.java结果成功
	CWgUtils wgutils = null;
	CMsgDef msgdef = new CMsgDef();
	private int FormRegMsg()
	{
		RegCount++;

		if (RegPk == null)
		{
			RegPk = new CUdpRegMsg();

			//continue;
		}

		if (MAC == null)
		{
			MAC = wgutils.getMac();

			//continue;
		}

//		String localipt = wgutils.getWifiIPAddress();
//		if (localipt.equals(localip) == false)
//		{
//			localip = localipt;
//
//			natret = mepd.NATDetect();
//			Log.e(TAG, "NATDetect: " + natret);
//		}
		natret = mepd.natret;

		// send reg msg
//		try {
//			ServerAddress = InetAddress.getByName(serverIP);
//		} catch (UnknownHostException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

		RegPk.FromUdpPkt(msgdef.MSG_TYPE_REG, uniqueId.getBytes());

//		String x = localip;
//		byte xx[] = localip.getBytes();

		String DeviceMode = android.os.Build.MODEL;
//		TelephonyManager.getDeviceId();
		String DeviceSerial = android.os.Build.SERIAL;

		// id, usrname, pwd, localIP, localPort, PhoneModel, MAC, NatType
		RegPk.usrid = "1".getBytes();
		RegPk.usrname = username.getBytes();
		RegPk.pwd = password.getBytes();
		RegPk.localIP = localip.getBytes();
		RegPk.localPort = 50000;
		RegPk.PhoneModel = DeviceMode.getBytes();
		if (MAC != null)
		{
			RegPk.MAC = MAC.getBytes();
		}
		else
		{
			RegPk.MAC = "0:0:0:0:0:0".getBytes();
		}
		RegPk.NatType = mepd.natret;
		RegPk.RegAcked = 0;
		RegPk.SessionId = SessionId;

		RegPk.keepalive = keepalive;

		//RegPk.AppendUUId2UdpPkt(uniqueId);

		RegPk.AppendRegMsg2UdpPkt();

		//int port = 50000;
		//RegPk.UdpSend(ServerAddress, serverPort, Tick); //, udppk, udppkLen);

		return 1;
	}

	epdeamon mepd;
	public void Setepdeamon(epdeamon epd)
	{
		mepd = epd;
	}
	public int RegAckMsgHandler(int i, byte Udpdata[], int udplen) //, byte PeerPubIp[], int PeerPubPort) //, struct sockaddr_in * from)
	{
		int dataleft = udplen - i;
		int Tag = 0;
		int L = 0; // 含 T L V总长
		int ii = 0;

		//printf("\nRegMsgHandler() called.");

		int RegRet = -1;

		while(dataleft > 4)
		{
			Tag = RegPk.getushort(Udpdata, i);
			i += 2;

			L = RegPk.getushort(Udpdata, i);
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
			}
			else
			{
				//printf("\nrcv udp pks, bad T: %d.", Tag);
				break;
			}

			{
				//char * d = (char *)Udpdata;
				//byte d[] = Udpdata;
				//RegRet = getushort(Udpdata, i);

				int SessionId = RegPk.getint(Udpdata, ii);
				ii += 4;

				RegRet = Udpdata[ii];
				ii += 1;

				userid = RegPk.getint(Udpdata, ii);
				ii += 4;

				// 0: success, 1: unreged user, 2: bad pwd
				if (RegRet == 0)
				{
					//
					mepd.SendRegAckMsg(msgdef.MSG_TYPE_REG_ACK, SessionId);

					// 周期注册
					if (keepalive == 0)
					{
//				    	CTrReg TrReg = new CTrReg();
//				    	//TrReg.setT0(Tick);
//				    	TrReg.setTimeout(2000);
//				    	TrReg.setResendInterval(200);
//				    	//TrReg.setTransprot(mTrans);
//				    	TrReg.setQuery(username, password, SessionId, wgutils);
//				    	TrReg.setDstAddr(ServerAddress, serverPort, socket);
//				    	mTrSet.addTr(TrReg.uniqueId, TrReg);
//				    	TrReg.Setepdeamon(mEpdeamon);
//				    	TrReg.Go();

						setTimeout(0x6FFFFFFF);
						setResendInterval(10000);
					}

					// 成功注册，后续“注册”消息实际上是保活消息
					keepalive = 1;
				}
				else if (RegRet == 1)
				{
					// 
					mepd.SendRegAckMsg(msgdef.MSG_TYPE_REG_ACK_UNREG, SessionId);
				}
				else if (RegRet == 2)
				{
					// 
					mepd.SendRegAckMsg(msgdef.MSG_TYPE_REG_ACK_WRONG_PWD, SessionId);
				}

			}


			i += L - 4;	// T L V

			dataleft -= L;	// T L V
		}

		return RegRet;
	}
}
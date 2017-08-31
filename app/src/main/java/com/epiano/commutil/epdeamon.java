package com.epiano.commutil;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

import com.epiano.eLearning.CMsgDef;
import com.epiano.eLearning.CTrGetFriend;
import com.epiano.eLearning.CTrReg;
import com.epiano.eLearning.CTrSet;
import com.epiano.eLearning.CTransport;
import com.epiano.eLearning.CUdpPacket;
import com.epiano.eLearning.CUdpPacketFromServer;
import com.epiano.eLearning.CUdpRegMsg;
import com.epiano.eLearning.CWgUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;
//import android.widget.EditText;


@SuppressWarnings("JniMissingFunction")
public class epdeamon extends Service {

	public static native int NATDetect();

	CMsgDef msgdef;

	private MyReceiver receiver=null;

	private static String TAG = "Video";
	String localip = "0.0.0.0"; // new String(); // "127.0.0.1"; //getWifiIPAddress();
	public int natret = 0;
	String MAC;
	String username;
	String password;
	int SessionId;
	int LoginNotified; // 是否通知过Login.java结果成功

	CWgUtils wgutils;

	DatagramSocket socket = null;
	int UdpRcvPort = 50010; // cfg

	CTrSet mTrSet;

	CTrReg TrReg = new CTrReg();

	int mSN = 0;
	public synchronized int GetSN()
	{
		return mSN++;
	}

	epdeamon mEpdeamon;
	CTrGetFriend TrGetFriend; // = new CTrGetFriend();

	//XmlPullParser parser = Xml.newPullParser();

	//	final Handler mHandler = new Handler() {
//		public void handleMessage(Message msg) {
//
//			if (msg.what == MSG_TYPE.MSG_TYPE_REG.ordinal())
//			{
//				Log.e(TAG, "MSG_TYPE.MSG_TYPE_REG_ACK rcv");
//			}
//
//			super.handleMessage(msg);
//		}
//    };
	// step 1
	class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub

//			if (msg.what == msgdef.MSG_TYPE_REG)
//			{
//				Log.e(TAG, "MSG_TYPE.MSG_TYPE_REG_ACK rcv");
//				new SigSendThread().start();
//			}
//			else
			{
				super.handleMessage(msg);
			}
		}
	}
	// step 2
	Messenger messenger = new Messenger(new IncomingHandler());
	// step 3
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return messenger.getBinder();
	}

	// 发送UDP消息处理
	String serverIP = "121.42.153.237";	// cfg
	InetAddress ServerAddress;
	int serverPort = 8800;			// cfg
	CUdpRegMsg RegPk; // = new CUdpRegMsg();
	//CUdpPacket UdpPkUtil = new CUdpPacket();
	CUdpPacketFromServer ServerUdpPk; // = new CUdpPacketFromServer();
	CTransport mTrans;

	//SigSendThread RegThread;

	@Override
	public void onCreate() {
		super.onCreate();
		Log.e(TAG, "onCreate() executed");


		System.loadLibrary("stunclient");

		mEpdeamon = this;

		msgdef = new CMsgDef();

		wgutils = new CWgUtils(getBaseContext());

		mTrSet = new CTrSet();

		TrGetFriend = new CTrGetFriend();

		// socket init
		try {
			socket = new DatagramSocket(UdpRcvPort);  //创建套接字
		} catch (IOException e) {
			e.printStackTrace();
		}
		RegPk = new CUdpRegMsg();
		RegPk.SetSocket(socket);
		ServerUdpPk = new CUdpPacketFromServer();
		ServerUdpPk.SetSocket(socket);
		//mTrans = new CTransport();
		//mTrans.SetSocket(ServerUdpPk);

		try {
			ServerAddress = InetAddress.getByName(serverIP);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		localip = wgutils.getWifiIPAddress();
		MAC = wgutils.getMac();

		{
			//注册广播接收器
			receiver = new MyReceiver();
			IntentFilter filter=new IntentFilter();
			filter.addAction(".RegReq");
			filter.addAction(".CheckService");
			epdeamon.this.registerReceiver(receiver,filter);
		}
	}

	CCaller caller;

	//boolean mServiceStarted = false;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		//Log.e(TAG, "onStartCommand() executed, mServiceStarted: " + mServiceStarted);
		Log.e(TAG, "onStartCommand() executed");

		{
//    		// socket init
//    		try {
//    			socket = new DatagramSocket(UdpRcvPort);  //创建套接字
//            } catch (IOException e) {
//                e.printStackTrace();
//            }

			// NAT detect
//    		natret = NATDetect();
//    		Log.e(TAG, "NATDetect: " + natret);

//    		RegPk = new CUdpRegMsg();
//    		RegPk.SetSocket(socket);
//    	    ServerUdpPk = new CUdpPacketFromServer();
//    	    ServerUdpPk.SetSocket(socket);
//    	    ServerUdpPk.Setepdeamon(this);

		}

		//Intent intent=getIntent();
//		username = intent.getStringExtra("username");
//		password = intent.getStringExtra("password");
//		SessionId = intent.getIntExtra("SessionId", (int)Math.random());

		// UdpRcvPort 50010
		new SigRcvThread().start();

		// 杂项任务处理线程，比如nat检测
		new MissiThread().start();

		// test
		caller = new CCaller();
		caller.SendInvite("junjun");

		return super.onStartCommand(intent, flags, startId);
	}


	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.e(TAG, "onDestroy() executed");
	}

	// crash
//    public void SendRegAckMsg(int RegAck)
//    {
//    	Handler target = new Login().new ActivityHandler();
//    	Messenger activityMessenger = new Messenger(target);
//        try {
//            activityMessenger.send(Message.obtain(null,RegAck,0,0)); //MSG_FROM_SERVICE_TO_ACTIVITY,0,0));
//        } catch (RemoteException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//    }
	// 向Login.java返回注册响应
	public void SendRegAckMsg(int RegAck, int SessionIdIn)
	{
		if (SessionId != SessionIdIn || SessionId == 0)
		{
			Log.e(TAG, "Reg session id mismatch: SessionId: " + SessionId + ", SessionIdIn: " + SessionIdIn);
			return;
		}

		if (RegAck == msgdef.MSG_TYPE_REG_ACK) {

			//Toast.makeText(getBaseContext(), "正在登录...", Toast.LENGTH_SHORT).show();

			if (LoginNotified == 0)
			{
				// 发送广播, to Login.java
				Intent intent = new Intent();
				intent.putExtra("RegAck", RegAck);
				intent.setAction(".RegAckHandleReq");
				sendBroadcast(intent);

				LoginNotified = 1;

				// 注册成功，请求朋友
				//if (false)
				{
					TrGetFriend = new CTrGetFriend();
					//TrGetFriend.setT0(Tick);
					TrGetFriend.setTimeout(10100);
					TrGetFriend.setResendInterval(2000);
					//TrGetFriend.setTransprot(mTrans);
					//TrGetFriend.setQuery(username, password, SessionId, wgutils);
					TrGetFriend.setQuery(TrReg.userid, username, password, SessionId, wgutils);
					TrGetFriend.setDstAddr(ServerAddress, serverPort, socket);
					mTrSet.addTr(TrGetFriend.uniqueId, TrGetFriend);
					TrGetFriend.Setepdeamon(mEpdeamon);
					TrGetFriend.Go();
				}
			}

			// NAT detect
			//natret = NATDetect();
			//Log.e(TAG, "NATDetect: " + natret);

		}
		else if (RegAck == msgdef.MSG_TYPE_REG_ACK_UNREG ||
				RegAck == msgdef.MSG_TYPE_REG_ACK_WRONG_PWD ||
				RegAck == msgdef.MSG_TYPE_REG_TIMEOUT)
		{

			//Toast.makeText(getBaseContext(), "密码错误，再试试吧.", Toast.LENGTH_SHORT).show();

			// 变更会话号，后续再收到注册反馈消息，在上面丢弃
			SessionId = 0;

			// 发送广播, to Login.java
			Intent intent = new Intent();
			intent.putExtra("RegAck", RegAck);
			intent.setAction(".RegAckHandleReq");
			sendBroadcast(intent);

//    		// 停止发送注册消息
//    		if (RegThread != null)
//    		{
//    			RegThread.mRuning = false;
//    		}
//    		RegThread = null;
		}
	}

//    @Override
//    public IBinder onBind(Intent intent) {
//    	Log.e(TAG, "onBind() executed");
//        return null;
//    }

	class MyBinder extends Binder {

		public void startDownload() {
			Log.d("TAG", "startDownload() executed");
			// 执行具体的下载任务

		}

	}

	// 杂项任务处理
	boolean MissiThreadRuning = false;
	class MissiThread extends Thread {

		boolean mRuning = true;

		long counter = 0;

		//DatagramSocket socket = null;

		public MissiThread()
		{

		}

		@Override
		public void run() {

			if (MissiThreadRuning)
			{
				return;
			}
			MissiThreadRuning = true;

			while (mRuning) {

				// NAT detect

				//natret = NATDetect();
				//Log.e(TAG, "idx: " + counter + ", NATDetect: " + natret);

				counter++;

				try {
					Thread.sleep(10000); // 10);
				} catch (InterruptedException e) {

					e.printStackTrace();
				}
			}

			MissiThreadRuning = false;
		}

	}

	// 信令处理
	// 接收信令，收到会话请求时打开通信窗口/Activity
	class SigRcvThread extends Thread {

		boolean mRuning = true;

		InetAddress address;
		byte[] udpbuf = new byte[2048];
		byte[] opusbuf = new byte[1024];
		DatagramPacket packet = null;
		long counter = 0;

		//DatagramSocket socket = null;

		public SigRcvThread()
		{
//    		// socket init
//    		try {
//    			socket = new DatagramSocket(UdpRcvPort);  //创建套接字
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
		}

		@Override
		public void run() {

			while (mRuning) {

				// 接收socket
				packet = new DatagramPacket(udpbuf, 2048); // udpbuf.length);
				if (packet == null || socket == null)
				{
					Log.e(TAG, "packet null or socket error. counter:" + counter
							+ ", packet: " + packet
							+ ", socket: " + socket);

					try {
						Thread.sleep(10); // 10);
					} catch (InterruptedException e) {

						e.printStackTrace();
					}

					continue;
					//return;
				}

				try {
					socket.receive(packet);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				int datalen = packet.getLength();
				byte data[] = packet.getData();

				/////////////////////////////////////////////////////
				//
				//ServerUdpPk.ProccessUdpPkt(data, datalen);
				try {
					ProccessUdpPkt(data, datalen);
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

			//OpenAVCom();
		}

		final int UUIDLEN = 36;
		byte uuid[] = new byte[UUIDLEN];
		String uuidS;
		public int ProccessUdpPkt(byte[] Udpdata, int udplen) throws UnsupportedEncodingException {
			int i = 0;

			short mCRC16;

			short mCRC16Calc;
			int SN;
			int mMsgType;
			long SorSendByte;

			// 计算包的crc值
			mCRC16Calc = ServerUdpPk.GetCRC16(Udpdata, 2, udplen - 2);
			// byte xx[] = new byte[2];
			// setushort(xx, 0, mCRC16Calc);

			//long tick = System.currentTimeMillis();
			// int tickcut0 = (int)((short)tick & 0xFFFF);
			// int tickcut = (int)(tick & 0xFFFF);
			//long tickcut = tick & 0xFFFFFFFF;

			i = 0;

			int Tag = 0;
			int L = 0; // 含 T L V总长

			// 取发端crc
			mCRC16 = (short) ServerUdpPk.getushort(Udpdata, i);
			i += 2;

			// 反馈: 本端B向A
			// TTL测量, 对端A发起TTL测量, 本端B记录A端时刻和本端收到消息的时刻
			// if (TTL_Sor == -1)
			// {
			// TTL_Sor = ts;
			// TTL_Local = tickcut;
			// }

			SN = ServerUdpPk.getushort(Udpdata, i); // & 0xFFFF;
			i += 2;

			if (mCRC16Calc != mCRC16) {
				ServerUdpPk.mBadCrcPkNum++;
				// mStat.pushval(NETSTAT.NETSTAT_RcvBadCrcPks, mBadCrcPkNum);

				Log.i(TAG, "rcv udp pks, crc check fail!, " + ", mCRC16Calc:"
								+ mCRC16Calc + ", mCRC16:" + mCRC16 + ", SN:" + SN
						// + ", m264FrmId:" + m264FrmId
						// + ", m264PkIdInGrp:" + m264PkIdInGrp
						// // + ", mFecIdInGrp:" + mFecIdInGrp
						// + ", OrgPkNum_N:" + OrgPkNum_N
						// + ", FecPkNum_n:" + FecPkNum_n
				);

				return 0;
			}


			// int ts = getushort(Udpdata, i); // & 0xFFFF;
			int ts = ServerUdpPk.getint(Udpdata, i); // & 0xFFFF;
			i += 4;

			mMsgType = ServerUdpPk.getushort(Udpdata, i); // & 0xFFFF;
			i += 2;

			// send report
			SorSendByte = ServerUdpPk.getint(Udpdata, i); // & 0xFFFF;
			SorSendByte *= 10;
			i += 4;

			// uuid
			ServerUdpPk.getBytes(Udpdata, i, uuid, UUIDLEN);
			//uuidS = uuid.toString();
			uuidS = new String(uuid, "GB2312");
			i += UUIDLEN;

			ServerUdpPk.mRcvPks++;

			// // jitter更新
			// mCurJitter = jitter.GetJitter(ts, tickcut);
			// // curve output
			// //String s1 = String.valueOf((int)mCurJitter);
			// if (IntTestSwitch) IntTestOut("Jitter",
			// String.valueOf((int)mCurJitter)); // CURVE_TYPE_NUMBER,

			// 源端发送总字节数
			if (ServerUdpPk.mSorSendByte < SorSendByte) {
				ServerUdpPk.mSorSendByte = SorSendByte;
			}

			// mSN回滚计数
			if (ServerUdpPk.mLastSN > SN) {
				if (ServerUdpPk.mLastSN - SN >= 32768) {
					ServerUdpPk.mSorSendPksSNRewindTime++;
				}
			}
			// 源端发送总包数
			ServerUdpPk.mSorSendPks = ServerUdpPk.mSorSendPksSNRewindTime * 65536 + SN;

			// 丢包统计
			if (ServerUdpPk.mLastSN == -1) {

			} else {
				int lostnum = 0;
				if (SN > ServerUdpPk.mLastSN) // 忽略回滚和乱序
				{
					lostnum = SN - ServerUdpPk.mLastSN;
					lostnum--;
				}
				// if (lostnum <= 0)
				// {
				// lostnum = -lostnum;
				// }
				// if (lostnum > 32768)
				// {
				// lostnum = 0xFFFF - lostnum;
				// }
				ServerUdpPk.mLostPkNum += lostnum;
				ServerUdpPk.mLostPkNumInPeriod += lostnum;
			}
			ServerUdpPk.mLastSN = SN;

			// 解包
			//int dataleft = udplen - i;

//			if (mMsgType == msgdef.MSG_TYPE_REG_ACK) //MSG_TYPE.MSG_TYPE_REG_ACK.ordinal())
//			{
//				ServerUdpPk.RegAckMsgHandler(i, Udpdata, udplen);
//			}
			//CCtr ctr = mTrSet.dispather(uuid, data, datalen)
			mTrSet.dispather(mMsgType, uuidS, Udpdata, i, udplen);

			return 1;
		}
	}


	public int OpenAVCom()
	{
		Intent intentx = new Intent();

		intentx.setClass(getBaseContext(), PianoELearning.class);

		intentx.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

//	        EditText FileNmaeEditText = (EditText)findViewById(R.id.editText1);
//	        String openfilename = FileNmaeEditText.getText().toString();
		String openfilename = "/mnt/sdcard/1.sco";
		Bundle bundle=new Bundle();
		bundle.putString("openfilename", openfilename);
		intentx.putExtras(bundle);

		getApplication().startActivity(intentx);

		return 1;
	}

	/**
	 * 获取广播数据
	 *
	 * @author jiqinlin
	 *
	 */
	public class MyReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle bundle = intent.getExtras();


			String act = intent.getAction();

			if (act.equals(".RegReq"))
			{
				int RegReqStart = bundle.getInt("RegReqStart");

				if (RegReqStart == 1)
				{
					// msg from Login.java, start to reg.

					username = intent.getStringExtra("username");
					password = intent.getStringExtra("password");
					SessionId = intent.getIntExtra("SessionId", (int)Math.random());

					LoginNotified = 0;

					Log.e(TAG, "Starting Reg.");
					// xxxx
					//				if (RegThread == null)
					//				{
					//					RegThread = new SigSendThread();
					//					RegThread.start();
					//				}

					// 用户注册
					{
						if (TrReg != null)
						{
							TrReg.kill();
						}

						{
							TrReg = new CTrReg();
							//TrReg.setT0(Tick);
							TrReg.setTimeout(2200);
							TrReg.setResendInterval(500);
							//TrReg.setTransprot(mTrans);
							TrReg.setQuery(username, password, SessionId, wgutils);
							TrReg.setDstAddr(ServerAddress, serverPort, socket);
							mTrSet.addTr(TrReg.uniqueId, TrReg);
							TrReg.Setepdeamon(mEpdeamon);
							TrReg.Go();
						}

					}
				}
				else if (RegReqStart == -1)
				{
					// from Login.java, finish reg. no use

					//				if (RegThread != null)
					//				{
					//					RegThread.mRuning = false;
					//				}
					//				RegThread = null;
				}
			}
			if (act.equals(".CheckService"))
			{
				//int alive = bundle.getInt("alive");
				//if (alive == 1)
				{
					Log.e(TAG, "service exist.");

					// 发送广播, to Login.java
					Intent intent2 = new Intent();
					intent2.putExtra("AliveAck", 1);
					intent2.setAction(".CheckServiceAck");
					sendBroadcast(intent2);
				}
			}

//			int RegAck = bundle.getInt("RegAck");
//			if (RegAck == msgdef.MSG_TYPE_REG_ACK) {
//
//				Toast.makeText(getBaseContext(), "正在登录...", Toast.LENGTH_SHORT).show();
//
//				// 记录用户名和密码
//				{
////					String username = et_name.getText().toString();
////					String password = et_pass.getText().toString();
//					configInfo.edit().putString("username", username).commit();
//					configInfo.edit().putString("password", password).commit();
//				}
//
//				intent = new Intent(Login.this, MainActivity.class);
//				startActivity(intent);
//				Login.this.finish();
//			}
//        	else if(RegAck == msgdef.MSG_TYPE_REG_ACK_UNREG) {
//
//        		stopservice();
//
//        		Toast.makeText(getBaseContext(), "还没有注册哦.", Toast.LENGTH_SHORT).show();
//        	}
//        	else if(RegAck == msgdef.MSG_TYPE_REG_ACK_WRONG_PWD) {
//
//        		stopservice();
//
//        		Toast.makeText(getBaseContext(), "密码错误，再试试吧.", Toast.LENGTH_SHORT).show();
//        	}
		}
	}

	enum CALL_STATUS
	{
		CALL_STATUS_NULL,

		CALL_STATUS_INVITE,
		CALL_STATUS_100_GOT,
		CALL_STATUS_COT_GOT,
		CALL_STATUS_RINGING,
		CALL_STATUS_ANSWERED,

		CALL_STATUS2_INVITE,
		CALL_STATUS2_COT_GOT,
		CALL_STATUS2_RINGING,
		CALL_STATUS2_ANSWER,
		CALL_STATUS2_ANSWER_ACK,

		CALL_STATUS_WAIT_100_FROM_B,
		CALL_STATUS_WAIT_COT_FROM_AB,
		CALL_STATUS_ANSWER_ACK_FROM_A,
	}
	private static Timer mTimer;
	private static TimerTask mTimerTask;
	private static Handler mHandler;
	protected static final int TIMEOUT_MSG = 1;
	class CCaller
	{
		/*
		Caller Status:
		null:
		invite_send/wait_100:	呼叫已发出，等100消息, 带会话号,    (服务器立刻返回100)
								wait: 100 from 服务器, 200ms, retry 10次;
		:m100_got/wait_ringing:  100消息已得到，等振铃，       		获得服务器的token, B的备选SDP(公网地址，私网地址, relay地址), 发起COT, 接收对应COT
		COT_got/reportCOT_OK:	收到对端COT，向服务器报告.   		(服务器收到两端的报告后，向AB两端发送ringing)
		:ringing: 				振铃消息已收到(选定对端接收Port)
		:answer:				应答消息收到，发送媒体

		Callee Status:
		null:
		invite_got/send_100:	呼叫已收到                    		获得服务器的token, A的备选SDP(公网地址，私网地址), 发起COT, 接收对应COT
		COT_got/reportCOT_OK:	收到对端COT，向服务器报告.   		(服务器收到两端的报告后，向AB两端发送ringing)
		ringing: 				振铃消息已收到(选定对端接收Port)
		answer:					应答消息收到，发送媒体

		服务器:
		on收到A的invite接受到:  立刻返回100，[token, B的备选SDP(公网地址，私网地址, relay地址)]
								发invite给B，[token, A的备选SDP(公网地址，私网地址, relay地址)]
								wait: 100 from B, 200, retry 5次;
		on收到A的reportCOT_OK:
		on收到B的reportCOT_OK: 两边收齐，向两个发送ringing, 每100ms重发，直到收到B的answer(停止向B发消息)， 直接收到A的answerAck(停止向A发消息)
		*/


		public CCaller()
		{
			// 当发送invite后，启动定时器
			mTimer = new Timer();
			mTimerTask = new TimerTask() {
				@Override
				public void run() {
					Message message = new Message();
					message.what = TIMEOUT_MSG;
					mHandler.sendMessage(message);
					// mHandler.sendEmptyMessage(UPDATE_TEXT);

				}
			};
			mHandler = new Handler() {
				public void handleMessage(Message msg) {
					switch (msg.what) {
						case TIMEOUT_MSG:
							OnTimeout();
							break;
						default:
							break;
					}

					super.handleMessage(msg);
				}
			};
			mTimer.schedule(mTimerTask, 100, 200); // delay, period
		}

		public void stop()
		{
			mTimer.cancel();
		}

		CALL_STATUS mStatus = CALL_STATUS.CALL_STATUS_NULL;

		// 迁状态
		int EnterStatus(CALL_STATUS status)
		{
			//if (mStatus != CALL_STATUS.CALL_STATUS_NULL)
			{
				mRetrytimes = 0;
			}

			mStatus = status;

			return 1;
		}

		//byte INVITE[] = new byte[2048];
		CUdpPacket InviteMsg = new CUdpPacket();
		int FormInvite()
		{
			String uuid = java.util.UUID.randomUUID().toString();

			return 1;
		}

		byte COT[] = new byte[2048];
		int FormCOT()
		{
			return 1;
		}

		byte reportCOTOK[] = new byte[2048];
		int FormRepCOTOK()
		{
			return 1;
		}

		int send(byte msg[])
		{
			return 1;
		}

		int kill()
		{
			return 1;
		}

		// 周期任务
		int Interval = 200;
		int mMaxRetrytimes = 10;
		int mRetrytimes = 0;
		int OnTimeout()
		{
			Interval = 200;

			if (mStatus == CALL_STATUS.CALL_STATUS_NULL)
			{
				return 1;
			}

			if (mRetrytimes >= mMaxRetrytimes)
			{
				mStatus = CALL_STATUS.CALL_STATUS_NULL;

				kill();

				return 1;
			}

			if (mStatus == CALL_STATUS.CALL_STATUS_INVITE)
			{
				//send(INVITE);
				Log.e(TAG, "INVITE.");
			}
			else if (mStatus == CALL_STATUS.CALL_STATUS_100_GOT)
			{
				Interval = 100;
				send(COT);
				Log.e(TAG, "COT.");
			}
			else if (mStatus == CALL_STATUS.CALL_STATUS_COT_GOT)
			{
				send(COT);
				send(reportCOTOK);
				Log.e(TAG, "COT.");
			}
//			else if (mStatus == CALL_STATUS.CALL_STATUS_RINGING)
//			{
//				send(COT);
//			}
//			else if (mStatus == CALL_STATUS.CALL_STATUS_ANSWERED)
//			{
//				// open media
//			}

			mRetrytimes++;

			return 1;
		}

		/////////////////////////////////////


		// 发送INVITE系列处理
		public int SendInvite(String caller)
		{
			EnterStatus(CALL_STATUS.CALL_STATUS_INVITE);

			// start timer

			//wait_100(200, 10);

			FormInvite();

			//send(INVITE);

			Log.e(TAG, "INVITE.");

			return 1;
		}

		public int SendRepCOTOK()
		{
			//EnterStatus(CALL_STATUS.CALL_STATUS_100_GOT);

			FormRepCOTOK();

			send(reportCOTOK);

			Log.e(TAG, "reportCOTOK.");

			return 1;
		}

		public int SendCOT()
		{
			//EnterStatus(CALL_STATUS.CALL_STATUS_100_GOT);

			FormCOT();

			send(COT);

			Log.e(TAG, "COT.");

			return 1;
		}

		/////////////////////////

		int On100(int token, byte BPubIp[], int BPubPort, byte BPrivteIp[], int BPrivatePort, byte BRelayIp[], int BRelayPort)
		{
			EnterStatus(CALL_STATUS.CALL_STATUS_100_GOT);

			send(COT);

			return 1;
		}

		int OnCOT()
		{
			EnterStatus(CALL_STATUS.CALL_STATUS_COT_GOT);

			SendRepCOTOK();

			return 1;
		}

		int OnRinging()
		{
			// broadcast to ring

			return 1;
		}

		int OnAnswer()
		{
			EnterStatus(CALL_STATUS.CALL_STATUS_ANSWERED);

			return 1;
		}

		int OnQuit()
		{
			return 1;
		}

	}

}

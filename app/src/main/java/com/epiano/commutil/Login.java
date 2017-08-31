package com.epiano.commutil;

//import java.io.IOException;
//import java.net.DatagramPacket;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.epiano.eLearning.CMsgDef;
import com.epiano.eLearning.CUdpRegMsg;
import com.epiano.eLearning.CWgUtils;
import com.epiano.slidepage.SlidePages;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.List;

//import java.net.UnknownHostException;
//import android.content.ComponentName;
//import android.content.ServiceConnection;
//import epdeamon.MSG_TYPE;
//import epdeamon.SigRcvThread;
//import epdeamon.SigSendThread;
//import android.os.IBinder;
//import android.os.Messenger;
//import android.os.RemoteException;
//import com.bipbip.main.BaseActivity;
//import com.bipbip.main.MainActivity;
//import com.bipbip.watch.R;

/**
 *
 *
 * 注 册： ValidatePhoneNumActivity --> RegisterActivity
 *
 * 忘记密码 ForgetCodeActivity --> RepasswordActivity
 *
 * @author liubao.zeng
 *
 */
// public class Login extends Activity { // implements OnClickListener
public class Login extends Activity implements OnClickListener
		,OnLongClickListener
//		,ServiceConnection
{
	// 声明控件对象
	private EditText et_name, et_pass;
	private Button mLoginButton, mLoginError, mRegister, ONLYTEST;
	int selectIndex = 1;
	int tempSelect = selectIndex;
	boolean isReLogin = false;
	private int SERVER_FLAG = 0;
	private RelativeLayout countryselect;
	private TextView coutry_phone_sn, coutryName;//
	// private String [] coutry_phone_sn_array,coutry_name_array;
	public final static int LOGIN_ENABLE = 0x01; // 注册完毕了
	public final static int LOGIN_UNABLE = 0x02; // 注册完毕了
	public final static int PASS_ERROR = 0x03; // 注册完毕了
	public final static int NAME_ERROR = 0x04; // 注册完毕了

	CMsgDef msgdef;

	// service
//	private epdeamon.MyBinder myBinder;
//	private ServiceConnection connection = new ServiceConnection() {
//
//        @Override
//        public void onServiceDisconnected(ComponentName name) {
//        }
//
//        @Override
//        public void onServiceConnected(ComponentName name, IBinder service) {
//            myBinder = (epdeamon.MyBinder) service;
//            myBinder.startDownload();
//        }
//    };

	final Handler UiMangerHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
				case LOGIN_ENABLE:
					mLoginButton.setClickable(true);
					// mLoginButton.setText(R.string.login);
					break;
				case LOGIN_UNABLE:
					mLoginButton.setClickable(false);
					break;
				case PASS_ERROR:

					break;
				case NAME_ERROR:
					break;
			}
			super.handleMessage(msg);
		}
	};


	private SharedPreferences configInfo;

	/////////////////////////bindService(new Intent(Login.this,epdeamon.class),this, Context.BIND_AUTO_CREATE);
//	private Messenger messenger;
//    private Boolean mBound = false;
//	@Override
//    protected void onStart() {
//        // TODO Auto-generated method stub
//        super.onStart();
//        bindService(new Intent(Login.this,epdeamon.class),this, Context.BIND_AUTO_CREATE);
//    }
//    @Override
//    protected void onStop() {
//        // TODO Auto-generated method stub
//        super.onStop();
//        if(!mBound) {
//            unbindService(this);
//        }
//    }
//    @Override
//    public void onServiceConnected(ComponentName arg0, IBinder arg1) {
//        // a message is a reference to the Handler
//        // use a messenger to wrap the binder,so can we send the message to service
//        messenger = new Messenger(arg1);
//        mBound = true;
//    }
//    @Override
//    public void onServiceDisconnected(ComponentName arg0) {
//         mBound = false;
//    }
	// A handler used to receive message from service
//  public class ActivityHandler extends Handler {
//      @Override
//      public void handleMessage(Message msg) {
//          // TODO Auto-generated method stub
////          if(msg.what == MessengerService.MSG_FROM_SERVICE_TO_ACTIVITY) {
////              //Log.i(FFFF,Received Service's Message!);
////          }
//      	if(msg.what == msgdef.MSG_TYPE_REG_ACK) {
//       	   Toast.makeText(getBaseContext(), "正在登录.", Toast.LENGTH_SHORT).show();
//              //Log.i(FFFF,Received Service's Message!);
//       	}
//      	else if(msg.what == msgdef.MSG_TYPE_REG_ACK_UNREG) {
//      	   Toast.makeText(getBaseContext(), "还没有注册哦.", Toast.LENGTH_SHORT).show();
//             //Log.i(FFFF,Received Service's Message!);
//      	}
//      	else if(msg.what == msgdef.MSG_TYPE_REG_ACK_WRONG_PWD) {
//      	   Toast.makeText(getBaseContext(), "密码错误，再试试吧.", Toast.LENGTH_SHORT).show();
//             //Log.i(FFFF,Received Service's Message!);
//      	}
//      }
//  }

	private MyReceiver receiver=null;
	boolean epdeamonRuningTest = false;
	boolean epdeamonRuning = false;

	private LinearLayout mLoginLL;
	private ProgressBar mLoadingPB;
	private void initView() {
		this.mLoginLL = (LinearLayout) findViewById(R.id.bmob_login_ll);
		this.setAlphaAnimation(mLoginLL);
		this.et_name = (EditText) findViewById(R.id.username);
		this.et_pass = (EditText) findViewById(R.id.password);
		this.mLoadingPB = (ProgressBar) findViewById(R.id.bmob_login_pb);
		this.mLoginButton = (Button) findViewById(R.id.login);
		this.mLoginError = (Button) findViewById(R.id.login_error);
		this.mRegister = (Button) findViewById(R.id.register);
		this.ONLYTEST = (Button) findViewById(R.id.registfer);
		ONLYTEST.setOnClickListener(this);
		ONLYTEST.setOnLongClickListener((OnLongClickListener) this);
		mLoginButton.setOnClickListener(this);
		mLoginError.setOnClickListener(this);
		mRegister.setOnClickListener(this);
	}

	private void setAlphaAnimation(View v) {
		AlphaAnimation aa = new AlphaAnimation(0, 1);
		aa.setDuration(5000);
		v.startAnimation(aa);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		//不显示系统的标题栏
		getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN,
			WindowManager.LayoutParams.FLAG_FULLSCREEN );

		//注册广播接收器
		receiver = new MyReceiver();
		IntentFilter filter=new IntentFilter();
		filter.addAction(".RegAckHandleReq");
		filter.addAction(".CheckServiceAck");
		Login.this.registerReceiver(receiver,filter);


		epdeamonRuningTest = isServiceRunning(this.getBaseContext(), "epdeamon");
		Log.e(TAG, "epdeamonRuning: " + epdeamonRuningTest);
		CheckService();

		msgdef = new CMsgDef();
		setContentView(R.layout.activitylogin);
		initView();

		// countryselect=(RelativeLayout)
		// findViewById(R.id.countryselect_layout);
		// countryselect.setOnClickListener(this);
		// coutry_phone_sn=(TextView) findViewById(R.id.contry_sn);
		// coutryName=(TextView) findViewById(R.id.country_name);

		// coutryName.setText(coutry_name_array[selectIndex]); //默认为1
		// coutry_phone_sn.setText("+"+coutry_phone_sn_array[selectIndex]);

		configInfo = getSharedPreferences("AdvideoConfig", Context.MODE_PRIVATE);
		username = configInfo.getString("username", "huihui");
		et_name.setText(username);
		password = configInfo.getString("password", "1234");
		et_pass.setText(password);

		//wgutils = new CWgUtils(getBaseContext());
		//MAC = wgutils.getMac();


		// 启动服务 //////////////////////
		if (!epdeamonRuning){
			startservice();
		}

//		// socket init
//		try {
//			socket = new DatagramSocket();  //创建套接字
//			address = InetAddress.getByName(serverIP); // "127.0.0.1");  //服务器地址
////			} catch (UnknownHostException e) {
////	            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

			String username = et_name.getText().toString();
			String password = et_pass.getText().toString();
			configInfo.edit().putString("username", username).commit();
			configInfo.edit().putString("password", password).commit();

		//stopservice();
	}


	private Intent intent;



	private int startservice()
	{
		{
			intent = new Intent(this, epdeamon.class);
			Bundle bundle=new Bundle();

			EditText Epassword = (EditText)findViewById(R.id.password);
			password = Epassword.getText().toString();
			bundle.putString("password", password);

			EditText Eusername = (EditText)findViewById(R.id.username);
			username = Eusername.getText().toString();
			bundle.putString("username", username);

			long Tick = System.currentTimeMillis();
			int tick = (int)Tick;
			bundle.putInt("SessionId", tick);

			intent.putExtras(bundle);

			startService(intent);
		}

//		intent = new Intent(Login.this, MainActivity.class);
//		startActivity(intent);
//		Login.this.finish();

		return 1;
	}

	private int stopservice()
	{
		{
			intent = new Intent(this, epdeamon.class);
			Bundle bundle=new Bundle();

			EditText Epassword = (EditText)findViewById(R.id.password);
			password = Epassword.getText().toString();
			bundle.putString("password", password);

			EditText Eusername = (EditText)findViewById(R.id.username);
			username = Eusername.getText().toString();
			bundle.putString("username", username);

			intent.putExtras(bundle);

			stopService(intent);
		}

//		intent = new Intent(Login.this, MainActivity.class);
//		startActivity(intent);
//		Login.this.finish();

		return 1;
	}

	DatagramSocket socket = null;  //创建套接字
	InetAddress address; // = InetAddress.getByName("192.168.1.80");  //服务器地址

	String serverIP = "121.42.153.237";	// cfg
	int serverPort = 8800;			// cfg
	CUdpRegMsg RegPk = new CUdpRegMsg();



	private static String TAG = "Video";
	String localip = "0.0.0.0"; // new String(); // "127.0.0.1"; //getWifiIPAddress();
	int natret = 0;
	CWgUtils wgutils; // = new CWgUtils(getBaseContext());
	String MAC; // = wgutils.getMac();
	String username;
	String password;

//    Handler mHandler = new Handler() {
//		public void handleMessage(Message msg) {
//
//			if (msg.what == MSG_TYPE.MSG_TYPE_REG_ACK.ordinal())
//			{
//				Log.e(TAG, "MSG_TYPE.MSG_TYPE_REG_ACK rcv");
//			}
//
//
//			super.handleMessage(msg);
//		}
//    };

	/**
	 * 用来判断服务是否运行.
	 *
	 * @param
	 * @param className
	 *            判断的服务名字
	 * @return true 在运行 false 不在运行
	 */
	public static boolean isServiceRunning(Context mContext, String className)
	{
		boolean isRunning = false;
		ActivityManager activityManager = (ActivityManager) mContext
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningServiceInfo> serviceList = activityManager
				.getRunningServices(30);
		if (!(serviceList.size() > 0)) {
			return false;
		}
		String classnameinlist;
		for (int i = 0; i < serviceList.size(); i++) {
			classnameinlist = serviceList.get(i).service.getClassName();
			Log.e(TAG, "classnameinlist: " + classnameinlist);
			if (classnameinlist.equals(className) == true) {
				isRunning = true;
				break;
			}
		}
		return isRunning;
	}

	void CheckService()
	{
		Intent intent = new Intent();

		intent.setAction(".CheckService");

		intent.putExtra("alive", 1);

		sendBroadcast(intent);
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		switch (arg0.getId()) {
			case R.id.login: // 登陆
				//int r = login();
				// startActivity(MainActivity.class);

				EditText Epassword1 = (EditText)findViewById(R.id.password);
				password = Epassword1.getText().toString();
				EditText Eusername1 = (EditText)findViewById(R.id.username);
//            username = Eusername1.getText().toString();

				// 启动Activity
				if (//r > 0 &&
						( password.isEmpty() == false)
								&&( username.isEmpty() == false))
				{
					// 启动服务 //////////////////////
					//stopservice();
//				if (!epdeamonRuning)
//				{
//					startservice();
//				}

					// 发送广播
					{
						Intent intent = new Intent();

						intent.setAction(".RegReq");

						intent.putExtra("RegReqStart", 1);

//					EditText Epassword = (EditText)findViewById(R.id.password);
//			        password = Epassword.getText().toString();
						intent.putExtra("password", password);

//					EditText Eusername = (EditText)findViewById(R.id.username);
//			        username = Eusername.getText().toString();
						intent.putExtra("username", username);

						long Tick = System.currentTimeMillis();
						int tick = (int)Tick;
						intent.putExtra("SessionId", tick);

						sendBroadcast(intent);
					}

//				try {
//					messenger.send(Message.obtain(null,msgdef.MSG_TYPE_REG,0,0));
//				} catch (RemoteException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
				}
				else
				{
					Toast.makeText(this, "用户名和密码都要填写哦.", Toast.LENGTH_SHORT).show();
				}

				break;
			case R.id.login_error: // 无法登陆(忘记密码了吧)
				// Intent login_error_intent=new Intent();
				// login_error_intent.setClass(LoginActivity.this,
				// ForgetCodeActivity.class);
				// startActivity(login_error_intent);
				break;
			case R.id.register: // 注册新的用户
//				 Intent intent=new Intent();
//				 intent.setClass(LoginActivity.this,
//				 ValidatePhoneNumActivity.class);
//				 startActivity(intent);

				Toast.makeText(this, "注册中…………", Toast.LENGTH_SHORT).show();

				break;

			case R.id.registfer:
				if (SERVER_FLAG > 10) {
					Toast.makeText(this, "[内部测试--谨慎操作]", Toast.LENGTH_SHORT).show();
				}
				SERVER_FLAG++;
				break;
		}
	}


	@Override
	public boolean onLongClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
			case R.id.registfer:
				if (SERVER_FLAG > 9) {

				}
				// SERVER_FLAG++;
				break;
		}
		return true;
	}

	/**
	 * 监听Back键按下事件,方法2: 注意: 返回值表示:是否能完全处理该事件 在此处返回false,所以会继续传播该事件.
	 * 在具体项目中此处的返回值视情况而定.
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			if (isReLogin) {
				Intent mHomeIntent = new Intent(Intent.ACTION_MAIN);
				mHomeIntent.addCategory(Intent.CATEGORY_HOME);
				mHomeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
						| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
				Login.this.startActivity(mHomeIntent);
			} else {
				Login.this.finish();
			}
			return false;
		} else {
			return super.onKeyDown(keyCode, event);
		}
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

			if (act.equals(".RegAckHandleReq"))
			{

				int RegAck = bundle.getInt("RegAck");

				if (RegAck == msgdef.MSG_TYPE_REG_ACK) {

					Toast.makeText(getBaseContext(), "正在登录...", Toast.LENGTH_SHORT).show();

					// 记录用户名和密码
					{
						//					String username = et_name.getText().toString();
						//					String password = et_pass.getText().toString();
						configInfo.edit().putString("username", username).commit();
						configInfo.edit().putString("password", password).commit();
					}

					//				intent = new Intent(Login.this, MainActivity.class);
					intent = new Intent(Login.this, SlidePages.class);

					startActivity(intent);

					Login.this.finish();
				}
				else if(RegAck == msgdef.MSG_TYPE_REG_ACK_UNREG) {

					Toast.makeText(getBaseContext(), "还没有注册哦.", Toast.LENGTH_SHORT).show();
				}
				else if(RegAck == msgdef.MSG_TYPE_REG_ACK_WRONG_PWD) {

					Toast.makeText(getBaseContext(), "密码错误，再试试吧.", Toast.LENGTH_SHORT).show();
				}
				else if(RegAck == msgdef.MSG_TYPE_REG_TIMEOUT) {

					Toast.makeText(getBaseContext(), "联系不上服务器哦.", Toast.LENGTH_SHORT).show();
				}
			}
			else if (act.equals(".CheckServiceAck"))
			{
				int AliveAck = bundle.getInt("AliveAck");

				//if(AliveAck == 1)
				{
					epdeamonRuning = true;
					Log.e(TAG, "epdeamonRuning 2: " + epdeamonRuning);
				}
			}
		}
	}
}

package com.epiano.commutil;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

//import android.view.Menu;            //如使用菜单加入此三包
//import android.view.MenuInflater;
//import android.view.MenuItem;

public class BTClient2 extends Activity {

	private final static int REQUEST_CONNECT_DEVICE = 1;    //宏定义查询设备句柄

	private final static String MY_UUID = "00001101-0000-1000-8000-00805F9B34FB";   //SPP服务UUID号

	private InputStream is;    //输入流，用来接收蓝牙数据
	public OutputStream os;    //输入流，用来接收蓝牙数据
	//private TextView text0;    //提示栏解句柄
	private EditText edit0;    //发送数据输入句柄
	private TextView dis;       //接收数据显示句柄
	private ScrollView sv;      //翻页句柄
	private ScrollView svPic;      //翻页句柄
	private String smsg = "";    //显示用数据缓存
	private String fmsg = "";    //保存用数据缓存
	private LinearLayout LLViewPic;

	//private ImageView disImgView;   //绘制key的视图
	private KeyCaptureView disImgView;   //绘制key的视图

	SharedPreferences preferences;
	String LastBTMAC = "NULL";

	public String filename=""; //用来保存存储的文件名
	BluetoothDevice _device = null;     //蓝牙设备
	BluetoothSocket _socket = null;      //蓝牙通信socket
	boolean _discoveryFinished = false;
	boolean bRun = true;
	boolean bThread = false;

	boolean bThreadOut = false;
	boolean bRunOut = true;

	private BluetoothAdapter _bluetooth = BluetoothAdapter.getDefaultAdapter();    //获取本地蓝牙适配器，即蓝牙设备

	// wg add
	KeyCapture KeyCap;
	KeyCaptureView KeyCapView;
	int vw = 0;
	int vh = 0; //d.getHeight();
	ViewGroup.LayoutParams lp;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bt_main2);   //设置画面为主画面 main.xml

		//text0 = (TextView)findViewById(R.id.Text0);  //得到提示栏句柄
		edit0 = (EditText)findViewById(R.id.Edit0);   //得到输入框句柄
		sv = (ScrollView)findViewById(R.id.ScrollView01);  //得到翻页句柄
		//int w0 = sv.getWidth();
		//int h0 = sv.getHeight();
		dis = (TextView) findViewById(R.id.in);      //得到数据显示句柄
		//disImgView = (ImageView) findViewById(R.id.imageViewofKeys);      //得到数据显示句柄
		//int w = dis.getWidth();
		//int h = dis.getHeight();
		LLViewPic = (LinearLayout) findViewById(R.id.LLViewPic);  //得到翻页句柄



		WindowManager m = getWindowManager();
		Display d = m.getDefaultDisplay();  //为获取屏幕宽、高
		vw = d.getWidth();
		vh = 300; //d.getHeight();
//		lp = new ViewGroup.LayoutParams(vw, vh); //ViewGroup.LayoutParams.MATCH_PARENT); // vh);
//		ViewGroup.LayoutParams lp;
		//KeyCapView.setLayoutParams(lp); //new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
//		KeyCapView.layout(0, 0, vw, vh);
//		KeyCapView.setBackgroundColor(0x888888);
//		svPic.addView(KeyCapView);
		//lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT); // vh);
		lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, d.getHeight() / 2);

		svPic = (ScrollView)findViewById(R.id.ScrollViewPic);  //得到翻页句柄
		KeyCap = new KeyCapture(); //this.os);
		KeyCapView = new KeyCaptureView(this, KeyCap, d.getWidth(), d.getHeight());

		KeyCapView.setLayoutParams(lp);
		LLViewPic.addView(KeyCapView);
		//svPic.addView(KeyCapView);

		//disImgView.postInvalidate();
		//KeyCapView.postInvalidate();

		//如果打开本地蓝牙设备不成功，提示信息，结束程序
		if (_bluetooth == null){
			Toast.makeText(this, "无法打开手机蓝牙，请确认手机是否有蓝牙功能！", Toast.LENGTH_LONG).show();
			finish();
			return;
		}

		// 设置设备可以被搜索
		new Thread(){
			public void run(){
				if(_bluetooth.isEnabled()==false){
					_bluetooth.enable();
				}
			}
		}.start();
	}

	@Override
	protected void onResume() {
		super.onResume();
		//Log.v(TAG, "protected void onResume()");

		int w = LLViewPic.getWidth();
		int h = LLViewPic.getHeight();

		Log.i("WG", "OnResume() called.");

		//KeyCapView.setLayoutParams(lp); //new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
		//KeyCapView.layout(0, 0, vw, vh);
		//KeyCapView.setBackgroundColor(0x888888);
	}

	//发送按键响应
//	public void onSendButtonClicked(View v){
//		int i=0;
//		int n=0;
//		try{
//			OutputStream os = _socket.getOutputStream();   //蓝牙连接输出流
//			byte[] bos = edit0.getText().toString().getBytes();
//			for(i=0;i<bos.length;i++){
//				if(bos[i]==0x0a)n++;
//			}
//			byte[] bos_new = new byte[bos.length+n];
//			n=0;
//			for(i=0;i<bos.length;i++){ //手机中换行为0a,将其改为0d 0a后再发送
//				if(bos[i]==0x0a){
//					bos_new[n]=0x0d;
//					n++;
//					bos_new[n]=0x0a;
//				}else{
//					bos_new[n]=bos[i];
//				}
//				n++;
//			}
//
//			os.write(bos_new);
//		}catch(IOException e){
//		}
//	}

	/**     * @description 创建并保存一些设置信息     */
	private int saveSharedPreferences(){
		/*       * getSharedPreferences(String name,int mode)方法是在Context中定义的抽象方法，在ContextWrapper中进行了具体实现，
		* * 该方法会根据用户传递的名称和写入类型创建一个SharedPreferences对象进行返回。
		* * 在Activity中，还有一个方法可以回去SharedPreferences对象，这个方法就是SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
		* * 改方法在Activity中定义并实现，这里没有name参数是因为该方法默认将当前Activity的类名作为name属性（这里的类名并不包含包路径哦）
		* * */
		SharedPreferences sharedPreferences = getSharedPreferences("preferences", MODE_PRIVATE);             //存储数据
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString("LastBTMAC", LastBTMAC); //"蔡依林");
//		editor.putInt("age", 31);
		Boolean b = editor.commit();
		if(b){
			Log.i("通知：", "保存成功！");
			return 1;
		}else{
			Log.i("通知", "保存失败！");
			return 0;
		}
	}
	/**     * @author chenzheng_java     * @description 读取我们添加到SharedPreference对象中的数据     * @since 2011/03/05     */
	private void readSharedPreferences(){
		String result = "美女信息：/n" ;
		SharedPreferences sharedPreferences = this.getSharedPreferences("preferences", MODE_PRIVATE);
//		 result+=" 姓名 "+sharedPreferences.getString("name", "暂时没有人");
//		 result+=" 年龄"+sharedPreferences.getInt("age", -1);
//		 TextView textView = (TextView)findViewById(R.id.textView);
//		 textView.setText(result);
		LastBTMAC = sharedPreferences.getString("LastBTMAC", "NULL");
	}

	public int ConnectBT(String address)
	{
//		String address = data.getExtras()
//				.getString(BTDeviceListActivity.EXTRA_DEVICE_ADDRESS);
		// 得到蓝牙设备句柄
		_device = _bluetooth.getRemoteDevice(address);

		// 用服务号得到socket
		try{
			_socket = _device.createRfcommSocketToServiceRecord(UUID.fromString(MY_UUID));
		}catch(IOException e){
			Toast.makeText(this, "连接失败！", Toast.LENGTH_SHORT).show();
		}
		//连接socket
		Button btn = (Button) findViewById(R.id.Button03);
		try{
			_socket.connect();
			Toast.makeText(this, "连接"+_device.getName()+"成功！", Toast.LENGTH_SHORT).show();
			btn.setText("断开");
		}catch(IOException e){
			try{
				Toast.makeText(this, "连接失败！", Toast.LENGTH_SHORT).show();
				_socket.close();
				_socket = null;
			}catch(IOException ee){
				Toast.makeText(this, "连接失败！", Toast.LENGTH_SHORT).show();
			}

			return 0;
		}

		//打开接收线程
		try{
			is = _socket.getInputStream();   //得到蓝牙数据输入流
		}catch(IOException e){
			Toast.makeText(this, "接收数据失败！", Toast.LENGTH_SHORT).show();
			return 0;
		}
		if(bThread==false){
			//ReadThread.setPriority(Thread.MAX_PRIORITY); //NORM_PRIORITY + 3);
			ReadThread.start();
			bThread=true;
		}else{
			bRun = true;
		}

		//打开发送线程
		try{
			os = _socket.getOutputStream();   //得到蓝牙数据输入流
		}catch(IOException e){
			Toast.makeText(this, "发送数据失败！", Toast.LENGTH_SHORT).show();
			return 0;
		}
		KeyCap.SetBlueToothOutputStream(os);

//					if(bThreadOut==false){
//						//ReadThread.setPriority(Thread.MAX_PRIORITY); //NORM_PRIORITY + 3);
//						WriteThread.start();
//						bThreadOut=true;
//					}else{
//						bRunOut = true;
//					}

		KeyCap.ResetKeyboard();

		return 1;
	}

	//接收活动结果，响应startActivityForResult()
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch(requestCode){
			case REQUEST_CONNECT_DEVICE:     //连接结果，由DeviceListActivity设置返回
				// 响应返回结果
				if (resultCode == Activity.RESULT_OK) {   //连接成功，由DeviceListActivity设置返回
					// MAC地址，由DeviceListActivity设置返回

					String address = data.getExtras()
							.getString(BTDeviceListActivity.EXTRA_DEVICE_ADDRESS);

					// 根据mac地址连接蓝牙: 98:D3:32:10:91:5B
					int r = ConnectBT(address);
					if (r > 0)
					{
						LastBTMAC = address;
					}

					// 保存最新的蓝牙MAC地址, 供下次直接优先打开，简化每次都选择蓝牙的重复操作
					saveSharedPreferences();
				}
				break;
			default:break;
		}
	}

	public void SendBT(byte Data[], int num) {
		try {
			os.write(Data);
		}catch(IOException e){
			Log.i("WG", "SendBT() fail.");
		}
	}

	//发送数据线程 test
	Thread WriteThread=new Thread() {

		public void run() {
			int num = 0;
			byte[] buffer = new byte[1024];
			byte[] buffer_new = new byte[1024];
			int i = 0;
			int n = 0;
			bRunOut = true;

			int sn = 0;

			long tick = System.currentTimeMillis();

			long tick_key_life = tick;

			//接收线程
			while (true) {
//				try {
////					while (is.available() == 0) {
////						while (bRun == false) {
////						}
////					}
//
//
//					os.write((byte)sn);
//					os.write((byte)0xd);
//					os.write((byte)0xa);
//					sn++;
//
//				}catch(IOException e){
//				}
				KeyCap.SndKeyEventAck(KeyCap.AckedSN);
				try
				{
					Thread.sleep(20);
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
		}
	};

	//接收数据线程
	Thread ReadThread=new Thread(){

		public void run(){
			int num = 0;
			byte[] buffer = new byte[1024];
			byte[] buffer_new = new byte[1024];
			int i = 0;
			int n = 0;
			bRun = true;

			long tick = System.currentTimeMillis();

			long tick_key_life = tick;

//			boolean reporting_down_only = true;
			boolean reporting_down_only = false;

			KeyCap.ResetKeyboard();

			//接收线程
			while(true){
				try{
					while(is.available()==0){
						while(bRun == false){}

						// wg
						long curtick = System.currentTimeMillis();
						if (curtick - tick >= 100) {
							KeyCapView.postInvalidate(); // wg
							tick = curtick;
						}

						// 生成key up, 当keyboard只报告key down事件时
						if (reporting_down_only == true) {
							if (curtick - tick_key_life >= 5) {    // 10 5
								KeyCap.KeyLifeGo(); // wg
								tick_key_life = curtick;
							}
						}
					}
					while(true){
						num = is.read(buffer);         //读入数据
						n=0;

//						String s0 = new String(buffer,0,num);
//						fmsg+=s0;    //保存收到数据
//						for(i=0;i<num;i++){
//							if((buffer[i] == 0x0d)&&(buffer[i+1]==0x0a)){
//								buffer_new[n] = 0x0a;
//								i++;
//							}else{
//								buffer_new[n] = buffer[i];
//							}
//							n++;
//						}
						//String s = new String(buffer_new,0,n);
						//String s = new String(buffer,0,num);
						//smsg+=s;   //写入接收缓存
						//smsg=s;

						String s = "";
						int key;
						for(i=0;i<num;i++) {
							key = buffer[i] & 0xFF;

							// cach this key event
							if (KeyCap != null) {

								//#define KEY_REPORT_MODE 1 // 0: reporting keydown only; 1: reporting down and up
								//#define KEY_REPORT_DOWN_AND_UP 1
//								if (true) {	// false
								if (reporting_down_only) {
									// reporting keydown only
									KeyCap.OnKeyEvent_reporting_down_only(buffer[i]);

									if (key >= 128)
									{
										// key down

										s = " D_" + String.valueOf(key - 128);
									}
									else
									{
										// key up

										s = " U_" + String.valueOf(key);
									}
									//Log.i("WG", s + " buffer[i]" + String.valueOf(buffer[i]) + ",");

									if (smsg.length() > 50)
									{
										smsg = "";
									}
									smsg += s;
								}
								else {
									// reporting down and up
									//KeyCap.OnKeyEvent(buffer[i]);
									KeyCap.OnKeyEventV2(buffer[i]);
								}
							}

//							if (KeyCapView.T0 == 0) {
//								KeyCapView.Kickoff(KeyCap.SYNC_KB_T0);
//							}
						}

						//KeyCapView.postInvalidate();

						if(is.available()==0)break;  //短时间没有数据才跳出进行显示
					}
					//发送显示消息，进行显示刷新
					//handler.sendMessage(handler.obtainMessage());

				}catch(IOException e){
				}
			}
		}
	};

	//消息处理队列
	Handler handler= new Handler(){
		public void handleMessage(Message msg){
			super.handleMessage(msg);
			dis.setText(smsg);   //显示数据
			sv.scrollTo(0,dis.getMeasuredHeight()); //跳至数据最后一页

			//Log.i("WG", smsg);

			//disImgView.setText(smsg);   //显示数据
			//sv.scrollTo(0,disImgView.getMeasuredHeight()); //跳至数据最后一页

//			KeyCapView.setLayoutParams(lp); //new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
//			KeyCapView.setBackgroundColor(0x888888);
			//KeyCapView.postInvalidate();
		}
	};

	//关闭程序掉用处理部分
	public void onDestroy(){
		super.onDestroy();
		if(_socket!=null)  //关闭连接socket
			try{
				_socket.close();
			}catch(IOException e){}
		//	_bluetooth.disable();  //关闭蓝牙服务
	}

	//菜单处理部分
  /* @Override
    public boolean onCreateOptionsMenu(Menu menu) {//建立菜单
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        return true;
    }*/

  /*  @Override
    public boolean onOptionsItemSelected(MenuItem item) { //菜单响应函数
        switch (item.getItemId()) {
        case R.id.scan:
        	if(_bluetooth.isEnabled()==false){
        		Toast.makeText(this, "Open BT......", Toast.LENGTH_LONG).show();
        		return true;
        	}
            // Launch the DeviceListActivity to see devices and do scan
            Intent serverIntent = new Intent(this, DeviceListActivity.class);
            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
            return true;
        case R.id.quit:
            finish();
            return true;
        case R.id.clear:
        	smsg="";
        	ls.setText(smsg);
        	return true;
        case R.id.save:
        	Save();
        	return true;
        }
        return false;
    }*/

	//连接按键响应函数
	public void onConnectButtonClicked(View v){
		if(_bluetooth.isEnabled()==false){  //如果蓝牙服务不可用则提示
			Toast.makeText(this, " 打开蓝牙中...", Toast.LENGTH_LONG).show();
			return;
		}

		//如未连接设备则打开DeviceListActivity进行设备搜索
		Button btn = (Button) findViewById(R.id.Button03);
		if(_socket==null){

			// 先尝试连接上次一的bt设备
			readSharedPreferences();
			if (!LastBTMAC.equals("NULL"))
			{
				// 根据mac地址连接蓝牙: 98:D3:32:10:91:5B
				int r = ConnectBT(LastBTMAC);
				if (r == 1)
				{
					return;
				}
			}

			// 再尝试搜索新设备
			Intent serverIntent = new Intent(this, BTDeviceListActivity.class); //跳转程序设置
			startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);  //设置返回宏定义
		}
		else{
			//关闭连接socket
			try{

				is.close();
				_socket.close();
				_socket = null;
				bRun = false;
				btn.setText("连接");
			}catch(IOException e){}
		}
		return;
	}

	//保存按键响应函数
//	public void onSaveButtonClicked(View v){
//		Save();
//	}
//
//	//清除按键响应函数
//	public void onClearButtonClicked(View v){
//		smsg="";
//		fmsg="";
//		dis.setText(smsg);
//		return;
//	}

	//退出按键响应函数
	public void onQuitButtonClicked(View v){
		finish();
	}

//	//保存功能实现
//	private void Save() {
//		//显示对话框输入文件名
//		LayoutInflater factory = LayoutInflater.from(BTClient2.this);  //图层模板生成器句柄
//		final View DialogView =  factory.inflate(R.layout.bt_sname, null);  //用sname.xml模板生成视图模板
//		new AlertDialog.Builder(BTClient2.this)
//				.setTitle("文件名")
//				.setView(DialogView)   //设置视图模板
//				.setPositiveButton("确定",
//						new DialogInterface.OnClickListener() //确定按键响应函数
//						{
//							public void onClick(DialogInterface dialog, int whichButton){
//								EditText text1 = (EditText)DialogView.findViewById(R.id.sname);  //得到文件名输入框句柄
//								filename = text1.getText().toString();  //得到文件名
//
//								try{
//									if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){  //如果SD卡已准备好
//
//										filename =filename+".txt";   //在文件名末尾加上.txt
//										File sdCardDir = Environment.getExternalStorageDirectory();  //得到SD卡根目录
//										File BuildDir = new File(sdCardDir, "/data");   //打开data目录，如不存在则生成
//										if(BuildDir.exists()==false)BuildDir.mkdirs();
//										File saveFile =new File(BuildDir, filename);  //新建文件句柄，如已存在仍新建文档
//										FileOutputStream stream = new FileOutputStream(saveFile);  //打开文件输入流
//										stream.write(fmsg.getBytes());
//										stream.close();
//										Toast.makeText(BTClient2.this, "存储成功！", Toast.LENGTH_SHORT).show();
//									}else{
//										Toast.makeText(BTClient2.this, "没有存储卡！", Toast.LENGTH_LONG).show();
//									}
//
//								}catch(IOException e){
//									return;
//								}
//
//
//
//							}
//						})
//				.setNegativeButton("取消",   //取消按键响应函数,直接退出对话框不做任何处理
//						new DialogInterface.OnClickListener() {
//							public void onClick(DialogInterface dialog, int which) {
//							}
//						}).show();  //显示对话框
//	}
}
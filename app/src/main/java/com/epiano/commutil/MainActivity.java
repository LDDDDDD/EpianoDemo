package com.epiano.commutil;

//import android.R;
//import android.R;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.epiano.av.ictvoip.androidvideo.capture.Camera2Test;
import com.epiano.av.ictvoip.androidvideo.capture.MainGUI;

//import android.epiano.com.commutil.R;


//import com.example.android.apis.view.Grid1;

//import com.example.android.apis.R;
//import com.example.android.apis.view.Grid1;
//import com.example.android.apis.view.Grid1.AppsAdapter;


public class MainActivity extends Activity{   // implements OnClickListener

	//private MusicScroe MScore;
	private LinearLayout ll;

/*	// service
	private epdeamon.MyBinder myBinder;
	private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            myBinder = (epdeamon.MyBinder) service;
            myBinder.startDownload();
        }
    }; */

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activitymain);

		//LinearLayout ll = new LinearLayout(AQueryTest2.this);


//		ll = (LinearLayout)findViewById(R.id.LLLayout);
////		ll = (RelativeLayout)findViewById(R.id.layout);
////		ll = (TableLayout)findViewById(R.id.layout);



		// 閿熸枻鎷锋椂閿熸枻鎷烽敓鏂ゆ嫹閿熸枻锟?
//		mTimer = new Timer(true);
//		if (mTimer != null ){
//			if (mTimerTask != null){
//				mTimerTask.cancel();  //閿熸枻鎷峰師閿熸枻鎷烽敓鏂ゆ嫹浣ｉ敓鏂ゆ嫹閿熸枻鎷烽敓鏂ゆ嫹鐬ラ敓锟?
//			}
//			//if (mDrawTask != null){
//			//	mDrawTask.cancel();  //閿熸枻鎷峰師閿熸枻鎷烽敓鏂ゆ嫹浣ｉ敓鏂ゆ嫹閿熸枻鎷烽敓鏂ゆ嫹鐬ラ敓锟?
//			//}
//			mTimerTask = new MyTimerTask();  // 閿熸枻鎷锋椂閿熸枻鎷烽敓鏂ゆ嫹閿熸枻鎷稰ingserver閿熸枻鎷稰NPing
//			mTimer.schedule(mTimerTask, 100, 5000);
//		}

		//setContentView(R.layout.main);
        /* findViewById(R.id.button1)鍙栭敓鐭鎷烽敓鏂ゆ嫹main.xml閿熷彨纰夋嫹button1 */
		Button button = (Button)findViewById(R.id.buttonKey2);
		EditText FileNmaeEditText = (EditText)findViewById(R.id.editText1);
		FileNmaeEditText.setText("/mnt/sdcard/sdir/1.sco"); //ComLang1_raw.sco");

        /* 閿熸枻鎷烽敓鏂ゆ嫹button閿熸枻鎷烽敓閾扮》鎷烽敓鏂ゆ嫹锟?*/
		button.setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View arg0) {
                /* 閿熼摪鏂ゆ嫹涓�閿熸枻鎷稩ntent閿熸枻鎷烽敓鏂ゆ嫹 */
				Intent intent = new Intent();
                /* 鎸囬敓鏂ゆ嫹intent瑕侀敓鏂ゆ嫹閿熸枻鎷烽敓鏂ゆ嫹閿熸枻锟?*/
				intent.setClass(MainActivity.this, MusicScore.class);
//                intent.setClass(MainActivity.this, TestActivity.class);
                /* 閿熸枻鎷烽敓鏂ゆ嫹涓�閿熸枻鎷烽敓閾扮鎷稟ctivity */

				//閿熸枻鎷烽敓鏂ゆ嫹閿熸枻鎷烽敓鏂ゆ嫹
				EditText FileNmaeEditText = (EditText)findViewById(R.id.editText1);
				String openfilename = FileNmaeEditText.getText().toString();
				Bundle bundle=new Bundle();
				//bundle.putString("openfilename", "閿熸枻鎷蜂竴閿熸枻鎷穉ctivity閿熸枻鎷烽敓鏂ゆ嫹閿熸枻锟?);
				bundle.putString("openfilename", openfilename);
				intent.putExtras(bundle);
				//涔熼敓鏂ゆ嫹閿熸枻鎷烽敓鏂ゆ嫹閿熸枻鎷烽敓琛楀嚖鎷峰紡閿熸枻鎷烽敓鏂ゆ嫹.
				//intent.putExtra("result", "閿熸枻鎷蜂竴閿熸枻鎷穉ctivity閿熸枻鎷烽敓鏂ゆ嫹閿熸枻锟?);

				startActivityForResult(intent, 0);
                /* 閿熸埅闂鎷峰墠閿熸枻鎷稟ctivity */
				//MainActivity.this.finish();
			}
		});

        /* 閿熸枻鎷烽敓鏂ゆ嫹button閿熸枻鎷烽敓閾扮》鎷烽敓鏂ゆ嫹锟?*/
		Button buttonBook3D = (Button)findViewById(R.id.ButtonBook3D);
		buttonBook3D.setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View arg0) {
                /* 閿熼摪鏂ゆ嫹涓�閿熸枻鎷稩ntent閿熸枻鎷烽敓鏂ゆ嫹 */
				Intent intent = new Intent();
                /* 鎸囬敓鏂ゆ嫹intent瑕侀敓鏂ゆ嫹閿熸枻鎷烽敓鏂ゆ嫹閿熸枻锟?*/
				intent.setClass(MainActivity.this, MusicScoreBook3D.class);
//                intent.setClass(MainActivity.this, TestActivity.class);
                /* 閿熸枻鎷烽敓鏂ゆ嫹涓�閿熸枻鎷烽敓閾扮鎷稟ctivity */

				//閿熸枻鎷烽敓鏂ゆ嫹閿熸枻鎷烽敓鏂ゆ嫹
				EditText FileNmaeEditText = (EditText)findViewById(R.id.editText1);
				String openfilename = FileNmaeEditText.getText().toString();
				Bundle bundle=new Bundle();
				//bundle.putString("openfilename", "閿熸枻鎷蜂竴閿熸枻鎷穉ctivity閿熸枻鎷烽敓鏂ゆ嫹閿熸枻锟?);
				bundle.putString("openfilename", openfilename);
				intent.putExtras(bundle);
				//涔熼敓鏂ゆ嫹閿熸枻鎷烽敓鏂ゆ嫹閿熸枻鎷烽敓琛楀嚖鎷峰紡閿熸枻鎷烽敓鏂ゆ嫹.
				//intent.putExtra("result", "閿熸枻鎷蜂竴閿熸枻鎷穉ctivity閿熸枻鎷烽敓鏂ゆ嫹閿熸枻锟?);

				startActivityForResult(intent, 0);
                /* 閿熸埅闂鎷峰墠閿熸枻鎷稟ctivity */
				//MainActivity.this.finish();
			}
		});

        /* 閿熸枻鎷烽敓鏂ゆ嫹button閿熸枻鎷烽敓閾扮》鎷烽敓鏂ゆ嫹锟?*/
		Button ButtonBook3DGl2 = (Button)findViewById(R.id.ButtonBook3DGl2);
		ButtonBook3DGl2.setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View arg0) {
                /* 閿熼摪鏂ゆ嫹涓�閿熸枻鎷稩ntent閿熸枻鎷烽敓鏂ゆ嫹 */
				Intent intent = new Intent();
                /* 鎸囬敓鏂ゆ嫹intent瑕侀敓鏂ゆ嫹閿熸枻鎷烽敓鏂ゆ嫹閿熸枻锟?*/
				intent.setClass(MainActivity.this, MusicScoreBook3DGL2.class);
//                intent.setClass(MainActivity.this, TestActivity.class);
                /* 閿熸枻鎷烽敓鏂ゆ嫹涓�閿熸枻鎷烽敓閾扮鎷稟ctivity */

				//閿熸枻鎷烽敓鏂ゆ嫹閿熸枻鎷烽敓鏂ゆ嫹
				EditText FileNmaeEditText = (EditText)findViewById(R.id.editText1);
				String openfilename = FileNmaeEditText.getText().toString();
				Bundle bundle=new Bundle();
				//bundle.putString("openfilename", "閿熸枻鎷蜂竴閿熸枻鎷穉ctivity閿熸枻鎷烽敓鏂ゆ嫹閿熸枻锟?);
				bundle.putString("openfilename", openfilename);
				intent.putExtras(bundle);
				//涔熼敓鏂ゆ嫹閿熸枻鎷烽敓鏂ゆ嫹閿熸枻鎷烽敓琛楀嚖鎷峰紡閿熸枻鎷烽敓鏂ゆ嫹.
				//intent.putExtra("result", "閿熸枻鎷蜂竴閿熸枻鎷穉ctivity閿熸枻鎷烽敓鏂ゆ嫹閿熸枻锟?);

				startActivityForResult(intent, 0);
                /* 閿熸埅闂鎷峰墠閿熸枻鎷稟ctivity */
				//MainActivity.this.finish();
			}
		});

        /* 閿熸枻鎷烽敓鏂ゆ嫹button閿熸枻鎷烽敓閾扮》鎷烽敓鏂ゆ嫹锟?*/
		Button button2 = (Button)findViewById(R.id.button2);
		button2.setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View arg0) {
                /* 閿熼摪鏂ゆ嫹涓�閿熸枻鎷稩ntent閿熸枻鎷烽敓鏂ゆ嫹 */
				Intent intent = new Intent();
                /* 鎸囬敓鏂ゆ嫹intent瑕侀敓鏂ゆ嫹閿熸枻鎷烽敓鏂ゆ嫹閿熸枻锟?*/
				intent.setClass(MainActivity.this, MusicScoreBook.class);
//                intent.setClass(MainActivity.this, TestActivity.class);
                /* 閿熸枻鎷烽敓鏂ゆ嫹涓�閿熸枻鎷烽敓閾扮鎷稟ctivity */

				//閿熸枻鎷烽敓鏂ゆ嫹閿熸枻鎷烽敓鏂ゆ嫹
				EditText FileNmaeEditText = (EditText)findViewById(R.id.editText1);
				String openfilename = FileNmaeEditText.getText().toString();
				Bundle bundle=new Bundle();
				//bundle.putString("openfilename", "閿熸枻鎷蜂竴閿熸枻鎷穉ctivity閿熸枻鎷烽敓鏂ゆ嫹閿熸枻锟?);
				bundle.putString("openfilename", openfilename);
				intent.putExtras(bundle);
				//涔熼敓鏂ゆ嫹閿熸枻鎷烽敓鏂ゆ嫹閿熸枻鎷烽敓琛楀嚖鎷峰紡閿熸枻鎷烽敓鏂ゆ嫹.
				//intent.putExtra("result", "閿熸枻鎷蜂竴閿熸枻鎷穉ctivity閿熸枻鎷烽敓鏂ゆ嫹閿熸枻锟?);

				startActivityForResult(intent, 0);
                /* 閿熸埅闂鎷峰墠閿熸枻鎷稟ctivity */
				//MainActivity.this.finish();
			}
		});

//        Button button2 = (Button)findViewById(R.id.button2);
//        /* 閿熸枻鎷烽敓鏂ゆ嫹button閿熸枻鎷烽敓閾扮》鎷烽敓鏂ゆ嫹锟?*/
//        button2.setOnClickListener(new Button.OnClickListener(){
//            @Override
//            public void onClick(View arg0) {
//                /* 閿熼摪鏂ゆ嫹涓�閿熸枻鎷稩ntent閿熸枻鎷烽敓鏂ゆ嫹 */
//                Intent intent = new Intent();
//                /* 鎸囬敓鏂ゆ嫹intent瑕侀敓鏂ゆ嫹閿熸枻鎷烽敓鏂ゆ嫹閿熸枻锟?*/
//              //  intent.setClass(MainActivity.this, MusicScroe.class);
//                intent.setClass(MainActivity.this, TestActivity.class);
//                /* 閿熸枻鎷烽敓鏂ゆ嫹涓�閿熸枻鎷烽敓閾扮鎷稟ctivity */
//                startActivity(intent);
//                /* 閿熸埅闂鎷峰墠閿熸枻鎷稟ctivity */
//                MainActivity.this.finish();
//            }
//        });

        /* 閿熸枻鎷烽敓鏂ゆ嫹button閿熸枻鎷烽敓閾扮》鎷烽敓鏂ゆ嫹锟?*/
		Button buttonAD = (Button)findViewById(R.id.buttonAD);
		buttonAD.setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View arg0) {
                /* 閿熼摪鏂ゆ嫹涓�閿熸枻鎷稩ntent閿熸枻鎷烽敓鏂ゆ嫹 */
				Intent intent = new Intent();
                /* 鎸囬敓鏂ゆ嫹intent瑕侀敓鏂ゆ嫹閿熸枻鎷烽敓鏂ゆ嫹閿熸枻锟?*/
				intent.setClass(MainActivity.this, AudioMaker.class);
//                intent.setClass(MainActivity.this, TestActivity.class);
                /* 閿熸枻鎷烽敓鏂ゆ嫹涓�閿熸枻鎷烽敓閾扮鎷稟ctivity */

				//閿熸枻鎷烽敓鏂ゆ嫹閿熸枻鎷烽敓鏂ゆ嫹
				EditText FileNmaeEditText = (EditText)findViewById(R.id.editText1);
				String openfilename = FileNmaeEditText.getText().toString();
				Bundle bundle=new Bundle();
				//bundle.putString("openfilename", "閿熸枻鎷蜂竴閿熸枻鎷穉ctivity閿熸枻鎷烽敓鏂ゆ嫹閿熸枻锟?);
				bundle.putString("openfilename", openfilename);
				intent.putExtras(bundle);
				//涔熼敓鏂ゆ嫹閿熸枻鎷烽敓鏂ゆ嫹閿熸枻鎷烽敓琛楀嚖鎷峰紡閿熸枻鎷烽敓鏂ゆ嫹.
				//intent.putExtra("result", "閿熸枻鎷蜂竴閿熸枻鎷穉ctivity閿熸枻鎷烽敓鏂ゆ嫹閿熸枻锟?);

				startActivityForResult(intent, 0);
                /* 閿熸埅闂鎷峰墠閿熸枻鎷稟ctivity */
				//MainActivity.this.finish();
			}
		});

		/* bluetooth test */
		Button buttonBTT = (Button)findViewById(R.id.buttonBTTest);
		buttonBTT.setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View arg0) {

				Intent intent = new Intent();

				intent.setClass(MainActivity.this, BlueToothTest.class);
//                intent.setClass(MainActivity.this, TestActivity.class);

				EditText FileNmaeEditText = (EditText)findViewById(R.id.editText1);
				String openfilename = FileNmaeEditText.getText().toString();
				Bundle bundle=new Bundle();
				//bundle.putString("openfilename", "閿熸枻鎷蜂竴閿熸枻鎷穉ctivity閿熸枻鎷烽敓鏂ゆ嫹閿熸枻锟?);
				bundle.putString("openfilename", openfilename);
				intent.putExtras(bundle);

				//intent.putExtra("result", "閿熸枻鎷蜂竴閿熸枻鎷穉ctivity閿熸枻鎷烽敓鏂ゆ嫹閿熸枻锟?);

				startActivityForResult(intent, 0);
                /* 閿熸埅闂鎷峰墠閿熸枻鎷稟ctivity */
				//MainActivity.this.finish();
			}
		});

		/* bluetooth test communication*/
		Button buttonBTTC = (Button)findViewById(R.id.buttonBTTestCom);
		buttonBTTC.setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View arg0) {

				Intent intent = new Intent();

				intent.setClass(MainActivity.this, BlueToothTestCom.class);
//                intent.setClass(MainActivity.this, TestActivity.class);

				EditText FileNmaeEditText = (EditText)findViewById(R.id.editText1);
				String openfilename = FileNmaeEditText.getText().toString();
				Bundle bundle=new Bundle();
				//bundle.putString("openfilename", "閿熸枻鎷蜂竴閿熸枻鎷穉ctivity閿熸枻鎷烽敓鏂ゆ嫹閿熸枻锟?);
				bundle.putString("openfilename", openfilename);
				intent.putExtras(bundle);

				//intent.putExtra("result", "閿熸枻鎷蜂竴閿熸枻鎷穉ctivity閿熸枻鎷烽敓鏂ゆ嫹閿熸枻锟?);

				startActivityForResult(intent, 0);
                /* 閿熸埅闂鎷峰墠閿熸枻鎷稟ctivity */
				//MainActivity.this.finish();
			}
		});

		/* bluetooth test communication*/
		Button buttonBTTC2 = (Button)findViewById(R.id.buttonBTTestCom2);
		buttonBTTC2.setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View arg0) {

				Intent intent = new Intent();

				intent.setClass(MainActivity.this, BTClient2.class);
//                intent.setClass(MainActivity.this, TestActivity.class);

				EditText FileNmaeEditText = (EditText)findViewById(R.id.editText1);
				String openfilename = FileNmaeEditText.getText().toString();
				Bundle bundle=new Bundle();
				//bundle.putString("openfilename", "閿熸枻鎷蜂竴閿熸枻鎷穉ctivity閿熸枻鎷烽敓鏂ゆ嫹閿熸枻锟?);
				bundle.putString("openfilename", openfilename);
				intent.putExtras(bundle);

				//intent.putExtra("result", "閿熸枻鎷蜂竴閿熸枻鎷穉ctivity閿熸枻鎷烽敓鏂ゆ嫹閿熸枻锟?);

				startActivityForResult(intent, 0);
                /* 閿熸埅闂鎷峰墠閿熸枻鎷稟ctivity */
				//MainActivity.this.finish();
			}
		});

		/* Auido Video communication*/
		Button buttonAV = (Button)findViewById(R.id.buttonAVideo);
		buttonAV.setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View arg0) {

				Intent intent = new Intent();

				intent.setClass(MainActivity.this, MainGUI.class);
//                intent.setClass(MainActivity.this, TestActivity.class);

				EditText FileNmaeEditText = (EditText)findViewById(R.id.editText1);
				String openfilename = FileNmaeEditText.getText().toString();
				Bundle bundle=new Bundle();
				//bundle.putString("openfilename", "閿熸枻鎷蜂竴閿熸枻鎷穉ctivity閿熸枻鎷烽敓鏂ゆ嫹閿熸枻锟?);
				bundle.putString("openfilename", openfilename);
				intent.putExtras(bundle);

				//intent.putExtra("result", "閿熸枻鎷蜂竴閿熸枻鎷穉ctivity閿熸枻鎷烽敓鏂ゆ嫹閿熸枻锟?);

				startActivityForResult(intent, 0);
                /* 閿熸埅闂鎷峰墠閿熸枻鎷稟ctivity */
				//MainActivity.this.finish();
			}
		});

		/* buttonCameraTest*/
		Button buttonCameraTest = (Button)findViewById(R.id.buttonCameraTest);
		buttonCameraTest.setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View arg0) {

				Intent intent = new Intent();

				intent.setClass(MainActivity.this, Camera2Test.class);
//                intent.setClass(MainActivity.this, TestActivity.class);

				EditText FileNmaeEditText = (EditText)findViewById(R.id.editText1);
				String openfilename = FileNmaeEditText.getText().toString();
				Bundle bundle=new Bundle();
				//bundle.putString("openfilename", "閿熸枻鎷蜂竴閿熸枻鎷穉ctivity閿熸枻鎷烽敓鏂ゆ嫹閿熸枻锟?);
				bundle.putString("openfilename", openfilename);
				intent.putExtras(bundle);

				//intent.putExtra("result", "閿熸枻鎷蜂竴閿熸枻鎷穉ctivity閿熸枻鎷烽敓鏂ゆ嫹閿熸枻锟?);

				startActivityForResult(intent, 0);
                /* 閿熸埅闂鎷峰墠閿熸枻鎷稟ctivity */
				//MainActivity.this.finish();
			}
		});

		/* USB Camera*/
		Button buttonUSBCam = (Button)findViewById(R.id.buttonUVCWebCam);
		buttonUSBCam.setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View arg0) {

				Intent intent = new Intent();

				intent.setClass(MainActivity.this, UvcWebCamApp.class);
//                intent.setClass(MainActivity.this, TestActivity.class);

				EditText FileNmaeEditText = (EditText)findViewById(R.id.editText1);
				String openfilename = FileNmaeEditText.getText().toString();
				Bundle bundle=new Bundle();
				//bundle.putString("openfilename", "閿熸枻鎷蜂竴閿熸枻鎷穉ctivity閿熸枻鎷烽敓鏂ゆ嫹閿熸枻锟?);
				bundle.putString("openfilename", openfilename);
				intent.putExtras(bundle);

				//intent.putExtra("result", "閿熸枻鎷蜂竴閿熸枻鎷穉ctivity閿熸枻鎷烽敓鏂ゆ嫹閿熸枻锟?);

				startActivityForResult(intent, 0);
                /* 閿熸埅闂鎷峰墠閿熸枻鎷稟ctivity */
				//MainActivity.this.finish();
			}
		});

		/* eLearning */
		Button buttoneLearning = (Button)findViewById(R.id.buttoneLearning);
		buttoneLearning.setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent();

				intent.setClass(MainActivity.this, PianoELearning.class);

				EditText FileNmaeEditText = (EditText)findViewById(R.id.editText1);
				String openfilename = FileNmaeEditText.getText().toString();
				Bundle bundle=new Bundle();
				bundle.putString("openfilename", openfilename);
				intent.putExtras(bundle);

				startActivityForResult(intent, 0);
                /* 閿熸埅闂鎷峰墠閿熸枻鎷稟ctivity */
				//MainActivity.this.finish();
			}
		});

		/* audiorecord */
		Button buttonaudiorecord = (Button)findViewById(R.id.buttonaudiorecord);
		buttonaudiorecord.setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent();

				intent.setClass(MainActivity.this, TestAudioRecord.class);

				EditText FileNmaeEditText = (EditText)findViewById(R.id.editText1);
				String openfilename = FileNmaeEditText.getText().toString();
				Bundle bundle=new Bundle();
				bundle.putString("openfilename", openfilename);
				intent.putExtras(bundle);

				startActivityForResult(intent, 0);
				//MainActivity.this.finish();
			}
		});

		// start the service
//		Intent startIntent = new Intent(this, epdeamon.class);
//        startService(startIntent);

		//Intent bindIntent = new Intent(this, epdeamon.class);
		//bindService(bindIntent, connection, BIND_AUTO_CREATE);
	}



	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub

		int action = event.getAction();

		switch (action) {
			case MotionEvent.ACTION_DOWN:
				//Log.i("MainActivity", "MainActivity-dispatchTouchEvent action = action down");


				break;
			case MotionEvent.ACTION_MOVE:
				//Log.i("MainActivity", "MainActivity-dispatchTouchEvent action = action move");

				break;
			case MotionEvent.ACTION_UP:
				//Log.i("MainActivity", "MainActivity-dispatchTouchEvent action = action up");

				break;

			default:
				break;
		}

		return super.dispatchTouchEvent(event);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		int action = event.getAction();
		switch (action) {
			case MotionEvent.ACTION_DOWN:
				//Log.i("MainActivity", "MainActivity-onTouchEvent action = action down");
				break;

			case MotionEvent.ACTION_UP:
				//Log.i("MainActivity", "MainActivity-onTouchEvent action = action up");
				break;

			default:
				break;
		}

		return super.onTouchEvent(event);
	}

//	@Override
//	public void onClick(View view) {
//		// TODO Auto-generated method stub
//		int id = view.getId();
////		switch (id) {
////		case R.id.view_btn:
////			Log.i("MainActivity", "MainActivity view_btn clicked");
////			break;
////
////		default:
////			break;
////		}
//	}


	// wg add
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		// Checks the orientation of the screen
		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			Toast.makeText(this, "妯睆妯″紡", Toast.LENGTH_SHORT).show();
		} else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
			Toast.makeText(this, "绔栧睆妯″紡", Toast.LENGTH_SHORT).show();
		}

	}


//	@Override
//	// 閿熸枻鎷锋瘡閿熸枻鎷烽敓鏂ゆ嫹View閿熸枻鎷峰浘閿熸枻鎷烽敓鍙鎷烽敓鏂ゆ嫹
//	protected void onLayout(boolean changed, int l, int t, int r, int b) {
//		// TODO Auto-generated method stub
//		// 閫氶敓鏂ゆ嫹init()閿熸枻鎷烽敓鏂ゆ嫹,閿熸枻鎷烽敓鏂ゆ嫹涓洪敓鏂ゆ嫹ViewGroup閿熸枻鎷烽敓鏂ゆ嫹閿熸枻鎷烽敓鏂ゆ嫹閿熸枻鎷烽敓鏂ゆ嫹閿熸枻鎷烽敓閰碉拷
//		int childCount = getChildCount();
//		int startLeft = 0; // 閿熸枻鎷烽敓鏂ゆ嫹姣忛敓鏂ゆ嫹閿熸枻鎷稸iew閿熸枻鎷烽敓鏂ゆ嫹濮嬮敓鏂ゆ嫹閿熸枻鎷烽敓鏂ゆ嫹
//		int startTop = 10; // 姣忛敓鏂ゆ嫹閿熸枻鎷稸iew閿熸枻鎷烽敓璇埗閿熸枻鎷峰浘閿熸枻鎷蜂綅閿熸枻锟? 閿熸枻鎷烽敓鏂ゆ嫹閿熸枻鎷蜂负10px,閿熸枻鎷烽敓鏂ゆ嫹閿熸枻鎷烽敓杞跨寴ndroid:margin =
//							// 10px;
//
//		Log.i("WG", "**** onLayout start ****");
//		for (int i = 0; i < childCount; i++) {
//			View child = getChildAt(i);
//			child.layout(startLeft, startTop,
//					startLeft.child.getMeasuredWidth(),
//					startTop + child.getMeasuredHeight());
//			startLeft = startLeft + child.getMeasuredWidth() + 10; // 鏍″噯startLeft锟?View涔嬮敓鏂ゆ嫹鍕熼敓鏂ゆ嫹閿熸枻鎷蜂负10px;
//			Log.i("WG", "**** onLayout startLeft ****" + startLeft);
//		}
//	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

//	private class MyTimerTask extends TimerTask{
//		@Override  public void run() {
//			boolean pkraterewind = false;
//
//			//Message msg = mHandler.obtainMessage(EVENT_PINGSERVER);
//			//msg.sendToTarget();
////			timercounter++;
//			//Log.i("Mytimertask:", "timercounter="+timercounter);
//
//		}
//	}

}

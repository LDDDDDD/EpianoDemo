package com.epiano.commutil;

//import android.R;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.TimerTask;

//import android.epiano.com.commutil.R;
//import commutil.com.epiano.android.R;


//import com.example.android.apis.view.Grid1;

//import com.example.android.apis.R;
//import com.example.android.apis.view.Grid1;
//import com.example.android.apis.view.Grid1.AppsAdapter;


public class TestActivity extends Activity  {
	
//	private MusicScore MScore;
	private LinearLayout ll;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.testactivity);
		
		//LinearLayout ll = new LinearLayout(AQueryTest2.this);
		

//		ll = (LinearLayout)findViewById(R.id.LLLayout);
////		ll = (RelativeLayout)findViewById(R.id.layout);
////		ll = (TableLayout)findViewById(R.id.layout);
		

		
//		mTimer = new Timer(true);
//		if (mTimer != null ){
//			if (mTimerTask != null){
//				mTimerTask.cancel();  //��ԭ����Ӷ������Ƴ� 
//			}
//			//if (mDrawTask != null){
//			//	mDrawTask.cancel();  //��ԭ����Ӷ������Ƴ� 
//			//}
//			mTimerTask = new MyTimerTask();  // ��ʱ������Pingserver��PNPing
//			mTimer.schedule(mTimerTask, 100, 5000);
//		}
		
		//setContentView(R.layout.main);  
        /* findViewById(R.id.button1)ȡ�ò���main.xml�е�button1 */  
        Button button = (Button)findViewById(R.id.button2);
        /* ����button���¼���Ϣ */  
        button.setOnClickListener(new OnClickListener(){
            @Override  
            public void onClick(View arg0) {  
                /* �½�һ��Intent���� */  
                Intent intent = new Intent();  
                /* ָ��intentҪ�������� */  
                intent.setClass(TestActivity.this, MainActivity.class);
                /* ����һ���µ�Activity */  
                startActivity(intent);  
                /* �رյ�ǰ��Activity */  
                TestActivity.this.finish();
            }  
        });  

	
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
	        Toast.makeText(this, "����ģʽ", Toast.LENGTH_SHORT).show();
	    } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
	        Toast.makeText(this, "����ģʽ", Toast.LENGTH_SHORT).show();
	    }    
	
	}
	

//	@Override  
//	// ��ÿ����View��ͼ���в���
//	protected void onLayout(boolean changed, int l, int t, int r, int b) {
//		// TODO Auto-generated method stub
//		// ͨ��init()����,����Ϊ��ViewGroup���������������ͼ
//		int childCount = getChildCount();
//		int startLeft = 0; // ����ÿ����View����ʼ������
//		int startTop = 10; // ÿ����View���븸��ͼ��λ��, ������Ϊ10px,�������Ϊandroid:margin =
//							// 10px;
//
//		Log.i("WG", "**** onLayout start ****");
//		for (int i = 0; i < childCount; i++) {
//			View child = getChildAt(i);
//			child.layout(startLeft, startTop,
//					startLeft.child.getMeasuredWidth(),
//					startTop + child.getMeasuredHeight());
//			startLeft = startLeft + child.getMeasuredWidth() + 10; // У׼startLeftֵ,View֮��ļ����Ϊ10px;
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
	
	private class MyTimerTask extends TimerTask{
		@Override  public void run() {	    		
			boolean pkraterewind = false;	
			
			//Message msg = mHandler.obtainMessage(EVENT_PINGSERVER);
			//msg.sendToTarget();	    		
//			timercounter++;
			//Log.i("Mytimertask:", "timercounter="+timercounter);

		}
	}
	
}

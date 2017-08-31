package com.epiano.commutil;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


public class AudioMaker extends Activity {
    /** Called when the activity is first created. */
	static final String Tag="AudioMaker";
    static final int frequency = 16000;//ȱʡ������  4000/8000/11050/22050/44100
    static final int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    static final int audioEncodeing = AudioFormat.ENCODING_PCM_16BIT;
	//static final int audioEncodeing = AudioFormat.ENCODING_PCM_8BIT;


	int minBufferSize = 0;//�ɼ�������Ҫ�Ļ�������С
	AudioRecord audioRecord = null;//¼��
	AudioProcess audioProcess = new AudioProcess();//����

    Button btnStart,btnExit;  //��ʼֹͣ��ť
    Button btnKey;  //��ʶ��ť
    Button btnKey2;  //��ʶ��ť, �ӱ����ļ�����
    Button btnMic;	// ���ƴ��ļ���mic��ȡ��Ƶ����
    SurfaceView sfv;  //��ͼ����
    SurfaceView sfvHor;  //��ͼ����
    TextView tv;  //��ͼ����
    CharSequence ResultText[] = new CharSequence[1];

    CheckBox cb_mic;
    SeekBar sb_audiofile;

    int  yRate = 100;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.audiomaker);

        initControl();
        }
    @Override
    protected void onDestroy(){
    	super.onDestroy();

		audioProcess.stop(sfv);

		try {
			// if (counter % 100 == 0)
			{
				//Log.i("WG", "Warning, Can't get buf to play");
				Thread.sleep(500); // 10);
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    	//android.os.Process.killProcess(android.os.Process.myPid()); // �˾䵼�±���
    }

  //��ʼ���ؼ���Ϣ
    private void initControl() {
    	//���ò�����
        Context mContext = getApplicationContext();
        //����
        btnStart = (Button)this.findViewById(R.id.btnStart);
        btnKey = (Button)this.findViewById(R.id.buttonKey);
        btnKey2 = (Button)this.findViewById(R.id.buttonKey2);
        btnExit = (Button)this.findViewById(R.id.btnExit);

        btnMic = (Button)this.findViewById(R.id.buttonMic);

        //�����¼�����
        btnStart.setOnClickListener(new ClickEvent());
        btnKey.setOnClickListener(new ClickEvent());
        btnKey2.setOnClickListener(new ClickEvent());
        btnExit.setOnClickListener(new ClickEvent());

        btnMic.setOnClickListener(new ClickEvent());

//        cb_mic = (CheckBox)this.findViewById(R.id.checkBoxMic);
//        cb_mic.setOnClickListener(new OnClickListener() {
//            public void onClick(View v) {
//
//            }
//        });
        sb_audiofile = (SeekBar)this.findViewById(R.id.seekBarAudioFile);
        sb_audiofile.setOnClickListener(new ClickEvent());

        //���ʺͻ���
        sfv = (SurfaceView)this.findViewById(R.id.SurfaceView01);
        sfvHor = (SurfaceView)this.findViewById(R.id.SurfaceView02);
        tv = (TextView)this.findViewById(R.id.textView1);
        //��ʼ����ʾ
        audioProcess.initDraw(yRate, sfv.getHeight(), sfvHor.getHeight(), mContext, frequency, ResultText);

	}

    /**
     * �����¼�����
     */
    class ClickEvent implements View.OnClickListener{
    	@Override
    	public void onClick(View v){

//    		CheckBox cb = (CheckBox)v;
//    		if (cb == cb_mic)	// ���ƴ�mic���ļ���ȡaudio����
//    		{
//    			boolean r = cb.isChecked();
//    			if (r == true)
//    			{
//    				audioProcess.ReadFromMic(1);
//    			}
//    			else
//    			{
//    				audioProcess.ReadFromMic(0);
//    			}
//    		}

//    		SeekBar sb = (SeekBar)v;
//    		if (sb == sb_audiofile)
//    		{
//
//    		}

    		Button button = (Button)v;
    		if(button == btnStart){
    			if(button.getText().toString().equals("Start")){
        	        try {
            			//¼��
            			if (audioRecord == null)
            			{
	            	        minBufferSize = AudioRecord.getMinBufferSize(frequency,
	            	        		channelConfiguration,
	            	        		audioEncodeing);
							Log.i(Tag,"-----minBufferSize--is:"+minBufferSize+" frequency:"+frequency+" audioEncodeing"+audioEncodeing);
	            	        //minBufferSize = 20 * minBufferSize;
	            	        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,frequency,
	            	        		channelConfiguration,
	            	        		audioEncodeing,
	            	        		minBufferSize);
            			}

            			audioProcess.baseLine = sfv.getHeight()-50;
            			audioProcess.frequence = frequency;
						audioProcess.start(audioRecord, minBufferSize, sfv, sfvHor, tv);

						//btnStart.setText(R.string.btn_exit);
						btnStart.setText("Stop");


    				} catch (Exception e) {
    					// TODO: handle exception
            			Toast.makeText(AudioMaker.this,
            					"��ǰ�豸��֧������ѡ��Ĳ�����"+String.valueOf(frequency)+",������ѡ��",
            					Toast.LENGTH_SHORT).show();
    				}
        		}else if (button.getText().equals("Stop")) {

    				//btnStart.setText(R.string.btn_start);
        			btnStart.setText("Start");

    				audioProcess.stop(sfv);
    			}
    		}
    		else if(button == btnKey){
    			// ��ʶ��
    	        try {
//        			//¼��
//        	        minBufferSize = AudioRecord.getMinBufferSize(frequency,
//        	        		channelConfiguration,
//        	        		audioEncodeing);
//					Log.i(Tag,"-----minBufferSize--is:"+minBufferSize+" frequency:"+frequency+" audioEncodeing"+audioEncodeing);
//        	        //minBufferSize = 20 * minBufferSize;
//        	        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,frequency,
//        	        		channelConfiguration,
//        	        		audioEncodeing,
//        	        		minBufferSize);
//        			audioProcess.baseLine = sfv.getHeight()-50;
//        			audioProcess.frequence = frequency;
    	        	if (btnStart.getText().equals("Stop"))
    	        	{
    	        		audioProcess.startKeyRec(audioRecord, minBufferSize, sfv);
    	        	}

					//btnStart.setText(R.string.btn_exit);
					//btnStart.setText("Stop");

					//audioProcess.stop(sfv);

				} catch (Exception e) {
					// TODO: handle exception
//        			Toast.makeText(AudioMaker.this,
//        					"��ǰ�豸��֧������ѡ��Ĳ�����"+String.valueOf(frequency)+",������ѡ��",
//        					Toast.LENGTH_SHORT).show();
				}
    		}
    		else if(button == btnKey2){
    			// ��ʶ��, �ӱ����ļ�����
    	        try {

    	        	audioProcess.mKeys.GetKeyFromLocalFile();
					//btnStart.setText(R.string.btn_exit);
					//btnStart.setText("Stop");

					//audioProcess.stop(sfv);

				} catch (Exception e) {
					// TODO: handle exception
//        			Toast.makeText(AudioMaker.this,
//        					"��ǰ�豸��֧������ѡ��Ĳ�����"+String.valueOf(frequency)+",������ѡ��",
//        					Toast.LENGTH_SHORT).show();
				}
    		}
    		else if(button == btnMic){
    			// ��ʶ��, �ӱ����ļ�����
    	        try {

    	        	if (button.getText().equals("Mic"))
        			{
    	        		audioProcess.ReadFromMic(0);
    	        		btnMic.setText("File");
        			}
    	        	else
    	        	{
    	        		audioProcess.ReadFromMic(1);
    	        		btnMic.setText("Mic");
    	        	}

				} catch (Exception e) {

				}
    		}
    		else {
    			new AlertDialog.Builder(AudioMaker.this)
    	         .setTitle("��ʾ")
    	         .setMessage("ȷ���˳�?")
    	         .setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
    	        public void onClick(DialogInterface dialog, int whichButton) {
    	        setResult(RESULT_OK);//ȷ����ť�¼�
 				AudioMaker.this.finish();
    	         finish();
    	         }
    	         })
    	         .setNegativeButton("ȡ��", new DialogInterface.OnClickListener() {
    	        public void onClick(DialogInterface dialog, int whichButton) {
    	         //ȡ����ť�¼�
    	         }
    	         })
    	         .show();
			}
    	}
    }

    ////////////////////////////////////// zoom //////////////////////////////

	float BaseDistanceOf2Fingers = 0;
	float mScale = 1;
	float mCurrentScale = 1;
	float last_x = -1;
	float last_y = -1;
	float delta_x = 0;
	int zoomed = 0;
	int mZoomStatus = 0;
	int r = 0;
	int OffsetX = 0;	// sfv. OffsetX

    @Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub

		int action = event.getAction();

		switch (action) {
		case MotionEvent.ACTION_DOWN:
			//Log.i("MainActivity", "MainActivity-dispatchTouchEvent action = action down");

			BaseDistanceOf2Fingers = 0;
			last_x = event.getRawX();
			delta_x = 0;
			zoomed = 0;

			break;
		case MotionEvent.ACTION_MOVE:
			//Log.i("MainActivity", "MainActivity-dispatchTouchEvent action = action move");
//        	// new
//			if (event.getPointerCount() == 2) {
//
//			}

			// new
			if (event.getPointerCount() == 2) {
				float x0, x1, y0, y1;
				x0 = event.getX(0);
				x1 = event.getX(1);
				y0 = event.getY(0);
				y1 = event.getY(1);
				float xx = x0 - x1;
				float yy = y0 - y1;
				float CurDistanceOf2Fingers = (float) Math.sqrt(xx * xx + yy * yy);// ��������ľ���
				if (BaseDistanceOf2Fingers == 0) {
					BaseDistanceOf2Fingers = CurDistanceOf2Fingers;
				} else {
					if (true) // value - BaseDistanceOf2Fingers >= 10 || value - BaseDistanceOf2Fingers <= -10)
					{
						if (mZoomStatus == 0)
						{
							//ProcessZoom(CurDistanceOf2Fingers);
						}
					}
				}
			} else if (event.getPointerCount() == 1) {
				float xx = event.getRawX();
				float yy = event.getRawY();
				int j = 0;
//				xx -= last_x;
//				yy -= last_y;
//				if (xx >= 10 || yy >= 10 || xx <= -10 || yy <= -10)
//					//img_transport(x, y); // �ƶ�ͼƬλ��
				delta_x = xx - last_x;	// delta_x < 0, means move left
				Log.i("MainActivity", "delta_x( " + delta_x + ") >= xx(" + xx + ")");

				// ���һ���
				if (mScale > 1)
				{
					// �Ŵ���������Ҫ���¸�ҳ��OffsetX

					int offsetx = OffsetX;
//					if (pv[j].OffsetX)
					offsetx += -delta_x;
					if (offsetx < 0)
					{
						offsetx = 0;
					}
//					else if (offsetx + sfv.getWidth() > pv[j].mBitmapPeerRctDst.right) // pv[j].getWidth())
//					{
//						offsetx = pv[j].mBitmapPeerRctDst.right - WinWidthCur;
//					}

					OffsetX = offsetx;
					//pv[j].postInvalidate();
				}
				else
				{
					// �����������OffsetX

//					if (pv[j].OffsetX != 0)
//					{
//						for (j = 0; j < PicViewCount; j++) {
//							pv[j].OffsetX = 0;
//						}
//						for (j = 0; j < PicViewCount; j++) {
//							pv[j].postInvalidate(); //0, 0, pv[j].mBitmapPeerRctDst.right, pv[j].mBitmapPeerRctDst.bottom);
//						}
//					}
				}

				last_x = xx;
				last_y = yy;
			}
			break;
		case MotionEvent.ACTION_UP:
			//Log.i("MainActivity", "MainActivity-dispatchTouchEvent action = action up");

			mZoomStatus = 0;

			//if (mScale > 1)
			if (zoomed == 1)
			{
				int sI, sF;
				sI = (int)mScale;
				sF = (int)(mScale * 10) % 10;
				//Toast.makeText(this, "����: " + sI + "." + sF, Toast.LENGTH_SHORT).show();
				Toast.makeText(this, "����: " + sI + "." + sF, Toast.LENGTH_SHORT).show();
			}
			else
			{

			}

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


//    public static Intent openFile(String filePath){
//
//        File file = new File(filePath);
//        if(!file.exists()) return null;
//        /* ȡ����չ�� */
//        String end=file.getName().substring(file.getName().lastIndexOf(".") + 1,file.getName().length()).toLowerCase();
//        /* ����չ�������;���MimeType */
//        if(end.equals("m4a")||end.equals("mp3")||end.equals("mid")||
//                end.equals("xmf")||end.equals("ogg")||end.equals("wav")){
//            return getAudioFileIntent(filePath);
//        }
//
//        return null;
//    }
//
//    //Android��ȡһ�����ڴ�AUDIO�ļ���intent
//    public static Intent getAudioFileIntent( String param ){
//
//        Intent intent = new Intent("android.intent.action.VIEW");
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        intent.putExtra("oneshot", 0);
//        intent.putExtra("configchange", 0);
//        Uri uri = Uri.fromFile(new File(param ));
//        intent.setDataAndType(uri, "audio/*");
//        return intent;
//    }



}
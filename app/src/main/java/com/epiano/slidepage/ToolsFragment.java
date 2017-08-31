package com.epiano.slidepage;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.epiano.av.ictvoip.androidvideo.capture.Camera2Test;
import com.epiano.av.ictvoip.androidvideo.capture.MainGUI;
import com.epiano.commutil.AudioMaker;
import com.epiano.commutil.BTClient2;
import com.epiano.commutil.BlueToothTest;
import com.epiano.commutil.BlueToothTestCom;
import com.epiano.commutil.MusicScore;
import com.epiano.commutil.MusicScoreBook;
import com.epiano.commutil.MusicScoreBook3D;
import com.epiano.commutil.MusicScoreBook3DGL2;
import com.epiano.commutil.PianoELearning;
import com.epiano.commutil.R;
import com.epiano.commutil.TestAudioRecord;
import com.epiano.commutil.UvcWebCamApp;


public class ToolsFragment extends Fragment {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
//		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
//		FrameLayout fl = new FrameLayout(getActivity());
//		fl.setLayoutParams(params);
//		DisplayMetrics dm = getResources().getDisplayMetrics();
//		final int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, dm);
//		TextView v = new TextView(getActivity());
//		params.setMargins(margin, margin, margin, margin);
//		v.setLayoutParams(params);
//		v.setLayoutParams(params);
//		v.setGravity(Gravity.CENTER);
//		v.setText("工具界面");
//		v.setTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, dm));
//		fl.addView(v);		
//		return fl;
		
		final View view = inflater.inflate(R.layout.activitymain, container, false);
		
		{
			//setContentView(R.layout.main);  
	        /* view.findViewById(R.id.button1)取锟矫诧拷锟斤拷main.xml锟叫碉拷button1 */  
	        Button button = (Button)view.findViewById(R.id.buttonKey2);
	        EditText FileNmaeEditText = (EditText)view.findViewById(R.id.editText1);
	        FileNmaeEditText.setText("/mnt/sdcard/sdir/1.sco"); //ComLang1_raw.sco");
	        
	        /* 锟斤拷锟斤拷button锟斤拷锟铰硷拷锟斤拷�?*/  
	        button.setOnClickListener(new Button.OnClickListener(){  
	            @Override  
	            public void onClick(View arg0) {  
	                /* 锟铰斤拷一锟斤拷Intent锟斤拷锟斤拷 */  
	                Intent intent = new Intent();  
	                /* 指锟斤拷intent要锟斤拷锟斤拷锟斤拷锟斤�?*/  
	                intent.setClass(getActivity(), MusicScore.class);
//	                intent.setClass(getActivity(), TestActivity.class);
	                /* 锟斤拷锟斤拷一锟斤拷锟铰碉拷Activity */  
	                
					//锟斤拷锟斤拷锟斤拷锟斤拷   
	                EditText FileNmaeEditText = (EditText)view.findViewById(R.id.editText1);
	                //String openfilename = "/mnt/sdcard/ComLang1_raw.sco"; // 锟斤拷锟侥硷拷锟斤拷	// editText.getText().toString();
	                String openfilename = FileNmaeEditText.getText().toString();
					Bundle bundle=new Bundle();  
					//bundle.putString("openfilename", "锟斤拷一锟斤拷activity锟斤拷锟斤拷锟斤�?);  
					bundle.putString("openfilename", openfilename);  
					intent.putExtras(bundle);  
					//也锟斤拷锟斤拷锟斤拷锟斤拷锟街凤拷式锟斤拷锟斤拷.   
					//intent.putExtra("result", "锟斤拷一锟斤拷activity锟斤拷锟斤拷锟斤�?);
					
	                startActivityForResult(intent, 0);  
	                /* 锟截闭碉拷前锟斤拷Activity */  
	                //getActivity().finish();  
	            }  
	        });
	        
	        /* 锟斤拷锟斤拷button锟斤拷锟铰硷拷锟斤拷�?*/  
	        Button buttonBook3D = (Button)view.findViewById(R.id.ButtonBook3D);
	        buttonBook3D.setOnClickListener(new Button.OnClickListener(){  
	            @Override  
	            public void onClick(View arg0) {  
	                /* 锟铰斤拷一锟斤拷Intent锟斤拷锟斤拷 */  
	                Intent intent = new Intent();  
	                /* 指锟斤拷intent要锟斤拷锟斤拷锟斤拷锟斤�?*/  
	                intent.setClass(getActivity(), MusicScoreBook3D.class);
//	                intent.setClass(getActivity(), TestActivity.class);
	                /* 锟斤拷锟斤拷一锟斤拷锟铰碉拷Activity */  
	                
					//锟斤拷锟斤拷锟斤拷锟斤拷   
	                EditText FileNmaeEditText = (EditText)view.findViewById(R.id.editText1);
	                //String openfilename = "/mnt/sdcard/ComLang1_raw.sco"; // 锟斤拷锟侥硷拷锟斤拷	// editText.getText().toString();
	                String openfilename = FileNmaeEditText.getText().toString();
					Bundle bundle=new Bundle();  
					//bundle.putString("openfilename", "锟斤拷一锟斤拷activity锟斤拷锟斤拷锟斤�?);  
					bundle.putString("openfilename", openfilename);  
					intent.putExtras(bundle);  
					//也锟斤拷锟斤拷锟斤拷锟斤拷锟街凤拷式锟斤拷锟斤拷.   
					//intent.putExtra("result", "锟斤拷一锟斤拷activity锟斤拷锟斤拷锟斤�?);

					startActivityForResult(intent, 0);  
	                /* 锟截闭碉拷前锟斤拷Activity */  
	                //getActivity().finish();  
	            }  
	        });  
	        
	        /* 锟斤拷锟斤拷button锟斤拷锟铰硷拷锟斤拷�?*/  
	        Button ButtonBook3DGl2 = (Button)view.findViewById(R.id.ButtonBook3DGl2);
	        ButtonBook3DGl2.setOnClickListener(new Button.OnClickListener(){  
	            @Override  
	            public void onClick(View arg0) {  
	                /* 锟铰斤拷一锟斤拷Intent锟斤拷锟斤拷 */  
	                Intent intent = new Intent();  
	                /* 指锟斤拷intent要锟斤拷锟斤拷锟斤拷锟斤�?*/  
	                intent.setClass(getActivity(), MusicScoreBook3DGL2.class);
//	                intent.setClass(getActivity(), TestActivity.class);
	                /* 锟斤拷锟斤拷一锟斤拷锟铰碉拷Activity */  
	                
					//锟斤拷锟斤拷锟斤拷锟斤拷   
	                EditText FileNmaeEditText = (EditText)view.findViewById(R.id.editText1);
	                //String openfilename = "/mnt/sdcard/ComLang1_raw.sco"; // 锟斤拷锟侥硷拷锟斤拷	// editText.getText().toString();
	                String openfilename = FileNmaeEditText.getText().toString();
					Bundle bundle=new Bundle();  
					//bundle.putString("openfilename", "锟斤拷一锟斤拷activity锟斤拷锟斤拷锟斤�?);  
					bundle.putString("openfilename", openfilename);  
					intent.putExtras(bundle);  
					//也锟斤拷锟斤拷锟斤拷锟斤拷锟街凤拷式锟斤拷锟斤拷.   
					//intent.putExtra("result", "锟斤拷一锟斤拷activity锟斤拷锟斤拷锟斤�?);

					startActivityForResult(intent, 0);  
	                /* 锟截闭碉拷前锟斤拷Activity */  
	                //getActivity().finish();  
	            }  
	        });   
	        
	        /* 锟斤拷锟斤拷button锟斤拷锟铰硷拷锟斤拷�?*/  
	        Button button2 = (Button)view.findViewById(R.id.button2);
	        button2.setOnClickListener(new Button.OnClickListener(){  
	            @Override  
	            public void onClick(View arg0) {  
	                /* 锟铰斤拷一锟斤拷Intent锟斤拷锟斤拷 */  
	                Intent intent = new Intent();  
	                /* 指锟斤拷intent要锟斤拷锟斤拷锟斤拷锟斤�?*/  
	                intent.setClass(getActivity(), MusicScoreBook.class);
//	                intent.setClass(getActivity(), TestActivity.class);
	                /* 锟斤拷锟斤拷一锟斤拷锟铰碉拷Activity */  
	                
					//锟斤拷锟斤拷锟斤拷锟斤拷   
	                EditText FileNmaeEditText = (EditText)view.findViewById(R.id.editText1);
	                //String openfilename = "/mnt/sdcard/ComLang1_raw.sco"; // 锟斤拷锟侥硷拷锟斤拷	// editText.getText().toString();
	                String openfilename = FileNmaeEditText.getText().toString();
					Bundle bundle=new Bundle();  
					//bundle.putString("openfilename", "锟斤拷一锟斤拷activity锟斤拷锟斤拷锟斤�?);  
					bundle.putString("openfilename", openfilename);  
					intent.putExtras(bundle);  
					//也锟斤拷锟斤拷锟斤拷锟斤拷锟街凤拷式锟斤拷锟斤拷.   
					//intent.putExtra("result", "锟斤拷一锟斤拷activity锟斤拷锟斤拷锟斤�?);

					startActivityForResult(intent, 0);  
	                /* 锟截闭碉拷前锟斤拷Activity */  
	                //getActivity().finish();  
	            }  
	        });  
	        
//	        Button button2 = (Button)view.findViewById(R.id.button2);  
//	        /* 锟斤拷锟斤拷button锟斤拷锟铰硷拷锟斤拷�?*/  
//	        button2.setOnClickListener(new Button.OnClickListener(){  
//	            @Override  
//	            public void onClick(View arg0) {  
//	                /* 锟铰斤拷一锟斤拷Intent锟斤拷锟斤拷 */  
//	                Intent intent = new Intent();  
//	                /* 指锟斤拷intent要锟斤拷锟斤拷锟斤拷锟斤�?*/  
//	              //  intent.setClass(getActivity(), MusicScroe.class);  
//	                intent.setClass(getActivity(), TestActivity.class);
//	                /* 锟斤拷锟斤拷一锟斤拷锟铰碉拷Activity */  
//	                startActivity(intent);  
//	                /* 锟截闭碉拷前锟斤拷Activity */  
//	                getActivity().finish();  
//	            }  
//	        });  

	        /* 锟斤拷锟斤拷button锟斤拷锟铰硷拷锟斤拷�?*/  
	        Button buttonAD = (Button)view.findViewById(R.id.buttonAD);
	        buttonAD.setOnClickListener(new Button.OnClickListener(){  
	            @Override  
	            public void onClick(View arg0) {  
	                /* 锟铰斤拷一锟斤拷Intent锟斤拷锟斤拷 */  
	                Intent intent = new Intent();  
	                /* 指锟斤拷intent要锟斤拷锟斤拷锟斤拷锟斤�?*/  
	                intent.setClass(getActivity(), AudioMaker.class);
//	                intent.setClass(getActivity(), TestActivity.class);
	                /* 锟斤拷锟斤拷一锟斤拷锟铰碉拷Activity */  
	                
					//锟斤拷锟斤拷锟斤拷锟斤拷   
	                EditText FileNmaeEditText = (EditText)view.findViewById(R.id.editText1);
	                //String openfilename = "/mnt/sdcard/ComLang1_raw.sco"; // 锟斤拷锟侥硷拷锟斤拷	// editText.getText().toString();
	                String openfilename = FileNmaeEditText.getText().toString();
					Bundle bundle=new Bundle();  
					//bundle.putString("openfilename", "锟斤拷一锟斤拷activity锟斤拷锟斤拷锟斤�?);  
					bundle.putString("openfilename", openfilename);  
					intent.putExtras(bundle);  
					//也锟斤拷锟斤拷锟斤拷锟斤拷锟街凤拷式锟斤拷锟斤拷.   
					//intent.putExtra("result", "锟斤拷一锟斤拷activity锟斤拷锟斤拷锟斤�?);

					startActivityForResult(intent, 0);  
	                /* 锟截闭碉拷前锟斤拷Activity */  
	                //getActivity().finish();  
	            }  
	        });

			/* bluetooth test */
			Button buttonBTT = (Button)view.findViewById(R.id.buttonBTTest);
			buttonBTT.setOnClickListener(new Button.OnClickListener(){
				@Override
				public void onClick(View arg0) {

					Intent intent = new Intent();

					intent.setClass(getActivity(), BlueToothTest.class);
//	                intent.setClass(getActivity(), TestActivity.class);

					EditText FileNmaeEditText = (EditText)view.findViewById(R.id.editText1);
					//String openfilename = "/mnt/sdcard/ComLang1_raw.sco"; // 锟斤拷锟侥硷拷锟斤拷	// editText.getText().toString();
					String openfilename = FileNmaeEditText.getText().toString();
					Bundle bundle=new Bundle();
					//bundle.putString("openfilename", "锟斤拷一锟斤拷activity锟斤拷锟斤拷锟斤�?);
					bundle.putString("openfilename", openfilename);
					intent.putExtras(bundle);

					//intent.putExtra("result", "锟斤拷一锟斤拷activity锟斤拷锟斤拷锟斤�?);

					startActivityForResult(intent, 0);
	                /* 锟截闭碉拷前锟斤拷Activity */
					//getActivity().finish();
				}
			});

			/* bluetooth test communication*/
			Button buttonBTTC = (Button)view.findViewById(R.id.buttonBTTestCom);
			buttonBTTC.setOnClickListener(new Button.OnClickListener(){
				@Override
				public void onClick(View arg0) {

					Intent intent = new Intent();

					intent.setClass(getActivity(), BlueToothTestCom.class);
//	                intent.setClass(getActivity(), TestActivity.class);

					EditText FileNmaeEditText = (EditText)view.findViewById(R.id.editText1);
					//String openfilename = "/mnt/sdcard/ComLang1_raw.sco"; // 锟斤拷锟侥硷拷锟斤拷	// editText.getText().toString();
					String openfilename = FileNmaeEditText.getText().toString();
					Bundle bundle=new Bundle();
					//bundle.putString("openfilename", "锟斤拷一锟斤拷activity锟斤拷锟斤拷锟斤�?);
					bundle.putString("openfilename", openfilename);
					intent.putExtras(bundle);

					//intent.putExtra("result", "锟斤拷一锟斤拷activity锟斤拷锟斤拷锟斤�?);

					startActivityForResult(intent, 0);
	                /* 锟截闭碉拷前锟斤拷Activity */
					//getActivity().finish();
				}
			});

			/* bluetooth test communication*/
			Button buttonBTTC2 = (Button)view.findViewById(R.id.buttonBTTestCom2);
			buttonBTTC2.setOnClickListener(new Button.OnClickListener(){
				@Override
				public void onClick(View arg0) {

					Intent intent = new Intent();

					intent.setClass(getActivity(), BTClient2.class);
//	                intent.setClass(getActivity(), TestActivity.class);

					EditText FileNmaeEditText = (EditText)view.findViewById(R.id.editText1);
					//String openfilename = "/mnt/sdcard/ComLang1_raw.sco"; // 锟斤拷锟侥硷拷锟斤拷	// editText.getText().toString();
					String openfilename = FileNmaeEditText.getText().toString();
					Bundle bundle=new Bundle();
					//bundle.putString("openfilename", "锟斤拷一锟斤拷activity锟斤拷锟斤拷锟斤�?);
					bundle.putString("openfilename", openfilename);
					intent.putExtras(bundle);

					//intent.putExtra("result", "锟斤拷一锟斤拷activity锟斤拷锟斤拷锟斤�?);

					startActivityForResult(intent, 0);
	                /* 锟截闭碉拷前锟斤拷Activity */
					//getActivity().finish();
				}
			});

			/* Auido Video communication*/
			Button buttonAV = (Button)view.findViewById(R.id.buttonAVideo);
			buttonAV.setOnClickListener(new Button.OnClickListener(){
				@Override
				public void onClick(View arg0) {

					Intent intent = new Intent();

					intent.setClass(getActivity(), MainGUI.class);
//	                intent.setClass(getActivity(), TestActivity.class);

					EditText FileNmaeEditText = (EditText)view.findViewById(R.id.editText1);
					//String openfilename = "/mnt/sdcard/ComLang1_raw.sco"; // 锟斤拷锟侥硷拷锟斤拷	// editText.getText().toString();
					String openfilename = FileNmaeEditText.getText().toString();
					Bundle bundle=new Bundle();
					//bundle.putString("openfilename", "锟斤拷一锟斤拷activity锟斤拷锟斤拷锟斤�?);
					bundle.putString("openfilename", openfilename);
					intent.putExtras(bundle);

					//intent.putExtra("result", "锟斤拷一锟斤拷activity锟斤拷锟斤拷锟斤�?);

					startActivityForResult(intent, 0);
	                /* 锟截闭碉拷前锟斤拷Activity */
					//getActivity().finish();
				}
			});

			/* buttonCameraTest*/
			Button buttonCameraTest = (Button)view.findViewById(R.id.buttonCameraTest);
			buttonCameraTest.setOnClickListener(new Button.OnClickListener(){
				@Override
				public void onClick(View arg0) {

					Intent intent = new Intent();

					intent.setClass(getActivity(), Camera2Test.class);
//	                intent.setClass(getActivity(), TestActivity.class);

					EditText FileNmaeEditText = (EditText)view.findViewById(R.id.editText1);
					//String openfilename = "/mnt/sdcard/ComLang1_raw.sco"; // 锟斤拷锟侥硷拷锟斤拷	// editText.getText().toString();
					String openfilename = FileNmaeEditText.getText().toString();
					Bundle bundle=new Bundle();
					//bundle.putString("openfilename", "锟斤拷一锟斤拷activity锟斤拷锟斤拷锟斤�?);
					bundle.putString("openfilename", openfilename);
					intent.putExtras(bundle);

					//intent.putExtra("result", "锟斤拷一锟斤拷activity锟斤拷锟斤拷锟斤�?);

					startActivityForResult(intent, 0);
	                /* 锟截闭碉拷前锟斤拷Activity */
					//getActivity().finish();
				}
			});

			/* USB Camera*/
			Button buttonUSBCam = (Button)view.findViewById(R.id.buttonUVCWebCam);
			buttonUSBCam.setOnClickListener(new Button.OnClickListener(){
				@Override
				public void onClick(View arg0) {

					Intent intent = new Intent();

					intent.setClass(getActivity(), UvcWebCamApp.class);
//	                intent.setClass(getActivity(), TestActivity.class);

					EditText FileNmaeEditText = (EditText)view.findViewById(R.id.editText1);
					//String openfilename = "/mnt/sdcard/ComLang1_raw.sco"; // 锟斤拷锟侥硷拷锟斤拷	// editText.getText().toString();
					String openfilename = FileNmaeEditText.getText().toString();
					Bundle bundle=new Bundle();
					//bundle.putString("openfilename", "锟斤拷一锟斤拷activity锟斤拷锟斤拷锟斤�?);
					bundle.putString("openfilename", openfilename);
					intent.putExtras(bundle);

					//intent.putExtra("result", "锟斤拷一锟斤拷activity锟斤拷锟斤拷锟斤�?);

					startActivityForResult(intent, 0);
	                /* 锟截闭碉拷前锟斤拷Activity */
					//getActivity().finish();
				}
			});
			
			/* eLearning */
			Button buttoneLearning = (Button)view.findViewById(R.id.buttoneLearning);
			buttoneLearning.setOnClickListener(new Button.OnClickListener(){
				@Override  
	            public void onClick(View arg0) {  
	                Intent intent = new Intent();  

	                intent.setClass(getActivity(), PianoELearning.class);

	                EditText FileNmaeEditText = (EditText)view.findViewById(R.id.editText1);
	                 String openfilename = FileNmaeEditText.getText().toString();
					Bundle bundle=new Bundle();  
					bundle.putString("openfilename", openfilename);  
					intent.putExtras(bundle);  
				
	                startActivityForResult(intent, 0);  
	                /* 锟截闭碉拷前锟斤拷Activity */  
	                //getActivity().finish();
				}
			});		
			
			/* audiorecord */
			Button buttonaudiorecord = (Button)view.findViewById(R.id.buttonaudiorecord);
			buttonaudiorecord.setOnClickListener(new Button.OnClickListener(){
				@Override  
	            public void onClick(View arg0) {  
	                Intent intent = new Intent();  

	                intent.setClass(getActivity(), TestAudioRecord.class);

	                EditText FileNmaeEditText = (EditText)view.findViewById(R.id.editText1);
	                 String openfilename = FileNmaeEditText.getText().toString();
					Bundle bundle=new Bundle();  
					bundle.putString("openfilename", openfilename);  
					intent.putExtras(bundle);  
				
	                startActivityForResult(intent, 0);  
	                //getActivity().finish();
				}
			});	
		}
		
		return view;
	}
}

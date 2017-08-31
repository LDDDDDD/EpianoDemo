package com.epiano.commutil;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.format.Time;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.epiano.uvc.UvcWebCam;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class UvcWebCamApp extends Activity {
    private ImageView mImag;
//    private int width = 320;
//    private int height = 240;
    private int width = 640;
    private int height = 480;
    private String devname = "/dev/video4"; //3";
    //private String devname = "/dev/video9"; //3";
    //private String devname = "/dev/video9"; //3";
    private byte[] mdata;
    private Handler mHandler;
    private int numbuf = 0;
    private int index = 0;
    private int ret = 0;
    private int ctype = 1;//0 is zc301 1 is uvc camera
    public Button mcap;
    private Bitmap bitmap;
    private Bitmap bmp;
    private int[] rgb;
    
    UvcWebCam Fimcgzsd;	// jni
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
          WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.uvcwebcam_main);
        
        Fimcgzsd = new UvcWebCam(this);
        
        mImag = (ImageView)findViewById(R.id.mimg);
        mcap = (Button)findViewById(R.id.mcap);
        bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        numbuf  = 4;
        mdata = new byte[width * height * numbuf];
        rgb = new int[width * height * numbuf];
        ret = Fimcgzsd.open(devname.getBytes());
        if(ret < 0)
        {
        	Toast.makeText(this, "Can't open camera.", 200).show();
            finish();
        }
        ret = Fimcgzsd.init(width, height, numbuf,ctype);
        if(ret < 0)
        {
        	Toast.makeText(this, "Can't set camera.", 200).show();
            finish();
        }
        ret = Fimcgzsd.streamon();
        if(ret < 0)
        {
        	Toast.makeText(this, "Can't open camera stream.", 200).show();
            finish();
        }
        
        try {

				Thread.sleep(500); // 10);
		} catch (InterruptedException e) {

			e.printStackTrace();
		}
        
        mHandler = new Handler();
        new StartThread().start();
        mcap.setOnClickListener(new CaptureListener());
    }
 
    final Runnable mUpdateUI = new Runnable() {
 
        @Override
        public void run() {
            // TODO Auto-generated method stub
            mImag.setImageBitmap(bitmap);
             
        }
    };
     
    class StartThread extends Thread {
 
        @Override
        public void run() {
            // TODO Auto-generated method stub
            //super.run();
            while(true) {
                if(ctype == 1){
                    index = Fimcgzsd.dqbuf(mdata);
                    if((index < 0) || (mdata == null)) {
                        onDestroy();
                        break;
                    }
                    Fimcgzsd.pixeltobmp(bmp);
                    mHandler.post(mUpdateUI);
                    bitmap = bmp;
                    Fimcgzsd.qbuf(index);
                    //Fimcgzsd.yuvtorgb(mdata, rgb);
                    //mHandler.post(mUpdateUI);
                    //bitmap = Bitmap.createBitmap(rgb,width,height,Bitmap.Config.ARGB_8888);
                    //Fimcgzsd.qbuf(index);
                } else {
                    index = Fimcgzsd.dqbuf(mdata);
                    if(index < 0) {
                        onDestroy();
                        break;
                    }
                    bitmap = BitmapFactory.decodeByteArray(mdata, 0, width * height);
                    mHandler.post(mUpdateUI);
                    Fimcgzsd.qbuf(index);
                }
            }
        }
    }
     
    public static void saveMyBitmap(Bitmap mBitmap) {
        Time mtime = new Time();
        mtime.setToNow();
        File fdir = new File(Environment.getExternalStorageDirectory().getPath() + "/DCIM/" + "/gzsd/");
        if(!fdir.exists()) {
            fdir.mkdir();
        }
        File f = new File(Environment.getExternalStorageDirectory().getPath() + "/DCIM/" + "/gzsd/" + mtime.year + mtime.month + mtime.monthDay + mtime.hour + mtime.minute +mtime.second+".png");
        try {
                f.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        FileOutputStream fOut = null;
        try {
                fOut = new FileOutputStream(f);
                mBitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
                fOut.flush();
                fOut.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
 
    }
     
    class CaptureListener implements OnClickListener{
 
        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            //new CapThread().start();
            //Fimcgzsd.streamoff();
            saveMyBitmap(bitmap);
            //Fimcgzsd.streamon();
            Toast.makeText(UvcWebCamApp.this, "Capture Successfully", Toast.LENGTH_SHORT).show();
        }   
    }
     
    class CapThread extends Thread {
 
        @Override
        public void run() {
            // TODO Auto-generated method stub
            //super.run();
            saveMyBitmap(bitmap);
            Toast.makeText(UvcWebCamApp.this, "Capture Successfully", Toast.LENGTH_LONG).show();
        }
         
    }
     
    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        Fimcgzsd.release();
        finish();
    }
 
    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        Fimcgzsd.release();
        finish();
    }
 
    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        Fimcgzsd.release();
    }
 
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        finish();
        return true;
    }
 
}
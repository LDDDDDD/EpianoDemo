package com.epiano.commutil;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap.Config;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;

//import com.xf.pageviewtest.R;


public class MusicScoreBook extends Activity {

	Config BmpConfig = Config.ARGB_8888;
	//Config BmpConfig = Bitmap.Config.RGB_565;
	String ImgFileSurfix = ".bmp";
	
	private PicView PicView1;// = (PicView)this.findViewById(R.id.PicView1);
	private Timer mTimer;
//	private MyTimerTask mTimerTask;
	private int timercounter;
	private PicView pv[];
	private int PicViewCount = 0;
	
	int PicViewWidthOrg = 0;					// ��ʼ����/ͼ����
	int PicViewHeightOrg = 0;
	int PicViewWidthCur = PicViewWidthOrg;		// ��ǰ��ͼ���ڿ��
	int PicViewHeightCur = PicViewHeightOrg;
	int WinWidthCur = PicViewWidthOrg;			// ��ǰ�����ڿ��
	int WinHeightCur = PicViewHeightOrg;
	int ImgWidth = PicViewWidthOrg;				// JNIͼ����
	int ImgHeight = PicViewHeightOrg;
	float ImgAndViewK = 1;						// JNI��ͼ������View��ȵı�ֵ
	float whK = (float)1.6;						// ͼ�񳤿��
	
	//int PicViewLastMouseMovePageId = -1;	// ��һ��pageid�������Ż���ͼʱ������ͼ��ҳ��, -1��Ч
	
	int onConfigurationChangedflag = 0;
	
	int EPianoAndroidJavaAPI_inited = 0;
	EPianoAndroidJavaAPI ObEPianoAndroidJavaAPI;
	
//	private LinearLayout ll;
//	LinearLayout.LayoutParams lp;
//	RelativeLayout.LayoutParams lp;
//	private TableLayout ll;
//	TableLayout.LayoutParams lp;
//	LinearLayout.LayoutParams lp2;
	GridView mGrid;
    GridLayout ll;
    GridView.LayoutParams lp;
    
	float BaseDistanceOf2Fingers = 0; 
	float mScale = 1;
	float mCurrentScale = 1;  
	float last_x = -1;  
	float last_y = -1;  
	float delta_x = 0;  
	int zoomed = 0;
	int mZoomStatus = 0;
	int r = 0;
	
	String openfilename;
	
	CNoteImgSet mNoteImgSet;

//	public class PageWidgetAdapter extends BaseAdapter {
//
//		private Context mContext;
//		private int count;
//		private LayoutInflater inflater;
//		private Integer[] imgs = { R.drawable.photo1, R.drawable.photo2, R.drawable.photo3,
//				  R.drawable.photo4, R.drawable.photo5, R.drawable.photo6};
//		
//		public PageWidgetAdapter(Context context) {
//			mContext = context;
//			inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//			count = (int) Math.ceil(imgs.length/2.0);
//		}
//		@Override
//		public int getCount() {
//			// TODO Auto-generated method stub
//			return count;
//		}
//
//		@Override
//		public Object getItem(int position) {
//			// TODO Auto-generated method stub
//			return imgs[position];
//		}
//
//		@Override
//		public long getItemId(int position) {
//			// TODO Auto-generated method stub
//			return position;
//		}
//
//		@Override
//		public View getView(int position, View convertView, ViewGroup parent) {
//			// TODO Auto-generated method stub
//			ViewGroup layout;
//			if(convertView == null) {
//				layout = (ViewGroup) inflater.inflate(R.layout.item_layout, null);
//			} else {
//				layout = (ViewGroup) convertView;
//			}
//			setViewContent(layout, position);
//			
//			return layout;
//		}
//		
//		private void setViewContent(ViewGroup group, int position) {
//			TextView text = (TextView) group.findViewById(R.id.item_layout_leftText);
//			text.setText(String.valueOf(position*2+1));
//			ImageView image = (ImageView) group.findViewById(R.id.item_layout_leftImage);
//			image.setImageResource(imgs[position*2]);
//			text = (TextView) group.findViewById(R.id.item_layout_rightText);
//			text.setText(String.valueOf(position*2+2));
//			image = (ImageView) group.findViewById(R.id.item_layout_rightImage);
//			image.setImageResource(imgs[position*2+1]);
//		}
//
//	}
	public class PageWidgetAdapter extends BaseAdapter {
  	
		private Context mContext;
		private int count;
		private LayoutInflater inflater;
		private Integer[] imgs = { R.drawable.photo1, R.drawable.photo1, R.drawable.photo1,
				  R.drawable.photo1, R.drawable.photo1, R.drawable.photo1};
	
        public PageWidgetAdapter(Context context) {
	  		mContext = context;
	  		inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	  		count = (int) Math.ceil(imgs.length/2.0);    	  
        }
        
        int selectItem = -1;  

        public View getView(int position, View convertView, ViewGroup parent) {
            PicView i;
            
//            System.out.println("AppsAdapter, position is " + position + ", convertView is " + convertView + ", pv[position].iPageId is " + pv[position].iPageId + ".");

//            if (convertView == null) {
////                //i = new ImageView(Grid1.this);
////            	//i = new PicView(MainActivity.this, position, PicViewWidthOrg, PicViewHeightOrg);
////            	i = pv[position];
////                //i.setScaleType(ImageView.ScaleType.FIT_CENTER);
////                i.setLayoutParams(new GridView.LayoutParams(PicViewWidthOrg, PicViewHeightOrg));
//////                i.setScaleX((float)0.5);
//////                i.setScaleY((float)0.5);
////            	i.setAdjustViewBounds(false);
////                i.setScaleType(ImageView.ScaleType.CENTER_CROP);
//            	
//            	i = new ImageView(MainActivity.this);
//                i.setScaleType(ImageView.ScaleType.FIT_CENTER);
//                i.setLayoutParams(new GridView.LayoutParams(50, 50));
//                
//            } else {
//                i = (PicView) convertView;
//            }
//
//            ResolveInfo info = mApps.get(position);
//            i.setImageDrawable(info.activityInfo.loadIcon(getPackageManager()));
            
//            int iPageId = position;
//            i = pv[iPageId];
//
//            return i;
            
            //if (position == 0)
            {
            	ViewGroup layout;
    			if(convertView == null) {
    				layout = (ViewGroup) inflater.inflate(R.layout.item_layout, null);
    			} else {
    				layout = (ViewGroup) convertView;
    			}
    			setViewContent(layout, position);
    			
    			return layout;
            }
            
            //return  pv[position];
        }

        public void setSelection(int position)  
        {  
            selectItem = position;  
        } 
        
        public final int getCount() {
            return PicViewCount; // mApps.size();
        }

        public final Object getItem(int position) {
            return position; //mApps.get(position);
        }

        public final long getItemId(int position) {
            return position;
        }
        
		private void setViewContent(ViewGroup group, int position) {
			TextView text = (TextView) group.findViewById(R.id.item_layout_leftText);
			text.setText(String.valueOf(position*2+1));
	//		ImageView image = (ImageView) group.findViewById(R.id.item_layout_leftImage);
	//		image.setImageResource(imgs[position*2]);
			ImageView image = (ImageView) group.findViewById(R.id.item_layout_leftImage);
			image.setImageResource(imgs[position*2]);
			text = (TextView) group.findViewById(R.id.item_layout_rightText);
			text.setText(String.valueOf(position*2+2));
	//		image = (ImageView) group.findViewById(R.id.item_layout_rightImage);
	//		image.setImageResource(imgs[position*2+1]);
			image = (ImageView) group.findViewById(R.id.item_layout_rightImage);
			image.setImageResource(imgs[position*2+1]);
		}
    }	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
		requestWindowFeature(Window.FEATURE_NO_TITLE);//���ر�����    
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,   
		WindowManager.LayoutParams.FLAG_FULLSCREEN);//����ȫ�� 		
    	
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.musicscorebook);
        setContentView(R.layout.activity_book);
        
        mNoteImgSet = new CNoteImgSet(this);
        
        // ��ȡ�ļ���
		Intent intent=getIntent(); 
		openfilename = intent.getStringExtra("openfilename");
		
		pv = new PicView[100];
		
		if (EPianoAndroidJavaAPI_inited == 0)
		{
			EPianoAndroidJavaAPI_inited = 1;
			
			ObEPianoAndroidJavaAPI = new EPianoAndroidJavaAPI(this);
			//ObEPianoAndroidJavaAPI.StartEngine();
			
			// ��������
			ObEPianoAndroidJavaAPI.StartEngine();
		}
		
		if (true)
		{
			RenderViews();
		}
		
//		Button buttonPlay = (Button)findViewById(R.id.imageButtonPlay);
//		/* ����button���¼���Ϣ */  
//        buttonPlay.setOnClickListener(new Button.OnClickListener(){  
//            @Override  
//            public void onClick(View arg0) {
//            	
//            	int PlayNoteSet[] = ObEPianoAndroidJavaAPI.GetPlayNoteSet();
//            	
//            	int DataCheckTag = PlayNoteSet[0];
//            	int DataLen = PlayNoteSet[1];
//            	
//            	int BarId_TSId[] 	= new int [1000];
//            	int pitch[] 		= new int [1000];
//            	int freqenceInt;
//            	float freqence[] 	= new float [1000];
//            	int durationInt;
//            	float duration[] 	= new float [1000];
//            	int strength[] 		= new int [1000];
//            	
//            	int dataidx = 0;
//            	for(int i = 0; i < DataLen && dataidx < 1000; dataidx++)
//            	{
//            		BarId_TSId[dataidx] 	= PlayNoteSet[i++];
//            		pitch[dataidx] 			= PlayNoteSet[i++];
//            		freqenceInt				= PlayNoteSet[i++];
//            		durationInt				= PlayNoteSet[i++];
//            		strength[dataidx]		= PlayNoteSet[i++];
//            		
//            		freqence[dataidx] = (float)(freqenceInt) / 100;	// ԭʼ���ݱ��Ŵ�100������
//            		duration[dataidx] = (float)(durationInt) / 100;	// ԭʼ���ݱ��Ŵ�100������
//            	}
//            	
//            	
//            }  
//        });  
		
        
//        MusicScoreBookPage page = (MusicScoreBookPage) findViewById(R.id.main_pageWidget);
//        BaseAdapter adapter = new PageWidgetAdapter(this);
//        page.setAdapter(adapter);
    }
    
    public boolean RenderViews()
	{
		// ���ò���������
		WindowManager m = getWindowManager();    
	    Display d = m.getDefaultDisplay();  //Ϊ��ȡ��Ļ����   
	    PicViewWidthOrg = d.getWidth();
	    PicViewHeightOrg = (int)(d.getHeight() * 0.8);
	    PicViewHeightOrg = (int)(PicViewWidthOrg * whK);
		PicViewWidthCur = PicViewWidthOrg;
		PicViewHeightCur = PicViewHeightOrg;
		WinWidthCur = PicViewWidthOrg;
		WinHeightCur = PicViewHeightOrg;			
		
		ImgWidth = (int)(PicViewWidthOrg * ImgAndViewK);
		ImgHeight = (int)(PicViewHeightOrg * ImgAndViewK);

//		int width = display.getWidth();
//		int height = display.getHeight();
		int leftmagin = PicViewWidthOrg / 35; //30;
		int topmagin = leftmagin * 2;
		int rightmagin = leftmagin;
		int bottommagin = leftmagin * 2;
		int backgroundclr = 0xfffae9;
		r = ObEPianoAndroidJavaAPI.SetWinInfo(ImgWidth, ImgHeight, leftmagin, topmagin, rightmagin, bottommagin, backgroundclr);
		if (r <= 0)
		{
			Toast.makeText(this, "SetWinInfo fail.", 200).show();
			return false;
		}
		
		// ��������demo
		if (openfilename.equals("demo"))
		{
			r =ObEPianoAndroidJavaAPI.DemoFile();	// ����demo
			if (r <= 0)
			{
				Toast.makeText(this, "Can't open demo.", 200).show();
				return false;
			}
		}
		else
		{
			//String openfilename = "/mnt/sdcard/ComLang1_raw.sco";
			r = ObEPianoAndroidJavaAPI.OpenFile(openfilename);		// ��ָ���ļ�
			if (r <= 0)
			{
				Toast.makeText(this, "Can't open " + openfilename + ".", 200).show();
				return false;
			}
		}
		
		System.out.println("Start Paint...");
		
		// �������ɻ���ָ��
		r = ObEPianoAndroidJavaAPI.NotifyPaint();
		if (r <= 0)
		{
			Toast.makeText(this, "Paint fail.", 200).show();
			return false;
		}
		
		// ��ѯҳ��
		PicViewCount = ObEPianoAndroidJavaAPI.QueryPageCount();			
		System.out.println("PicViewCount = " + PicViewCount); // print
		
		//ll.setColumnStretchable(columnIndex, isStretchable);
		
		// ���javaҳ
//		int PicViewCountLocal = PicViewCount;
//		lp = new GridView.LayoutParams(PicViewWidthOrg, PicViewHeightOrg);
		for(int i = 0; i < PicViewCount; i++)
		{
			pv[i] = new PicView(MusicScoreBook.this, BmpConfig, ImgFileSurfix, mNoteImgSet, i, PicViewWidthOrg, PicViewHeightOrg, ImgWidth, ImgHeight);
//			//pv[i].setPadding(0, 20, 0, 20);				
//            pv[i].setLayoutParams(lp);//���ò��ֲ��� 
//            //pv[i].setClickable(true);
			
//			ll.addView(pv[i]);
//			
////			lp = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, PicViewHeightOrg); 
//			lp = new LinearLayout.LayoutParams(PicViewWidthOrg, PicViewHeightOrg);
////			lp = new RelativeLayout.LayoutParams(PicViewWidthOrg, PicViewHeightOrg);
////			lp2 = new LinearLayout.LayoutParams(PicViewWidthOrg, PicViewHeightOrg);
//
//			pv[i].setLayoutParams(lp);//���ò��ֲ��� 
			
			//////////////
			if (i > 0)
			{
				//continue;
			}
			System.out.println("the view " + i + " is clickable " + pv[i].isClickable());

			//pv[i].setClickable(true);
		}
		
		  MusicScoreBookPage page = (MusicScoreBookPage) findViewById(R.id.main_pageWidget);
	      BaseAdapter adapter = new PageWidgetAdapter(this);
	      page.setAdapter(adapter);
//        mGrid = (GridView) findViewById(R.id.GridView1);
//        mGrid.setAdapter(new AppsAdapter());	        
//        mGrid.setColumnWidth(PicViewWidthOrg);
//		  mGrid.setOnItemClickListener(new OnItemClickListener() {
//			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
//				//Toast.makeText(MainActivity.this, "mGrid.setOnItemClickListener: pic" + (position + 1), Toast.LENGTH_SHORT).show();
//				
//
//            	// process after picview onTouchEvent(){ iMouseInPage = 1;}		            	
//            	
////            	if (onConfigurationChangedflag == 1)
////            	{
////            		onConfigurationChangedflag = 0;
////            		ProcessZoom(BaseDistanceOf2Fingers);
////            	}
//
//            	// find Mouse In which Page: foundPv
//            	int j = position;
//            	int jbak = 0;
//            	int foundPv = -1;
////				for(j = 0; j < PicViewCount; j++)
////				{
////					if (pv[j].iMouseInPage > 0)
////					{
////						break;
////					}
////				}
//				
//				mScale = (float)pv[j].getWidth() / PicViewWidthOrg;
//				
//				if (j < PicViewCount)
//				{
//					System.out.println("onClick------->>iPageId: " + j);
//					
//					//pv[j].mScale = mScale;
//					
//					foundPv = j;
//					//ObEPianoAndroidJavaAPI.OnMouseMove(j, pv[j].iXInPage, pv[j].iYInPage);
//					if (pv[j].mScale > 0)
//					{
//						float scalesum = pv[j].mScale / ImgAndViewK;
//						if (pv[j].mScale <= 1)
//						{
//							// ��С���������������
//							ObEPianoAndroidJavaAPI.OnMouseMove(j, (int)(pv[j].iXInPage / scalesum), (int)(pv[j].iYInPage / scalesum));
//						}
//						else
//						{
//							// �Ŵ���������Ϊ���һ������ڲ�û��ʹ�������scrollview���ܣ�����ͨ��ͼ��ƽ�Ʒ�ʽ��ʵ�ֵģ������������ʱ�ֹ�������pv[j].OffsetX(����x���򻬶���)���ء�
//							ObEPianoAndroidJavaAPI.OnMouseMove(j, (int)((pv[j].iXInPage + pv[j].OffsetX) / scalesum), (int)(pv[j].iYInPage / scalesum));
//						}
//						
//					}
//					else
//					{
//						System.out.println("setOnClickListener, error, pv[" + j + "].mScale is 0.");
//					}
//				}
//				
//				// ָ�껭�����
//				if (true)
//				{
//					int MouseLinePara[];
//					MouseLinePara = ObEPianoAndroidJavaAPI.QueryMouseLine();
//					if (MouseLinePara.length != 5)
//					{
//						System.out.println("setOnClickListener, error, MouseLinePara.leng is " + MouseLinePara.length);
//					}
//					else
//					{
//						// clear mouse line in all pages
//						for(j = 0; j < PicViewCount; j++)
//						{									
//							pv[j].mMouseTimeSlice_iPageId = -1;
//							
//							//pv[j].mScale = mScale;
//						}
//						
//						// set mouseline in page
//						int iPageId = MouseLinePara[0];
//						if (iPageId >= 0 && iPageId < PicViewCount)
//						{
//							pv[iPageId].mMouseTimeSlice_iPageId 	= MouseLinePara[0];	// -1 ��Ч
//							pv[iPageId].mMouseTimeSlice_iBarId 		= MouseLinePara[1];
//							pv[iPageId].mMouseTimeSlice_xInPage 	= MouseLinePara[2];
//							pv[iPageId].mMouseTimeSlice_y0InPage 	= MouseLinePara[3];
//							pv[iPageId].mMouseTimeSlice_y1InPage 	= MouseLinePara[4];
//							
//							pv[iPageId].postInvalidate(); //(0, 0, pv[iPageId]., PicViewHeightCur);
//						}
//						
//						// refresh win
//						for(j = 0; j < PicViewCount; j++)
//						{
//							pv[j].postInvalidate(); //0, 0, PicViewWidthCur, PicViewHeightCur);
//						}
//					}
//				}
//            
//			}
//		});


		// ��ѯ��ͼָ��
		byte drawoderset[];
		for(int i = 0; i < PicViewCount; i++)
		{
			drawoderset = ObEPianoAndroidJavaAPI.QueryDrawOderSet(i);
			//if (drawoderset.length > 0)
			{
				pv[i].DrawOderSet = new byte[drawoderset.length];
				pv[i].DrawOderSet = drawoderset;
			}
			if (drawoderset.length > 0)
			{
				pv[i].ReDraw = 1;
				//pv[i].drawwgFromMem();
			}
		}

		// ֪ͨjava����
		for(int i = 0; i < PicViewCount; i++) // 
		{
			pv[i].render();
			pv[i].postInvalidate(); //0, 0, PicViewWidthOrg, PicViewHeightOrg);
		}

		// over
		System.out.println("fresh PicView over.");
			
		return true;
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.activity_main, menu);
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    
}

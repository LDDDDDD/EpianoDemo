package com.epiano.commutil;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


@SuppressWarnings("ResourceType")
public class MySurfaceView extends GLSurfaceView
{

	Config BmpConfig = Config.ARGB_8888;
	//Config BmpConfig = Bitmap.Config.RGB_565;
	String ImgFileSurfix = ".png";
	int RecycleBmpSwitch = 1;	// 回收bmp, cfg...
	int OpenGl_2N_switch = 0; // 1;	// opengl纹理边长满足2n开关, cfg...

	int LoadAllTextureSwitch = 0;		// 一次性加载所有图片开关（一次性全加载会影响启动速度）, cfg...

	public float screenWidth;//屏幕宽带
	public float screenHeight;//屏幕高度
	public float ratio;//屏幕宽高比
	private final float TOUCH_SCALE_FACTOR = 180.0f/320;//角度缩放比例
	private SceneRenderer mRenderer;//场景渲染器

	float bookdistance = 0;

	float mPreviousX;//上次按下位置x坐标
	float mPreviousY;//上次按下位置y坐标
	long previousTime;//上次按下的时间
	boolean isCheck=false;//是否点击查看图片
	boolean isMove=false;

	//int[] textureIds=new int[PHOTO_COUNT];//n张照片纹理id数组
	int[] textureIds;//n张照片纹理id数组, 下标0对应的是正面封面
	float yAngle=0;//总场景旋转角度
	int currIndex=0;//当前选中的索引
	float yAngleV=0;//总场景角度变化速度
	public float yAngleWg=0;//总场景旋转角度
	int MoveJudgeDistance = 10;	// 判断是否算做移动的最小距离 cfg...

	// 鼠标线
	int MouseLineX = 0;
	int MouseLineY0 = 0;
	int MouseLineY1 = 0;
	int MousePageId = -1;	// 单页号, 非纸号
	int MousePaperId = -1;	// 纸
	int MousePageLeftOrRight = -1; // 0:left, 1:right

	// 播放线 play line
	int PlayLineX = 0;
	int PlayLineY0 = 0;
	int PlayLineY1 = 0;
	public int PlayPageId = -1;	// 单页号, 非纸号	//
	int PlayPaperId = -1;	// 纸			//
	public int PlayPageLeftOrRight = -1; // 0:left, 1:right

	EPianoAndroidJavaAPI ObEPianoAndroidJavaAPI;

	PicView pv[];		// 单面页
	int PicViewCount = 0;
	int paperCount = 0;	// 纸页, = 1 + (PicViewCount - 1 ) / 2 + 1;	 // 首页在b面, 未页...,
	float Photo_Angle_Span = 0;

	//int angleLeft[];
	//int angleRight[];
	//int ScrollPaperCount = 0;
	int paperAngle[];

	long FallDur = 300; //500; //1300; // ms cfg...

	//
	int leftPaperId = 0;		// 左边未悬空最上面页号
	int rightPaperId = 1;		// 右边未悬空最上面页号
	//int leftPaperId_Pre = 0;	// 上一次的
	// int rightPaperId_Pre = 1;
	int GrabPaperId = -1;	// 当前抓住的一页
	int GrabPaperIdDup = GrabPaperId;
	int leftPaperIdDup = leftPaperId;
	int rightPaperIdDup = rightPaperId;
	int paperSpan = 45; //30; //45; //20; //30;		// 纸音的角度, cfg ...
	long scrollpageStartTime = 0;	// 开始页面滚动时间
	long scrollpageFallTime = 0;	// 自由落体开始时间
	enum SCROLL_PAGE_STATUS			// 翻连续页状态控制
	{
		SCROLL_PAGE_NULL,
		SCROLL_PAGE_STARTED,		// 滑动中
		SCROLL_PAGE_FINGER_OFF,		// 滑动结束, 页自由落体
		SCROLL_PAGE_BUT,			// 结
	}
	SCROLL_PAGE_STATUS ScrollPageStatus = SCROLL_PAGE_STATUS.SCROLL_PAGE_NULL;
	boolean KeyUPEvent = false;
	public enum SCROLL_PAGE_MOD			// 翻转模式
	{
		SCROLL_PAGE_MODE_NULL,
		SCROLL_PAGE_MODE_MULTI_PAGES,	// 多页
		SCROLL_PAGE_MODE_SINGLE_PAGE,	// 单页
	}
	public SCROLL_PAGE_MOD ScrollPageMode = SCROLL_PAGE_MOD.SCROLL_PAGE_MODE_MULTI_PAGES;

	enum SCROLL_DIRECTION			// 翻转方向
	{
		SCROLL_PAGE_DIR_NULL,
		SCROLL_PAGE_DIR_LEFT,			//
		SCROLL_PAGE_DIR_RIGHT,			//
	}
	SCROLL_DIRECTION ScrollPageDir = SCROLL_DIRECTION.SCROLL_PAGE_DIR_NULL;

	enum TEXTURE_MOD				// 纹理生成模式
	{
		TEXTURE_MOD_UNIQUE,						// 全局唯一，　即每页一个纹理, 浪费空间
		TEXTURE_MOD_START_FROM_0_EACH_TIME,		// 固定纹理总数, 每次从0号纹理开始重新填充纹理图像，浪费时间, 连续翻页有些卡
		TEXTURE_MOD_MAP,						// 固定纹理总数, 建立纹理号和页的映射，每次只填充新加入的页，老化旧的纹理
	}
	TEXTURE_MOD TextTureMod = TEXTURE_MOD.TEXTURE_MOD_MAP; //TEXTURE_MOD_START_FROM_0_EACH_TIME; //TEXTURE_MOD_UNIQUE; //TEXTURE_MOD_START_FROM_0_EACH_TIME;	// cfg...

	enum PAGE_CACH_MOD				// 页缓冲模式
	{
		PAGE_CACH_BMP_FILE_AND_BITMAP,			// 生成位图文件, 预加载Bitmap
		PAGE_CACH_BMP_FILE_ONLY,				// 生成位图文件, 不预加载Bitmap
	}
	PAGE_CACH_MOD PageCachMod = PAGE_CACH_MOD.PAGE_CACH_BMP_FILE_ONLY; //PAGE_CACH_BMP_FILE_AND_BITMAP; // PAGE_CACH_BMP_FILE_ONLY; // cfg...


	//初始化纹理
	int MaxTextSetSize = (180 / paperSpan + 1) * 2 + 2; // 6 * 2;
	int ActTextSetSize = 0;
	int mTextureSet[] = new int[MaxTextSetSize + 1]; 	// 索引:从0开始, 值:纹理在gl内部的索引号, 最后一项给pv最后一页的后一个封面页
	int mTextureSet_PvId[] = new int[MaxTextSetSize]; 	// mTextureSet[]每个项目对应的pvId
	int mTextureSetOccupy[] = new int[MaxTextSetSize]; 	// 索引:从0开始, 占用状态
	//int mPvTextureSet[];	// = new int[PicViewCount]; 	// pv对应的mTextureSet[]的索引

	//int mTextureSet_pvId[] = new int[MaxTextSetSize]; 	// 索引:从0开始, 值:纹理在gl内部的索引号
	int leftPaperId_last = -1;
	int rightPaperId_last = -1;
	//int mTextureSet_bak[] = new int[MaxTextSetSize]; 	// 索引:从0开始, 值:纹理在gl内部的索引号
	//int mTexture_pv2Set_Map[] = new int[MaxTextSetSize];						// 索引:pvId, 值:pvID对应在mTextureSet中的索引
	//int mTexture_pv2Set_Map_bak[] = new int[MaxTextSetSize];
//	private ArrayList<int[]> mTexture_pv2Set_Map2 = new ArrayList<int[]>();// pvID对应在mTextureSet中的索引
	//List mTexture_pv2Set_Map2 = new ArrayList();

	boolean OnScrollZoomKSwitch = true;		// 页面滚动中的画面比例控制, cfg...
	float zoomKDest = 0.9f;	//0.8f			// 缩小的目标比例, cfg...
	float zoomDur   = 500;	// 300			// 缩小或放大变化过程的时间长度, ms, cfg...
	float zoomSpeed = (float)(1.0 - zoomKDest) / zoomDur;
	float zoomOrg = 1; //0.5f; //1;
	float zoomK = zoomOrg;					// 连续翻页中进行缩放

	boolean OneTimeOnSingleMode = true;		// SCROLL_PAGE_MODE_SINGLE_PAGE时, true:一次只翻一页, false:只要手指在滑动，就可以连续翻页
	public int 	OneTimeOnSingleModeCount = 0;

	public boolean threadGoOn = true;

	MusicScoreBook3D mMusicScoreBook3D;

	public Board tp;

	public MySurfaceView(Context context, MusicScoreBook3D iMusicScoreBook3D, String ImgFileSurfixIn, int OpenGl_2N_switchIn) {
		super(context);

		mMusicScoreBook3D = iMusicScoreBook3D;
		ImgFileSurfix = ImgFileSurfixIn;
		OpenGl_2N_switch = OpenGl_2N_switchIn;
	}

	public int SetWin(DisplayMetrics dm) {

		screenHeight = dm.heightPixels;
		screenWidth = dm.widthPixels;
		ratio = screenWidth / screenHeight;

		int axisRadias = (int)(screenWidth / 2 * 0.005); //0.02); //0.02);
		int pageW = (int)(screenWidth / 2 - axisRadias);
		tp = new Board(axisRadias, pageW, (int)screenHeight / 2); //用于显示照片的纹理矩形

		return 1;
	}

	// 翻到首页
	public int Rewind() {

		{
			// 停止翻页
			ScrollPageMode = SCROLL_PAGE_MOD.SCROLL_PAGE_MODE_SINGLE_PAGE;
			ScrollPageStatus = SCROLL_PAGE_STATUS.SCROLL_PAGE_NULL;
			yAngleWg = 0;

			// 跳转到首页
			leftPaperId = 0;
			rightPaperId = 1;
		}

		return 1;
	}

	// 创建场景渲染器, 启动旋转线程
	public int SetParam(Context context, Config BmpConfigIn, EPianoAndroidJavaAPI ObEPianoAndroidJavaAPIIn, PicView pvInput[], int PicViewCountInput, DisplayMetrics dm) {
		//super(context);

		BmpConfig = BmpConfigIn;

//      int axisRadias = (int)(screenWidth / 2 * 0.01); //0.02); //0.02);
//      int pageW = (int)(screenWidth / 2 - axisRadias);
//      tp = new Board(axisRadias, pageW, (int)screenHeight / 2); //用于显示照片的纹理矩形
		if (tp == null)
		{
			SetWin(dm);
		}

		ObEPianoAndroidJavaAPI = ObEPianoAndroidJavaAPIIn;

		screenHeight = dm.heightPixels;
		screenWidth = dm.widthPixels;
		ratio = screenWidth / screenHeight;

		pv = pvInput;
		PicViewCount = PicViewCountInput;					// 单面页
		paperCount = 1 + (PicViewCount - 1) / 2 + 1;	// 纸页
		Photo_Angle_Span = 360.0f / paperCount;
		//PHOTO_COUNT = PicViewCount;
		//textureIds=new int[PicViewCount];
		textureIds=new int[paperCount * 2 + 1];
		for(int i = 0; i < paperCount * 2 + 1; i++)
		{
			textureIds[i] = -1;
		}
		//mTexture_pv2Set_Map = new int[PicViewCount];

//        mPvTextureSet = new int[PicViewCount + 2];
//        for(int i = 0; i <PicViewCount + 2; i++)
//        {
//        	mPvTextureSet[i] = -1;
//        }
		for(int i = 0; i < MaxTextSetSize; i++)
		{
			mTextureSet_PvId[i] = -1;
			mTextureSet[i] = -1;
		}
		mTextureSet[MaxTextSetSize] = -1;	// 末尾的封面页


		// 初始文件缓冲器
		pv[0].InitCach();

		// 删除缓存文件
		for (int i = 0; i < PicViewCount; i++) // i is pvId
		{
//			if (pv[i] != null)
//			{
//				pv[i].RemoveCach();
//			}
			RemoveCach(i);
		}

		//angleLeft = new int[paperCount];
		//angleRight = new int[paperCount];
		paperAngle = new int[paperCount + 1];

		mRenderer = new SceneRenderer();	//创建场景渲染器
		setRenderer(mRenderer);				//设置渲染器
		setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);//设置渲染模式为主动渲染


		//启动一个线程根据当前的角速度旋转场景
		//threadWork=true;
		new Thread()
		{
			public void run()
			{
				while(threadGoOn)
				{
					if(Float.isNaN(yAngle)||Float.isNaN(yAngleV))
					{
						throw new RuntimeException("yangle "+yAngle+"yAngleV="+yAngleV);
					}

					//根据角速度计算新的场景旋转角度
					yAngle+=yAngleV;
					if(Float.isNaN(yAngle))
					{
						throw new RuntimeException("yangle"+yAngle);
					}
					//将角度规格化到0～360之间
					//yAngle=(yAngle+360)%360;
					if(Float.isNaN(yAngle)||Float.isNaN(yAngleV))
					{
						throw new RuntimeException("yangle "+yAngle+"yAngleV="+yAngleV);
					}
					//若当前手指已经抬起则角速度衰减
					if(!isMove)
					{
						yAngleV=yAngleV*0.7f;
					}
					//若 角速度小于阈值则归0
					if(Math.abs(yAngleV)<0.05)
					{
						yAngleV=0;
					}

					try
					{
						Thread.sleep(50);
					}
					catch (InterruptedException e)
					{
						e.printStackTrace();
					}
				}
			}
		}.start();


		// 提前准备页面位图线程: 根据当前页面号, 选择前后若干页面进行缓冲, 其它页面可以释放(仅释放内存，不释放文件缓冲),　但缓冲操作最好不在本线程中处理，　而是在绘制线程的后段进行处理
		//threadWork=true;
		new Thread()
		{
			public void run()
			{
				int count = 0;
				while(threadGoOn)
				//if (false)
				{
					count++;

//         			if (count % 100 == 0)
//         			{
//         				Log.i("wg", ""
//    							+ "ValidBmpPv:" + StatValidBmpPvCount()
//    							+ ", ValidTextPv:" + StatValidTexturePvCount()
//    							);
//         			}

					// 左边向上的pvID
					int leftPvId =  2 * leftPaperId + 1;
					int r0 = leftPvId - 2 * 2;					// 当前页前m页
					//int r1 = leftPvId + 2 + 2 * 2;
					int r1 = leftPvId + MaxTextSetSize + 2 * 2; // 当前页后n页
					if (r0 < 0)
					{
						r0 = 0;
					}
					if (r1 >= PicViewCount)
					{
						r1 = PicViewCount - 1;
					}

					// 页面加载
					int pageId = 0;
					int r = 0;
					int loadcount = 0;
					for (int pvId = r0; pvId <= r1; pvId++)
					{
						pageId = pvId2TextPageId(pvId);

						if (pv[pvId] == null)
						{
							mMusicScoreBook3D.CreatePV(pvId);
						}

						if (pv[pvId] == null)
						{
							Log.i("wg", "Create pv fail:" + pvId);

							continue;
						}

						PicView pvdupset[] = pv;
						PicView pvdup = pv[pvId];

						//Log.i("wg", "In --------- synchronized pv:" + pvId);

						if (pv[pvId].mPvTextureId == -1)	// pvId无纹理
						{
							synchronized(pv[pvId]) // 同步
							{
								// PageCachMod, 配置项
								if (PageCachMod == PAGE_CACH_MOD.PAGE_CACH_BMP_FILE_AND_BITMAP)
								{
									if (!pv[pvId].isBmpValid())
									{
										r = pv[pvId].LoadCach();			// 先从文件缓冲加载
										if (r == 0)
										{
											LoadPagesToPicView(pvId, pvId);	// 从jni进行加载
										}

										if (pv[pvId].isBmpValid())
										{
											pv[pvId].Cach();				// mBitmapPeer缓冲到文件

											loadcount++;
											if (loadcount >= 1)				// 间隙性工作
											{
												break;
											}
										}
									}
								}
								else if (PageCachMod == PAGE_CACH_MOD.PAGE_CACH_BMP_FILE_ONLY)
								{
									r = pv[pvId].IsCached();			// 先从文件缓冲加载
									if (r == 0)
									{
										LoadPagesToPicView(pvId, pvId);	// 从jni进行加载

										pv[pvId].Cach();				// mBitmapPeer缓冲到文件

										loadcount++;

										// 为节省内存, 不保存mBitmapPeer
										if (RecycleBmpSwitch > 0)
										{
											if (pv[pvId].isBmpValid())
											{
												pv[pvId].mBitmapPeer.recycle();

//				        						Log.i("wg", "pv:" + pvId + " is recycled, "
//				        							+ "ValidBmpPv:" + StatValidBmpPvCount()
//				        							+ ", ValidTextPv:" + StatValidTexturePvCount()
//				        							);
											}
										}

										if (loadcount >= 1)				// 间隙性工作
										{
											break;
										}

									}
								}
							}
						}

						//Log.i("wg", "Out --------- synchronized pv:" + pvId);
					}

					try
					{
						Thread.sleep(10);	// 100
					} catch (InterruptedException e)
					{
						e.printStackTrace();
					}
				}
			}
		}.start();

		return 1;
	}

	// 统计有合法bmp的pv的个数
	public int StatValidBmpPvCount()
	{
		int c = 0;
		for(int i = 0; i < PicViewCount; i++)
		{
			if (pv[i] != null)
			{
				if (pv[i].isBmpValid())
				{
					c++;
				}
			}
		}

		return c;
	}

	// 统计有合法纹理的pv的个数
	public int StatValidTexturePvCount()
	{
		int c = 0;
		for(int i = 0; i < PicViewCount; i++)
		{
			if (pv[i] != null)
			{
				if (pv[i].mPvTextureId != -1)
				{
					c++;
				}
			}
		}

		return c;
	}

//	/**
//     * 添加引导图片
//     */
//    public void addGuideImage(PicView pvIn[], int id) {
//    	int guideResourceId = 0;
//        //View view = getWindow().getDecorView().findViewById(R.id.my_content_view);//查找通过setContentView上的根布局
//    	PicView view = pvIn[id];
//        if(view == null)
//        {
//        	return;
//        }
////        if(MyPreferences.activityIsGuided(this, this.getClass().getName())){
////            //引导过了
////            return;
////        }
//        ViewParent viewParent = view.getParent();
//        if(viewParent instanceof FrameLayout){
//            final FrameLayout frameLayout = (FrameLayout)viewParent;
//            if(guideResourceId!=0){//设置了引导图片
//                final ImageView guideImage = new ImageView(this);
//                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
//                guideImage.setLayoutParams(params);
//                guideImage.setScaleType(ScaleType.FIT_XY);
//                guideImage.setImageResource(guideResourceId);
//                guideImage.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        frameLayout.removeView(guideImage);
//                        MyPreferences.setIsGuided(getApplicationContext(), BasicActivity.this.getClass().getName());//设为已引导
//                    }
//                });
//                frameLayout.addView(guideImage);//添加引导图片
//
//            }
//        }
//    }


	@Override
	public boolean onKeyDown(int keyCode, KeyEvent e)
	{
		if(keyCode==4)
		{//若按下的是返回键
			if(isCheck)
			{//若在detail场景中则回主场景
				isCheck=false;
			}
			else
			{//若在主场景中则退出程序
				System.exit(0);
			}
		}
		return true;
	}



	public int SetCursor(EPianoAndroidJavaAPI ObEPianoAndroidJavaAPI, int xIn3dView, int yIn3dView)
	{
		float ImgAndViewK = 1;						// JNI中图像宽度与View宽度的比值
		float whK = (float)1.6;						// 图像长宽比

		// 判别鼠标选中是左页还是右页0:左，1:右
		int LeftOrRight = tp.InLeftOrRight(xIn3dView, yIn3dView);
		if (LeftOrRight == -1)
		{
			return 0;
		}

		// 获取页号
		int iPageIdIn = -1;
		if (LeftOrRight == -1)
		{
			return -1;
		}
		else if (LeftOrRight == 0)
		{
			// left

			iPageIdIn = 2 * leftPaperId;
			MousePaperId = leftPaperId;
		}
		else if (LeftOrRight == 1)
		{
			// right

			iPageIdIn = 2 * rightPaperId - 1;
			MousePaperId = rightPaperId;
		}

		int position = iPageIdIn;
		int j = position;
		//int PicViewCount = PicViewCount;

		if (j >= PicViewCount)
		{
			return 0;
		}
		if (pv[j] == null)
		{
			return 0;
		}

		//final float PI = (float)3.14159265;

		//Toast.makeText(MainActivity.this, "mGrid.setOnItemClickListener: pic" + (position + 1), Toast.LENGTH_SHORT).show();

		// process after picview onTouchEvent(){ iMouseInPage = 1;}

//            	if (onConfigurationChangedflag == 1)
//            	{
//            		onConfigurationChangedflag = 0;
//            		ProcessZoom(BaseDistanceOf2Fingers);
//            	}

		// find Mouse In which Page: foundPv
		int jbak = 0;
		int foundPv = -1;

		float mScale = 1;
		//mScale = (float)pv[j].getWidth() / PicViewWidthOrg;

		//MousePageId = iPageIdIn;	// j
		MousePageId = -1;	// j
		MousePageLeftOrRight = LeftOrRight;
		int xInPicView = xMapFromView3D2PicView(tp, LeftOrRight, xIn3dView, yIn3dView, (int)pv[j].mBitmapPeerRct.right);
		int yInPicView = yMapFromView3D2PicView(tp, LeftOrRight, xIn3dView, yIn3dView, (int)pv[j].mBitmapPeerRct.bottom);
		pv[j].iXInPage = xInPicView;
		pv[j].iYInPage = yInPicView;

		//if (j < PicViewCount)
		{
			System.out.println("onClick------->>iPageId: " + j);

			//pv[j].mScale = mScale;

			foundPv = j;
			//ObEPianoAndroidJavaAPI.OnMouseMove(j, pv[j].iXInPage, pv[j].iYInPage);
			if (pv[j].mScale > 0)
			{
				float scalesum = pv[j].mScale / ImgAndViewK;
				if (pv[j].mScale <= 1)
				{
					// 缩小的情况，正常处理
					ObEPianoAndroidJavaAPI.OnMouseMove(j, (int)(pv[j].iXInPage / scalesum), (int)(pv[j].iYInPage / scalesum));
				}
				else
				{
					// 放大的情况，因为左右滑动窗口并没有使用正规的scrollview功能，而是通过图像平移方式来实现的，所以下面计算时手工加入了pv[j].OffsetX(窗口x方向滑动量)因素。
					ObEPianoAndroidJavaAPI.OnMouseMove(j, (int)((pv[j].iXInPage + pv[j].OffsetX) / scalesum), (int)(pv[j].iYInPage / scalesum));
				}
			}
			else
			{
				System.out.println("setOnClickListener, error, pv[" + j + "].mScale is 0.");
			}
		}

		// 指标画鼠标线
		if (true)
		{
			int MouseLinePara[];
			MouseLinePara = ObEPianoAndroidJavaAPI.QueryMouseLine();
			if (MouseLinePara.length != 5)
			{
				System.out.println("setOnClickListener, error, MouseLinePara.leng is " + MouseLinePara.length);
			}
			else
			{
				// clear mouse line in all pages
//				for(j = 0; j < PicViewCount; j++)
//				{
//					pv[j].mMouseTimeSlice_iPageId = -1;
//				}

				// set mouseline in page
				int iPageId = MouseLinePara[0];
				if (iPageId >= 0 && iPageId < PicViewCount && pv[iPageId] != null)
				{
					MousePageId = iPageIdIn;	// j

					pv[iPageId].mMouseTimeSlice_iPageId 	= MouseLinePara[0];	// -1 无效
					pv[iPageId].mMouseTimeSlice_iBarId 		= MouseLinePara[1];
					pv[iPageId].mMouseTimeSlice_xInPage 	= MouseLinePara[2];
					pv[iPageId].mMouseTimeSlice_y0InPage 	= MouseLinePara[3];
					pv[iPageId].mMouseTimeSlice_y1InPage 	= MouseLinePara[4];

					if (LeftOrRight == 1)	//
					{
						// 右面保持直接映射
						MouseLineX = pv[iPageId].mMouseTimeSlice_xInPage;
					}
					else
					{
						// 左页在页内进行左右颠倒
						MouseLineX = pv[j].mBitmapPeerRct.right - pv[iPageId].mMouseTimeSlice_xInPage;
					}
					MouseLineY0 = yMapFromView3D2PicView_R(tp, LeftOrRight, pv[iPageId].mMouseTimeSlice_xInPage, pv[iPageId].mMouseTimeSlice_y0InPage, pv[iPageId].mBitmapPeerRct.bottom);
					MouseLineY1 = yMapFromView3D2PicView_R(tp, LeftOrRight, pv[iPageId].mMouseTimeSlice_xInPage, pv[iPageId].mMouseTimeSlice_y1InPage, pv[iPageId].mBitmapPeerRct.bottom);
					MouseLineY0 = -(MouseLineY0 - (int)tp.height);	// "-()", 为什么要上下颠倒?
					MouseLineY1 = -(MouseLineY1 - (int)tp.height);

					//pv[iPageId].postInvalidate(); //(0, 0, pv[iPageId]., PicViewHeightCur);
				}

				// refresh win
//				for(j = 0; j < PicViewCount; j++)
//				{
//					pv[j].postInvalidate(); //0, 0, PicViewWidthCur, PicViewHeightCur);
//				}
			}
		}

		return 1;
	}

	//触摸事件回调方法
	@Override
	public boolean onTouchEvent(MotionEvent e) {

		if(isCheck)
		{//若在detail中不处理触控事件
			return true;
		}

		float x = e.getX();//获取触控点X坐标
		float y = e.getY();//获取触控点Y坐标
		float dx = x - mPreviousX;//计算X向触控位移
		float dy = y - mPreviousY;//计算Y向触控位移
		long currTime=System.currentTimeMillis();//获取当前时间戳
		long timeSpan=(currTime-previousTime)/10;//计算两次触控事件之间的时间差

		switch (e.getAction()) {
			case MotionEvent.ACTION_DOWN:
				isMove=false;

				ScrollPageStatus = SCROLL_PAGE_STATUS.SCROLL_PAGE_NULL;

				// 翻页模式
				if (y < screenHeight / 3)
				{
					// 划上半区域, 多页模式
					ScrollPageMode = SCROLL_PAGE_MOD.SCROLL_PAGE_MODE_MULTI_PAGES;
				}
				else
				{
					// 单页模式
					ScrollPageMode = SCROLL_PAGE_MOD.SCROLL_PAGE_MODE_SINGLE_PAGE;
				}

				yAngleWg = 0;
				OneTimeOnSingleModeCount = 0;

				break;
			case MotionEvent.ACTION_MOVE:
				if (Math.abs(dx) > MoveJudgeDistance || Math.abs(dy) > MoveJudgeDistance) { // 触控位移大于阈值则进入移动状态

					isMove = true;

					yAngleWg += dx * 0.2f;
				}
				if (isMove) {// 若在移动状态则计算角度变化速度
					if (timeSpan != 0) {
						yAngleV = dx * TOUCH_SCALE_FACTOR / timeSpan;
					}
				}
				break;
			case MotionEvent.ACTION_UP:
				//若在非移动状态且角度速度为0则看选中的是哪幅照片来显示
//        	if(!isMove&&yAngleV==0)
//        	{
//        		//折算出触控点在NEAR面上的位置
//        		float nearX=(x-screenWidth/2)*ratio/(screenWidth/2);
//        		float nearY=(screenHeight/2-y)/(screenHeight/2);
//
//        		//先判断点下去的是左边还是右边
//        		if(x>screenWidth/2)
//        		{//右边
//        			ArrayList<CandidateDis> al=new ArrayList<CandidateDis>();
//        			for(int i=0;i<paperCount;i++)	//PHOTO_COUNT
//        			{
//        				//计算此幅照片的角度
//        				float tempAngle=(i*Photo_Angle_Span+yAngle + 180)%360;	//PHOTO_ANGLE_SPAN
//        				//若角度在270到360范围内则在右边的前面
//        				if(tempAngle>270&&tempAngle<360)
//        				{
//        					al.add(new CandidateDis(tempAngle-270,tempAngle,i));
//        				}
//        			}
//        			//根据与270度的夹角排序，夹角小的排在前面
//        		    Collections.sort(al);
//        		    //遍历候选列表谁在X范围内谁单独显示
//        		    currIndex=-1;
//        		    for(CandidateDis cd:al)
//        		    {
//        		    	if(cd.isInXRange(tp, nearX,nearY))
//        		    	{
//        		    		currIndex=cd.index;
//        		    		break;
//        		    	}
//        		    }
//        		}
//        		else
//        		{
//        			ArrayList<CandidateDis> al=new ArrayList<CandidateDis>();
//        			for(int i=0;i<paperCount;i++)
//        			{
//        				//计算此幅照片的角度
//        				float tempAngle=(i*Photo_Angle_Span+yAngle + 180)%360;	// PHOTO_ANGLE_SPAN
//        				//若角度在180到270范围内则在左边的前面
//        				if(tempAngle>180&&tempAngle<270)
//        				{
//        					al.add(new CandidateDis(270-tempAngle,tempAngle,i));
//        				}
//        			}
//        			//根据与270度的夹角排序，夹角小的排在前面
//        			Collections.sort(al);
//        			//遍历候选列表谁在X范围内谁单独显示
//        			currIndex=-1;
//        		    for(CandidateDis cd:al)
//        		    {
//        		    	if(cd.isInXRange(tp, nearX,nearY))
//        		    	{
//        		    		currIndex=cd.index;
//        		    		break;
//        		    	}
//        		    }
//        		}
//        		//若有选中照片，则设置进入detail显示状态
//        		if(currIndex!=-1)
//        		{
//        			isCheck=true;
//        		}
//        	}

				// 设置鼠标线, 检查一下状态，如果在翻页就不要进行鼠标线设定操作
				if (ScrollPageStatus == SCROLL_PAGE_STATUS.SCROLL_PAGE_NULL)
				{
					SetCursor(ObEPianoAndroidJavaAPI, (int)x, (int)y);
				}

				isMove=false;

				KeyUPEvent = true;

				break;
		}

		mPreviousX = x;//记录触控笔位置
		mPreviousY = y;//记录触控笔位置
		previousTime=currTime;//记录此次时间

		return true;
	}

	private class SceneRenderer implements Renderer
	{

		@Override
		public void onDrawFrame(GL10 gl)
		{
//            gl.glShadeModel(GL10.GL_SMOOTH);// 启用阴影平滑
//            gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);// 真正精细的透视修正

			//清除颜色缓存于深度缓存
			gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
			//设置当前矩阵为模式矩阵
			gl.glMatrixMode(GL10.GL_MODELVIEW);


//            // 线性滤波
//            gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER,
//                    GL10.GL_NEAREST_MIPMAP_NEAREST);
//            gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER,
//                    GL10.GL_NEAREST_MIPMAP_NEAREST);


			//设置当前矩阵为单位矩阵
			gl.glLoadIdentity();

//            if(isCheck)
//            {
//            	//显示某一幅照片，detail状态
//            	gl.glPushMatrix();
//                gl.glTranslatef(0, 0f, -52f);
//            	gl.glTranslatef(-tp.length-tp.width*0.5f, 0, 0);
//            	if (textureIds[currIndex] != -1)
//            	{
//            		int texId = DecideTextId(0, textureIds[currIndex], 0);
//            		tp.drawSelf(gl,0,0,texId);	// ...
//            	}
//            	gl.glPopMatrix();
//
//            	return;
//            }

			// scroll
			if (ScrollPageMode == SCROLL_PAGE_MOD.SCROLL_PAGE_MODE_MULTI_PAGES)
			{
				Scroll_MultiPages(gl);
			}
			else if (ScrollPageMode == SCROLL_PAGE_MOD.SCROLL_PAGE_MODE_SINGLE_PAGE)		/////////////////////////////////////
			{
				Scroll_SinglePage(gl);
			}

			return;
		}

		// 单页滚动
		int Scroll_SinglePage(GL10 gl)
		{
			// yAngle = yAngle % 360;

			int i = 0;

			// if (GrabPaperIdDup == -1) //!isMove)
			if (ScrollPageStatus == SCROLL_PAGE_STATUS.SCROLL_PAGE_NULL) {
				int start = 0;

				// if (yAngleWg == 0)
				{
					// 一般情况

					GrabPaperId = -1;
					GrabPaperIdDup = GrabPaperId;
					leftPaperIdDup = leftPaperId;
					rightPaperId = leftPaperId + 1;
					rightPaperIdDup = rightPaperId;

					// 鼠标线
					// int clr = 0;
					// DrawLine3d(gl, MouseLineX, MouseLineY0, MouseLineX,
					// MouseLineY1, clr);
					// //yAngleWg = 0; // test
				}

				if (yAngleWg > 0) {
					// 向右翻(手指向右滑)

					ScrollPageDir = SCROLL_DIRECTION.SCROLL_PAGE_DIR_RIGHT;

					if (leftPaperIdDup == 0) {
						// return;
					} else {
						start = 1;
					}
				} else if (yAngleWg < 0) {
					// 向左翻(手指向左滑)

					ScrollPageDir = SCROLL_DIRECTION.SCROLL_PAGE_DIR_LEFT;

					if (rightPaperIdDup == paperCount - 1) {
						// return;
					} else {
						start = 1;
					}
				}

				// SCROLL_PAGE_MODE_SINGLE_PAGE时, true:一次只翻一页,
				// false:只要手指在滑动，就可以连续翻页
				if (OneTimeOnSingleMode && start > 0) {
					if (OneTimeOnSingleModeCount > 0) {
						start = 0;
					}
				}

				if (start > 0) {
					// 滚动开始, 初始化

					// 确定GrabPaperIdDup
					if (ScrollPageDir == SCROLL_DIRECTION.SCROLL_PAGE_DIR_RIGHT) {
						// 向右翻(手指向右滑)

						if (leftPaperIdDup == 0) {
							return 0;
						}

						GrabPaperIdDup = leftPaperIdDup;
						leftPaperId = leftPaperIdDup - 1;
						rightPaperId = rightPaperIdDup - 1;
						paperAngle[leftPaperIdDup] = 0;
						leftPaperIdDup--;
					} else if (ScrollPageDir == SCROLL_DIRECTION.SCROLL_PAGE_DIR_LEFT) {
						// 向左翻

						if (rightPaperIdDup == paperCount - 1) {
							return 0;
						}

						GrabPaperIdDup = rightPaperIdDup;
						leftPaperId = leftPaperIdDup + 1;
						rightPaperId = rightPaperIdDup + 1;
						paperAngle[rightPaperIdDup] = 180;
						rightPaperIdDup++;
					}
					GrabPaperId = GrabPaperIdDup;

					zoomK = zoomOrg;

					KeyUPEvent = false;

					ScrollPageStatus = SCROLL_PAGE_STATUS.SCROLL_PAGE_FINGER_OFF; // SCROLL_PAGE_STARTED;
					scrollpageFallTime = System.currentTimeMillis();

					//if (leftPaperIdDup + 1 > rightPaperIdDup)
					if (leftPaperIdDup > rightPaperIdDup)
					{
						Log.i("wg",
								"Scroll started, error, leftPaperIdDup:"
										+ leftPaperIdDup
										+ ", rightPaperIdDup:"
										+ rightPaperIdDup);
					}

					// 初始化各页角度, 左右最上面的页分别为0, 180，其它页各下背面偏离10度
					for (i = 0; i < leftPaperIdDup; i++) {
						paperAngle[i] = -10;
					}
					paperAngle[leftPaperIdDup] = 0;
					paperAngle[rightPaperIdDup] = 180;
					for (i = rightPaperIdDup + 1; i < paperCount; i++) {
						paperAngle[i] = 180 + 10;
					}

					if (false) {
						Log.i("wg", "--yAngleWg:"
								+ yAngleWg
								// + ",yAngleV:" + yAngleV
								+ ",ScorStat:" + ScrollPageStatus
								+ ",KEvent:" + KeyUPEvent + ",leftPaperId:"
								+ leftPaperIdDup + ",rightPaperId:"
								+ rightPaperIdDup + ",GrabPaperIdDup:"
								+ GrabPaperIdDup + ",leftPaperIdDup:"
								+ leftPaperIdDup + ",rightPaperIdDup:"
								+ rightPaperIdDup);
					}
				}
			} else if (ScrollPageStatus == SCROLL_PAGE_STATUS.SCROLL_PAGE_FINGER_OFF) {
				// 松手, 左右的悬空页开始自由落体运动

				// long FallDur = 300; //500; //1300; // ms cfg...

				Scroll_SinglePage(gl, FallDur, ScrollPageDir);

				// test
				if (ScrollPageStatus == SCROLL_PAGE_STATUS.SCROLL_PAGE_NULL) {
					if (false) {
						Log.i("wg", "End, yAngle:"
								+ yAngle
								// + ",yAngleV:" + yAngleV
								+ ",ScorStat:" + ScrollPageStatus
								+ ",KEvent:" + KeyUPEvent + ",leftPaperId:"
								+ leftPaperIdDup + ",rightPaperId:"
								+ rightPaperIdDup + ",GrabPaperIdDup:"
								+ GrabPaperIdDup + ",leftPaperIdDup:"
								+ leftPaperIdDup + ",rightPaperIdDup:"
								+ rightPaperIdDup);
					}

					// FallDur = 0;
					// long z = FallDur;

					yAngleWg = 0;

					OneTimeOnSingleModeCount++;
				}
			}

			// 显示照片组，可触控旋转选择
			gl.glPushMatrix();
			// gl.glTranslatef(0, 0f, CENTER_Z);
			// gl.glTranslatef(0, 0f, (float)(-(tp.width + tp.length) * 2));
			gl.glTranslatef(0, 0f, (float) (-bookdistance));

			// 绘制leftPaperIdDup及前面的约，和，　rightPaperIdDup以后面的纸, 不包括中间悬空的纸
			// DrawStaticPapers(gl, leftPaperIdDup, rightPaperIdDup);

			OnScollPageOver_DrawAllPapers(gl, leftPaperIdDup,
					rightPaperIdDup, paperAngle, zoomK); // /////////////

			gl.glPopMatrix();

			// if (GrabPaperIdDup != -1)
			if (false) {
				Log.i("wg", "yAngleWg:"
						+ yAngleWg
						// + ",yAngleV:" + yAngleV
						+ ",ScorStat:" + ScrollPageStatus + ",KEvent:"
						+ KeyUPEvent + ",leftPaperId:" + leftPaperIdDup
						+ ",rightPaperId:" + rightPaperIdDup
						+ ",GrabPaperIdDup:" + GrabPaperIdDup
						+ ",leftPaperIdDup:" + leftPaperIdDup
						+ ",rightPaperIdDup:" + rightPaperIdDup);
			}


			return 1;
		}

		// 多页滚动
		int Scroll_MultiPages(GL10 gl) {

			// 显示照片组，可触控旋转选择
			gl.glPushMatrix();
			// gl.glTranslatef(0, 0f, CENTER_Z);
			// gl.glTranslatef(0, 0f, (float)(-(tp.width + tp.length) * 2));
			gl.glTranslatef(0, 0f, (float) (-bookdistance));

			// yAngle = yAngle % 360;

			if (true) {
				int i = 0;

				// if (GrabPaperIdDup == -1) //!isMove)
				if (ScrollPageStatus == SCROLL_PAGE_STATUS.SCROLL_PAGE_NULL) {
					if (yAngleWg == 0) {
						// 一般情况

						GrabPaperId = -1;
						GrabPaperIdDup = GrabPaperId;
						leftPaperIdDup = leftPaperId;
						rightPaperId = leftPaperId + 1;
						rightPaperIdDup = rightPaperId;

						// // 鼠标线
						// int clr = 0;
						// DrawLine3d(gl, MouseLineX, MouseLineY0, MouseLineX,
						// MouseLineY1, clr);
					} else {
						// 滚动开始, 初始化

						KeyUPEvent = false;

						ScrollPageStatus = SCROLL_PAGE_STATUS.SCROLL_PAGE_STARTED;

						if (leftPaperIdDup + 1 != rightPaperIdDup) {
							Log.i("wg",
									"Scroll started, error, leftPaperIdDup:"
											+ leftPaperIdDup
											+ ", rightPaperIdDup:"
											+ rightPaperIdDup);
						}

						// 初始化各页角度, 左右最上面的页分别为0, 180，其它页各下背面偏离10度
						for (i = 0; i < leftPaperIdDup; i++) {
							paperAngle[i] = -10;
						}
						paperAngle[leftPaperIdDup] = 0;
						paperAngle[rightPaperIdDup] = 180;
						for (i = rightPaperIdDup + 1; i < paperCount; i++) {
							paperAngle[i] = 180 + 10;
						}

						// 确定GrabPaperIdDup
						if (yAngleWg > 0) {
							GrabPaperIdDup = leftPaperIdDup;
						} else if (yAngleWg < 0) {
							GrabPaperIdDup = rightPaperIdDup;
						}
						GrabPaperId = GrabPaperIdDup;

						// 滚动中的画面比例控制
						zoomK = zoomOrg;
						if (OnScrollZoomKSwitch) {
							scrollpageStartTime = System.currentTimeMillis();
						}

						if (false) {
							Log.i("wg", "--yAngleWg:"
									+ yAngleWg
									// + ",yAngleV:" + yAngleV
									+ ",ScorStat:" + ScrollPageStatus
									+ ",KEvent:" + KeyUPEvent + ",leftPaperId:"
									+ leftPaperIdDup + ",rightPaperId:"
									+ rightPaperIdDup + ",GrabPaperIdDup:"
									+ GrabPaperIdDup + ",leftPaperIdDup:"
									+ leftPaperIdDup + ",rightPaperIdDup:"
									+ rightPaperIdDup);
						}
					}
				}
				// if (GrabPaperIdDup != -1)
				else if (ScrollPageStatus == SCROLL_PAGE_STATUS.SCROLL_PAGE_STARTED) {
					// 滚动中

					// zoom control
					if (OnScrollZoomKSwitch) {
						long tickdiff = System.currentTimeMillis()
								- scrollpageStartTime;
						zoomK -= zoomSpeed * tickdiff;
						if (zoomK <= zoomKDest) {
							zoomK = zoomKDest;
						}
					}

					// int ll = leftPaperIdDup;
					if (!KeyUPEvent) {
						ScrollPage(gl, paperSpan, yAngleWg, GrabPaperIdDup);

						// test
						// Log.i("wg", "-----ScrollPage, yAngleWg:" + yAngleWg
						// + ", leftPaperIdDup a:" + ll
						// + ", leftPaperIdDup b:" + leftPaperIdDup);
					} else {
						// KeyUPEvent = false;

						// 翻页停止, 页开始自由落体运动
						if (ScrollPageStatus == SCROLL_PAGE_STATUS.SCROLL_PAGE_STARTED) {
							// leftPaperId_Pre = leftPaperId;
							// rightPaperId_Pre = rightPaperId;

							// 根据paperAngle，leftPaperIdDup和rightPaperIdDup(滑动过程中动态生成的),
							// 确定最终的leftPaperId和rightPaperId
							// 从leftPaperId落到leftPaperIdDup,
							// 从rightPaperId落到rightPaperIdDup
							int l = leftPaperIdDup;
							int r = rightPaperIdDup;
							for (i = leftPaperIdDup; i <= rightPaperIdDup; i++) {
								if (i < paperCount - 1) {
									if (paperAngle[i] <= 90
											&& paperAngle[i + 1] >= 90) {
										// 目标
										leftPaperId = i;
										rightPaperId = i + 1;
										break;
									}
								} else {
									// 异常
									Log.i("wg",
											"SCROLL_PAGE_STARTED, error, i is:"
													+ i);
									ScrollPageStatus = SCROLL_PAGE_STATUS.SCROLL_PAGE_NULL;
									break;
								}
							}

							if (ScrollPageStatus != SCROLL_PAGE_STATUS.SCROLL_PAGE_NULL) {

							}

							// 页面开始自由落地
							ScrollPageStatus = SCROLL_PAGE_STATUS.SCROLL_PAGE_FINGER_OFF;
							scrollpageFallTime = System.currentTimeMillis();

							GrabPaperId = -1;
						}

						yAngleWg = 0;
					}
				} else if (ScrollPageStatus == SCROLL_PAGE_STATUS.SCROLL_PAGE_FINGER_OFF) {
					// 松手, 左右的悬空页开始自由落体运动

					// zoom control
					if (OnScrollZoomKSwitch) {
						long tickdiff = System.currentTimeMillis()
								- scrollpageFallTime;
						zoomK += zoomSpeed * tickdiff;
						if (zoomK > zoomOrg) {
							zoomK = zoomOrg;
						}
					}

					// 从leftPaperIdDup到leftPaperId，　从rightPaperIdDup到rightPaperId
					Scroll_FingurUp(gl, FallDur);

					if (ScrollPageStatus == SCROLL_PAGE_STATUS.SCROLL_PAGE_NULL) {
						if (false) {
							Log.i("wg", "End, yAngle:"
									+ yAngle
									// + ",yAngleV:" + yAngleV
									+ ",ScorStat:" + ScrollPageStatus
									+ ",KEvent:" + KeyUPEvent + ",leftPaperId:"
									+ leftPaperIdDup + ",rightPaperId:"
									+ rightPaperIdDup + ",GrabPaperIdDup:"
									+ GrabPaperIdDup + ",leftPaperIdDup:"
									+ leftPaperIdDup + ",rightPaperIdDup:"
									+ rightPaperIdDup);
						}

						// FallDur = 0;
						// long z = FallDur;

						yAngleWg = 0;

						zoomK = zoomOrg;
					}
				}

				// 绘制leftPaperIdDup及前面的约，和，　rightPaperIdDup以后面的纸, 不包括中间悬空的纸
				// DrawStaticPapers(gl, leftPaperIdDup, rightPaperIdDup);

				OnScollPageOver_DrawAllPapers(gl, leftPaperIdDup,
						rightPaperIdDup, paperAngle, zoomK);

				// if (GrabPaperIdDup != -1)
				if (false) {
					Log.i("wg", "yAngle:"
							+ yAngle
							// + ",yAngleV:" + yAngleV
							+ ",ScorStat:" + ScrollPageStatus + ",KEvent:"
							+ KeyUPEvent + ",leftPaperId:" + leftPaperIdDup
							+ ",rightPaperId:" + rightPaperIdDup
							+ ",GrabPaperIdDup:" + GrabPaperIdDup
							+ ",leftPaperIdDup:" + leftPaperIdDup
							+ ",rightPaperIdDup:" + rightPaperIdDup);
				}
			} else {
				// 等分

				gl.glRotatef(yAngle, 0, 1, 0);

				// for(int i=0;i<textureIds.length;i++)
				for (int i = 0; i < paperCount; i++)
				// for(int i=0;i<2;i++)
				{// 遍历纹理数组，显示每幅照片
					gl.glPushMatrix();
					gl.glRotatef(i * Photo_Angle_Span + 180, 0, 1, 0); // PHOTO_ANGLE_SPAN
					// if (textureIds[currIndex] != -1)
					{
						// 计算此幅照片的角度
						float paperAngle = (i * Photo_Angle_Span + yAngle + 180) % 360; // PHOTO_ANGLE_SPAN

						int texId = DecideTextId(paperAngle, textureIds[2 * i],
								textureIds[2 * i + 1]);
						tp.drawSelf(gl, i, paperAngle, texId, 0); // 0, test, pvId未正确填写， 暂时...
					}
					gl.glPopMatrix();
				}
			}

			gl.glPopMatrix();

			return 1;
		}

		// 从leftPaperId落到leftPaperIdDup, 从rightPaperId落到rightPaperIdDup
		int Scroll_FingurUp(GL10 gl, long FallDur)
		{
			int i = 0;

			long CurTick = System.currentTimeMillis();
			long TickDiff = CurTick - scrollpageFallTime;
			//int WSpeed =

			float k = 0;

			if (TickDiff > FallDur)
			{
				k = 0;
			}
			else
			{
				k = (float)TickDiff / FallDur;
				k = 1.0f - k;
			}

			if (k != 0)
			{

//        		Log.i("wg", "k:" + k
//        				+ ",leftPaperId:" + leftPaperId
//        				+ ",leftPaperIdDup:" + leftPaperIdDup
//        				+ ",rightPaperId:" + rightPaperId
//        				+ ",rightPaperIdDup:" + rightPaperIdDup
//        				+ ",paperAngle[]:" + paperAngle[leftPaperId]
//        				+ ",paperAngle[]:" + paperAngle[rightPaperId]);

				paperAngle[leftPaperIdDup] = 0;
				paperAngle[rightPaperIdDup] = 180;

				// 从leftPaperId落到leftPaperIdDup, 从rightPaperId落到rightPaperIdDup
				for(i = leftPaperIdDup + 1; i <= leftPaperId; i++)
				{
					paperAngle[i] *= k;

					if (paperAngle[i] == 0)
					{
						leftPaperIdDup = i;
					}
				}
				for(i = rightPaperIdDup - 1; i >= rightPaperId; i--)
				{
					paperAngle[i] = (int)(180 - (180 - paperAngle[i]) * k);

					if (paperAngle[i] == 180)
					{
						rightPaperIdDup = i;
					}
				}

				if (false)
				{
					Log.i("wg", "k1:" + k
							+ ", leftPaperId:" + leftPaperId
							+ ", rightPaperId:" + rightPaperId
							+ ", ldup:" + leftPaperIdDup
							+ ", rdup:" + rightPaperIdDup
							+ ", A[l]:" + paperAngle[leftPaperId]
							+ ", A[ldup]:" + paperAngle[leftPaperIdDup]
							+ ", A[rdup-1]:" + paperAngle[rightPaperIdDup-1]
					);
				}

				//OnScollPageOver_DrawAllPapers(gl, leftPaperIdDup, rightPaperIdDup, paperAngle);
			}
			else if (k == 0)
			{
				// 过程结束

				ScrollPageStatus = SCROLL_PAGE_STATUS.SCROLL_PAGE_NULL;

				if (false)
				{
					Log.i("wg", "k2:" + k
							+ ", leftPaperId:" + leftPaperId
							+ ", rightPaperId:" + rightPaperId
							+ ", ldup:" + leftPaperIdDup
							+ ", rdup:" + rightPaperIdDup
							+ ", A[rdup-1]:" + paperAngle[rightPaperIdDup-1]
					);
				}

				// 更新leftPaperId
//    			leftPaperId = leftPaperIdDup;
//    			rightPaperId = rightPaperIdDup;
				leftPaperIdDup = leftPaperId;
				rightPaperIdDup = rightPaperId;

				paperAngle[leftPaperIdDup] = 0;
				paperAngle[rightPaperIdDup] = 180;

				//OnScollPageOver_DrawAllPapers(gl, leftPaperIdDup, rightPaperIdDup, paperAngle);

				GrabPaperId = -1;
				GrabPaperIdDup = -1;
			}

			return 1;
		}

		//
		int Scroll_SinglePage(GL10 gl, long FallDur, SCROLL_DIRECTION dir)	// 0: to left, 1: to right
		{
			int i = 0;

			long CurTick = System.currentTimeMillis();
			long TickDiff = CurTick - scrollpageFallTime;
			//int WSpeed =

			float k = 0;

			if (TickDiff > FallDur)
			{
				k = 0;
			}
			else
			{
				k = (float)TickDiff / FallDur;
				k = 1.0f - k;
			}
			// k : 1 -> 0

			if (k != 0)
			{

				paperAngle[leftPaperIdDup] = 0;
				paperAngle[rightPaperIdDup] = 180;

				if (dir == SCROLL_DIRECTION.SCROLL_PAGE_DIR_LEFT)
				{
					// 从leftPaperId落到leftPaperIdDup, 从rightPaperId落到rightPaperIdDup
					//for(i = leftPaperIdDup + 1; i <= leftPaperId; i++)
					i = rightPaperIdDup - 1;
					{
						paperAngle[i] *= k;

						if (paperAngle[i] == 0)
						{
							leftPaperIdDup = i;
						}
					}
				}
				else
				{
					//for(i = rightPaperIdDup - 1; i >= rightPaperId; i--)
					i = leftPaperIdDup + 1;
					{
						paperAngle[i] = (int)(180 - (180 - paperAngle[i]) * k);

						if (paperAngle[i] == 180)
						{
							rightPaperIdDup = i;
						}
					}
				}

				if (false)
				{
					Log.i("wg", "k:" + k
							+ ",leftPaperId:" + leftPaperId
							+ ",leftPaperIdDup:" + leftPaperIdDup
							+ ",rightPaperId:" + rightPaperId
							+ ",rightPaperIdDup:" + rightPaperIdDup
							+ ",paperAngle[" + i + "]:" + paperAngle[i]
					);
				}

				if (false)
				{
					Log.i("wg", "k1:" + k
							+ ", leftPaperId:" + leftPaperId
							+ ", rightPaperId:" + rightPaperId
							+ ", ldup:" + leftPaperIdDup
							+ ", rdup:" + rightPaperIdDup
							+ ", A[l]:" + paperAngle[leftPaperId]
							+ ", A[ldup]:" + paperAngle[leftPaperIdDup]
							+ ", A[rdup-1]:" + paperAngle[rightPaperIdDup-1]
					);
				}

				//OnScollPageOver_DrawAllPapers(gl, leftPaperIdDup, rightPaperIdDup, paperAngle);
			}
			else if (k == 0)
			{
				// 过程结束

				ScrollPageStatus = SCROLL_PAGE_STATUS.SCROLL_PAGE_NULL;

				if (false)
				{
					Log.i("wg", "k2:" + k
							+ ", leftPaperId:" + leftPaperId
							+ ", rightPaperId:" + rightPaperId
							+ ", ldup:" + leftPaperIdDup
							+ ", rdup:" + rightPaperIdDup
							+ ", A[rdup-1]:" + paperAngle[rightPaperIdDup-1]
					);
				}

				// 更新leftPaperId
//    			leftPaperId = leftPaperIdDup;
//    			rightPaperId = rightPaperIdDup;
				leftPaperIdDup = leftPaperId;
				rightPaperIdDup = rightPaperId;

				paperAngle[leftPaperIdDup] = 0;
				paperAngle[rightPaperIdDup] = 180;

				//OnScollPageOver_DrawAllPapers(gl, leftPaperIdDup, rightPaperIdDup, paperAngle);

				GrabPaperId = -1;
				GrabPaperIdDup = -1;
			}

			return 1;
		}

		// 滚动页
		// input/output: leftPaperIdDup, rightPaperIdDup, paperAngle[]
		int ScrollPage(GL10 gl, int paperSpan, float yAngle,
					   int GrabPaperIdDup)
		{
			int i = 0;

			if (yAngle > 0)
			{
				// 向右翻(手指向右划)

				if (GrabPaperIdDup > 0 && GrabPaperIdDup < paperCount - 1)
				{
					// test
					//Log.i("wg", "-----ScrollPage, right, yAngleWg:" + yAngleWg);

					int anglesLeft = (int)yAngle;	// 剩余角度
					i = 0;
					for(; anglesLeft > 0; i++)
					{
//						if (i >= paperCount)
//						{
//							break;
//						}

						// 页号
						int p = GrabPaperIdDup - i;
						if (p <= 0)
						{
							break;
						}

						// 绝对页角度paperAngle[p]
						{
							paperAngle[p] = anglesLeft;
							if (paperAngle[p] >= 180)
							{
								paperAngle[p] = 180;	// 本页滚动到页面位置

								rightPaperIdDup = p;
								paperAngle[rightPaperIdDup] = 180;

//								// 挤下一页
//								if (p < paperCount - 1)
//								{
//									paperAngle[p + 1] = 180 + 10;
//								}
							}
							else
							{
								// 情况: 先向左滑，再回滑的过程中, rightPaperIdDup可能需要调整 2016.08。13
								if (rightPaperIdDup <= p && leftPaperIdDup < paperCount - 1)
								{
									rightPaperIdDup = p + 1;
								}
							}
						}


//						angleLeft[i] = anglesLeft;
						if (anglesLeft > paperSpan)
						{
							anglesLeft -= paperSpan;

							if (p == 0)
							{
								leftPaperIdDup = p;
								//break;
							}
							else
							{
								leftPaperIdDup = p - 1;
							}
						}
						else
						{
							anglesLeft = 0;
							leftPaperIdDup = p;
							if (leftPaperIdDup < 0)
							{
								leftPaperIdDup = 0;
							}
							//break;
						}
					}
					paperAngle[leftPaperIdDup] = 0;
				}

				if (leftPaperIdDup >= rightPaperIdDup)
				{
					leftPaperIdDup = rightPaperIdDup - 1;
				}
			}
			else if (yAngle < 0)
			{
				// 向左翻(手指向左划)

				if (GrabPaperIdDup < paperCount - 1
						&& GrabPaperIdDup > 0)
				{
					// test
					//Log.i("wg", "-----ScrollPage, left, yAngleWg:" + yAngleWg
					//	+ ", leftPaperIdDup:" + leftPaperIdDup
					//	+ ", rightPaperIdDup:" + rightPaperIdDup
					//	+ ", GrabPaperIdDup:" + GrabPaperIdDup
					//	);

					int anglesLeft = (int)-yAngle;
					i = 0;
					for(; anglesLeft > 0; i++)
					{
//						if (i >= paperCount)
//						{
//							break;
//						}

						int p = GrabPaperIdDup + i;
						if (p >= paperCount - 1)
						{
							break;
						}

						// 绝对页角度
						{
							paperAngle[p] = 180 - anglesLeft;
							if (paperAngle[p] <= 0)
							{
								paperAngle[p] = 0;	// 本页滚动到页面位置

								leftPaperIdDup = p;

//								// 挤下一页
//								if (p > 0)
//								{
//									paperAngle[p - 1] = -10;
//								}
							}
							else
							{
								// 情况: 先向左滑，再回滑的过程中, leftPaperIdDup可能需要调整 2016.08。13
								if (leftPaperIdDup >= p && leftPaperIdDup > 0)
								{
									leftPaperIdDup = p - 1;
								}
							}
						}

//						angleRight[i] = anglesLeft;
						if (anglesLeft > paperSpan)
						{
							anglesLeft -= paperSpan;

							if (p == paperCount - 1)
							{
								rightPaperIdDup = p;
								//break;
							}
							else
							{
								rightPaperIdDup = p + 1;
							}
						}
						else
						{
							anglesLeft = 0;
							rightPaperIdDup = p;
							if (rightPaperIdDup > paperCount - 1)
							{
								rightPaperIdDup = paperCount - 1;
							}
							//break;
						}
					}

					paperAngle[rightPaperIdDup] = 180;

//					rightPaperIdDup += i;
//					if (rightPaperIdDup >= paperCount)
//					{
//						rightPaperIdDup = paperCount - 1;
//					}

					//Log.i("wg", "rightPaperIdDup:" + rightPaperIdDup);
				}

				if (rightPaperIdDup <= leftPaperIdDup)
				{
					rightPaperIdDup = leftPaperIdDup + 1;
				}
			}
			else
			{
			}

			return 1;
		}

		// 绘制每纸
		int OnScollPageOver_DrawAllPapers(GL10 gl, int leftPaperId, int rightPaperId, int paperAngle[], float zoomK)
		{
			// test
			if (true)
			{
				//return 1;
			}

			gl.glPushMatrix();

			// 对整体进行缩放
			if (zoomK != 1)
			{
				gl.glScalef(zoomK, zoomK, zoomK);
			}

			// 绘每页

			for(int p = 0; p < leftPaperId; p++)
			{
				paperAngle[p] = -10;
			}

			paperAngle[leftPaperId] = 0;
			paperAngle[rightPaperId] = 180;
			int textureId = 0;
			int pvId = 0;
			int PageId = 0;


			// 纹理空间可重复利用模式
			// 每页的纹理准备
			if (	leftPaperId_last == -1
					|| leftPaperId_last != leftPaperId
					|| rightPaperId_last != rightPaperId
					)
			{
				if (TextTureMod == TEXTURE_MOD.TEXTURE_MOD_MAP)
				{
					int TextId = 0;

					for(int i = 0; i < MaxTextSetSize; i++)
					{
						mTextureSetOccupy[i] = 0;
					}

					// 置mTextureSet[]的占用标记
					for(int p = leftPaperId; p <= rightPaperId; p++)
					{
						// 每页的两面
						for(int i = 0; i < 2; i++)
						{
							PageId = 2*p + i;
							pvId = TextPageId2pvId(PageId);
							if (pvId < 0 || pvId >= PicViewCount)
							{
								continue;
							}

							TextId = pv[pvId].mPvTextureId;
							if (TextId != -1)	// pv已经有相应的纹理
							{
								mTextureSetOccupy[TextId] = 1;	// 置被pvId占用标记
							}
						}
					}

					// 让没有纹理的页有纹理
					// 对还没有纹理的页, 分配mTextureSet[]的纹理号, 填充纹理
					for(int p = leftPaperId; p <= rightPaperId; p++)
					{
						// 每页的两面
						for(int i = 0; i < 2; i++)
						{
							PageId = 2*p + i;
							pvId = TextPageId2pvId(PageId);
							if (pvId < 0 || pvId >= PicViewCount)
							{
								continue;
							}

							if (pv[pvId].mPvTextureId == -1)
							{
								// pvId无纹理, 分配

								// 查一个空闲TextId
								TextId = -1;
								for(int j = 0; j < MaxTextSetSize; j++)
								{
									if (mTextureSetOccupy[j] == 0)
									{
										TextId = j;
										break;
									}
								}

								if (TextId != -1)
								{
									// 有空闲纹理

									// 清旧的pv与纹理的映射
									int pvIdOld = mTextureSet_PvId[TextId];
									if (pvIdOld >= 0) // 此纹理对应的旧的pvId
									{
										pv[pvIdOld].mPvTextureId = -1;
									}
									else
									{
										Log.i("wg", "error, mPvTextureSet, old map is bad.");
									}

									synchronized(pv[pvId]) // 同步
									{
										pv[pvId].mPvTextureId = TextId;
										mTextureSet_PvId[TextId] = pvId;
										mTextureSetOccupy[TextId] = 1;

										int mTextureSetdup[] = mTextureSet;
										initTexture2(gl, pvId, mTextureSet[TextId], TextId);

										Log.i("wg", "OnScollPageOver_DrawAllPapers(), initTexture2(), pvId:" + pvId);	// test

										// 回收mBitmapPeer
										if (RecycleBmpSwitch > 0)
										{
											if (pv[pvId].isBmpValid())
											{
												pv[pvId].mBitmapPeer.recycle(); // x
												pv[pvId].mBitmapPeer = null;
											}
										}
									}
								}
								else
								{
									Log.i("wg", "error, pvId(" + pvId + ") has no texture resoruce");
								}
							}
						}
					}
				}
			}

			// 绘制
			for(int p = leftPaperId; p <= rightPaperId; p++)
			{
				DrawAPage(gl, p);
			}

			leftPaperId_last = leftPaperId;
			rightPaperId_last = rightPaperId;

			for(int p = rightPaperId + 1; p < paperCount; p++)
			{
				paperAngle[p] = 180 + 10;
			}

			gl.glPopMatrix();


			return 1;
		}

		// 画一页, p, paperId
		int DrawAPage(GL10 gl, int p)
		{
			int pvId = 0;
			int textureId = -1;
			int PageId = 0;

			gl.glPushMatrix();
			int a = paperAngle[p] + 180;

			gl.glRotatef(a, 0, 1, 0);	// PHOTO_ANGLE_SPAN

			// 页
			{
				//计算此幅照片的角度
				float paperA=(a) % 360;	//PHOTO_ANGLE_SPAN

				// 页面加载

				int texId = 0;

//            		if (leftPaperId_last != leftPaperId
//    						|| rightPaperId_last != rightPaperId
//    						)
				{
					//页
					//TextTureMod = TEXTURE_MOD.TEXTURE_MOD_START_FROM_0_EACH_TIME;
					if (TextTureMod == TEXTURE_MOD.TEXTURE_MOD_START_FROM_0_EACH_TIME)
					{
						// 纹理空间可重复利用模式

						for(int i = 0; i < 2 && textureId < ActTextSetSize; i++, textureId++)
						{
							PageId = 2*p + i;
							pvId = TextPageId2pvId(PageId);

							if (textureId >= MaxTextSetSize)
							{
								continue;
							}

							// 存在则跳过
							if (leftPaperId_last != leftPaperId
									|| rightPaperId_last != rightPaperId
									)
							{
								synchronized(pv[pvId])
								{
									Log.i("wg", "DrawAPage(), initTexture2(), pvId:" + pvId);	// test

									initTexture2(gl, pvId, mTextureSet[textureId], textureId);
								}
							}
						}
						texId = DecideTextId(paperA, mTextureSet[textureId - 2], mTextureSet[textureId - 1]);
					}
					else if (TextTureMod == TEXTURE_MOD.TEXTURE_MOD_MAP)
					{
						// 纹理空间可重复利用模式

						// 每页的两面
						int TSetId[] = new int[2];
						TSetId[0] = -1;
						TSetId[1] = -1;
						int i = 0;
						for(i = 0; i < 2; i++)
						{
							PageId = 2*p + i;
							pvId = TextPageId2pvId(PageId);
							if (pvId < 0)
							{
								continue;
							}
							if (pvId >= PicViewCount)
							{
								break;
							}

							TSetId[i] = pv[pvId].mPvTextureId;
						}

						int TId[] = new int[2];
						if (TSetId[0] != -1)
						{
							TId[0] = mTextureSet[TSetId[0]];
						}
						else
						{
							TId[0] = -1;
						}

						if (TSetId[1] != -1)
						{
							TId[1] = mTextureSet[TSetId[1]];
						}
						else
						{
							TId[1] = -1;
						}

						// 末尾的封面页
						if (pvId == PicViewCount)
						{
							if (mTextureSet[MaxTextSetSize] == -1)
							{
								mTextureSet[MaxTextSetSize] = initTexture2(gl, pvId, mTextureSet[MaxTextSetSize], MaxTextSetSize);	// 末尾的封面页
							}
							TId[i] = mTextureSet[MaxTextSetSize];
						}

						texId = DecideTextId(paperA, TId[0], TId[1]);
					}
					else if (TextTureMod == TEXTURE_MOD.TEXTURE_MOD_UNIQUE)
					{
						// 纹理唯一模式

						for(int i = 0; i < 2; i++)
						{
							if (textureIds[2*p + i] == -1)
							{
								pvId = TextPageId2pvId(2*p + i);
								textureIds[2*p + i] = initTexture(gl, pvId, 1);
							}
						}
						texId = DecideTextId(paperA, textureIds[2*p], textureIds[2*p + 1]);
					}

					// 绘制
					int reverse = DecideTextReverse(paperA);
					tp.drawSelf(gl,p,paperA,texId, reverse);  /////////////////
				}
			}

			// 鼠标线
			if (p == MousePaperId && MousePageId != -1) //MousePageId)
			{
				int linew = 3;

				DrawLine3d(gl, MouseLineX, MouseLineY0, MouseLineX, MouseLineY1, MousePageLeftOrRight, linew, 0x10000, 0, 0, (0x10000 / 1000)); // R

//				Log.i("wg", "Draw MouseLine: "
//						+ ", MousePageId:" + MousePageId
//						+ ", MousePaperId:" + MousePaperId
//						+ ", MouseLineX:" + MouseLineX
//						+ ", MouseLineY0:" + MouseLineY0
//						+ ", MouseLineY1:" + MouseLineY1);
			}

			// 播放线 play line
			int PlayPageIdT = PlayPageId;
			PlayPaperId = (PlayPageIdT + 1) / 2;
			if (p == PlayPaperId && PlayPageIdT >= 0)
			{
				int linew = 3;

				//int x = 0;

				int valid = 0;	// 左边的纸对应偶数页(0 based), 右边的对应奇数页, 为合法

				int LeftOrRight = -1;
				if (a < 90 + 180)
				{
					LeftOrRight = 0; // left
					if ((PlayPageIdT % 2) == 0)
					{
						valid = 1;
					}
				}
				else if (a > 90 + 180)
				{
					LeftOrRight = 1;
					if ((PlayPageIdT % 2) == 1)
					{
						valid = 1;
					}
				}
				PlayPageLeftOrRight = LeftOrRight;

				if (pv[PlayPageIdT] != null)
				{
					if (LeftOrRight != -1 && valid > 0)
					{
						int iPageId = 0;
						if (LeftOrRight == 1)	//
						{
							// right

							iPageId = PlayPageIdT;
							PlayLineX = pv[PlayPageIdT].mPlayLine_xInPage;
						}
						else
						{
							// left

							iPageId = PlayPageIdT;
							if (pv[iPageId] != null)
							{
								PlayLineX = pv[iPageId].mBitmapPeerRct.right - pv[iPageId].mPlayLine_xInPage;
							}
						}

						if (pv[iPageId] != null)
						{
							PlayLineY0 = yMapFromView3D2PicView_R(tp, LeftOrRight, pv[iPageId].mPlayLine_xInPage, pv[iPageId].mPlayLine_y0InPage, pv[iPageId].mBitmapPeerRct.bottom);
							PlayLineY1 = yMapFromView3D2PicView_R(tp, LeftOrRight, pv[iPageId].mPlayLine_xInPage, pv[iPageId].mPlayLine_y1InPage, pv[iPageId].mBitmapPeerRct.bottom);
							PlayLineY0 = -(PlayLineY0 - (int)tp.height);	// "-()", 为什么要上下颠倒?
							PlayLineY1 = -(PlayLineY1 - (int)tp.height);

							if (false)
							{
								Log.i("wg", "Draw playline: "
										+ ", iPageId:" + iPageId
										+ ", paper:" + PlayPaperId
										+ ", LeftOrRight:" + LeftOrRight
										+ ", PlayLineX:" + PlayLineX
										+ ", PlayLineY0:" + PlayLineY0
								);
							}

							DrawLine3d(gl, PlayLineX, PlayLineY0, PlayLineX, PlayLineY1, PlayPageLeftOrRight, linew, 0, 0, 0x10000, (0x10000 / 1000)); // B
						}
					}
				}
			}


			gl.glPopMatrix();

			return 1;
		}

//    	// 绘制leftPaperIdDup及前面的约，和，　rightPaperIdDup以后面的纸, 不包括中间悬空的纸
//    	int DrawStaticPapers(GL10 gl, int leftPaperIdDup, int rightPaperIdDup)
//    	{
//    		int i = 0;
//
//            // 首部的页
//    		if (false)
//    		{
//	        	float angleHdr = -10;
//	        	for(i = 0; i < leftPaperIdDup; i++)
//	        	{
//	        		gl.glPushMatrix();
//	                gl.glRotatef(angleHdr + 180, 0, 1, 0);	// PHOTO_ANGLE_SPAN
//	            	{
//	            		//计算此幅照片的角度
//	            		float paperAngle=(angleHdr + 180)%360;	//PHOTO_ANGLE_SPAN
//	            		int texId = DecideTextId(paperAngle, textureIds[2*i], textureIds[2*i + 1]);
//	                	tp.drawSelf(gl,i,paperAngle,texId);
//	            	}
//	                gl.glPopMatrix();
//	        	}
//    		}
//
//        	// 左页
//        	i = leftPaperIdDup;
//        	if (i < 0)
//        	{
//        		i = 0;
//        	}
//        	gl.glPushMatrix();
//            gl.glRotatef(0 + 180, 0, 1, 0);	// PHOTO_ANGLE_SPAN
//        	{
//        		//计算此幅照片的角度
//        		float paperAngle=(0 + 180)%360;	//PHOTO_ANGLE_SPAN
//        		int texId = DecideTextId(paperAngle, textureIds[2*i], textureIds[2*i + 1]);
//            	tp.drawSelf(gl,i,paperAngle,texId);
//        	}
//            gl.glPopMatrix();
//
//
//            // 右页
//        	i = rightPaperIdDup;
//        	if (i < paperCount && i >= 0)
//        	{
//
//        	}
//        	else
//        	{
//        		i = paperCount - 1;
//        	}
//        	{
//            	gl.glPushMatrix();
//                gl.glRotatef(180 + 180, 0, 1, 0);	// PHOTO_ANGLE_SPAN
//            	{
//            		//计算此幅照片的角度
//            		float paperAngle=(180 + 180)%360;	//PHOTO_ANGLE_SPAN
//            		int texId = DecideTextId(paperAngle, textureIds[2*i], textureIds[2*i + 1]);
//                	tp.drawSelf(gl,i,paperAngle,texId);
//            	}
//                gl.glPopMatrix();
//        	}
//
//        	// 尾部的页
//        	if (false)
//        	{
//	        	float angleTail = 180 + 10;
//	        	for(i = rightPaperIdDup + 1; i < paperCount; i++)
//	        	{
//	        		if (i < 0)
//	        		{
//	        			continue;
//	        		}
//
//	        		gl.glPushMatrix();
//	                gl.glRotatef(angleTail + 180, 0, 1, 0);	// PHOTO_ANGLE_SPAN
//	            	{
//	            		//计算此幅照片的角度
//	            		float paperAngle=(angleTail + 180)%360;	//PHOTO_ANGLE_SPAN
//	            		int texId = DecideTextId(paperAngle, textureIds[2*i], textureIds[2*i + 1]);
//	                	tp.drawSelf(gl,i,paperAngle,texId);
//	            	}
//	                gl.glPopMatrix();
//	        	}
//        	}
//
//        	return 1;
//    	}

		@Override
		public void onSurfaceChanged(GL10 gl, int width, int height) {
			//设置视窗大小及位置
			//gl.glViewport(0, 0, width, height);
			//设置当前矩阵为投影矩阵
			gl.glMatrixMode(GL10.GL_PROJECTION);
			//设置当前矩阵为单位矩阵
			gl.glLoadIdentity();
			//调用此方法计算产生透视投影矩阵
			float r = ratio;
			//gl.glFrustumf(-r, r, -1, 1, NEAR, width * 4); //width * 10); //100);
			//gl.glFrustumf(-r, r, -1, 1, 10, width * 2); //width * 10); //100);
			float w2 = width / 2 * 1f;
			float h2 = height / 2 * 1f;
			float ra = 0.7f;
			float w2V = w2 * ra;
			float h2V = h2 * ra;
			float k = (w2 - w2V) / w2;
			float near = w2V / k;
			float far = near + w2 * 2;
			bookdistance = (far + near) / 2;
			gl.glFrustumf(-w2V, w2V, -h2V, h2V, near, far); //width * 10); //100);

			//设置视窗大小及位置
			//gl.glViewport(0, 0, width, height);

			//glMatrixMode(GL10.GL_MODELVIEW);

			//设置为关闭背面剪裁
			gl.glDisable(GL10.GL_CULL_FACE);
		}

		@Override
		public void onSurfaceCreated(GL10 gl, EGLConfig config)
		{

//    		//关闭抗抖动
//        	gl.glDisable(GL10.GL_DITHER);
//        	//设置特定Hint项目的模式，这里为设置为使用快速模式
//            gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT,GL10.GL_FASTEST);

			gl.glShadeModel(GL10.GL_SMOOTH);// 启用阴影平滑
			gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);// 真正精细的透视修正

			//设置屏幕背景色黑色RGBA
			gl.glClearColor(0,0,0,0);
			//启用深度测试
			gl.glEnable(GL10.GL_DEPTH_TEST);

			// 消除锯齿设置
			//antiAliased(gl);

			// 加载n幅纹理图
			// textureIds[0]=initTexture(gl,R.drawable.fj1, 0);//图片1
			// textureIds[1]=initTexture(gl,R.drawable.fj2, 0);//图片2
			// textureIds[2]=initTexture(gl,R.drawable.fj3, 0);//图片3
			// textureIds[3]=initTexture(gl,R.drawable.fj4, 0);//图片4
			// textureIds[4]=initTexture(gl,R.drawable.fj5, 0);//图片5
			// textureIds[5]=initTexture(gl,R.drawable.fj6, 0);//图片6
			// textureIds[6]=initTexture(gl,R.drawable.fj7, 0);//图片7
			// textureIds[7]=initTexture(gl,R.drawable.fj8, 0);//图片8
			// textureIds[8]=initTexture(gl,R.drawable.fj9, 0);//图片9
			// textureIds[9]=initTexture(gl,R.drawable.fj10, 0);//图片10
			// textureIds[10]=initTexture(gl,R.drawable.fj11, 0);//图片11
			// textureIds[11]=initTexture(gl,R.drawable.fj12, 0);//图片12
			// textureIds[12]=initTexture(gl,R.drawable.fj13, 0);//图片13
			// textureIds[13]=initTexture(gl,R.drawable.fj14, 0);//图片14
			// for(int i = 0; i < PicViewCount; i++)

			// 分配纹理空间
			if (ActTextSetSize == 0)
			{
				initTexture_alloc(gl, MaxTextSetSize, pv);
				if (ActTextSetSize == 0)
				{
					Log.i("wg", "initTexture_allo(), fail");
					return;
				}
			}

			// 加载图片到纹理
			if (LoadAllTextureSwitch > 0)            // 根据配置决定是否动态加载，影响启动时间
			{
				int TexturePageId = 0;	// 书中的首页在第一张纸的北背面, 因此pv的号与书中的页号差1
				for (int i = 0; i < PicViewCount; i++) // i is pvId
				{
					TexturePageId = pvId2TextPageId(i);
					synchronized(pv[i])
					{
						pv[i].mPvTextureId = i;
						mTextureSet_PvId[i] = i;

						Log.i("wg", "DrawAPage(), initTexture2(), pvId:"+ i);	// test

						textureIds[TexturePageId] = initTexture2(gl, i, mTextureSet[i], i);
					}
				}
			}
			else
			{
				// 无论配置如何，头两页要先加载
				int TexturePageId = 0;	// 书中的首页在第一张纸的北背面, 因此pv的号与书中的页号差1
				for (int i = 0; i < PicViewCount
						&& i < 2; i++) // i is pvId
				{
					TexturePageId = pvId2TextPageId(i);
					//textureIds[TexturePageId] = initTexture(gl, i, 1);
					synchronized(pv[i])
					{
						pv[i].mPvTextureId = i;
						mTextureSet_PvId[i] = i;
						textureIds[TexturePageId] = initTexture2(gl, i, mTextureSet[i], i);
					}
				}
			}

		}
	}

	public int DecideTextId(float paperAngle, int texId, int texId2)
	{
		int texIdSelect = -1; //texId2;
		if ((paperAngle > 270 && paperAngle <= 360)
				|| (paperAngle >= 0 && paperAngle < 90))
		{
			texIdSelect = texId;
		}
		else
		{
			texIdSelect = texId2;
		}

		return texIdSelect;
	}

	// 决定是否左右翻转贴图
	public int DecideTextReverse(float paperAngle)
	{
		int Reverse = 0;
		if ((paperAngle > 270 && paperAngle <= 360)
				|| (paperAngle >= 0 && paperAngle < 90))
		{

		}
		else
		{
			Reverse = 1;
		}

		return Reverse;
	}

	// 把pvID转换成3D图中的角下页号ID
	public int pvId2TextPageId(int pvId)
	{
		int PageId;

		PageId = pvId + 1;

		return PageId;
	}
	// 把3D图中的角下页号ID转换成pvID
	public int TextPageId2pvId(int pageId)
	{
		int pvId;

		pvId = pageId - 1;

		return pvId;
	}

	/**
	 * 向上取最接近iint的2的幂次数.比如iint=230时,返回256
	 * @param iint
	 * @return
	 */
	private int up2int2(int iint) {
		int ret = 1;
		while (ret<=iint) {
			ret = ret << 1;
		}
		return ret; //>>1;
	}

	// 一次性分配最大可用数量的纹理
	// 把gl中的内部纹理号存放到mTextureSet中
	public int initTexture_alloc(GL10 gl, int MaxPageCount, PicView pv[])
	{
		Bitmap bitmapTmp;

		if (MaxPageCount > MaxTextSetSize)
		{
			return 0;
		}
		//ActTextSetSize = MaxPageCount;

		//mTexture_pv2Set_Map2


		// 用白板位图填充纹理
		int w2n = pv[0].ImgWidth;
		int h2n = pv[0].ImgHeight;
		if (OpenGl_2N_switch > 0)	// 要求位图边长是2n
		{
			w2n = up2int2(w2n);
			h2n = up2int2(h2n);
		}
		bitmapTmp = Bitmap.createBitmap(w2n, h2n, BmpConfig); //reverseBitmap(pv[pvId].mBitmapPeer, 0); // 左右翻转
		Canvas piccanvasPeer = new Canvas(bitmapTmp);
		//piccanvasPeer.drawColor(Color.rgb(pv[0].BackgoundR, pv[0].BackgoundG, pv[0].BackgoundB));
		piccanvasPeer.drawColor(Color.rgb(255, 0, 0));

		int i = 0;
		for(i = 0; i < MaxPageCount; i++)
		{
			int[] textures = new int[1];
			gl.glGenTextures(1, textures, 0);    //生成纹理ID: textures, currTextureId

			int currTextureId = textures[0];
			if (currTextureId <= 0)
			{
				//break;
			}
			mTextureSet[i] = currTextureId;	///////////// 记录到纹理数组
			//mTexture_pv2Set_Map[i] = -1;

			gl.glBindTexture(GL10.GL_TEXTURE_2D, currTextureId);

			if (false)
			{
//    			gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER,GL10.GL_NEAREST);
//    	        gl.glTexParameterf(GL10.GL_TEXTURE_2D,GL10.GL_TEXTURE_MAG_FILTER,GL10.GL_LINEAR);
				gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S,GL10.GL_CLAMP_TO_EDGE);
				gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T,GL10.GL_CLAMP_TO_EDGE);

				// 线性滤波
				gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER,GL10.GL_NEAREST);
				gl.glTexParameterf(GL10.GL_TEXTURE_2D,GL10.GL_TEXTURE_MAG_FILTER,GL10.GL_NEAREST_MIPMAP_NEAREST);
//    	        gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER,
//    	                GL10.GL_NEAREST_MIPMAP_NEAREST);
//    	        gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER,
//    	                GL10.GL_NEAREST_MIPMAP_NEAREST);
			}
			else
			{
				// 设置2D纹理通道当前绑定的纹理的属性
				gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER,
						GL10.GL_NEAREST);
				gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER,
						GL10.GL_LINEAR);
				gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S,
						GL10.GL_REPEAT);
				gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T,
						GL10.GL_REPEAT);
			}

			GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmapTmp, 0);
		}
		ActTextSetSize = i;

		bitmapTmp.recycle();

		return 1;
	}

	private void antiAliased(GL10 gl) {
		gl.glPointSize(8);
		gl.glLineWidth(5);

		gl.glEnable(GL10.GL_POINT_SMOOTH);
		gl.glEnable(GL10.GL_LINE_SMOOTH);
		gl.glHint(GL10.GL_POINT_SMOOTH_HINT, GL10.GL_NICEST);
		gl.glHint(GL10.GL_LINE_SMOOTH_HINT, GL10.GL_NICEST);
		gl.glEnable(GL10.GL_BLEND);
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
	}

	// 初始化纹理: 把pvId的图片生成纹理， 存放到TextTureId对应的纹理中
	public int initTexture2(GL10 gl, int pvId, int TextTureId, int AppTextId)
	{
		if (pvId < 0)
		{
			return 0;
		}

		if (AppTextId >= MaxTextSetSize	//TextTureId
//				|| TextTureId < 0
				)
		{
			return 0;
		}

		//生成纹理ID
		int currTextureId = TextTureId;
//		int[] textures = new int[1];
//		gl.glGenTextures(1, textures, 0);    //生成纹理ID: textures, currTextureId
//		int currTextureId = textures[0];
//		gl.glBindTexture(GL10.GL_TEXTURE_2D, currTextureId);
		gl.glBindTexture(GL10.GL_TEXTURE_2D, TextTureId);

		if (false)
		{
//			gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER,GL10.GL_NEAREST);
//	        gl.glTexParameterf(GL10.GL_TEXTURE_2D,GL10.GL_TEXTURE_MAG_FILTER,GL10.GL_LINEAR);
			gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S,GL10.GL_CLAMP_TO_EDGE);
			gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T,GL10.GL_CLAMP_TO_EDGE);

			// 线性滤波
			gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER,GL10.GL_NEAREST);
			gl.glTexParameterf(GL10.GL_TEXTURE_2D,GL10.GL_TEXTURE_MAG_FILTER,GL10.GL_NEAREST_MIPMAP_NEAREST);
//	        gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER,
//	                GL10.GL_NEAREST_MIPMAP_NEAREST);
//	        gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER,
//	                GL10.GL_NEAREST_MIPMAP_NEAREST);
		}
		else
		{
			// 设置2D纹理通道当前绑定的纹理的属性
			gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER,
					GL10.GL_NEAREST);
			gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER,
					GL10.GL_LINEAR);
			gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S,
					GL10.GL_REPEAT);
			gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T,
					GL10.GL_REPEAT);
		}

		Bitmap bitmapTmp = null;
		Bitmap bitmap2n = null;
		int w2n = pv[0].ImgWidth;
		int h2n = pv[0].ImgHeight;

		int mod = 1;
		if (mod == 0) 	// RawResource
		{
			InputStream is = this.getResources().openRawResource(R.drawable.lambskin01);
			try
			{
				bitmapTmp = BitmapFactory.decodeStream(is);
				if (OpenGl_2N_switch > 0)	// 要求位图边长是2n
				{
					w2n = up2int2(w2n);
					h2n = up2int2(h2n);

					bitmap2n = zoomBitmap(bitmapTmp, (int)(w2n), (int)(h2n));

					GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap2n, 0);
					//pv[0].SaveBitmap("/mnt/sdcard/epcach/x.png", bitmapTmp, ".png"); // test
					bitmapTmp.recycle();
					bitmap2n.recycle();
				}
				else
				{
					GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmapTmp, 0);
					//pv[0].SaveBitmap("/mnt/sdcard/epcach/x.png", bitmapTmp, ".png"); // test
					bitmapTmp.recycle();
				}
			}
			finally
			{
				try
				{
					is.close();
				}
				catch(IOException e)
				{
					e.printStackTrace();
				}
			}

		}
		else
		{
			if (pvId < PicViewCount && pv[pvId] != null)
			{
				//synchronized(pv[pvId])	/////////
				{
					//GLES20.glTextParameteri(GL_TEXTURE_2D，GL_TEXTURE_MAX_FILTER，“纹理过滤模式”)；

					if (!pv[pvId].isBmpValid())
					{
						long tick = System.currentTimeMillis();
						int r = pv[pvId].LoadCach();			// 先从文件缓冲加载
						tick = System.currentTimeMillis() - tick;
						Log.i("WG", "initTexture2(), LoadCach(), loading pic:" + pvId + ". duration:" + tick);

						if (r == 0)
						{
							LoadPagesToPicView(pvId, pvId);
						}
					}

					if (pv[pvId].isBmpValid())
					{
						if (OpenGl_2N_switch > 0)	// 要求位图边长是2n
						{
							w2n = up2int2(w2n);
							h2n = up2int2(h2n);

							bitmap2n = zoomBitmap(pv[pvId].mBitmapPeer, (int)(w2n), (int)(h2n));

							// 正常, 不用翻转
							GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap2n, 0);

//	    	    			if (pvId % 2 == 1)
//			        		{
//			        			// 正常, 不用翻转
//			        			GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap2n, 0);
//			        		}
//			        		else
//			        		{
//			        			// 反面页，左右翻转
//
//			        			Bitmap bmporg = bitmap2n;
//			        			bitmapTmp = reverseBitmap(bmporg, 0); // 左右翻转
//				        		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmapTmp, 0);
//				        		bitmapTmp.recycle();
//			        		}
						}
						else
						{
							long tick = System.currentTimeMillis();

							// 正常, 不用翻转
							GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, pv[pvId].mBitmapPeer, 0);

							// 以下代码性能太低：
//	    	        		if (pvId % 2 == 1)
//			        		{
//			        			// 正常, 不用翻转reverseBitmap()耗时300ms。改为在opengl贴图时进行左右翻转（位置: tp.drawSelf())
//			        			GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, pv[pvId].mBitmapPeer, 0);
//
//								tick = System.currentTimeMillis() - tick;
//								Log.i("WG", "initTexture2(), texImage2D(), loading pic:" + pvId + ". duration:" + tick);
//			        		}
//			        		else
//			        		{
//			        			// 反面页，左右翻转
//
//			        			Bitmap bmporg = pv[pvId].mBitmapPeer;
//			        			bitmapTmp = reverseBitmap(bmporg, 0); // 左右翻转
//				        		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmapTmp, 0);
//				        		bitmapTmp.recycle();
//
//								tick = System.currentTimeMillis() - tick;
//								Log.i("WG", "initTexture2(), reverseBitmap(), loading pic:" + pvId + ". duration:" + tick);
//			        		}


						}


						// 回收
//		        		pv[pvId].mBitmapPeer.recycle();
//		        		pv[pvId].mBitmapPeer = null;
					}
					else
					{
						Log.i("WG", "initTexture2(), LoadPages2PicView(), warning, drawableId:" + pvId);
					}
				}
			}
			else
			{
				// 用白板位图填充纹理
				bitmapTmp = Bitmap.createBitmap(pv[0].ImgWidth, pv[0].ImgHeight, BmpConfig); //reverseBitmap(pv[pvId].mBitmapPeer, 0); // 左右翻转
				Canvas piccanvasPeer = new Canvas(bitmapTmp);

				//piccanvasPeer.drawColor(Color.rgb(pv[0].BackgoundR, pv[0].BackgoundG, pv[0].BackgoundB));
				if (pv[0] != null)
				{
					if (pv[0].mBitmapPeerBG != null)
					{
						//mBitmapPeer = mBitmapPeerBG.copy(config, isMutable);
						piccanvasPeer.drawBitmap(pv[0].mBitmapPeerBG, 0, 0, null);
					}
				}

				GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmapTmp, 0);
				bitmapTmp.recycle();
			}
		}

		return currTextureId;
	}

	//初始化纹理, 老方式
	// mod: 0_RawResource; 1_Page
	public int initTexture(GL10 gl,int drawableId, int mod)//textureId
	{
		if (drawableId < 0)
		{
			return 0;
		}

		//生成纹理ID
		int[] textures = new int[1];
		gl.glGenTextures(1, textures, 0);    //生成纹理ID: textures, currTextureId
		int currTextureId = textures[0];
		gl.glBindTexture(GL10.GL_TEXTURE_2D, currTextureId);

//		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER,GL10.GL_NEAREST);
//        gl.glTexParameterf(GL10.GL_TEXTURE_2D,GL10.GL_TEXTURE_MAG_FILTER,GL10.GL_LINEAR);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S,GL10.GL_CLAMP_TO_EDGE);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T,GL10.GL_CLAMP_TO_EDGE);

		// 线性滤波
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER,GL10.GL_NEAREST);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D,GL10.GL_TEXTURE_MAG_FILTER,GL10.GL_NEAREST_MIPMAP_NEAREST);
//        gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER,
//                GL10.GL_NEAREST_MIPMAP_NEAREST);
//        gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER,
//                GL10.GL_NEAREST_MIPMAP_NEAREST);

		Bitmap bitmapTmp = null;
		if (mod == 0) 	// RawResource
		{
			InputStream is = this.getResources().openRawResource(drawableId);
			try
			{
				bitmapTmp = BitmapFactory.decodeStream(is);
			}
			finally
			{
				try
				{
					is.close();
				}
				catch(IOException e)
				{
					e.printStackTrace();
				}
			}

			GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmapTmp, 0);
			bitmapTmp.recycle();
		}
		else if (mod == 1)
		{
			// ...
			int pvId = drawableId;

			if (pvId < PicViewCount && pv[pvId] != null)
			{
				synchronized(pv[pvId])	/////////
				{
					//GLES20.glTextParameteri(GL_TEXTURE_2D，GL_TEXTURE_MAX_FILTER，“纹理过滤模式”)；

					if (!pv[pvId].isBmpValid())
					{
						Log.i("WG", "initTexture(), LoadPages2PicView(), loading pic:" + pvId);
						LoadPagesToPicView(pvId, pvId);
					}

					if (pv[pvId].isBmpValid())
					{
						//bitmapTmp = pv[drawableId].mBitmapPeer;
						if (pvId % 2 == 1)
						{
							// 正常, 不用翻转
							GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, pv[pvId].mBitmapPeer, 0);
						}
						else
						{
							// 反面页，左右翻转

							bitmapTmp = reverseBitmap(pv[pvId].mBitmapPeer, 0); // 左右翻转
							GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmapTmp, 0);
							bitmapTmp.recycle();
						}

//		        		// 回收
//		        		pv[pvId].mBitmapPeer.recycle();
//		        		//pv[drawableId].mBitmapPeer = null;
					}
					else
					{
						Log.i("WG", "initTexture(), LoadPages2PicView(), warning, drawableId:" + drawableId);
					}
				}
			}
			else
			{
				// 用白板位图填充纹理
				bitmapTmp = Bitmap.createBitmap(pv[0].ImgWidth, pv[0].ImgHeight, BmpConfig); //reverseBitmap(pv[pvId].mBitmapPeer, 0); // 左右翻转
				Canvas piccanvasPeer = new Canvas(bitmapTmp);

				piccanvasPeer.drawColor(Color.rgb(pv[0].BackgoundR, pv[0].BackgoundG, pv[0].BackgoundB));

				GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmapTmp, 0);
				bitmapTmp.recycle();
			}
		}

		return currTextureId;
	}

	// 加载页面 synchronized
	public int LoadPagesToPicView(int startpage, int endpage)
	{
		if (endpage >= PicViewCount)
		{
			Log.i("WG", "LoadPages(), error, endpage is too big, " + endpage);

			return 0;
		}

		// 查询组图指令
		byte drawoderset[];
		for(int i = startpage; i <= endpage; i++)
		{
			Log.i("WG", "LoadPages(), loading pv:" + i);

			if (pv[i] == null)
			{
				continue;
			}

			drawoderset = ObEPianoAndroidJavaAPI.QueryDrawOderSet(i);
			//if (drawoderset.length > 0)
			{
				//pv[i].DrawOderSet = new byte[drawoderset.length];
				pv[i].DrawOderSet = drawoderset;
			}
			if (drawoderset.length > 0)
			{
				pv[i].ReDraw = 1;
				//pv[i].drawwgFromMem();

				// 通知java绘制
				pv[i].render();
				//pv[i].postInvalidate(); //0, 0, PicViewWidthOrg, PicViewHeightOrg);
				pv[i].DrawOderSet = null;
			}
		}

		return 1;
	}

	// Drawable缩放
	public Bitmap CopyBmp(Bitmap bmp) {
		int width = bmp.getWidth();
		int height = bmp.getHeight();
		Bitmap oldbmp = bmp;
		// 创建操作图片用的Matrix对象
		Matrix matrix = new Matrix();
		// 计算缩放比例
		float sx = 1; //((float) w / width);
		float sy = 1; //((float) h / height);
		// 设置缩放比例
		matrix.postScale(sx, sy);

		// 建立新的bitmap，其内容是对原bitmap的缩放后的图
		Bitmap newbmp = Bitmap.createBitmap(oldbmp, 0, 0, width, height,
				matrix, true);
		return newbmp;
	}

	public static Bitmap reverseBitmap(Bitmap bmp, int flag) {
		float[] floats = null;
		switch (flag) {
			case 0: // 水平反转
				floats = new float[] { -1f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f };
				break;
			case 1: // 垂直反转
				floats = new float[] { 1f, 0f, 0f, 0f, -1f, 0f, 0f, 0f, 1f };
				break;
		}

		if (floats != null) {
			Matrix matrix = new Matrix();
			matrix.setValues(floats);
			int w = bmp.getWidth();
			int h = bmp.getHeight();
			Bitmap bmpout = Bitmap.createBitmap(bmp, 0, 0, w, h, matrix, true);
			return bmpout;
		}

		return null;
	}

	// test
	public class GLRender implements Renderer{
		int one = 255; //0x10000;
		private IntBuffer colorBuffer = IntBuffer.wrap(new int[]{
				one,0,0,one,
				0,one,0,one,
				0,0,one,one
		});
		//三角形三个顶点
		private IntBuffer triggerBuffer = IntBuffer.wrap(new int[]{
				0,one,0,		//上顶点
				-one,-one,0,    //左下点
				one,-one,0,});  //右下点
		//正方形的4个顶点
		private IntBuffer quaterBuffer = IntBuffer.wrap(new int[]{
				one,one,0,
				-one,one,0,
				one,-one,0,
				-one,-one,0});

		public void onDrawFrame(GL10 gl) {
			Log.d(this.getClass().getName(), "onDrawFrame.........");
			// 清除屏幕和深度缓存
			gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

			//  三角形
			// 重置当前的模型观察矩阵
			gl.glLoadIdentity();
			// 左移 1.5 单位，并移入屏幕 6.0
			gl.glTranslatef(-1.5f, 0.0f, -6.0f);
			// 允许设置顶点
			gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
			//设置颜色数组
			gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
			// 设置三角形
			int trigvertexs_x[] = {
					0,one,0,		//上顶点
					-one,-one,0,    //左下点
					one,-one,0};
			//IntBuffer ClrBuffer;
			triggerBuffer = getIntBuffer(trigvertexs_x);
			//IntBuffer ClrBuffer;
			gl.glVertexPointer(3, GL10.GL_FIXED, 0, triggerBuffer);
			//对三角形平滑着色
			int Clrvertexs_x[] = {
					one,0,0,one,
					0,one,0,one,
					0,0,one,one};
			colorBuffer = getIntBuffer(Clrvertexs_x);
			gl.glColorPointer(3, GL10.GL_FIXED, 0, colorBuffer);
			//绘制三角形
			gl.glDrawArrays(GL10.GL_TRIANGLES, 0, 3);
			//关闭颜色数组
			gl.glDisableClientState(GL10.GL_COLOR_ARRAY);


//			//  正方形
//			// 重置当前的模型观察矩阵
//			gl.glLoadIdentity();
//			// 左移 1.5 单位，并移入屏幕 6.0
//			gl.glTranslatef(1.5f, 0.0f, -6.0f);
//			//设置和绘制正方形
//			gl.glVertexPointer(3, GL10.GL_FIXED, 0, quaterBuffer);
//			gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);
//			//对正方形单调着色
//			gl.glColor4f(0.5f, 0.5f, 1.0f, 1.0f);
			// 取消顶点设置
			gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);

		}

		public void onSurfaceChanged(GL10 gl, int width, int height) {
			Log.d(this.getClass().getName(), "onSurfaceChanged.........");
			float ratio = (float) width / height;
			//设置OpenGL场景的大小
			gl.glViewport(0, 0, width, height);
			//设置投影矩阵
			gl.glMatrixMode(GL10.GL_PROJECTION);
			//重置投影矩阵
			gl.glLoadIdentity();
			// 设置视图的大小
			gl.glFrustumf(-ratio, ratio, -1, 1, 1, 10);
			// 选择模型观察矩阵
			gl.glMatrixMode(GL10.GL_MODELVIEW);
			// 重置模型观察矩阵
			gl.glLoadIdentity();
		}

		public void onSurfaceCreated(GL10 gl, EGLConfig config) {
			Log.d(this.getClass().getName(), "onSurfaceCreated.........");
			// 启用阴影平滑
			gl.glShadeModel(GL10.GL_SMOOTH);
			// 黑色背景
			gl.glClearColor(0, 0, 0, 0);
			// 设置深度缓存
			gl.glClearDepthf(1.0f);
			// 启用深度测试
			gl.glEnable(GL10.GL_DEPTH_TEST);
			// 所作深度测试的类型
			gl.glDepthFunc(GL10.GL_LEQUAL);
			// 告诉系统对透视进行修正
			gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_FASTEST);
		}

	}

	// 把3d视图中的x映射为picview中的x
	public int xMapFromView3D2PicView(Board tp, int LeftOrRight, int xIn3dView, int yIn3dView, int PicViewW)
	{
		// 获取左页左上角坐标
		int xInPicView = -1;

		//int LeftOrRight = tp.InLeftOrRight(xIn3dView, yIn3dView);

		float kx = (float) PicViewW / tp.width;
		if (LeftOrRight == -1)
		{
			return -1;
		}
		else if (LeftOrRight == 0)
		{
			// left
			xInPicView = (int)((xIn3dView - tp.GetLeftX0()) * kx);

			//xInPicView = PicViewW - xInPicView;		// 左右翻转
		}
		else if (LeftOrRight == 1)
		{
			// right
			xInPicView = (int)((xIn3dView - tp.GetRightX0()) * kx);
		}

		return xInPicView;
	}

	// 把3d视图中的y映射为picview中的y
	public int yMapFromView3D2PicView(Board tp, int LeftOrRight, int xIn3dView, int yIn3dView, int PicViewH)
	{
		// 获取左页左上角坐标
		int yInPicView = -1;

		//int LeftOrRight = tp.InLeftOrRight(xIn3dView, yIn3dView);

		float ky = (float) PicViewH / (tp.height * 2);
		if (LeftOrRight == -1)
		{
			return -1;
		}
		else if (LeftOrRight == 0)
		{
			// left
			yInPicView = (int)((yIn3dView - tp.GetLeftY0()) * ky);
		}
		else if (LeftOrRight == 1)
		{
			// right
			yInPicView = (int)((yIn3dView - tp.GetRightY0()) * ky);
		}

		yInPicView = yInPicView; // - PicViewH / 2;

		return yInPicView;
	}

	// 把3d视图中的x映射为picview中的x, 逆
	public int xMapFromView3D2PicView_R(Board tp, int LeftOrRight, int xInPicView, int yInPicView, int PicViewW)
	{
		// 获取左页左上角坐标
		int xIn3DView = -1;

		//int LeftOrRight = tp.InLeftOrRight(xIn3dView, yIn3dView);

		//int View3dW = tp.GetPageWidth();
		float kx = (float) tp.width / PicViewW;
		if (LeftOrRight == -1)
		{
			return -1;
		}
		else if (LeftOrRight == 0)
		{
			// left
			xIn3DView = (int)((xInPicView) * kx) + tp.GetLeftX0();
		}
		else if (LeftOrRight == 1)
		{
			// right
			xIn3DView = (int)((xInPicView) * kx) + tp.GetRightX0();
		}

		return xIn3DView;
	}

	// 把3d视图中的y映射为picview中的y, 逆
	public int yMapFromView3D2PicView_R(Board tp, int LeftOrRight, int xInPicView, int yInPicView, int PicViewH)
	{
		// 获取左页左上角坐标
		int yIn3DView = -1;

		//int LeftOrRight = tp.InLeftOrRight(xIn3dView, yIn3dView);

		float ky = (float) (tp.height * 2) / PicViewH;
		if (LeftOrRight == -1)
		{
			return -1;
		}
		else if (LeftOrRight == 0)
		{
			// left
			yIn3DView = (int)((yInPicView) * ky) + tp.GetLeftY0();
		}
		else if (LeftOrRight == 1)
		{
			// right
			yIn3DView = (int)((yInPicView) * ky) + tp.GetRightY0();
		}

		return yIn3DView;
	}

	//获取浮点形缓冲数据
	public static FloatBuffer getFloatBuffer(float[] vertexs)
	{
		FloatBuffer buffer;

		ByteBuffer qbb = ByteBuffer.allocateDirect(vertexs.length * 4);
		qbb.order(ByteOrder.nativeOrder());
		buffer = qbb.asFloatBuffer();
		buffer.put(vertexs);
		buffer.position(0);

		return buffer;
	}

	//获取整形缓冲数据
	public static IntBuffer getIntBuffer(int[] vertexs)
	{
		IntBuffer buffer;

		ByteBuffer qbb = ByteBuffer.allocateDirect(vertexs.length * 4);
		qbb.order(ByteOrder.nativeOrder());
		buffer = qbb.asIntBuffer();
		buffer.put(vertexs);
		buffer.position(0);

		return buffer;
	}

	// 鼠标线
	private void DrawLine3d(GL10 gl, int x0, int y0, int x1, int y1, int LeftOrRight, int linew, int R, int G, int B, int A)
	{
		gl.glLineWidth(linew);
		//gl.glEnable(GL10.GL_COLOR_MATERIAL);

		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL10.GL_COLOR_ARRAY);

		// color
		int one = 0x10000;
		int Clrvertexs_x[] = {
				one,0,0,one,
				one,0,0,one};
		Clrvertexs_x[0] = R;
		Clrvertexs_x[1] = G;
		Clrvertexs_x[2] = B;
		Clrvertexs_x[3] = A;
		Clrvertexs_x[4] = R;
		Clrvertexs_x[5] = G;
		Clrvertexs_x[6] = B;
		Clrvertexs_x[7] = A;

		IntBuffer ClrBuffer;
		ClrBuffer = getIntBuffer(Clrvertexs_x);
//			    IntBuffer ClrBuffer = IntBuffer.wrap(new int[]{
//			    		1,0,0,1,
//			    		1,0,0,1,
//			    });
		gl.glColorPointer(4, GL10.GL_FIXED, 0, ClrBuffer);	// GL_UNSIGNED_BYTE


		if (LeftOrRight == 0)
		{
			// left
			float linevertexs_x[] = {x0,y0, -2,x1,y1, -2};	// 5: 让线条在页面上悬空
			FloatBuffer PlaneBuffer;
			PlaneBuffer = getFloatBuffer(linevertexs_x);
			//PlaneBuffer = FloatBuffer.wrap(new float[]{(float)x0,(float)y0, 1f,(float)x1,(float)y1, 100f});
			gl.glVertexPointer(3, GL10.GL_FLOAT,0, PlaneBuffer);	// gl.GL_FLOAT
			gl.glDrawArrays(GL10.GL_LINES,0, 2);
		}
		else if (LeftOrRight == 1)
		{
			// right
			float linevertexs_x[] = {x0,y0, 2,x1,y1, 2};
			FloatBuffer PlaneBuffer;
			PlaneBuffer = getFloatBuffer(linevertexs_x);
			//PlaneBuffer = FloatBuffer.wrap(new float[]{(float)x0,(float)y0, 1f,(float)x1,(float)y1, 100f});
			gl.glVertexPointer(3, GL10.GL_FLOAT,0, PlaneBuffer);	// gl.GL_FLOAT
			gl.glDrawArrays(GL10.GL_LINES,0, 2);
		}

		gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);

		return;
	}

//	public int PageY2View3DY(int XInPage, int YInPage, int iPageId)
//	{
//		int LeftOrRight;
//
//		int y0;
//		y0 = yMapFromView3D2PicView_R(tp, LeftOrRight, XInPage, YInPage, pv[iPageId].mBitmapPeerRct.bottom);
//		y0 = -(y0 - (int)tp.height);	// "-()", 为什么要上下颠倒?
//
//		return y0;
//	}

	public int RemoveCach(int iPageId)
	{
		String BmpCachDir = "/mnt/sdcard/epcach/";
		//String BmpCachDir = "/data/data/android.epiano.com/files/matter/epcach/";

		String filename = String.valueOf(iPageId);
		String pathFileName = BmpCachDir + filename + ".png";

		deleteFile(pathFileName);

		return 1;
	}

	/**
	 * 删除单个文件
	 * @param   filePath    被删除文件的文件名
	 * @return 文件删除成功返回true，否则返回false
	 */
	public boolean deleteFile(String filePath) {
		File file = new File(filePath);
		if (file.isFile() && file.exists()) {
			return file.delete();
		}
		return false;
	}

	// Bitmap缩放
	public static Bitmap zoomBitmap(Bitmap bitmap, int width, int height) {
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();
		Matrix matrix = new Matrix();
		float scaleWidth = ((float) width / w);
		float scaleHeight = ((float) height / h);
		matrix.postScale(scaleWidth, scaleHeight);
		Bitmap newbmp = Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);
		return newbmp;
	}
}


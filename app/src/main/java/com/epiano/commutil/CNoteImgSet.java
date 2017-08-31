package com.epiano.commutil;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;


// Audio素材加载, 立体声合成

public class CNoteImgSet {

	//ENUM_NOTE
	Bitmap mBmps[] = new Bitmap[ENUM_NOTE.NOTE_BUTT.ordinal()];
	int BmpsResId[] = new int[ENUM_NOTE.NOTE_BUTT.ordinal()];
	Context mContext;

	public int BackgoundR = 0xff;
	public int BackgoundG = 0xfa;
	public int BackgoundB = 0xe9;

	//String mfilename;
	String filename;

	// 构造函数
	public CNoteImgSet(Context context)
	{
		mContext = context;

		LoadAllBmps();
	}

	// 把背景色改为白色
	public Bitmap MakeWhiteBackground(Bitmap bmp, int R, int G, int B)
	{
		int w = bmp.getWidth();
		int h = bmp.getHeight();

		Config config = Config.ARGB_8888;
		//boolean isMutable = true;
		Bitmap bmpcopy = Bitmap.createBitmap(w, h, config);
		if (bmpcopy == null)
		{
			return null;
		}

		bmpcopy.copy(config, true);

		int bkclr = Color.rgb(R, G, B);
		int Whiteclr = Color.rgb(0xFF, 0xFF, 0xFF);

		for (int i = 0; i < h; i++) {
			for (int j = 0; j < w; j++) {

				int c = bmp.getPixel(j, i);
				if (c == bkclr) {
					bmpcopy.setPixel(j, i, Whiteclr);
				}
				else
				{
					bmpcopy.setPixel(j, i, c);
				}
			}
		}

		bmp.recycle();

		return bmpcopy;
	}

	// 加载所有位图
	public int LoadAllBmps()
	{
//		ENUM_NOTE eNote;
		String pathDir = "/mnt/sdcard/NoteBmp/";

		int i = 0;
		for(i = 0; i < ENUM_NOTE.NOTE_BUTT.ordinal(); i++)
		{
			BmpsResId[i] = GetNoteBmpInfo(i);
			if (BmpsResId[i] != -1)
			{
				if (false)
				{
					// 从drawable目录加载图片方式: 图片大小与手机手关，tbd...

					mBmps[i] = BitmapFactory.decodeResource(mContext.getResources(), BmpsResId[i]);    //获取位图
				}
				else
				{
					// 从文件加载图片
					if (filename != null)
					{
						String pathfileName = pathDir + filename;
						mBmps[i] = BitmapFactory.decodeFile(pathfileName); //获取位图
					}
				}

				// 背影变更为白色, 便于进行图层叠加操作
				if (mBmps[i] != null)
				{
					mBmps[i] = MakeWhiteBackground(mBmps[i], BackgoundR, BackgoundG, BackgoundB);
				}
			}
		}

		return 1;
	}

	//画一行开始处的谱号、节奏号
	public int GetNoteBmpInfo(int eNoteType)
	{
		//Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.pic180);    //获取位图

		//String pathDir = "/mnt/sdcard/NoteBmp/bg/";
		//String filename = "null";

		int drawableId = -1;
		// zoombmplocal = BitmapFactory.decodeResource(this.getContext().getResources(), R.drawable.lambskin07);

		if (eNoteType == (int) ENUM_NOTE.NOTE_4NOTE.ordinal()
				|| eNoteType == (int) ENUM_NOTE.NOTE_8NOTE.ordinal()
				|| eNoteType == (int) ENUM_NOTE.NOTE_16NOTE.ordinal()
				|| eNoteType == (int) ENUM_NOTE.NOTE_32NOTE.ordinal())
		{
			filename = "note4.bmp";
			drawableId = R.drawable.note4;
		}
		else if (eNoteType == (int) ENUM_NOTE.NOTE_NOTE.ordinal())
		{
			filename = "note1.bmp";
			drawableId = R.drawable.note1;
		}
		else if (eNoteType == (int) ENUM_NOTE.NOTE_2NOTE.ordinal())
		{
			filename = "note2.bmp";
			drawableId = R.drawable.note2;
		}
		else if (eNoteType == (int) ENUM_NOTE.NOTE_PAUSE.ordinal())
		{
			filename = "pause1.bmp";
			drawableId = R.drawable.pause1;
		}
		else if (eNoteType == (int) ENUM_NOTE.NOTE_2PAUSE.ordinal())
		{
			filename = "pause2.bmp";
			drawableId = R.drawable.pause2;
		}
		else if (eNoteType == (int) ENUM_NOTE.NOTE_4PAUSE.ordinal())
		{
			filename = "pause4.bmp";
			drawableId = R.drawable.pause4;
		}
		else if (eNoteType == (int) ENUM_NOTE.NOTE_8PAUSE.ordinal())
		{
			filename = "pause8.bmp";
			drawableId = R.drawable.pause8;
		}
		else if (eNoteType == (int) ENUM_NOTE.NOTE_16PAUSE.ordinal())
		{
			filename = "pause16.bmp";
			drawableId = R.drawable.pause16;
		}
		else if (eNoteType == (int) ENUM_NOTE.NOTE_32PAUSE.ordinal())
		{
			filename = "pause32.bmp";
			drawableId = R.drawable.pause32;
		}
		else if (eNoteType == (int) ENUM_NOTE.NOTE_ASCEND.ordinal())
		{
			filename = "ascent.bmp";
			drawableId = R.drawable.ascent;
		}
		else if (eNoteType == (int) ENUM_NOTE.NOTE_ASCEND2.ordinal())
		{
			filename = "ascent2.bmp";
			drawableId = R.drawable.ascent2;
		}
		else if (eNoteType == (int) ENUM_NOTE.NOTE_DESCEND.ordinal())
		{
			filename = "descent.bmp";
			drawableId = R.drawable.descent;
		}
		else if (eNoteType == (int) ENUM_NOTE.NOTE_ADESCEND_RES.ordinal())
		{
			filename = "a_descentrestore.bmp";
			drawableId = R.drawable.a_descentrestore;
		}
		else if (eNoteType == (int) ENUM_NOTE.NOTE_TREMBLE.ordinal())
		{
			filename = "tremble.bmp";
			drawableId = R.drawable.tremble;
		}
		else if (eNoteType == (int) ENUM_NOTE.NOTE_ALTO.ordinal())
		{
			filename = "alto.bmp";
			drawableId = R.drawable.alto;
		}
		else if (eNoteType == (int) ENUM_NOTE.NOTE_BASS.ordinal())
		{
			filename = "bass.bmp";
			drawableId = R.drawable.bass;
		}
		else if (eNoteType == (int) ENUM_NOTE.NOTE_TREMBLE_SMALL.ordinal())
		{
			filename = "tremble_small.bmp";
			drawableId = R.drawable.tremble_small;
		}
		else if (eNoteType == (int) ENUM_NOTE.NOTE_ALTO_SMALL.ordinal())
		{
			filename = "alto_small.bmp";
			drawableId = R.drawable.alto_small;
		}
		else if (eNoteType == (int) ENUM_NOTE.NOTE_BASS_SMALL.ordinal())
		{
			filename = "bass_small.bmp";
			drawableId = R.drawable.bass_small;
		}
		else if (eNoteType == (int) ENUM_NOTE.NOTE_ARPEGGIO.ordinal())
		{
			filename = "arpeggio.bmp";
			drawableId = R.drawable.arpeggio;
		}
		else if (eNoteType == (int) ENUM_NOTE.NOTE_STAGATO1.ordinal())
		{
			filename = "stagato1.bmp";
			drawableId = R.drawable.stagato1;
		}
		else if (eNoteType == (int) ENUM_NOTE.NOTE_STAGATO2.ordinal())
		{
			filename = "stagato2.bmp";
			drawableId = R.drawable.stagato2;
		}
		else if (eNoteType == (int) ENUM_NOTE.NOTE_STAGATO3.ordinal())
		{
			filename = "stagato3.bmp";
			drawableId = R.drawable.stagato3;
		}
		else if (eNoteType == (int) ENUM_NOTE.NOTE_ACCENT.ordinal())
		{
			filename = "accent.bmp";
			drawableId = R.drawable.accent;
		}
		else if (eNoteType == (int) ENUM_NOTE.NOTE_HOOF.ordinal())
		{
			filename = "step.bmp";
			drawableId = R.drawable.step;
		}
		else if (eNoteType == (int) ENUM_NOTE.NOTE_LOOSEHOOF.ordinal())
		{
			filename = "stepoff.bmp";
			drawableId = R.drawable.stepoff;
		}
		else if (eNoteType == (int) ENUM_NOTE.NOTE_0.ordinal())
		{
			filename = "alpha_0_small.bmp";
			drawableId = R.drawable.alpha_0_small;
		}
		else if (eNoteType == (int) ENUM_NOTE.NOTE_1.ordinal())
		{
			filename = "alpha_1_small.bmp";
			drawableId = R.drawable.alpha_1_small;
		}
		else if (eNoteType == (int) ENUM_NOTE.NOTE_2.ordinal())
		{
			filename = "alpha_2_small.bmp";
			drawableId = R.drawable.alpha_2_small;
		}
		else if (eNoteType == (int) ENUM_NOTE.NOTE_3.ordinal())
		{
			filename = "alpha_3_small.bmp";
			drawableId = R.drawable.alpha_3_small;
		}
		else if (eNoteType == (int) ENUM_NOTE.NOTE_4.ordinal())
		{
			filename = "alpha_4_small.bmp";
			drawableId = R.drawable.alpha_4_small;
		}
		else if (eNoteType == (int) ENUM_NOTE.NOTE_5.ordinal())
		{
			filename = "alpha_5_small.bmp";
			drawableId = R.drawable.alpha_5_small;
		}
		else if (eNoteType == (int) ENUM_NOTE.NOTE_6.ordinal())
		{
			filename = "alpha_6_small.bmp";
			drawableId = R.drawable.alpha_6_small;
		}
		else if (eNoteType == (int) ENUM_NOTE.NOTE_7.ordinal())
		{
			filename = "alpha_7_small.bmp";
			drawableId = R.drawable.alpha_7_small;
		}
		else if (eNoteType == (int) ENUM_NOTE.NOTE_8.ordinal())
		{
			filename = "alpha_8_small.bmp";
			drawableId = R.drawable.alpha_8_small;
		}
		else if (eNoteType == (int) ENUM_NOTE.NOTE_9.ordinal())
		{
			filename = "alpha_9_small.bmp";
			drawableId = R.drawable.alpha_9_small;
		}
		else if (eNoteType == (int) ENUM_NOTE.NOTE_s.ordinal())
		{
			filename = "alpha_s.bmp";
			drawableId = R.drawable.alpha_s;
		}
		else if (eNoteType == (int) ENUM_NOTE.NOTE_f.ordinal())
		{
			filename = "alpha_f.bmp";
			drawableId = R.drawable.alpha_f;
		}
		else if (eNoteType == (int) ENUM_NOTE.NOTE_p.ordinal())
		{
			filename = "alpha_p.bmp";
			drawableId = R.drawable.alpha_p;
		}
		else if (eNoteType == (int) ENUM_NOTE.NOTE_3_tripletnote.ordinal())
		{
			filename = "alpha_3_tripletnote.bmp";
			drawableId = R.drawable.alpha_3_tripletnote;
		}
		else if (eNoteType == (int) ENUM_NOTE.NOTE_6_shrink.ordinal())
		{
			filename = "alpha_6_shrink.bmp";
			drawableId = R.drawable.alpha_6_shrink;
		}
		else if (eNoteType == (int) ENUM_NOTE.NOTE_9_shrink.ordinal())
		{
			filename = "alpha_9_shrink.bmp";
			drawableId = R.drawable.alpha_9_shrink;
		}
		else if (eNoteType == (int) ENUM_NOTE.NOTE_12_shrink.ordinal())
		{
			filename = "alpha_12_shrink.bmp";
			drawableId = R.drawable.alpha_12_shrink;
		}
		else if (eNoteType == (int) ENUM_NOTE.NOTE_2_rhythm.ordinal())
		{
			filename = "alpha_2_rhythm.bmp";
			drawableId = R.drawable.alpha_2_rhythm;
		}
		else if (eNoteType == (int) ENUM_NOTE.NOTE_3_rhythm.ordinal())
		{
			filename = "alpha_3_rhythm.bmp";
			drawableId = R.drawable.alpha_3_rhythm;
		}
		else if (eNoteType == (int) ENUM_NOTE.NOTE_4_rhythm.ordinal())
		{
			filename = "alpha_4_rhythm.bmp";
			drawableId = R.drawable.alpha_4_rhythm;
		}
		else if (eNoteType == (int) ENUM_NOTE.NOTE_5_rhythm.ordinal())
		{
			filename = "alpha_5_rhythm.bmp";
			drawableId = R.drawable.alpha_5_rhythm;
		}
		else if (eNoteType == (int) ENUM_NOTE.NOTE_6_rhythm.ordinal())
		{
			filename = "alpha_6_rhythm.bmp";
			drawableId = R.drawable.alpha_6_rhythm;
		}
		else if (eNoteType == (int) ENUM_NOTE.NOTE_7_rhythm.ordinal())
		{
			filename = "alpha_7_rhythm.bmp";
			drawableId = R.drawable.alpha_7_rhythm;
		}
		else if (eNoteType == (int) ENUM_NOTE.NOTE_8_rhythm.ordinal())
		{
			filename = "alpha_8_rhythm.bmp";
			drawableId = R.drawable.alpha_8_rhythm;
		}
		else if (eNoteType == (int) ENUM_NOTE.NOTE_9_rhythm.ordinal())
		{
			filename = "alpha_9_rhythm.bmp";
			drawableId = R.drawable.alpha_9_rhythm;
		}
		else if (eNoteType == (int) ENUM_NOTE.NOTE_12_rhythm.ordinal())
		{
			filename = "alpha_12_rhythm.bmp";
			drawableId = R.drawable.alpha_12_rhythm;
		}
		else if (eNoteType == (int) ENUM_NOTE.NOTE_S1_F_1.ordinal())
		{
			filename = "s1_fingur_1.bmp";
			drawableId = R.drawable.s1_fingur_1;
		}
		else if (eNoteType == (int) ENUM_NOTE.NOTE_S1_F_2.ordinal())
		{
			filename = "s1_fingur_2.bmp";
			drawableId = R.drawable.s1_fingur_2;
		}
		else if (eNoteType == (int) ENUM_NOTE.NOTE_S1_F_3.ordinal())
		{
			filename = "s1_fingur_3.bmp";
			drawableId = R.drawable.s1_fingur_3;
		}
		else if (eNoteType == (int) ENUM_NOTE.NOTE_S1_F_4.ordinal())
		{
			filename = "s1_fingur_4.bmp";
			drawableId = R.drawable.s1_fingur_4;
		}
		else if (eNoteType == (int) ENUM_NOTE.NOTE_S1_F_5.ordinal())
		{
			filename = "s1_fingur_5.bmp";
			drawableId = R.drawable.s1_fingur_5;
		}
		else if (eNoteType == (int) ENUM_NOTE.NOTE_8VA.ordinal())
		{
			filename = "8va.bmp";
			drawableId = R.drawable.va8;
		}
		else if (eNoteType == (int) ENUM_NOTE.NOTE_8VB.ordinal())
		{
			filename = "8vb.bmp";
			drawableId = R.drawable.vb8;
		}
		else if (eNoteType == (int) ENUM_NOTE.NOTE_STEM.ordinal())
		{
			filename = "stem.bmp";
			drawableId = R.drawable.stem;
		}
		else if (eNoteType == (int) ENUM_NOTE.NOTE_SPEED.ordinal())
		{
			filename = "speed.bmp";
			drawableId = R.drawable.speed;
		}
		else if (eNoteType == (int) ENUM_NOTE.NOTE_STICKTAIL_8_UP.ordinal())
		{
			filename = "sticktail_8_up.bmp";
			drawableId = R.drawable.sticktail_8_up;
		}
		else if (eNoteType == (int) ENUM_NOTE.NOTE_STICKTAIL_8_DOWN.ordinal())
		{
			filename = "sticktail_8_down.bmp";
			drawableId = R.drawable.sticktail_8_down;
		}
		else if (eNoteType == (int) ENUM_NOTE.NOTE_STICKTAIL_16_UP.ordinal())
		{
			filename = "sticktail_16_up.bmp";
			drawableId = R.drawable.sticktail_16_up;
		}
		else if (eNoteType == (int) ENUM_NOTE.NOTE_STICKTAIL_16_DOWN.ordinal())
		{
			filename = "sticktail_16_down.bmp";
			drawableId = R.drawable.sticktail_16_down;
		}
		else if (eNoteType == (int) ENUM_NOTE.NOTE_STICKTAIL_32_UP.ordinal())
		{
			filename = "sticktail_32_up.bmp";
			drawableId = R.drawable.sticktail_32_up;
		}
		else if (eNoteType == (int) ENUM_NOTE.NOTE_STICKTAIL_32_DOWN.ordinal())
		{
			filename = "sticktail_32_down.bmp";
			drawableId = R.drawable.sticktail_32_down;
		}
		else if (eNoteType == (int) ENUM_NOTE.NOTE_8NOTE_SUB0.ordinal())
		{
			filename = "note8_sub0.bmp";
			drawableId = R.drawable.note8_sub0;
		}
		else if (eNoteType == (int) ENUM_NOTE.NOTE_8NOTE_SUB.ordinal())
		{
			filename = "note8_sub.bmp";
			drawableId = R.drawable.note8_sub;
		}
		else if (eNoteType == (int) ENUM_NOTE.NOTE_8NOTE_SUB2.ordinal())
		{
			filename = "note8_sub2.bmp";
			drawableId = R.drawable.note8_sub2;
		}
		else if (eNoteType == (int) ENUM_NOTE.NOTE_FINE.ordinal())
		{
			filename = "fine.bmp";
			drawableId = R.drawable.fine;
		}
		else if (eNoteType == (int) ENUM_NOTE.NOTE_BRANKET.ordinal())
		{
			filename = "branket.bmp";
			drawableId = R.drawable.branket;
		}
		else
		{
			// ...
		}

		//mfilename = filename;

		return drawableId;
	}

	//画一行开始处的谱号、节奏号, no use
	public int LoadABmpFromFile(Canvas mCanvas, int iPageId, String filename, int x, int y, int cNoteSelected, float zoomx, float zoomy) // enum ENUM_NOTE int actx, int acty,
	{
		String pathDir = "/mnt/sdcard/NoteBmp/";

		String pathName = pathDir + filename;
		Bitmap bmp = BitmapFactory.decodeFile(pathName); //获取位图
		int w = bmp.getWidth();
		int h = bmp.getHeight();

		//y -= h / 2;

		if (cNoteSelected > 0) {

			Config config = Config.ARGB_8888;
			boolean isMutable = true;
			Bitmap bmpcopy = Bitmap.createBitmap(w, h, config);
			bmpcopy.copy(config, true);

//				Matrix matrix = new Matrix();
//				matrix.postScale(1, 1); // (0.2f, 0.2f);
//				Bitmap bmpcopy = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(),
//						bmp.getHeight(), matrix, true);

			for (int i = 0; i < h; i++) {
				for (int j = 0; j < w; j++) {

					int color = bmp.getPixel(j, i);
					if ((color & 0xFF) < 5) {
						bmpcopy.setPixel(j, i, Color.RED);
					}
				}
			}

			if (zoomx == 1 && zoomy == 1)
			{
				mCanvas.drawBitmap(bmpcopy, x, y, null);
			}
			else
			{
//					int bmpw = bmpcopy.getWidth();
//					int bmph = bmpcopy.getHeight();
//					Matrix matrix = new Matrix();
//					float scaleWidth = ((float) bmpw / w);
//					float scaleHeight = ((float) bmph / h);
//					matrix.postScale(scaleWidth, scaleHeight);


				Bitmap zoombmplocal = zoomBitmap(bmpcopy, (int)(w * zoomx), (int)(h * zoomy));

//					mCanvas.drawBitmap(mBitmapPeer, matrix, mPaint);
				mCanvas.drawBitmap(zoombmplocal, x, y, null);

				zoombmplocal.recycle();
			}

			bmpcopy.recycle();
		}
		else
		{
			if (zoomx == 1 && zoomy == 1)
			{
				mCanvas.drawBitmap(bmp, x, y, null);                       //显示位图
			}
			else
			{
				Bitmap zoombmplocal = zoomBitmap(bmp, (int)(bmp.getWidth() * zoomx), (int)(bmp.getHeight() * zoomy));

				mCanvas.drawBitmap(zoombmplocal, x, y, null);

				zoombmplocal.recycle();
			}
		}

		bmp.recycle();


		return 1;
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

	// 符号定义
	enum ENUM_NOTE
	{
		NOTE_NULL,

		NOTE_NOTE, 				// 全音符 NOTE_S EMIBREVE 1
		NOTE_2NOTE,				// 2分音符	half note
		NOTE_4NOTE,				// 4分音符	quarter note
		NOTE_8NOTE,				// 8分音符
		NOTE_16NOTE,			// 16分音符
		NOTE_32NOTE,			// 32分音符
		NOTE_64NOTE,			// 64分音符
		NOTE_128NOTE,			// 128分音符
		NOTE_256NOTE,			// 256分音符
		//NOTE_TRIPLETENDNOTE,	// 三连音结尾音符，即第四个虚拟音符，把三连音用四三连音表示，第四个音符设置为本音符，表示前三个是三连音

		NOTE_PAUSE,		 		// 全休止符 10
		NOTE_2PAUSE,			// 2分休止符
		NOTE_4PAUSE,			// 4分休止符
		NOTE_8PAUSE,			// 8分休止符
		NOTE_16PAUSE,			// 16分休止符
		NOTE_32PAUSE,			// 32分休止符
		NOTE_64PAUSE,			// 64分休止符
		NOTE_128PAUSE,			// 128分休止符
		NOTE_256PAUSE,			// 256分休止符

		NOTE_DOT,				// 附点 19
		NOTE_DOT2,				// 附点2
		NOTE_DOT3,				// 附点3

		NOTE_ASCEND,			// 升记号 22
		NOTE_ASCEND2,			// 升记号 23
		NOTE_DESCEND,			// 降记号
		NOTE_DESCEND2,			// 双降记号
		NOTE_ADESCEND_RES,		// 还原记号

		NOTE_STICKTAIL_64_UP,	// 向上棍尾 27
		NOTE_STICKTAIL_64_DOWN,	// 向下棍尾
		NOTE_STICKTAIL_32_UP,	// 向上棍尾
		NOTE_STICKTAIL_32_DOWN,	// 向下棍尾
		NOTE_STICKTAIL_16_UP,	// 向上棍尾
		NOTE_STICKTAIL_16_DOWN,	// 向下棍尾
		NOTE_STICKTAIL_8_UP,	// 向上棍尾
		NOTE_STICKTAIL_8_DOWN,	// 向下棍尾

		NOTE_TREMBLE,			// 高音谱号 35
		NOTE_ALTO,				// 中音谱号
		NOTE_BASS,				// 低音谱号
		NOTE_TREMBLE_SMALL,		// 高音谱号 38
		NOTE_ALTO_SMALL,		// 中音谱号
		NOTE_BASS_SMALL,		// 低音谱号

		NOTE_ARPEGGIO,			// 琶音 41

		NOTE_STAGATO1,			// 跳音/短断音，三角	1/4
		NOTE_STAGATO2,			// 跳音/断音, 圆点	1/2
		NOTE_STAGATO3,			// 跳音/保持音
		NOTE_ACCENT,			// 重音
		NOTE_HOOF, 				// 踏踏板
		NOTE_LOOSEHOOF,			// 松踏板

		NOTE_0,					// 字母0 48
		NOTE_1,					// 字母1
		NOTE_2,					// 字母2
		NOTE_3,					// 字母3
		NOTE_4,					// 字母4
		NOTE_5,					// 字母5
		NOTE_6,					// 字母6
		NOTE_7,					// 字母7
		NOTE_8,					// 字母8
		NOTE_9,					// 字母9 57
		NOTE_s,					// 字母s
		NOTE_f,					// 字母f
		NOTE_p,					// 字母p
		NOTE_3_tripletnote,		// 字母3, 三连音 61
		NOTE_6_shrink,			// 字母6, 6连音
		NOTE_9_shrink,			// 字母9, 9连音
		NOTE_12_shrink,			// 字母串12, 12连音
		NOTE_2_rhythm,			// 字母2, 节奏
		NOTE_3_rhythm,			// 字母3, 节奏
		NOTE_4_rhythm,			// 字母4, 节奏 67
		NOTE_5_rhythm,			// 字母4, 节奏
		NOTE_6_rhythm,			// 字母6, 节奏
		NOTE_7_rhythm,			// 字母7, 节奏
		NOTE_8_rhythm,			// 字母8, 节奏
		NOTE_9_rhythm,			// 字母9, 节奏
		NOTE_12_rhythm,			// 字母12, 节奏

		NOTE_S1_F_1,			// 样本1的指法数字1 74
		NOTE_S1_F_2,			// 字母2
		NOTE_S1_F_3,			// 字母3
		NOTE_S1_F_4,			// 字母4
		NOTE_S1_F_5,			// 字母5

		NOTE_8VA,				// 升8度指示 79
		NOTE_8VB,				// 降8度指示
		NOTE_HOR_DASH_END,		// 水平虚线右端
		// 反复数字记号...

		NOTE_STEM,				// 干

		NOTE_BARSTART,			// 83
		NOTE_BARSTART1,
		NOTE_BAREND,
		NOTE_BAREND1,
		NOTE_REPEATSTART,		// 87
		NOTE_REPEATSTART1,
		NOTE_REPEATEND,
		NOTE_REPEATEND1,		// 90
		NOTE_BARDUP,
		NOTE_BARDUP1,
		NOTE_SCOREEND,
		NOTE_SCOREEND1,
		NOTE_BARSTARTMid,		//
		NOTE_SPEED,

		NOTE_8NOTE_SUB0,		// 倚, 纯note
		NOTE_8NOTE_SUB,			// 倚, 带stem
		NOTE_8NOTE_SUB2,
		NOTE_FINE,
		NOTE_SLUR,				// 连线
		NOTE_BRANKET,			// 共括号, 左边

		NOTE_SPEED_0,			// 速率数字
		NOTE_SPEED_1,
		NOTE_SPEED_2,
		NOTE_SPEED_3,
		NOTE_SPEED_4,
		NOTE_SPEED_5,
		NOTE_SPEED_6,
		NOTE_SPEED_7,
		NOTE_SPEED_8,
		NOTE_SPEED_9,
		//NOTE_SPEED_0_b,
		//NOTE_SPEED_2_b,
		//NOTE_SPEED_6_b,

		NOTE_HGRP,				// 横杠
		NOTE_CLEF_UNKOWN,		// 未知clef

		NOTE_STRENGTH_P,		// 弱	103
		NOTE_STRENGTH_MP,		// 中弱
		NOTE_STRENGTH_PP,		// 很弱
		NOTE_STRENGTH_PPP,		// 非常弱
		NOTE_STRENGTH_SP,		// 超弱

		NOTE_STRENGTH_F,		// 强
		NOTE_STRENGTH_MF,		// 中强
		NOTE_STRENGTH_FF,		// 很强
		NOTE_STRENGTH_FFF,		// 非常强
		NOTE_STRENGTH_SF,		// 超强


		NOTE_DECRESCENDO,		// 渐弱decrescendo
		NOTE_CRESCENDO,			// 渐强crescendo

		NOTE_FLAG1,				// 8分flag
		NOTE_FLAG2,				// 16分flag
		NOTE_FLAG3,				// 32分flag
		NOTE_FLAG4,				// 64分flag


		// 升8度
		// 升16度
		// 琶音
		// 颤音
		// 连音


		NOTE_BUTT,
	};
}

package com.epiano.commutil;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.widget.ImageView;

/**
 * Created by John on 2016/11/13 0013.
 */
public class KeyCaptureView extends ImageView {

    KeyCapture mkeyCap;

    Paint mPaint = null;
    Paint mPaint1 = null;
    Paint mPaintNote = null;
    Paint mPaintGray = null;
    Rect mBitmapRct;

    Bitmap mBitmap;
    //Bitmap.Config BmpConfigIn;
    Bitmap.Config BmpConfig = Bitmap.Config.ARGB_8888; //ALPHA_8; // .ARGB_8888;
    boolean DrawInited = false;
    Canvas piccanvas;

    int ViewWidth = 0;
    int ViewHeight = 0;

    static int keyDrawWidth = 8;
    static int BitmapWidth = 0;//1000;
    static int BitmapHeight = (88 + 1) * keyDrawWidth;
    float xScale = 0.1f; //0.06f; 0.03f;
    long scrollNum = 0;

    long T0 = 0;
    long T0_Phone = 0;
    //long SYNC_PHONE_KB_T_DIFF = 0; // 鏈満涓庨敭鐩樻椂閽熺殑宸紓

    public KeyCaptureView(Context context, KeyCapture keyCap, int winW, int winH) {
        super(context);

        mkeyCap = keyCap;


    }

    @Override
    protected void onDraw(Canvas mCanvas)
    {
        //mCanvas = piccanvasPeer;

        onDrawDir(mCanvas);

        super.onDraw(mCanvas);
    }

    public void Kickoff(long t0) {
        T0 = t0; //System.currentTimeMillis();
        T0_Phone = System.currentTimeMillis();
        scrollNum = 0;
        //SYNC_PHONE_KB_T_DIFF = T0_Phone - T0;
    }

    public void ResetScreen()
    {
        piccanvas.drawRect(0, 0, getWidth(), getHeight(), mPaintGray);

        int y = 0;
        for(int i = 0; i < 88; i++)
        {
            y = i * keyDrawWidth + keyDrawWidth / 2;
            piccanvas.drawLine(0, y, mBitmapRct.width(), y, mPaint1);
        }
    }

    public void onDrawDir(Canvas mCanvas) {

        long tick = System.currentTimeMillis();
        //long tick_kb = tick - SYNC_PHONE_KB_T_DIFF;


        int w = this.getWidth();
        int h = this.getHeight();

        // init
        if (!DrawInited)
        {
            DrawInited = true;

            ViewWidth = w;
            ViewHeight = h;

            BitmapWidth = w;
            BitmapHeight = h;

            //keyDrawWidth = 8;
            keyDrawWidth = BitmapHeight / (88 + 1);

//            int w = this.getWidth();
//            int h = this.getHeight();
            //ViewGroup.LayoutParams lp;
            //lp = new ViewGroup.LayoutParams(vw, vh);
            //this.setLayoutParams(lp);

            mPaint = new Paint();
            mPaint.reset();
            mPaint.setStrokeWidth(1);
            mPaint.setColor(0xFF888800);
            mPaint1 = new Paint();//Paint.DITHER_FLAG);
            mPaint1.reset();
            mPaint1.setStrokeWidth(1);
            mPaint1.setColor(0xFF880088);
            mPaintNote = new Paint();
            mPaintNote.reset();
            mPaintNote.setStrokeWidth((int)(keyDrawWidth * 0.8));
            mPaintNote.setColor(0xFF222222);
            mPaintGray = new Paint();
            mPaintGray.reset();
            mPaintGray.setStrokeWidth(1);
            int PenColor = 0xCCCCCC; //0x888888; // gray
            mPaintGray.setColor(PenColor | 0xFF000000);

            mBitmapRct = new Rect();
            mBitmapRct.left = 0;
            mBitmapRct.top = 0;
            mBitmapRct.right = BitmapWidth;
            mBitmapRct.bottom = BitmapHeight;

            mBitmap = Bitmap.createBitmap(BitmapWidth, BitmapHeight, BmpConfig);
            piccanvas = new Canvas(mBitmap);

            ResetScreen();

            //T0 = tick;
        }

//        if (T0 == 0)
//        {
//            mCanvas.drawBitmap(mBitmap, 0, 0, null);
//            return;
//        }


        // draw to screen
//        Matrix matrix = new Matrix();
//        matrix.setTranslate(0,0); //(-OffsetX, 0);
//        mCanvas.drawBitmap(mBitmap, matrix, mPaint);

        // test
        //piccanvas.drawRect(0, 0, 600, 600, mPaintGray);
//        piccanvas.drawLine(0, 0, mBitmapRct.width(), 600, mPaintGray); // mPaint1);
//        piccanvas.drawLine(mBitmapRct.width(), 0, 0, 600, mPaint1); // mPaint1);
//        {
//            int y = 0;
//            for (int i = 0; i < 44; i++) {
//                y = i * keyDrawWidth + keyDrawWidth / 2;
//                piccanvas.drawLine(0, y, mBitmapRct.width(), y, mPaint1);
//            }
//        }

        if (mkeyCap != null)
        {
            KeyCapture.KeyHit khit = null;

            if (T0 == 0) {
                Kickoff(mkeyCap.SYNC_KB_T0);
            }

            float x0, x1, y;
            int s = mkeyCap.KeyListDrawing.size();
            for(int i = s - 1; i >= 0; i--)
            {
                khit= mkeyCap.KeyListDrawing.get(i);

                if (khit.t1 == 0 || khit.drawed == false) {
                    //x0 = (float) ((khit.t_draw - mkeyCap.SYNC_KB_T0) * xScale);
                    //x1 = (float) ((tick_kb - mkeyCap.SYNC_KB_T0) * xScale) + 1;
                    double x0t = ((double)khit.t0     - mkeyCap.SYNC_KB_T0) / mkeyCap.TTR;
                    //x0 = (float) ((x0t + T0_Phone) * xScale);
                    x0 = (float) ((x0t) * xScale);
                    if (khit.t1 == 0) {
                        long te = tick;
                        te = tick - 20;
                        x1 = (float) ((te - T0_Phone) * xScale) + 1;
                        if (x1 < x0) {
                            x1 = x0 + 1;
                        }
                    }
                    else {
                        x1 = (float) (((khit.t1 - mkeyCap.SYNC_KB_T0) / mkeyCap.TTR) * xScale) + 1;
                        khit.drawed = true;
                    }

                    // auto scorll hor
                    x0 -= scrollNum * BitmapWidth;
                    x1 -= scrollNum * BitmapWidth;
                    if (x1 >= BitmapWidth)
                    {
                        scrollNum++;

                        ResetScreen();
                    }

                    y = keyDrawWidth * khit.keyId + keyDrawWidth / 2;

//                    if (khit.t1 == 0) {
//                        khit.t_draw = tick_kb; //khit.t0 + (long)((tick_kb - khit.t0) * 0.6);
//                        //khit.drawed = true;
//                    }
//                    else {
//                        khit.wait2deleteFromDrawingList = true; // 鍑嗗鍒犻櫎
//                    }
//                    else if (khit.drawed = false) {
//                        // ensure key is drawed at least once
//                        khit.drawed = true;
//                    }
//                    khit.drawed = true;

                    piccanvas.drawLine(x0, y, x1, y, mPaintNote);
                }

                // 鍒犻櫎鑰佸寲鐨刪it
                if (khit.t1 != 0)
                {
                    mkeyCap.KeyListDrawing.remove(i);
                }
            }

//            for(int i = s; i < s; i++) {
//                khit = mkeyCap.KeyListDrawing.get(i);
//                if (khit.t1 != 0)
//                {
//
//                }
//            }
        }

        mCanvas.drawBitmap(mBitmap, 0, 0, null);

//        int y = 0;
//        for(int i = 0; i < 44; i++)
//        {
//            y = i * keyDrawWidth + keyDrawWidth / 2;
//            mCanvas.drawLine(0, y, mBitmapRct.width(), y, mPaint1);
//        }

//        //mCanvas = piccanvasPeer;
//
//        //super.onDraw(mCanvas);
//
//        //render();
//
//        // 锟斤拷示
//        if (true) {
//            if (mBitmapRct.right <= 0) {
//                System.out.println("PicView onDraw(), error, mBitmapPeerRctDst.right is 0.");
//                return;
//            }
//
//            if (mBitmap.getWidth() == mBitmapRct.width()
//                    && mBitmap.getHeight() == mBitmapRct.height()) {
//                // 原始锟斤拷锟斤拷
//
//                // 平锟斤拷
//                Matrix matrix = new Matrix();
//                matrix.setTranslate(-OffsetX, 0);
//
////				//锟斤拷锟斤拷锟斤拷转30锟姐，锟斤拷图片锟斤拷锟斤拷
////				matrix.setRotate(30, zoombmp.getWidth()/2, zoombmp.getHeight()/2);
////				//锟斤拷锟斤拷锟姐法锟斤拷锟斤拷锟斤拷锟斤拷转锟斤拷效锟斤拷
////				mPaint.setAntiAlias(true);
//
//                mCanvas.drawBitmap(mBitmap, matrix, mPaint);
//            } else {
//                // 锟斤拷锟斤拷, 锟斤拷锟斤拷matrix锟斤拷createBitmap
//
//                Bitmap zoombmp;
//                //if (zoombmp == null)
//                {
//                    zoombmp = zoomBitmap(mBitmap, mBitmapRct.width(), mBitmapRct.height());
//                }
//
////				mBitmapPeerRctMin.left = 0;
////				mBitmapPeerRctMin.top = 0;
////				mBitmapPeerRctMin.right = Math.min(mBitmapPeerRctDst.right, mBitmapPeerRct.right);
////				mBitmapPeerRctMin.bottom = Math.min(mBitmapPeerRctDst.bottom, mBitmapPeerRct.bottom);
////	            mCanvas.drawBitmap(zoombmp, mBitmapPeerRctDst, mBitmapPeerRctMin, mPaint);
////				mBitmapPeerRctOffsetX = mBitmapPeerRctDst;
////				mBitmapPeerRctOffsetX.left += 20;//OffsetX;
////				mBitmapPeerRctOffsetX.right += 20; //OffsetX;
////				mBitmapPeerRct.left += 20;//OffsetX;
////				mBitmapPeerRct.right += 20; //OffsetX;
////				mCanvas.drawBitmap(zoombmp, mBitmapPeerRctOffsetX, mBitmapPeerRct, mPaint);
//
//                // 平锟斤拷
//                Matrix matrix = new Matrix();
//                matrix.setTranslate(-OffsetX, 0);
//
////				//锟斤拷锟斤拷锟斤拷转30锟姐，锟斤拷图片锟斤拷锟斤拷
////				matrix.setRotate(30, zoombmp.getWidth()/2, zoombmp.getHeight()/2);
////				//锟斤拷锟斤拷锟姐法锟斤拷锟斤拷锟斤拷锟斤拷转锟斤拷效锟斤拷
////				mPaint.setAntiAlias(true);
//
//                mCanvas.drawBitmap(zoombmp, matrix, mPaint);
//
//                zoombmp.recycle();
//            }
//        }
    }

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

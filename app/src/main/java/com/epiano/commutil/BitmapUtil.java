package com.epiano.commutil;

import android.graphics.Bitmap;
import android.graphics.Matrix;

public class BitmapUtil {

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

	public Bitmap reverseBitmap(Bitmap bmp, int flag) {
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

			// test
			w = bmpout.getWidth();
			h = bmpout.getHeight();
			int x = w;
			x = h;

			return bmpout;
		}

		return null;
	}

	// Bitmap缩放
	public Bitmap zoomBitmap(Bitmap bitmap, int width, int height) {
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




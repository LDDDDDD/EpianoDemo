package com.epiano.av.example.javazip;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class testzip {

	public static int deFlateFile(byte[] srcBuf, int srcLen, byte[] destBuf){

		Deflater deflater = new Deflater();
		deflater.setInput(srcBuf, 0, srcLen);
		deflater.finish();
		
//		Log.d("Debug-->","srcLen:"+srcLen+" Bytes");
		
		int destLen = 0;
		while (!deflater.finished()) {
			destLen += deflater.deflate(destBuf);
		}
		deflater.end();
		
//		Log.d("Debug-->","deFlateFile:"+destLen+" Bytes");		
//		Log.d("Debug-->","data:"+destBuf[0]+" " +destBuf[1]+" "+destBuf[2]+" "+destBuf[3]+" "+destBuf[4]);
		
		return destLen;

	}
	
	public static int zipFile(String srcfilename, String zipfilename) {
		
		byte[] buf = new byte[1024];
		ZipOutputStream out = null;
		FileInputStream in = null;
		int filesize = 0;
		
		Log.d("Debug-->","src:"+srcfilename+" zip:"+zipfilename);
		//srcfile.
		try {
			// Create the ZIP file
			out = new ZipOutputStream(new FileOutputStream(zipfilename));
			// Compress the files
			//for (int i = 0; i < srcfile.length; i++)
			{
				in = new FileInputStream(srcfilename);
				// Add ZIP entry to output stream.
				out.putNextEntry(new ZipEntry(srcfilename));
				// Transfer bytes from the file to the ZIP file
				int len;
				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}
				// Complete the entry
				out.closeEntry();
				in.close();
				in = null;
			}
			// Complete the ZIP file
		
			out.close();
			out = null;
			File zipFileNew = new File(zipfilename);
			filesize = (int)zipFileNew.length();
			return filesize;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (in != null) {
				try {
					in.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return 0;
	}
	

	public static int intMethod(int num){
		return num*num;
	}
}

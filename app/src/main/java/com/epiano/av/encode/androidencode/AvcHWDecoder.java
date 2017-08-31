package com.epiano.av.encode.androidencode;

import android.annotation.SuppressLint;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;

import java.nio.ByteBuffer;

public class AvcHWDecoder {
	private static final String TAG = "AndroidHWDec";
	private MediaCodec mediaCodec;
	private MediaCodec.BufferInfo bufferInfo = null;
	ByteBuffer[] inputBuffers = null;
	ByteBuffer[] outputBuffers  = null;
	ByteBuffer inputBuffer = null;
	ByteBuffer outputBuffer = null;
	
	int FRAME_RATE = 15;  //used for presentation time
	int m_width;
	int m_height;
	int mCount = 0;
	
	@SuppressLint("NewApi")
	public AvcHWDecoder(int width, int height, Surface sface)
	{
		if (mediaCodec != null)
		{
			close();
		}
		
		m_width  = width;
		m_height = height;
		try {
			mediaCodec = MediaCodec.createDecoderByType("video/avc");
		} catch (Exception e){
			e.printStackTrace();
		}
	    MediaFormat mediaFormat = MediaFormat.createVideoFormat("video/avc", width, height);
	    //mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar);
	    mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar); // 4418
	    bufferInfo = new MediaCodec.BufferInfo();
	    mediaCodec.configure(mediaFormat, sface, null, 0);
	    
	    if(mediaCodec == null)
	    {
	    	Log.e(TAG,"mediaCodec is null.");
	    	return;
	    }
	  //  mediaCodec.flush();
	    mediaCodec.start();
	}
	
	@SuppressLint("NewApi")
	public void close() {
	    try {
	        mediaCodec.stop();
	        mediaCodec.release();
	        bufferInfo = null;
	    } catch (Exception e){ 
	        e.printStackTrace();
	    }
	}
	public void flush(){
//		try {
//			mediaCodec.flush();
//		} catch (Exception e){
//			e.printStackTrace();
//		}
	}
	@SuppressLint("NewApi")
	public int offerDecoder(int length, byte[] input, int offset, byte[] output) 
	{
		int pos = 0;
		
		if ((input[offset + 4] & 0xF) == 5)
		{
			Log.d("offerDecoder", "offerDecoder iiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii");  
		}
		
		try{
			inputBuffers = mediaCodec.getInputBuffers();
			int inputBufferIndex = mediaCodec.dequeueInputBuffer(-1);
			if (inputBufferIndex >= 0) {  
				inputBuffer = inputBuffers[inputBufferIndex];  
				inputBuffer.clear();  
				inputBuffer.put(input, offset, length);
				mediaCodec.queueInputBuffer(inputBufferIndex, 0, length, mCount * 1000000 / FRAME_RATE, 0);  
				mCount++;  
			}

			int outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 10000);  //0  microseconds			
			outputBuffers = mediaCodec.getOutputBuffers();
			//Log.d("DecodeActivity", "New format " + mediaCodec.getOutputFormat());
			
			switch (outputBufferIndex) {  
			case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:  
				Log.d("offerDecoder", "INFO_OUTPUT_BUFFERS_CHANGED");  
				outputBuffers = mediaCodec.getOutputBuffers();  
				break;  
			case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:  
				Log.d("offerDecoder", "New format " + mediaCodec.getOutputFormat());  
				break;  
			case MediaCodec.INFO_TRY_AGAIN_LATER:  
				Log.d("offerDecoder", "dequeueOutputBuffer timed out!");  
				break;  
			default:  
				//ByteBuffer buffer = outputBuffers[outputBufferIndex];  
				Log.v("offerDecoder", "outputBufferIndex = " + outputBufferIndex);
			} 
			
			while (outputBufferIndex >= 0)
			if (outputBufferIndex >= 0)
			{				
				outputBuffer = outputBuffers[outputBufferIndex];
	            byte[] outData = new byte[bufferInfo.size];
	            outputBuffer.get(outData);
	            System.arraycopy(outData, 0, output, pos, outData.length);
	            pos += outData.length;
				mediaCodec.releaseOutputBuffer(outputBufferIndex, true);  
				outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 0);
				outData = null;
			}

		}catch(Throwable t) {
			t.printStackTrace();
		}
		
		return pos;
	}
}

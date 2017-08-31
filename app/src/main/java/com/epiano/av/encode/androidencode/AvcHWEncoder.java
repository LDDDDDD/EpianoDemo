package com.epiano.av.encode.androidencode;

import android.annotation.SuppressLint;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecInfo.CodecCapabilities;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.util.Log;

import java.nio.ByteBuffer;
import java.util.HashMap;

public class AvcHWEncoder 
{
	private static final String TAG = "AndroidHWEnc";
	
	private MediaCodec mediaCodec = null;
	private MediaCodec.BufferInfo bufferInfo = null;
	ByteBuffer[] inputBuffers = null;
	ByteBuffer[] outputBuffers  = null;
	ByteBuffer inputBuffer = null;
	ByteBuffer outputBuffer = null;
	
	int m_width;
	int m_height;
	byte[] m_info = null;
	
	int mDecCount = 0;
	int FRAME_RATE = 15;
	
	// 鑾峰彇鍚勭被闊宠棰戠‖缂栫爜鍣ㄧ殑淇℃伅
	HashMap<String, CodecCapabilities> mEncoderInfos = new HashMap<String, CodecCapabilities>();
	void initEncoderInfos(){
	    for(int i = MediaCodecList.getCodecCount() - 1; i >= 0; i--){
	        MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);
	        if(codecInfo.isEncoder()){
	            for(String t : codecInfo.getSupportedTypes()){
	                try{
	                    mEncoderInfos.put(t, codecInfo.getCapabilitiesForType(t));
	                } catch(IllegalArgumentException e){
	                    e.printStackTrace();
	                }
	            }
	        }
	    }
	}
	
	private static MediaCodecInfo selectCodec(String mimeType) {  
        int numCodecs = MediaCodecList.getCodecCount();  
        for (int i = 0; i < numCodecs; i++) {  
            MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);  
  
            if (!codecInfo.isEncoder()) {  
                continue;  
            }  
  
            String[] types = codecInfo.getSupportedTypes();  
            for (int j = 0; j < types.length; j++) {  
                if (types[j].equalsIgnoreCase(mimeType)) {  
                    return codecInfo;  
                }  
            }  
        }  
        return null;  
    } 
	
//	@SuppressLint("NewApi")
	private static void printColorFormat(MediaCodecInfo codecInfo, String mimeType)
	{  
	    CodecCapabilities capabilities = codecInfo.getCapabilitiesForType(mimeType);
	    for (int i = 0; i < capabilities.colorFormats.length; i++)
	    {  
	        int colorFormat = capabilities.colorFormats[i];  
	         switch (colorFormat) 
	         {  
		        // these are the formats we know how to handle for this testcase MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar:  
		        case CodecCapabilities.COLOR_FormatYUV420PackedPlanar:
					Log.e(TAG,"COLOR_FormatYUV420PackedPlanar" );  
					break;  
		        case CodecCapabilities.COLOR_FormatYUV420SemiPlanar:
					Log.e(TAG,"COLOR_FormatYUV420SemiPlanar");  
					break;  
				case CodecCapabilities.COLOR_FormatYUV420PackedSemiPlanar:
					Log.e(TAG,"COLOR_FormatYUV420PackedSemiPlanar");  
					break;  
				case CodecCapabilities.COLOR_FormatYUV420Planar:
					Log.e(TAG,"COLOR_FormatYUV420Planar");  //////////////////
					break;  		       
		        default:  
		            Log.e(TAG,"colorFormat: "+colorFormat);  
		            break;
	         }
	     }
	}

	//private byte[] yuv420 = null;  //閼规彃鍍电粚娲？鏉烆剚宕叉担璺ㄦ暏
	@SuppressLint("NewApi")
	public AvcHWEncoder(int width, int height, int framerate, int bitrate) {
		StartAvcHWEncoder(width, height, framerate, bitrate);
	}
	@SuppressLint("NewApi")
	public int StartAvcHWEncoder(int width, int height, int framerate, int bitrate) { 
		
		// test
		//bitrate = 100000;
		
		m_width  = width;
		m_height = height;
		//yuv420 = new byte[width*height*3/2];  //閼规彃鍍电粚娲？鏉烆剚宕叉担璺ㄦ暏
		
		if (mediaCodec != null)
		{
			close();
		}
		
		MediaCodecInfo codecinfo = selectCodec("video/avc");
		printColorFormat(codecinfo, "video/avc");
		initEncoderInfos();

		try {
			mediaCodec = MediaCodec.createEncoderByType("video/avc");
		} catch (Exception e){
			e.printStackTrace();
		}
		
		Log.e(TAG, "video encode, bitrate: " + bitrate + ", framerate: " + framerate );
		
	    MediaFormat mediaFormat = MediaFormat.createVideoFormat("video/avc", width, height);
	    mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitrate);
	    mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, framerate);
	    
	    String DeviceMode = android.os.Build.MODEL;	
	    if (DeviceMode.equals("AOSP on drone2")) // 4418
	    {
	    	mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, CodecCapabilities.COLOR_FormatYUV420Planar);  // 4418 //.COLOR_FormatYUV420SemiPlanar);  //.COLOR_FormatYUV420Planar
	    }
	    else
	    {
	    	mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, CodecCapabilities.COLOR_FormatYUV420SemiPlanar);  //.COLOR_FormatYUV420Planar); //.COLOR_FormatYUV420SemiPlanar);  //.COLOR_FormatYUV420Planar; //.COLOR_FormatSurface
	    }
	    
	    
	    mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 20); //5000); //1); //500); //1);   // I frame per 1 sec

	    mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
	    
	    //MediaCodecInfo & Capabilities:
	    /*
	    MediaCodecInfo mediacodeinfo = selectCodec("video/avc");
	    //mediacodeinfo = mediaCodec.getCodecInfo();   //api level 18
	    //String codecName = mediaCodec.getName();     //api level 18
	    CodecCapabilities codeccapabilities = mediacodeinfo.getCapabilitiesForType("video/avc");
	    CodecProfileLevel []codecprofilelevel = codeccapabilities.profileLevels;
	    for (int i = 0; i < codecprofilelevel.length; i++) 
	    {
	    	 Log.i(TAG,"Level["+i+"]:"+codecprofilelevel[i].level);
	    }
	   */
	    
	    bufferInfo = new MediaCodec.BufferInfo();
	    mediaCodec.start();
	    
	    return 1;
	}

	@SuppressLint("NewApi")
	public void close() {
	    try {
	        mediaCodec.stop();
	        mediaCodec.release();
	        m_info = null;
	        bufferInfo = null;
	    } catch (Exception e){ 
	        e.printStackTrace();
	    }
	}
	@SuppressLint("NewApi")
	public void restart(int width, int height, int framerate, int bitrate) {
	    try {
	        close();
	    } catch (Exception e){ 
	        e.printStackTrace();
	    }
	    
	    StartAvcHWEncoder(width, height, framerate, bitrate);
	}
	public void flush(){
//		try {
//			mediaCodec.flush();
//		} catch (Exception e){
//			e.printStackTrace();
//		}
	}
	
    //yv12 �?yuv420p  yvu -> yuv  
   private void swapYV12toI420(byte[] yv12bytes, byte[] i420bytes, int width, int height)   
   {        
       System.arraycopy(yv12bytes, 0, i420bytes, 0,width*height);  
       System.arraycopy(yv12bytes, width*height+width*height/4, i420bytes, width*height,width*height/4);  
       System.arraycopy(yv12bytes, width*height, i420bytes, width*height+width*height/4,width*height/4);    
   }    
	
	// output: 264 stream
	@SuppressLint("NewApi")
	public int offerEncoder(byte[] input, byte[] output, int w, int h, int [] nalcount, int [] nalpos) 
	{	
		int pos = 0;
        int idx = 0;
        
		nalcount[0] = 0; // 输出nal数量
		
		int inlen = w * h * 3 / 2;
//		input = new byte[inlen];
//		for(int i = 0; i < 1000; i++)
//		{
//			input[i] = (byte)((i % 150) + 100);
//		}
		
		//swapYV12toI420(input, yuv420, m_width, m_height);  //鑹插僵绌洪棿杞�?		//褰撳墠鑹插僵绌洪棿锛氭憚鍍忓ご杈撳嚭NV21,缂栫爜鍣ㄨ缃甕UV420semiPlanner 涓嶉渶瑕佽浆鎹�	
//		Log.i(TAG,"in:("+input.length +")" + bytesToHexStringTrim(input) + ", w:" + w + ", h:" + h);
	    try {
	       inputBuffers = mediaCodec.getInputBuffers();
	       outputBuffers = mediaCodec.getOutputBuffers();
	        int inputBufferIndex = mediaCodec.dequeueInputBuffer(-1);
	        if (inputBufferIndex >= 0) 
	        {
	            inputBuffer = inputBuffers[inputBufferIndex];
	            inputBuffer.clear();
	            inputBuffer.put(input);  //
	            mediaCodec.queueInputBuffer(inputBufferIndex, 0, inlen, 0, 0);  //input.length
	        }

	        //MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
	        int outputBufferIndex = 0;
	        
	        outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo,0);	       
	       // while (outputBufferIndex >= 0) 
	        if(outputBufferIndex < 0) // honor6 首次返回-2，但继续取数据又能成�?
	        {
	        	outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo,0);
//	        	Log.i(TAG,"outputBufferIndex 2:"+ outputBufferIndex);
	        }
	        //else
	        nalpos[0] = 0;
	        //synchronized(output)
	        {
		        while (outputBufferIndex >= 0)
		        {
		        	
		            outputBuffer = outputBuffers[outputBufferIndex];
		            
		            byte[] outData = new byte[bufferInfo.size];
		            outputBuffer.get(outData);
		            
		            //Log.i(TAG,"outbuffer:("+outData.length +")"+ bytesToHexStringTrim(outData));	            
	
		            System.arraycopy(outData, 0,  output, pos, outData.length);
		            pos += outData.length;
		            nalpos[idx + 1] = pos;
		            
		            // outData: stream frame definition
	//	            00 00 00 01:
	//	            01: no IDR
	//	            05: IDR
	//	            06: SEI
	//	            07: SPS
	//	            08: PPS
	//	            ...
		            
		            //get sps&pps into m_info 
	//	            if(m_info == null)
	//	            {
	//	              	 ByteBuffer spsPpsBuffer = ByteBuffer.wrap(outData);  
	//	                 if (spsPpsBuffer.getInt() == 0x00000001) 
	//	                 {  
	//	                	 m_info = new byte[outData.length];
	//	                	 //System.arraycopy(outData, 0, m_info, 0, outData.length); // ...
	//	                 } 
	//	                 else 
	//	                 {  
	//	                        return -1;
	//	                 }
	//	                 String temp;
	//	                 temp = bytesToHexString(outData);
	////	                 Log.i(TAG,"spsppsbuf:"+temp);
	//	            }
		            
		            mediaCodec.releaseOutputBuffer(outputBufferIndex, false);
		            outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 0);
	//	            outData = null;  //uncommit @ 2014-6-12
		            
		        	idx++;	            
		        }
	        }

//	        if(output[4] == 0x25) //key frame:0x25  none-keyframe:ox21?  
//	        {
//	        	//Log.i(TAG,"Key frame");
//	        	System.arraycopy(output, 0,  input, 0, pos);  //yuv420
//	        	System.arraycopy(m_info, 0,  output, 0, m_info.length);
//	        	System.arraycopy(input, 0,  output, m_info.length, pos);  //yuv420
//	        	pos += m_info.length;
//	        }
	        
	        inputBuffers = null;	        
	        outputBuffers = null;	
	    } catch (Throwable t) {
	        //t.printStackTrace();
	    	Log.e(TAG,"hwencode fail");
	    }
	   	    
	    //Log.i(TAG,"out:("+output.length +")"+ bytesToHexStringTrim(output));
	    
	    nalcount[0] = idx;
	  
	    return pos;
	}
	
	// output: 264 stream
	@SuppressLint("NewApi")
	public int offerEncoder2(byte[] input, byte[] output, int w, int h, int [] nalcount, int [] nallen) 
	{	
		int pos = 0;
		//swapYV12toI420(input, yuv420, m_width, m_height);  //色彩空间转换
		//当前色彩空间：摄像头输出NV21,编码器设置YUV420semiPlanner 不需要转�?
//		Log.i(TAG,"in:("+input.length +")" + bytesToHexStringTrim(input));
	    try {
	       inputBuffers = mediaCodec.getInputBuffers();
	       outputBuffers = mediaCodec.getOutputBuffers();
	        int inputBufferIndex = mediaCodec.dequeueInputBuffer(-1);
	        if (inputBufferIndex >= 0) 
	        {
	            inputBuffer = inputBuffers[inputBufferIndex];
	            inputBuffer.clear();
	            inputBuffer.put(input);  //
	            mediaCodec.queueInputBuffer(inputBufferIndex, 0, input.length, 0, 0);  //
	        }

	        //MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
	        int outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo,0);
	       
	        while (outputBufferIndex >= 0) 
	        //if(outputBufferIndex >= 0)
	        {
	            outputBuffer = outputBuffers[outputBufferIndex];
	            
	            byte[] outData = new byte[bufferInfo.size];
	            outputBuffer.get(outData);
	            
	            //Log.i(TAG,"outbuffer:("+outData.length +")"+ bytesToHexStringTrim(outData));	            

	            System.arraycopy(outData, 0, output, pos, outData.length);
	            pos += outData.length;	                     
	            
	            //get sps&pps into m_info 
	            if(m_info == null)
	            {
	              	 ByteBuffer spsPpsBuffer = ByteBuffer.wrap(outData);  
	                 if (spsPpsBuffer.getInt() == 0x00000001) 
	                 {  
	                	 m_info = new byte[outData.length];
	                	 System.arraycopy(outData, 0, m_info, 0, outData.length);
	                 } 
	                 else 
	                 {  
	                        return -1;
	                 }
	                 String temp;
	                 temp = bytesToHexString(outData);
	                 Log.i(TAG,"spsppsbuf:"+temp);
	            }
	            
	            mediaCodec.releaseOutputBuffer(outputBufferIndex, false);
	            outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 0);
	            outData = null;  //uncommit @ 2014-6-12
	        }

	        if(output[4] == 0x25) //key frame:0x25  none-keyframe:ox21?  
	        {
	        	//Log.i(TAG,"Key frame");
	        	System.arraycopy(output, 0,  input, 0, pos);  //yuv420
	        	System.arraycopy(m_info, 0,  output, 0, m_info.length);
	        	System.arraycopy(input, 0,  output, m_info.length, pos);  //yuv420
	        	pos += m_info.length;
	        }
	        
	        inputBuffers = null;	        
	        outputBuffers = null;	
	    } catch (Throwable t) {
	        t.printStackTrace();
	    }
	   	    
	    //Log.i(TAG,"out:("+output.length +")"+ bytesToHexStringTrim(output));
	  
	    return pos;
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
				mediaCodec.queueInputBuffer(inputBufferIndex, 0, length, mDecCount * 1000000 / FRAME_RATE, 0);  
				mDecCount++;  
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
	
   /* private void swapYV12toI420(byte[] yv12bytes, byte[] i420bytes, int width, int height) 
    {      
    	System.arraycopy(yv12bytes, 0, i420bytes, 0,width*height);
    	System.arraycopy(yv12bytes, width*height+width*height/4, i420bytes, width*height,width*height/4);
    	System.arraycopy(yv12bytes, width*height, i420bytes, width*height+width*height/4,width*height/4);  
    }  */

    public static String bytesToHexString(byte[] src){
    	if (src == null || src.length <= 0) {
    		return null;
    	}
    	//String[] str = new String[src.length];
    	String str = "";
    	for (int i = 0; i < src.length; i++) {
    		int v = src[i] & 0xFF;
    		String hv = Integer.toHexString(v);
    		if (hv.length() < 2) {
    			//str[i] = "0";
    			hv = "0" + hv;
    		}
    		//str[i] = hv;
    		str += hv;
    	} 
    	return str;
    }
    
    //閹垫挸宓冮弰鍓с仛閸擄�?00娑擃亜鐡х粭锟�?    
    public static String bytesToHexStringTrim(byte[] src){
        if (src == null || src.length <= 0) {
            return null;
        }     
        String str = "";
        int length;
        if (src.length > 50)
        {
        	length = 50;
        }
        else
        {
        	length = src.length;
        }     	
        
        for (int i = 0; i < length; i++) {
        	int v = src[i] & 0xFF;
        	String hv = Integer.toHexString(v);
        	if (hv.length() < 2) {              
        		hv = "0" + hv;
        	}         
        	str += hv;
        } 
        return str;
    }
    
    public static String[] bytesToHexStrings(byte[] src){
    	if (src == null || src.length <= 0) {
    		return null;
    	}
    	String[] str = new String[src.length];     
    	for (int i = 0; i < src.length; i++) {
    		int v = src[i] & 0xFF;
    		String hv = Integer.toHexString(v);
    		if (hv.length() < 2) {
    			str[i] = "0";
    		}
    		str[i] = hv;            
    	}

    	return str;
    }

   /* private static MediaCodecInfo selectCodec(String mimeType) {
        int numCodecs = MediaCodecList.getCodecCount();
        for (int i = 0; i < numCodecs; i++) {
            MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);

            if (!codecInfo.isEncoder()) {
                continue;
            }

            String[] types = codecInfo.getSupportedTypes();
            for (int j = 0; j < types.length; j++) {
                if (types[j].equalsIgnoreCase(mimeType)) {
                    return codecInfo;
                }
            }
        }
        return null;
    }*/
    
    public void decodeYUV420SP(byte[] rgbBuf, byte[] yuv420sp, int width, int height)
    {
        final int frameSize = width * height;  
	    if (rgbBuf == null)  
	        throw new NullPointerException("buffer 'rgbBuf' is null");  
	    if (rgbBuf.length < frameSize * 3)  
	        throw new IllegalArgumentException("buffer 'rgbBuf' size "  
	                + rgbBuf.length + " < minimum " + frameSize * 3);  
	      
	    if (yuv420sp == null)  
	        throw new NullPointerException("buffer 'yuv420sp' is null");  
	      
	    if (yuv420sp.length < frameSize * 3 / 2)  
	        throw new IllegalArgumentException("buffer 'yuv420sp' size " + yuv420sp.length  
	                + " < minimum " + frameSize * 3 / 2);  
	          
	        int i = 0, y = 0;  
	        int uvp = 0, u = 0, v = 0;  
	        int y1192 = 0, r = 0, g = 0, b = 0;  
	          
	        for (int j = 0, yp = 0; j < height; j++) {  
	            uvp = frameSize + (j >> 1) * width;  
	            u = 0;  
	            v = 0;  
	            for (i = 0; i < width; i++, yp++) {  
	                y = (0xff & ((int) yuv420sp[yp])) - 16;  
	                if (y < 0) y = 0;  
	                if ((i & 1) == 0) {  
	                    v = (0xff & yuv420sp[uvp++]) - 128;  
	                    u = (0xff & yuv420sp[uvp++]) - 128;  
	                }  
	                  
	                y1192 = 1192 * y;  
	                r = (y1192 + 1634 * v);  
	                g = (y1192 - 833 * v - 400 * u);  
	                b = (y1192 + 2066 * u);  
	                  
	                if (r < 0) r = 0; else if (r > 262143) r = 262143;  
	                if (g < 0) g = 0; else if (g > 262143) g = 262143;  
	                if (b < 0) b = 0; else if (b > 262143) b = 262143;  
	                  
	                rgbBuf[yp * 3] = (byte)(r >> 10);  
	                rgbBuf[yp * 3 + 1] = (byte)(g >> 10);  
	                rgbBuf[yp * 3 + 2] = (byte)(b >> 10);  
	            }  
	        }  
      }
    
}

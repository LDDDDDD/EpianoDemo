package com.epiano.eLearning;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.util.Log;

import com.epiano.commutil.PianoELearning;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.microedition.khronos.opengles.GL10;

//import java.nio.FloatBuffer;
//import PianoELearning.GLProgram;

public class GLFrameRenderer implements Renderer {

	private static String TAG = "Video";

	private PianoELearning mParentAct; //请无视之
	private GLSurfaceView mTargetSurface;
	private GLProgram prog = new GLProgram(0);
	private int mVideoWidth = -1, mVideoHeight = -1;
	private ByteBuffer y;
	private ByteBuffer u;
	private ByteBuffer v;

	public GLFrameRenderer(PianoELearning callback, GLSurfaceView surface) {
		mParentAct = callback; //请无视之
		mTargetSurface = surface;
	}

	//    //@Override
//    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
//        Log.i(TAG, "GLFrameRenderer :: onSurfaceCreated");
//        if (!prog.isProgramBuilt()) {
//            prog.buildProgram();
//            Log.i(TAG, "GLFrameRenderer :: buildProgram done");
//        }
//    }
	@Override
	public void onSurfaceCreated(GL10 arg0,
								 javax.microedition.khronos.egl.EGLConfig arg1) {
		// TODO Auto-generated method stub
		Log.i(TAG, "GLFrameRenderer :: onSurfaceCreated");
		if (!prog.isProgramBuilt()) {
			prog.buildProgram();
			Log.i(TAG, "GLFrameRenderer :: buildProgram done");
		}
	}

	//@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		Log.i(TAG, "GLFrameRenderer :: onSurfaceChanged");
		GLES20.glViewport(0, 0, width, height);
	}


	//@Override
	public void onDrawFrame(GL10 gl) {
		synchronized (this)
		{
			if (y != null) {
				// reset position, have to be done

				//Log.i(TAG, "onDrawFrame E, w: " + mVideoWidth + ", h: " + mVideoHeight);

				y.position(0);
				u.position(0);
				v.position(0);
				prog.buildTextures(y, u, v, mVideoWidth, mVideoHeight);
				GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
				GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
				prog.drawFrame();
			}
		}
	}

	/**
	 * this method will be called from native code, it happens when the video is about to play or
	 * the video size changes.
	 */
	public void update(int w, int h) {
		//Log.i(TAG, "INIT E, w: " + w + ", h: " + h);
		if (w > 0 && h > 0) {
			if (w != mVideoWidth && h != mVideoHeight) {
				this.mVideoWidth = w;
				this.mVideoHeight = h;
				int yarraySize = w * h;
				int uvarraySize = yarraySize / 4;
				synchronized (this) {
					y = ByteBuffer.allocate(yarraySize);
					u = ByteBuffer.allocate(uvarraySize);
					v = ByteBuffer.allocate(uvarraySize);
				}
			}
		}

		mParentAct.onPlayStart(); //请无视之
		//Log.i(TAG, "INIT X");
	}

	/**
	 * this method will be called from native code, it's used for passing yuv data to me.
	 */
	public void update(byte[] ydata, byte[] udata, byte[] vdata) {
		synchronized (this)
		{
			y.clear();
			u.clear();
			v.clear();
			y.put(ydata, 0, ydata.length);
			u.put(udata, 0, udata.length);
			v.put(vdata, 0, vdata.length);
		}

		// request to render
		mTargetSurface.requestRender();
	}

	public void update(byte[] yvudata, int offset, int w, int h) {
		int s = w * h;
		int s4  = s / 4;
		synchronized (this)
		{
			y.clear();
			u.clear();
			v.clear();
			y.put(yvudata, offset, s);
			u.put(yvudata, offset + s + s4, s4);
			v.put(yvudata, offset + s, s4);
		}

		// request to render
		mTargetSurface.requestRender();
	}

	public void updateYVV(byte[] yvudata, int offset, int w, int h) {
		int s = w * h;
		int s4  = s / 4;
		synchronized (this)
		{
			y.clear();
			u.clear();
			v.clear();
			y.put(yvudata, offset, s);
			v.put(yvudata, offset + s + s4, s4);
			u.put(yvudata, offset + s, s4);
		}

		// request to render
		mTargetSurface.requestRender();
	}

	public class GLProgram {

		boolean isProgBuilt = false;
		int _program = 0;
		//float [] _vertices = new float[100];
		int _positionHandle = 0;
		int _coordHandle = 0;
		int _yhandle = 0;
		int _uhandle = 0;
		int _vhandle = 0;
		int _video_width = 0;
		int _video_height = 0;
		int _ytid = 0;
		int _utid = 0;
		int _vtid = 0;
		ByteBuffer _vertice_buffer;
		ByteBuffer _coord_buffer;
		int _textureI = GLES20.GL_TEXTURE0;
		int _textureII = GLES20.GL_TEXTURE1;
		int _textureIII = GLES20.GL_TEXTURE2;
		int _tIindex = 0;
		int _tIIindex = 1;
		int _tIIIindex = 2;

		public GLProgram(int id)
		{

		}

		public boolean isProgramBuilt() {
			return isProgBuilt;
		}

		public void buildProgram() {
			createBuffers(squareVertices, coordVertices);
			if (_program <= 0) {
				_program = createProgram(VERTEX_SHADER, FRAGMENT_SHADER);
			}
			Log.i(TAG, "_program = " + _program);

    	    /*
    	     * get handle for "vPosition" and "a_texCoord"
    	     */
			_positionHandle = GLES20.glGetAttribLocation(_program, "vPosition");
			Log.i(TAG, "_positionHandle = " + _positionHandle);
			checkGlError("glGetAttribLocation vPosition");
			if (_positionHandle == -1) {
				throw new RuntimeException("Could not get attribute location for vPosition");
			}
			_coordHandle = GLES20.glGetAttribLocation(_program, "a_texCoord");
			Log.i(TAG, "_coordHandle = " + _coordHandle);
			checkGlError("glGetAttribLocation a_texCoord");
			if (_coordHandle == -1) {
				throw new RuntimeException("Could not get attribute location for a_texCoord");
			}

    	    /*
    	     * get uniform location for y/u/v, we pass data through these uniforms
    	     */
			_yhandle = GLES20.glGetUniformLocation(_program, "tex_y");
			Log.i(TAG, "_yhandle = " + _yhandle);
			checkGlError("glGetUniformLocation tex_y");
			if (_yhandle == -1) {
				throw new RuntimeException("Could not get uniform location for tex_y");
			}
			_uhandle = GLES20.glGetUniformLocation(_program, "tex_u");
			Log.i(TAG, "_uhandle = " + _uhandle);
			checkGlError("glGetUniformLocation tex_u");
			if (_uhandle == -1) {
				throw new RuntimeException("Could not get uniform location for tex_u");
			}
			_vhandle = GLES20.glGetUniformLocation(_program, "tex_v");
			Log.i(TAG, "_vhandle = " + _vhandle);
			checkGlError("glGetUniformLocation tex_v");
			if (_vhandle == -1) {
				throw new RuntimeException("Could not get uniform location for tex_v");
			}

			isProgBuilt = true;
		}

		/**
		 * build a set of textures, one for Y, one for U, and one for V.
		 */
		public void buildTextures(Buffer y, Buffer u, Buffer v, int width, int height) {
			boolean videoSizeChanged = (width != _video_width || height != _video_height);
			if (videoSizeChanged) {
				_video_width = width;
				_video_height = height;
				Log.i(TAG, "buildTextures videoSizeChanged: w=" + _video_width + " h=" + _video_height);
			}

			// building texture for Y data
			if (_ytid < 0 || videoSizeChanged) {
				if (_ytid >= 0) {
					Log.i(TAG, "glDeleteTextures Y");
					GLES20.glDeleteTextures(1, new int[] { _ytid }, 0);
					checkGlError("glDeleteTextures");
				}
				// GLES20.glPixelStorei(GLES20.GL_UNPACK_ALIGNMENT, 1);
				int[] textures = new int[1];
				GLES20.glGenTextures(1, textures, 0);
				checkGlError("glGenTextures");
				_ytid = textures[0];
				Log.i(TAG, "glGenTextures Y = " + _ytid);
			}
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, _ytid);
			checkGlError("glBindTexture");
			GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE, _video_width, _video_height, 0,
					GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, y);
			checkGlError("glTexImage2D");
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

			// building texture for U data
			if (_utid < 0 || videoSizeChanged) {
				if (_utid >= 0) {
					Log.i(TAG, "glDeleteTextures U");
					GLES20.glDeleteTextures(1, new int[] { _utid }, 0);
					checkGlError("glDeleteTextures");
				}
				int[] textures = new int[1];
				GLES20.glGenTextures(1, textures, 0);
				checkGlError("glGenTextures");
				_utid = textures[0];
				Log.i(TAG, "glGenTextures U = " + _utid);
			}
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, _utid);
			GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE, _video_width / 2, _video_height / 2, 0,
					GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, u);
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

			// building texture for V data
			if (_vtid < 0 || videoSizeChanged) {
				if (_vtid >= 0) {
					Log.i(TAG, "glDeleteTextures V");
					GLES20.glDeleteTextures(1, new int[] { _vtid }, 0);
					checkGlError("glDeleteTextures");
				}
				int[] textures = new int[1];
				GLES20.glGenTextures(1, textures, 0);
				checkGlError("glGenTextures");
				_vtid = textures[0];
				Log.i(TAG, "glGenTextures V = " + _vtid);
			}
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, _vtid);
			GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE, _video_width / 2, _video_height / 2, 0,
					GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, v);
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

			//Log.i(TAG, "buildTextures()");
		}

		/**
		 * render the frame
		 * the YUV data will be converted to RGB by shader.
		 */
		public void drawFrame() {
			GLES20.glUseProgram(_program);
			checkGlError("glUseProgram");

			GLES20.glVertexAttribPointer(_positionHandle, 2, GLES20.GL_FLOAT, false, 8, _vertice_buffer);
			checkGlError("glVertexAttribPointer mPositionHandle");
			GLES20.glEnableVertexAttribArray(_positionHandle);

			GLES20.glVertexAttribPointer(_coordHandle, 2, GLES20.GL_FLOAT, false, 8, _coord_buffer);
			checkGlError("glVertexAttribPointer maTextureHandle");
			GLES20.glEnableVertexAttribArray(_coordHandle);

			// bind textures
			GLES20.glActiveTexture(_textureI);
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, _ytid);
			GLES20.glUniform1i(_yhandle, _tIindex);

			GLES20.glActiveTexture(_textureII);
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, _utid);
			GLES20.glUniform1i(_uhandle, _tIIindex);

			GLES20.glActiveTexture(_textureIII);
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, _vtid);
			GLES20.glUniform1i(_vhandle, _tIIIindex);

			GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
			GLES20.glFinish();

			GLES20.glDisableVertexAttribArray(_positionHandle);
			GLES20.glDisableVertexAttribArray(_coordHandle);

			//Log.i(TAG, "drawFrame()");
		}

		/**
		 * create program and load shaders, fragment shader is very important.
		 */
		public int createProgram(String vertexSource, String fragmentSource) {
			// create shaders
			int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
			int pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
			// just check
			Log.i(TAG, "vertexShader = " + vertexShader);
			Log.i(TAG, "pixelShader = " + pixelShader);

			int program = GLES20.glCreateProgram();
			if (program != 0) {
				GLES20.glAttachShader(program, vertexShader);
				checkGlError("glAttachShader");
				GLES20.glAttachShader(program, pixelShader);
				checkGlError("glAttachShader");
				GLES20.glLinkProgram(program);
				int[] linkStatus = new int[1];
				GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
				if (linkStatus[0] != GLES20.GL_TRUE) {
					Log.i(TAG, "Could not link program: ", null);
					Log.i(TAG, GLES20.glGetProgramInfoLog(program), null);
					GLES20.glDeleteProgram(program);
					program = 0;
				}
			}
			return program;
		}

		/**
		 * create shader with given source.
		 */
		private int loadShader(int shaderType, String source) {
			int shader = GLES20.glCreateShader(shaderType);
			if (shader != 0) {
				GLES20.glShaderSource(shader, source);
				GLES20.glCompileShader(shader);
				int[] compiled = new int[1];
				GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
				if (compiled[0] == 0) {
					Log.i(TAG, "Could not compile shader " + shaderType + ":", null);
					Log.i(TAG, GLES20.glGetShaderInfoLog(shader), null);
					GLES20.glDeleteShader(shader);
					shader = 0;
				}
			}
			return shader;
		}

		/**
		 * these two buffers are used for holding vertices, screen vertices and texture vertices.
		 */
		private void createBuffers(float[] vert, float[] coord) {
			_vertice_buffer = ByteBuffer.allocateDirect(vert.length * 4);
			_vertice_buffer.order(ByteOrder.nativeOrder());
			_vertice_buffer.asFloatBuffer().put(vert);
			_vertice_buffer.position(0);

			if (_coord_buffer == null) {
				_coord_buffer = ByteBuffer.allocateDirect(coord.length * 4);
				_coord_buffer.order(ByteOrder.nativeOrder());
				_coord_buffer.asFloatBuffer().put(coord);
				_coord_buffer.position(0);
			}
		}

		private void checkGlError(String op) {
			int error;
			while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
				Log.i(TAG, "***** " + op + ": glError " + error, null);
				throw new RuntimeException(op + ": glError " + error);
			}
		}

		private float[] squareVertices = { -1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, 1.0f, }; // fullscreen

		private float[] coordVertices = { 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, };// whole-texture

		private static final String VERTEX_SHADER = "attribute vec4 vPosition;\n" + "attribute vec2 a_texCoord;\n"
				+ "varying vec2 tc;\n" + "void main() {\n" + "gl_Position = vPosition;\n" + "tc = a_texCoord;\n" + "}\n";

		private static final String FRAGMENT_SHADER = "precision mediump float;\n" + "uniform sampler2D tex_y;\n"
				+ "uniform sampler2D tex_u;\n" + "uniform sampler2D tex_v;\n" + "varying vec2 tc;\n" + "void main() {\n"
				+ "vec4 c = vec4((texture2D(tex_y, tc).r - 16./255.) * 1.164);\n"
				+ "vec4 U = vec4(texture2D(tex_u, tc).r - 128./255.);\n"
				+ "vec4 V = vec4(texture2D(tex_v, tc).r - 128./255.);\n" + "c += V * vec4(1.596, -0.813, 0, 0);\n"
				+ "c += U * vec4(0, -0.392, 2.017, 0);\n" + "c.a = 1.0;\n" + "gl_FragColor = c;\n" + "}\n";
	}
}


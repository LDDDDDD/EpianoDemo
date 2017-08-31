package com.epiano.commutil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

//显示照片的纹理矩形
public class Board {
    FloatBuffer   mVertexBuffer;//顶点坐标数据缓冲
    FloatBuffer   mTextureBuffer;//顶点纹理数据缓冲
    int vCount=0;//顶点数

    public float vertices[]; //=new float[18]; // 3 * 6
    public float textures[];
    //    public static final float length=3f;//0度位置图片左侧距离原点的横向距离
//    public static final float width=20f;//图片的宽
//    public static final float height=15f*0.5f;//图片的半高度
    public float length = 3f;// 0度位置图片左侧距离原点的横向距离
    public float width = 20f;// 图片的宽
    public float height = 15f * 0.5f;// 图片的半高度


    public Board(int axisRadias, int pageW, int pageH)
    {

        length = (float)axisRadias;
        width = (float)pageW;
        height = (float)pageH;

        //顶点坐标数据的初始化================begin============================
        // z = 0; 图像在xoy平面
        vCount=6;
        float verticest[]=new float[]
                {
                        length,height,0,		//
                        length,-height,0,
                        length+width,height,0,
                        length+width,height,0,	//
                        length,-height,0,
                        length+width,-height,0,

                        // hov reverse bitmap
                        length+width,height,0,	//
                        length+width,-height,0,
                        length,height,0,
                        length,height,0,	//
                        length+width,-height,0,
                        length,-height,0,
                };
        vertices = verticest;

        //创建顶点坐标数据缓冲
        //vertices.length*4是因为一个整数四个字节
        ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length*4);
        vbb.order(ByteOrder.nativeOrder());//设置字节顺序
        mVertexBuffer = vbb.asFloatBuffer();//转换为int型缓冲
        mVertexBuffer.put(vertices);//向缓冲区中放入顶点坐标数据
        mVertexBuffer.position(0);//设置缓冲区起始位置: 0_左右不翻转 or 3*6_翻转
        //特别提示：由于不同平台字节顺序不同数据单元不是字节的一定要经过ByteBuffer
        //转换，关键是要通过ByteOrder设置nativeOrder()，否则有可能会出问题
        //顶点坐标数据的初始化================end============================

        //顶点着色数据的初始化================begin============================
        float texturest[]=new float[]//顶点颜色值数组，每个顶点4个色彩值RGBA
                {
                        0,0,
                        0,1,
                        1,0,

                        1,0,
                        0,1,
                        1,1
                };
        textures = texturest;
        //创建顶点纹理数据缓冲
        ByteBuffer cbb = ByteBuffer.allocateDirect(textures.length*4);
        cbb.order(ByteOrder.nativeOrder());//设置字节顺序
        mTextureBuffer = cbb.asFloatBuffer();//转换为int型缓冲
        mTextureBuffer.put(textures);//向缓冲区中放入顶点着色数据
        mTextureBuffer.position(0);//设置缓冲区起始位置
        //特别提示：由于不同平台字节顺序不同数据单元不是字节的一定要经过ByteBuffer
        //转换，关键是要通过ByteOrder设置nativeOrder()，否则有可能会出问题
        //顶点着色数据的初始化================end============================
    }

    // 获取页宽
    public int GetPageWidth()
    {
        return (int)width;
    }

    // 获取页高
    public int GetPageHeight()
    {
        return (int)height * 2;
    }

    // 获取左页左上角坐标
    public int GetLeftX0()
    {
        return 0;
    }
    public int GetLeftY0()
    {
        //return (int)-height;
        return 0;
    }

    // 获取右页左上角坐标
    public int GetRightX0()
    {
        return (int)(length * 2 + width);
    }
    public int GetRightY0()
    {
        return 0;
    }

    public int InLeftOrRight(int xInView, int yInView)	// 0:left, 1:right, -1无效
    {
        if (xInView > 0 && xInView < width
                && yInView > 0  && yInView < height * 2)
        {
            return 0;
        }

        int rightx0 = GetRightX0();
        if (xInView > rightx0 && xInView < rightx0 + width
                && yInView > 0  && yInView < height * 2)
        {
            return 1;
        }

        return -1;
    }

    // 显示纹理
    // texId正面, texId2背面
    public void drawSelf(GL10 gl,int paperId, float paperAngle, int texIdSelect, int reverse)
    {

        //int face = 0;

        //计算此幅照片的角度
//    	float tempAngle = paperAngle;
//	    int texIdSelect = -1; //texId2;
//	    if ((tempAngle > 270 && tempAngle <= 360)
//	    	|| (tempAngle >= 0 && tempAngle < 90))
//	    {
//	    	texIdSelect = texId;
//	    }
//	    else
//	    {
//	    	texIdSelect = texId2;
//	    }

        //绑定当前纹理
        if (texIdSelect == -1)
        {
            return;
        }

        if (reverse == 0)
        {
            mVertexBuffer.position(0);// 0_not hov reverse or 3*6_hov reverse
        }
        else
        {
            mVertexBuffer.position(18); // 0_not hov reverse or 3*6_hov reverse
        }

        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);//启用顶点坐标数组
        //为画笔指定顶点坐标数据
        gl.glVertexPointer
                (
                        3,				//每个顶点的坐标数量为3  xyz
                        GL10.GL_FLOAT,	//顶点坐标值的类型为 GL_FLOAT
                        0, 				//连续顶点坐标数据之间的间隔
                        mVertexBuffer	//顶点坐标数据
                );

        //gl_FrontFacing = 0;

        //开启纹理
        gl.glEnable(GL10.GL_TEXTURE_2D);
        //允许使用纹理ST坐标缓冲
        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
        //为画笔指定纹理ST坐标缓冲
        gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, mTextureBuffer);

        // 从视点看过去，每页朝向视频的是正面还是反面？
        // 视频坐标
        float viewPoint[] = new float[3];
        viewPoint[0] = 0;
        viewPoint[1] = 0;
        viewPoint[2] = 10;
        //gltGetNormalVector()

        gl.glBindTexture(GL10.GL_TEXTURE_2D, texIdSelect);

        //绘制图形
        gl.glDrawArrays
                (
                        GL10.GL_TRIANGLES, 		//以三角形方式填充
                        0, 			 			//开始点编号
                        vCount					//顶点数量
                );

        //关闭纹理
        gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
        gl.glDisable(GL10.GL_TEXTURE_2D);
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
    }



    ///////////// 向量操作

    // Given three points on a plane in counter clockwise order, calculate the unit normal
    void gltGetNormalVector(float[] vP1, float[] vP2, float[] vP3, float[] vNormal)
    {
        float[] vV1 = new float[3];
        float[] vV2 = new float[3];

        gltSubtractVectors(vP2, vP1, vV1);
        gltSubtractVectors(vP3, vP1, vV2);

        gltVectorCrossProduct(vV1, vV2, vNormal);
        gltNormalizeVector(vNormal);
    }

    // 向量减法
    void gltSubtractVectors(float vFirst[], float vSecond[], float vResult[])
    {
        vResult[0] = vFirst[0] - vSecond[0];
        vResult[1] = vFirst[1] - vSecond[1];
        vResult[2] = vFirst[2] - vSecond[2];
    }

    // Calculate the cross product of two vectors
    void gltVectorCrossProduct(float[] vU, float[] vV, float[] vResult)
    {
        vResult[0] = vU[1]*vV[2] - vV[1]*vU[2];
        vResult[1] = -vU[0]*vV[2] + vV[0]*vU[2];
        vResult[2] = vU[0]*vV[1] - vV[0]*vU[1];
    }

    // Scales a vector by it's length - creates a unit vector
    void gltNormalizeVector(float[] vNormal)
    {
        float fLength = 1.0f / gltGetVectorLength(vNormal);
        gltScaleVector(vNormal, fLength);
    }

    // Gets the length of a vector
    float gltGetVectorLength(float[] vVector)
    {
        return (float)Math.sqrt(gltGetVectorLengthSqrd(vVector));
    }

    // Scales a vector by a scalar
    void gltScaleVector(float[] vVector, float fScale)
    {
        vVector[0] *= fScale; vVector[1] *= fScale; vVector[2] *= fScale;
    }

    // Gets the length of a vector squared
    float gltGetVectorLengthSqrd(float[] vVector)
    {
        return (vVector[0]*vVector[0]) + (vVector[1]*vVector[1]) + (vVector[2]*vVector[2]);
    }

}

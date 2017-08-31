package com.epiano.commutil;

//可能被拾取选中照片的描述信息存储对象所属类
public class CandidateDis implements Comparable<CandidateDis>
{
	float currAngleSpan;//此幅照片与270度角的夹角
	float currAngle;//此幅照片的角度
	int index;//此幅照片的索引

	public CandidateDis(float currAngleSpan,float currAngle,int index)
	{
		this.currAngleSpan=currAngleSpan;
		this.currAngle=currAngle;
		this.index=index;
	}

	@Override
	public int compareTo(CandidateDis another)
	{
		//比较两幅照片谁与270度的夹角小
		if(this.currAngleSpan<another.currAngleSpan)
		{
			return -1;
		}
		if(this.currAngleSpan==another.currAngleSpan)
		{
			return 0;
		}
		if(this.currAngleSpan>another.currAngleSpan)
		{
			return 1;
		}
		return 0;
	}


	public boolean isInXRange(Board tp, float x,float y)//xy为NEAR面上的触控坐标
	{
		//计算照片内侧点（靠近中心）的X坐标
		double xn=tp.length*Math.cos(Math.toRadians(currAngle));
		//计算照片外侧点（远离中心）的X坐标
		double xw=(tp.length+tp.width)*Math.cos(Math.toRadians(currAngle));
		//计算照片内侧点（靠近中心）的Z坐标
		double zn=-tp.length*Math.sin(Math.toRadians(currAngle))+ Constant.CENTER_Z;
		//计算照片外侧点（远离中心）的Z坐标
		double zw=-(tp.length+tp.width)*Math.sin(Math.toRadians(currAngle))+ Constant.CENTER_Z;

		//根据等比三角形原理分别计算出两个点在NEAR面上的X坐标投影
		double proj_xn=-Constant.NEAR*xn/zn;
		double proj_xw=-Constant.NEAR*xw/zw;

		//分别求出两个投影X坐标中大的和小的，有利于进行范围逻辑判断
		double xmax=Math.max(proj_xn, proj_xw);
		double xmin=Math.min(proj_xn, proj_xw);

		//触控处的Y投影范围
		double k=x/ Constant.NEAR;
		double p=xn/(zn- Constant.CENTER_Z);
		double zq= Constant.CENTER_Z*p/(p+k);
		double xq=-k*zq;
		double oa=Math.sqrt(x*x+ Constant.NEAR* Constant.NEAR);
		double ob=Math.sqrt(xq*xq+zq*zq);
		double yq=oa*tp.height/ob;

		if(x<xmax&&x>xmin)
		{//若在范围内返回true
			if(y>-yq&&y<yq)
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		else
		{//若不在范围内返回false
			return false;
		}
	}
}

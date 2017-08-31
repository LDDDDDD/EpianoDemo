package com.epiano.eLearning;
 
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.UUID;


public abstract class CTr {

	
	public UUID uuid = null;
	public String uniqueId = null;
	int retrytimes = 0;
	int resendInterval = 500; // ms
	int timeoutlong = 0;
	CTransport mTrans = null;
	
	InetAddress mDstAddress;
    int mDstPort = 0; // = 8800;
    DatagramSocket mSocket;
	
	long T0 = 0;	
	
	boolean mFinished = false;
	
	final int UUIDLEN = 36;
	
	public CTr()
	{
		uuid = UUID.randomUUID();
		uniqueId = uuid.toString();
		//int len = uniqueId.length();
		
		T0 = System.currentTimeMillis();
	}
	
	public void setDstAddr(InetAddress DstAddress, int DstPort, DatagramSocket socketIn)
	{
		mDstAddress = DstAddress;
		mDstPort = DstPort;
		mSocket = socketIn;
	}
	
	public void setTimeout(int ms)
	{
		timeoutlong = ms;
	}
	
	public void setT0(long t0)
	{
		T0 = t0;
	}
	
	public void setResendInterval(int t)
	{
		resendInterval = t;
	}
	
//	public void setTransprot(CTransport Trans)
//	{
//		mTrans = Trans;
//	}
	
	public boolean timeout(long t1)
	{
		if (t1 - T0 >= timeoutlong)
		{
			return true;
		}
		
		return false;
	}
	
	public void kill()
	{
		mFinished = true;
	}
	
	public boolean killed()
	{
		return mFinished;
	}
	
	public abstract int Resend(long t1);
//	{
//		if (t1 - T0 >= resendInterval * retrytimes)
//		{
//			retrytimes++;
//			
//			
//			
//			return 1;
//		}
//		
//		return 0;
//	}
	
//	public int send(byte data[], int datalen)
//	{
//		mTrans.send(mDstAddress, mDstPort, data, datalen);
//		
//		return 1;
//	}
	
	public abstract int rcv(int msgtype, byte data[], int offset, int datelen);	
	
	public abstract int TimeOutNotify();

//	public class CTimerE extends CTimer {
//		
//		public CTimerE()
//		{
//			
//		}
//		
//	}
//	
//	public class CFifoE extends CFifo {
//		
//		public CFifoE()
//		{
//			
//		}		
//	}
//	
//	public class CTransportE extends CTransport {
//		
//		public CTransportE()
//		{
//			
//		}		
//	}

	
}
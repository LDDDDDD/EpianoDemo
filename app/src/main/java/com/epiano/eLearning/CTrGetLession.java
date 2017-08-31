package com.epiano.eLearning;


public class CTrGetLession extends CTr{


	public CTrGetLession()
	{

	}

	// (teacher, student,date,before/after,tuples), student可通配
	public int setQuery(long teacherid, long studentid, String time, int beforeafter, int tuples)
	{
		return 1;
	}

//	public int send(byte data[], int datelen)
//	{
//		return 1;
//	}

	@Override
	public int rcv(int msgtype, byte data[], int offset, int datelen)
	{
		return 1;
	}

	@Override
	public int TimeOutNotify()
	{
		return 1;
	}

	@Override
	public int Resend(long t1)
	{
		if (t1 - T0 >= resendInterval * (retrytimes+1))
		{
			retrytimes++;

			//RegPk.UdpSend(mDstAddress, mDstPort, mepd.GetSN(), System.currentTimeMillis()); // RegPk.udppk, RegPk.udppkLen);

			return 1;
		}

		return 0;
	}

	public int Go()
	{
		// send msg

		return 1;
	}

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
package com.epiano.eLearning;
 
import java.net.InetAddress;


public class CTransport {
	
	//Socket msocket = null;
	CUdpPacket updpack = null;
	
	public CTransport()
	{
		
	}
	
	public void SetSocket(CUdpPacket udpp)
	{
		updpack = udpp;
	}
	
	public int send(InetAddress DstAddress, int DstPort, byte data[], int datalen)
	{
		long Tick = System.currentTimeMillis();
		//updpack.UdpSend(DstAddress, DstPort, Tick);
		
		return 1;
	}
	
}
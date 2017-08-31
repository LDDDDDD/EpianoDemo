package com.epiano.eLearning;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;


public class CUdpPacket {

	private static String TAG = "Video";

	DatagramSocket socket = null;

	final int MAX_UDP_SIZE = 2048;
	public byte [] udppk = new byte[MAX_UDP_SIZE];
	public int udppkLen = 0;
	int mSN = 0;

	public CUdpPacket()
	{    		// socket init
//		try {
//			socket = new DatagramSocket();  //创建套接字
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
		//socket = socketIn;
	}

	public void SetSocket(DatagramSocket socketIn)
	{
		socket = socketIn;
	}


	void SetZero(byte dst[], int offset, int len) {
		for (int i = 0; i < len; i++) {
			dst[offset + i] = 0;
		}
	}

	void AddItem(byte pudppk[], byte data[], int MaxLen, int pos)
	{
		int l = Math.min(data.length, MaxLen);
		System.arraycopy(data, 0, pudppk, pos, l);
		SetZero(pudppk, pos + l, MaxLen - l);
	}

//	void reset()
//	{
//		udppkLen = 0;
//	}

	// 生成一个udp包

	//nt mCRC16 = 0;
	public int FromUdpPkt(int mMsgType, byte uuid[])
	{
		//long tick = System.currentTimeMillis() & 0xFFFF;

		int i = 0;
		//udppk = new byte[MAX_UDP_SIZE];

		// Hdr
		{
//			setushort(udppk, i, mCRC16);
			i += 2;

//			setushort(udppk, i, tick);
//			i += 2;
//			setint(udppk, i, tick);
			i += 4;

			setushort(udppk, i, mSN++);				// 包序列号
			i += 2;

			//mMsgType
			setushort(udppk, i, mMsgType);
			i += 2;

			// send Report
			// sendbyte
//			setint(udppk, i, (int)ByteSendToal);
			i += 4;

			// uuid
			AddItem(udppk, uuid, UUIDLEN, i);
			i += UUIDLEN;
		}

		udppkLen = i;

		// crc计算
		//i = 0;
		//mCRC16 = GetCRC16(udppk, 2, udppkLen - 2);
		//setushort(udppk, i, mCRC16);

		return udppkLen;
	}

	final int UUIDLEN = 36;
	public int AppendUUId2UdpPkt(String uuid) {
		int offset = udppkLen;
		int i = offset;
		int l = 0;

		{
			// T
			setushort(udppk, i, 0); // (int)UDP_ITEM.UDP_ITEM_REG.ordinal());
			i += 2;

			// L
			// udppk[udppkLen + i] = lenIn;
			i += 2;

			// V
			AddItem(udppk, uuid.getBytes(), UUIDLEN, i);
			i += UUIDLEN;
		}

		return 1;
	}

	//
	public void SetUdpSN(int SN)
	{
		//setushort(udppk, 2, (int)tick);
		setushort(udppk, 2, (int)SN);
	}

	//
	public void SetUdpTick(long tick)
	{
		//setushort(udppk, 2, (int)tick);
		setint(udppk, 2, (int)tick);
	}

	// 计算udp包crc
	public void CRCUdpPkt()
	{
		int i = 0;

		// 保证长度是偶数
		if ((udppkLen % 2) == 1)
		{
			udppkLen++;
		}

		int CRC16 = GetCRC16(udppk, 2, udppkLen - 2);
		setushort(udppk, i, CRC16);
	}


	public void setushort(byte by[], int offset, int value)
	{
		by[offset] 		= (byte)(value & 0xFF);
		by[offset + 1] 	= (byte)((value >> 8) & 0xFF);
	}
	public void setint(byte by[], int offset, int value)
	{
		by[offset] 		= (byte)(value & 0xFF);
		by[offset + 1] 	= (byte)((value >> 8) & 0xFF);
		by[offset + 2] 	= (byte)((value >> 16) & 0xFF);
		by[offset + 3] 	= (byte)((value >> 24) & 0xFF);
	}
	public int getushort(byte by[], int offset)
	{
//				short v1 = (short)(by[offset] & 0xFF);
//				short v2 = (short)(by[offset + 1] & 0xFF);
//				short value = (short)((v2 << 8) + v1);
//				int value = (int)(((by[offset + 1] & 0xff) << 8) | (by[offset] & 0xff));
//				int value = (int)(((int)((by[offset + 1]) << 8)) | (by[offset]));
		int targets = (by[offset] & 0xff) | ((by[offset + 1] << 8) & 0xff00);

		return targets;
	}
	public int getint(byte by[], int offset)
	{
		int targets = (by[offset] & 0xff) | ((by[offset + 1] << 8) & 0xff00)
				| ((by[offset + 2] << 16) & 0xff0000) | ((by[offset + 3] << 24) & 0xff000000);
		return targets;
	}

	public int getBytes(byte by[], int offset, byte out[], int len)
	{
		System.arraycopy(by, offset, out, 0, len);

		return 1;
	}

	// 计算crc, bytelen必须为2的倍数
	public short GetCRC16(byte data[], int offset, int bytelen)
	{
//		short crc = 0;
////				int shortlen = bytelen / 2;
//		short shortd;

//		{
//			shortd = (short)((((short)1) << 8) + 1);
//			crc ^= shortd;
//			shortd = (short)((((short)10) << 8) + 1);
//			crc ^= shortd;
//			shortd = (short)((((short)15) << 8) + 1);
//			crc ^= shortd;
//		}

//		for(int i = 0 + offset; i < bytelen; i += 2)
//		{
//			shortd = (short)((((short)data[i]) << 8) + data[i+1]);
//
//			crc ^= shortd;
//		}

		byte crc0 = 0;
		byte crc1 = 0;
		for(int i = 0 + offset; i < bytelen; i += 2)
		{
			crc0 ^= data[i];
			crc1 ^= data[i+1];
		}

		//short crcR = (short)((short)(crc0 << 8) | crc1);
		short crcR = (short)((crc0 & 0xff) | ((crc1 << 8) & 0xff00));

		return crcR;
	}

	//DatagramSocket socket = null;  //创建套接字
	InetAddress address; // = InetAddress.getByName("192.168.1.80");  //服务器地址
	public void UdpSend(InetAddress address, int port, int SN, long Tick) //, byte data[], int datalen)
	{
		if (socket == null)
		{
			Log.e(TAG, "socket error!");
			return;
		}

		//if (false)
		{
			SetUdpSN(SN);

			// 打时间戳
			SetUdpTick(Tick);// & 0xFFFF

			// 计算包校验
			CRCUdpPkt();

			try {
				//	            InetAddress address = InetAddress.getByName("192.168.1.80");  //服务器地址
				//	            int port = 8080;  //服务器的端口号
				//创建发送方的数据报信息
				DatagramPacket dataGramPacket = new DatagramPacket(udppk, udppkLen, address, port);

				//DatagramSocket socket1 = new DatagramSocket();  //创建套接字
				socket.send(dataGramPacket);  //通过套接字发送数据
				//	            socket.close();
				//	        } catch (UnknownHostException e) {
				//	            e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}
}
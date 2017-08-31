package com.epiano.eLearning;

// 处理来自服务器的消息
public class CUdpRegMsg extends CUdpPacket {

	private static String TAG = "Video";

	// 注册消息

	final int USRIDLEN = 20;
	final int USRNAMELEN = 20;
	final int PWDLEN = 8;
	final int IPLEN = 16;
	final int PHONEMODLEN = 10;
	final int MACLEN = 17; // xx:xx:xx:xx:xx:xx

	public byte usrid[] = new byte[USRIDLEN];
	public byte usrname[] = new byte[USRNAMELEN];
	public byte pwd[] = new byte[PWDLEN];
	public byte localIP[] = new byte[IPLEN];
	public int localPort = 0;
	public byte PhoneModel[] = new byte[PHONEMODLEN];
	public byte MAC[] = new byte[MACLEN];
	public int NatType = 0;
	public int RegAcked = 0;
	public int SessionId = 0;
	public int keepalive = 0;

	public CUdpRegMsg() {

	}

	// 注册消息
	// byte usrname[], byte pwd[], byte localIP[], int localPort, byte
	// PhoneModel[], byte MAC[], int NatType
	public int AppendRegMsg2UdpPkt() {
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

			setint(udppk, i, SessionId);
			i += 4;

			// id, 用户名, PWD, Local IP, port, phone model, MAC, NAT type,
			AddItem(udppk, usrid, USRIDLEN, i);
			i += USRIDLEN;

			AddItem(udppk, usrname, USRNAMELEN, i);
			i += USRNAMELEN;

			AddItem(udppk, pwd, PWDLEN, i);
			i += PWDLEN;

			AddItem(udppk, localIP, IPLEN, i);
			i += IPLEN;

			setushort(udppk, i, localPort);
			i += 2;

			AddItem(udppk, PhoneModel, PHONEMODLEN, i);
			i += PHONEMODLEN;

			AddItem(udppk, MAC, MACLEN, i);
			i += MACLEN;

			setushort(udppk, i, NatType);
			i += 2;

			udppk[i] = (byte)keepalive;
			i += 1;
		}

		int itemLen = i - offset; // 长度含T L V

		// L
		setushort(udppk, offset + 2, itemLen);

		udppkLen += itemLen;

		return itemLen;
	}
}
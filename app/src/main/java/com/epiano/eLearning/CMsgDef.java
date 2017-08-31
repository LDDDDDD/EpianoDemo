package com.epiano.eLearning;

//enum MSG_TYPE
//{
//	MSG_TYPE_NULL,
//		
//	MSG_TYPE_REG,
//	MSG_TYPE_REG_ACK,
//
//	MSG_TYPE_CALL,
//	MSG_TYPE_CALL_ACK,	
// 
//	MSG_TYPE_BUTT,
//}

public class CMsgDef {
	public static final int MSG_TYPE_REG = 1;
	public static final int MSG_TYPE_REG_ACK = 2;
	public static final int MSG_TYPE_REG_ACK_UNREG = 3;
	public static final int MSG_TYPE_REG_ACK_WRONG_PWD = 4;
	public static final int MSG_TYPE_REG_TIMEOUT = 5;
	
	public static final int MSG_TYPE_GET_FRIEND = 6;
	public static final int MSG_TYPE_GET_FRIEND_ACK = 7;

	public static final int MSG_TYPE_CALL = 8;
	public static final int MSG_TYPE_CALL_ACK = 9;	

	
}
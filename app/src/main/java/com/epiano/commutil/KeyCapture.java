package com.epiano.commutil;

import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * Created by John on 2016/11/13 0013.
 */
public class KeyCapture {

    // process key events
    // cach key envents

    static byte NULL_KEY = 127;

    enum COM_STATUS {
        COM_STATUS_RESETING,         // 0
        COM_STATUS_NORMAL,           // 1
    };

    enum KEY_ENVENT {
        KEY_ENVENT_UP,	 	         // 0
        KEY_ENVENT_DOWN,           // 1
    };

    // EVENT definition
    enum EVENT_TYPE
    {
        EVENT_NULL,

        EVENT_KEEP_ALIVE,										// keep alive

        EVENT_RESET,												// reset

        EVENT_KEYDOWN,											// key down
        EVENT_KEYUP,												// key up

        EVENT_ACK,													// ack

        EVENT_BUTT,
    };

    // BT串行接收整理缓冲区
    final int SIZEOFEVENT = 10; //9;
    byte BTRcvBuf[] = new byte[SIZEOFEVENT];

    int NumofKeysCaptured;              // num of key captured
    private static int MaxNumofKey = 5000;     // max capacity of key captured

    static int PIANO_KEY_COUNT = 89;            // 88 keys & 1 step
    KEY_ENVENT KeyStatus[] = new KEY_ENVENT[PIANO_KEY_COUNT];        // 1: key is down; 0: key is up
    int KeyDownPos[] = new int[PIANO_KEY_COUNT];                    // key pos in KeyList when key is down, for speed OPT.

    // 通信状态
    COM_STATUS mComStatus = COM_STATUS.COM_STATUS_RESETING;

    // status
    int BadKeyIdCount = 0;
    int BadKeyStatusCount = 0;

    public ArrayList<KeyHit> KeyList = new ArrayList<KeyHit>(); // Cach All Keys
    public ArrayList<KeyHit> KeyListDrawing = new ArrayList<KeyHit>(); // Cach All drawing Keys
    public ArrayList<KeyHit> KeyListForAck = new ArrayList<KeyHit>(); // 接收队列, 连续性处理队列, 接收的Key要进行SN连续性检查

    static int RcvDataBufMaxLen = 1000;
    byte RcvDataBuf[];
    int RcvDataBufLen = 0;
    private ArrayList<byte[]> RcvDataList = new ArrayList<byte[]>();
    //List RcvDataList = new ArrayList();

    long SYNC_KB_T0 = 0;            // 键盘发送的首个同步时刻, 键盘时钟系
    long SYNC_PHONE_T0 = 0;        // 键盘发送的首个同步时刻, 本机时钟系
    long KB_t_oveflowtimes = 0;     // 键盘t溢出次数
    long KB_t_last = 0;            // 上一个键盘消息的t
    long KB_t_integal = 0;         // 消除回滚 ， 连续化的键盘当前的时间
    //long SYNC_PHONE_KB_T_DIFF = 0; // 本机与键盘时钟的差异
    double TTR = 1;                 // KB与phone的tick长度比，用于时间换算

    byte SndMsgBuf[] = new byte[100];   // 固定结构： msg: msgtype + random + chksum + data0 + data1

    //BTClient mBTC;  // 用于bluetooth发送
    OutputStream BlueToothOs;

    //int mTransactionId = ((byte)System.currentTimeMillis()) & 0xFF;

    static int TRID_RESET = 200;      // 对端收到此消息后，会复位TRId
    static int MIN_TRID = 201;         // TRID取值范围
    static int MAX_TRID = 210;
    int mTRId = MIN_TRID;            // 当前正在使用的TRID
    long TRIdInterVal = System.currentTimeMillis(); // 变更TRID的时间间隔控制

    static int MAX_SN_NUM = 199;            // 最大SN号
    static int MAX_SN_NUM_HALF = 100;

    // 已经确定的SN号
    int AckedSN = -1;
    int AckedSNMsgCount = 0;

    // test
    String smsg = "";    //显示用数据缓存
    String s = "";

    public KeyCapture() { //OutputStream os) {    // BTClient BTC
        // TODO Auto-generated constructor stub

        //mBTC = BTC;
        //BlueToothOs = os;

        RcvDataBuf = new byte[RcvDataBufMaxLen + 1];

//        KeyInfoInit();
//
//        // init key status
//        for(int i = 0; i < PIANO_KEY_COUNT; i++)
//        {
//            KeyStatus[i] = KEY_ENVENT.KEY_ENVENT_UP; //
//        }

        LocalReset();

    }
    int SetBlueToothOutputStream(OutputStream os) {
        BlueToothOs = os;

        return 1;
    }
    int LocalReset()
    {
        //mTRId = TRID_RESET;
        KeyInfoInit();

        KB_t_last = 0;
        KB_t_oveflowtimes = 0;
        SYNC_KB_T0 = 0;
//        AckedSN = 0;

        // init key status
        for(int i = 0; i < PIANO_KEY_COUNT; i++)
        {
            KeyStatus[i] = KEY_ENVENT.KEY_ENVENT_UP; //
        }

        RcvDataList.clear();
        KeyListForAck.clear();
        //KeyList.clear();
        KeyListDrawing.clear();

        return 1;
    }

    // 递增TRId号
    int IncCurTRId()
    {
        ++mTRId;
        if (mTRId >= MAX_TRID)
        {
            mTRId = MIN_TRID;
        }
        AckedSN = -1;

        return mTRId;
    }
    // reset key board
    void ResetKeyboard()
    {
        long ctick = System.currentTimeMillis();

        // 控制变更TRId的时间间隔
        if (ctick - TRIdInterVal <= 700) {
            return;
        }
        TRIdInterVal = ctick;

        LocalReset();

//        AckedSN = -1;

        // msg: msgtype + random + chksum + data0 + data1
//        EVENT_TYPE.EVENT_KEYDOWN
        //SndMsgBT(mTRId, (int)EVENT_TYPE.EVENT_RESET.ordinal(), 0, 0); // EVENT_RESET
        int mTRId = TRID_RESET;
        SndMsgBT(0, 0, mTRId, 0);
        SndMsgBT(0, 0, mTRId, 0);
        SndMsgBT(0, 0, mTRId, 0);
        SndMsgBT(0, 0, mTRId, 0);
    }

    void UpdateClientTRid()
    {
        long ctick = System.currentTimeMillis();

        // 控制变更TRId的时间间隔
        if (ctick - TRIdInterVal <= 700) {
            return;
        }
        TRIdInterVal = ctick;

        LocalReset();

//        AckedSN = -1;

        // msg: msgtype + random + chksum + data0 + data1
//        EVENT_TYPE.EVENT_KEYDOWN
        //SndMsgBT(mTRId, (int)EVENT_TYPE.EVENT_RESET.ordinal(), 0, 0); // EVENT_RESET

        IncCurTRId();

        SndMsgBT(0, 0, mTRId, 0);
        SndMsgBT(0, 0, mTRId, 0);
        SndMsgBT(0, 0, mTRId, 0);
        SndMsgBT(0, 0, mTRId, 0);
    }

    // Snd keyevent ack
    void SndKeyEventAck(int AckSN)
    {
        // msg: msgtype + random + chksum + data0 + data1
//        EVENT_TYPE.EVENT_KEYDOWN
        //SndMsgBT(mTRId, (int)EVENT_TYPE.EVENT_ACK.ordinal(), (int)((AckSN & 0xFF00) >> 8), (int)(AckSN & 0xFF)); // EVENT_RESET
        SndMsgBT(0, 0, AckSN, 0);
        //SndMsgBT(0, 0, AckSN, 0);
        //SndMsgBT(0, 0, AckSN, 0);
    }

    void SndMsgBT(int trid, int msgtype, int data0, int data1)
    {
        SndMsgBuf[0] = (byte)data0;
        //mBTC.SendBT(SndMsgBuf, 1);

        if (BlueToothOs == null) {
            return;
        }

        try {
            BlueToothOs.write(SndMsgBuf);
        }catch(IOException e){
            Log.i("WG", "SendBT() fail.");
        }
        /*
        if (false) {
            // msg: msgtype + random + chksum + data0 + data1
            SndMsgBuf[0] = (byte) msgtype;
            SndMsgBuf[1] = (byte) System.currentTimeMillis();
            SndMsgBuf[3] = (byte) data0;
            SndMsgBuf[4] = (byte) data1;
            SndMsgBuf[2] = (byte) (SndMsgBuf[0] ^ SndMsgBuf[1] ^ SndMsgBuf[3] ^ SndMsgBuf[4]);
            mBTC.SendBT(SndMsgBuf, 5); // EVENT_RESET
        }
        else {
            // msg: msgtype + random + chksum + data0 + data1
            SndMsgBuf[0] = (byte) 0;
            SndMsgBuf[1] = (byte) trid;
            SndMsgBuf[2] = (byte) msgtype;
            SndMsgBuf[3] = (byte) System.currentTimeMillis();
            SndMsgBuf[4] = (byte) 0;    // sn0
            SndMsgBuf[5] = (byte) 0;    // sn1
            SndMsgBuf[6] = (byte) 0;     // chksum
            SndMsgBuf[7] = (byte) data0;

            byte sum = 0;
            for(int i = 0; i < 8; i++) {
                sum ^= SndMsgBuf[i];
            }
            SndMsgBuf[6] = sum;
            //SndMsgBuf[6] = (byte) (SndMsgBuf[0] ^ SndMsgBuf[1] ^ SndMsgBuf[2] ^ SndMsgBuf[3] ^ SndMsgBuf[4] ^ SndMsgBuf[5] ^ SndMsgBuf[7]);

            // test
            for(int i = 0; i < 8; i++) {
                SndMsgBuf[i] = (byte)(0x31 + i);
            }

            mBTC.SendBT(SndMsgBuf, 8); // EVENT_RESET
        }
        */
    }

    ////////////////////////////////////////////// 当keyboard只报告key down事件时，通过本预处理把事件转化为down and up事件
    static int KEY_BUF_LEN = 24;   // 不要超过128, 注意循环变量
    static int NULL_KEYID = 128;
    static int KEY_LIFE = 10;//20; //30 10 50;
    int gKeyBuf[] = new int[KEY_BUF_LEN];
    int gKeyLife[] = new int[KEY_BUF_LEN];
    void KeyInfoInit()
    {
        int k;

        for(k = 0; k < KEY_BUF_LEN; k++)
        {
            gKeyBuf[k] = NULL_KEYID;
        }

        //KeyLifeThread.start();
    }
    // 在gKeyBuf[]中查找keyid对应的存储位置
    int GetKeyInfoIdx(int keyid)
    {
        int k;

        for(k = 0; k < KEY_BUF_LEN; k++)
        {
            if (gKeyBuf[k] == keyid)
            {
                return (byte)k;
            }
        }

        return NULL_KEYID;
    }
    // 获得一个空闲的gKeyBuf[]位置
    int GetIdleIdx()
    {
        int k;

        for(k = 0; k < KEY_BUF_LEN; k++)
        {
            if (gKeyBuf[k] == NULL_KEYID)
            {
                return k;
            }
        }

        return NULL_KEYID;
    }
    // 按键
    int OnKeyDown(int keyid, int pos)
    {
        gKeyBuf[pos] 	= keyid;
        gKeyLife[pos] 	= KEY_LIFE;

        // send to com...
        //send(gKeyBuf[pos] + 128);		// 高位为1表示key down

        int keyid_out = keyid + 128;   // 128: down

        OnKeyEventV0((byte)keyid_out, (int)System.currentTimeMillis());  // 输出key

        return keyid_out;
    }
    // 扫描键盘, 报告down事件
    int OnKeyEvent_reporting_down_only(byte KeyIdIn)
    {
        int keyint = KeyIdIn & 0xFF;            // turn key to unsigned
        byte KeyId = (byte)(keyint & 0x7f);          // key id

        char c;
        //int KeyId = PowerLine_Col_Id; // 0~87
        int idx;

        int keyId_out = NULL_KEYID;

        idx = GetKeyInfoIdx(KeyId);
        if (idx != NULL_KEYID)
        {
            gKeyLife[idx] 	= KEY_LIFE;		// 曾经按下的key
        }
        else
        {									// 新按下的key
            idx = GetIdleIdx();
            if (idx != NULL_KEYID)
            {
                keyId_out = OnKeyDown(KeyId, idx); // tx
                return keyId_out;
            }
        }

        return NULL_KEYID;
    }
    // gKeyBuf[]中的key生命值递减
    void KeyLifeGo()
    {
        int k;

        for(k = 0; k < KEY_BUF_LEN; k++)
        {
            if (gKeyBuf[k] != NULL_KEYID)
            {
                if (gKeyLife[k] == 1)
                {
                    // key up

                    // send to com...
                    //send(gKeyBuf[k]);	// 高位为0表示按键取消
                    byte keyid_out = (byte)(gKeyBuf[k] + 0);   // 0: up
                    OnKeyEventV0(keyid_out, (int)System.currentTimeMillis());  // 输出key

                    gKeyLife[k] = 0;
                    gKeyBuf[k] = NULL_KEYID;
                }
                else
                {
                    gKeyLife[k]--;
                }
            }
        }
    }

    //////////////////////////////////////////////

    int SNCompare(int sn0, int sn1)
    {
        if (sn0 == sn1)
        {
            return 0;
        }
        else if (sn0 < sn1)
        {
            if (sn1 - sn0 < MAX_SN_NUM_HALF)
            {
                return -1;
            }

            return 1;
        }
        else
        {
            if (sn0 - sn1 < MAX_SN_NUM_HALF)
            {
                return 1;
            }

            return -1;
        }

    }

    void OnKeyEventV2(byte RcvData) {

    	byte[] rd = new byte[1];
        //RcvDataList.add(RcvData);
    	rd[0] = RcvData;
    	RcvDataList.add(rd);

        int r;
        // 处理消息缓冲, 当
        while(RcvDataList.size() >= SIZEOFEVENT) //sizeof(struct EVENT))
        {
            r = GetValidMsg();  // RcvDataList
        }

    }

    // 检查SN是否正好比AckedSN大1，即连续
    boolean SNContinous(int SN)
    {
        if (SN == ((AckedSN + 1) % MAX_SN_NUM))
        {
            return true;
        }

        return false;
    }

    // 处理连续SN消息
    int ProcessAckingEvent()
    {
        int i;
        int r;
        int n;
        KeyHit khit = null;
        KeyHit khitIt = null;

        n = KeyListForAck.size();

        // 检查连续SN事件数
        for (i = 0; i < n; i++) {
            khitIt = KeyListForAck.get(i);
            if (!SNContinous(khitIt.sn)) {
                break;
            }
            AckedSN = (AckedSN + 1) % MAX_SN_NUM;
        }

        n = i;
        for (i = 0; i < n; i++) {
            khitIt = KeyListForAck.get(0); //i);

//            if (khitIt.key_envent == EVENT_TYPE.EVENT_KEYDOWN) // EVENT_KEYDOWN
//            {
////                key += 128;
////                OnKeyEvent(key, timestamp);
//            }
//            else if (khitIt.key_envent == EVENT_TYPE.EVENT_KEYUP) // EVENT_KEYUP
//            {
////                OnKeyEvent(key, timestamp);
//            }
            OnKeyEvent(khitIt);

            KeyListForAck.remove(0);
        }

        // 回Ack消息, 结合定时器加强回复...
        if (n > 0)
        {
            //SndMsgBT(0, 0, AckedSN, 0);
            SndKeyEventAck(AckedSN);

            Log.i("WG", "ProcessAckingEvent(): n = " + mTRId + ", AckedSN: " + AckedSN);
        }

        return 1;
    }

    int mMsgCount = 0;

    int GetValidMsg() {

//        struct EVENT
//        {
//            uchar tag0;													// 00, peer master Transaction ID
//            //uchar tag1;	// 00
//            ushort chksum;												// chksum of event
//            uchar trid;
//            uchar type;													// enum EVENT_TYPE
//            ushort SN;													// ê??tsno?, ?é??1?
//            ushort timestamp;										// ?Yó?é¨?è??o?ì?′ú uchar
//            //uchar EventLen;											// ê??t3¤?è
//            uchar Data[1];											// êy?Y, e.g. è?1?ê?keyê??t, ±íê?keyid
//        };



        int i;
        int r;
        int trid;
        int SN;                                                    // ê??tsno?, ?é??1?
        int SN0, SN1;                                                    // ê??tsno?, ?é??1?
        int EventLen = 0;                                            // ê??t3¤?è
        int type;                                                    // enum EVENT_TYPE

        int timestamp;                                        // ?Yó?é¨?è??o?ì?′ú
        int ts0, ts1;

        int chksum;                                                // chksum of event
        int sum = 0;
        //int Data[1];											// êy?Y, e.g. è?1?ê?keyê??t, ±íê?keyid

        int chksumLocal = 0;

        byte tag0;
        byte tag1;
//        byte BTRcvBuf;
        byte d1;
        byte BTRcvBuf[] = new byte[SIZEOFEVENT];
        int data0;

        int len = RcvDataList.size();

        if (len <= 2) {
            return 0;
        }

        // start pos check
        int pre = 0;
        i = 0;
        //BTRcvBuf[pre] = (byte) RcvDataList.get(0);
        BTRcvBuf[pre] = (byte) RcvDataList.get(0)[0];
        if (BTRcvBuf[pre] != -1) {
            RcvDataList.remove(0);

            return 0;
        }
//        chksumLocal ^= (BTRcvBuf[i] & 0xff) + i;
        tag0 = BTRcvBuf[pre++];
//        RcvDataList.remove(0);

        BTRcvBuf[pre] = (byte) RcvDataList.get(pre)[0];
        pre++;
        BTRcvBuf[pre] = (byte) RcvDataList.get(pre)[0];
        chksum = ((BTRcvBuf[pre] & 0xff) << 8) + (BTRcvBuf[pre - 1] & 0xff);	// tbd, & 0xff可能存在问题
        pre++;

        for(i = pre; i < SIZEOFEVENT; i++)      // sizeof (struct EVENT)
        {
            BTRcvBuf[i] = (byte) RcvDataList.get(i)[0];

            //chksumLocal ^= (BTRcvBuf[i] & 0xff) + i;
        }

        chksumLocal = Crc16(BTRcvBuf, pre, SIZEOFEVENT);
        chksumLocal &= 0xFFFF;    // 取低8位

        i = pre;
        trid = BTRcvBuf[i++] & 0xff;
        type = BTRcvBuf[i++] & 0xff;
        SN0 = BTRcvBuf[i++] & 0xff;
        SN1 = BTRcvBuf[i++] & 0xff;
        SN = (SN1 << 8) + SN0;
//        chksum = BTRcvBuf[i++] & 0xff;

        ts0 = BTRcvBuf[i++] & 0xff;
        ts1 = BTRcvBuf[i++] & 0xff;
        timestamp = (ts1 << 8) + ts0;
        //timestamp = BTRcvBuf[i++] & 0xff;

        data0 = BTRcvBuf[i++] & 0xff;

        //chksumLocal = SN0 + SN1 + EventLen + type + timestamp + data0;

        //chksumLocal = tag0 ^ trid ^ type ^ timestamp ^ SN0 ^ SN1 ^ sum ^ data0; // EventLen ^
        //chksumLocal = (tag0+0) ^ (trid+1) ^ (type+2) ^ (timestamp+3) ^ (SN0+4) ^ (SN1+5) ^ (sum+6) ^ (data0+7); // EventLen ^
        if (chksum != chksumLocal) {

            RcvDataList.remove(0);

            Log.i("WG", "Msg error: tag0_"+tag0
                    + ", trid_" + trid
                    + ", type_" + type
                    + ", ts_" + timestamp
//                    + ", SN0_" + SN0
//                    + ", SN1_" + SN1
                    + ", SN_" + SN
                    + ", data0_" + data0
                    + ", chksum_" + chksum
                    + ", chksumLocal_" + chksumLocal
            );

            return 0;
        }

        for (int l = 0; l < i; l++) {
            RcvDataList.remove(0);
        }

        //Log.i("WG", "Msg type: " + type + ", trid: " + trid);

        long tick = System.currentTimeMillis();

        // 与键盘进行时间同步， 把时间差异记录到SYNC_KB_PHONE_T_DIFF
        if (mTRId == trid) {
            if (SYNC_KB_T0 == 0) {
                SYNC_KB_T0 = timestamp * 10; // unit: 10ms
                //SYNC_PHONE_KB_T_DIFF = tick - timestamp;

                SYNC_PHONE_T0 = tick;
            }
            else
            {
                //TTR = ((double)timestamp * 10 - SYNC_KB_T0) / (tick - SYNC_PHONE_T0);
            	TTR = ((double)KB_t_integal - SYNC_KB_T0) / (tick - SYNC_PHONE_T0);
            }
        }

        // 键盘时钟益处/回滚次数
        if (timestamp < KB_t_last && KB_t_last - timestamp > 30000)
        {
            KB_t_oveflowtimes++;
        }
        KB_t_last = timestamp;
        // 键盘时钟连续化
        KB_t_integal = (timestamp + KB_t_oveflowtimes * 65536) * 10; // unit: 10ms

        if (type == EVENT_TYPE.EVENT_KEEP_ALIVE.ordinal()) {
            Log.i("WG", "Msg type: " + type + ", trid: " + trid + ", TS: " + timestamp + ", ScanSpeed: " + SN + ", D: " + data0 + ", TTR: " + TTR);
        }
        else {
            Log.i("WG", "Msg type: " + type + ", trid: " + trid + ", TS: " + timestamp + ", SN: " + SN + ", D: " + data0);
        }

        if (type == EVENT_TYPE.EVENT_KEEP_ALIVE.ordinal()) // EVENT_KEEP_ALIVE
        {
            if (trid == mTRId)
            {
                // 迁正常接收状态, 不再发送RESET/变更TRId
                mComStatus = COM_STATUS.COM_STATUS_NORMAL;
            }
            else
            {
                mComStatus = COM_STATUS.COM_STATUS_RESETING;

                UpdateClientTRid(); // 要求对端变更TRId，重新开始新的接收事务

                Log.i("WG", "UpdateClientTRid(): " + mTRId);

                // local reset ...
            }

            return 0;
        }


        // test
//        if ((mMsgCount++ % 2) == 0)
//        {
//            return 0;
//        }


        // 接收状态检测
        if (mComStatus != COM_STATUS.COM_STATUS_NORMAL)
        {
            Log.i("WG", "Key msg rcved while status is unnormal.");

            return 0;
        }

        // TRId检查
        if (trid != mTRId)
        {
            return 0;
        }

        EVENT_TYPE enventtype = EVENT_TYPE.EVENT_KEYUP;
        if (type == EVENT_TYPE.EVENT_KEYDOWN.ordinal() // EVENT_KEYDOWN
                || type == EVENT_TYPE.EVENT_KEYUP.ordinal())
        {
            KeyHit khit = null;
            KeyHit khitIt = null;

            byte key;
            key = (byte)data0;

            // key range check
            int keyint = key & 0xFF;            // turn key to unsigned
            //int keystatus = keyint & 128;       // down or up
            int keyid = keyint & 0x7f;          // key id
            if (keyid >= PIANO_KEY_COUNT)
            {
                BadKeyIdCount++;

                Log.i("WG", "Bad key, key id is " + String.valueOf(keyid) + ", BadKeyIdCount is " + String.valueOf(BadKeyIdCount) + ".");

                return 0;
            }

            if (type == EVENT_TYPE.EVENT_KEYDOWN.ordinal()) // EVENT_KEYDOWN
            {
                enventtype = EVENT_TYPE.EVENT_KEYDOWN;
            }
            else if (type == EVENT_TYPE.EVENT_KEYUP.ordinal())
            {
                enventtype = EVENT_TYPE.EVENT_KEYUP;
            }

            r = SNCompare(SN0, AckedSN);
            if (r <= 0)
            {
                // 数据太旧, 丢弃

                if (AckedSN != -1) {
                    SndKeyEventAck(AckedSN);
                    Log.i("WG", "Data too old, SndKeyEventAck(), count: " + AckedSNMsgCount++);
                }

                return 0;
            }


            if (KeyListForAck.size() == 0) {

                // 连续性处理队列为空

//                r = SNCompare(SN0, AckedSN);
//                if (r <= 0) {
//                    // 老数据，忽略
//
//                    SndKeyEventAck(AckedSN);
//
//                    return 0;
//                }

                //OnKeyEvent(key, timestamp);

                if (SNContinous(SN0))
                {
                    // SN与AckedSN连续: 加速, 直接输出，不必缓冲，更新AckedSN

                    // 输出
                    khit = new KeyHit();
                    khit.keyId = key;
                    //khit.setT0(KB_t_integal); //tick);
                    if (enventtype == EVENT_TYPE.EVENT_KEYDOWN) {
                        khit.setT0(KB_t_integal);
                    }else if (enventtype == EVENT_TYPE.EVENT_KEYUP) {
                        khit.setT1(KB_t_integal);
                    }
                    khit.sn = SN0;
                    khit.key_envent = enventtype;

                    OnKeyEvent(khit);

                    AckedSN = (AckedSN + 1) % MAX_SN_NUM;

                    SndKeyEventAck(AckedSN);

                    Log.i("WG", "1 KeyListForAck.size(): " + KeyListForAck.size() + ", AckedSN: " + AckedSN);

                    return 1;
                }
                else
                {
                    // 不连续

                    // 缓存，待连续处理

                    khit = new KeyHit();
                    khit.keyId = key;
                    //khit.setT0(tick);
                    if (enventtype == EVENT_TYPE.EVENT_KEYDOWN) {
                        khit.setT0(KB_t_integal);
                    }else if (enventtype == EVENT_TYPE.EVENT_KEYUP) {
                        khit.setT1(KB_t_integal);
                    }
                    khit.sn = SN0;
                    khit.key_envent = enventtype;
                    KeyListForAck.add(khit);

                    Log.i("WG", "2 KeyListForAck.size(): " + KeyListForAck.size() + ", AckedSN: " + AckedSN);
                }
            }
            else {
                // 队列已有成员

                int n = KeyListForAck.size();

                // 插入排序位置
                i = 0;
                for (; i < n; i++) {
                    khitIt = KeyListForAck.get(i);
                    r = SNCompare(SN0, khitIt.sn);
                    if (r < 0) {
                        khit = new KeyHit();
                        khit.keyId = key;
                        //khit.setT0(tick);
                        if (enventtype == EVENT_TYPE.EVENT_KEYDOWN) {
                            khit.setT0(KB_t_integal);
                        }else if (enventtype == EVENT_TYPE.EVENT_KEYUP) {
                            khit.setT1(KB_t_integal);
                        }
                        khit.sn = SN0;
                        khit.key_envent = enventtype;
                        KeyListForAck.add(i, khit);

                        Log.i("WG", "3 KeyListForAck.size(): " + KeyListForAck.size() + ", AckedSN: " + AckedSN);

                        break;
                    }
                    else if (r == 0)
                    {
                        // 数据已存在

                        Log.i("WG", "4 KeyListForAck.size(): " + KeyListForAck.size());

                        if (AckedSN != -1) {
                            SndKeyEventAck(AckedSN);
                        }

                        return 0;
                    }
                }
                if (i == n) {
                    // 追加
                    khit = new KeyHit();
                    khit.keyId = key;
                    //khit.setT0(tick);
                    if (enventtype == EVENT_TYPE.EVENT_KEYDOWN) {
                        khit.setT0(KB_t_integal);
                    }else if (enventtype == EVENT_TYPE.EVENT_KEYUP) {
                        khit.setT1(KB_t_integal);
                    }
                    khit.sn = SN0;
                    khit.key_envent = enventtype;
                    KeyListForAck.add(khit);

                    Log.i("WG", "5 KeyListForAck.size(): " + KeyListForAck.size() + ", AckedSN: " + AckedSN);
                }
            }

            // 检查是否有连续的事件可以处理
            ProcessAckingEvent();

//            if (type == EVENT_TYPE.EVENT_KEYDOWN.ordinal()) // EVENT_KEYDOWN
//            {
//                key += 128;
//
//                OnKeyEvent(key, timestamp);
//            }
//            else if (type == EVENT_TYPE.EVENT_KEYUP.ordinal()) // EVENT_KEYUP
//            {
//                //key += 128;
//                OnKeyEvent(key, timestamp);
//            }
        }


        return 1;
    }

    //
    void OnKeyEvent(KeyHit khit)
    {
        String s = "";

        // buff size check
        if (KeyList.size() > MaxNumofKey)
        {
            Log.i("WG", "Key buff is full, size is " + String.valueOf(MaxNumofKey));
            return;
        }

        int keyid = khit.keyId & 0x7f;

        KEY_ENVENT ke = KEY_ENVENT.KEY_ENVENT_UP;

        // key status check
        if (khit.key_envent == EVENT_TYPE.EVENT_KEYUP)
        {
            if (KeyStatus[keyid] == KEY_ENVENT.KEY_ENVENT_UP)
            {
                BadKeyStatusCount++;

                Log.i("WG", "Bad key status(up), key id is " + String.valueOf(keyid) + ", BadKeyStatusCount is " + String.valueOf(BadKeyStatusCount) + ".");

                return;
            }
        }
        else if (khit.key_envent == EVENT_TYPE.EVENT_KEYDOWN)
        {
            ke = KEY_ENVENT.KEY_ENVENT_DOWN;

            if (KeyStatus[keyid] == KEY_ENVENT.KEY_ENVENT_DOWN)
            {
                BadKeyStatusCount++;

                Log.i("WG", "Bad key status(down), key id is " + String.valueOf(keyid) + ", BadKeyStatusCount is " + String.valueOf(BadKeyStatusCount) + ".");

                return;
            }
        }

        long tick = System.currentTimeMillis();

        if (khit.key_envent == EVENT_TYPE.EVENT_KEYDOWN)
        {
            KeyList.add(khit);              // new key
            KeyDownPos[keyid] = KeyList.size() - 1;

            KeyListDrawing.add(khit);   // save for drawing

            //khit.setT0(ts); //tick);

            s += " DD_" + String.valueOf((int)(khit.keyId) - 0); // 128); // test
        }
        else if (khit.key_envent == EVENT_TYPE.EVENT_KEYUP)
        {
            //Log.i("WG", "Key up " + keyid);

            //Log.i("WG", "Key up " + keyid + ", KeyDownPos[keyid]" + KeyDownPos[keyid]);
            KeyHit khit2 = KeyList.get(KeyDownPos[keyid]);  // get key
            khit2.setT1(khit.t1); //tick);

            s += " UU_" + String.valueOf(khit.keyId); // test
        }

        // test
        Log.i("WG", s + " buffer[i]" + String.valueOf(khit.keyId) + ",");
        //smsg += s;

        KeyStatus[keyid] = ke;

        //KeyCapView.postInvalidate();
    }

    //
    void OnKeyEventV0(byte key, int timestap)
    {
        String s = "";

        // buff size check
        if (KeyList.size() > MaxNumofKey)
        {
            Log.i("WG", "Key buff is full, size is " + String.valueOf(MaxNumofKey));
            return;
        }
//        if (KeyList.size() > 300) // test
//        {
//            KeyList.remove(0);
//        }

        // check envent type: KEY_ENVENT_UP, KEY_ENVENT_DOWN

        int keyint = key & 0xFF;            // turn key to unsigned
        int keystatus = keyint & 128;       // down or up
        int keyid = keyint & 0x7f;          // key id

        // key range check
        if (keyid >= PIANO_KEY_COUNT)
        {
            BadKeyIdCount++;

            Log.i("WG", "Bad key, key id is " + String.valueOf(keyid) + ", BadKeyIdCount is " + String.valueOf(BadKeyIdCount) + ".");

            return;
        }

        KEY_ENVENT ke;

        long tick = System.currentTimeMillis();

        // get key event type
        if ((keystatus) == 0)
        {
            ke = KEY_ENVENT.KEY_ENVENT_UP;
        }
        else
        {
            ke = KEY_ENVENT.KEY_ENVENT_DOWN;
        }

        // key status check
        if (ke == KEY_ENVENT.KEY_ENVENT_UP)
        {
            if (KeyStatus[keyid] == KEY_ENVENT.KEY_ENVENT_UP)
            {
                BadKeyStatusCount++;

                Log.i("WG", "Bad key status(up), key id is " + String.valueOf(keyid) + ", BadKeyStatusCount is " + String.valueOf(BadKeyStatusCount) + ".");

                return;
            }
        }
        else if (ke == KEY_ENVENT.KEY_ENVENT_DOWN)
        {
            if (KeyStatus[keyid] == KEY_ENVENT.KEY_ENVENT_DOWN)
            {
                BadKeyStatusCount++;

                Log.i("WG", "Bad key status(down), key id is " + String.valueOf(keyid) + ", BadKeyStatusCount is " + String.valueOf(BadKeyStatusCount) + ".");

                return;
            }
        }

        KeyHit khit = null;

        if (ke == KEY_ENVENT.KEY_ENVENT_DOWN)
        {
            khit = new KeyHit();
            khit.keyId = keyid;
            KeyList.add(khit);              // new key
            KeyDownPos[keyid] = KeyList.size() - 1;

            KeyListDrawing.add(khit);   // save for drawing

            khit.setT0(tick);
           // khit.keyId = (byte)keyid;

            //Log.i("WG", "Key down " + keyid);

            s += " UU_" + String.valueOf((int)(key&0xff) - 128); // test
        }
        else if (ke == KEY_ENVENT.KEY_ENVENT_UP)
        {
            //Log.i("WG", "Key up " + keyid);

            //Log.i("WG", "Key up " + keyid + ", KeyDownPos[keyid]" + KeyDownPos[keyid]);
            khit = KeyList.get(KeyDownPos[keyid]);  // get key
            khit.setT1(tick);

            s += " DD_" + String.valueOf(key); // test
        }

        // test
        Log.i("WG", s + " buffer[i]" + String.valueOf(key) + ",");
        //smsg += s;

        KeyStatus[keyid] = ke;

        //KeyCapView.postInvalidate();
    }

    static int  auchCRCHi[] = {
            0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x01, 0xC0,
            0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41,
            0x00, 0xC1, 0x81, 0x40, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0,
            0x80, 0x41, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40,
            0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1,
            0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x01, 0xC0, 0x80, 0x41,
            0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1,
            0x81, 0x40, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41,
            0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x01, 0xC0,
            0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x00, 0xC1, 0x81, 0x40,
            0x01, 0xC0, 0x80, 0x41, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1,
            0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40,
            0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x01, 0xC0,
            0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x00, 0xC1, 0x81, 0x40,
            0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0,
            0x80, 0x41, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40,
            0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x01, 0xC0,
            0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41,
            0x00, 0xC1, 0x81, 0x40, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0,
            0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41,
            0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0,
            0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x00, 0xC1, 0x81, 0x40,
            0x01, 0xC0, 0x80, 0x41, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1,
            0x81, 0x40, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41,
            0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x01, 0xC0,
            0x80, 0x41, 0x00, 0xC1, 0x81, 0x40
    };
    static int  auchCRCLo[] = {
            0x00, 0xC0, 0xC1, 0x01, 0xC3, 0x03, 0x02, 0xC2, 0xC6, 0x06,
            0x07, 0xC7, 0x05, 0xC5, 0xC4, 0x04, 0xCC, 0x0C, 0x0D, 0xCD,
            0x0F, 0xCF, 0xCE, 0x0E, 0x0A, 0xCA, 0xCB, 0x0B, 0xC9, 0x09,
            0x08, 0xC8, 0xD8, 0x18, 0x19, 0xD9, 0x1B, 0xDB, 0xDA, 0x1A,
            0x1E, 0xDE, 0xDF, 0x1F, 0xDD, 0x1D, 0x1C, 0xDC, 0x14, 0xD4,
            0xD5, 0x15, 0xD7, 0x17, 0x16, 0xD6, 0xD2, 0x12, 0x13, 0xD3,
            0x11, 0xD1, 0xD0, 0x10, 0xF0, 0x30, 0x31, 0xF1, 0x33, 0xF3,
            0xF2, 0x32, 0x36, 0xF6, 0xF7, 0x37, 0xF5, 0x35, 0x34, 0xF4,
            0x3C, 0xFC, 0xFD, 0x3D, 0xFF, 0x3F, 0x3E, 0xFE, 0xFA, 0x3A,
            0x3B, 0xFB, 0x39, 0xF9, 0xF8, 0x38, 0x28, 0xE8, 0xE9, 0x29,
            0xEB, 0x2B, 0x2A, 0xEA, 0xEE, 0x2E, 0x2F, 0xEF, 0x2D, 0xED,
            0xEC, 0x2C, 0xE4, 0x24, 0x25, 0xE5, 0x27, 0xE7, 0xE6, 0x26,
            0x22, 0xE2, 0xE3, 0x23, 0xE1, 0x21, 0x20, 0xE0, 0xA0, 0x60,
            0x61, 0xA1, 0x63, 0xA3, 0xA2, 0x62, 0x66, 0xA6, 0xA7, 0x67,
            0xA5, 0x65, 0x64, 0xA4, 0x6C, 0xAC, 0xAD, 0x6D, 0xAF, 0x6F,
            0x6E, 0xAE, 0xAA, 0x6A, 0x6B, 0xAB, 0x69, 0xA9, 0xA8, 0x68,
            0x78, 0xB8, 0xB9, 0x79, 0xBB, 0x7B, 0x7A, 0xBA, 0xBE, 0x7E,
            0x7F, 0xBF, 0x7D, 0xBD, 0xBC, 0x7C, 0xB4, 0x74, 0x75, 0xB5,
            0x77, 0xB7, 0xB6, 0x76, 0x72, 0xB2, 0xB3, 0x73, 0xB1, 0x71,
            0x70, 0xB0, 0x50, 0x90, 0x91, 0x51, 0x93, 0x53, 0x52, 0x92,
            0x96, 0x56, 0x57, 0x97, 0x55, 0x95, 0x94, 0x54, 0x9C, 0x5C,
            0x5D, 0x9D, 0x5F, 0x9F, 0x9E, 0x5E, 0x5A, 0x9A, 0x9B, 0x5B,
            0x99, 0x59, 0x58, 0x98, 0x88, 0x48, 0x49, 0x89, 0x4B, 0x8B,
            0x8A, 0x4A, 0x4E, 0x8E, 0x8F, 0x4F, 0x8D, 0x4D, 0x4C, 0x8C,
            0x44, 0x84, 0x85, 0x45, 0x87, 0x47, 0x46, 0x86, 0x82, 0x42,
            0x43, 0x83, 0x41, 0x81, 0x80, 0x40
    };
    int Crc16(byte pucFrame[], int offset, int usLen)
    {
 /* int result;
 BYTE high,low;*/
        int i = offset;
        int ucCRCHi = 0xFF;
        int ucCRCLo = 0xFF;
        int iIndex = 0x0000;
        usLen -= offset;
        while (usLen-- > 0)
        {
            iIndex = (int)(ucCRCLo ^ (pucFrame[i++] & 0xFF));
            ucCRCLo = (int)((ucCRCHi ^ auchCRCHi[iIndex]) & 0xFF);
            ucCRCHi = auchCRCLo[iIndex];
        }
        return (ucCRCHi << 8 | ucCRCLo);
    }

    // one key hit
    public class KeyHit {

        int keyId = NULL_KEY;

        EVENT_TYPE key_envent = EVENT_TYPE.EVENT_KEYUP;

        int timestamp = 0;  // record TS from keyboard

        long t0_phone = 0;  // key downtime, phone time
        long t0 = 0;        // key down time, KB time
        long t1 = 0;        // key up time
        //boolean wait2deleteFromDrawingList = false;

        long t_draw = 0;    // last drawing end t

        int sn = -1;


        boolean drawed = false;

        byte strength = 1;  // 1~10, 10: max power

        public KeyHit() {
            // TODO Auto-generated constructor stub

        }

        void setTS(int ts)
        {
            timestamp = ts;
        }

        void setT0(long t)
        {
            t0 = t;
            t_draw = t0;
        }

        void setT1(long t)
        {
            t1 = t;
        }

        int CheckIntegral()
        {
            if (t0 == 0 || t1 == 0 || keyId == NULL_KEY)
            {
                return 0;
            }

            return 1;
        }

        long GetKeyDurationInMs()
        {
            return t1 - t0;
        }
    }

}

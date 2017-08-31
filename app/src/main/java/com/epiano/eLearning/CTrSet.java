package com.epiano.eLearning;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;

public class CTrSet {

	static Timer timer;

	static Map<String, CTr> TrMap = null;
	static Map<String, CTr> TrMapTmp = null;
	//Map<String, int> TrMapTimeout = null;

	//CTransport mTrans;


	public CTrSet()
	{
		TrMap = new HashMap<String, CTr>();

		timer = new Timer();
		timer.schedule(new MyTimerTask1(), 50); // ms
	}

	public void Stop()
	{
		if (timer != null)
		{
			timer.cancel();
		}
	}

	// 登记TR
	public int addTr(String uuid, CTr tr)
	{
		if (tr == null)
		{
			return 0;
		}

		synchronized(TrMap)
		{
			CTr trInMap = TrMap.get(uuid);
			if (trInMap != null)
			{
				TrMap.remove(uuid);
			}

			TrMap.put(uuid,  tr);
		}

		return 1;
	}

	// 分发TR
	@SuppressWarnings("null")
	public int dispather(int msgtype, String uuid, byte data[], int offset, int datalen)
	{
		if (data == null)
		{
			return 0;
		}

		synchronized(TrMap)
		{
			CTr trInMap = TrMap.get(uuid);
			if (trInMap == null)
			{
				return 0;
			}

			if (trInMap.killed() == false)
			{
				trInMap.rcv(msgtype, data, offset, datalen);
			}
		}

		return 1;
	}

	private static void TimeOutProcess()
	{
		long tick = System.currentTimeMillis();

		TrMapTmp = new HashMap<String, CTr>();

		boolean mapchanged = false;

		synchronized(TrMap)
		{

			Iterator<Entry<String, CTr>> itr = TrMap.entrySet().iterator();
			while (itr.hasNext()) {

				Entry entry = itr.next();
				//CTr tr = (CTr)entry.getValue();

				// get key
				String key = (String)entry.getKey();
				// get value
				CTr tr = (CTr)entry.getValue();

				if (tr.killed())
				{
					//itr.remove();

					mapchanged = true;
				}
				else if (tr.timeout(tick))
				{
					//itr.remove();
					tr.TimeOutNotify();

					tr.kill();

					mapchanged = true;
				}
				else
				{
					tr.Resend(tick);

					TrMapTmp.put(key, tr);
				}
			}

			if (mapchanged)
			{
				TrMap.clear();
				TrMap = TrMapTmp;
			}
			else
			{
				TrMapTmp.clear();
			}
		}
	}

	static class MyTimerTask1 extends TimerTask {
		public void run() {
			TimeOutProcess();

			timer.schedule(new MyTimerTask1(), 50); // ms
		}
	}

}
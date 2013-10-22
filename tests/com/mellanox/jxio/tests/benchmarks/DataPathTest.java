package com.mellanox.jxio.tests.benchmarks;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.mellanox.jxio.ClientSession;
import com.mellanox.jxio.EventQueueHandler;
import com.mellanox.jxio.Msg;
import com.mellanox.jxio.MsgPool;


public class DataPathTest implements Runnable {

	EventQueueHandler        eqh;
	ClientSession            cs;
	MsgPool                  msgPool;

	private int  msgSize;
	private long cnt, print_cnt;
	boolean firstTime;
	long startTime;
	private final static Log LOG = LogFactory.getLog(DataPathTest.class.getCanonicalName());

	public DataPathTest(String url, int msg_size) {
		msgSize = msg_size;
		eqh = new EventQueueHandler();
		cs =  new ClientSession(eqh, url, new DataPathClientCallbacks());
		msgPool = new MsgPool(2048, msgSize, msgSize);
		int msgKSize = msgSize/1024;
	    if(msgKSize == 0) {
	    	print_cnt = 4000000;
	    }
	    else {
	    	print_cnt = 4000000/msgKSize;
	    	System.out.println("print_cnt = " + print_cnt);
	    }
		cnt = 0;
		firstTime=true;
	}

	public void run() {
		
		for (int i=0; i<512; i++) {
			Msg msg = msgPool.getMsg();
			if(msg == null) {
				print("Cannot get new message");
				break;
			}
			
			if(! cs.sendMessage(msg)) {
				print("Error sending");
				msgPool.releaseMsg(msg);
			}
		}
		
		eqh.runEventLoop(-1, -1);
	}
	
	public void print(String str) {
		LOG.debug("********" + str);
	}

	public static void main(String[] args) {
		if (args.length < 3) {
			System.out.println("StatMain $IP $PORT $MSG_SIZE");
			return;
		}
		String url = "rdma://" + args[0] + ":" + Integer.parseInt(args[1]);
		Runnable test = new DataPathTest(url, Integer.parseInt(args[2]));
		test.run();
	}
	class DataPathClientCallbacks implements ClientSession.Callbacks {

		public void onMsgError() {
			// logger.log(Level.debug, "onMsgErrorCallback");
			print("onMsgErrorCallback");
		}

		public void onSessionEstablished() {
			// logger.log(Level.debug, "[SUCCESS] Session Established");
			print(" Session Established");
		}

		public void onSessionError(int session_event, String reason) {

			String event;
			switch (session_event) {
				case 0:
					event = "SESSION_REJECT";
					break;
				case 1:
					event = "SESSION_TEARDOWN";
					print(" Session Teardown");
					eqh.breakEventLoop();
					break;
				case 2:
					event = "CONNECTION_CLOSED";
					// This is fine - connection closed by choice
					// there are two options: close session or reopen it
					break;
				case 3:
					event = "CONNECTION_ERROR";
					// this.close(); //through the bridge calls connection close
					break;
				case 4:
					event = "SESSION_ERROR";
					break;
				default:
					event = "UNKNOWN_EVENT";
					break;
			}
			print("GOT EVENT " + event + " because of " + reason);
		}

		public void onReply(Msg msg) {
			print("Got Reply");
			if(firstTime) {
				startTime = System.nanoTime();
				firstTime = false;
			}
			
			cnt++;
			
			if(cnt == print_cnt) {				
				long delta = System.nanoTime() - startTime;
				long pps = (cnt*1000000000)/delta;
				double bw = (1.0*pps*msgSize/(1024*1024));
				//System.out.println("delta = " + delta + " cnt = " + cnt);
				System.out.println("TPS = " + pps + " BW =" + bw + " msg_size =" +msgSize);
				
				cnt = 0;
				startTime = System.nanoTime();
			}
				
			if(! cs.sendMessage(msg)) {
				msgPool.releaseMsg(msg);
			}
		}
	}
}
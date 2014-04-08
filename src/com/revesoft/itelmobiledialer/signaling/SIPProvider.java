package com.revesoft.itelmobiledialer.signaling;

import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;

import com.revesoft.itelmobiledialer.video.decoding.CodecParameters;

public class SIPProvider   {

	public static final boolean DEBUG = true;
	public static final int BUFFER_SIZE = 1400;
	public static volatile InetSocketAddress videoAddress;
	public static int localVideoPort = 33333;
	public static int videoPort = 33333;
	public static String mediaIP = "127.0.0.1";
	public volatile boolean running = true;
	public static volatile DatagramSocket videoSocket ;
	
	/*public static int videoWidth = 352;
	public static int videoHeight = 288;*/
	
	public static int videoWidth = 320;
	public static int videoHeight = 240;
	
	public static void initSocket()
	{
		try {
			videoAddress = new InetSocketAddress(mediaIP.toString(),
					videoPort);
			videoSocket = new DatagramSocket(localVideoPort);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void closeSocket()
	{
		videoSocket.close();
	}
	public SIPProvider() { }

	public static final int CALLTYPE_AUDIO = 0;
	public static final int CALLTYPE_VIDEO = 1;
	public volatile static int callType = CALLTYPE_VIDEO;

	public static final int PACK_MODE_ZERO = 0;
	public static final int PACK_MODE_ONE = 1;
	public static volatile int videoCodecType = CodecParameters.CODEC_ID_H263_1998;
	//public static volatile int videoCodecType = CodecParameters.CODEC_ID_H264;


}

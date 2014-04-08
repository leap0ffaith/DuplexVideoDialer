package com.revesoft.itelmobiledialer.video.stream.packetizer;

import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;

import com.revesoft.itelmobiledialer.video.encoding.Frame;

import android.util.Log;

/**
 * An abstract class for converting continuous video data into RTP packets.
 * Runs of a separate thread. This class should be subclassed for each type
 * of video codec. The subclasses should define their own packetization rules.
 * 
 * @author Sufian Latif
 */
public abstract class VideoPacketizer implements Runnable {
	private final String TAG = "VideoPacketizer";
	
	/**
	 * Set this to <code>true</code> to get Log messages from this class.
	 */
	private static final boolean DEBUG = true;

	/**
	 * The {@link InputStream} that provides video data.
	 */
//	protected InputStream input;
	
	/**
	 * An instance of {@link RTPSocket} wich would send the packets.
	 */
	protected RTPSocket rtpSocket;
	
	/**
	 * The buffer to store video data. This should be the buffer of <code>rtpSocket</code>.
	 */
	protected byte[] buffer;
	
	/**
	 * Length of RTP header defined in <code>rtpSocket</code>.
	 */
	protected int rtphl = RTPSocket.HEADER_LENGTH;
	
	/**
	 * Maximum RTP packet length defined in <code>rtpSocket</code>.
	 */
	//protected int maxSize = RTPSocket.MAX_SIZE;
	protected int maxSize = RTPSocket.MAX_SIZE;
	
	/**
	 * The thread runs while this is <code>true</code>.
	 */
	protected boolean streaming;
	
	protected BlockingQueue<Frame> queue;

	/**
	 * Creates a VideoPacketizer.
	 * 
	 * @param in The {@link InputStream} that supplies video data
	 * @throws SocketException If an error occurs while creating or binding the socket
	 */
	public VideoPacketizer(BlockingQueue<Frame> queue) throws SocketException {
		rtpSocket = new RTPSocket();
//		input = in;
		this.queue = queue;
		buffer = rtpSocket.getBuffer();
	}

	/**
	 * Connects <code>rtpSocket</code> to a remote address defined by IP address and port number.
	 * @param ip IP address of the remote host
	 * @param port Port number of the remote host
	 * @throws UnknownHostException If the address lookup fails
	 */
//	public void connect(String ip, int port) throws UnknownHostException {
//		try{
//		    
//			InetAddress targetAddress = InetAddress.getByName(ip);
//			//Log.d("DEBUG", "targetAddress: " + targetAddress.toString());
//			
//			SocketAddress sockAddress = new InetSocketAddress(targetAddress, port);
//			try{
//				rtpSocket.connect(sockAddress);
//			}catch(Exception e){
//				Log.d("DEBUG", "couldn't connect: " + e.getMessage());
//				e.printStackTrace();
//			}
//			rtpSocket.setTarget(targetAddress, port);
//		}catch(Exception e){
//			Log.d("DEBUG", "connect failed: " + e.getMessage());
//			e.printStackTrace();
//		}
//	}

	/**
	 * Starts a new thread that runs this packetizer.
	 */
	public void start() {
		new Thread(this).start();
	}

	/**
	 * Stops the thread.
	 */
	public void stop() { 
		streaming = false;
		rtpSocket.close();
	}
	
	public RTPSocket getSocket(){
		return this.rtpSocket;
	}

	public void pause() {
		// TODO Auto-generated method stub
		streaming = false;
	}

	public void resume(InputStream inputStream) {
		// TODO Auto-generated method stub
//		this.input = inputStream;
		start();
	}
}

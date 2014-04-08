package com.revesoft.itelmobiledialer.video.receiver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import android.os.Environment;
import android.util.Log;

import com.revesoft.itelmobiledialer.signaling.SIPProvider;
import com.revesoft.itelmobiledialer.video.encoding.H264ParameterSets;
import com.revesoft.itelmobiledialer.video.player.VideoPlayer;

/*
 * an abstract class 
 */

public abstract class RtpReceiver extends Thread {

	protected VideoPlayer player = null;
	public static final int rtphl = 12;
	//private FileOutputStream fos;
	//private static final String recv_packets_file = Environment.getExternalStorageDirectory() + "/media/recv_seq";
	//protected boolean isPlaying = false;
	/*
	 * the datagram packet received
	 */
	protected DatagramPacket packet;

	/*
	 * a volatile field indicating the state of the receiver
	 */
	protected static volatile boolean running;

	/*
	 * byte array to hold the packet data
	 */
	protected byte[] buffer;
	/**
	 * The SSRC (Synchronization Source Identifier) of the RTP stream. Value -1
	 * means it is uninitialized.
	 */
	protected int ssrc = -1;
	/*
	 * the timestamp field of the packet
	 */

	protected long tStamp = -1;

	/*
	 * the sequence number field of the packet
	 */

	protected int sequenceNumber = -1;

	protected int packetSize = -1;

	public RtpReceiver() throws SocketException {
		buffer = new byte[2048];
		running = false;
		/*try {
			fos = new FileOutputStream(new File(recv_packets_file));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	}

	/**
	 * For debugging purposes.
	 * 
	 * @return Sequence number of last received packet
	 */
	protected int getSeq() {
		int seq = (((buffer[2] & 0xff) << 8)) | ((int) buffer[3] & 0xff);
		return seq;
	}

	/**
	 * For debugging purposes.
	 * 
	 * @return true if the last received packet was marked
	 */
	protected boolean isMarked() {

		return ((buffer[1] & 0x80) == 0) ? false : true;
	}

	/**
	 * For debugging purposes.
	 * 
	 * @return The timestamp of last received packet
	 */
	protected long getTStamp() {
		return ((buffer[4] & 0xff) << 24) | ((buffer[5] & 0xff) << 16)
				| ((buffer[6] & 0xff << 8)) | (buffer[7] & 0xff);

	}

	protected int getSSRC() {
		return ((buffer[8] & 0xff << 24)) | ((buffer[9] & 0xff << 16))
				| ((buffer[10] & 0xff << 8)) | (buffer[11] & 0xff);
	}

	public void start() {
		running = true;
		if(SIPProvider.videoSocket.isClosed())
			SIPProvider.initSocket();
		
		new Thread(this).start();
		
	}
	
	public void stopReceiving() {
		running = false;
		this.interrupt();
		// this.datagramSocket.close();
	}

	protected void startPlayer() {
		Log.d("tag:isplaying","StartPlayer() function called");
		player.isPlaying = true;
		// player.setH264ParameterSet(new H264ParameterSets(sps, pps));
		player.prepare();
		player.start();
		//player.isPlaying = true;
		//isPlaying = true;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		packet = new DatagramPacket(buffer, buffer.length);
		try {
			if(SIPProvider.videoSocket.isClosed())
			{
				this.stopReceiving();
				return ;
			}
			SIPProvider.videoSocket.setSoTimeout(20000);
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		buffer = new byte[2048];
		while (running) {
			try {
				packet.setData(buffer);
				packet.setLength(buffer.length);
				if(SIPProvider.videoSocket.isClosed())
				{
					running = false;
					break;
				}
				else
				{
					SIPProvider.videoSocket.receive(packet);
				}
				if(!running)
					break;
			} catch (SocketTimeoutException e) {
				Log.d("DEBUG",
						"Can't receive from: "
								+ SIPProvider.videoSocket.getLocalPort());
				e.printStackTrace();
				continue;
			} catch (IOException e) {
				e.printStackTrace();
				break;
			}

			byte[] buf = packet.getData();
			packetSize = packet.getLength();

			if (packetSize == 0)
				continue;

			// Log.d("DEBUG", "packet size: " + packetSize);

			tStamp = getTStamp();

			if (ssrc == -1) {
				ssrc = getSSRC();
			}

			sequenceNumber = getSeq();

			Log.d("sequenceNumber", "sequenceNumber: " + sequenceNumber);
			/*try {
				fos.write(new String(
						sequenceNumber
								+ "\t"
								+ (buffer[4] * 4096 + buffer[5] * 256
										+ buffer[6] * 16 + buffer[7]) + "\t"
								+ buffer[12] + "\n").getBytes());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
			
			processData(buf);

		}

	}

	abstract protected void processData(byte[] buf);

}

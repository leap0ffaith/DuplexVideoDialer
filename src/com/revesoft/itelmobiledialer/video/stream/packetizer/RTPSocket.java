package com.revesoft.itelmobiledialer.video.stream.packetizer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Random;

import android.os.Environment;
import android.util.Log;

import com.revesoft.itelmobiledialer.signaling.SIPProvider;
import com.revesoft.itelmobiledialer.video.encoding.H263Encoder;

/**
 * A simple implementation of an RTP socket.
 * This is basically a {@link DatagramSocket} with a buffer which contains
 * the standard RTP header.
 * 
 * @see <a href = 'http://www.ietf.org/rfc/rfc3550.txt'>RFC 3550</a>
 * for RTP specifications and Section 5 for header description .
 * 
 * @author Sufian Latif
 */
public class RTPSocket { 
	private String TAG = "RTPSocket";

	/**
	 * Set this to <code>true</code> to get Log messages from this class.
	 */
	private static final boolean DEBUG = false;

	/**
	 * Standard RTP header length: 12 bytes.
	 */
	public final static int HEADER_LENGTH = 12;
 
	/**
	 * Maximum length of an RTP packet (including header).
	 */
//	public final static int MAX_SIZE = 10240; 
	public final static int MAX_SIZE = 1400;

	/**
	 * Buffer to hold packet data.
	 */
	private byte [] buffer;

	/**
	 * Payload type.
	 */
	private byte pType;

	/**
	 * RTP sequence number.
	 */
	private short seq;

	/**
	 * Synchronization source identifier.
	 */
	private int ssrc;

	/**
	 * Creates an RTPSocket over a {@link DatagramSocket} on an arbitrary available port.
	 * Initializes the buffer with <code>MAX_SIZE</code> bytes, <code>seq</code> and
	 * <code>ssrc</code> with two random integers and <code>pType</code> with 96 
	 * (dynamic type).
	 * 
	 * @throws SocketException If an error occurs while creating or binding the socket
	 */
	private InetAddress targetAddress;
	
	private int targetPort;
	
	private DatagramSocket rtpSocket;
	
	//private FileOutputStream fos;
	
	//private static final String sent_packets_file = Environment.getExternalStorageDirectory() + "/media/sent_seq"; 
	private final byte[] eo_packet = {'e', 'o', 'p'};
	public RTPSocket() throws SocketException {
		rtpSocket= new DatagramSocket();
		if(DEBUG) Log.i(TAG, "" + rtpSocket.getLocalPort());
		buffer = new byte[MAX_SIZE + 20];
		seq = 0;
		ssrc = new Random().nextInt();

		buffer[0] = (byte) 0x80;
		buffer[1] = 0; // to be set later
		/*try {
			 fos = new FileOutputStream(new File(sent_packets_file));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		setPType((byte) 99);
		setSSRC();
	}

	/**
	 * @return The buffer to write the data on
	 */
	public byte [] getBuffer() {
		return buffer;
	}

	/**
	 * For setting the payload type externally.
	 * @param t Value of the payload type
	 */
	public void setPType (byte t) {
		pType = t;
		buffer[1] |= (t & 0x7f);
	}

	/**
	 * Sets the 'mark' bit on.
	 */
	public void markPacket() {
		buffer[1] |= (byte) 0x80;
	}

	/**
	 * Writes the sequence number to the packet and
	 * updates it.
	 */
	private void setSeq() {
		buffer[2] = (byte)((seq >> 8) & 0x00ff);
		buffer[3] = (byte)(seq & 0x00ff);
		seq++;
	}

	/**
	 * Writes the SSRC to the packet. This will remain unchanged in the whole session.
	 */
	private void setSSRC() {
		buffer[8] = (byte)((ssrc >> 24) & 0xff);
		buffer[9] = (byte)((ssrc >> 16) & 0xff);
		buffer[10] = (byte)((ssrc >> 8) & 0xff);
		buffer[11] = (byte)(ssrc & 0xff);
	}

	/**
	 * Writes the timestamp field.
	 * @param ts A 32-bit value to set
	 */
	public void setTStamp(long ts) {
		buffer[4] = (byte)((ts >> 24) & 0xff);
		buffer[5] = (byte)((ts >> 16) & 0xff);
		buffer[6] = (byte)((ts >> 8) & 0xff);
		buffer[7] = (byte)(ts & 0xff);
	}
 
	/**
	 * Sends the specified number of bytes of the buffer to the remote port.
	 * 
	 * @param size Number of bytes to send
	 * @throws IOException If an error occurs while sending the packet
	 */
	public void send(int size) throws IOException {
		/*Log.d("bufferSize", "bufferSize: "+size);
		H263Encoder.printByteArray(buffer);*/
		setSeq();
		DatagramPacket packet = new DatagramPacket(buffer, size);
		packet.setSocketAddress(SIPProvider.videoAddress);  

//		int ts = ((buffer[4] & 0xff) << 24) + ((buffer[5] & 0xff) << 16) + ((buffer[6] & 0xff) << 8) + (buffer[7] & 0xff);
//		Log.d("DEBUG", "timestamp value while sending packet: " + ts);
//		Log.d("DEBUG", "sequence number value while sending packet: " + seq);
//		if(DEBUG) Log.i(TAG, "Type : " + (buffer[HEADER_LENGTH] & 0x1f) + " Seq : " + seq + " TStamp : " + ts);
//		if(DEBUG) Log.i(TAG, Integer.toHexString(buffer[4] & 0xff) + " " +Integer.toHexString(buffer[5] & 0xff) +
//				" " +Integer.toHexString(buffer[6] & 0xff) + " " +Integer.toHexString(buffer[7] & 0xff));
//		if(DEBUG) Log.i(TAG, ((packet.getData()[2] << 8) + packet.getData()[3]) + " " + new Date().getTime());

		//fos.write(new String((seq-1)+"\t"+(buffer[4]*4096+buffer[5]*256+buffer[6]*16+buffer[7])+"\t"+buffer[12]+"\n").getBytes());
		
		try{
			if(!SIPProvider.videoSocket.isClosed())
				SIPProvider.videoSocket.send(packet);
			//Log.d("DEBUG", "sending successful to "+SIPProvider.videoAddress.getAddress()+":"+SIPProvider.videoAddress.getPort());
		}catch(Exception e){
			e.printStackTrace();
		}
		buffer[1] &= (byte) 0x7f;
	}
	
	public void setTarget(InetAddress target_address, int target_port){
		this.targetAddress = target_address;
		this.targetPort = target_port;
	}
	//---------------------------------------------------------------------------

	public void close() {
		// TODO Auto-generated method stub
		rtpSocket.close();
	}

//	public void connect(SocketAddress sockAddress) throws Exception {
//		// TODO Auto-generated method stub
//		rtpSocket.connect(sockAddress);
//	}
	
	public void changeSocket(DatagramSocket socket){
		rtpSocket = socket;
	}
}

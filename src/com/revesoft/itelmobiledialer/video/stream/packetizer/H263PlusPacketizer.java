package com.revesoft.itelmobiledialer.video.stream.packetizer;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;

import com.revesoft.itelmobiledialer.video.encoding.Frame;
import com.revesoft.itelmobiledialer.video.encoding.H263Encoder;

import android.os.SystemClock;
import android.util.Log;

/**
 * A subclass of {@link VideoPacketizer} for packetizing H.263 encoded video.
 * 
 * @see <a href='http://www.ietf.org/rfc/rfc3984.txt'>RFC 4629</a> for RTP
 *      packetization scheme for H.263 video.
 * 
 * @author Sufian Latif
 */

public class H263PlusPacketizer extends VideoPacketizer {
	private final String TAG = "H263Packetizer";

	/**
	 * Set this to <code>true</code> to get Log messages from this class.
	 */
	private static final boolean DEBUG = true;
	
	/** The starting timestamp of the packetizer.
	 */
	private long startTime;
	
	/**
	 * Creates and {@link H263PlusPacketizer}.
	 * 
	 * @param in
	 *            The {@link InputStream} to read data from
	 * @throws SocketException
	 *             If an error occurs while creating or binding the socket
	 * */

	public H263PlusPacketizer(BlockingQueue<Frame> queue)
			throws SocketException {
		super(queue);
	}

	/**
	 * <code>H263Packetizer</code>'s own scheme of packetizing video data.
	 * <p>
	 * Workflow:
	 * <ol>
	 * <li>Reads and skips bytes from <code>input</code> until it encounters the
	 * byte sequence <code>"mdat"</code>
	 * <li>While the thread is running:
	 * <ul>
	 * <li>Fills <code>buffer</code> with bytes read from <code>input</code>
	 * <li>Scans the buffer for start code <code>00 00 1X</code>
	 * <li>According to search result, sends the frame in single of multiple
	 * packets
	 * 
	 * @see <a href = 'http://www.ietf.org/rfc/rfc3984.txt'>RFC 4629</a> Section
	 *      5.1 for the RTP payload header format of H.263 video.
	 */
	@Override
	public void run() {
		long time, duration = 0, ts = 0;
		//		int i = 0, j = 0, tr;
		boolean firstFragment = true;
		
		startTime = SystemClock.elapsedRealtime();
		streaming = true;
		buffer[rtphl] = 0;
		buffer[rtphl + 1] = 0;

		while (streaming) {
			try {

				time = SystemClock.elapsedRealtime();
				if (DEBUG)
					Log.i(TAG, "" + time);
				Frame frame = queue.take();

				if (frame.frameData.length <= maxSize) {
					sendSingleNALUnit(frame);
				} else {
					sendFragmentedNALUnit(frame);
				}
			} catch (Exception e) {
				streaming = false;
				if (DEBUG)
					Log.e(TAG, "IOException: " + e.getMessage());
				e.printStackTrace();
			}
		}

		if (DEBUG)
			Log.d(TAG, "Packetizer stopped !");
		rtpSocket.close();

	}

	private void sendFragmentedNALUnit(Frame frame) throws Exception{
//		buffer[rtphl] = 4;
//		buffer[rtphl + 1] = 0;
//		System.arraycopy(buffer, rtphl + 2, frame.frameData, 0,
//				maxSize);
//		ts += duration;
//		duration = 0;
//		// The last fragment of a frame has to be marked
//
//		rtpSocket.setTStamp((int) (ts * 90));
//		if (DEBUG)
//			Log.i(TAG, "Sending " + maxSize + " bytes");
//		rtpSocket.send(rtphl + 2 + maxSize);
//
//		int sentDataLength = maxSize;
//
//		while (sentDataLength < frame.frameData.length) {
//			buffer[rtphl] = 0;
//			buffer[rtphl + 1] = 0;
//			if (sentDataLength + maxSize <= frame.frameData.length) {
//				System.arraycopy(buffer, rtphl + 2,
//						frame.frameData, 0, maxSize);
//				rtpSocket.send(rtphl + 2 + maxSize);
//			} else {
//				System.arraycopy(buffer, rtphl + 2,
//						frame.frameData, 0, frame.frameData.length
//						- sentDataLength);
//				rtpSocket.markPacket();
//				rtpSocket.send(rtphl + 2 + frame.frameData.length
//						- sentDataLength);
//				break;
//			}
//			sentDataLength += maxSize;
//
//		}
		
		
		
		
		int n = (frame.frameData.length + maxSize - 1) / maxSize;
		rtpSocket.setTStamp(90 * (SystemClock.elapsedRealtime() - startTime));
		buffer[rtphl+1] = 0;

		for (int i = 0; i < n; i++) {
			int tmp = (frame.frameData.length - i * maxSize > maxSize) ? maxSize : frame.frameData.length - i * maxSize;
			buffer[rtphl] = (byte) (((i == 0 ? 4 : 0)));
			
			System.arraycopy(frame.frameData, i * maxSize, buffer, rtphl + 2, tmp); // old
			if (i == n - 1)
				rtpSocket.markPacket();
			rtpSocket.send(rtphl + 2 + tmp);
		}
		
	}

	private void sendSingleNALUnit(Frame frame) throws Exception{
		buffer[rtphl] = 4;
		buffer[rtphl + 1] = 0;
		System.arraycopy(frame.frameData, 0, buffer, rtphl + 2, 
				frame.frameData.length);

		rtpSocket.markPacket();
		rtpSocket.setTStamp(90 * (SystemClock.elapsedRealtime() - startTime));
		
		if (DEBUG)
			Log.i(TAG, "Sending " + frame.frameData.length + " bytes");
		rtpSocket.send(rtphl + 2 +frame.frameData.length);
	}
}

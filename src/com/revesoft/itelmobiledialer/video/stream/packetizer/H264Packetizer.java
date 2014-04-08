package com.revesoft.itelmobiledialer.video.stream.packetizer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;

import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import com.revesoft.itelmobiledialer.video.encoding.Frame;

/**
 * A subclass of {@link VideoPacketizer} for packetizing H.264 encoded video.
 * 
 * @see <a href='http://www.ietf.org/rfc/rfc3984.txt'>RFC 3984</a> for RTP
 *      packetization scheme for H.264 video:
 *      <ul>
 *      <li>Section 5.3 for NAL unit header format
 *      <li>Section 5.4 for packetization modes
 *      <li>Section 5.8 for fragmentation units
 * 
 * @author Sufian Latif
 */
public class H264Packetizer extends VideoPacketizer {
	private final String TAG = "H264Packetizer";

	/**
	 * Set this to <code>true</code> to get Log messages from this class.
	 */
	private static final boolean DEBUG = false;

	/**
	 * A buffer to hold SPS value.
	 */
	private byte[] sps = null;

	/**
	 * A buffer to hold PPS value.
	 */
	private byte[] pps = null;

	/**
	 * Length of a NAL unit.
	 */
	private int naluLength;

	/**
	 * The starting timestamp of the packetizer.
	 */
	private long startTime;

	/**
	 * The maximum number of bytes to send in a packet.
	 */
	private int limit;

	/**
	 * Creates an {@link H264Packetizer}.
	 * 
	 * @param in
	 *            The {@link InputStream} containing video data
	 * @throws SocketException
	 *             If an error occurs while creating or binding the socket
	 */

	private final static int ParameterSetTransmissionCount = 3;

	private byte[] nalUnitDelimeter = { 0, 0, 0, 1 };

	public H264Packetizer(BlockingQueue<Frame> queue) throws SocketException 
	{
		super(queue);
		limit = maxSize - rtphl - 2;
	}

	public H264Packetizer(BlockingQueue<Frame> queue, byte[] sps, byte[] pps)
			throws SocketException {
		super(queue);
		limit = maxSize - rtphl - 2;
		this.sps = sps;
		this.pps = pps;
	}

	/**
	 * Sets the values of <code>sps</code> and <code>pps</code>. This method is
	 * to be called from the {@link Streamer} class to set the values.
	 * 
	 * @param _sps
	 *            Value of SPS
	 * @param _pps
	 *            Value of PPS
	 */
	public void setH264Params(byte[] _sps, byte[] _pps) {
		sps = _sps;
		pps = _pps;
	}

	/**
	 * Sends SPS and PPS values. This is called before sending each I-frame.
	 * 
	 * @throws IOException
	 *             If an error occurs while sending the packet
	 */
	private void sendSPSandPPS() throws IOException {
		if (sps != null && pps != null) {
			rtpSocket
			.setTStamp(90 * (SystemClock.elapsedRealtime() - startTime));
			System.arraycopy(sps, 0, buffer, rtphl, sps.length);
			rtpSocket.send(rtphl + sps.length);
			System.arraycopy(pps, 0, buffer, rtphl, pps.length);
			rtpSocket.send(rtphl + pps.length);
		}
	}

	/**
	 * Sends a whole NAL unit in a single packet. This is called when the length
	 * of a NAL unit is less than the maximum packet size.
	 * 
	 * @throws IOException
	 *             If an error occurs while sending the packet
	 */

	private void sendSingleNALU(byte[] nalu) throws IOException {

		System.arraycopy(nalu, 0, buffer, rtphl, naluLength);
		rtpSocket.markPacket();
		rtpSocket.setTStamp(90 * (SystemClock.elapsedRealtime() - startTime));
		rtpSocket.send(rtphl + naluLength);
	}

	/**
	 * Sends a large NAL unit fragmented in multiple packets. This is called
	 * when the length of the NAL unit is larger than maximum packet size.
	 * 
	 * @throws IOException
	 */
	private void sendFragmentedNALU(byte[] nalu) throws IOException {
		byte type = nalu[0];
		int size = naluLength - 1;
		int n = (size + limit - 1) / limit;
		rtpSocket.setTStamp(90 * (SystemClock.elapsedRealtime() - startTime));
		buffer[rtphl] = (byte) ((type & 0xe0) | 28);

		for (int i = 0; i < n; i++) {
			int tmp = (size - i * limit > limit) ? limit : size - i * limit;
			buffer[rtphl + 1] = (byte) (((i == 0 ? 1 : 0) << 7) | ((i == n - 1 ? 1 : 0) << 6) | (type & 0x1f));
			System.arraycopy(nalu, i * limit + 1, buffer, rtphl + 2, tmp); // old
			if (i == n - 1)
				rtpSocket.markPacket();
			rtpSocket.send(rtphl + 2 + tmp);
		}
	}

	/**
	 * <code>H264Packetizer</code>'s own scheme of packetizing video data.
	 * <p>
	 * Workflow of this method:
	 * <ol>
	 * 
	 * <li>Reads and skips the input data until it encounters the 4-byte
	 * sequence <code>"mdat"</code>
	 * <li>Starts packetization
	 * <li>While the thread is running
	 * <ul>
	 * <li>Reads a NAL unit
	 * <li>If it is an I-frame, sends SPS and PPS packets
	 * <li>If the NAL unit is small enough, sends it in one packet
	 * <li>Otherwise sends the fragmented NAL unit in multiple packets
	 */
	@Override
	public synchronized void run() 
	{
		try 
		{
			int cursor = rtphl + 2;
			startTime = SystemClock.elapsedRealtime();
			streaming = true;

			Frame currentFrame=null;
			while (streaming) 
			{
				currentFrame = queue.take();
				if(currentFrame.frameData==null)
					continue;
				naluLength = currentFrame.frameData.length;
				if (naluLength < 0)
					break;
				if ((currentFrame.frameData[0] & 0x1f) == 5)
					sendSPSandPPS();

				if ((naluLength) < limit)
					sendSingleNALU(currentFrame.frameData);
				else
					sendFragmentedNALU(currentFrame.frameData);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return;
		} 
	}

}

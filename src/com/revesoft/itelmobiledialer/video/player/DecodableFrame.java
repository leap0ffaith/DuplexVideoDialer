package com.revesoft.itelmobiledialer.video.player;

import com.revesoft.itelmobiledialer.video.encoding.NalUnit;

/**
 * a class for constructing a decodable frame that is decoded and subsequently
 * played by the player
 * 
 * @author Kazi Tasnif
 * 
 */
public class DecodableFrame {
	private byte[] buffer = null;
	private long presentationTime = -1;
	private long timeStamp = 0;

	public static final int H263P_FRAME = 0;
	public static final int H264_FRAME = 1;
	public static final int H263_FRAME = 2;

	private static final byte[] delimeter = { 0, 0, 0, 1 };
	private static final byte[] h263PFramedelimeter = { 0, 0 };
	private static final int MAX_FRAME_LENGTH = 65536;
	private int frameLength = 0;

	public DecodableFrame(byte[] buf, int flag) {
		if (flag == DecodableFrame.H263P_FRAME) {
			this.buffer = new byte[buf.length + h263PFramedelimeter.length];
			System.arraycopy(h263PFramedelimeter, 0, this.buffer, 0,
					h263PFramedelimeter.length);
			System.arraycopy(buf, 0, this.buffer, h263PFramedelimeter.length,
					buf.length);
			this.frameLength = buf.length + h263PFramedelimeter.length;
		} else if (flag == DecodableFrame.H263_FRAME) {
			this.buffer = new byte[buf.length];
			System.arraycopy(buf, 0, this.buffer, 0, buf.length);
			this.frameLength = buf.length;
		} else if (flag == DecodableFrame.H264_FRAME) {
			this.buffer = new byte[buf.length + delimeter.length];
			System.arraycopy(delimeter, 0, this.buffer, 0, delimeter.length);
			System.arraycopy(buf, 0, this.buffer, delimeter.length, buf.length);
			this.frameLength = buf.length + delimeter.length;
		}
	}

	public DecodableFrame(byte[] buf, int offset, int length, int flag) {
		if (flag == DecodableFrame.H263P_FRAME) {
			this.buffer = new byte[length + h263PFramedelimeter.length];
			System.arraycopy(h263PFramedelimeter, 0, this.buffer, 0,
					h263PFramedelimeter.length);
			System.arraycopy(buf, offset, this.buffer,
					h263PFramedelimeter.length, length);
			this.frameLength = length + h263PFramedelimeter.length;
		} else if (flag == DecodableFrame.H263_FRAME) {
			this.buffer = new byte[buf.length];
			System.arraycopy(buf, offset, this.buffer, 0, length);
			this.frameLength = length;
		} else if (flag == DecodableFrame.H264_FRAME) {
			this.buffer = new byte[length + delimeter.length];
			System.arraycopy(delimeter, 0, this.buffer, 0, delimeter.length);
			System.arraycopy(buf, offset, this.buffer, delimeter.length, length);
			this.frameLength = length + delimeter.length;
		}
	}

	/*
	 * construct a decodable frame from an array of nal units
	 */
	public DecodableFrame(NalUnit[] nUnitArray) {

		buffer = new byte[MAX_FRAME_LENGTH];
		int runningFrameLength = 0;
		// Log.d("DEBUG", "nal unit array length: " + nUnitArray.length);
		for (int i = 0; i < nUnitArray.length; i++) {
			if (nUnitArray[i] != null) {
				System.arraycopy(delimeter, 0, buffer, runningFrameLength,
						delimeter.length);
				runningFrameLength += delimeter.length;
				System.arraycopy(nUnitArray[i].getNalUnitData(), 0, buffer,
						runningFrameLength, nUnitArray[i].getLength());
				runningFrameLength += nUnitArray[i].getLength();
			}
		}

		frameLength = runningFrameLength;
	}

	public DecodableFrame(NalUnit[] nUnitArray, byte[] sps, byte[] pps) {

		buffer = new byte[MAX_FRAME_LENGTH];
		int runningFrameLength = 0;

		System.arraycopy(delimeter, 0, buffer, runningFrameLength,
				delimeter.length);
		runningFrameLength += delimeter.length;

		System.arraycopy(sps, 0, buffer, runningFrameLength, sps.length);
		runningFrameLength += sps.length;

		System.arraycopy(delimeter, 0, buffer, runningFrameLength,
				delimeter.length);
		runningFrameLength += delimeter.length;

		System.arraycopy(pps, 0, buffer, runningFrameLength, pps.length);
		runningFrameLength += pps.length;

		for (int i = 0; i < nUnitArray.length; i++) {
			if (nUnitArray[i] != null) {
				System.arraycopy(delimeter, 0, buffer, runningFrameLength,
						delimeter.length);
				runningFrameLength += delimeter.length;
				System.arraycopy(nUnitArray[i].getNalUnitData(), 0, buffer,
						runningFrameLength, nUnitArray[i].getLength());
				runningFrameLength += nUnitArray[i].getLength();
			}
		}

		frameLength = runningFrameLength;
	}

	public byte getHeader() {
		return buffer[0];
	}

	public int getLength() {
		return this.frameLength;
	}

	public byte[] getBuffer() {
		byte[] frameData = new byte[frameLength];
		System.arraycopy(buffer, 0, frameData, 0, frameLength);
		return frameData;
	}

	public void setPresentationTime(long tStamp) {
		this.presentationTime = 0;
	}

	public long getPresentationTime() {
		return this.presentationTime;
	}

	public long getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}

}

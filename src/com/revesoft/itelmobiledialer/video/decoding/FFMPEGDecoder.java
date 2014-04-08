package com.revesoft.itelmobiledialer.video.decoding;

public class FFMPEGDecoder {
	static {
		System.loadLibrary("ffmpeg");
		System.loadLibrary("ffmpeg_decoder");
	}
	
	private int codecID = 0;

	public FFMPEGDecoder(int codecID) {
		register();
		this.codecID = codecID;
	}

	public int configure(int resolution_id) {
		return initdecoder(codecID, resolution_id);
	}

	public int[] getDecodedData(int size, byte[] data) {
		int[] decodedData = decode(size, data);
		return decodedData;
	}

	public void release() {
		closedecoder();
	}

	private native void register();

	private native int initdecoder(int codec_id, int resolution_id);

	private native int[] decode(int size, byte[] data);

	private native void closedecoder();

	public native int getWidth();

	public native int getHeight();

}

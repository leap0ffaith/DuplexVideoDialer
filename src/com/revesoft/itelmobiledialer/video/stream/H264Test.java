package com.revesoft.itelmobiledialer.video.stream;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;

/**
 * A class for acquiring H.264 parameters (SPS and PPS) from a pre-recorded
 * sample.
 * 
 * @author Sufian Latif
 */
public class H264Test {
	private static final String TAG = "H264Test";

	public static final String SAMPLE_FILE_PATH = Environment
			.getExternalStorageDirectory() + "/sampleClip.mp4"; // "/Media/sampleClip.mp4";

	// private static final int quality = CamcorderProfile.QUALITY_480P;

	/**
	 * Set this to <code>true</code> to get Log messages from this class.
	 */
	private static final boolean DEBUG = false;

	/**
	 * The {@link android.media.MediaRecorder} to take the sample.
	 */
	private MediaRecorder recorder;
	private byte[] data, pps, sps;
	private int spsStart, spsLen, ppsStart, ppsLen;

	/**
	 * Initializes <code>recorder</code> to record the sample.
	 * 
	 * @param params
	 *            Parameters to be applied to <code>recorder</code>
	 * @param sh
	 *            Preview display for <code>recorder</code>
	 */
	public H264Test(RecordingParameters params, SurfaceHolder sh) {
		recorder = new MediaRecorder();
		params.apply(recorder);
		recorder.setPreviewDisplay(sh.getSurface());
	}

	/**
	 * Initializes <code>recorder</code> to record the sample.
	 * 
	 * @param params
	 *            Parameters to be applied to <code>recorder</code>
	 * 
	 */
	public H264Test(RecordingParameters params) {
		recorder = new MediaRecorder();
		params.apply(recorder);
	}

	/**
	 * Records the sample file, writes to SD card.
	 * 
	 * @throws IllegalStateException
	 *             If {@link android.media.MediaRecorder} methods are called out
	 *             of proper sequence
	 * @throws IOException
	 *             If <code>prepare()</code> fails
	 * @throws InterruptedException
	 *             if <code>interrupt()</code> was called for this thread while
	 *             it was sleeping
	 */
	public void takeSample() throws IllegalStateException, IOException,
			InterruptedException {
		// String name = "/sdcard/test.3gp";
		int duration = 1000;
		// recorder.setProfile(CamcorderProfile.get(quality));
		recorder.setOutputFile(H264Test.SAMPLE_FILE_PATH);
		recorder.setMaxDuration(duration);
		recorder.prepare();
		recorder.start();
		Thread.sleep(duration + 100);
		recorder.stop();
		recorder.reset();
		recorder.release();
	}

	/**
	 * Parses the recorded file and extracts SPS and PPS.
	 * 
	 * @throws IOException
	 *             If file cannot be found or cannot be read from
	 */
	public void parse() throws IOException {
		File fin = new File(H264Test.SAMPLE_FILE_PATH);
		FileInputStream fis = new FileInputStream(fin);
		data = new byte[(int) fin.length()];
		fis.read(data, 0, (int) fin.length());
		fin.delete();

		int pos = findavcC();
		int start = pos + 8;
		spsStart = start + 8;
		spsLen = (data[spsStart - 2] << 8) + data[spsStart - 1];
		ppsStart = spsStart + spsLen + 3;
		ppsLen = (data[ppsStart - 2] << 8) + data[ppsStart - 1];
		sps = new byte[spsLen];
		pps = new byte[ppsLen];
		if (DEBUG)
			Log.i(TAG, "spslen = " + spsLen + ", ppslen = " + ppsLen);

		/*
		 * Log.d("DEBUG", "sps length: " + spsLen); Log.d("DEBUG",
		 * "pps length: " + ppsLen);
		 */

		System.arraycopy(data, spsStart, sps, 0, spsLen);
		System.arraycopy(data, ppsStart, pps, 0, ppsLen);

		/*
		 * for(int i = 0; i < spsLen; i++){ Log.d("DEBUG", "sps byte: " + i +
		 * " " + sps[i]); } for(int i = 0; i < ppsLen; i++){ Log.d("DEBUG",
		 * "pps byte: " + i + " " + pps[i]); }
		 */
		try {
			fis.close();
		} catch (IOException io) {
			Log.d("DEBUG", "error while closing");
		}
	}

	/**
	 * Finds the avcC block in the 3gp file header.
	 * 
	 * @return Position of the avcC block, or <code>-1</code> if not found
	 */
	int findavcC() {
		int i;

		for (i = 0; i < data.length - 4; i++) {
			if (data[i] == 'a' && data[i + 1] == 'v' && data[i + 2] == 'c'
					&& data[i + 3] == 'C')
				return i - 4;
		}
		return -1;
	}

	public byte[] getPPS() {
		return pps;
	}

	public byte[] getSPS() {
		return sps;
	}

//	public MediaFormat getDecoderFormat() {
//		Log.d("DEBUG", "in getDecoderFormat");
//		MediaExtractor extractor = new MediaExtractor();
//		extractor.setDataSource(H264Test.SAMPLE_FILE_PATH);
//		MediaFormat format = null;
//
//		Log.d("DEBUG", "track count: " + extractor.getTrackCount());
//
//		for (int i = 0; i < extractor.getTrackCount(); i++) {
//			format = extractor.getTrackFormat(i);
//			Log.d("DecodeActivity", "Media Format: " + format);
//
//			String mime = format.getString(MediaFormat.KEY_MIME);
//			if (mime.startsWith("video/")) {
//				Log.d("DEBUG", "video format found");
//				Log.d("DEBUG", "media format found in getDecoderFormat: "
//						+ format);
//				break;
//			}
//		}
//
//		File fin = new File(H264Test.SAMPLE_FILE_PATH);
//		fin.delete();
//		extractor.release();
//		return format;
//	}
}

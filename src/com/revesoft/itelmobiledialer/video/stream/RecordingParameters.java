package com.revesoft.itelmobiledialer.video.stream;

import java.io.Serializable;

import android.media.MediaRecorder;
import android.media.MediaRecorder.OutputFormat;
import android.media.MediaRecorder.VideoEncoder;
import android.media.MediaRecorder.VideoSource;
import android.util.Log;

/**
 * A class specifying the parameters of recording video.
 * 
 * @author Sufian Latif
 * @author Kazi Tasnif
 * 
 */
public class RecordingParameters implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final String TAG = "RecordingParameters";

	/**
	 * Set this to <code>true</code> to get Log messages from this class.
	 */
	private static final boolean DEBUG = true;

	private int source = VideoSource.CAMERA;

	private int format = OutputFormat.MPEG_4;
	private int fps = 15;
	private int width = 320, height = 240;
	private int kbps = 128;
	private int encoder = VideoEncoder.H264;

	public int getEncoder() {
		return encoder;
	}

	public void setEncoder(int enc) {
		encoder = enc;
	}

	public void setBitRate(int rate) {
		kbps = rate;
	}

	public void setFrameRate(int rate) {
		fps = rate;
	}

	public void setResolution(int w, int h) {
		width = w;
		height = h;
	}

	public int getFrameRate() {
		return fps;
	}

	public String toString() {
		return "Encoder: " + encoder + ", Framerate: " + fps + ", Bitrate: "
				+ kbps + ", Resolution: " + width + "x" + height;
	}

	/**
	 * Applies the settings on {@link android.media.MediaRecorder} in correct
	 * order.
	 * 
	 * @param rec
	 *            The {@link android.media.MediaRecorder} object to apply the
	 *            settings on
	 */
	public void apply(MediaRecorder rec) {
		if (DEBUG)
			Log.i(TAG, "applying...");
		// rec.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
		rec.setVideoSource(source);
		if (encoder == VideoEncoder.H263) {
			format = OutputFormat.THREE_GPP;
		} else if (encoder == VideoEncoder.H264) {
			format = OutputFormat.MPEG_4;
		}
		rec.setOutputFormat(format);
		rec.setVideoEncoder(encoder);
		rec.setVideoSize(width, height);
		rec.setVideoFrameRate(fps);
		rec.setVideoEncodingBitRate(kbps * 1024);
		rec.setOrientationHint(90);
	}

	@Override
	public boolean equals(Object o) {
		// TODO Auto-generated method stub
		if (o == this) {
			return true;
		}
		if (!(o instanceof RecordingParameters)) {
			return false;
		}
		RecordingParameters other = (RecordingParameters) o;
		/*
		 * Log.d("DEBUG", "encoder that: " + other.encoder + "encoder this: " +
		 * encoder ); Log.d("DEBUG", "format that: " + other.format +
		 * "encoder this: " + format ); Log.d("DEBUG", "fps that: " + other.fps
		 * + "encoder this: " + format ); Log.d("DEBUG", "height that: " +
		 * other.height + "encoder this: " + height ); Log.d("DEBUG",
		 * "kbps that: " + other.kbps + "encoder this: " + kbps );
		 * Log.d("DEBUG", "source that: " + other.source + "encoder this: " +
		 * source ); Log.d("DEBUG", "width that: " + other.width +
		 * "encoder this: " + width );
		 */
		return other.encoder == encoder && other.format == format
				&& other.fps == fps && other.height == height
				&& other.kbps == kbps && other.source == source
				&& other.width == width;

	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub

		/*
		 * Refer to Effective Java by Joshua Block:(Item 9)
		 */
		int result = 17;

		result = 31 * result + encoder;
		result = 31 * result + format;
		result = 31 * result + fps;
		result = 31 * result + height;
		result = 31 * result + kbps;
		result = 31 * result + source;
		result = 31 * result + width;

		return result;
	}
}

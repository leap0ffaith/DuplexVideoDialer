package com.revesoft.itelmobiledialer.video.player;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Comparator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.revesoft.itelmobiledialer.video.decoding.CodecParameters;
import com.revesoft.itelmobiledialer.video.encoding.H264ParameterSets;

/**
 * a general interface for playing video
 * 
 * @author Kazi Tasnif
 */
public abstract class VideoPlayer implements Runnable {

	public enum RecorderState {
		UNINITIALIZED, PREPARED, STARTED, STOPPED, RESET
	};

	protected volatile AtomicReference<RecorderState> decoderState;

	protected int display_height, display_width;

	private int resID = -1;
	// private MediaCodec decoder = null;
	protected H264ParameterSets paramSets = null;
	protected int width = 0;
	protected int height = 0;
	/*
	 * type of the media data i.e "video/avc", "video/h263"
	 */
	protected int codecID = 0;

	static public boolean isPlaying = false;

	protected Thread player = null;

	/*
	 * the wrapped surface object the player expects
	 */
	protected RenderingSurface surface = null;

	/*
	 * a queue to hold the frames to be decoded and rendered frames are dequeued
	 * from this queue, decoded and rendered
	 */

	protected BlockingQueue<DecodableFrame> playerQueue = null;

	/*
	 * a 4 byte delimeter that has to be put before the sps and pps array for
	 * decoder configuration
	 */
	protected static final byte[] delimeter = { 0, 0, 0, 1 };

	//public FileOutputStream fos1 = null;

	public VideoPlayer(int codecID, int res) {
		decoderState = new AtomicReference<VideoPlayer.RecorderState>();
		this.codecID = codecID;
		playerQueue = new PriorityBlockingQueue<DecodableFrame>(10,
				new Comparator<DecodableFrame>() {
					@Override
					public int compare(DecodableFrame lhs, DecodableFrame rhs) {
						return (int)(lhs.getTimeStamp() - rhs.getTimeStamp());
					}
				});
		player = new Thread(this);
		decoderState.set(RecorderState.UNINITIALIZED);
		this.resID = res;

		if (this.resID == CodecParameters.RES_128x96_Sub_QCIF) {
			this.width = 128;
			this.height = 96;
		} else if (this.resID == CodecParameters.RES_176x144_QCIF) {
			this.width = 176;
			this.height = 144;
		} else if (this.resID == CodecParameters.RES_352x288_CIF) {
			this.width = 352;
			this.height = 288;
		} else if (this.resID == CodecParameters.RES_704x576_4CIF) {
			this.width = 704;
			this.height = 576;
		} else if (this.resID == CodecParameters.RES_1408x1152_16CIF) {
			this.width = 1408;
			this.height = 1152;
		}

		// for writing whole frame in a file
		/*File encodedFramesAfterFile = new File(
				Environment.getExternalStorageDirectory(),
				"afterFragmentationAndMerge.264");
		try {
			fos1 = new FileOutputStream(encodedFramesAfterFile, false);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}*/
	}

	/*
	 * set the height and width of the content
	 */
	public void setResolution(int width, int height) {
		this.width = width;
		this.height = height;
	}

	/*
	 * set the parameter set values for configuring the decoder as a player
	 * which can play h264 encoded frames
	 */
	public void setH264ParameterSet(H264ParameterSets paramSets) {
		this.paramSets = paramSets;
	}

	/*
	 * provides the decoder with a surface to render the outputted data
	 */
	@SuppressLint("NewApi")
	public void setRenderingSurface(RenderingSurface surface) {
		this.surface = surface;

		Point size = new Point();
		WindowManager w1 = (WindowManager) surface.getContext()
				.getSystemService(Context.WINDOW_SERVICE);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			w1.getDefaultDisplay().getSize(size);

			display_width = size.x;
			display_height = size.y;
		} else {
			Display d = w1.getDefaultDisplay();
			display_width = d.getWidth();
			display_height = d.getHeight();
		}
	}

	/*
	 * the client should call this function to submit video frame data. The
	 * player expects a the frame to be delimited by 0x00 0x00 0x00 0x01
	 */
	public void submitFrame(DecodableFrame frame) {
		
		playerQueue.offer(frame);
	}

	public void prepare() {
		decoderState.set(RecorderState.PREPARED);
	}

	public void start() {
		Log.d("Decoderlog","VideoPlayer.start() function called");
		isPlaying = true;
		player.start();
		decoderState.set(RecorderState.STARTED);
	}

	public void stop() {
		isPlaying = false;
		synchronized (playerQueue) 
		{
			playerQueue.notifyAll();	
		}
		
		//player.interrupt();
		decoderState.set(RecorderState.STOPPED);
	}

	public void release() {
		decoderState.set(RecorderState.UNINITIALIZED);
	}

	public void pause() {
		// TODO Auto-generated method stub
	}

	public void resume() {
		// TODO Auto-generated method stub
	}

}

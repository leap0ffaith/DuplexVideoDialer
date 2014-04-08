package com.revesoft.itelmobiledialer.video.stream;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicReference;

import android.media.MediaRecorder;
import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.util.Log;

/**
 * An extended customized {@link android.media.MediaRecorder} that can write the
 * captured data directly to a socket.
 * 
 * @see MediaRecorder
 * 
 * @author Sufian Latif
 */
public class StreamingRecorder extends MediaRecorder {

	public enum RecorderState {
		UNINITIALIZED, PREPARED, STARTED, STOPPED, RESET
	};

	private volatile AtomicReference<RecorderState> recorderState;

	private final String TAG = "StreamingRecorder";

	// private final MediaRecorder recorder;
	/**
	 * Set this to <code>true</code> to get Log messages from this class.
	 * 
	 * 
	 */
	private static final boolean DEBUG = true;

	/**
	 * A {@link android.net.LocalServerSocket} to connect two
	 * {@link android.net.LocalSocket}s.
	 */
	private LocalServerSocket ss;

	/**
	 * A {@link android.net.LocalSocket} to receive recorded data.
	 */
	private LocalSocket sender;

	/**
	 * A {@link android.net.LocalSocket} to connect and capture data from
	 * <code>receiver</code>.
	 */
	private LocalSocket receiver;

	private static int soc_no = 1;

	/**
	 * Creates a <code>Recorder</code> object.
	 */
	public StreamingRecorder() {
		// recorder = new MediaRecorder();
		super();
		recorderState = new AtomicReference<StreamingRecorder.RecorderState>();
		setOnErrorListener(new MediaRecorder.OnErrorListener() {

			@Override
			public void onError(MediaRecorder mr, int what, int extra) {
				if (DEBUG)
					Log.i(TAG, "Error! what:" + what + " extra:" + extra);
			}
		});

		recorderState.set(RecorderState.UNINITIALIZED);
	}

	/**
	 * Overridden method of {@link android.media.MediaRecorder}
	 * <code>.prepare()</code>.
	 * <p>
	 * Sets
	 * <code>sender</coder>'s file descriptor as the output file of the underlying {@link android.media.MediaRecorder}.
	 */

	public void prepare() {
		if (createSockets()) {
			try {
				setOutputFile(sender.getFileDescriptor());
				super.prepare();
				recorderState.set(RecorderState.PREPARED);
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				if (DEBUG)
					Log.e(TAG, e.getMessage());
				recorderState.set(RecorderState.UNINITIALIZED);
			} catch (IOException e) {
				e.printStackTrace();
				recorderState.set(RecorderState.UNINITIALIZED);
			}
		}
	}

	/**
	 * Returns the {@link InputStream} of the {@link android.net.LocalSocket}
	 * <code>receiver</code>. It contains the camera output data.
	 * 
	 * @return The {@link InputStream} of <code>receiver</code>
	 */
	public InputStream getInputStream() {
		try {
			return receiver.getInputStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			if (DEBUG)
				Log.e(TAG, e.getMessage());
			return null;
		}
	}

	private boolean createSockets() {
		boolean socketsCreated = false;
		try {
			String socket = "LocalServerSocket" + soc_no;
			ss = new LocalServerSocket(socket);
			sender = new LocalSocket();
			// sender.setReceiveBufferSize(5000);
			// sender.setSendBufferSize(5000);
			sender.connect(new LocalSocketAddress(socket));
			receiver = ss.accept();

			// receiver.setReceiveBufferSize(5000);
			// receiver.setSendBufferSize(5000);
			socketsCreated = true;
			soc_no++;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e(TAG, "LocalSocket not created...");
		}
		return socketsCreated;
	}

	public void start() {
		try {
			super.start();
			recorderState.set(RecorderState.STARTED);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void reset() {
		try {
			super.reset();
			closeSockets();
			recorderState.set(RecorderState.RESET);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void stopRecording() {
		try {
			super.stop();
			recorderState.set(RecorderState.STOPPED);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void closeSockets() {
		try {
			ss.close();
			receiver.close();
			sender.close();
			ss = null;
			receiver = null;
			sender = null;
		} catch (Exception e) {
			Log.e(TAG, "error while closing sockets");
		}
	}

	public RecorderState getState() {
		return recorderState.get();
	}
}

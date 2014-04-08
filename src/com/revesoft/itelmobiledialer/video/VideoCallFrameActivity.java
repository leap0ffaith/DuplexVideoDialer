package com.revesoft.itelmobiledialer.video;

import static android.hardware.Sensor.TYPE_PROXIMITY;
import static android.hardware.SensorManager.SENSOR_DELAY_NORMAL;
import static android.os.PowerManager.ACQUIRE_CAUSES_WAKEUP;
import static android.os.PowerManager.SCREEN_DIM_WAKE_LOCK;

import java.net.SocketException;

import revesoft.videodialer.R;
import android.app.Activity;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.revesoft.itelmobiledialer.customview.MySurfaceView;
import com.revesoft.itelmobiledialer.customview.TouchListener;
import com.revesoft.itelmobiledialer.signaling.SIPProvider;
import com.revesoft.itelmobiledialer.util.ApplicationState;
import com.revesoft.itelmobiledialer.util.Constants;
import com.revesoft.itelmobiledialer.util.DTMFTones;
import com.revesoft.itelmobiledialer.video.decoding.CodecParameters;
import com.revesoft.itelmobiledialer.video.encoding.Encoder;
import com.revesoft.itelmobiledialer.video.encoding.H263Encoder;
import com.revesoft.itelmobiledialer.video.encoding.H264Encoder;
import com.revesoft.itelmobiledialer.video.encoding.H264ParameterSets;
import com.revesoft.itelmobiledialer.video.player.FFMPEGBasedVideoPlayer;
import com.revesoft.itelmobiledialer.video.player.MediaCodecBasedVideoPlayer;
import com.revesoft.itelmobiledialer.video.player.RenderingSurface;
import com.revesoft.itelmobiledialer.video.player.VideoPlayer;
import com.revesoft.itelmobiledialer.video.receiver.H263PlusVideoReceiver;
import com.revesoft.itelmobiledialer.video.receiver.H263VideoReceiver;
import com.revesoft.itelmobiledialer.video.receiver.H264VideoReceiver;
import com.revesoft.itelmobiledialer.video.receiver.RtpReceiver;
import com.revesoft.itelmobiledialer.video.stream.RecordingParameters;
import com.revesoft.itelmobiledialer.video.utility.ParameterSetStorer;

public class VideoCallFrameActivity extends Activity implements
		SensorEventListener {
	private static String TAG = "VideoCallFrameActivity";
	private static final boolean DEBUG = true;

	private MySurfaceView preview;
	private SurfaceView playerSurface;
	// private Streamer streamer;
	private RtpReceiver receiver;
	private VideoPlayer player = null;

	// public static final int BACK_CAMERA = 0;
	// public static final int FRONT_CAMERA = 1;

	public static int CURRENT_CAMERA = Camera.CameraInfo.CAMERA_FACING_FRONT;
	// public static int CURRENT_CAMERA = Camera.CameraInfo.CAMERA_FACING_BACK;

	// private static Camera mCamera = null;

	private LinearLayout view = null;

	private RenderingSurface renderSurface = null;
	private Handler handler = null;

	private LinearLayout endCallButtonSpace;
	private LinearLayout acceptDeclineButtonSpace;

	WakeLock screenLock, proximityLock;
	private SensorManager sensorManager;
	private Sensor proximitySensor;

	private H264ParameterSets h264params = null;
	private Encoder encoder = null;
	float scale;
	DTMFTones tones;

	/*public static RecordingParameters loadParams() {
		RecordingParameters rp = new RecordingParameters();
		if (SIPProvider.videoCodecType == CodecParameters.CODEC_ID_H264) {
			Log.d(TAG, "setting h264 encoding");
			rp.setEncoder(MediaRecorder.VideoEncoder.H264);
		} else {
			Log.d(TAG, "setting h263 encoding");
			rp.setEncoder(MediaRecorder.VideoEncoder.H263);
		}
		rp.setResolution(352, 288);
		rp.setFrameRate(15);
		rp.setBitRate(128);
		if (DEBUG)
			Log.i(TAG + ": RecordingParameters", "params loaded: " + rp);
		return rp;
	}*/

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		SIPProvider.initSocket();
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.video_main);
		handler = new Handler();
		/*
		 * CameraProfileChecker profileChecker = new CameraProfileChecker();
		 * 
		 * profileChecker.checkProfile();
		 */
		scale = this.getResources().getDisplayMetrics().density;
		playerSurface = (SurfaceView) findViewById(R.id.videoPlayer);
		preview = (MySurfaceView) findViewById(R.id.cameraPreview);
		
		TouchListener mListener=new TouchListener(preview);
		preview.setOnTouchListener(mListener);

		/*RelativeLayout rl = (RelativeLayout) findViewById(R.id.main_relative_layout); 
		FrameLayout frame = new FrameLayout(this); 
		addContentView(frame, new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
		SurfaceView playerSurface = new SurfaceView(this);
		frame.addView(playerSurface);
		MySurfaceView preview = new MySurfaceView(this,this.CURRENT_CAMERA);
		addContentView(preview, new LayoutParams(320, 240));
		preview.setVisibility(View.VISIBLE); preview.bringToFront();*/
		

		final ParameterSetStorer storer = new ParameterSetStorer(this);

		// linear layout for options

		view = (LinearLayout) findViewById(R.id.view1);
		//final RecordingParameters params = loadParams();

		playerSurface.getHolder().addCallback(new SurfaceHolder.Callback() {

			@Override
			public void surfaceDestroyed(SurfaceHolder holder) {
				Toast.makeText(getApplicationContext(), "lpayer srface destroy called", Toast.LENGTH_LONG).show();
				if (receiveVideo) {
					renderSurface.markAsInactive();
					player.stop();
					player.release();
					receiver.stopReceiving();
				}
			}

			@Override
			public void surfaceCreated(SurfaceHolder holder) {
				Log.d(TAG, "video surface created");
				if (receiveVideo)
					startPlaying();
			}

			@Override
			public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) 
			{
				
				preview.setZOrderOnTop(true);
			}
		});

		endCallButtonSpace = (LinearLayout) findViewById(R.id.endcall_button_space);
		acceptDeclineButtonSpace = (LinearLayout) findViewById(R.id.accept_decline_button_space);
		acceptDeclineButtonSpace.setVisibility(View.VISIBLE);
		endCallButtonSpace.setVisibility(View.GONE);

		LocalBroadcastManager.getInstance(this).registerReceiver(
				mMessageReceiver,
				new IntentFilter(Constants.INTENT_FROM_DIALER));

		String type = this.getIntent().getStringExtra(
				Constants.BROADCAST_MESSAGE_FROM_DIALER);
		if (type == null)
			;
		else if (type.compareTo(Constants.TYPE_INCOMING) == 0) {
			incomingGUISetup();
		} else if (type.compareTo(Constants.TYPE_OUTGOING) == 0) {
			outgoingGUISetup();
		}
		String number = this.getIntent().getStringExtra(
				Constants.BROADCAST_MESSAGE_NUMBER);

		boolean b = this.getIntent().getBooleanExtra("fromDialogue", false);
		if (b) {
			outgoingGUISetup();
		}

		PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
		try {
			screenLock = powerManager.newWakeLock(SCREEN_DIM_WAKE_LOCK
					| ACQUIRE_CAUSES_WAKEUP, "Video");
		} catch (Exception e) {
			if (DEBUG)
				Log.e("Video", "ScreenLock: ", e);
		}

		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		proximitySensor = sensorManager.getDefaultSensor(TYPE_PROXIMITY);

		acquireLocksInCall();
	}

	private void startPlaying() {
		renderSurface = new RenderingSurface(playerSurface.getHolder().getSurface(), VideoCallFrameActivity.this);
		try {
			if (SIPProvider.videoCodecType == CodecParameters.CODEC_ID_H264) {
				Log.d(TAG, "H.264");
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
					player = new MediaCodecBasedVideoPlayer(
							// player = new FFMPEGBasedVideoPlayer(
							CodecParameters.CODEC_ID_H264,
							CodecParameters.RES_352x288_CIF);
				} else {
					player = new FFMPEGBasedVideoPlayer(
							CodecParameters.CODEC_ID_H264,
							CodecParameters.RES_352x288_CIF);
				}
				player.setRenderingSurface(renderSurface);
				receiver = new H264VideoReceiver(player);

				// if (SIPProvider.pack_mode == SIPProvider.PACK_MODE_ZERO) {
				// } else {
				// receiver = new H264VideoReceiverMode1(player);
				// }
			} else {
				Log.d(TAG, "H.263");
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
					player = new MediaCodecBasedVideoPlayer(
							CodecParameters.CODEC_ID_H263,
							CodecParameters.RES_352x288_CIF);
				} else {
					player = new FFMPEGBasedVideoPlayer(
							CodecParameters.CODEC_ID_H263,
							CodecParameters.RES_352x288_CIF);
				}
				player.setResolution(SIPProvider.videoWidth,
						SIPProvider.videoHeight);
				player.setRenderingSurface(renderSurface);

				if (SIPProvider.videoCodecType == CodecParameters.CODEC_ID_H263)
					receiver = new H263VideoReceiver(player);
				else if (SIPProvider.videoCodecType == CodecParameters.CODEC_ID_H263_1998)
					receiver = new H263PlusVideoReceiver(player);
			}
			receiver.start();

		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// Get extra data included in the Intent

			String message = intent
					.getStringExtra(Constants.BROADCAST_MESSAGE_DISPLAY_DURATION);
			if (message != null) {
				return;
			}

			message = intent
					.getStringExtra(Constants.BROADCAST_MESSAGE_START_VIDEO_RECEIVING);
			if (message != null) {
				if (!receiveVideo) {
					// TODO fd
					// streamer.startStreaming();
					try {
						if (SIPProvider.videoCodecType == CodecParameters.CODEC_ID_H264) {
							encoder = new H264Encoder(SIPProvider.videoWidth, SIPProvider.videoHeight);
							preview.setEncoder(encoder);
						} else if (SIPProvider.videoCodecType == CodecParameters.CODEC_ID_H263_1998){
							encoder = new H263Encoder(SIPProvider.videoWidth, SIPProvider.videoHeight);
							preview.setEncoder(encoder);
						}
						encoder.setStartMS(System.currentTimeMillis());
						preview.startEncoding(SIPProvider.videoWidth,
								SIPProvider.videoHeight);
						encoder.startSending();
						receiveVideo = true;
						ViewGroup.LayoutParams lp = preview.getLayoutParams();
					    lp.width = (int) (176 * scale + 0.5f);
					    lp.height = (int) (144 * scale + 0.5f);
					    preview.setLayoutParams(lp);
						startPlaying();
					} catch (SocketException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

			message = intent
					.getStringExtra(Constants.BROADCAST_MESSAGE_DISPLAY_STATUS);
			if (message != null) {
				if (message.equalsIgnoreCase("Connected"))
					outgoingGUISetup();
				else if (message == "Call End")
					endActivity();
				return;
			}

			message = intent.getStringExtra(Constants.BROADCAST_MESSAGE_FINISH);
			if (message != null) {
				endActivity();
			}
		}
	};

	private void endActivity() {
		handler.post(new FinishRunnable());
	}

	private void sendMessage(String type, String message) {
		Intent intent = new Intent(Constants.DIALER_INTENT_FILTER);
		intent.putExtra(type, message);
		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.endcall_button:
			endCall();
			// Toast.makeText(CallFrameGUIActivity.this, "End call",
			// Toast.LENGTH_SHORT).show();
			break;
		case R.id.accept_button:
			accept();
			// Toast.makeText(CallFrameGUIActivity.this, "Accept",
			// Toast.LENGTH_SHORT).show();
			break;
		case R.id.decline_button:
			reject();
			// Toast.makeText(CallFrameGUIActivity.this, "Decline",
			// Toast.LENGTH_SHORT).show();
			break;
		case R.id.cameraSwitchButton:
			Toast.makeText(getApplicationContext(),
					"camera switch button pressed", Toast.LENGTH_SHORT).show();
			preview.switchCam(SIPProvider.videoWidth, SIPProvider.videoHeight);

			// reject();
			// Toast.makeText(CallFrameGUIActivity.this, "End call",
			// Toast.LENGTH_SHORT).show();
			break;
		}
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		char numberTyped = KeyCharacterMap.load(event.getDeviceId()).getNumber(
				keyCode);
		if ((numberTyped >= '0' && numberTyped <= '9') || numberTyped == '#'
				|| numberTyped == '*') {

		} else if (keyCode == KeyEvent.KEYCODE_DEL) {

		} else if (keyCode == KeyEvent.KEYCODE_CALL) {
			accept();
		} else if (keyCode == KeyEvent.KEYCODE_ENDCALL) {
			reject();
		} else
			return false;
		return true;
	}

	private void accept() {
		sendMessage(Constants.BROADCAST_MESSAGE_FROM_CALL,
				Constants.BROADCAST_MESSAGE_ACCEPT);

		// this part has to be removed while integrating.
		Intent intent = new Intent(Constants.INTENT_FROM_DIALER);
		intent.putExtra(Constants.BROADCAST_MESSAGE_START_VIDEO_RECEIVING,
				Constants.BROADCAST_MESSAGE_ACCEPT);
		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
		outgoingGUISetup();
	}

	private void reject() {
		sendMessage(Constants.BROADCAST_MESSAGE_FROM_CALL,
				Constants.BROADCAST_MESSAGE_REJECT);
		endActivity();
	}
	private void endCall()
	{
		sendMessage(Constants.BROADCAST_MESSAGE_FROM_CALL,
				Constants.BROADCAST_MESSAGE_REJECT);
		endActivity();
		if(SIPProvider.videoSocket != null)
		{
			//SIPProvider.videoSocket.close();
			receiver.stopReceiving();
		}
	}
	private boolean receiveVideo = false;

	private void incomingGUISetup() {
		findViewById(R.id.endcall_button_space).setVisibility(View.INVISIBLE);
		findViewById(R.id.accept_decline_button_space).setVisibility(
				View.VISIBLE);
		receiveVideo = true;
		// findViewById(R.id.activitybutton).setVisibility(View.INVISIBLE);
	}

	private void outgoingGUISetup() {
		findViewById(R.id.accept_decline_button_space).setVisibility(
				View.INVISIBLE);
		findViewById(R.id.endcall_button_space).setVisibility(View.VISIBLE);
		// findViewById(R.id.activitybutton).setVisibility(View.INVISIBLE);
	}

	private volatile boolean finishCalled = false;

	private class FinishRunnable implements Runnable {
		public void run() {
			if (!finishCalled) {
				finishCalled = true;
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				finish();
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		ApplicationState.activityResumed();
	}

	@Override
	protected void onPause() {
		Log.d(TAG, "on pause method callback");
		ApplicationState.activityPaused();

		super.onPause();
	}

	@Override
	protected void onDestroy() {

		LocalBroadcastManager.getInstance(this).unregisterReceiver(
				mMessageReceiver);

		relaeaseLocksInCall();

		// streamer.stopStreaming();
		// streamer.stopRecording();
		// player.stop();
		// player.release();
		// receiver.stop();

		KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
		KeyguardLock keyguard = km.newKeyguardLock("MyApp");
		keyguard.reenableKeyguard();
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if(SIPProvider.videoSocket != null)
		{
			SIPProvider.videoSocket.close();
			SIPProvider.videoSocket = null;
		}
		player.isPlaying = false;
		Log.i("DuplexVideo", "Stopping DuplexVideoActivity");
		super.onDestroy();
	}

	/********************************************/
	/***** switch camera *****/

	// *camera switching*/
	/*
	 * public void switchCamera() { if (CURRENT_CAMERA ==
	 * Camera.CameraInfo.CAMERA_FACING_BACK) { Log.d(TAG,
	 * "trying to open front camera"); if
	 * (safeCameraOpen(Camera.CameraInfo.CAMERA_FACING_FRONT)) { CURRENT_CAMERA
	 * = Camera.CameraInfo.CAMERA_FACING_FRONT; } } else { Log.d(TAG,
	 * "trying to open back camera"); if
	 * (safeCameraOpen(Camera.CameraInfo.CAMERA_FACING_BACK)) { CURRENT_CAMERA =
	 * Camera.CameraInfo.CAMERA_FACING_BACK; } } streamer.switchCamera(mCamera);
	 * }
	 */

	public void acquireLocksInCall() {
		sensorManager.registerListener(this, proximitySensor,
				SENSOR_DELAY_NORMAL, handler);
		if (screenLock != null)
			screenLock.acquire();

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		PowerManager powerManager = (PowerManager) this
				.getSystemService(Context.POWER_SERVICE);
		long l = SystemClock.uptimeMillis();
		powerManager.userActivity(l, true);
	}

	public void relaeaseLocksInCall() {
		sensorManager.unregisterListener(this);
		if (screenLock != null && screenLock.isHeld())
			screenLock.release();
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {

	}

	public void onSensorChanged(SensorEvent event) {
		final Window window = getWindow();
		WindowManager.LayoutParams lAttrs = getWindow().getAttributes();
		View view = ((ViewGroup) window.getDecorView().findViewById(
				android.R.id.content)).getChildAt(0);
		if (event.values[0] > 0.0) {
			lAttrs.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
			view.setVisibility(View.VISIBLE);
		} else {
			lAttrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
			view.setVisibility(View.INVISIBLE);
		}
		window.setAttributes(lAttrs);
	}

}

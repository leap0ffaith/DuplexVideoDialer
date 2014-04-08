package com.revesoft.itelmobiledialer.video.player;

import java.nio.ByteBuffer;
import java.util.NoSuchElementException;
import com.revesoft.itelmobiledialer.video.decoding.CodecParameters;
import android.annotation.SuppressLint;
import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaFormat;
import android.os.SystemClock;
import android.util.Log;

/**
 * A player based on the Android MediaCodec API It has-a queue of
 * DecodableFrames which is subsequently dequeued and played.
 * 
 * @author Kazi Tasnif 
 * @edited Dhiman paul
 */

@SuppressLint("NewApi")
public class MediaCodecBasedVideoPlayer extends VideoPlayer {

	private MediaCodec decoder = null;
	private String type="";
	/*
	 * inputBuffers associated with the codec that gets transferred to the
	 * application
	 */
	protected ByteBuffer[] inputBuffers = null;
	public MediaCodecBasedVideoPlayer(int codecID, int resID) {
		super(codecID, resID);
		if(codecID == CodecParameters.CODEC_ID_H263 || codecID == CodecParameters.CODEC_ID_H263_1998)
			type = "video/3gpp";
		else if(codecID == CodecParameters.CODEC_ID_H264)
			type = "video/avc";
		decoder = MediaCodec.createDecoderByType(type);
	}

	@Override
	public void prepare() {
		// TODO Auto-generated method stub
		MediaFormat format = MediaFormat.createVideoFormat(type, 240, 320);
		if (type == "video/avc") {
			/*
			 * extra 4 bytes for the delimeter
			 */
			byte[] sps = new byte[paramSets.getSpsLength() + 4];
			byte[] pps = new byte[paramSets.getPpsLength() + 4];

			System.arraycopy(delimeter, 0, sps, 0, 4);
			System.arraycopy(paramSets.getSpsValue(), 0, sps, 4,
					paramSets.getSpsLength());

			System.arraycopy(delimeter, 0, pps, 0, 4);
			System.arraycopy(paramSets.getPpsValue(), 0, pps, 4,
					paramSets.getPpsLength());

			ByteBuffer spsBuffer = ByteBuffer.wrap(sps);

			ByteBuffer ppsBuffer = ByteBuffer.wrap(pps);

			/*
			 * submit the spsBuffer in the mediaFormat object with entry csd-0
			 */
			format.setByteBuffer("csd-0", spsBuffer);

			/*
			 * submit the ppsBuffer in the mediaFormat object with entry csd-1
			 */
 
			format.setByteBuffer("csd-1", ppsBuffer);
		}
		/*
		 * configure the decoder
		 */
		try {
			decoder.configure(format, surface.getSurface(), null, 0);
			decoder.setVideoScalingMode(MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
			Log.d("DEBUG", "Media Format: " + format);
			super.prepare();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public void release() {
		
		if (decoder != null && decoderState.get() == RecorderState.STARTED) {
			Log.d("DEBUG", "stopping and releasing decoder");
			if(isPlaying)
				stop();
			decoder.stop();
			decoder.release();
			decoder = null;
			super.release();
			
		}
		isPlaying = false;
	}

	
	@Override
	public void run() {
		// TODO Auto-generated method stub

		try {
			long startMs = SystemClock.elapsedRealtime();
			decoder.start(); 
			Log.d("DEBUG", "Decoder started");
			inputBuffers = decoder.getInputBuffers();
			decoder.getOutputBuffers();
			DecodableFrame frame = null;
			MediaFormat format = null;
			long presentationTime = -1;

			while (isPlaying) {
				boolean dequeued = false;
				while (!dequeued) {
					try {
						frame = playerQueue.take();
						dequeued = true;
						
					} catch (NoSuchElementException e) {
						// Log.d("DEBUG", "empty queue: " + e.getMessage());
						e.printStackTrace();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				if(!isPlaying)
					break;
				BufferInfo info = new BufferInfo();
				int inIndex;
				while ((inIndex = decoder.dequeueInputBuffer(1)) < 0)
					;

				if (inIndex >= 0) {
					ByteBuffer buffer = inputBuffers[inIndex];
					buffer.clear();

					buffer.put(frame.getBuffer());
					presentationTime = frame.getPresentationTime();
					Log.d("DEBUG", "input time stamp: " + presentationTime);
					// decoder.queueInputBuffer(inIndex, 0, 65536,
					// presentationTime,
					// 0); //confusion about the valid data size
					decoder.queueInputBuffer(inIndex, 0, frame.getLength(),
							presentationTime, 0); // confusion about the valid
													// data
													// size
				}

				int outIndex = decoder.dequeueOutputBuffer(info, 100000);
				switch (outIndex) {
				case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
					Log.d("DEBUG", "INFO_OUTPUT_BUFFERS_CHANGED");
					decoder.getOutputBuffers();
					break;
				case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
					try {
						format = decoder.getOutputFormat();
					} catch (Exception e) {
						Log.d("DEBUG", "couldn't get output format");
					}
					Log.d("DEBUG", "New format " + format);
					break;
				case MediaCodec.INFO_TRY_AGAIN_LATER:

					Log.d("DEBUG", "dequeueOutputBuffer timed out!");
					break;
				default:
					// buffer.clear();
					Log.d("DEBUG",
							"elapsed time: "
									+ (System.currentTimeMillis() - startMs));
					Log.d("DEBUG", "output presentation time: "
							+ info.presentationTimeUs);
					while (info.presentationTimeUs / 1000 > (SystemClock
							.elapsedRealtime() - startMs)) {
						// Log.d("DEBUG", "waiting before presenting");
						try {
							Thread.sleep(10);
						} catch (InterruptedException e) {
							e.printStackTrace();
							break;
						}
					}
					Log.d("DEBUG", "Decoded data length: " + info.size);
					if (surface.isActive()) {
						decoder.releaseOutputBuffer(outIndex, true);
					}
					break;
				}
			}

			Log.i("DEBUG", "stopping decoder");

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				decoder.stop();
				decoder.release();
				decoder = null;
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

	}

}

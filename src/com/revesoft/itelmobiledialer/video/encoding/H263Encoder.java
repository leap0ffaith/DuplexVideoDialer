package com.revesoft.itelmobiledialer.video.encoding;

import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;

import com.revesoft.itelmobiledialer.video.VideoCallFrameActivity;
import com.revesoft.itelmobiledialer.video.stream.packetizer.H263PlusPacketizer;
import com.revesoft.itelmobiledialer.video.stream.packetizer.H264Packetizer;

import android.graphics.ImageFormat;
import android.hardware.Camera.CameraInfo;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.Toast;

public class H263Encoder extends Encoder{

	MediaCodec mMediaCodec;
	ByteBuffer[] inputBuffers;
	ByteBuffer[] outputBuffers;
	MediaFormat mediaFormat = null;
	public static byte[] SPS = null;
	public static byte[] PPS = null;
	public static int frameID = 0;

	public H263Encoder(int previewWidth, int previewHieght) throws SocketException 
	{
		super(previewWidth, previewHieght);

		queue = new ArrayBlockingQueue<Frame>(100);

		packetizer = new H263PlusPacketizer(queue);
		
		mMediaCodec = MediaCodec.createEncoderByType("video/3gpp");
		mediaFormat = MediaFormat.createVideoFormat("video/3gpp", previewHieght, previewWidth);
		//mediaFormat = MediaFormat.createVideoFormat("video/3gpp", previewWidth, previewHieght); // when no manual rotation on camera data is done
		mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 125000);
		mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 15);
		mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 5);
		mediaFormat.setInteger(MediaFormat.KEY_SAMPLE_RATE, 8000);
		mediaFormat.setInteger(MediaFormat.KEY_CHANNEL_COUNT, 1);

		try
		{
			mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar);
//			mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar);
			
			mMediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
			frameID = 0;
			
			rotatedFrameData = new byte[previewWidth*previewHieght*(ImageFormat.getBitsPerPixel(ImageFormat.YV12)) / 8];
			planeManagedData = new byte[previewWidth*previewHieght*(ImageFormat.getBitsPerPixel(ImageFormat.YV12)) / 8];
			
			encoderStarted = true;
			mMediaCodec.start();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	
	
	
	public void stopEncoder()
	{
		encoderStarted = false;
		if(mMediaCodec != null)
		{
			mMediaCodec.stop();
			mMediaCodec.release();
			mMediaCodec = null;
		}
	}


	

	@Override
	public void encode(byte[] rawData) 
	{
		//Log.d("EncodeDecode", "ENCODE FUNCTION CALLED");
		inputBuffers = mMediaCodec.getInputBuffers();
		outputBuffers = mMediaCodec.getOutputBuffers();

		int inputBufferIndex = mMediaCodec.dequeueInputBuffer(0);
		if (inputBufferIndex >= 0)
		{
			ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
			inputBuffer.clear();

			int size = inputBuffer.limit();
			//inputBuffer.put(YV12toYUV420PackedSemiPlanar(rawData,previewWidth,previewHieght));
			//inputBuffer.put(rotateYUVCameraData(rawData, VideoCallFrameActivity.CURRENT_CAMERA, previewWidth, previewHieght));
			//inputBuffer.put(rawData);
			rotateYUVCameraData(rawData, planeManagedData, rotatedFrameData, VideoCallFrameActivity.CURRENT_CAMERA, previewWidth, previewHieght);
			inputBuffer.put(rotatedFrameData);
			
			mMediaCodec.queueInputBuffer(inputBufferIndex, 0 /* offset */, size, (System.currentTimeMillis() - startMS) * 1000 /* timeUs */, 0);
			//mMediaCodec.queueInputBuffer(inputBufferIndex, 0 /* offset */, size, 0 /* timeUs */, 0);
			//Log.d("EncodeDecode", "InputBuffer queued");
		}
		else
		{
			//Log.d("EncodeDecode", "inputBufferIndex < 0, returning null");
			return ;
		}

		MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
		int outputBufferIndex = mMediaCodec.dequeueOutputBuffer(bufferInfo, 0);
		//Log.d("EncodeDecode", "outputBufferIndex = " + outputBufferIndex);
		do
		{
			if (outputBufferIndex >= 0)
			{
				Frame frame = new Frame(frameID);
				ByteBuffer outBuffer = outputBuffers[outputBufferIndex];
				byte[] outData = new byte[bufferInfo.size];
				int dataLength = 0;

				outBuffer.get(outData);

				dataLength = outData.length-2;
				frame.frameData = new byte[dataLength];
				// skipping 0x00 0x80 while copying
				System.arraycopy(outData, 2 , frame.frameData, 0, dataLength);

				// for testing
				Log.d("EncodeDecode" , "Frame no :: " + frameID + " :: frameSize:: " + frame.frameData.length + " :: ");
				//printByteArray(frame.frameData);

				// if encoding type is h263 then, enqueueing the frame in the queue
				try
				{
					queue.put(frame);
				}
				catch(InterruptedException e)
				{
					Log.e("EncodeDecode", "interrupted while waiting");
					e.printStackTrace();
				}
				catch(NullPointerException e)
				{
					Log.e("EncodeDecode", "frame is null");
					e.printStackTrace();
				}
				catch(IllegalArgumentException e)
				{
					Log.e("EncodeDecode", "problem inserting in the queue");
					e.printStackTrace();
				}
				Log.d("EncodeDecode", "H263 frame enqueued. queue size now: " + queue.size());
			
				frameID++;
				mMediaCodec.releaseOutputBuffer(outputBufferIndex, false);
				outputBufferIndex = mMediaCodec.dequeueOutputBuffer(bufferInfo, 0);

			}
			else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED)
			{
				outputBuffers = mMediaCodec.getOutputBuffers();
				Log.e("EncodeDecode","output buffer of encoder : info changed");
			}
			else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED)
			{
				Log.e("EncodeDecode","output buffer of encoder : format changed");
			}
			else
			{
				Log.e("EncodeDecode", "unknown value of outputBufferIndex : " + outputBufferIndex);
				//printByteArray(data);
			}
		} while (outputBufferIndex >= 0);

	}

}

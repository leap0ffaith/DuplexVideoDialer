package com.revesoft.itelmobiledialer.video.encoding;

import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;

import com.revesoft.itelmobiledialer.video.VideoCallFrameActivity;
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

public class H264Encoder extends Encoder{

	MediaCodec mMediaCodec;
	ByteBuffer[] inputBuffers;
	ByteBuffer[] outputBuffers;
	MediaFormat mediaFormat = null;
	public static byte[] SPS = null;
	public static byte[] PPS = null;
	public static int frameID = 0;
	

	public H264Encoder(int previewWidth, int previewHieght) throws SocketException 
	{
		super(previewWidth, previewHieght);

		queue = new ArrayBlockingQueue<Frame>(100);

		packetizer = new H264Packetizer(queue);

		mMediaCodec = MediaCodec.createEncoderByType("video/avc");
		mediaFormat = MediaFormat.createVideoFormat("video/avc", previewHieght, previewWidth);
		//mediaFormat = MediaFormat.createVideoFormat("video/avc", previewWidth, previewHieght); // when no manual rotation on camera data is done
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

			mMediaCodec.start();
			encoderStarted = true;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	/**========================================================================*/
	/** This function gets the starting index of the first appearance of match array in source array. The function will search in source array from startIndex position.*/
	public static int find(byte[] source, byte[] match, int startIndex) 
	{  
		if(source == null || match == null)
		{
			//Log.d("EncodeDecode", "ERROR in find : null");
			return -1;
		}
		if(source.length == 0 || match.length == 0)
		{
			//Log.d("EncodeDecode", "ERROR in find : length 0");
			return -1;
		}

		int ret = -1;  
		int spos = startIndex;  
		int mpos = 0;  
		byte m = match[mpos];  
		for( ; spos < source.length; spos++ ) 
		{  
			if(m == source[spos]) 
			{  
				// starting match  
				if(mpos == 0)  
					ret = spos;  
				// finishing match  
				else if(mpos == match.length - 1)  
					return ret;  

				mpos++;  
				m = match[mpos];  
			}  
			else 
			{  
				ret = -1;  
				mpos = 0;  
				m = match[mpos];  
			}  
		}  
		return ret;  
	}


	/**========================================================================*/
	/** For H264 encoding, this function will retrieve SPS & PPS from the given data and will insert into SPS & PPS global arrays. */
	public static boolean getSPS_PPS(byte[] data, int startingIndex)
	{
		byte[] spsHeader = {0x00, 0x00, 0x00, 0x01, 0x67};
		byte[] ppsHeader = {0x00, 0x00, 0x00, 0x01, 0x68};
		byte[] frameHeader = {0x00, 0x00, 0x00, 0x01};

		int spsStartingIndex = -1;
		int nextFrameStartingIndex = -1;
		int ppsStartingIndex = -1;

		spsStartingIndex = find(data, spsHeader, startingIndex);
		//Log.d("EncodeDecode", "spsStartingIndex: " + spsStartingIndex);
		if(spsStartingIndex >= 0)
		{
			nextFrameStartingIndex = find(data, frameHeader, spsStartingIndex+1);
			int spsLength = 0;
			if(nextFrameStartingIndex>=0)
				spsLength = nextFrameStartingIndex - spsStartingIndex;
			else
				spsLength = data.length - spsStartingIndex;
			if(spsLength > 0)
			{
				SPS = new byte[spsLength];
				System.arraycopy(data, spsStartingIndex, SPS, 0, spsLength);
			}
		}

		ppsStartingIndex = find(data, ppsHeader, startingIndex);
		//Log.d("EncodeDecode", "ppsStartingIndex: " + ppsStartingIndex);
		if(ppsStartingIndex >= 0)
		{
			nextFrameStartingIndex = find(data, frameHeader, ppsStartingIndex+1);
			int ppsLength = 0;
			if(nextFrameStartingIndex>=0)
				ppsLength = nextFrameStartingIndex - ppsStartingIndex;
			else
				ppsLength = data.length - ppsStartingIndex;
			if(ppsLength > 0)
			{
				PPS = new byte[ppsLength];
				System.arraycopy(data, ppsStartingIndex, PPS, 0, ppsLength);
			}
		}
		return (SPS!=null && PPS!=null);
	}


	public void stopEncoder()
	{
		if(encoderStarted)
		{
			SPS = null;
			PPS = null;
			encoderStarted = false;
			if(mMediaCodec != null)
			{
				mMediaCodec.stop();
				mMediaCodec.release();
				mMediaCodec = null;
			}
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
			rotateYUVCameraData(rawData, planeManagedData, rotatedFrameData, VideoCallFrameActivity.CURRENT_CAMERA, previewWidth, previewHieght);
			inputBuffer.put(rotatedFrameData);
			//inputBuffer.put(rawData);

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
			
				//printByteArray(outData);
				// If SPS & PPS is not ready then 
				if( (SPS == null || SPS.length ==0) || (PPS == null || PPS.length == 0) )
				{
					Log.d("spspps","SPS PPS required");
					if(getSPS_PPS(outData, 0))
					{	
						Log.d("spspps","SPS PPS found");
						((H264Packetizer)this.packetizer).setH264Params(SPS, PPS);

						frame.frameData = new byte[SPS.length-4];
						System.arraycopy(SPS, 4 , frame.frameData, 0, SPS.length-4);
						try
						{
							Log.d("EncodeDecode", "enqueueing frame no: " + (frameID));
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
						frameID++;
						frame = new Frame(frameID);
						frame.frameData = new byte[PPS.length-4];
						System.arraycopy(PPS, 4 , frame.frameData, 0, PPS.length-4);
						try
						{
							Log.d("EncodeDecode", "enqueueing frame no: " + (frameID));
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
						frameID++;
						continue;
					}
					// somehow set the SPS & PPS of the streamer class here
				}

				dataLength = outData.length-4;
				frame.frameData = new byte[dataLength];
				//System.arraycopy(outData, 0 , frame.frameData, 0, dataLength);
				// skipping 0x00 0x00 0x00 0x01 while copying
				System.arraycopy(outData, 4 , frame.frameData, 0, dataLength);

				// for testing
				//Log.d("EncodeDecode" , "Frame no :: " + frameID + " :: frameSize:: " + frame.frameData.length + " :: ");
				//printByteArray(frame.frameData);

				// if encoding type is h264 and sps & pps is ready then, enqueueing the frame in the queue
				// if encoding type is h263 then, enqueueing the frame in the queue
				if(SPS != null && PPS != null && SPS.length != 0 && PPS.length != 0)
				{
					Log.d("EncodeDecode", "enqueueing frame no: " + (frameID));

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
					//Log.d("EncodeDecode", "frame enqueued. queue size now: " + queue.size());
				}

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

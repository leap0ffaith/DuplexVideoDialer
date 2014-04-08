package com.revesoft.itelmobiledialer.video.encoding;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import android.hardware.Camera.CameraInfo;
import android.util.Log;

import com.revesoft.itelmobiledialer.video.stream.packetizer.VideoPacketizer;

public abstract class Encoder {

	BlockingQueue<Frame> queue = new ArrayBlockingQueue<Frame>(100);
	VideoPacketizer packetizer = null;
	int previewWidth;
	int previewHieght;
	long startMS = 0;
	byte[] rotatedFrameData = null;
	byte[] planeManagedData = null;
	boolean encoderStarted = false;
	
	public Encoder( int previewWidth, int previewHieght) {
		this.previewHieght = previewHieght;
		this.previewWidth = previewWidth;
	}

	abstract public void encode(byte[] rawData);

	public void setPacketizer(VideoPacketizer packetizer) {
		this.packetizer = packetizer;
	}

	public boolean startSending() {
		if (this.packetizer == null)
			return false;
		this.packetizer.start();
		return true;
	}
	
	public abstract void stopEncoder();
	
	public boolean stopSending(){
		if (this.packetizer == null)
			return false;
		this.packetizer.stop();
		return true;
	}
	public boolean hasEncoderStarted()
	{
		return encoderStarted;
	}
	public void setStartMS(long ms)
	{
		this.startMS = ms;
	}
	
	/**========================================================================*/
	/** Prints the byte array in hex */
	protected void printByteArray(byte[] array)
	{
		StringBuilder sb1 = new StringBuilder();
		for (byte b : array) 
		{
			sb1.append(String.format("%02X ", b));
		}
		Log.d("EncodeDecode", sb1.toString());
	}



	/*public static byte[] YV12toYUV420PackedSemiPlanar(final byte[] input, final int width, final int height) {

	 * COLOR_TI_FormatYUV420PackedSemiPlanar is NV12
	 * We convert by putting the corresponding U and V bytes together (interleaved).

	    final int frameSize = width * height;
	    final int qFrameSize = frameSize/4;
	    byte[] output = new byte[input.length];


	    //for resolution 320x240	
	    System.arraycopy(input, 0, output, 0, frameSize);
	    for (int i = 0; i < (qFrameSize); i++) 
	    {
	    	output[frameSize + i*2] =   (input[frameSize + qFrameSize + i - 32 - 320]);
	        output[frameSize + i*2 + 1] = (input[frameSize + i - 32 - 320]);
	    }

	    //for resolution 640x480 and above,
	    System.arraycopy(input, 0, output, 0, frameSize);    
        for (int i = 0; i < (qFrameSize); i++) {  
            output[frameSize + i*2] = (input[frameSize + qFrameSize + i]);  
            output[frameSize + i*2 + 1] = (input[frameSize + i]);   
        } 

	    System.arraycopy(input, 0, output, 0, frameSize); // Y

	    for (int i = 0; i < qFrameSize; i++) {
	        output[frameSize + i*2] = input[frameSize + i + qFrameSize]; // Cb (U)
	        output[frameSize + i*2 + 1] = input[frameSize + i]; // Cr (V)
	    }
	    return output;
	}*/

	public static void YV12toYUV420PackedSemiPlanar(final byte[] input, byte[] out, final int width, final int height) {
		/* 
		 * COLOR_TI_FormatYUV420PackedSemiPlanar is NV12
		 * We convert by putting the corresponding U and V bytes together (interleaved).
		 */
		final int frameSize = width * height;
		final int qFrameSize = frameSize/4;
		
		for (int i = 0; i < input.length ; i++) 
		{
			if(i<frameSize)
				out[i] = input[i];
			if(i < (qFrameSize))
			{
				out[frameSize + i*2] = input[frameSize + i + qFrameSize]; // Cb (U)
				out[frameSize + i*2 + 1] = input[frameSize + i]; // Cr (V)
			}
		}
	}

	static byte[] NV21toYUV420p(byte[] data, int width, int height) 
	{
		int len_target = (width * height * 3) / 2;
		byte[] buf_target = new byte[len_target];
		System.arraycopy(data, 0, buf_target, 0, width * height);

		for (int i = 0; i < (width * height / 4); i++) {
			buf_target[(width * height) + i] = data[(width * height) + 2 * i + 1];
			buf_target[(width * height) + (width * height / 4) + i] = data[(width * height) + 2 * i];
		}
		return buf_target;
	}

	public void rotateYUV420Degree90(byte[] data, byte[] output, int imageWidth, int imageHeight) 
	{
		//byte [] yuv = new byte[imageWidth*imageHeight*3/2];
		// Rotate the Y luma
		int i = 0;
		for(int x = 0;x < imageWidth;x++)
		{
			for(int y = imageHeight-1;y >= 0;y--)                               
			{
				output[i] = data[y*imageWidth+x];
				i++;
			}
		}
		// Rotate the U and V color components 
		i = imageWidth*imageHeight*3/2-1;
		for(int x = imageWidth-1; x > 0; x=x-2)
		{
			for(int y = 0;y < imageHeight/2;y++)                                
			{
				output[i] = data[(imageWidth*imageHeight)+(y*imageWidth)+x];
				i--;
				output[i] = data[(imageWidth*imageHeight)+(y*imageWidth)+(x-1)];
				i--;
			}
		}
		//return yuv;
	}

	/**========================================================================*/
	/** This function rotates the data that comes from the onPreviewFrame function */
	public void rotateYUVCameraData(byte[] data, byte[] plane, byte[] rotation, int cameraFace, int width, int hieght)
	{

		// color right, but rotated
		/*byte[] output = YV12toYUV420PackedSemiPlanar(data,320,240);
		 */

		//rotation array => output 

		// back camera 
		if(cameraFace == CameraInfo.CAMERA_FACING_BACK)
		{
			YV12toYUV420PackedSemiPlanar(data, plane, width,hieght);
			rotateYUV420Degree90(plane, rotation ,width,hieght);

		}

		// front camera
		if(cameraFace == CameraInfo.CAMERA_FACING_FRONT)
		{
			YV12toYUV420PackedSemiPlanar(data, plane, width,hieght);
			rotateYUV420Degree90(plane, rotation ,width,hieght);
			rotateYUV420Degree90(rotation,plane,hieght,width);
			rotateYUV420Degree90(plane, rotation ,width,hieght);
			//return output;
		}
		//return data;
	}
}

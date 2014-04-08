package com.revesoft.itelmobiledialer.customview;

import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import com.revesoft.itelmobiledialer.signaling.SIPProvider;
import com.revesoft.itelmobiledialer.video.VideoCallFrameActivity;
import com.revesoft.itelmobiledialer.video.encoding.Encoder;

public class MySurfaceView extends SurfaceView implements SurfaceHolder.Callback  
{
	SurfaceHolder holder;
	Camera mCamera = null;
	//int CAMERA_FACE = CameraInfo.CAMERA_FACING_FRONT; //default value
//	int cameraPreviewWidth = 320; //default value
//	int cameraPreviewHeight = 240; //default value
	private Encoder encoder;
	boolean mPreviewRunning = false;
	
	public MySurfaceView(Context context, int cameraFace) 
	{  
		super(context); 
		holder = this.getHolder();
		holder.addCallback(this); 
//		cameraPreviewWidth = camPrevWidth;
//		cameraPreviewHeight = camPrevHeight;
	}

	public MySurfaceView(Context context, AttributeSet attrs) {  
		super(context,attrs); 
		holder = this.getHolder();
		holder.addCallback(this); 
	}
	
	public void setEncoder(Encoder enc){
		this.encoder = enc;
	}
	
	private void setCameraDisplayOrientation(int cameraId,
			android.hardware.Camera camera) {
		android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
		android.hardware.Camera.getCameraInfo(cameraId, info);
		int rotation = ((Activity)this.getContext()).getWindowManager().getDefaultDisplay().getRotation();
		int degrees = 0;
		switch (rotation) {
		case Surface.ROTATION_0:
			degrees = 0;
			break;
		case Surface.ROTATION_90:
			degrees = 90;
			break;
		case Surface.ROTATION_180:
			degrees = 180;
			break;
		case Surface.ROTATION_270:
			degrees = 270;
			break;
		}

		int result;
		if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
			result = (info.orientation + degrees) % 360;
			result = (360 - result) % 360; // compensate the mirror
		} else { // back-facing
			result = (info.orientation - degrees + 360) % 360;
		}
		camera.setDisplayOrientation(result);
	}
	
	public boolean safeCameraOpen(int id) {
		// Camera camera = null;

		try {
			releaseCamera();
			if (!isCameraPresent(id))
			{
				Log.e("MySurfaceView", "no camera Present!!");
				return false;
			}
			mCamera = Camera.open(id);
			Parameters p = mCamera.getParameters();
			p.set("orientation", "landscape");
			p.setRotation(getRotation(id, ((Activity)this.getContext()).getWindowManager()
					.getDefaultDisplay().getRotation()));
			mCamera.setParameters(p);
			setCameraDisplayOrientation(id, mCamera);

			// Method rotateMethod;
			// rotateMethod =
			// android.hardware.Camera.class.getMethod("setDisplayOrientation",
			// int.class);
			// rotateMethod.invoke(mCamera, 90);

			//Log.d(TAG, "Camera opened: " + id);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return mCamera != null;
	}
	
	private void releaseCamera() {
		Log.w("CameraLOG","releaseCamera Function called");
		// mPreview.setCamera(null);
		if (mCamera != null) {
			mCamera.lock();
			mCamera.release();
			mCamera = null;
		}
	}

	public int getRotation(int cameraId, int orientation) {
		android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
		android.hardware.Camera.getCameraInfo(cameraId, info);
		orientation = (orientation + 45) / 90 * 90;
		int rotation = 0;
		if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
			rotation = (info.orientation - orientation + 360) % 360;
		} else { // back-facing camera
			rotation = (info.orientation + orientation) % 360;
		}
		return rotation;
	}

	public boolean isCameraPresent(int camID) {
		CameraInfo ci = new CameraInfo();
		for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
			Camera.getCameraInfo(i, ci);
			if (ci.facing == camID)
				return true;
		}
		return false; // No front-facing camera found
	}
	
	public void startEncoding(int previewWidth, int previewHeight){
		if (mPreviewRunning) 
		{
			mCamera.stopPreview();
//			Log.e("EncodeDecode","preview stopped");
		}
		try 
		{
			if(mCamera == null)
			{
				return;
			}

			Camera.Parameters p = mCamera.getParameters();
			p.setPreviewSize(previewWidth, previewHeight);

//			p.setPreviewFormat(ImageFormat.NV21);
			p.setPreviewFormat(ImageFormat.YV12);
			mCamera.setParameters(p);
			mCamera.setPreviewDisplay(holder);
			mCamera.unlock();
			mCamera.reconnect();
			mCamera.setPreviewCallback(new PreviewCallback()
			{
				@Override
				public void onPreviewFrame(byte[] data, Camera camera)
				{
					encoder.encode(data);
				}
			});
//			Log.d("EncodeDecode", "previewCallBack set");
			mCamera.startPreview();
			mPreviewRunning = true;
		}
		catch (Exception e)
		{
//			Log.e("EncodeDecode","surface changed:set preview display failed");
			e.printStackTrace();
		}
	}

	public void surfaceCreated(SurfaceHolder holder) {  
		try
		{

//			cameraPreviewWidth = this.getWidth();
//			cameraPreviewHeight = this.getHeight();
			
			//safeCameraOpen(CAMERA_FACE);
			safeCameraOpen(VideoCallFrameActivity.CURRENT_CAMERA);
			
			Camera.Parameters p = mCamera.getParameters();
			p.setPreviewSize(SIPProvider.videoWidth, SIPProvider.videoHeight);
			
			//p.setPreviewFormat(ImageFormat.NV21);
			mCamera.setParameters(p);
			mCamera.setPreviewDisplay(holder);
			mCamera.setPreviewCallback(new PreviewCallback()
			{
				@Override
				public void onPreviewFrame(byte[] data, Camera camera)
				{ 
					
				}
			});
			
			mCamera.startPreview();
			mPreviewRunning = true;
		} 
		catch (IOException e) 
		{
//			Log.e("EncodeDecode","surfaceCreated():: in setPreviewDisplay(holder) function");
			e.printStackTrace();
		}
		catch (NullPointerException e)
		{
//			Log.e("EncodeDecode","surfaceCreated Nullpointer");
			e.printStackTrace();
		}
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) 
	{  
		//startEncoding();
	}

	public void surfaceDestroyed(SurfaceHolder holder) 
	{
		//mCamera.lock();
		mCamera.setPreviewCallback(null);
		mCamera.stopPreview();
		try {
			mCamera.setPreviewDisplay(null);
		} catch (IOException e1) {
			Log.e("cameraLog", "error while setting null in setPreviewDisplay of camera");
			e1.printStackTrace();
		}
		holder.removeCallback(this);
		holder = null; 
		//mCamera.unlock();
		mCamera.release();
		
		if(encoder != null)
			encoder.stopEncoder();
		
		/*if(SIPProvider.videoSocket != null)
			SIPProvider.videoSocket.close();*/
	}  
	
	public void switchCam(int previewWidth, int previewHeight)
	{
		if(!mPreviewRunning)
		{
			if(VideoCallFrameActivity.CURRENT_CAMERA==CameraInfo.CAMERA_FACING_FRONT)
			{
				if(isCameraPresent(CameraInfo.CAMERA_FACING_BACK))
				{
					VideoCallFrameActivity.CURRENT_CAMERA = CameraInfo.CAMERA_FACING_BACK;
				}
				else
				{
					Log.e("EncodeDecode", "CAMERA switching falied :: Only Front camera available!");
					return ;
				}
			}
			else if(VideoCallFrameActivity.CURRENT_CAMERA==CameraInfo.CAMERA_FACING_BACK)
			{
				if(isCameraPresent(CameraInfo.CAMERA_FACING_FRONT))
				{
					VideoCallFrameActivity.CURRENT_CAMERA = CameraInfo.CAMERA_FACING_FRONT;
				}
				else
				{
					Log.e("EncodeDecode", "CAMERA switching falied :: Only Back camera available!");
					return ;
				}
			}
		} 
		else
		{
			try 
			{
				if (mPreviewRunning) 
				{
					mCamera.stopPreview();
					Log.e("EncodeDecode","preview stopped");
				}
				mCamera.setPreviewCallback(null);
				mCamera.release();
				mCamera = null;
			
				if(VideoCallFrameActivity.CURRENT_CAMERA==CameraInfo.CAMERA_FACING_FRONT)
				{
					if(isCameraPresent(CameraInfo.CAMERA_FACING_BACK))
					{
						VideoCallFrameActivity.CURRENT_CAMERA = CameraInfo.CAMERA_FACING_BACK;
					}
					else
					{
						Log.e("EncodeDecode", "CAMERA switching falied :: Only Front camera available!");
						return ;
					}
				}
				else if(VideoCallFrameActivity.CURRENT_CAMERA==CameraInfo.CAMERA_FACING_BACK)
				{
					if(isCameraPresent(CameraInfo.CAMERA_FACING_FRONT))
					{
						VideoCallFrameActivity.CURRENT_CAMERA =  CameraInfo.CAMERA_FACING_FRONT;
					}
					else
					{
						Log.e("EncodeDecode", "CAMERA switching falied :: Only Back camera available!");
						return ;
					}
				}
				else
				{
					Log.e("EncodeDecode", "CAMERA switching falied!");
					return ;
				}
				//safeCameraOpen(CAMERA_FACE); 
				mCamera = Camera.open(VideoCallFrameActivity.CURRENT_CAMERA);
				//mCamera.setDisplayOrientation(90);
				setCameraDisplayOrientation(VideoCallFrameActivity.CURRENT_CAMERA, mCamera);
				
				Camera.Parameters p = mCamera.getParameters();
				p.setPreviewSize(previewWidth, previewHeight);
				//p.setPreviewFormat(ImageFormat.NV21);
				p.setPreviewFormat(ImageFormat.YV12);
				mCamera.setParameters(p);
				mCamera.setPreviewDisplay(holder);
				mCamera.unlock();
				mCamera.reconnect();
				mCamera.setPreviewCallback(new PreviewCallback()
				{
					@Override
					public void onPreviewFrame(byte[] data, Camera camera)
					{
						encoder.encode(data);
					}
				});
//				Log.d("EncodeDecode", "previewCallBack set");
				mCamera.startPreview();
				mPreviewRunning = true;
			}
			catch (Exception e)
			{
				Log.e("EncodeDecode", "CAMERA switching falied!");
				e.printStackTrace();
				return ;
			}
		}
	}
}
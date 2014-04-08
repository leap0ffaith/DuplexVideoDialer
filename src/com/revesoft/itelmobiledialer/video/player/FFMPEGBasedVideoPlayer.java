package com.revesoft.itelmobiledialer.video.player;

import java.util.NoSuchElementException;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.util.Log;

import com.revesoft.itelmobiledialer.video.decoding.CodecParameters;
import com.revesoft.itelmobiledialer.video.decoding.FFMPEGDecoder;

/**
 * A player based on the Android MediaCodec API It has-a queue of
 * DecodableFrames which is subsequently dequeued and played.
 * 
 * @author Dhiman paul
 */

public class FFMPEGBasedVideoPlayer extends VideoPlayer {

	private FFMPEGDecoder decoder = null;

	public FFMPEGBasedVideoPlayer(int codecID, int resID) {
		super(codecID, resID);
		decoder = new FFMPEGDecoder(codecID);
	}

	@Override
	public void prepare() {
		/*
		 * configure the decoder
		 */
		try {
			decoder.configure(CodecParameters.RES_352x288_CIF);
			super.prepare();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public void release() {
		if (decoder != null && decoderState.get() == RecorderState.STARTED) {
			Log.d("DEBUG", "stopping and releasing decoder");
			if (isPlaying)
				stop();
			decoder.release();
			decoder = null;
			super.release();
		}
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

		try {
			Log.d("DEBUG", "Decoder started");
			DecodableFrame frame = null;
			while (isPlaying) {
				boolean dequeued = false;
				while (!dequeued) {
					try {
						Log.d("DecoderQueue","Size of playerQueue is "+ playerQueue.size());
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

				int[] decoded_data = decoder.getDecodedData(frame.getLength(),
						frame.getBuffer());
				try {
					if (decoded_data.length > 0)
						render(decoded_data, decoder.getWidth(),
								decoder.getHeight());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				Log.i("DEBUG", "stopping decoder");
				decoder.release();
				decoder = null;
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

	}

	private void render(int[] rgb, int w, int h) throws Exception {
		Log.i("FFMPEGBasedVideoPlayer", " Height: " + h + " Width: " + w);
		final Bitmap bmp = Bitmap.createBitmap(rgb, w, h,
				Bitmap.Config.ARGB_8888);
		try {
			Canvas canvas = surface.getSurface().lockCanvas(null);
			Paint p = new Paint();
		    p.setColor(Color.WHITE);
		    canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
		    canvas.drawColor(Color.BLACK);
		    
			Matrix mat = new Matrix();
			mat.setTranslate(0, 0);
			if (w > h)
				mat.setRotate(270, w / 2, h / 2);
			if (display_width / w > display_height / h)
				mat.postScale(display_height / h, display_height / h);
			else
				mat.postScale(display_width / w, display_width / w);

			canvas.drawBitmap(bmp, mat, p);
			surface.getSurface().unlockCanvasAndPost(canvas);
		} catch (Exception e) {
			e.printStackTrace();
			Log.w("FFMPEGBasedVideoPlayer", "can't render image");
		}
	}
}
package com.revesoft.itelmobiledialer.video.receiver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.SocketException;

import android.os.Environment;
import android.util.Log;

import com.revesoft.itelmobiledialer.video.encoding.FrameFragment;
import com.revesoft.itelmobiledialer.video.encoding.H264ParameterSets;
import com.revesoft.itelmobiledialer.video.encoding.NalUnit;
import com.revesoft.itelmobiledialer.video.player.DecodableFrame;
import com.revesoft.itelmobiledialer.video.player.VideoPlayer;

/**
 * A receiver class that extends the abstract RtpReceiver class. It receives rtp
 * packets for h264 encoded data for packetization mode 0 i.e Single Nal Unit
 * Mode and de-packetize accordingly.
 * 
 * @author Kazi Tasnif
 */

public class H264VideoReceiver extends RtpReceiver {

	private NalUnit sps;

	private NalUnit pps;

	private H264ParameterSets h264params = null;

	private NalUnitBuffer nalUnitBuffer = null;
	private FrameFragmentBuffer frUnitBuffer = null;
	
	private FrameFragment fragment = null;

	private DecodableFrame frame = null;
	/*
	 * max size of received nal unit fragments or nalUnits
	 */
	private static final int windowSize = 400;

	private boolean startOfFrame = false;

	private NalUnit nUnit = null;

	private NalUnit[] frameArray = null;

	private byte[] delimeter = { 'e', 'o', 'p' };
	/*
	 * after a marked packet is received the expected sequence number is set to
	 * the next sequence number of the marked packet. if a corresponding packet
	 * in the arrival order contains the nextSeq the corresponding nal unit in
	 * the packet is marked as the first nal unit of the frame and inserted in
	 * the nal unit buffer accordingly
	 */

	private int nextSeq = -1;

	//private FileOutputStream fos = null;

	private boolean endOfFrame = false;
	
	private int receivedFrameCount = 0;
	
	public H264VideoReceiver(VideoPlayer player) throws SocketException {
		super();
		// TODO Auto-generated constructor stub

		this.player = player;

		nalUnitBuffer = new NalUnitBuffer(windowSize);
		frUnitBuffer = new FrameFragmentBuffer(windowSize);

		sps = null;

		pps = null;

		h264params = null;

		frameArray = new NalUnit[windowSize + 2];

		/*try {
			fos = new FileOutputStream(
					Environment.getExternalStorageDirectory()
							+ "/iphoneFramesb");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/

		
		receivedFrameCount = 0;
	}

	@Override
	protected void processData(byte[] buf) {
		// TODO Auto-generated method stub
		// boolean skip = false;
		/*
		 * set the timestamp associated with the nal units to be stored in the
		 * buffer
		 */

		nalUnitBuffer.setTimeStamp(tStamp);

		nUnit = new NalUnit(buf, rtphl, packetSize - rtphl);

		int type = nUnit.getType();

		if (type == 0) {
			return;
		} else if (type == 7) {
			sps = nUnit;
			return;
		} else if (type == 8) {
			pps = nUnit;
			return;
		} else if (type == 6) {
			return;
		} else if (type == 5 || type == 1) {
			if (type == 5) {
				nalUnitBuffer.markAsIdrFrameBuffer();
			}
			nalUnitBuffer.insert(nUnit, sequenceNumber);
			if (isMarked()) {

				NalUnit[] bufferedNalUnits = nalUnitBuffer.getFrameNalUnits();
				int numberOfNalUnits = bufferedNalUnits.length;
				if ((nalUnitBuffer.isIdrFrameBuffer())
						&& (sps != null && pps != null)) {
					frameArray = new NalUnit[2 + numberOfNalUnits];
					frameArray[0] = sps;
					frameArray[1] = pps;
					System.arraycopy(bufferedNalUnits, 0, frameArray, 2,
							numberOfNalUnits);
					frame = new DecodableFrame(frameArray);

				} else {
					frame = new DecodableFrame(bufferedNalUnits);
				}
				frame.setPresentationTime(tStamp);
				nalUnitBuffer.reset();

			}else {
				return;
			}

		} else if (type == 28) {
			Log.d("DEBUG", "receiving fragmented nal unit");

			int end = ((buf[rtphl + 1] & 0x40) >> 6) & 0x01; // check the end
																// bit
			int beginning = ((buf[rtphl + 1] & 0x80) >> 7) & 0x01; // check the
																	// start bit

			if (beginning == 1) {
				byte[] firstFragment = new byte[packetSize - (rtphl + 2) + 1];
				firstFragment[0] = (byte) ((buf[rtphl] & 0xe0) + (buf[rtphl + 1] & 0x1f));
				System.arraycopy(buf, rtphl + 2, firstFragment, 1, packetSize
						- (rtphl + 2)); 
				fragment = new FrameFragment(firstFragment);
				fragment.markAsFirstFragment();
				Log.d("DEBUG", "received first fragment");
			} else {
				fragment = new FrameFragment(buf, rtphl + 2, packetSize
						- (rtphl + 2)); 
				if (end == 1) {
					fragment.markAsLastFragment();
					Log.d("DEBUG", "received last fragment");
				}
			}

			frUnitBuffer.insert(fragment, sequenceNumber);
			if (frUnitBuffer.hasReceivedAll()) {
				Log.d("DEBUG", "received all fragments");
				NalUnit fu = new NalUnit(frUnitBuffer.accumulate());
				
				if(fu.getType()==5){
					frameArray = new NalUnit[3];
					frameArray[0] = sps;
					frameArray[1] = pps;
					frameArray[2] = fu;
					frame = new DecodableFrame(frameArray);
				} else {
					frame = new DecodableFrame(fu.getNalUnitData(), DecodableFrame.H264_FRAME);
				}
				frame.setPresentationTime((tStamp / 90) * 1000);
				frUnitBuffer.reset();
			} else {
				return;
			}
		} 

		if(frame != null)
		{
			if(frame.getLength()==4) return;
			frame.setTimeStamp(tStamp);
			receivedFrameCount++;
			Log.d("DecoderQueue", receivedFrameCount + " no frame queued in playerQueue");
			player.submitFrame(frame);
			// TODO write this data in file
//			try
//			{
//				player.fos1.write(frame.getBuffer(),0,frame.getBuffer().length);
//			}
//			catch (Exception e)
//			{
//				e.printStackTrace();
//				System.exit(0);
//			}
		
			
			Log.d("isplaying", "isPlaying is " + player.isPlaying + ".");
			if (!player.isPlaying) {
				if (sps != null && pps != null) {
					h264params = new H264ParameterSets(sps, pps);
				} else {
					Log.d("isplaying", "returned");
					return;
				}
	
				player.setH264ParameterSet(h264params);
				
				startPlayer();
			}
		}

	}
}

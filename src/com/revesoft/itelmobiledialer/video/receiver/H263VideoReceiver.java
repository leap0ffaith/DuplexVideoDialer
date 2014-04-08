package com.revesoft.itelmobiledialer.video.receiver;

import java.net.SocketException;

import com.revesoft.itelmobiledialer.video.encoding.FrameFragment;
import com.revesoft.itelmobiledialer.video.player.DecodableFrame;
import com.revesoft.itelmobiledialer.video.player.VideoPlayer;

/**
 * A receiver class that extends the abstract RtpReceiver class. It receives rtp
 * packets for h263 encoded data and depacketize accordingly.
 * 
 * @author Kazi Tasnif
 */

public class H263VideoReceiver extends RtpReceiver {

	private DecodableFrame frame = null;

	private FrameFragment fragment = null;
	private int fragmentBufferSize = 400;

	private FrameFragmentBuffer fragmentBuffer = null;
	private static final int PAYLOAD_HEADER_LEN = 4;

	public H263VideoReceiver(VideoPlayer player) throws SocketException {
		super();

		this.player = player;
		fragmentBuffer = new FrameFragmentBuffer(fragmentBufferSize);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void processData(byte[] buf) {
		

		fragmentBuffer.setTimeStamp(tStamp);

		if( packetSize < rtphl + PAYLOAD_HEADER_LEN)
			return; 
		
		int startOfFrame = ((buf[rtphl] & 0x04) >> 2);
		if (startOfFrame == 1 ) {

			if (isMarked()) {
				/*
				 * a single frame has arrived in a single packet so construct a
				 * decodable frame and submit it to the player
				 */

				frame = new DecodableFrame(buf, rtphl + PAYLOAD_HEADER_LEN, packetSize
						- (rtphl + PAYLOAD_HEADER_LEN), DecodableFrame.H263_FRAME);
				frame.setPresentationTime(tStamp);
				// fragmentBuffer.reset();
			} else {
				fragment = new FrameFragment(buf, rtphl + PAYLOAD_HEADER_LEN, packetSize
						- (rtphl + PAYLOAD_HEADER_LEN));
				fragment.markAsFirstFragment();
				fragmentBuffer.insert(fragment, sequenceNumber);
				return;

			}

		} else {

			// Log.d("DEBUG", "receiving fragment data");
			fragment = new FrameFragment(buf, rtphl + PAYLOAD_HEADER_LEN, packetSize
					- (rtphl + PAYLOAD_HEADER_LEN));
			if (isMarked()) {
				/*
				 * optionally insert this frame too to the buffer. Another
				 * choice is to append it to
				 */
				fragment.markAsLastFragment();
				fragmentBuffer.insert(fragment, sequenceNumber);
				frame = new DecodableFrame(fragmentBuffer.accumulate(),
						DecodableFrame.H263_FRAME);
				frame.setPresentationTime(fragmentBuffer.getTimeStamp());
				fragmentBuffer.reset();
			} else {

				fragmentBuffer.insert(fragment, sequenceNumber);
				return;
			}

		}

		/*
		 * submit the constructed frame to the player
		 */

		if (frame != null)
			player.submitFrame(frame);

		/*
		 * if playing hasn't yet started start playing.
		 */
		if (!player.isPlaying) {
			startPlayer();
		}

	}

}

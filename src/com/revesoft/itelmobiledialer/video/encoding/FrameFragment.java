package com.revesoft.itelmobiledialer.video.encoding;


/**
 * this class co-serves as a fragment of 
 * a single NAL unit or a h263 frame.
 * in case of fragmentation of a single NAL unit
 * a NAL unit is fragmented for packet types
 * FU-A or FU-B. Similarly a single h263 frame
 * can arrive in multiple packets where each packet
 * contains the fragment of a single frame.
 * 
 * @author Kazi Tasnif
 */


public class FrameFragment{
	
	private byte[] buf = null;
	
	private boolean firstFragment = false;
	private boolean lastFragment = false;
	
	public FrameFragment(byte[] buffer){
		
		this.buf = new byte[buffer.length];
		System.arraycopy(buffer, 0, buf, 0, buffer.length);
		
	}
	
	public FrameFragment(byte[] buffer, int offset, int length){
		
		this.buf = new byte[length];
		System.arraycopy(buffer, offset, buf, 0, length);
		
	}
	
	public int getLength(){
		return this.buf.length;
	}
	public byte[] getFragmentData(){
		
		byte[] fragmentData = new byte[buf.length];
		System.arraycopy(buf, 0, fragmentData, 0, buf.length);
		return fragmentData;
	}

	/*
	 * mark this fragment as the
	 * first fragment of the
	 * nal unit or the h263 frame. The first fragment
	 * obviously contains the nal unit header if it
	 * is a fragment of the nal unit.
	 * 
	 */
	public void markAsFirstFragment(){
		this.firstFragment = true;
	}
	/*mark this fragment
	 * as the last fragment
	 * of the corresponding nal unit
	 * or the h263 frame
	 * 
	 */
	public void markAsLastFragment(){
		this.lastFragment = true;
	}
	
	public boolean isLastFragment(){
		return lastFragment;
	}

	public boolean isFirstFragment() {
		return firstFragment;
	}

}

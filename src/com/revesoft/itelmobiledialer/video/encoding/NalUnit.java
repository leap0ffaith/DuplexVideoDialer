package com.revesoft.itelmobiledialer.video.encoding;

import java.io.Serializable;

/**
 * a class for representing a nal unit. a nal unit is
 * * the primary unit for h264 encoded data. first byte of
 * each nal unit acts as the nal unit header.
 *  
 *  @author Kazi Tasnif
 */
/*

 *The structure of the nal unit is as follows
+---------------+
|0|1|2|3|4|5|6|7|
+-+-+-+-+-+-+-+-+
|F|NRI|   Type  |
+---------------+
*/
public class NalUnit implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/*buffer for keeping the nal unit*/
	private byte[] buf = new byte[0];
	
	public NalUnit(byte[] buffer){
		if(buffer == null)
			return;
		buf = new byte[buffer.length];
		System.arraycopy(buffer, 0, buf, 0, buffer.length);
	}
	
    public NalUnit(byte[] buffer, int offset, int length){
    	if(buffer == null)
			return;
		buf = new byte[length];
		System.arraycopy(buffer, offset, buf, 0, length);
	}
	/*
	 * constructs a nal unit from fragments
	 * 
	 */
	public NalUnit(FrameFragment[] fragmentArray){
		byte[] tempBuffer = new byte[65536];
		
		int destinationPosition = 0;
		
		for(int i = 0; i < fragmentArray.length; i++){
			
			byte[] fragmentData = fragmentArray[i].getFragmentData();
			
			System.arraycopy(fragmentData, 0, tempBuffer, destinationPosition, fragmentData.length);
			
			destinationPosition += fragmentData.length;
		}
		
		System.arraycopy(tempBuffer, 0, buf, 0, destinationPosition);
		
		
	}
	
	/*returns the type of the nal unit*/
	public int getType(){
		if(buf==null)
			return -1;
		if(buf.length>0)
			return (buf[0] & 0x1f);
		else
			return -1;
	}
	/*returns the header*/
	public byte getHeader(){
		return buf[0];
	}
	/*get the value of nri*/
	
	public int getNalRefIdc(){
		if(buf==null)
			return -1;
		return ((buf[0] & 0x60) >> 5);
	}
	
	/*get the length of the nal unit*/
    public int getLength(){
    	if(buf==null)
			return 0;
		return buf.length;
	}
    /*return nal unit data*/
	public byte[] getNalUnitData(){
		if(buf==null)
			return new byte[0];
		byte[] nalUnitData = new byte[buf.length];
		System.arraycopy(buf, 0, nalUnitData, 0, buf.length);
		return nalUnitData;
	}
	
}

package com.revesoft.itelmobiledialer.video.encoding;

import java.io.Serializable;

import android.util.Base64;

/**
 * A class for storing the Sequence parameter set and
 * picture parameter set. It also provides a functionality
 * for parsing profile level frome the sequence parameter
 * set.
 * @author Kazi Tasnif
 */

public class H264ParameterSets implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
    /*sequence parameter set which
     * itself is a nalunit
     */
	private NalUnit sps = null;
	/*
	 * picture parameter set which
	 * itself is a nalunit
	 */
	private NalUnit pps = null;
	
	public H264ParameterSets(NalUnit sps, NalUnit pps){
		this.sps = sps;
		this.pps = pps;
	}
	
	public H264ParameterSets(byte[] sps, byte[] pps){
		this.sps = new NalUnit(sps);
		this.pps = new NalUnit(pps);
	}
	
	/*
	 * get the sequence parameter set as a nalunit
	 */
	public NalUnit getSps(){
		return this.sps;
	}
	
	
	/*
	 * get the picture parameter set as a nalunit
	 */
	public NalUnit getPps(){
		return this.pps;
	}
	/*
	 * get the raw sps data
	 */
	public byte[] getSpsValue(){
		return this.sps.getNalUnitData();
	}
	
	/*
	 * get the raw pps data
	 */
	
	public byte[] getPpsValue(){
		return this.pps.getNalUnitData();
	}
	/*
	 * return the number of bytes in the sequence parameter set
	 */
	public int getSpsLength(){
		return this.sps.getLength();
	}
	/*
	 * return the number of bytes in the picture parameter set
	 */
	public int getPpsLength(){
		return this.pps.getLength();
	}
	
	/*
	 *  a function for building a hex string
	 */
	static private String toHexString(byte[] buffer,int start, int len) {
		String c;
		StringBuilder s = new StringBuilder();
		for (int i=start;i<start+len;i++) {
			c = Integer.toHexString(buffer[i]&0xFF);
			s.append( c.length()<2 ? "0"+c : c );
		}
		return s.toString();
	}
	/*
	 * return the profile level id by parsing the sps value
	 */
	public String getProfileLevel() {
	    
		return toHexString(sps.getNalUnitData(), 1, 3);
	}
	
	public String getB64PPS() {
        return Base64.encodeToString(pps.getNalUnitData(), 0, pps.getLength(), Base64.NO_WRAP);
    }

    public String getB64SPS() {
        return Base64.encodeToString(sps.getNalUnitData(), 0, sps.getLength(), Base64.NO_WRAP);
    }

}

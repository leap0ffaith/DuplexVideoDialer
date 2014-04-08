package com.revesoft.itelmobiledialer.video.encoding;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Set;

import com.revesoft.itelmobiledialer.video.stream.RecordingParameters;

/**
 * This class has-a hashmap
 * that contains the recording parameters
 * as a key and the corresponding sps pps value
 * as the value with respect to that key.
 * This class is serializable and is written to
 * a private file to store the sps pps value 
 * with respect to different recording parameters
 * .
 * 
 * @author Kazi Tasnif
 */

public class ParameterSetHashmap implements Serializable{
	
	private static final long serialVersionUID = 1L;
	/*
	 * the hashmap to store the parameter set as a value
	 * against the recording parameters class
	 */
	private HashMap<RecordingParameters, H264ParameterSets> hashMap;
	public ParameterSetHashmap(){
		hashMap = new HashMap<RecordingParameters, H264ParameterSets>();
	}
	public void put(RecordingParameters key, H264ParameterSets value ){
		hashMap.put(key, value);
    }
	public H264ParameterSets get(RecordingParameters key){
	
		return hashMap.get(key);
	}
	
	public Set<RecordingParameters>  keySet(){
		return hashMap.keySet();
	}
}

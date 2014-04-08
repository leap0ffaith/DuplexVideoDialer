package com.revesoft.itelmobiledialer.video.receiver;

import com.revesoft.itelmobiledialer.video.encoding.FrameFragment;


/**
 * a buffer that can store fragments of a nal unit
 * or a h263 frame. The insertion takes place in a sorted order
 * 
 * @author Kazi Tasnif
 */
public class FrameFragmentBuffer
 {
	private int windowLength = -1;
	private boolean receivedFirstFragment = false;
	private boolean receivedLastFragment = false;

	private FrameFragment[] fragmentBuffer;
	/*
	 * an array to store the sequence numbers 
	 * of the packets in which the fragments
	 * have arrived
	 */
	

	private int[] sequenceArray;
    
	private int length = 0;
    
	private int numberOfFragments = 0;
	/*
	 * each fragment must conform to a single timestamp value
	 */
	private long tStamp = -1;
	
	public FrameFragmentBuffer(int windowSize){
		this.windowLength = windowSize;
		this.fragmentBuffer = new FrameFragment[windowLength];
		this.sequenceArray = new int[windowLength];
	}
	
	public FrameFragmentBuffer(int windowSize, long timeStamp){
        this.windowLength = windowSize;
        
        this.fragmentBuffer = new FrameFragment[windowLength];
        this.sequenceArray = new int[windowLength];
        this.tStamp = timeStamp;
    }
	/*
	 * insert a nal unit fragment or a h263 frame fragment in the buffer 
	 * with the sequence number
	 * of the packet passed as a parameter
	 */
	public void insert(FrameFragment fragment, int sequenceNumber){
		
		length += fragment.getLength();
		fragmentBuffer[numberOfFragments] = fragment;
		sequenceArray[numberOfFragments] = sequenceNumber;
		numberOfFragments++;
		
		if(fragment.isFirstFragment()){
			this.receivedFirstFragment = true;
		}
		else if(fragment.isLastFragment()){  
			this.receivedLastFragment = true;
		}
		sort();
	}
	
	private void sort(){
	    for (int j = numberOfFragments - 1; j > 0 && (sequenceArray[j-1] > sequenceArray[j] ); j--){
	        exch(fragmentBuffer, j - 1, j);
            
            sequenceArray[j - 1] = sequenceArray[j - 1] ^ sequenceArray[j];
            sequenceArray[j] = sequenceArray[j - 1] ^ sequenceArray[j];
            sequenceArray[j - 1] = sequenceArray[j - 1] ^ sequenceArray[j];
	     }
	}
	
	private static void exch(Object[] a, int i, int j) {
	        Object swap = a[i];
	        a[i] = a[j];
	        a[j] = swap;
	}
	
	private boolean isIntegral(){
		boolean integral = false;
	    
		int i = 0;
		for(i = 0; i < numberOfFragments - 1; i++){
			if(sequenceArray[i + 1] != (sequenceArray[i] + 1))break;
		}
		
		if(i == numberOfFragments - 1 ){
			integral = true;
		}
		return integral;
		
	}
	
	public boolean hasReceivedAll(){
		if(receivedFirstFragment && receivedLastFragment ){
			return true;
		}
		return false;
	
	}
	
	public byte[] accumulate(){   
		
		byte[] data = new byte[length];
		int dstPos = 0;
	    for(int i = 0; i < numberOfFragments; i++){
	    	byte[] fragmentData = fragmentBuffer[i].getFragmentData();
	    	
	    	System.arraycopy(fragmentData, 0, data, dstPos, fragmentData.length);
	    	dstPos += fragmentData.length;
	    }
	    this.reset();
	    if(!isIntegral()&&data.length>0)
	    	data[0]|=0x80;
		return data;
		
	}
	/*
	 * set the timestamp associated with the fragments
	 * stored in this buffer
	 */
	public void setTimeStamp(long timeStamp){
		this.tStamp = timeStamp;
	}
	
	public long getTimeStamp(){
	    return this.tStamp;
	}
     
	public void reset(){
		
	   // Log.d("DEBUG", "resetting");
		length = 0;
		numberOfFragments = 0;
		
        receivedFirstFragment = false;
		receivedLastFragment = false;
	}
}

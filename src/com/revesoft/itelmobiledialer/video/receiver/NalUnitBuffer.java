package com.revesoft.itelmobiledialer.video.receiver;

import com.revesoft.itelmobiledialer.video.encoding.NalUnit;

/**
 * A buffer to hold the nal units that
 * constitute a single frame. The nal units
 * stored in this buffer must conform to the same
 * frame and therefore same timeStamp value.
 * 
 * @author Kazi Tasnif
 */
public class NalUnitBuffer {
    private int windowLength = -1;
    
    /*
     * an array to store the nal units 
     */
    private NalUnit[] nalUnitBuffer;
 
    /*
     * an array to store the sequence numbers 
     * of the packets in which the nal unit fragment
     * has arrived
     */

    private int[] sequenceArray;
    
    private boolean isIdrFrameBuffer = false;
    
    private int numberOfNalUnits = 0;
    
    /*
     * the timestamp of the packets in which the nal
     * units stored in this buffer arrive.
     * They conform to the same timestamp value
     * as they are part of the same frame
     */
    private long tStamp = -1;
    
    public NalUnitBuffer(int windowSize){
        this.windowLength = windowSize;
        this.nalUnitBuffer = new NalUnit[windowLength];
        this.sequenceArray = new int[windowLength];
    }
   
    public void insert(NalUnit nUnit, int sequenceNumber){
        
       
        nalUnitBuffer[numberOfNalUnits] = nUnit;
        sequenceArray[numberOfNalUnits] = sequenceNumber;
        
        numberOfNalUnits++;

        sort();
    }
    
    private void sort(){
        for (int j = numberOfNalUnits - 1; j > 0 && (sequenceArray[j-1] > sequenceArray[j] ); j--){
            exch(nalUnitBuffer, j - 1, j);
            
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
    /*
     * set the timestamp associated with the
     * packets in which the nal units in this
     * buffer are stored
     * 
     */
    public void setTimeStamp(long ts){
        
        this.tStamp = ts;
    }
    
    public long getTimeStamp(){
        return this.tStamp;
    }
    
    public void markAsIdrFrameBuffer(){
        this.isIdrFrameBuffer = true;
    }
    
    public boolean isIdrFrameBuffer(){
        return isIdrFrameBuffer;
    }
    
    public NalUnit[] getFrameNalUnits(){
        
        NalUnit[] nalUnitArray = new NalUnit[numberOfNalUnits];
        
        System.arraycopy(nalUnitBuffer, 0, nalUnitArray, 0, numberOfNalUnits);
        
        return nalUnitArray;
        
    }
    
    public int getNumberOfNalUnits(){
        return numberOfNalUnits;
    }
    public void reset(){
        numberOfNalUnits = 0;
        isIdrFrameBuffer = false;
    }
}

package com.revesoft.itelmobiledialer.customview;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

@SuppressLint("NewApi")
public class TouchListener implements OnTouchListener {

	private final static String TAG="Mkhan";
	private float aPosX;
	private float aPosY;
	private float aLastTouchX;
	private float aLastTouchY;
	private static final int INVALID_POINTER_ID = -1;
	
	 
	// The active pointer is the one currently moving our object.
	private int mActivePointerId = INVALID_POINTER_ID;
	private View frame =null;
	
	
	public TouchListener(View frame) {
	     super();
	     this.frame = frame;
	     
	     
	    
	   }
	
	 
	@SuppressLint("NewApi")
	public boolean onTouch(View view, MotionEvent event) {
	 
	switch (event.getAction() & MotionEvent.ACTION_MASK) {
	   case MotionEvent.ACTION_DOWN:
	     //from http://android-developers.blogspot.com/2010/06/making-sense-of-multitouch.html
	     Log.d(TAG, "action down");
	     // Save the ID of this pointer
	     mActivePointerId = event.getPointerId(0);
	     final float x = event.getX(mActivePointerId);
	     final float y = event.getY(mActivePointerId);
	     // Remember where we started
	     aLastTouchX = x;
	     aLastTouchY = y;
	//to prevent an initial jump of the magnifier, aposX and aPosY must
	//have the values from the magnifier frame
	     if (aPosX == 0){
	         aPosX = frame.getX();
	      }
	      if (aPosY == 0){
	          aPosY = frame.getY();
	       }
	    break;
	 
	    case MotionEvent.ACTION_UP:
	      Log.d(TAG, "action up");
	      reset();
	    break;
	 
	    case MotionEvent.ACTION_POINTER_DOWN:
	    break;
	 
	    case MotionEvent.ACTION_POINTER_UP:
	      // Extract the index of the pointer that left the touch sensor
	      final int pointerIndex = (event.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
	      final int pointerId = event.getPointerId(pointerIndex);
	      if (pointerId == mActivePointerId) {
	         // This was our active pointer going up. Choose a new
	         // active pointer and adjust accordingly.
	         final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
	          mActivePointerId = event.getPointerId(newPointerIndex);
	       }
	  break;
	  case MotionEvent.ACTION_MOVE:
	 
	     // Find the index of the active pointer and fetch its position
	    final int pointerIndexMove = event.findPointerIndex(mActivePointerId);
	    Log.d(TAG, "action move");
	    float xMove = event.getX(pointerIndexMove);
	    float yMove = event.getY(pointerIndexMove);
	     
	     
	 
	//from http://android-developers.blogspot.com/2010/06/making-sense-of-multitouch.html
	     // Calculate the distance moved
	    final float dx = xMove - aLastTouchX;
	    final float dy = yMove - aLastTouchY;
	 
	     
	    // Move the frame
	    aPosX += dx;
	    aPosY += dy;   
	 
		// Remember this touch position for the next move event
		//no! see http://stackoverflow.com/questions/17530589/jumping-imageview-while-dragging-getx-and-gety-values-are-jumping?rq=1 and
		// last comment in http://stackoverflow.com/questions/16676097/android-getx-gety-interleaves-relative-absolute-coordinates?rq=1
		//aLastTouchX = xMove;
		//aLastTouchY = yMove;
		Log.d(TAG, "we moved");
		 
		//in this area would be code for doing something with the magnified view as the frame moves.
		
		
		
	    frame.setX(aPosX);
	    frame.setY(aPosY);
	    
	    break;
	 
	    case MotionEvent.ACTION_CANCEL: {
	      mActivePointerId = INVALID_POINTER_ID;
	    break;
	   }
	  }
	 
	    return true;
	}
	 
	 private void reset(){
	   aPosX = 0;
	   
	   aPosY = 0;
	   aLastTouchX = 0;
	   aLastTouchY = 0;
//	   frame.setVisibility(View.INVISIBLE);
	 
	  }
	 /**
	 private void restrictBound() {
		 float containerX=container.getX();
		 float containerY=container.getY();
		 
		 Log.d(TAG, "Container X "+containerX+ " Container Y "+containerY);
	 }
	**/
}

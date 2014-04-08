package com.revesoft.itelmobiledialer.video.player;

import java.util.concurrent.atomic.AtomicReference;



import android.content.Context;
import android.view.Surface;

/**
 * a wrapper class to wrap the Surface implementation in android.view
 * It keeps a stateReference to check whether the surface has already 
 * been destroyed or not
 * 
 * This implementation is used for rendering the decoded data.
 * 
 * @author Kazi Tasnif
 */



public class RenderingSurface{

	private enum State{Active, Destroyed};
	private AtomicReference<State> stateReference;
	private Surface surface = null;
	private Context context = null; 
	
	public RenderingSurface(Surface surface, Context cont) {
		this.surface = surface;
		stateReference = new AtomicReference<State>();
		stateReference.set(State.Active);
		context = cont;
	}
	/*
	 * the original surface has been destroyed
	 * so the wrapper object is marked as inactive 
	 */
	public void markAsInactive(){
		stateReference.set(State.Destroyed);
	}
	/*
	 * check if the surface is active
	 * for rendering 
	 */
	public boolean isActive(){
		boolean active = true;
	    if(stateReference.get() == State.Destroyed){
			active = false;
		}
	    return active;
	}
	/*
	 * return the wrapped surface object
	 */
	public Surface getSurface(){
		if(stateReference.get() == State.Active){
			return surface;
		}
		else return null;
	}
	
	public Context getContext(){
		return context;
	}
}

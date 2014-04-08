package com.revesoft.itelmobiledialer.util;

import android.media.AudioManager;
import android.media.ToneGenerator;

public class DTMFTones {
	private ToneGenerator tones;
	public DTMFTones(){
		tones = new ToneGenerator(AudioManager.STREAM_MUSIC, 60);
	}
	
	public void playTone(int tone){
		if(tone>-1 || tone<12)
			tones.startTone(tone, 150);
	}
	
	public void release(){
		tones.release();
	}
}

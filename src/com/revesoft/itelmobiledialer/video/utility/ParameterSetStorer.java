package com.revesoft.itelmobiledialer.video.utility;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.StreamCorruptedException;
import java.util.Iterator;
import java.util.Set;

import android.content.Context;
import android.util.Log;
import android.view.SurfaceHolder;

import com.revesoft.itelmobiledialer.video.encoding.H264ParameterSets;
import com.revesoft.itelmobiledialer.video.encoding.NalUnit;
import com.revesoft.itelmobiledialer.video.encoding.ParameterSetHashmap;
import com.revesoft.itelmobiledialer.video.stream.H264Test;
import com.revesoft.itelmobiledialer.video.stream.RecordingParameters;

public final class ParameterSetStorer {
	private static final String fileName = "H264ParameterSet";
	private FileInputStream fis;
	
	private FileOutputStream fos;
	private Context context;
	private ParameterSetHashmap hashmap;
	private ObjectOutputStream oos = null;
	private ObjectInputStream ois = null;
	public ParameterSetStorer(Context context){
		this.context = context;
	}
	
	public boolean checkIfFirstRun(){
		boolean firstRun = true;
		try {
			fis = this.context.openFileInput(ParameterSetStorer.fileName);
			firstRun = false;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			Log.d("DEBUG", "recorded parameters file not found" + e.getMessage());
			//e.printStackTrace();
		}
		return firstRun;
	}
	
	public boolean write(ParameterSetHashmap hMap){
		boolean isObjectWritten = false;
		
		if(fos == null){
			try {
				fos = this.context.openFileOutput(fileName, Context.MODE_PRIVATE);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try{
			oos = new ObjectOutputStream(fos);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			oos.writeObject(hMap);
			isObjectWritten = true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return isObjectWritten;
		
	}
	
	public ParameterSetHashmap read(){
		if(fis == null){
			try{
				fis = new FileInputStream(fileName);
			}catch(Exception e){
				Log.d("DEBUG", "couldn't open file input stream: " + e.getMessage());
			}
		}
		try {
			ois = new ObjectInputStream(fis);
		} catch (StreamCorruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ParameterSetHashmap tempMap = null;
		try {
			tempMap = (ParameterSetHashmap) ois.readObject();
		} catch (OptionalDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return tempMap;
	}
	
	public H264ParameterSets parseSpsPps(RecordingParameters params, SurfaceHolder sh) {
		H264Test test = null;
		try {
			test = new H264Test(params, sh);
			test.takeSample();
			test.parse();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		byte[] sps = test.getSPS();
		byte[] pps = test.getPPS();
		H264ParameterSets _h264params = new H264ParameterSets(
				new NalUnit(sps), new NalUnit(pps));
		return _h264params;
	}
	
	public H264ParameterSets storeParameterSet(RecordingParameters params, SurfaceHolder sh){
		H264ParameterSets h264params = null;
		if (checkIfFirstRun()) {

			h264params = parseSpsPps(params, sh);
			ParameterSetHashmap toWriteHashMap = new ParameterSetHashmap();
			toWriteHashMap.put(params, h264params);

			write(toWriteHashMap);
			// toWriteHashMap.put(params, );

		} else {
			Log.d("DEBUG", "second run");
			ParameterSetHashmap storedMap = read();
			Set<RecordingParameters> keySet = storedMap.keySet();
			Iterator<RecordingParameters> it = keySet.iterator();
			while (it.hasNext()) {
				RecordingParameters recParams = it.next();
				if (recParams.equals(params)) {
					Log.d("DEBUG", "equal my buddy :D");
				}
			}

			h264params = storedMap.get(params);
			if (h264params == null) {
				h264params = parseSpsPps(params, sh);
				storedMap.put(params, h264params);
			}

			write(storedMap);
		}
		
		return h264params;
	}
	

}

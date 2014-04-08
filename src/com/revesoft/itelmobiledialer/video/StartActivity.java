package com.revesoft.itelmobiledialer.video;

import java.util.Map;

import revesoft.videodialer.R;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class StartActivity extends PreferenceActivity{
	String TAG = "StartActivity";
	private static final boolean DEBUG = true;

	private ListPreference getCameraPreference() {
		ListPreference camList = new ListPreference(this);
		camList.setKey("camera");
		camList.setTitle("Camera");
		camList.setDefaultValue("0");
		int n = Camera.getNumberOfCameras();

		if(n == 1) {
			camList.setEntries(R.array.one_cam);
			camList.setEntryValues(R.array.one_cam_value);
			camList.setSummary("Back camera");
		}

		else if(n == 2) {
			camList.setEntries(R.array.two_cam);
			camList.setEntryValues(R.array.two_cam_value);
			camList.setSummary("Front camera");
		}

		return camList;
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		((PreferenceCategory)getPreferenceScreen().findPreference("sender_properties")).addPreference(getCameraPreference());
		setContentView(R.layout.start_screen);

		Button btnOK = (Button) findViewById(R.id.btnOK);

		btnOK.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent in = new Intent(StartActivity.this, VideoCallFrameActivity.class);
				startActivity(in);
			}
		});

		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

//		Editor ed = pref.edit();
//		ed.clear();
//		ed.commit();

		findPreference("target_ip").setEnabled(!pref.getBoolean("loopback", false));

		Map <String, ?> allPrefs = pref.getAll();

		for(String key : allPrefs.keySet()) {
			Preference p = findPreference(key);
			if(key.equals("loopback") || key.equals("camera")) continue;
			p.setSummary(pref.getString(key, ""));
			if(DEBUG) Log.i(TAG, key  + ": " + p.getSummary());
			p.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					preference.setSummary(newValue.toString());
					return true;
				}
			});
		}

		findPreference("camera").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				if(newValue.toString().equals("0")) {
					preference.setSummary("Back camera");
				}
				else if(newValue.toString().equals("1")) {
					preference.setSummary("Front camera");
				}
				return true;
			}
		});

		findPreference("loopback").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				findPreference("target_ip").setEnabled(!(Boolean)newValue);
				return true;
			}
		});
	}
}

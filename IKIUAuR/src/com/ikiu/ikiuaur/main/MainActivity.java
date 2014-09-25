package com.ikiu.ikiuaur.main;

import com.example.ikiuaur.R;
import com.ikiu.ikiuaur.data.ARData;
import com.ikiu.ikiuaur.data.LocalDataSource;

import android.content.Context;
import android.os.Bundle;

import android.view.Menu;
import android.view.WindowManager;
/**
 * This class is main.
 * 
 */
public class MainActivity extends ARActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		LocalDataSource localData = new LocalDataSource(this.getResources(),
				this);
		ARData.addMarkers(localData.getMarkers());

	}

	public void onPause() {
		super.onPause();
		// sensor.unRegisterListneres();

	}

	public void onResume() {
		super.onResume();
		// sensor.registerListeners();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}

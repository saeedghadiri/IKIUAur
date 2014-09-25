package com.ikiu.ikiuaur.main;

import com.example.ikiuaur.R;
import com.example.ikiuaur.R.id;
import com.example.ikiuaur.R.layout;
import com.ikiu.ikiuaur.camera.CameraSurface;
import com.ikiu.ikiuaur.sensors.Sensors;

import android.app.Activity;
import android.content.Context;

import android.hardware.Camera;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.FrameLayout;

/**
 * This class creates necessary classes.
 * 
 */
public class ARActivity extends Activity {

	public static boolean ui_portrait = false;
	public static boolean useMarkerAutoRotate = true;
	public static boolean useDataSmoothing = true;
	public static boolean useCollisionDetection = false;


	protected static CameraSurface camScreen = null;
	protected static AugmentedView augmentedView = null;
	
	Sensors sensor;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		camScreen = new CameraSurface(this);
		setContentView(camScreen);
		augmentedView = new AugmentedView(this);

		LayoutParams augLayout = new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		addContentView(augmentedView, augLayout);

		SensorManager sensorMan = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		LocationManager locationMgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		sensor = new Sensors(sensorMan, locationMgr, augmentedView);

	}

	@Override
	public void onResume() {
		super.onResume();

		sensor.makeStuffReady();
	}

	@Override
	public void onPause() {
		super.onPause();

		sensor.ClearStuff();
	}

}
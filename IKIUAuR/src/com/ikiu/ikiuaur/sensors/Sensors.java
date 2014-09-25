package com.ikiu.ikiuaur.sensors;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.ikiu.ikiuaur.common.Matrix;
import com.ikiu.ikiuaur.common.LowPassFilter;
import com.ikiu.ikiuaur.common.Orientation;
import com.ikiu.ikiuaur.common.Vector;
import com.ikiu.ikiuaur.data.ARData;
import com.ikiu.ikiuaur.main.ARActivity;
import com.ikiu.ikiuaur.main.AugmentedView;
import com.ikiu.ikiuaur.common.Navigation;

import android.content.Context;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.FloatMath;
import android.util.Log;
/**
 * This class is used to get location and rotation Matrix from sensors
 * 
 * This file was adapted from Mixare <http://www.mixare.org/>
 * 
 * @author Daniele Gobbetti <info@mixare.org>
 */
public class Sensors implements SensorEventListener, LocationListener {

	private static final String TAG = "SensorsActivity";
	private static final AtomicBoolean computing = new AtomicBoolean(false);

	private static final int MIN_TIME = 30 * 1000;
	private static final int MIN_DISTANCE = 10;

	private static final float temp[] = new float[9]; // Temporary rotation
														// matrix in Android
														// format
	private static final float rotation[] = new float[9]; // Final rotation
															// matrix in Android
															// format
	private static final float grav[] = new float[3]; // Gravity (a.k.a
														// accelerometer data)
	private static final float mag[] = new float[3]; // Magnetic

	/*
	 * Using Matrix operations instead. This was way too inaccurate, private
	 * static final float apr[] = new float[3]; //Azimuth, pitch, roll
	 */

	private static final Matrix worldCoord = new Matrix();
	private static final Matrix magneticCompensatedCoord = new Matrix();
	private static final Matrix xAxisRotation = new Matrix();
	private static final Matrix yAxisRotation = new Matrix();
	private static final Matrix mageticNorthCompensation = new Matrix();

	private static GeomagneticField gmf = null;
	private static float smooth[] = new float[3];
	private static SensorManager sensorMgr = null;
	private static List<Sensor> sensors = null;
	private static Sensor sensorGrav = null;
	private static Sensor sensorMag = null;
	private static LocationManager locationMgr = null;
	private static AugmentedView augmentedView = null;

	public Sensors(SensorManager sensorMan, LocationManager locationMgr,
			AugmentedView augmentedView) {
		this.sensorMgr = sensorMan;
		this.locationMgr = locationMgr;
		this.augmentedView = augmentedView;

	}

	public void makeStuffReady() {

		float neg90rads = (float) Math.toRadians(-90);

		// Counter-clockwise rotation at -90 degrees around the x-axis
		// [ 1, 0, 0 ]
		// [ 0, cos, -sin ]
		// [ 0, sin, cos ]
		xAxisRotation.set(1f, 0f, 0f, 0f, FloatMath.cos(neg90rads),
				-FloatMath.sin(neg90rads), 0f, FloatMath.sin(neg90rads),
				FloatMath.cos(neg90rads));

		// Counter-clockwise rotation at -90 degrees around the y-axis
		// [ cos, 0, sin ]
		// [ 0, 1, 0 ]
		// [ -sin, 0, cos ]
		yAxisRotation.set(FloatMath.cos(neg90rads), 0f,
				FloatMath.sin(neg90rads), 0f, 1f, 0f,
				-FloatMath.sin(neg90rads), 0f, FloatMath.cos(neg90rads));

		try {

			sensors = sensorMgr.getSensorList(Sensor.TYPE_ACCELEROMETER);
			if (sensors.size() > 0)
				sensorGrav = sensors.get(0);

			sensors = sensorMgr.getSensorList(Sensor.TYPE_MAGNETIC_FIELD);
			if (sensors.size() > 0)
				sensorMag = sensors.get(0);

			sensorMgr.registerListener(this, sensorGrav,
					SensorManager.SENSOR_DELAY_UI);
			sensorMgr.registerListener(this, sensorMag,
					SensorManager.SENSOR_DELAY_UI);

			locationMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER,
					MIN_TIME, MIN_DISTANCE, this);

			try {

				try {
					Location gps = locationMgr
							.getLastKnownLocation(LocationManager.GPS_PROVIDER);
					Location network = locationMgr
							.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
					if (gps != null)
						onLocationChanged(gps);
					else if (network != null)
						onLocationChanged(network);
					else
						onLocationChanged(ARData.hardFix);
				} catch (Exception ex2) {
					onLocationChanged(ARData.hardFix);
				}

				gmf = new GeomagneticField((float) ARData.getCurrentLocation()
						.getLatitude(), (float) ARData.getCurrentLocation()
						.getLongitude(), (float) ARData.getCurrentLocation()
						.getAltitude(), System.currentTimeMillis());

				float dec = (float) Math.toRadians(-gmf.getDeclination());

				synchronized (mageticNorthCompensation) {
					// Identity matrix
					// [ 1, 0, 0 ]
					// [ 0, 1, 0 ]
					// [ 0, 0, 1 ]
					mageticNorthCompensation.toIdentity();

					// Counter-clockwise rotation at negative declination around
					// the y-axis
					// note: declination of the horizontal component of the
					// magnetic field
					// from true north, in degrees (i.e. positive means the
					// magnetic
					// field is rotated east that much from true north).
					// note2: declination is the difference between true north
					// and magnetic north
					// [ cos, 0, sin ]
					// [ 0, 1, 0 ]
					// [ -sin, 0, cos ]
					mageticNorthCompensation.set(FloatMath.cos(dec), 0f,
							FloatMath.sin(dec), 0f, 1f, 0f,
							-FloatMath.sin(dec), 0f, FloatMath.cos(dec));
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} catch (Exception ex1) {
			try {
				if (sensorMgr != null) {
					sensorMgr.unregisterListener(this, sensorGrav);
					sensorMgr.unregisterListener(this, sensorMag);
					sensorMgr = null;
				}
				if (locationMgr != null) {
					locationMgr.removeUpdates(this);
					locationMgr = null;
				}
			} catch (Exception ex2) {
				ex2.printStackTrace();
			}
		}

	}

	public void ClearStuff() {
		try {
			try {
				sensorMgr.unregisterListener(this, sensorGrav);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			try {
				sensorMgr.unregisterListener(this, sensorMag);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			sensorMgr = null;

			try {
				locationMgr.removeUpdates(this);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			locationMgr = null;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void onSensorChanged(SensorEvent evt) {
		if (!computing.compareAndSet(false, true))
			return;

		if (evt.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			if (ARActivity.useDataSmoothing) {
				smooth = LowPassFilter.filter(0.5f, 1.0f, evt.values, grav);
				grav[0] = smooth[0];
				grav[1] = smooth[1];
				grav[2] = smooth[2];
			} else {
				grav[0] = evt.values[0];
				grav[1] = evt.values[1];
				grav[2] = evt.values[2];
			}
			Orientation.calcOrientation(grav);
			ARData.setDeviceOrientation(Orientation.getDeviceOrientation());
			ARData.setDeviceOrientationAngle(Orientation.getDeviceAngle());
		} else if (evt.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
			if (ARActivity.useDataSmoothing) {
				smooth = LowPassFilter.filter(2.0f, 4.0f, evt.values, mag);
				mag[0] = smooth[0];
				mag[1] = smooth[1];
				mag[2] = smooth[2];
			} else {
				mag[0] = evt.values[0];
				mag[1] = evt.values[1];
				mag[2] = evt.values[2];
			}
		}

		// // Find real world position relative to phone location ////
		// Get rotation matrix given the gravity and geomagnetic matrices
		SensorManager.getRotationMatrix(temp, null, grav, mag);

		SensorManager.remapCoordinateSystem(temp, SensorManager.AXIS_Y,
				SensorManager.AXIS_MINUS_Z, rotation);

		/*
		 * Using Matrix operations instead. This was way too inaccurate, //Get
		 * the azimuth, pitch, roll SensorManager.getOrientation(rotation,apr);
		 * float floatAzimuth = (float)Math.toDegrees(apr[0]); if
		 * (floatAzimuth<0) floatAzimuth+=360; ARData.setAzimuth(floatAzimuth);
		 * ARData.setPitch((float)Math.toDegrees(apr[1]));
		 * ARData.setRoll((float)Math.toDegrees(apr[2]));
		 */

		// Convert from float[9] to Matrix
		worldCoord
				.set(rotation[0], rotation[1], rotation[2], rotation[3],
						rotation[4], rotation[5], rotation[6], rotation[7],
						rotation[8]);

		// // Find position relative to magnetic north ////
		// Identity matrix
		// [ 1, 0, 0 ]
		// [ 0, 1, 0 ]
		// [ 0, 0, 1 ]
		magneticCompensatedCoord.toIdentity();

		synchronized (mageticNorthCompensation) {
			// Cross product the matrix with the magnetic north compensation
			magneticCompensatedCoord.prod(mageticNorthCompensation);
		}

		// The compass assumes the screen is parallel to the ground with the
		// screen pointing
		// to the sky, rotate to compensate.
		magneticCompensatedCoord.prod(xAxisRotation);

		// Cross product with the world coordinates to get a mag north
		// compensated coords
		magneticCompensatedCoord.prod(worldCoord);

		// Y axis
		magneticCompensatedCoord.prod(yAxisRotation);

		// Invert the matrix since up-down and left-right are reversed in
		// landscape mode
		magneticCompensatedCoord.invert();

		// Set the rotation matrix (used to translate all object from lat/lon to
		// x/y/z)
		ARData.setRotationMatrix(magneticCompensatedCoord);

		// Update the pitch and bearing using the phone's rotation matrix
		Navigation.calcPitchBearing(magneticCompensatedCoord);
		ARData.setAzimuth(Navigation.getAzimuth());

		computing.set(false);
		if (evt.sensor.getType() == Sensor.TYPE_ACCELEROMETER
				|| evt.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
			augmentedView.postInvalidate();
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		if (sensor == null)
			throw new NullPointerException();

		if (sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD
				&& accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
			Log.e(TAG, "Compass data unreliable");
		}

	}

	public float getLongtitude() {
		return 0;
	}

	public float getLatitude() {
		return 0;
	}

	public float getAltitude() {
		return 0;
	}

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		//this should be deleted
		//location.setAltitude(1111);
		ARData.setCurrentLocation(location);
		gmf = new GeomagneticField((float) ARData.getCurrentLocation()
				.getLatitude(), (float) ARData.getCurrentLocation()
				.getLongitude(), (float) ARData.getCurrentLocation()
				.getAltitude(), System.currentTimeMillis());

		float dec = (float) Math.toRadians(-gmf.getDeclination());

		synchronized (mageticNorthCompensation) {
			mageticNorthCompensation.toIdentity();

			mageticNorthCompensation.set(FloatMath.cos(dec), 0f,
					FloatMath.sin(dec), 0f, 1f, 0f, -FloatMath.sin(dec), 0f,
					FloatMath.cos(dec));
		}

	}

	@Override
	public void onProviderDisabled(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// TODO Auto-generated method stub

	}

}

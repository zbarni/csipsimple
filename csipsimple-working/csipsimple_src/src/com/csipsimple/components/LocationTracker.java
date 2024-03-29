package com.csipsimple.components;

import android.location.*;
import android.os.Bundle;

import com.csipsimple.components.ComponentProfile.Components;
import com.csipsimple.utils.Log;

public class LocationTracker extends AbstractComponent implements LocationListener, SensorInterface {
	private static final String THIS_FILE = "LOCATION";
	private static final long MIN_TIME_UPDATES = 1000 * 60 * 10;

	private String mProvider;
	private LocationManager mLocationManager;
	private LocationListener mLocationListener;
	private Location mLocation;

	private boolean mGPSenabled;
	private boolean isNetworkEnabled;

	public LocationTracker(String id, String name, String type,Components descriptor) {
		super(id, name, type, descriptor);
		initialize();
	}

	public void initialize() {
		Log.d(THIS_FILE,"Location receiver initialized");
		try {
			mLocationManager = (LocationManager)getContext().getSystemService(getContext().LOCATION_SERVICE);
			// getting GPS status
			mGPSenabled = mLocationManager
					.isProviderEnabled(LocationManager.GPS_PROVIDER);

			// getting network status
			isNetworkEnabled = mLocationManager
					.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

			if (!mGPSenabled && !isNetworkEnabled) {
				// no network provider is enabled
				setStatus(ComponentStatus.AVAILABLE);
			} else {
				setStatus(ComponentStatus.ON);
				// First get location from Network Provider
				if (isNetworkEnabled) {
					mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_UPDATES, 0, this);
					Log.d("Network", "Network");
					if (mLocationManager != null) {
						mLocation = mLocationManager
								.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
						if (mLocation != null) {
							//							latitude = location.getLatitude();
							//							longitude = location.getLongitude();
						}
					}
				}
				// if GPS Enabled get lat/long using GPS Services
				if (mGPSenabled) {
					if (mLocation == null) {
						mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_UPDATES, 0, this);
						Log.d("GPS Enabled", "GPS Enabled");
						if (mLocationManager != null) {
							mLocation = mLocationManager
									.getLastKnownLocation(LocationManager.GPS_PROVIDER);
							if (mLocation != null) {
								//								latitude = location.getLatitude();
								//								longitude = location.getLongitude();
							}
						}
					}
				}
			}
		} catch (Exception e) {
			Log.e(THIS_FILE,"Exception ocurred while accessing location component.", e);
		}
	}

	public void startGPS() {
		Log.d(THIS_FILE,"@zbuddy starting gps okay");
		mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_UPDATES, 0, this);
	}
	
	@Override
	public void onLocationChanged(Location location) {
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

}
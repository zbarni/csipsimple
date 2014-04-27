package com.csipsimple.components;

import java.util.HashMap;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;

import com.csipsimple.components.AbstractComponent.ComponentStatus;
import com.csipsimple.components.ComponentProfile.Components;
import com.csipsimple.service.ComponentService;
import com.csipsimple.utils.Log;

@SuppressLint("NewApi")
public class ComponentManager {
	public static final String THIS_FILE = "COMP MGR";

	// Service connection
	private Context mContext = null;
	private static ComponentManager mInstance = null;
	private ComponentService mComponentService = null;

	// Sensors
	public Proximity proximity = null;
	public LocationTracker location = null;
	public Headset headset = null;
	public Accelerometer accelerometer = null;

	// Actuators	
	public Vibrator vibrator = null;
	public Loudspeaker loudspeaker = null; 

	private HashMap<String, AbstractComponent> componentsMap = new HashMap<String, AbstractComponent>();
	
	/**
	 * Returns an instance of ComponentManager class.
	 * @return class instance
	 */
	public static ComponentManager getInstance() {
		synchronized (ComponentManager.class) {
			if (mInstance == null) {
				mInstance = new ComponentManager();
			} 
			else {
			}
			return mInstance;
		}
	}
		
	// empty constructor
	private ComponentManager() {}
	
	/**
	 * This must be called the first time this class is created, otherwise 
	 * it won't have access to PublicationManager and SubscriptionManager
	 * classes and their functions. 
	 * @param service
	 */
	public void initManager (ComponentService service) {
		this.mContext = (Context) service.getSipService();
		this.mComponentService = service;
		initComponents();
	}

	/**
	 * 
	 */
	public void initComponents() {
		proximity = new Proximity(ComponentProfile.ID_PROXIMITY,
				"Proximity", 
				ComponentProfile.SENSOR,
				Components.PROXIMITY);
		componentsMap.put(ComponentProfile.ID_PROXIMITY,proximity);
		
		headset = new Headset(ComponentProfile.ID_HEADSET_WIRED,
				"Wired headset", 
				ComponentProfile.SENSOR,
				Components.HEADSET);
		componentsMap.put(ComponentProfile.ID_HEADSET_WIRED,headset);
		
		location = new LocationTracker(ComponentProfile.ID_GPS_RECEIVER,
				"GPS receiver",
				ComponentProfile.SENSOR, 
				Components.GPS_RECEIVER);
		componentsMap.put(ComponentProfile.ID_GPS_RECEIVER,location);
		
		accelerometer = new Accelerometer(ComponentProfile.ID_ACCELEROMETER,
				"Accelerometer",
				ComponentProfile.SENSOR, 
				Components.ACCELEROMETER);
		componentsMap.put(ComponentProfile.ID_ACCELEROMETER,accelerometer);

		vibrator = new Vibrator(ComponentProfile.ID_VIBRATOR,
				"Vibrator", 
				ComponentProfile.ACTUATOR,
				Components.VIBRATOR);
		componentsMap.put(ComponentProfile.ID_VIBRATOR,vibrator);
		
		Log.d(THIS_FILE,"@zsub Components have been initialized!");
//		loudspeaker = new Loudspeaker("built.in.loudspeaker","Built in loudspeaker", ComponentProfile.ACTUATOR, this);
	}
	
	public void onComponentChange(AbstractComponent component) {
		mComponentService.getPublicationManager().updateComponentModel(component);
	}

	public AbstractComponent getAbstractComponent(int value) {
		Components c = Components.get(value);
		switch (c) {
		case ACCELEROMETER:
			return this.accelerometer;
		case GPS_RECEIVER:
			return this.location;
		case HEADSET:
			if (this.headset == null) return null;
			return this.headset.getStatus() == ComponentStatus.UNAVAILABLE ? null : this.headset;
		case VIBRATOR:
			return this.vibrator;
		case PROXIMITY:
			return this.proximity;
//		case LOUDSPEAKER:   
//			return this.loudspeaker;
		default:
			return null;
		}
	}
	
	public AbstractComponent getAbstractComponentById(String componentId) {
		return componentsMap.get(componentId);
	}

	/**
	 * Gets device manufacturer and model number.
	 * @return string
	 */
	public static String getDeviceName() {
		String manufacturer = Build.MANUFACTURER;
		String model = Build.MODEL;
		if (model.startsWith(manufacturer)) {
			return capitalize(model);
		} else {
			return capitalize(manufacturer) + " " + model;
		}
	}

	private static String capitalize(String s) {
		if (s == null || s.length() == 0) {
			return "";
		}
		char first = s.charAt(0);
		if (Character.isUpperCase(first)) {
			return s;
		} else {
			return Character.toUpperCase(first) + s.substring(1);
		}
	} 
	
	public Context getContext() {
		return mContext;
	}
}

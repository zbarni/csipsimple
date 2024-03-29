package com.csipsimple.components;

import com.csipsimple.components.ComponentProfile.Components;
import com.csipsimple.utils.Log;

public class Vibrator extends AbstractComponent implements Actuator {
	private static final String THIS_FILE = "VIBRATOR";
	private android.os.Vibrator vibrator;
	
	public Vibrator(String id, String name, String type, Components descriptor) {
		super(id, name, type, descriptor);
		initialize();
	}

	public void updateComponent() {
//		this.setStatus(this.mHeadsetConnected ? ComponentStatus.AVAILABLE : ComponentStatus.UNAVAILABLE); 
//		Log.d(THIS_FILE, "@headset updating headset");
//		this.getComponentManager().updateComponentModel(this);
	}
	
	public void onActuatorActivation() {
		vibrator.vibrate(500);
	}
	
	public void initialize() {
		vibrator = (android.os.Vibrator) getContext().getSystemService(getContext().VIBRATOR_SERVICE);
		if (vibrator == null) {
			setStatus(ComponentStatus.UNAVAILABLE);
			Log.d(THIS_FILE,"Vibrator unavailable");
		}
		else {
			setStatus(ComponentStatus.AVAILABLE);
			Log.d(THIS_FILE,"Vibrator initialized");
		}
	}
}

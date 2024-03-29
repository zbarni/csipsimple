package com.csipsimple.components;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Class describing the unique ID attributes for individual components.
 * @author barni
 *
 */

public class ComponentProfile {

	// Attributes
	public static final String ID_ACCELEROMETER= "accelerometer";
	public static final String ID_GPS_RECEIVER = "location.gps";
	public static final String ID_PROXIMITY 	= "proximity";
	public static final String ID_VIBRATOR 	= "vibrator";
	public static final String ID_HEADSET_WIRED= "wired.headset";

	// XML
	public static final String XML_UA = "ua";
	public static final String XML_DEVICE = "device";
	public static final String XML_TYPE = "type";
	public static final String XML_CATEGORY = "category";
	public static final String XML_MODEL = "model";
	public static final String XML_OS = "os";
	public static final String XML_URI = "uri";
	public static final String XML_OS_NAME = "name";
	public static final String XML_OS_VERSION = "version";
	public static final String XML_SENSORS = "sensors";
	public static final String XML_SENSOR = "sensor";
	public static final String XML_ACTUATORS = "actuators";
	public static final String XML_ACTUATOR = "actuator";
	public static final String XML_COMP_NAME = "name";
	public static final String XML_PROFILE = "profile";

	public static final String SENSOR = "sensor";
	public static final String ACTUATOR = "actuator";

	public static enum Components {
		ACCELEROMETER(0),
		GPS_RECEIVER(1),
		PROXIMITY(2),
		VIBRATOR(3),
		HEADSET(4);

		private static final Map<Integer,Components> lookup	= new HashMap<Integer,Components>();

		static {
			for(Components w : EnumSet.allOf(Components.class))
				lookup.put(w.getValue(), w);
		}

		private final int value;
		private Components(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}

		public static Components get(int value) {
			return lookup.get(value);
		}
	}
}

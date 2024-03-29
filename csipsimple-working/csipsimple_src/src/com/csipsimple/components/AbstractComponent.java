package com.csipsimple.components;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import com.csipsimple.components.ComponentProfile.Components;
import com.csipsimple.utils.Log;

public class AbstractComponent {
	private static final String THIS_FILE = "CMP BASE";
	
	public enum ComponentStatus {
		UNAVAILABLE,
		AVAILABLE, // component is there but is NOT active
		ON // component is there and it IS active 
	}

	private String mType;
	private Element mXmlElement;
	private Components mDescriptor;
	
	protected String mId;
	protected String mName;
	protected String mProfile = "http://google.com";
	
	protected ComponentStatus status;
	// only read sensor data if this is true
	protected boolean readSensor = false;
	
	/**
	 * 
	 */
	public void onActivateComponent() { 
		Log.d(THIS_FILE,"@zsub onActivateComponent in base class called, this should not happen.");
	}

	public AbstractComponent(String id, String name, String type, Components descriptor) {
		Log.d(THIS_FILE,"Component constructor");
		this.mId = id;
		this.mName = name;
		this.mType = type;
		this.mXmlElement = null;
		this.mDescriptor = descriptor;
		
	}
	
	@TargetApi(Build.VERSION_CODES.FROYO)
	public Element getComponentElement(Document document) {
		if (this.mXmlElement == null) {
			this.mXmlElement = document.createElement((this.mType == ComponentProfile.ACTUATOR) ? 
					ComponentProfile.ACTUATOR : ComponentProfile.SENSOR);
			this.mXmlElement.setAttribute("id", this.mId);
			
			Element name = document.createElement("name");
			Element profile = document.createElement("profile");
			name.setTextContent(this.mName);
			profile.setTextContent("http://temporary-profile-link");
			this.mXmlElement.appendChild(name);
			this.mXmlElement.appendChild(profile);
		}
		return (this.getStatus() == ComponentStatus.AVAILABLE || 
				this.getStatus() == ComponentStatus.ON) ? 
						this.mXmlElement : null;
	}
	

	public Components getDescriptor() {
		return mDescriptor;
	}

	public void setDescriptor(Components mDescriptor) {
		this.mDescriptor = mDescriptor;
	}
	
	public ComponentStatus getStatus() {
		return status;
	}

	public void setStatus(ComponentStatus status) {
		this.status = status;
	}

	public Context getContext() {
		return ComponentManager.getInstance().getContext();
	}

	
	public String getType() {
		return this.mType;
	}
	
	public String getId() {
		return mId;
	}

	public void setId(String mId) {
		this.mId = mId;
	}

	public String getName() {
		return mName;
	}

	public void setmName(String mName) {
		this.mName = mName;
	}

	public String getmProfile() {
		return mProfile;
	}

	public void setProfile(String mProfile) {
		this.mProfile = mProfile;
	}
}
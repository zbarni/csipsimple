package com.csipsimple.components;

import java.util.Set;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.csipsimple.api.ComponentSubscription;
import com.csipsimple.components.ComponentProfile.Components;
import com.csipsimple.db.DBProvider;
import com.csipsimple.service.ComponentService;

public class SubscriptionManager  {
	public static final String THIS_FILE = "SUB MGR";
	private ComponentService mComponentService;
	private Context mContext;

	public SubscriptionManager(ComponentService cService) {
		this.mComponentService = cService;
		this.mContext = (Context) cService.getSipService();
	}

	public void onSubscriptionUpdate(int accountId, int buddyId) {
		Cursor c = mContext.getContentResolver().query(ComponentSubscription.COMPONENT_SUBSCRIPTIONS_URI,
				DBProvider.COMPONENT_SUBSCRIPTIONS_FULL_PROJECTION, 
				ComponentSubscription.FIELD_ACC_ID + "=? and "+ComponentSubscription.FIELD_BUDDY_ID + "=?",
				new String [] {Integer.toString(accountId),Integer.toString(buddyId)},null);

		ComponentSubscription cs;
 
		if (c != null) {
			try {
				if(c.getCount() > 0) {
					c.moveToFirst();
					do {
						cs = new ComponentSubscription(c);
						switch (cs.getStatus()) {
						// create new subscription
						case ComponentSubscription.STATUS_SELECTED:
							Log.d(THIS_FILE,"@zsub status is selected for component [ " +cs.getComponentName() + " ]");	
							createSubscription(cs);
							break; 
						case ComponentSubscription.STATUS_DESELECTED:
							Log.d(THIS_FILE,"@zsub status is selected for component [ " +cs.getComponentName() + " ]");
							cancelSubscription(cs);
						case ComponentSubscription.STATUS_FAILED:
						case ComponentSubscription.STATUS_PENDING:
							Log.d(THIS_FILE,"@zsub status is ELSE THAN selected");
							break;
							//						case ComponentSubscription.STATUS_SELECTED:
						default:
							break;
						}

					} while ( c.moveToNext() );
				}
			} catch (Exception e) {
				Log.e(THIS_FILE, "Error on looping over comp subscr", e);
			} 
		}
	}

	public void onSubscriptionStateChange(int buddyId, String componentId, String status) {
		Log.d(THIS_FILE, "@zsub Subscription state change for " + componentId + 
				", status is " + status);
		ContentValues cv = new ContentValues();
		if (status.equals("ACTIVE") || status.equals("ACCEPTED")) {
			cv.put(ComponentSubscription.FIELD_STATUS, ComponentSubscription.STATUS_ACTIVE);
		} else if (status.equals("PENDING") || status.equals("SENT")) { 
			cv.put(ComponentSubscription.FIELD_STATUS, ComponentSubscription.STATUS_PENDING);
		} else { 
			cv.put(ComponentSubscription.FIELD_STATUS, ComponentSubscription.STATUS_NONE);
		}
		mContext.getContentResolver().update(ComponentSubscription.COMPONENT_SUBSCRIPTIONS_URI, cv,
				ComponentSubscription.FIELD_BUDDY_ID + "=? and " + ComponentSubscription.FIELD_COMPONENT_ID + "=?",
				new String [] {Integer.toString(buddyId),componentId});
	}
	
	public boolean isSubscriptionToComponent(Set<Components> set, Components component) {
		if (set == null) {
			return false;
		}
		if (set.contains(component)) {
			return false;
		}
		return true;
	}

	/**
	 * If uri is not in Buddy List, SipService will automatically add it.
	 *  
	 * @param uri
	 * @param component
	 */
	public void createSubscription(ComponentSubscription cs) {
		mComponentService.getSipService().createComponentSubscription(cs);
	}

	public void cancelSubscription(ComponentSubscription cs) {
		mComponentService.getSipService().cancelComponentSubscription(cs);
	}

	public void onIncomingSubscription(int accId, String componentId, String fromUri) {
		AbstractComponent ac = ComponentManager.getInstance().getAbstractComponentById(componentId);
		if (ac != null) {
			ac.onActivateComponent();
		}
		else {
			//TODO
		}
	}

	public void cleanUp() {
		//TODO implement
	}
}

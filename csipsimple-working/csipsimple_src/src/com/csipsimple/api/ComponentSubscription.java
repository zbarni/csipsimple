/**
 * Copyright (C) 2010-2012 Regis Montoya (aka r3gis - www.r3gis.fr)
 * This file is part of CSipSimple.
 *
 *  CSipSimple is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  If you own a pjsip commercial license you can also redistribute it
 *  and/or modify it under the terms of the GNU Lesser General Public License
 *  as an android library.
 *
 *  CSipSimple is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with CSipSimple.  If not, see <http://www.gnu.org/licenses/>.
 *  
 *  This file and this file only is also released under Apache license as an API file
 */

package com.csipsimple.api;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;

/**
 * Holder for a sip message.<br/>
 * It allows to prepare / unpack content values of a SIP message.
 */
public class ComponentSubscription {

    public static final String FIELD_ID = "id";
    public static final String FIELD_ACC_ID = "accountnr";
    public static final String FIELD_BUDDY_ID = "buddynr";
    public static final String FIELD_BUDDY_URI = "buddyuri";
    public static final String FIELD_COMPONENT_ID = "componentnr";
    public static final String FIELD_STATUS = "status";
    public static final String FIELD_COMPONENT_NAME = "componentname";
    public static final String COMPONENT_SUBSCRIPTIONS_TABLE_NAME = "component_subscriptions";

    public final static String COMPONENT_SUBSCRIPTIONS_CONTENT_TYPE = SipManager.BASE_DIR_TYPE + ".component_subscriptions";
    public final static String COMPONENT_SUBSCRIPTIONS_ITEM_TYPE = SipManager.BASE_ITEM_TYPE + ".component_subscriptions";
    
    public static final Uri COMPONENT_SUBSCRIPTIONS_URI = Uri.parse(ContentResolver.SCHEME_CONTENT + "://"
            + SipManager.AUTHORITY + "/" + COMPONENT_SUBSCRIPTIONS_TABLE_NAME);
    public static final Uri COMPONENT_SUBSCRIPTION_ID_URI_BASE = Uri.parse(ContentResolver.SCHEME_CONTENT + "://"
            + SipManager.AUTHORITY + "/" + COMPONENT_SUBSCRIPTIONS_TABLE_NAME + "/");
    
    public static final Uri COMPONENT_SUBSCRIPTION_GROUP_URI_BASE = Uri.parse(ContentResolver.SCHEME_CONTENT + "://"
            + SipManager.AUTHORITY + "/" + COMPONENT_SUBSCRIPTIONS_TABLE_NAME + "/group");

    public static final int STATUS_FAILED = 2;
    public static final int STATUS_ACTIVE = 1;
    // when buddy was created or subscription was terminated
    public static final int STATUS_NONE = -1;
    // when the user select new components, subscription must be created
    public static final int STATUS_SELECTED = 3;
    // when the user select new components, subscription must be terminated
    public static final int STATUS_DESELECTED = 5;
    // SUBSCRIBE has been sent to server
    public static final int STATUS_PENDING = 4;
//    public static final int STATUS_ = 4;
    
    public static final String SELF = "SELF";

    private int accId;
    private int buddyId;
    private String buddyUri;
    private String componentId;
    private String componentName;
    private int status = STATUS_NONE;

    /**
     * Construct from raw datas.
     * 
     * @param aFullFrom {@link #FIELD_FROM_FULL}
     */
    public ComponentSubscription(int aAccId, int aBuddyId, String aBuddyUri, String aComponentId) {
    	accId = aAccId;
    	buddyId = aBuddyId;
    	buddyUri = aBuddyUri;
    	componentId = aComponentId;
    }

    public ComponentSubscription(Cursor c) {
        ContentValues args = new ContentValues();
        DatabaseUtils.cursorRowToContentValues(c, args);
        createFromContentValue(args);
    }

    public String getComponentName() {
		return componentName;
	}

	public void setComponentName(String componentName) {
		this.componentName = componentName;
	}

	/**
     * Pack the object content value to store
     * 
     * @return The content value representing the message
     */
    public ContentValues getContentValues() {
        ContentValues cv = new ContentValues();
        cv.put(FIELD_ACC_ID, accId);
        cv.put(FIELD_BUDDY_ID, buddyId);
        cv.put(FIELD_BUDDY_URI, buddyUri);
        cv.put(FIELD_COMPONENT_ID, componentId);
        cv.put(FIELD_COMPONENT_NAME, componentName);
        cv.put(FIELD_STATUS, status);
        return cv;
    }
    
    public final void createFromContentValue(ContentValues args) {
        Integer tmp_i;
        String tmp_s;
        
        tmp_i = args.getAsInteger(FIELD_ACC_ID);
        if(tmp_i != null) {
            accId = tmp_i;
        }
        tmp_i = args.getAsInteger(FIELD_BUDDY_ID);
        if(tmp_i != null) {
            buddyId = tmp_i;
        }
        tmp_s = args.getAsString(FIELD_BUDDY_URI);
        if(tmp_s != null) {
            buddyUri = tmp_s;
        }
        tmp_s = args.getAsString(FIELD_COMPONENT_ID);
        if(tmp_s != null) {
            componentId = tmp_s;
        }
        tmp_s = args.getAsString(FIELD_COMPONENT_NAME);
        if(tmp_s != null) {
            componentName = tmp_s;
        }
        tmp_i = args.getAsInteger(FIELD_STATUS);
        if(tmp_i != null) {
            status = tmp_i;
        }
    }

	public int getAccId() {
		return accId;
	}

	public void setAccId(int accId) {
		this.accId = accId;
	}

	public int getBuddyId() {
		return buddyId;
	}

	public ComponentSubscription(String componentName) {
		super();
		this.componentName = componentName;
	}

	public void setBuddyId(int buddyId) {
		this.buddyId = buddyId;
	}

	public String getBuddyUri() {
		return buddyUri;
	}

	public void setBuddyUri(String buddyUri) {
		this.buddyUri = buddyUri;
	}

	public String getComponentId() {
		return componentId;
	}

	public void setComponentId(String componentId) {
		this.componentId = componentId;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}
}
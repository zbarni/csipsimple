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
 */

package com.csipsimple.ui.favorites;

import java.util.ArrayList;
import java.util.List;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.BaseColumns;
import android.provider.ContactsContract.Contacts;
import android.support.v4.widget.ResourceCursorAdapter;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.internal.utils.UtilityWrapper;
import com.actionbarsherlock.internal.view.menu.ActionMenuPresenter;
import com.actionbarsherlock.internal.view.menu.ActionMenuView;
import com.actionbarsherlock.internal.view.menu.MenuBuilder;
import com.actionbarsherlock.internal.view.menu.MenuBuilder.Callback;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.csipsimple.R;
import com.csipsimple.api.ComponentSubscription;
import com.csipsimple.api.ISipService;
import com.csipsimple.api.SipManager;
import com.csipsimple.api.SipProfile;
import com.csipsimple.api.SipUri;
import com.csipsimple.components.AbstractComponent;
import com.csipsimple.components.ComponentManager;
import com.csipsimple.components.ComponentProfile;
import com.csipsimple.db.DBProvider;
import com.csipsimple.models.Filter;
import com.csipsimple.service.SipService;
import com.csipsimple.utils.ContactsAsyncHelper;
import com.csipsimple.utils.Log;
import com.csipsimple.utils.contacts.ContactsWrapper;
import com.csipsimple.utils.contacts.ContactsWrapper.ContactInfo;
import com.csipsimple.utils.contacts.ContactsWrapper.Phone;
import com.csipsimple.widgets.contactbadge.QuickContactBadge;
import com.csipsimple.wizards.WizardUtils;

import java.util.List;

public class FavAdapter extends ResourceCursorAdapter implements OnClickListener {

	private Context mContext = null;

    private static final String THIS_FILE = "FavAdapter";
    

    /** Listener for the primary action in the list, opens the call details. */
    private final View.OnClickListener mPrimaryActionListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            ContactInfo ci = (ContactInfo) view.getTag();
            Intent it = ContactsWrapper.getInstance().getViewContactIntent(ci.contactId);
            mContext.startActivity(it);
        }
    };
    /** Listener for the secondary action in the list, either call or play. */
    private final View.OnClickListener mSecondaryActionListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            ContactInfo ci = (ContactInfo) view.getTag();
            List<String> phones = ContactsWrapper.getInstance().getCSipPhonesContact(mContext, ci.contactId);
            boolean useCSip = true;
            String toCall = null;
            if(phones != null && phones.size() > 0) {
                toCall = phones.get(0);
            }else {
                List<Phone> cPhones = ContactsWrapper.getInstance().getPhoneNumbers(mContext, ci.contactId, ContactsWrapper.URI_ALLS);
                if(cPhones != null && cPhones.size() > 0) {
                    toCall = cPhones.get(0).getNumber();
                    useCSip = false;
                }
            }
            
            if(!TextUtils.isEmpty(toCall) ) {
                Cursor c = (Cursor) getItem((Integer) ci.userData);
                Long profileId = null;
                while(c.moveToPrevious()) {
                    int cTypeIdx = c.getColumnIndex(ContactsWrapper.FIELD_TYPE);
                    int cAccIdx = c.getColumnIndex(BaseColumns._ID);
                    if(cTypeIdx >= 0 && cAccIdx >= 0) {
                        if(c.getInt(cTypeIdx) == ContactsWrapper.TYPE_GROUP) {
                            profileId = c.getLong(cAccIdx);
                            break;
                        }
                    }
                }
                
                Intent it = new Intent(Intent.ACTION_CALL);
                it.setData(SipUri.forgeSipUri(useCSip ? SipManager.PROTOCOL_CSIP : SipManager.PROTOCOL_SIP, toCall));
                it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if(profileId != null) {
                    it.putExtra(SipProfile.FIELD_ACC_ID, profileId);
                }
                mContext.startActivity(it);
            }
        }
    };
    
    public FavAdapter(Context context, Cursor c) {
        super(context, R.layout.fav_list_item, c, 0);
		mContext = context;
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        ContentValues cv = new ContentValues();
        DatabaseUtils.cursorRowToContentValues(cursor, cv);
        
        int type = ContactsWrapper.TYPE_CONTACT;
        if(cv.containsKey(ContactsWrapper.FIELD_TYPE)) {
            type = cv.getAsInteger(ContactsWrapper.FIELD_TYPE);
        }

        showViewForType(view, type);
        
        
        if(type == ContactsWrapper.TYPE_GROUP) {
            // Get views
            TextView tv = (TextView) view.findViewById(R.id.header_text);
            ImageView icon = (ImageView) view.findViewById(R.id.header_icon);
            PresenceStatusSpinner presSpinner = (PresenceStatusSpinner) view.findViewById(R.id.header_presence_spinner);
            
            // Get datas
            SipProfile acc = new SipProfile(cursor);
            
            final Long profileId = cv.getAsLong(BaseColumns._ID);
            final String groupName = acc.android_group;
            final String displayName = acc.display_name;
            final String wizard = acc.wizard;
            final boolean publishedEnabled = (acc.publish_enabled == 1);
            final String domain = acc.getDefaultDomain();
            
            // Bind datas to view
            tv.setText(displayName);
            icon.setImageResource(WizardUtils.getWizardIconRes(wizard));
            presSpinner.setProfileId(profileId);
            
            // Extra menu view if not already set
            ViewGroup menuViewWrapper = (ViewGroup) view.findViewById(R.id.header_cfg_spinner);
            
            MenuCallback newMcb = new MenuCallback(context, profileId, groupName, domain, publishedEnabled);
            MenuBuilder menuBuilder;
            if(menuViewWrapper.getTag() == null) {

                final LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT,
                        LayoutParams.MATCH_PARENT);

                ActionMenuPresenter mActionMenuPresenter = new ActionMenuPresenter(mContext);
                mActionMenuPresenter.setReserveOverflow(true);
                menuBuilder = new MenuBuilder(context);
                menuBuilder.setCallback(newMcb);
                MenuInflater inflater = new MenuInflater(context);
                inflater.inflate(R.menu.fav_menu, menuBuilder);
                menuBuilder.addMenuPresenter(mActionMenuPresenter);
                ActionMenuView menuView = (ActionMenuView) mActionMenuPresenter.getMenuView(menuViewWrapper);
                UtilityWrapper.getInstance().setBackgroundDrawable(menuView, null);
                menuViewWrapper.addView(menuView, layoutParams);
                menuViewWrapper.setTag(menuBuilder);
            }else {
                menuBuilder = (MenuBuilder) menuViewWrapper.getTag();
                menuBuilder.setCallback(newMcb);
            }
            menuBuilder.findItem(R.id.share_presence).setTitle(publishedEnabled ? R.string.deactivate_presence_sharing : R.string.activate_presence_sharing);
            menuBuilder.findItem(R.id.set_sip_data).setVisible(!TextUtils.isEmpty(groupName));
            
        }else if(type == ContactsWrapper.TYPE_CONTACT) {
            ContactInfo ci = ContactsWrapper.getInstance().getContactInfo(context, cursor);
            ci.userData = cursor.getPosition();
            // Get views
            TextView tv = (TextView) view.findViewById(R.id.contact_name);
            QuickContactBadge badge = (QuickContactBadge) view.findViewById(R.id.quick_contact_photo);
            TextView statusText = (TextView) view.findViewById(R.id.status_text);
            ImageView statusImage = (ImageView) view.findViewById(R.id.status_icon);

            // Bind
            if(ci.contactId != null) {
                tv.setText(ci.displayName);
                badge.assignContactUri(ci.callerInfo.contactContentUri);
                ContactsAsyncHelper.updateImageViewWithContactPhotoAsync(context, badge.getImageView(),
                        ci.callerInfo,
                        R.drawable.ic_contact_picture_holo_dark);
    
                statusText.setVisibility(ci.hasPresence ? View.VISIBLE : View.GONE);
                statusText.setText(ci.status);
                statusImage.setVisibility(ci.hasPresence ? View.VISIBLE : View.GONE);
                statusImage.setImageResource(ContactsWrapper.getInstance().getPresenceIconResourceId(ci.presence));
            }
            View v;
            v = view.findViewById(R.id.contact_view);
            v.setTag(ci);
            v.setOnClickListener(mPrimaryActionListener);
            v = view.findViewById(R.id.secondary_action_icon);
            v.setTag(ci);
            v.setOnClickListener(mSecondaryActionListener);
        } else if (type == ContactsWrapper.TYPE_CONFIGURE) {
            // We only bind if it's the correct type
            View v = view.findViewById(R.id.configure_view);
            v.setOnClickListener(this);
            ConfigureObj cfg = new ConfigureObj();
            cfg.profileId = cv.getAsLong(BaseColumns._ID);
            v.setTag(cfg);
        }
    }
    
    private class ConfigureObj extends Object {
        Long profileId = SipProfile.INVALID_ID;
        String groupName = "";
    }
    
    private void showViewForType(View view, int type) {
        
        view.findViewById(R.id.header_view).setVisibility((type == ContactsWrapper.TYPE_GROUP) ? View.VISIBLE : View.GONE);
        view.findViewById(R.id.contact_view).setVisibility((type == ContactsWrapper.TYPE_CONTACT) ? View.VISIBLE : View.GONE);
        view.findViewById(R.id.configure_view).setVisibility((type == ContactsWrapper.TYPE_CONFIGURE) ? View.VISIBLE : View.GONE);
    }
    
    private class MenuCallback implements Callback {
        private Long profileId = SipProfile.INVALID_ID;
        private Context context;
        private String groupName;
        private String domain;
        private boolean publishEnabled;
        
        public MenuCallback(Context ctxt, Long aProfileId, String aGroupName, String aDomain, boolean aPublishedEnabled) {
            profileId = aProfileId;
            context = ctxt;
            groupName = aGroupName;
            domain = aDomain;
            publishEnabled = aPublishedEnabled;
        }
        
        @Override
        public void onMenuModeChange(MenuBuilder menu) {
            // Nothing to do
        }
        
        @Override
        public boolean onMenuItemSelected(MenuBuilder menu, MenuItem item) {
            int itemId = item.getItemId();
            if(itemId == R.id.set_group) {
                showDialogForGroupSelection(context, profileId, groupName);
                return true;
            }else if(itemId == R.id.share_presence) {
                ContentValues cv = new ContentValues();
                cv.put(SipProfile.FIELD_PUBLISH_ENABLED, publishEnabled ? 0 : 1);
                context.getContentResolver().update(ContentUris.withAppendedId(SipProfile.ACCOUNT_ID_URI_BASE, profileId), cv, null, null);
                return true;
            }else if(itemId == R.id.set_sip_data) {
                showDialogForSipData(context, profileId, groupName, domain);
                return true;
            }
			//@zajzi
			else if (itemId == R.id.share_component_presence) {
				try {
					//        			cService.getPublicationManager().publish();
				}
				catch (Exception e) {
					Log.e(THIS_FILE, "@XML exception while trying to publish");
				}
			}
			else if (itemId == R.id.manage_component_subscriptions) {
				showDialogForBuddySubscription(context, profileId);
				return true;
			}
            return false;
        }
    }

	private void showDialogForBuddySubscription(final Context context, final Long profileId) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		
		builder.setTitle("Select buddy");
		List<String> buddies = new ArrayList<String>();
		
		final Cursor c = context.getContentResolver().query(ComponentSubscription.COMPONENT_SUBSCRIPTION_GROUP_URI_BASE,
				DBProvider.COMPONENT_SUBSCRIPTIONS_FULL_PROJECTION, null, null, null);
		if (c != null) {
			try {
				if(c.getCount() > 0) {
					c.moveToFirst();
					do {
						String s = (new ComponentSubscription(c)).getBuddyUri();
							buddies.add(s);
					} while ( c.moveToNext() );
				}
			} catch (Exception e) {
				Log.e(THIS_FILE, "Error on looping over comp subscr", e);
			} 
		}
		final CharSequence[] buddyItems = buddies.toArray(new CharSequence[buddies.size()]);
		final List<String> finalBuddies = buddies;
		final EditText input = new EditText(context);
		builder.setView(input);
		builder.setItems(buddyItems, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            		c.moveToPosition(which);
            		int bid = (new ComponentSubscription(c)).getBuddyId();
            		if (c != null) {
            			c.close();
            		}
            		showDialogForSubscriptionManagement(context,profileId,bid);
            }
		});
		
		builder.setCancelable(true);
		builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
			  	String buddyUri = input.getText().toString();
			  	// check if buddy exists
			  	if (finalBuddies.contains((String) buddyUri)) {
			  		Log.d(THIS_FILE, "Budddy already saved");
			  	}
			  	else {
			  		int buddyId = -1;
			  		try {
						buddyId = service.addBuddyForOutgoingSubscription(profileId.intValue(),buddyUri);
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			  		if (buddyId == -1) {
			  			Log.e(THIS_FILE, "@zbuddy Invalid buddy URI");
			  		}
			  		Log.d(THIS_FILE, "@zbuddy new buddy id is " + buddyId);
			  		for (ComponentProfile.Components comp : ComponentProfile.Components.values()) {
			  			String id = null;
			  			String name = null;
						try {
							id = service.getComponentId(comp.getValue());
							name = service.getComponentName(comp.getValue());
						} catch (Exception e) {
							// TODO: handle exception
						}
			  			if (id == null || name == null)
			  				continue;
			  			
			  			ContentValues cv = new ContentValues();
				  		cv.put(ComponentSubscription.FIELD_ACC_ID, profileId.intValue());
				  		cv.put(ComponentSubscription.FIELD_BUDDY_ID, buddyId);
				  		cv.put(ComponentSubscription.FIELD_BUDDY_URI, buddyUri);
				  		cv.put(ComponentSubscription.FIELD_COMPONENT_ID, id);
				  		cv.put(ComponentSubscription.FIELD_COMPONENT_NAME, name);
				  		cv.put(ComponentSubscription.FIELD_STATUS, ComponentSubscription.STATUS_NONE);
				  		context.getContentResolver().insert(ComponentSubscription.COMPONENT_SUBSCRIPTIONS_URI, cv);
			  		}
			  		showDialogForSubscriptionManagement(context, profileId,buddyId);
			  	}
			  	if (c != null) {
        			c.close();
        		}
				dialog.dismiss();
			}
		});
		builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (c != null) {
        			c.close();
        		}
				dialog.dismiss();
			}
		});
		builder.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				if (c != null) {
        			c.close();
        		}
			}
		});
		
		final Dialog dialog = builder.create();
		dialog.show();
	}
	

	private void showDialogForSubscriptionManagement(final Context context, final Long profileId, final int buddyId) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		final Cursor c = context.getContentResolver().query(ComponentSubscription.COMPONENT_SUBSCRIPTIONS_URI,
				new String [] {ComponentSubscription.FIELD_ID + " AS _id",
				ComponentSubscription.FIELD_BUDDY_URI,
				ComponentSubscription.FIELD_COMPONENT_ID,
				ComponentSubscription.FIELD_COMPONENT_NAME,
				ComponentSubscription.FIELD_STATUS
				}, 
				ComponentSubscription.FIELD_ACC_ID + "=? and "+ComponentSubscription.FIELD_BUDDY_ID + "=?",
				new String [] {Long.toString(profileId),Integer.toString(buddyId)},null);
		ComponentSubscription cs = null;		
		if (c != null) {
			try {
				if(c.getCount() > 0) {
					c.moveToFirst();
					cs = new ComponentSubscription(c);
				}
			} catch (Exception e) {
				Log.e(THIS_FILE, "Error on looping over comp subscr", e);
			} 
		}
		else {
			Log.e(THIS_FILE, "Cursor is null, buddy + account not found");
			return;
		}
		final String buddyUri = cs.getBuddyUri();
		builder.setTitle("Subscriptions to " + cs.getBuddyUri());
		
		builder.setMultiChoiceItems(c, ComponentSubscription.FIELD_STATUS, ComponentSubscription.FIELD_COMPONENT_NAME, new DialogInterface.OnMultiChoiceClickListener() {
			//          @Override
			public void onClick(DialogInterface dialog, int which, boolean isChecked) {
				c.moveToPosition(which);
				ContentValues cv = new ContentValues();
				int rowId = c.getInt(c.getColumnIndex("_id")); 
				if (isChecked) {
					cv.put(ComponentSubscription.FIELD_STATUS,ComponentSubscription.STATUS_SELECTED);
					context.getContentResolver().update(ContentUris.withAppendedId(ComponentSubscription.COMPONENT_SUBSCRIPTION_ID_URI_BASE, rowId), cv, null, null);
				}
				else {
					cv.put(ComponentSubscription.FIELD_STATUS,ComponentSubscription.STATUS_DESELECTED);
					context.getContentResolver().update(ContentUris.withAppendedId(ComponentSubscription.COMPONENT_SUBSCRIPTION_ID_URI_BASE, rowId), cv, null, null);
				}
			}
		});

		builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				try {
					service.onSubscriptionUpdate(profileId.intValue(),buddyId);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (c != null) {
        			c.close();
        		}
				dialog.dismiss();
			}
		});
		builder.setNeutralButton("Delete buddy", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				context.getContentResolver().delete(ComponentSubscription.COMPONENT_SUBSCRIPTIONS_URI,
						ComponentSubscription.FIELD_BUDDY_URI + "=?", new String [] {buddyUri});
				if (c != null) {
        			c.close();
        		}
				try {
					service.removeBuddyForOutgoingSubscription(profileId.intValue(),buddyUri);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
				Toast.makeText(context, "Buddy deleted", Toast.LENGTH_LONG).show();
				dialog.dismiss();
			}
		});
		
		builder.setCancelable(true);
		builder.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				if (c != null) {
        			c.close();
        		}
			}
		});
		
		final Dialog dialog = builder.create();
		dialog.show();
	}
    
    private void showDialogForGroupSelection(final Context context, final Long profileId, final String groupName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.set_android_group);
        final Cursor choiceCursor = ContactsWrapper.getInstance().getGroups(context);
        int selectedIndex = -1;
        if(choiceCursor != null) {
            if (choiceCursor.moveToFirst()) {
                int i = 0;
                int colIdx = choiceCursor.getColumnIndex(ContactsWrapper.FIELD_GROUP_NAME);
                do {
                    String name = choiceCursor.getString(colIdx);
                    if(!TextUtils.isEmpty(name) && name.equalsIgnoreCase(groupName)) {
                        selectedIndex = i;
                        break;
                    }
                    i ++;
                } while (choiceCursor.moveToNext());
            }
        }
        builder.setSingleChoiceItems(choiceCursor, selectedIndex, ContactsWrapper.FIELD_GROUP_NAME, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(choiceCursor != null) {
                    choiceCursor.moveToPosition(which);
                    String name = choiceCursor.getString(choiceCursor.getColumnIndex(ContactsWrapper.FIELD_GROUP_NAME));
                    ContentValues cv = new ContentValues();
                    cv.put(SipProfile.FIELD_ANDROID_GROUP, name);
                    context.getContentResolver().update(ContentUris.withAppendedId(SipProfile.ACCOUNT_ID_URI_BASE, profileId), cv, null, null);
                    choiceCursor.close();
                }
                dialog.dismiss();
            }
        });
        
        builder.setCancelable(true);
        builder.setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(choiceCursor != null) {
                    choiceCursor.close();
                }
                dialog.dismiss();
            }
        });
        builder.setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if(choiceCursor != null) {
                    choiceCursor.close();
                }
            }
        });
        final Dialog dialog = builder.create();
        dialog.show();
    }
    
    private void showDialogForSipData(final Context context, final Long profileId, final String groupName, final String domain) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.set_android_group);
        builder.setCancelable(true);
        builder.setItems(R.array.sip_data_sources, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                applyNumbersToCSip(groupName, 1 << which, domain, profileId);
            }
        });
        
        final Dialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if(id == R.id.configure_view) {
            ConfigureObj cfg = (ConfigureObj) v.getTag();
            showDialogForGroupSelection(mContext, cfg.profileId, cfg.groupName);
        }
    }
    
    private void applyNumbersToCSip(String groupName, int flag, String domain, long profileId) {
        Log.d(THIS_FILE, "Apply numbers to csip " + groupName + " > " + domain);
        ContactsWrapper cw = ContactsWrapper.getInstance();
        Cursor c = cw.getContactsByGroup(mContext, groupName);
        try {
            while (c.moveToNext()) {
                long contactId = c.getLong(c.getColumnIndex(Contacts._ID));
                List<Phone> phones = cw.getPhoneNumbers(mContext, contactId, flag);
                if(phones.size() > 0){
                    String nbr = phones.get(0).getNumber();
                    if(!nbr.contains("@")){
                        if(flag == ContactsWrapper.URI_NBR) {
                            // Apply rewriting rules
                            nbr = Filter.rewritePhoneNumber(mContext, profileId, nbr);
                        }
                        nbr += "@" + domain;
                    }
                    Log.d(THIS_FILE, "Apply number to " + contactId + " > " + nbr);
                    cw.insertOrUpdateCSipUri(mContext, contactId, nbr);
                }
            }
        } catch (Exception e) {
            Log.e(THIS_FILE, "Error while looping on contacts", e);
        } finally {
            c.close();
        }
    }
	public void bindToService() {
		mContext.bindService(new Intent(mContext,SipService.class), connection, Context.BIND_AUTO_CREATE);
	}
	
	public void unBindFromService() {
		mContext.unbindService(connection);
		service = null;
	}
	
	public ISipService getService() {
		return service;
	}
	
	   // Service connection
    private ISipService service;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            service = ISipService.Stub.asInterface(arg1);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            service = null;
        }
    };
}


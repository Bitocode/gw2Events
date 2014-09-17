package com.firelink.gw2.objects;

import android.graphics.drawable.BitmapDrawable;


public class EventHolder
{
	public static final String PREFS_NAME 			= "GW2EventReminderPreferences";
    public static final String PREFS_SERVER_ID 		= "SelectedServerID";
    public static final String PREFS_SERVER_NAME 	= "SelectedServerName";
    
    public String name;
    public String eventID;
    public String type;
    public String description;
    public String waypoint;
    public BitmapDrawable image;
    public String imagePath;
    public int level;
    public int typeID;
    public boolean isActive;
}

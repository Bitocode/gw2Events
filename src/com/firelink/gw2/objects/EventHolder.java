package com.firelink.gw2.objects;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import android.graphics.drawable.BitmapDrawable;


public class EventHolder implements Cloneable
{
    //Variables
    public String name;
    public String eventID;
    public String type;
    public String description;
    public String waypoint;
    public String imageName;
    public Date[] startTimes;
    public Date startTime;
    public String countdownTimer;
    public BitmapDrawable image;
    public int level;
    public int typeID;
    public boolean isActive;
    
    @Override
    public EventHolder clone() throws CloneNotSupportedException 
    {
    	return (EventHolder)super.clone();
    }
    
    /**
     * 
     * @param date
     * @return
     */
    public static Date convertDateToLocal(String date)
    {
    	Date utcDate;
    	Date localDate;
    	
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.US);
	    	TimeZone utcTZ   = TimeZone.getTimeZone("UTC");
	    	TimeZone localTZ = TimeZone.getDefault();
	    	
	    	//Parse UTC times
			sdf.setTimeZone(utcTZ);
			utcDate = sdf.parse(date);
			
			//Change to local timezone somewhere
			sdf.setTimeZone(localTZ);
			//Add offset from DST, if there is one
			localDate = sdf.parse(sdf.format(utcDate) + 
					((localTZ.inDaylightTime(new Date()) ? localTZ.getDSTSavings() : 0) / 1000));
		} catch (ParseException e) {
			localDate = null;
		}
    	
    	return localDate;
    }
    
    /**
     * 
     * @param date
     * @return
     */
    public static String formatDateToTime(Date date)
    {
    	SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.US);
    	
    	return sdf.format(date);
    }
    
    /**
     * 
     * @param date
     * @return
     */
    public static String formatForCountdown(Date date)
    {
    	SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.US);
    	
    	return sdf.format(date);
    }
}

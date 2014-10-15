package com.firelink.gw2.objects;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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
    public Date[] endTimes;
    public Date startTime;
    public Date endTime;
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
     * @param dates
     * @return The index to the date closest to now
     */
    public static int getClosestDate(Date[] dates, Date currDate)
    {
    	long min = dates[0].getTime() - currDate.getTime();
    	long maxDate = dates[0].getTime(); 
		int startIndex = 0;
		boolean nextDay = false;
		
		for (int i = 0; i < dates.length; i++) {
			
			if (dates[i].getTime() >= maxDate) {
				maxDate = dates[i].getTime();
			} else {
				nextDay = true;
			}
			
			if (nextDay) {
				Calendar calendar = Calendar.getInstance();
    			calendar.setTime(dates[i]);
    			calendar.add(Calendar.DAY_OF_YEAR, 1);
    			dates[i] = calendar.getTime();
			}
			
			long diff = dates[i].getTime() - currDate.getTime();
			
			if (diff < 0) {
				continue;
			}
			
			if (min > diff || min < 0) {
				min = diff;
				startIndex = i;
			}
		}
		
		return startIndex;
    	
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

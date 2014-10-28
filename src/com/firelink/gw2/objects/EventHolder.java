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
    public long timeUntilNextStart;
    public long timeUntilNextEnd;
    public String countdownTimer;
    public BitmapDrawable image;
    public int level;
    public int typeID;
    public int indexOffset;
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
    public static int getClosestDate(Date[] dates, Date currDate, int indexOffset)
    {
    	long min = dates[0].getTime() - currDate.getTime();
    	long maxDate = dates[0].getTime(); 
		int startIndex = 0;
		boolean nextDay = false;
		
		for (int i = 0; i < dates.length; i++) {
			
			if (dates[i].getTime() == currDate.getTime()){
				continue; 
			}
			
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
			
			if (min > diff || min <= 0) {
				min = diff;
				startIndex = i;
			}
		}
		
		if (indexOffset + startIndex > dates.length - 1) {
			startIndex = (indexOffset + startIndex) - dates.length;
		} else {
			startIndex += indexOffset;
		}
		
		return startIndex;
    	
    }
    
    /**
     * 
     * @param dates
     * @return The index to the date closest to now
     */
    public static int getClosestEventDates(Date[] startDates, Date[] endDates, Date currDate)
    {
    	long min = startDates[0].getTime() - currDate.getTime();
    	long maxDate = startDates[0].getTime(); 
		int startIndex = 0;
		
		for (int i = 0; i < startDates.length; i++) {
			
			if (startDates[i].getTime() == currDate.getTime()){
				continue; 
			}
			
			if (startDates[i].getTime() >= maxDate) {
				maxDate = startDates[i].getTime();
			}
			
			long diff = startDates[i].getTime() - currDate.getTime();
			
			if (diff < startDates[i].getTime() - endDates[i].getTime()) {
				continue;
			}
			
			if (min > diff || min <= startDates[i].getTime() - endDates[i].getTime()) {
				min = diff;
				startIndex = i;
			}
		}
		
		return startIndex;
    	
    }
    
    /**
     * 
     * @param holder
     * @param currentTime
     * @return
     */
    public static EventHolder parseDates(EventHolder holder, Date currentTime)
    {
    	Date[] startDates = holder.startTimes;
    	Date[] endDates = holder.endTimes;
    	
    	Calendar currentCal = Calendar.getInstance();
    	currentCal.setTime(currentTime);
    	
    	for (int i = 0; i < startDates.length; i++)
    	{
    		Calendar startCal = Calendar.getInstance();
    		Calendar endCal   = Calendar.getInstance();
    		
    		startCal.setTime(startDates[i]);
    		startCal.set(Calendar.YEAR, currentCal.get(Calendar.YEAR));
    		startCal.set(Calendar.DAY_OF_YEAR, currentCal.get(Calendar.DAY_OF_YEAR));
    		
    		endCal.setTime(endDates[i]);
    		endCal.set(Calendar.YEAR, currentCal.get(Calendar.YEAR));
    		endCal.set(Calendar.DAY_OF_YEAR, currentCal.get(Calendar.DAY_OF_YEAR));
    		
    		if (startCal.getTimeInMillis() < currentCal.getTimeInMillis() && endCal.getTimeInMillis() < currentCal.getTimeInMillis()) {
    			startCal.roll(Calendar.DAY_OF_YEAR, 1);
    			endCal.roll(Calendar.DAY_OF_YEAR, 1);
    		} else if (startCal.getTimeInMillis() > currentCal.getTimeInMillis() && endCal.getTimeInMillis() < currentCal.getTimeInMillis()) {
    			endCal.roll(Calendar.DAY_OF_YEAR, 1);
    		}
    		
    		holder.startTimes[i] = startCal.getTime();
    		holder.endTimes[i] = endCal.getTime();
    	}
    	
    	return holder;
    }
    
    /**
     * 
     * @param startTime
     * @param endTime
     * @param currentTime
     * @return
     */
    public static boolean isEventActive(Date startTime, Date endTime, Date currentTime)
    {
    	//Make our dates into Calendars
    	Calendar startCal = Calendar.getInstance();
    	Calendar endCal   = Calendar.getInstance();
    	Calendar currCal  = Calendar.getInstance();
    	startCal.setTime(startTime);
    	endCal.setTime(endTime);
    	currCal.setTime(currentTime);
    	
    	if (currCal.getTimeInMillis() >= startCal.getTimeInMillis() && currCal.getTimeInMillis() < endCal.getTimeInMillis()) {
    		return true;
    	} else if (currCal.getTimeInMillis() < endCal.getTimeInMillis() && endCal.getTimeInMillis() < startCal.getTimeInMillis()) {
    		return true;
    	}
    	
    	return false;
    }
    
    /**
     * 
     * @param dates
     * @return The index to the date closest to now
     */
//    public static int isDateWithinHours(Date date, Date currDate, long hours)
//    {
//    	long min = date.getTime() - currDate.getTime();
//    	long maxDate = date.getTime(); 
//		int startIndex = 0;
//		boolean nextDay = false;
//		
//		for (int i = 0; i < dates.length; i++) {
//			
//			if (dates[i].getTime() == currDate.getTime()){
//				continue; 
//			}
//			
//			if (dates[i].getTime() >= maxDate) {
//				maxDate = dates[i].getTime();
//			} else {
//				nextDay = true;
//			}
//			
//			if (nextDay) {
//				Calendar calendar = Calendar.getInstance();
//    			calendar.setTime(dates[i]);
//    			calendar.add(Calendar.DAY_OF_YEAR, 1);
//    			dates[i] = calendar.getTime();
//			}
//			
//			long diff = dates[i].getTime() - currDate.getTime();
//			
//			if (diff < 0) {
//				continue;
//			}
//			
//			if (min > diff || min <= 0) {
//				min = diff;
//				startIndex = i;
//			}
//		}
//		
//		return startIndex;
//    	
//    }
    
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

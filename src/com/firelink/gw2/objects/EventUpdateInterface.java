package com.firelink.gw2.objects;

import java.util.Date;

public interface EventUpdateInterface 
{	
	/**
	 * 
	 * @param eventID
	 * @param index
	 */
	public abstract EventHolder updateStartAndEndTimes(EventHolder holder, Date date);
}

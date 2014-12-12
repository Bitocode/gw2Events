package com.firelink.gw2.objects;

public interface RefreshInterface 
{
	/**
     * Should this activity refresh upon reopening?
     */
	public abstract boolean isRefreshOnOpen();
	/**
     * Refreshes the data
     */
	public abstract void refresh();
}

package com.firelink.gw2.objects;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;

import org.json.JSONObject;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

public class EventCacher 
{	
	final private static String MEDIA_CACHE_DIR = "media";
	final private static String EVENTS_TABLE_NAME = "tblEvents";
	
	final public static String EVENTS_CACHE_DIR = "eventsMedia";
	
	private Context context;
	final private String cachePath;
	
	public HashMap<String, BitmapDrawable> images;
	
	public EventCacher(Context context)
	{
		this.context = context;
		
		images = new HashMap<String, BitmapDrawable>();
		
		//Check if media is mounted
    	cachePath =
    			Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) || 
    				!Environment.isExternalStorageRemovable() ? this.context.getExternalCacheDir().getPath() :
    					this.context.getCacheDir().getPath();
	}

	public String getMediaCachePath()
    {
    	return cachePath + File.separator + MEDIA_CACHE_DIR + File.separator;
    }
	
	public void cacheRemoteMedia(String source, String path, String fileName)
	{
		File tempFile = new File(getMediaCachePath() + path, fileName);
		
		if (tempFile.exists()) {
			return;
		}
		
		new CacheMediaTask().execute(source, path, fileName);
	}
	
	public void cacheEventsAPI(JSONObject json)
	{
		
	}
	
	
	
	private class CacheMediaTask extends AsyncTask<String, Void, Void>
    {
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String...params)
        {
        	String URL = params[0];
        	String path = params[1];
        	String fileName = params[2];
        	
        	File dir = new File(getMediaCachePath() + path);
    		dir.mkdirs();
    		dir = null;
    		
    		File file = new File(getMediaCachePath() + path, fileName);
    		
    		if (file.exists()) {
    			return null;
    		}
    		
    		byte[] buffer    = new byte[1024];
    		int bufferLength = 0;
    		
    		Log.d("GW2Events", "Caching " + fileName + " to " + path);
    		
        	try {
        		FileOutputStream fileOutput = new FileOutputStream(file);
        		
	        	URL aURL = new URL(URL);
	            URLConnection conn = aURL.openConnection();
	            conn.connect();
	            InputStream is = conn.getInputStream();
	            
	            while ((bufferLength = is.read(buffer)) > 0) {
	            	fileOutput.write(buffer, 0, bufferLength);
	            }
	            
	            fileOutput.close();
	            is.close();
	            
        	} catch (IOException e) {
        		Log.d("GW2Events", e.getMessage());
        	}
        	
        	
			return null;
        }
    }
}

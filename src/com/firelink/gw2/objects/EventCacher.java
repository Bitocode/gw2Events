package com.firelink.gw2.objects;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

/**
 * 
 * @author Justin
 *
 */
public class EventCacher 
{	
	//SQL
	public static final String SQL_TABLE_EVENT_NAMES   = "tblEventName";
	public static final String SQL_TABLE_MAP 	       = "tblMapName";
	public static final String SQL_TABLE_EVENT_DETAILS = "tblEventDetails";
	public static final String SQL_TABLE_EVENTS        = "tblEvents";
	//Preferences
	public static final String PREFS_NAME        = "GW2EventReminderPreferences";
    public static final String PREFS_SERVER_ID 	 = "SelectedServerID";
    public static final String PREFS_SERVER_NAME = "SelectedServerName";
	//Cache
	public static final String CACHE_MEDIA_DIR = "media";
	public static final String CACHE_APIS_DIR  = "apis";
	
	private Context context;
	final private String cachePath;
	
	public HashMap<String, BitmapDrawable> images;
	
	/**
	 * 
	 * @param context
	 */
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
	
	/**
	 * 
	 * @return String
	 */
	public String getCachePath()
    {
    	return cachePath + File.separator;
    }
	
	/**
	 * 
	 * @param source
	 * @param path
	 * @param fileName
	 */
	public void cacheRemoteMedia(String source, String path, String fileName)
	{
		File tempFile = new File(getCachePath() + path, fileName);
		
		if (tempFile.exists()) {
			return;
		}
		
		new CacheMediaTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, source, path, fileName);
	}
	
	/**
	 * 
	 * @param json
	 */
	public void cacheEventsAPI(String json, String path, String fileName)
	{
		File dir = new File(getCachePath() + path);
		dir.mkdirs();
		dir = null;
		
		File file = new File(getCachePath() + path, fileName);
		
		try {
    		FileOutputStream fileOutput = new FileOutputStream(file);
    		Log.d("GW2Events", "Caching " + fileName + " to " + path);
    		fileOutput.write(json.getBytes());
            fileOutput.close();
            
    	} catch (IOException e) {
    		Log.d("GW2Events", e.getMessage());
    	}
				
		
	}
	
	
	/**
	 * 
	 * @author Justin
	 *
	 */
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
        	
        	File dir = new File(getCachePath() + path);
    		dir.mkdirs();
    		dir = null;
    		
    		File file = new File(getCachePath() + path, fileName);
    		
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

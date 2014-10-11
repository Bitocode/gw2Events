package com.firelink.gw2.objects;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.Date;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
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
	private String cachePath;
	
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
		try {
			cachePath = Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) || 
    				!Environment.isExternalStorageRemovable() ? 
    					this.context.getExternalCacheDir().getPath() :
    					this.context.getCacheDir().getPath();
		} catch (Exception e) {
			cachePath = this.context.getCacheDir().getPath();
		}
    	
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
	 * @param forSub
	 * @return
	 */
	public static HashMap<String, EventHolder> getCachedEventNames(Context context)
    {
		HashMap<String, EventHolder> events = new HashMap<String, EventHolder>();
    	SQLHelper sqlHelper = new SQLHelper(context);
        SQLiteDatabase sqlRead = sqlHelper.getReadableDatabase();
        
		Cursor sqlCursor = sqlRead.query(SQLHelper.TABLE_NAME_EVENT, null, null, null, null, null, null);
		
		while (sqlCursor.moveToNext()) {
			String name        = sqlCursor.getString(sqlCursor.getColumnIndex("eventName"));
			String eventID     = sqlCursor.getString(sqlCursor.getColumnIndex("eventID"));
			String description = sqlCursor.getString(sqlCursor.getColumnIndex("eventDescription"));
			String eventType   = sqlCursor.getString(sqlCursor.getColumnIndex("eventType"));
			int typeID         = sqlCursor.getInt(sqlCursor.getColumnIndex("typeID"));
			
			EventHolder tempHolder = new EventHolder();
			tempHolder.name = name;
			tempHolder.eventID = eventID;
			tempHolder.description = description;
			tempHolder.type = eventType;
			tempHolder.typeID = typeID;
			
			events.put(eventID, tempHolder);
		}
		
		sqlHelper.close();
		sqlRead.close();
		sqlCursor.close();
		
		return events;
    }
	
	/**
	 * 
	 * @param eventID
	 * @return String[]
	 */
	public static EventHolder getEventCache(Context context, String eventID)
	{
		EventHolder eventHolder = new EventHolder();
		eventHolder.eventID = eventID;
		
		EventCacher ec = new EventCacher(context);
		File cacheFile = new File(ec.getCachePath() + File.separator + EventCacher.CACHE_APIS_DIR + File.separator + eventHolder.eventID);
		String json = "";
		
		if (cacheFile.exists())
		{
			try {
				BufferedReader br = new BufferedReader(new FileReader(cacheFile));
				StringBuilder buffer = new StringBuilder();
				String line = "";
				
				while ((line = br.readLine()) != null)
				{
					buffer.append(line);
				}
				
				json = buffer.toString();
				br.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			
			try
	        {
	        	JSONObject eventObject = new JSONObject(json);
	            
	        	eventHolder.description = URLDecoder.decode(eventObject.getString("description"), "UTF-8");
	            eventHolder.imageName   = URLDecoder.decode(eventObject.getString("imageFileName"), "UTF-8");
	            eventHolder.name        = URLDecoder.decode(eventObject.getString("name"), "UTF-8");
	            eventHolder.type        = URLDecoder.decode(eventObject.getString("event_class_name"), "UTF-8");
	            eventHolder.typeID      = eventObject.getInt("event_class_id");
	            
	            //Get the image
	            EventCacher tempCacher = new EventCacher(context);
	    		File tempFile          = new File(tempCacher.getCachePath() + EventCacher.CACHE_MEDIA_DIR, eventHolder.imageName);
	        	eventHolder.image      = new BitmapDrawable(context.getResources(), BitmapFactory.decodeFile(tempFile.getAbsolutePath()));
	        	
	        	//Figure out this time BS
	        	JSONArray timeArray = eventObject.getJSONArray("start_times");
	        	
	        	eventHolder.startTimes = new Date[timeArray.length()];
	        	for (int i = 0; i < timeArray.length(); i++)
	        	{
	        		eventHolder.startTimes[i] = EventHolder.convertDateToLocal(timeArray.getString(i));
	        	}
	        }
	        catch (JSONException e)
	        {
	            Log.d("GW2Events", e.getMessage() + ": " + json);
	        } catch (UnsupportedEncodingException e) {
				Log.d("GW2Events", e.getMessage());
			}
		}
		
		return eventHolder;
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

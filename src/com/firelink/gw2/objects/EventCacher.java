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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

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
	
	public boolean checkCache()
	{
		//return if there is a valid cache
		return false;
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
	 * @param fields
	 * @return
	 */
	public EventHolder getEventJSONCache(EventHolder fields)
	{
		File cacheFile = new File(getCachePath() + File.separator + EventCacher.CACHE_APIS_DIR + File.separator + fields.eventID);
		String json = "";
		
		Date currentTime = new Date();
		
		try {
			SimpleDateFormat sd = new SimpleDateFormat("hh:mm:ss a", Locale.US);
    		currentTime = sd.parse(sd.format(Calendar.getInstance().getTime()));
		} catch (ParseException e) {}
		
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
	        	
	        	if (fields.description == null) {
	        		fields.description = URLDecoder.decode(eventObject.getString("description"), "UTF-8");
	        	}
	        	if (fields.imageName == null) {
	        		fields.imageName   = URLDecoder.decode(eventObject.getString("imageFileName"), "UTF-8");
	        	}
	        	if (fields.name == null) {
	        		fields.name        = URLDecoder.decode(eventObject.getString("name"), "UTF-8");
	        	}
	            if (fields.type == null) {
	            	fields.type        = URLDecoder.decode(eventObject.getString("event_class_name"), "UTF-8");
	            }
	            if (fields.typeID == 0) {
	            	fields.typeID      = eventObject.getInt("event_class_id");
	            }
	            
	        	//Figure out this time BS
	    		if (fields.startTimes == null) {
	    			JSONArray timeStartArray = eventObject.getJSONArray("start_times");
	        	
	    			fields.startTimes = new Date[timeStartArray.length()];
		        	for (int i = 0; i < timeStartArray.length(); i++)
		        	{
		        		fields.startTimes[i] = EventHolder.convertDateToLocal(timeStartArray.getString(i));
		        	}
	    		}
	        	
	    		fields.startTime = fields.startTimes[EventHolder.getClosestDate(fields.startTimes, currentTime)];
	        	
	        	//Figure out this time BS
	    		if (fields.endTimes == null) {
	    			JSONArray timeEndArray = eventObject.getJSONArray("end_times");
	        	
	    			fields.endTimes = new Date[timeEndArray.length()];
		        	for (int i = 0; i < timeEndArray.length(); i++)
		        	{
		        		fields.endTimes[i] = EventHolder.convertDateToLocal(timeEndArray.getString(i));
		        	}
	    		}
	    		
	    		fields.endTime = fields.endTimes[EventHolder.getClosestDate(fields.endTimes, currentTime)];
	        	
	        }
	        catch (JSONException e)
	        {
	            Log.d("GW2Events", e.getMessage() + ": " + json);
	        } catch (UnsupportedEncodingException e) {
				Log.d("GW2Events", e.getMessage());
			}
		} else {
			fields = null;
		}
		
		return fields;
	}
	
	/**
	 * 
	 * @param imageName
	 * @return
	 */
	public BitmapDrawable getCachedImage(String imageName)
	{
		//Get the image
		BitmapDrawable bd = null;
		
		try {
			File tempFile     = new File(getCachePath() + EventCacher.CACHE_MEDIA_DIR, imageName);
        	
	        bd = new BitmapDrawable(context.getResources(), BitmapFactory.decodeFile(tempFile.getAbsolutePath()));
		} catch(Exception e) {
			Log.d("GW2Events", e.getMessage());
		}
		
		return bd;
        
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
	 * @param eventID
	 */
	public void cacheEventDetails(String eventID, TaskCompletedInterface theInterface) {
		Log.d("GW2Events", "Caching " + eventID);
		new EventDetailsAPI(theInterface).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, eventID);
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
     * This is the class for making our API call to retrieve the event contents.
     */
    public class EventDetailsAPI extends AsyncTask<String, Void, String>
    {
    	private TaskCompletedInterface listener;
    	private boolean isError;
    	
    	public EventDetailsAPI(TaskCompletedInterface listener)
    	{
    		if (listener != null) {
    			this.listener = listener;
    		}
    		
    		isError = false;
    	}
    	
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String...params)
        {
            String result = "";
            APICaller api = new APICaller();

            api.setAPI(APICaller.API_EVENT_DETAILS);
            api.setLanguage(APICaller.LANG_ENGLISH);
            api.setEventID(params[0]);

            if (api.callAPI()) {
                result = api.getJSONString();
            } else {
                result = api.getLastError();
            }

            Log.d("GW2Events", result + "");
            
            try
            {
            	JSONObject eventObject = new JSONObject(result);
            	
            	eventObject = eventObject.getJSONObject("events").getJSONObject(params[0]);
            	
            	String imagePath = URLDecoder.decode(eventObject.getString("imagePath"), "UTF-8");
            	String imageFileName = URLDecoder.decode(eventObject.getString("imageFileName"), "UTF-8");
                
            	cacheRemoteMedia(imagePath + imageFileName, EventCacher.CACHE_MEDIA_DIR, imageFileName);
            	cacheEventsAPI(eventObject.toString(), EventCacher.CACHE_APIS_DIR, params[0]);
            	
            	BitmapDrawable test = getCachedImage(imageFileName);
            	
            	while (test.getBitmap() == null) {
            		test = getCachedImage(imageFileName);
            	}
            }
            catch (JSONException e)
            {
                Log.d("GW2Events", e.getMessage());
                result  = "Unable to contact server. Try again later";
                isError = true;
            } catch (UnsupportedEncodingException e) {
				Log.d("GW2Events", e.getMessage());
				result  = "Unable to contact server. Try again later";
				isError = true;
			}

            return result;
        }

        @Override
        public void onPostExecute(String result)
        {
        	if (listener != null) {
        		if (isError) {
        			listener.onTaskCompletedWIthErrors(result);
        		} else {
        			listener.onTaskCompleted();
        		}
        	}
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

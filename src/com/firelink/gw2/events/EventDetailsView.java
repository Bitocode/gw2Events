package com.firelink.gw2.events;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.firelink.gw2.objects.APICaller;
import com.firelink.gw2.objects.EventCacher;
import com.firelink.gw2.objects.EventHolder;

public class EventDetailsView extends Activity 
{
	protected TextView logoTextView;
	protected TextView descriptionTextView;
	protected TextView startTimesTextView;
	protected ImageView eventImageView;
	
	protected EventHolder eventHolder;
	
	protected ProgressDialog eventDetailsDialog;
	protected Activity activity;
	protected Context context;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.event_details_layout);
		
		activity = this;
		context  = this;
		
		eventHolder        = new EventHolder();
		eventDetailsDialog = new ProgressDialog(activity);
		
		Bundle bundle = getIntent().getExtras();
		
		eventHolder.eventID = bundle.getString("eventID");
		
		logoTextView        = (TextView)findViewById(R.id.menuBar_logoTextView);
		descriptionTextView = (TextView)findViewById(R.id.eventDetailsView_descriptionTextView);
		startTimesTextView  = (TextView)findViewById(R.id.eventDetailsView_startTimesTextView);
		eventImageView      = (ImageView)findViewById(R.id.eventDetailsView_eventImageView);
		
		parseCache();
		
		//new EventDetailsAPI().execute();
	}
	
	@Override
	public void onBackPressed() 
	{
		super.onBackPressed();
		
		finish();
		overridePendingTransition(0, android.R.anim.fade_out);
	}
	
	private void parseCache()
	{
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
	            
	        	eventHolder.description = URLDecoder.decode(eventObject.getString("description"));
	            eventHolder.imageName   = URLDecoder.decode(eventObject.getString("imageFileName"));
	            eventHolder.name        = URLDecoder.decode(eventObject.getString("name"));
	            
	            EventCacher tempCacher = new EventCacher(context);
	    		File tempFile          = new File(tempCacher.getCachePath() + EventCacher.CACHE_MEDIA_DIR, eventHolder.imageName);
	        	eventHolder.image      = new BitmapDrawable(BitmapFactory.decodeFile(tempFile.getAbsolutePath()));
	        	
	        	logoTextView.setText(eventHolder.name);
	        	eventImageView.setImageDrawable(eventHolder.image);
	        	descriptionTextView.setText(eventHolder.description);
	        	
	        	JSONArray timeArray = eventObject.getJSONArray("start_times");
	        	
	        	SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.US);
	        	TimeZone utcTZ   = TimeZone.getTimeZone("UTC");
	        	TimeZone localTZ = TimeZone.getDefault();
	        	
	        	eventHolder.startTimes = new Date[timeArray.length()];
	        	String startTimes = "";
	        	for (int i = 0; i < timeArray.length(); i++)
	        	{
	        		//Parse UTC times
	        		sdf.setTimeZone(utcTZ);
	        		eventHolder.startTimes[i] = sdf.parse(timeArray.getString(i));
	        		//Change to local timezone somewhere
	        		sdf.setTimeZone(localTZ);
	        		//Add offset from DST, if there is one
	        		eventHolder.startTimes[i] = sdf.parse(sdf.format(eventHolder.startTimes[i]) + 
	        				((localTZ.inDaylightTime(new Date()) ? localTZ.getDSTSavings() : 0) / 1000));
	        		
	        		//Print it
	        		startTimes = startTimes.concat(sdf.format(eventHolder.startTimes[i]) + "\n");

	        		Log.d("GW2Events", i + ": " + sdf.format(eventHolder.startTimes[i]));
	        	}
	        	
	        	startTimesTextView.setText(startTimes);
	        }
	        catch (JSONException e)
	        {
	            Log.d("GW2Events", e.getMessage() + ": " + json);
	        } catch (ParseException e) {
	        	Log.d("GW2Events", e.getMessage());
			}
		}
	}
	
	 /**
     * This is the class for making our API call to retrieve the event contents.
     */
    public class EventDetailsAPI extends AsyncTask<Void, Void, String>
    {
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            
            eventDetailsDialog.setMessage("Retrieving event details...");
            eventDetailsDialog.setIndeterminate(false);
            eventDetailsDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            eventDetailsDialog.setCancelable(false);
            eventDetailsDialog.show();
        }

        @Override
        protected String doInBackground(Void...params)
        {
            String result = "";
            APICaller api = new APICaller();

            api.setAPI(APICaller.API_EVENT_DETAILS);
            api.setLanguage(APICaller.LANG_ENGLISH);
            api.setEventID(eventHolder.eventID);

            if (api.callAPI()) {
                result = api.getJSONString();
            } else {
                result = api.getLastError();
            }

            Log.d("GW2Events", result + "");
            
            try
            {
            	JSONObject eventObject = new JSONObject(result);
            	eventObject = eventObject.getJSONObject("events").getJSONObject(eventHolder.eventID);
                
            	eventHolder.description = URLDecoder.decode(eventObject.getString("description"));
                eventHolder.imageName = URLDecoder.decode(eventObject.getString("imageFileName"));
                
                EventCacher tempCacher = new EventCacher(context);
        		File tempFile = new File(tempCacher.getCachePath() + EventCacher.CACHE_MEDIA_DIR, eventHolder.imageName);
            	eventHolder.image = new BitmapDrawable(BitmapFactory.decodeFile(tempFile.getAbsolutePath()));
            }
            catch (JSONException e)
            {
                Log.d("GW2Events", e.getMessage());
            }

            return result;
        }

        @Override
        public void onPostExecute(String result)
        {
        	descriptionTextView.setText(eventHolder.description);
            eventImageView.setImageDrawable(eventHolder.image);
            
            eventDetailsDialog.dismiss();
        }
    }
}

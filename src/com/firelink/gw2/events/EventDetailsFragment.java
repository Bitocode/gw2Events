package com.firelink.gw2.events;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
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
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firelink.gw2.objects.APICaller;
import com.firelink.gw2.objects.ChildFragmentInterface;
import com.firelink.gw2.objects.EventCacher;
import com.firelink.gw2.objects.EventHolder;

public class EventDetailsFragment extends Fragment
{
	private SharedPreferences sharedPrefs;
	private SharedPreferences.Editor sharedPrefsEditor;
	private ChildFragmentInterface childFragInto;
	
	protected TextView descriptionTextView;
	protected TextView startTimesTextView;
	protected TextView[] headersTextView;
	protected ImageView eventImageView;
	
	protected EventHolder eventHolder;
	
	protected ProgressDialog eventDetailsDialog;
	protected Activity activity;
	protected Context context;
	
	public EventDetailsFragment(){}
	
	
	@Override
	public void onAttach(Activity activity) 
	{
		super.onAttach(activity);
		
		try {
			childFragInto = (ChildFragmentInterface) activity;
		} catch (ClassCastException e) {
			
		}
	}
	/**
	 * 
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		setHasOptionsMenu(true);
		
		sharedPrefs 		= getActivity().getSharedPreferences(EventCacher.PREFS_NAME, 0);
		sharedPrefsEditor 	= sharedPrefs.edit();
		sharedPrefsEditor.commit();		
	}
	
	/**
	 * 
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) 
	{
		View view = inflater.inflate(R.layout.event_details_layout, container, false);
		
		activity = getActivity();
		context  = getActivity().getApplicationContext();
		
		//Set ActionBar stuff
		activity.getActionBar().setDisplayShowTitleEnabled(true);
		activity.getActionBar().setDisplayHomeAsUpEnabled(true);
		
		
		eventHolder        = new EventHolder();
		eventDetailsDialog = new ProgressDialog(activity);
		
		Bundle bundle = getArguments();
		
		eventHolder.eventID = bundle.getString("eventID");
		
		headersTextView = new TextView[]{
			(TextView)view.findViewById(R.id.eventDetailsView_descriptionHeaderTextView),
			(TextView)view.findViewById(R.id.eventDetailsView_startTimesHeaderTextView),
		};
		
		descriptionTextView = (TextView)view.findViewById(R.id.eventDetailsView_descriptionTextView);
		startTimesTextView  = (TextView)view.findViewById(R.id.eventDetailsView_startTimesTextView);
		eventImageView      = (ImageView)view.findViewById(R.id.eventDetailsView_eventImageView);
		
		parseCache();
		return view;
	}
	
	/**
	 * 
	 */
	@Override
	public void onStart() 
	{
		super.onStart();
		
		activity.getActionBar().setTitle("Event Details");
		activity.getActionBar().setSubtitle(eventHolder.name);
	}
	
	/**
	 * 
	 */
	@Override
	public void onPause() 
	{
		super.onPause();
		activity.overridePendingTransition(0, android.R.anim.fade_out);
	}
	
	@Override
	public void onDetach() 
	{
		super.onDetach();
		//refreshInterface.refresh();
		childFragInto.refreshOnUpdate();
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) 
	{
		inflater.inflate(R.menu.event_details_actions, menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
		switch (item.getItemId()) {
			case R.id.eventDetails_action_subscribe:
				toggleSubscribe();
				setSubscribeIcon(item);
				break;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onPrepareOptionsMenu(Menu menu) 
	{
		super.onPrepareOptionsMenu(menu);
		
		for (int i = 0; i < menu.size(); i++) {
			MenuItem item = menu.getItem(i);
			if (item.getItemId() == R.id.eventDetails_action_subscribe) {
				setSubscribeIcon(item);
			}
		}
	}
	
	/**
	 * 
	 * @param item
	 */
	private void toggleSubscribe()
	{
		//Get list of subscribed users
		int isSub = sharedPrefs.getInt(eventHolder.eventID, 0);
		
		if (isSub == 1) {
			sharedPrefsEditor.putInt(eventHolder.eventID, 0);
		} else {
			sharedPrefsEditor.putInt(eventHolder.eventID, 1);
		}
		
		sharedPrefsEditor.apply();
	}
	
	private void setSubscribeIcon(MenuItem item)
	{
		//Get list of subscribed users
		int isSub = sharedPrefs.getInt(eventHolder.eventID, 0);
		
		if (isSub == 1) {
			item.setIcon(getResources().getDrawable(R.drawable.ic_action_important));
		} else {
			item.setIcon(getResources().getDrawable(R.drawable.ic_action_not_important));
		}
	}
	
	/**
	 * 
	 */
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
	        	
	        	//Set our views
	        	//Determine which color to add to the eventClass left bar thing
	            int eventColor;
	            switch(eventHolder.typeID)
	            {
	                case 1:
	                	eventColor      = R.color.gw_event_level_high;
	                    break;
	                case 2:
	                	eventColor      = R.color.gw_event_level_standard;
	                    break;
	                case 3:
	                	eventColor      = R.color.gw_event_level_low;
	                    break;
	                default:
	                	eventColor      = R.color.gw_event_level_standard;
	                    break;
	            }
	            
	            for(int i = 0; i < headersTextView.length; i++) {
	            	headersTextView[i].setBackgroundColor(context.getResources().getColor(eventColor));
	            }
	            
	        	eventImageView.setImageDrawable(eventHolder.image);
	        	descriptionTextView.setText(eventHolder.description);
	        	startTimesTextView.setText(startTimes);
	        }
	        catch (JSONException e)
	        {
	            Log.d("GW2Events", e.getMessage() + ": " + json);
	        } catch (ParseException e) {
	        	Log.d("GW2Events", e.getMessage());
			} catch (UnsupportedEncodingException e) {
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
                
            	eventHolder.description = URLDecoder.decode(eventObject.getString("description"), "UTF-8");
                eventHolder.imageName = URLDecoder.decode(eventObject.getString("imageFileName"), "UTF-8");
                
                EventCacher tempCacher = new EventCacher(context);
        		File tempFile = new File(tempCacher.getCachePath() + EventCacher.CACHE_MEDIA_DIR, eventHolder.imageName);
            	eventHolder.image = new BitmapDrawable(context.getResources(), BitmapFactory.decodeFile(tempFile.getAbsolutePath()));
            }
            catch (JSONException e)
            {
                Log.d("GW2Events", e.getMessage());
            } catch (UnsupportedEncodingException e) {
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

package com.firelink.gw2.events;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Iterator;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.firelink.gw2.objects.APICaller;
import com.firelink.gw2.objects.EventAdapter;
import com.firelink.gw2.objects.EventCacher;
import com.firelink.gw2.objects.EventHolder;
import com.firelink.gw2.objects.RefreshInterface;
import com.firelink.gw2.objects.SQLHelper;

public class EventNamesFragment extends Fragment implements RefreshInterface
{
    public static final int INTENT_SERVER_SELECTOR_REQUEST_CODE = 143;

    protected Activity activity;
    protected Context context;
    protected Fragment fragment;

    protected ListView eventListView;
    protected ProgressDialog eventProgDialog;
    protected ActionBar actionBar;

    protected int serverID;
    protected String serverName;
    protected EventAdapter eventAdapter;

    public EventNamesFragment(){}
    
    @Override
    public void onAttach(Activity activity) 
    {
    	super.onAttach(activity);
    }
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        //Set actionbar stuff
        actionBar = getActivity().getActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        
        activity = getActivity();
        context  = getActivity().getApplicationContext();
        fragment = this;
        
        //
        SharedPreferences sharedPrefs = activity.getSharedPreferences(EventCacher.PREFS_NAME, 0);

        serverID   = sharedPrefs.getInt(EventCacher.PREFS_SERVER_ID, 0);
        serverName = sharedPrefs.getString(EventCacher.PREFS_SERVER_NAME, "Pizza");
    }
    
    /**
     * 
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
    		Bundle savedInstanceState) 
    {
    	View view = inflater.inflate(R.layout.event_names_layout, container, false);

        eventListView  = (ListView)view.findViewById(R.id.eventNamesView_eventListView);
        eventListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        eventListView.setOnItemClickListener(eventSelectAdapterView);
        
        if(serverID != 0){
            initEventView();
        } else {
        	//This is first run. Cache!
        	new DataCacher().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            new SQLDataCacher().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            
            Intent intent = new Intent(activity, WorldView.class);
            startActivityForResult(intent, INTENT_SERVER_SELECTOR_REQUEST_CODE);
        }
    	
        return view;
    }

    /**
     * 
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(requestCode == INTENT_SERVER_SELECTOR_REQUEST_CODE){
            if(resultCode == Activity.RESULT_OK){

                serverID 	= data.getIntExtra("serverID", 0);
                serverName 	= data.getStringExtra("serverName");

                initEventView();
            }
        }
    }
    
    @Override
    public boolean isRefreshOnOpen() 
    {
    	return false;
    }
    
    /**
     * 
     */
    @Override
    public void refresh()
    {
        //Fix server name. Depends on size of the name
        setServerName();

    	eventAdapter = new EventAdapter(context);
    	
    	for(Entry<String, EventHolder> entry : EventCacher.getCachedEventNames(context).entrySet()) {
    		EventHolder tempHolder = entry.getValue();
    		
    		eventAdapter.add(tempHolder);
    	}
        eventListView.setAdapter(eventAdapter);
    }
    
    /**
     * Initiates the events view
     * 
     * @return void
     */
    private void initEventView()
    {
        //Fix server name. Depends on size of the name
        setServerName();

        if (eventAdapter == null) {
        	refresh();
        } else {
        	eventListView.setAdapter(eventAdapter);
        }
    }
    
    /**
     * Adjusts the server name depending on the size of the name
     * 
     * @return void
     */
    private void setServerName()
    {
        actionBar.setTitle("All Events");
        actionBar.setSubtitle(serverName);
    }
    
    /**
     * 
     */
    AdapterView.OnItemClickListener eventSelectAdapterView = new AdapterView.OnItemClickListener()
    {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        {
            Bundle bundle = new Bundle();
            EventHolder tempEvent = eventAdapter.getItem(position);

            bundle.putString("eventID", tempEvent.eventID);
            bundle.putString("eventName", tempEvent.name);

            Fragment childFragment = new EventDetailsFragment();
            childFragment.setArguments(bundle);
            childFragment.setTargetFragment(fragment, 0);
            ((HomeLayout)getActivity()).selectDetailItem(childFragment);
        }
    };
    
    /**
	 * This caches our background data that we might use in the future
	 */
	public class SQLDataCacher extends AsyncTask<Void, Void, Boolean>
	{
		@Override
		protected Boolean doInBackground(Void... params) {
			
			//TODO: Cache the map names, map tiles, map information, and anything else I can think of

			//Create our database tables
			String[] initialQueries = new String[1];
			initialQueries[0] = "CREATE TABLE " + SQLHelper.TABLE_NAME_EVENT + " ( eventID VARCHAR(255) PRIMARY KEY, eventName VARCHAR(255), eventDescription VARCHAR(900), eventType VARCHAR(900), typeID INTEGER ) ";
			
			SQLHelper sqlHelper	= new SQLHelper(context, initialQueries);
			
			//Retrieve database handlers
			SQLiteDatabase sqlWrite	= sqlHelper.getWritableDatabase();
			
			//Get our event name API data
			APICaller eventNameAPI = new APICaller();
			eventNameAPI.setAPI(APICaller.API_EVENT_NAMES);
			eventNameAPI.setLanguage(APICaller.LANG_ENGLISH);
			eventNameAPI.callAPI();
			String eventNameJSON = eventNameAPI.getJSONString();
			
			
			//Process the data and add it to our DB
			try {
				//Set our JSON arrays
				JSONArray eventNameJSONArray 	= new JSONArray(eventNameJSON);
				
				ContentValues eventNameCV 	= new ContentValues();
				
				for (int i = 0; i < eventNameJSONArray.length(); i++) {
					JSONObject jsonObject = eventNameJSONArray.getJSONObject(i);
					String name 		 = URLDecoder.decode(jsonObject.getString("short_name"), "UTF-8");
                    String description   = URLDecoder.decode(jsonObject.getString("name"), "UTF-8");
                    String eventID       = URLDecoder.decode(jsonObject.getString("id"), "UTF-8");
                    String eventType     = URLDecoder.decode(jsonObject.getString("event_class_name"), "UTF-8");
                    int typeID           = jsonObject.getInt("event_class_id");
					
					eventNameCV.put("eventID", eventID);
					eventNameCV.put("eventName", name);
					eventNameCV.put("eventDescription", description);
					eventNameCV.put("eventType", eventType);
					eventNameCV.put("typeID", typeID);
					
					try {
						sqlWrite.insertWithOnConflict(SQLHelper.TABLE_NAME_EVENT, null, eventNameCV, SQLiteDatabase.CONFLICT_IGNORE);
					} catch (SQLException e) {
						Log.e("GW2Events", e.getMessage());
					}
				}
			} catch (JSONException e) {
				Log.d("GW2Events", e.getMessage());
			} catch (UnsupportedEncodingException e) {
				Log.d("GW2Events", e.getMessage());
			}
			
			return true;
		}	
	}

    /**
	 * This caches our background data that we might use in the future
	 */
	public class DataCacher extends AsyncTask<Void, Void, String>
	{
		 @Override
	        protected void onPreExecute()
	        {
	            super.onPreExecute();
	        }

	        @Override
	        protected String doInBackground(Void...params)
	        {
	            String result = "";
	            APICaller api = new APICaller();

	            api.setAPI(APICaller.API_EVENT_DETAILS);
	            api.setLanguage(APICaller.LANG_ENGLISH);
	            api.setEventID("");

	            if (api.callAPI()) {
	                result = api.getJSONString();
	            } else {
	                result = api.getLastError();
	            }

	            Log.d("GW2Events", result + "");
	            
	            try
	            {
	                EventCacher dCH = new EventCacher(context);
	                
	                JSONObject eventsObject = new JSONObject(result);
	                eventsObject = eventsObject.getJSONObject("events");
	                Iterator<?> iterator = eventsObject.keys();
	                
	                while (iterator.hasNext()) 
	                {
	                	String key = iterator.next().toString();
	                	JSONObject eventObject = eventsObject.getJSONObject(key);
	                	String imagePath       = eventObject.getString("imagePath");
	                	String imageFileName   = eventObject.getString("imageFileName");
	                	
	                    dCH.cacheRemoteMedia(imagePath + imageFileName, EventCacher.CACHE_MEDIA_DIR, imageFileName);
	                    
	                    dCH.cacheEventsAPI(eventObject.toString(), EventCacher.CACHE_APIS_DIR, key);
	                }
	            }
	            catch (JSONException e)
	            {
	                Log.d("GW2Events", e.getMessage());
	            }

	            return result;
	        }
	}
}

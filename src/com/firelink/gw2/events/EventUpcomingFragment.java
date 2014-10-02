package com.firelink.gw2.events;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
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

public class EventUpcomingFragment extends Fragment implements RefreshInterface
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

    public EventUpcomingFragment(){}
    
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
        
        SharedPreferences sharedPrefs = activity.getSharedPreferences(EventCacher.PREFS_NAME, 0);

        serverID   = sharedPrefs.getInt(EventCacher.PREFS_SERVER_ID, 0);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
    		Bundle savedInstanceState) 
    {
    	View view = inflater.inflate(R.layout.event_upcoming_layout, container, false);

        eventListView  = (ListView)view.findViewById(R.id.eventUpcomingView_eventListView);
        eventListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        eventListView.setOnItemClickListener(eventSelectAdapterView);
        
        initEventView();
    	
        return view;
    }
    
    @Override
    public boolean isRefreshOnOpen() 
    {
    	return false;
    }
    
    @Override
	public void refresh() 
    {
    	setServerName();
    	
    	eventAdapter = new EventAdapter(context);
        new EventSelectAPI().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
        	eventAdapter = new EventAdapter(context);
            new EventSelectAPI().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
        actionBar.setTitle("Upcoming Events");
        actionBar.setSubtitle(null);
    }

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
            
            ((HomeLayout)getActivity()).selectDetailItem(childFragment);
        }
    };

    /**
     * This is the class for making our API call to retrieve the event contents.
     */
    public class EventSelectAPI extends AsyncTask<Void, Void, String>
    {
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();

            eventProgDialog = new ProgressDialog(activity);
            eventProgDialog.setMessage("Retrieving events...");
            eventProgDialog.setIndeterminate(false);
            eventProgDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            eventProgDialog.setCancelable(false);
            eventProgDialog.show();
        }

        @Override
        protected String doInBackground(Void...params)
        {
            String result = "";
            APICaller api = new APICaller();

            api.setAPI(APICaller.API_EVENTS);
            api.setWorld(serverID);

            if (api.callAPI()) {
                result = api.getJSONString();
            } else {
                result = api.getLastError();
            }

            Log.d("GW2Events", result + "");

            return result;
        }

        @Override
        public void onPostExecute(String result)
        {
            try
            {
                JSONArray json;
                json = new JSONArray(result);
                HashMap<String, EventHolder> tempMap = EventCacher.getCachedEventNames(context);
                

                for(int i = 0; i < json.length(); i++){
                    JSONObject jsonObject = json.getJSONObject(i);
                    String eventID       = URLDecoder.decode(jsonObject.getString("event_id"), "UTF-8");
                    int typeID           = jsonObject.getInt("event_class_id");

                    //Add to adapter at some point
                    eventAdapter.add(tempMap.get(eventID).name, tempMap.get(eventID).description, eventID, typeID);
                    
                    //dCH.cacheRemoteMedia(imagePath + imageFileName, EventCacher.EVENTS_CACHE_DIR, imageFileName);
                }
            }
            catch (JSONException e)
            {
            	Log.d("GW2Events", e.getMessage());
            } catch (UnsupportedEncodingException e) {
            	Log.d("GW2Events", e.getMessage());
			}

            //Reset adapter
            eventListView.setAdapter(null);
            eventListView.setAdapter(eventAdapter);

            eventProgDialog.dismiss();
        }
    }
}

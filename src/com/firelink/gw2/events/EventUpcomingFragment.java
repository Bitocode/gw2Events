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
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
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
    protected SwipeRefreshLayout refreshLayout;
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
        
        fragment.setRetainInstance(true);
        
        SharedPreferences sharedPrefs = activity.getSharedPreferences(EventCacher.PREFS_NAME, 0);

        serverID = sharedPrefs.getInt(EventCacher.PREFS_SERVER_ID, 0);
    }
    /**
     * 
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
    		Bundle savedInstanceState) 
    {
    	View view = inflater.inflate(R.layout.event_upcoming_layout, container, false);

        eventListView  = (ListView)view.findViewById(R.id.eventUpcomingView_eventListView);
        eventListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        eventListView.setOnItemClickListener(eventSelectAdapterView);
        
        refreshLayout = (SwipeRefreshLayout)view.findViewById(R.id.eventUpcoming_refreshLayout);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				refresh();
			}
		});
        refreshLayout.setColorSchemeResources(R.color.gw_red, R.color.black, R.color.gw_event_level_standard, R.color.gw_event_level_high);
        
        stopCountdown();
        initEventView();
    	
        return view;
    }
    /**
     * 
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) 
    {
    	super.onConfigurationChanged(newConfig);
    	
    	stopCountdown();
    }
    @Override
    public void onDetach() 
    {
    	super.onDetach();
    }
    /**
     * 
     */
    @Override
    public void onDestroy() 
    {
    	stopCountdown();
    	super.onDestroy();
    }
    /**
     * 
     */
    @Override
    public void onPause() 
    {
    	stopCountdown();
    	super.onPause();
    }
    /**
     * 
     */
    @Override
    public void onResume() 
    {
    	super.onResume();
    	startCountdown();
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
    	
    	stopCountdown();
    	eventAdapter = new EventAdapter(context);
    	eventAdapter.setInterface(getActivity());
        new EventSelectAPI().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}
    
    private void startCountdown()
    {
    	if (eventAdapter != null) {
    		eventAdapter.startCountdown();
    	}
    }
    
    private void stopCountdown()
    {
    	if (eventAdapter != null) {
    		eventAdapter.stopCountdown();
    	}
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
        	eventAdapter.setInterface(getActivity());
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
            refreshLayout.setRefreshing(true);
            
            stopCountdown();
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
                    String startTime     = URLDecoder.decode(jsonObject.getString("start_time"), "UTF-8");
                    String status        = URLDecoder.decode(jsonObject.getString("status"), "UTF-8");
                    boolean isActive     = status.equals("Active")? true : false;
                    
                    EventHolder temp = tempMap.get(eventID).clone();
                    
	        		temp.startTime = EventHolder.convertDateToLocal(startTime);
	        		temp.isActive       = isActive;
	        		 
                    //Add to adapter at some point
                    eventAdapter.add(temp);
                }
            }
            catch (JSONException e)
            {
            	Log.d("GW2Events", e.getMessage());
            } catch (UnsupportedEncodingException e) {
            	Log.d("GW2Events", e.getMessage());
			} catch (CloneNotSupportedException e) {
				Log.d("GW2Events", e.getMessage());
			}

            //Reset adapter
            eventListView.setAdapter(null);
            eventListView.setAdapter(eventAdapter);
            startCountdown();

            refreshLayout.setRefreshing(false);
        }
    }
}

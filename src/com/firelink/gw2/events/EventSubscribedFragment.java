package com.firelink.gw2.events;

import java.util.ArrayList;
import java.util.Map.Entry;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.firelink.gw2.objects.EventAdapter;
import com.firelink.gw2.objects.EventCacher;
import com.firelink.gw2.objects.EventHolder;
import com.firelink.gw2.objects.RefreshInterface;

public class EventSubscribedFragment extends Fragment implements RefreshInterface
{
	//High level fields
    protected Activity activity;
    protected Context context;
    protected Fragment fragment;
    //Views
    protected ListView eventListView;
    protected ProgressDialog eventProgDialog;
    protected ActionBar actionBar;
    //Custom data
    protected int serverID;
    protected String serverName;
    protected EventAdapter eventAdapter;
    protected EventCacher ec;
    
    /** Some empty constructor */
    public EventSubscribedFragment(){}
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        //Set the activity
        activity = getActivity();
        context  = getActivity().getApplicationContext();
        fragment = this;
        
        //Set ActionBar stuff
        actionBar = activity.getActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        
        //Preference
        SharedPreferences sharedPrefs = activity.getSharedPreferences(EventCacher.PREFS_NAME, 0);

        serverID   = sharedPrefs.getInt(EventCacher.PREFS_SERVER_ID, 0);
        serverName = sharedPrefs.getString(EventCacher.PREFS_SERVER_NAME, "Pizza");
        
        ec = new EventCacher(context);
        
        fragment.setRetainInstance(true);
    }
    
    /** Called when the view is inflated */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
    		Bundle savedInstanceState) 
    {
    	View view = inflater.inflate(R.layout.event_subscribed_layout, container, false);

        eventListView  = (ListView)view.findViewById(R.id.eventUpcomingView_eventListView);
        eventListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        eventListView.setOnItemClickListener(eventSelectAdapterView);
    	
        return view;
    }
    
    /** Called when the fragment resumes. */
    @Override
    public void onResume() 
    {
    	super.onResume();
    	
    	initEventView();
    }
    
    /** Called when the fragment pauses. */
    @Override
    public void onPause() 
    {
    	stopCountdown();
    	super.onPause();
    }
    /** Called when the fragment detaches. */
    @Override
    public void onDetach() 
    {
    	super.onDetach();
    }
    /** Called when the fragment is destroyed. */
    @Override
    public void onDestroy() 
    {
    	stopCountdown();
    	super.onDestroy();
    }
    
    /**
     * Should this activity refresh upon reopening?
     */
    @Override
    public boolean isRefreshOnOpen() 
    {
    	return true;
    }
    
    /**
     * Refreshes the data
     */
	@Override
    public void refresh()
    {
        setServerName();
        
        new DisplayData().execute();
    }
    
    /**
     * Initiates the CountDown sequence
     */
    private void startCountdown()
    {
    	if (eventAdapter != null) {
    		eventAdapter.startInfiniteCountdown();
    	}
    }
    
    /**
     * Stops the CountDown sequence
     */
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
        setServerName();

        if (eventAdapter == null) {
        	eventAdapter = new EventAdapter(context);
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
        actionBar.setTitle("Subscribed Events");
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
            childFragment.setTargetFragment(fragment, 0);
            ((HomeLayout)getActivity()).selectDetailItem(childFragment);
        }
    };
    
    /**
   	 * This caches our background data that we might use in the future
   	 */
	public class DisplayData extends AsyncTask<Void, Void, ArrayList<EventHolder>> 
	{
		@Override
		protected void onPreExecute() 
		{
			eventAdapter.stopCountdown();
			eventAdapter.empty();
		}

		@Override
		protected ArrayList<EventHolder> doInBackground(Void... params) 
		{
			SharedPreferences sharedPrefs = context.getSharedPreferences(
					EventCacher.PREFS_NAME, 0);
			
			ArrayList<EventHolder> results = new ArrayList<EventHolder>();
			
			for (Entry<String, EventHolder> entry : EventCacher.getCachedEventNames(context).entrySet()) {
				EventHolder tempHolder = entry.getValue();

				int check = sharedPrefs.getInt(tempHolder.eventID, 0);

				if (check == 1) {
					tempHolder = ec.getEventJSONCache(tempHolder);

					results.add(tempHolder);
				}
			}

			return results;
		}

		@Override
		protected void onPostExecute(ArrayList<EventHolder> result) 
		{
			for (EventHolder holder : result) {
				eventAdapter.add(holder);
			}
			
			startCountdown();
			eventListView.setAdapter(eventAdapter);
		}
	}
}

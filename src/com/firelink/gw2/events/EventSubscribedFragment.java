package com.firelink.gw2.events;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Map.Entry;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.firelink.gw2.objects.EventAdapter;
import com.firelink.gw2.objects.EventCacher;
import com.firelink.gw2.objects.EventHolder;
import com.firelink.gw2.objects.EventUpdateInterface;
import com.firelink.gw2.objects.RefreshInterface;
import com.firelink.gw2.objects.TaskCompletedInterface;

public class EventSubscribedFragment extends Fragment implements RefreshInterface, TaskCompletedInterface, EventUpdateInterface
{
	//High level fields
    protected Activity activity;
    protected Context context;
    protected Fragment fragment;
    //Views
    protected ListView eventListView;
    protected TextView errorTextView;
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
        
        eventProgDialog = new ProgressDialog(activity);
        
        fragment.setRetainInstance(true);
    }
    
    /** Called when the view is inflated */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
    		Bundle savedInstanceState) 
    {
    	View view = inflater.inflate(R.layout.event_subscribed_layout, container, false);

        eventListView  = (ListView)view.findViewById(R.id.eventSubscribedView_eventListView);
        eventListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        eventListView.setOnItemClickListener(eventSelectAdapterView);
        
        errorTextView = (TextView)view.findViewById(R.id.eventSubscribedView_errorView);
    	
        return view;
    }
    
    /***************************************************
     *************************************************** 
     *	Start of non-standard lifecycle activities
     ***************************************************
     ***************************************************/
    
    /** Called when the fragment resumes. */
    @Override
    public void onResume() 
    {
    	initEventView();
    	super.onResume();
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
    	stopCountdown();
    	super.onDetach();
    }
    /** Called when the fragment is destroyed. */
    @Override
    public void onDestroy() 
    {
    	stopCountdown();
    	super.onDestroy();
    }
    
    
    /***************************************************
     *************************************************** 
     *	Start of RefreshInterface methods
     ***************************************************
     ***************************************************/
    
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
	
	/***************************************************
     *************************************************** 
     *	Start of EventUpdateInterface methods
     ***************************************************
     ***************************************************/
	
	/**
	 *
	 * @param holder
	 * @param date
	 * @return EventHolder
	 */
	@Override
	public EventHolder updateStartAndEndTimes(EventHolder holder, Date date) 
	{
		holder = EventHolder.parseDates(holder, date);
		
		int timeIndex = EventHolder.getClosestEventDates(holder.startTimes, holder.endTimes, date);
		holder.startTime = holder.startTimes[timeIndex];
		holder.endTime = holder.endTimes[timeIndex];
		
		holder.timeUntilNextStart = holder.startTime.getTime() - date.getTime();
		holder.timeUntilNextEnd = holder.endTime.getTime() - date.getTime();
		
		return holder;
	}
	
	@Override
	public void eventFinished() 
	{
		eventAdapter.organizeEvents(null);
	}
    
	/***************************************************
     *************************************************** 
     *	Start of TaskCompletedInterface methods
     ***************************************************
     ***************************************************/
	@Override
	public void onTaskCompleted() 
	{
		refresh();
	}
	@Override
	public void onTaskCompletedWIthErrors(String message) 
	{
		errorTextView.setText(message);
		errorTextView.setVisibility(View.VISIBLE);
		
		View view = (View) errorTextView.getParent().getParent();
		
		errorTextView.setHeight(view.getHeight());
		errorTextView.setGravity(Gravity.CENTER);
		
		eventProgDialog.dismiss();
	}
	
	/***************************************************
     *************************************************** 
     *	Start of EventAdapter related methods
     ***************************************************
     ***************************************************/
    /**
     * Initiates the CountDown sequence
     */
    private void startCountdown()
    {
    	if (eventAdapter != null) {
    		eventAdapter.organizeEvents(null);
    		eventAdapter.startEventCountdown();
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
   
    /***************************************************
     *************************************************** 
     *	Start of fragment-specific methods
     ***************************************************
     ***************************************************/
    
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
        	eventAdapter.setEventUpdateInterface((EventUpdateInterface)fragment);
        	refresh();
        } else {
        	eventListView.setAdapter(eventAdapter);
        	startCountdown();
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
    
    
    /***************************************************
     *************************************************** 
     *	Start of Background tasks
     ***************************************************
     ***************************************************/
    
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
				EventHolder eventHolder = entry.getValue();

				int check = sharedPrefs.getInt(eventHolder.eventID, 0);

				if (check == 1) {
					EventHolder tempHolder = ec.getEventJSONCache(eventHolder);
					
					if (tempHolder == null) {
						ec.cacheEventDetails(eventHolder.eventID, (TaskCompletedInterface) fragment);
						cancel(true);
						break;
					}
					
					tempHolder.isActive = EventHolder.isEventActive(tempHolder.startTime, tempHolder.endTime, Calendar.getInstance().getTime());
					
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
			
			eventListView.setAdapter(eventAdapter);
			startCountdown();
			eventProgDialog.dismiss();
		}
		
		@Override
		protected void onCancelled() 
		{
			if (!eventProgDialog.isShowing()) {
				eventProgDialog.setMessage("Retrieving event details...");
				eventProgDialog.setIndeterminate(false);
				eventProgDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				eventProgDialog.setCancelable(false);
				eventProgDialog.show();
			}
		}
	}
}

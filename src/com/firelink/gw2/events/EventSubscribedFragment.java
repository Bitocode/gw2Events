package com.firelink.gw2.events;

import java.util.Map.Entry;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
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
    protected Activity activity;
    protected Context context;
    protected Fragment fragment;

    protected ListView eventListView;
    protected ProgressDialog eventProgDialog;
    protected ActionBar actionBar;
    protected SharedPreferences sharedPrefs;

    protected int serverID;
    protected String serverName;
    protected EventAdapter eventAdapter;

    public EventSubscribedFragment(){}
    
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
        sharedPrefs = activity.getSharedPreferences(EventCacher.PREFS_NAME, 0);

        serverID   = sharedPrefs.getInt(EventCacher.PREFS_SERVER_ID, 0);
        serverName = sharedPrefs.getString(EventCacher.PREFS_SERVER_NAME, "Pizza");
    }
    
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
    
    @Override
    public void onResume() 
    {
    	super.onResume();
    	
    	initEventView();
    }
    
    @Override
    public boolean isRefreshOnOpen() 
    {
    	return true;
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
    	SharedPreferences sharedPrefs = context.getSharedPreferences(EventCacher.PREFS_NAME, 0);
		
    	for(Entry<String, EventHolder> entry : EventCacher.getCachedEventNames(context).entrySet()) {
    		EventHolder tempHolder = entry.getValue();
    		
			int check = sharedPrefs.getInt(tempHolder.eventID, 0);
			
			if (check == 1) {
				eventAdapter.add(tempHolder);
			}
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
}

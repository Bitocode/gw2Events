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

public class EventNamesFragment extends Fragment implements RefreshInterface
{
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
        
        fragment.setRetainInstance(true);
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
        }
    	
        return view;
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
}

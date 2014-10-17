package com.firelink.gw2.events;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firelink.gw2.objects.ChildFragmentInterface;
import com.firelink.gw2.objects.EventCacher;
import com.firelink.gw2.objects.EventHolder;
import com.firelink.gw2.objects.TaskCompletedInterface;

public class EventDetailsFragment extends Fragment implements TaskCompletedInterface
{
	private SharedPreferences sharedPrefs;
	private SharedPreferences.Editor sharedPrefsEditor;
	private ChildFragmentInterface childFragInto;
	
	protected TextView descriptionTextView;
	protected TextView startTimesTextView;
	protected TextView[] headersTextView;
	protected ImageView eventImageView;
	protected TextView errorTextView;
	
	protected EventHolder eventHolder;
	protected EventCacher ec;
	
	protected ProgressDialog eventDetailsDialog;
	protected Activity activity;
	protected Context context;
	protected Fragment fragment;
	
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
		
		activity = getActivity();
		context  = getActivity().getApplicationContext();
		fragment = this;
		
		//Set ActionBar stuff
		activity.getActionBar().setDisplayShowTitleEnabled(true);
		activity.getActionBar().setDisplayHomeAsUpEnabled(true);
		
		sharedPrefs 		= activity.getSharedPreferences(EventCacher.PREFS_NAME, 0);
		sharedPrefsEditor 	= sharedPrefs.edit();
		sharedPrefsEditor.commit();
		
		eventHolder        = new EventHolder();
		ec                 = new EventCacher(context);
		eventDetailsDialog = new ProgressDialog(activity);
		
		Bundle bundle = getArguments();
		
		if (null != bundle) {
			eventHolder.eventID = bundle.getString("eventID", "0");
		}
		
		if (null != savedInstanceState) { 
			eventHolder.eventID = savedInstanceState.getString("eventID", "0");
		}
	}
	
	/**
	 * 
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) 
	{
		View view = inflater.inflate(R.layout.event_details_layout, container, false);
		
		
		headersTextView = new TextView[]{
			(TextView)view.findViewById(R.id.eventDetailsView_descriptionHeaderTextView),
			(TextView)view.findViewById(R.id.eventDetailsView_startTimesHeaderTextView),
		};
		
		descriptionTextView = (TextView)view.findViewById(R.id.eventDetailsView_descriptionTextView);
		startTimesTextView  = (TextView)view.findViewById(R.id.eventDetailsView_startTimesTextView);
		errorTextView       = (TextView)view.findViewById(R.id.eventDetailsView_errorView);
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
		childFragInto.refreshOnBack();
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
	public void onSaveInstanceState(Bundle outState) 
	{
		outState.putString("eventID", eventHolder.eventID);
		super.onSaveInstanceState(outState);
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
	 */
	@Override
	public void onTaskCompleted() 
	{
		eventDetailsDialog.dismiss();
		parseCache();
	}
	/**
	 * 
	 */
	@Override
	public void onTaskCompletedWIthErrors(String message) 
	{
		eventDetailsDialog.dismiss();
		errorTextView.setText(message);
		errorTextView.setVisibility(View.VISIBLE);
		
		View view = (View) errorTextView.getParent().getParent();
		
		errorTextView.setHeight(view.getHeight());
		errorTextView.setGravity(Gravity.CENTER);
	}
	
	private void recacheEvent()
	{
		ec.cacheEventDetails(eventHolder.eventID, (TaskCompletedInterface)fragment);
		eventDetailsDialog.setMessage("Retrieving event details...");
		eventDetailsDialog.setIndeterminate(false);
		eventDetailsDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		eventDetailsDialog.setCancelable(false);
		eventDetailsDialog.show();
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
		EventHolder tempEvent = ec.getEventJSONCache(eventHolder);
		
		if (tempEvent == null) {
			recacheEvent();
		} else {
			eventHolder = ec.getEventJSONCache(eventHolder);
			eventHolder.image = ec.getCachedImage(eventHolder.imageName);
			String startTimes = "";
	    	for (int i = 0; i < eventHolder.startTimes.length; i++)
	    	{
	    		//Print it
	    		startTimes = startTimes.concat(EventHolder.formatDateToTime(eventHolder.startTimes[i]) + "\n");

	    		Log.d("GW2Events", i + ": " + EventHolder.formatDateToTime(eventHolder.startTimes[i]));
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
	}
}

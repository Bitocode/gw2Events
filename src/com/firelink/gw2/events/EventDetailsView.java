package com.firelink.gw2.events;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.firelink.gw2.objects.EventHolder;

public class EventDetailsView extends Activity 
{
	protected TextView logoTextView;
	protected EventHolder eventHolder;

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.event_details_layout);
		
		Bundle bundle = getIntent().getExtras();
		
		logoTextView = (TextView)findViewById(R.id.eventDetailsView_logoTextView);
		logoTextView.setText(bundle.getString("eventID"));
		
	}
}

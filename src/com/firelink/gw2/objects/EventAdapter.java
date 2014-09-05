package com.firelink.gw2.objects;

import java.util.ArrayList;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.firelink.gw2.events.R;

public class EventAdapter extends BaseAdapter
{
	
	private Context context;
	private ArrayList<EventHolder> eventData;
	
	
	/**
	 * 
	 * @param context
	 */
	public EventAdapter(Context context)
	{	
		super();
		
		this.context   = context;
		this.eventData = new ArrayList<EventHolder>();
		
	}
	
	/**
	 * 
	 * @param context
	 * @param eventNames
	 * @param eventTypes
	 */
	public EventAdapter(Context context, String[] eventNames, String[] eventTypes, int[] eventTypeIDs)
	{
		super();
		
		this.context   = context;
		this.eventData = new ArrayList<EventHolder>();
		
		for (int i = 0; i < eventNames.length; i++)
		{
			EventHolder events = new EventHolder();
			events.name   = eventNames[i];
			events.type   = eventTypes[i];
			events.typeID = eventTypeIDs[i];
			
			this.eventData.add(events);
		}
	}
	
	public void add(String name, String type, int typeID)
	{		
		EventHolder events = new EventHolder();
		events.name   = name;
		events.type   = type;
		events.typeID = typeID;
		
		this.eventData.add(events);
		
		this.notifyDataSetChanged();
	}
	
	/**
	 * @param position
	 * @param view
	 * @param viewGroup
	 */
	public View getView(int position, View convertView, ViewGroup viewGroup)
	{
		View eventView = convertView;
		
		if (null == eventView)
		{
			LayoutInflater vi;
			vi = LayoutInflater.from(context);
			
			eventView = vi.inflate(R.layout.event_adapter, viewGroup, false);
		}
		
		TextView eventNameTV = (TextView)eventView.findViewById(R.id.eventAdapterTextView);
		ImageView eventTypeIV = (ImageView)eventView.findViewById(R.id.eventAdapterLeftImageView);
		
		eventNameTV.setText(eventData.get(position).name);
		
		//Determine which color to add to the eventClass left bar thing
		int eventResource;
		switch(eventData.get(position).typeID)
		{
		case 1:
			eventResource = R.drawable.event_level_high;
			break;
		case 2:
			eventResource = R.drawable.event_level_standard;
			break;
		case 3:
			eventResource = R.drawable.event_level_low;
			break;
		default:
			eventResource = R.drawable.event_level_standard;
			break;
		}
		
		eventTypeIV.setImageResource(eventResource);
		
		return (eventView);
	}

	@Override
	public int getCount() 
	{
		return this.eventData.size();
	}

	@Override
	public Object getItem(int position) 
	{
		return null;
	}

	@Override
	public long getItemId(int position) 
	{
		return 0;
		
	}
}

			

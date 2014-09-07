package com.firelink.gw2.objects;

import com.firelink.gw2.events.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class EventAdapter extends BaseAdapter
{

    public static class ViewHolder
    {
        public TextView eventNameTV;
        public TextView eventDescTV;
        public ImageView eventTypeIV;
    }

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

    /**
     * 
     * @param name
     * @param type
     * @param typeID
     */
    public void add(String name, String type, String description, int eventID, int typeID)
    {
        EventHolder events = new EventHolder();
        events.name        = name;
        events.type        = type;
        events.description = description;
        events.typeID      = typeID;
        events.eventID     = eventID;

        this.eventData.add(events);

        this.notifyDataSetChanged();
    }

    /**
     * @param position
     * @param view
     * @param viewGroup
     */
    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup)
    {
        ViewHolder holder;

        if (null == convertView)
        {
            LayoutInflater vi;
            vi = LayoutInflater.from(context);

            convertView = vi.inflate(R.layout.event_adapter, viewGroup, false);
            holder = new ViewHolder();
            holder.eventNameTV = (TextView)convertView.findViewById(R.id.eventAdapterTitleTextView);
            holder.eventDescTV = (TextView)convertView.findViewById(R.id.eventAdapterDescriptionTextView);
            holder.eventTypeIV = (ImageView)convertView.findViewById(R.id.eventAdapterLeftImageView);
            convertView.setTag(holder);
        } else
        {
            holder = (ViewHolder) convertView.getTag();
        }

        EventHolder tempEvent = getItem(position);
        holder.eventNameTV.setText(tempEvent.name);
        holder.eventDescTV.setText(tempEvent.description);

        if (tempEvent.displayExtraData)
        {
            holder.eventDescTV.setVisibility(TextView.VISIBLE);
        }
        else
        {
            holder.eventDescTV.setVisibility(TextView.GONE);
        }

        //Determine which color to add to the eventClass left bar thing
        int eventResource;
        switch(tempEvent.typeID)
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

        holder.eventTypeIV.setImageResource(eventResource);

        return (convertView);
    }

    /**
     * 
     */
    @Override
    public int getCount()
    {
        return this.eventData.size();
    }

    /**
     * 
     */
    @Override
    public EventHolder getItem(int position)
    {
        return eventData.get(position);
    }

    /**
     * 
     */
    @Override
    public long getItemId(int position)
    {
        return 0;

    }

    public void setItem(int position, EventHolder event)
    {
        eventData.set(position, event);
    }
}



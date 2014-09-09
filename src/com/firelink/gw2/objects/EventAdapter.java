package com.firelink.gw2.objects;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firelink.gw2.events.R;

public class EventAdapter extends BaseAdapter
{

    static class ViewHolder
    {
        TextView eventNameTV;
        TextView eventDescTV;
        TextView eventWaypointTV;
        TextView eventLevelTV;
        ImageView eventTypeIV;
        ImageView eventImageIV;
        RelativeLayout eventExtraInfoRL;
    }

    private Context context;
    private ArrayList<EventHolder> eventData;
    
    private static final String DISK_CACHE_DIR = "media";


    /**
     * 
     * @param context
     */
    public EventAdapter(Context context)
    {
        super();

        this.context   = context;
        this.eventData = new ArrayList<EventHolder>();
        
        File cacheDir = getDiskCacheDir(context, DISK_CACHE_DIR);

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
    public void add(String name, String type, String description, String waypoint, String imageFileName, int level, int eventID, int typeID)
    {
        EventHolder events = new EventHolder();
        events.name        = name;
        events.type        = type;
        events.description = description;
        events.waypoint    = waypoint;
        events.imagePath   = imageFileName;
        events.level       = level;
        events.typeID      = typeID;
        events.eventID     = eventID;

        this.eventData.add(events);
        
        new CacheImagesTask().execute(eventData.size() - 1);

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
            holder.eventNameTV      = (TextView)convertView.findViewById(R.id.eventAdapterTitleTextView);
            holder.eventDescTV      = (TextView)convertView.findViewById(R.id.eventAdapterDescriptionTextView);
            holder.eventWaypointTV  = (TextView)convertView.findViewById(R.id.eventAdapterWaypointTextView);
            holder.eventLevelTV     = (TextView)convertView.findViewById(R.id.eventAdapterLevelTextView);
            holder.eventTypeIV      = (ImageView)convertView.findViewById(R.id.eventAdapterLeftImageView);
            holder.eventExtraInfoRL = (RelativeLayout)convertView.findViewById(R.id.eventAdapterExtraInfoLayout);
            holder.eventImageIV     = (ImageView)convertView.findViewById(R.id.eventAdapterImageImageView);
            convertView.setTag(holder);
        } else
        {
            holder = (ViewHolder) convertView.getTag();
        }

        EventHolder tempEvent = getItem(position);
        holder.eventNameTV.setText(tempEvent.name);
        holder.eventDescTV.setText(tempEvent.description);
        holder.eventLevelTV.setText(tempEvent.level + "");
        holder.eventWaypointTV.setText(tempEvent.waypoint);
        

        //Determine which color to add to the eventClass left bar thing
        int eventResource;
        int eventBGResource;
        int eventBGPressed;
        switch(tempEvent.typeID)
        {
            case 1:
                eventResource   = R.drawable.event_level_high;
                eventBGResource = R.color.gw_event_level_high;
                eventBGPressed  = R.drawable.event_selector_bg_pressed_high;
                break;
            case 2:
                eventResource   = R.drawable.event_level_standard;
                eventBGResource = R.color.gw_event_level_standard;
                eventBGPressed  = R.drawable.event_selector_bg_pressed_standard;
                break;
            case 3:
                eventResource   = R.drawable.event_level_low;
                eventBGResource = R.color.gw_event_level_low;
                eventBGPressed  = R.drawable.event_selector_bg_pressed_low;
                break;
            default:
                eventResource   = R.drawable.event_level_standard;
                eventBGResource = R.color.gw_event_level_standard;
                eventBGPressed  = R.drawable.event_selector_bg_pressed_high;
                break;
        }

        holder.eventTypeIV.setImageResource(eventResource);
        
        if (tempEvent.isActive)
        {
        	holder.eventExtraInfoRL.setVisibility(View.VISIBLE);
            holder.eventNameTV.setBackgroundResource(eventBGResource);
            holder.eventNameTV.setTextColor(context.getResources().getColor(R.color.white));
            holder.eventNameTV.setActivated(true);
            holder.eventImageIV.setImageDrawable(tempEvent.image);
        }
        else
        {
        	holder.eventExtraInfoRL.setVisibility(View.GONE);
            //holder.eventNameTV.setBackgroundResource(eventBGPressed);
        	holder.eventNameTV.setBackgroundResource(eventBGPressed);
            holder.eventNameTV.setTextColor(context.getResources().getColor(R.color.black));
            holder.eventNameTV.setActivated(false);
            holder.eventImageIV.setImageDrawable(null);
        }
        

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
    
    
    public static File getDiskCacheDir(Context context, String name)
    {
    	//Check if media is mounted
    	final String cachePath =
    			Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) || 
    				!Environment.isExternalStorageRemovable() ? context.getExternalCacheDir().getPath() :
    					context.getCacheDir().getPath();
    	
    	return new File(cachePath + File.separator + name);
    }
    
    public class CacheImagesTask extends AsyncTask<Integer, Void, Void>
    {
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Integer...params)
        {
    		Bitmap bm = null;
        	try {
	        	URL aURL = new URL(eventData.get(params[0]).imagePath);
	            URLConnection conn = aURL.openConnection();
	            conn.connect();
	            InputStream is = conn.getInputStream();
	            BufferedInputStream bis = new BufferedInputStream(is);
	            bm = BitmapFactory.decodeStream(bis);
	            bis.close();
	            is.close();
        	} catch (IOException e) {
        		Log.d("GW2Evnets", e.getMessage());
        	}
        	
        	eventData.get(params[0]).image = new BitmapDrawable(bm);
        	
			return null;	
        }
    }
}



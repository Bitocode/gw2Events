package com.firelink.gw2.objects;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.os.CountDownTimer;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.firelink.gw2.events.R;

public class EventAdapter extends BaseAdapter
{	
    static class ViewHolder
    {
        TextView eventNameTV;
        TextView eventTypeTV;
        TextView eventTimerTV;
        ImageView eventTypeIV;
    }

    private Context context;
    private ArrayList<EventHolder> eventData;
    private ArrayList<CountDownTimer> countDowns;
    private ChildFragmentInterface childFragInto;

    /**
     * 
     * @param context
     */
    public EventAdapter(Context context)
    {
        super();

        this.context   = context;
        this.eventData = new ArrayList<EventHolder>();
        countDowns = new ArrayList<CountDownTimer>();
    }

    /**
     * 
     * @param context
     * @param eventNames
     * @param eventTypes
     */
    public EventAdapter(Context context, EventHolder[] events)
    {
        super();

        this.context   = context;
        this.eventData = new ArrayList<EventHolder>();

        for (int i = 0; i < events.length; i++)
        {
            this.eventData.add(events[i]);
        }
    }

    /**
     * 
     * @param name
     * @param type
     * @param typeID
     */
    public void add(EventHolder events)
    {
        this.eventData.add(events);

        this.notifyDataSetChanged();
    }
    
    /**
     * 
     * @param activity
     */
    public void setInterface(Activity activity)
    {
    	try {
			childFragInto = (ChildFragmentInterface) activity;
		} catch (ClassCastException e) {
			Log.d("GW2Events", e.getMessage());
		}
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
            holder.eventTypeTV      = (TextView)convertView.findViewById(R.id.eventAdapterTypeTextView);
            holder.eventTimerTV     = (TextView)convertView.findViewById(R.id.eventAdapterStartTimeTextView);
            holder.eventTypeIV      = (ImageView)convertView.findViewById(R.id.eventAdapterLeftImageView);
            
            convertView.setTag(holder);
        } else
        {
            holder = (ViewHolder) convertView.getTag();
        }

        EventHolder tempEvent = getItem(position);
        
        //Determine which color to add to the eventClass left bar thing
        int eventColor;
        int eventResource;
        int eventBGPressed;
        int eventBGActive;
        switch(tempEvent.typeID)
        {
            case 1:
            	eventColor      = R.color.gw_event_level_high;
                eventResource   = R.drawable.event_level_high;
                eventBGPressed  = R.drawable.event_selector_bg_pressed_high;
                eventBGActive   = R.color.gw_event_level_high_pressed;
                break;
            case 2:
            	eventColor      = R.color.gw_event_level_standard;
                eventResource   = R.drawable.event_level_standard;
                eventBGPressed  = R.drawable.event_selector_bg_pressed_standard;
                eventBGActive   = R.color.gw_event_level_standard_pressed;
                break;
            case 3:
            	eventColor      = R.color.gw_event_level_low;
                eventResource   = R.drawable.event_level_low;
                eventBGPressed  = R.drawable.event_selector_bg_pressed_low;
                eventBGActive   = R.color.gw_event_level_low_pressed;
                break;
            default:
            	eventColor      = R.color.gw_event_level_standard;
                eventResource   = R.drawable.event_level_standard;
                eventBGPressed  = R.drawable.event_selector_bg_pressed_high;
                eventBGActive   = R.color.gw_event_level_standard_pressed;
                break;
        }
        
        holder.eventNameTV.setText(tempEvent.name);
    	holder.eventNameTV.setBackgroundResource(eventBGPressed);
        holder.eventNameTV.setTextColor(context.getResources().getColor(R.color.black));
        
        holder.eventTypeTV.setText(tempEvent.type);
        holder.eventTypeTV.setBackgroundColor(context.getResources().getColor(eventColor));
        
        holder.eventTypeIV.setImageResource(eventResource);
        
        if (tempEvent.countdownTimer != null) {
        	holder.eventTimerTV.setText(tempEvent.countdownTimer);
        } else {
        	holder.eventTimerTV.setText("");
        }
        
        if (tempEvent.isActive == true) {
        	holder.eventNameTV.setBackgroundResource(0);
        	holder.eventNameTV.setBackgroundColor(context.getResources().getColor(eventBGActive));
        	
        	holder.eventTimerTV.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17.0f);
        }
        
        
        
        return (convertView);
    }
    
    /**
     * 
     */
    public void refreshView()
    {
    	notifyDataSetChanged();
    }
    
    /**
     * 
     */
    public void startCountdown()
    {
    	Date date = new Date();
    	try {
			SimpleDateFormat sd = new SimpleDateFormat("hh:mm:ss a", Locale.US);
    		date = sd.parse(sd.format(Calendar.getInstance().getTime()));
		} catch (ParseException e) {}
    	
    	for (int i = 0; i < getCount(); i++) {
    		final EventHolder temp = getItem(i);
    		
    		long diff = temp.startTime.getTime() - date.getTime();
    		
    		if (diff >= -1000) {
    			CountDownTimer timer = new CountDownTimer(diff + 1000, 1000) {
					
					@Override
					public void onTick(long millisUntilFinished) {
						double hours = ((millisUntilFinished / 1000) / 60) / 60;
						double minutes = (millisUntilFinished / 1000) / 60 % 60;
						double seconds = (millisUntilFinished / 1000) % 60 % 60;
						
						//Display countdown
						temp.countdownTimer = String.format("%02d", (int)hours) + ":" + String.format("%02d", (int)minutes) + ":" + String.format("%02d", (int)seconds);
						
						refreshView();
					}
					
					@Override
					public void onFinish() {
						if (childFragInto != null) {
							childFragInto.forceRefresh();
						}
					}
				};
				
				countDowns.add(timer);
    		} else {
    			temp.countdownTimer = "Active";
    		}
    	}
    	
    	
		
		for (int i = 0; i < countDowns.size(); i++) {
			countDowns.get(i).start();
		}
    }
    
    /**
     * 
     */
    public void stopCountdown()
    {
    	if (countDowns != null) {
    		for (int i = 0; i < countDowns.size(); i++) {
    			countDowns.get(i).cancel();
    		}
    	}
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



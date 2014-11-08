package com.firelink.gw2.objects;

import java.util.Calendar;
import java.util.Date;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.util.LongSparseArray;
import android.util.Log;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.firelink.gw2.events.R;

/**
 * 
 * @author Justin
 *
 */
public class EventAdapter extends BaseAdapter
{	
	/**
	 * 
	 * @author Justin
	 *
	 */
    static class ViewHolder
    {
        TextView eventNameTV;
        TextView eventTypeTV;
        TextView eventTimerTV;
        ImageView eventTypeIV;
    }
    
    private static final long TIMER_INTERVAL_MS = 1000;
    private static final int UPDATE_UI = 143;

    private Context context;
    private SparseArray<EventHolder> eventData;
    private EventUpdateInterface eventUpdateInterface;
    private Runnable mRunnable;
    private Handler mHandler;
    private Date currentTime;

    /**
     * 
     * @param context
     */
    public EventAdapter(Context context)
    {
        super();

        this.context   = context;
        this.eventData = new SparseArray<EventHolder>();
        this.mHandler = new Handler(Looper.getMainLooper()) {
        	@Override
        	public void handleMessage(Message msg) 
        	{
        		if (msg.what == UPDATE_UI) {
        			refreshView();
        		}
        	}
        };
        
    	this.currentTime = Calendar.getInstance().getTime();
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
        this.eventData = new SparseArray<EventHolder>();

        for (int i = 0; i < events.length; i++)
        {
            this.eventData.put(i, events[i]);
        }
    }
    
    /***************************************************
     *************************************************** 
     *	Start of lifecycle methods
     ***************************************************
     ***************************************************/
    
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
        } else {
        	holder.eventTimerTV.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14.0f);
        }
        
        return (convertView);
    }
    
    /***************************************************
     *************************************************** 
     *	Start of EventAdapter data methods
     ***************************************************
     ***************************************************/

    /**
     * 
     * @param name
     * @param type
     * @param typeID
     */
    public void add(EventHolder events)
    {
        this.eventData.put(getCount(), events);

        this.notifyDataSetChanged();
    }
    /**
     * 
     * @param events
     */
    public void addWithoutNotify(EventHolder events)
    {
    	this.eventData.put(getCount(), events);
    }
    /**
     * 
     */
    public void empty()
    {
    	this.eventData.clear();
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
    /**
     * 
     * @param position
     * @param event
     * @param organize
     */
    public void setItem(int position, EventHolder event, boolean organize)
    {
        eventData.put(position, event);
        
        if (organize) {
        	organizeEvents(currentTime);
        }
    }
    
    /**
     * 
     * @param date
     */
    public void organizeEvents(Date date)
    {
    	if (null == date) {
    		date = currentTime;
    	}
    	
    	LongSparseArray<EventHolder> tempHolders = new LongSparseArray<EventHolder>();
    	
    	
    	int numActive = 0;
    	for (int i = 0; i < getCount(); i++) {
    		EventHolder temp = eventData.get(i);
    		Log.d("GW2Events", "i = " + i + " Event = " + temp.name);
    		if (temp.startTimes == null) {
    			return;
    		}
    		
    		final long diff = temp.startTime.getTime() - date.getTime();
    		
    		if (temp.isActive) {
    			tempHolders.put(numActive++, temp);
    			continue;
    		}
    		
    		Log.d("GW2Events", "i = " + i + " Event = " + temp.name + " Diff = " + diff + " isActive = " + temp.isActive);
    		
			if (diff > 0) {
				if (tempHolders.get(diff) == null) {
					tempHolders.put(diff, temp);
				} else {
					tempHolders.put(diff + 1, temp);
				}
			}
    	}
    	
    	for (int i = 0; i < tempHolders.size(); i++) {
    		Log.i("GW2Events", "i = " + i + " Event = " + tempHolders.get(tempHolders.keyAt(i)).name);
    		this.eventData.put(i, tempHolders.get(tempHolders.keyAt(i)));
    	}
    }
    
    /***************************************************
     *************************************************** 
     *	Start of Interface methods
     ***************************************************
     ***************************************************/
    
    /**
     * 
     * @param eui
     */
    public void setEventUpdateInterface(EventUpdateInterface eui)
    {
    	eventUpdateInterface = eui;
    }

    /**
     * 
     */
    public void refreshView()
    {
    	notifyDataSetChanged();
    }
    
    
    /***************************************************
     *************************************************** 
     *	Start of Countdown methods
     ***************************************************
     ***************************************************/
    
    /**
     * 
     */
    public void startEventCountdown()
    {
    	if (mRunnable == null) {
    		mRunnable = new CountdownRunnable();
    	}
    	
    	mHandler.removeCallbacks(mRunnable);
    	mHandler.post(mRunnable);
    }
    
    /**
     * 
     */
    public void stopCountdown()
    {
    	if (null != mHandler) {
    		mHandler.removeCallbacks(mRunnable);
    	}
    }

    
    /***************************************************
     *************************************************** 
     *	Start of Countdown class
     ***************************************************
     ***************************************************/
    
    /**
     * 
     * @author Justin
     *
     */
//    protected class CountdownRunnable implements Runnable
//    {
//		@Override
//		public void run() 
//		{
//			android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
//			try {
//				SimpleDateFormat sd = new SimpleDateFormat("hh:mm:ss a",
//						Locale.US);
//				currentTime = sd.parse(sd.format(Calendar.getInstance().getTime()));
//			} catch (ParseException e) {}
//			
//			boolean thereIsAnUpdate = false;
//			for (int i = 0; i < getCount(); i++) {
//				EventHolder temp = eventData.get(i);
//				
//				if (temp.timeUntilNextEnd <= 0 && null != eventUpdateInterface) {
//					temp = eventUpdateInterface.updateStartAndEndTimes(temp, currentTime);
//				}
//				
//				temp.timeUntilNextStart = temp.startTime.getTime() - currentTime.getTime();
//				temp.timeUntilNextEnd = temp.endTime.getTime() - currentTime.getTime();
//				
//				if (!EventHolder.isEventActive(temp.startTime, temp.endTime, currentTime)) {
//					if (temp.isActive) {
//						temp.isActive = false;
//					}
//					
//					int hours = (int) ((temp.timeUntilNextStart / 1000) / 60) / 60;
//					int minutes = (int) (temp.timeUntilNextStart / 1000) / 60 % 60;
//					int seconds = (int) (temp.timeUntilNextStart / 1000) % 60 % 60;
//					
//					// Display CountDown
//					temp.countdownTimer = String.format("%02d", hours) + ":" + String.format("%02d", minutes) + ":" + String.format("%02d", seconds);
//				} else {
//					if (!temp.isActive) {
//						temp.isActive = true;
//						thereIsAnUpdate = true;
//					}
//					
//					temp.countdownTimer = "Active";
//				}
//				eventData.setValueAt(i, temp);
//			}
//			if (thereIsAnUpdate) {
//				// Organize
//				organizeEvents(currentTime);
//				thereIsAnUpdate = false;
//			}
//			Message message = mHandler.obtainMessage(UPDATE_UI);
//			mHandler.dispatchMessage(message);
//			mHandler.postDelayed(this, TIMER_INTERVAL_MS);
//		}
//	}
    
    /***************************************************
     *************************************************** 
     *	Start of Countdown class
     ***************************************************
     ***************************************************/
    
    /**
     * 
     * @author Justin
     *
     */
    protected class CountdownRunnable implements Runnable
    {
		@Override
		public void run() {
			android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
			
	    	currentTime = Calendar.getInstance().getTime();
			
	    	boolean thereIsAnUpdate = false;
	    	
			for (int i = 0; i < getCount(); i++) {
				EventHolder temp = eventData.get(i);
				
				temp.timeUntilNextEnd   = temp.endTime.getTime() - currentTime.getTime();
				
				if (temp.timeUntilNextEnd <= 0 && null != eventUpdateInterface) {
					temp = eventUpdateInterface.updateStartAndEndTimes(temp, currentTime);
				}
					
				temp.timeUntilNextStart = temp.startTime.getTime() - currentTime.getTime();
				temp.timeUntilNextEnd   = temp.endTime.getTime() - currentTime.getTime();
				
				if (temp.timeUntilNextStart >= 0) {
					
					if (temp.isActive) {
						temp.isActive = false;
						
						thereIsAnUpdate = true;
					}
					
					int hours   = (int)((temp.timeUntilNextStart / 1000) / 60) / 60;
					int minutes = (int)(temp.timeUntilNextStart / 1000) / 60 % 60;
					int seconds = (int)(temp.timeUntilNextStart / 1000) % 60 % 60;
					
					//Display CountDown
					temp.countdownTimer = String.format("%02d", hours) + ":" + String.format("%02d", minutes) + ":" + String.format("%02d", seconds);
				} else {
					if (!temp.isActive) {
						temp.isActive = true;
						
						thereIsAnUpdate = true;
					}
					
					temp.countdownTimer = "Active";
				}
				
		    	eventData.setValueAt(i, temp);
			}
			
			if (thereIsAnUpdate) {
		    	//Organize
		    	if (null != eventUpdateInterface) {
		    		eventUpdateInterface.eventFinished();
		    	}
		    	
		    	thereIsAnUpdate = false;
			}
			
			Message message = mHandler.obtainMessage(UPDATE_UI);
	    	mHandler.dispatchMessage(message);
	    	
			mHandler.postDelayed(this, TIMER_INTERVAL_MS);
		}	
    }
}



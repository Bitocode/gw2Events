package com.firelink.gw2.objects;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.os.CountDownTimer;
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

public class EventAdapter extends BaseAdapter
{	
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
    private HashMap<String, CountDownTimer> countDowns;
    private ChildFragmentInterface childFragInto;
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
        this.countDowns = new HashMap<String, CountDownTimer>();
        this.mHandler = new Handler(Looper.getMainLooper()) {
        	@Override
        	public void handleMessage(Message msg) 
        	{
        		if (msg.what == UPDATE_UI) {
        			refreshView();
        		}
        	}
        };
        
        try {
			SimpleDateFormat sd = new SimpleDateFormat("hh:mm:ss a", Locale.US);
    		this.currentTime = sd.parse(sd.format(Calendar.getInstance().getTime()));
		} catch (ParseException e) {}
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
    
    public void empty()
    {
    	this.eventData.clear();
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
        } else {
        	holder.eventTimerTV.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14.0f);
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
    public void startInfiniteCountdown()
    {
        organizeEvents(currentTime);
        
    	if (mRunnable == null) {
    		mRunnable = new CountdownRunnable();
    	}
    	mHandler.post(mRunnable);
    }
    
    private void organizeEvents(Date date)
    {
    	LongSparseArray<EventHolder> tempHolders = new LongSparseArray<EventHolder>();
    	
    	
    	int numActive = 0;
    	for (int i = 0; i < getCount(); i++) {
    		EventHolder temp = eventData.get(i);
    		
    		if (temp.startTimes == null) {
    			return;
    		}
    		
    		temp.startTime = temp.startTimes[EventHolder.getClosestDate(temp.startTimes, date)];
    		temp.endTime   = temp.endTimes[EventHolder.getClosestDate(temp.endTimes, date)];
    		
    		final long diff = temp.startTime.getTime() - date.getTime();
    		final long endDiff = temp.endTime.getTime() - date.getTime();
    		
    		
			if (diff > 0) {
				if (endDiff < diff && endDiff > 0) {
					tempHolders.put(numActive++, temp);
				} else {
					if (tempHolders.get(diff) == null) {
						tempHolders.put(diff, temp);
					} else {
						tempHolders.put(diff + 1, temp);
					}
				}
			}
    	}
    	
    	for (int i = 0; i < tempHolders.size(); i++) {
    		Log.d("GW2Events", "i = " + i + " Event = " + tempHolders.get(tempHolders.keyAt(i)).name);
    		this.eventData.put(i, tempHolders.get(tempHolders.keyAt(i)));
    	}
    }
    
    /**
     * 
     */
    public void startCountdown()
    {
    	countDowns.clear();
    	
    	Date date = new Date();
    	try {
			SimpleDateFormat sd = new SimpleDateFormat("hh:mm:ss a", Locale.US);
    		date = sd.parse(sd.format(Calendar.getInstance().getTime()));
			//date = sd.parse("11:30:55 PM");
		} catch (ParseException e) {}
    	
    	for (int i = 0; i < getCount(); i++) {
    		final EventHolder temp = getItem(i);
    		
    		final long diff;
    		final long firstCheck = temp.startTime.getTime() - date.getTime();
    		
    		if (firstCheck <= -(1000 * 60 * 15)) {
    			Log.d("GW2Events", firstCheck + " Something");
    			Calendar calendar = Calendar.getInstance();
    			calendar.setTime(temp.startTime);
    			calendar.add(Calendar.DAY_OF_YEAR, 1);
    			temp.startTime = calendar.getTime();
    			
    			diff = temp.startTime.getTime() - date.getTime();
    		} else {
    			diff = firstCheck;
    		}
    		
    		if (diff >= -500 && !temp.isActive) {
    			CountDownTimer timer = new CountDownTimer(diff + 2000, 1000) {
					
					@Override
					public void onTick(long millisUntilFinished) {
						if (diff > 0) {
							int hours = (int)((millisUntilFinished / 1000) / 60) / 60;
							int minutes = (int)(millisUntilFinished / 1000) / 60 % 60;
							int seconds = (int)(millisUntilFinished / 1000) % 60 % 60;
							
							//Display CountDown
							temp.countdownTimer = String.format("%02d", hours) + ":" + String.format("%02d", minutes) + ":" + String.format("%02d", seconds);
						} else {
							temp.countdownTimer = "Active";
						}
						
						refreshView();
					}
					
					@Override
					public void onFinish() {
						if (childFragInto != null) {
							childFragInto.forceRefresh();
						} else {
							temp.isActive = true;
							temp.countdownTimer = "Active";
						}
					}
				};
				
				countDowns.put(i + "", timer);
    		} else {
    			temp.countdownTimer = "Active";
    		}
    	}
    	
    	Iterator<String> i = countDowns.keySet().iterator();
		while (i.hasNext()) {
			String key = i.next();
			countDowns.get(key).start();
		}
    }
    
    /**
     * 
     */
    public void stopCountdown()
    {
    	if (countDowns != null) {
    		Iterator<String> i = countDowns.keySet().iterator();
    		while (i.hasNext()) {
    			String key = i.next();
    			countDowns.get(key).cancel();
    		}
    	}
    	
    	if (null != mHandler) {
    		mHandler.removeCallbacks(mRunnable);
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
        eventData.put(position, event);
    }
    
    protected class CountdownRunnable implements Runnable
    {
		@Override
		public void run() {
			android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
			
	    	try {
				SimpleDateFormat sd = new SimpleDateFormat("hh:mm:ss a", Locale.US);
	    		currentTime = sd.parse(sd.format(Calendar.getInstance().getTime()));
				//date = sd.parse("10:44:55 AM");
			} catch (ParseException e) {}
			
	    	boolean thereIsAnUpdate = false;
	    	
			for (int i = 0; i < getCount(); i++) {
				EventHolder temp = eventData.get(i);
				
				if (temp.timeUntilNextEnd <= 0) {
					temp.startTime = temp.startTimes[EventHolder.getClosestDate(temp.startTimes, currentTime)];
					temp.endTime   = temp.endTimes[EventHolder.getClosestDate(temp.endTimes, currentTime)];
				}
				
				temp.timeUntilNextStart = temp.startTime.getTime() - currentTime.getTime();
				temp.timeUntilNextEnd = temp.endTime.getTime() - currentTime.getTime();
				
				if (temp.timeUntilNextStart > 0 && temp.timeUntilNextStart < temp.timeUntilNextEnd) {
					
					if (temp.isActive) {
						temp.isActive = false;
					}
					
					int hours = (int)((temp.timeUntilNextStart / 1000) / 60) / 60;
					int minutes = (int)(temp.timeUntilNextStart / 1000) / 60 % 60;
					int seconds = (int)(temp.timeUntilNextStart / 1000) % 60 % 60;
					
					//Display CountDown
					temp.countdownTimer = String.format("%02d", hours) + ":" + String.format("%02d", minutes) + ":" + String.format("%02d", seconds);
				} else {
					if (!temp.isActive) {
						temp.isActive = true;
						temp.countdownTimer = "Active";
						
						thereIsAnUpdate = true;
					}
				}
				
		    	eventData.setValueAt(i, temp);
			}
			
			if (thereIsAnUpdate) {
		    	//Organize
		    	organizeEvents(currentTime);
		    	thereIsAnUpdate = false;
			}
			
			Message message = mHandler.obtainMessage(UPDATE_UI);
	    	mHandler.dispatchMessage(message);
	    	
			mHandler.postDelayed(this, TIMER_INTERVAL_MS);
		}	
    }
}



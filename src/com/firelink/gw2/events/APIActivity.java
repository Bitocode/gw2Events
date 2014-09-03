package com.firelink.gw2.events;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.firelink.gw2.events.firstStart.FirstRun;

public class APIActivity extends Activity
{
	public static final String PREFS_NAME 			= "GW2EventReminderPreferences";
	public static final String PREFS_SERVER_ID 		= "SelectedServerID";
	public static final String PREFS_SERVER_NAME 	= "SelectedServerName";
	
	public static final int INTENT_SERVER_SELECTOR_REQUEST_CODE = 143;
	
	
	public TextView resultTextView;
	public ListView eventListView;
	
	public int serverID;
	public String serverName;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.api_view);
        
        resultTextView = (TextView)findViewById(R.id.apiView_resultTextView);
        eventListView  = (ListView)findViewById(R.id.apiView_eventListView);
        eventListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        
        SharedPreferences sharedPrefs = getSharedPreferences(PREFS_NAME, 0);
        
        serverID   = sharedPrefs.getInt(PREFS_SERVER_ID, 0);
        serverName = sharedPrefs.getString(PREFS_SERVER_NAME, "Pizza");
        
        if(serverID != 0){
        	initEventView();
        } else {
        	Intent intent = new Intent(this, FirstRun.class);
        	startActivityForResult(intent, INTENT_SERVER_SELECTOR_REQUEST_CODE);        	
        }
    }
    
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
    	if(requestCode == INTENT_SERVER_SELECTOR_REQUEST_CODE){    		
    		if(resultCode == RESULT_OK){
    			
    			serverID 	= data.getIntExtra("serverID", 0);
    			serverName 	= data.getStringExtra("serverName");
    		
    			initEventView();
    		}        	
    	}
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
    	
    	//Make list adapter to attach to listview
    	ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.event_list_text_view);
    	adapter.add("Test1");
    	adapter.add("Test2");
    	
    	eventListView.setAdapter(adapter);
    }
    
    /**
     * Adjusts the server name depending on the size of the name
     * 
     * @return void
     */
    private void setServerName()
    {
    	resultTextView.setText(serverName);
    	
    	android.view.ViewGroup.LayoutParams lp = resultTextView.getLayoutParams();
    	int width = getResources().getDisplayMetrics().widthPixels;
    	int height = (int)((width * 174) / 1203);
    	
    	lp.height = height;
    	resultTextView.setLayoutParams(lp);
    	
    	//1203x249
    }
}

package com.firelink.gw2.events;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import com.firelink.gw2.events.firstStart.FirstRun;

public class APIActivity extends Activity
{
	public static final String PREFS_NAME 			= "GW2EventReminderPreferences";
	public static final String PREFS_SERVER_ID 		= "SelectedServerID";
	public static final String PREFS_SERVER_NAME 	= "SelectedServerName";
	
	public static final int INTENT_SERVER_SELECTOR_REQUEST_CODE = 143;
	
	
	public TextView resultTextView;
	
	public int serverID;
	public String serverName;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.api_view);
        
        SharedPreferences sharedPrefs = getSharedPreferences(PREFS_NAME, 0);
        
        serverID = sharedPrefs.getInt(PREFS_SERVER_ID, 0);
        serverName = sharedPrefs.getString(PREFS_SERVER_NAME, "Pizza");
  
        resultTextView = (TextView)findViewById(R.id.apiView_resultTextView);
        
        if(serverID != 0){
        	
        	setServerName();
        	
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
    		
    			setServerName();
    		}        	
    	}
    }
    
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

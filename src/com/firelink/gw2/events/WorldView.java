package com.firelink.gw2.events;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.firelink.gw2.objects.APICaller;
import com.firelink.gw2.objects.EventCacher;

public class WorldView extends Activity
{	
	public String region;
	public String selectedServer;
	public Activity activity;
	public Context context;
	public SharedPreferences sharedPrefs;
	public SharedPreferences.Editor sharedPrefsEditor;
	public ArrayAdapter<String> adapterNA;
	public ArrayAdapter<String> adapterEU;
	public ListView lvServer;	
	public Spinner regionSpinner;
	public ProgressDialog progDialog;
	public JSONArray json;
	public HashMap<String, Integer> naServerID;
	public HashMap<String, Integer> euServerID;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.world_view_layout);
        
		//Set actionbar stuff
        getActionBar().setTitle("Select World");
        getActionBar().setDisplayShowTitleEnabled(true);
		
        //Initialize globals
        selectedServer = "";
        
        context 	= this;
        activity 	= this;
    
        
        lvServer 		= (ListView)findViewById(R.id.worldView_serverListView);
		adapterNA	 	= new ArrayAdapter<String>(this, R.layout.world_view_serverlist_textview);
		adapterEU	 	= new ArrayAdapter<String>(this, R.layout.world_view_serverlist_textview);
		naServerID 		= new HashMap<String, Integer>();
		euServerID		= new HashMap<String, Integer>();
		
		//Preferences
		sharedPrefs 		= getSharedPreferences(EventCacher.PREFS_NAME, 0);
		sharedPrefsEditor 	= sharedPrefs.edit();
	
        //Initialize the spinner
        ArrayList<String> regionList = new ArrayList<String>();
        regionList.add("Select One");
        regionList.add("North America");
        regionList.add("Europe");
        
        ArrayAdapter<String> regionSpinnerAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, regionList);
        regionSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        
		regionSpinner = (Spinner)findViewById(R.id.worldView_serverRegionSpinner);
		regionSpinner.setAdapter(regionSpinnerAdapter);
		regionSpinner.setOnItemSelectedListener(regionSpinnerSelector);
		
		//Initialize the ListView
		lvServer.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		
		lvServer.setOnItemClickListener(lvServerHandler);
		
		//Initialize the server select button
		Button serverSelectButton = (Button)findViewById(R.id.worldView_serverSelectButton);
		
		serverSelectButton.setOnClickListener(serverSelectButtonHandler);
		
    }
    
    /**
     * Handler for the region spinner
     */
    AdapterView.OnItemSelectedListener regionSpinnerSelector = new AdapterView.OnItemSelectedListener() {
    	
    	@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1,
				int arg2, long arg3) {
			
			region = ((TextView)arg1).getText().toString();
			
			if(region == "North America"){
				if(adapterNA.isEmpty()){
					new ServerSelectAPI().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				} else {
					lvServer.setAdapter(adapterNA);
				}
			} else if (region == "Europe"){
				if(adapterEU.isEmpty()){
					new ServerSelectAPI().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				} else {
					lvServer.setAdapter(adapterEU);
				}
			} else {
				lvServer.setAdapter(null);
			}
	
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {}
		
	};
	
	/**
	 * Handler for the ListView server selector
	 */
	AdapterView.OnItemClickListener lvServerHandler = new AdapterView.OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			
			TextView lvTextView = (TextView)arg1;
			selectedServer = lvTextView.getText().toString();

		}
	
	};
	
	/**
	 * Handler for the server select button
	 */
	View.OnClickListener serverSelectButtonHandler = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			
			int selectedServerID = 0;
			
			if(region == "North America"){
				selectedServerID = naServerID.get(selectedServer);
			} else if (region == "Europe"){
				selectedServerID = euServerID.get(selectedServer);
			}
			
			sharedPrefsEditor.putInt(EventCacher.PREFS_SERVER_ID, selectedServerID);
			sharedPrefsEditor.putString(EventCacher.PREFS_SERVER_NAME, selectedServer);
			sharedPrefsEditor.apply();
			
			Toast.makeText(context, "Server ID Saved", Toast.LENGTH_LONG).show();
			
			Intent returnIntent = new Intent();
			returnIntent.putExtra("serverName", selectedServer);
			returnIntent.putExtra("serverID", selectedServerID);
			setResult(RESULT_OK, returnIntent);
			
			finish();
		}
	};

		
	/**
	 * This is the class for making our API call to retrieve the server contents.
	 */
	public class ServerSelectAPI extends AsyncTask<Void, Void, String>
	{
		protected void onPreExecute()
		{
			super.onPreExecute();
			
			if(region == "Select One"){
				return;
			}
			
			progDialog = new ProgressDialog(activity);	
			progDialog.setMessage("Getting server list...");
			progDialog.setIndeterminate(false);
			progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progDialog.setCancelable(false);
			progDialog.show();
		}
	
		protected String doInBackground(Void...params)
		{
			if(region == "Select One"){
				return null;
			}
			
			String result = "";
			APICaller api = new APICaller();
			api.setAPI(APICaller.API_WORLD_NAMES);
			if (api.callAPI()) {
				result = api.getJSONString();
			} else {
				result = api.getLastError();
			}
			
			Log.d("GW2Events", result + "");
			
			return result;
		}

		public void onPostExecute(String result)
		{
			if(region == "Select One"){
				return;
			}
			
			
			try
			{
				json = new JSONArray(result);
				
				for(int i = 0; i < json.length(); i++){
					JSONObject jsonObject 	= json.getJSONObject(i);
					String key 				= URLDecoder.decode(jsonObject.getString("name"), "UTF-8");
					int value 				= jsonObject.getInt("id");
					
					
					if(region == "North America"){
						if(value < 2000){
							adapterNA.add(key);
							naServerID.put(key, value);
						}
					} else if (region == "Europe"){
						if(value >= 2000){
							adapterEU.add(key);
							euServerID.put(key, value);
						}
					}
				}
			}
			catch (JSONException e)
			{
				Log.d("GW2Events", e.getMessage());
			} catch (UnsupportedEncodingException e) {
				Log.d("GW2Events", e.getMessage());
			}
			
			lvServer.setAdapter(null);
			if(region == "North America"){
				lvServer.setAdapter(adapterNA);
			} else {
				lvServer.setAdapter(adapterEU);
			}

			progDialog.dismiss();
		}
	}
}
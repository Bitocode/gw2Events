package com.firelink.gw2.events;

import java.net.URLDecoder;

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
import android.widget.ListView;
import android.widget.TextView;

import com.firelink.gw2.events.firstStart.FirstRun;
import com.firelink.gw2.objects.APICaller;
import com.firelink.gw2.objects.DiskCacheHelper;
import com.firelink.gw2.objects.EventAdapter;
import com.firelink.gw2.objects.EventHolder;

public class EventActivity extends Activity
{
    public static final String PREFS_NAME 			= "GW2EventReminderPreferences";
    public static final String PREFS_SERVER_ID 		= "SelectedServerID";
    public static final String PREFS_SERVER_NAME 	= "SelectedServerName";

    public static final int INTENT_SERVER_SELECTOR_REQUEST_CODE = 143;

    public Activity activity;
    public Context context;

    public TextView resultTextView;
    public ListView eventListView;
    public ProgressDialog eventProgDialog;

    public int serverID;
    public String serverName;
    //public ArrayAdapter<String> eventAdapter;
    public EventAdapter eventAdapter;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_list_layout);

        activity = this;
        context  = this;

        resultTextView = (TextView)findViewById(R.id.apiView_resultTextView);
        eventListView  = (ListView)findViewById(R.id.apiView_eventListView);
        eventListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        eventListView.setOnItemClickListener(eventSelectAdapterView);

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

    @Override
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

    AdapterView.OnItemClickListener eventSelectAdapterView = new AdapterView.OnItemClickListener()
    {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        {
            //            Bundle bundle = new Bundle();
            //            EventHolder tempEvent = eventAdapter.getItem(position);
            //
            //            bundle.putString("name", tempEvent.name);
            //            bundle.putString("description", tempEvent.description);
            //            bundle.putString("type", tempEvent.type);
            //            bundle.putInt("typeID", tempEvent.typeID);
            //            bundle.putInt("eventID", tempEvent.eventID);
            //
            //            Intent intent = new Intent(activity, SingleEventActivity.class);
            //            intent.putExtras(bundle);
            //            startActivity(intent);
            EventHolder temp = eventAdapter.getItem(position);
            temp.isActive = !temp.isActive;
            eventAdapter.setItem(position, temp);
            ((EventAdapter)eventListView.getAdapter()).notifyDataSetChanged();
        }
    };
   

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
        //eventAdapter = new ArrayAdapter<String>(this, R.layout.event_list_text_view);

        eventAdapter = new EventAdapter(this);

        new EventSelectAPI().execute();
    }

    /**
     * Adjusts the server name depending on the size of the name
     * 
     * @return void
     */
    private void setServerName()
    {
        resultTextView.setText(serverName);
    }

    /**
     * This is the class for making our API call to retrieve the event contents.
     */
    public class EventSelectAPI extends AsyncTask<Void, Void, String>
    {
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();

            eventProgDialog = new ProgressDialog(activity);
            eventProgDialog.setMessage("Retrieving events...");
            eventProgDialog.setIndeterminate(false);
            eventProgDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            eventProgDialog.setCancelable(false);
            eventProgDialog.show();
        }

        @Override
        protected String doInBackground(Void...params)
        {
            String result = "";
            APICaller api = new APICaller();

            api.setAPI(APICaller.API_EVENT_NAMES);
            api.setWorld(serverID);

            if (api.callAPI()) {
                result = api.getJSONString();
            } else {
                result = api.getLastError();
            }

            Log.d("GW2Events", result + "");

            return result;
        }

        @Override
        public void onPostExecute(String result)
        {
            try
            {
                JSONArray json;
                json = new JSONArray(result);
                DiskCacheHelper dCH = new DiskCacheHelper(context);

                for(int i = 0; i < json.length(); i++){
                    JSONObject jsonObject = json.getJSONObject(i);

                    String name 		 = URLDecoder.decode(jsonObject.getString("name"));
                    String type 		 = URLDecoder.decode(jsonObject.getString("type"));
                    String description   = URLDecoder.decode(jsonObject.getString("description"));
                    String waypoint      = URLDecoder.decode(jsonObject.getString("waypoint"));
                    String imagePath     = URLDecoder.decode(jsonObject.getString("imagePath"));
                    String imageFileName = URLDecoder.decode(jsonObject.getString("imageFileName"));
                    int level            = jsonObject.getInt("level");
                    int typeID           = jsonObject.getInt("typeID");
                    int eventID          = jsonObject.getInt("actual_event_id");

                    //Add to adapter at some point
                    eventAdapter.add(name, type, description, waypoint, imageFileName, level, eventID, typeID);
                    
                    dCH.cacheRemoteMedia(imagePath + imageFileName, DiskCacheHelper.EVENTS_CACHE_DIR, imageFileName);
                }
            }
            catch (JSONException e)
            {
                Log.d("GW2Events", e.getMessage());
            }

            //Reset adapter
            eventListView.setAdapter(null);
            eventListView.setAdapter(eventAdapter);

            eventProgDialog.dismiss();
        }
    }
}

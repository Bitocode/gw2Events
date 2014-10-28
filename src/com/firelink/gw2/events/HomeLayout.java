package com.firelink.gw2.events;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.firelink.gw2.objects.APICaller;
import com.firelink.gw2.objects.ChildFragmentInterface;
import com.firelink.gw2.objects.EventCacher;
import com.firelink.gw2.objects.RefreshInterface;
import com.firelink.gw2.objects.SQLHelper;
import com.firelink.gw2.objects.ServerSelectedInterface;

public class HomeLayout extends Activity implements ChildFragmentInterface, ServerSelectedInterface
{
	public static final int INTENT_SERVER_SELECTOR_REQUEST_CODE = 143;
	
	private boolean selectServer;
	private String[] jEventViews;
	private int currentPosition;
	private DrawerLayout jDrawerLayout;
	private ListView jDrawerListView;
	private Fragment parentFragment;
	private Fragment childFragment;
	private EventCacher dCH;
	public ActionBarDrawerToggle jDrawerToggle;
	public ArrayList<Class<?>> jEventClasses;
	
	public RefreshInterface refresh;
	
	
	/**
	 * Called when the activity is first created. Used to set some standard variables and settings
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.drawer_layout);
		
		selectServer = false;
		//Preference
        SharedPreferences sharedPrefs = this.getSharedPreferences(EventCacher.PREFS_NAME, 0);

        if (sharedPrefs.getInt(EventCacher.PREFS_SERVER_ID, 0) == 0) {
        	//This is first run. Cache!
            new SQLDataCacher().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            new DataCacher().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            
            selectServer = true;
        }
		        
		//The "tabs"
		jEventViews = new String[]{"Event Names", "Upcoming Events", "Subscribed Events"};
		jEventClasses = new ArrayList<Class<?>>();
		jEventClasses.add(EventNamesFragment.class);
		jEventClasses.add(EventLocalUpcomingFragment.class);
		jEventClasses.add(EventSubscribedFragment.class);
		jEventClasses.add(ServerSelectFragment.class);
		
		//Set our views
		jDrawerLayout = (DrawerLayout)findViewById(R.id.drawerLayout);
		jDrawerListView = (ListView)findViewById(R.id.drawerLayout_leftDrawer);
		//Set the adapter and the clicker listener
		jDrawerListView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, jEventViews));
		jDrawerListView.setOnItemClickListener(new DrawerItemClickListener());
		
		//
		jDrawerToggle = new ActionBarDrawerToggle(this, jDrawerLayout, R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close){
			
			@Override
			public void onDrawerClosed(
					View drawerView) 
			{
				setArrowOrBurger();
			}
			
			@Override
			public void onDrawerOpened(
					View drawerView) 
			{
				jDrawerToggle.setDrawerIndicatorEnabled(true);
			}
			
		};
		
		getFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
			@Override
			public void onBackStackChanged() 
			{
				setArrowOrBurger();
			}
		});
		
		jDrawerLayout.setDrawerListener(jDrawerToggle);
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);
		
		
		dCH = new EventCacher(this);
		
		//Set default fragment
		if (savedInstanceState != null) {
			//Load parent tab
			selectItem(savedInstanceState.getInt("currentTab", 0));
			//Check if child tab was open
			String className = savedInstanceState.getString("childFragment", "0");
			
			if (className != "0") {
				try {
					Fragment fragment = (Fragment) Class.forName(className).newInstance();
					fragment.setArguments(savedInstanceState.getBundle("childArgs"));
					selectDetailItem(fragment);
				} catch (ClassNotFoundException e) {
					Log.d("GW2Events", e.getMessage());
				} catch (InstantiationException e) {
					Log.d("GW2Events", e.getMessage());
				} catch (IllegalAccessException e) {
					Log.d("GW2Events", e.getMessage());
				}
			}
		} else {
			if (selectServer) {
				selectServer = false;
				selectItem(jEventClasses.size() - 1);
				jDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
				getActionBar().setDisplayHomeAsUpEnabled(false);
				getActionBar().setHomeButtonEnabled(false);
			} else {
				selectItem(0);
			}
		}
		
		
	}
	
	/***************************************************
     *************************************************** 
     *	Start of non-standard lifecycle activities
     ***************************************************
     ***************************************************/
	
	/**
	 * 
	 */
	@Override
	protected void onDestroy() 
	{
		super.onDestroy();
	}
	@Override
	protected void onPause() 
	{
		super.onPause();
	}
	@Override
	protected void onResume() 
	{
		super.onResume();
	}
	/**
	 * 
	 */
	@Override
	protected void onPostCreate(Bundle savedInstanceState) 
	{
		super.onPostCreate(savedInstanceState);
		
		jDrawerToggle.syncState();
	}
	
	/***************************************************
     *************************************************** 
     *	Start of standard user activities
     ***************************************************
     ***************************************************/
	/**
	 * 
	 */
	@Override
	public void onBackPressed() 
	{
		childFragment = null;
		super.onBackPressed();
	}
	/**
	 * 
	 */
	@Override
	public void onConfigurationChanged(Configuration newConfig) 
	{
		super.onConfigurationChanged(newConfig);
		jDrawerToggle.onConfigurationChanged(newConfig);
	}
	/**
	 * 
	 * @param outState
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) 
	{
		outState.putInt("currentTab", currentPosition);
		if (childFragment != null) {
			outState.putBundle("childArgs", childFragment.getArguments());
			outState.putString("childFragment", childFragment.getClass().getName());
		}
		
		super.onSaveInstanceState(outState);
	}
	/**
	 * 
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
		if (getFragmentManager().getBackStackEntryCount() == 0) {
			if (jDrawerToggle.onOptionsItemSelected(item))
			{
				return true;
			}
			
			return super.onOptionsItemSelected(item);
		}
	
		switch (item.getItemId()) {
			case android.R.id.home:
				childFragment = null;
				super.onBackPressed();
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	/***************************************************
     *************************************************** 
     *	Start of ChildFragmentInterface methods
     ***************************************************
     ***************************************************/
	
	/**
	 * 
	 */
	@Override
	public void refreshOnBack() 
	{
		boolean refresh = false;
		
		Fragment fragment = getFragmentManager().findFragmentByTag(parentFragment.getClass().getName());
		if (fragment != null) {
			refresh = ((RefreshInterface)fragment).isRefreshOnOpen();
		}
		
		if (refresh) {
			((RefreshInterface) getFragmentManager().findFragmentByTag(parentFragment.getClass().getName())).refresh();
		}
	}
	/**
	 * 
	 */
	@Override
	public void forceRefresh() 
	{
		boolean refresh = false;
		
		Fragment fragment = getFragmentManager().findFragmentByTag(parentFragment.getClass().getName());
		if (fragment != null) {
			refresh = true;
		}
		
		if (refresh) {
			((RefreshInterface) getFragmentManager().findFragmentByTag(parentFragment.getClass().getName())).refresh();
		}
	}
	
	/***************************************************
     *************************************************** 
     *	Start of ServerSelectedInterface methods
     ***************************************************
     ***************************************************/
	
	/**
	 * 
	 */
	@Override
	public void onServerSelected() 
	{
		selectItem(0);
		jDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);
	}
	
	/***************************************************
     *************************************************** 
     *	Start of HomeLayout-specific methods
     ***************************************************
     ***************************************************/
	
	/**
	 * 
	 * @author Justin
	 *
	 */
	private class DrawerItemClickListener implements ListView.OnItemClickListener
	{

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			selectItem(position);
		}
		
	}
	/**
	 * 
	 */
	private void setArrowOrBurger()
	{
		if (getFragmentManager().getBackStackEntryCount() == 0) {
			jDrawerToggle.setDrawerIndicatorEnabled(true);
		} else {
			jDrawerToggle.setDrawerIndicatorEnabled(false);
		}
	}
	/**
	 * 
	 * @param position
	 */
	private void selectItem(int position)
	{	
		try {
			parentFragment = (Fragment) (jEventClasses.get(position)).newInstance();
			
			FragmentManager fragmentManager = getFragmentManager();
			if (!fragmentManager.popBackStackImmediate(parentFragment.getClass().getName(), FragmentManager.POP_BACK_STACK_INCLUSIVE)) {
				//Erase all stack
				fragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
				fragmentManager.beginTransaction().replace(R.id.drawerLayout_mainLayout, parentFragment, parentFragment.getClass().getName()).commit();
			}
			
			jDrawerListView.setItemChecked(position, true);
			jDrawerLayout.closeDrawer(jDrawerListView);
			
			currentPosition = position;
			childFragment = null;
		} catch (InstantiationException e) {
			Log.d("GW2Events", e.getMessage());
		} catch (IllegalAccessException e) {
			Log.d("GW2Events", e.getMessage());
		}
	}
	/**
	 * 
	 * @param fragment
	 */
	public void selectDetailItem(Fragment fragment)
	{	
		childFragment = fragment;
		
		FragmentTransaction tFrag = getFragmentManager().beginTransaction();
		tFrag.replace(R.id.drawerLayout_mainLayout, childFragment, childFragment.getClass().getName());
        tFrag.addToBackStack(parentFragment.getClass().getName());
        
        //Commit
        tFrag.commit();
	}
	
	/***************************************************
     *************************************************** 
     *	Start of HomeLayout-specific Classes
     ***************************************************
     ***************************************************/
	
	/**
	 * This caches our background data that we might use in the future
	 */
	public class DataCacher extends AsyncTask<Void, Void, String>
	{
		@Override
        protected void onPreExecute()
        {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void...params)
        {
            String result = "";
            APICaller api = new APICaller();

            api.setAPI(APICaller.API_EVENT_DETAILS);
            api.setLanguage(APICaller.LANG_ENGLISH);
            api.setEventID("");

            if (api.callAPI()) {
                result = api.getJSONString();
            } else {
                result = api.getLastError();
            }

            Log.d("GW2Events", result + "");
            
            try
            {
                JSONObject eventsObject = new JSONObject(result);
                eventsObject = eventsObject.getJSONObject("events");
                Iterator<?> iterator = eventsObject.keys();
                
                while (iterator.hasNext()) 
                {
                	String key = iterator.next().toString();
                	JSONObject eventObject = eventsObject.getJSONObject(key);
                	String imagePath       = eventObject.getString("imagePath");
                	String imageFileName   = eventObject.getString("imageFileName");
                	
                    dCH.cacheRemoteMedia(imagePath + imageFileName, EventCacher.CACHE_MEDIA_DIR, imageFileName);
                    
                    dCH.cacheEventsAPI(eventObject.toString(), EventCacher.CACHE_APIS_DIR, key);
                }
            }
            catch (JSONException e)
            {
                Log.d("GW2Events", e.getMessage());
            }

            return result;
        }
	}
	
	/**
	 * This caches our background data that we might use in the future
	 */
	public class SQLDataCacher extends AsyncTask<Void, Void, Boolean>
	{
		@Override
		protected Boolean doInBackground(Void... params) {
			
			//TODO: Cache the map names, map tiles, map information, and anything else I can think of

			//Create our database tables
			String[] initialQueries = new String[1];
			initialQueries[0] = "CREATE TABLE " + SQLHelper.TABLE_NAME_EVENT + " ( eventID VARCHAR(255) PRIMARY KEY, eventName VARCHAR(255), eventDescription VARCHAR(900), eventType VARCHAR(900), typeID INTEGER ) ";
			
			SQLHelper sqlHelper	= new SQLHelper(getApplicationContext(), initialQueries);
			
			//Retrieve database handlers
			SQLiteDatabase sqlWrite	= sqlHelper.getWritableDatabase();
			
			//Get our event name API data
			APICaller eventNameAPI = new APICaller();
			eventNameAPI.setAPI(APICaller.API_EVENT_NAMES);
			eventNameAPI.setLanguage(APICaller.LANG_ENGLISH);
			eventNameAPI.callAPI();
			String eventNameJSON = eventNameAPI.getJSONString();
			
			
			//Process the data and add it to our DB
			try {
				//Set our JSON arrays
				JSONArray eventNameJSONArray 	= new JSONArray(eventNameJSON);
				
				ContentValues eventNameCV 	= new ContentValues();
				
				for (int i = 0; i < eventNameJSONArray.length(); i++) {
					JSONObject jsonObject = eventNameJSONArray.getJSONObject(i);
					String name 		 = URLDecoder.decode(jsonObject.getString("short_name"), "UTF-8");
                    String description   = URLDecoder.decode(jsonObject.getString("name"), "UTF-8");
                    String eventID       = URLDecoder.decode(jsonObject.getString("id"), "UTF-8");
                    String eventType     = URLDecoder.decode(jsonObject.getString("event_class_name"), "UTF-8");
                    int typeID           = jsonObject.getInt("event_class_id");
					
					eventNameCV.put("eventID", eventID);
					eventNameCV.put("eventName", name);
					eventNameCV.put("eventDescription", description);
					eventNameCV.put("eventType", eventType);
					eventNameCV.put("typeID", typeID);
					
					try {
						sqlWrite.insertWithOnConflict(SQLHelper.TABLE_NAME_EVENT, null, eventNameCV, SQLiteDatabase.CONFLICT_IGNORE);
					} catch (SQLException e) {
						Log.e("GW2Events", e.getMessage());
					}
				}
			} catch (JSONException e) {
				Log.d("GW2Events", e.getMessage());
			} catch (UnsupportedEncodingException e) {
				Log.d("GW2Events", e.getMessage());
			}
			
			sqlHelper.close();
			sqlWrite.close();
			
			return true;
		}	
	}
}

package com.firelink.gw2.events;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.firelink.gw2.objects.ChildFragmentInterface;
import com.firelink.gw2.objects.RefreshInterface;

public class HomeLayout extends Activity implements ChildFragmentInterface
{
	private String[] jEventViews;
	private int currentPosition;
	private DrawerLayout jDrawerLayout;
	private ListView jDrawerListView;
	private Fragment parentFragment;
	private Fragment childFragment;
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
		
		//The "tabs"
		jEventViews = new String[]{"Event Names", "Upcoming Events", "Subscribed Events"};
		jEventClasses = new ArrayList<Class<?>>();
		jEventClasses.add(EventNamesFragment.class);
		jEventClasses.add(EventUpcomingFragment.class);
		jEventClasses.add(EventSubscribedFragment.class);
		
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
			selectItem(0);
		}
		
		
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
	 * @param callingFragment
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
}
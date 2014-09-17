package com.firelink.gw2.objects;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.SparseArray;

public class APICaller 
{
	public static final String LANG_ENGLISH = "en";
	public static final String LANG_FRENCH = "fr";
	public static final String LANG_SPANISH = "es";
	public static final String LANG_GERMAN = "de";
	
	public static final String API_EVENTS        = "https://api.guildwars2.com/v1/events.json?world_id=<WORLD_ID>";
	public static final String API_EVENT_NAMES   = "http://api.bitocode.com/1/guildwars/event_names/<LANG>";
	public static final String API_EVENT_DETAILS = "http://api.bitocode.com/1/guildwars/event_details/<LANG>";
	public static final String API_MAP_NAMES     = "https://api.guildwars2.com/v1/map_names.json";
	public static final String API_WORLD_NAMES   = "http://api.bitocode.com/1/guildwars/world_names/en/";
	
	private String API;
	private String errorMessage;
	private String language;
	private SparseArray<HashMap<String, String>> apiData;
	private int worldID;
	
	public APICaller()
	{
		//Default constructor. Set defaults
		this.apiData = new SparseArray<HashMap<String, String>>();
		this.API = APICaller.API_EVENTS;
		this.worldID = 1013;
		this.errorMessage = "";
	}
	
	public void setAPI(String api)
	{
		this.API = api;
	}
	
	public String getAPI()
	{
		return this.API;
	}
	
	public boolean setWorld(int world)
	{
		if (APICaller.API_EVENTS == this.API|| APICaller.API_EVENT_NAMES == this.API){
			this.worldID = world;
			this.API = this.API.replace("<WORLD_ID>", this.worldID + "");
			return true;
		}
		
		return false;
	}
	
	public boolean setLanguage(String language)
	{
		if (APICaller.API_EVENT_NAMES == this.API)
		{
			this.language = language;
			this.API = this.API.replace("<LANG>", this.language);
			return true;
		}
		
		return false;
	}
	
	public boolean callAPI()
	{
		
		DefaultHttpClient httpClient 	= new DefaultHttpClient(new BasicHttpParams());
		HttpGet httpGet 				= new HttpGet(this.API);
		
		httpGet.setHeader("Content-type", "application/json");
		
		InputStream iStream 	= null;
		String result 			= null;
		HttpResponse response 	= null;
		HttpEntity entity 		= null;
		
		try
		{
			response 	= httpClient.execute(httpGet);
			entity 		= response.getEntity();
			iStream 	= entity.getContent();
			
			try
			{
				BufferedReader bReader 	= new BufferedReader(new InputStreamReader(iStream, "UTF-8"), 8);
				StringBuilder sb 		= new StringBuilder();
				String line 			= null;
				
				while((line = bReader.readLine()) != null){
					sb.append(line);
				}
				
				result = sb.toString();
			}
			catch (UnsupportedEncodingException e)
			{
				this.errorMessage = e.getMessage();
				return false;
			}
		}
		catch (IOException e)
		{
			this.errorMessage = e.getMessage();
			return false;
		}
		
		try
		{
			JSONArray json = null;
			json = new JSONArray(result);
			
			for(int i = 0; i < json.length(); i++){
				JSONObject jsonObject 	= json.getJSONObject(i);
				Iterator<?> iterator = jsonObject.keys();
				
				HashMap<String, String> tempMap = new HashMap<String, String>();
				while(iterator.hasNext()) {
					String key = iterator.next().toString();
					tempMap.put(key, jsonObject.getString(key));
				}
				
				this.apiData.put(i, tempMap);
				
				tempMap = null;
			}
		}
		catch (JSONException e)
		{
			this.errorMessage = e.getMessage();
			return false;
		}
		
		
		return true;
	}
	
	public SparseArray<HashMap<String, String>> getAPIData()
	{
		return this.apiData;
	}
	
	public String getJSONString()
	{
		String result = "";
		SparseArray<HashMap<String, String>> map = this.apiData;
		
		result += "[";
		for (int j = 0; j < map.size(); j++) {
			HashMap<String, String> tempMap = map.get(j);
			Object[] keySet = tempMap.keySet().toArray();
			
			result += "{";
			
			for (int s = 0; s < keySet.length; s++) {
				String key = (String)keySet[s];
				String value = tempMap.get(key);
				
				if (s == keySet.length - 1) {
					result += "\"" + key + "\":\"" + URLEncoder.encode(value) + "\"";
				} else {
					result += "\"" + key + "\":\"" + URLEncoder.encode(value) + "\",";
				}
			}
			
			if (j == map.size() - 1) {
				result += "}";
			} else {
				result += "},";
			}
		}
		
		result += "]";
		
		return result;
	}
	
	public String getLastError()
	{
		return this.errorMessage;
	}
}

package com.firelink.gw2.objects;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;

public class APICaller 
{
	public static final String LANG_ENGLISH = "en";
	public static final String LANG_FRENCH = "fr";
	public static final String LANG_SPANISH = "es";
	public static final String LANG_GERMAN = "de";
	
	public static final String API_EVENTS        = "https://api.guildwars2.com/v1/events.json?world_id=<WORLD_ID>";
	public static final String API_EVENT_NAMES   = "http://api.bitocode.com/1/guildwars/event_names/<LANG>";
	public static final String API_EVENT_DETAILS = "http://api.bitocode.com/1/guildwars/event_details/<LANG>/<EVENT_ID>";
	public static final String API_MAP_NAMES     = "https://api.guildwars2.com/v1/map_names.json";
	public static final String API_WORLD_NAMES   = "http://api.bitocode.com/1/guildwars/world_names/en/";
	
	private String API;
	private String errorMessage;
	private String language;
	private String eventID;
	private String jsonString;
	private int worldID;
	
	public APICaller()
	{
		//Default constructor. Set defaults
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
		if (this.API.contains("<WORLD_ID>"))
		{
			this.worldID = world;
			this.API = this.API.replace("<WORLD_ID>", this.worldID + "");
			return true;
		}
		
		return false;
	}
	
	public boolean setLanguage(String language)
	{
		if (this.API.contains("<LANG>"))
		{
			this.language = language;
			this.API = this.API.replace("<LANG>", this.language);
			return true;
		}
		
		return false;
	}
	
	public boolean setEventID(String eventID)
	{
		if (this.API.contains("<EVENT_ID>"))
		{
			this.eventID = eventID;
			this.API = this.API.replace("<EVENT_ID>", this.eventID);
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
		
		return parseJSONString(result);
	}
	
	private boolean parseJSONString(String json)
	{	
		this.jsonString = json;
		
		return true;
	}
	
	public String getJSONString()
	{
		return this.jsonString;
	}
	
	public String getLastError()
	{
		return this.errorMessage;
	}
}

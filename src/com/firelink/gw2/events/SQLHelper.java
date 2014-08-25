package com.firelink.gw2.events;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class SQLHelper extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 2;
	private static final String DATABASE_NAME = "comFirelinkGW2Events";
	private String[] databaseQueries;
	private String databaseQuery;
	
	public SQLHelper(Context context, String[] query) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.databaseQueries = query;
	}
	
	public SQLHelper(Context context, String query) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.databaseQuery = query;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		if (this.databaseQueries != null) {
			for (int i = 0; i < this.databaseQueries.length; i++) {
				db.execSQL(this.databaseQueries[i]);
				Log.d("GW2Events", this.databaseQueries[i]);
			}
		} else {
			db.execSQL(this.databaseQuery);
			Log.d("GW2Events", this.databaseQuery);
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}

}

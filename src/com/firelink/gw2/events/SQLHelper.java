package com.firelink.gw2.events;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLHelper extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 2;
	private static final String DATABASE_NAME = "comFirelinkGw2Events";
	private String databaseQuery = "";
	
	public SQLHelper(Context context, String query) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.databaseQuery = query;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(this.databaseQuery);
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}

}

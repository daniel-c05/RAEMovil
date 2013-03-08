package com.lazybits.rae.movil.utils;

import com.lazybits.rae.movil.Constants;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
	
	private static final int DB_VERSION = 1;
	
	public static final String TABLE_NAME = "savedSearches";
	
	public static final String _ID = "_id";
	public static final String TERM = "term";
	public static final String URL = "url";
	public static final String DATA = "data";
	public static final String SEARCH_MODE = "mode";
	
	public static final String [] ALL_COLS = {
		_ID,
		TERM,
		SEARCH_MODE,
		URL,
		DATA,
	};
	
	public static final String DEFAULT_SORT_ORDER = TERM;
	
	private static final String DATABASE_CREATE = "create table " + TABLE_NAME 
			+ "(" + _ID + " integer primary key autoincrement, " 
			+ TERM + " text not null, " 
			+ SEARCH_MODE + " integer not null, "
			+ URL + " text not null, "
			+ DATA + " text not null);"
			;

	public DBHelper(Context context, CursorFactory factory,
			int version) {
		super(context, TABLE_NAME, factory, version);
	}
	
	public DBHelper(Context context) {
		super(context, TABLE_NAME, null, DB_VERSION);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DATABASE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Constants.LogMessage("Upgrading database from version " + oldVersion + " to "
		            + newVersion + ", which will destroy all old data");
		    db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
		    onCreate(db);
	}	
	
	
}

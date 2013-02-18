package com.lazybits.rae.movil.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import com.lazybits.rae.movil.Constants;

public class DbManager {
	
	private DBHelper mHelper;
	private SQLiteDatabase mDatabase;
	
	public DbManager (Context context) {
		mHelper = new DBHelper(context);
	  }
	
	public void open () {
		try {
			mDatabase = mHelper.getWritableDatabase();
		} catch (Exception e) {
			Constants.LogMessage(e.toString());
		}
	}
	
	public void close () {
		mDatabase.close();
	}
	
	/**
	 * Saves the search information to the database for further offline access. 
	 * 
	 * @param term The actual search term. 
	 * @param url The url corresponding to the search term. 
	 * @param data The data obtained from the url.
	 * @return The id of the search that was saved successfully, or -1 if the write failed.
	 */
	public long addSearchToDatabase (String term, String url, String data) {
		
		ContentValues mValues = new ContentValues();
		mValues.put(DBHelper.TERM, term);
		mValues.put(DBHelper.URL, url);
		mValues.put(DBHelper.DATA, data);
		
		return mDatabase.insert(DBHelper.TABLE_NAME, null, mValues);
		
	}
	
	/**
	 * Deletes a search record corresponding to the supplied id.
	 * @param _id The id of the search record. 
	 */
	public void deleteSearchFromDb (long _id) {		
		String where = DBHelper._ID + " = '" + _id + "'";		
		mDatabase.delete(DBHelper.TABLE_NAME, where, null);		
	}
	
	/**
	 * Provides details on a search record for the supplied _id. 
	 * @param _id The id of the search record on the database. 
	 * @return A Bundle that contains the search term, the url for the search itself, and the data associated. 
	 */
	public Bundle getSearchInfo (long _id) {
		Bundle info = new Bundle();
		String where = DBHelper._ID + " = '" + _id + "'";
		Cursor cursor = mDatabase.query(DBHelper.TABLE_NAME, DBHelper.ALL_COLS, where, null, null, null, DBHelper.DEFAULT_SORT_ORDER);
		
		if (cursor.moveToFirst()) {
			info.putString(DBHelper.TERM, cursor.getString(1));
			info.putString(DBHelper.URL, cursor.getString(2));
			info.putString(DBHelper.DATA, cursor.getString(3));
		}
		
		cursor.close();
		
		return info;
	}
	
	/**
	 * Provides the ability to use a Cursor with all table items on an adapter. See {@link SearchUtils} for details on usage. 
	 * @return A cursor for all table items on the database. 
	 */
	public Cursor getSearchHistory () {
		return mDatabase.query(DBHelper.TABLE_NAME, DBHelper.ALL_COLS, null, null, null, null, DBHelper.DEFAULT_SORT_ORDER);				
	}

}

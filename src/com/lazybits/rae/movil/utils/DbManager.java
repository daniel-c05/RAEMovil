package com.lazybits.rae.movil.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Toast;

import com.lazybits.rae.movil.Constants;

public class DbManager {
	
	private static DBHelper mHelper;
	private static SQLiteDatabase mDatabase;
	
	private static void open (Context context) {
		if (mDatabase != null && mDatabase.isOpen()) {
			return;
		}
		try {
			mHelper = new DBHelper(context);
			mDatabase = mHelper.getWritableDatabase();
		} catch (Exception e) {
			Constants.LogMessage(e.toString());
		}
	}
	
	private static void close () {
		if (mDatabase != null) {
			mDatabase.close();
		}		
	}
	
	/**
	 * Guarda la informacion para futuro accesso de la misma
	 * @param context El contexto utilizado para abrir la base de datos
	 * @param termino El termino de busqueda. 
	 * @param url El url correspondiente al termino de busqueda. 
	 * @param data La data obtenida del url.
	 * @return El id de la busqueda que se almacenoy, or -1 if the write failed.
	 */
	public static long addSearchToDatabase (Context context, String termino, int isRel, String url, String data) {
		
		
		if (mDatabase == null || !mDatabase.isOpen())
			open(context);	

		ContentValues mValues = new ContentValues();
		mValues.put(DBHelper.TERM, termino);
		mValues.put(DBHelper.SEARCH_MODE, isRel);
		mValues.put(DBHelper.URL, url);
		mValues.put(DBHelper.DATA, data);		
		
		long _id = mDatabase.insert(DBHelper.TABLE_NAME, null, mValues);	
		
		close();
		
		return _id;
		
	}
	
	/**
	 * Elimina una entrada en la base de datos para el id suplementado
	 * @param context El contexto utilizado para abrir la base de datos
	 * @param _id The id of the search record. 
	 */
	public static void deleteSearchFromDb (Context context, long _id) {
		
		if (mDatabase == null || !mDatabase.isOpen())
			open(context);	
		
		String where = DBHelper._ID + " = '" + _id + "'";		
		mDatabase.delete(DBHelper.TABLE_NAME, where, null);
		
		close();
	}
	
	/**
	 * Elimina todas las entradas de la base de datos.
	 * @param context El contexto utilizado para abrir la base de datos
	 */
	public static void deleteSearchHistory (Context context) {	
		
		if (mDatabase == null || !mDatabase.isOpen())
			open(context);	
		
		mDatabase.delete(DBHelper.TABLE_NAME, null, null);	
		Toast.makeText(context, "Historial de busqueda eliminado!", Toast.LENGTH_SHORT).show();
		
		close();
	}		
	
	/**
	 * Provee los detalles de un record correspondientes al id suplido. 
	 * @param context El contexto utilizado para abrir la base de datos
	 * @param _id El id asociado al record en la base de datos. 
	 * @return Un Bundle que contiene el termino de busqueda, el url, y la data asociada al id. 
	 */
	public static Bundle getSearchInfo (Context context, long _id) {
		
		if (mDatabase == null || !mDatabase.isOpen())
			open(context);	
		
		Bundle info = new Bundle();
		String where = DBHelper._ID + " = '" + _id + "'";
		Cursor cursor = mDatabase.query(DBHelper.TABLE_NAME, DBHelper.ALL_COLS, where, null, null, null, DBHelper.DEFAULT_SORT_ORDER);
		
		if (cursor.moveToFirst()) {
			info.putString(DBHelper.TERM, cursor.getString(1));
			info.putString(DBHelper.URL, cursor.getString(3));
			info.putString(DBHelper.DATA, cursor.getString(4));
		}
		
		cursor.close();
		close();
		
		return info;
	}
	
	/**
	 * Utilizado para retirar solamente el codigo html almacenado para el url suplementado. 
	 * @param context El contexto utilizado para abrir la base de datos
	 * @param url El url a buscar en la tabla
	 * @return
	 */
	public static String getSearchHtmlData (Context context, String url) {	
		
		if (mDatabase == null || !mDatabase.isOpen())
			open(context);	
		
		String where = DBHelper.URL + " = '" + url + "'";
		Cursor cursor = mDatabase.query(DBHelper.TABLE_NAME, DBHelper.ALL_COLS, where, null, null, null, DBHelper.DEFAULT_SORT_ORDER);
		
		String data = "";
		
		if (cursor.moveToFirst()) {
			data = cursor.getString(4);
		}
		
		cursor.close();
		close();
		
		return data;
	}
	
	/**
	 * Provee detalles de un record de busqueda para el id suplementado
	 * @param context El contexto utilizado para abrir la base de datos
	 * @param url El url del parametro de busqueda
	 * @return Un Bundle de datos que contiene el id del record, el termino de busqueda, y el html almacenado. 
	 */
	public static Bundle getSearchInfo (Context context, String url) {
		
		if (mDatabase == null || !mDatabase.isOpen())
			open(context);	
		
		Bundle info = new Bundle();
		String where = DBHelper.URL + " = '" + url + "'";
		Cursor cursor = mDatabase.query(DBHelper.TABLE_NAME, DBHelper.ALL_COLS, where, null, null, null, DBHelper.DEFAULT_SORT_ORDER);
		
		if (cursor.moveToFirst()) {
			info.putLong(DBHelper._ID, cursor.getLong(0));
			info.putString(DBHelper.TERM, cursor.getString(1));
			info.putString(DBHelper.DATA, cursor.getString(4));
		}
		
		cursor.close();
		close();
		
		return info;
	}
	
	/**
	 * Provee la abilidad de usar un {@link Cursor} para accesar y retirar los datos de todos los items en la tabla. 
	 * Favor ver {@link SearchUtils} para mas detalles en su uso en este proyecto. 
	 * @param context El contexto utilizado para abrir la base de datos
	 * @return Un Cursor con todos los items en la tabla.  
	 */
	public static Cursor getSearchHistory (Context context) {
		if (mDatabase == null || !mDatabase.isOpen())
			open(context);		
		
		String where = DBHelper.SEARCH_MODE + " = '" + SearchUtils.SEARCH_LENGUA + " ' or " + 
				DBHelper.SEARCH_MODE + " = '" + SearchUtils.SEARCH_PREHISPANICO + " '";
		
		close();
		
		return mDatabase.query(DBHelper.TABLE_NAME, DBHelper.ALL_COLS, where, null, null, null, DBHelper.DEFAULT_SORT_ORDER);				
	}
	
	public static Cursor getSearchSuggestions (Context context, int mode, String query) {
		
		Constants.LogMessage("Getting suggestions for: " + query);
		
		if (mDatabase == null || !mDatabase.isOpen())
			open(context);		
		
		String where = DBHelper.SEARCH_MODE + " = '" + mode + "' and " + 
				DBHelper.TERM + " like '%" + query + "%'";
		
		Cursor cursor = mDatabase.query(DBHelper.TABLE_NAME, DBHelper.ALL_COLS, where, null, null, null, DBHelper.DEFAULT_SORT_ORDER);
		
		if (cursor.moveToFirst()) {
			Constants.LogMessage("Cursor lenght: " + cursor.getCount());
		}
		
		close();
		
		return cursor;
	}

}

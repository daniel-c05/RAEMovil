package com.lazybits.rae.movil;

import android.util.Log;

public class Constants {
	public static final String LOG_TAG = "RAE-Movil";
	
	public static final boolean LOG_ENABLED = true;
	
	public static void LogMessage (String message) {
		Log.v(LOG_TAG, message);
	}
	
}

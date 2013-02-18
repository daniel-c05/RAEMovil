package com.lazybits.rae.movil;

import android.util.Log;

public class Constants {
	
	private static final String LOG_TAG = "RAE-Movil";

	private static final boolean LOG_ENABLED = true;

	public static void LogMessage (String message) {
		if (LOG_ENABLED)
			Log.v(LOG_TAG, message);		
	}

}

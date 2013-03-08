package com.lazybits.rae.movil;

import android.util.Log;

/**
 * Clase utilizada para manejar la constante de mensajes. 
 *
 */
public class Constants {
	
	private static final String LOG_TAG = "RAE-Movil";
	
	//Cambiar a falso cuando se envie app a producción. 
	private static final boolean LOG_ENABLED = false;

	public static void LogMessage (String message) {
		if (LOG_ENABLED)
			Log.v(LOG_TAG, message);		
	}

}
package com.lazybits.rae.movil.utils;

import android.annotation.SuppressLint;

@SuppressLint("DefaultLocale")
public class SearchUtils {
	
	public static final String LENGUA_BASE_SEARCH_URL = "http://lema.rae.es/drae/srv/search?val=";
	public static final String PREHISPANICO_BASE_SEARCH_URL = "http://lema.rae.es/dpd/?key=";
	public static final String HOST_URL = "lema.rae.es";
	
	public static final int SEARCH_LENGUA = 0001;
	public static final int SEARCH_PREHISPANICO = 0002;
	
	public static String getSearchUrl (int searchMode, String word) {
		
		word = word.toLowerCase();
		
		switch (searchMode) {
		case SEARCH_LENGUA:
			return LENGUA_BASE_SEARCH_URL + word;
		case SEARCH_PREHISPANICO:
			return PREHISPANICO_BASE_SEARCH_URL + word;
		default:
			return "";
		}
	}
	
}


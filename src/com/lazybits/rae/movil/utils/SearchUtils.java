package com.lazybits.rae.movil.utils;

import android.annotation.SuppressLint;

@SuppressLint("DefaultLocale")
public class SearchUtils {
	
	private static final String LENGUA_BASE_SEARCH_URL = "http://lema.rae.es/drae/srv/search?val=";
	private static final String PREHISPANICO_BASE_SEARCH_URL = "http://lema.rae.es/dpd/?key=";
	public static final String HOST_URL = "lema.rae.es";
	
	public static final int SEARCH_LENGUA = 0001;
	public static final int SEARCH_PREHISPANICO = 0002;
	public static final int SEARCH_LENGUA_REL = 0003;
	
	public static final String MIME_TYPE = "text/html";
	public static final String CHARSET = "utf-8";
	
	public static final String LENGUA_RELATED_URL = "http://lema.rae.es/drae/srv/";
	public static final String RELATED_SEARCH_KEY = "search?id=";
	
	public static String getSearchUrl (int searchMode, String word) {		
		
		switch (searchMode) {
		case SEARCH_LENGUA:
			word = word.toLowerCase();
			return LENGUA_BASE_SEARCH_URL + word;
		case SEARCH_PREHISPANICO:
			word = word.toLowerCase();
			return PREHISPANICO_BASE_SEARCH_URL + word;
		case SEARCH_LENGUA_REL:
			return LENGUA_RELATED_URL + word;
		default:
			return "";
		}
	}
	
}


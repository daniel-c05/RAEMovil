package com.lazybits.rae.movil.utils;

import com.lazybits.rae.movil.Constants;

import android.annotation.SuppressLint;

@SuppressLint("DefaultLocale")
public class SearchUtils {
	
	private static final String LENGUA_BASE_SEARCH_URL = "http://lema.rae.es/drae/srv/search?val=";
	private static final String LENGUA_RELATED_URL = "http://lema.rae.es/drae/srv/";
	private static final String PREHISPANICO_BASE_SEARCH_URL = "http://lema.rae.es/dpd/srv/search?key=";
	private static final String PREHISPANICO_RELATED_URL = "http://lema.rae.es/dpd/srv/";
	
	public static final int SEARCH_LENGUA = 0001;
	public static final int SEARCH_PREHISPANICO = 0002;
	public static final int SEARCH_LENGUA_REL = 0003;
	public static final int SEARCH_PREHISPANICO_REL = 0004;
	
	public static final String MIME_TYPE = "text/html";
	public static final String CHARSET = "utf-8";
	
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
		case SEARCH_PREHISPANICO_REL:
			return PREHISPANICO_RELATED_URL + word;
		default:
			return "";
		}
	}

	/**
	 * Utilidad para obtener el termino de busqueda a partir de un Url
	 * @param searchMode El modo en que estamos buscando, puede ser {@link #SEARCH_LENGUA}, {@link #SEARCH_LENGUA_REL}, {@link #SEARCH_PREHISPANICO}, or {@link #SEARCH_PREHISPANICO_REL}
	 * @param url The url to retrieve the search term from
	 * @return the search term itself
	 */
	public static String getSearchTerm(int searchMode, String url) {
		String term = "";
		int start = 0;
		int end = 0;
		
		switch (searchMode) {
		//Si es una busqueda por termino
		case SEARCH_LENGUA:
		case SEARCH_PREHISPANICO:
			start = url.indexOf("=") + 1;
			end = url.length();
			break;
		//Si es una busqueda por termino relacionado (clicks dentro del webview		
		case SEARCH_LENGUA_REL:
		case SEARCH_PREHISPANICO_REL:
			start = url.lastIndexOf("/") + 1;
			end = url.length();
			break;
		default:
			break;
		}
		
		term = url.substring(start, end);
		Constants.LogMessage("New Search Term: " + term);
		
		return term;
	}
	
	/**
	 * Tomado y modificado a partir de <a href="https://github.com/android/platform_packages_apps_browser/blob/master/src/com/android/browser/DownloadHandler.java">Android Developers</a>
	 * @param path El url a codificar
	 * @return Un nuevo url codificado para uso sin problemas 
	 */
	public static String encodePath(String path) {
        char[] chars = path.toCharArray();

        boolean needed = false;
        for (char c : chars) {
            if (c == '[' || c == ']' || c == '|' || c == 'ñ' || c == 'á' || c == 'é' || c == 'í' || c == 'ó' || c == 'ú' || c == 'ü') {
                needed = true;
                break;
            }
        }
        if (needed == false) {
            return path;
        }

        StringBuilder sb = new StringBuilder("");
        for (char c : chars) {
            if (c == '[' || c == ']' || c == '|' || c == 'ñ' || c == 'á' || c == 'é' || c == 'í' || c == 'ó' || c == 'ú' || c == 'ü') {
                sb.append('%');
                sb.append(Integer.toHexString(c));
            } else {
                sb.append(c);
            }
        }

        return sb.toString();
    }

}


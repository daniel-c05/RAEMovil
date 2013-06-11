package com.lazybits.rae.movil.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.view.View;
import android.webkit.WebView;

import com.lazybits.rae.movil.Constants;

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
	
	/**
	 * Metodo estatico que permite esconder o mostrar la barra de progreso como elemento visual
	 * que le deja saber al usuario que los datos estan cargando. 
	 * 
	 * @param context El contexto
	 * @param progress El contenedor de la barra de progreso. 
	 * @param results El webview que contiene los resultados
	 * @param show Si se mostrara o escondera la barra de progreso
	 */
	@SuppressLint("NewApi")
	public static void showProgress (final Context context, final View progress, final WebView results, final boolean show) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = context.getResources().getInteger(
					android.R.integer.config_shortAnimTime);

			progress.setVisibility(View.VISIBLE);
			progress.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							progress.setVisibility(show ? View.VISIBLE
									: View.GONE);
						}
					});

			results.setVisibility(View.VISIBLE);
			results.animate().setDuration(shortAnimTime)
					.alpha(show ? 0 : 1)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							results.setVisibility(show ? View.GONE
									: View.VISIBLE);
						}
					});
		} else {
			progress.setVisibility(show ? View.VISIBLE : View.GONE);
			results.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}
	
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
            if (c == '[' || c == ']' || c == '|' || c == 'ñ' || c == 'á' || c == 'é' || c == 'í' || c == 'ó' || c == 'ú' || c == 'ü' || c == ' ') {
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
            } else if (c == ' ') {
				sb.append('%');
				sb.append('2');
				sb.append('0');
			}
            else {
                sb.append(c);
            }
        }

        return sb.toString();
    }

}


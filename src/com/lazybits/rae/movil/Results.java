package com.lazybits.rae.movil;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.SearchRecentSuggestions;
import android.support.v4.app.NavUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.SearchView.OnSuggestionListener;

import com.lazybits.rae.movil.tasks.GetSearchHtmlAsync;
import com.lazybits.rae.movil.utils.DbManager;
import com.lazybits.rae.movil.utils.SearchSuggestionsProvider;
import com.lazybits.rae.movil.utils.SearchUtils;

@SuppressLint("DefaultLocale")
public class Results extends Activity {

	public static final String EXTRA_TERM = "term";
	public static final String EXTRA_SAERCH_MODE = "mode";
	public static final String EXTRA_SAERCH_MODE_REL = "mode-rel";

	private WebView webView;
	private SearchView searchView; 	//El widget de busqueda
	private MenuItem searchMenu;	//El menu item que corresponde a la busqueda, utilizado para colapsar o mostrar el widget. 
	private String mTerm, mUrl, mHtmlData;
	private SharedPreferences mPreferences;	
	private SearchRecentSuggestions suggestions;
	private int searchMode = SearchUtils.SEARCH_LENGUA;	//Por defecto se busca en el diccionario de la lengua española
	private int searchModeRel = SearchUtils.SEARCH_LENGUA_REL;	//Para manejar clicks dentro del Webview
	
	private ArrayList<String> searchHistory;
	private int searchHistoryPos;	//Lleva cuenta de en que parte del historial de busquedas estamos.
	private View mProgressContainer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_results);

		getActionBar().setDisplayHomeAsUpEnabled(true);

		mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		suggestions = new SearchRecentSuggestions(this,
				SearchSuggestionsProvider.AUTHORITY, SearchSuggestionsProvider.MODE);

		Bundle extras = getIntent().getExtras();
		
		mProgressContainer = findViewById(R.id.progress_container); 
		webView = (WebView) findViewById(R.id.results_webview);		

		if (extras != null && extras.containsKey(EXTRA_TERM)) {
			//inicializa el array que contiene los terminos buscados en la sesión actual
			SearchUtils.showProgress(this, mProgressContainer, webView, true);
			searchHistory = new ArrayList<String>();
			mTerm = extras.getString(EXTRA_TERM);
			//agrega el termino a la historia.
			searchHistory.add(mTerm);
			searchHistoryPos = searchHistory.size() -1; 
			searchMode = extras.getInt(EXTRA_SAERCH_MODE);
			searchModeRel = extras.getInt(EXTRA_SAERCH_MODE_REL);
			mUrl = SearchUtils.getSearchUrl(searchMode, mTerm);			
			suggestions.saveRecentQuery(mTerm, null);
			Constants.LogMessage(mUrl);			
			mHtmlData = DbManager.getSearchHtmlData(this, mUrl);
		}		

		WebSettings settings = webView.getSettings();
		settings.setJavaScriptEnabled(true);
		webView.setWebViewClient(new MyWebViewClient());

		if (mHtmlData == null || mHtmlData == "") {
			Constants.LogMessage("Data not loaded from database, calling asynctask");
			new GetSearchHtmlAsync(this, webView, mProgressContainer, searchMode).execute(mTerm);
		}
		else {
			Constants.LogMessage("Data loaded from database, loading to webview now");
			webView.loadDataWithBaseURL(mUrl, mHtmlData, SearchUtils.MIME_TYPE, SearchUtils.CHARSET, "");
			SearchUtils.showProgress(this, mProgressContainer, webView, false);
		}		
	}

	/**
	 * Taken from <a href="http://developer.android.com/guide/webapps/webview.html">Android Developers</a>
	 */
	private class MyWebViewClient extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {

			Constants.LogMessage("Url clicked: " + url);

			//Si el url que se esta manejando contiene la clave correcta "searchId?", maneja el click. 
			if (url.contains(SearchUtils.RELATED_SEARCH_KEY)) {
				SearchUtils.showProgress(Results.this, mProgressContainer, webView, true);
				if (!url.startsWith("http://")) {
					//Si el url no es en si un url construido, el url sera el termino de busqueda.
					mTerm = url;
					//Se usa searchModeRel porque estamos manejando clicks dentro del webview y no una busqueda formal 
					url = SearchUtils.getSearchUrl(searchModeRel, url);					
				}
				else {
					//Si el url es realmente un url construido, obtengamos el termino mediante nuestros searchutils.
					mTerm = SearchUtils.getSearchTerm(searchModeRel, url);						    		
				}

				Constants.LogMessage("Url is now: " + url);
				
				searchHistory.add(mTerm);
				searchHistoryPos = searchHistory.size() -1; 
				mHtmlData = DbManager.getSearchHtmlData(Results.this, url);
				if (mHtmlData == null || mHtmlData == "") {
					Constants.LogMessage("Data not loaded from database, calling asynctask");
					new GetSearchHtmlAsync(Results.this, webView, mProgressContainer, searchModeRel).execute(mTerm);
				}
				else {
					Constants.LogMessage("Data loaded from database, loading to webview now");
					view.loadDataWithBaseURL(mUrl, mHtmlData, SearchUtils.MIME_TYPE, SearchUtils.CHARSET, "");
					SearchUtils.showProgress(Results.this, mProgressContainer, webView, false);
				}
				return true;
			}	    	

			//De lo contrario deja que el sistema maneje el nuevo click
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
			startActivity(intent);
			return true;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_results, menu);
		//Infla y prepara el search widget para manejar futuras busquedas sin volver a la actividad principal. 
		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		searchMenu = menu.findItem(R.id.menu_search);
		searchView = (SearchView) searchMenu.getActionView();
		searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
		searchView.setOnSuggestionListener(new OnSuggestionListener() {
			
			@Override
			public boolean onSuggestionSelect(int position) {
				//No hacemos nada
				return false;
			}
			
			@Override
			public boolean onSuggestionClick(int position) {
				//Collapse menu, and continue with to handle the click on the suggestion
				if (searchMenu != null) {
		            searchMenu.collapseActionView();
		        }
				return false;
			}
		});
		
		searchView.setOnQueryTextListener(new OnQueryTextListener() {
			
			@Override
			public boolean onQueryTextSubmit(String query) {
				//Collapse the menu, and continue with the search.
				if (searchMenu != null) {
		            searchMenu.collapseActionView();
		        }
				return false;
			}
			
			@Override
			public boolean onQueryTextChange(String newText) {
				// Do nothing
				return false;
			}
		});
		return true;
	}

	@Override
	public void onNewIntent (Intent intent) {
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			String query = intent.getStringExtra(SearchManager.QUERY);
			//Esto ya deberia ser minusculas, pero por si acaso.
			showNewResults(query.toLowerCase());
		}
	}

	/**
	 * A partir de un nuevo string query, intenta cargar la informacion de la base de datos local, de no encontrar datos
	 * vuelve a buscar en internet y almacena los resultados.
	 * @param query El termino a buscar
	 */
	private void showNewResults(String query) {
		Constants.LogMessage(query);
		
		//muestra el progress bar por si acaso la data no esta offline. 
		
		SearchUtils.showProgress(this, mProgressContainer, webView, true);
		
		//una vez mas, a buscar como evitar duplicados
		mTerm = query.toLowerCase();
		searchHistory.add(mTerm);
		searchHistoryPos = searchHistory.size() -1; 
		suggestions.saveRecentQuery(mTerm, null);
		mUrl = SearchUtils.getSearchUrl(searchMode, mTerm);
		Constants.LogMessage(mUrl);			
		mHtmlData = DbManager.getSearchHtmlData(this, mUrl);
		if (mHtmlData == null || mHtmlData == "") {
			Constants.LogMessage("Data not loaded from database, calling asynctask");
			new GetSearchHtmlAsync(this, webView, mProgressContainer, searchMode).execute(mTerm);
		}
		else {
			Constants.LogMessage("Data loaded from database, loading to webview now");
			webView.loadDataWithBaseURL(mUrl, mHtmlData, SearchUtils.MIME_TYPE, SearchUtils.CHARSET, "");
			SearchUtils.showProgress(this, mProgressContainer, webView, false);
		}				
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		case R.id.menu_settings:
			Intent settings = new Intent(this, Settings.class);
			startActivity(settings);
			return true;
		case R.id.menu_change_dictionary:
			showChangeDictionary();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {		

		//Revisa si el keypress fue de la tecla back, y si tenemos historial de busqueda
		if ((keyCode == KeyEvent.KEYCODE_BACK) && searchHistoryPos > 0) {
			Constants.LogMessage("Handling back keyevent");
			//Revisa si la navegacion atras esta habilitada o si debemos regresar a la pagina principal de busqueda
			if (mPreferences.getString(Settings.KEY_BACK_BEHAVIOR, "search").equals("prev_word")) {
				Constants.LogMessage("Going to previous search term");
				//Remueve el ultimo item buscado.
				searchHistory.remove(mTerm);
				searchHistoryPos = searchHistory.size() -1; 
				
				mTerm = searchHistory.get(searchHistoryPos);
				boolean isRel = false;
				
				if (mTerm.contains(SearchUtils.RELATED_SEARCH_KEY)) {
					//El termino anterior era una busqueda relacionada? 
					mUrl = SearchUtils.getSearchUrl(searchModeRel, mTerm);
					isRel = true;
				}
				else {
					//Si no lo era, obten la informacion normal. 
					mUrl = SearchUtils.getSearchUrl(searchMode, mTerm);
					isRel = false;
				}
				
				mHtmlData = DbManager.getSearchHtmlData(this, mUrl);
				
				if (mHtmlData == null || mHtmlData == "") {
					Constants.LogMessage("Data not loaded from database, calling asynctask");
					if (isRel) {
						new GetSearchHtmlAsync(this, webView, mProgressContainer, searchModeRel).execute(mTerm);
					}
					else {
						new GetSearchHtmlAsync(this, webView, mProgressContainer, searchMode).execute(mTerm);
					}											
				}
				else {
					Constants.LogMessage("Data loaded from database, loading to webview now");
					webView.loadDataWithBaseURL(mUrl, mHtmlData, SearchUtils.MIME_TYPE, SearchUtils.CHARSET, "");
				}				
				return true;
			}	        
		}
		
		else if (keyCode == KeyEvent.KEYCODE_SEARCH) {
			onSearchRequested();
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}
	
	@Override
    public boolean onSearchRequested() {
		if (searchMenu != null) {
            searchMenu.expandActionView();
        }
        return false;
    }

	protected void showChangeDictionary() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);    		
		builder.setSingleChoiceItems(R.array.array_dictionary_selecton, searchMode - 1, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				Constants.LogMessage("Item clicked: " + which);
				switch (which) {
				case 0:
					searchMode = SearchUtils.SEARCH_LENGUA;
					searchModeRel = SearchUtils.SEARCH_LENGUA_REL;
					break;
				case 1:
					searchMode = SearchUtils.SEARCH_PREHISPANICO;
					searchModeRel = SearchUtils.SEARCH_PREHISPANICO_REL;
				default:
					break;					
				}	
				dialog.dismiss();
			}
		})	
		.setTitle(R.string.title_dialog_select_dictionary)
		.setCancelable(true).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {				
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		AlertDialog dialog = builder.create();
		dialog.show();
	}

}

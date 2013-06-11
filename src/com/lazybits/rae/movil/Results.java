package com.lazybits.rae.movil;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter.CursorToStringConverter;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.ads.AdRequest;
import com.google.ads.AdView;
import com.lazybits.rae.movil.tasks.GetSearchHtmlAsync;
import com.lazybits.rae.movil.utils.DBHelper;
import com.lazybits.rae.movil.utils.DbManager;
import com.lazybits.rae.movil.utils.SearchUtils;

@SuppressLint("DefaultLocale")
public class Results extends SherlockActivity implements OnEditorActionListener {

	public static final String EXTRA_TERM = "term";
	public static final String EXTRA_SAERCH_MODE = "mode";
	public static final String EXTRA_SAERCH_MODE_REL = "mode-rel";

	private WebView webView;
	private SimpleCursorAdapter mSuggestionsAdapter;
	private MenuItem searchMenu;
	private AutoCompleteTextView mSearchInput;
	private ImageView mClearButton;
	private String mTerm, mUrl, mHtmlData;
	private int searchMode = SearchUtils.SEARCH_LENGUA;	//Por defecto se busca en el diccionario de la lengua española
	private int searchModeRel = SearchUtils.SEARCH_LENGUA_REL;	//Para manejar clicks dentro del Webview
	private AdView mAdView;
	private ArrayList<String> searchHistory;
	private int searchHistoryPos;	//Lleva cuenta de en que parte del historial de busquedas estamos.
	private View mProgressContainer;

	/**
	 * Se Ocupa de actualizar las sugerencias cada vez que el usuario modifica el texto del input.
	 */
	private final TextWatcher watcher = new TextWatcher() {

		@Override
		public void afterTextChanged(Editable s) {	
			//No hacemos nada
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {	
			//No hacemos nada
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {

			Constants.LogMessage("new charsequence: " + s);

			if (s.length() < 1) {
				mSearchInput.dismissDropDown();
				mClearButton.setVisibility(View.GONE);
				mSuggestionsAdapter.swapCursor(null);				
			}
			else {
				mClearButton.setVisibility(View.VISIBLE);
				mSuggestionsAdapter.swapCursor(DbManager.getSearchSuggestions(Results.this, searchMode, s.toString()));				
			}
			mSuggestionsAdapter.notifyDataSetChanged();
			mSearchInput.setAdapter(mSuggestionsAdapter);
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_results);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		setupAds();

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

	private void setupAds() {
		mAdView = (AdView)this.findViewById(R.id.adView);
		mAdView.loadAd(new AdRequest());
	}

	/**
	 * Tomado de <a href="http://developer.android.com/guide/webapps/webview.html">Android Developers</a>
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
		getSupportMenuInflater().inflate(R.menu.activity_results, menu);
		//Infla y prepara el search widget para manejar futuras busquedas sin volver a la actividad principal. 
		searchMenu = menu.findItem(R.id.menu_search);		
		searchMenu.setActionView(R.layout.collapsible_edit_text)
		.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);

		RelativeLayout mCollapsibleContainer = (RelativeLayout) searchMenu.getActionView();
		mSearchInput = (AutoCompleteTextView) mCollapsibleContainer.findViewById(R.id.collapsible_input);
		mClearButton = (ImageView) mCollapsibleContainer.findViewById(R.id.collapsible_action_button);
		mClearButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mSearchInput.setText("");
			}
		});

		mSuggestionsAdapter = new SimpleCursorAdapter(this, 
				android.R.layout.simple_list_item_1, 
				null, 
				new String [] {DBHelper.TERM}, new int [] {android.R.id.text1} , 0); 

		mSuggestionsAdapter.setCursorToStringConverter(new CursorToStringConverter() {
			//Anula el comportamiento normal de cursor
			@Override
			public CharSequence convertToString(Cursor cursor) {
				//En vez de regresar el cursor convertido a string, regresamos el valor contenido en una columna especifica.
				return cursor.getString(cursor.getColumnIndex(DBHelper.TERM));
			}
		});
		if (mSuggestionsAdapter != null || mSuggestionsAdapter.getCount() != 0) {
			mSearchInput.setAdapter(mSuggestionsAdapter);
		}	

		mSearchInput.addTextChangedListener(watcher);
		mSearchInput.setOnEditorActionListener(this);			
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

		else if (keyCode == KeyEvent.KEYCODE_SEARCH) {
			onSearchRequested();
			return true;
		}

		else if (keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER) {
			Constants.LogMessage("Enter");
			if (searchMenu.isActionViewExpanded() && mSearchInput.hasFocus()) {
				showNewResults(mSearchInput.getText().toString());
				mSearchInput.setText("");
			}			
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

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		final String input =  mSearchInput.getText().toString();
		if (EditorInfo.IME_ACTION_SEARCH == actionId) {			
			Constants.LogMessage("Handling search via Action Done");		
			if (input.length() == 0) {
				return true;
			}
			showNewResults(input);
			return true;
		}
		//Cuando se oprime enter
		else if (EditorInfo.IME_NULL == actionId && event.getAction() == KeyEvent.ACTION_DOWN) {
			Constants.LogMessage("Handling search via Enter");
			if (input.length() == 0) {
				return true;
			}
			showNewResults(input);
			return true;
		}
		return false;
	}

}
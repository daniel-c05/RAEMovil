package com.lazybits.rae.movil;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.v4.app.NavUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.SearchView;

import com.lazybits.rae.movil.tasks.GetSearchHtmlAsync;
import com.lazybits.rae.movil.utils.DbManager;
import com.lazybits.rae.movil.utils.SearchSuggestionsProvider;
import com.lazybits.rae.movil.utils.SearchUtils;

public class Results extends Activity {

	public static final String EXTRA_TERM = "term";

	private WebView webView;
	private String mTerm, mUrl, mHtmlData;
	private SharedPreferences mPreferences;	
	private SearchRecentSuggestions suggestions;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_results);

		getActionBar().setDisplayHomeAsUpEnabled(true);

		mPreferences = getPreferences(MODE_PRIVATE);
		suggestions = new SearchRecentSuggestions(this,
				SearchSuggestionsProvider.AUTHORITY, SearchSuggestionsProvider.MODE);

		Bundle extras = getIntent().getExtras();

		if (extras != null && extras.containsKey(EXTRA_TERM)) {					
			mTerm = extras.getString(EXTRA_TERM);
			mUrl = SearchUtils.getSearchUrl(SearchUtils.SEARCH_LENGUA, mTerm);			
			suggestions.saveRecentQuery(mTerm, null);
			Constants.LogMessage(mUrl);			
			mHtmlData = DbManager.getSearchHtmlData(mUrl);
		}		

		webView = (WebView) findViewById(R.id.results_webview);
		WebSettings settings = webView.getSettings();
		settings.setJavaScriptEnabled(true);
		webView.setWebViewClient(new MyWebViewClient());

		if (mHtmlData == null || mHtmlData == "") {
			Constants.LogMessage("Data not loaded from database, calling asynctask");
			new GetSearchHtmlAsync(webView, SearchUtils.SEARCH_LENGUA).execute(mTerm);
		}
		else {
			Constants.LogMessage("Data loaded from database, loading to webview now");
			webView.loadDataWithBaseURL(mUrl, mHtmlData, SearchUtils.MIME_TYPE, SearchUtils.CHARSET, "");
		}		
	}

	/**
	 * Taken from <a href="http://developer.android.com/guide/webapps/webview.html">Android Developers</a>
	 */
	private class MyWebViewClient extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {

			Constants.LogMessage("Url clicked: " + url);

			if (url.contains(SearchUtils.RELATED_SEARCH_KEY)) {
				if (!url.startsWith("http://")) {
					//If the search is a related term, handle the search. 
					mTerm = url;
					url = SearchUtils.getSearchUrl(SearchUtils.SEARCH_LENGUA_REL, url);
				}
				else {
					mTerm = SearchUtils.getSearchTerm(SearchUtils.SEARCH_LENGUA_REL, url);	    			
				}

				Constants.LogMessage("Url is now: " + url);
				mHtmlData = DbManager.getSearchHtmlData(url);
				if (mHtmlData == null || mHtmlData == "") {
					Constants.LogMessage("Data not loaded from database, calling asynctask");
					new GetSearchHtmlAsync(view, SearchUtils.SEARCH_LENGUA_REL).execute(mTerm);
				}
				else {
					Constants.LogMessage("Data loaded from database, loading to webview now");
					view.loadDataWithBaseURL(mUrl, mHtmlData, SearchUtils.MIME_TYPE, SearchUtils.CHARSET, "");
				}
				return true;
			}	    	

			if (Uri.parse(url).getHost().equals(SearchUtils.HOST_URL)) {
				//If the search host is from RAE then let the webview load the url.
				//This never happens
				return false;
			}

			// Otherwise, the link is not for a page on my site, so launch another Activity that handles URLs
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
			startActivity(intent);
			return true;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_results, menu);
		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
		searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
		searchView.setIconifiedByDefault(false);
		return true;
	}

	@Override
	public void onNewIntent (Intent intent) {
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			String query = intent.getStringExtra(SearchManager.QUERY);
			showNewResults(query);
		}
	}

	/**
	 * Requery RAE's database for the new query term. The method will attempt to query offline db first, if no results, it will attempt to load from RAE.es
	 * @param query The term to query
	 */
	private void showNewResults(String query) {
		Constants.LogMessage(query);
		mTerm = query;
		suggestions.saveRecentQuery(mTerm, null);
		mUrl = SearchUtils.getSearchUrl(SearchUtils.SEARCH_LENGUA, mTerm);
		Constants.LogMessage(mUrl);			
		mHtmlData = DbManager.getSearchHtmlData(mUrl);
		if (mHtmlData == null || mHtmlData == "") {
			Constants.LogMessage("Data not loaded from database, calling asynctask");
			new GetSearchHtmlAsync(webView, SearchUtils.SEARCH_LENGUA).execute(mTerm);
		}
		else {
			Constants.LogMessage("Data loaded from database, loading to webview now");
			webView.loadDataWithBaseURL(mUrl, mHtmlData, SearchUtils.MIME_TYPE, SearchUtils.CHARSET, "");
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
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {		

		if (keyCode == KeyEvent.KEYCODE_SEARCH) {
			Constants.LogMessage("Handling search keyevent");
			return true;
		}

		// Check if the key event was the Back button and if there's history
		if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
			Constants.LogMessage("Handling back keyevent");
			//Check saved prefs to see if user disabled back click handling
			if (mPreferences.getString(Settings.KEY_BACK_BEHAVIOR, "search").equals("prev_word")) {
				Constants.LogMessage("Going to previous search term");
				webView.goBack();
				return true;
			}	        
		}

		return super.onKeyDown(keyCode, event);
	}

}

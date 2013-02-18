package com.lazybits.rae.movil;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.lazybits.rae.movil.tasks.GetSearchHtmlAsync;
import com.lazybits.rae.movil.utils.DbManager;
import com.lazybits.rae.movil.utils.SearchUtils;

public class Results extends Activity {
	
	public static final String EXTRA_TERM = "term";

	private WebView webView;
	private String mTerm, mUrl, mHtmlData;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_results);
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		Bundle extras = getIntent().getExtras();
		
		if (extras != null && extras.containsKey(EXTRA_TERM)) {
			mTerm = extras.getString(EXTRA_TERM);
			mUrl = SearchUtils.getSearchUrl(SearchUtils.SEARCH_LENGUA, mTerm);
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
			webView.loadDataWithBaseURL("", mHtmlData, SearchUtils.MIME_TYPE, SearchUtils.CHARSET, "");
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
	    		//If the search is a related term, handle the search. 
	    		mTerm = url;
	    		url = SearchUtils.getSearchUrl(SearchUtils.SEARCH_LENGUA_REL, url);
	    		Constants.LogMessage("Url is now: " + url);
	    	  	mHtmlData = DbManager.getSearchHtmlData(url);
	        	if (mHtmlData == null || mHtmlData == "") {
	    			Constants.LogMessage("Data not loaded from database, calling asynctask");
	    			new GetSearchHtmlAsync(view, SearchUtils.SEARCH_LENGUA_REL).execute(mTerm);
	    		}
	    		else {
	    			Constants.LogMessage("Data loaded from database, loading to webview now");
	    			view.loadDataWithBaseURL("", mHtmlData, SearchUtils.MIME_TYPE, SearchUtils.CHARSET, "");
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
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}

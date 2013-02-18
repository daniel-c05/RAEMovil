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

import com.lazybits.rae.movil.utils.SearchUtils;

public class Results extends Activity {
	
	public static final String EXTRA_TERM = "term";

	private WebView webView;
	private String url;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_results);
		
		Bundle extras = getIntent().getExtras();
		if (extras != null && extras.containsKey(EXTRA_TERM)) {
			url = SearchUtils.getSearchUrl(SearchUtils.SEARCH_LENGUA, extras.getString(EXTRA_TERM));
			Constants.LogMessage(url);
		}
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		webView = (WebView) findViewById(R.id.results_webview);	
		WebSettings settings = webView.getSettings();
		settings.setJavaScriptEnabled(true);
		webView.setWebViewClient(new MyWebViewClient());
		webView.loadUrl(url);
		
	}
	
	private class MyWebViewClient extends WebViewClient {
	    @Override
	    public boolean shouldOverrideUrlLoading(WebView view, String url) {
	        if (Uri.parse(url).getHost().equals(SearchUtils.HOST_URL)) {
	            // This is my web site, so do not override; let my WebView load the page
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
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_results, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}

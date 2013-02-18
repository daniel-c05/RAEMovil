package com.lazybits.rae.movil.tasks;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.AsyncTask;
import android.webkit.WebView;

import com.lazybits.rae.movil.Constants;
import com.lazybits.rae.movil.utils.DbManager;
import com.lazybits.rae.movil.utils.SearchUtils;

public class GetSearchHtmlAsync extends AsyncTask<String, Void, String> {
	
	String mTerm, mUrl;
	WebView mWebView;
	private int mSearchMode;
	
	public GetSearchHtmlAsync (WebView webView, int mode) {
		this.mWebView = webView;
		this.mSearchMode = mode;
	}

	@Override
	protected String doInBackground(String... params) {
		
		this.mTerm = params[0];
		this.mUrl = SearchUtils.getSearchUrl(mSearchMode, mTerm);
		
		return getHtmlFromUrl(mUrl);
	}
	
	private String getHtmlFromUrl(String url) {
		
		Constants.LogMessage("AsyncTask calling to donwnload: " + url);
		
		DefaultHttpClient client = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(url);
        String response = "";
        
        try {
            HttpResponse execute = client.execute(httpGet);
            InputStream content = execute.getEntity().getContent();

            BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
            String s = "";
            
            while ((s = buffer.readLine()) != null) {
                response += s;
              }
            
        }
        catch (Exception e) {
        	 Constants.LogMessage(e.toString());
        }
        
		return response;
	}

	@Override
	protected void onPostExecute (String htmlData) {
		Constants.LogMessage("OnPostExecute");
		//Save and associate html data to url only if the data exists. 
		if (htmlData != null && htmlData != "") {
			if (mWebView != null) {
				Constants.LogMessage("Loading to webview");
				DbManager.addSearchToDatabase(mTerm, mUrl, htmlData);
				//There is no url provided as it isn't needed.
				mWebView.loadDataWithBaseURL("", htmlData, SearchUtils.MIME_TYPE, SearchUtils.CHARSET, "");
				mWebView.scrollTo(0, 0);
			}			
		}
		else {
			Constants.LogMessage("Html data was null");
		}		
	}
	
	

}

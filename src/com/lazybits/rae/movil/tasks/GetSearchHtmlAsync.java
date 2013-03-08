package com.lazybits.rae.movil.tasks;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.webkit.WebView;
import android.widget.Toast;

import com.lazybits.rae.movil.Constants;
import com.lazybits.rae.movil.R;
import com.lazybits.rae.movil.utils.DbManager;
import com.lazybits.rae.movil.utils.SearchUtils;

public class GetSearchHtmlAsync extends AsyncTask<String, Void, String> {
	
	/*
	 * Establecemos la cantidad en milisegundos para el timeout de la conexión. 
	 */
	private static final int NETWORK_TIMEOUT = 5000;
	private static final int SOCKET_TIMEOUT = 10000;
	
	String mTerm, mUrl;
	WebView mWebView;
	View mProgress;
	Context mContext;
	private int mSearchMode;
	
	public GetSearchHtmlAsync (final Context context, final WebView webView, final View progress, int mode) {
		this.mWebView = webView;
		this.mSearchMode = mode;
		this.mProgress = progress;
		this.mContext = context;
	}

	@Override
	protected String doInBackground(String... params) {
		
		this.mTerm = params[0];
		this.mUrl = SearchUtils.getSearchUrl(mSearchMode, mTerm);
		
		return getHtmlFromUrl(mUrl);
	}
	
	private String getHtmlFromUrl(String url) {
		
		Constants.LogMessage("AsyncTask calling to donwnload: " + url);
		
		url = SearchUtils.encodePath(url);
		Constants.LogMessage("encoded url: " + url);
		
		HttpParams httpParameters = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParameters, NETWORK_TIMEOUT);
		HttpConnectionParams.setSoTimeout(httpParameters, SOCKET_TIMEOUT);
		
		DefaultHttpClient client = new DefaultHttpClient();		
		client.setParams(httpParameters);
		
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
        catch (SocketTimeoutException  socketTimeout) {
        	 handleTimout();
        }
        catch (ConnectTimeoutException  e) {
        	 handleTimout();
		}
        catch (Exception e) {
        	Constants.LogMessage(e.toString());
        }
        
		return response;
	}

	@Override
	protected void onPostExecute (String htmlData) {
		Constants.LogMessage("OnPostExecute");
		//Guarda y asocia la data solo si la data se descargo bien. 
		if (htmlData != null && htmlData != "") {
			if (mWebView != null) {
				Constants.LogMessage("Loading to webview");
				DbManager.addSearchToDatabase(mWebView.getContext(), mTerm, mSearchMode, mUrl, htmlData);
				mWebView.loadDataWithBaseURL(mUrl, htmlData, SearchUtils.MIME_TYPE, SearchUtils.CHARSET, "");
				mWebView.scrollTo(0, 0);
			}			
		}
		else {
			String noText = mWebView.getContext().getResources().getString(R.string.label_no_html);
			mWebView.loadData(noText, "text/html", null);
			Constants.LogMessage("Html data was null");
		}	
		SearchUtils.showProgress(mContext, mProgress, mWebView, false);
	}
	
	/**
	 * Alerta al usuario que la conexión esta tomando mucho tiempo, y que debería volver a intentar la búsqueda.
	 */
	private void handleTimout () {
		Toast.makeText(mContext, mContext.getString(R.string.error_connection_timeout), Toast.LENGTH_SHORT).show();
	}
	
	

}

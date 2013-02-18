package com.lazybits.rae.movil;

import com.lazybits.rae.movil.utils.DbManager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class Home extends Activity implements OnEditorActionListener {

	private Button searchButton, clearButton;
	private EditText searchInput;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		setupViews();
		DbManager.open(this);
	}

	private void setupViews() {
		searchButton = (Button) findViewById(R.id.home_search);
		clearButton = (Button) findViewById(R.id.home_clear);
		searchInput = (EditText) findViewById(R.id.home_input);
		
		searchInput.setOnEditorActionListener(this);	

		searchButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showSearchResults();	
			}
		});		

		clearButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {				
				searchInput.setText("");			
			}
		});
		
		getWindow().setSoftInputMode(
                LayoutParams.SOFT_INPUT_STATE_VISIBLE);	
	
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_home, menu);
		return true;
	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		if (EditorInfo.IME_ACTION_SEARCH == actionId) {
			Constants.LogMessage("Handling search via Action Done");
			showSearchResults();
			return true;
		}
		return false;
	}

	private void showSearchResults() {
		String text = searchInput.getText().toString();
		if (text != null && text.length() > 3) {
			Intent search = new Intent(Home.this, Results.class);
			search.putExtra(Results.EXTRA_TERM, text);
			startActivity(search);
		}
		else {
			Toast.makeText(Home.this, "Favor ingrese una palabra de mas de 3 letras", Toast.LENGTH_SHORT).show();
		}	
	}	

}

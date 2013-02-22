package com.lazybits.rae.movil;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.lazybits.rae.movil.utils.DbManager;
import com.lazybits.rae.movil.utils.SearchUtils;

public class Home extends Activity implements OnEditorActionListener {

	private Resources mResources;
	private Button searchButton, clearButton;
	private EditText searchInput;
	int searchMode = SearchUtils.SEARCH_LENGUA;
	int searchModeRel = SearchUtils.SEARCH_LENGUA_REL;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		setupViews();
		DbManager.open(this);
		mResources = getResources();
	}

	/**
	 * Initializa la interfaz de usuario. 
	 */
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
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_settings:
			Intent settings = new Intent(this, Settings.class);
			startActivity(settings);
			return true;
		case R.id.menu_change_dictionary:
			showChangeDictionary();
			return true;
		default:
			return false;
		}
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
			search.putExtra(Results.EXTRA_SAERCH_MODE, searchMode);
			search.putExtra(Results.EXTRA_SAERCH_MODE_REL, searchModeRel);
			startActivity(search);
		}
		else {
			Toast.makeText(Home.this, mResources.getString(R.string.label_not_enough_chars), Toast.LENGTH_SHORT).show();
		}	
	}	

	protected void showChangeDictionary() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);    		
		builder.setSingleChoiceItems(R.array.array_dictionary_selecton, searchMode -1, new DialogInterface.OnClickListener() {

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

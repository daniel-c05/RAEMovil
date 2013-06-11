package com.lazybits.rae.movil;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter.CursorToStringConverter;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.ads.AdRequest;
import com.google.ads.AdView;
import com.lazybits.rae.movil.utils.DBHelper;
import com.lazybits.rae.movil.utils.DbManager;
import com.lazybits.rae.movil.utils.SearchUtils;

@SuppressLint("DefaultLocale")
public class Home extends SherlockActivity implements OnEditorActionListener {

	private Resources mResources;
	private SimpleCursorAdapter mSuggestionsAdapter;
	private Button searchButton, clearButton;
	private AutoCompleteTextView searchInput;
	private AdView mAdView;
	int searchMode = SearchUtils.SEARCH_LENGUA;
	int searchModeRel = SearchUtils.SEARCH_LENGUA_REL;

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
				searchInput.dismissDropDown();
				mSuggestionsAdapter.swapCursor(null);				
			}
			else {
				mSuggestionsAdapter.swapCursor(DbManager.getSearchSuggestions(Home.this, searchMode, s.toString()));				
			}
			mSuggestionsAdapter.notifyDataSetChanged();
			searchInput.setAdapter(mSuggestionsAdapter);
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		setupViews();
		setupAds();
		mResources = getResources();
	}

	@Override
	protected void onDestroy () {
		mSuggestionsAdapter = null;
		searchInput.removeTextChangedListener(watcher);
		super.onDestroy();
	}

	private void setupAds() {
		mAdView = (AdView)this.findViewById(R.id.adView);
		mAdView.loadAd(new AdRequest());
	}

	/**
	 * Inicializa la interfaz de usuario. 
	 */
	private void setupViews() {
		searchButton = (Button) findViewById(R.id.home_search);
		clearButton = (Button) findViewById(R.id.home_clear);
		searchInput = (AutoCompleteTextView) findViewById(R.id.home_input);

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
			searchInput.setAdapter(mSuggestionsAdapter);
		}		
		searchInput.addTextChangedListener(watcher);
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

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.activity_home, menu);
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
	public boolean onKeyDown(int keyCode, KeyEvent event) {		

		if ((keyCode == KeyEvent.KEYCODE_SEARCH)) {
			showSoftKeyboard(searchInput);
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	public void showSoftKeyboard(View view) {
		if (view.requestFocus()) {
			InputMethodManager imm = (InputMethodManager)
					getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
		}
	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		if (EditorInfo.IME_ACTION_SEARCH == actionId) {
			Constants.LogMessage("Handling search via Action Done");
			showSearchResults();
			return true;
		}
		//Cuando se oprime enter
		else if (EditorInfo.IME_NULL == actionId && event.getAction() == KeyEvent.ACTION_DOWN) {
			Constants.LogMessage("Handling search via Enter");
			showSearchResults();
			return true;
		}
		return false;
	}

	private void showSearchResults() {
		String text = searchInput.getText().toString();
		if (text != null && text.length() >= 1) {
			Intent search = new Intent(Home.this, Results.class);
			//Evitamos busquedas duplicadas pasando el termino a minusculas.
			search.putExtra(Results.EXTRA_TERM, text.toLowerCase());
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

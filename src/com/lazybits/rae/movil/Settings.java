package com.lazybits.rae.movil;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.provider.SearchRecentSuggestions;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;

import com.lazybits.rae.movil.utils.DbManager;
import com.lazybits.rae.movil.utils.SearchSuggestionsProvider;

public class Settings extends PreferenceActivity {

	public static final String KEY_BACK_BEHAVIOR = "pref_title_back_behaviour";
	public static final String KEY_DELETE_HISTORY = "pref_title_clear_history";

	Preference mSmsLimitPref;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().setDisplayHomeAsUpEnabled(true);		
		loadPrefs();			
	}

	@SuppressWarnings("deprecation")
	private void loadPrefs() {
		addPreferencesFromResource(R.xml.preferences);   	
		mSmsLimitPref = findPreference(KEY_DELETE_HISTORY);
		bindPreferenceSummaryToValue(findPreference(KEY_BACK_BEHAVIOR));		
	}

	private static OnPreferenceChangeListener mChangeListener = new OnPreferenceChangeListener() {

		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			Resources mResources = preference.getContext().getResources();

			String stringValue = newValue.toString();

			if (preference instanceof ListPreference) {
				Constants.LogMessage(stringValue);
				// For list preferences, look up the correct display value in
				// the preference's 'entries' list.
				ListPreference listPreference = (ListPreference) preference;
				int index = listPreference.findIndexOfValue(stringValue);
				String summary = "";
				switch (index) {
				case 0:
					summary = mResources.getString(R.string.pref_summary_behaviour_back_to_search);
					break;
				case 1:
					summary = mResources.getString(R.string.pref_summary_behaviour_back_to_prev_word);
				default:

					break;
				}
				preference
				.setSummary(summary);
			} 
			return true;
		}
	};

	@SuppressWarnings("deprecation")
	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {
		if (preference == mSmsLimitPref) {
			//Show a dialog that ensures user doesn't clear database by mistake.
			showDeleteHistoryDialog();
		}
		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}

	private static void bindPreferenceSummaryToValue(Preference preference) {
		preference.setOnPreferenceChangeListener(mChangeListener);

		mChangeListener.onPreferenceChange(
				preference,
				PreferenceManager.getDefaultSharedPreferences(
						preference.getContext()).getString(preference.getKey(),
								""));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_settings, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		case R.id.menu_about:
			showAboutDialog();
		}
		return super.onOptionsItemSelected(item);
	}

	protected void showDeleteHistoryDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);    		
		builder.setMessage(R.string.label_delete_history_alert)
		.setPositiveButton(android.R.string.ok, new OnClickListener() {				
			@Override
			public void onClick(DialogInterface dialog, int which) {
				DbManager.deleteSearchHistory();
				SearchRecentSuggestions suggestions = new SearchRecentSuggestions(Settings.this,
						SearchSuggestionsProvider.AUTHORITY, SearchSuggestionsProvider.MODE);
				suggestions.clearHistory();
				dialog.dismiss();
			}
		})
		.setCancelable(true).setNegativeButton(android.R.string.cancel, new OnClickListener() {				
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	protected void showAboutDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);  		
		builder.setMessage(R.string.about_author)
		.setTitle(R.string.app_name)
		.setCancelable(true).setNegativeButton(R.string.button_close, new OnClickListener() {				
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		AlertDialog dialog = builder.create();
		dialog.show();
	}

}

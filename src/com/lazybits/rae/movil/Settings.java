package com.lazybits.rae.movil;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
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
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.lazybits.rae.movil.utils.DbManager;
import com.lazybits.rae.movil.utils.SearchSuggestionsProvider;

@SuppressLint("ValidFragment")
public class Settings extends PreferenceActivity {

	public static final String KEY_BACK_BEHAVIOR = "pref_title_back_behaviour";
	public static final String KEY_DELETE_HISTORY = "pref_title_clear_history";

	Preference deleteHistoryPref;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().setDisplayHomeAsUpEnabled(true);		
		loadPrefs();			
	}

	@SuppressWarnings("deprecation")
	private void loadPrefs() {
		addPreferencesFromResource(R.xml.preferences);   	
		deleteHistoryPref = findPreference(KEY_DELETE_HISTORY);
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
		if (preference == deleteHistoryPref) {
			//Muestra un dialogo para evitar que el usuario borre la base de datos por error.
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
			showCustomAboutDialog();
		}
		return super.onOptionsItemSelected(item);
	}

	protected void showDeleteHistoryDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);    		
		builder.setMessage(R.string.label_delete_history_alert)
		.setPositiveButton(android.R.string.ok, new OnClickListener() {				
			@Override
			public void onClick(DialogInterface dialog, int which) {
				DbManager.deleteSearchHistory(Settings.this);
				SearchRecentSuggestions suggestions = new SearchRecentSuggestions(Settings.this,
						SearchSuggestionsProvider.AUTHORITY, SearchSuggestionsProvider.MODE);
				suggestions.clearHistory();
				dialog.dismiss();
			}
		})
		.setCancelable(true).setNegativeButton(R.string.button_cancel, new OnClickListener() {				
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	protected void showCustomAboutDialog () {
		FragmentManager fm = getFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		Fragment prev = fm.findFragmentByTag("dialog_about");
		if (prev != null) {
			ft.remove(prev);
		}
		ft.addToBackStack(null);

		new AboutDialog().show(ft, "dialog_about");
	}

	/**
	 * Tomado y modificado a partir de <a href="http://code.google.com/p/dashclock/source/browse/main/src/com/google/android/apps/dashclock/HelpUtils.java">here.</a>
	 * @author Roman Nurik
	 *
	 */
	private class AboutDialog extends DialogFragment {

		private static final String VERSION_UNAVAILABLE = "N/A";

		public AboutDialog() {
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			LayoutInflater layoutInflater = getActivity().getLayoutInflater();
			View rootView = layoutInflater.inflate(R.layout.dialog_about, null);
			
			PackageManager pm = getActivity().getPackageManager();
			//Nombre del paquete: "RAE Movil"
			String packageName = getActivity().getPackageName();
			String versionName;
			try {
				PackageInfo info = pm.getPackageInfo(packageName, 0);
				//Nombre de la version tomado del manifest
				versionName = info.versionName;
			} catch (PackageManager.NameNotFoundException e) {
				versionName = VERSION_UNAVAILABLE;
			}

			TextView header = (TextView) rootView.findViewById(
					R.id.about_title);
			header.setText(Html.fromHtml(getString(R.string.about_title, versionName)));

			TextView body = (TextView) rootView.findViewById(R.id.about_body);
			body.setText(Html.fromHtml(getString(R.string.about_details)));
			body.setMovementMethod(new LinkMovementMethod());

			return new AlertDialog.Builder(getActivity())
			.setView(rootView)
			.setPositiveButton(R.string.button_close,
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					dialog.dismiss();
				}
			}
					)
					.create();
		}

	}
}

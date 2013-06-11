package com.lazybits.rae.movil;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.support.v4.app.NavUtils;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;
import com.lazybits.rae.movil.utils.DbManager;

@SuppressLint("ValidFragment")
public class Settings extends SherlockPreferenceActivity {

	private static final String VERSION_UNAVAILABLE = "N/A";
	
	public static final String KEY_DELETE_HISTORY = "pref_title_clear_history";

	Preference deleteHistoryPref, aboutVersionPref, sourceCodePref, feedbackPref, reportProblemPref, removeAdsPref;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);		
		loadPrefs();				
	}

	@SuppressWarnings("deprecation")
	private void loadPrefs() {
		addPreferencesFromResource(R.xml.preferences);   	
		deleteHistoryPref = findPreference(KEY_DELETE_HISTORY);
		aboutVersionPref = findPreference(getString(R.string.pref_key_version));
		sourceCodePref = findPreference(getString(R.string.pref_key_about_source));
		feedbackPref = findPreference(getString(R.string.pref_key_send_feedback));
		reportProblemPref = findPreference(getString(R.string.pref_key_report_problem));
		removeAdsPref = findPreference(getString(R.string.pref_key_remove_ads));
		updateBuildVersionInfo();
	}
	
	private void updateBuildVersionInfo() {
		PackageManager pm = getPackageManager();
		String packageName = getPackageName();
		String versionName;
		try {
			PackageInfo info = pm.getPackageInfo(packageName, 0);
			versionName = info.versionName;
		} catch (PackageManager.NameNotFoundException e) {
			versionName = VERSION_UNAVAILABLE;
		}
		
		aboutVersionPref.setSummary(packageName + " v" + versionName);
	}	
	
	
	
	private void launchWebUrl(int urlRes) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse(getString(urlRes)));
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
		startActivity(intent);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {
		if (preference == deleteHistoryPref) {
			//Muestra un dialogo para evitar que el usuario borre la base de datos por error.
			showDeleteHistoryDialog();
		}
		else if (preference == sourceCodePref) {
			launchWebUrl(R.string.pref_source_url);
		}
		else if (preference == feedbackPref) {
			launchWebUrl(R.string.pref_feedback_app_link);
		}
		else if (preference == reportProblemPref) {
			startEmail();
		}
		else if (preference == removeAdsPref) {
			launchWebUrl(R.string.pref_remove_ads_link);
		}
		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}
	
	private void startEmail() {
		Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
	            "mailto", getString(R.string.pref_email_address), null));
		intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.pref_email_subject));
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
		startActivity(Intent.createChooser(intent, getString(R.string.pref_title_report_problem)));
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

	protected void showDeleteHistoryDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);    		
		builder.setMessage(R.string.label_delete_history_alert)
		.setPositiveButton(android.R.string.ok, new OnClickListener() {				
			@Override
			public void onClick(DialogInterface dialog, int which) {
				DbManager.deleteSearchHistory(Settings.this);
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
}

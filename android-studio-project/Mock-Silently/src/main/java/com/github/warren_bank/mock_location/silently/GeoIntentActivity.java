package com.github.warren_bank.mock_location.silently;

import com.github.warren_bank.mock_location.silently.R;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

public class GeoIntentActivity extends Activity {
    private int purpose;
    private int trip_duration_seconds;
    private boolean force_start;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        onNewIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (intent != null) {
            loadSettings();

            Intent newIntent = new Intent(intent);

            newIntent.setAction("android.intent.action.VIEW");
            newIntent.setClassName("com.github.warren_bank.mock_location", "com.github.warren_bank.mock_location.ui.GeoIntentActivity");
            newIntent.putExtra("silent_update", true);
            newIntent.putExtra("purpose", purpose);
            newIntent.putExtra("trip_duration_seconds", trip_duration_seconds);
            newIntent.putExtra("force_start", force_start);

            startActivity(newIntent);
        }

        finish();
    }

    private void loadSettings() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        purpose = Integer.parseInt(
            preferences.getString(
                getString(R.string.pref_purpose_key),
                getString(R.string.pref_purpose_default)
            )
        );
        trip_duration_seconds = Integer.parseInt(
            preferences.getString(
                getString(R.string.pref_trip_duration_seconds_key),
                getString(R.string.pref_trip_duration_seconds_default)
            )
        );
        force_start = preferences.getBoolean(
            getString(R.string.pref_force_start_key),
            Boolean.parseBoolean(
                getString(R.string.pref_force_start_default)
            )
        );
    }
}

package com.github.warren_bank.mock_location.silently;

import com.github.warren_bank.mock_location.silently.R;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity {
    private Preference purpose;
    private Preference force_start;
    private Preference trip_duration_seconds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        purpose = findPreference(
            getString(R.string.pref_purpose_key)
        );
        force_start = findPreference(
            getString(R.string.pref_force_start_key)
        );
        trip_duration_seconds = findPreference(
            getString(R.string.pref_trip_duration_seconds_key)
        );

        initPurpose();
    }

    private void initPurpose() {
        updatePurpose(
            purpose.getSharedPreferences().getString(
                getString(R.string.pref_purpose_key),
                getString(R.string.pref_purpose_default)
            )
        );

        purpose.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                updatePurpose(
                    newValue.toString()
                );
                return true;
            }
        });
    }

    private void updatePurpose(String newValue) {
        int purpose_value = Integer.parseInt(newValue.toString(), 10);

        switch(purpose_value) {
            case 1:
                force_start.setEnabled(true);
                trip_duration_seconds.setEnabled(false);
                break;
            case 3:
                force_start.setEnabled(false);
                trip_duration_seconds.setEnabled(true);
                break;
        }
    }
}

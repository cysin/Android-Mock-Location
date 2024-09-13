package com.github.warren_bank.mock_location.ui;

// based on:
//   https://github.com/osmandapp/Osmand/blob/2.0.0/OsmAnd/src/net/osmand/plus/activities/search/GeoIntentActivity.java

import com.github.warren_bank.mock_location.R;
import com.github.warren_bank.mock_location.data_model.LocPoint;
import com.github.warren_bank.mock_location.data_model.SharedPrefs;
import com.github.warren_bank.mock_location.service.LocationService;
import com.github.warren_bank.mock_location.util.GeoPointParserUtil;
import com.github.warren_bank.mock_location.util.GeoPointParserUtil.GeoParsedPoint;

import android.app.Activity;
import android.app.AlertDialog;
import android.net.Uri;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

public class GeoIntentActivity extends Activity {
    private final static String EXTRA_PURPOSE               = "purpose";
    private final static String EXTRA_SILENT_UPDATE         = "silent_update";
    private final static String EXTRA_TRIP_DURATION_SECONDS = "trip_duration_seconds";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        onNewIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (intent == null) {
            handleNoData(null);
            return;
        }

        final Uri uri = intent.getData();
        if (uri == null) {
            handleNoData(null);
            return;
        }

        final String uriString = uri.toString();
        final GeoParsedPoint geoPoint = GeoPointParserUtil.parse(uriString);
        if (geoPoint == null) {
            handleNoData(getString(R.string.error_geo_intent, uriString));
            return;
        }
        if (!geoPoint.isGeoPoint()) {
            handleNoData(getString(R.string.error_geo_intent, uriString));
            return;
        }

        final LocPoint point = new LocPoint(
            geoPoint.getLatitude(),
            geoPoint.getLongitude()
        );

        boolean handled = false;
        if (intent.hasExtra(EXTRA_PURPOSE)) {
            int purpose               = intent.getIntExtra(EXTRA_PURPOSE, 0);
            boolean silent_update     = intent.getBooleanExtra(EXTRA_SILENT_UPDATE, false);
            int trip_duration_seconds = intent.getIntExtra(EXTRA_TRIP_DURATION_SECONDS, 60);
            handled = handleGeoDataPurpose(point, purpose, silent_update, trip_duration_seconds);
        }

        if (!handled)
            handleGeoData(point);
    }

    private void handleNoData(String error_message) {
        startMainActivity(error_message);
    }

    private void handleGeoData(LocPoint point) {
        AlertDialog.Builder builder = new AlertDialog.Builder(GeoIntentActivity.this);
        builder.setTitle(R.string.geo_intent_menu_title);
        builder.setItems(R.array.geo_intent_menu_options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int purpose = which + 1;
                boolean handled = handleGeoDataPurpose(point, purpose, false, 0);

                if (handled)
                  dialog.dismiss();
            }
        });
        builder.show();
    }

    private boolean handleGeoDataPurpose(LocPoint point, int purpose, boolean silent_update, int trip_duration_seconds) {
        switch (purpose) {
            case 1:
                // fixed position
                if (silent_update && LocationService.isStarted()) {
                  LocationService.getLocationThreadManager().jumpToLocation(point);
                  finish();
                }
                else {
                  handleFixedPosition(point);
                }
                return true;
            case 2:
                // trip origin
                handleTripOrigin(point);
                return true;
            case 3:
                // trip destination
                if (silent_update && LocationService.isStarted()) {
                  LocationService.getLocationThreadManager().flyToLocation(point, trip_duration_seconds);
                  finish();
                }
                else {
                  handleTripDestination(point);
                }
                return true;
            case 4:
                // new bookmark
                handleNewBookmark(point);
                return true;
        }
        return false;
    }

    private void handleFixedPosition(LocPoint point) {
        SharedPrefs.putTripOrigin(GeoIntentActivity.this, point);
        startMainActivity();
    }

    private void handleTripOrigin(LocPoint point) {
        SharedPrefs.putTripOrigin(GeoIntentActivity.this, point);
        startMainActivity(2);
    }

    private void handleTripDestination(LocPoint point) {
        SharedPrefs.putTripDestination(GeoIntentActivity.this, point);
        startMainActivity(2);
    }

    private void handleNewBookmark(LocPoint point) {
        startBookmarksActivity(point);
    }

    private void startMainActivity() {
        startMainActivity(0, null);
    }

    private void startMainActivity(int tab) {
        startMainActivity(tab, null);
    }

    private void startMainActivity(String toast) {
        startMainActivity(0, toast);
    }

    private void startMainActivity(int tab, String toast) {
        Intent intent = new Intent(GeoIntentActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        if (tab > 0) {
            String current_tab_tag = (tab == 1)
                ? getString(R.string.MainActivity_tab_1_tag)
                : getString(R.string.MainActivity_tab_2_tag)
            ;

            intent.putExtra(getString(R.string.MainActivity_extra_current_tab_tag), current_tab_tag);
        }
        if (toast != null) {
            intent.putExtra(getString(R.string.MainActivity_extra_toast), toast);
        }
        startActivity(intent);
    }

    private void startBookmarksActivity(LocPoint point) {
        double lat = point.getLatitude();
        double lon = point.getLongitude();

        Intent intent = new Intent(GeoIntentActivity.this, BookmarksActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(getString(R.string.BookmarksActivity_extra_add_lat), lat);
        intent.putExtra(getString(R.string.BookmarksActivity_extra_add_lon), lon);
        startActivity(intent);
    }

}

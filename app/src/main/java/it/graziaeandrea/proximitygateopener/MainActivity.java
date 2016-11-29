package it.graziaeandrea.proximitygateopener;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements
        ConnectionCallbacks, OnConnectionFailedListener, ResultCallback<Status> {
    private final String TAG = getClass().getSimpleName();
    private List<Geofence> mFences = new ArrayList<>();

    // Properties of the geofence.
    private static final String LAT = "0.0";
    private static final String LNG = "0.0";
    private static final String DEFAULT_RADIUS_METERS = "50";
    private static final String DEFAULT_DELAY_MILLISECONDS = "3000";
    private static final String NAME = "GEOFENCE";

    // Whether the geofencing is active or not.
    private boolean mGeofencingActive = false;

    private GoogleApiClient mGoogleApiClient;
    private PendingIntent mGeofencePendingIntent;

    private ToggleButton mToggleButton;
    private EditText mPhoneNumberEditText;
    private EditText mLatitudeEditText;
    private EditText mLongitudeEditText;
    private EditText mRadiusEditText;
    private EditText mLoiteringDelayTimeEditText;
    private Button mSaveButton;
    private SharedPreferences mPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPreferences = getSharedPreferences(String.valueOf(R.string.preferences), MODE_PRIVATE);
        mPhoneNumberEditText = (EditText) findViewById(R.id.phoneNumberEditText);
        mLatitudeEditText = (EditText) findViewById(R.id.latEditText);
        mLongitudeEditText = (EditText) findViewById(R.id.lngEditText);
        mRadiusEditText = (EditText) findViewById(R.id.radiusEditText);
        mLoiteringDelayTimeEditText = (EditText) findViewById(R.id.delayTimeEditText);
        mSaveButton = (Button) findViewById(R.id.saveButton);
        mToggleButton = (ToggleButton) findViewById(R.id.statusToggleButton);
        mToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mPhoneNumberEditText.getText().toString().isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Enter phone number", Toast.LENGTH_LONG).show();
                    mToggleButton.setChecked(false);
                    return;
                } else if (mToggleButton.isChecked()) {
                    startProximityLookup();
                } else {
                    stopProximityLookup();
                }
            }
        });
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phoneNumber = mPhoneNumberEditText.getText().toString();
                String latitude = mLatitudeEditText.getText().toString();
                String longitude = mLongitudeEditText.getText().toString();
                String radius = mRadiusEditText.getText().toString();
                String loiteringDelay = mLoiteringDelayTimeEditText.getText().toString();

                if(!phoneNumber.equals("")) {
                    mPreferences.edit().putString(String.valueOf(R.string.phoneNumberPreference), phoneNumber).apply();
                } else {
                    Toast.makeText(getApplicationContext(), "Enter phone number", Toast.LENGTH_LONG).show();
                    return;
                }

                if(!latitude.equals("")) {
                    mPreferences.edit().putString(String.valueOf(R.string.latitudePreference), latitude).apply();
                } else {
                    Toast.makeText(getApplicationContext(), "Enter destination Latitude", Toast.LENGTH_LONG).show();
                    return;
                }

                if(!longitude.equals("")) {
                    mPreferences.edit().putString(String.valueOf(R.string.longitudePreference), longitude).apply();
                } else {
                    Toast.makeText(getApplicationContext(), "Enter destination Longitude", Toast.LENGTH_LONG).show();
                    return;
                }

                if(!radius.equals("")) {
                    mPreferences.edit().putString(String.valueOf(R.string.radiusPreference), radius).apply();
                } else {
                    Toast.makeText(getApplicationContext(), "Enter geofence radius", Toast.LENGTH_LONG).show();
                    return;
                }

                if(!loiteringDelay.equals("")) {
                    mPreferences.edit().putString(String.valueOf(R.string.loiteringDelayPreference), loiteringDelay).apply();
                } else {
                    Toast.makeText(getApplicationContext(), "Enter loitering delay", Toast.LENGTH_LONG).show();
                    return;
                }

                Geofence geofence = new Geofence.Builder()
                        .setRequestId(NAME)
                        .setCircularRegion(Double.parseDouble(latitude), Double.parseDouble(longitude), Float.parseFloat(radius))
                        .setExpirationDuration(Geofence.NEVER_EXPIRE)
                        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_DWELL)
                        .setLoiteringDelay(Integer.parseInt(loiteringDelay))
                        .build();
                mFences.clear();
                mFences.add(geofence);
            }
        });
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

    }

    @Override
    protected void onResume() {
        super.onResume();
        String number = mPreferences.getString(String.valueOf(R.string.phoneNumberPreference), "");
        String latitude = mPreferences.getString(String.valueOf(R.string.latitudePreference), "");
        String longitude = mPreferences.getString(String.valueOf(R.string.longitudePreference), "");
        String radius = mPreferences.getString(String.valueOf(R.string.radiusPreference), "");
        String loiteringDelay = mPreferences.getString(String.valueOf(R.string.loiteringDelayPreference), "");

        if(!number.isEmpty()) {
            mPhoneNumberEditText.setText(number);
        }

        if(!latitude.isEmpty()) {
            mLatitudeEditText.setText(latitude);
        } else {
            mLatitudeEditText.setText(LAT);
        }

        if(!longitude.isEmpty()) {
            mLongitudeEditText.setText(longitude);
        } else {
            mLongitudeEditText.setText(LNG);
        }

        if(!radius.isEmpty()) {
            mRadiusEditText.setText(radius);
        } else {
            mRadiusEditText.setText(DEFAULT_RADIUS_METERS);
        }

        if(!loiteringDelay.isEmpty()) {
            mLoiteringDelayTimeEditText.setText(loiteringDelay);
        } else {
            mLoiteringDelayTimeEditText.setText(DEFAULT_DELAY_MILLISECONDS);
        }
    }

    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d(TAG, "Connected to GoogleApiClient");

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Permissions insufficient.");
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location loc = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (loc != null) {
            Log.d(TAG, String.format("Current location (%f, %f).", loc.getLatitude(), loc.getLongitude()));
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason.
        Log.d(TAG, "Connection suspended");
        // onConnected() will be called again automatically when the service reconnects
    }

    private PendingIntent getGeofencePendingIntent() {

        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void stopProximityLookup() {
        Log.d(TAG, "stopProximityLookup");

        // TODO: this should stop any listening Geofences.
    }

    private void startProximityLookup() {
        Log.d(TAG, "startProximityLookup");

        if (!mGoogleApiClient.isConnected()) {
            Toast.makeText(this, getString(R.string.not_connected), Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    getGeofencingRequest(),
                    getGeofencePendingIntent()
            ).setResultCallback(this); // Will call onResult().
        } catch (SecurityException e) {
            Log.e(TAG, "Missing permissions: " + e.getMessage());
        }
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();

        // DWELL will make the Location Services trigger the event only if the user stops within
        // the Geofence for some time.
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_DWELL);
        builder.addGeofences(mFences);
        return builder.build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onResult(@NonNull Status status) {
        if (status.isSuccess()) {
            mGeofencingActive = !mGeofencingActive;

            Toast.makeText(
                    this,
                    getString(mGeofencingActive ? R.string.geofence_active :
                            R.string.geofence_inactive),
                    Toast.LENGTH_SHORT
            ).show();
        }
    }
}

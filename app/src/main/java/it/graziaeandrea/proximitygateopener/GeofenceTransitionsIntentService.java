package it.graziaeandrea.proximitygateopener;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

public class GeofenceTransitionsIntentService extends IntentService {
    private static final String TAG = "GeofenceIntentService";

    public GeofenceTransitionsIntentService() {
        super("GeoFenceService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        MainActivity.log(TAG, "onHandleIntent");
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        if (geofencingEvent.hasError()) {
            String errorMessage = R.string.geofence_error + " " + geofencingEvent.getErrorCode();
            MainActivity.log(TAG, errorMessage);
            return;
        }
        String phoneNumber = getSharedPreferences(String.valueOf(R.string.preferences), MODE_PRIVATE).getString(String.valueOf(R.string.phoneNumberPreference), "");

        if(phoneNumber.isEmpty()) {
            String message = "No phone number saved";
            MainActivity.log(TAG, message);
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG);
            return;
        }
        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();
        MainActivity.log(TAG, "geofenceTransition: "+geofenceTransition);

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL) {
            MainActivity.log(TAG, "geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL");
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            callIntent.setData(Uri.parse("tel:" + phoneNumber));

            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                MainActivity.log(TAG, "Permissions not granted");
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            MainActivity.log(TAG, "Permission granted, starting call...");
            getApplicationContext().startActivity(callIntent);
        } else {
            MainActivity.log(TAG, "Invalid geofence error type.");
        }
    }
}

package in.errorlabs.trackmebuddy.Activities;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONArrayRequestListener;

import org.json.JSONArray;

import java.util.concurrent.TimeUnit;

import in.errorlabs.trackmebuddy.Configs.UrlConfigs;
import in.errorlabs.trackmebuddy.Helpers.SharedPrefs;
import in.errorlabs.trackmebuddy.R;
import okhttp3.OkHttpClient;


public class GPS_Service extends Service {

    private LocationListener listener;
    private LocationManager locationManager;
    SharedPrefs sharedPrefs;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {

        sharedPrefs = new SharedPrefs(this);

            listener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    String lat = String.valueOf(location.getLatitude());
                    String lng = String.valueOf(location.getLongitude());
                    uploadtoserver(lat,lng);
                    Intent i = new Intent("location_update");
                    i.putExtra("coordinates",location.getLongitude()+" "+location.getLatitude());
                    sendBroadcast(i);
                }

                @Override
                public void onStatusChanged(String s, int i, Bundle bundle) {

                }

                @Override
                public void onProviderEnabled(String s) {

                }

                @Override
                public void onProviderDisabled(String s) {
                        Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                }
            };

        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,3000,0,listener);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(locationManager != null){
            locationManager.removeUpdates(listener);
        }
    }

    public void uploadtoserver(final String lat, final String lng){

        final String appkey = getString(R.string.APPKEY);
        final String phno = sharedPrefs.getLogedInPhoneNumber();

        Runnable newthread = new Runnable() {
            @Override
            public void run() {

                OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                        .connectTimeout(120, TimeUnit.SECONDS)
                        .readTimeout(120, TimeUnit.SECONDS)
                        . writeTimeout(120, TimeUnit.SECONDS)
                        .build();
                AndroidNetworking.post(UrlConfigs.UploadCoordinates_Url)
                        .addBodyParameter("APPKEY",appkey)
                        .addBodyParameter("Latitude", lat)
                        .addBodyParameter("Longitude", lng)
                        .addBodyParameter("PhoneNumber", phno)
                        .addBodyParameter("Status", "1")
                        .setPriority(Priority.MEDIUM)
                        .setOkHttpClient(okHttpClient)
                        .build()
                        .getAsJSONArray(new JSONArrayRequestListener() {
                            @Override
                            public void onResponse(JSONArray response) {
                                Log.d("Response","LOG"+response.toString());
                            }
                            @Override
                            public void onError(ANError anError) {
                                Log.d("Response","LOG"+anError.toString());
                            }
                        });
            }
        };

        Thread t = new Thread(newthread);
        t.start();

    }

}

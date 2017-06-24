package in.errorlabs.trackmebuddy.Activities;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONArrayRequestListener;

import net.steamcrafted.loadtoast.LoadToast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import in.errorlabs.trackmebuddy.Configs.UrlConfigs;
import in.errorlabs.trackmebuddy.Helpers.SharedPrefs;
import in.errorlabs.trackmebuddy.R;
import okhttp3.OkHttpClient;

public class HomeScreen extends AppCompatActivity{

    ImageView start,stop;
    SharedPrefs sharedPrefs;
    private BroadcastReceiver broadcastReceiver;
    Context context;
    LoadToast loadToast;


    @Override
    protected void onResume() {
        super.onResume();
        if(broadcastReceiver == null){
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String i =intent.getExtras().getString("coordinates");
                }
            };
        }
        registerReceiver(broadcastReceiver,new IntentFilter("location_update"));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);
        start = (ImageView) findViewById(R.id.starttracking);
        stop = (ImageView) findViewById(R.id.stoptracking);
        sharedPrefs = new SharedPrefs(this);
        loadToast = new LoadToast(this);
        if(!runtime_permissions()){
            enable_buttons();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.logout) {
            sharedPrefs.clearprefs();
            startActivity(new Intent(getApplicationContext(),Splash.class));
        }

        return super.onOptionsItemSelected(item);
    }

    private void enable_buttons() {

         if (sharedPrefs.gettrackstatus()==null){
            stop.setVisibility(View.INVISIBLE);
            start.setVisibility(View.VISIBLE);
        }else {
            start.setVisibility(View.INVISIBLE);
            stop.setVisibility(View.VISIBLE);
        }

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final AlertDialog.Builder alert = new AlertDialog.Builder(HomeScreen.this);
                final EditText edittext = new EditText(HomeScreen.this);
                alert.setMessage("Enter Your Destination");
                alert.setTitle("TrackMeBuddy");
                alert.setView(edittext);
                alert.setPositiveButton("Proceed", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        String destination = edittext.getText().toString();
                        if (destination.isEmpty() || destination.length()<0){
                            Toast.makeText(getApplicationContext(),"Enter a valid Destination",Toast.LENGTH_LONG).show();
                        }else {
                            updatedestination(destination);
                        }
                    }
                });
                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                });

                alert.show();
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sharedPrefs.disabletrackstatus();
                Toast.makeText(getApplicationContext(),"Tracking Disabled",Toast.LENGTH_LONG).show();
                stop.setVisibility(View.INVISIBLE);
                start.setVisibility(View.VISIBLE);
                StopUpload();
                Intent i = new Intent(getApplicationContext(),GPS_Service.class);
                stopService(i);
            }
        });
    }

    private boolean runtime_permissions() {
        if(Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},100);
            return true;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 100){
            if( grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                enable_buttons();
            }else {
                runtime_permissions();
            }
        }
    }

    public void StopUpload(){
        String appkey = getString(R.string.APPKEY);
        String phno = sharedPrefs.getLogedInPhoneNumber();

        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(120, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                . writeTimeout(120, TimeUnit.SECONDS)
                .build();
        AndroidNetworking.post(UrlConfigs.StopUpload_Url)
                .addBodyParameter("APPKEY",appkey)
                .addBodyParameter("PhoneNumber", phno)
                .addBodyParameter("Status", "0")
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

    public void updatedestination(String destination){
        loadToast.show();
        String appkey = getString(R.string.APPKEY);
        String phno = sharedPrefs.getLogedInPhoneNumber();
        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(120, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(120, TimeUnit.SECONDS)
                .build();
        AndroidNetworking.post(UrlConfigs.Set_Destination)
                .addBodyParameter("APPKEY",appkey)
                .addBodyParameter("PhoneNumber", phno)
                .addBodyParameter("Destination", destination)
                .setPriority(Priority.MEDIUM)
                .setOkHttpClient(okHttpClient)
                .build()
                .getAsJSONArray(new JSONArrayRequestListener() {
                    @Override
                    public void onResponse(JSONArray response) {
                        loadToast.success();
                        int j = response.length();
                        for (int i = 0; i < j; i++) {
                            JSONObject json;
                            try{
                                json = response.getJSONObject(i);
                                if (!json.has("AppKeyError")){
                                    if (!json.has("Failed")){
                                        if (json.has("Done")){
                                            Log.d("Response","LOG"+response.toString());
                                            Toast.makeText(getApplicationContext(),"Enabling Tracking(takes upto 10-15 sec)",Toast.LENGTH_LONG).show();
                                            sharedPrefs.enabletrackstatus();
                                            start.setVisibility(View.INVISIBLE);
                                            stop.setVisibility(View.VISIBLE);
                                            Intent intent =new Intent(getApplicationContext(),GPS_Service.class);
                                            startService(intent);
                                            finish();
                                        }else {
                                            Toast.makeText(getApplicationContext(), "Failed to start", Toast.LENGTH_SHORT).show();
                                        }
                                    }else {
                                        Toast.makeText(getApplicationContext(), "Failed to start", Toast.LENGTH_SHORT).show();
                                    }
                                }else {
                                    Toast.makeText(getApplicationContext(), "Problem Occured", Toast.LENGTH_SHORT).show();
                                }

                            }catch (Exception e){
                                Toast.makeText(getApplicationContext(),"Problem Occured"+e.toString(),Toast.LENGTH_SHORT).show();
                                Log.d("LOGG","PBLM"+e.toString());
                            }
                        }


                    }
                    @Override
                    public void onError(ANError anError) {
                        loadToast.error();
                        Log.d("Response","LOG"+anError.toString());
                    }
                });
    }

}

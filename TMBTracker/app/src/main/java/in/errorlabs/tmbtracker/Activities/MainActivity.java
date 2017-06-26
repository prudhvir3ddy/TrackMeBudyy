package in.errorlabs.tmbtracker.Activities;

import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONArrayRequestListener;

import net.steamcrafted.loadtoast.LoadToast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import in.errorlabs.tmbtracker.Helpers.Connection;
import in.errorlabs.tmbtracker.Helpers.SharedPrefs;
import in.errorlabs.tmbtracker.R;
import okhttp3.OkHttpClient;

import static in.errorlabs.tmbtracker.Configs.UrlConfigs.CheckStatus;
import static in.errorlabs.tmbtracker.Configs.UrlConfigs.GetLocation;
import static in.errorlabs.tmbtracker.Configs.UrlConfigs.LastLocation_Url;
import static in.errorlabs.tmbtracker.Configs.UrlConfigs.Update_User_Offline_Url;

public class MainActivity extends AppCompatActivity {

    String  temp_lat="",temp_lng="",temp_name="";
    Connection connection;
    SharedPrefs sharedPrefs;
    LoadToast loadToast;
    ImageView start,stop;
    TimerTask mTimerTask;
    Button logout;
    final Handler handler = new Handler();
    Timer t = new Timer();
    int count =0,number=1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connection = new Connection(this);
        sharedPrefs = new SharedPrefs(this);
        loadToast=new LoadToast(this);
        loadToast.setText("Loading...");
        start= (ImageView) findViewById(R.id.start);
        stop= (ImageView) findViewById(R.id.stop);
        logout= (Button) findViewById(R.id.logout);

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (connection.isInternet()){
                    checkstatus();
                }else {
                    Toast.makeText(getApplicationContext(),"No Internet",Toast.LENGTH_SHORT).show();
                }
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopTask();
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopTask();
                sharedPrefs.clearprefs();
                startActivity(new Intent(getApplicationContext(),Splash.class));
                finish();
            }
        });

    }

    public void checkstatus(){
        loadToast.show();

        String authkey = sharedPrefs.getLogedInKey();
        String ph = sharedPrefs.getLogedInPhoneNumber();

        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(120, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                . writeTimeout(120, TimeUnit.SECONDS)
                .build();

        AndroidNetworking.post(CheckStatus)
                .addBodyParameter("AUTHKEY",authkey)
                .addBodyParameter("PhoneNumber",ph)
                .setPriority(Priority.MEDIUM)
                .setOkHttpClient(okHttpClient)
                .build()
                .getAsJSONArray(new JSONArrayRequestListener() {
                    @Override
                    public void onResponse(JSONArray response) {

                        loadToast.success();
                        Log.d("LOGG",response.toString());

                        int j = response.length();
                        for (int i = 0; i < j; i++) {
                            JSONObject json;
                            try{
                                json = response.getJSONObject(i);
                                if (!json.has("Authkeyerror")){

                                    if (!json.has("InvalidDetails")){

                                        String FullName = json.getString("FullName");
                                        String last_lat = json.getString("LastLat");
                                        String last_lng = json.getString("LastLng");
                                        String TrackStatusEnabled = json.getString("TrackStatusEnabled");
                                        String Destination = json.getString("Destination");
                                        String DestinationStatus = json.getString("DestinationReachedStatus");
                                        if (DestinationStatus.equals("1") && TrackStatusEnabled.equals("0")){
                                            parsedata(FullName,last_lat,last_lng,TrackStatusEnabled,DestinationStatus,Destination);
                                        }else if (TrackStatusEnabled.equals("1") && Destination.length()>0 && DestinationStatus.equals("0") ){

                                            final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                            builder.setIcon(R.drawable.tmb_tracker);
                                            builder.setTitle("TrackMeBuddy Tracker");
                                            builder.setMessage("The user is travelling and has set the Destination to-> " +Destination+ " and is ONLINE " +
                                                    "\n\nWould you like to track ?");
                                            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    doTimerTask();
                                                    Toast.makeText(getApplicationContext(),"Hold On..!",Toast.LENGTH_LONG).show();
                                                    startActivity(new Intent(getApplicationContext(),MapsActivity.class));
                                                }
                                            });
                                            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    stopTask();
                                                    startActivity(new Intent(getApplication(),Splash.class));
                                                    finish();
                                                }
                                            });
                                            builder.setCancelable(false);
                                            builder.setIcon(R.drawable.tmb_tracker);
                                            AlertDialog welcomeAlert = builder.create();
                                            welcomeAlert.show();
                                            ((TextView) welcomeAlert.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
                                        }else {
                                            Toast.makeText(getApplicationContext(), "Wait for a momemt and start again", Toast.LENGTH_SHORT).show();
                                        }

                                    }else {
                                        Toast.makeText(getApplicationContext(), "Invalid Details", Toast.LENGTH_SHORT).show();
                                        Log.d("LOGG","PBLM"+"invalid details".toString());
                                    }

                                }else {
                                    Toast.makeText(getApplicationContext(), "Problem Occured", Toast.LENGTH_SHORT).show();
                                    Log.d("LOGG","PBLM"+"authkey".toString());
                                }

                            }catch (Exception e){
                                Toast.makeText(getApplicationContext(),"Problem Occured"+e,Toast.LENGTH_SHORT).show();
                                Log.d("LOGG","PBLM"+e.toString());
                            }
                        }

                    }

                    @Override
                    public void onError(ANError anError) {
                        loadToast.error();
                        Toast.makeText(getApplicationContext(),"Problem occured,try again",Toast.LENGTH_SHORT).show();

                    }
                });

    }

    public void parsedata(String fullname,String lstlat,String lstlng,String status,String dest_status,String dest){

        Toast.makeText(getApplicationContext(),status+dest_status,Toast.LENGTH_SHORT).show();

        if (status.equals("0") && dest_status.equals("1")){

            Intent intent = new Intent("user_offline_broadcast");
            intent.putExtra("status","offline");
            intent.putExtra("FullName",fullname);
            intent.putExtra("LastLat",lstlat);
            intent.putExtra("LastLng",lstlng);
            sendBroadcast(intent);

            final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setIcon(R.drawable.tmb_tracker);
            builder.setTitle("TrackMeBuddy Tracker");
            builder.setMessage("The user IS NOT TRAVELLING and IS OFFLINE right now." +
                    "\n\nWould you like to view the last available location ?");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    getlastlocation();
                    startActivity(new Intent(getApplicationContext(),MapsActivity.class));
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    stopTask();
                    startActivity(new Intent(getApplication(),Splash.class));
                    finish();
                }
            });
            builder.setCancelable(false);
            builder.setIcon(R.drawable.ic_error);
            AlertDialog welcomeAlert = builder.create();
            welcomeAlert.show();
            ((TextView) welcomeAlert.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());

        }else if(status.equals("0")&&dest_status.equals("0")){

            Intent intent = new Intent("user_offline_broadcast_not_reached");
            intent.putExtra("status","offline");
            intent.putExtra("FullName",fullname);
            intent.putExtra("LastLat",lstlat);
            intent.putExtra("LastLng",lstlng);
            intent.putExtra("Dest",dest);
            sendBroadcast(intent);

            final AlertDialog.Builder builder2 = new AlertDialog.Builder(MainActivity.this);
            builder2.setIcon(R.drawable.tmb_tracker);
            builder2.setTitle("TrackMeBuddy Tracker");
            builder2.setMessage("The user has set destination to-> " +dest+". \nAnd is offline without reaching the destination yet..!"+ "\n\nWould you like to view the last available location ?");
            builder2.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    getlastlocation();
                    startActivity(new Intent(getApplicationContext(),MapsActivity.class));
                }
            });
            builder2.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    stopTask();
                    startActivity(new Intent(getApplication(),Splash.class));
                    finish();
                }
            });
            builder2.setCancelable(false);
            builder2.setIcon(R.drawable.ic_error);
            AlertDialog welcomeAlert = builder2.create();
            welcomeAlert.show();
            ((TextView) welcomeAlert.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());


        }else {
            doTimerTask();
            startActivity(new Intent(getApplicationContext(),MapsActivity.class));
        }
    }


    public void getlastlocation(){

        if (connection.isInternet()){
            loadToast.show();

            String authkey = sharedPrefs.getLogedInKey();

            OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                    .connectTimeout(120, TimeUnit.SECONDS)
                    .readTimeout(120, TimeUnit.SECONDS)
                    . writeTimeout(120, TimeUnit.SECONDS)
                    .build();

            AndroidNetworking.post(LastLocation_Url)
                    .addBodyParameter("AUTHKEY",authkey)
                    .addBodyParameter("PhoneNumber",sharedPrefs.getLogedInPhoneNumber())
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
                                    if (!json.has("Authkeyerror")){

                                        String lat = json.getString("LastLat");
                                        String lng =json.getString("LastLng");
                                        String name =json.getString("Name");
                                        lastlatlng(lat,lng,name);

                                    }else {
                                        Toast.makeText(getApplicationContext(), "Problem Occured", Toast.LENGTH_SHORT).show();
                                        Log.d("LOGG","PBLM"+"authkey".toString());
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
                            Toast.makeText(getApplicationContext(),"Problem Occured"+ anError.toString(),Toast.LENGTH_SHORT).show();
                            Log.d("LOGG","PBLM"+anError.toString());
                        }
                    });
        }
        else {
            Toast.makeText(getApplicationContext(),"No Internet Available",Toast.LENGTH_SHORT).show();
        }

    }

    public void doTimerTask() {

        if (connection.isInternet()){
            loadToast.show();

            final String authkey = sharedPrefs.getLogedInKey();
            final String ph = sharedPrefs.getLogedInPhoneNumber();

            mTimerTask = new TimerTask() {
                public void run() {
                    handler.post(new Runnable() {
                        public void run() {

                            OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                                    .connectTimeout(120, TimeUnit.SECONDS)
                                    .readTimeout(120, TimeUnit.SECONDS)
                                    . writeTimeout(120, TimeUnit.SECONDS)
                                    .build();

                            AndroidNetworking.post(GetLocation)
                                    .addBodyParameter("AUTHKEY",authkey)
                                    .addBodyParameter("PhoneNumber",ph)
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
                                                    if (!json.has("Authkeyerror")){

                                                        if (!json.has("InvalidDetails")){

                                                            String lat = json.getString("CurLat");
                                                            String lng =json.getString("CurLng");
                                                            String name =json.getString("Name");
                                                            String dest =json.getString("Destination");
                                                            String dest_status =json.getString("Dest_Status");
                                                            if (dest_status.equals("1")){
                                                                notifyreached(dest);
                                                                Toast.makeText(getApplicationContext(), "The User has reached the destination successfully.", Toast.LENGTH_SHORT).show();
                                                            }else {

                                                                if (temp_lat.equals(lat) && temp_lng.equals(lng) && temp_name.equals(name)){

                                                                    count++;

                                                                    if (count==3){
                                                                        update_user_offline();
                                                                    }

                                                                }else {
                                                                    count=0;
                                                                    temp_lat=lat;
                                                                    temp_lng=lng;
                                                                    temp_name=name;

                                                                    Intent in = new Intent("last_location_update");
                                                                    in.putExtra("last_lat",lat);
                                                                    in.putExtra("last_lng",lng);
                                                                    in.putExtra("name",name+"-path- "+number);
                                                                    sendBroadcast(in);
                                                                    number++;
                                                                }
                                                            }

                                                        }else {
                                                            Toast.makeText(getApplicationContext(), "Problem Occured", Toast.LENGTH_SHORT).show();
                                                            Log.d("LOGG","PBLM"+"authkey".toString());
                                                        }


                                                    }else {
                                                        Toast.makeText(getApplicationContext(), "Problem Occured", Toast.LENGTH_SHORT).show();
                                                        Log.d("LOGG","PBLM"+"invalid_details".toString());
                                                    }

                                                }catch (Exception e){
                                                    Toast.makeText(getApplicationContext(),"Problem Occured"+e.toString(),Toast.LENGTH_SHORT).show();
                                                }
                                            }

                                        }

                                        @Override
                                        public void onError(ANError anError) {
                                            loadToast.error();
                                            Toast.makeText(getApplicationContext(),"Problem Occured"+anError.toString(),Toast.LENGTH_SHORT).show();
                                            Log.d("LOGG","PBLM"+anError.toString());
                                        }
                                    });

                            Log.d("TIMER", "TimerTask run");
                        }
                    });
                }
            };

            t.schedule(mTimerTask, 5000, 8000); //
        }else {
            Toast.makeText(getApplicationContext(),"No Internet Available",Toast.LENGTH_SHORT).show();
        }

    }

    public void lastlatlng(String lat, String lng,String name){


        Intent i = new Intent("last_location_update");
        i.putExtra("last_lat",lat);
        i.putExtra("last_lng",lng);
        i.putExtra("name",name+"--Last Available Location");
        sendBroadcast(i);
    }

    public void update_user_offline(){
        final String authkey =sharedPrefs.getLogedInKey();
        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(120, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                . writeTimeout(120, TimeUnit.SECONDS)
                .build();

        AndroidNetworking.post(Update_User_Offline_Url)
                .addBodyParameter("AUTHKEY",authkey)
                .addBodyParameter("PhoneNumber",sharedPrefs.getLogedInPhoneNumber())
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
                                if (!json.has("Authkeyerror")){

                                    if (!json.has("InvalidDetails")){

                                        String FullName =json.getString("FullName");
                                        String lastlat=json.getString("LastLat");
                                        String lastlng =json.getString("LastLng");

                                            Intent intent = new Intent("user_alive_status");
                                            intent.putExtra("status","offline");
                                            intent.putExtra("FullName",FullName);
                                            intent.putExtra("LastLat",lastlat);
                                            intent.putExtra("LastLng",lastlng);
                                            sendBroadcast(intent);
                                    }else {
                                        Toast.makeText(getApplicationContext(), "Problem Occured, Try again", Toast.LENGTH_SHORT).show();

                                    }

                                }else {
                                    Toast.makeText(getApplicationContext(), "Authentication Failed, Login to continue", Toast.LENGTH_SHORT).show();
                                    sharedPrefs.clearprefs();
                                    startActivity(new Intent(getApplicationContext(),Splash.class));
                                    finish();
                                }

                            }catch (Exception e){
                                Toast.makeText(getApplicationContext(),"Problem Occured",Toast.LENGTH_SHORT).show();
                                Log.d("LOGG","PBLM"+e.toString());
                            }
                        }
                    }
                    @Override
                    public void onError(ANError anError) {
                        loadToast.error();
                        Toast.makeText(getApplicationContext(),"Problem Occured"+anError.toString(),Toast.LENGTH_SHORT).show();
                        Log.d("LOGG","PBLM"+anError.toString());
                    }
                });
    }


    public void stopTask() {
        if (mTimerTask != null) {
            Log.d("TIMER", "timer canceled");
            Toast.makeText(getApplicationContext(),"Disabling tracking in few seconds",Toast.LENGTH_LONG).show();
            mTimerTask.cancel();
        }

    }

    private void notifyreached(String dest) {
        NotificationCompat.Builder builder =
                (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.tmb_tracker)
                        .setContentTitle("TrackMeBuddy")
                        .setContentText("The User has successfully reached the destination- " +dest+".  ");
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0, builder.build());
    }

}

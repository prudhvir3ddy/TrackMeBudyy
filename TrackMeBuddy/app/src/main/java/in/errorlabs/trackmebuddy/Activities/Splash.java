package in.errorlabs.trackmebuddy.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
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
import in.errorlabs.trackmebuddy.Helpers.Connection;
import in.errorlabs.trackmebuddy.Helpers.SharedPrefs;
import in.errorlabs.trackmebuddy.R;
import okhttp3.OkHttpClient;

public class Splash extends AppCompatActivity {
    SharedPrefs sharedPrefs;
    LoadToast loadToast;
    Connection connection;
    EditText passwd;
    Button btn;
    RelativeLayout rel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        sharedPrefs=new SharedPrefs(this);
        loadToast=new LoadToast(this);
        loadToast.setText("Loading...");
        connection = new Connection(this);
        passwd= (EditText) findViewById(R.id.passwd);
        btn= (Button) findViewById(R.id.btn);
        rel= (RelativeLayout) findViewById(R.id.rel1);

        if (sharedPrefs.getLogedInKey()==null || sharedPrefs.getLogedInPhoneNumber()==null){
            startActivity(new Intent(getApplicationContext(),MainScreen.class));
            finish();
            
        }else {
            rel.setVisibility(View.VISIBLE);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String pc=passwd.getText().toString();
                    if (pc.isEmpty() || pc.length()==0){
                        passwd.setText(null);
                        passwd.setError("Invalid");
                    }else if(pc.equals(sharedPrefs.getPassCode())){

                        if (connection.isInternet()){
                            loadToast.show();
                            OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                                    .connectTimeout(120, TimeUnit.SECONDS)
                                    .readTimeout(120, TimeUnit.SECONDS)
                                    . writeTimeout(120, TimeUnit.SECONDS)
                                    .build();
                            AndroidNetworking.post(UrlConfigs.Get_Startup_Status)
                                    .addBodyParameter("AUTHKEY",sharedPrefs.getLogedInKey())
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

                                                            String lat = json.getString("TrackStatusEnabled");
                                                            String dest_status = json.getString("DestinationReachedStatus");
                                                            if (lat.equals("0") && dest_status.equals("1")){
                                                                sharedPrefs.disabletrackstatus();
                                                            }else {
                                                                sharedPrefs.enabletrackstatus();
                                                                Intent intent =new Intent(getApplicationContext(),GPS_Service.class);
                                                                startService(intent);
                                                            }
                                                            startActivity(new Intent(getApplicationContext(),HomeScreen.class));
                                                            finish();

                                                        }else {
                                                            Toast.makeText(getApplicationContext(), "Invalid,Login to continue", Toast.LENGTH_SHORT).show();
                                                            sharedPrefs.clearprefs();
                                                            startActivity(new Intent(getApplicationContext(),Login.class));
                                                            finish();
                                                        }

                                                    }else {
                                                        Toast.makeText(getApplicationContext(), "Invalid,Login to continue", Toast.LENGTH_SHORT).show();
                                                        sharedPrefs.clearprefs();
                                                        startActivity(new Intent(getApplicationContext(),Login.class));
                                                        finish();
                                                    }

                                                }catch (Exception e){
                                                    Toast.makeText(getApplicationContext(),"Problem Occured"+e.toString(),Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        }
                                        @Override
                                        public void onError(ANError anError) {
                                            loadToast.error();
                                            Log.d("Response","LOG"+anError.toString());
                                        }
                                    });

                        }else {
                            Toast.makeText(getApplicationContext(),"No Internet",Toast.LENGTH_SHORT).show();
                        }
                    }else {
                        passwd.setText(null);
                        passwd.setError("Wrong Passcode");
                        Toast.makeText(getApplicationContext(),"Invalid PassCode",Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }

    }
}

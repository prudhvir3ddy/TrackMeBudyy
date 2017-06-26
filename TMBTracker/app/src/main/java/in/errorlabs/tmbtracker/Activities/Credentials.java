package in.errorlabs.tmbtracker.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONArrayRequestListener;

import net.steamcrafted.loadtoast.LoadToast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import in.errorlabs.tmbtracker.Helpers.Connection;
import in.errorlabs.tmbtracker.Helpers.SharedPrefs;
import in.errorlabs.tmbtracker.R;
import okhttp3.OkHttpClient;

import static in.errorlabs.tmbtracker.Configs.UrlConfigs.CheckDetails_Url;

public class Credentials extends AppCompatActivity {

    EditText ph,passcode;
    Button start;
    Connection connection;
    SharedPrefs sharedPrefs;
    LoadToast loadToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credentials);
        ph= (EditText) findViewById(R.id.mobile);
        passcode= (EditText) findViewById(R.id.passcode);
        start = (Button) findViewById(R.id.startbtn);
        connection = new Connection(this);
        sharedPrefs = new SharedPrefs(this);
        loadToast=new LoadToast(this);
        loadToast.setText("Loading...");
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phone = ph.getText().toString();
                String pass = passcode.getText().toString();
                if (!phone.isEmpty() || phone.length()!=0){
                    if (!pass.isEmpty() || pass.length()!=0){

                        if (connection.isInternet()){
                            checkdetails(phone,pass);
                        }else {
                            Toast.makeText(getApplicationContext(),"No Internet Connection",Toast.LENGTH_SHORT).show();
                        }


                    }else {
                        passcode.setText(null);
                        passcode.setError("Invalid");
                    }

                }else {
                    ph.setText(null);
                    ph.setError("Invalid");
                }

            }
        });
    }

    public void checkdetails(final String phone, String pcode){
        loadToast.show();

        String appkey = getString(R.string.APPKEY);

        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(120, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                . writeTimeout(120, TimeUnit.SECONDS)
                .build();

        AndroidNetworking.post(CheckDetails_Url)
                .addBodyParameter("APPKEY",appkey)
                .addBodyParameter("PhoneNumber", phone)
                .addBodyParameter("Passcode", pcode)
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

                                    if (!json.has("InvalidDetails")){

                                        String authkey = json.getString("AuthKey");
                                        sharedPrefs.setLogedInKey(authkey);
                                        sharedPrefs.setLogedInPhoneNumber(phone);
                                        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                                        startActivity(intent);
                                        finish();

                                    }else {

                                        Toast.makeText(getApplicationContext(),"Invalid PhoneNumber or Passcode",Toast.LENGTH_SHORT).show();
                                    }

                                }else{
                                    Toast.makeText(getApplicationContext(),"Problem Occured",Toast.LENGTH_SHORT).show();
                                    Log.d("LOGG","PBLM"+"invaliddetails".toString());
                                }

                            }catch (Exception e){

                                Toast.makeText(getApplicationContext(),"Problem Occured",Toast.LENGTH_SHORT).show();
                                Log.d("LOGG","PBLM"+e.toString());
                            }
                        }
                    }
                    @Override
                    public void onError(ANError anError) {
                        Toast.makeText(getApplicationContext(),"Problem Occured",Toast.LENGTH_SHORT).show();
                        Log.d("LOGG","PBLM"+anError.toString());
                    }
                });
    }

}

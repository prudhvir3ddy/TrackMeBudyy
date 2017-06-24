package in.errorlabs.trackmebuddy.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
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

public class Login extends AppCompatActivity {

    RelativeLayout r1,r2,r3,r4;
    EditText login_phonenumber,login_password,reset_registered_email,reset_code,new_pass,new_cpass;
    TextView donthavaaccnt,forgotpasswd;
    Button login,recover_btn,recover_code_btn,reset_final_btn;
    LoadToast loadToast;
    Connection connection;
    SharedPrefs sharedPrefs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        r1= (RelativeLayout) findViewById(R.id.r1);
        r2= (RelativeLayout) findViewById(R.id.r2);
        r3= (RelativeLayout) findViewById(R.id.r3);
        r4= (RelativeLayout) findViewById(R.id.r4);
        login_phonenumber= (EditText) findViewById(R.id.phnumber);
        login_password = (EditText) findViewById(R.id.password);
        reset_registered_email= (EditText) findViewById(R.id.recover_email);
        reset_code= (EditText) findViewById(R.id.recover_code);
        new_pass= (EditText) findViewById(R.id.new_password);
        new_cpass= (EditText) findViewById(R.id.new_cpassword);
        donthavaaccnt= (TextView) findViewById(R.id.regtext);
        forgotpasswd= (TextView) findViewById(R.id.forgotpasswd);
        login= (Button) findViewById(R.id.login);
        recover_btn= (Button) findViewById(R.id.recover_btn);
        recover_code_btn= (Button) findViewById(R.id.recover_code_btn);
        reset_final_btn= (Button) findViewById(R.id.reset_final_btn);
        loadToast=new LoadToast(this);
        loadToast.setText("Loading...");
        connection =new Connection(this);
        sharedPrefs = new SharedPrefs(this);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String ph= login_phonenumber.getText().toString();
                String paswd=login_password.getText().toString();
                if (ph.length()==10){
                    if (paswd.length()>0){
                        login(ph,paswd);
                    }else {
                        login_password.setError("Enter Password");
                    }

                }else {
                    login_phonenumber.setText(null);
                    login_phonenumber.setError("Enter a valid PhoneNumber");
                }
            }
        });

        donthavaaccnt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),Register.class));
            }
        });

        forgotpasswd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                r1.setVisibility(View.GONE);
                r2.setVisibility(View.VISIBLE);
            }
        });

        recover_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String mail =reset_registered_email.getText().toString();
                if (isValidEmail(mail)){

                    if (connection.isInternet()){
                        requestcode(mail);
                    }else {
                        Toast.makeText(getApplicationContext(),"No Internet Connection",Toast.LENGTH_SHORT).show();
                    }

                }else {
                    reset_registered_email.setText(null);
                    reset_registered_email.setError("Invalid Email");
                }
            }
        });

        recover_code_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String code = reset_code.getText().toString();
                if (code.length() >0){

                    if (connection.isInternet()){
                        verifycode(code);
                    }else {
                        Toast.makeText(getApplicationContext(),"No Internet Connection",Toast.LENGTH_SHORT).show();
                    }

                }else {
                    reset_code.setText(null);
                    reset_code.setError("Enter Reset Code");
                }
            }
        });


        reset_final_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String p = new_pass.getText().toString();
                String cp = new_cpass.getText().toString();
                if (p.equals(cp) && p.length()>0 && p.length()>0){

                    if (connection.isInternet()){
                        resetpassword(p);
                    }else {
                        Toast.makeText(getApplicationContext(),"No Internet Connection",Toast.LENGTH_SHORT).show();
                    }

                }else {
                    new_pass.setText(null);
                    new_pass.setError("Invalid password");
                    new_cpass.setText(null);
                    new_cpass.setError("Invalid password");
                }
            }
        });


    }

    public  static boolean isValidEmail(CharSequence target) {

        return !TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    public void login(String ph,String pass){
        loadToast.show();

        String appkey = getString(R.string.APPKEY);

        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(120, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                . writeTimeout(120, TimeUnit.SECONDS)
                .build();

        AndroidNetworking.post(UrlConfigs.Login_Url)
                .addBodyParameter("APPKEY",appkey)
                .addBodyParameter("PhoneNumber", ph)
                .addBodyParameter("Password", pass)
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

                                    if (!json.has("InvalidPhoneNumber")){

                                        if (!json.has("InvalidPassword")){

                                            String key = json.getString("AuthKey");
                                            String authkey =json.getString("PN");
                                            String passcode =json.getString("PCode");
                                            sharedPrefs.setLogedInKey(key);
                                            sharedPrefs.setLogedInPhoneNumber(authkey);
                                            sharedPrefs.setPassCode(passcode);
                                            startActivity(new Intent(getApplicationContext(),HomeScreen.class));
                                            finish();

                                        }else {
                                            Toast.makeText(getApplicationContext(),"Invalid Password",Toast.LENGTH_SHORT).show();
                                        }

                                    }else {
                                        Toast.makeText(getApplicationContext(),"PhoneNumber Does'nt Exists",Toast.LENGTH_SHORT).show();
                                    }

                                }else{
                                    Toast.makeText(getApplicationContext(),"Problem Occured",Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(getApplicationContext(),MainScreen.class));
                                    finish();
                                }

                            }catch (Exception e){

                                Toast.makeText(getApplicationContext(),"Problem Occured",Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(getApplicationContext(),MainScreen.class));
                                finish();
                            }
                        }
                    }
                    @Override
                    public void onError(ANError anError) {
                            loadToast.error();
                        Toast.makeText(getApplicationContext(),"Problem Occured,Try Later",Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(),MainScreen.class));
                        finish();
                    }
                });
    }

    public void requestcode(final String mail){
        loadToast.show();
        String appkey = getString(R.string.APPKEY);

        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(120, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                . writeTimeout(120, TimeUnit.SECONDS)
                .build();

        AndroidNetworking.post(UrlConfigs.ForgotPassword_Url)
                .addBodyParameter("APPKEY",appkey)
                .addBodyParameter("Email", mail)
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

                                    if (!json.has("InvalidEmail")){

                                        if (!json.has("Failed")){

                                            if (!json.has("SentFailed")){

                                                if (json.has("Sent")){

                                                    Toast.makeText(getApplicationContext(),"Verification Code Sent",Toast.LENGTH_SHORT).show();
                                                    sharedPrefs.saveresetemail(mail);
                                                    r2.setVisibility(View.GONE);
                                                    r3.setVisibility(View.VISIBLE);

                                                }else {
                                                    Toast.makeText(getApplicationContext(),"Email sending failed",Toast.LENGTH_SHORT).show();

                                                }

                                            }else {
                                                Toast.makeText(getApplicationContext(),"Email sending failed",Toast.LENGTH_SHORT).show();

                                            }

                                        }else {
                                            Toast.makeText(getApplicationContext(),"Failed, Try later",Toast.LENGTH_SHORT).show();

                                        }

                                    }else {

                                        Toast.makeText(getApplicationContext(),"Email Does'nt Exists",Toast.LENGTH_SHORT).show();


                                    }

                                }else{
                                    Toast.makeText(getApplicationContext(),"Problem Occured",Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(getApplicationContext(),MainScreen.class));
                                    finish();
                                }

                            }catch (Exception e){

                                Toast.makeText(getApplicationContext(),"Problem Occured",Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(getApplicationContext(),MainScreen.class));
                                finish();
                            }
                        }


                    }

                    @Override
                    public void onError(ANError anError) {
                        loadToast.error();
                        Log.d("LOG","LOGG"+anError.toString());
                        Toast.makeText(getApplicationContext(),"Problem Occured,Try Later"+anError.toString(),Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(),MainScreen.class));
                        finish();
                    }
                });

    }

    public void verifycode(String code){

        loadToast.show();
        String appkey = getString(R.string.APPKEY);
        String mail = sharedPrefs.getPasswordResetEmail();

        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(120, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                . writeTimeout(120, TimeUnit.SECONDS)
                .build();

        AndroidNetworking.post(UrlConfigs.Verifycode_Url)
                .addBodyParameter("APPKEY",appkey)
                .addBodyParameter("Email", mail)
                .addBodyParameter("Code", code)
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

                                    if (!json.has("InvalidEmail")){

                                        if (!json.has("Failed")){
                                            if (json.has("Matched")){

                                                r3.setVisibility(View.GONE);
                                                r4.setVisibility(View.VISIBLE);

                                            }else {
                                                Toast.makeText(getApplicationContext(),"Failed, Try later",Toast.LENGTH_SHORT).show();
                                                startActivity(new Intent(getApplicationContext(),MainScreen.class));
                                                finish();
                                            }

                                        }else {
                                            Toast.makeText(getApplicationContext(),"Invalid Code",Toast.LENGTH_SHORT).show();
                                            reset_code.setText(null);
                                            reset_code.setError("Invalid Code");
                                        }

                                    }else {

                                        Toast.makeText(getApplicationContext(),"Invalid",Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(getApplicationContext(),MainScreen.class));
                                        finish();

                                    }

                                }else{
                                    Toast.makeText(getApplicationContext(),"Problem Occured",Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(getApplicationContext(),MainScreen.class));
                                    finish();
                                }

                            }catch (Exception e){

                                Toast.makeText(getApplicationContext(),"Problem Occured",Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(getApplicationContext(),MainScreen.class));
                                finish();
                            }
                        }

                    }

                    @Override
                    public void onError(ANError anError) {
                        loadToast.error();
                        Toast.makeText(getApplicationContext(),"Problem Occured,Try Later",Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(),MainScreen.class));
                        finish();
                    }
                });

    }

    public  void resetpassword(String pass){
        loadToast.show();

        String appkey = getString(R.string.APPKEY);
        String mail = sharedPrefs.getPasswordResetEmail();

        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(120, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                . writeTimeout(120, TimeUnit.SECONDS)
                .build();

        AndroidNetworking.post(UrlConfigs.ResetPassword_Url)
                .addBodyParameter("APPKEY",appkey)
                .addBodyParameter("Email", mail)
                .addBodyParameter("Password", pass)
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

                                            if (json.has("Reset")){

                                                Toast.makeText(getApplicationContext(),"Password Successfully Changed",Toast.LENGTH_SHORT).show();
                                                sharedPrefs.clearresetemail();
                                                r4.setVisibility(View.GONE);
                                                r1.setVisibility(View.VISIBLE);

                                            }else {
                                                Toast.makeText(getApplicationContext(),"Failed, Try later",Toast.LENGTH_SHORT).show();
                                                startActivity(new Intent(getApplicationContext(),MainScreen.class));
                                                finish();
                                            }

                                    }else {

                                        Toast.makeText(getApplicationContext(),"Invalid",Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(getApplicationContext(),MainScreen.class));
                                        finish();

                                    }

                                }else{
                                    Toast.makeText(getApplicationContext(),"Problem Occured",Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(getApplicationContext(),MainScreen.class));
                                    finish();
                                }

                            }catch (Exception e){

                                Toast.makeText(getApplicationContext(),"Problem Occured",Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(getApplicationContext(),MainScreen.class));
                                finish();
                            }
                        }

                    }

                    @Override
                    public void onError(ANError anError) {
                        loadToast.error();
                        Toast.makeText(getApplicationContext(),"Problem Occured,Try Later",Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(),MainScreen.class));
                        finish();
                    }
                });

    }


}

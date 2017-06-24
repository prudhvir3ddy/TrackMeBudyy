package in.errorlabs.trackmebuddy.Activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import in.errorlabs.trackmebuddy.Configs.UrlConfigs;
import in.errorlabs.trackmebuddy.Helpers.Connection;
import in.errorlabs.trackmebuddy.R;
import okhttp3.OkHttpClient;

public class Register extends AppCompatActivity {

    EditText fullname,phnumber,email,password,cpassword,passcode;
    Button register;
    LoadToast loadToast;
    Connection connection;
    Context context;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        fullname= (EditText) findViewById(R.id.fullname);
        phnumber= (EditText) findViewById(R.id.phnumber);
        email= (EditText) findViewById(R.id.email);
        password= (EditText) findViewById(R.id.password);
        cpassword= (EditText) findViewById(R.id.confirmpassword);
        passcode = (EditText) findViewById(R.id.passcode);
        register= (Button) findViewById(R.id.register);
        loadToast=new LoadToast(this);
        loadToast.setText("Loading...");
        connection =new Connection(this);

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String fname = fullname.getText().toString();
                String phnmbr = phnumber.getText().toString();
                String mail = email.getText().toString();
                String passwd = password.getText().toString();
                String c_passwd = cpassword.getText().toString();
                String pass_code = passcode.getText().toString();

                if (namevalidator(fname)){
                    if (phnmbr.length()==10){

                        if (isValidEmail(mail)){

                            if (passwd.length()>=8 && c_passwd.length()>=8){

                                if (passwd.equals(c_passwd)){

                                    if (pass_code.length()==4){

                                        if (connection.isInternet()){

                                            register(fname,phnmbr,mail,passwd,pass_code);

                                        }else {
                                            Toast.makeText(getApplicationContext(),"No Internet Connection",Toast.LENGTH_SHORT).show();
                                        }


                                    }else {
                                        passcode.setText(null);
                                        passcode.setError("4 digits required");
                                    }
                                }else {

                                    password.setText(null);
                                    cpassword.setText(null);
                                    password.setError("Invalid Phone Number");
                                    cpassword.setError("Invalid Phone Number");
                                }
                            }else {
                                password.setText(null);
                                cpassword.setText(null);
                                password.setError("Min 8 Characters");
                                cpassword.setError("Min 8 Characters");
                            }

                        }else {
                            email.setText(null);
                            email.setError("Invalid Email");
                        }

                    }else {

                        phnumber.setText(null);
                        phnumber.setError("Invalid Phone Number");
                    }
                }else {
                    fullname.setText(null);
                    fullname.setError("Enter a valid Name");
                }

            }
        });
    }

    public  static boolean isValidEmail(CharSequence target) {

        return !TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    public boolean namevalidator(String name){
        Pattern pattern;
        Matcher matcher;
        final String USERNAME_PATTERN = "^[a-zA-Z ]{2,25}$";
        pattern = Pattern.compile(USERNAME_PATTERN);
        matcher = pattern.matcher(name);
        return matcher.matches();
    }

    public void register(String fname, String phno, final String mail, String password, String passcode){
        loadToast.show();
        String appkey = getString(R.string.APPKEY);

        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(120, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                . writeTimeout(120, TimeUnit.SECONDS)
                .build();

        AndroidNetworking.post(UrlConfigs.Register_Url)
                .addBodyParameter("APPKEY",appkey)
                .addBodyParameter("FullName", fname)
                .addBodyParameter("PhoneNumber", phno)
                .addBodyParameter("Email", mail)
                .addBodyParameter("Password", password)
                .addBodyParameter("PassCode", passcode)
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

                                    if (!json.has("PhoneExists")){

                                        if (!json.has("EmailExists")){

                                            if (!json.has("Failed")){

                                                if (json.has("Registered")){

                                                    Toast.makeText(getApplicationContext(),"Successfully Register, Login to continue",Toast.LENGTH_SHORT).show();
                                                    startActivity(new Intent(getApplicationContext(),MainScreen.class));
                                                    finish();

                                                }else {
                                                    Toast.makeText(getApplicationContext(),"Failed to Register",Toast.LENGTH_SHORT).show();
                                                    startActivity(new Intent(getApplicationContext(),MainScreen.class));
                                                    finish();
                                                }

                                            }else {
                                                Toast.makeText(getApplicationContext(),"Failed to Register",Toast.LENGTH_SHORT).show();
                                                startActivity(new Intent(getApplicationContext(),MainScreen.class));
                                                finish();
                                            }


                                        }else {
                                            Toast.makeText(getApplicationContext(),"PhoneNumber Already Exists",Toast.LENGTH_SHORT).show();
                                            email.setText("");
                                            email.setError("Already Exists");
                                        }

                                    }else {

                                        Toast.makeText(getApplicationContext(),"Email Already Exists",Toast.LENGTH_SHORT).show();
                                        email.setText("");
                                        email.setError("Already Exists");
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
                        Log.d("LOG","LOG"+anError.toString());
                        Toast.makeText(getApplicationContext(),"No Internet Connection",Toast.LENGTH_SHORT).show();
                    }
                });

    }

}

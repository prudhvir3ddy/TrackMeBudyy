package in.errorlabs.tmbtracker.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import in.errorlabs.tmbtracker.Helpers.Connection;
import in.errorlabs.tmbtracker.Helpers.SharedPrefs;
import in.errorlabs.tmbtracker.R;

public class Splash extends AppCompatActivity {

    Button start;
    Connection connection;
    SharedPrefs sharedPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        start = (Button) findViewById(R.id.start);
        connection = new Connection(this);
        sharedPrefs = new SharedPrefs(this);

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sharedPrefs.getLogedInKey()==null || sharedPrefs.getLogedInPhoneNumber()==null){
                    sharedPrefs.clearprefs();
                    startActivity(new Intent(getApplicationContext(),Credentials.class));
                    finish();
                }else {
                    startActivity(new Intent(getApplicationContext(),MainActivity.class));
                    finish();
                }

            }
        });
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        finish();
    }
}

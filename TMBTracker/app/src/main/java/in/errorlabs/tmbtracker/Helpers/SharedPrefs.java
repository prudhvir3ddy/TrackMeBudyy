package in.errorlabs.tmbtracker.Helpers;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by root on 2/25/17.
 */

public class SharedPrefs {

    Context context;
    public static final String myprefs = "myprefs";
    public static final String LogedInPhoneNumber = "PhoneNumber";
    public static final String LogedInKey = "Key";

    SharedPreferences sharedpreferences;
    SharedPreferences.Editor editor;

    public SharedPrefs(Context context) {
        this.context = context;
        sharedpreferences = context.getSharedPreferences(myprefs, Context.MODE_PRIVATE);
        editor = sharedpreferences.edit();
    }

    public  String getLogedInPhoneNumber() {
        return sharedpreferences.getString(LogedInPhoneNumber,null);
    }

    public  String getLogedInKey() {
        return sharedpreferences.getString(LogedInKey,null);
    }

    public void setLogedInPhoneNumber(String ph){
        editor.putString(LogedInPhoneNumber,ph);
        editor.commit();
    }

    public void setLogedInKey(String key){
        editor.putString(LogedInKey,key);
        editor.commit();
    }

    public void clearprefs(){
        editor.putString(LogedInKey,null);
        editor.putString(LogedInPhoneNumber,null);
        editor.commit();
    }

}

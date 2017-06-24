package in.errorlabs.trackmebuddy.Helpers;

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
    public static final String PassCode = "PassCode";
    public static final String PasswordResetEmail = "Email";
    public static final String TrackStatusEnabled = "status";

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

    public  String getPassCode() {
        return sharedpreferences.getString(PassCode,null);
    }

    public void setLogedInPhoneNumber(String ph){
        editor.putString(LogedInPhoneNumber,ph);
        editor.commit();
    }

    public void setLogedInKey(String key){
        editor.putString(LogedInKey,key);
        editor.commit();
    }

    public void setPassCode(String passcode){
        editor.putString(PassCode,passcode);
        editor.commit();
    }

    public void clearprefs(){
        editor.putString(LogedInKey,null);
        editor.putString(LogedInPhoneNumber,null);
        editor.putString(PassCode,null);
        editor.commit();
    }

    public void saveresetemail(String mail){
        editor.putString(PasswordResetEmail,mail);
        editor.commit();
    }

    public String getPasswordResetEmail() {
        return sharedpreferences.getString(PasswordResetEmail,null);
    }

    public void clearresetemail(){
        editor.putString(PasswordResetEmail,null);
        editor.commit();
    }

    public void enabletrackstatus(){
        editor.putString(TrackStatusEnabled,"Enabled");
        editor.commit();
    }

    public void disabletrackstatus(){
        editor.putString(TrackStatusEnabled,null);
        editor.commit();
    }

    public String gettrackstatus(){
        return sharedpreferences.getString(TrackStatusEnabled,null);
    }


}

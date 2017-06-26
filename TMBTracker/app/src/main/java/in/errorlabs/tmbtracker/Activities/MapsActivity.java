package in.errorlabs.tmbtracker.Activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

import in.errorlabs.tmbtracker.R;

import static in.errorlabs.tmbtracker.R.id.map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    public List<LatLng> route = new ArrayList<LatLng>();

    int count =1;

    @Override
    protected void onResume() {
        super.onResume();
        BroadcastListener receiver = new BroadcastListener();
        IntentFilter filterRefresh = new IntentFilter("last_location_update");
        IntentFilter filterUpdate = new IntentFilter("user_alive_status");
        registerReceiver(receiver, filterRefresh);
        registerReceiver(receiver, filterUpdate);

    }

    private class BroadcastListener extends BroadcastReceiver {

        public void onReceive(Context ctx, Intent intent) {

            if (intent.getAction().equals("last_location_update")) {

                String lt =intent.getExtras().getString("last_lat");
                String lg =intent.getExtras().getString("last_lng");
                String name =intent.getExtras().getString("name");
                Double lat = Double.parseDouble(lt);
                Double lng = Double.parseDouble(lg);
                updatemarker(lat,lng,name);
                count++;
                LatLng latLng = new LatLng(Double.parseDouble(lt.trim()),Double.parseDouble(lg.trim()));
                route.add(latLng);
                drawLine(route);

            } else if(intent.getAction().equals("user_alive_status")) {

                String l_status =intent.getExtras().getString("status");
                final String full_name =intent.getExtras().getString("FullName");
                String l_lat =intent.getExtras().getString("LastLat");
                String l_lng =intent.getExtras().getString("LastLng");
                final Double lat = Double.parseDouble(l_lat);
                final Double lng = Double.parseDouble(l_lng);

                final AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
                builder.setIcon(R.drawable.tmb_tracker);
                builder.setTitle("TrackMeBuddy Tracker");
                builder.setMessage("It seems that the user WENT OFFLINE without reaching the destinantion!" +
                        "\n\nWould you like to view the last available location ?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mMap.clear();
                        updatemarker(lat,lng,full_name+"--Last Available Location");
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startActivity(new Intent(getApplication(),Splash.class));
                        finish();
                    }
                });
                builder.setNegativeButton("Cancel",null);
                builder.setIcon(R.drawable.ic_error);
                AlertDialog welcomeAlert = builder.create();
                welcomeAlert.show();
                ((TextView) welcomeAlert.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
            }
        }


    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }


    public  void updatemarker(Double lat,Double lng,String name){
        LatLng location = new LatLng(lat,lng);
        Marker mymarker= mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).title(name));
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 20);
        mMap.animateCamera(cameraUpdate);
    }

    public void drawLine(List<LatLng> points) {
        if (points == null) {
            Log.e("Draw Line", "got null as parameters");
            return;
        }

        Polyline line = mMap.addPolyline(new PolylineOptions().width(10).color(Color.BLUE).geodesic(true)
                .zIndex(5.0f));
        line.setPoints(points);
    }


}

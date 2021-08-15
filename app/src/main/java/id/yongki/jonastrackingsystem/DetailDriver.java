package id.yongki.jonastrackingsystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static id.yongki.jonastrackingsystem.MainActivity.EMAIL;
import static id.yongki.jonastrackingsystem.MainActivity.NAMA;
import static id.yongki.jonastrackingsystem.MainActivity.NOWA;
import static id.yongki.jonastrackingsystem.MainActivity.USERNAME;

public class DetailDriver extends AppCompatActivity implements OnMapReadyCallback, LocationListener {

    private GoogleMap mMap;
    private DatabaseReference reference;
    private LocationManager manager;
    private final int MIN_TIME = 1000;
    private final int MIN_DISTANCE = 1; //meter
    private static final int interval = 400; //milisecond
    Marker myMarker;
    TextView mNama;
    Button wabutton, telbutton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_driver);
        Intent intent = getIntent();
        String email = intent.getStringExtra(EMAIL);
        String username = intent.getStringExtra(USERNAME);
        String nowa = intent.getStringExtra(NOWA);
        String nama = intent.getStringExtra(NAMA);
        Log.d("Detail Email",email);
        Log.d("Detail Username",username);



        manager = (LocationManager) getSystemService(LOCATION_SERVICE);
        reference = FirebaseDatabase.getInstance("https://jonas-tracking-system-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child(username);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_driver);
        mapFragment.getMapAsync(DetailDriver.this);

        getLocationUpdate();
        readChanges();


        mNama = findViewById(R.id.tv_nama);
        mNama.setText(nama);
        wabutton = findViewById(R.id.detail_wabtn);
        telbutton = findViewById(R.id.detail_telbtn);
        wabutton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("smsto:" + nowa);
                Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
                intent.setPackage("com.whatsapp");
                String text = "Halo, Apa kondisimu sedang mengantuk?";
                intent.putExtra(Intent.EXTRA_TEXT, text);
                startActivity(Intent.createChooser(intent, ""));
            }
        });
        telbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", nowa, null));
                startActivity(intent);
            }
        });
    }

    private void readChanges() {
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                myMarker.remove();
                if (dataSnapshot.exists()) {
                    try {

                        MyLocation location = dataSnapshot.getValue(MyLocation.class);
                        if (location != null) {

                            LatLng marker = new LatLng(location.getLatitude(), location.getLongitude());
                            myMarker = mMap.addMarker(new MarkerOptions().position(marker));
                            mMap.setMinZoomPreference(18);
                            mMap.getUiSettings().setZoomControlsEnabled(false);
                            mMap.getUiSettings().setAllGesturesEnabled(false);
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(marker));
                        }
                    } catch (Exception e) {
                        Toast.makeText(DetailDriver.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
    private void getLocationUpdate() {
        if (manager != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
                } else if (manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, this);

                } else {
                    AlertDialog alertDialog = new AlertDialog.Builder(DetailDriver.this).create();
                    alertDialog.setTitle("GPS Tidak Aktif");
                    alertDialog.setMessage("Mohon Aktifkan GPS Anda");
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    startActivity(new Intent(getApplicationContext(), MapsActivity.class));
                                    finish();
                                }
                            });
                    alertDialog.show();
                }
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            }

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocationUpdate();
            } else {
                Toast.makeText(this, "Permission Required", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        LatLng bandung = new LatLng(-6.9030882, 107.5030728);
        myMarker = mMap.addMarker(new MarkerOptions().position(bandung).title("Kamu Disini"));

        mMap.setMinZoomPreference(15);

        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setAllGesturesEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(bandung));


    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        if (location != null) {
            saveLocation(location);

        } else {
            Toast.makeText(this, "Tidak Ada Lokasi", Toast.LENGTH_LONG).show();
        }
    }

    private void saveLocation(Location location) {
        reference.setValue(location);
    }

}
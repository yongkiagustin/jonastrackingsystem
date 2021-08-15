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

public class DetailDriver extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private DatabaseReference reference;
    private LocationManager manager;
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



        manager = (LocationManager) getSystemService(LOCATION_SERVICE);
        reference = FirebaseDatabase.getInstance("https://jonas-tracking-system-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child(username);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_driver);
        mapFragment.getMapAsync(DetailDriver.this);

        readChanges();

        Log.d("username",username);

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
                }else{
                    Log.d("data tidak ada","data tidak ada");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
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


}
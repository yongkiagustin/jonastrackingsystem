package id.yongki.jonastrackingsystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import static java.security.AccessController.getContext;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {

    private GoogleMap mMap;
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private DatabaseReference reference;
    private LocationManager manager;
    private final int MIN_TIME = 1000;
    private final int MIN_DISTANCE = 1; //meter
    private static final int interval = 400; //milisecond
    private MediaPlayer mediaPlayer;
    Marker myMarker;

    boolean flag = false;
    private TextView statusMata, kondisi;
    CameraSource cameraSource;
    long lastUpdate;
    long curenttime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        manager = (LocationManager) getSystemService(LOCATION_SERVICE);

        reference = FirebaseDatabase.getInstance("https://jonas-tracking-system-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Users");

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        getLocationUpdate();
        readChanges();

//        Face detection
        statusMata = findViewById(R.id.tv_mata);
        curenttime = 0;
        lastUpdate = 0;
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
            Toast.makeText(this, "Permission not granted!\n Grant permission and restart app", Toast.LENGTH_SHORT).show();
        } else {
            init();
        }

        // ALARM

        final AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        int a = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (int)(a), 0);

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
                        Toast.makeText(MapsActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
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
                    AlertDialog alertDialog = new AlertDialog.Builder(MapsActivity.this).create();
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

        // Add a marker in Sydney and move the camera
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_signout) {
            firebaseAuth.signOut();
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            finish();
            return true;
        } else if (item.getItemId() == R.id.action_profile) {
            startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
        } else if (item.getItemId() == R.id.action_changepassword) {
            startActivity(new Intent(getApplicationContext(), LupaPasswordActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    //    face detection
    private void init() {
        statusMata = findViewById(R.id.tv_mata);
        kondisi = findViewById(R.id.tv_kondisi);
        flag = true;

        initCameraSource();
    }

    private void initCameraSource() {
        FaceDetector detector = new FaceDetector.Builder(this)
                .setTrackingEnabled(true)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .setMode(FaceDetector.FAST_MODE)
                .build();
        detector.setProcessor(new MultiProcessor.Builder(new FaceTrackerDaemon(MapsActivity.this)).build());

        cameraSource = new CameraSource.Builder(this, detector)
                .setRequestedPreviewSize(1024, 768)
                .setFacing(CameraSource.CAMERA_FACING_FRONT)
                .setRequestedFps(30.0f)
                .build();

        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            cameraSource.start();
        } catch (IOException e) {
            Toast.makeText(MapsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (cameraSource != null) {
            try {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                cameraSource.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (cameraSource != null) {
            cameraSource.stop();
        }

        statusMata.setText("Tidak Terdeteksi");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraSource != null) {
            cameraSource.release();
        }
    }

    //update view
    public void updateMainView(Condition condition) {
        switch (condition) {
            case USER_EYES_OPEN:
                curenttime = System.currentTimeMillis();
//                Log.d("Status Mata", "Terbuka");
//                statusMata.setText("Mata Terdeteksi Terbuka");
                stopAlarm();
                break;
            case USER_EYES_CLOSED:
                lastUpdate = System.currentTimeMillis();
                Log.d("Status Mata", "Tertutup");

                if (lastUpdate >= (curenttime + interval)) {
//                    Log.d("Status current", String.valueOf(curenttime));
//                    Log.d("Status last", String.valueOf(lastUpdate));
//                    Log.d("Status Mata", "Alarm");
                    playAlarm();

                } else {
                    Log.d("else close", String.valueOf(lastUpdate));
                }
                break;
            case FACE_NOT_FOUND:
                stopAlarm();
                Log.d("Status Mata", "Tidak Terdeteksi");
//                statusMata.setText("Mata Tidak Terdeteksi");
                break;
            default:
//                statusMata.setText("Mata Tidak Terdeteksi");
        }
    }

    public void playAlarm() {

        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(this, R.raw.alarm);
        }
        mediaPlayer.start();
    }

    public void stopAlarm() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        stopAlarm();
    }
}

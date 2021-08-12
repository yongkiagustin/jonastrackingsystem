package id.yongki.jonastrackingsystem;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import id.yongki.jonastrackingsystem.model.UserModel;

public class Splash extends AppCompatActivity {
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseUser mUser = firebaseAuth.getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                if (firebaseUser == null) {
                    startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                    finish();
                } else {
                    String uid = mUser.getEmail();
                    Log.d("USER",uid);
                    final DocumentReference docRef = db.collection("DataUsers").document(uid);
                    docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if(task.isSuccessful()){
                                DocumentSnapshot documentSnapshot = task.getResult();
                                if (documentSnapshot.exists()){
                                    final UserModel userModel = new UserModel(
                                            (String) documentSnapshot.get("nama"),
                                            (String) documentSnapshot.get("nowa"),
                                            (String) documentSnapshot.get("jabatan"),
                                            (String) documentSnapshot.get("status")
                                    );
                                    if(userModel.status.equals("aktif")){
                                        if(userModel.jabatan.equals("Admin")){
                                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                        }else{
                                            startActivity(new Intent(getApplicationContext(), MapsActivity.class));
                                        }
                                    }else{
                                        Toast.makeText(getApplicationContext(), "Status anda belum aktif, Hubungi admin", Toast.LENGTH_LONG).show();
                                    }
                                }
                            }else {
                                Toast.makeText(getApplicationContext(), "Terjadi Error, Mohon Coba Kembali", Toast.LENGTH_LONG).show();
                            }

                        }
                    });

                }
            }
        }, 2000);
    }
}
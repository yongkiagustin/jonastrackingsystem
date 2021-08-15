package id.yongki.jonastrackingsystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
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

public class PraLoginActivity extends AppCompatActivity {

    public static final String USERNAME_PRA = "id.yongki.jonastrackingsystem.MESSAGE";
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pra_login);
        String uid = mUser.getEmail();
        final DocumentReference docRef = db.collection("DataUsers").document(uid);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot.exists()){
                        final UserModel userModel = new UserModel(
                                (String) documentSnapshot.get("nama"),
                                (String) documentSnapshot.get("username"),
                                (String) documentSnapshot.get("email"),
                                (String) documentSnapshot.get("nowa"),
                                (String) documentSnapshot.get("jabatan"),
                                (String) documentSnapshot.get("status")
                        );

                        if(userModel.status.equals("aktif")){
                            if(userModel.jabatan.equals("Admin")){
                                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                finish();
                            }else{
                                Intent i = new Intent(PraLoginActivity.this, MapsActivity.class);
                                i.putExtra(USERNAME_PRA, userModel.username);
                                Log.d("username extra", USERNAME_PRA);
                                startActivity(i);
                                finish();
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

//TODO membuat fungsi deteksi kantuk : DONE
//TODO benerin MapsActivity masih belum muncul action barnya : DONE
//TODO map dengan mlkit masih bentrok dependenciesnya : DONE
//TODO membuat tampilan untuk admin : DONE
// TODO map detail driver belum seharusnya
// TODO membuat tampilan my profil
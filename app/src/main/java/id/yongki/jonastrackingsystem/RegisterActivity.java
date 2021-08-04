package id.yongki.jonastrackingsystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    private EditText etnama,etnowa,etemail, etpassword, etrepassword;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        TextView login = findViewById(R.id.register_tvlogin);
        progressBar = findViewById(R.id.register_progressbar);
        Button regisBtn = findViewById(R.id.register_regisbtn);
        etnama = findViewById(R.id.etnama);
        etnowa = findViewById(R.id.etnowa);
        etemail = findViewById(R.id.register_etemail);
        etpassword = findViewById(R.id.register_etpassword);
        etrepassword = findViewById(R.id.register_etrepassword);

        String[] jabatan = { "Driver", "Admin"};
        Spinner spin = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, jabatan);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin.setAdapter(adapter);

        regisBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String gnama = etnama.getText().toString();
                String gnowa = etnowa.getText().toString();
                String gjabatan = spin.getSelectedItem().toString();
                final String gemail = etemail.getText().toString();
                String gpassword = etpassword.getText().toString();
                String grepassword = etrepassword.getText().toString();

                final String semail = getString(R.string.email_kosong);
                final String spassword = getString(R.string.pass_kosong);
                final String spassTidakCocok = getString(R.string.pass_tidak_cocok);

                progressBar.setVisibility(View.VISIBLE);

                if (gemail.isEmpty()) {
                    etemail.setError(semail);
                    progressBar.setVisibility(View.GONE);
                } else if (gpassword.isEmpty()) {
                    etpassword.setError(spassword);
                    progressBar.setVisibility(View.GONE);
                } else if (!gpassword.equals(grepassword)) {
                    progressBar.setVisibility(View.GONE);
                    etrepassword.setError(spassTidakCocok);
                } else {

                    mAuth.createUserWithEmailAndPassword(gemail, gpassword)
                            .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                @Override
                                public void onSuccess(AuthResult authResult) {
                                    //membuat data user ke firestore
                                    Map<String, Object> user = new HashMap<>();
                                    user.put("nama", gnama);
                                    user.put("nowa", gnowa);
                                    user.put("email", gemail);
                                    user.put("jabatan", gjabatan);
//                    user.put("profilePic", imageUrl);
                                    db.collection("DataUsers").document(gemail).set(user)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    progressBar.setVisibility(View.GONE);
                                                    showDialog();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    progressBar.setVisibility(View.GONE);
                                                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                                                }
                                            });

                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });


                }

            }
        });
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish();

            }
        });
    }
    private void showDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);

        // set title dialog
        alertDialogBuilder.setTitle("Selamat!");

        // set pesan dari dialog
        alertDialogBuilder
                .setMessage("Anda berhasil mendaftar, klik Ok untuk melakukan login!")
                //.setIcon(R.mipmap.ic_launcher)
                .setCancelable(false)
                .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                        finish();
                    }
                });

        // membuat alert dialog dari builder
        AlertDialog alertDialog = alertDialogBuilder.create();

        // menampilkan alert dialog
        alertDialog.show();
    }
}
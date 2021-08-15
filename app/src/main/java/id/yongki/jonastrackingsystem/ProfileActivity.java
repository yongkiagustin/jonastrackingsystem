package id.yongki.jonastrackingsystem;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class ProfileActivity extends AppCompatActivity {

    TextView nama, email, nowa, jabatan;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        nama = findViewById(R.id.account_etnama);
        email = findViewById(R.id.account_etemail);
        nowa = findViewById(R.id.account_etnohp);
        jabatan = findViewById(R.id.account_etjabatan);

        nama.setEnabled(false);
        email.setEnabled(false);
        nowa.setEnabled(false);
        jabatan.setEnabled(false);


    }
}
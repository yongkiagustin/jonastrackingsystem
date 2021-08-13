package id.yongki.jonastrackingsystem;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import static id.yongki.jonastrackingsystem.MainActivity.EMAIL;
import static id.yongki.jonastrackingsystem.MainActivity.USERNAME;

public class DetailDriver extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_driver);
        Intent intent = getIntent();
        String email = intent.getStringExtra(EMAIL);
        String username = intent.getStringExtra(USERNAME);
        Log.d("Detail Email",email);
        Log.d("Detail Username",username);
    }
}
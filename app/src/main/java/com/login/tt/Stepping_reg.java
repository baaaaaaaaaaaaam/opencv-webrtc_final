package com.login.tt;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class Stepping_reg extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("test111", "Stepping_reg()");
        Intent serviceRestart=new Intent(getApplicationContext(),SocketConnectService.class);
        startService(serviceRestart);
        finish();
    }
}

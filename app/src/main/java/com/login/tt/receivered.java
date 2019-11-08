package com.login.tt;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


public class receivered extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("test111", "receiver()");
        Intent restart=new Intent(context, Stepping_reg.class);
        context.startActivity(restart);
    }
}

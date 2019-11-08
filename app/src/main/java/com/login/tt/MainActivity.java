package com.login.tt;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    EditText input_NikName;
    Button Join;
//    static Socket socket;
//    private BufferedReader br;
//    private BufferedWriter bw;
//    private String ip="15.164.61.218";
//    private int port=5005;
//    private String message;
//    private Handler mHandler;
    SharedPreferences sharedPreferences;

    private boolean isJoin;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        PowerManager pm = (PowerManager) getApplicationContext().getSystemService(POWER_SERVICE);
        boolean isWhiteListing = false;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            isWhiteListing = pm.isIgnoringBatteryOptimizations(getApplicationContext().getPackageName());
        }
        if (!isWhiteListing) {
            Intent intent = new Intent();
            intent.setAction(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + getApplicationContext().getPackageName()));
            startActivity(intent);
        }


        input_NikName=findViewById(R.id.input_Nicname);
        Join=findViewById(R.id.join);
//        mHandler = new Handler();
        sharedPreferences = getSharedPreferences("Shared",MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        //쉐어드를 검사하여 Join한 값이 있을 경우 현재 페이지를 그냥 넘어간다.
        isJoin=sharedPreferences.getBoolean("Join",false);
        if(isJoin){
            Intent intent= new Intent(getApplicationContext(),userList.class);
            startActivity(intent);
            finish();
        }


        //조인버튼을 누를경우 TCP 소켓을 연결하고 서버는 소켓을 맺은 유저의 아이디를 접속자 목록에서 등록한다.
        Join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String compare="";
                if("".equals(input_NikName.getText().toString())){

                }else{

                    editor.putString("nikName",input_NikName.getText().toString());
                    editor.putBoolean("Join",true);
                    editor.commit();
                    Intent intent= new Intent(getApplicationContext(),SocketConnectService.class);
                    startService(intent);
                    Intent intent1= new Intent(getApplicationContext(),userList.class);
                    startActivity(intent1);
                    finish();
                }

            }
        });
    }



}

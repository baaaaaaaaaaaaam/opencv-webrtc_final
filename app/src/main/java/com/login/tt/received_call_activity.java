package com.login.tt;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.net.Socket;


public class received_call_activity extends AppCompatActivity {
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    ImageButton no_call;   //전화를 받지 않을 경우
    ImageButton received_call;  //전화를 받을 경우
    TextView userId;          // 보낸사람 아이디  // 화면에 표시할때와 상대방에게 reject를 보낼때 사용된다

    String nikName; // 내 아이디 ; 상대방에게 reject를 보낼때 사용한다
    String sender;
    Socket socket=SocketConnectService.socket;
    private BufferedReader br=SocketConnectService.br;  //서비스 소켓
    private BufferedWriter bw=SocketConnectService.bw;  //서비스 소켓

    private boolean isReject;
    private boolean countdown=true;

    boolean isReceiverCallactivity;
    //서비스에서  소켓을 전달 받을 예정인데 readLine() 에서 대기를 하고있다가 유저에게 request:받는이:보낸는이 :방번호를 받게되면 해당 액티비티가 실행 된다
    // 해당 액티비티에서 received_call 버튼을 드래그 할경우 전화 받기가 실행되고 no_call을 드래그 할 경우 상대방에게 socket으로 거부 메시지를 보낸다

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onCreate(savedInstanceState);


        setContentView(R.layout.received_call_activity);



        //전화를 받는 화면에 들어오면 isReceiverCallactivity ( 상대방이전화를 받아 영상을 전송하겟다는 booelan값) 을 false로 만든다
        //이후 수신버튼을 눌러 영상통화가 시작되면 내 영상을 전송할수있게 true로만들어준다.
        sharedPreferences = getSharedPreferences("Shared",MODE_PRIVATE);
        editor=sharedPreferences.edit();


        no_call=findViewById(R.id.no_call);
        received_call=findViewById(R.id.received_call);
        userId=findViewById(R.id.senderID);

        // 이 파일은 전화가 온경우에만 실행이된다.
        sharedPreferences=getSharedPreferences("Shared",MODE_PRIVATE);
        sender=sharedPreferences.getString("senderID","");
        userId.setText(sender+"에게 전화가 왔습니다.");


        countDown cd=new countDown();
        cd.start();
        nikName=sharedPreferences.getString("nikName","");
        // 터치 리스너 사용방법
        // View 해당 버튼을 의미한다.
        //MotionEvent 는 Down,UP,Move등 값을 바을 수 있다.
        received_call.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:

                        break;
                    case MotionEvent.ACTION_UP:
                        if(event.getX()<=-100 || event.getX()>=350 || event.getY()<=-100 || event.getY()>=360){
                            //전화를 받을 경우 ConnectActivioty 로이동을시킨다
                            editor.putBoolean("isReceiverCallactivity",true);
                            //callActivity에서 상대방에게 영상을 전달하게 되는데 이 때 receiveUser 에게 메시지를 보낸다 ,
                            //전화를 받는 사람입장에서는 sender ( 전화를 거는 사람이 ) 가 받아야함으로 여기서 sender를 receiver 변수에 담아준다.
                            editor.putString("receiveUser",sender);
                            editor.commit();
                            Intent intent=new Intent(getApplicationContext(),ConnectActivity.class);
                            startActivity(intent);
                            countdown=false;
                            isReject=true;
                            //callActivity가 실행되면 쉐어드에 receiveImage를 true 로변경하고 callActivity가 종료되면 false 처리한다
                            sendCalled();

                            finish();
                        }
                        break;
                }
                return false;
            }
        });

        no_call.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:

                        break;
                    case MotionEvent.ACTION_UP:
                        if(event.getX()<=-100 || event.getX()>=350 || event.getY()<=-100 || event.getY()>=360){
                            //전화를 받지않을 경우 상대방에게 전화 거절 메시지를 보낸다.

                            finish();
                        }
                        break;
                }
                return false;
            }
        });




    }

    void sendCalled(){
        sendCalled thread=new sendCalled();
        thread.start();
    }
    class sendCalled extends java.lang.Thread {
        @Override
        public void run() {
            super.run();
            PrintWriter out = new PrintWriter(bw, true);
            out.println("called:"+nikName+":"+sender);
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(isReject==false){
            Thread a=new Thread();
            a.start();
        }
    }

    class countDown extends java.lang.Thread{
        @Override
        public void run(){
            int i=10;
            while(countdown){
                   if(i==0){
                       finish();
                       break;
                   }
                   i--;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    class Thread extends java.lang.Thread {
        @Override
        public void run() {
            super.run();
            PrintWriter out = new PrintWriter(bw, true);
            out.println("reject:"+nikName+":"+sender);
        }
    }

}

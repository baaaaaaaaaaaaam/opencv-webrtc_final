package com.login.tt;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;

public class SocketConnectService extends Service {

    static Socket socket;
    static BufferedReader br;
    static BufferedWriter bw;
    private String ip="35.243.90.95";
    private int port=5005;


    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    String nikName;
    Thread thread;
    boolean clickServiceOut=true;


    @Override
    public IBinder onBind(Intent intent) {
        Log.d("test111", "onBind()");
        return null;
    }

    @Override
    public void onCreate() {
        Log.d("test111","onCreate()");
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("test111","onStartCommand()");
        sharedPreferences = getSharedPreferences("Shared",MODE_PRIVATE);
        editor=sharedPreferences.edit();
        nikName=sharedPreferences.getString("nikName","");


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final String strId = "test";
            final String strTitle = getString(R.string.app_name);

            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            NotificationChannel channel = nm.getNotificationChannel(strId);
            if (channel == null) {
                channel = new NotificationChannel(strId, strTitle, NotificationManager.IMPORTANCE_HIGH);
                nm.createNotificationChannel(channel);
            }

            Notification notification = new NotificationCompat.Builder(this, strId).build();
            startForeground(1 , notification);
            stopForeground(true);
        }


//서비스 실행시  동작

        thread = new Thread ();
        thread.start();
        Log.d("test111","thread 시작");
        return START_STICKY;
    }




    public void onDestroy() {
        super.onDestroy();
        Log.d("test111", "onDestory()");
        clickServiceOut=sharedPreferences.getBoolean("clickServiceOut",true);
        serviceOutThread thread = new serviceOutThread();
        thread.start();

        if(clickServiceOut){
            Intent intent=new Intent(this,receivered.class);
            sendBroadcast(intent);
            Log.d("test111", "receivered-send()");
        }

        // 서비스가 종료될 때 실행
    //   thread.stopThread();
     //   thread = null;//쓰레기 값을 만들어서 빠르게 회수하라고 null을 넣어줌.



    }
    final Handler handler = new Handler(){

        @Override
        public void handleMessage(Message msg){
            switch (msg.what) {
                case 0:
                    Log.d("test111","전화 요청 옴");
                    editor.putBoolean("isReceived",true);  //전화받을 경우 connectActivity에서 아래에서 받은 룸번호로 바로 연결할 예정
                    editor.putString("roomID",msg.obj.toString());     //전화받을 룸번호 저장

                    editor.commit();
                    Intent intent=new Intent(getApplicationContext(),received_call_activity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);

                    break;

                case 1:
                    Log.d("test111","거절 요청 옴");
                    editor.putBoolean("isReceived",false);  //현재 액티비티의 callFragment를 호출하여 onCallhangup을 호출한다.
                    editor.commit();
                    ((CallActivity)CallActivity.mContext).onCallHangUp();   //싱글톤을 사용하여 해당 메시지가올경우 callActivity의 onCallhangUP을 실행한다
                    Toast.makeText(getApplicationContext(),"상대방이 전화를 거절하였습니다.",Toast.LENGTH_LONG).show();

                    break;
                case 2:
                    Intent t = new Intent("receiveImage");
                    t.putExtra("called", (String)msg.obj);
                    //상대방에게  이미지를 전달받으면 이미지를 callactivity로 넘겨주는 로컬 브로드케스트 역활을 한다.
                    LocalBroadcastManager.getInstance(SocketConnectService.this).sendBroadcast(t);
                    break;
                case 3:
                    Intent t1 = new Intent("receiveImage");
                    t1.putExtra("called", "called");
                    //상대방이 전화를 받앗다는 신호를 보내면 이미지를 전송을 시작한다.
                    LocalBroadcastManager.getInstance(SocketConnectService.this).sendBroadcast(t1);
                    break;
                case 4:
                    thread.stopThread();
                    break;
                default:
                    break;
            }


        }
    };

    class Thread extends java.lang.Thread {

        boolean stopped = false;
        int i = 0;

        public Thread(){
            stopped = false;
        }

        public void stopThread() {
            stopped = true;
        }



        @Override
        public void run() {
            super.run();
            try {
                socket = new Socket(ip, port);
                bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            PrintWriter out = new PrintWriter(bw, true);
            out.println("join:"+nikName);
            Log.d("test111","join 보냄");
            String line="";
            while(stopped == false) {
//            Log.d("test111","readLine() 전");
                try {
                    line = br.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }

             String[] Text=line.split(":");


                if(Text[0].equals("request")){
                    //request:senderId:receiverID:roomID 형태
                    editor.putString("senderID",Text[1]);     //전화받을 룸번호 저장
                    editor.commit();
                    Message message = handler.obtainMessage();
                    message.what = 0;
                    message.obj = Text[3];
                    handler.sendMessage(message);
                    Log.d("test111","request 받음 ");
                }else if(Text[0].equals("reject")){
                    //reject:senderId:receiverID
                    Message message = handler.obtainMessage();
                    message.what = 1;
                    message.obj = Text[2];
                    handler.sendMessage(message);
                    Log.d("test111","reject 받음 ");
                }else if(Text[0].equals("image")){
                    Message message = handler.obtainMessage();
                    message.what = 2;
                    message.obj = Text[3];
                    handler.sendMessage(message);

                }else if(Text[0].equals("called")){
                    //상대방이 전화를 받앗다는 메시지 , 이것을 받은 이후부터 이미지를 전송한다
                    Message message = handler.obtainMessage();
                    message.what = 3;
                    Log.d("test111","service called 받음 ");
                    handler.sendMessage(message);
                }
            }
        }
    }



    class serviceOutThread extends Thread {
        public void run(){
            Log.d("test111", "serviceOutThread()");
//            PrintWriter out = new PrintWriter(bw, true);
//            out.println("bye:"+nikName);
            serviceOut();
        }
    }



    private void serviceOut(){
        Log.d("test111", "serviceOut()");
        String userInfo="";
        try {
            URL url = new URL("http://49.247.135.126/androidExit.php");
            Map<String,Object> params = new LinkedHashMap<>();
            params.put("nikName",nikName);
            StringBuilder postData = new StringBuilder();
            for (Map.Entry<String,Object> param : params.entrySet()) {
                if (postData.length() != 0) postData.append('&');
                postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                postData.append('=');
                postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
            }
            byte[] postDataBytes = postData.toString().getBytes("UTF-8");

            HttpURLConnection conn = (HttpURLConnection)url.openConnection();

            conn.setRequestMethod("POST");

            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));

            conn.setDoOutput(true);
            conn.getOutputStream().write(postDataBytes);

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            String text;
            while((text=reader.readLine())!=null){

                userInfo+=text;
            }
            Log.d("test111",userInfo);

        }catch(Exception e) {
            Log.d("test111", "Exception");
        }


    }


}

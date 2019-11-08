package com.login.tt;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class userList extends AppCompatActivity implements userListAdapter.r_Click_Listener {

//    Socket socket=MainActivity.socket;   //이전에 연결한 소켓
    RecyclerView recyclerView;             //유저리스트를 보여줄 리사이클러뷰
    userListAdapter adapter;              //유저 리스트에 리사이클러뷰를 적용할 어뎁터
    ArrayList<currentJoinUser> joinList;        //객체를 담을 ArrayList
    SwipeRefreshLayout swipeRefresh ;          //리사이클러뷰를 감싼 새로고침 뷰
    Button serviceOut;                     // 서비스 종료시킬 버튼
    String url;                             //  유저정보를 받아올 경로
    String LoadNikName;                     // 쉐어드에서 불러올 접속자 닉네임
    SharedPreferences sharedPreferences;      //쉐어드
    SharedPreferences.Editor editor;
    Button opencv;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_list);
        serviceOut=findViewById(R.id.serviceOut);
        url="http://35.243.90.95/androidUserList.php";
        opencv=findViewById(R.id.opencvTest);
        sharedPreferences=getSharedPreferences("Shared",MODE_PRIVATE);
       editor = sharedPreferences.edit(); //나의 JoinSeq 를 저장하기 위해 생성
        LoadNikName=sharedPreferences.getString("nikName","");


        //권한을 물어본다 , 저장 , 카메라 , 불러오기
        int CameraPermission= ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        int StoragePermission= ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int MicPermission= ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);

        //만약 권한이 0 ( permission) 이 아니라면 checkPermission을 실행 시켜 권한을 물어보게한다.
        if(CameraPermission==PackageManager.PERMISSION_GRANTED&&StoragePermission==PackageManager.PERMISSION_GRANTED&&MicPermission==PackageManager.PERMISSION_GRANTED){

        }else{
            CheckPerMission();
        }

        // AsynkTask
        NetworkTask networkTask = new NetworkTask(url);
        networkTask.execute();




        //스와이프 리프레시
        swipeRefresh=findViewById(R.id.SwipeRefresh);


        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d("test111","swipeRefresh");
                NetworkTask networkTask = new NetworkTask(url);
                networkTask.execute();
                Toast.makeText(getApplicationContext(),"업데이트",Toast.LENGTH_SHORT).show();
                swipeRefresh.setRefreshing(false);
        }
        });

        //리사이클러뷰
        recyclerView = findViewById(R.id.user_list_recyclerView);
        @SuppressLint("WrongConstant") LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false); //레이아웃매니저 생성
        recyclerView.setLayoutManager(layoutManager);

        opencv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getApplicationContext(),MainActivity1.class);
                startActivity(intent);
            }
        });
        serviceOut.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //스레드나 AsynkTask를 사용하여 인터넷 통신을 시켜야 동작한다.
                    editor.putBoolean("Join",false);
                    editor.putBoolean("clickServiceOut",false);
                    editor.commit();
                    serviceOutThread thread = new serviceOutThread();
                    thread.start();
                }
        });







    }

    @Override
    protected void onStart() {
        super.onStart();
        editor.putBoolean("isReceived",false);
        editor.putBoolean("clickServiceOut",true);
        editor.commit();
    }

    @Override
    public void onItemClicked(int position) {
        Toast.makeText(getApplicationContext(),joinList.get(position)+"에게 전화걸기",Toast.LENGTH_SHORT).show();
        editor.putString("receiveUser", joinList.get(position).getNikName());  //전화걸 아이디 등록  ConnectActivity에서 전화를 걸때 request:receiveUser:roomId 형태로 전달
      editor.commit();
        Intent intent= new Intent(getApplicationContext(),ConnectActivity.class);
        startActivity(intent);
    }






    public class NetworkTask extends AsyncTask<Void, Void, String> {

        private String url;
        private ContentValues values;

        public NetworkTask(String url) {

            this.url = url;
          ;
        }

        @Override
        protected String doInBackground(Void... params) {

            String result; // 요청 결과를 저장할 변수.
            result=getUserList();
            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if(s.equals("no")){
                Log.d("test111",s);
            }else{
                joinList =new ArrayList<>();
                JsonParser jsonParser=new JsonParser();   //생성한다
                JsonObject jsonObj = (JsonObject) jsonParser.parse(s);   // String 타입으로 받은 s를 JsonObject화 시킨다

                Gson gson=new Gson();         //Gson을 생성한다
                currentJoinUserList joinUserList=gson.fromJson(jsonObj,currentJoinUserList.class);         // jsonObject를 List에 담는다
                //LIst를 분해하여 객체화 하여 arraylist에 담는다.
                for(currentJoinUser a: joinUserList.getList()){
                    if(a.getNikName().equals(LoadNikName)){
                        //조인한 이름과 디비 아이디가 같으면 아무것도하지않는다.
                        Log.d("test111","같은아이디");
                    }else{
                        currentJoinUser joinUser =new currentJoinUser();
                        joinUser.setId(a.getId());
                        joinUser.setNikName(a.getNikName());
                        joinList.add(joinUser);
                    }

                }
                adapter=new userListAdapter(joinList);
                adapter.setOnclickListener(userList.this);
                recyclerView.setAdapter(adapter);

            }

      }
    }

    private String getUserList(){
        String userInfo="";
        try {
            URL url = new URL ("http://35.243.90.95/androidUserList.php");
            Map<String,Object> params = new LinkedHashMap<>();

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

        }
        return userInfo;
    }


    private void serviceOut(){
        Intent intent= new Intent(getApplicationContext(),SocketConnectService.class);
        stopService(intent);



        String userInfo="";
        try {
            URL url = new URL ("http://35.243.90.95/androidUserDelete.php");
            Map<String,Object> params = new LinkedHashMap<>();
            params.put("nikName",LoadNikName);
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


        }catch(Exception e) {

        }
      finish();
    }


    class serviceOutThread extends Thread {
        public void run(){
            serviceOut();
        }
    }



    //권한 관련 요청
    private void CheckPerMission(){
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO},1);
    }


    @Override
    protected void onResume() {
        super.onResume();
        //유저 목록 엑티비티로 오면 영상을 보내는 쓰레드를 동작시킬수잇는 boolean값을 false 처리한다
        //received call acitivty 에서 전화수신버튼을 누르면 true가된다
        //SocketConnectService에서 상대방이 전화받기버튼을 눌르면  true가되고 localbroadcast 로 true값을 전달한다.
        editor.putBoolean("isReceiverCallactivity",false);
        editor.commit();
    }
}


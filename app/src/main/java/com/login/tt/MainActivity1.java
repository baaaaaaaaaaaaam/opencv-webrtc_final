
package com.login.tt;

        import androidx.appcompat.app.AlertDialog;
        import androidx.appcompat.app.AppCompatActivity;

        import android.content.DialogInterface;
        import android.content.Intent;
        import android.graphics.Bitmap;
        import android.graphics.BitmapFactory;
        import android.graphics.Matrix;
        import android.net.Uri;
        import android.os.Bundle;
        import android.annotation.TargetApi;
        import android.content.pm.PackageManager;
        import android.os.Build;
        import android.os.Environment;
        import android.os.Handler;
        import android.os.Message;
        import android.util.Base64;
        import android.util.Log;
        import android.view.SurfaceView;
        import android.view.View;
        import android.view.WindowManager;
        import android.widget.Button;
        import android.widget.ImageView;

        import org.opencv.android.BaseLoaderCallback;
        import org.opencv.android.CameraBridgeViewBase;
        import org.opencv.android.LoaderCallbackInterface;
        import org.opencv.android.OpenCVLoader;
        import org.opencv.android.Utils;
        import org.opencv.core.Core;
        import org.opencv.core.CvException;
        import org.opencv.core.Mat;
        import org.opencv.core.Point;
        import org.opencv.imgproc.Imgproc;

        import java.io.ByteArrayOutputStream;
        import java.io.File;
        import java.io.FileNotFoundException;
        import java.io.FileOutputStream;
        import java.io.IOException;
        import java.util.Collections;
        import java.util.List;

        import static android.Manifest.permission.CAMERA;
        import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
        import static org.opencv.core.Core.flip;


public class MainActivity1 extends AppCompatActivity
        implements CameraBridgeViewBase.CvCameraViewListener2 {




    private static final String TAG = "opencv";
    private Mat matInput;
    private Mat matResult;


    private CameraBridgeViewBase mOpenCvCameraView;
    private Button changeCamera;
    private boolean checkCamera;

    public native long loadCascade(String cascadeFileName);



    public native void detect(long cascadeClassifier_face, long cascadeClassifier_eyes,long matAddrInput, long matAddrResult,int choice);


    public long cascadeClassifier_face = 0;
    public long cascadeClassifier_eyes=0;

    public Button mask;
    public Button glass;
    public int choice=0;

    private Button capture;
    boolean iscapture;


    boolean imageshow=false;
    boolean threadrun=true;
    static {
        System.loadLibrary("opencv_java4");
        System.loadLibrary("native-lib");
    }


    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {

                    mOpenCvCameraView.enableView();
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    ImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main1);
        read_cascade_file();

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.activity_surface_view);


        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

        mOpenCvCameraView.setMaxFrameSize(720  , 720);
        mOpenCvCameraView.setCvCameraViewListener(this);
        mOpenCvCameraView.setCameraIndex(1); // front-camera(1),  back-camera(0)

        imageView=(ImageView)findViewById(R.id.receive);
        mask=findViewById(R.id.mask);
        glass=findViewById(R.id.glass);
        capture=findViewById(R.id.capture);
        mask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choice=1;
            }
        });
        glass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choice=2;
            }
        });
        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iscapture=true;
            }
        });
        Thread thread=new Thread();
        thread.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "onResume :: Internal OpenCV library not found.");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "onResum :: OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }


    public void onDestroy() {
        super.onDestroy();

        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
        threadrun=false;
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        Log.d("test112", width + "");
        Log.d("test112", height + "");
    }

    @Override
    public void onCameraViewStopped() {

    }
    //bitmap to string
    String bTos=null;
    //string to bitmap
    Bitmap receiveBitmap=null;
    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) throws IOException {

        matInput = inputFrame.rgba();
        if (matResult == null)


            matResult = new Mat(matInput.rows(), matInput.cols(), matInput.type());


        //파일의 중앙을 찾는다
        Point center = new Point(matInput.cols()/2, matInput.rows()/2);
        //rotationMatrix 변수에  중앙을 기준으로 90도한 계산한 이미지의 값을 담는다.
        Mat rotationMatrix=  Imgproc.getRotationMatrix2D(center, 90, 1);
        //정확한 의미는 모르겟으나 matInput이미지를 위에서 계산한90을 적용하여 matResult로 변환시킨다
        Imgproc.warpAffine(matInput, matResult, rotationMatrix, matResult.size());
        //이미지를 검사하여 얼굴을 찾아 리본을 씌운다


        detect(cascadeClassifier_face, cascadeClassifier_eyes,matResult.getNativeObjAddr(),matResult.getNativeObjAddr(),choice);


        Mat rotationMatrix1=  Imgproc.getRotationMatrix2D(center, -90, 1);
        Imgproc.warpAffine(matResult, matResult, rotationMatrix1, matResult.size());
        Bitmap bmp = Bitmap.createBitmap(matResult.cols(),matResult.rows(), Bitmap.Config.ARGB_8888);
        if(iscapture){

            try {
                //Imgproc.cvtColor(seedsImage, tmp, Imgproc.COLOR_RGB2BGRA);
                Utils.matToBitmap(matResult, bmp);}
            catch (CvException e){Log.d("Exception",e.getMessage());}

            long now = System.currentTimeMillis();
            String filename = now+".jpg";
            //생성할 파일 미리 껍데기 만들기
            File file = new File(Environment.getExternalStorageDirectory().getPath(), filename);
            //껍데기 파일에 이미지 저장하기
            FileOutputStream out = new FileOutputStream(file);
            //사진 90도 돌리고 , 좌우 반전 돌려서 저장하기
            bmp=RotateBitmap(bmp,-90);
            // bitmap 파일 jpeg파일로 만들기
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.close();
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                    Uri.parse("file://" + Environment.getExternalStorageDirectory())));
            iscapture=false;
        }
        try {
            //Imgproc.cvtColor(seedsImage, tmp, Imgproc.COLOR_RGB2BGRA);

            Utils.matToBitmap(matResult, bmp);}
        catch (CvException e){Log.d("Exception",e.getMessage());}
        bTos=BitmapToString(bmp);
        receiveBitmap=StringToBitmap(bTos);
        imageshow=true;

        return matResult;
    }

    //bitmap to string 변환
    public static String BitmapToString(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 70, baos);
        byte[] bytes = baos.toByteArray();
        String temp = Base64.encodeToString(bytes, Base64.DEFAULT);
        return temp;
    }

    //string to bitmap 변환
    public static Bitmap StringToBitmap(String encodedString) {
        try {
            byte[] encodeByte = Base64.decode(encodedString, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        } catch (Exception e) {
            e.getMessage();
            return null;
        }
    }

    //이미지를 그리기위한 스레드
    class Thread extends java.lang.Thread {

        @Override
        public void run() {
            super.run();
            while(threadrun){
                if(imageshow){
                    Message message = handler.obtainMessage();
                    message.what = 0;
                    handler.sendMessage(message);
                    imageshow=false;
                }
            }
        }
    }

    final Handler handler = new Handler(){

        @Override
        public void handleMessage(Message msg){
            switch (msg.what) {
                case 0:
                    receiveBitmap=RotateBitmap(receiveBitmap,270);
                    imageView.setImageBitmap(receiveBitmap);
                    break;
                default:
                    break;
            }


        }
    };


    //각도 변환
    public static Bitmap RotateBitmap(Bitmap source, float angle)
    {
        Matrix matrix = new Matrix();
        matrix.setScale(1,-1);
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    private void read_cascade_file() {
        cascadeClassifier_face = loadCascade("haarcascade_frontalface_default.xml");
        cascadeClassifier_eyes = loadCascade("haarcascade_eye.xml");
        Log.d(TAG, "read_cascade_file:");
    }


    protected List<? extends CameraBridgeViewBase> getCameraViewList() {
        return Collections.singletonList(mOpenCvCameraView);
    }


    //여기서부턴 퍼미션 관련 메소드
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 200;


    protected void onCameraPermissionGranted() {
        List<? extends CameraBridgeViewBase> cameraViews = getCameraViewList();
        if (cameraViews == null) {
            return;
        }
        for (CameraBridgeViewBase cameraBridgeViewBase : cameraViews) {
            if (cameraBridgeViewBase != null) {
                cameraBridgeViewBase.setCameraPermissionGranted();


            }
        }
    }

    @Override
    protected void onStart() {  //@
        super.onStart();
        boolean havePermission = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (checkSelfPermission(CAMERA) != PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(new String[]{CAMERA, WRITE_EXTERNAL_STORAGE}, CAMERA_PERMISSION_REQUEST_CODE);
                havePermission = false;
            }
        }
        if (havePermission) {
            onCameraPermissionGranted();
        }
    }

    @Override  //@
    @TargetApi(Build.VERSION_CODES.M)
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            onCameraPermissionGranted();
        } else {
            showDialogForPermission("앱을 실행하려면 퍼미션을 허가하셔야합니다.");
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    @TargetApi(Build.VERSION_CODES.M)
    private void showDialogForPermission(String msg) {

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity1.this);
        builder.setTitle("알림");
        builder.setMessage(msg);
        builder.setCancelable(false);
        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) { //@
                requestPermissions(new String[]{CAMERA, WRITE_EXTERNAL_STORAGE}, CAMERA_PERMISSION_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                finish();
            }
        });
        builder.create().show();
    }
}
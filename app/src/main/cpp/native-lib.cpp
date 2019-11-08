#include <jni.h>
#include <opencv2/opencv.hpp>
#include <android/log.h>

using namespace std;
using namespace cv;



void overlayImage(const Mat &background, const Mat &foreground,
                  Mat &output, Point2i location) {
    background.copyTo(output);


    // start at the row indicated by location, or at row 0 if location.y is negative.
    for (int y = std::max(location.y, 0); y < background.rows; ++y) {
        int fY = y - location.y; // because of the translation

        // we are done of we have processed all rows of the foreground image.
        if (fY >= foreground.rows)
            break;

        // start at the column indicated by location,

        // or at column 0 if location.x is negative.
        for (int x = std::max(location.x, 0); x < background.cols; ++x) {
            int fX = x - location.x; // because of the translation.

            // we are done with this row if the column is outside of the foreground image.
            if (fX >= foreground.cols)
                break;

            // determine the opacity of the foregrond pixel, using its fourth (alpha) channel.
            double opacity =
                    ((double) foreground.data[fY * foreground.step + fX * foreground.channels() +
                                              3])

                    / 255.;


            // and now combine the background and foreground pixel, using the opacity,

            // but only if opacity > 0.
            for (int c = 0; opacity > 0 && c < output.channels(); ++c) {
                unsigned char foregroundPx =
                        foreground.data[fY * foreground.step + fX * foreground.channels() + c];
                unsigned char backgroundPx =
                        background.data[y * background.step + x * background.channels() + c];
                output.data[y * output.step + output.channels() * x + c] =
                        backgroundPx * (1. - opacity) + foregroundPx * opacity;
            }
        }
    }
}

float resize(Mat img_src, Mat &img_resize, int resize_width){
    float scale = resize_width / (float)img_src.cols ;
    if (img_src.cols > resize_width) {
        int new_height = cvRound(img_src.rows * scale);
        resize(img_src, img_resize, Size(resize_width, new_height));
    }
    else {
        img_resize = img_src;
    }
    return scale;
}

extern "C"
JNIEXPORT jlong JNICALL
Java_com_login_tt_MainActivity1_loadCascade(JNIEnv *env, jobject instance,
                                            jstring cascadeFileName_) {
    const char *nativeFileNameString = env->GetStringUTFChars(cascadeFileName_, 0);


    string baseDir("/sdcard/");
    baseDir.append(nativeFileNameString);
    const char *pathDir = baseDir.c_str();
    jlong ret = 0;

    ret = (jlong) new CascadeClassifier(pathDir);

    if (((CascadeClassifier *) ret)->empty()) {

        __android_log_print(ANDROID_LOG_DEBUG, "native-lib :: ",

                            "CascadeClassifier로 로딩 실패  %s", nativeFileNameString);
    }
    else
        __android_log_print(ANDROID_LOG_DEBUG, "native-lib :: ",
                            "CascadeClassifier로 로딩 성공 %s", nativeFileNameString);

    env->ReleaseStringUTFChars(cascadeFileName_, nativeFileNameString);
    return ret;

}extern "C"
JNIEXPORT void JNICALL
Java_com_login_tt_MainActivity1_detect(JNIEnv *env, jobject instance, jlong cascadeClassifier_face,
                                       jlong cascadeClassifier_eyes, jlong matAddrInput,
                                       jlong matAddrResult, jint choice) {

    Mat &img_input = *(Mat *) matAddrInput;  //받은이미지

    Mat &img_result = *(Mat *) matAddrResult; //보낼이미지
    img_result = img_input.clone();   //받은이미지를 복사하여 받을 이미지에 저장한다 결국 img_result를 변경하면 mainActivity로 전달한다.
    std::vector<Rect> faces; //얼굴 좌표값 저장할 공간
    std::vector<Rect> eyes; //눈의 좌표를 저장할 공간
    Mat img_resize;
    //화면에서 얼굴비율을 scale 값을 받는다  한다 ,  size 480일때 인식 좋음
    //img_resize=img_input을 넣는다.
    float resizeRatio = resize(img_input, img_resize, 480);


    //얼굴을 검출한다 , 첫번쨰 값은 검출할 이미지 , faces는 얼굴의 좌표를 담을 배열
    // 1.1 은 잘모르겟고 , 다섯번 검사한다는 의미 ,0 은 flag로 현재 사용되징낳는다,  cacade와 size는 최대 최소 크기를 의미한다
    ((CascadeClassifier *) cascadeClassifier_face)->detectMultiScale(img_resize, faces, 1.1,5,
                                                                     0 | CASCADE_SCALE_IMAGE,
                                                                     Size(30, 30));


    ((CascadeClassifier *) cascadeClassifier_eyes)->detectMultiScale(img_resize, eyes, 1.1,3,
                                                                     0 | CASCADE_SCALE_IMAGE,
                                                                     Size(10, 10));

    __android_log_print(ANDROID_LOG_DEBUG, "native-lib :: ",
                        "found eye size() %d", eyes.size());
    __android_log_print(ANDROID_LOG_DEBUG, "native-lib :: ",
                        "found face size() %d", faces.size());


    Mat mask;

    if(choice==0){

    }else if(choice==1){
        //가면을 불러온다
        mask=imread("/sdcard/mask1.jpg");
    }else if(choice==2){
        //가면을 불러온다
        mask=imread("/sdcard/glass1.jpg");
    }
    // for문을 사용하여 얼굴을 검사한다
    // real_facesize_x 실제 얼굴에서 아까 구한 scale 로 나눈 값
    //real_facesize_width 실제 길이에서 아까 구한 scale로 나눈 값

    for (int i = 0; i < faces.size();i++) {
        //얼굴 크기 측정
        double real_facesize_x = faces[i].x / resizeRatio;
        double real_facesize_y = faces[i].y / resizeRatio;
        double real_facesize_width = faces[i].width / resizeRatio;
        double real_facesize_height = faces[i].height / resizeRatio;
        if(eyes.size()>0) {
            //눈 좌표값
            int count=0;
            for (int j = 0; j < eyes.size(); j++) {
                double real_eyes_x = eyes[j].x / resizeRatio;
                double real_eyes_y = eyes[j].y / resizeRatio;

                if(real_facesize_x<real_eyes_x&&real_eyes_x<real_facesize_x+real_facesize_width){
                    if(real_facesize_y<real_eyes_y&&real_eyes_y<real_facesize_y+real_facesize_height/2){

                        count++;
                    }
                }

                if(count==2){
                    // 얼굴의 중앙 값
                    Point faceCenter(real_facesize_x+ real_facesize_width / 2,
                                     real_facesize_y+ real_facesize_height / 2);
                    //얼굴의 원을 그린다 이미지 원본에 , 얼굴중앙값부터 반지름의 원의둘래로 ,각도 ? 뭔지모름 ,
                    //첫번째각도 0 부터 360도 그린다 , 선의 색은 초록 , 선의두끼는 10, 선 타입은 1 이다
                    ellipse(img_result, faceCenter, Size(real_facesize_width / 1.5, real_facesize_height / 1.5), 0, 0,
                            360,
                            Scalar(0, 255, 0), 10, 1, 0);

                    //얼굴의 눈위치 찾는 방법
                    double ratio = real_facesize_width/mask.cols;
                    if(choice==1){

                        Mat mask_resize;
                        //ratio나 숫자가 올라갈수록 가면은 내려간다
                        Point maskCenter(real_facesize_x,
                                         real_facesize_y+45+ratio);

                        //기존의 마스크 크기를 새로운 사이즈로 재 정리한다 , ratio는 얼굴크기를 마스크 길이로 나눈 값이다 비율에 따라 가까워지면
                        //마스크가 커지고 멀어지면 마스크가 멀어진다
                        resize(mask, mask_resize, Size(), ratio, ratio);

                        overlayImage(img_result,mask_resize,img_result,maskCenter);
                    }else if(choice==2){
                        Mat mask_resize;
                        Point maskCenter(real_facesize_x,
                                         real_facesize_y-ratio-10);

                        //기존의 마스크 크기를 새로운 사이즈로 재 정리한다 , ratio는 얼굴크기를 마스크 길이로 나눈 값이다 비율에 따라 가까워지면
                        //마스크가 커지고 멀어지면 마스크가 멀어진다
                        resize(mask, mask_resize, Size(), ratio, ratio);

                        overlayImage(img_result,mask_resize,img_result,maskCenter);

                    }
                }

            }
        }
    }

}extern "C"
JNIEXPORT jlong JNICALL
Java_com_login_tt_CallActivity_loadCascade(JNIEnv *env, jobject instance,
                                           jstring cascadeFileName_) {
    const char *nativeFileNameString = env->GetStringUTFChars(cascadeFileName_, 0);


    string baseDir("/sdcard/");
    baseDir.append(nativeFileNameString);
    const char *pathDir = baseDir.c_str();
    jlong ret = 0;

    ret = (jlong) new CascadeClassifier(pathDir);

    if (((CascadeClassifier *) ret)->empty()) {

        __android_log_print(ANDROID_LOG_DEBUG, "native-lib :: ",

                            "CascadeClassifier로 로딩 실패  %s", nativeFileNameString);
    }
    else
        __android_log_print(ANDROID_LOG_DEBUG, "native-lib :: ",
                            "CascadeClassifier로 로딩 성공 %s", nativeFileNameString);

    env->ReleaseStringUTFChars(cascadeFileName_, nativeFileNameString);
    return ret;

}extern "C"
JNIEXPORT void JNICALL
Java_com_login_tt_CallActivity_detect(JNIEnv *env, jobject instance, jlong cascadeClassifier_face,
                                      jlong cascadeClassifier_eyes, jlong matAddrInput,
                                      jlong matAddrResult, jint choice) {

    Mat &img_input = *(Mat *) matAddrInput;  //받은이미지

    Mat &img_result = *(Mat *) matAddrResult; //보낼이미지
    img_result = img_input.clone();   //받은이미지를 복사하여 받을 이미지에 저장한다 결국 img_result를 변경하면 mainActivity로 전달한다.
    std::vector<Rect> faces; //얼굴 좌표값 저장할 공간
    std::vector<Rect> eyes; //눈의 좌표를 저장할 공간
    Mat img_resize;
    //화면에서 얼굴비율을 scale 값을 받는다  한다 ,  size 480일때 인식 좋음
    //img_resize=img_input을 넣는다.
    float resizeRatio = resize(img_input, img_resize, 480);


    //얼굴을 검출한다 , 첫번쨰 값은 검출할 이미지 , faces는 얼굴의 좌표를 담을 배열
    // 1.1 은 잘모르겟고 , 다섯번 검사한다는 의미 ,0 은 flag로 현재 사용되징낳는다,  cacade와 size는 최대 최소 크기를 의미한다
    ((CascadeClassifier *) cascadeClassifier_face)->detectMultiScale(img_resize, faces, 1.1,5,
                                                                     0 | CASCADE_SCALE_IMAGE,
                                                                     Size(30, 30));


    ((CascadeClassifier *) cascadeClassifier_eyes)->detectMultiScale(img_resize, eyes, 1.1,3,
                                                                     0 | CASCADE_SCALE_IMAGE,
                                                                     Size(10, 10));

    __android_log_print(ANDROID_LOG_DEBUG, "native-lib :: ",
                        "found eye size() %d", eyes.size());
    __android_log_print(ANDROID_LOG_DEBUG, "native-lib :: ",
                        "found face size() %d", faces.size());


    Mat mask;

    if(choice==0){

    }else if(choice==1){
        //가면을 불러온다
        mask=imread("/sdcard/mask1.jpg");
    }else if(choice==2){
        //가면을 불러온다
        mask=imread("/sdcard/glass1.jpg");
    }
    // for문을 사용하여 얼굴을 검사한다
    // real_facesize_x 실제 얼굴에서 아까 구한 scale 로 나눈 값
    //real_facesize_width 실제 길이에서 아까 구한 scale로 나눈 값

    for (int i = 0; i < faces.size();i++) {
        //얼굴 크기 측정
        double real_facesize_x = faces[i].x / resizeRatio;
        double real_facesize_y = faces[i].y / resizeRatio;
        double real_facesize_width = faces[i].width / resizeRatio;
        double real_facesize_height = faces[i].height / resizeRatio;
        if(eyes.size()>0) {
            //눈 좌표값
            int count=0;
            for (int j = 0; j < eyes.size(); j++) {
                double real_eyes_x = eyes[j].x / resizeRatio;
                double real_eyes_y = eyes[j].y / resizeRatio;

                if(real_facesize_x<real_eyes_x&&real_eyes_x<real_facesize_x+real_facesize_width){
                    if(real_facesize_y<real_eyes_y&&real_eyes_y<real_facesize_y+real_facesize_height/2){

                        count++;
                    }
                }

                if(count==2){
                    // 얼굴의 중앙 값
                    Point faceCenter(real_facesize_x+ real_facesize_width / 2,
                                     real_facesize_y+ real_facesize_height / 2);
                    //얼굴의 원을 그린다 이미지 원본에 , 얼굴중앙값부터 반지름의 원의둘래로 ,각도 ? 뭔지모름 ,
                    //첫번째각도 0 부터 360도 그린다 , 선의 색은 초록 , 선의두끼는 10, 선 타입은 1 이다
                    ellipse(img_result, faceCenter, Size(real_facesize_width / 1.5, real_facesize_height / 1.5), 0, 0,
                            360,
                            Scalar(0, 255, 0), 10, 1, 0);

                    //얼굴의 눈위치 찾는 방법
                    double ratio = real_facesize_width/mask.cols;
                    if(choice==1){

                        Mat mask_resize;
                        //ratio나 숫자가 올라갈수록 가면은 내려간다
                        Point maskCenter(real_facesize_x,
                                         real_facesize_y+45+ratio);

                        //기존의 마스크 크기를 새로운 사이즈로 재 정리한다 , ratio는 얼굴크기를 마스크 길이로 나눈 값이다 비율에 따라 가까워지면
                        //마스크가 커지고 멀어지면 마스크가 멀어진다
                        resize(mask, mask_resize, Size(), ratio, ratio);

                        overlayImage(img_result,mask_resize,img_result,maskCenter);
                    }else if(choice==2){
                        Mat mask_resize;
                        Point maskCenter(real_facesize_x,
                                         real_facesize_y-ratio-10);

                        //기존의 마스크 크기를 새로운 사이즈로 재 정리한다 , ratio는 얼굴크기를 마스크 길이로 나눈 값이다 비율에 따라 가까워지면
                        //마스크가 커지고 멀어지면 마스크가 멀어진다
                        resize(mask, mask_resize, Size(), ratio, ratio);

                        overlayImage(img_result,mask_resize,img_result,maskCenter);

                    }
                }

            }
        }
    }


}
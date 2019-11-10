package io.varenyzc.mycar;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;

import com.google.android.things.pio.Gpio;
import com.starrtc.starrtcsdk.api.XHClient;
import com.starrtc.starrtcsdk.core.im.message.XHIMMessage;

import java.util.ArrayList;
import java.util.List;

import io.varenyzc.mycar.listener.IEventListener;
import io.varenyzc.mycar.peripheral.GpioManager;
import io.varenyzc.mycar.services.KeepLiveService;
import io.varenyzc.mycar.utils.AEvent;
import io.varenyzc.mycar.utils.MLOC;

public class SplashActivity extends Activity implements IEventListener {

    private boolean isLogin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams. FLAG_FULLSCREEN ,
                WindowManager.LayoutParams. FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);
        GpioManager.getInstance().init();
        GpioManager.getInstance().switchSysLed(true);
        AEvent.setHandler(new Handler());
        addListener();
        checkPermission();
    }

    private void addListener() {
        AEvent.addListener(AEvent.AEVENT_C2C_REV_MSG,this);
    }

    private void removeListener(){
        AEvent.removeListener(AEvent.AEVENT_C2C_REV_MSG, this);
    }

    private int times = 0;
    private final int REQUEST_PHONE_PERMISSIONS = 0;
    private void checkPermission(){
        times++;
        final List<String> permissionsList = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if ((checkSelfPermission(Manifest.permission.ACCESS_NETWORK_STATE)!= PackageManager.PERMISSION_GRANTED)) permissionsList.add(Manifest.permission.ACCESS_NETWORK_STATE);
            if ((checkSelfPermission(Manifest.permission.READ_PHONE_STATE)!= PackageManager.PERMISSION_GRANTED)) permissionsList.add(Manifest.permission.READ_PHONE_STATE);
            if ((checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED)) permissionsList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if ((checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED)) permissionsList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            if ((checkSelfPermission(Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED)) permissionsList.add(Manifest.permission.CAMERA);
            if ((checkSelfPermission(Manifest.permission.BLUETOOTH)!= PackageManager.PERMISSION_GRANTED)) permissionsList.add(Manifest.permission.BLUETOOTH);
            if ((checkSelfPermission(Manifest.permission.RECORD_AUDIO)!= PackageManager.PERMISSION_GRANTED)) permissionsList.add(Manifest.permission.RECORD_AUDIO);
            if (permissionsList.size() != 0){
                if(times==1){
                    requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
                            REQUEST_PHONE_PERMISSIONS);
                }else{
                    new AlertDialog.Builder(this)
                            .setCancelable(true)
                            .setTitle("提示")
                            .setMessage("获取不到授权，APP将无法正常使用，请允许APP获取权限！")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
                                                REQUEST_PHONE_PERMISSIONS);
                                    }
                                }
                            }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            finish();
                        }
                    }).show();
                }
            }else{
                initSDK();
            }
        }else{
            initSDK();
        }
    }

    private void initSDK(){
        startService();
        //startAnimation();
    }


    private void startService(){
        Intent intent = new Intent(SplashActivity.this, KeepLiveService.class);
        startService(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,  final String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        checkPermission();
    }

    private void startCar(){
        isLogin = true;
        if (isLogin) {
            startActivity(new Intent(SplashActivity.this,MainActivity.class));
            removeListener();
            finish();
        }
    }

    @SuppressLint("WrongConstant")
    private void startAnimation(){
        isLogin = true;
        final View eye = findViewById(R.id.eye);
        eye.setAlpha(0.2f);
        final View black = findViewById(R.id.black_view);
        final View white = findViewById(R.id.white_view);

        final ObjectAnimator va = ObjectAnimator.ofFloat(eye,"alpha",0.2f,1f);
        va.setDuration(1000);
        va.setRepeatCount(ValueAnimator.INFINITE);
        va.setRepeatMode(Animation.REVERSE);
        va.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                if(isLogin){
                    va.cancel();
                    ObjectAnimator va1 = ObjectAnimator.ofFloat(white,"alpha",0f,1f);
                    ObjectAnimator va2 = ObjectAnimator.ofFloat(black,"alpha",1f,0f);

                    AnimatorSet animatorSet = new AnimatorSet();
                    animatorSet.setDuration(1500);
                    animatorSet.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            new Handler(){
                                @Override
                                public void handleMessage(Message msg){
                                    startActivity(new Intent(SplashActivity.this,MainActivity.class));
                                    finish();
                                }

                            }.sendEmptyMessageDelayed(0,500);
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    });
                    animatorSet.playTogether(va1,va2);
                    animatorSet.start();
                }
            }
        });
        va.start();
    }

    @Override
    public void dispatchEvent(String aEventID, boolean success, Object eventObj) {
        switch (aEventID) {
            case AEvent.AEVENT_C2C_REV_MSG:
                XHIMMessage message = (XHIMMessage) eventObj;
                String commond = message.contentData;
                if (commond.equals("MyCarStart")) {
                    startCar();
                    //MLOC.d("varenyzc", commond);
                    //XHClient.getInstance().getChatManager().sendOnlineMessage("mycar_1", "mycar", null);
                }
                break;
        }
    }
}

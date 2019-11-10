package io.varenyzc.mycar.services;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.starrtc.starrtcsdk.api.XHClient;
import com.starrtc.starrtcsdk.api.XHConstants;
import com.starrtc.starrtcsdk.api.XHCustomConfig;
import com.starrtc.starrtcsdk.apiInterface.IXHErrorCallback;
import com.starrtc.starrtcsdk.apiInterface.IXHResultCallback;
import com.starrtc.starrtcsdk.core.videosrc.XHVideoSourceManager;

import java.util.Random;

import io.varenyzc.mycar.MainActivity;
import io.varenyzc.mycar.R;
import io.varenyzc.mycar.listener.DemoVideoSourceCallback;
import io.varenyzc.mycar.listener.IEventListener;
import io.varenyzc.mycar.listener.XHChatManagerListener;
import io.varenyzc.mycar.listener.XHGroupManagerListener;
import io.varenyzc.mycar.listener.XHLoginManagerListener;
import io.varenyzc.mycar.listener.XHVoipManagerListener;
import io.varenyzc.mycar.listener.XHVoipP2PManagerListener;
import io.varenyzc.mycar.peripheral.GpioManager;
import io.varenyzc.mycar.utils.AEvent;
import io.varenyzc.mycar.utils.MLOC;


/**
 * Created by zhangjt on 2017/8/6.
 */

public class KeepLiveService extends Service implements IEventListener {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        initSDK();
        return super.onStartCommand(intent, flags, startId);
    }

    private void initSDK(){
        MLOC.init(this);
        initFree();
    }

    private boolean isLogin = false;
    //开放版SDK初始化
    private void initFree(){
        MLOC.d("KeepLiveService","initFree");
        isLogin = XHClient.getInstance().getIsOnline();
        if(!isLogin){
            if(MLOC.userId.equals("")){
                MLOC.userId = "mycar";
                MLOC.saveUserId(MLOC.userId);
            }
            addListener();

            XHCustomConfig customConfig =  XHCustomConfig.getInstance();
            customConfig.setChatroomServerUrl(MLOC.CHATROOM_SERVER_URL);
            customConfig.setLiveSrcServerUrl(MLOC.LIVE_SRC_SERVER_URL);
            customConfig.setLiveVdnServerUrl(MLOC.LIVE_VDN_SERVER_URL);
            customConfig.setImServerUrl(MLOC.IM_SERVER_URL);
            customConfig.setVoipServerUrl(MLOC.VOIP_SERVER_URL);
            customConfig.initSDKForFree(this, MLOC.userId, new IXHErrorCallback() {
                @Override
                public void error(final String errMsg, Object data) {
                    MLOC.showMsg(KeepLiveService.this,errMsg);
                }
            },new Handler());
//        customConfig.setLogDirPath(Environment.getExternalStorageDirectory().getPath()+"/starrtcLog");
            customConfig.setDefConfigOpenGLESEnable(false);
            customConfig.setDefConfigCamera2Enable(true);
            customConfig.setDefConfigCameraId(0);
            customConfig.setDefConfigIsIotDevice(true);
            /*customConfig.setCustomEncoderConfig(640,480,
                    640,480,30,1024,45);*/
            customConfig.setDefConfigVideoSize(XHConstants.XHCropTypeEnum.STAR_VIDEO_CONFIG_360BW_640BH_180SW_320SH); //垃圾摄像头
            XHClient.getInstance().getChatManager().addListener(new XHChatManagerListener());
            XHClient.getInstance().getGroupManager().addListener(new XHGroupManagerListener());
            XHClient.getInstance().getVoipManager().addListener(new XHVoipManagerListener());
            XHClient.getInstance().getVoipP2PManager().addListener(new XHVoipP2PManagerListener());
            XHClient.getInstance().getLoginManager().addListener(new XHLoginManagerListener());
            XHVideoSourceManager.getInstance().setVideoSourceCallback(new DemoVideoSourceCallback());

            XHClient.getInstance().getLoginManager().loginFree(new IXHResultCallback() {
                @Override
                public void success(Object data) {
                    isLogin = true;
                }
                @Override
                public void failed(final String errMsg) {
                    MLOC.d("KeepLiveService",errMsg);
                    MLOC.showMsg(KeepLiveService.this,errMsg);
                }
            });
        }

    }

    @Override
    public void dispatchEvent(String aEventID, boolean success, Object eventObj) {
        switch (aEventID){
            case AEvent.AEVENT_USER_OFFLINE:
                    if (XHClient.getInstance().getIsOnline()) {
                        GpioManager.getInstance().switchNetLed(true);
                    } else {
                        GpioManager.getInstance().switchNetLed(false);
                    }
                break;
            case AEvent.AEVENT_USER_ONLINE:
                    if (XHClient.getInstance().getIsOnline()) {
                        GpioManager.getInstance().switchNetLed(true);
                    } else {
                        GpioManager.getInstance().switchNetLed(false);
                    }
                break;
            case AEvent.AEVENT_VOIP_REV_CALLING:
                if(MLOC.canPickupVoip){
                    /*Intent intent = new Intent(this, VoipRingingActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
                    intent.putExtra("targetId",eventObj.toString());
                    startActivity(intent);*/
                }else{
                    MLOC.hasNewVoipMsg = true;
                }
                break;
            case AEvent.AEVENT_VOIP_REV_CALLING_AUDIO:
                if(MLOC.canPickupVoip){
                    /*Intent intent = new Intent(this, VoipAudioRingingActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
                    intent.putExtra("targetId",eventObj.toString());
                    startActivity(intent);*/
                }else{
                    MLOC.hasNewVoipMsg = true;
                }
                break;
            case AEvent.AEVENT_VOIP_P2P_REV_CALLING:
                if(MLOC.canPickupVoip){
                    /*Intent intent = new Intent(this, VoipP2PRingingActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
                    intent.putExtra("targetId",eventObj.toString());
                    startActivity(intent);*/
                }
                break;
            case AEvent.AEVENT_VOIP_P2P_REV_CALLING_AUDIO:
                if(MLOC.canPickupVoip){
                    /*Intent intent = new Intent(this, VoipP2PRingingActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
                    intent.putExtra("targetId",eventObj.toString());
                    startActivity(intent);*/
                }
                break;
            case AEvent.AEVENT_C2C_REV_MSG:
                MLOC.hasNewC2CMsg = true;
                break;
            case AEvent.AEVENT_GROUP_REV_MSG:
                MLOC.hasNewGroupMsg = true;
                break;
            case AEvent.AEVENT_LOGOUT:
                removeListener();
                this.stopSelf();
                break;

        }
    }


    private void addListener(){
        AEvent.addListener(AEvent.AEVENT_USER_OFFLINE, this);
        AEvent.addListener(AEvent.AEVENT_USER_ONLINE,this);
        AEvent.addListener(AEvent.AEVENT_LOGOUT,this);
        AEvent.addListener(AEvent.AEVENT_VOIP_REV_CALLING,this);
        AEvent.addListener(AEvent.AEVENT_VOIP_REV_CALLING_AUDIO,this);
        AEvent.addListener(AEvent.AEVENT_VOIP_P2P_REV_CALLING,this);
        AEvent.addListener(AEvent.AEVENT_C2C_REV_MSG,this);
        AEvent.addListener(AEvent.AEVENT_GROUP_REV_MSG,this);
    }
    private void removeListener(){
        AEvent.removeListener(AEvent.AEVENT_USER_OFFLINE, this);
        AEvent.removeListener(AEvent.AEVENT_USER_ONLINE,this);
        AEvent.removeListener(AEvent.AEVENT_LOGOUT,this);
        AEvent.removeListener(AEvent.AEVENT_VOIP_REV_CALLING,this);
        AEvent.removeListener(AEvent.AEVENT_VOIP_REV_CALLING_AUDIO,this);
        AEvent.removeListener(AEvent.AEVENT_VOIP_P2P_REV_CALLING,this);
        AEvent.removeListener(AEvent.AEVENT_C2C_REV_MSG,this);
        AEvent.removeListener(AEvent.AEVENT_GROUP_REV_MSG,this);
    }

}

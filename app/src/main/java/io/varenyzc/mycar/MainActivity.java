package io.varenyzc.mycar;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.starrtc.starrtcsdk.api.XHClient;
import com.starrtc.starrtcsdk.api.XHConstants;
import com.starrtc.starrtcsdk.api.XHCustomConfig;
import com.starrtc.starrtcsdk.api.XHLiveItem;
import com.starrtc.starrtcsdk.api.XHLiveManager;
import com.starrtc.starrtcsdk.api.XHSDKHelper;
import com.starrtc.starrtcsdk.apiInterface.IXHResultCallback;
import com.starrtc.starrtcsdk.core.im.message.XHIMMessage;
import com.starrtc.starrtcsdk.core.player.StarPlayer;
import com.starrtc.starrtcsdk.core.pusher.XHCameraRecorder;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import io.varenyzc.mycar.listener.IEventListener;
import io.varenyzc.mycar.listener.XHLiveManagerListener;
import io.varenyzc.mycar.peripheral.GpioManager;
import io.varenyzc.mycar.peripheral.PwmManager;
import io.varenyzc.mycar.peripheral.UartManager;
import io.varenyzc.mycar.utils.AEvent;
import io.varenyzc.mycar.utils.InterfaceUrls;
import io.varenyzc.mycar.utils.MLOC;
import io.varenyzc.mycar.utils.StarNetUtil;

/**
 * @author varenyzc
 */
public class MainActivity extends Activity implements IEventListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private boolean isLogin = false;
    private TextView tv_userId;
    private TextView tv_isLogin;
    private TextView tv_ip;

    public static String CREATER_ID         = "CREATER_ID";          //创建者ID
    public static String LIVE_TYPE          = "LIVE_TYPE";           //创建信息
    public static String LIVE_ID            = "LIVE_ID";            //直播ID
    public static String LIVE_NAME          = "LIVE_NAME";          //直播名称

    private String mPrivateMsgTargetId;
    private XHLiveManager liveManager;
    private Boolean isUploader = false;
    private String createrId;
    private String liveId;
    private String liveName;
    private XHConstants.XHLiveType liveType;

    @Override
    protected void onResume() {
        super.onResume();
        if(MLOC.hasLogout){
            finish();
            MLOC.hasLogout = false;
            return;
        }
        if(MLOC.userId==null){
            startActivity(new Intent(MainActivity.this,SplashActivity.class));
            finish();
        }
        isLogin = XHClient.getInstance().getIsOnline();
        if (isLogin) {
            tv_isLogin.setText("当前在线");
            GpioManager.getInstance().switchNetLed(true);
        } else {
            tv_isLogin.setText("当前掉线，请检查网络状态");
            GpioManager.getInstance().switchNetLed(false);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv_userId = findViewById(R.id.usrId);
        tv_isLogin = findViewById(R.id.isLogin);
        tv_ip = findViewById(R.id.ip);
        MLOC.userId = MLOC.loadSharedData(getApplicationContext(),"userId");
        tv_userId.setText("userId:"+MLOC.userId);
        tv_ip.setText(StarNetUtil.getIP(this));
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        PwmManager.getInstance().startPwm();
        UartManager.getInstance().init();
        addListener();
        createLive();
    }

    private void createLive() {
        /*mXHSDKHelper = new XHSDKHelper();
        mXHSDKHelper.setDefaultCameraId(0);
        //mXHSDKHelper.startPerview(this,mStarPlayer);*/

        init();
    }

    private void init() {
        createrId = MLOC.userId;
        liveName = MLOC.userId+"_1";
        liveType = XHConstants.XHLiveType.XHLiveTypeGlobalPublic;
        liveManager = XHClient.getInstance().getLiveManager(this);
        liveManager.setRtcMediaType(XHConstants.XHRtcMediaTypeEnum.STAR_RTC_MEDIA_TYPE_VIDEO_ONLY);
        liveManager.setRecorder(new XHCameraRecorder());
        liveManager.addListener(new XHLiveManagerListener());
        if (liveId == null) {
            createNewLive();
        }else{
            starLive();
        }
    }

    private void starLive() {
        isUploader = true;
        liveManager.startLive(liveId, new IXHResultCallback() {
            @Override
            public void success(Object data) {
                MLOC.d("XHLiveManager","startLive success "+data);
            }
            @Override
            public void failed(final String errMsg) {
                MLOC.d("XHLiveManager","startLive failed "+errMsg);
                //MLOC.showMsg(VideoLiveActivity.this,errMsg);
                //stopAndFinish();
            }
        });
    }

    private void createNewLive() {
        isUploader = true;
        XHLiveItem liveItem = new XHLiveItem();
        liveItem.setLiveName(liveName);
        liveItem.setLiveType(liveType);
        liveManager.createLive(liveItem, new IXHResultCallback() {
            @Override
            public void success(Object data) {
                liveId = (String) data;
                starLive();
//上报到直播列表
                try {
                    JSONObject info = new JSONObject();
                    info.put("id",liveId);
                    info.put("creator",MLOC.userId);
                    info.put("name",liveName);
                    String infostr = info.toString();
                    infostr = URLEncoder.encode(infostr,"utf-8");
                    if(MLOC.AEventCenterEnable){
                        InterfaceUrls.demoSaveToList(MLOC.userId,MLOC.LIST_TYPE_LIVE,liveId,infostr);
                    }else {
                        liveManager.saveToList(MLOC.userId, MLOC.LIST_TYPE_LIVE, liveId, infostr, null);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void failed(final String errMsg) {
                MLOC.showMsg(MainActivity.this,errMsg);
                //stopAndFinish();
            }
        });
    }

    private void addListener() {
        AEvent.addListener(AEvent.AEVENT_USER_ONLINE,this);
        AEvent.addListener(AEvent.AEVENT_USER_OFFLINE,this);
        AEvent.addListener(AEvent.AEVENT_LIVE_REV_REALTIME_DATA,this);
        AEvent.addListener(AEvent.AEVENT_LIVE_REV_MSG, this);
    }

    private void removeListener(){
        AEvent.removeListener(AEvent.AEVENT_USER_ONLINE,this);
        AEvent.removeListener(AEvent.AEVENT_USER_OFFLINE,this);
        AEvent.removeListener(AEvent.AEVENT_LIVE_REV_REALTIME_DATA,this);
        AEvent.removeListener(AEvent.AEVENT_LIVE_REV_MSG, this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeListener();
    }

    @Override
    public void dispatchEvent(String aEventID, boolean success, Object eventObj) {
        switch (aEventID) {
            case AEvent.AEVENT_USER_OFFLINE:
                MLOC.showMsg(MainActivity.this, "服务已断开");
                if (findViewById(R.id.isLogin) != null) {
                    if (XHClient.getInstance().getIsOnline()) {
                        tv_isLogin.setText("当前在线");
                        GpioManager.getInstance().switchNetLed(true);
                    } else {
                        tv_isLogin.setText("当前掉线，请检查网络状态");
                        GpioManager.getInstance().switchNetLed(false);
                    }
                }
                break;
            case AEvent.AEVENT_USER_ONLINE:
                if (findViewById(R.id.isLogin) != null) {
                    if (XHClient.getInstance().getIsOnline()) {
                        tv_isLogin.setText("当前在线");
                        GpioManager.getInstance().switchNetLed(true);
                    } else {
                        tv_isLogin.setText("当前掉线，请检查网络状态");
                        GpioManager.getInstance().switchNetLed(false);
                    }
                }
                break;
            case AEvent.AEVENT_LIVE_REV_REALTIME_DATA:
                if (success) {
                    try {
                        JSONObject jsonObject = (JSONObject) eventObj;
                        final byte[] tData = (byte[]) jsonObject.get("data");
                        Log.d(TAG, "dispatchEvent: "+tData.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            case AEvent.AEVENT_LIVE_REV_MSG:
                XHIMMessage revMsgPrivate = (XHIMMessage) eventObj;
                Log.d(TAG, "dispatchEvent: "+revMsgPrivate.contentData);
                PwmManager.getInstance().gotCommand(revMsgPrivate.contentData);
                if(!revMsgPrivate.contentData.contains("camera")){
                    UartManager.getInstance().write( revMsgPrivate.contentData+"\r\n");
                    Log.d("varenyzc2", revMsgPrivate.contentData + "\r\n");

                }
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return true;
    }
}

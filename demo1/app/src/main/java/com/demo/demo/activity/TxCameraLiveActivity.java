package com.demo.demo.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.demo.demo.R;
import com.demo.demo.util.TimeUtil;
import com.tencent.live.TXLivePusher;
import com.tencent.rtmp.ITXLivePushListener;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.TXLivePushConfig;
import com.tencent.rtmp.ui.TXCloudVideoView;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TxCameraLiveActivity extends AppCompatActivity implements ITXLivePushListener, com.tencent.rtmp.TXLivePusher.OnBGMNotify {

    private TXLivePushConfig mLivePushConfig;
    private TXLivePusher mLivePusher;
    private TXCloudVideoView liveCameraView;

    private Button endLiveBtn;

    //事实情况是，只使用摄像头推流，不使用录屏推流，所以 isUseCamera == true
    private boolean isUseCamera;        // true:使用摄像头
    private String TAG = "demo.CameraLive";

    // TODO: 推流地址写一下
    // rtmp://推流域名/live/39***_1*****?txSecret=b******c&txTime=5**d
    private static String rtmpURL = ""; //此处填写 rtmp 推流地址

    private static final char[] DIGITS_LOWER =
            {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tx_camera_live);
        checkUsePermission();
        init();
    }

    private void init() {
        generatePushUrl();

        isUseCamera = getIntent().getBooleanExtra("UseCamera", false);

        mLivePushConfig  = new TXLivePushConfig();
        mLivePusher = new TXLivePusher(this);

        /*一些配置
        mLivePushConfig.setVideoEncodeGop(5);

        mWaterMarkBitmap = decodeResource(getResources(), R.drawable.watermark);
        * */
        // 一般情况下不需要修改 config 的默认配置
        mLivePusher.setConfig(mLivePushConfig);

        liveCameraView = findViewById(R.id.camera_preview);
        endLiveBtn = findViewById(R.id.btn_endLive);
        endLiveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeFun();
            }
        });

        startPush();
    }


    /*
    * 生成推流地址
    *
    * 形如 rtmp://推流域名/live/39***_1*****?txSecret=b******c&txTime=5**d
    * 大体思路  【rtmp://推流域名/live/流名称?腾讯云官方生成的鉴权串】
    * 其中流名称为 【用户id_时间长整型（单位s）】
    * 鉴权生成需要三个参数：key（见推流域名的详细内容中）、用户id、截止时间的长整型（单位s）
    * */
    private void generatePushUrl() {
        TimeUtil.test();
        long nowTime = new Date().getTime();
        long nowTimeSecond = TimeUtil.msToSecond(nowTime);      //当前时间，单位s
        long txTime = TimeUtil.msToSecond(TimeUtil.tomorrowTime(nowTime));    //获取明日时间作为截止时间，单位s
        String key = "";    //鉴权加密key
        String userId = "39***";
        String streamName = userId + "_" + nowTimeSecond;
        rtmpURL = "rtmp://推流域名/live/"+streamName+"?"+getSafeUrl(key, streamName, txTime);
        generatePullUrl(streamName);    // 顺便生成一下拉流地址
        Log.d(TAG ,"time/1000:"+txTime+" ,PushUrl:   "+rtmpURL);
    }

    private String generatePullUrl(String streamName) {
        String pullUrl = "http://播流域名/live/"+streamName+".flv";
        Log.d(TAG, "pullUrl: "+pullUrl);
        return pullUrl;
    }

    /*
     * KEY+ streamName + txTime
     */
    private static String getSafeUrl(String key, String streamName, long txTime) {
        String input = new StringBuilder().
                append(key).
                append(streamName).
                append(Long.toHexString(txTime).toUpperCase()).toString();
        String txSecret = null;
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            txSecret  = byteArrayToHexString(
                    messageDigest.digest(input.getBytes("UTF-8")));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return txSecret == null ? "" :
                new StringBuilder().
                        append("txSecret=").
                        append(txSecret).
                        append("&").
                        append("txTime=").
                        append(Long.toHexString(txTime).toUpperCase()).
                        toString();
    }
    private static String byteArrayToHexString(byte[] data) {
        char[] out = new char[data.length << 1];
        for (int i = 0, j = 0; i < data.length; i++) {
            out[j++] = DIGITS_LOWER[(0xF0 & data[i]) >>> 4];
            out[j++] = DIGITS_LOWER[0x0F & data[i]];
        }
        return new String(out);
    }


    /*
    *   权限相关回调
    * */
    // 勿忘加入权限的回调，否则没法提示、申请权限等
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 100:
                for (int ret : grantResults) {
                    if (ret != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                }
                break;
            default:
                break;
        }
    }

    public boolean checkUsePermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            List<String> permissions = new ArrayList<>();
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)) {
                //ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 2);

                permissions.add(Manifest.permission.CAMERA);
            } else {
                Log.d(TAG, "open camera");
            }
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)) {
                permissions.add(Manifest.permission.RECORD_AUDIO);
            }
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)) {
                permissions.add(Manifest.permission.READ_PHONE_STATE);
            }
            Log.d(TAG, "checkPermission-beforeAddPermission-"+permissions.size());
            if (permissions.size() != 0) {
                ActivityCompat.requestPermissions(this,
                        permissions.toArray(new String[0]),
                        100);
                return false;
            }
        }
        return true;
    }

    private void closeFun() {
        if (isUseCamera) {
            mLivePusher.stopCameraPreview(true);//如果已经启动了摄像头预览，在结束推流时将其关闭
        } else {
            mLivePusher.stopScreenCapture();
        }
        mLivePusher.setPushListener(null);
        mLivePusher.stopPusher();
    }

    private void startPush() {
        Log.d(TAG, "---ready to push--");

        //播放回调
        mLivePusher.setPushListener(this);
        //监听声音
        mLivePusher.setBGMNofify(this);
        //麦克风推流
        mLivePusher.setMute(false);

        liveCameraView.setVisibility(View.VISIBLE);
        if (isUseCamera) {
            mLivePusher.startCameraPreview(liveCameraView);
        } else {
            mLivePusher.startScreenCapture();
        }

        int ret = mLivePusher.startPusher(rtmpURL.trim());
        if (ret == -5) {
            Log.i(TAG, "startRTMPPush: license 校验失败");
        }
    }


    //推流器状态回调
    @Override
    public void onPushEvent(int event, Bundle param) {
        // Toast错误内容
        if (event < 0) {
            Toast.makeText(getApplicationContext(), param.getString(TXLiveConstants.EVT_DESCRIPTION), Toast.LENGTH_SHORT).show();
        }

        if (event == TXLiveConstants.PUSH_ERR_NET_DISCONNECT
                || event == TXLiveConstants.PUSH_ERR_INVALID_ADDRESS
                || event == TXLiveConstants.PUSH_ERR_OPEN_CAMERA_FAIL
                || event == TXLiveConstants.PUSH_ERR_OPEN_MIC_FAIL) {
            // 遇到以上错误，则停止推流
            stopRTMPPush();
        } else if (event == TXLiveConstants.PUSH_WARNING_HW_ACCELERATION_FAIL) {
            // 开启硬件加速失败
            Toast.makeText(getApplicationContext(), param.getString(TXLiveConstants.EVT_DESCRIPTION), Toast.LENGTH_SHORT).show();
        } else if (event == TXLiveConstants.PUSH_EVT_CHANGE_RESOLUTION) {
            Log.d(TAG, "change resolution to " + param.getInt(TXLiveConstants.EVT_PARAM2) + ", bitrate to" + param.getInt(TXLiveConstants.EVT_PARAM1));
        } else if (event == TXLiveConstants.PUSH_EVT_CHANGE_BITRATE) {
            Log.d(TAG, "change bitrate to" + param.getInt(TXLiveConstants.EVT_PARAM1));
        } else if (event == TXLiveConstants.PUSH_WARNING_NET_BUSY) {
            //showNetBusyTips();
        } else if (event == TXLiveConstants.PUSH_EVT_START_VIDEO_ENCODER) {
//            int encType = param.getInt(TXLiveConstants.EVT_PARAM1);
//            boolean hwAcc = (encType == TXLiveConstants.ENCODE_VIDEO_HARDWARE);
//            Toast.makeText(CameraPusherActivity.this, "是否启动硬编：" + hwAcc, Toast.LENGTH_SHORT).show();
        } else if (event == TXLiveConstants.PUSH_EVT_OPEN_CAMERA_SUCC) {
            // 只有后置摄像头可以打开闪光灯，若默认需要开启闪光灯。 那么在打开摄像头成功后，才可以进行配置。 若果当前是前置，设定无效；若是后置，打开闪光灯。
            //mLivePusher.turnOnFlashLight(mPushMoreFragment.isFlashEnable());
        }
    }

    /**
     * 停止 RTMP 推流
     */
    private void stopRTMPPush() {

        // 停止BGM
        mLivePusher.stopBGM();

        if (isUseCamera) {
            mLivePusher.stopCameraPreview(true);    //停止本地预览
        } else {
            mLivePusher.stopScreenCapture();
        }
        // 移除监听
        mLivePusher.setPushListener(null);
        // 停止推流
        mLivePusher.stopPusher();
        // 隐藏本地预览的View
        liveCameraView.setVisibility(View.GONE);
        // 移除垫片图像
        //mLivePushConfig.setPauseImg(null);
        // 关闭隐私模式

    }

    @Override
    public void onNetStatus(Bundle bundle) {

    }

    @Override
    public void onBGMStart() {

    }

    @Override
    public void onBGMProgress(long l, long l1) {

    }

    @Override
    public void onBGMComplete(int i) {

    }
}

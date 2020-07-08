package com.demo.demo_agora_1.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.demo.demo_agora_1.R;

import java.util.ArrayList;
import java.util.List;

import io.agora.rtc.Constants;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoCanvas;

public class LiveRoomActivity extends AppCompatActivity {

    private static String TAG = "hello.LiveRoomActivity";

    private Button videoPlayBtn, audioPlayBtn, switchCameraBtn, returnBtn;
    private TextView textViewTitle;

    private String liveRoomId, token = "";
    private List<Integer> mUidList = new ArrayList<>(4);
    private int isAudience;
    // 创建 SurfaceView 对象。
    private FrameLayout mLocalContainer;

    private boolean isAudioMute = true, isCameraMute = true;
    private int uid = 1;

    private RtcEngine mRtcEngine;
    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {
        @Override
        // 注册 onJoinChannelSuccess 回调。
        // 本地用户成功加入频道时，会触发该回调。
        public void onJoinChannelSuccess(String channel, final int uid, int elapsed) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.i(TAG,"本地用户成功加入频道 ,Join channel success, uid: " + (uid & 0xFFFFFFFFL));
                }
            });
        }

        @Override
        // 注册 onFirstRemoteVideoDecoded 回调。
        // SDK 接收到第一帧远端视频并成功解码时，会触发该回调。
        // 可以在该回调中调用 setupRemoteVideo 方法设置远端视图。
        public void onFirstRemoteVideoDecoded(final int uid, int width, int height, int elapsed) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.i(TAG,"第一帧远端视频并成功解码 ,First remote video decoded, uid: " + (uid & 0xFFFFFFFFL));
                    setupRemoteVideo(uid);
                }
            });
        }

        @Override
        // 注册 onUserOffline 回调。
        // 远端主播离开频道或掉线时，会触发该回调。
        public void onUserOffline(final int uid, final int reason) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.i(TAG,"远端主播离开频道或掉线 ,User offline, uid: " + (uid & 0xFFFFFFFFL)+" . reason:"+ reason);
                    Toast.makeText(getBaseContext(), "主播已下线", Toast.LENGTH_SHORT).show();
                    onRemoteUserLeft(uid);
                }
            });
        }

        /*
        // ******************
        // 之前加上了本方法，情况是，一主播+一观众，观众身份变成主播后，原观众关闭直播，则原主播也下线
        // ******************
        @Override
        public void onUserMuteVideo(final int uid, final boolean muted) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //其他用户已停发/已重发视频流回调
                    Log.i(TAG,"其他用户已停发/已重发视频流回调, uid: " + (uid & 0xFFFFFFFFL)+" . muted:"+ muted + "isAudience" + isAudience);
                    onRemoteUserVideoMuted(uid, muted);
                }
            });
        }*/
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_room);

        init();
        initializeEngine();
        if (isAudience == 1) startPush();
        else mRtcEngine.joinChannel(token, liveRoomId, "", uid++);
    }

    private void init() {
        liveRoomId = getIntent().getStringExtra("liveRoomName");
        isAudience = getIntent().getIntExtra("isAudience", 2);

        videoPlayBtn = findViewById(R.id.btn_video);
        audioPlayBtn = findViewById(R.id.btn_audio);
        switchCameraBtn = findViewById(R.id.btn_switch_camera);
        returnBtn = findViewById(R.id.btn_return);
        mLocalContainer = findViewById(R.id.layout_video_view);
        textViewTitle = findViewById(R.id.textView_title);

        textViewTitle.setText("直播间名称（ID）： " + liveRoomId);
        if (isCameraMute) {         //摄像头开启
            videoPlayBtn.setText("关闭摄像头");
        } else {
            videoPlayBtn.setText("开启摄像头");
        }
        if (isAudioMute) {         //声音开启
            audioPlayBtn.setText("关闭声音");
        } else {
            audioPlayBtn.setText("开启声音");
        }

        videoPlayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                controlVideo();
            }
        });
        audioPlayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                controlAudio();
            }
        });
        switchCameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchCamera();
            }
        });
        returnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeLive();
            }
        });
    }

    // 初始化 RtcEngine 对象。
    private void initializeEngine() {
        try {
            mRtcEngine = RtcEngine.create(getBaseContext(), getString(R.string.private_app_id), mRtcEventHandler);
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
            throw new RuntimeException("NEED TO check rtc sdk init fatal error\n" + Log.getStackTraceString(e));
        }
        mRtcEngine.enableVideo();       // 打开视频模式
        mRtcEngine.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING);      // 设置本地视频属性频道场景：直播

        mRtcEngine.setClientRole(isAudience);       // 1：主播， 2：观众

        // mRtcEngine.enableWebSdkInteroperability(true);
    }

    private void controlVideo() {       // 开启或停止直播
        isCameraMute = !isCameraMute;
        mRtcEngine.muteLocalVideoStream(isCameraMute);
        if (isCameraMute) {         //摄像头开启
            startPush();
            videoPlayBtn.setText("关闭摄像头");
        } else {
            stopPush(isCameraMute);
            videoPlayBtn.setText("开启摄像头");
        }
    }

    private void controlAudio() {
        isAudioMute = !isAudioMute;
        mRtcEngine.muteLocalAudioStream(isAudioMute);
        if (isAudioMute) {         //声音开启
            audioPlayBtn.setText("关闭声音");
        } else {
            audioPlayBtn.setText("开启声音");
        }
    }

    private void switchCamera() {
        mRtcEngine.switchCamera();
    }

    private void closeLive() {
        mRtcEngine.setupLocalVideo(null);
        //if (!mCallEnd) {
        leaveChannel();
        //}
        RtcEngine.destroy();
    }

    private void setupRemoteVideo(int uid) {
        if (mLocalContainer.getChildCount() >= 1) {
            return ;
        }
        SurfaceView mLocalView = RtcEngine.CreateRendererView(getBaseContext());
        mLocalContainer.addView(mLocalView);
        mLocalView.setTag(uid);
        if (mUidList.contains(uid)) {
            mUidList.remove((Integer) uid);
            //mUserViewList.remove(uid);
        }

        if (mUidList.size() < 4) {
            //id = uid;
        }
        mUidList.add(uid);
        mRtcEngine.setupRemoteVideo(new VideoCanvas(mLocalView, VideoCanvas.RENDER_MODE_HIDDEN, uid));
    }

    private void onRemoteUserLeft(int uid) {
        Log.d(TAG, "test");
        mLocalContainer.removeAllViews();
        mRtcEngine.setupRemoteVideo(new VideoCanvas(null, VideoCanvas.RENDER_MODE_HIDDEN, uid));
        if (mUidList.contains(uid)) {
            mUidList.remove((Integer) uid);
        }
    }

    private void onRemoteUserVideoMuted(int uid, boolean muted) {
        SurfaceView mLocalView = RtcEngine.CreateRendererView(getBaseContext());
        Object tag = mLocalView.getTag();
        if (tag != null && String.valueOf(uid).equals(tag.toString())) {
            mLocalView.setVisibility(muted ? View.GONE : View.VISIBLE);
        }
    }

    private void startPush() {      // 主播
        mRtcEngine.setClientRole(1);
        setupLocalVideo();
        Log.d(TAG, "----开启本地视图---");

        mRtcEngine.joinChannel(token, liveRoomId, "", 0);
        Log.d(TAG, "---进入频道---");
    }

    private void stopPush(boolean isCameraMute) {
        mRtcEngine.setClientRole(2);        //设为观众
        mRtcEngine.setupLocalVideo(null);
        if (mUidList.contains(0)) {
            mUidList.remove(0);
        }
        SurfaceView mLocalView = RtcEngine.CreateRendererView(getBaseContext());
        mLocalView.setZOrderMediaOverlay(true);
        mLocalView.setVisibility(isCameraMute ? View.GONE : View.VISIBLE);
        mLocalContainer.removeAllViews();
        mRtcEngine.muteLocalAudioStream(false);
        videoPlayBtn.setText("开启摄像头");
        Log.d(TAG, "---关闭直播---");
    }

    // 设置本地视图，以便主播在直播中看到本地图像
    private void setupLocalVideo() {
        SurfaceView mLocalView = RtcEngine.CreateRendererView(getBaseContext());
        mLocalView.setZOrderMediaOverlay(true);
        mLocalContainer.addView(mLocalView);
        // 设置本地视图。
        VideoCanvas localVideoCanvas = new VideoCanvas(mLocalView, VideoCanvas.RENDER_MODE_HIDDEN, 0);
        mRtcEngine.setupLocalVideo(localVideoCanvas);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //if (!mCallEnd) {
            leaveChannel();
        //}
        RtcEngine.destroy();
        mRtcEngine = null;
    }

    private void leaveChannel() {
        // 离开当前频道。
        mRtcEngine.leaveChannel();
    }

    public void onEncCallClicked(View view) {
        finish();
    }
}

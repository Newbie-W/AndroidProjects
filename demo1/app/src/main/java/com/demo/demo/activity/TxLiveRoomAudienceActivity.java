package com.demo.demo.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.demo.demo.R;
import com.tencent.rtmp.ITXLivePlayListener;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.TXLivePlayConfig;
import com.tencent.rtmp.TXLivePlayer;
import com.tencent.rtmp.ui.TXCloudVideoView;

public class TxLiveRoomAudienceActivity extends AppCompatActivity implements ITXLivePlayListener {

    public static final int ACTIVITY_TYPE_PUBLISH      = 1;
    public static final int ACTIVITY_TYPE_LIVE_PLAY    = 2;
    public static final int ACTIVITY_TYPE_VOD_PLAY     = 3;
    public static final int ACTIVITY_TYPE_LINK_MIC     = 4;
    public static final int ACTIVITY_TYPE_REALTIME_PLAY = 5;

    private String TAG = "demo.LiveRoomPull";

    // TODO: 此处添加拉流地址
    private String flvUrl = "";

    private TXCloudVideoView videoView;
    private TXLivePlayer mLivePlayer;
    private TXLivePlayConfig mLivePlayerConfig;

    private Button playBtn, closeBtn;
    private boolean mIsPlaying = false;
    private int mActivityType = ACTIVITY_TYPE_LIVE_PLAY;    //ACTIVITY_TYPE_REALTIME_PLAY
    private int mPlayType = TXLivePlayer.PLAY_TYPE_LIVE_FLV; // player 播放链接类型      PLAY_TYPE_LIVE_RTMP
    private long mStartPlayTS = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tx_live_room_audience);
        init();
    }

    private void init() {
        if (mLivePlayer == null) {
            mLivePlayer = new TXLivePlayer(this);
        }
        videoView = findViewById(R.id.video_view);
        mLivePlayerConfig = new TXLivePlayConfig();

        // TODO： 后期还可加个进度缓冲条（Loading）

        playBtn = findViewById(R.id.btn_play_video);
        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickPlayBtn();
            }
        });

        closeBtn = findViewById(R.id.btn_close_video);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickCloseVideoBtn();
            }
        });

        //startPull();
    }

    private void clickCloseVideoBtn() {
        closeVideo();
    }

    private void closeVideo() {

    }

    private void clickPlayBtn() {
        if (mIsPlaying) {       //正在播放
            stopPlay();
        } else {
            mIsPlaying = startPlay();
        }
        // mIsPlaying = !mIsPlaying;
    }

    private boolean startPlay() {
        if (!checkPlayUrl(flvUrl)) {
            Log.d(TAG, "--访问的播流地址不合法,"+flvUrl);
            return false;
        }

        playBtn.setText("关闭");
        mLivePlayer.setPlayerView(videoView);
        mLivePlayer.setPlayListener(this);

        //一定的设置
        mLivePlayer.setConfig(mLivePlayerConfig);
        int result = mLivePlayer.startPlay(flvUrl, TXLivePlayer.PLAY_TYPE_LIVE_FLV); //传入的视频格式为FLV
        if (result != 0) {
            playBtn.setText("播放");
            return false;
        }

        mStartPlayTS = System.currentTimeMillis();      //获取当前时间，单位ms

        return true;
    }

    private void stopPlay() {
        playBtn.setText("播放");
        // 可加入停止显示加载的进度条
        if (mLivePlayer != null) {
            mLivePlayer.stopRecord();
            mLivePlayer.setPlayListener(null);
            mLivePlayer.stopPlay(true);
        }
        mIsPlaying = false;
        Log.d(TAG, "关闭成功");
    }

    private void startPull() {
        mLivePlayer.startPlay(flvUrl, TXLivePlayer.PLAY_TYPE_LIVE_FLV); //传入的视频格式为FLV
    }


    /*
    * 官方demo的代码，检查url的合法性相关
    * */
    private boolean checkPlayUrl(final String playUrl) {
        if (TextUtils.isEmpty(playUrl) || (!playUrl.startsWith("http://") && !playUrl.startsWith("https://") && !playUrl.startsWith("rtmp://")  && !playUrl.startsWith("/"))) {
            Toast.makeText(getApplicationContext(), "播放地址不合法，直播目前仅支持rtmp,flv播放方式!", Toast.LENGTH_SHORT).show();
            return false;
        }

        switch (mActivityType) {
            case ACTIVITY_TYPE_LIVE_PLAY:
            {
                if (playUrl.startsWith("rtmp://")) {
                    mPlayType = TXLivePlayer.PLAY_TYPE_LIVE_RTMP;
                } else if ((playUrl.startsWith("http://") || playUrl.startsWith("https://"))&& playUrl.contains(".flv")) {
                    mPlayType = TXLivePlayer.PLAY_TYPE_LIVE_FLV;
                } else {
                    Toast.makeText(getApplicationContext(), "播放地址不合法，直播目前仅支持rtmp,flv播放方式!", Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
            break;
            case ACTIVITY_TYPE_REALTIME_PLAY:
            {
                if (!playUrl.startsWith("rtmp://")) {
                    Toast.makeText(getApplicationContext(), "低延时拉流仅支持rtmp播放方式", Toast.LENGTH_SHORT).show();
                    return false;
                } else if (!playUrl.contains("txSecret")) {
                    new AlertDialog.Builder(this)
                            .setTitle("播放出错")
                            .setMessage("低延时拉流地址需要防盗链签名，详情参考 https://cloud.tencent.com/document/product/454/7880#RealTimePlay!")
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Uri uri = Uri.parse("https://cloud.tencent.com/document/product/454/7880#RealTimePlay!");
                            startActivity(new Intent(Intent.ACTION_VIEW,uri));
                            dialog.dismiss();
                        }
                    }).show();
                    return false;
                }

                mPlayType = TXLivePlayer.PLAY_TYPE_LIVE_RTMP_ACC;
                break;
            }
            default:
                Toast.makeText(getApplicationContext(), "播放地址不合法，目前仅支持rtmp,flv播放方式!", Toast.LENGTH_SHORT).show();
                return false;
        }
        return true;
    }

    @Override
    public void onPlayEvent(int event, Bundle param) {
        if (event == TXLiveConstants.PLAY_EVT_PLAY_BEGIN) {
            //stopLoadingAnimation();
            Log.d("AutoMonitor", "PlayFirstRender,cost=" +(System.currentTimeMillis()-mStartPlayTS));
        } else if (event == TXLiveConstants.PLAY_ERR_NET_DISCONNECT || event == TXLiveConstants.PLAY_EVT_PLAY_END) {
            stopPlay();
        } else if (event == TXLiveConstants.PLAY_EVT_PLAY_LOADING){
            //startLoadingAnimation();
        } else if (event == TXLiveConstants.PLAY_EVT_RCV_FIRST_I_FRAME) {
            //stopLoadingAnimation();
        } else if (event == TXLiveConstants.PLAY_EVT_CHANGE_RESOLUTION) {
            Log.d(TAG, "size "+param.getInt(TXLiveConstants.EVT_PARAM1) + "x" + param.getInt(TXLiveConstants.EVT_PARAM2));
        } else if (event == TXLiveConstants.PLAY_EVT_CHANGE_ROTATION) {
            return;
        } else if (event == TXLiveConstants.PLAY_EVT_GET_MESSAGE) {
            if (param != null) {
                byte data[] = param.getByteArray(TXLiveConstants.EVT_GET_MSG);
                String seiMessage = "";
                if (data != null && data.length > 0) {
                    try {
                        seiMessage = new String(data, "UTF-8");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                Toast.makeText(getApplicationContext(), seiMessage, Toast.LENGTH_SHORT).show();
            }
        } /*else if (event == TXLiveConstants.PLAY_EVT_GET_FLVSESSIONKEY) {       //自己引入的SDK无此常量（官方demo里有），暂时注释掉
            //String flvSessionKey = param.getString(TXLiveConstants.EVT_DESCRIPTION, "");
            //Toast.makeText(getApplicationContext(), "event PLAY_EVT_GET_FLVSESSIONKEY: " + flvSessionKey, Toast.LENGTH_SHORT).show();
        }*/

        if (event < 0) {
            Toast.makeText(getApplicationContext(), param.getString(TXLiveConstants.EVT_DESCRIPTION), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onNetStatus(Bundle bundle) {

    }


    @Override
    protected void onStop() {
        super.onStop();
        //mCancelRecordFlag = true;     看官方demo源码未能了解其用处
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mLivePlayer != null) {
            mLivePlayer.stopPlay(true);
            mLivePlayer = null;
        }
        if (videoView != null) {
            videoView.onDestroy();
            videoView = null;
        }
        mLivePlayerConfig = null;
    }

    @Override
    public void onBackPressed() {
        stopPlay();
        super.onBackPressed();
    }
}

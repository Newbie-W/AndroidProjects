package com.demo.demo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.demo.demo.R;

public class MainActivity extends AppCompatActivity {
    private Button testBtn, cameraLiveBtn, screenLiveBtn, liveRoomAudienceBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        testBtn = findViewById(R.id.btn_test);
        cameraLiveBtn = findViewById(R.id.btn_camera_live);
        screenLiveBtn = findViewById(R.id.btn_screen_live);
        liveRoomAudienceBtn = findViewById(R.id.btn_live_room_audience);

        testBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickTestBtn();
            }
        });
        cameraLiveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickCameraLiveBtn();
            }
        });
        screenLiveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickScreenLiveBtn();
            }
        });
        liveRoomAudienceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickLiveRoom();
            }
        });
    }

    private void clickTestBtn() {       //测试
        Intent intent = new Intent(this, TxCameraLiveActivity.class);
        startActivity(intent);
    }

    private void clickCameraLiveBtn() {     //摄像头直播推流
        //Log.d("demo.live", ""+TXLiveBase.getInstance().isInited());
        Intent intent = new Intent(this, TxCameraLiveActivity.class);
        intent.putExtra("UseCamera", true);
        //intent.putExtra(Constants.LIVE_SDK, liveSdk);
        //intent.putExtra(SyncStateContract.Constants.LIVE_KSY_CONFIG, bean);
        //intent.putExtra(SyncStateContract.Constants.HAVE_STORE, haveStore);
        startActivity(intent);


    }

    private void clickScreenLiveBtn() {     //录屏推流
        Intent intent = new Intent(this, TxCameraLiveActivity.class);
        //可写可不写  intent.putExtra("UseCamera", false);
        startActivity(intent);
    }

    private void clickLiveRoom() {      //直播拉流
        Intent intent = new Intent(this, TxLiveRoomAudienceActivity.class);
        startActivity(intent);
    }

}

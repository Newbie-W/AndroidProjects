package com.demo.demo_agora_1.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.demo.demo_agora_1.R;

public class MainActivity extends AppCompatActivity {

    private static String TAG = "hello.MainActivity";

    private Button audienceEntranceBtn, anchorEntranceBtn;
    private EditText liveRoomNameEditText;

    private static final int PERMISSION_REQ_ID = 22;

    // App 运行时确认麦克风和摄像头设备的使用权限。
    private static final String[] REQUESTED_PERMISSIONS = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 获取权限后，初始化 RtcEngine，并加入频道。
        if (checkSelfPermission(REQUESTED_PERMISSIONS[0], PERMISSION_REQ_ID) &&
                checkSelfPermission(REQUESTED_PERMISSIONS[1], PERMISSION_REQ_ID) &&
                checkSelfPermission(REQUESTED_PERMISSIONS[2], PERMISSION_REQ_ID)) {
            //initEngineAndJoinChannel();
            init();
        } else {
            Toast.makeText(this, "权限错误，无法获取必要的权限", Toast.LENGTH_LONG).show();
            Log.d(TAG, "错误"+ checkSelfPermission(REQUESTED_PERMISSIONS[0], PERMISSION_REQ_ID)+ checkSelfPermission(REQUESTED_PERMISSIONS[1], PERMISSION_REQ_ID) + checkSelfPermission(REQUESTED_PERMISSIONS[2], PERMISSION_REQ_ID));
        }
    }

    private boolean checkSelfPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(this, permission) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, requestCode);
            return false;
        }
        return true;
    }

    private void initEngineAndJoinChannel() {
    }

    private void init() {
        liveRoomNameEditText = findViewById(R.id.editText_live_room_id);


        audienceEntranceBtn = findViewById(R.id.btn_audience);
        anchorEntranceBtn = findViewById(R.id.btn_anchor);
        audienceEntranceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                forwardToLiveRoomActivity(2);
            }
        });
        anchorEntranceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                forwardToLiveRoomActivity(1);
            }
        });
    }

    private void forwardToLiveRoomActivity(int isAudience) {  // 1：主播， 2：观众
        String liveRoomId = liveRoomNameEditText.getText().toString();
        Log.d(TAG, "直播间id："+liveRoomId);
        Intent intent = new Intent(MainActivity.this, LiveRoomActivity.class);
        Log.d(TAG, "跳转中... "+ ((isAudience==1)?"主播":"观众"));
        intent.putExtra("liveRoomName", liveRoomId);
        intent.putExtra("isAudience", isAudience);
        startActivity(intent);
    }
}

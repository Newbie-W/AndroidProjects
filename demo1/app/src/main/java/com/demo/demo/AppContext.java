package com.demo.demo;

import android.Manifest;
import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.tencent.live.TXLiveBase;

import java.util.ArrayList;
import java.util.List;


// 全局文件

public class AppContext extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        //腾讯直播相关licence，最好放在Application里
        String ugcLicenceUrl = "";		//licence
        //腾讯云鉴权key
        String ugcKey = "";
        TXLiveBase.getInstance().setLicence(this, ugcLicenceUrl, ugcKey);
        Log.d("demo.live", "-----4ok");
    }

}

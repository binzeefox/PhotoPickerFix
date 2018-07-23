package com.example.tongxiwen.photopickerfix.base;

import android.app.Application;

import com.tencent.bugly.Bugly;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        String appID = "96a25b326f";
        Bugly.init(getApplicationContext(), appID, true);
    }
}

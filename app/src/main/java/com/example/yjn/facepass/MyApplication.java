package com.example.yjn.facepass;

import android.app.Application;

import com.example.yjn.facepass.utils.Utils;


/**
 * Created by yang.jianan on 2017/08/21 15:27.
 */

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Utils.init(this);
    }
}

package com.sfdy.sfdyandroid;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.githang.statusbar.StatusBarCompat;
import com.sfdy.sfdyandroid.utils.APKVersionInfoUtils;
import com.xuexiang.xupdate.XUpdate;

public class SplashActivity extends AppCompatActivity {

    private final String mUpdateUrl = "https://jyavs.com/app/common/check/1/1/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarCompat.setStatusBarColor(this, 0xFF6200EE, false);
        setContentView(R.layout.activity_splash);
//        Intent intent = new Intent(this, MainActivity.class);
//        startActivity(intent);
        int uiMode = getApplicationContext().getResources().getConfiguration().uiMode;
        // 深色模式的值为0x21 - 33
        // 浅色模式的值为0x11 - 17
        System.out.println("uiMode:" + uiMode);
        XUpdate.newBuild(this)
                .updateUrl(mUpdateUrl + APKVersionInfoUtils.getVersionName(this))
                .update();
    }
}
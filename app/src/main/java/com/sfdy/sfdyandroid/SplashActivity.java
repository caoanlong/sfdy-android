package com.sfdy.sfdyandroid;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sfdy.sfdyandroid.model.AppVersion;
import com.sfdy.sfdyandroid.model.ResEntity;
import com.sfdy.sfdyandroid.utils.APKVersionInfoUtils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.lang.reflect.Type;

import okhttp3.Call;

public class SplashActivity extends AppCompatActivity {

    private final String url = "https://jyavs.com/app/common/check/1/";

    private void checkVersion() {
        Intent intent = new Intent(this, MainActivity.class);
        String versionName = APKVersionInfoUtils.getVersionName(this);
        String URL = url + versionName;
        OkHttpUtils.get().url(URL).build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                startActivity(intent);
            }

            @Override
            public void onResponse(String response, int id) {
                Gson gson = new Gson();
                Type type = new TypeToken<ResEntity<AppVersion>>() {
                }.getType();
                ResEntity<AppVersion> res = gson.fromJson(response, type);
                if (res.getCode() == 200) {
                    AppVersion data = res.getData();
                    Boolean update = data.getUpdate();
                    Boolean forceUpdate = data.getForceUpdate();
                    if (update) {
                        // 弹窗
                        new AlertDialog.Builder(SplashActivity.this)
                                .setTitle("版本更新")
                                .setMessage("当前版本:" + versionName)
                                .setPositiveButton("确定", (dialog, which) -> {
                                    dialog.dismiss();
                                    startActivity(intent);
                                })
                                .show();
                    } else {
                        startActivity(intent);
                    }
                } else {
                    startActivity(intent);
                }
            }
        });


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        checkVersion();
    }
}
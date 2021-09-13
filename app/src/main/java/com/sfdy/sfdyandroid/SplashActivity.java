package com.sfdy.sfdyandroid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.widget.Toast;

import com.githang.statusbar.StatusBarCompat;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sfdy.sfdyandroid.model.AppVersion;
import com.sfdy.sfdyandroid.model.ResEntity;
import com.sfdy.sfdyandroid.utils.APKVersionInfoUtils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import okhttp3.Call;

public class SplashActivity extends AppCompatActivity {

    private final String url = "https://jyavs.com/app/common/check/1/";

    private static final int PROGRESS = 100;//进度
    private static final int DOWNLOAD_COMPLETE = 200;//下载完成
    private static final int INSTALL_PERMISS_CODE = 500;//安装权限
    private static final int INSTALL_COMPLETE = 600;

    private ProgressDialog progressDialog;

    File file;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case DOWNLOAD_COMPLETE:
                    progressDialog.dismiss();
                    Toast.makeText(SplashActivity.this, "下载完成，准备安装！", Toast.LENGTH_SHORT).show();
                    installApk();
                    break;
                case PROGRESS:
                    int result = (int) msg.obj;
                    progressDialog.setProgress(result);
                    break;
            }
        }
    };

    private void checkVersion() {
        Intent intent = new Intent(this, MainActivity.class);
        String versionName = APKVersionInfoUtils.getVersionName(this);
        String URL = url + versionName;
        OkHttpUtils.get().url(URL).build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                startActivity(intent);
                overridePendingTransition(0, 0); // 取消Activity跳转默认动画
            }

            @Override
            public void onResponse(String response, int id) {
                Gson gson = new Gson();
                Type type = new TypeToken<ResEntity<AppVersion>>() {
                }.getType();
                ResEntity<AppVersion> res = gson.fromJson(response, type);
                if (res.getCode() == 200) {
                    AppVersion data = res.getData();
                    String appUrl = data.getAppUrl();
                    Boolean update = data.getUpdate();
                    Boolean forceUpdate = data.getForceUpdate();
                    if (update) {
                        // 如果有更新，则初始化下载进度条弹窗
                        progressDialog = new ProgressDialog(SplashActivity.this);
                        progressDialog.setTitle("正在下载...");
                        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                        progressDialog.setMax(100);
                        progressDialog.setProgress(0);
                        // 弹窗
                        AlertDialog.Builder modal = new AlertDialog.Builder(SplashActivity.this)
                                .setTitle("版本更新")
                                .setMessage("当前版本:" + versionName);

                        if (forceUpdate) {
                            modal.setPositiveButton("确定", (dialog, which) -> {
                                downFile(appUrl);
                            }).show();
                        } else {
                            modal.setPositiveButton("确定", (dialog, which) -> {
                                downFile(appUrl);
                            }).setNegativeButton("取消", (dialog, which) -> {
                                dialog.dismiss();
                                startActivity(intent);
                                overridePendingTransition(0, 0);
                            }).show();
                        }

                    } else {
                        startActivity(intent);
                        overridePendingTransition(0, 0);
                    }
                } else {
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                }
            }
        });
    }


    // 接收到安装完成apk的广播
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            Toast.makeText(context, "安装完成！", Toast.LENGTH_SHORT).show();

            Message message = handler.obtainMessage();
            message.what = INSTALL_COMPLETE;
            handler.sendMessage(message);
        }
    };

    /**
     * 后台在下面一个Apk 下载完成后返回下载好的文件
     * @param httpUrl
     * @return
     */
    private File downFile(String httpUrl) {
        progressDialog.show();
        new Thread(() -> {
            try {
                URL url = new URL(httpUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                FileOutputStream fileOutputStream = null;
                InputStream inputStream;
                if (connection.getResponseCode() == 200) {
                    inputStream = connection.getInputStream();
                    if (inputStream != null) {
                        int appLength = connection.getContentLength();
                        file = getFile(httpUrl);
                        fileOutputStream = new FileOutputStream(file);
                        byte[] buffer = new byte[1024];
                        int length = 0, total = 0;
                        while ((length = inputStream.read(buffer)) != -1) {
                            // 写入文件中
                            fileOutputStream.write(buffer, 0, length);
                            // 统计进度
                            total += length;
                            Message message = handler.obtainMessage();
                            message.what = PROGRESS;
                            message.obj = (int) (total * 1.0 / appLength * 100);
                            handler.sendMessage(message);
                        }
                        fileOutputStream.close();
                        fileOutputStream.flush();
                    }
                    inputStream.close();
                }
                //下载完成,开始安装
                Message message = handler.obtainMessage();
                message.what = DOWNLOAD_COMPLETE;
                handler.sendMessage(message);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
        return file;
    }

    /**
     * 根据传过来url创建文件
     */
    private File getFile(String url) {
        // 使用缓存目录,这个时候不需要申请存储权限
        // 目录不存在，那么创建
        File dir = new File(getExternalCacheDir(),"download");
        if (!dir.exists()){
            dir.mkdir();
        }
        // 创建文件
        File file = new File(dir, getFilePath(url));
        return file;
    }

    /**
     * 截取出url后面的apk的文件名
     * @param url
     * @return
     */
    private String getFilePath(String url) {
        return url.substring(url.lastIndexOf("/"), url.length());
    }

    /**
     * 安装APK
     */
    private void installApk() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        if (file != null && file.exists()){
            // 兼容7.0
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                Uri contentUri = FileProvider.getUriForFile(this, this.getPackageName() + ".fileProvider", file);
                intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
                //兼容8.0
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    boolean hasInstallPermission = this.getPackageManager().canRequestPackageInstalls();
                    if (!hasInstallPermission) {
                        startInstallPermissionSettingActivity();
                        return;
                    }
                }
            } else {
                // <7.0
                intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            // activity任务栈中Activity的个数>0
            if (this.getPackageManager().queryIntentActivities(intent, 0).size() > 0) {
                this.startActivity(intent);
            }
        }
    }

    private void startInstallPermissionSettingActivity() {
        //注意这个是8.0新API
        Uri packageURI = Uri.parse("package:"+getPackageName());
        Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,packageURI);
        startActivityForResult(intent, INSTALL_PERMISS_CODE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarCompat.setStatusBarColor(this, 0xFF6200EE, false);
        setContentView(R.layout.activity_splash);
        checkVersion();
//        Intent intent = new Intent(this, MainActivity.class);
//        startActivity(intent);
        int uiMode = getApplicationContext().getResources().getConfiguration().uiMode;
        // 深色模式的值为0x21 - 33
        // 浅色模式的值为0x11 - 17
        System.out.println("uiMode:" + uiMode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 授权完成
        if (resultCode == RESULT_OK && requestCode == INSTALL_PERMISS_CODE) {
            Toast.makeText(this,"安装应用",Toast.LENGTH_SHORT).show();
            installApk();
        } else {
            Toast.makeText(this,"授权失败，无法安装应用",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        intentFilter.addDataScheme("package");
        // 注册一个广播
        registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 解除广播
        unregisterReceiver(broadcastReceiver);
    }
}
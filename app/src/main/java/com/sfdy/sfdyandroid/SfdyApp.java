package com.sfdy.sfdyandroid;

import android.app.Application;
import android.widget.Toast;

import com.sfdy.sfdyandroid.utils.APKVersionInfoUtils;
import com.sfdy.sfdyandroid.utils.CustomUpdateParser;
import com.sfdy.sfdyandroid.utils.NoFileEncryptor;
import com.sfdy.sfdyandroid.utils.OKHttpUpdateHttpService;
import com.xuexiang.xupdate.XUpdate;

import static com.xuexiang.xupdate.entity.UpdateError.ERROR.CHECK_NO_NEW_VERSION;

public class SfdyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Set listening for version update errors
        XUpdate.get()
                .debug(true)
                .isWifiOnly(true)                                               // By default, only version updates are checked under WiFi
                .isGet(true)                                                    // The default setting uses Get request to check versions
                .isAutoMode(false)                                              // The default setting is non automatic mode
                .param("versionCode", APKVersionInfoUtils.getVersionCode(this))         // Set default public request parameters
                .param("appKey", getPackageName())
                .setOnUpdateFailureListener(error -> {
                    if (error.getCode() != CHECK_NO_NEW_VERSION) {          // Handling different errors
                        Toast.makeText(SfdyApp.this, error.toString(), Toast.LENGTH_SHORT).show();
                    }
                })
                .supportSilentInstall(false)                                     // Set whether silent installation is supported. The default is true
                .setIUpdateHttpService(new OKHttpUpdateHttpService())           // This must be set! Realize the network request function.
                .setIUpdateParser(new CustomUpdateParser())
                .setIFileEncryptor(new NoFileEncryptor())
                .init(this);                                                    // This must be initialized

    }
}

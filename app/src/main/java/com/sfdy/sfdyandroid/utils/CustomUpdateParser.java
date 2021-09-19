package com.sfdy.sfdyandroid.utils;

import com.google.gson.Gson;
import com.xuexiang.xupdate.entity.UpdateEntity;
import com.xuexiang.xupdate.listener.IUpdateParseCallback;
import com.xuexiang.xupdate.proxy.IUpdateParser;

import lombok.Data;

public class CustomUpdateParser implements IUpdateParser {
    @Override
    public UpdateEntity parseJson(String json) {
        Gson gson = new Gson();
        ResBean resBean = gson.fromJson(json, ResBean.class);
        ResData data = resBean.getData();
        return new UpdateEntity()
                .setHasUpdate(data.getUpdate())
                .setForce(data.getForceUpdate())
                .setVersionCode(data.getVersionCode())
                .setVersionName(data.getVersionName())
                .setUpdateContent(data.getDecription())
                .setDownloadUrl(data.getAppUrl())
                .setSize(data.getSize());
    }

    @Override
    public void parseJson(String json, IUpdateParseCallback callback) throws Exception {

    }

    @Override
    public boolean isAsyncParser() {
        return false;
    }

    @Data
    private class ResBean {
        private Integer code;
        private String message;
        private ResData data;
    }

    @Data
    private class ResData {
        private Boolean forceUpdate;
        private Boolean update;
        private Integer versionCode;
        private String versionName;
        private String decription;
        private String appUrl;
        private Long size;
    }
}

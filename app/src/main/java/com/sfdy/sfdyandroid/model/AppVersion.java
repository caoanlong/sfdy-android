package com.sfdy.sfdyandroid.model;


import lombok.Data;

@Data
public class AppVersion {
    private Boolean forceUpdate;
    private Boolean update;
    private String appUrl;
    private String decription;
}

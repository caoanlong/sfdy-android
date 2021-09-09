package com.sfdy.sfdyandroid.model;

import lombok.Data;

@Data
public class ResEntity<T> {
    private Short code;
    private String message;
    private T data;
}

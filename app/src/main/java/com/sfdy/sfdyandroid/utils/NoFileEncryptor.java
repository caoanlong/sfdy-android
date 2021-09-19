package com.sfdy.sfdyandroid.utils;

import com.xuexiang.xupdate.proxy.IFileEncryptor;

import java.io.File;

public class NoFileEncryptor implements IFileEncryptor {

    @Override
    public String encryptFile(File file) {
        return "";
    }

    @Override
    public boolean isFileValid(String encrypt, File file) {
        return true;
    }
}

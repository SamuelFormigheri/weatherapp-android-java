package com.example.weatherapp;

import android.app.Application;
import android.graphics.Bitmap;

public class MyApplication extends Application {

    private Bitmap userImg = null;

    public Bitmap getUserImg() {
        return userImg;
    }

    public void setUserImg(Bitmap userImg) {
        this.userImg = userImg;
    }
}
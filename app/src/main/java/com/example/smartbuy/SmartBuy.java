package com.example.smartbuy;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

public class SmartBuy extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}

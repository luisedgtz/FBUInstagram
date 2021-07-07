package com.example.instagram;

import android.app.Application;

import com.parse.Parse;

public class ParseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("taaLJCNenEOZ8ldLyrUG01uxxgE76OT4peGvro6P")
                .clientKey("Uau8G12QPTs0ut1mzSJA2qBVkDCgRiqbQzKkjZA8")
                .server("https://parseapi.back4app.com")
                .build()
        );
    }
}

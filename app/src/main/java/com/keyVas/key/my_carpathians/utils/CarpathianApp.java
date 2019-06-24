package com.keyVas.key.my_carpathians.utils;

import android.app.Application;
import android.content.Context;
import androidx.multidex.MultiDex;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;

import io.fabric.sdk.android.Fabric;

/**
 * Created by Key on 25.06.2017.
 */

public class CarpathianApp extends Application {
    private FirebaseDatabase fb;
    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        if (!FirebaseApp.getApps(this).isEmpty())
        {
            fb =FirebaseDatabase.getInstance();
            fb.setPersistenceEnabled(true);
            fb.getReference().keepSynced(true);
        }

    }
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base, LocaleHelper.checkCurrentLocale()));
        MultiDex.install(this);
    }
}

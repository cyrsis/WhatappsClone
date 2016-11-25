package com.sourcecanyon.whatsClone.app;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.StrictMode;
import android.support.multidex.MultiDex;

import com.sourcecanyon.whatsClone.helpers.PreferenceManager;
import com.sourcecanyon.whatsClone.services.MainService;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by Abderrahim El imame on 20/02/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class WhatsCloneApplication extends Application {
    private static Context AppContext;


    @Override
    public void onCreate() {
        super.onCreate();

        MultiDex.install(this);
        AppContext = getApplicationContext();
        Realm.setDefaultConfiguration(
                new RealmConfiguration.Builder(this)
                        .name(AppConstants.DATABASE_LOCAL_NAME)
                        .deleteRealmIfMigrationNeeded()
                        .build());
        if (PreferenceManager.getToken(this) != null) {
            startService(new Intent(this, MainService.class));
        }



    }


    public static Context getAppContext() {
        return AppContext;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }


    @Override
    public void onTerminate() {
        super.onTerminate();
        MainService.disconnectSocket();
    }


}

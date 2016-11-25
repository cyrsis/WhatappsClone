package com.sourcecanyon.whatsClone.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.sourcecanyon.whatsClone.helpers.AppHelper;

/**
 * Created by Abderrahim El imame on 01/03/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class SyncService extends Service {

    private static final Object sSyncAdapterLock = new Object();
    private static SyncAdapter mSyncAdapter = null;

    @Override
    public void onCreate() {
        AppHelper.LogCat("Sync Service created.");
        synchronized (sSyncAdapterLock) {
            if (mSyncAdapter == null) {
                mSyncAdapter = new SyncAdapter(getApplicationContext(),
                        true);
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        AppHelper.LogCat("Sync Service binded.");
        return mSyncAdapter.getSyncAdapterBinder();
    }
}
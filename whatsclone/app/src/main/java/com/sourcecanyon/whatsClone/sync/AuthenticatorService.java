package com.sourcecanyon.whatsClone.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by Abderrahim El imame on 01/03/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class AuthenticatorService extends Service {

    private Authenticator mAuthenticator;
    // Account type and auth token type
    public static final String ACCOUNT_TYPE = "com.novasera.whatsclone";

    public AuthenticatorService() {

    }


    @Override
    public void onCreate() {
        // Create a new authenticator object
        mAuthenticator = new Authenticator(this);

    }

    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }
}


package com.sourcecanyon.whatsClone.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Date;

/**
 * Created by Abderrahim El imame on 20/02/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class UpdateSettings {
    private static final String LAST_CONTACTS_UPDATE = "lastContactsUpdate";
    private final SharedPreferences preferences;

    public UpdateSettings(Context context) {
        this.preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    /**
     * Saves the current timestamp as last contacts update.
     */
    public void setLastContactsUpdate() {
        long time = new Date().getTime();
        preferences.edit()
                .putLong(LAST_CONTACTS_UPDATE, time)
                .apply();
    }

    /**
     * Returns the timestamp of the last contacts update.
     *
     * @return Long
     */
    public Long getLastContactsUpdate() {
        return preferences.getLong(LAST_CONTACTS_UPDATE, 0);
    }


}

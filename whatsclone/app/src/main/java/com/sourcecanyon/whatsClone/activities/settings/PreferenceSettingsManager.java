package com.sourcecanyon.whatsClone.activities.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.PreferenceManager;

import com.sourcecanyon.whatsClone.R;

/**
 * Created by Abderrahim El imame on 8/17/16.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/bencherif_el
 */

public class PreferenceSettingsManager {

    private static final String default_wallpaper = "#E6E6E6";
    private static final float default_message_font_size = 14.0F;
    private static final Uri default_message_notifications_settings_tone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    private static final String default_message_notifications_settings_light = "#03A9F4";
    private static final Uri default_message_group_notifications_settings_tone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    private static final String default_message_group_notifications_settings_light = "#03A9F4";


    private static SharedPreferences preferenceSettingsManager(Context mContext) {
        return PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    /**
     * chats settings methods
     */

    /**
     * method for  enter send key
     *
     * @param mContext this is parameter for enter_send  method
     * @return this is what method will return
     */
    public static boolean enter_send(Context mContext) {
        return preferenceSettingsManager(mContext).getBoolean(mContext.getString(R.string.key_enter_send), false);
    }


    /**
     * method to change message font size
     *
     * @param mContext this is parameter for enter_send  method
     * @return this is what method will return
     */
    public static float getMessage_font_size(Context mContext) {
        return Float.parseFloat(preferenceSettingsManager(mContext).getString(mContext.getString(R.string.key_message_font_size), "" + default_message_font_size));
    }

    /**
     * method to change the wallpaper
     *
     * @param mContext this is parameter for enter_send  method
     * @return this is what method will return
     */
    public static String getDefault_wallpaper(Context mContext) {
        return (preferenceSettingsManager(mContext).getString(mContext.getString(R.string.key_wallpaper_message), "" + default_wallpaper));
    }


    /**
     * notifications settings methods
     */

    /**
     * method to change if conversation sounds on/off
     *
     * @param mContext this is parameter for enter_send  method
     * @return this is what method will return
     */
    public static boolean conversation_tones(Context mContext) {
        return preferenceSettingsManager(mContext).getBoolean(mContext.getString(R.string.key_conversations_tones), true);
    }

    /**
     * method to change user message notifications tone
     *
     * @param mContext this is parameter for enter_send  method
     * @return this is what method will return
     */
    public static Uri getDefault_message_notifications_settings_tone(Context mContext) {
        return Uri.parse(preferenceSettingsManager(mContext).getString(mContext.getString(R.string.key_message_notifications_settings_tone), "" + default_message_notifications_settings_tone));
    }

    /**
     * method to change vibrate mode
     *
     * @param mContext this is parameter for enter_send  method
     * @return this is what method will return
     */
    public static boolean getDefault_message_notifications_settings_vibrate(Context mContext) {
        return preferenceSettingsManager(mContext).getBoolean(mContext.getString(R.string.key_message_notifications_settings_vibrate), true);
    }


    /**
     * method to change light mode
     *
     * @param mContext this is parameter for enter_send  method
     * @return this is what method will return
     */
    public static String getDefault_message_notifications_settings_light(Context mContext) {
        return preferenceSettingsManager(mContext).getString(mContext.getString(R.string.key_message_notifications_settings_light), "" + default_message_notifications_settings_light);
    }

    /**
     * method to change group message notifications tone
     *
     * @param mContext this is parameter for enter_send  method
     * @return this is what method will return
     */
    public static Uri getDefault_message_group_notifications_settings_tone(Context mContext) {
        return Uri.parse(preferenceSettingsManager(mContext).getString(mContext.getString(R.string.key_message_group_notifications_settings_tone), "" + default_message_group_notifications_settings_tone));
    }

    /**
     * method to change vibrate mode for group
     *
     * @param mContext this is parameter for enter_send  method
     * @return this is what method will return
     */
    public static boolean getDefault_message_group_notifications_settings_vibrate(Context mContext) {
        return preferenceSettingsManager(mContext).getBoolean(mContext.getString(R.string.key_message_group_notifications_settings_vibrate), true);
    }


    /**
     * method to change light mode for group
     *
     * @param mContext this is parameter for enter_send  method
     * @return this is what method will return
     */
    public static String getDefault_message_group_notifications_settings_light(Context mContext) {
        return (preferenceSettingsManager(mContext).getString(mContext.getString(R.string.key_message_group_notifications_settings_light), "" + default_message_group_notifications_settings_light));
    }
}

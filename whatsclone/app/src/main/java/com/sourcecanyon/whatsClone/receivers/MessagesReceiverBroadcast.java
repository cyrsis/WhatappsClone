package com.sourcecanyon.whatsClone.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Abderrahim El imame on 29/01/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public abstract class MessagesReceiverBroadcast extends BroadcastReceiver {
    @Override
    public void onReceive(Context mContext, Intent intent) {
        MessageReceived(mContext, intent);
    }
    protected abstract void MessageReceived(Context context, Intent intent);
}

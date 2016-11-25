package com.sourcecanyon.whatsClone.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import com.sourcecanyon.whatsClone.app.AppConstants;
import com.sourcecanyon.whatsClone.helpers.AppHelper;
import com.sourcecanyon.whatsClone.services.SMSVerificationService;


/**
 * Created by Abderrahim El imame on 23/02/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class SmsReceiverBroadCast extends BroadcastReceiver {
    private static final String TAG = SmsReceiverBroadCast.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {

        final Bundle bundle = intent.getExtras();
        try {
            if (bundle != null) {
                Object[] pdusObj = (Object[]) bundle.get("pdus");
                for (Object aPdusObj : pdusObj) {
                    SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) aPdusObj);
                    String senderAddress = currentMessage.getDisplayOriginatingAddress();
                    String message = currentMessage.getDisplayMessageBody();
                    // if the SMS is not from our gateway, ignore the message
                    if (!senderAddress.toLowerCase().contains(AppConstants.SMS_SENDER_NAME.toLowerCase())) {
                        return;
                    }

                    // verification code from sms
                    String verificationCode = getVerificationCode(message);
                    AppHelper.LogCat("code received SmsReceiverBroadCast : " + verificationCode);

                    Intent mIntent = new Intent(context, SMSVerificationService.class);
                    mIntent.putExtra("code", verificationCode);
                    context.startService(mIntent);

                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e.getMessage());
        }
    }

    /**
     * Getting the Code from sms message body
     * ':' is the separator of OTP from the message
     *
     * @param message this is parameter for  getVerificationCodemethod
     * @return return value
     */
    private String getVerificationCode(String message) {
        String code = null;
        int index = message.indexOf(AppConstants.CODE_DELIMITER);

        if (index != -1) {
            int start = index + 2;
            int length = 6;
            code = message.substring(start, start + length);
            return code;
        }

        return code;
    }
}
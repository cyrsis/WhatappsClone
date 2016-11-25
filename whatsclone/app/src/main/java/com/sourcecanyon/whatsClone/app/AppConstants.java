package com.sourcecanyon.whatsClone.app;

import com.sourcecanyon.whatsClone.R;

/**
 * Created by Abderrahim on 09/02/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class AppConstants {

    /* debugging constants  for developer */
    public static final String TAG = "benCherif";
    public static final boolean DEBUGGING_MODE = false;


    public static final int MESSAGE_COLOR_WARNING = R.color.colorOrangeLight;
    public static final int MESSAGE_COLOR_SUCCESS = R.color.colorGreenDark;
    public static final int TEXT_COLOR = R.color.colorWhite;
    public static final String DATABASE_LOCAL_NAME = "WhatsClone.realm";
    public static final String INVITE_MESSAGE_SMS = "Hello checkout the WhatsClone application";


    // SMS provider identification
    // It should match with your SMS gateway origin
    // You can use  WatsClone, TESTER  as sender ID
    // If you want custom sender Id, approve MSG91 to get one
    public static final String SMS_SENDER_NAME = "WHTSCLO";

    // special character to prefix the code. Make sure this character appears only once in the sms
    public static final String CODE_DELIMITER = ":";


    /**
     * upload image or video constants
     */
    public static final int UPLOAD_PICTURE_REQUEST_CODE = 0x001;
    public static final int UPLOAD_VIDEO_REQUEST_CODE = 0x002;
    public static final int UPLOAD_AUDIO_REQUEST_CODE = 0x003;
    public static final int UPLOAD_DOCUMENT_REQUEST_CODE = 0x004;
    public static final int SELECT_PROFILE_PICTURE = 0x005;
    public static final int SELECT_PROFILE_CAMERA = 0x006;
    public static final int SELECT_MESSAGES_CAMERA = 0x007;
    public static final int SELECT_MESSAGES_RECORD_VIDEO = 0x008;
    public static final int PERMISSION_REQUEST_CODE = 0x009;

    /**
     * Chat socket constants (be careful if u want to change them !!)
     */

    //user socket constants:
    public static final int STATUS_USER_TYPING = 0x010;
    public static final int STATUS_USER_STOP_TYPING = 0x011;
    public static final int STATUS_USER_CONNECTED = 0x012;
    public static final int STATUS_USER_DISCONNECTED = 0x013;
    public static final int STATUS_USER_LAST_SEEN = 0x014;


    //single user socket constants:
    public static final String SOCKET_IS_MESSAGE_SENT = "send_message";
    public static final String SOCKET_IS_MESSAGE_DELIVERED = "delivered";
    public static final String SOCKET_IS_MESSAGE_SEEN = "seen";
    public static final String SOCKET_NEW_MESSAGE = "new_message";
    public static final String SOCKET_SAVE_NEW_MESSAGE = "save_new_message";
    public static final String SOCKET_USER_PING = "user_ping";
    public static final String SOCKET_IS_TYPING = "typing";
    public static final String SOCKET_IS_STOP_TYPING = "stop_typing";
    public static final String SOCKET_IS_ONLINE = "is_online";
    public static final String SOCKET_IS_LAST_SEEN = "last_seen";
    public static final String SOCKET_CONNECTED = "user_connect";
    public static final String SOCKET_DISCONNECTED = "user_disconnect";
    //group socket constants:
    public static final String SOCKET_SAVE_NEW_MESSAGE_GROUP = "save_group_message";
    public static final String SOCKET_NEW_MESSAGE_GROUP = "new_group_message";
    public static final String SOCKET_USER_PING_GROUP = "user_ping_group";
    public static final String SOCKET_USER_PINGED_GROUP = "user_pinged_group";
    public static final String SOCKET_IS_MESSAGE_GROUP_SENT = "send_group_message";
    public static final String SOCKET_IS_MESSAGE_GROUP_DELIVERED = "group_delivered";
    public static final String SOCKET_IS_MESSAGE_GROUP_SENTT = "group_sent";
    public static final String SOCKET_IS_MEMBER_TYPING = "member_typing";
    public static final String SOCKET_IS_MEMBER_STOP_TYPING = "member_stop_typing";


    /**
     * Status constants
     */

    public static final int IS_WAITING = 0;
    public static final int IS_SENT = 1;
    public static final int IS_DELIVERED = 2;
    public static final int IS_SEEN = 3;

}

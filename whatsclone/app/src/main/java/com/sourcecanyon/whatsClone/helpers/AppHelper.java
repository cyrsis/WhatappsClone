package com.sourcecanyon.whatsClone.helpers;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sourcecanyon.whatsClone.R;
import com.sourcecanyon.whatsClone.app.AppConstants;
import com.sourcecanyon.whatsClone.models.messages.MessagesModel;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import io.realm.Realm;

import static android.content.Context.ACTIVITY_SERVICE;

/**
 * Created by Abderrahim on 09/02/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class AppHelper {

    private static ProgressDialog mDialog;


    /**
     * method to show the progress dialog
     *
     * @param mContext this is parameter for showDialog method
     */
    public static void showDialog(Context mContext, String message) {
        mDialog = new ProgressDialog(mContext);
        mDialog.setMessage(message);
        mDialog.setIndeterminate(true);
        mDialog.setCancelable(true);
        mDialog.setCancelable(true);
        mDialog.show();
    }

    /**
     * method to hide the progress dialog
     */
    public static void hideDialog() {
        if (mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }

    /**
     * method for get a custom CustomToast
     *
     * @param mContext this is the first parameter for CustomToast  method
     * @param Message  this is the second parameter for CustomToast  method
     */
    public static void CustomToast(Context mContext, String Message) {
        LinearLayout CustomToastLayout = new LinearLayout(mContext);
        CustomToastLayout.setBackgroundResource(R.drawable.bg_custom_toast);
        CustomToastLayout.setGravity(Gravity.TOP);
        TextView message = new TextView(mContext);
        message.setTextColor(Color.WHITE);
        message.setTextSize(13);
        message.setPadding(20, 20, 20, 20);
        message.setGravity(Gravity.CENTER);
        message.setText(Message);
        CustomToastLayout.addView(message);
        Toast toast = new Toast(mContext);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(CustomToastLayout);
        toast.setGravity(Gravity.CENTER, 0, 50);
        toast.show();
    }

    /**
     * method to check if android version is lollipop
     *
     * @return this return value
     */
    public static boolean isAndroid5() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    /**
     * method to get color
     *
     * @param context this is the first parameter for getColor  method
     * @param id      this is the second parameter for getColor  method
     * @return return value
     */
    public static int getColor(Context context, int id) {
        if (isAndroid5()) {
            return ContextCompat.getColor(context, id);
        } else {
            return context.getResources().getColor(id);
        }
    }

    /**
     * method to get drawable
     *
     * @param context this is the first parameter for getDrawable  method
     * @param id      this is the second parameter for getDrawable  method
     * @return return value
     */
    public static Drawable getDrawable(Context context, int id) {
        if (isAndroid5()) {
            return ContextCompat.getDrawable(context, id);
        } else {
            return context.getResources().getDrawable(id);
        }
    }

    /**
     * shake EditText error
     *
     * @param mContext this is the first parameter for showErrorEditText  method
     * @param editText this is the second parameter for showErrorEditText  method
     */
    private void showErrorEditText(Context mContext, EditText editText) {
        Animation shake = AnimationUtils.loadAnimation(mContext, R.anim.shake);
        editText.startAnimation(shake);
    }

    /**
     * method for LogCat
     *
     * @param Message this is  parameter for LogCat  method
     */
    public static void LogCat(String Message) {
        if (AppConstants.DEBUGGING_MODE)
            Log.e(AppConstants.TAG, Message);
    }

    /**
     * method for Log cat Throwable
     *
     * @param Message this is  parameter for LogCatThrowable  method
     */
    public static void LogCat(Throwable Message) {
        if (AppConstants.DEBUGGING_MODE)
            Log.e(AppConstants.TAG, " Throwable " + Message.getMessage());
    }

    /**
     * method to export realm database
     *
     * @param mContext this is parameter for CustomToast  method
     */
    private void ExportRealmDatabase(Context mContext) {

        // init realm
        Realm realm = Realm.getDefaultInstance();

        File exportRealmFile = null;
        try {
            // get or create an "whatsClone.realm" file
            exportRealmFile = new File(mContext.getExternalCacheDir(), "whatsClone.realm");

            // if "whatsClone.realm" already exists, delete
            exportRealmFile.delete();

            // copy current realm to "export.realm"
            realm.writeCopyTo(exportRealmFile);

        } catch (IOException e) {
            e.printStackTrace();
        }
        realm.close();

        // init email intent and add export.realm as attachment
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("plain/text");
        intent.putExtra(Intent.EXTRA_EMAIL, "abderrahim.elimame@gmail.com");
        intent.putExtra(Intent.EXTRA_SUBJECT, "this is ur local realm database whatsClone");
        intent.putExtra(Intent.EXTRA_TEXT, "Hi man");
        Uri u = Uri.fromFile(exportRealmFile);
        intent.putExtra(Intent.EXTRA_STREAM, u);

        // start email intent
        mContext.startActivity(Intent.createChooser(intent, "Choose an application"));
    }


    /**
     * method to check if there is a connection
     *
     * @param context this is  parameter for isNetworkAvailable  method
     * @return return value
     */
    public static boolean isNetworkAvailable(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            if (netInfo != null && netInfo.getState() == NetworkInfo.State.CONNECTED) {
                return true;
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    /**
     * method to load json files from asset directory
     *
     * @param mContext this is  parameter for loadJSONFromAsset  method
     * @return return value
     */
    public static String loadJSONFromAsset(Context mContext) {
        String json = null;
        try {
            InputStream is = mContext.getAssets().open("country_phones.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    /**
     * method to launch the activities
     *
     * @param mContext  this is the first parameter for LaunchActivity  method
     * @param mActivity this is the second parameter for LaunchActivity  method
     */
    public static void LaunchActivity(Activity mContext, Class mActivity) {
        Intent mIntent = new Intent(mContext, mActivity);
        mContext.startActivity(mIntent);
        mContext.overridePendingTransition(R.anim.push_down_in, R.anim.push_down_out);
    }

    /**
     * method to convert dp  to pixel
     *
     * @param dp this is  parameter for dpToPx  method
     * @return return value
     */
    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    /**
     * method to convert pixel to dp
     *
     * @param px this is  parameter for pxToDp  method
     * @return return value
     */
    public static int pxToDp(int px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }

    /**
     * method to show snack bar
     *
     * @param mContext    this is the first parameter for Snackbar  method
     * @param view        this is the second parameter for Snackbar  method
     * @param Message     this is the thirded parameter for Snackbar  method
     * @param colorId     this is the fourth parameter for Snackbar  method
     * @param TextColorId this is the fifth parameter for Snackbar  method
     */
    public static void Snackbar(Context mContext, View view, String Message, int colorId, int TextColorId) {
        Snackbar snackbar = Snackbar.make(view, Message, Snackbar.LENGTH_LONG);
        View snackView = snackbar.getView();
        snackView.setBackgroundColor(ContextCompat.getColor(mContext, colorId));
        TextView snackbarTextView = (TextView) snackView.findViewById(android.support.design.R.id.snackbar_text);
        snackbarTextView.setTextColor(ContextCompat.getColor(mContext, TextColorId));
        snackbar.show();
    }

    /**
     * method to check if activity is running or not
     *
     * @param mContext     this is the first parameter for isActivityRunning  method
     * @param activityName this is the second parameter for isActivityRunning  method
     * @return return value
     */
    public static boolean isActivityRunning(Context mContext, String activityName) {
        ActivityManager activityManager = (ActivityManager) mContext.getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = activityManager.getRunningTasks(3);
        for (ActivityManager.RunningTaskInfo task : tasks) {
            if ((mContext.getPackageName() + "." + activityName).equals(task.topActivity.getClassName())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Check permissions methods for Android M
     */

    /**
     * method to check for permissions
     *
     * @param activity   this is the first parameter for checkPermission  method
     * @param permission this is the second parameter for checkPermission  method
     * @return return value
     */
    public static boolean checkPermission(Activity activity, String permission) {
        int result = ContextCompat.checkSelfPermission(activity, permission);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * method to request permissions
     *
     * @param mActivity  this is the first parameter for requestPermission  method
     * @param permission this is the second parameter for requestPermission  method
     */
    public static void requestPermission(Activity mActivity, String permission) {

        if (ActivityCompat.shouldShowRequestPermissionRationale(mActivity, permission)) {
            String title = null;
            String Message = null;
            switch (permission) {
                case Manifest.permission.CAMERA:
                    title = mActivity.getString(R.string.camera_permission);
                    Message = mActivity.getString(R.string.camera_permission_message);
                    break;
                case Manifest.permission.RECORD_AUDIO:
                    title = mActivity.getString(R.string.audio_permission);
                    Message = mActivity.getString(R.string.record_audio_permission_message);
                    break;

                case Manifest.permission.MODIFY_AUDIO_SETTINGS:
                    title = mActivity.getString(R.string.camera_permission);
                    Message = mActivity.getString(R.string.settings_audio_permission_message);
                    break;
                case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                    title = mActivity.getString(R.string.storage_permission);
                    Message = mActivity.getString(R.string.write_storage_permission_message);
                    break;
                case Manifest.permission.READ_EXTERNAL_STORAGE:
                    title = mActivity.getString(R.string.storage_permission);
                    Message = mActivity.getString(R.string.read_storage_permission_message);
                    break;
                case Manifest.permission.READ_CONTACTS:
                    title = mActivity.getString(R.string.contacts_permission);
                    Message = mActivity.getString(R.string.read_contacts_permission_message);
                    break;
                case Manifest.permission.WRITE_CONTACTS:
                    title = mActivity.getString(R.string.contacts_permission);
                    Message = mActivity.getString(R.string.write_contacts_permission_message);
                    break;
                case Manifest.permission.VIBRATE:
                    title = mActivity.getString(R.string.vibrate_permission);
                    Message = mActivity.getString(R.string.vibrate_permission_message);
                    break;
                case Manifest.permission.RECEIVE_SMS:
                    title = mActivity.getString(R.string.receive_sms_permission);
                    Message = mActivity.getString(R.string.receive_sms_permission_message);
                    break;

                case Manifest.permission.READ_SMS:
                    title = mActivity.getString(R.string.read_sms_permission);
                    Message = mActivity.getString(R.string.read_sms_permission_message);
                    break;
                case Manifest.permission.CALL_PHONE:
                    title = mActivity.getString(R.string.call_phone_permission);
                    Message = mActivity.getString(R.string.call_phone_permission_message);
                    break;
                case Manifest.permission.GET_ACCOUNTS:
                    title = mActivity.getString(R.string.get_accounts_permission);
                    Message = mActivity.getString(R.string.get_accounts_permission_message);
                    break;
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
            builder.setTitle(title);
            builder.setMessage(Message);
            builder.setPositiveButton(mActivity.getString(R.string.yes), (dialog, which) -> {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", mActivity.getPackageName(), null);
                intent.setData(uri);
                mActivity.startActivityForResult(intent, AppConstants.PERMISSION_REQUEST_CODE);
            });
            builder.setNegativeButton(R.string.no_thanks, (dialog, which) -> dialog.dismiss());
            builder.show();
        } else {

            ActivityCompat.requestPermissions(mActivity, new String[]{permission}, AppConstants.PERMISSION_REQUEST_CODE);
        }
    }

    /**
     * method to copy text
     *
     * @param context       this is the first parameter for copyText  method
     * @param messagesModel this is the second parameter for copyText  method
     * @return return value
     */
    public static boolean copyText(Context context, MessagesModel messagesModel) {
        int sdk = Build.VERSION.SDK_INT;
        if (sdk < Build.VERSION_CODES.HONEYCOMB) {
            android.text.ClipboardManager clipboard = (android.text.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setText(messagesModel.getMessage());
        } else {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText(context.getString(R.string.message_copy), messagesModel.getMessage());
            clipboard.setPrimaryClip(clip);
        }
        return true;
    }
}

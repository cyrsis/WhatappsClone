package com.sourcecanyon.whatsClone.helpers.Files;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.webkit.MimeTypeMap;

import com.sourcecanyon.whatsClone.R;
import com.sourcecanyon.whatsClone.api.APIService;
import com.sourcecanyon.whatsClone.api.FilesDownloadService;
import com.sourcecanyon.whatsClone.app.EndPoints;
import com.sourcecanyon.whatsClone.app.WhatsCloneApplication;
import com.sourcecanyon.whatsClone.helpers.AppHelper;
import com.sourcecanyon.whatsClone.helpers.PreferenceManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.realm.internal.IOException;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Abderrahim El imame on 6/12/16.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/bencherif_el
 */

public class FilesManager {


    /**
     * ********************************************************************************* ************************************************
     * *************************************************** Methods to create  Files path ************************************************
     * **********************************************************************************************************************************
     */

    /**
     * method to create root  directory
     *
     * @return root directory
     */
    private static File getMainPath() {

        // External sdcard location
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), WhatsCloneApplication.getAppContext().getString(R.string.app_name));
        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                AppHelper.LogCat("Oops! Failed create " + WhatsCloneApplication.getAppContext().getString(R.string.app_name) + " directory");
                return null;
            }
        }

        return mediaStorageDir;
    }

    /**
     * method to create root images directory
     *
     * @return all images directory
     */
    private static File getImagesPath() {

        // External sdcard location
        File mediaStorageDir = new File(getMainPath(), WhatsCloneApplication.getAppContext().getString(R.string.app_name) + " " + WhatsCloneApplication.getAppContext().getString(R.string.images_directory));
        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                AppHelper.LogCat("Oops! Failed create " + WhatsCloneApplication.getAppContext().getString(R.string.app_name) + " directory");
                return null;
            }
        }

        return mediaStorageDir;
    }

    /**
     * method to create image profile directory
     *
     * @return image profile directory
     */
    private static File getImagesProfilePath() {

        // External sdcard location
        File mediaStorageDir = new File(getImagesPath(), WhatsCloneApplication.getAppContext().getString(R.string.app_name) + " " + WhatsCloneApplication.getAppContext().getString(R.string.images_profile_directory));
        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                AppHelper.LogCat("Oops! Failed create " + WhatsCloneApplication.getAppContext().getString(R.string.app_name) + " directory");
                return null;
            }
        }

        return mediaStorageDir;
    }


    /**
     * method to create image profile directory
     *
     * @return image profile directory
     */
    private static File getImagesOtherPath() {

        // External sdcard location
        File mediaStorageDir = new File(getImagesPath(), WhatsCloneApplication.getAppContext().getString(R.string.app_name) + " " + WhatsCloneApplication.getAppContext().getString(R.string.images_other_directory));
        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                AppHelper.LogCat("Oops! Failed create " + WhatsCloneApplication.getAppContext().getString(R.string.app_name) + " directory");
                return null;
            }
        }

        return mediaStorageDir;
    }


    private static File getVideosPath() {

        // External sdcard location
        File mediaStorageDir = new File(getMainPath(), WhatsCloneApplication.getAppContext().getString(R.string.app_name) + " " + WhatsCloneApplication.getAppContext().getString(R.string.videos_directory));
        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                AppHelper.LogCat("Oops! Failed create " + WhatsCloneApplication.getAppContext().getString(R.string.app_name) + " directory");
                return null;
            }
        }

        return mediaStorageDir;
    }

    private static File getAudiosPath() {

        // External sdcard location
        File mediaStorageDir = new File(getMainPath(), WhatsCloneApplication.getAppContext().getString(R.string.app_name) + " " + WhatsCloneApplication.getAppContext().getString(R.string.audios_directory));
        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                AppHelper.LogCat("Oops! Failed create " + WhatsCloneApplication.getAppContext().getString(R.string.app_name) + " directory");
                return null;
            }
        }

        return mediaStorageDir;
    }


    private static File getDocumentsPath() {

        // External sdcard location
        File mediaStorageDir = new File(getMainPath(), WhatsCloneApplication.getAppContext().getString(R.string.app_name) + " " + WhatsCloneApplication.getAppContext().getString(R.string.documents_directory));
        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                AppHelper.LogCat("Oops! Failed create " + WhatsCloneApplication.getAppContext().getString(R.string.app_name) + " directory");
                return null;
            }
        }

        return mediaStorageDir;
    }
    /**
     * ********************************************************************************* ************************************************
     * *************************************************** Methods to get Files absolute path string ************************************
     * **********************************************************************************************************************************
     */

    /**
     * @return Images profile path string
     */
    private static String getImagesProfilePathString() {
        return String.valueOf(getImagesProfilePath());
    }

    /**
     * @return Images group path string
     */
    private static String getImagesGroupPathString() {
        return String.valueOf(getImagesProfilePath());
    }

    /**
     * @return other Images path string
     */
    private static String getImagesOtherPathString() {
        return String.valueOf(getImagesOtherPath());
    }

    /**
     * @return Videos path string
     */
    private static String getVideosPathString() {
        return String.valueOf(getVideosPath());
    }

    /**
     * @return Audios path string
     */
    private static String getAudiosPathString() {
        return String.valueOf(getAudiosPath());
    }

    /**
     * @return Documents path string
     */
    private static String getDocumentsPathString() {
        return String.valueOf(getDocumentsPath());
    }

/**
 * ********************************************************************************* ************************************************
 * *************************************************** Methods to Check if Files exists *********************************************
 * **********************************************************************************************************************************
 */

    /**
     * Check file if exists method
     *
     * @param Id this is the first parameter isFileImagesProfileExists method
     * @return Boolean
     */
    public static boolean isFileImagesProfileExists(String Id) {

        File file = new File(getImagesProfilePathString(), Id);
        return file.exists();
    }

    /**
     * Check file if exists method
     *
     * @param Id this is the first parameter isFileImagesProfileExists method
     * @return Boolean
     */
    public static boolean isFileImagesGroupExists(String Id) {
        File file = new File(getImagesGroupPathString(), Id);
        return file.exists();
    }

    /**
     * Check file if exists method
     *
     * @param Id this is the first parameter isFileImagesOtherExists method
     * @return Boolean
     */
    public static boolean isFileImagesOtherExists(String Id) {
        File file = new File(getImagesOtherPathString(), Id);
        return file.exists();
    }

    /**
     * Check file if exists method
     *
     * @param Id this is the first parameter isFileVideosExists method
     * @return Boolean
     */
    public static boolean isFileVideosExists(String Id) {
        File file = new File(getVideosPathString(), Id);
        return file.exists();
    }

    /**
     * Check file if exists method
     *
     * @param Id this is the first parameter isFileVideosExists method
     * @return Boolean
     */
    public static boolean isFileAudioExists(String Id) {
        File file = new File(getAudiosPathString(), Id);
        return file.exists();
    }

    /**
     * Check file if exists method
     *
     * @param Path this is the first parameter isFileVideosExists method
     * @return Boolean
     */
    public static boolean isFileRecordExists(String Path) {
        File file = new File(Path);
        return file.exists();
    }

    /**
     * Check file if exists method
     *
     * @param Id this is the first parameter isFileVideosExists method
     * @return Boolean
     */
    public static boolean isFileDocumentsExists(String Id) {
        File file = new File(getDocumentsPathString(), Id);
        return file.exists();
    }


    /**
     * ********************************************************************************* ************************************************
     * *************************************************** Methods to get Files *********************************************************
     * **********************************************************************************************************************************
     */

    /**
     * method to get file
     *
     * @param Identifier this is first parameter of getFileImageProfile method
     * @param name       this is second parameter of getFileImageProfile method
     * @return file
     */
    public static File getFileImageProfile(String Identifier, String name) {
        return new File(getFileImageProfilePath(Identifier, name));
    }

    /**
     * method to get file
     *
     * @param Identifier this is first parameter of getFileImageProfile method
     * @param name       this is second parameter of getFileImageProfile method
     * @return file
     */
    public static File getFileImageGroup(String Identifier, String name) {
        return new File(getFileImageGroupPath(Identifier, name));
    }

    /**
     * method to get file
     *
     * @param Identifier this is first parameter of getFileImageOther method
     * @param name       this is second parameter of getFileImageOther method
     * @return file
     */
    public static File getFileImageOther(String Identifier, String name) {
        return new File(getFileImageOtherPath(Identifier, name));
    }

    /**
     * method to get file
     *
     * @param Identifier this is first parameter of getFileVideo method
     * @param name       this is second parameter of getFileVideo method
     * @return file
     */
    public static File getFileVideo(String Identifier, String name) {
        return new File(getFileVideoPath(Identifier, name));
    }

    /**
     * method to get file
     *
     * @param Identifier this is first parameter of getFileAudio method
     * @param name       this is second parameter of getFileAudio method
     * @return file
     */
    public static File getFileAudio(String Identifier, String name) {
        return new File(getFileAudioPath(Identifier, name));
    }

    /**
     * method to get file
     *
     * @param Path this is a parameter of getFileRecord method
     * @return file
     */
    public static File getFileRecord(String Path) {
        return new File(Path);
    }

    /**
     * method to get file
     *
     * @param Identifier this is first parameter of getFileAudio method
     * @param name       this is second parameter of getFileAudio method
     * @return file
     */
    public static File getFileDocument(String Identifier, String name) {
        return new File(getFileDocumentsPath(Identifier, name));
    }

    /**
     * ********************************************************************************* ************************************************
     * *************************************************** Methods to get Files Paths (use those methods in other classes) **************
     * **********************************************************************************************************************************
     */

    public static String getProfileImage(String Identifier, String name) {
        return String.format("%s-profile-%s", name, Identifier + ".jpg");
    }

    public static String getGroupImage(String Identifier, String name) {
        return String.format("%s-group-%s", name, Identifier + ".jpg");
    }

    public static String getOthersSentImage(String Identifier, String name) {
        return String.format("%s-other-%s", name, Identifier + ".jpg");
    }

    public static String getSentAudio(String Identifier, String name) {
        return String.format("%s-audio-%s", name, Identifier + ".mp3");
    }

    public static String getDocument(String Identifier, String name) {
        return String.format("%s-document-%s", name, Identifier + ".pdf");
    }

    public static String getVideo(String Identifier, String name) {
        return String.format("%s-video-%s", name, Identifier + ".mp4");
    }

    /**
     * **************************************************************** *****************************************************************
     * *************************************************** Methods to get String Paths **************************************************
     * **********************************************************************************************************************************
     */

    /**
     * @param Identifier his is first parameter of getFileImageProfilePath method
     * @param name       his is second parameter of getFileImageProfilePath method
     * @return String path
     */
    private static String getFileImageProfilePath(String Identifier, String name) {
        return String.format(getImagesProfilePathString() + File.separator + "%s-profile-%s", name, Identifier + ".jpg");
    }

    /**
     * @param Identifier his is first parameter of getFileImageProfilePath method
     * @param name       his is second parameter of getFileImageProfilePath method
     * @return String path
     */
    private static String getFileImageGroupPath(String Identifier, String name) {
        return String.format(getImagesGroupPathString() + File.separator + "%s-group-%s", name, Identifier + ".jpg");
    }

    /**
     * @param Identifier his is first parameter of getFileImageOtherPath method
     * @param name       his is second parameter of getFileImageOtherPath method
     * @return String path
     */
    public static String getFileImageOtherPath(String Identifier, String name) {
        return String.format(getImagesOtherPathString() + File.separator + "%s-other-%s", name, Identifier + ".jpg");
    }

    /**
     * @param Identifier his is first parameter of getFileAudio method
     * @param name       his is second parameter of getFileAudio method
     * @return String path
     */
    public static String getFileVideoPath(String Identifier, String name) {
        //String timeStamp = new SimpleDateFormat("yyyy-MM-dd-HHmmss", Locale.getDefault()).format(new Date());
        return String.format(getVideosPathString() + File.separator + "%s-video-%s", name, Identifier + ".mp4");
    }

    /**
     * @param Identifier his is first parameter of getFileAudio method
     * @param name       his is second parameter of getFileAudio method
     * @return String path
     */
    public static String getFileAudioPath(String Identifier, String name) {
        return String.format(getAudiosPathString() + File.separator + "%s-audio-%s", name, Identifier + ".mp3");
    }

    /**
     * @return String path
     */
    public static String getFileRecordPath() {
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd-HHmmss", Locale.getDefault()).format(new Date());
        return String.format(getAudiosPathString() + File.separator + "record_%s", timeStamp + ".mp3");
    }

    /**
     * @param Identifier his is first parameter of getFileAudio method
     * @param name       his is second parameter of getFileAudio method
     * @return String path
     */
    public static String getFileDocumentsPath(String Identifier, String name) {
        return String.format(getDocumentsPathString() + File.separator + "%s-document-%s", name, Identifier + ".pdf");
    }

    /**
     * @return String path
     */
    public static String getFileThumbnailPath() {
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd-HHmmss", Locale.getDefault()).format(new Date());
        return String.format(getVideosPathString() + File.separator + "thumbnail_%s", timeStamp + ".jpg");
    }
    /**
     * **************************************************************** *****************************************************************
     * *************************************************** Methods to get downloads files ***********************************************
     * **********************************************************************************************************************************
     * */


    /**
     * method to do
     *
     * @param mContext   this is the first parameter downloadFilesToDevice method
     * @param fileUrl    this is the second parameter downloadFilesToDevice method
     * @param Identifier this is the third parameter downloadFilesToDevice method
     * @param name       this is the fourth parameter downloadFilesToDevice method
     */
    public static void downloadFilesToDevice(Context mContext, String fileUrl, String Identifier, String name, String type) {

        APIService apiService = new APIService(mContext);
        final FilesDownloadService downloadService = apiService.RootService(FilesDownloadService.class, PreferenceManager.getToken(mContext), EndPoints.BASE_URL);

        new AsyncTask<Void, Long, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                Call<ResponseBody> call = downloadService.downloadSmallFileSizeUrlSync(fileUrl);
                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            AppHelper.LogCat("server contacted and has file");
                               try{
                                   writeResponseBodyToDisk(response.body(), Identifier, name, type);
                               }catch (Exception e){
                                   AppHelper.LogCat("file download was a failed");
                               }


                           //AppHelper.LogCat("file download was a success? " + writtenToDisk);
                        } else {
                            AppHelper.LogCat("server contact failed");
                        }

                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        AppHelper.LogCat("download failed " + t.getMessage());
                    }


                });

                return null;
            }
        }.execute();
    }

    /**
     * @param body       this is the first parameter writeResponseBodyToDisk method
     * @param Identifier this is the first parameter writeResponseBodyToDisk method
     * @param name       this is the first parameter writeResponseBodyToDisk method
     * @return boolean
     */
    private static boolean writeResponseBodyToDisk(ResponseBody body, String Identifier, String name, String type) {
        try {
            boolean deleted = true;

            if (isFileImagesProfileExists(FilesManager.getProfileImage(Identifier, name))) {
                deleted = getFileImageProfile(Identifier, name).delete();
            } else if (isFileImagesGroupExists(FilesManager.getGroupImage(Identifier, name))) {
                deleted = getFileImageGroup(Identifier, name).delete();
            } else if (isFileImagesOtherExists(FilesManager.getOthersSentImage(Identifier, name))) {
                deleted = getFileImageOther(Identifier, name).delete();
            } else if (isFileVideosExists(FilesManager.getVideo(Identifier, name))) {
                deleted = getFileVideo(Identifier, name).delete();
            } else if (isFileAudioExists(FilesManager.getSentAudio(Identifier, name))) {
                deleted = getFileAudio(Identifier, name).delete();
            } else if (isFileDocumentsExists(FilesManager.getDocument(Identifier, name))) {
                deleted = getFileDocument(Identifier, name).delete();
            }

            if (!deleted) {
                AppHelper.LogCat(" not deleted ");
                return false;
            } else {
                AppHelper.LogCat("deleted");
                File futureStudioIconFile = null;
                switch (type) {
                    case "profile":
                        futureStudioIconFile = new File(getFileImageProfilePath(Identifier, name));
                        break;
                    case "group":
                        futureStudioIconFile = new File(getFileImageGroupPath(Identifier, name));
                        break;
                    case "other":
                        futureStudioIconFile = new File(getFileImageOtherPath(Identifier, name));
                        break;
                    case "audio":
                        futureStudioIconFile = new File(getFileAudioPath(Identifier, name));
                        break;
                    case "document":
                        futureStudioIconFile = new File(getFileDocumentsPath(Identifier, name));
                        break;
                    case "video":
                        futureStudioIconFile = new File(getFileVideoPath(Identifier, name));
                        break;
                }

                InputStream inputStream = null;
                OutputStream outputStream = null;

                try {
                    byte[] fileReader = new byte[4096];

                /*long fileSize = body.contentLength();
                long fileSizeDownloaded = 0;*/

                    inputStream = body.byteStream();
                    try {
                        outputStream = new FileOutputStream(futureStudioIconFile);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                    while (true) {
                        int read = 0;
                        try {
                            read = inputStream.read(fileReader);
                        } catch (java.io.IOException e) {
                            e.printStackTrace();
                        }

                        if (read == -1) {
                            break;
                        }

                        try {
                            outputStream.write(fileReader, 0, read);
                        } catch (java.io.IOException e) {
                            e.printStackTrace();
                        }

                    /*fileSizeDownloaded += read;*/

                    /*AppHelper.LogCat("file download: " + fileSizeDownloaded + " of " + fileSize);*/
                    }

                    try {
                        outputStream.flush();
                    } catch (java.io.IOException e) {
                        e.printStackTrace();
                    }

                    return true;
                } catch (IOException e) {
                    return false;
                } finally {
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (java.io.IOException e) {
                            e.printStackTrace();
                        }
                    }

                    if (outputStream != null) {
                        try {
                            outputStream.close();
                        } catch (java.io.IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (IOException e) {
            return false;

        }
    }

    /**
     * method to get mime type of files
     *
     * @param url
     * @return
     */
    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }


    public static String getFileSize(long size) {
        if (size <= 0) return "0";
        final String[] units = new String[]{"B", "kB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }


    public static File getFileThumbnail(Bitmap bmp) throws java.io.IOException {
        File file = new File(getFileThumbnailPath());
        file.getParentFile().mkdirs();
        FileOutputStream out = new FileOutputStream(file);
        bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
        out.close();
        return file;
    }

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri     The Uri to query.
     */
    @SuppressLint("NewApi")
    public static String getPath(final Context context, final Uri uri) {



        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    private static String getDataColumn(Context context, Uri uri, String selection,
                                        String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
}

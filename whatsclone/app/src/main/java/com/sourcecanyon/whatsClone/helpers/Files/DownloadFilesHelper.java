package com.sourcecanyon.whatsClone.helpers.Files;

import android.os.Handler;
import android.os.Looper;

import com.sourcecanyon.whatsClone.helpers.AppHelper;
import com.sourcecanyon.whatsClone.interfaces.DownloadCallbacks;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import io.realm.internal.IOException;
import okhttp3.ResponseBody;

import static com.sourcecanyon.whatsClone.helpers.Files.FilesManager.getFileAudio;
import static com.sourcecanyon.whatsClone.helpers.Files.FilesManager.getFileDocument;
import static com.sourcecanyon.whatsClone.helpers.Files.FilesManager.getFileImageOther;
import static com.sourcecanyon.whatsClone.helpers.Files.FilesManager.getFileVideo;
import static com.sourcecanyon.whatsClone.helpers.Files.FilesManager.isFileAudioExists;
import static com.sourcecanyon.whatsClone.helpers.Files.FilesManager.isFileDocumentsExists;
import static com.sourcecanyon.whatsClone.helpers.Files.FilesManager.isFileImagesOtherExists;
import static com.sourcecanyon.whatsClone.helpers.Files.FilesManager.isFileVideosExists;

/**
 * Created by Abderrahim El imame on 7/28/16.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/bencherif_el
 */

public class DownloadFilesHelper {

    private DownloadCallbacks mDownloadCallbacks;
    private static final int DEFAULT_BUFFER_SIZE = 4096;
    private String type;
    private String Identifier;
    private String name;
    private ResponseBody mFile;

    public DownloadFilesHelper(final ResponseBody mFile, String Identifier, String name, String type, final DownloadCallbacks mDownloadCallbacks) {
        this.mFile = mFile;
        this.Identifier = Identifier;
        this.type = type;
        this.name = name;
        this.mDownloadCallbacks = mDownloadCallbacks;

    }


    public boolean writeResponseBodyToDisk() {
        try {
            try {
                if (isFileImagesOtherExists(Identifier)) {
                    getFileImageOther(Identifier, name).delete();
                    return false;
                } else if (isFileVideosExists(Identifier)) {
                    getFileVideo(Identifier, name).delete();
                    return false;
                } else if (isFileAudioExists(Identifier)) {
                    getFileAudio(Identifier, name).delete();
                    return false;
                } else if (isFileDocumentsExists(Identifier)) {
                    getFileDocument(Identifier, name).delete();
                    return false;
                }
            } catch (Exception ignored) {
            }

            File futureStudioIconFile = null;
            switch (type) {
                case "image":
                    futureStudioIconFile = new File(FilesManager.getFileImageOtherPath(Identifier, name));
                    break;
                case "video":
                    futureStudioIconFile = new File(FilesManager.getFileVideoPath(Identifier, name));
                    break;
                case "audio":
                    futureStudioIconFile = new File(FilesManager.getFileAudioPath(Identifier, name));
                    break;
                case "document":
                    futureStudioIconFile = new File(FilesManager.getFileDocumentsPath(Identifier, name));
                    break;
            }

            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {
                byte[] fileReader = new byte[DEFAULT_BUFFER_SIZE];

                long fileSize = mFile.contentLength();
                long fileSizeDownloaded = 0;

                inputStream = mFile.byteStream();
                try {
                    outputStream = new FileOutputStream(futureStudioIconFile);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                Handler handler = new Handler(Looper.getMainLooper());
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

                    fileSizeDownloaded += read;
                    // update progress on UI thread
                    handler.post(new Updater(fileSizeDownloaded, fileSize));
                    //AppHelper.LogCat("file download: " + fileSizeDownloaded + " of " + fileSize);
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
        } catch (IOException e) {
            return false;
        }
    }

    private class Updater implements Runnable {
        private long mUploaded;
        private long mTotal;

        Updater(long uploaded, long total) {
            mUploaded = uploaded;
            mTotal = total;
        }


        @Override
        public void run() {
            mDownloadCallbacks.onUpdate((int) (100 * mUploaded / mTotal),type);
        }
    }

}

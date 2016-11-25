package com.sourcecanyon.whatsClone.helpers.Files;

import android.os.Handler;
import android.os.Looper;

import com.sourcecanyon.whatsClone.interfaces.UploadCallbacks;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;

/**
 * Created by Abderrahim El imame on 7/26/16.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/bencherif_el
 */

public class UploadFilesHelper extends RequestBody {


    private File mFile;
    private UploadCallbacks mUploadCallbacks;
    private String mimeType;
    private String mType;

    private static final int DEFAULT_BUFFER_SIZE = 2048;//// TODO: 8/4/16 khasni nzid fih


    public UploadFilesHelper(final File mFile, final UploadCallbacks mUploadCallbacks, String mimeType, String mType) {
        this.mFile = mFile;
        this.mUploadCallbacks = mUploadCallbacks;
        this.mimeType = mimeType;
        this.mType = mType;
    }

    @Override
    public MediaType contentType() {
        return MediaType.parse(mimeType);

    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        long fileLength = mFile.length();
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        FileInputStream in = new FileInputStream(mFile);
        long uploaded = 0;

        try {
            int read;
            Handler handler = new Handler(Looper.getMainLooper());
            while ((read = in.read(buffer)) != -1) {

                // update progress on UI thread
                handler.post(new Updater(uploaded, fileLength));

                uploaded += read;
                sink.write(buffer, 0, read);
            }
        } finally {
            in.close();
        }
    }

    private class Updater implements Runnable {
        private long mUploaded;
        private long mTotal;

        public Updater(long uploaded, long total) {
            mUploaded = uploaded;
            mTotal = total;
        }

        @Override
        public void run() {
            mUploadCallbacks.onUpdate((int) (100 * mUploaded / mTotal),mType);
        }
    }

}

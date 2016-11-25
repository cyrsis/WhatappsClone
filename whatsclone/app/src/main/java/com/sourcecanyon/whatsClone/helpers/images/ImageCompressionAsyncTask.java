package com.sourcecanyon.whatsClone.helpers.images;

import android.os.AsyncTask;

/**
 * Created by Abderrahim El imame on 6/13/16.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/bencherif_el
 */

public abstract class ImageCompressionAsyncTask extends AsyncTask<String, Void, byte[]> {
    @Override
    protected byte[] doInBackground(String... strings) {
        if (strings.length == 0 || strings[0] == null)
            return null;
        return ImageUtils.compressImage(strings[0]);
    }

    protected abstract void onPostExecute(byte[] imageBytes);

}

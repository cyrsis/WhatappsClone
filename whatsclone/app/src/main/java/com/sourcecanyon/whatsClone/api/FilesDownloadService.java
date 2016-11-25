package com.sourcecanyon.whatsClone.api;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/**
 * Created by Abderrahim El imame on 6/13/16.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/bencherif_el
 */

public interface FilesDownloadService {

    /**
     * method to download a small file size
     *
     * @param fileName this is  parameter for  downloadSmallFileSizeUrlSync method
     * @return this is return value
     */
    @GET
    Call<ResponseBody> downloadSmallFileSizeUrlSync(@Url String fileName);

    /**
     * method to download a large file size
     *
     * @param fileName this is   parameter for  downloadLargeFileSizeUrlSync method
     * @return this is return value
     */
    @Streaming
    @GET
    Call<ResponseBody> downloadLargeFileSizeUrlSync(@Url String fileName);
}

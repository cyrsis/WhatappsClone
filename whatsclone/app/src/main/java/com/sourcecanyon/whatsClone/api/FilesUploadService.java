package com.sourcecanyon.whatsClone.api;

import com.sourcecanyon.whatsClone.app.EndPoints;
import com.sourcecanyon.whatsClone.helpers.Files.UploadFilesHelper;
import com.sourcecanyon.whatsClone.models.messages.FilesResponse;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/**
 * Created by Abderrahim El imame on 7/26/16.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/bencherif_el
 */

public interface FilesUploadService {

    /**
     * method to upload images
     *
     * @param image this is  the second parameter for  uploadMessageImage method
     * @return this is return value
     */
    @Multipart
    @POST(EndPoints.UPLOAD_MESSAGES_IMAGE)
    Call<FilesResponse> uploadMessageImage(@Part("image\"; filename=\"messageImage\" ") UploadFilesHelper image);

    /**
     * method to upload videos
     *
     * @param video     this is  the first parameter for  uploadMessageVideo method
     * @param thumbnail this is  the second parameter for  uploadMessageVideo method
     * @return this is return value
     */
    @Multipart
    @POST(EndPoints.UPLOAD_MESSAGES_VIDEO)
    Call<FilesResponse> uploadMessageVideo(@Part("video\"; filename=\"messageVideo.mp4\" ") UploadFilesHelper video,
                                           @Part("thumbnail\"; filename=\"messageVideoThumbnail.jpg\" ") RequestBody thumbnail);

    /**
     * method to upload audio
     *
     * @param audio this is   parameter for  uploadMessageAudio method
     * @return this is return value
     */
    @Multipart
    @POST(EndPoints.UPLOAD_MESSAGES_AUDIO)
    Call<FilesResponse> uploadMessageAudio(@Part("audio\"; filename=\"messageAudio\" ") UploadFilesHelper audio);

    /**
     * method to upload document
     *
     * @param document this is  parameter for  uploadMessageDocument method
     * @return this is return value
     */
    @Multipart
    @POST(EndPoints.UPLOAD_MESSAGES_DOCUMENT)
    Call<FilesResponse> uploadMessageDocument(@Part("document\"; filename=\"messageDocument\" ") UploadFilesHelper document);

}

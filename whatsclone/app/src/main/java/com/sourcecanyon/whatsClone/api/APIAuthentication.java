package com.sourcecanyon.whatsClone.api;



import com.sourcecanyon.whatsClone.app.EndPoints;
import com.sourcecanyon.whatsClone.models.JoinModel;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by Abderrahim El imame on 01/11/2015.
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/bencherif_el
 */
public interface APIAuthentication {
    /**
     * method to join
     * @param phone this is parameter for join method
     */

    @FormUrlEncoded
    @POST(EndPoints.JOIN)
    Call<JoinModel> join(@Field("phone") String phone,@Field("country") String country);

    /**
     * method to resend SMS request
     * @param phone this is parameter for resend method
     */

    @FormUrlEncoded
    @POST(EndPoints.RESEND_REQUEST_SMS)
    Call<JoinModel> resend(@Field("phone") String phone);

    /**
     * method to verify the user code
     *
     * @param code this is parameter for verifyUser method
     * @return this is what method will return
     */
    @FormUrlEncoded
    @POST(EndPoints.VERIFY_USER)
    Call<JoinModel> verifyUser(@Field("code") String code);
}

package com.sourcecanyon.whatsClone.services;

import android.app.IntentService;
import android.content.Intent;

import com.sourcecanyon.whatsClone.activities.main.MainActivity;
import com.sourcecanyon.whatsClone.api.APIAuthentication;
import com.sourcecanyon.whatsClone.api.APIService;
import com.sourcecanyon.whatsClone.app.EndPoints;
import com.sourcecanyon.whatsClone.helpers.AppHelper;
import com.sourcecanyon.whatsClone.helpers.PreferenceManager;
import com.sourcecanyon.whatsClone.models.JoinModel;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Abderrahim El imame on 23/02/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class SMSVerificationService extends IntentService {


    public SMSVerificationService() {
        super(SMSVerificationService.class.getSimpleName());
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            String code = intent.getStringExtra("code");
            verifyUser(code);
        }
    }

    private void verifyUser(String code) {
        APIAuthentication mAPIAuthentication = APIService.RootService(APIAuthentication.class, EndPoints.BASE_URL);
        Call<JoinModel> VerifyUser = mAPIAuthentication.verifyUser(code);
        VerifyUser.enqueue(new Callback<JoinModel>() {
            @Override
            public void onResponse(Call<JoinModel> call, Response<JoinModel> response) {
                if (response.isSuccessful()) {
                    if (response.body().isSuccess()) {
                        AppHelper.CustomToast(getApplicationContext(), response.body().getMessage());
                        PreferenceManager.setID(response.body().getUserID(), SMSVerificationService.this);
                        PreferenceManager.setToken(response.body().getToken(), SMSVerificationService.this);
                        Intent intent = new Intent(SMSVerificationService.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        startService(new Intent(SMSVerificationService.this, MainService.class));


                    } else {
                        AppHelper.CustomToast(getApplicationContext(), response.body().getMessage());
                    }
                } else {
                    AppHelper.CustomToast(getApplicationContext(), response.message());
                }
            }

            @Override
            public void onFailure(Call<JoinModel> call, Throwable t) {
                AppHelper.LogCat("SMS verification failure  SMSVerificationService" + t.getMessage());
                AppHelper.CustomToast(getApplicationContext(), t.getMessage());

            }
        });

    }
}

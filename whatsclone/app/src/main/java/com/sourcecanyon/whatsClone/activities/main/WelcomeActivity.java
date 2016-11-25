package com.sourcecanyon.whatsClone.activities.main;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.sourcecanyon.whatsClone.R;
import com.sourcecanyon.whatsClone.adapters.recyclerView.CountriesAdapter;
import com.sourcecanyon.whatsClone.adapters.recyclerView.TextWatcherAdapter;
import com.sourcecanyon.whatsClone.api.APIAuthentication;
import com.sourcecanyon.whatsClone.api.APIService;
import com.sourcecanyon.whatsClone.app.EndPoints;
import com.sourcecanyon.whatsClone.app.WhatsCloneApplication;
import com.sourcecanyon.whatsClone.helpers.AppHelper;
import com.sourcecanyon.whatsClone.helpers.PreferenceManager;
import com.sourcecanyon.whatsClone.helpers.SignUpPreferenceManager;
import com.sourcecanyon.whatsClone.models.CountriesModel;
import com.sourcecanyon.whatsClone.models.JoinModel;
import com.sourcecanyon.whatsClone.models.users.Pusher;
import com.sourcecanyon.whatsClone.services.MainService;
import com.sourcecanyon.whatsClone.services.SMSVerificationService;
import com.sourcecanyon.whatsClone.sync.AuthenticatorService;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Abderrahim El imame on 09/02/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class WelcomeActivity extends AccountAuthenticatorActivity implements View.OnClickListener {
    @Bind(R.id.numberPhone)
    TextInputEditText phoneNumberWrapper;
    @Bind(R.id.inputOtpWrapper)
    TextInputEditText inputOtpWrapper;
    @Bind(R.id.btn_request_sms)
    FloatingActionButton btnNext;
    @Bind(R.id.btn_verify_otp)
    TextView btnVerifyOtp;
    @Bind(R.id.viewPagerVertical)
    ViewPager viewPager;
    @Bind(R.id.TimeCount)
    TextView textViewShowTime;
    @Bind(R.id.Resend)
    TextView Resend;
    @Bind(R.id.progressbar)
    ProgressBar mProgressBar;
    @Bind(R.id.code)
    TextView code;
    @Bind(R.id.btn_change_number)
    TextView EditBtn;
    @Bind(R.id.toolbar)
    LinearLayout toolbar;
    @Bind(R.id.CounrtriesList)
    RecyclerView CountriesList;
    @Bind(R.id.txtEditMobile)
    TextView txtEditMobile;
    @Bind(R.id.search_input)
    TextInputEditText searchInput;
    @Bind(R.id.clear_btn_search_view)
    ImageView clearBtn;


    private CountriesAdapter mCountriesAdapter;
    private String Code;
    private String Country;
    private CountDownTimer countDownTimer;
    private long totalTimeCountInMilliseconds;
    private long seconds, ResumeSeconds;
    private ViewPagerAdapter adapter;
    private SignUpPreferenceManager mSignUpPreferenceManager;
    public static final String PARAM_AUTH_TOKEN_TYPE = "auth.token";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        initializerView();
    }

    /**
     * method to initialize the view
     */
    private void initializerView() {
        /**
         * Checking if user already connected
         */
        if (PreferenceManager.getToken(this) != null) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();

        }

        initializerSearchView(searchInput, clearBtn);
        clearBtn.setOnClickListener(v -> clearSearchView());
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(WhatsCloneApplication.getAppContext());
        CountriesList.setLayoutManager(mLinearLayoutManager);
        mCountriesAdapter = new CountriesAdapter(this);
        CountriesList.setAdapter(mCountriesAdapter);
        Gson gson = new Gson();
        final List<CountriesModel> list = gson.fromJson(AppHelper.loadJSONFromAsset(this), new TypeToken<List<CountriesModel>>() {
        }.getType());
        mCountriesAdapter.setCountries(list);
        Code = "" + list.get(1).getDial_code();
        Country = "" + list.get(1).getName();
        code.setText(Code);
        toolbar.setBackgroundColor(AppHelper.getColor(this, R.color.colorPrimary));
        btnNext.setOnClickListener(this);
        btnVerifyOtp.setOnClickListener(this);
        Resend.setOnClickListener(this);
        EditBtn.setOnClickListener(this);
        mSignUpPreferenceManager = new SignUpPreferenceManager(this);
        adapter = new ViewPagerAdapter();
        viewPager.setAdapter(adapter);

        /**
         * Checking if the device is waiting for sms
         * showing the user OTP screen
         */
        if (mSignUpPreferenceManager.isWaitingForSms()) {
            viewPager.setCurrentItem(1);
            resumeTimer();
        }

        if (viewPager.getCurrentItem() == 1) {

            if (AppHelper.checkPermission(this, Manifest.permission.RECEIVE_SMS)) {
                AppHelper.LogCat("RECEIVE SMS permission already granted.");
            } else {
                AppHelper.LogCat("Please request RECEIVE SMS permission.");
                AppHelper.requestPermission(this, Manifest.permission.RECEIVE_SMS);
            }
            if (AppHelper.checkPermission(this, Manifest.permission.READ_SMS)) {
                AppHelper.LogCat("READ SMS permission already granted.");
            } else {
                AppHelper.LogCat("Please request READ SMS permission.");
                AppHelper.requestPermission(this, Manifest.permission.READ_SMS);
            }

        }


    }

    /**
     * method to clear/reset search view content
     */
    public void clearSearchView() {
        if (searchInput.getText() != null) {
            searchInput.setText("");
            Gson gson = new Gson();
            final List<CountriesModel> list = gson.fromJson(AppHelper.loadJSONFromAsset(this), new TypeToken<List<CountriesModel>>() {
            }.getType());
            mCountriesAdapter.setCountries(list);
        }

    }

    /**
     * method to initial the search view
     */
    public void initializerSearchView(TextInputEditText searchInput, ImageView clearSearchBtn) {

        final Context context = this;
        searchInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                InputMethodManager inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }

        });
        searchInput.addTextChangedListener(new TextWatcherAdapter() {
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                clearSearchBtn.setVisibility(View.GONE);
            }

            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mCountriesAdapter.setString(s.toString());
                Search(s.toString().trim());
                clearSearchBtn.setVisibility(View.VISIBLE);
            }

            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void afterTextChanged(Editable s) {

                if (s.length() == 0) {
                    clearSearchBtn.setVisibility(View.GONE);
                    Gson gson = new Gson();
                    final List<CountriesModel> list = gson.fromJson(AppHelper.loadJSONFromAsset(WelcomeActivity.this), new TypeToken<List<CountriesModel>>() {
                    }.getType());
                    mCountriesAdapter.setCountries(list);
                }
            }
        });

    }

    /**
     * method to start searching
     *
     * @param string this is parameter of Search method
     */
    public void Search(String string) {

        final List<CountriesModel> filteredModelList;
        filteredModelList = FilterList(string);
        if (filteredModelList.size() != 0) {
            mCountriesAdapter.animateTo(filteredModelList);
            CountriesList.scrollToPosition(0);
        }
    }

    /**
     * method to filter the list
     *
     * @param query this is parameter of FilterList method
     * @return this for what method return
     */
    private List<CountriesModel> FilterList(String query) {
        query = query.toLowerCase();
        List<CountriesModel> countriesModelList = mCountriesAdapter.getCountries();
        final List<CountriesModel> filteredModelList = new ArrayList<>();
        for (CountriesModel countriesModel : countriesModelList) {
            final String name = countriesModel.getName().toLowerCase();
            if (name.contains(query)) {
                filteredModelList.add(countriesModel);
            }
        }
        return filteredModelList;
    }

    /**
     * method to validate user information
     */
    private void validateInformation() {
        String mobile = null;
        try {
            mobile = phoneNumberWrapper.getText().toString().trim();
        } catch (Exception e) {
            AppHelper.LogCat(" number mobile is null Exception WelcomeActivity " + e.getMessage());
        }
        if (mobile != null) {
            PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
            Phonenumber.PhoneNumber phNumberProto = null;
            Code = Code.replace("+", "");
            String countryCode = phoneUtil.getRegionCodeForCountryCode(Integer.parseInt(Code));
            try {
                phNumberProto = phoneUtil.parse(mobile, countryCode);
            } catch (NumberParseException e) {
                e.printStackTrace();
                AppHelper.LogCat("number  error  NumberParseException  WelcomeActivity" + mobile);
                phoneNumberWrapper.setError(getString(R.string.enter_a_val_number));
            }
            if (phNumberProto != null) {
                boolean isValid = phoneUtil.isValidNumber(phNumberProto);
                if (isValid) {
                    String internationalFormat = phoneUtil.format(phNumberProto, PhoneNumberUtil.PhoneNumberFormat.E164);
                    mSignUpPreferenceManager.setMobileNumber(internationalFormat);
                    requestForSMS(internationalFormat, Country);
                } else {
                    phoneNumberWrapper.setError(getString(R.string.enter_a_val_number));
                }
            }
        } else {
            phoneNumberWrapper.setError(getString(R.string.enter_a_val_number));
        }
    }

    /**
     * method to resend a request for SMS
     *
     * @param mobile this is parameter of ResendRequestForSMS method
     */
    private void ResendRequestForSMS(String mobile) {

        APIAuthentication mAPIAuthentication = APIService.RootService(APIAuthentication.class, EndPoints.BASE_URL);
        Call<JoinModel> ResendModelCall = mAPIAuthentication.resend(mobile);
        ResendModelCall.enqueue(new Callback<JoinModel>() {
            @Override
            public void onResponse(Call<JoinModel> call, Response<JoinModel> response) {
                if (response.isSuccessful()) {
                    if (response.body().isSuccess()) {
                        Resend.setVisibility(View.GONE);
                        mProgressBar.setVisibility(View.VISIBLE);
                        textViewShowTime.setVisibility(View.VISIBLE);
                        setTimer();
                        startTimer();
                        mSignUpPreferenceManager.setIsWaitingForSms(true);
                        viewPager.setCurrentItem(1);
                        txtEditMobile.setText(mSignUpPreferenceManager.getMobileNumber());
                    } else {
                        AppHelper.CustomToast(WelcomeActivity.this, response.body().getMessage());
                    }
                } else {
                    AppHelper.CustomToast(WelcomeActivity.this, response.message());
                }
            }

            @Override
            public void onFailure(Call<JoinModel> call, Throwable t) {
                AppHelper.CustomToast(WelcomeActivity.this, t.getMessage());
            }
        });
    }

    /**
     * method to send an SMS request to provider
     *
     * @param mobile  this the first parameter of  requestForSMS method
     * @param country this the second parameter of requestForSMS  method
     */
    private void requestForSMS(String mobile, String country) {
        APIAuthentication mAPIAuthentication = APIService.RootService(APIAuthentication.class, EndPoints.BASE_URL);
        Call<JoinModel> JoinModelCall = mAPIAuthentication.join(mobile, country);
        AppHelper.showDialog(this, getString(R.string.set_back_and_keep_calm_you_will_receive_an_sms_of_verification));
        JoinModelCall.enqueue(new Callback<JoinModel>() {
            @Override
            public void onResponse(Call<JoinModel> call, Response<JoinModel> response) {
                if (response.isSuccessful()) {
                    if (response.body().isSuccess()) {
                        AppHelper.hideDialog();
                        setTimer();
                        startTimer();
                        mSignUpPreferenceManager.setIsWaitingForSms(true);
                        viewPager.setCurrentItem(1);
                        txtEditMobile.setText(mSignUpPreferenceManager.getMobileNumber());
                        String accountType = getIntent().getStringExtra(PARAM_AUTH_TOKEN_TYPE);
                        if (accountType == null) {
                            accountType = AuthenticatorService.ACCOUNT_TYPE;
                        }
                        AccountManager accMgr = AccountManager.get(WelcomeActivity.this);
                        // This is the magic that add the account to the Android Account Manager
                        final Account account = new Account(getResources().getString(R.string.app_name), accountType);
                        accMgr.addAccountExplicitly(account, response.body().getCode(), null);
                        // Now we tell our caller, could be the Android Account Manager or even our own application
                        // that the process was successful
                        final Intent intent = new Intent();
                        intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, getResources().getString(R.string.app_name));
                        intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, accountType);
                        intent.putExtra(AccountManager.KEY_AUTHTOKEN, accountType);
                        setAccountAuthenticatorResult(intent.getExtras());
                        setResult(RESULT_OK, intent);
                        AppHelper.LogCat("Failed here 0 " + response.body().getMessage());
                        new Handler().postDelayed(() -> testRun(response.body().getCode()), 4000);
                    } else {
                        AppHelper.hideDialog();
                        AppHelper.CustomToast(WelcomeActivity.this, response.body().getMessage());
                        AppHelper.LogCat("Failed here 1 " + response.body().getMessage());
                    }
                } else {
                    AppHelper.hideDialog();
                    AppHelper.CustomToast(WelcomeActivity.this, response.message());
                    AppHelper.LogCat("Failed here 2 " + response.message());
                }
            }

            @Override
            public void onFailure(Call<JoinModel> call, Throwable t) {
                AppHelper.hideDialog();
                AppHelper.LogCat("Failed to create your account " + t.getMessage());
                AppHelper.CustomToast(WelcomeActivity.this, "Please your internet connection and try again");
                //Hide the Keyboard
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(phoneNumberWrapper.getWindowToken(), 0);
            }
        });

    }

    private void  testRun(String  code){
        if (!code.isEmpty()) {
            Intent otpIntent = new Intent(getApplicationContext(), SMSVerificationService.class);
            otpIntent.putExtra("code", code);
            startService(otpIntent);
        } else {
            AppHelper.CustomToast(this, getString(R.string.please_enter_your_ver_code));
        }
    }

    /**
     * method to verify the code received by user then activating the user
     */
    private void verificationOfCode() {
        String code = inputOtpWrapper.getText().toString().trim();
        if (!code.isEmpty()) {
            Intent otpIntent = new Intent(getApplicationContext(), SMSVerificationService.class);
            otpIntent.putExtra("code", code);
            startService(otpIntent);
        } else {
            AppHelper.CustomToast(this, getString(R.string.please_enter_your_ver_code));
        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_request_sms:
                validateInformation();
                break;

            case R.id.btn_verify_otp:
                verificationOfCode();
                break;

            case R.id.btn_change_number:
                viewPager.setCurrentItem(0);
                stopTimer();
                PreferenceManager.setID(0, this);
                PreferenceManager.setToken(null, this);
                mSignUpPreferenceManager.setIsWaitingForSms(false);
                break;

            case R.id.Resend:
                viewPager.setCurrentItem(1);
                ResendRequestForSMS(mSignUpPreferenceManager.getMobileNumber());
                break;
        }
    }

    class ViewPagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == ((View) object);
        }

        public Object instantiateItem(View collection, int position) {

            int resId = 0;
            switch (position) {
                case 0:
                    resId = R.id.numberPhone_layout;
                    break;
                case 1:
                    resId = R.id.layout_verification;
                    break;
            }
            return findViewById(resId);
        }
    }

    private void setTimer() {
        int time = 4;
        mProgressBar.setMax(60 * time);
        totalTimeCountInMilliseconds = 60 * time * 1000;

    }

    private void startTimer() {
        countDownTimer = new WhatsCloneCounter(totalTimeCountInMilliseconds, 500).start();
    }

    public void stopTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    public void resumeTimer() {
        countDownTimer = new WhatsCloneCounter(ResumeSeconds, 500).start();
    }


    public class WhatsCloneCounter extends CountDownTimer {
        public WhatsCloneCounter(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long leftTimeInMilliseconds) {
            ResumeSeconds = leftTimeInMilliseconds;
            seconds = leftTimeInMilliseconds / 1000;
            mProgressBar.setProgress((int) (leftTimeInMilliseconds / 1000));
            textViewShowTime.setText(String.format("%02d", seconds / 60) + ":" + String.format("%02d", seconds % 60));
        }

        @Override
        public void onFinish() {
            textViewShowTime.setVisibility(View.GONE);
            mProgressBar.setVisibility(View.GONE);
            Resend.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    /**
     * method of EventBus
     *
     * @param pusher this is parameter of onEventMainThread method
     */
    @SuppressWarnings("unused")
    public void onEventMainThread(Pusher pusher) {
        switch (pusher.getAction()) {
            case "countryCode":
                Code = "" + pusher.getData();
                code.setText(Code);
                break;
            case "countryName":
                Country = "" + pusher.getData();
                break;
        }
    }

}

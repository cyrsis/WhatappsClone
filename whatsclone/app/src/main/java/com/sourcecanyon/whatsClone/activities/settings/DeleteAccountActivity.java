package com.sourcecanyon.whatsClone.activities.settings;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.sourcecanyon.whatsClone.R;
import com.sourcecanyon.whatsClone.activities.main.WelcomeActivity;
import com.sourcecanyon.whatsClone.adapters.recyclerView.CountriesAdapter;
import com.sourcecanyon.whatsClone.adapters.recyclerView.TextWatcherAdapter;
import com.sourcecanyon.whatsClone.api.APIService;
import com.sourcecanyon.whatsClone.app.WhatsCloneApplication;
import com.sourcecanyon.whatsClone.helpers.AppHelper;
import com.sourcecanyon.whatsClone.helpers.PreferenceManager;
import com.sourcecanyon.whatsClone.models.CountriesModel;
import com.sourcecanyon.whatsClone.models.users.Pusher;
import com.sourcecanyon.whatsClone.services.apiServices.ContactsService;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import io.realm.Realm;

/**
 * Created by Abderrahim El imame on 8/17/16.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/bencherif_el
 */

public class DeleteAccountActivity extends AppCompatActivity {


    @Bind(R.id.app_bar)
    Toolbar toolbar;
    @Bind(R.id.delete_account_btn)
    TextView deleteAccount;
    @Bind(R.id.CounrtriesList)
    RecyclerView CountriesList;
    @Bind(R.id.numberPhone)
    TextInputEditText phoneNumberWrapper;
    @Bind(R.id.search_input)
    TextInputEditText searchInput;
    @Bind(R.id.clear_btn_search_view)
    ImageView clearBtn;

    @Bind(R.id.code)
    TextView code;

    private CountriesAdapter mCountriesAdapter;
    private String Code;
    private Realm realm;
    private ContactsService mContactsServiceDelete;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_account);
        ButterKnife.bind(this);
        realm = Realm.getDefaultInstance();
        EventBus.getDefault().register(this);
        setupToolbar();
        initializerView();
        APIService mApiServiceDelete = APIService.with(this);
        mContactsServiceDelete = new ContactsService(realm, this, mApiServiceDelete);

        deleteAccount.setOnClickListener(view -> verifyNumberPhone());

    }

    private void verifyNumberPhone() {
        String mobile = null;
        try {
            mobile = phoneNumberWrapper.getText().toString().trim();
        } catch (Exception e) {
            AppHelper.LogCat(" number mobile is null Exception DeleteAccountActivity " + e.getMessage());
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
                AppHelper.LogCat("number  error  NumberParseException  DeleteAccountActivity" + mobile);
                phoneNumberWrapper.setError(getString(R.string.enter_a_val_number));
            }
            if (phNumberProto != null) {
                boolean isValid = phoneUtil.isValidNumber(phNumberProto);
                if (isValid) {
                    String internationalFormat = phoneUtil.format(phNumberProto, PhoneNumberUtil.PhoneNumberFormat.E164);
                    deleteAccount(internationalFormat);
                } else {
                    phoneNumberWrapper.setError(getString(R.string.enter_a_val_number));
                }
            }
        } else {
            phoneNumberWrapper.setError(getString(R.string.enter_a_val_number));
        }
    }

    private void deleteAccount(String phone) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.alert_message_delete_account);
        builder.setPositiveButton(R.string.Yes, (dialog, whichButton) -> {
            AppHelper.showDialog(this, getString(R.string.deleting));
            mContactsServiceDelete.deleteAccount(phone).subscribe(statusResponse -> {
                if (statusResponse.isSuccess()) {
                    AppHelper.hideDialog();
                    AppHelper.CustomToast(this, statusResponse.getMessage());
                    PreferenceManager.setToken(null, this);
                    PreferenceManager.setID(0, this);
                    AppHelper.LaunchActivity(this, WelcomeActivity.class);
                    finish();
                } else {
                    AppHelper.hideDialog();
                    AppHelper.LogCat("delete  account " + statusResponse.getMessage());
                    AppHelper.CustomToast(this, statusResponse.getMessage());
                }
            }, throwable -> {
                AppHelper.hideDialog();
                AppHelper.LogCat("delete  account " + throwable.getMessage());
                AppHelper.CustomToast(this, getString(R.string.delete_account_failed_please_try_later));
            });
        });
        builder.setNegativeButton(R.string.No, (dialog, whichButton) -> {

        });

        builder.show();
    }

    private void initializerView() {

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
        code.setText(Code);
    }


    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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
                    final List<CountriesModel> list = gson.fromJson(AppHelper.loadJSONFromAsset(DeleteAccountActivity.this), new TypeToken<List<CountriesModel>>() {
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


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            overridePendingTransition(R.anim.push_up_in, R.anim.push_up_out);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.push_up_in, R.anim.push_up_out);
        finish();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        realm.close();
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

        }
    }
}

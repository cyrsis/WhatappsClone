package com.sourcecanyon.whatsClone.presenters;


import com.sourcecanyon.whatsClone.activities.settings.SettingsActivity;
import com.sourcecanyon.whatsClone.api.APIService;
import com.sourcecanyon.whatsClone.helpers.AppHelper;
import com.sourcecanyon.whatsClone.helpers.PreferenceManager;
import com.sourcecanyon.whatsClone.interfaces.Presenter;
import com.sourcecanyon.whatsClone.services.apiServices.ContactsService;

import io.realm.Realm;

/**
 * Created by Abderrahim El imame on 20/02/2016. Email : abderrahim.elimame@gmail.com
 */
public class SettingsPresenter implements Presenter {
    private final SettingsActivity view;
    private final Realm realm;

    public SettingsPresenter(SettingsActivity settingsActivity) {
        this.view = settingsActivity;
        this.realm = Realm.getDefaultInstance();

    }

    @Override
    public void onStart() {

    }

    @Override
    public void
    onCreate() {
        APIService mApiService = APIService.with(view);
        ContactsService mContactsService = new ContactsService(realm, view, mApiService);
        try {
            mContactsService.getContact(PreferenceManager.getID(view)).subscribe(view::ShowContact, throwable -> AppHelper.LogCat(throwable.getMessage()));
            mContactsService.getContactInfo(PreferenceManager.getID(view)).subscribe(view::ShowContact, throwable -> AppHelper.LogCat(throwable.getMessage()));
        } catch (Exception e) {
            AppHelper.LogCat("get contact settings Activity " + e.getMessage());
        }
    }


    @Override
    public void onPause() {

    }

    @Override
    public void onResume() {

    }

    @Override
    public void onDestroy() {
        realm.close();
    }

    @Override
    public void onLoadMore() {

    }

    @Override
    public void onRefresh() {

    }

    @Override
    public void onStop() {

    }
}
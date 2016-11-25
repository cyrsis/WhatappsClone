package com.sourcecanyon.whatsClone.presenters;


import com.sourcecanyon.whatsClone.activities.NewConversationContactsActivity;
import com.sourcecanyon.whatsClone.activities.messages.TransferMessageContactsActivity;
import com.sourcecanyon.whatsClone.api.APIService;
import com.sourcecanyon.whatsClone.helpers.AppHelper;
import com.sourcecanyon.whatsClone.interfaces.Presenter;
import com.sourcecanyon.whatsClone.services.apiServices.ContactsService;

import io.realm.Realm;

/**
 * Created by Abderrahim El imame on 20/02/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class SelectContactsPresenter implements Presenter {
    private NewConversationContactsActivity newConversationContactsActivity;
    private TransferMessageContactsActivity transferMessageContactsActivity;
    private Realm realm;
    private boolean selector;

    public SelectContactsPresenter(NewConversationContactsActivity newConversationContactsActivity) {
        this.newConversationContactsActivity = newConversationContactsActivity;
        this.realm = Realm.getDefaultInstance();
        selector = true;
    }

    public SelectContactsPresenter(TransferMessageContactsActivity transferMessageContactsActivity) {
        this.transferMessageContactsActivity = transferMessageContactsActivity;
        this.realm = Realm.getDefaultInstance();
        selector = false;
    }


    @Override
    public void onStart() {

    }

    @Override
    public void onCreate() {
        if (selector) {
            APIService mApiService = APIService.with(this.newConversationContactsActivity);
            ContactsService mContactsService = new ContactsService(realm, this.newConversationContactsActivity, mApiService);
            mContactsService.getLinkedContacts().subscribe(newConversationContactsActivity::ShowContacts, throwable -> {
                AppHelper.LogCat("Error contacts selector " + throwable.getMessage());
            });

        } else {
            APIService mApiService = APIService.with(this.transferMessageContactsActivity);
            ContactsService mContactsService = new ContactsService(realm, this.transferMessageContactsActivity, mApiService);
            mContactsService.getLinkedContacts().subscribe(transferMessageContactsActivity::ShowContacts, throwable -> {
                AppHelper.LogCat("Error contacts selector " + throwable.getMessage());
            });
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
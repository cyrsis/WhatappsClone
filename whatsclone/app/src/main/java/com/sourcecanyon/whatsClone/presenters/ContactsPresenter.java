package com.sourcecanyon.whatsClone.presenters;


import android.os.Handler;

import com.sourcecanyon.whatsClone.R;
import com.sourcecanyon.whatsClone.api.APIService;
import com.sourcecanyon.whatsClone.app.WhatsCloneApplication;
import com.sourcecanyon.whatsClone.fragments.ContactsFragment;
import com.sourcecanyon.whatsClone.helpers.AppHelper;
import com.sourcecanyon.whatsClone.helpers.PreferenceManager;
import com.sourcecanyon.whatsClone.helpers.UpdateSettings;
import com.sourcecanyon.whatsClone.helpers.UtilsPhone;
import com.sourcecanyon.whatsClone.interfaces.Presenter;
import com.sourcecanyon.whatsClone.models.users.contacts.ContactsModel;
import com.sourcecanyon.whatsClone.models.users.contacts.SyncContacts;
import com.sourcecanyon.whatsClone.services.apiServices.ContactsService;

import java.util.Date;
import java.util.List;

import io.realm.Realm;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Abderrahim El imame on 20/02/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class ContactsPresenter implements Presenter {
    private ContactsFragment contactsFragmentView;
    private Realm realm;
    private ContactsService mContactsService;
    private UpdateSettings updateSettings;
    private static final int UPDATE_MINUTES = 2 * 60 * 1000;

    public ContactsPresenter(ContactsFragment contactsFragment) {
        this.contactsFragmentView = contactsFragment;
        this.realm = Realm.getDefaultInstance();

    }


    @Override
    public void onStart() {
    }

    @Override
    public void onCreate() {
        Handler handler = new Handler();
        APIService mApiService = APIService.with(contactsFragmentView.getActivity());
        updateSettings = new UpdateSettings(contactsFragmentView.getActivity());
        mContactsService = new ContactsService(realm, contactsFragmentView.getActivity(), mApiService);
        mContactsService.getAllContacts().subscribe(contactsFragmentView::ShowContacts, contactsFragmentView::onErrorLoading, contactsFragmentView::onHideLoading);

        // Only update contacts on start if it hasn't been done in the past 2 minutes.
        if (new Date().getTime() - updateSettings.getLastContactsUpdate() > UPDATE_MINUTES) {
            rx.Observable.create(new rx.Observable.OnSubscribe<List<ContactsModel>>() {
                @Override
                public void call(Subscriber<? super List<ContactsModel>> subscriber) {
                    try {
                        List<ContactsModel> contactsList = UtilsPhone.GetPhoneContacts(contactsFragmentView.getActivity());
                        subscriber.onNext(contactsList);
                        subscriber.onCompleted();
                    } catch (Exception e) {
                        subscriber.onError(e);
                    }
                }
            }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread()).subscribe(contactsList -> {
                SyncContacts syncContacts = new SyncContacts();
                syncContacts.setUserID(PreferenceManager.getID(contactsFragmentView.getActivity()));
                syncContacts.setContactsModelList(contactsList);
                mContactsService.updateContacts(syncContacts)
                        .subscribe(contactsModelList -> {
                            contactsFragmentView.updateContacts(contactsModelList);
                            updateSettings.setLastContactsUpdate();
                        }, contactsFragmentView::onErrorLoading);
            }, contactsFragmentView::onErrorLoading);

        }
        handler.postDelayed(() -> {
            try {
                mContactsService.getContactInfo(PreferenceManager.getID(WhatsCloneApplication.getAppContext())).subscribe(contactsModel -> AppHelper.LogCat("info user log contacts"), AppHelper::LogCat);
                mContactsService.getUserStatus().subscribe(statusModels -> AppHelper.LogCat("status log contacts"), AppHelper::LogCat);
            } catch (Exception e) {
                AppHelper.LogCat("contact info Exception ");
            }
        }, 1500);
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
        contactsFragmentView.onShowLoading();
        rx.Observable.create(new rx.Observable.OnSubscribe<List<ContactsModel>>() {
            @Override
            public void call(Subscriber<? super List<ContactsModel>> subscriber) {
                try {
                    List<ContactsModel> contactsList = UtilsPhone.GetPhoneContacts(contactsFragmentView.getActivity());
                    subscriber.onNext(contactsList);
                    subscriber.onCompleted();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(contactsList -> {
            SyncContacts syncContacts = new SyncContacts();
            syncContacts.setUserID(PreferenceManager.getID(contactsFragmentView.getActivity()));
            syncContacts.setContactsModelList(contactsList);
            mContactsService.updateContacts(syncContacts).subscribe(contactsModelList -> {
                contactsFragmentView.updateContacts(contactsModelList);
                updateSettings.setLastContactsUpdate();
                AppHelper.CustomToast(contactsFragmentView.getActivity(), contactsFragmentView.getString(R.string.success_response_contacts));
            }, throwable -> {
                contactsFragmentView.onErrorLoading(throwable);
                AppHelper.CustomToast(contactsFragmentView.getActivity(), contactsFragmentView.getString(R.string.error_response_contacts));
            }, contactsFragmentView::onHideLoading);

        }, contactsFragmentView::onErrorLoading);
        mContactsService.getContactInfo(PreferenceManager.getID(WhatsCloneApplication.getAppContext())).subscribe(contactsModel -> AppHelper.LogCat(""), AppHelper::LogCat);

    }

    @Override
    public void onStop() {

    }
}
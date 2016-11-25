package com.sourcecanyon.whatsClone.presenters;


import com.sourcecanyon.whatsClone.activities.profile.ProfilePreviewActivity;
import com.sourcecanyon.whatsClone.api.APIService;
import com.sourcecanyon.whatsClone.helpers.AppHelper;
import com.sourcecanyon.whatsClone.interfaces.Presenter;
import com.sourcecanyon.whatsClone.services.apiServices.ContactsService;
import com.sourcecanyon.whatsClone.services.apiServices.GroupsService;

import io.realm.Realm;

/**
 * Created by Abderrahim El imame on 20/02/2016. Email : abderrahim.elimame@gmail.com
 */
public class ProfilePreviewPresenter implements Presenter {
    private final ProfilePreviewActivity view;
    private final Realm realm;

    public ProfilePreviewPresenter(ProfilePreviewActivity profilePreviewActivity) {
        this.view = profilePreviewActivity;
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
        GroupsService mGroupsService = new GroupsService(realm, view, mApiService);
        if (view.getIntent().hasExtra("userID")) {
            int userID = view.getIntent().getExtras().getInt("userID");
            try {
                mContactsService.getContact(userID).subscribe(view::ShowContact, view::onErrorLoading);
            } catch (Exception e) {
                AppHelper.LogCat("" + e.getMessage());
            }
            mContactsService.getContactInfo(userID).subscribe(view::ShowContact, view::onErrorLoading);
        }

        if (view.getIntent().hasExtra("groupID")) {
            int groupID = view.getIntent().getExtras().getInt("groupID");
            try {
                mGroupsService.getGroup(groupID).subscribe(view::ShowGroup,AppHelper::LogCat);
                mGroupsService.getGroupInfo(groupID).subscribe(view::ShowGroup, view::onErrorLoading);
            } catch (Exception e) {
                AppHelper.LogCat("Null group info " + e.getMessage());
            }
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
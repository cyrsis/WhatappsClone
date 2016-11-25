package com.sourcecanyon.whatsClone.services.apiServices;

import android.content.Context;

import com.sourcecanyon.whatsClone.api.APIContact;
import com.sourcecanyon.whatsClone.api.APIService;
import com.sourcecanyon.whatsClone.app.EndPoints;
import com.sourcecanyon.whatsClone.helpers.PreferenceManager;
import com.sourcecanyon.whatsClone.helpers.UtilsPhone;
import com.sourcecanyon.whatsClone.models.users.contacts.ContactsModel;
import com.sourcecanyon.whatsClone.models.users.contacts.SyncContacts;
import com.sourcecanyon.whatsClone.models.users.status.EditStatus;
import com.sourcecanyon.whatsClone.models.users.status.StatusModel;
import com.sourcecanyon.whatsClone.models.users.status.StatusResponse;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Abderrahim El imame on 20/02/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class ContactsService {
    private APIContact mApiContact;
    private Context mContext;
    private Realm realm;
    private APIService mApiService;

    public ContactsService(Realm realm, Context context, APIService mApiService) {
        this.mContext = context;
        this.realm = realm;
        this.mApiService = mApiService;

    }

    /**
     * method to initialize the api contact
     *
     * @return return value
     */
    private APIContact initializeApiContact() {
        if (mApiContact == null) {
            mApiContact = this.mApiService.RootService(APIContact.class, PreferenceManager.getToken(mContext), EndPoints.BASE_URL);
        }
        return mApiContact;
    }

    /**
     * method to get general user information
     *
     * @param userID this is parameter  getContact for method
     * @return return value
     */
    public Observable<ContactsModel> getContact(int userID) {
        return realm.where(ContactsModel.class).equalTo("id", userID).findFirst().asObservable();
    }

    /**
     * method to get all contacts
     *
     * @return return value
     */
    public Observable<RealmResults<ContactsModel>> getAllContacts() {
        return realm.where(ContactsModel.class).notEqualTo("id", PreferenceManager.getID(mContext)).equalTo("Exist", true).findAllSorted("Linked", Sort.DESCENDING, "username", Sort.ASCENDING).asObservable();
    }

    /**
     * method to get linked contacts
     *
     * @return return value
     */
    public Observable<RealmResults<ContactsModel>> getLinkedContacts() {
        return realm.where(ContactsModel.class).notEqualTo("id", PreferenceManager.getID(mContext)).equalTo("Exist", true).equalTo("Linked", true).findAllSorted("username", Sort.ASCENDING).asObservable();
    }

    /**
     * method to update(syncing) contacts
     *
     * @param ListString this is parameter for  updateContacts method
     * @return return value
     */
    public Observable<List<ContactsModel>> updateContacts(SyncContacts ListString) {
        return initializeApiContact().contacts(ListString)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(this::copyOrUpdateContacts);
    }

    /**
     * method to get user information from the server
     *
     * @param userID this is parameter for getContactInfo method
     * @return return  value
     */
    public Observable<ContactsModel> getContactInfo(int userID) {
        return initializeApiContact().contact(userID)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(this::copyOrUpdateContactInfo);
    }

    /**
     * method to get user status from server
     *
     * @return return value
     */
    public Observable<List<StatusModel>> getUserStatus() {
        return initializeApiContact().status()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(this::copyOrUpdateStatus);
    }

    /**
     * method to delete user status
     *
     * @param statusID this is parameter for deleteStatus method
     * @return return  value
     */
    public Observable<StatusResponse> deleteStatus(int statusID) {
        return initializeApiContact().deleteStatus(statusID)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(statusResponse -> statusResponse);
    }

    /**
     * method to delete all user status
     *
     * @return return value
     */
    public Observable<StatusResponse> deleteAllStatus() {
        return initializeApiContact().deleteAllStatus()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(statusResponse -> statusResponse);
    }

    /**
     * method to update user status
     *
     * @param statusID this is parameter for updateStatus method
     * @return return  value
     */
    public Observable<StatusResponse> updateStatus(int statusID) {
        return initializeApiContact().updateStatus(statusID)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(statusResponse -> statusResponse);
    }

    /**
     * method to edit user status
     *
     * @param newStatus this is the first parameter for editStatus method
     * @param statusID  this is the second parameter for editStatus method
     * @return return  value
     */
    public Observable<StatusResponse> editStatus(String newStatus, int statusID) {
        EditStatus editStatus = new EditStatus();
        editStatus.setNewStatus(newStatus);
        editStatus.setStatusID(statusID);
        return initializeApiContact().editStatus(editStatus)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(statusResponse -> statusResponse);
    }

    /**
     * method to edit username
     *
     * @param newName this is parameter for editUsername method
     * @return return  value
     */
    public Observable<StatusResponse> editUsername(String newName) {
        EditStatus editUsername = new EditStatus();
        editUsername.setNewStatus(newName);
        return initializeApiContact().editUsername(editUsername)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(statusResponse -> statusResponse);
    }

    /**
     * method to edit group name
     *
     * @param newName this is the first parameter for editGroupName method
     * @param groupID this is the second parameter for editGroupName method
     * @return return  value
     */
    public Observable<StatusResponse> editGroupName(String newName, int groupID) {
        EditStatus editGroupName = new EditStatus();
        editGroupName.setNewStatus(newName);
        editGroupName.setStatusID(groupID);
        return initializeApiContact().editGroupName(editGroupName)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(statusResponse -> statusResponse);
    }

    /**
     * method to get all status
     *
     * @return return value
     */
    public Observable<RealmResults<StatusModel>> getAllStatus() {
        return realm.where(StatusModel.class).equalTo("userID", PreferenceManager.getID(mContext)).findAllSorted("id", Sort.DESCENDING).asObservable();
    }

    /**
     * method to get current status fron local
     *
     * @return return value
     */
    public Observable<StatusModel> getCurrentStatusFromLocal() {
        return realm.where(StatusModel.class).equalTo("userID", PreferenceManager.getID(mContext)).equalTo("current", 1).findFirst().asObservable();
    }

    /**
     * method to delete user status
     *
     * @param phone this is parameter for deleteStatus method
     * @return return  value
     */
    public Observable<StatusResponse> deleteAccount(String phone) {
        return initializeApiContact().deleteAccount(phone)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(statusResponse -> statusResponse);
    }


    /**
     * method to copy or update user status
     *
     * @param statusModels this is parameter for copyOrUpdateStatus method
     * @return return  value
     */
    private List<StatusModel> copyOrUpdateStatus(List<StatusModel> statusModels) {
        realm.beginTransaction();
        List<StatusModel> statusModels1 = realm.copyToRealmOrUpdate(statusModels);
        realm.commitTransaction();
        return statusModels1;
    }

    /**
     * method to copy or update contacts list
     *
     * @param contacts this is parameter for copyOrUpdateContacts method
     * @return return  value
     */
    private List<ContactsModel> copyOrUpdateContacts(List<ContactsModel> contacts) {

        realm.beginTransaction();
        List<ContactsModel> realmContacts = realm.copyToRealmOrUpdate(contacts);
        realm.commitTransaction();
        return realmContacts;
    }

    /**
     * method to copy or update user information
     *
     * @param contactsModel this is parameter for copyOrUpdateContactInfo method
     * @return return  value
     */
    private ContactsModel copyOrUpdateContactInfo(ContactsModel contactsModel) {
        ContactsModel realmContacts;
        if (UtilsPhone.checkIfContactExist(mContext, contactsModel.getPhone())) {
            realm.beginTransaction();
            contactsModel.setExist(true);
            realmContacts = realm.copyToRealmOrUpdate(contactsModel);
            realm.commitTransaction();
        } else {
            realm.beginTransaction();
            contactsModel.setExist(false);
            realmContacts = realm.copyToRealmOrUpdate(contactsModel);
            realm.commitTransaction();

        }
        return realmContacts;
    }
}

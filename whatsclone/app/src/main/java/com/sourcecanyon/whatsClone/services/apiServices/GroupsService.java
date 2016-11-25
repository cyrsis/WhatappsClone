package com.sourcecanyon.whatsClone.services.apiServices;

import android.content.Context;

import com.sourcecanyon.whatsClone.api.APIGroups;
import com.sourcecanyon.whatsClone.api.APIService;
import com.sourcecanyon.whatsClone.app.EndPoints;
import com.sourcecanyon.whatsClone.helpers.PreferenceManager;
import com.sourcecanyon.whatsClone.models.groups.GroupResponse;
import com.sourcecanyon.whatsClone.models.groups.GroupsModel;
import com.sourcecanyon.whatsClone.models.groups.MembersGroupModel;
import com.sourcecanyon.whatsClone.models.messages.ConversationsModel;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmQuery;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Abderrahim El imame on 20/02/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class GroupsService {
    private APIGroups mApiGroups;
    private Context mContext;
    private Realm realm;
    private APIService mApiService;

    public GroupsService(Realm realm, Context context, APIService mApiService) {
        this.mContext = context;
        this.realm = realm;
        this.mApiService = mApiService;

    }

    /**
     * method to initialize the api groups
     * @return return value
     */
    private APIGroups initializeApiGroups() {
        if (mApiGroups == null) {
            mApiGroups = this.mApiService.RootService(APIGroups.class, PreferenceManager.getToken(mContext), EndPoints.BASE_URL);
        }
        return mApiGroups;
    }

    /**
     * method to get all groups list
     * @return return value
     */
    public Observable<List<GroupsModel>> getGroups() {
        List<GroupsModel> groups = realm.where(GroupsModel.class).findAll();
        return Observable.just(groups);
    }

    /**
     * method to update groups
     * @return return value
     */
    public Observable<List<GroupsModel>> updateGroups() {
        return initializeApiGroups().groups()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(this::copyOrUpdateGroups);
    }

    /**
     * method to get single group information
     * @param groupID this is parameter for  getGroupInfo method
     * @return return value
     */
    public Observable<GroupsModel> getGroupInfo(int groupID) {
        return initializeApiGroups().getGroup(groupID)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(this::copyOrUpdateGroup);
    }

    /**
     * method to get group information from local
     * @param groupID this is parameter for getGroup method
     * @return return value
     */
    public Observable<GroupsModel> getGroup(int groupID) {
        return realm.where(GroupsModel.class).equalTo("id", groupID).findFirst().asObservable();
    }

    /**
     * method to copy or update groups list
     * @param groups this is parameter for copyOrUpdateGroups method
     * @return return value
     */
    private List<GroupsModel> copyOrUpdateGroups(List<GroupsModel> groups) {
        realm.beginTransaction();
        List<GroupsModel> realmGroups = realm.copyToRealmOrUpdate(groups);
        realm.commitTransaction();
        return realmGroups;
    }

    /**
     * methid to copy or update a single group
     * @param group this is parameter for copyOrUpdateGroup method
     * @return return value
     */
    private GroupsModel copyOrUpdateGroup(GroupsModel group) {
        realm.beginTransaction();
        GroupsModel groupsModel = realm.where(GroupsModel.class).equalTo("id",group.getId()).findFirst();
        GroupsModel realmGroups = realm.copyToRealmOrUpdate(groupsModel);
        realm.commitTransaction();
        return realmGroups;
    }

    /**
     * method to check if a group conversation exist
     * @param groupID this is parameter for checkIfGroupConversationExist method
     * @return return value
     */
    public boolean checkIfGroupConversationExist(int groupID) {
        RealmQuery<ConversationsModel> query = realm.where(ConversationsModel.class).equalTo("groupID", groupID);
        return query.count() == 0 ? false : true;
    }



    /**
     * methods for get group members
     * @param groupID this is parameter for getGroupMembers method
     * @return return value
     */
    public Observable<List<MembersGroupModel>> getGroupMembers(int groupID) {
        GroupsModel groupsModel = realm.where(GroupsModel.class).equalTo("id", groupID).findFirst();
        return Observable.just(groupsModel.getMembers());
    }

    /**
     * method to update group members
     * @param groupID this is parameter for updateGroupMembers method
     * @return return value
     */
    public Observable<List<MembersGroupModel>> updateGroupMembers(int groupID) {
        return initializeApiGroups().groupMembers(groupID)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(this::copyOrUpdateGroupMembers);
    }

    /**
     * method to copy or update group members
     * @param groupMembers this is parameter for copyOrUpdateGroupMembers method
     * @return return value
     */
    private List<MembersGroupModel> copyOrUpdateGroupMembers(List<MembersGroupModel> groupMembers) {
        realm.beginTransaction();
        List<MembersGroupModel> realmGroupMembers = realm.copyToRealmOrUpdate(groupMembers);
        realm.commitTransaction();
        return realmGroupMembers;
    }

    /**
     * method to exit a group
     * @param groupID this is parameter for ExitGroup method
     * @return return value
     */
    public Observable<GroupResponse> ExitGroup(int groupID) {
        return initializeApiGroups().exitGroup(groupID)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }


    /**
     * method to delete group
     * @param groupID this is parameter for DeleteGroup method
     * @return return value
     */
    public Observable<GroupResponse> DeleteGroup(int groupID) {
        return initializeApiGroups().deleteGroup(groupID)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }
}

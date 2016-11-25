package com.sourcecanyon.whatsClone.services.apiServices;

import com.sourcecanyon.whatsClone.helpers.AppHelper;
import com.sourcecanyon.whatsClone.models.groups.GroupsModel;
import com.sourcecanyon.whatsClone.models.messages.ConversationsModel;
import com.sourcecanyon.whatsClone.models.messages.MessagesModel;
import com.sourcecanyon.whatsClone.models.users.contacts.ContactsModel;

import java.util.List;

import io.realm.Realm;
import io.realm.Sort;
import rx.Observable;

/**
 * Created by Abderrahim El imame on 20/02/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class MessagesService {

    private Realm realm;

    public MessagesService(Realm realm) {
        this.realm = realm;

    }

    /**
     * method to get all conversation messages
     *
     * @param conversationID this is the first parameter for getConversation method
     * @param recipientID    this is the second parameter for getConversation method
     * @param senderID       this is the thirded parameter for getConversation method
     * @return return value
     */
    public Observable<List<MessagesModel>> getConversation(int conversationID, int recipientID, int senderID) {

        List<MessagesModel> messages = null;
        if (conversationID == 0) {
            try {
                ConversationsModel conversationsModel = realm.where(ConversationsModel.class)
                        .beginGroup()
                        .equalTo("RecipientID", recipientID)
                        .or()
                        .equalTo("RecipientID", senderID)
                        .endGroup().findAll().first();
                messages = realm.where(MessagesModel.class)
                        .equalTo("conversationID", conversationsModel.getId())
                        .equalTo("isGroup", false)
                        .findAllSorted("id", Sort.ASCENDING);

            } catch (Exception e) {
                AppHelper.LogCat(e.getMessage());
            }
        } else {
            messages = realm.where(MessagesModel.class)
                    .equalTo("conversationID", conversationID)
                    .equalTo("isGroup", false)
                    .findAllSorted("id", Sort.ASCENDING);
        }
        return Observable.just(messages);
    }

    /**
     * method to get messages list from local
     *
     * @param conversationID this is parameter for getConversation method
     * @return return value
     */
    public Observable<List<MessagesModel>> getConversation(int conversationID) {
        List<MessagesModel> messages = realm.where(MessagesModel.class).equalTo("conversationID", conversationID).equalTo("isGroup", true).findAllSorted("id", Sort.ASCENDING);
        return Observable.just(messages);
    }

    /**
     * method to get user media for profile
     *
     * @param recipientID this is the first parameter for getUserMedia method
     * @param senderID    this is the second parameter for getUserMedia method
     * @return return value
     */
    public Observable<List<MessagesModel>> getUserMedia(int recipientID, int senderID) {
        List<MessagesModel> messages;
        ConversationsModel conversationsModel = realm.where(ConversationsModel.class)
                .beginGroup()
                .equalTo("RecipientID", recipientID)
                .or()
                .equalTo("RecipientID", senderID)
                .endGroup()
                .findAll()
                .first();

        messages = realm.where(MessagesModel.class)
                .beginGroup()
                .notEqualTo("imageFile", "null")
                .or()
                .notEqualTo("videoFile", "null")
                .or()
                .notEqualTo("audioFile", "null")
                .or()
                .notEqualTo("documentFile", "null")
                .endGroup()
                .equalTo("isFileUpload", true)
                .equalTo("isFileDownLoad", true)
                .equalTo("conversationID", conversationsModel.getId())
                .equalTo("isGroup", false)
                .findAllSorted("id", Sort.ASCENDING);
        return Observable.just(messages);
    }


    /**
     * method to get group media for profile
     *
     * @param groupID  this is the first parameter for getGroupMedia method
     * @param senderID this is the second parameter for getGroupMedia method
     * @return return value
     */
    public Observable<List<MessagesModel>> getGroupMedia(int groupID, int senderID) {
        List<MessagesModel> messages;

        messages = realm.where(MessagesModel.class)
                .beginGroup()
                .notEqualTo("imageFile", "null")
                .or()
                .notEqualTo("videoFile", "null")
                .or()
                .notEqualTo("audioFile", "null")
                .or()
                .notEqualTo("documentFile", "null")
                .endGroup()
                .equalTo("isFileUpload", true)
                .equalTo("isFileDownLoad", true)
                .equalTo("groupID", groupID)
                .equalTo("isGroup", true)
                .findAllSorted("id", Sort.ASCENDING);
        return Observable.just(messages);
    }

    /**
     * method to a single contact information
     *
     * @param ContactID this is parameter for getContact method
     * @return return value
     */
    public Observable<ContactsModel> getContact(int ContactID) {
        ContactsModel contactsModel = realm.where(ContactsModel.class).equalTo("id", ContactID).findFirst();
        return Observable.just(contactsModel);
    }

    /**
     * method to get single group information
     *
     * @param GroupID this is parameter for getGroupInfo method
     * @return return value
     */
    public Observable<GroupsModel> getGroupInfo(int GroupID) {
        GroupsModel groupsModel = realm.where(GroupsModel.class).equalTo("id", GroupID).findFirst();
        return Observable.just(groupsModel);
    }
}

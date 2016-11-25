package com.sourcecanyon.whatsClone.presenters;

import com.sourcecanyon.whatsClone.api.APIService;
import com.sourcecanyon.whatsClone.app.AppConstants;
import com.sourcecanyon.whatsClone.fragments.ConversationsFragment;
import com.sourcecanyon.whatsClone.helpers.AppHelper;
import com.sourcecanyon.whatsClone.helpers.Files.FilesManager;
import com.sourcecanyon.whatsClone.interfaces.Presenter;
import com.sourcecanyon.whatsClone.models.groups.GroupsModel;
import com.sourcecanyon.whatsClone.models.groups.MembersGroupModel;
import com.sourcecanyon.whatsClone.models.messages.ConversationsModel;
import com.sourcecanyon.whatsClone.models.messages.MessagesModel;
import com.sourcecanyon.whatsClone.models.users.Pusher;
import com.sourcecanyon.whatsClone.services.apiServices.ConversationsService;
import com.sourcecanyon.whatsClone.services.apiServices.GroupsService;

import java.util.List;

import de.greenrobot.event.EventBus;
import io.realm.Realm;
import io.realm.RealmList;

/**
 * Created by Abderrahim El imame on 20/02/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class ConversationsPresenter implements Presenter {
    private final ConversationsFragment conversationsFragmentView;
    private final Realm realm;
    private ConversationsService mConversationsService;
    private GroupsService mGroupsService;


    public ConversationsPresenter(ConversationsFragment conversationsFragment) {
        this.conversationsFragmentView = conversationsFragment;
        this.realm = Realm.getDefaultInstance();
    }


    @Override
    public void onStart() {
    }

    @Override
    public void onCreate() {
        if (!EventBus.getDefault().isRegistered(conversationsFragmentView))
            EventBus.getDefault().register(conversationsFragmentView);
        APIService mApiService = APIService.with(conversationsFragmentView.getContext());
        mConversationsService = new ConversationsService(realm);
        mGroupsService = new GroupsService(realm, conversationsFragmentView.getContext(), mApiService);
        loadDataLocal();
    }

    private void loadDataLocal() {

        mGroupsService.updateGroups().subscribe(this::checkGroups, throwable -> AppHelper.LogCat("Groups list ConversationsPresenter " + throwable.getMessage()));
        mConversationsService.getConversations().subscribe(conversationsFragmentView::ShowConversation, conversationsFragmentView::onErrorLoading, conversationsFragmentView::onHideLoading);

    }

    private void checkGroups(List<GroupsModel> groupsModels) {
        for (GroupsModel groupsModel1 : groupsModels) {
            if (!FilesManager.isFileImagesGroupExists(FilesManager.getGroupImage(String.valueOf(groupsModel1.getId()), groupsModel1.getGroupName()))) {
                FilesManager.downloadFilesToDevice(conversationsFragmentView.getActivity(), groupsModel1.getGroupImage(), String.valueOf(groupsModel1.getId()), groupsModel1.getGroupName(), "group");
            }
            if (!mGroupsService.checkIfGroupConversationExist(groupsModel1.getId())) {
                realm.executeTransaction(realm1 -> {
                    int lastConversationID = 1;
                    int UnreadMessageCounter = 0;
                    int lastID = 1;
                    try {
                        ConversationsModel conversationsModel = realm1.where(ConversationsModel.class).findAll().last();
                        lastConversationID = conversationsModel.getId();
                        lastConversationID++;

                        UnreadMessageCounter = Integer.parseInt(conversationsModel.getUnreadMessageCounter());
                        UnreadMessageCounter++;

                        List<MessagesModel> messagesModel1 = realm1.where(MessagesModel.class).findAll();
                        lastID = messagesModel1.size();
                        lastID++;

                        AppHelper.LogCat("last ID group message" + lastID);

                    } catch (Exception e) {
                        AppHelper.LogCat("last conversation  ID group if conversation id = 0 Exception" + e.getMessage());
                        lastConversationID = 1;
                    }
                    ConversationsModel conversationsModel = new ConversationsModel();
                    RealmList<MessagesModel> messagesModelRealmList = new RealmList<MessagesModel>();
                    MessagesModel messagesModel = null;
                    for (MembersGroupModel membersGroupModel1 : groupsModel1.getMembers()) {
                        messagesModel = new MessagesModel();
                        messagesModel.setId(lastID);
                        messagesModel.setDate(groupsModel1.getCreatedDate());

                        messagesModel.setSenderID(groupsModel1.getCreatorID());
                        messagesModel.setRecipientID(0);
                        messagesModel.setStatus(AppConstants.IS_SEEN);
                        messagesModel.setUsername(groupsModel1.getCreator());
                        messagesModel.setGroup(true);
                        messagesModel.setPhone(membersGroupModel1.getPhone());
                        messagesModel.setImageFile("null");
                        messagesModel.setVideoFile("null");
                        messagesModel.setAudioFile("null");
                        messagesModel.setDocumentFile("null");
                        messagesModel.setVideoThumbnailFile("null");
                        messagesModel.setFileDownLoad(true);
                        messagesModel.setFileUpload(true);
                        messagesModel.setGroupID(groupsModel1.getId());
                        messagesModel.setConversationID(lastConversationID);
                        if (!membersGroupModel1.isLeft())
                            messagesModel.setMessage("FK");
                        else
                            messagesModel.setMessage("LT");

                        if (!membersGroupModel1.isLeft())
                            conversationsModel.setLastMessage("FK");
                        else
                            conversationsModel.setLastMessage("LT");
                        messagesModelRealmList.add(messagesModel);
                    }

                    conversationsModel.setLastMessageId(lastID);
                    conversationsModel.setRecipientID(0);
                    conversationsModel.setCreatorID(groupsModel1.getCreatorID());
                    conversationsModel.setRecipientUsername(groupsModel1.getGroupName());
                    conversationsModel.setRecipientImage(groupsModel1.getGroupImage());
                    conversationsModel.setGroupID(groupsModel1.getId());
                    conversationsModel.setMessageDate(groupsModel1.getCreatedDate());
                    conversationsModel.setId(lastConversationID);
                    conversationsModel.setGroup(true);
                    conversationsModel.setMessages(messagesModelRealmList);
                    conversationsModel.setStatus(AppConstants.IS_SEEN);
                    conversationsModel.setUnreadMessageCounter(String.valueOf(UnreadMessageCounter));
                    conversationsModel.setCreatedOnline(true);
                    realm1.copyToRealmOrUpdate(conversationsModel);

                });
                EventBus.getDefault().post(new Pusher("createGroup"));
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
        EventBus.getDefault().unregister(conversationsFragmentView);
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


    public void onEvent(Pusher pusher) {
        if (pusher.getAction().equals("new_message")
                || pusher.getAction().equals("new_message_sent")
                || pusher.getAction().equals("messages_seen")
                || pusher.getAction().equals("messages_delivered")
                || pusher.getAction().equals("deleteConversation")) {

            loadDataLocal();
        } else if (pusher.getAction().equals("createGroup")
                || pusher.getAction().equals("deleteGroup")
                || pusher.getAction().equals("exitGroup")) {
            loadDataLocal();
        }
    }
}

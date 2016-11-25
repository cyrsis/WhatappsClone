package com.sourcecanyon.whatsClone.models.messages;

import com.google.gson.annotations.Expose;
import com.sourcecanyon.whatsClone.models.groups.MembersGroupModel;
import com.sourcecanyon.whatsClone.models.users.contacts.ContactsModel;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Abderrahim El imame on 20/02/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class ConversationsModel extends RealmObject {
    @PrimaryKey
    @Expose
    private int id;
    @Expose
    private int Status;
    @Expose
    private String MessageDate;
    @Expose
    private String LastMessage;
    @Expose
    private int LastMessageId;
    @Expose
    private String RecipientPhone;
    @Expose
    private String RecipientUsername;// or GroupName
    @Expose
    private String RecipientImage;//or GroupImage
    @Expose
    private String UnreadMessageCounter;
    @Expose
    private int RecipientID;
    @Expose
    private RealmList<MessagesModel> Messages;

    /**
     * field of groups
     */
    @Expose
    private int CreatorID;
    @Expose
    private boolean isGroup;
    @Expose
    private boolean createdOnline;
    @Expose
    private int groupID; //groupID

    public int getGroupID() {
        return groupID;
    }

    public void setGroupID(int groupID) {
        this.groupID = groupID;
    }
    public boolean getCreatedOnline() {
        return createdOnline;
    }

    public void setCreatedOnline(boolean createdOnline) {
        this.createdOnline = createdOnline;
    }


    public boolean isGroup() {
        return isGroup;
    }

    public void setGroup(boolean group) {
        isGroup = group;
    }





    public int getCreatorID() {
        return CreatorID;
    }

    public void setCreatorID(int creatorID) {
        CreatorID = creatorID;
    }


    public int getLastMessageId() {
        return LastMessageId;
    }

    public void setLastMessageId(int lastMessageId) {
        LastMessageId = lastMessageId;
    }

    public ConversationsModel() {

    }

    public RealmList<MessagesModel> getMessages() {
        return Messages;
    }

    public void setMessages(RealmList<MessagesModel> messages) {
        Messages = messages;
    }



    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRecipientID() {
        return RecipientID;
    }

    public void setRecipientID(int recipientID) {
        RecipientID = recipientID;
    }

    public String getUnreadMessageCounter() {
        return UnreadMessageCounter;
    }

    public void setUnreadMessageCounter(String unreadMessageCounter) {
        UnreadMessageCounter = unreadMessageCounter;
    }


    public String getRecipientImage() {
        return RecipientImage;
    }

    public void setRecipientImage(String recipientImage) {
        RecipientImage = recipientImage;
    }

    public String getRecipientUsername() {
        return RecipientUsername;
    }

    public void setRecipientUsername(String recipientUsername) {
        RecipientUsername = recipientUsername;
    }

    public String getRecipientPhone() {
        return RecipientPhone;
    }

    public void setRecipientPhone(String recipientPhone) {
        RecipientPhone = recipientPhone;
    }

    public String getLastMessage() {
        return LastMessage;
    }

    public void setLastMessage(String lastMessage) {
        LastMessage = lastMessage;
    }

    public String getMessageDate() {
        return MessageDate;
    }

    public void setMessageDate(String messageDate) {
        MessageDate = messageDate;
    }

    public int getStatus() {
        return Status;
    }

    public void setStatus(int status) {
        Status = status;
    }


}

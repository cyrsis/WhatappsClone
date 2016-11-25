package com.sourcecanyon.whatsClone.models.groups;

import com.google.gson.annotations.Expose;
import com.sourcecanyon.whatsClone.models.messages.MessagesModel;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Abderrahim El imame on 20/02/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class GroupsModel extends RealmObject {
    @PrimaryKey
    @Expose
    private int id;
    @Expose
    private String CreatedDate;
    @Expose
    private int Status;
    @Expose
    private String GroupName;
    @Expose
    private String GroupImage;
    @Expose
    private int CreatorID;
    @Expose
    private String Creator;
    @Expose
    private RealmList<MembersGroupModel> Members;

    public RealmList<MembersGroupModel> getMembers() {
        return Members;
    }

    public void setMembers(RealmList<MembersGroupModel> members) {
        Members = members;
    }

    public int getCreatorID() {
        return CreatorID;
    }

    public void setCreatorID(int creatorID) {
        CreatorID = creatorID;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public String getCreator() {
        return Creator;
    }

    public void setCreator(String creator) {
        Creator = creator;
    }

    public String getGroupName() {
        return GroupName;
    }

    public void setGroupName(String groupName) {
        GroupName = groupName;
    }

    public String getGroupImage() {
        return GroupImage;
    }

    public void setGroupImage(String groupImage) {
        GroupImage = groupImage;
    }


     public String getCreatedDate() {
        return CreatedDate;
    }

    public void setCreatedDate(String createdDate) {
        CreatedDate = createdDate;
    }

    public int getStatus() {
        return Status;
    }

    public void setStatus(int status) {
        Status = status;
    }
}

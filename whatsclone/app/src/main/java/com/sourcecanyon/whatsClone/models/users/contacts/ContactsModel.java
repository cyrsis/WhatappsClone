package com.sourcecanyon.whatsClone.models.users.contacts;

import com.google.gson.annotations.Expose;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Abderrahim El imame on 20/02/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class ContactsModel extends RealmObject {
    @PrimaryKey
    @Expose
    private int id;
    @Expose
    private int contactID;
    @Expose
    private String username;
    @Expose
    private String phone;
    @Expose
    private boolean Linked;
    @Expose
    private boolean Exist;
    @Expose
    private String image;
    @Expose
    private String status;
    @Expose
    private String status_date;



    public ContactsModel() {

    }

    public ContactsModel(int contactID, String username, String phone) {
        this.contactID = contactID;
        this.username = username;
        this.phone = phone;
    }


    public boolean isExist() {
        return Exist;
    }

    public void setExist(boolean exist) {
        Exist = exist;
    }


    public int getContactID() {
        return contactID;
    }

    public void setContactID(int contactID) {
        this.contactID = contactID;
    }


    public String getStatus_date() {
        return status_date;
    }

    public void setStatus_date(String status_date) {
        this.status_date = status_date;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public boolean isLinked() {
        return Linked;
    }

    public void setLinked(boolean linked) {
        Linked = linked;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}

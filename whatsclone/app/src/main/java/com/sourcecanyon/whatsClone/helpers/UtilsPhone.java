package com.sourcecanyon.whatsClone.helpers;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.sourcecanyon.whatsClone.interfaces.ContactMobileNumbQuery;
import com.sourcecanyon.whatsClone.models.users.contacts.ContactsModel;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by Abderrahim El imame on 03/03/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class UtilsPhone {


    private static ArrayList<ContactsModel> mListContacts = new ArrayList<ContactsModel>();

    /**
     * method to retrieve all contacts from the book
     *
     * @param mContext this is  parameter for GetPhoneContacts  method
     * @return return value
     */
    public static ArrayList<ContactsModel> GetPhoneContacts(Activity mContext) {

        if (AppHelper.checkPermission(mContext, Manifest.permission.READ_CONTACTS)) {
            AppHelper.LogCat("Read contact data permission already granted.");
        } else {
            AppHelper.LogCat("Please request Read contact data permission.");
            AppHelper.requestPermission(mContext, Manifest.permission.READ_CONTACTS);
        }


        if (AppHelper.checkPermission(mContext, Manifest.permission.WRITE_CONTACTS)) {
            AppHelper.LogCat("write contact data permission already granted.");
        } else {
            AppHelper.LogCat("Please request write contact data permission.");
            AppHelper.requestPermission(mContext, Manifest.permission.WRITE_CONTACTS);
        }

        ContentResolver cr = mContext.getContentResolver();
        Cursor cur = cr.query(ContactMobileNumbQuery.CONTENT_URI, ContactMobileNumbQuery.PROJECTION, ContactMobileNumbQuery.SELECTION, null, ContactMobileNumbQuery.SORT_ORDER);
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        if (cur != null) {
            if (cur.getCount() > 0) {
                while (cur.moveToNext()) {
                    ContactsModel data = new ContactsModel();
                    String name = cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    String phoneNumber = cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    String id = cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID));
                    if (name.contains("\\s+")) {
                        String[] nameArr = name.split("\\s+");
                        data.setUsername(nameArr[0]);
                        data.setUsername(nameArr[1]);
                        //  AppHelper.LogCat("Fname --> " + nameArr[0]);
                        //  AppHelper.LogCat("Lname --> " + nameArr[1]);
                    } else {
                        data.setUsername(name);
                        // data.setUsername(" ");
                        //  AppHelper.LogCat("name" + name);
                    }
                    Phonenumber.PhoneNumber phNumberProto = null;
                    String countryCode = Locale.getDefault().getCountry();
                    try {
                        phNumberProto = phoneUtil.parse(phoneNumber, countryCode);
                    } catch (NumberParseException e) {
                        e.printStackTrace();
                        // AppHelper.LogCat("number parsing err --> " + phoneNumber);
                    }
                    if (phNumberProto != null) {
                        boolean isValid = phoneUtil.isValidNumber(phNumberProto);
                        if (isValid) {
                            String internationalFormat = phoneUtil.format(phNumberProto, PhoneNumberUtil.PhoneNumberFormat.E164);
                            data.setPhone(internationalFormat.trim());
                            data.setContactID(Integer.parseInt(id));
                            //AppHelper.LogCat("phone --> " + internationalFormat);
                            // AppHelper.LogCat("id --> " + id);
                            int flag = 0;
                            if (mListContacts.size() == 0) {
                                mListContacts.add(data);
                            }
                            for (int i = 0; i < mListContacts.size(); i++) {

                                if (!mListContacts.get(i).getPhone().trim().equals(internationalFormat)) {
                                    flag = 1;

                                } else {
                                    flag = 0;
                                    break;
                                }
                            }
                            if (flag == 1) {
                                mListContacts.add(data);
                            }

                        } else {
                            // AppHelper.LogCat("invalid phone --> ");
                        }
                    }

                }
                cur.close();
            }
        }
        return mListContacts;
    }

    /**
     * method to get contact ID
     *
     * @param mContext this is the first parameter for getContactID  method
     * @param phone    this is the second parameter for getContactID  method
     * @return return value
     */
    public static long getContactID(Activity mContext, String phone) {
        if (AppHelper.checkPermission(mContext, Manifest.permission.READ_CONTACTS)) {
            AppHelper.LogCat("Read contact data permission already granted.");
        } else {
            AppHelper.LogCat("Please request Read contact data permission.");
            AppHelper.requestPermission(mContext, Manifest.permission.READ_CONTACTS);
        }


        if (AppHelper.checkPermission(mContext, Manifest.permission.WRITE_CONTACTS)) {
            AppHelper.LogCat("write contact data permission already granted.");
        } else {
            AppHelper.LogCat("Please request write contact data permission.");
            AppHelper.requestPermission(mContext, Manifest.permission.WRITE_CONTACTS);
        }
        // CONTENT_FILTER_URI allow to search contact by phone number
        Uri lookupUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phone));
        // This query will return NAME and ID of contact, associated with phone //number.
        Cursor mcursor = mContext.getContentResolver().query(lookupUri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup._ID}, null, null, null);
        //Now retrieve _ID from query result
        long idPhone = 0;
        try {
            if (mcursor != null) {
                if (mcursor.moveToFirst()) {
                    idPhone = Long.valueOf(mcursor.getString(mcursor.getColumnIndex(ContactsContract.PhoneLookup._ID)));
                }
            }
        } finally {
            mcursor.close();
        }
        return idPhone;
    }


    /**
     * method to check for contact name
     *
     * @param mContext this is the first parameter for getContactName  method
     * @param phone    this is the second parameter for getContactName  method
     * @return return value
     */
    public static String getContactName(Context mContext, String phone) {
        // CONTENT_FILTER_URI allow to search contact by phone number
        Uri lookupUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phone));
        // This query will return NAME and ID of contact, associated with phone //number.
        Cursor mcursor = mContext.getContentResolver().query(lookupUri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup._ID}, null, null, null);
        //Now retrieve _ID from query result
        String name = null;
        try {
            if (mcursor != null) {
                if (mcursor.moveToFirst()) {
                    name = mcursor.getString(mcursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
                }
            }
        } finally {
            mcursor.close();
        }
        return name;
    }

    /**
     * method to check if user contact exist
     *
     * @param mContext this is the first parameter for checkIfContactExist  method
     * @param phone    this is the second parameter for checkIfContactExist  method
     * @return return value
     */
    public static boolean checkIfContactExist(Context mContext, String phone) {
        // CONTENT_FILTER_URI allow to search contact by phone number
        Uri lookupUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phone));
        // This query will return NAME and ID of contact, associated with phone //number.
        Cursor mcursor = mContext.getContentResolver().query(lookupUri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup._ID}, null, null, null);
        //Now retrieve _ID from query result
        String name = null;
        try {
            if (mcursor != null) {
                if (mcursor.moveToFirst()) {
                    name = mcursor.getString(mcursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
                }
            }
        } finally {
            mcursor.close();
        }

        if (name != null)
            return true;
        else
            return false;
    }
}

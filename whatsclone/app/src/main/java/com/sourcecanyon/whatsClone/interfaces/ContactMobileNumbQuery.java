package com.sourcecanyon.whatsClone.interfaces;

import android.net.Uri;
import android.provider.ContactsContract;

/**
 * Created by Abderrahim El imame on 29/02/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public interface ContactMobileNumbQuery {

    final static int QUERY_ID = 1;

    //A Content Uri for Phone table
    final static Uri CONTENT_URI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;

    //The search or filter query Uri
    final static Uri FILTER_URI = ContactsContract.CommonDataKinds.Phone.CONTENT_FILTER_URI;

    // The selection clause for the CursorLoader query. The search criteria defined here
    // restrict results to contacts that have a phone number and display name.
    // Notice that the search on the string provided by the user is implemented by appending
    // the search string to CONTENT_FILTER_URI.
    //final static String SELECTION = ContactsContract.CommonDataKinds.Phone.HAS_PHONE_NUMBER + "=1" + " AND " + ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + "<>''";
    final static String SELECTION = ContactsContract.CommonDataKinds.Phone.IN_VISIBLE_GROUP + " = '" + ("1") + "'" + " AND " + ContactsContract.CommonDataKinds.Phone.HAS_PHONE_NUMBER + "=1" + " AND " + ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + "<>''";

    // The desired sort order for the returned Cursor - Order by DISPLAY_NAME_PRIMARY
    //in Ascending with case insensitively
    final static String SORT_ORDER = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY + " COLLATE NOCASE ASC";

    // The projection for the CursorLoader query. This is a list of columns that the Contacts
    // Provider should return in the Cursor.
    final static String[] PROJECTION = {

            // The contact's row id
            ContactsContract.CommonDataKinds.Phone._ID,

            // the Contacts table contains DISPLAY_NAME_PRIMARY, which either contains
            // the contact's displayable name or some other useful identifier such as an
            // email address.
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,

            // A pointer to the contact that is guaranteed to be more permanent than _ID. Given
            // a contact's current _ID value and LOOKUP_KEY, the Contacts Provider can generate
            // a "permanent" contact URI.
            ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY,

            //Phone number of the contact
            ContactsContract.CommonDataKinds.Phone.NUMBER,
    };
    // The query column numbers which map to each value in the projection
    final static int ID = 0;
    final static int DISPLAY_NAME = 1;
    final static int LOOKUP_KEY = 2;
    final static int NUMBER = 3;
}

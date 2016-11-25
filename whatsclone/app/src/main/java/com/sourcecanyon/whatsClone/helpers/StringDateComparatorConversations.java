package com.sourcecanyon.whatsClone.helpers;

import com.sourcecanyon.whatsClone.models.messages.ConversationsModel;
import com.sourcecanyon.whatsClone.models.messages.MessagesModel;

import java.util.Comparator;

/**
 * Created by Abderrahim El imame on 6/24/16.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/bencherif_el
 */

public class StringDateComparatorConversations implements Comparator<ConversationsModel> {

    public int compare(ConversationsModel app1, ConversationsModel app2) {

        String date1 = app1.getMessageDate();
        String date2 = app2.getMessageDate();
        return date2.compareTo(date1);
    }
    /* try {
            Collections.sort(messagesModelList, new StringDateComparatorMessages());
        } catch (Exception e) {
            AppHelper.LogCat("messages compare " + e.getMessage());
        }*/
}


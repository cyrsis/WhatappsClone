package com.sourcecanyon.whatsClone.services.apiServices;

import com.sourcecanyon.whatsClone.models.messages.ConversationsModel;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import rx.Observable;

/**
 * Created by Abderrahim El imame on 20/04/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class ConversationsService {
    private Realm realm;

    public ConversationsService(Realm realm) {
        this.realm = realm;

    }

    /**
     * method to get Conversations list
     *
     * @return return value
     */
    public Observable<RealmResults<ConversationsModel>> getConversations() {
        return realm.where(ConversationsModel.class).findAllSorted("LastMessageId", Sort.DESCENDING).asObservable();
    }


}

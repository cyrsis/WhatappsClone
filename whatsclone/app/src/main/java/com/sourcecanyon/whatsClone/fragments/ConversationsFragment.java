package com.sourcecanyon.whatsClone.fragments;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ActionMode;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.sourcecanyon.whatsClone.R;
import com.sourcecanyon.whatsClone.adapters.recyclerView.messages.ConversationsAdapter;
import com.sourcecanyon.whatsClone.app.WhatsCloneApplication;
import com.sourcecanyon.whatsClone.helpers.AppHelper;
import com.sourcecanyon.whatsClone.interfaces.LoadingData;
import com.sourcecanyon.whatsClone.models.messages.ConversationsModel;
import com.sourcecanyon.whatsClone.models.messages.MessagesModel;
import com.sourcecanyon.whatsClone.models.users.Pusher;
import com.sourcecanyon.whatsClone.presenters.ConversationsPresenter;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;

/**
 * Created by Abderrahim El imame  on 20/01/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class ConversationsFragment extends Fragment implements LoadingData, RecyclerView.OnItemTouchListener, ActionMode.Callback {

    @Bind(R.id.ConversationsList)
    RecyclerView ConversationList;
    @Bind(R.id.empty)
    LinearLayout emptyConversations;

    private ConversationsAdapter mConversationsAdapter;
    private ConversationsPresenter mConversationsPresenter = new ConversationsPresenter(this);
   private Realm realm;
    private GestureDetectorCompat gestureDetector;
    private ActionMode actionMode;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View mView = inflater.inflate(R.layout.fragment_conversations, container, false);
        ButterKnife.bind(this, mView);
        realm = Realm.getDefaultInstance();
        initializerView();
        mConversationsPresenter.onCreate();
        return mView;
    }

    /**
     * method to initialize the view
     */
    private void initializerView() {
        setHasOptionsMenu(true);

        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(WhatsCloneApplication.getAppContext());
        mLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mConversationsAdapter = new ConversationsAdapter(getActivity());
        ConversationList.setLayoutManager(mLinearLayoutManager);
        ConversationList.setAdapter(mConversationsAdapter);
        ConversationList.setItemAnimator(new DefaultItemAnimator());
        ConversationList.addOnItemTouchListener(this);
        gestureDetector = new GestureDetectorCompat(getActivity(), new RecyclerViewBenOnGestureListener());

    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        gestureDetector.onTouchEvent(e);
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {

    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }


    /**
     * method to toggle the selection
     * @param position
     */
    private void ToggleSelection(int position) {
        mConversationsAdapter.toggleSelection(position);
        String title = String.format("%s selected", mConversationsAdapter.getSelectedItemCount());
        actionMode.setTitle(title);


    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.select_conversation_menu, menu);
        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
        EventBus.getDefault().post(new Pusher("actionModeStarted"));
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete_conversations:

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());


                builder.setMessage(R.string.alert_message_delete_conversation);

                builder.setPositiveButton(R.string.Yes, (dialog, whichButton) -> {
                    Realm realm = Realm.getDefaultInstance();
                    int currentPosition;
                    if (mConversationsAdapter.getSelectedItemCount() != 0) {

                        AppHelper.showDialog(getActivity(), getString(R.string.deleting_chat));

                        for (int x = 0; x < mConversationsAdapter.getSelectedItems().size(); x++) {
                            currentPosition = mConversationsAdapter.getSelectedItems().get(x);
                            ConversationsModel conversationsModel = mConversationsAdapter.getItem(currentPosition);

                            int groupID = conversationsModel.getGroupID();
                            int conversationID = conversationsModel.getId();
                            int recipientID = conversationsModel.getRecipientID();
                            boolean isGroup = conversationsModel.isGroup();

                            realm.executeTransactionAsync(realm1 -> {
                                if (isGroup) {
                                    RealmResults<MessagesModel> messagesModel = realm1.where(MessagesModel.class)
                                            .equalTo("conversationID", conversationID)
                                            .equalTo("groupID", groupID)
                                            .equalTo("isGroup", true)
                                            .findAll();
                                    messagesModel.deleteAllFromRealm();


                                } else {
                                    RealmResults<MessagesModel> messagesModel = realm1.where(MessagesModel.class)
                                            .equalTo("conversationID", conversationID)
                                            .equalTo("isGroup", false)
                                            .equalTo("recipientID", recipientID)
                                            .findAll();
                                    messagesModel.deleteAllFromRealm();

                                }

                            }, () -> {
                                AppHelper.LogCat("Messages deleted successfully ConversationsFragment");
                                realm.executeTransactionAsync(realm1 -> {
                                    ConversationsModel conversationsModel1 = realm1.where(ConversationsModel.class).equalTo("id", conversationID).findFirst();
                                    conversationsModel1.deleteFromRealm();
                                }, () -> {
                                    AppHelper.LogCat("Conversation  deleted successfully ConversationsFragment");
                                    mConversationsAdapter.notifyDataSetChanged();
                                }, error -> {
                                    AppHelper.LogCat("Delete conversation failed ConversationsFragment " + error.getMessage());

                                });
                            }, error -> {

                                AppHelper.LogCat("Delete messages failed ConversationsFragment" + error.getMessage());
                            });

                        }
                        AppHelper.hideDialog();
                    }
                    if (actionMode != null) {
                        mConversationsAdapter.clearSelections();
                        actionMode.finish();
                        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
                    }
                    realm.close();
                });


                builder.setNegativeButton(R.string.No, (dialog, whichButton) -> {

                });

                builder.show();
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        this.actionMode = null;
        mConversationsAdapter.clearSelections();
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
        EventBus.getDefault().post(new Pusher("actionModeDestroyed"));
    }


    private class RecyclerViewBenOnGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            return super.onSingleTapConfirmed(e);
        }

        public void onLongPress(MotionEvent e) {
            View view = ConversationList.findChildViewUnder(e.getX(), e.getY());
            int currentPosition = ConversationList.getChildAdapterPosition(view);
            ConversationsModel conversationsModel = mConversationsAdapter.getItem(currentPosition);
            if (!conversationsModel.isGroup()) {
                if (actionMode != null) {
                    return;
                }
                actionMode = getActivity().startActionMode(ConversationsFragment.this);
                ToggleSelection(currentPosition);
            }

            super.onLongPress(e);
        }

    }

    /**
     * method to show conversation list
     * @param conversationsModels this is parameter for  ShowConversation  method
     */
    public void ShowConversation(List<ConversationsModel> conversationsModels) {

        if (conversationsModels.size() != 0) {
            ConversationList.setVisibility(View.VISIBLE);
            emptyConversations.setVisibility(View.GONE);
            RealmList<ConversationsModel> conversationsModels1 = new RealmList<ConversationsModel>();
            for (ConversationsModel conversationsModel : conversationsModels) {
                conversationsModels1.add(conversationsModel);
            }
            mConversationsAdapter.setConversations(conversationsModels1);
        } else {
            ConversationList.setVisibility(View.GONE);
            emptyConversations.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mConversationsPresenter.onDestroy();
        realm.close();
    }

    @Override
    public void onStop() {
        super.onStop();

    }

    @Override
    public void onShowLoading() {

    }

    @Override
    public void onHideLoading() {
    }


    /**
     * method of EventBus
     *
     * @param pusher this is parameter of onEventMainThread method
     */
    @SuppressWarnings("unused")
    public void onEventMainThread(Pusher pusher) {
        mConversationsPresenter.onEvent(pusher);
        if (pusher != null) {
            if (pusher.getAction().equals("ItemIsActivated")) {
                int idx = ConversationList.getChildAdapterPosition(pusher.getView());
                if (actionMode != null) {
                    ToggleSelection(idx);
                    return;
                }

            }
        }
    }

    @Override
    public void onErrorLoading(Throwable throwable) {
        AppHelper.LogCat("Conversations Fragment " + throwable.getMessage());
    }


}

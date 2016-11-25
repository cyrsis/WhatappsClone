package com.sourcecanyon.whatsClone.activities.groups;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.sourcecanyon.whatsClone.R;
import com.sourcecanyon.whatsClone.adapters.recyclerView.groups.AddMembersToGroupAdapter;
import com.sourcecanyon.whatsClone.adapters.recyclerView.groups.AddMembersToGroupSelectorAdapter;
import com.sourcecanyon.whatsClone.app.AppConstants;
import com.sourcecanyon.whatsClone.app.WhatsCloneApplication;
import com.sourcecanyon.whatsClone.helpers.AppHelper;
import com.sourcecanyon.whatsClone.helpers.PreferenceManager;
import com.sourcecanyon.whatsClone.models.groups.MembersGroupModel;
import com.sourcecanyon.whatsClone.models.users.Pusher;
import com.sourcecanyon.whatsClone.models.users.contacts.ContactsModel;
import com.sourcecanyon.whatsClone.presenters.AddMembersToGroupPresenter;
import com.sourcecanyon.whatsClone.ui.RecyclerViewFastScroller;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import io.realm.Realm;

/**
 * Created by Abderrahim El imame on 20/03/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class AddMembersToGroupActivity extends AppCompatActivity implements RecyclerView.OnItemTouchListener, View.OnClickListener {
    @Bind(R.id.ContactsList)
    RecyclerView ContactsList;
    @Bind(R.id.ParentLayoutAddContact)
    RelativeLayout ParentLayoutAddContact;
    @Bind(R.id.app_bar)
    Toolbar toolbar;
    @Bind(R.id.fab)
    FloatingActionButton floatingActionButton;
    @Bind(R.id.ContactsListHeader)
    RecyclerView ContactsListHeader;
    @Bind(R.id.fastscroller)
    RecyclerViewFastScroller fastScroller;

    private List<ContactsModel> mContactsModelList;
    private AddMembersToGroupAdapter mAddMembersToGroupListAdapter;
    private AddMembersToGroupSelectorAdapter mAddMembersToGroupSelectorAdapter;
    private GestureDetectorCompat gestureDetector;
    private AddMembersToGroupPresenter mAddMembersToGroupPresenter = new AddMembersToGroupPresenter(this);
    private Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_members_to_group);
        ButterKnife.bind(this);
        realm = Realm.getDefaultInstance();
        initializeView();
        setupToolbar();
        EventBus.getDefault().register(this);
    }

    /**
     * method to initialize the view
     */
    private void initializeView() {
        mAddMembersToGroupPresenter.onCreate();
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(WhatsCloneApplication.getAppContext());
        mLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        ContactsList.setLayoutManager(mLinearLayoutManager);
        mAddMembersToGroupListAdapter = new AddMembersToGroupAdapter(this, mContactsModelList);
        ContactsList.setAdapter(mAddMembersToGroupListAdapter);
        // set recycler view to fastScroller
        fastScroller.setRecyclerView(ContactsList);
        fastScroller.setViewsToUse(R.layout.contacts_fragment_fast_scroller, R.id.fastscroller_bubble, R.id.fastscroller_handle);
        ContactsList.setItemAnimator(new DefaultItemAnimator());
        ContactsList.addOnItemTouchListener(this);
        gestureDetector = new GestureDetectorCompat(this, new RecyclerViewBenOnGestureListener());
        floatingActionButton.setOnClickListener(this);


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(WhatsCloneApplication.getAppContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        ContactsListHeader.setLayoutManager(linearLayoutManager);
        mAddMembersToGroupSelectorAdapter = new AddMembersToGroupSelectorAdapter(this);
        ContactsListHeader.setAdapter(mAddMembersToGroupSelectorAdapter);
    }

    /**
     * method to show contacts
     *
     * @param contactsModels this  parameter of ShowContacts method
     */
    public void ShowContacts(List<ContactsModel> contactsModels) {
        mContactsModelList = contactsModels;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mAddMembersToGroupPresenter.onDestroy();
        EventBus.getDefault().unregister(this);
        realm.close();
    }

    /**
     * method to setup the toolbar
     */
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(R.string.title_activity_add_members_to_group);
        String title = String.format("%s of %s selected", mAddMembersToGroupListAdapter.getSelectedItemCount(), mAddMembersToGroupListAdapter.getContacts().size());
        toolbar.setSubtitle(title);
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
     *
     * @param position this is parameter of ToggleSelection method
     */
    private void ToggleSelection(int position) {
        mAddMembersToGroupListAdapter.toggleSelection(position);
        String title = String.format("%s of %s selected", mAddMembersToGroupListAdapter.getSelectedItemCount(), mAddMembersToGroupListAdapter.getContacts().size());
        toolbar.setSubtitle(title);

    }


    @Override
    public void onClick(View v) {
        try {
            if (v.getId() == R.id.container_list_item) {

                int position = ContactsList.getChildAdapterPosition(v);
                ToggleSelection(position);


            } else if (v.getId() == R.id.fab) {
                if (mAddMembersToGroupListAdapter.getSelectedItemCount() != 0) {

                    for (int x = 0; x < mAddMembersToGroupListAdapter.getSelectedItems().size(); x++) {
                        MembersGroupModel membersGroupModel = new MembersGroupModel();
                        int position = mAddMembersToGroupListAdapter.getSelectedItems().get(x);
                        int id = mAddMembersToGroupListAdapter.getContacts().get(position).getId();
                        String username = mAddMembersToGroupListAdapter.getContacts().get(position).getUsername();
                        String phone = mAddMembersToGroupListAdapter.getContacts().get(position).getPhone();
                        String status = mAddMembersToGroupListAdapter.getContacts().get(position).getStatus();
                        String statusDate = mAddMembersToGroupListAdapter.getContacts().get(position).getStatus_date();
                        String userImage = mAddMembersToGroupListAdapter.getContacts().get(position).getImage();
                        String role = "member";
                        membersGroupModel.setUserId(id);
                        membersGroupModel.setUsername(username);
                        membersGroupModel.setPhone(phone);
                        membersGroupModel.setStatus(status);
                        membersGroupModel.setStatus_date(statusDate);
                        membersGroupModel.setImage(userImage);
                        membersGroupModel.setRole(role);
                        PreferenceManager.addMember(this, membersGroupModel);
                    }
                    AppHelper.LaunchActivity(this, CreateGroupActivity.class);
                } else {
                    AppHelper.Snackbar(this, ParentLayoutAddContact, getString(R.string.select_one_at_least), AppConstants.MESSAGE_COLOR_WARNING, AppConstants.TEXT_COLOR);

                }


            }
        } catch (Exception e) {
            AppHelper.LogCat(" Touch Exception AddMembersToGroupActivity " + e.getMessage());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private class RecyclerViewBenOnGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            View view = ContactsList.findChildViewUnder(e.getX(), e.getY());
            onClick(view);
            return super.onSingleTapConfirmed(e);
        }

    }

    /**
     * method to scroll to the bottom of recyclerView
     */
    private void scrollToBottom() {
        ContactsListHeader.scrollToPosition(mAddMembersToGroupSelectorAdapter.getItemCount() - 1);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (mAddMembersToGroupListAdapter.getSelectedItemCount() != 0) {
                mAddMembersToGroupListAdapter.clearSelections();
            }
            PreferenceManager.clearMembers(this);
            finish();
            overridePendingTransition(R.anim.push_up_in, R.anim.push_up_out);

        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * method of EventBus
     *
     * @param pusher this is parameter of onEventMainThread method
     */
    @SuppressWarnings("unused")
    public void onEventMainThread(Pusher pusher) {
        switch (pusher.getAction()) {
            case "removeCreateMember":
                mAddMembersToGroupSelectorAdapter.remove(pusher.getContactsModel());
                if (mAddMembersToGroupSelectorAdapter.getContacts().size() == 0) {
                    ContactsListHeader.setVisibility(View.GONE);
                }
                break;
            case "addCreateMember":
                ContactsListHeader.setVisibility(View.VISIBLE);
                mAddMembersToGroupSelectorAdapter.add(pusher.getContactsModel());
                scrollToBottom();
                break;
            case "deleteCreateMember":
                int position = mAddMembersToGroupListAdapter.getItemPosition(pusher.getContactsModel());
                ToggleSelection(position);
                break;
            case "createGroup":
                finish();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (mAddMembersToGroupListAdapter.getSelectedItemCount() != 0) {
            mAddMembersToGroupListAdapter.clearSelections();
        }
        PreferenceManager.clearMembers(this);
        finish();
        overridePendingTransition(R.anim.push_up_in, R.anim.push_up_out);
    }
}

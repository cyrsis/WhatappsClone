package com.sourcecanyon.whatsClone.activities.groups;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.LinearLayout;

import com.sourcecanyon.whatsClone.R;
import com.sourcecanyon.whatsClone.adapters.recyclerView.groups.AddNewMembersToGroupAdapter;
import com.sourcecanyon.whatsClone.api.APIService;
import com.sourcecanyon.whatsClone.app.WhatsCloneApplication;
import com.sourcecanyon.whatsClone.helpers.AppHelper;
import com.sourcecanyon.whatsClone.models.users.contacts.ContactsModel;
import com.sourcecanyon.whatsClone.presenters.AddNewMembersToGroupPresenter;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.realm.Realm;

/**
 * Created by Abderrahim El imame on 20/03/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class AddNewMembersToGroupActivity extends AppCompatActivity  {
    @Bind(R.id.ContactsList)
    RecyclerView ContactsList;
    @Bind(R.id.ParentLayoutAddNewMembers)
    LinearLayout ParentLayoutAddContact;
    @Bind(R.id.app_bar)
    Toolbar toolbar;

    private List<ContactsModel> mContactsModelList;
    private AddNewMembersToGroupPresenter mAddMembersToGroupPresenter = new AddNewMembersToGroupPresenter(this);
    private int groupID;
    private Realm realm;
    private APIService mApiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_members_to_group);
        ButterKnife.bind(this);

        mApiService = new APIService(this);
        realm = Realm.getDefaultInstance();
        if (getIntent().hasExtra("groupID")) {
            groupID = getIntent().getExtras().getInt("groupID");
        }
        initializeView();
        setupToolbar();
    }

    /**
     * method to initialize the view
     */
    private void initializeView() {
        mAddMembersToGroupPresenter.onCreate();
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(WhatsCloneApplication.getAppContext());
        mLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        ContactsList.setLayoutManager(mLinearLayoutManager);
        AddNewMembersToGroupAdapter mAddMembersToGroupListAdapter = new AddNewMembersToGroupAdapter(this, mContactsModelList, groupID, mApiService);
        ContactsList.setAdapter(mAddMembersToGroupListAdapter);
        // this is the default; this call is actually only necessary with custom ItemAnimators
        ContactsList.setItemAnimator(new DefaultItemAnimator());


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
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }


}

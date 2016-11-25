package com.sourcecanyon.whatsClone.activities.messages;

import android.annotation.TargetApi;
import android.app.SearchManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.sourcecanyon.whatsClone.R;
import com.sourcecanyon.whatsClone.adapters.recyclerView.messages.TransferMessageContactsAdapter;
import com.sourcecanyon.whatsClone.app.WhatsCloneApplication;
import com.sourcecanyon.whatsClone.helpers.PreferenceManager;
import com.sourcecanyon.whatsClone.models.users.contacts.ContactsModel;
import com.sourcecanyon.whatsClone.presenters.SelectContactsPresenter;
import com.sourcecanyon.whatsClone.ui.RecyclerViewFastScroller;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.realm.Case;
import io.realm.Realm;

/**
 * Created by abderrahimelimame on 6/9/16.
 * Email : abderrahim.elimame@gmail.com
 */

public class TransferMessageContactsActivity extends AppCompatActivity {

    @Bind(R.id.ContactsList)
    RecyclerView ContactsList;
    @Bind(R.id.fastscroller)

    RecyclerViewFastScroller fastScroller;
    private List<ContactsModel> mContactsModelList;
    private TransferMessageContactsAdapter mTransferMessageContactsAdapter;
    private SelectContactsPresenter mContactsPresenter = new SelectContactsPresenter(this);
    private ArrayList<String> messageCopied = new ArrayList<>();

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        ButterKnife.bind(this);
        if (getIntent().getExtras() != null) {
            if (getIntent().hasExtra("messageCopied")) {
                messageCopied = getIntent().getExtras().getStringArrayList("messageCopied");
            }
        }
        initializeView();
        mContactsPresenter.onCreate();

    }

    /**
     * method to initialize the view
     */
    private void initializeView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getString(R.string.title_select_contacts));

        }
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(WhatsCloneApplication.getAppContext());
        mLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mTransferMessageContactsAdapter = new TransferMessageContactsAdapter(this, mContactsModelList, messageCopied);
        ContactsList.setLayoutManager(mLinearLayoutManager);
        ContactsList.setAdapter(mTransferMessageContactsAdapter);
        // set recyclerView to fastScroller
        fastScroller.setRecyclerView(ContactsList);
        fastScroller.setViewsToUse(R.layout.contacts_fragment_fast_scroller, R.id.fastscroller_bubble, R.id.fastscroller_handle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);
        // Set up SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search_contacts).getActionView();
        searchView.setIconified(true);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(mQueryTextListener);
        searchView.setQueryHint(getString(R.string.search_hint));
        return super.onCreateOptionsMenu(menu);
    }

    private SearchView.OnQueryTextListener mQueryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String s) {
            return false;
        }

        @Override
        public boolean onQueryTextChange(String s) {
            Search(s.trim());
            return true;
        }
    };

    /**
     * method to start searching
     * @param string  this is parameter for Search method
     */
    public void Search(String string) {
        mTransferMessageContactsAdapter.setString(string);
        List<ContactsModel> filteredModelList = FilterList(string);
        if (filteredModelList.size() != 0) {
            mTransferMessageContactsAdapter.setContacts(filteredModelList);
        }
    }

    /**
     * method to filter the list of contacts
     * @param query  this is parameter for FilterList method
     * @return this is what method will return
     */
    private List<ContactsModel> FilterList(String query) {
        Realm realm = Realm.getDefaultInstance();

        List<ContactsModel> contactsModels = realm.where(ContactsModel.class)
                .equalTo("Linked", true)
                .equalTo("Exist", true)
                .notEqualTo("id", PreferenceManager.getID(this))
                .beginGroup()
                .contains("phone", query, Case.INSENSITIVE)
                .or()
                .contains("username", query, Case.INSENSITIVE)
                .endGroup()
                .findAll();
        realm.close();
        return contactsModels;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        overridePendingTransition(R.anim.push_up_in, R.anim.push_up_out);
        super.onOptionsItemSelected(item);
        return true;
    }

    /**
     * method to show linked contacts
     * @param contactsModels  this is parameter for ShowContacts method
     */
    public void ShowContacts(List<ContactsModel> contactsModels) {
        mContactsModelList = contactsModels;
        if (getSupportActionBar() != null)
            getSupportActionBar().setSubtitle("" + mContactsModelList.size() + getString(R.string.of) + PreferenceManager.getContactSize(this));
        mTransferMessageContactsAdapter.setContacts(mContactsModelList);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mContactsPresenter.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.push_up_in, R.anim.push_up_out);
    }
}

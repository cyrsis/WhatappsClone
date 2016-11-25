package com.sourcecanyon.whatsClone.activities.groups;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.transition.Fade;
import android.transition.Transition;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.rockerhieu.emojicon.EmojiconEditText;
import com.rockerhieu.emojicon.EmojiconGridFragment;
import com.rockerhieu.emojicon.EmojiconsFragment;
import com.rockerhieu.emojicon.emoji.Emojicon;
import com.sourcecanyon.whatsClone.R;
import com.sourcecanyon.whatsClone.adapters.recyclerView.groups.CreateGroupMembersToGroupAdapter;
import com.sourcecanyon.whatsClone.app.AppConstants;
import com.sourcecanyon.whatsClone.app.WhatsCloneApplication;
import com.sourcecanyon.whatsClone.helpers.AppHelper;
import com.sourcecanyon.whatsClone.helpers.Files.FilesManager;
import com.sourcecanyon.whatsClone.helpers.PreferenceManager;
import com.sourcecanyon.whatsClone.models.groups.GroupsModel;
import com.sourcecanyon.whatsClone.models.groups.MembersGroupModel;
import com.sourcecanyon.whatsClone.models.messages.ConversationsModel;
import com.sourcecanyon.whatsClone.models.messages.MessagesModel;
import com.sourcecanyon.whatsClone.models.users.Pusher;
import com.sourcecanyon.whatsClone.models.users.contacts.ContactsModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import io.realm.Realm;
import io.realm.RealmList;
import jp.wasabeef.glide.transformations.CropCircleTransformation;

import static com.sourcecanyon.whatsClone.helpers.UtilsString.escapeJava;

/**
 * Created by Abderrahim El imame on 20/03/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class CreateGroupActivity extends AppCompatActivity implements EmojiconGridFragment.OnEmojiconClickedListener, EmojiconsFragment.OnEmojiconBackspaceClickedListener {


    @Bind(R.id.subject_wrapper)
    EmojiconEditText subjectWrapper;
    @Bind(R.id.group_image)
    ImageView groupImage;
    @Bind(R.id.add_image_group)
    ImageView addImageGroup;
    @Bind(R.id.fab)
    FloatingActionButton doneBtn;
    @Bind(R.id.emoticonBtn)
    ImageView emoticonBtn;
    @Bind(R.id.emojicons)
    FrameLayout emojiIconLayout;
    @Bind(R.id.ContactsList)
    RecyclerView ContactsList;
    @Bind(R.id.participantCounter)
    TextView participantCounter;
    @Bind(R.id.app_bar)
    Toolbar toolbar;

    private CreateGroupMembersToGroupAdapter mAddMembersToGroupListAdapter;
    private boolean emoticonShown = false;
    private String selectedImagePath = null;
    private Realm realm;

    private int lastGroupId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);
        ButterKnife.bind(this);
        realm = Realm.getDefaultInstance();
        initializeView();
        setupToolbar();
        loadData();

        subjectWrapper.setOnClickListener(v1 -> {
            if (emoticonShown) {
                emoticonShown = false;
                emojiIconLayout.setVisibility(View.GONE);
            }
        });
        emoticonBtn.setOnClickListener(v -> {
            if (!emoticonShown) {
                emoticonShown = true;
                View view = this.getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    final Animation animation = AnimationUtils.loadAnimation(this, R.anim.push_up_in);
                    animation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            emojiIconLayout.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                    emojiIconLayout.startAnimation(animation);
                }
            }

        });
        setEmojIconFragment();
    }

    /**
     * method to load members form shared preference
     */
    private void loadData() {
        List<ContactsModel> contactsModels = new ArrayList<>();
        int id;
        for (int x = 0; x < PreferenceManager.getMembers(this).size(); x++) {
            id = PreferenceManager.getMembers(this).get(x).getUserId();
            ContactsModel contactsModel = realm.where(ContactsModel.class).equalTo("id", id).findFirst();
            contactsModels.add(contactsModel);
        }
        mAddMembersToGroupListAdapter.setContacts(contactsModels);

        String text = String.format(getString(R.string.participants) + " %s/%s ", mAddMembersToGroupListAdapter.getItemCount(), PreferenceManager.getContactSize(this));
        participantCounter.setText(text);
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

    /**
     * method to setup the  EmojIcon Fragment
     */
    private void setEmojIconFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.emojicons, EmojiconsFragment.newInstance(false))
                .commit();
    }

    /**
     * method to initialize  the view
     */
    private void initializeView() {
        GridLayoutManager mLinearLayoutManager = new GridLayoutManager(WhatsCloneApplication.getAppContext(), 4);
        ContactsList.setLayoutManager(mLinearLayoutManager);
        mAddMembersToGroupListAdapter = new CreateGroupMembersToGroupAdapter(this);
        ContactsList.setAdapter(mAddMembersToGroupListAdapter);
        doneBtn.setOnClickListener(v -> createGroupOffline());
        addImageGroup.setOnClickListener(v -> launchImageChooser());
        if (AppHelper.isAndroid5()) {
            Transition enterTrans = new Fade();
            getWindow().setEnterTransition(enterTrans);
            enterTrans.setDuration(300);
        }


    }

    /**
     * method to select an image
     */
    private void launchImageChooser() {
        Intent mIntent = new Intent();
        mIntent.setType("image/*");
        mIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(
                Intent.createChooser(mIntent, getString(R.string.select_picture)),
                AppConstants.UPLOAD_PICTURE_REQUEST_CODE);
    }

    /**
     * method to create group in offline mode
     */
    private void createGroupOffline() {
        String groupName = escapeJava(subjectWrapper.getText().toString());
        if (groupName.length() <= 3) {
            subjectWrapper.setError(getString(R.string.name_is_too_short));
        } else {
            Calendar current = Calendar.getInstance();
             String createTime = String.valueOf(current.getTime());
            if (selectedImagePath != null) {
                realm.executeTransactionAsync(realm1 -> {
                    int lastGroupID = 1;
                    int lastID = 1;
                    try {
                        ConversationsModel groupsModel = realm1.where(ConversationsModel.class).findAll().last();
                        lastGroupID = groupsModel.getId();
                        lastGroupID++;

                        List<MessagesModel> messagesModel1 = realm1.where(MessagesModel.class).findAll();
                        lastID = messagesModel1.size();
                        lastID++;

                        AppHelper.LogCat("last message ID  CreateGroupActivity " + lastID);

                    } catch (Exception e) {
                        AppHelper.LogCat("last conversation  ID  conversation 0 Exception  CreateGroupActivity" + e.getMessage());
                        lastGroupID = 1;
                    }
                    RealmList<MembersGroupModel> membersGroupModelRealmList = new RealmList<>();
                    ContactsModel membersGroupModel1 = realm1.where(ContactsModel.class).equalTo("id", PreferenceManager.getID(this)).findFirst();
                    MembersGroupModel membersGroupModel = new MembersGroupModel();
                    String role = "admin";
                    membersGroupModel.setUserId(membersGroupModel1.getId());
                    membersGroupModel.setGroupID(lastGroupID);
                    membersGroupModel.setUsername(membersGroupModel1.getUsername());
                    membersGroupModel.setPhone(membersGroupModel1.getPhone());
                    membersGroupModel.setStatus(membersGroupModel1.getStatus());
                    membersGroupModel.setStatus_date(membersGroupModel1.getStatus_date());
                    membersGroupModel.setImage(membersGroupModel1.getImage());
                    membersGroupModel.setRole(role);
                    membersGroupModelRealmList.add(membersGroupModel);
                    PreferenceManager.addMember(this, membersGroupModel);
                    RealmList<MessagesModel> messagesModelRealmList = new RealmList<MessagesModel>();
                    MessagesModel messagesModel = new MessagesModel();
                    messagesModel.setId(lastID);
                    messagesModel.setDate(createTime);
                    messagesModel.setSenderID(PreferenceManager.getID(this));
                    messagesModel.setRecipientID(0);
                    messagesModel.setStatus(AppConstants.IS_SEEN);
                    messagesModel.setUsername(null);
                    messagesModel.setGroup(true);
                    messagesModel.setMessage("FK");
                    messagesModel.setGroupID(lastGroupID);
                    messagesModel.setConversationID(lastGroupID);
                    messagesModelRealmList.add(messagesModel);
                    GroupsModel groupsModel = new GroupsModel();
                    groupsModel.setId(lastGroupID);
                    groupsModel.setMembers(membersGroupModelRealmList);
                    groupsModel.setGroupImage(selectedImagePath);
                    groupsModel.setGroupName(groupName);
                    groupsModel.setCreatorID(PreferenceManager.getID(this));
                    realm1.copyToRealmOrUpdate(groupsModel);
                    ConversationsModel conversationsModel = new ConversationsModel();
                    conversationsModel.setLastMessage("FK");
                    conversationsModel.setLastMessageId(lastID);
                    conversationsModel.setCreatorID(PreferenceManager.getID(this));
                    conversationsModel.setRecipientID(0);
                    conversationsModel.setRecipientUsername(groupName);
                    conversationsModel.setRecipientImage(selectedImagePath);
                    conversationsModel.setGroupID(lastGroupID);
                    conversationsModel.setMessageDate(createTime);
                    conversationsModel.setId(lastGroupID);
                    conversationsModel.setGroup(true);
                    conversationsModel.setMessages(messagesModelRealmList);
                    conversationsModel.setStatus(AppConstants.IS_SEEN);
                    conversationsModel.setUnreadMessageCounter("0");
                    conversationsModel.setCreatedOnline(false);
                    realm1.copyToRealmOrUpdate(conversationsModel);
                    lastGroupId = lastGroupID;

                }, () -> {
                    EventBus.getDefault().post(new Pusher("createGroup"));
                    AppHelper.Snackbar(this, findViewById(R.id.create_group), getString(R.string.group_created_successfully), AppConstants.MESSAGE_COLOR_SUCCESS, AppConstants.TEXT_COLOR);
                    new Handler().postDelayed(() -> {
                        EventBus.getDefault().post(new Pusher("createGroup"));
                        EventBus.getDefault().post(new Pusher("addMember", String.valueOf(lastGroupId)));
                        finish();
                        overridePendingTransition(R.anim.push_up_in, R.anim.push_up_out);
                    }, 200);
                }, error -> {
                    AppHelper.LogCat("Realm Error create group offline CreateGroupActivity " + error.getMessage());
                    AppHelper.Snackbar(this, findViewById(R.id.create_group), getString(R.string.create_group_failed), AppConstants.MESSAGE_COLOR_WARNING, AppConstants.TEXT_COLOR);

                });

            } else {
                AppHelper.Snackbar(this, findViewById(R.id.create_group), getString(R.string.please_choose_an_avatar), AppConstants.MESSAGE_COLOR_WARNING, AppConstants.TEXT_COLOR);

            }
        }
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == AppConstants.UPLOAD_PICTURE_REQUEST_CODE) {

                if (AppHelper.checkPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    AppHelper.LogCat("Read contact data permission already granted.");
                } else {
                    AppHelper.LogCat("Please request Read contact data permission.");
                    AppHelper.requestPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
                }


                if (AppHelper.checkPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    AppHelper.LogCat("Read contact data permission already granted.");
                } else {
                    AppHelper.LogCat("Please request Read contact data permission.");
                    AppHelper.requestPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                }
                selectedImagePath = FilesManager.getPath(this, data.getData());
                Glide.with(this)
                        .load(data.getData())
                        .asBitmap()
                        .transform(new CropCircleTransformation(this))
                        .into(groupImage);
                if (groupImage.getVisibility() != View.VISIBLE) {
                    groupImage.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    @Override
    public void onEmojiconClicked(Emojicon emojicon) {
        EmojiconsFragment.input(subjectWrapper, emojicon);
    }

    @Override
    public void onEmojiconBackspaceClicked(View v) {
        EmojiconsFragment.backspace(subjectWrapper);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (mAddMembersToGroupListAdapter.getContacts().size() != 0) {
                PreferenceManager.clearMembers(this);
                mAddMembersToGroupListAdapter.getContacts().clear();

            }
            finish();
            overridePendingTransition(R.anim.push_up_in, R.anim.push_up_out);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (mAddMembersToGroupListAdapter.getContacts().size() != 0) {
            PreferenceManager.clearMembers(this);
            mAddMembersToGroupListAdapter.getContacts().clear();

        }
        finish();
        overridePendingTransition(R.anim.push_up_in, R.anim.push_up_out);
    }
}

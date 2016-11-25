package com.sourcecanyon.whatsClone.activities.profile;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.transition.Fade;
import android.transition.Transition;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sourcecanyon.whatsClone.R;
import com.sourcecanyon.whatsClone.activities.groups.AddNewMembersToGroupActivity;
import com.sourcecanyon.whatsClone.activities.groups.EditGroupActivity;
import com.sourcecanyon.whatsClone.activities.main.MainActivity;
import com.sourcecanyon.whatsClone.activities.messages.MessagesActivity;
import com.sourcecanyon.whatsClone.adapters.recyclerView.MediaProfileAdapter;
import com.sourcecanyon.whatsClone.adapters.recyclerView.groups.GroupMembersAdapter;
import com.sourcecanyon.whatsClone.animations.AnimationsUtil;
import com.sourcecanyon.whatsClone.api.APIGroups;
import com.sourcecanyon.whatsClone.api.APIService;
import com.sourcecanyon.whatsClone.app.AppConstants;
import com.sourcecanyon.whatsClone.app.EndPoints;
import com.sourcecanyon.whatsClone.app.WhatsCloneApplication;
import com.sourcecanyon.whatsClone.fragments.BottomSheetEditGroupImage;
import com.sourcecanyon.whatsClone.helpers.AppHelper;
import com.sourcecanyon.whatsClone.helpers.Files.FilesManager;
import com.sourcecanyon.whatsClone.helpers.PreferenceManager;
import com.sourcecanyon.whatsClone.helpers.UtilsPhone;
import com.sourcecanyon.whatsClone.helpers.UtilsTime;
import com.sourcecanyon.whatsClone.models.groups.GroupResponse;
import com.sourcecanyon.whatsClone.models.groups.GroupsModel;
import com.sourcecanyon.whatsClone.models.groups.MembersGroupModel;
import com.sourcecanyon.whatsClone.models.messages.ConversationsModel;
import com.sourcecanyon.whatsClone.models.messages.MessagesModel;
import com.sourcecanyon.whatsClone.models.users.Pusher;
import com.sourcecanyon.whatsClone.models.users.contacts.ContactsModel;
import com.sourcecanyon.whatsClone.models.users.status.StatusResponse;
import com.sourcecanyon.whatsClone.presenters.ProfilePresenter;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.realm.Realm;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.sourcecanyon.whatsClone.helpers.UtilsString.unescapeJava;

/**
 * Created by Abderrahim El imame on 27/03/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class ProfileActivity extends AppCompatActivity {

    @Bind(R.id.cover)
    ImageView UserCover;
    @Bind(R.id.anim_toolbar)
    Toolbar toolbar;
    @Bind(R.id.appbar)
    AppBarLayout AppBarLayout;
    @Bind(R.id.collapsing_toolbar)
    CollapsingToolbarLayout collapsingToolbar;
    @Bind(R.id.containerProfile)
    CoordinatorLayout containerProfile;
    @Bind(R.id.created_title)
    TextView mCreatedTitle;
    @Bind(R.id.group_container_title)
    LinearLayout GroupTitleContainer;
    @Bind(R.id.group_edit)
    ImageView EditGroupBtn;
    @Bind(R.id.statusPhoneContainer)
    CardView statusPhoneContainer;
    @Bind(R.id.status)
    TextView status;
    @Bind(R.id.numberPhone)
    TextView numberPhone;
    @Bind(R.id.status_date)
    TextView status_date;
    @Bind(R.id.send_message)
    ImageView sendMessageBtn;
    @Bind(R.id.call)
    ImageView callBtn;
    @Bind(R.id.MembersList)
    RecyclerView MembersList;
    @Bind(R.id.participantContainer)
    CardView participantContainer;
    @Bind(R.id.participantContainerExit)
    CardView participantContainerExit;
    @Bind(R.id.participantContainerDelete)
    CardView participantContainerDelete;
    @Bind(R.id.participantCounter)
    TextView participantCounter;
    @Bind(R.id.add_contact_participate)
    LinearLayout addNewParticipant;
    @Bind(R.id.mediaProfileList)
    RecyclerView mediaList;
    @Bind(R.id.media_counter)
    TextView mediaCounter;
    @Bind(R.id.media_section)
    CardView mediaSection;

    private MediaProfileAdapter mMediaProfileAdapter;
    private GroupMembersAdapter mGroupMembersAdapter;
    private ContactsModel mContactsModel;
    private GroupsModel mGroupsModel;
    public int userID;
    public int groupID;
    private boolean isGroup;
    private int mutedColor;
    private int mutedColorStatusBar;
    int numberOfColors = 16;
    private ProfilePresenter mProfilePresenter = new ProfilePresenter(this);
    private boolean left;
    private boolean isAnAdmin;
    private APIService mApiService;
    private String PicturePath;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ButterKnife.bind(this);
        initializerView();

        if (getIntent().hasExtra("userID")) {
            isGroup = getIntent().getExtras().getBoolean("isGroup");
            userID = getIntent().getExtras().getInt("userID");
        }

        if (getIntent().hasExtra("groupID")) {
            isGroup = getIntent().getExtras().getBoolean("isGroup");
            groupID = getIntent().getExtras().getInt("groupID");
        }

        mProfilePresenter.onCreate();


        if (isGroup) {
            if (isAnAdmin) {
                AppHelper.LogCat("Admin left " + left);
                if (left) {
                    participantContainerExit.setVisibility(View.GONE);
                    participantContainerDelete.setVisibility(View.VISIBLE);
                    addNewParticipant.setVisibility(View.GONE);
                } else {
                    participantContainerExit.setVisibility(View.VISIBLE);
                    participantContainerDelete.setVisibility(View.GONE);
                    addNewParticipant.setVisibility(View.VISIBLE);
                }
            } else {
                AppHelper.LogCat("Creator left" + left);
                if (left) {
                    participantContainerExit.setVisibility(View.GONE);
                    participantContainerDelete.setVisibility(View.VISIBLE);
                    addNewParticipant.setVisibility(View.GONE);
                } else {
                    participantContainerExit.setVisibility(View.VISIBLE);
                    participantContainerDelete.setVisibility(View.GONE);
                    addNewParticipant.setVisibility(View.GONE);
                }
            }


        } else {
            participantContainerExit.setVisibility(View.GONE);
            participantContainerDelete.setVisibility(View.GONE);
            participantContainer.setVisibility(View.GONE);
        }
        addNewParticipant.setOnClickListener(v -> {
            Intent mIntent = new Intent(this, AddNewMembersToGroupActivity.class);
            mIntent.putExtra("groupID", groupID);
            mIntent.putExtra("profileAdd", "add");
            startActivity(mIntent);
        });
        participantContainerExit.setOnClickListener(v -> {

            String name = unescapeJava(mGroupsModel.getGroupName());
            if (name.length() > 10) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(getString(R.string.exit_group) + name.substring(0, 10) + "... " + "" + getString(R.string.group_ex))
                        .setPositiveButton(getString(R.string.exit), (dialog, which) -> {
                            AppHelper.showDialog(this, getString(R.string.exiting_group_dialog));
                            mProfilePresenter.ExitGroup();
                        }).setNegativeButton(getString(R.string.cancel), null).show();
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(getString(R.string.exit_group) + name + "" + getString(R.string.group_ex))
                        .setPositiveButton(getString(R.string.exit), (dialog, which) -> {
                            AppHelper.showDialog(this, getString(R.string.exiting_group_dialog));
                            mProfilePresenter.ExitGroup();
                        }).setNegativeButton(getString(R.string.cancel), null).show();
            }


        });

        participantContainerDelete.setOnClickListener(v -> {
            String name = unescapeJava(mGroupsModel.getGroupName());
            if (name.length() > 10) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(getString(R.string.delete) + name.substring(0, 10) + "... " + "" + getString(R.string.group_ex))
                        .setPositiveButton(getString(R.string.delete), (dialog, which) -> {
                            AppHelper.showDialog(this, getString(R.string.deleting_group_dialog));
                            mProfilePresenter.DeleteGroup();
                        }).setNegativeButton(getString(R.string.cancel), null).show();
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(getString(R.string.delete) + name + "" + getString(R.string.group_ex))
                        .setPositiveButton(getString(R.string.delete), (dialog, which) -> {
                            AppHelper.showDialog(this, getString(R.string.deleting_group_dialog));
                            mProfilePresenter.DeleteGroup();
                        }).setNegativeButton(getString(R.string.cancel), null).show();
            }
        });
        callBtn.setOnClickListener(view -> callContact(mContactsModel));
        sendMessageBtn.setOnClickListener(view -> sendMessage(mContactsModel));
    }

    /**
     * method to initialize group members view
     */
    private void initializerGroupMembersView() {
        mApiService = new APIService(this);
        participantContainer.setVisibility(View.VISIBLE);
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(WhatsCloneApplication.getAppContext());
        mLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mGroupMembersAdapter = new GroupMembersAdapter(this, mApiService, isAnAdmin);
        MembersList.setLayoutManager(mLinearLayoutManager);
        MembersList.setAdapter(mGroupMembersAdapter);
    }

    /**
     * method to initialize the view
     */
    private void initializerView() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        if (AppHelper.isAndroid5()) {
            collapsingToolbar.setTransitionName(getString(R.string.user_name_transition));
            UserCover.setTransitionName(getString(R.string.user_image_transition));
        }


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(WhatsCloneApplication.getAppContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mediaList.setLayoutManager(linearLayoutManager);
        mMediaProfileAdapter = new MediaProfileAdapter(this);
        mediaList.setAdapter(mMediaProfileAdapter);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (isGroup) {
            if (isAnAdmin && !left) {
                getMenuInflater().inflate(R.menu.profile_menu_group_add, menu);
            } else {
                getMenuInflater().inflate(R.menu.profile_menu_group, menu);
            }
        } else {
            getMenuInflater().inflate(R.menu.profile_menu, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (AppHelper.isAndroid5()) {
                Transition enterTrans = new Fade();
                getWindow().setEnterTransition(enterTrans);
                enterTrans.setDuration(300);
            }
            finish();
        } else if (item.getItemId() == R.id.add_contact) {
            Intent mIntent = new Intent(this, AddNewMembersToGroupActivity.class);
            mIntent.putExtra("groupID", groupID);
            mIntent.putExtra("profileAdd", "add");
            startActivity(mIntent);
        } else if (item.getItemId() == R.id.share) {
            shareContact(mContactsModel);
        } else if (item.getItemId() == R.id.edit_contact) {
            editContact(mContactsModel);
        } else if (item.getItemId() == R.id.view_contact) {
            viewContact(mContactsModel);
        } else if (item.getItemId() == R.id.edit_group_name) {
            launchEditGroupName();
        }

        return super.onOptionsItemSelected(item);
    }

    private void launchEditGroupName() {
        Intent mIntent = new Intent(this, EditGroupActivity.class);
        mIntent.putExtra("currentGroupName", mGroupsModel.getGroupName());
        mIntent.putExtra("groupID", mGroupsModel.getId());
        startActivity(mIntent);
    }

    public void ShowContact(ContactsModel contactsModel) {
        mContactsModel = contactsModel;
        updateUI(null, mContactsModel);
    }

    public void ShowMedia(List<MessagesModel> messagesModel) {
        if (messagesModel.size() != 0) {
            mediaSection.setVisibility(View.VISIBLE);
            mediaCounter.setText(String.valueOf(messagesModel.size()));
            mMediaProfileAdapter.setMessages(messagesModel);

        } else {
            mediaSection.setVisibility(View.GONE);
        }

    }

    public void ShowGroup(GroupsModel groupsModel) {
        mGroupsModel = groupsModel;
        updateUI(mGroupsModel, null);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void updateUI(GroupsModel mGroupsModel, ContactsModel mContactsModel) {

        try {
            if (isGroup) {
                AnimationsUtil.expandToolbar(containerProfile, AppBarLayout);
                GroupTitleContainer.setVisibility(View.VISIBLE);
                String groupDate = UtilsTime.convertDateToString(this, UtilsTime.convertStringToDate(mGroupsModel.getCreatedDate()));
                if (mGroupsModel.getCreatorID() == PreferenceManager.getID(this)) {
                    mCreatedTitle.setText(String.format(getString(R.string.created_by_you_at) + " %s", groupDate));
                } else {
                    String name = UtilsPhone.getContactName(this, mGroupsModel.getCreator());
                    if (name != null) {
                        mCreatedTitle.setText(String.format(getString(R.string.created_by) + " %s " + getString(R.string.group_at) + " %s ", name, groupDate));
                    } else {
                        mCreatedTitle.setText(String.format(getString(R.string.created_by) + " %s " + getString(R.string.group_at) + " %s ", mGroupsModel.getCreator(), groupDate));
                    }
                }
                String name = unescapeJava(mGroupsModel.getGroupName());
                if (name.length() > 10)
                    collapsingToolbar.setTitle(name.substring(0, 10) + "... " + "");
                else
                    collapsingToolbar.setTitle(name);

                if (mGroupsModel.getGroupImage() != null) {
                    String groupId = String.valueOf(mGroupsModel.getId());
                    if (FilesManager.isFileImagesGroupExists(FilesManager.getGroupImage(groupId, mGroupsModel.getGroupName()))) {
                        Target target = new Target() {
                            @Override
                            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {

                                Palette.from(bitmap).maximumColorCount(numberOfColors).generate(palette -> {
                                    UserCover.setImageBitmap(bitmap);
                                    Palette.Swatch vibrantSwatch = palette.getVibrantSwatch();
                                    Palette.Swatch darkVibrantSwatch = palette.getDarkVibrantSwatch();
                                    if (vibrantSwatch != null)
                                        mutedColor = vibrantSwatch.getRgb();
                                    else
                                        mutedColor = palette.getVibrantColor(AppHelper.getColor(ProfileActivity.this, R.color.colorPrimary));
                                    collapsingToolbar.setContentScrimColor(mutedColor);
                                    if (darkVibrantSwatch != null)
                                        mutedColorStatusBar = darkVibrantSwatch.getRgb();
                                    else
                                        mutedColorStatusBar = palette.getDarkVibrantColor(AppHelper.getColor(ProfileActivity.this, R.color.colorPrimaryDark));

                                    if (AppHelper.isAndroid5()) {
                                        getWindow().setStatusBarColor(mutedColorStatusBar);
                                    }


                                });
                            }

                            @Override
                            public void onBitmapFailed(Drawable errorDrawable) {
                                UserCover.setImageDrawable(errorDrawable);
                                mutedColor = AppHelper.getColor(ProfileActivity.this, R.color.colorPrimary);
                                collapsingToolbar.setContentScrimColor(mutedColor);
                                mutedColorStatusBar = AppHelper.getColor(ProfileActivity.this, R.color.colorPrimaryDark);
                                if (AppHelper.isAndroid5()) {
                                    getWindow().setStatusBarColor(mutedColorStatusBar);
                                }
                            }

                            @Override
                            public void onPrepareLoad(Drawable placeHolderDrawable) {
                                UserCover.setImageDrawable(placeHolderDrawable);
                            }
                        };
                        Picasso.with(this)
                                .load(FilesManager.getFileImageGroup(groupId, mGroupsModel.getGroupName()))
                                .resize(500, 500)
                                .centerCrop()
                                .into(target);

                    } else {
                        if (mGroupsModel.getGroupImage() != null) {

                            Target target = new Target() {
                                @Override
                                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                    Palette.from(bitmap).maximumColorCount(numberOfColors).generate(palette -> {
                                        UserCover.setImageBitmap(bitmap);
                                        Palette.Swatch vibrantSwatch = palette.getVibrantSwatch();
                                        Palette.Swatch darkVibrantSwatch = palette.getDarkVibrantSwatch();
                                        if (vibrantSwatch != null)
                                            mutedColor = vibrantSwatch.getRgb();
                                        else
                                            mutedColor = palette.getVibrantColor(AppHelper.getColor(ProfileActivity.this, R.color.colorPrimary));
                                        collapsingToolbar.setContentScrimColor(mutedColor);
                                        if (darkVibrantSwatch != null)
                                            mutedColorStatusBar = darkVibrantSwatch.getRgb();
                                        else
                                            mutedColorStatusBar = palette.getDarkVibrantColor(AppHelper.getColor(ProfileActivity.this, R.color.colorPrimaryDark));
                                        if (AppHelper.isAndroid5()) {
                                            getWindow().setStatusBarColor(mutedColorStatusBar);
                                        }


                                    });
                                }

                                @Override
                                public void onBitmapFailed(Drawable errorDrawable) {
                                    UserCover.setImageDrawable(errorDrawable);
                                    mutedColor = AppHelper.getColor(ProfileActivity.this, R.color.colorPrimary);
                                    collapsingToolbar.setContentScrimColor(mutedColor);
                                    mutedColorStatusBar = AppHelper.getColor(ProfileActivity.this, R.color.colorPrimaryDark);
                                    if (AppHelper.isAndroid5()) {
                                        getWindow().setStatusBarColor(mutedColorStatusBar);
                                    }
                                }

                                @Override
                                public void onPrepareLoad(Drawable placeHolderDrawable) {
                                    UserCover.setImageDrawable(placeHolderDrawable);
                                }
                            };
                            Picasso.with(this)
                                    .load(EndPoints.BASE_URL + mGroupsModel.getGroupImage())
                                    .resize(500, 500)
                                    .centerCrop()
                                    .into(target);
                        } else {
                            UserCover.setPadding(100, 100, 100, 100);
                            UserCover.setBackground(AppHelper.getDrawable(ProfileActivity.this, R.drawable.bg_rect_group_image_holder));
                            UserCover.setImageResource(R.drawable.ic_group_holder_white_opacity_48dp);
                            mutedColor = AppHelper.getColor(ProfileActivity.this, R.color.colorPrimary);
                            collapsingToolbar.setContentScrimColor(mutedColor);
                            mutedColorStatusBar = AppHelper.getColor(ProfileActivity.this, R.color.colorPrimaryDark);
                            if (AppHelper.isAndroid5()) {
                                getWindow().setStatusBarColor(mutedColorStatusBar);
                            }

                        }
                    }


                } else {

                    UserCover.setPadding(100, 100, 100, 100);
                    UserCover.setBackground(AppHelper.getDrawable(ProfileActivity.this, R.drawable.bg_rect_group_image_holder));
                    UserCover.setImageResource(R.drawable.ic_group_holder_white_opacity_48dp);
                    mutedColor = AppHelper.getColor(ProfileActivity.this, R.color.colorPrimary);
                    collapsingToolbar.setContentScrimColor(mutedColor);
                    mutedColorStatusBar = AppHelper.getColor(ProfileActivity.this, R.color.colorPrimaryDark);
                    if (AppHelper.isAndroid5()) {
                        getWindow().setStatusBarColor(mutedColorStatusBar);
                    }
                }
                UserCover.setOnClickListener(view -> {
                    BottomSheetEditGroupImage bottomSheetEditGroupImage = new BottomSheetEditGroupImage();
                    bottomSheetEditGroupImage.show(getSupportFragmentManager(), bottomSheetEditGroupImage.getTag());
                });
            } else {
                AnimationsUtil.expandToolbar(containerProfile, AppBarLayout);
                String name = UtilsPhone.getContactName(this, mContactsModel.getPhone());
                if (name != null) {
                    collapsingToolbar.setTitle(name);
                } else {
                    collapsingToolbar.setTitle(mContactsModel.getPhone());
                }


                statusPhoneContainer.setVisibility(View.VISIBLE);
                String Status = unescapeJava(mContactsModel.getStatus());
                if (Status.length() > 18)
                    status.setText(Status.substring(0, 18) + "... " + "");
                else
                    status.setText(Status);
                numberPhone.setText(mContactsModel.getPhone());
                status_date.setText(mContactsModel.getStatus_date());
                if (mContactsModel.getImage() != null) {
                    String userId = String.valueOf(mContactsModel.getId());
                    if (FilesManager.isFileImagesProfileExists(FilesManager.getProfileImage(userId, mContactsModel.getUsername()))) {

                        Target target = new Target() {
                            @Override
                            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                Palette.from(bitmap).maximumColorCount(numberOfColors).generate(palette -> {
                                    UserCover.setImageBitmap(bitmap);
                                    Palette.Swatch vibrantSwatch = palette.getVibrantSwatch();
                                    Palette.Swatch darkVibrantSwatch = palette.getDarkVibrantSwatch();
                                    if (vibrantSwatch != null)
                                        mutedColor = vibrantSwatch.getRgb();
                                    else
                                        mutedColor = palette.getVibrantColor(AppHelper.getColor(ProfileActivity.this, R.color.colorPrimary));
                                    collapsingToolbar.setContentScrimColor(mutedColor);
                                    if (darkVibrantSwatch != null)
                                        mutedColorStatusBar = darkVibrantSwatch.getRgb();
                                    else
                                        mutedColorStatusBar = palette.getDarkVibrantColor(AppHelper.getColor(ProfileActivity.this, R.color.colorPrimaryDark));

                                    if (AppHelper.isAndroid5()) {
                                        getWindow().setStatusBarColor(mutedColorStatusBar);
                                    }


                                });
                            }

                            @Override
                            public void onBitmapFailed(Drawable errorDrawable) {
                                UserCover.setImageDrawable(errorDrawable);
                                mutedColor = AppHelper.getColor(ProfileActivity.this, R.color.colorPrimary);
                                collapsingToolbar.setContentScrimColor(mutedColor);
                                mutedColorStatusBar = AppHelper.getColor(ProfileActivity.this, R.color.colorPrimaryDark);
                                if (AppHelper.isAndroid5()) {
                                    getWindow().setStatusBarColor(mutedColorStatusBar);
                                }

                            }

                            @Override
                            public void onPrepareLoad(Drawable placeHolderDrawable) {
                                UserCover.setImageDrawable(placeHolderDrawable);
                            }
                        };
                        Picasso.with(this)
                                .load(FilesManager.getFileImageProfile(userId, mContactsModel.getUsername()))
                                .resize(500, 500)
                                .centerCrop()
                                .into(target);

                    } else {
                        if (mContactsModel.getImage() != null) {
                            Target target = new Target() {
                                @Override
                                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                    Palette.from(bitmap).maximumColorCount(numberOfColors).generate(palette -> {
                                        UserCover.setImageBitmap(bitmap);
                                        Palette.Swatch vibrantSwatch = palette.getVibrantSwatch();
                                        Palette.Swatch darkVibrantSwatch = palette.getDarkVibrantSwatch();
                                        if (vibrantSwatch != null)
                                            mutedColor = vibrantSwatch.getRgb();
                                        else
                                            mutedColor = palette.getVibrantColor(AppHelper.getColor(ProfileActivity.this, R.color.colorPrimary));
                                        collapsingToolbar.setContentScrimColor(mutedColor);
                                        if (darkVibrantSwatch != null)
                                            mutedColorStatusBar = darkVibrantSwatch.getRgb();
                                        else
                                            mutedColorStatusBar = palette.getDarkVibrantColor(AppHelper.getColor(ProfileActivity.this, R.color.colorPrimaryDark));

                                        if (AppHelper.isAndroid5()) {
                                            getWindow().setStatusBarColor(mutedColorStatusBar);
                                        }
                                    });
                                }

                                @Override
                                public void onBitmapFailed(Drawable errorDrawable) {
                                    UserCover.setImageDrawable(errorDrawable);
                                    mutedColor = AppHelper.getColor(ProfileActivity.this, R.color.colorPrimary);
                                    collapsingToolbar.setContentScrimColor(mutedColor);
                                    mutedColorStatusBar = AppHelper.getColor(ProfileActivity.this, R.color.colorPrimaryDark);
                                    if (AppHelper.isAndroid5()) {
                                        getWindow().setStatusBarColor(mutedColorStatusBar);
                                    }
                                }

                                @Override
                                public void onPrepareLoad(Drawable placeHolderDrawable) {
                                    UserCover.setImageDrawable(placeHolderDrawable);
                                }
                            };
                            Picasso.with(this)
                                    .load(EndPoints.BASE_URL + mContactsModel.getImage())
                                    .resize(500, 500)
                                    .centerCrop()
                                    .into(target);
                        } else {

                            UserCover.setPadding(100, 100, 100, 100);
                            UserCover.setBackground(AppHelper.getDrawable(ProfileActivity.this, R.drawable.bg_rect_contact_image_holder));
                            UserCover.setImageResource(R.drawable.ic_user_holder_opacity_48dp);
                            mutedColor = AppHelper.getColor(ProfileActivity.this, R.color.colorPrimary);
                            collapsingToolbar.setContentScrimColor(mutedColor);
                            mutedColorStatusBar = AppHelper.getColor(ProfileActivity.this, R.color.colorPrimaryDark);
                            if (AppHelper.isAndroid5()) {
                                getWindow().setStatusBarColor(mutedColorStatusBar);
                            }

                        }


                    }

                } else {
                    UserCover.setPadding(100, 100, 100, 100);
                    UserCover.setBackground(AppHelper.getDrawable(ProfileActivity.this, R.drawable.bg_rect_contact_image_holder));
                    UserCover.setImageResource(R.drawable.ic_user_holder_opacity_48dp);

                    mutedColor = AppHelper.getColor(ProfileActivity.this, R.color.colorPrimary);
                    collapsingToolbar.setContentScrimColor(mutedColor);
                    mutedColorStatusBar = AppHelper.getColor(ProfileActivity.this, R.color.colorPrimaryDark);
                    if (AppHelper.isAndroid5()) {
                        getWindow().setStatusBarColor(mutedColorStatusBar);
                    }
                }
            }

        } catch (Exception e) {
            AppHelper.LogCat("Error in profile UI Exception " + e.getMessage());
        }


    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mProfilePresenter.onDestroy();
    }


    public void onErrorLoading(Throwable throwable) {
        AppHelper.LogCat("Profile throwable " + throwable.getMessage());
    }

    public void onErrorDeleting() {
        AppHelper.Snackbar(this, containerProfile, getString(R.string.failed_to_delete_this_group_check_connection), AppConstants.MESSAGE_COLOR_WARNING, AppConstants.TEXT_COLOR);

    }

    public void onErrorExiting() {
        AppHelper.Snackbar(this, containerProfile, getString(R.string.failed_to_exit_this_group_check_connection), AppConstants.MESSAGE_COLOR_WARNING, AppConstants.TEXT_COLOR);

    }

    /**
     * method to show group members list
     *
     * @param contactsModelList this is parameter for ShowGroupMembers  method
     */
    public void ShowGroupMembers(List<MembersGroupModel> contactsModelList) {

        if (contactsModelList.size() != 0) {
            for (MembersGroupModel membersGroupModel : contactsModelList) {
                if (membersGroupModel.getUserId() == PreferenceManager.getID(this)) {
                    left = membersGroupModel.isLeft();
                    isAnAdmin = membersGroupModel.isAdmin();
                    break;
                }
            }
            initializerGroupMembersView();
            mGroupMembersAdapter.setContacts(contactsModelList);
            participantCounter.setText(String.valueOf(contactsModelList.size()));
        } else {
            participantContainerExit.setVisibility(View.GONE);
            participantContainer.setVisibility(View.GONE);
        }


    }


    /**
     * method of EventBus
     *
     * @param pusher this is parameter of onEventMainThread method
     */
    @SuppressWarnings("unused")
    public void onEventMainThread(Pusher pusher) {
        switch (pusher.getAction()) {
            case "addMember":
                mProfilePresenter.onEventPush(pusher);
                break;
            case "exitGroup":
                participantContainerExit.setVisibility(View.GONE);
                participantContainerDelete.setVisibility(View.VISIBLE);
                AppHelper.Snackbar(this, containerProfile, pusher.getData(), AppConstants.MESSAGE_COLOR_SUCCESS, AppConstants.TEXT_COLOR);
                break;
            case "deleteGroup":
                AppHelper.Snackbar(this, containerProfile, pusher.getData(), AppConstants.MESSAGE_COLOR_SUCCESS, AppConstants.TEXT_COLOR);
                startActivity(new Intent(this, MainActivity.class));
                finish();
                break;
            case "PathGroup":
                PicturePath = pusher.getData();
                new UploadFileToServer().execute();
                break;

        }


    }


    private void editContact(ContactsModel mContactsModel) {
        long ContactID = UtilsPhone.getContactID(this, mContactsModel.getPhone());
        try {
            Intent intent = new Intent(Intent.ACTION_EDIT);
            intent.setData(ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, ContactID));
            startActivity(intent);
        } catch (Exception e) {
            AppHelper.LogCat("error edit contact " + e.getMessage());
        }
    }

    private void viewContact(ContactsModel mContactsModel) {
        long ContactID = UtilsPhone.getContactID(this, mContactsModel.getPhone());
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, ContactID));
            startActivity(intent);
        } catch (Exception e) {
            AppHelper.LogCat("error view contact " + e.getMessage());
        }
    }

    private void sendMessage(ContactsModel mContactsModel) {
        Intent messagingIntent = new Intent(this, MessagesActivity.class);
        messagingIntent.putExtra("conversationID", 0);
        messagingIntent.putExtra("recipientID", mContactsModel.getId());
        messagingIntent.putExtra("isGroup", false);
        startActivity(messagingIntent);
    }

    private void callContact(ContactsModel mContactsModel) {

        try {
            String uri = "tel:" + mContactsModel.getPhone().trim();
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse(uri));
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            startActivity(intent);
        } catch (Exception e) {
            AppHelper.LogCat("error view contact " + e.getMessage());
        }
    }


    private void shareContact(ContactsModel mContactsModel) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("text/*");
        String subject = null;
        if (mContactsModel.getUsername() != null) {
            subject = mContactsModel.getUsername();
        }
        if (mContactsModel.getPhone() != null) {
            if (subject != null) {
                subject = subject + " " + mContactsModel.getPhone();
            } else {
                subject = mContactsModel.getPhone();
            }
        }
        if (subject != null) {
            shareIntent.putExtra(Intent.EXTRA_TEXT, subject);
        }

        startActivity(Intent.createChooser(shareIntent, getString(R.string.shareContact)));
    }


    private void setImage(String path, String groupID) {
        AnimationsUtil.expandToolbar(containerProfile, AppBarLayout);
        FilesManager.downloadFilesToDevice(this, path, groupID, mGroupsModel.getGroupName(), "group");
        Target target = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {

                Palette.from(bitmap).maximumColorCount(numberOfColors).generate(palette -> {
                    UserCover.setImageBitmap(bitmap);
                    Palette.Swatch vibrantSwatch = palette.getVibrantSwatch();
                    Palette.Swatch darkVibrantSwatch = palette.getDarkVibrantSwatch();
                    if (vibrantSwatch != null)
                        mutedColor = vibrantSwatch.getRgb();
                    else
                        mutedColor = palette.getVibrantColor(AppHelper.getColor(ProfileActivity.this, R.color.colorPrimary));
                    collapsingToolbar.setContentScrimColor(mutedColor);
                    if (darkVibrantSwatch != null)
                        mutedColorStatusBar = darkVibrantSwatch.getRgb();
                    else
                        mutedColorStatusBar = palette.getDarkVibrantColor(AppHelper.getColor(ProfileActivity.this, R.color.colorPrimaryDark));

                    if (AppHelper.isAndroid5()) {
                        getWindow().setStatusBarColor(mutedColorStatusBar);
                    }


                });
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                UserCover.setImageDrawable(errorDrawable);
                mutedColor = AppHelper.getColor(ProfileActivity.this, R.color.colorPrimary);
                collapsingToolbar.setContentScrimColor(mutedColor);
                mutedColorStatusBar = AppHelper.getColor(ProfileActivity.this, R.color.colorPrimaryDark);
                if (AppHelper.isAndroid5()) {
                    getWindow().setStatusBarColor(mutedColorStatusBar);
                }
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                UserCover.setImageDrawable(placeHolderDrawable);
            }
        };
        Picasso.with(this)
                .load(EndPoints.BASE_URL + path)
                .networkPolicy(NetworkPolicy.NO_CACHE)
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .resize(500, 500)
                .centerCrop()
                .into(target);

    }

    /**
     * Uploading the file to server
     */
    private class UploadFileToServer extends AsyncTask<Void, Integer, StatusResponse> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
        }

        @Override
        protected StatusResponse doInBackground(Void... params) {
            return uploadFile();
        }

        private StatusResponse uploadFile() {
            RequestBody requestFile;
            final StatusResponse statusResponse = null;
            if (PicturePath != null) {
                // use the FileUtils to get the actual file by uri
                File file = new File(PicturePath);
                // create RequestBody instance from file
                requestFile =
                        RequestBody.create(MediaType.parse("image/*"), file);
            } else {
                requestFile = null;
            }
            APIGroups apiGroups = mApiService.RootService(APIGroups.class, PreferenceManager.getToken(ProfileActivity.this), EndPoints.BASE_URL);
            ProfileActivity.this.runOnUiThread(() -> AppHelper.showDialog(ProfileActivity.this, "Updating ... "));
            Call<GroupResponse> statusResponseCall = apiGroups.uploadImage(requestFile, groupID);
            statusResponseCall.enqueue(new Callback<GroupResponse>() {
                @Override
                public void onResponse(Call<GroupResponse> call, Response<GroupResponse> response) {
                    if (response.isSuccessful()) {
                        AppHelper.hideDialog();
                        if (response.body().isSuccess()) {
                            int groupId = response.body().getGroupID();
                            Realm realm = Realm.getDefaultInstance();
                            realm.executeTransactionAsync(realm1 -> {
                                        GroupsModel groupsModel = realm1.where(GroupsModel.class).equalTo("id", groupId).findFirst();
                                        groupsModel.setGroupImage(response.body().getGroupImage());
                                        realm1.copyToRealmOrUpdate(groupsModel);

                                    }, () -> realm.executeTransactionAsync(realm1 -> {
                                        ConversationsModel conversationsModel = realm1.where(ConversationsModel.class).equalTo("groupID", groupId).findFirst();
                                        conversationsModel.setRecipientImage(response.body().getGroupImage());
                                        realm1.copyToRealmOrUpdate(conversationsModel);
                                    }, () -> {
                                        setImage(response.body().getGroupImage(), String.valueOf(groupId));
                                        AppHelper.CustomToast(ProfileActivity.this, response.body().getMessage());
                                    }, error -> AppHelper.LogCat("error update group image in conversation model " + error.getMessage())),
                                    error -> AppHelper.LogCat("error update group image in group model " + error.getMessage()));
                            realm.close();
                        } else {
                            AppHelper.CustomToast(ProfileActivity.this, response.body().getMessage());
                        }
                    } else {
                        AppHelper.hideDialog();
                        AppHelper.CustomToast(ProfileActivity.this, response.message());
                    }
                }

                @Override
                public void onFailure(Call<GroupResponse> call, Throwable t) {
                    AppHelper.hideDialog();
                    AppHelper.LogCat("Failed  upload your image " + t.getMessage());
                }
            });
            return statusResponse;
        }


        @Override
        protected void onPostExecute(StatusResponse response) {
            super.onPostExecute(response);
            // AppHelper.LogCat("Response from server: " + response);

        }


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (AppHelper.isAndroid5()) {
            //Transition animation
            Transition enterTrans = new Fade();
            getWindow().setEnterTransition(enterTrans);
            enterTrans.setDuration(300);
        }
        finish();
    }
}

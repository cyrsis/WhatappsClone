package com.sourcecanyon.whatsClone.activities.profile;


import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.AppCompatImageView;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.rockerhieu.emojicon.EmojiconTextView;
import com.sourcecanyon.whatsClone.R;
import com.sourcecanyon.whatsClone.activities.messages.MessagesActivity;
import com.sourcecanyon.whatsClone.animations.AnimationsUtil;
import com.sourcecanyon.whatsClone.app.EndPoints;
import com.sourcecanyon.whatsClone.helpers.AppHelper;
import com.sourcecanyon.whatsClone.helpers.Files.FilesManager;
import com.sourcecanyon.whatsClone.helpers.UtilsPhone;
import com.sourcecanyon.whatsClone.helpers.images.BlurTransformation;
import com.sourcecanyon.whatsClone.models.groups.GroupsModel;
import com.sourcecanyon.whatsClone.models.users.contacts.ContactsModel;
import com.sourcecanyon.whatsClone.presenters.ProfilePreviewPresenter;
import com.squareup.picasso.Callback;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import butterknife.Bind;
import butterknife.ButterKnife;

import static com.sourcecanyon.whatsClone.helpers.UtilsString.unescapeJava;

/**
 * Created by Abderrahim El imame on 27/03/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class ProfilePreviewActivity extends Activity  {
    @Bind(R.id.userProfileName)
    EmojiconTextView userProfileName;
    @Bind(R.id.ContactBtn)
    AppCompatImageView ContactBtn;
    @Bind(R.id.AboutBtn)
    AppCompatImageView AboutBtn;
    @Bind(R.id.userProfilePicture)
    AppCompatImageView userProfilePicture;
    @Bind(R.id.actionProfileArea)
    LinearLayout actionProfileArea;
    @Bind(R.id.InviteBtn)
    TextView InviteBtn;
    @Bind(R.id.containerProfile)
    LinearLayout containerProfile;
    @Bind(R.id.containerProfileInfo)
    LinearLayout containerProfileInfo;


    private ContactsModel mContactsModel;
    public int userID;
    public int groupID;
    public int conversationID;
    private boolean isGroup;
    private long Duration = 500;
    private Intent mIntent;

    private ProfilePreviewPresenter mProfilePresenter = new ProfilePreviewPresenter(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (AppHelper.isAndroid5()) {
            getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }

        // Make us non-modal, so that others can receive touch events.
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
        // but notify us that it happened.
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH);

        setContentView(R.layout.activity_profile_perview);
        ButterKnife.bind(this);
        initializerView();
        setupProgressBar();
        if (getIntent().hasExtra("userID")) {
            isGroup = getIntent().getExtras().getBoolean("isGroup");
            userID = getIntent().getExtras().getInt("userID");
        }

        if (getIntent().hasExtra("groupID")) {
            isGroup = getIntent().getExtras().getBoolean("isGroup");
            groupID = getIntent().getExtras().getInt("groupID");
            conversationID = getIntent().getExtras().getInt("conversationID");
        }
        mProfilePresenter.onCreate();
        if (AppHelper.isAndroid5()) {
            containerProfileInfo.post(() -> AnimationsUtil.show(containerProfileInfo, Duration));
        }


        InviteBtn.setOnClickListener(v -> {
            String number = mContactsModel.getPhone();
            Intent smsIntent = new Intent(Intent.ACTION_VIEW);
            smsIntent.setType("vnd.android-dir/mms-sms");
            smsIntent.putExtra("address", number);
            smsIntent.putExtra("sms_body", "Hello checkout the " + getString(R.string.app_name) + " application");
            startActivity(smsIntent);
        });


    }

    /**
     * method to initialize the view
     */
    private void initializerView() {
        if (AppHelper.isAndroid5()) {
            userProfilePicture.setTransitionName(getString(R.string.user_image_transition));
            userProfileName.setTransitionName(getString(R.string.user_name_transition));
        }
        ContactBtn.setOnClickListener(v -> {
            if (isGroup) {
                Intent messagingIntent = new Intent(this, MessagesActivity.class);
                messagingIntent.putExtra("conversationID", conversationID);
                messagingIntent.putExtra("groupID", groupID);
                messagingIntent.putExtra("isGroup", true);
                startActivity(messagingIntent);
            } else {
                Intent messagingIntent = new Intent(this, MessagesActivity.class);
                messagingIntent.putExtra("conversationID", 0);
                messagingIntent.putExtra("recipientID", userID);
                messagingIntent.putExtra("isGroup", false);
                startActivity(messagingIntent);

            }
        });
        AboutBtn.setOnClickListener(v -> {
            if (isGroup) {
                if (AppHelper.isAndroid5()) {
                    mIntent = new Intent(this, ProfileActivity.class);
                    mIntent.putExtra("groupID", groupID);
                    mIntent.putExtra("isGroup", true);
                    ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this, new Pair<>(userProfilePicture, getString(R.string.user_image_transition)), new Pair<>(userProfileName, getString(R.string.user_name_transition)));
                    startActivity(mIntent, options.toBundle());
                    finish();
                } else {
                    mIntent = new Intent(this, ProfileActivity.class);
                    mIntent.putExtra("groupID", groupID);
                    mIntent.putExtra("isGroup", true);
                    startActivity(mIntent);
                    finish();
                }
            } else {
                if (AppHelper.isAndroid5()) {
                    mIntent = new Intent(this, ProfileActivity.class);
                    mIntent.putExtra("userID", userID);
                    mIntent.putExtra("isGroup", false);
                    ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this, new Pair<>(userProfilePicture, getString(R.string.user_image_transition)), new Pair<>(userProfileName, getString(R.string.user_name_transition)));
                    startActivity(mIntent, options.toBundle());
                    finish();
                } else {
                    mIntent = new Intent(this, ProfileActivity.class);
                    mIntent.putExtra("userID", userID);
                    mIntent.putExtra("isGroup", false);
                    startActivity(mIntent);
                    finish();
                }
            }

        });
        containerProfile.setOnClickListener(v -> {
            if (AppHelper.isAndroid5())
                containerProfileInfo.post(() -> AnimationsUtil.hide(this, containerProfileInfo, Duration));
            else
                finish();
        });
        containerProfileInfo.setOnClickListener(v -> {
            if (AppHelper.isAndroid5())
                containerProfileInfo.post(() -> AnimationsUtil.hide(this, containerProfileInfo, Duration));
            else
                finish();
        });

    }

    /**
     * method to setup the progressBar
     */
    private void setupProgressBar() {
        ProgressBar mProgress = (ProgressBar) findViewById(R.id.progress_bar);
        mProgress.getIndeterminateDrawable().setColorFilter(Color.parseColor("#0EC654"),
                PorterDuff.Mode.SRC_IN);


    }

    /**
     *  method to show user information
     * @param contactsModels this is parameter for  ShowContact method
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void ShowContact(ContactsModel contactsModels) {
        mContactsModel = contactsModels;
        UpdateUI(mContactsModel, null);
    }

    /**
     * method to show group information
     * @param groupsModel this is parameter for   ShowGroup method
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void ShowGroup(GroupsModel groupsModel) {
        UpdateUI(null, groupsModel);
    }

    /**
     * method to update the UI
     * @param mContactsModel this is the first parameter for  UpdateUI  method
     * @param mGroupsModel this is the second parameter for   UpdateUI  method
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void UpdateUI(ContactsModel mContactsModel, GroupsModel mGroupsModel) {
        try {

            Transformation blurTransformation = new Transformation() {
                @Override
                public Bitmap transform(Bitmap source) {
                    Bitmap blurred = BlurTransformation.fastblur(ProfilePreviewActivity.this, source, 10);
                    source.recycle();
                    return blurred;
                }

                @Override
                public String key() {
                    return "blur()";
                }
            };
            if (isGroup) {
                if (mGroupsModel.getGroupName() != null) {
                    String groupname = unescapeJava(mGroupsModel.getGroupName());
                    if (groupname.length() > 18)
                        userProfileName.setText(groupname.substring(0, 18) + "... " + "");
                    else
                        userProfileName.setText(groupname);

                }
                String userId = String.valueOf(mGroupsModel.getId());
                if (mGroupsModel.getGroupImage() != null) {
                    if (FilesManager.isFileImagesGroupExists(FilesManager.getGroupImage(userId, mGroupsModel.getGroupName()))) {
                        Picasso.with(this)
                                .load(FilesManager.getFileImageGroup(userId, mGroupsModel.getGroupName()))
                                .networkPolicy(NetworkPolicy.NO_CACHE)
                                .memoryPolicy(MemoryPolicy.NO_CACHE)
                                .resize(500, 500)
                                .centerCrop()
                                .into(userProfilePicture);
                    } else {
                        Picasso.with(this)
                                .load(EndPoints.BASE_URL + mGroupsModel.getGroupImage())
                                .transform(blurTransformation)
                                .networkPolicy(NetworkPolicy.NO_CACHE)
                                .memoryPolicy(MemoryPolicy.NO_CACHE)
                                .resize(200, 200)
                                .centerCrop()
                                .into(userProfilePicture, new Callback() {
                                    @Override
                                    public void onSuccess() {
                                        Picasso.with(ProfilePreviewActivity.this)
                                                .load(EndPoints.BASE_URL + mGroupsModel.getGroupImage())
                                                .resize(500, 500)
                                                .centerCrop()
                                                .placeholder(userProfilePicture.getDrawable())
                                                .into(userProfilePicture);
                                    }

                                    @Override
                                    public void onError() {
                                    }
                                });

                    }
                } else {
                    userProfilePicture.setPadding(100, 100, 100, 100);
                    userProfilePicture.setBackground(AppHelper.getDrawable(this, R.drawable.bg_rect_group_image_holder));
                    userProfilePicture.setImageResource(R.drawable.ic_group_holder_white_opacity_48dp);
                }

                actionProfileArea.setVisibility(View.VISIBLE);
                InviteBtn.setVisibility(View.GONE);
            } else {
                if (mContactsModel.isLinked()) {
                    actionProfileArea.setVisibility(View.VISIBLE);
                    InviteBtn.setVisibility(View.GONE);
                } else {
                    actionProfileArea.setVisibility(View.GONE);
                    InviteBtn.setVisibility(View.VISIBLE);
                }
                String name = UtilsPhone.getContactName(this, mContactsModel.getPhone());
                if (name != null) {
                    userProfileName.setText(name);
                } else {
                    userProfileName.setText(mContactsModel.getPhone());
                }

                String userId = String.valueOf(mContactsModel.getId());
                if (mContactsModel.getImage() != null) {
                    if (FilesManager.isFileImagesProfileExists(FilesManager.getProfileImage(userId, mContactsModel.getUsername()))) {

                        Picasso.with(this)
                                .load(FilesManager.getFileImageProfile(userId, mContactsModel.getUsername()))
                                .networkPolicy(NetworkPolicy.NO_CACHE)
                                .memoryPolicy(MemoryPolicy.NO_CACHE)
                                .resize(500, 500)
                                .centerCrop()
                                .into(userProfilePicture);
                    } else {

                        Picasso.with(this)
                                .load(EndPoints.BASE_URL + mContactsModel.getImage())
                                .transform(blurTransformation)
                                .networkPolicy(NetworkPolicy.NO_CACHE)
                                .memoryPolicy(MemoryPolicy.NO_CACHE)
                                .resize(200, 200)
                                .centerCrop()
                                .into(userProfilePicture, new Callback() {
                                    @Override
                                    public void onSuccess() {

                                        Picasso.with(ProfilePreviewActivity.this)
                                                .load(EndPoints.BASE_URL + mContactsModel.getImage())
                                                .resize(500, 500)
                                                .centerCrop()
                                                .placeholder(userProfilePicture.getDrawable())
                                                .into(userProfilePicture);
                                    }

                                    @Override
                                    public void onError() {
                                    }
                                });
                    }
                } else {

                    userProfilePicture.setPadding(100, 100, 100, 100);
                    userProfilePicture.setBackground(AppHelper.getDrawable(this, R.drawable.bg_rect_contact_image_holder));
                    userProfilePicture.setImageDrawable(AppHelper.getDrawable(this, R.drawable.ic_user_holder_opacity_48dp));
                }

            }
        } catch (Exception e) {
            AppHelper.LogCat(" Profile preview Exception" + e.getMessage());
        }
    }

    public void onErrorLoading(Throwable throwable) {
        AppHelper.LogCat(throwable.getMessage());
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        mProfilePresenter.onDestroy();
    }


    @Override
    public void onBackPressed() {
        if (AppHelper.isAndroid5())
            containerProfileInfo.post(() -> AnimationsUtil.hide(this, containerProfileInfo, Duration));
        else
            finish();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // If we've received a touch notification that the user has touched
        // outside the app, finish the activity.
        if (MotionEvent.ACTION_OUTSIDE == event.getAction()) {
            if (AppHelper.isAndroid5())
                containerProfileInfo.post(() -> AnimationsUtil.hide(this, containerProfileInfo, Duration));
            else
                finish();
            return true;
        }

        return super.onTouchEvent(event);
    }


}

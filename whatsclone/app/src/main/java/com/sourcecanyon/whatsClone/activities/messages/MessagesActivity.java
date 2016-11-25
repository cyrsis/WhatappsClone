package com.sourcecanyon.whatsClone.activities.messages;

import android.Manifest;
import android.animation.Animator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActivityOptions;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaRecorder;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.os.SystemClock;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.support.design.widget.TextInputEditText;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.util.Pair;
import android.view.ActionMode;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.rockerhieu.emojicon.EmojiconEditText;
import com.rockerhieu.emojicon.EmojiconGridFragment;
import com.rockerhieu.emojicon.EmojiconTextView;
import com.rockerhieu.emojicon.EmojiconsFragment;
import com.rockerhieu.emojicon.emoji.Emojicon;
import com.sourcecanyon.whatsClone.R;
import com.sourcecanyon.whatsClone.activities.profile.ProfileActivity;
import com.sourcecanyon.whatsClone.activities.settings.PreferenceSettingsManager;
import com.sourcecanyon.whatsClone.adapters.recyclerView.TextWatcherAdapter;
import com.sourcecanyon.whatsClone.adapters.recyclerView.messages.MessagesAdapter;
import com.sourcecanyon.whatsClone.animations.ViewAudioProxy;
import com.sourcecanyon.whatsClone.app.AppConstants;
import com.sourcecanyon.whatsClone.app.EndPoints;
import com.sourcecanyon.whatsClone.app.WhatsCloneApplication;
import com.sourcecanyon.whatsClone.helpers.AppHelper;
import com.sourcecanyon.whatsClone.helpers.Files.FilesManager;
import com.sourcecanyon.whatsClone.helpers.PreferenceManager;
import com.sourcecanyon.whatsClone.helpers.UtilsPhone;
import com.sourcecanyon.whatsClone.helpers.UtilsString;
import com.sourcecanyon.whatsClone.helpers.UtilsTime;
import com.sourcecanyon.whatsClone.helpers.notifications.NotificationsManager;
import com.sourcecanyon.whatsClone.interfaces.LoadingData;
import com.sourcecanyon.whatsClone.models.groups.GroupsModel;
import com.sourcecanyon.whatsClone.models.groups.MembersGroupModel;
import com.sourcecanyon.whatsClone.models.messages.ConversationsModel;
import com.sourcecanyon.whatsClone.models.messages.MessagesModel;
import com.sourcecanyon.whatsClone.models.users.Pusher;
import com.sourcecanyon.whatsClone.models.users.contacts.ContactsModel;
import com.sourcecanyon.whatsClone.presenters.MessagesPresenter;
import com.sourcecanyon.whatsClone.services.MainService;
import com.sourcecanyon.whatsClone.ui.CropSquareTransformation;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import io.codetail.animation.ViewAnimationUtils;
import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;
import io.socket.client.Ack;
import io.socket.client.Socket;

import static com.sourcecanyon.whatsClone.helpers.UtilsString.escapeJavaString;

/**
 * Created by Abderrahim El imame on 05/03/2016.
 * Email : abderrahim.elimame@gmail.com
 */

@SuppressLint("SetTextI18n")
public class MessagesActivity extends AppCompatActivity implements EmojiconGridFragment.OnEmojiconClickedListener, EmojiconsFragment.OnEmojiconBackspaceClickedListener, LoadingData, RecyclerView.OnItemTouchListener, ActionMode.Callback, View.OnClickListener {

    @Bind(R.id.activity_messages)
    LinearLayout mView;
    @Bind(R.id.listMessages)
    RecyclerView messagesList;
    @Bind(R.id.send_button)
    ImageButton SendButton;
    @Bind(R.id.send_record_button)
    ImageButton SendRecordButton;
    @Bind(R.id.pictureBtn)
    ImageButton PictureButton;
    @Bind(R.id.emoticonBtn)
    ImageButton EmoticonButton;
    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.toolbar_image)
    ImageView ToolbarImage;
    @Bind(R.id.MessageWrapper)
    EmojiconEditText messageWrapper;
    @Bind(R.id.toolbar_title)
    EmojiconTextView ToolbarTitle;
    @Bind(R.id.toolbar_status)
    TextView lastVu;
    @Bind(R.id.toolbarLinear)
    LinearLayout ToolbarLinearLayout;
    @Bind(R.id.progress_bar)
    ProgressBar mProgressBar;
    @Bind(R.id.emojicons)
    FrameLayout emojiIconLayout;
    @Bind(R.id.arrow_back)
    LinearLayout BackButton;
    @Bind(R.id.send_message)
    LinearLayout SendMessageLayout;
    @Bind(R.id.send_message_panel)
    View sendMessagePanel;

    final int MIN_INTERVAL_TIME = 2000;
    long mStartTime;
    private boolean emoticonShown = false;
    public Intent mIntent = null;
    private MessagesAdapter mMessagesAdapter;
    public Context context;
    private String messageTransfer = null;
    private ContactsModel mUsersModel;
    private GroupsModel mGroupsModel;
    private ContactsModel mUsersModelRecipient;
    private String FileImagePath = null;
    private String FileVideoThumbnailPath = null;
    private String FileVideoPath = null;
    private String FileAudioPath = null;
    private String FileDocumentPath = null;
    private MessagesPresenter mMessagesPresenter = new MessagesPresenter(this);
    private int ConversationID;
    private int groupID;
    private boolean isGroup;

    //for sockets
    private Socket mSocket;
    private int senderId;
    private int recipientId;
    private Timer TYPING_TIMER_LENGTH = new Timer();
    private boolean isTyping = false;
    private boolean isOpen;
    private Realm realm;

    //for audio
    @Bind(R.id.recording_time_text)
    TextView recordTimeText;
    @Bind(R.id.record_panel)
    View recordPanel;
    @Bind(R.id.slide_text_container)
    View slideTextContainer;
    @Bind(R.id.slideToCancelText)
    TextView slideToCancelText;
    private MediaRecorder mMediaRecorder = null;
    private float startedDraggingX = -1;
    private float distCanMove = convertToDp(80);
    private long startTime = 0L;
    private Timer recordTimer;

    /* for serach */
    @Bind(R.id.close_btn_search_view)
    ImageView closeBtn;
    @Bind(R.id.search_input)
    TextInputEditText searchInput;
    @Bind(R.id.clear_btn_search_view)
    ImageView clearBtn;
    @Bind(R.id.app_bar_search_view)
    View searchView;


    /**
     * For Attachment container
     */
    @Bind(R.id.items_container)
    LinearLayout mFrameLayoutReveal;
    @Bind(R.id.attach_camera)
    ImageView attachCamera;
    @Bind(R.id.attach_image)
    ImageView attachImage;
    @Bind(R.id.attach_audio)
    ImageView attachAudio;
    @Bind(R.id.attach_document)
    ImageView attachDocument;
    @Bind(R.id.attach_video)
    ImageView attachVideo;
    @Bind(R.id.attach_record_video)
    ImageView attachRecordVideo;

    private Animator.AnimatorListener mAnimatorListenerOpen, mAnimatorListenerClose;
    private GestureDetectorCompat gestureDetector;
    private ActionMode actionMode;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);
        ButterKnife.bind(this);
        realm = Realm.getDefaultInstance();
        if (getIntent().getExtras() != null) {
            if (getIntent().hasExtra("recipientID")) {
                recipientId = getIntent().getExtras().getInt("recipientID");
            }
            if (getIntent().hasExtra("groupID")) {
                groupID = getIntent().getExtras().getInt("groupID");
            }

            if (getIntent().hasExtra("conversationID")) {
                ConversationID = getIntent().getExtras().getInt("conversationID");
            }
            if (getIntent().hasExtra("isGroup")) {
                isGroup = getIntent().getExtras().getBoolean("isGroup");
            }
            setEmojIconFragment();
        }

        senderId = PreferenceManager.getID(this);
        AppHelper.LogCat("senderId id extra " + senderId);
        AppHelper.LogCat("recipientId id extra " + recipientId);
        initializerSearchView(searchInput, clearBtn);
        initView();
        mMessagesPresenter.onCreate();


        initializerMessageWrapper();
        if (getIntent().getExtras() != null) {
            if (getIntent().hasExtra("messageCopied")) {
                ArrayList<String> messageCopied = getIntent().getExtras().getStringArrayList("messageCopied");
                for (String message : messageCopied) {
                    messageTransfer = message;
                    new Handler().postDelayed(this::sendMessage, 50);
                }
            }

        }

        mAnimatorListenerOpen = new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                mFrameLayoutReveal.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {
            }
        };

        mAnimatorListenerClose = new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                mFrameLayoutReveal.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        };

        mFrameLayoutReveal.setOnClickListener(view -> {
            if (isOpen) {
                isOpen = false;
                animateItems(false);

            }
        });


        connectToChatServer();
        JSONObject json = new JSONObject();
        try {
            json.put("connected", true);
            json.put("senderId", senderId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mSocket.emit(AppConstants.SOCKET_IS_ONLINE, json);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

    }

    /**
     * method to call a user
     */
    private void callContact() {

        try {
            String uri = "tel:" + mUsersModelRecipient.getPhone().trim();
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse(uri));

            if (AppHelper.checkPermission(this, Manifest.permission.CALL_PHONE)) {
                AppHelper.LogCat("CALL PHONE  permission already granted.");
            } else {
                AppHelper.LogCat("Please request CALL PHONE permission.");
                AppHelper.requestPermission(this, Manifest.permission.CALL_PHONE);
            }
            startActivity(intent);
        } catch (Exception e) {
            AppHelper.LogCat("error view contact " + e.getMessage());
        }
    }

    /**
     * method to animate the attachment items
     *
     * @param opened
     */
    private void animateItems(boolean opened) {
        float startRadius = 0.0f;
        float endRadius = Math.max(mFrameLayoutReveal.getWidth(), mFrameLayoutReveal.getHeight());
        if (opened) {
            int cy = mFrameLayoutReveal.getRight();
            int dx = mFrameLayoutReveal.getTop();
            Animator supportAnimator = ViewAnimationUtils.createCircularReveal(mFrameLayoutReveal, cy, dx, startRadius, endRadius);
            supportAnimator.setInterpolator(new AccelerateInterpolator());
            supportAnimator.setDuration(400);
            supportAnimator.addListener(mAnimatorListenerOpen);
            supportAnimator.start();
        } else {
            int cy = mFrameLayoutReveal.getRight();
            int dx = mFrameLayoutReveal.getTop();
            Animator supportAnimator2 = ViewAnimationUtils.createCircularReveal(mFrameLayoutReveal, cy, dx, endRadius, startRadius);
            supportAnimator2.setInterpolator(new DecelerateInterpolator());
            supportAnimator2.setDuration(400);
            supportAnimator2.addListener(mAnimatorListenerClose);
            supportAnimator2.start();
        }
    }

    /**
     * method to setup EmojIcon Fragment
     */
    private void setEmojIconFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.emojicons, EmojiconsFragment.newInstance(false))
                .commit();
    }

    @Override
    public void onEmojiconClicked(Emojicon emojicon) {
        EmojiconsFragment.input(messageWrapper, emojicon);
    }

    @Override
    public void onEmojiconBackspaceClicked(View v) {
        EmojiconsFragment.backspace(messageWrapper);
    }


    /**
     * method initialize the view
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void initView() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        mMessagesAdapter = new MessagesAdapter(this, realm);
        LinearLayoutManager layoutManager = new LinearLayoutManager(WhatsCloneApplication.getAppContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.setStackFromEnd(true);
        messagesList.setLayoutManager(layoutManager);
        messagesList.setAdapter(mMessagesAdapter);
        messagesList.setItemAnimator(new DefaultItemAnimator());
        messagesList.addOnItemTouchListener(this);
        gestureDetector = new GestureDetectorCompat(this, new RecyclerViewBenOnGestureListener());
        mView.setBackgroundColor(Color.parseColor(PreferenceSettingsManager.getDefault_wallpaper(this)));
        EmoticonButton.setVisibility(View.VISIBLE);
        messageWrapper.setOnClickListener(v1 -> {
            if (emoticonShown) {
                emoticonShown = false;
                emojiIconLayout.setVisibility(View.GONE);

            }
        });

        EmoticonButton.setOnClickListener(v -> {
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
        slideToCancelText.setText(R.string.slide_to_cancel_audio);
        messageWrapper.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                InputMethodManager inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }

        });
        messageWrapper.addTextChangedListener(new TextWatcherAdapter() {
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (PreferenceSettingsManager.enter_send(MessagesActivity.this)) {
                    messageWrapper.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT);
                    messageWrapper.setSingleLine(true);
                    messageWrapper.setOnEditorActionListener((v, actionId, event) -> {
                        if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_SEND)) {
                            sendMessage();
                        }
                        return false;
                    });
                }

            }

            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        SendButton.setOnClickListener(v -> sendMessage());
        SendRecordButton.setOnTouchListener((view, motionEvent) -> {
            setDraggingAnimation(motionEvent, view);
            return true;

        });
        PictureButton.setOnClickListener(v -> launchAttachCamera());
        attachCamera.setOnClickListener(view -> launchAttachCamera());
        attachImage.setOnClickListener(view -> launchImageChooser());
        attachVideo.setOnClickListener(view -> launchVideoChooser());
        attachRecordVideo.setOnClickListener(view -> launchAttachRecordVideo());
        attachDocument.setOnClickListener(view -> launchDocumentChooser());
        attachAudio.setOnClickListener(view -> launchAudioChooser());

        if (AppHelper.isAndroid5()) {
            ToolbarImage.setTransitionName(getString(R.string.user_image_transition));
            ToolbarTitle.setTransitionName(getString(R.string.user_name_transition));
        }
        ToolbarLinearLayout.setOnClickListener(v -> {
            if (isGroup) {
                if (AppHelper.isAndroid5()) {
                    mIntent = new Intent(this, ProfileActivity.class);
                    mIntent.putExtra("groupID", groupID);
                    mIntent.putExtra("isGroup", true);
                    ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this, new Pair<>(ToolbarImage, getString(R.string.user_image_transition)), new Pair<>(ToolbarTitle, getString(R.string.user_name_transition)));
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
                    mIntent.putExtra("userID", recipientId);
                    mIntent.putExtra("isGroup", false);
                    ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this, new Pair<>(ToolbarImage, getString(R.string.user_image_transition)), new Pair<>(ToolbarTitle, getString(R.string.user_name_transition)));
                    startActivity(mIntent, options.toBundle());
                    finish();
                } else {
                    mIntent = new Intent(this, ProfileActivity.class);
                    mIntent.putExtra("userID", recipientId);
                    mIntent.putExtra("isGroup", false);
                    startActivity(mIntent);
                    finish();
                }
            }
        });
        BackButton.setOnClickListener(v -> {
            mMessagesAdapter.stopAudio();
            finish();

            if (NotificationsManager.getManager()) {
                if (isGroup)
                    NotificationsManager.cancelNotification(groupID);
                else
                    NotificationsManager.cancelNotification(recipientId);
            }

        });


    }

    /**
     * method to launch the camera preview
     */
    private void launchAttachCamera() {
        if (AppHelper.checkPermission(this, Manifest.permission.CAMERA)) {
            AppHelper.LogCat("camera permission already granted.");
        } else {
            AppHelper.LogCat("Please request camera  permission.");
            AppHelper.requestPermission(this, Manifest.permission.CAMERA);
        }
        if (isOpen) {
            isOpen = false;
            animateItems(false);
        }
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_SCREEN_ORIENTATION, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        startActivityForResult(cameraIntent, AppConstants.SELECT_MESSAGES_CAMERA);
    }

    /**
     * method to launch the image chooser
     */
    private void launchImageChooser() {
        if (isOpen) {
            isOpen = false;
            animateItems(false);
        }


        if (AppHelper.checkPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            AppHelper.LogCat("Read data permission already granted.");
        } else {
            AppHelper.LogCat("Please request Read data permission.");
            AppHelper.requestPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        }

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(
                Intent.createChooser(intent, "Choose An image"),
                AppConstants.UPLOAD_PICTURE_REQUEST_CODE);
    }

    /**
     * method  to launch a video preview
     */
    private void launchAttachRecordVideo() {
        if (isOpen) {
            isOpen = false;
            animateItems(false);
        }

        if (AppHelper.checkPermission(this, Manifest.permission.CAMERA)) {
            AppHelper.LogCat("Camera permission already granted.");
        } else {
            AppHelper.LogCat("Please request camera  permission.");
            AppHelper.requestPermission(this, Manifest.permission.CAMERA);
        }
        Intent cameraIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_SCREEN_ORIENTATION, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        startActivityForResult(cameraIntent, AppConstants.SELECT_MESSAGES_RECORD_VIDEO);
    }

    /**
     * method to launch a video chooser
     */
    private void launchVideoChooser() {
        if (isOpen) {
            isOpen = false;
            animateItems(false);
        }


        if (AppHelper.checkPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            AppHelper.LogCat("Read data permission already granted.");
        } else {
            AppHelper.LogCat("Please request Read data permission.");
            AppHelper.requestPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        }

        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(
                Intent.createChooser(intent, "Choose video"),
                AppConstants.UPLOAD_VIDEO_REQUEST_CODE);
    }

    /**
     * method to launch a document chooser
     */
    private void launchDocumentChooser() {
        if (isOpen) {
            isOpen = false;
            animateItems(false);
        }


        if (AppHelper.checkPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            AppHelper.LogCat("Read data permission already granted.");
        } else {
            AppHelper.LogCat("Please request Read data permission.");
            AppHelper.requestPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        }

        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        try {
            startActivityForResult(
                    Intent.createChooser(intent, "Choose  document"),
                    AppConstants.UPLOAD_DOCUMENT_REQUEST_CODE);
        } catch (ActivityNotFoundException ex) {
            AppHelper.CustomToast(this, "Please install a File Manager.");
        }
    }

    /**
     * method to launch audio chooser
     */
    private void launchAudioChooser() {
        if (isOpen) {
            isOpen = false;
            animateItems(false);
        }
        if (AppHelper.checkPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            AppHelper.LogCat("Read data permission already granted.");
        } else {
            AppHelper.LogCat("Please request Read data permission.");
            AppHelper.requestPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        }

        Intent intent = new Intent();
        intent.setType("audio/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(
                Intent.createChooser(intent, "Choose an audio"),
                AppConstants.UPLOAD_AUDIO_REQUEST_CODE);
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        File fileVideo = null;
        if (resultCode == RESULT_OK) {
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
            switch (requestCode) {
                case AppConstants.UPLOAD_PICTURE_REQUEST_CODE:
                    FileImagePath = FilesManager.getPath(WhatsCloneApplication.getAppContext(), data.getData());
                    sendMessage();
                    break;
                case AppConstants.SELECT_MESSAGES_CAMERA:
                    if (data.getData() != null) {
                        FileImagePath = FilesManager.getPath(WhatsCloneApplication.getAppContext(), data.getData());
                        sendMessage();
                    } else {
                        try {
                            String[] projection = new String[]{MediaStore.Images.ImageColumns._ID, MediaStore.Images.ImageColumns.DATA, MediaStore
                                    .Images.ImageColumns.BUCKET_DISPLAY_NAME, MediaStore.Images.ImageColumns.DATE_TAKEN, MediaStore.Images
                                    .ImageColumns.MIME_TYPE};
                            final Cursor cursor = WhatsCloneApplication.getAppContext().getContentResolver()
                                    .query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, MediaStore.Images.ImageColumns
                                            .DATE_TAKEN + " DESC");

                            if (cursor != null && cursor.moveToFirst()) {
                                String imageLocation = cursor.getString(1);
                                cursor.close();
                                File imageFile = new File(imageLocation);
                                if (imageFile.exists()) {
                                    FileImagePath = imageFile.getPath();
                                }
                            }
                        } catch (Exception e) {
                            AppHelper.LogCat("error" + e);
                        }
                    }
                    break;
                case AppConstants.UPLOAD_VIDEO_REQUEST_CODE:
                    FileVideoPath = FilesManager.getPath(WhatsCloneApplication.getAppContext(), data.getData());
                    Bitmap thumbnailBitmap = ThumbnailUtils.createVideoThumbnail(FileVideoPath, MediaStore.Video.Thumbnails.MINI_KIND);
                    try {
                        fileVideo = FilesManager.getFileThumbnail(thumbnailBitmap);
                    } catch (IOException e) {
                        AppHelper.LogCat("IOException video thumbnail " + e.getMessage());
                    }
                    FileVideoThumbnailPath = FilesManager.getPath(WhatsCloneApplication.getAppContext(), Uri.fromFile(fileVideo));
                    sendMessage();
                    break;
                case AppConstants.SELECT_MESSAGES_RECORD_VIDEO:
                    if (data.getData() != null) {
                        FileVideoPath = FilesManager.getPath(WhatsCloneApplication.getAppContext(), data.getData());
                        thumbnailBitmap = ThumbnailUtils.createVideoThumbnail(FileVideoPath, MediaStore.Video.Thumbnails.MINI_KIND);
                        try {
                            fileVideo = FilesManager.getFileThumbnail(thumbnailBitmap);
                        } catch (IOException e) {
                            AppHelper.LogCat("IOException video thumbnail " + e.getMessage());
                        }
                        FileVideoThumbnailPath = FilesManager.getPath(WhatsCloneApplication.getAppContext(), Uri.fromFile(fileVideo));
                        sendMessage();
                    } else {
                        try {
                            String[] projection = new String[]{MediaStore.Video.VideoColumns._ID, MediaStore.Video.VideoColumns.DATA, MediaStore
                                    .Video.VideoColumns.BUCKET_DISPLAY_NAME, MediaStore.Video.VideoColumns.DATE_TAKEN, MediaStore.Video
                                    .VideoColumns.MIME_TYPE};
                            final Cursor cursor = WhatsCloneApplication.getAppContext().getContentResolver()
                                    .query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection, null, null, MediaStore.Video.VideoColumns
                                            .DATE_TAKEN + " DESC");

                            if (cursor != null && cursor.moveToFirst()) {
                                String videoLocation = cursor.getString(1);
                                cursor.close();
                                File videoFile = new File(videoLocation);
                                if (videoFile.exists()) {
                                    FileVideoPath = videoFile.getPath();
                                    thumbnailBitmap = ThumbnailUtils.createVideoThumbnail(FileVideoPath, MediaStore.Video.Thumbnails.MINI_KIND);
                                    try {
                                        fileVideo = FilesManager.getFileThumbnail(thumbnailBitmap);
                                    } catch (IOException e) {
                                        AppHelper.LogCat("IOException video thumbnail " + e.getMessage());
                                    }
                                    FileVideoThumbnailPath = FilesManager.getPath(WhatsCloneApplication.getAppContext(), Uri.fromFile(fileVideo));
                                    sendMessage();
                                }
                            }
                        } catch (Exception e) {
                            AppHelper.LogCat("Exception videoLocation MessagesActivity " + e);
                        }
                    }
                    break;
                case AppConstants.UPLOAD_AUDIO_REQUEST_CODE:
                    FileAudioPath = FilesManager.getPath(WhatsCloneApplication.getAppContext(), data.getData());
                    sendMessage();
                    break;
                case AppConstants.UPLOAD_DOCUMENT_REQUEST_CODE:
                    FileDocumentPath = FilesManager.getPath(WhatsCloneApplication.getAppContext(), data.getData());
                    sendMessage();
                    break;

            }


        }
    }


    /**
     * method to initialize the massage wrapper
     */
    private void initializerMessageWrapper() {

        final Context context = this;
        messageWrapper.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                InputMethodManager inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }

        });
        messageWrapper.addTextChangedListener(new TextWatcherAdapter() {
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                SendRecordButton.setVisibility(View.VISIBLE);
                SendButton.setVisibility(View.GONE);
                PictureButton.setVisibility(View.VISIBLE);
            }

            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                SendRecordButton.setVisibility(View.GONE);
                SendButton.setVisibility(View.VISIBLE);
                PictureButton.setVisibility(View.GONE);
                if (isGroup) {
                    for (MembersGroupModel membersGroupModel : mGroupsModel.getMembers()) {
                        if (TYPING_TIMER_LENGTH != null) TYPING_TIMER_LENGTH.cancel();
                        if (!isTyping && s.length() != 0) {
                            JSONObject data = new JSONObject();
                            try {
                                data.put("recipientId", membersGroupModel.getUserId());
                                data.put("senderId", senderId);
                                data.put("groupId", groupID);
                            } catch (JSONException e) {
                                AppHelper.LogCat(e);
                            }
                            mSocket.emit(AppConstants.SOCKET_IS_MEMBER_TYPING, data);
                            isTyping = true;

                        }
                    }
                } else {
                    if (TYPING_TIMER_LENGTH != null) TYPING_TIMER_LENGTH.cancel();
                    if (!isTyping && s.length() != 0) {
                        JSONObject data = new JSONObject();
                        try {
                            data.put("recipientId", recipientId);
                            data.put("senderId", senderId);
                        } catch (JSONException e) {
                            AppHelper.LogCat(e);
                        }
                        mSocket.emit(AppConstants.SOCKET_IS_TYPING, data);
                        isTyping = true;

                    }
                }

            }

            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void afterTextChanged(Editable s) {

                if (s.length() == 0) {
                    SendRecordButton.setVisibility(View.VISIBLE);
                    SendButton.setVisibility(View.GONE);
                    PictureButton.setVisibility(View.VISIBLE);
                }
                if (isGroup) {

                    TYPING_TIMER_LENGTH = new Timer();
                    long DELAY = 1000;
                    TYPING_TIMER_LENGTH.schedule(new TimerTask() {
                        @Override
                        public void run() {

                            if (isTyping) {

                                runOnUiThread(() -> {
                                    for (MembersGroupModel membersGroupModel : mGroupsModel.getMembers()) {
                                        JSONObject json = new JSONObject();
                                        try {
                                            json.put("recipientId", membersGroupModel.getUserId());
                                            json.put("senderId", senderId);
                                            json.put("groupId", groupID);
                                        } catch (JSONException e) {
                                            AppHelper.LogCat(e);
                                        }
                                        mSocket.emit(AppConstants.SOCKET_IS_MEMBER_STOP_TYPING, json);
                                        isTyping = false;
                                    }
                                });
                            }
                        }

                    }, DELAY);

                } else {
                    TYPING_TIMER_LENGTH = new Timer();
                    long DELAY = 1000;
                    TYPING_TIMER_LENGTH.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            if (isTyping) {
                                JSONObject json = new JSONObject();
                                try {
                                    json.put("recipientId", recipientId);
                                    json.put("senderId", senderId);
                                } catch (JSONException e) {
                                    AppHelper.LogCat(e);
                                }
                                mSocket.emit(AppConstants.SOCKET_IS_STOP_TYPING, json);
                                isTyping = false;
                            }
                        }

                    }, DELAY);
                }

            }
        });
        messageWrapper.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT);
        messageWrapper.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT);
        messageWrapper.setSingleLine(false);
    }


    /**
     * method to send the new message
     */
    private void sendMessage() {
        EventBus.getDefault().post(new Pusher("startConversation"));//for change viewpager current item to 0
        String messageBody = escapeJavaString(messageWrapper.getText().toString());
        if (messageTransfer != null)
            messageBody = messageTransfer;

        if (FileImagePath == null && FileAudioPath == null && FileDocumentPath == null && FileVideoPath == null) {
            if (messageBody.isEmpty()) return;
        }
        Calendar current = Calendar.getInstance();
        String sendTime = String.valueOf(current.getTime());

        if (isGroup) {
            final JSONObject messageGroup = new JSONObject();
            try {
                messageGroup.put("messageBody", messageBody);
                messageGroup.put("senderId", senderId);
                try {
                    if (mUsersModel.getUsername() != null) {
                        messageGroup.put("senderName", mUsersModel.getUsername());
                    } else {
                        messageGroup.put("senderName", "null");
                    }
                    messageGroup.put("phone", mUsersModel.getPhone());
                    if (mGroupsModel.getGroupImage() != null)
                        messageGroup.put("GroupImage", mGroupsModel.getGroupImage());
                    else
                        messageGroup.put("GroupImage", "null");
                    if (mGroupsModel.getGroupName() != null)
                        messageGroup.put("GroupName", mGroupsModel.getGroupName());
                    else
                        messageGroup.put("GroupName", "null");
                } catch (Exception e) {
                    AppHelper.LogCat(e);
                }

                messageGroup.put("groupID", groupID);
                messageGroup.put("date", sendTime);
                messageGroup.put("isGroup", true);

                if (FileImagePath != null)
                    messageGroup.put("image", FileImagePath);
                else
                    messageGroup.put("image", "null");

                if (FileVideoPath != null)
                    messageGroup.put("video", FileVideoPath);
                else
                    messageGroup.put("video", "null");

                if (FileVideoThumbnailPath != null)
                    messageGroup.put("thumbnail", FileVideoThumbnailPath);
                else
                    messageGroup.put("thumbnail", "null");

                if (FileAudioPath != null)
                    messageGroup.put("audio", FileAudioPath);
                else
                    messageGroup.put("audio", "null");

                if (FileDocumentPath != null)
                    messageGroup.put("document", FileDocumentPath);
                else
                    messageGroup.put("document", "null");

                messageGroup.put("socketId", MainService.getSocketID());
            } catch (JSONException e) {
                AppHelper.LogCat("send group message " + e.getMessage());
            }
            unSentMessagesGroup(groupID);
            new Handler().postDelayed(() -> runOnUiThread(() -> setStatusAsWaiting(messageGroup, true)), 100);
            AppHelper.LogCat("send group message ");

        } else {
            final JSONObject message = new JSONObject();
            try {
                message.put("messageBody", messageBody);
                message.put("recipientId", recipientId);
                message.put("senderId", senderId);
                try {
                    if (mUsersModel.getUsername() != null) {
                        message.put("senderName", mUsersModel.getUsername());
                    } else {
                        message.put("senderName", "null");
                    }
                    if (mUsersModel.getImage() != null)
                        message.put("senderImage", mUsersModel.getImage());
                    else
                        message.put("senderImage", "null");
                    message.put("phone", mUsersModel.getPhone());
                } catch (Exception e) {
                    AppHelper.LogCat(e);
                }


                message.put("date", sendTime);
                message.put("isGroup", false);
                message.put("conversationId", ConversationID);
                if (FileImagePath != null)
                    message.put("image", FileImagePath);
                else
                    message.put("image", "null");

                if (FileVideoPath != null)
                    message.put("video", FileVideoPath);
                else
                    message.put("video", "null");

                if (FileVideoThumbnailPath != null)
                    message.put("thumbnail", FileVideoThumbnailPath);
                else
                    message.put("thumbnail", "null");

                if (FileAudioPath != null)
                    message.put("audio", FileAudioPath);
                else
                    message.put("audio", "null");


                if (FileDocumentPath != null)
                    message.put("document", FileDocumentPath);
                else
                    message.put("document", "null");

            } catch (JSONException e) {
                AppHelper.LogCat("send message " + e.getMessage());
            }
            unSentMessagesForARecipient(recipientId);
            new Handler().postDelayed(() -> runOnUiThread(() -> setStatusAsWaiting(message, false)), 100);
        }
        messageWrapper.setText("");
        messageBody = null;
        messageTransfer = null;


    }

    /**
     * method to check  for unsent messages group
     *
     * @param groupID this parameter of  unSentMessagesGroup  method
     */
    private void unSentMessagesGroup(int groupID) {
        Realm realm = Realm.getDefaultInstance();

        List<MessagesModel> messagesModelsList = realm.where(MessagesModel.class)
                .equalTo("status", AppConstants.IS_WAITING)
                .equalTo("isGroup", true)
                .equalTo("groupID", groupID)
                .equalTo("isFileUpload", true)
                .equalTo("senderID", PreferenceManager.getID(WhatsCloneApplication.getAppContext()))
                .findAllSorted("id", Sort.ASCENDING);
        AppHelper.LogCat("size " + messagesModelsList.size());
        for (MessagesModel messagesModel : messagesModelsList) {
            sendMessagesGroup(messagesModel);
        }
        realm.close();
    }

    /**
     * method to send group messages
     *
     * @param messagesModel this is parameter of sendMessagesGroup method
     */
    private void sendMessagesGroup(MessagesModel messagesModel) {

        JSONObject message = new JSONObject();
        try {

            if (mUsersModel.getUsername() != null) {
                message.put("senderName", mUsersModel.getUsername());
            } else {
                message.put("senderName", "null");
            }
            message.put("phone", mUsersModel.getPhone());
            if (mGroupsModel.getGroupImage() != null)
                message.put("GroupImage", mGroupsModel.getGroupImage());
            else
                message.put("GroupImage", "null");
            if (mGroupsModel.getGroupName() != null)
                message.put("GroupName", mGroupsModel.getGroupName());
            else
                message.put("GroupName", "null");

            message.put("messageBody", messagesModel.getMessage());
            message.put("senderId", messagesModel.getSenderID());
            message.put("groupID", messagesModel.getGroupID());
            message.put("date", messagesModel.getDate());
            message.put("isGroup", true);
            message.put("image", messagesModel.getImageFile());
            message.put("video", messagesModel.getVideoFile());
            message.put("audio", messagesModel.getAudioFile());
            message.put("thumbnail", messagesModel.getVideoThumbnailFile());
            message.put("document", messagesModel.getDocumentFile());

            int senderId = message.getInt("senderId");
            String messageBody = message.getString("messageBody");
            String senderName = message.getString("senderName");
            String senderPhone = message.getString("phone");
            String GroupImage = message.getString("GroupImage");
            String GroupName = message.getString("GroupName");
            String dateTmp = message.getString("date");
            String video = message.getString("video");
            String thumbnail = message.getString("thumbnail");
            boolean isGroup = message.getBoolean("isGroup");
            String image = message.getString("image");
            String audio = message.getString("audio");
            String document = message.getString("document");
            int groupID = message.getInt("groupID");

            mSocket.emit(AppConstants.SOCKET_SAVE_NEW_MESSAGE_GROUP, message, (Ack) argObjects -> {
                JSONObject dataString = (JSONObject) argObjects[0];// TODO hna ghandir condition if lmessage is not saved on the data base bach nbdl dakchi f realm ondir b7al failled and he can try to send it
                runOnUiThread(() -> {
                    List<MembersGroupModel> contactsModelList = mGroupsModel.getMembers();
                    for (int i = 0; i < contactsModelList.size(); i++) {
                        int recipientID = contactsModelList.get(i).getUserId();
                        new Handler().postDelayed(() -> runOnUiThread(() -> {
                            try {
                                int messageId = dataString.getInt("messageId");
                                JSONObject jsonObject = new JSONObject();
                                jsonObject.put("messageId", messageId);
                                jsonObject.put("recipientId", recipientID);
                                jsonObject.put("messageBody", messageBody);
                                jsonObject.put("senderId", senderId);
                                jsonObject.put("senderName", senderName);
                                jsonObject.put("phone", senderPhone);
                                jsonObject.put("GroupImage", GroupImage);
                                jsonObject.put("GroupName", GroupName);
                                jsonObject.put("groupID", groupID);
                                jsonObject.put("date", dateTmp);
                                jsonObject.put("isGroup", isGroup);
                                jsonObject.put("image", image);
                                jsonObject.put("video", video);
                                jsonObject.put("thumbnail", thumbnail);
                                jsonObject.put("audio", audio);
                                jsonObject.put("document", document);


                                mSocket.emit(AppConstants.SOCKET_NEW_MESSAGE_GROUP, jsonObject);
                            } catch (JSONException e) {
                                AppHelper.LogCat("JSONException " + e.getMessage());
                            }

                        }), 200);

                    }
                });


            });
        } catch (JSONException e) {
            AppHelper.LogCat(e.getMessage());
        }
    }


    /**
     * method to check for unsent user messages
     *
     * @param recipientID this is parameter of unSentMessagesForARecipient method
     */
    private void unSentMessagesForARecipient(int recipientID) {
        Realm realm = Realm.getDefaultInstance();
        List<MessagesModel> messagesModelsList = realm.where(MessagesModel.class)
                .equalTo("status", AppConstants.IS_WAITING)
                .equalTo("recipientID", recipientID)
                .equalTo("isFileUpload", true)
                .equalTo("isGroup", false)
                .equalTo("senderID", PreferenceManager.getID(WhatsCloneApplication.getAppContext()))
                .findAllSorted("id", Sort.ASCENDING);
        AppHelper.LogCat("size " + messagesModelsList.size());
        AppHelper.LogCat("list " + messagesModelsList.toString());
        for (MessagesModel messagesModel : messagesModelsList) {
            MainService.sendMessages(messagesModel);
        }
        realm.close();

    }

    /**
     * method to get a conversation id
     *
     * @param recipientId this is the first parameter for getConversationId method
     * @param senderId    this is the second parameter for getConversationId method
     * @param realm       this is the thirded parameter for getConversationId method
     * @return conversation id
     */
    private int getConversationId(int recipientId, int senderId, Realm realm) {
        try {
            ConversationsModel conversationsModelNew = realm.where(ConversationsModel.class)
                    .beginGroup()
                    .equalTo("RecipientID", recipientId)
                    .or()
                    .equalTo("RecipientID", senderId)
                    .endGroup().findFirst();
            return conversationsModelNew.getId();
        } catch (Exception e) {
            AppHelper.LogCat("Get conversation id Exception MessagesActivity " + e.getMessage());
            return 0;
        }
    }


    /**
     * method to save new message as waitng messages
     *
     * @param data    this is the first parameter for setStatusAsWaiting method
     * @param isgroup this is the second parameter for setStatusAsWaiting method
     */
    private void setStatusAsWaiting(JSONObject data, boolean isgroup) {

        try {
            if (isgroup) {

                int senderId = data.getInt("senderId");
                String messageBody = data.getString("messageBody");
                String senderName = data.getString("senderName");
                String senderPhone = data.getString("phone");
                String GroupImage = data.getString("GroupImage");
                String GroupName = data.getString("GroupName");
                String dateTmp = data.getString("date");
                String video = data.getString("video");
                String thumbnail = data.getString("thumbnail");
                boolean isGroup = data.getBoolean("isGroup");
                String image = data.getString("image");
                String audio = data.getString("audio");
                String document = data.getString("document");
                int groupID = data.getInt("groupID");
                realm.executeTransactionAsync(realm1 -> {
                    int lastID = 1;
                    try {

                        MessagesModel messagesModel1 = realm1.where(MessagesModel.class).findAll().last();
                        lastID = messagesModel1.getId() + 1;
                        ConversationsModel conversationsModel = realm1.where(ConversationsModel.class).equalTo("groupID", groupID).findFirst();
                        RealmList<MessagesModel> messagesModelRealmList = conversationsModel.getMessages();
                        MessagesModel messagesModel = new MessagesModel();
                        messagesModel.setId(lastID);
                        messagesModel.setDate(dateTmp);
                        messagesModel.setStatus(AppConstants.IS_WAITING);
                        messagesModel.setUsername(senderName);
                        messagesModel.setSenderID(PreferenceManager.getID(this));
                        messagesModel.setGroup(isGroup);
                        messagesModel.setMessage(messageBody);
                        messagesModel.setGroupID(groupID);
                        messagesModel.setImageFile(image);
                        messagesModel.setVideoFile(video);
                        messagesModel.setAudioFile(audio);
                        messagesModel.setDocumentFile(document);
                        messagesModel.setVideoThumbnailFile(thumbnail);
                        if (!image.equals("null") || !video.equals("null") || !audio.equals("null") || !document.equals("null") || !thumbnail.equals("null")) {
                            messagesModel.setFileUpload(false);

                        } else {
                            messagesModel.setFileUpload(true);
                        }
                        messagesModel.setFileDownLoad(true);
                        messagesModel.setConversationID(conversationsModel.getId());
                        messagesModelRealmList.add(messagesModel);
                        conversationsModel.setLastMessage(messageBody);
                        conversationsModel.setLastMessageId(lastID);
                        conversationsModel.setMessages(messagesModelRealmList);
                        conversationsModel.setStatus(AppConstants.IS_WAITING);
                        conversationsModel.setUnreadMessageCounter("0");
                        realm1.copyToRealmOrUpdate(conversationsModel);
                        runOnUiThread(() -> addMessage(messagesModel));

                    } catch (Exception e) {
                        AppHelper.LogCat("last conversation  ID  group if conversation id = 0 Exception MessagesActivity  " + e.getMessage());
                    }


                }, () -> {
                    if (!image.equals("null") || !video.equals("null") || !audio.equals("null") || !document.equals("null") || !thumbnail.equals("null"))
                        return;
                    mSocket.emit(AppConstants.SOCKET_SAVE_NEW_MESSAGE_GROUP, data, (Ack) argObjects -> {
                        JSONObject dataString = (JSONObject) argObjects[0];//TODO hna ghandir condition if lmessage is not saved on the data base bach nbdl dakchi f realm ondir b7al failled and he can try to send it
                        runOnUiThread(() -> {
                            List<MembersGroupModel> contactsModelList = mGroupsModel.getMembers();
                            for (int i = 0; i < contactsModelList.size(); i++) {
                                int recipientID = contactsModelList.get(i).getUserId();
                                new Handler().postDelayed(() -> runOnUiThread(() -> {
                                    try {
                                        int messageId = dataString.getInt("messageId");
                                        JSONObject jsonObject = new JSONObject();
                                        jsonObject.put("messageId", messageId);
                                        jsonObject.put("recipientId", recipientID);
                                        jsonObject.put("messageBody", messageBody);
                                        jsonObject.put("senderId", senderId);
                                        jsonObject.put("senderName", senderName);
                                        jsonObject.put("phone", senderPhone);
                                        jsonObject.put("GroupImage", GroupImage);
                                        jsonObject.put("GroupName", GroupName);
                                        jsonObject.put("groupID", groupID);
                                        jsonObject.put("date", dateTmp);
                                        jsonObject.put("isGroup", isGroup);
                                        jsonObject.put("image", image);
                                        jsonObject.put("video", video);
                                        jsonObject.put("thumbnail", thumbnail);
                                        jsonObject.put("audio", audio);
                                        jsonObject.put("document", document);


                                        mSocket.emit(AppConstants.SOCKET_NEW_MESSAGE_GROUP, jsonObject);
                                    } catch (JSONException e) {
                                        AppHelper.LogCat("JSONException group " + e.getMessage());
                                    }

                                }), 200);
                            }
                        });


                    });


                }, error -> {
                    AppHelper.LogCat("Save group message failed MessagesActivity " + error.getMessage());
                });


            } else {

                int senderId = data.getInt("senderId");
                int recipientId = data.getInt("recipientId");
                String messageBody = data.getString("messageBody");
                String senderName = data.getString("senderName");
                String dateTmp = data.getString("date");
                String video = data.getString("video");
                String thumbnail = data.getString("thumbnail");
                boolean isGroup = data.getBoolean("isGroup");
                String image = data.getString("image");
                String audio = data.getString("audio");
                String document = data.getString("document");
                String phone = data.getString("phone");

                String recipientName = mUsersModelRecipient.getUsername();
                String recipientImage = mUsersModelRecipient.getImage();
                String recipientPhone = mUsersModelRecipient.getPhone();
                int conversationID = getConversationId(recipientId, senderId, realm);
                if (conversationID == 0) {
                    realm.executeTransactionAsync(realm1 -> {
                        int lastConversationID = 1;
                        int lastID = 1;
                        try {
                            ConversationsModel conversationsModel = realm1.where(ConversationsModel.class).findAll().last();
                            lastConversationID = conversationsModel.getId() + 1;

                            MessagesModel messagesModel1 = realm1.where(MessagesModel.class).findAll().last();
                            lastID = messagesModel1.getId() + 1;

                            AppHelper.LogCat("last ID  message  MessagesActivity " + lastID);

                        } catch (Exception e) {
                            AppHelper.LogCat("last conversation  ID  if conversation id = 0 Exception MessagesActivity " + e.getMessage());
                            lastConversationID = 1;
                        }
                        RealmList<MessagesModel> messagesModelRealmList = new RealmList<MessagesModel>();
                        MessagesModel messagesModel = new MessagesModel();
                        messagesModel.setId(lastID);
                        messagesModel.setUsername(senderName);
                        messagesModel.setRecipientID(recipientId);
                        messagesModel.setDate(dateTmp);
                        messagesModel.setStatus(AppConstants.IS_WAITING);
                        messagesModel.setGroup(isGroup);
                        messagesModel.setSenderID(senderId);
                        messagesModel.setConversationID(lastConversationID);
                        messagesModel.setMessage(messageBody);
                        messagesModel.setImageFile(image);
                        messagesModel.setVideoFile(video);
                        messagesModel.setAudioFile(audio);
                        messagesModel.setDocumentFile(document);
                        messagesModel.setVideoThumbnailFile(thumbnail);
                        if (!image.equals("null") || !video.equals("null") || !audio.equals("null") || !document.equals("null") || !thumbnail.equals("null")) {
                            messagesModel.setFileUpload(false);

                        } else {
                            messagesModel.setFileUpload(true);
                        }
                        messagesModel.setFileDownLoad(true);
                        messagesModel.setPhone(phone);
                        messagesModelRealmList.add(messagesModel);
                        ConversationsModel conversationsModel1 = new ConversationsModel();
                        conversationsModel1.setRecipientID(recipientId);
                        conversationsModel1.setLastMessage(messageBody);
                        conversationsModel1.setRecipientUsername(recipientName);
                        conversationsModel1.setRecipientImage(recipientImage);
                        conversationsModel1.setMessageDate(dateTmp);
                        conversationsModel1.setId(lastConversationID);
                        conversationsModel1.setStatus(AppConstants.IS_WAITING);
                        conversationsModel1.setRecipientPhone(recipientPhone);
                        conversationsModel1.setMessages(messagesModelRealmList);
                        conversationsModel1.setUnreadMessageCounter("0");
                        conversationsModel1.setLastMessageId(lastID);
                        conversationsModel1.setCreatedOnline(true);
                        realm1.copyToRealmOrUpdate(conversationsModel1);
                        ConversationID = lastConversationID;
                        runOnUiThread(() -> addMessage(messagesModel));
                        try {
                            data.put("messageId", lastID);
                        } catch (JSONException e) {
                            AppHelper.LogCat("last id");
                        }
                    }, () -> {
                        if (!image.equals("null") || !video.equals("null") || !audio.equals("null") || !document.equals("null") || !thumbnail.equals("null"))
                            return;
                        mSocket.emit(AppConstants.SOCKET_NEW_MESSAGE, data);
                        mSocket.emit(AppConstants.SOCKET_SAVE_NEW_MESSAGE, data);
                    }, error -> AppHelper.LogCat("Error  conversation id MessagesActivity " + error.getMessage()));


                } else {

                    realm.executeTransactionAsync(realm1 -> {
                        try {


                            MessagesModel messagesModel1 = realm1.where(MessagesModel.class).findAll().last();
                            int lastID = messagesModel1.getId() + 1;

                            AppHelper.LogCat("last ID  message   MessagesActivity" + lastID);
                            ConversationsModel conversationsModel;
                            RealmQuery<ConversationsModel> conversationsModelRealmQuery = realm1.where(ConversationsModel.class).equalTo("id", conversationID);
                            conversationsModel = conversationsModelRealmQuery.findAll().first();
                            MessagesModel messagesModel = new MessagesModel();
                            messagesModel.setId(lastID);
                            messagesModel.setUsername(senderName);
                            messagesModel.setRecipientID(recipientId);
                            messagesModel.setDate(dateTmp);
                            messagesModel.setStatus(AppConstants.IS_WAITING);
                            messagesModel.setGroup(isGroup);
                            messagesModel.setSenderID(senderId);
                            messagesModel.setConversationID(conversationID);
                            messagesModel.setMessage(messageBody);
                            messagesModel.setImageFile(image);
                            messagesModel.setVideoFile(video);
                            messagesModel.setAudioFile(audio);
                            messagesModel.setDocumentFile(document);
                            messagesModel.setVideoThumbnailFile(thumbnail);
                            if (!image.equals("null") || !video.equals("null") || !audio.equals("null") || !document.equals("null") || !thumbnail.equals("null")) {
                                messagesModel.setFileUpload(false);

                            } else {
                                messagesModel.setFileUpload(true);
                            }
                            messagesModel.setFileDownLoad(true);
                            messagesModel.setPhone(phone);
                            conversationsModel.getMessages().add(messagesModel);
                            conversationsModel.setLastMessageId(lastID);
                            conversationsModel.setLastMessage(messageBody);
                            conversationsModel.setMessageDate(dateTmp);
                            conversationsModel.setCreatedOnline(true);
                            realm1.copyToRealmOrUpdate(conversationsModel);
                            runOnUiThread(() -> addMessage(messagesModel));
                            try {
                                data.put("messageId", lastID);
                            } catch (JSONException e) {
                                AppHelper.LogCat("last id");
                            }
                        } catch (Exception e) {
                            AppHelper.LogCat("Exception  last id message  MessagesActivity " + e.getMessage());
                        }
                    }, () -> {
                        if (!image.equals("null") || !video.equals("null") || !audio.equals("null") || !document.equals("null") || !thumbnail.equals("null"))
                            return;
                        mSocket.emit(AppConstants.SOCKET_NEW_MESSAGE, data);
                        mSocket.emit(AppConstants.SOCKET_SAVE_NEW_MESSAGE, data);
                    }, error -> AppHelper.LogCat("Error  last id  MessagesActivity " + error.getMessage()));
                }
            }


        } catch (JSONException e) {
            AppHelper.LogCat("JSONException  MessagesActivity " + e);
        }

        FileAudioPath = null;
        FileVideoPath = null;
        FileDocumentPath = null;
        FileImagePath = null;
        FileVideoThumbnailPath = null;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (isGroup) {
            getMenuInflater().inflate(R.menu.groups_menu, menu);
        } else {
            getMenuInflater().inflate(R.menu.messages_menu, menu);
        }

        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (isGroup) {

            switch (item.getItemId()) {
                case R.id.attach_file:
                    if (!isOpen) {
                        isOpen = true;
                        animateItems(true);

                    } else {
                        isOpen = false;
                        animateItems(false);
                    }
                    break;
                case R.id.search_messages_group:
                    launcherSearchView();
                    break;
                case R.id.view_group:
                    mIntent = new Intent(this, ProfileActivity.class);
                    mIntent.putExtra("groupID", groupID);
                    mIntent.putExtra("isGroup", true);
                    startActivity(mIntent);
                    break;
            }
        } else {

            switch (item.getItemId()) {
                case R.id.attach_file:
                    if (!isOpen) {
                        isOpen = true;
                        animateItems(true);

                    } else {
                        isOpen = false;
                        animateItems(false);
                    }
                    break;
                case R.id.call_contact:
                    if (isOpen) {
                        isOpen = false;
                        animateItems(false);
                    }
                    callContact();
                    break;
                case R.id.search_messages:
                    launcherSearchView();
                    break;
                case R.id.view_contact:
                    mIntent = new Intent(this, ProfileActivity.class);
                    mIntent.putExtra("userID", recipientId);
                    mIntent.putExtra("isGroup", false);
                    startActivity(mIntent);
                    break;
                case R.id.clear_chat:
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage(R.string.clear_chat);

                    builder.setPositiveButton(R.string.Yes, (dialog, whichButton) -> {
                        AppHelper.showDialog(this, getString(R.string.clear_chat));
                        realm.executeTransactionAsync(realm1 -> {
                            RealmResults<MessagesModel> messagesModel1 = realm1.where(MessagesModel.class).equalTo("conversationID", ConversationID).findAll();
                            messagesModel1.deleteAllFromRealm();
                        }, () -> {
                            AppHelper.LogCat("Message Deleted  successfully  MessagesActivity");

                            RealmResults<MessagesModel> messagesModel1 = realm.where(MessagesModel.class).equalTo("conversationID", ConversationID).findAll();
                            if (messagesModel1.size() == 0) {
                                realm.executeTransactionAsync(realm1 -> {
                                    ConversationsModel conversationsModel1 = realm1.where(ConversationsModel.class).equalTo("id", ConversationID).findFirst();
                                    conversationsModel1.deleteFromRealm();
                                }, () -> {
                                    AppHelper.LogCat("Conversation deleted successfully MessagesActivity");
                                    EventBus.getDefault().post(new Pusher("deleteConversation"));
                                    finish();
                                }, error -> {
                                    AppHelper.LogCat("Delete conversation failed  MessagesActivity" + error.getMessage());

                                });
                            } else {
                                MessagesModel lastMessage = realm.where(MessagesModel.class).equalTo("conversationID", ConversationID).findAll().last();
                                realm.executeTransactionAsync(realm1 -> {
                                    ConversationsModel conversationsModel1 = realm1.where(ConversationsModel.class).equalTo("id", ConversationID).findFirst();
                                    conversationsModel1.setLastMessage(lastMessage.getMessage());
                                    conversationsModel1.setLastMessageId(lastMessage.getId());
                                    realm1.copyToRealmOrUpdate(conversationsModel1);
                                }, () -> {
                                    AppHelper.LogCat("Conversation deleted successfully MessagesActivity ");
                                    EventBus.getDefault().post(new Pusher("deleteConversation"));
                                    finish();
                                }, error -> {
                                    AppHelper.LogCat("Delete conversation failed  MessagesActivity" + error.getMessage());

                                });
                            }
                        }, error -> {
                            AppHelper.LogCat("Delete message failed MessagesActivity" + error.getMessage());

                        });
                        AppHelper.hideDialog();

                    });

                    builder.setNegativeButton(R.string.No, (dialog, whichButton) -> {

                    });

                    builder.show();
                    break;


            }
        }
        return true;
    }

    /**
     * method to close the searchview with animation
     */
    @SuppressWarnings("unused")
    @OnClick(R.id.close_btn_search_view)
    public void closeSearchView() {
        final Animation animation = AnimationUtils.loadAnimation(MessagesActivity.this, R.anim.scale_for_button_animtion_exit);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                searchView.setVisibility(View.GONE);
                toolbar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        searchView.startAnimation(animation);
    }

    /**
     * method to clear/reset search view
     */
    @SuppressWarnings("unused")
    @OnClick(R.id.clear_btn_search_view)
    public void clearSearchView() {
        searchInput.setText("");
    }

    private void launcherSearchView() {
        final Animation animation = AnimationUtils.loadAnimation(MessagesActivity.this, R.anim.scale_for_button_animtion_enter);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                searchView.setVisibility(View.VISIBLE);
                toolbar.setVisibility(View.GONE);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        searchView.startAnimation(animation);
    }

    /**
     * method to initialize the search view
     *
     * @param searchInput    this is the  first parameter for initializerSearchView method
     * @param clearSearchBtn this is the second parameter for initializerSearchView method
     */
    public void initializerSearchView(TextInputEditText searchInput, ImageView clearSearchBtn) {

        final Context context = this;
        searchInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                InputMethodManager inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }

        });
        searchInput.addTextChangedListener(new TextWatcherAdapter() {
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                clearSearchBtn.setVisibility(View.GONE);
            }

            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mMessagesAdapter.setString(s.toString());
                Search(s.toString().trim());
                clearSearchBtn.setVisibility(View.VISIBLE);
            }

            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void afterTextChanged(Editable s) {

                if (s.length() == 0) {
                    clearSearchBtn.setVisibility(View.GONE);
                }
            }
        });

    }

    /**
     * method to start searching
     *
     * @param string this  is the parameter for Search method
     */
    public void Search(String string) {
        final List<MessagesModel> filteredModelList;
        filteredModelList = FilterList(string);
        if (filteredModelList.size() != 0) {
            mMessagesAdapter.animateTo(filteredModelList);
            messagesList.scrollToPosition(0);
        }
    }


    /**
     * method to filter the list
     *
     * @param query this is parameter for FilterList method
     * @return this what method will return
     */
    private List<MessagesModel> FilterList(String query) {
        Realm realm = Realm.getDefaultInstance();
        List<MessagesModel> messagesModels = null;
        if (isGroup) {

            messagesModels = realm.where(MessagesModel.class)
                    .contains("message", query, Case.INSENSITIVE)
                    .equalTo("conversationID", ConversationID)
                    .equalTo("isGroup", true).findAllSorted("id", Sort.ASCENDING);
        } else {


            if (ConversationID == 0) {
                try {
                    ConversationsModel conversationsModel = realm.where(ConversationsModel.class)
                            .beginGroup()
                            .equalTo("RecipientID", recipientId)
                            .or()
                            .equalTo("RecipientID", senderId)
                            .endGroup().findAll().first();

                    messagesModels = realm.where(MessagesModel.class)
                            .contains("message", query, Case.INSENSITIVE)
                            .equalTo("conversationID", conversationsModel.getId())
                            .equalTo("isGroup", false)
                            .findAllSorted("id", Sort.ASCENDING);

                } catch (Exception e) {
                    AppHelper.LogCat(" Conversation Exception MessagesActivity" + e.getMessage());
                }
            } else {
                messagesModels = realm.where(MessagesModel.class)
                        .equalTo("conversationID", ConversationID)
                        .contains("message", query, Case.INSENSITIVE)
                        .equalTo("isGroup", false)
                        .findAllSorted("id", Sort.ASCENDING);
            }

        }


        return messagesModels;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    /**
     * method to emit that message are seen by user
     */
    private void emitMessageSeen() {
        JSONObject json = new JSONObject();
        try {
            json.put("recipientId", recipientId);
            json.put("senderId", senderId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mSocket.emit(AppConstants.SOCKET_IS_MESSAGE_SEEN, json);
    }

    /**
     * method to show all user messages
     *
     * @param messagesModels this is parameter for ShowMessages method
     */
    public void ShowMessages(List<MessagesModel> messagesModels) {

        RealmList<MessagesModel> mMessagesList = new RealmList<MessagesModel>();
        for (MessagesModel messagesModel : messagesModels) {
            mMessagesList.add(messagesModel);
            ConversationID = messagesModel.getConversationID();
        }
        mMessagesAdapter.setMessages(mMessagesList);
    }

    /**
     * method to update  contact information
     *
     * @param contactsModels this is parameter for updateContact method
     */
    public void updateContact(ContactsModel contactsModels) {
        mUsersModel = contactsModels;
    }

    /**
     * method to update group information
     *
     * @param groupsModel
     */
    public void updateGroupInfo(GroupsModel groupsModel) {
        mGroupsModel = groupsModel;
        if (groupsModel.getGroupImage() != null) {
            String userId = String.valueOf(groupsModel.getId());
            String name = groupsModel.getGroupName();
            if (FilesManager.isFileImagesGroupExists(FilesManager.getGroupImage(userId, name))) {

                Picasso.with(this)
                        .load(FilesManager.getFileImageGroup(userId, name))
                        .transform(new CropSquareTransformation())
                        .resize(100, 100)
                        .centerCrop()
                        .into(ToolbarImage);
            } else {

                Picasso.with(this)
                        .load(EndPoints.BASE_URL + groupsModel.getGroupImage())
                        .transform(new CropSquareTransformation())
                        .resize(100, 100)
                        .centerCrop()
                        .into(ToolbarImage);
            }

        } else {
            String userId = String.valueOf(groupsModel.getId());
            String name = groupsModel.getGroupName();
            if (FilesManager.isFileImagesGroupExists(FilesManager.getGroupImage(userId, name))) {

                Picasso.with(this)
                        .load(FilesManager.getFileImageGroup(userId, name))
                        .transform(new CropSquareTransformation())
                        .resize(100, 100)
                        .centerCrop()
                        .into(ToolbarImage);
            } else {
                ToolbarImage.setPadding(4, 4, 4, 4);
                ToolbarImage.setImageResource(R.drawable.ic_group_holder_wihte_48dp);
            }

        }
        String name = UtilsString.unescapeJava(groupsModel.getGroupName());
        if (name.length() > 13)
            ToolbarTitle.setText(name.substring(0, 10) + "... " + "");
        else
            ToolbarTitle.setText(name);

        StringBuilder names = new StringBuilder();
        for (int x = 0; x <= groupsModel.getMembers().size() - 1; x++) {
            if (x <= 1) {
                String finalName;
                if (groupsModel.getMembers().get(x).getUserId() == PreferenceManager.getID(this)) {
                    finalName = "You";
                } else {
                    String phone = UtilsPhone.getContactName(this, groupsModel.getMembers().get(x).getPhone());
                    if (phone != null) {
                        finalName = phone.substring(0, 5);
                    } else {
                        finalName = groupsModel.getMembers().get(x).getPhone().substring(0, 5);
                    }

                }
                names.append(finalName);
                names.append(",");
            }

        }
        String groupsNames = UtilsString.removelastString(names.toString());
        lastVu.setVisibility(View.VISIBLE);
        lastVu.setText(groupsNames);


    }

    public void updateContactRecipient(ContactsModel contactsModels) {
        mUsersModelRecipient = contactsModels;

        try {
            String name = UtilsPhone.getContactName(this, contactsModels.getPhone());
            if (name != null) {
                ToolbarTitle.setText(name);
            } else {
                ToolbarTitle.setText(contactsModels.getPhone());
            }

        } catch (Exception e) {
            AppHelper.LogCat(" Recipient username  is null MessagesActivity" + e.getMessage());
        }
        if (contactsModels.getImage() != null) {
            String userId = String.valueOf(contactsModels.getId());
            String name = contactsModels.getUsername();
            if (FilesManager.isFileImagesProfileExists(FilesManager.getProfileImage(userId, name))) {

                Picasso.with(this)
                        .load(FilesManager.getFileImageProfile(userId, name))
                        .transform(new CropSquareTransformation())
                        .resize(100, 100)
                        .centerCrop()
                        .into(ToolbarImage);
            } else {

                Picasso.with(this)
                        .load(EndPoints.BASE_URL + contactsModels.getImage())
                        .transform(new CropSquareTransformation())
                        .resize(100, 100)
                        .centerCrop()
                        .into(ToolbarImage);
            }

        } else {
            String userId = String.valueOf(contactsModels.getId());
            String name = contactsModels.getUsername();
            if (FilesManager.isFileImagesProfileExists(FilesManager.getProfileImage(userId, name))) {

                Picasso.with(this)
                        .load(FilesManager.getFileImageProfile(userId, name))
                        .transform(new CropSquareTransformation())
                        .resize(100, 100)
                        .centerCrop()
                        .into(ToolbarImage);
            } else {
                ToolbarImage.setPadding(2, 2, 2, 2);
                ToolbarImage.setImageResource(R.drawable.ic_user_holder_white_48dp);
            }


        }


    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onBackPressed() {
        if (isOpen) {
            isOpen = false;
            animateItems(false);
        } else if (emoticonShown) {
            emoticonShown = true;
            emojiIconLayout.setVisibility(View.GONE);
            SendMessageLayout.setBackground(getResources().getDrawable(android.R.color.transparent));
        } else {
            super.onBackPressed();
            mMessagesAdapter.stopAudio();
            if (NotificationsManager.getManager()) {
                if (isGroup)
                    NotificationsManager.cancelNotification(groupID);
                else
                    NotificationsManager.cancelNotification(recipientId);
            }
        }


    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!isGroup)
            LastSeenTimeEmit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMessagesPresenter.onDestroy();
        realm.close();
    }

    @Override
    public void onShowLoading() {

    }

    @Override
    public void onHideLoading() {

    }

    @Override
    public void onErrorLoading(Throwable throwable) {
        AppHelper.LogCat("Messages " + throwable.getMessage());
    }

    /**
     * method to connect to the chat sever by socket
     */
    private void connectToChatServer() {
        mSocket = MainService.mSocket;
        if (mSocket == null) {
            MainService.connectToServer();
            mSocket = MainService.mSocket;
        }
        if (mSocket == null) {
            return;
        }
        if (!mSocket.connected()) {
            MainService.connectToServer();
            mSocket = MainService.mSocket;
        }
        setTypingEvent();
        if (isGroup) {
            AppHelper.LogCat("here group seen");
        } else {
            checkIfUserIsOnline();
            emitMessageSeen();
        }


    }


    /**
     * method to check if user is online or not
     */
    private void checkIfUserIsOnline() {
        mSocket.on(AppConstants.SOCKET_IS_ONLINE, args -> runOnUiThread(() -> {
            final JSONObject data = (JSONObject) args[0];
            try {
                int senderID = data.getInt("senderId");
                if (senderID == recipientId) {
                    if (data.getBoolean("connected")) {
                        updateUserStatus(AppConstants.STATUS_USER_CONNECTED, null);
                        unSentMessagesForARecipient(recipientId);
                    } else {
                        updateUserStatus(AppConstants.STATUS_USER_DISCONNECTED, null);
                    }
                }
            } catch (JSONException e) {
                AppHelper.LogCat(e);
            }

        }));
    }

    /**
     * method to set user typing event
     */
    private void setTypingEvent() {
        if (isGroup) {
            mSocket.on(AppConstants.SOCKET_IS_MEMBER_TYPING, args -> runOnUiThread(() -> {

                JSONObject data = (JSONObject) args[0];
                try {

                    int senderID = data.getInt("senderId");
                    ContactsModel contactsModel = realm.where(ContactsModel.class).equalTo("id", senderID).findFirst();
                    String finalName;
                    String name = UtilsPhone.getContactName(this, contactsModel.getPhone());
                    if (name != null) {
                        finalName = name;
                    } else {
                        finalName = contactsModel.getPhone();
                    }
                    int groupId = data.getInt("groupId");
                    if (groupId == groupID) {
                        if (senderID == PreferenceManager.getID(this)) return;
                        updateGroupMemberStatus(AppConstants.STATUS_USER_TYPING, finalName);
                    }

                } catch (Exception e) {
                    AppHelper.LogCat(e);
                }
            }));

            mSocket.on(AppConstants.SOCKET_IS_MEMBER_STOP_TYPING, args -> runOnUiThread(() -> {
                updateGroupMemberStatus(AppConstants.STATUS_USER_STOP_TYPING, null);

            }));
        } else {
            mSocket.on(AppConstants.SOCKET_IS_TYPING, args -> runOnUiThread(() -> {

                JSONObject data = (JSONObject) args[0];
                try {

                    int senderID = data.getInt("senderId");
                    int recipientID = data.getInt("recipientId");
                    if (senderID == recipientId && recipientID == senderId) {
                        updateUserStatus(AppConstants.STATUS_USER_TYPING, null);
                    }

                } catch (Exception e) {
                    AppHelper.LogCat(e);
                }
            }));

            mSocket.on(AppConstants.SOCKET_IS_STOP_TYPING, args -> runOnUiThread(() -> {
                try {
                    JSONObject json = new JSONObject();
                    try {
                        json.put("connected", true);
                        json.put("senderId", senderId);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    mSocket.emit(AppConstants.SOCKET_IS_ONLINE, json);
                } catch (Exception e) {
                    AppHelper.LogCat(e);
                }
            }));
            mSocket.on(AppConstants.SOCKET_IS_LAST_SEEN, args -> runOnUiThread(() -> {
                JSONObject data = (JSONObject) args[0];
                try {
                    int senderID = data.getInt("senderId");
                    int recipientID = data.getInt("recipientId");
                    String lastSeen = data.getString("lastSeen");
                    if (senderID == recipientId && recipientID == senderId) {
                        String lastTime = UtilsTime.convertDateToString(this, UtilsTime.convertStringToDate(lastSeen));
                        updateUserStatus(AppConstants.STATUS_USER_LAST_SEEN, lastTime);
                    }
                } catch (Exception e) {
                    AppHelper.LogCat(e);
                }
            }));
        }


    }

    /**
     * method to emit last seen of conversation
     */
    private void LastSeenTimeEmit() {
        Calendar current = Calendar.getInstance();
        String lastTime = String.valueOf(current.getTime());
        JSONObject data = new JSONObject();
        try {
            data.put("senderId", PreferenceManager.getID(this));
            data.put("recipientId", recipientId);
            data.put("lastSeen", lastTime);
        } catch (JSONException e) {
            AppHelper.LogCat(e);
        }
        mSocket.emit(AppConstants.SOCKET_IS_LAST_SEEN, data);
    }

    /**
     * method to update  group members  to show them on toolbar status
     *
     * @param statusUserTyping this is the first parameter for  updateGroupMemberStatus method
     * @param memberName       this is the second parameter for updateGroupMemberStatus method
     */
    private void updateGroupMemberStatus(int statusUserTyping, String memberName) {
        StringBuilder names = new StringBuilder();
        for (int x = 0; x <= mGroupsModel.getMembers().size() - 1; x++) {
            if (x <= 1) {
                String finalName;
                if (mGroupsModel.getMembers().get(x).getUserId() == PreferenceManager.getID(this)) {
                    finalName = "You";
                } else {
                    String phone = UtilsPhone.getContactName(this, mGroupsModel.getMembers().get(x).getPhone());
                    if (phone != null) {
                        finalName = phone.substring(0, 5);
                    } else {
                        finalName = mGroupsModel.getMembers().get(x).getPhone().substring(0, 5);
                    }

                }
                names.append(finalName);
                names.append(",");
            }

        }
        String groupsNames = UtilsString.removelastString(names.toString());
        switch (statusUserTyping) {
            case AppConstants.STATUS_USER_TYPING:
                lastVu.setVisibility(View.VISIBLE);
                lastVu.setText(memberName + " " + getString(R.string.isTyping));
                break;
            case AppConstants.STATUS_USER_STOP_TYPING:
                lastVu.setVisibility(View.VISIBLE);
                lastVu.setText(groupsNames);
                break;
            default:
                lastVu.setVisibility(View.VISIBLE);
                lastVu.setText(groupsNames);
                break;
        }
    }

    /**
     * method to update user status
     *
     * @param statusUserTyping this is the first parameter for  updateUserStatus method
     * @param lastTime         this is the second parameter for  updateUserStatus method
     */
    private void updateUserStatus(int statusUserTyping, String lastTime) {


        switch (statusUserTyping) {
            case AppConstants.STATUS_USER_TYPING:
                lastVu.setVisibility(View.VISIBLE);
                lastVu.setText(getString(R.string.isTyping));
                AppHelper.LogCat("typing...");
                break;
            case AppConstants.STATUS_USER_DISCONNECTED:
                lastVu.setVisibility(View.VISIBLE);
                lastVu.setText(getString(R.string.isOffline));
                AppHelper.LogCat("Offline...");
                break;
            case AppConstants.STATUS_USER_CONNECTED:
                lastVu.setVisibility(View.VISIBLE);
                lastVu.setText(getString(R.string.isOnline));
                AppHelper.LogCat("Online...");
                break;
            case AppConstants.STATUS_USER_STOP_TYPING:
                break;
            case AppConstants.STATUS_USER_LAST_SEEN:
                lastVu.setVisibility(View.VISIBLE);
                lastVu.setText(getString(R.string.lastSeen) + " " + lastTime);
                break;
            default:
                lastVu.setVisibility(View.VISIBLE);
                lastVu.setText(getString(R.string.isOffline));
                break;
        }
    }

    /**
     * method to check if user is exist
     *
     * @param id    this is the first parameter for checkIfUserIsExist method
     * @param realm this is the second parameter for checkIfUserIsExist method
     * @return this is for what checkIfUserIsExist method will return
     */
    private boolean checkIfUserIsExist(int id, Realm realm) {
        RealmQuery<ContactsModel> query = realm.where(ContactsModel.class).equalTo("id", id);
        return query.count() == 0 ? false : true;

    }

    /**
     * method of EventBus
     *
     * @param pusher this is parameter of onEventMainThread method
     */
    @SuppressWarnings("unused")
    public void onEventMainThread(Pusher pusher) {


        switch (pusher.getAction()) {
            case "new_message":
                MessagesModel messagesModel = pusher.getMessagesModel();
                if (messagesModel.getSenderID() == recipientId && messagesModel.getRecipientID() == senderId) {
                    new Handler().postDelayed(() -> addMessage(messagesModel), 1000);
                    mMessagesPresenter.updateConversationStatus();
                }
                if (!checkIfUserIsExist(recipientId, realm)) {
                    mMessagesPresenter.loadLocalGroupData();
                }

                break;
            case "new_message_group":
                if (isGroup){
                    MessagesModel messagesModel1 = pusher.getMessagesModel();
                    if (messagesModel1.getSenderID() != PreferenceManager.getID(this)) {
                        new Handler().postDelayed(() -> addMessage(messagesModel1), 1000);
                        mMessagesPresenter.updateConversationStatus();
                    }
                }
                break;

            case "messages_delivered":
                if (isGroup) {
                    mMessagesPresenter.loadLocalGroupData();
                } else {
                    mMessagesPresenter.loadLocalData();
                }

                break;
            case "new_message_sent":
                if (isGroup) {
                    mMessagesPresenter.loadLocalGroupData();
                } else {
                    mMessagesPresenter.loadLocalData();
                }
                break;
            case "messages_seen":
                if (isGroup) {
                    mMessagesPresenter.loadLocalGroupData();
                } else {
                    mMessagesPresenter.loadLocalData();
                }

                break;
            case "uploadMessageFiles":
                unSentMessagesForARecipient(pusher.getMessagesModel().getRecipientID());
                break;
            case "ItemIsActivatedMessages":
                AppHelper.LogCat("here it ");
                int idx = messagesList.getChildAdapterPosition(pusher.getView());
                if (actionMode != null) {
                    ToggleSelection(idx);
                    return;
                }
                break;


        }
    }


    /**
     * method to add a new message to list messages
     *
     * @param newMsg this is the parameter for addMessage
     */

    private void addMessage(MessagesModel newMsg) {
        mMessagesAdapter.addMessage(newMsg);
        scrollToBottom();
    }

    /**
     * method to scroll to the bottom of list
     */
    private void scrollToBottom() {
        messagesList.scrollToPosition(mMessagesAdapter.getItemCount() - 1);
    }

    /**
     * method to set teh draging animation for audio layout
     *
     * @param motionEvent this is the first parameter for setDraggingAnimation  method
     * @param view        this the second parameter for  setDraggingAnimation  method
     * @return this is what method will return
     */
    private boolean setDraggingAnimation(MotionEvent motionEvent, View view) {

        sendMessagePanel.setVisibility(View.GONE);
        recordPanel.setVisibility(View.VISIBLE);
        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) slideTextContainer
                    .getLayoutParams();
            params.leftMargin = convertToDp(30);
            slideTextContainer.setLayoutParams(params);
            ViewAudioProxy.setAlpha(slideTextContainer, 1);
            startedDraggingX = -1;
            mStartTime = System.currentTimeMillis();
            startRecording();
            SendRecordButton.getParent().requestDisallowInterceptTouchEvent(true);
            recordPanel.setVisibility(View.VISIBLE);
        } else if (motionEvent.getAction() == MotionEvent.ACTION_UP || motionEvent.getAction() == MotionEvent.ACTION_CANCEL) {
            startedDraggingX = -1;
            recordPanel.setVisibility(View.GONE);
            sendMessagePanel.setVisibility(View.VISIBLE);

            long intervalTime = System.currentTimeMillis() - mStartTime;
            if (intervalTime < MIN_INTERVAL_TIME) {

                messageWrapper.setError(getString(R.string.hold_to_record));
                try {
                    if (FilesManager.isFileRecordExists(FileAudioPath)) {
                        boolean deleted = FilesManager.getFileRecord(FileAudioPath).delete();
                        if (deleted)
                            FileAudioPath = null;
                    }
                } catch (Exception e) {
                    AppHelper.LogCat("Exception record path file  MessagesActivity");
                }
            } else {
                sendMessage();
                FileAudioPath = null;
            }
            stopRecording();
        } else if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
            float x = motionEvent.getX();
            if (x < -distCanMove) {
                AppHelper.LogCat("here we will delete  the file ");
                try {
                    if (FilesManager.isFileRecordExists(FileAudioPath)) {
                        boolean deleted = FilesManager.getFileRecord(FileAudioPath).delete();
                        if (deleted)
                            FileAudioPath = null;
                    }


                } catch (Exception e) {
                    AppHelper.LogCat("Exception exist record  " + e.getMessage());
                }
                FileAudioPath = null;
                stopRecording();
            }
            x = x + ViewAudioProxy.getX(SendRecordButton);
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) slideTextContainer
                    .getLayoutParams();
            if (startedDraggingX != -1) {
                float dist = (x - startedDraggingX);
                params.leftMargin = convertToDp(30) + (int) dist;
                slideTextContainer.setLayoutParams(params);
                float alpha = 1.0f + dist / distCanMove;
                if (alpha > 1) {
                    alpha = 1;
                } else if (alpha < 0) {
                    alpha = 0;
                }
                ViewAudioProxy.setAlpha(slideTextContainer, alpha);
            }
            if (x <= ViewAudioProxy.getX(slideTextContainer) + slideTextContainer.getWidth()
                    + convertToDp(30)) {
                if (startedDraggingX == -1) {
                    startedDraggingX = x;
                    distCanMove = (recordPanel.getMeasuredWidth()
                            - slideTextContainer.getMeasuredWidth() - convertToDp(48)) / 2.0f;
                    if (distCanMove <= 0) {
                        distCanMove = convertToDp(80);
                    } else if (distCanMove > convertToDp(80)) {
                        distCanMove = convertToDp(80);
                    }
                }
            }
            if (params.leftMargin > convertToDp(30)) {
                params.leftMargin = convertToDp(30);
                slideTextContainer.setLayoutParams(params);
                ViewAudioProxy.setAlpha(slideTextContainer, 1);
                startedDraggingX = -1;
            }
        }

        view.onTouchEvent(motionEvent);
        return true;
    }

    /**
     * method to start recording audio
     */
    private void startRecording() {

        if (AppHelper.checkPermission(this, Manifest.permission.RECORD_AUDIO)) {
            AppHelper.LogCat("Record audio permission already granted.");
        } else {
            AppHelper.LogCat("Please request Record audio permission.");
            AppHelper.requestPermission(this, Manifest.permission.RECORD_AUDIO);
        }

        if (AppHelper.checkPermission(this, Manifest.permission.MODIFY_AUDIO_SETTINGS)) {
            AppHelper.LogCat("Record audio permission already granted.");
        } else {
            AppHelper.LogCat("Please request Record audio permission.");
            AppHelper.requestPermission(this, Manifest.permission.MODIFY_AUDIO_SETTINGS);
        }


        if (AppHelper.checkPermission(this, Manifest.permission.VIBRATE)) {
            AppHelper.LogCat("Vibrate permission already granted.");
        } else {
            AppHelper.LogCat("Please request Vibrate permission.");
            AppHelper.requestPermission(this, Manifest.permission.VIBRATE);
        }
        try {
            startRecordingAudio();
            startTime = SystemClock.uptimeMillis();
            recordTimer = new Timer();
            UpdaterTimerTask updaterTimerTask = new UpdaterTimerTask();
            recordTimer.schedule(updaterTimerTask, 1000, 1000);
            vibrate();
        } catch (Exception e) {
            AppHelper.LogCat("IOException start audio " + e.getMessage());
        }


    }


    /**
     * method to stop recording auido
     */
    @SuppressLint("SetTextI18n")
    private void stopRecording() {
        if (recordTimer != null) {
            recordTimer.cancel();
        }
        if (recordTimeText.getText().toString().equals("00:00")) {
            return;
        }
        recordTimeText.setText("00:00");
        vibrate();
        recordPanel.setVisibility(View.GONE);
        sendMessagePanel.setVisibility(View.VISIBLE);
        stopRecordingAudio();


    }

    /**
     * method to initialize the audio for start recording
     *
     * @throws IOException
     */
    @SuppressLint("SetTextI18n")
    private void startRecordingAudio() throws IOException {
        stopRecordingAudio();
        FileAudioPath = FilesManager.getFileRecordPath();
        mMediaRecorder = new MediaRecorder();
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mMediaRecorder.setOutputFile(FileAudioPath);
        mMediaRecorder.setOnErrorListener(errorListener);
        mMediaRecorder.setOnInfoListener(infoListener);
        mMediaRecorder.prepare();
        mMediaRecorder.start();

    }

    /**
     * method to reset and clear media recorder
     */
    private void stopRecordingAudio() {
        try {
            if (mMediaRecorder != null) {
                mMediaRecorder.stop();
                mMediaRecorder.reset();
                mMediaRecorder.release();
                mMediaRecorder = null;
                FileAudioPath = null;
            }
        } catch (Exception e) {
            AppHelper.LogCat("Exception stop recording " + e.getMessage());
        }

    }

    private MediaRecorder.OnErrorListener errorListener = (mr, what, extra) -> AppHelper.LogCat("Error: " + what + ", " + extra);

    private MediaRecorder.OnInfoListener infoListener = (mr, what, extra) -> AppHelper.LogCat("Warning: " + what + ", " + extra);

    /**
     * method to make device vibrate when user start recording
     */
    private void vibrate() {
        try {
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(200);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private int convertToDp(float value) {
        return (int) Math.ceil(1 * value);
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
     * @param position this is parameter for  ToggleSelection method
     */
    private void ToggleSelection(int position) {
        mMessagesAdapter.toggleSelection(position);
        String title = String.format("%s selected", mMessagesAdapter.getSelectedItemCount());
        actionMode.setTitle(title);
    }

    @Override
    public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
        MenuInflater inflater = actionMode.getMenuInflater();
        inflater.inflate(R.menu.select_messages_menu, menu);
        getSupportActionBar().hide();
        if (AppHelper.isAndroid5()) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(AppHelper.getColor(this, R.color.colorGrayDarkerBar));
        }
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {


        switch (menuItem.getItemId()) {
            case R.id.copy_message:
                int currentPosition;
                for (int x = 0; x < mMessagesAdapter.getSelectedItems().size(); x++) {
                    currentPosition = mMessagesAdapter.getSelectedItems().get(x);
                    MessagesModel messagesModel = mMessagesAdapter.getItem(currentPosition);
                    if (AppHelper.copyText(this, messagesModel)) {
                        AppHelper.CustomToast(this, getString(R.string.message_is_copied));
                        if (actionMode != null) {
                            mMessagesAdapter.clearSelections();
                            actionMode.finish();
                            getSupportActionBar().show();
                        }
                    }


                }

                break;
            case R.id.delete_message:

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.message_delete);

                builder.setPositiveButton(R.string.Yes, (dialog, whichButton) -> {
                    AppHelper.showDialog(this, getString(R.string.deleting_chat));
                    for (int x = 0; x < mMessagesAdapter.getSelectedItems().size(); x++) {
                        int currentPosition1 = mMessagesAdapter.getSelectedItems().get(x);
                        MessagesModel messagesModel = mMessagesAdapter.getItem(currentPosition1);
                        int messageId = messagesModel.getId();
                        realm.executeTransactionAsync(realm1 -> {
                            MessagesModel messagesModel1 = realm1.where(MessagesModel.class).equalTo("id", messageId).equalTo("conversationID", ConversationID).findFirst();
                            messagesModel1.deleteFromRealm();
                        }, () -> {
                            AppHelper.LogCat("Message deleted successfully MessagesActivity ");
                            mMessagesAdapter.remove(currentPosition1);
                            RealmResults<MessagesModel> messagesModel1 = realm.where(MessagesModel.class).equalTo("conversationID", ConversationID).findAll();
                            if (messagesModel1.size() == 0) {
                                realm.executeTransactionAsync(realm1 -> {
                                    ConversationsModel conversationsModel1 = realm1.where(ConversationsModel.class).equalTo("id", ConversationID).findFirst();
                                    conversationsModel1.deleteFromRealm();
                                }, () -> {
                                    AppHelper.LogCat("Conversation deleted successfully MessagesActivity ");
                                    EventBus.getDefault().post(new Pusher("deleteConversation"));
                                }, error -> {
                                    AppHelper.LogCat("delete conversation failed MessagesActivity " + error.getMessage());

                                });
                            } else {
                                MessagesModel lastMessage = realm.where(MessagesModel.class).equalTo("conversationID", ConversationID).findAll().last();
                                realm.executeTransactionAsync(realm1 -> {
                                    ConversationsModel conversationsModel1 = realm1.where(ConversationsModel.class).equalTo("id", ConversationID).findFirst();
                                    conversationsModel1.setLastMessage(lastMessage.getMessage());
                                    conversationsModel1.setLastMessageId(lastMessage.getId());
                                    realm1.copyToRealmOrUpdate(conversationsModel1);
                                }, () -> {
                                    AppHelper.LogCat("Conversation deleted successfully  MessagesActivity ");
                                    EventBus.getDefault().post(new Pusher("deleteConversation"));
                                }, error -> {
                                    AppHelper.LogCat("delete conversation failed  MessagesActivity" + error.getMessage());

                                });
                            }
                        }, error -> {
                            AppHelper.LogCat("delete message failed  MessagesActivity" + error.getMessage());

                        });

                    }
                    AppHelper.hideDialog();

                    if (actionMode != null) {
                        mMessagesAdapter.clearSelections();
                        actionMode.finish();
                        getSupportActionBar().show();
                    }

                });

                builder.setNegativeButton(R.string.No, (dialog, whichButton) -> {

                });

                builder.show();

                break;
            case R.id.transfer_message:
                if (mMessagesAdapter.getSelectedItems().size() != 0) {
                    int currentPos;
                    ArrayList<String> messagesModelList = new ArrayList<>();
                    for (int x = 0; x < mMessagesAdapter.getSelectedItems().size(); x++) {
                        currentPos = mMessagesAdapter.getSelectedItems().get(x);
                        MessagesModel messagesModel = mMessagesAdapter.getItem(currentPos);
                        messagesModelList.add(messagesModel.getMessage());
                    }

                    Intent intent = new Intent(this, TransferMessageContactsActivity.class);
                    intent.putExtra("messageCopied", messagesModelList);
                    startActivity(intent);
                    finish();
                }
                break;
            default:
                return false;
        }


        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode actionMode) {
        this.actionMode = null;
        mMessagesAdapter.clearSelections();
        getSupportActionBar().show();
        if (AppHelper.isAndroid5()) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(AppHelper.getColor(this, R.color.colorPrimaryDark));
        }
    }

    @Override
    public void onClick(View view) {

        int position = messagesList.getChildAdapterPosition(view);
        if (actionMode != null) {
            ToggleSelection(position);
            return;
        }
    }


    private class RecyclerViewBenOnGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            return super.onSingleTapConfirmed(e);
        }

        public void onLongPress(MotionEvent e) {
            View view = messagesList.findChildViewUnder(e.getX(), e.getY());

            int currentPosition = messagesList.getChildAdapterPosition(view);
            MessagesModel messagesModel = mMessagesAdapter.getItem(currentPosition);
            if (!messagesModel.isGroup()) {

                if (actionMode != null) {
                    return;
                }
                actionMode = startActionMode(MessagesActivity.this);
                ToggleSelection(currentPosition);
            }
            super.onLongPress(e);
        }

    }


    private class UpdaterTimerTask extends TimerTask {

        @Override
        public void run() {
            long timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
            long timeSwapBuff = 0L;
            long updatedTime = timeSwapBuff + timeInMilliseconds;
            final String recordTime = String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes(updatedTime) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(updatedTime)), TimeUnit.MILLISECONDS.toSeconds(updatedTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(updatedTime)));
            runOnUiThread(() -> {
                try {
                    if (recordTimeText != null)
                        recordTimeText.setText(recordTime);
                } catch (Exception e) {
                    AppHelper.LogCat("Exception record MessagesActivity");
                }

            });
        }
    }


}


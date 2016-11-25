package com.sourcecanyon.whatsClone.adapters.recyclerView.messages;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.rockerhieu.emojicon.EmojiconTextView;
import com.sourcecanyon.whatsClone.R;
import com.sourcecanyon.whatsClone.activities.settings.PreferenceSettingsManager;
import com.sourcecanyon.whatsClone.api.APIService;
import com.sourcecanyon.whatsClone.api.FilesDownloadService;
import com.sourcecanyon.whatsClone.api.FilesUploadService;
import com.sourcecanyon.whatsClone.app.AppConstants;
import com.sourcecanyon.whatsClone.app.EndPoints;
import com.sourcecanyon.whatsClone.helpers.AppHelper;
import com.sourcecanyon.whatsClone.helpers.Files.DownloadFilesHelper;
import com.sourcecanyon.whatsClone.helpers.Files.FilesManager;
import com.sourcecanyon.whatsClone.helpers.Files.UploadFilesHelper;
import com.sourcecanyon.whatsClone.helpers.PreferenceManager;
import com.sourcecanyon.whatsClone.helpers.UtilsPhone;
import com.sourcecanyon.whatsClone.helpers.UtilsTime;
import com.sourcecanyon.whatsClone.interfaces.AudioCallbacks;
import com.sourcecanyon.whatsClone.interfaces.DownloadCallbacks;
import com.sourcecanyon.whatsClone.interfaces.UploadCallbacks;
import com.sourcecanyon.whatsClone.models.messages.FilesResponse;
import com.sourcecanyon.whatsClone.models.messages.MessagesModel;
import com.sourcecanyon.whatsClone.models.users.Pusher;
import com.sourcecanyon.whatsClone.models.users.contacts.ContactsModel;
import com.sourcecanyon.whatsClone.ui.ColorGenerator;
import com.sourcecanyon.whatsClone.ui.CropSquareTransformation;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import io.realm.Realm;
import io.realm.RealmList;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import wseemann.media.FFmpegMediaMetadataRetriever;

import static com.sourcecanyon.whatsClone.helpers.UtilsString.unescapeJava;

/**
 * Created by Abderrahim El imame on 20/02/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class MessagesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Activity mActivity;
    private RealmList<MessagesModel> mMessagesModel;
    private static final int INCOMING_MESSAGES = 0;
    private static final int OUTGOING_MESSAGES = 1;
    private static final int LEFT_MESSAGES = 2;
    private APIService mApiService;
    private Realm realm;
    private MediaPlayer mMediaPlayer;
    private Handler mHandler;
    private String SearchQuery;
    private SparseBooleanArray selectedItems;


    public MessagesAdapter(@NonNull Activity mActivity, Realm realm) {
        this.mActivity = mActivity;
        this.mMessagesModel = new RealmList<>();
        this.mApiService = new APIService(mActivity);
        this.realm = realm;
        this.mHandler = new Handler();
        this.mMediaPlayer = new MediaPlayer();
        this.selectedItems = new SparseBooleanArray();
    }

    public void setMessages(RealmList<MessagesModel> messagesModelList) {
        this.mMessagesModel = messagesModelList;
        notifyDataSetChanged();
    }

    public void addMessage(MessagesModel messagesModel) {
        this.mMessagesModel.add(messagesModel);
        notifyItemInserted(mMessagesModel.size() - 1);
    }


    //Methods for search start
    public void setString(String SearchQuery) {
        this.SearchQuery = SearchQuery;
        notifyDataSetChanged();
    }

    public void animateTo(List<MessagesModel> models) {
        applyAndAnimateRemovals(models);
        applyAndAnimateAdditions(models);
        applyAndAnimateMovedItems(models);
    }

    private void applyAndAnimateRemovals(List<MessagesModel> newModels) {
        for (int i = mMessagesModel.size() - 1; i >= 0; i--) {
            final MessagesModel model = mMessagesModel.get(i);
            if (!newModels.contains(model)) {
                removeItem(i);
            }
        }
    }

    private void applyAndAnimateAdditions(List<MessagesModel> newModels) {
        for (int i = 0, count = newModels.size(); i < count; i++) {
            final MessagesModel model = newModels.get(i);
            if (!mMessagesModel.contains(model)) {
                addItem(i, model);
            }
        }
    }

    private void applyAndAnimateMovedItems(List<MessagesModel> newModels) {
        for (int toPosition = newModels.size() - 1; toPosition >= 0; toPosition--) {
            final MessagesModel model = newModels.get(toPosition);
            final int fromPosition = mMessagesModel.indexOf(model);
            if (fromPosition >= 0 && fromPosition != toPosition) {
                moveItem(fromPosition, toPosition);
            }
        }
    }

    private MessagesModel removeItem(int position) {
        final MessagesModel model = mMessagesModel.remove(position);
        notifyItemRemoved(position);
        return model;
    }

    private void addItem(int position, MessagesModel model) {
        mMessagesModel.add(position, model);
        notifyItemInserted(position);
    }

    private void moveItem(int fromPosition, int toPosition) {
        final MessagesModel model = mMessagesModel.remove(fromPosition);
        mMessagesModel.add(toPosition, model);
        notifyItemMoved(fromPosition, toPosition);
    }
    //Methods for search end

    @Override
    public int getItemViewType(int position) {
        try {
            MessagesModel messagesModel = mMessagesModel.get(position);


            if (messagesModel.getSenderID() == PreferenceManager.getID(mActivity)) {
                if (messagesModel.getMessage().equals("FK") || messagesModel.getMessage().equals("LT")) {
                    return LEFT_MESSAGES;
                } else {
                    return OUTGOING_MESSAGES;

                }
            } else {
                if (messagesModel.getMessage().equals("FK") || messagesModel.getMessage().equals("LT")) {
                    return LEFT_MESSAGES;
                } else {
                    return INCOMING_MESSAGES;
                }

            }

        } catch (Exception e) {
            AppHelper.LogCat("kdoub rminin Exception" + e.getMessage());
            return 0;
        }

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (viewType == INCOMING_MESSAGES) {
            view = LayoutInflater.from(mActivity).inflate(R.layout.bubble_left, parent, false);
            return new MessagesViewHolder(view);
        } else if (viewType == OUTGOING_MESSAGES) {
            view = LayoutInflater.from(mActivity).inflate(R.layout.bubble_right, parent, false);
            return new MessagesViewHolder(view);
        } else {
            view = LayoutInflater.from(mActivity).inflate(R.layout.created_group_view, parent, false);
            return new MessagesViewHolder(view);
        }
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof MessagesViewHolder) {
            MessagesViewHolder mMessagesViewHolder = (MessagesViewHolder) holder;
            MessagesModel messagesModel = this.mMessagesModel.get(position);


            ContactsModel SenderInfo = realm.where(ContactsModel.class).equalTo("id", messagesModel.getSenderID()).findFirst();
            ContactsModel RecipientInfo = realm.where(ContactsModel.class).equalTo("id", messagesModel.getRecipientID()).findFirst();

            boolean isGroup;
            if (messagesModel.isGroup()) {
                isGroup = true;
                if (messagesModel.getSenderID() != PreferenceManager.getID(mActivity)) {
                    try {


                        String name = UtilsPhone.getContactName(mActivity, messagesModel.getPhone());
                        if (name != null) {
                            ColorGenerator generator = ColorGenerator.MATERIAL; // or use DEFAULT
                            // generate random color
                            int color = generator.getColor(name);
                            mMessagesViewHolder.showSenderName();
                            mMessagesViewHolder.setSenderName(name);
                            mMessagesViewHolder.setSenderColor(color);
                        } else {
                            ColorGenerator generator = ColorGenerator.MATERIAL; // or use DEFAULT
                            // generate random color
                            int color = generator.getColor(messagesModel.getPhone());
                            mMessagesViewHolder.showSenderName();
                            mMessagesViewHolder.setSenderName(messagesModel.getPhone());
                            mMessagesViewHolder.setSenderColor(color);
                        }

                        //  }

                    } catch (Exception e) {
                        AppHelper.LogCat("Group username is null" + e.getMessage());
                    }
                }
            } else {
                isGroup = false;
                mMessagesViewHolder.hideSenderName();
            }
            if (messagesModel.getMessage() != null) {
                String message = unescapeJava(messagesModel.getMessage());
                switch (messagesModel.getMessage()) {
                    case "FK":
                        if (isGroup) {
                            if (messagesModel.getSenderID() == PreferenceManager.getID(mActivity)) {
                                mMessagesViewHolder.message.setText(mActivity.getString(R.string.you_created_this_group), TextView.BufferType.NORMAL);
                                mMessagesViewHolder.message.setTextSize(PreferenceSettingsManager.getMessage_font_size(mActivity));
                            } else {
                                String name = UtilsPhone.getContactName(mActivity, messagesModel.getPhone());
                                if (name != null) {
                                    mMessagesViewHolder.message.setText("" + name + mActivity.getString(R.string.he_created_this_group), TextView.BufferType.NORMAL);
                                    mMessagesViewHolder.message.setTextSize(PreferenceSettingsManager.getMessage_font_size(mActivity));
                                } else {
                                    mMessagesViewHolder.message.setText("" + messagesModel.getPhone() + mActivity.getString(R.string.he_created_this_group), TextView.BufferType.NORMAL);
                                    mMessagesViewHolder.message.setTextSize(PreferenceSettingsManager.getMessage_font_size(mActivity));
                                }

                            }

                        }

                        break;
                    case "LT":

                        if (isGroup) {
                            if (messagesModel.getSenderID() != PreferenceManager.getID(mActivity)) {
                                mMessagesViewHolder.message.setText(mActivity.getString(R.string.you_left), TextView.BufferType.NORMAL);
                                mMessagesViewHolder.message.setTextSize(PreferenceSettingsManager.getMessage_font_size(mActivity));
                            } else {
                                String name = UtilsPhone.getContactName(mActivity, messagesModel.getPhone());
                                if (name != null) {
                                    mMessagesViewHolder.message.setText("" + name + mActivity.getString(R.string.he_left), TextView.BufferType.NORMAL);
                                    mMessagesViewHolder.message.setTextSize(PreferenceSettingsManager.getMessage_font_size(mActivity));
                                } else {
                                    mMessagesViewHolder.message.setText("" + messagesModel.getPhone() + mActivity.getString(R.string.he_left), TextView.BufferType.NORMAL);
                                    mMessagesViewHolder.message.setTextSize(PreferenceSettingsManager.getMessage_font_size(mActivity));
                                }


                            }


                        }

                        break;
                    default:
                        SpannableString Message = SpannableString.valueOf(message);
                        if (SearchQuery != null) {
                            int index = TextUtils.indexOf(message.toLowerCase(), SearchQuery.toLowerCase());
                            if (index >= 0) {
                                Message.setSpan(new ForegroundColorSpan(AppHelper.getColor(mActivity, R.color.colorAccent)), index, index + SearchQuery.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                                Message.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), index, index + SearchQuery.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                            }
                            mMessagesViewHolder.message.setText(Message, TextView.BufferType.SPANNABLE);
                            mMessagesViewHolder.message.setTextSize(PreferenceSettingsManager.getMessage_font_size(mActivity));
                        } else {
                            mMessagesViewHolder.message.setText(message, TextView.BufferType.NORMAL);
                            mMessagesViewHolder.message.setTextSize(PreferenceSettingsManager.getMessage_font_size(mActivity));
                        }

                        break;
                }


            } else {
                mMessagesViewHolder.message.setVisibility(View.GONE);
            }

            if (messagesModel.isFileUpload()) {

                if (messagesModel.getImageFile() != null && !messagesModel.getImageFile().equals("null")) {
                    mMessagesViewHolder.message.setVisibility(View.GONE);
                    mMessagesViewHolder.imageLayout.setVisibility(View.VISIBLE);
                    if (messagesModel.getSenderID() == PreferenceManager.getID(mActivity)) {
                        mMessagesViewHolder.showImageFile();
                        mMessagesViewHolder.setImageFile(messagesModel);
                        mMessagesViewHolder.setDateColorExistFile();
                    } else {
                        mMessagesViewHolder.showImageFile();
                        mMessagesViewHolder.setImageFile(messagesModel);
                        mMessagesViewHolder.setDateColorExistFile();
                    }

                } else {
                    mMessagesViewHolder.message.setVisibility(View.VISIBLE);
                    mMessagesViewHolder.imageLayout.setVisibility(View.GONE);
                    if (messagesModel.getSenderID() == PreferenceManager.getID(mActivity)) {
                        mMessagesViewHolder.mProgressUploadImage.setVisibility(View.GONE);
                        mMessagesViewHolder.mProgressUploadImageInitial.setVisibility(View.GONE);
                        mMessagesViewHolder.cancelUploadImage.setVisibility(View.GONE);
                        mMessagesViewHolder.retryUploadImage.setVisibility(View.GONE);
                        mMessagesViewHolder.hideImageFile();
                        mMessagesViewHolder.setDateColor();
                    } else {
                        mMessagesViewHolder.mProgressDownloadImage.setVisibility(View.GONE);
                        mMessagesViewHolder.mProgressDownloadImageInitial.setVisibility(View.GONE);
                        mMessagesViewHolder.cancelDownloadImage.setVisibility(View.GONE);
                        mMessagesViewHolder.downloadImage.setVisibility(View.GONE);
                        mMessagesViewHolder.hideImageFile();
                        mMessagesViewHolder.setDateColor();
                    }
                }

                if (messagesModel.getVideoFile() != null && !messagesModel.getVideoFile().equals("null")
                        && messagesModel.getVideoThumbnailFile() != null && !messagesModel.getVideoThumbnailFile().equals("null")) {
                    mMessagesViewHolder.message.setVisibility(View.GONE);
                    mMessagesViewHolder.videoLayout.setVisibility(View.VISIBLE);
                    if (messagesModel.getSenderID() == PreferenceManager.getID(mActivity)) {
                        mMessagesViewHolder.showVideoThumbnailFile();
                        mMessagesViewHolder.setVideoThumbnailFile(messagesModel);
                        mMessagesViewHolder.setVideoTotalDuration(messagesModel);
                        mMessagesViewHolder.setDateColorExistFile();
                    } else {
                        mMessagesViewHolder.showVideoThumbnailFile();
                        mMessagesViewHolder.setVideoThumbnailFile(messagesModel);
                        mMessagesViewHolder.setVideoTotalDuration(messagesModel);
                        mMessagesViewHolder.setDateColorExistFile();
                    }

                } else {
                    mMessagesViewHolder.message.setVisibility(View.VISIBLE);
                    mMessagesViewHolder.videoLayout.setVisibility(View.GONE);
                    if (messagesModel.getSenderID() == PreferenceManager.getID(mActivity)) {
                        mMessagesViewHolder.mProgressUploadVideo.setVisibility(View.GONE);
                        mMessagesViewHolder.mProgressUploadVideoInitial.setVisibility(View.GONE);
                        mMessagesViewHolder.cancelUploadVideo.setVisibility(View.GONE);
                        mMessagesViewHolder.retryUploadVideo.setVisibility(View.GONE);
                        mMessagesViewHolder.hideVideoThumbnailFile();
                        mMessagesViewHolder.setDateColor();
                    } else {
                        mMessagesViewHolder.mProgressDownloadVideo.setVisibility(View.GONE);
                        mMessagesViewHolder.mProgressDownloadVideoInitial.setVisibility(View.GONE);
                        mMessagesViewHolder.cancelDownloadVideo.setVisibility(View.GONE);
                        mMessagesViewHolder.downloadVideo.setVisibility(View.GONE);
                        mMessagesViewHolder.hideVideoThumbnailFile();
                        mMessagesViewHolder.setDateColor();
                    }
                }


                if (messagesModel.getAudioFile() != null && !messagesModel.getAudioFile().equals("null")) {
                    mMessagesViewHolder.message.setVisibility(View.GONE);
                    mMessagesViewHolder.audioLayout.setVisibility(View.VISIBLE);
                    if (messagesModel.getSenderID() == PreferenceManager.getID(mActivity)) {
                        if (SenderInfo != null)
                            mMessagesViewHolder.setUserAudioImage(SenderInfo.getImage(), String.valueOf(SenderInfo.getId()), SenderInfo.getUsername());
                        else
                            mMessagesViewHolder.setUnregistredUserAudioImage();
                        mMessagesViewHolder.setAudioTotalDurationAudio(messagesModel);
                        mMessagesViewHolder.mProgressUploadAudioInitial.setVisibility(View.GONE);
                        mMessagesViewHolder.mProgressDownloadAudio.setVisibility(View.GONE);
                        mMessagesViewHolder.cancelUploadAudio.setVisibility(View.GONE);
                        mMessagesViewHolder.retryUploadAudio.setVisibility(View.GONE);
                        mMessagesViewHolder.playBtnAudio.setVisibility(View.VISIBLE);
                        mMessagesViewHolder.audioSeekBar.setEnabled(true);

                    } else {
                        if (SenderInfo != null)
                            mMessagesViewHolder.setUserAudioImage(SenderInfo.getImage(), String.valueOf(SenderInfo.getId()), SenderInfo.getUsername());
                        else
                            mMessagesViewHolder.setUnregistredUserAudioImage();
                        mMessagesViewHolder.setAudioTotalDurationAudio(messagesModel);
                        if (messagesModel.isFileDownLoad()) {
                            mMessagesViewHolder.retryDownloadAudio.setVisibility(View.GONE);
                            mMessagesViewHolder.mProgressDownloadAudio.setVisibility(View.GONE);
                            mMessagesViewHolder.mProgressDownloadAudioInitial.setVisibility(View.GONE);
                            mMessagesViewHolder.cancelDownloadAudio.setVisibility(View.GONE);
                            mMessagesViewHolder.playBtnAudio.setVisibility(View.VISIBLE);
                            mMessagesViewHolder.audioSeekBar.setEnabled(true);
                        } else {
                            mMessagesViewHolder.retryDownloadAudio.setVisibility(View.VISIBLE);
                            mMessagesViewHolder.mProgressDownloadAudio.setVisibility(View.GONE);
                            mMessagesViewHolder.mProgressDownloadAudioInitial.setVisibility(View.GONE);
                            mMessagesViewHolder.cancelDownloadAudio.setVisibility(View.GONE);
                            mMessagesViewHolder.playBtnAudio.setVisibility(View.GONE);
                            mMessagesViewHolder.audioSeekBar.setEnabled(false);
                        }
                    }

                } else {
                    mMessagesViewHolder.message.setVisibility(View.VISIBLE);
                    mMessagesViewHolder.audioLayout.setVisibility(View.GONE);
                    if (messagesModel.getSenderID() == PreferenceManager.getID(mActivity)) {
                        mMessagesViewHolder.mProgressUploadAudio.setVisibility(View.GONE);
                        mMessagesViewHolder.mProgressUploadAudioInitial.setVisibility(View.GONE);
                        mMessagesViewHolder.cancelUploadAudio.setVisibility(View.GONE);
                        mMessagesViewHolder.retryUploadAudio.setVisibility(View.GONE);
                        mMessagesViewHolder.playBtnAudio.setVisibility(View.GONE);
                    } else {
                        mMessagesViewHolder.mProgressDownloadAudio.setVisibility(View.GONE);
                        mMessagesViewHolder.mProgressDownloadAudioInitial.setVisibility(View.GONE);
                        mMessagesViewHolder.cancelDownloadAudio.setVisibility(View.GONE);
                        mMessagesViewHolder.retryDownloadAudio.setVisibility(View.GONE);
                        mMessagesViewHolder.playBtnAudio.setVisibility(View.GONE);

                    }
                }
                if (messagesModel.getDocumentFile() != null && !messagesModel.getDocumentFile().equals("null")) {
                    mMessagesViewHolder.message.setVisibility(View.GONE);
                    mMessagesViewHolder.documentLayout.setVisibility(View.VISIBLE);
                    if (messagesModel.getSenderID() == PreferenceManager.getID(mActivity)) {
                        mMessagesViewHolder.setDocumentTitle(messagesModel);
                        mMessagesViewHolder.mProgressUploadDocumentInitial.setVisibility(View.GONE);
                        mMessagesViewHolder.mProgressDownloadDocument.setVisibility(View.GONE);
                        mMessagesViewHolder.cancelUploadDocument.setVisibility(View.GONE);
                        mMessagesViewHolder.retryUploadDocument.setVisibility(View.GONE);

                    } else {
                        mMessagesViewHolder.setDocumentTitle(messagesModel);
                        if (messagesModel.isFileDownLoad()) {
                            mMessagesViewHolder.retryDownloadDocument.setVisibility(View.GONE);
                            mMessagesViewHolder.mProgressDownloadDocument.setVisibility(View.GONE);
                            mMessagesViewHolder.mProgressDownloadDocumentInitial.setVisibility(View.GONE);
                            mMessagesViewHolder.cancelDownloadDocument.setVisibility(View.GONE);
                            mMessagesViewHolder.documentImage.setVisibility(View.VISIBLE);
                        } else {
                            mMessagesViewHolder.retryDownloadDocument.setVisibility(View.VISIBLE);
                            mMessagesViewHolder.mProgressDownloadDocument.setVisibility(View.GONE);
                            mMessagesViewHolder.mProgressDownloadDocumentInitial.setVisibility(View.GONE);
                            mMessagesViewHolder.cancelDownloadDocument.setVisibility(View.GONE);
                        }
                    }

                } else {
                    mMessagesViewHolder.message.setVisibility(View.VISIBLE);
                    mMessagesViewHolder.documentLayout.setVisibility(View.GONE);
                    if (messagesModel.getSenderID() == PreferenceManager.getID(mActivity)) {
                        mMessagesViewHolder.mProgressUploadDocument.setVisibility(View.GONE);
                        mMessagesViewHolder.mProgressUploadDocumentInitial.setVisibility(View.GONE);
                        mMessagesViewHolder.cancelUploadDocument.setVisibility(View.GONE);
                        mMessagesViewHolder.retryUploadDocument.setVisibility(View.GONE);
                    } else {
                        mMessagesViewHolder.mProgressDownloadDocument.setVisibility(View.GONE);
                        mMessagesViewHolder.mProgressDownloadDocumentInitial.setVisibility(View.GONE);
                        mMessagesViewHolder.cancelDownloadDocument.setVisibility(View.GONE);
                        mMessagesViewHolder.retryDownloadDocument.setVisibility(View.GONE);
                    }
                }
            } else {
                if (messagesModel.getImageFile() != null && !messagesModel.getImageFile().equals("null")) {
                    mMessagesViewHolder.message.setVisibility(View.GONE);
                    if (messagesModel.getSenderID() == PreferenceManager.getID(mActivity)) {
                        mMessagesViewHolder.imageLayout.setVisibility(View.VISIBLE);
                        mMessagesViewHolder.showImageFile();
                        mMessagesViewHolder.setImageFileOffline(messagesModel.getImageFile());
                        mMessagesViewHolder.setDateColorExistFile();
                        mMessagesViewHolder.mProgressUploadImageInitial.setVisibility(View.VISIBLE);
                        mMessagesViewHolder.cancelUploadImage.setVisibility(View.VISIBLE);
                        mMessagesViewHolder.mProgressUploadImageInitial.setIndeterminate(true);
                        new Handler().postDelayed(() -> {
                            mMessagesViewHolder.mProgressUploadImageInitial.setVisibility(View.GONE);
                            mMessagesViewHolder.cancelUploadImage.setVisibility(View.GONE);
                            mMessagesViewHolder.retryUploadImage.setVisibility(View.VISIBLE);
                        }, 2000);
                    } else {
                        mMessagesViewHolder.imageLayout.setVisibility(View.VISIBLE);
                        mMessagesViewHolder.showImageFile();
                        mMessagesViewHolder.setImageFileOffline(messagesModel.getImageFile());
                        mMessagesViewHolder.setDateColorExistFile();

                    }
                } else {
                    mMessagesViewHolder.message.setVisibility(View.VISIBLE);
                    if (messagesModel.getSenderID() == PreferenceManager.getID(mActivity)) {
                        mMessagesViewHolder.imageLayout.setVisibility(View.GONE);
                        mMessagesViewHolder.mProgressUploadImage.setVisibility(View.GONE);
                        mMessagesViewHolder.mProgressUploadImageInitial.setVisibility(View.GONE);
                        mMessagesViewHolder.cancelUploadImage.setVisibility(View.GONE);
                        mMessagesViewHolder.retryUploadImage.setVisibility(View.GONE);
                        mMessagesViewHolder.hideImageFile();
                        mMessagesViewHolder.setDateColor();
                    } else {
                        mMessagesViewHolder.imageLayout.setVisibility(View.GONE);
                        mMessagesViewHolder.mProgressDownloadImage.setVisibility(View.GONE);
                        mMessagesViewHolder.mProgressDownloadImageInitial.setVisibility(View.GONE);
                        mMessagesViewHolder.cancelDownloadImage.setVisibility(View.GONE);
                        mMessagesViewHolder.downloadImage.setVisibility(View.GONE);
                        mMessagesViewHolder.hideImageFile();
                        mMessagesViewHolder.setDateColor();
                    }
                }


                if (messagesModel.getVideoFile() != null && !messagesModel.getVideoFile().equals("null")
                        && messagesModel.getVideoThumbnailFile() != null && !messagesModel.getVideoThumbnailFile().equals("null")) {
                    mMessagesViewHolder.message.setVisibility(View.GONE);
                    if (messagesModel.getSenderID() == PreferenceManager.getID(mActivity)) {
                        mMessagesViewHolder.videoLayout.setVisibility(View.VISIBLE);
                        mMessagesViewHolder.showVideoThumbnailFile();
                        mMessagesViewHolder.setVideoThumbnailFileOffline(messagesModel.getVideoThumbnailFile());
                        mMessagesViewHolder.setDateColorExistFile();
                        mMessagesViewHolder.mProgressUploadVideoInitial.setVisibility(View.VISIBLE);
                        mMessagesViewHolder.cancelUploadVideo.setVisibility(View.VISIBLE);
                        mMessagesViewHolder.mProgressUploadVideoInitial.setIndeterminate(true);
                        new Handler().postDelayed(() -> {
                            mMessagesViewHolder.mProgressUploadVideoInitial.setVisibility(View.GONE);
                            mMessagesViewHolder.cancelUploadVideo.setVisibility(View.GONE);
                            mMessagesViewHolder.retryUploadVideo.setVisibility(View.VISIBLE);
                        }, 2000);
                    } else {
                        mMessagesViewHolder.videoLayout.setVisibility(View.VISIBLE);
                        mMessagesViewHolder.showVideoThumbnailFile();
                        mMessagesViewHolder.setVideoThumbnailFileOffline(messagesModel.getVideoThumbnailFile());
                        mMessagesViewHolder.setDateColorExistFile();

                    }
                } else {
                    mMessagesViewHolder.message.setVisibility(View.VISIBLE);
                    if (messagesModel.getSenderID() == PreferenceManager.getID(mActivity)) {
                        mMessagesViewHolder.videoLayout.setVisibility(View.GONE);
                        mMessagesViewHolder.mProgressUploadVideo.setVisibility(View.GONE);
                        mMessagesViewHolder.mProgressUploadVideoInitial.setVisibility(View.GONE);
                        mMessagesViewHolder.cancelUploadVideo.setVisibility(View.GONE);
                        mMessagesViewHolder.retryUploadVideo.setVisibility(View.GONE);
                        mMessagesViewHolder.hideVideoThumbnailFile();
                        mMessagesViewHolder.setDateColor();
                    } else {
                        mMessagesViewHolder.videoLayout.setVisibility(View.GONE);
                        mMessagesViewHolder.mProgressDownloadVideo.setVisibility(View.GONE);
                        mMessagesViewHolder.mProgressDownloadVideoInitial.setVisibility(View.GONE);
                        mMessagesViewHolder.cancelDownloadVideo.setVisibility(View.GONE);
                        mMessagesViewHolder.downloadVideo.setVisibility(View.GONE);
                        mMessagesViewHolder.hideVideoThumbnailFile();
                        mMessagesViewHolder.setDateColor();
                    }
                }
                if (messagesModel.getAudioFile() != null && !messagesModel.getAudioFile().equals("null")) {
                    mMessagesViewHolder.message.setVisibility(View.GONE);
                    if (messagesModel.getSenderID() == PreferenceManager.getID(mActivity)) {
                        mMessagesViewHolder.audioLayout.setVisibility(View.VISIBLE);
                        if (SenderInfo != null)
                            mMessagesViewHolder.setUserAudioImage(SenderInfo.getImage(), String.valueOf(SenderInfo.getId()), SenderInfo.getUsername());
                        else
                            mMessagesViewHolder.setUnregistredUserAudioImage();
                        mMessagesViewHolder.setAudioTotalDurationAudio(messagesModel);
                        mMessagesViewHolder.retryUploadAudio.setVisibility(View.VISIBLE);
                        mMessagesViewHolder.audioSeekBar.setEnabled(false);

                    } else {
                        mMessagesViewHolder.audioLayout.setVisibility(View.VISIBLE);
                        mMessagesViewHolder.setUserAudioImage(RecipientInfo.getImage(), String.valueOf(RecipientInfo.getId()), RecipientInfo.getUsername());
                    }
                } else {
                    mMessagesViewHolder.message.setVisibility(View.VISIBLE);
                    if (messagesModel.getSenderID() == PreferenceManager.getID(mActivity)) {
                        mMessagesViewHolder.audioLayout.setVisibility(View.GONE);
                        mMessagesViewHolder.mProgressUploadAudio.setVisibility(View.GONE);
                        mMessagesViewHolder.mProgressUploadAudioInitial.setVisibility(View.GONE);
                        mMessagesViewHolder.cancelUploadAudio.setVisibility(View.GONE);
                        mMessagesViewHolder.retryUploadAudio.setVisibility(View.GONE);
                    } else {
                        mMessagesViewHolder.audioLayout.setVisibility(View.GONE);
                        mMessagesViewHolder.mProgressDownloadAudio.setVisibility(View.GONE);
                        mMessagesViewHolder.mProgressDownloadAudioInitial.setVisibility(View.GONE);
                        mMessagesViewHolder.cancelDownloadAudio.setVisibility(View.GONE);
                        mMessagesViewHolder.retryDownloadAudio.setVisibility(View.GONE);
                    }
                }
                if (messagesModel.getDocumentFile() != null && !messagesModel.getDocumentFile().equals("null")) {
                    mMessagesViewHolder.message.setVisibility(View.GONE);
                    if (messagesModel.getSenderID() == PreferenceManager.getID(mActivity)) {
                        mMessagesViewHolder.documentLayout.setVisibility(View.VISIBLE);
                        mMessagesViewHolder.setDocumentTitle(messagesModel);
                        mMessagesViewHolder.retryUploadDocument.setVisibility(View.VISIBLE);
                    } else {
                        mMessagesViewHolder.documentLayout.setVisibility(View.VISIBLE);
                        mMessagesViewHolder.setDocumentTitle(messagesModel);
                    }
                } else {
                    mMessagesViewHolder.message.setVisibility(View.VISIBLE);
                    if (messagesModel.getSenderID() == PreferenceManager.getID(mActivity)) {
                        mMessagesViewHolder.documentLayout.setVisibility(View.GONE);
                        mMessagesViewHolder.mProgressUploadDocument.setVisibility(View.GONE);
                        mMessagesViewHolder.mProgressUploadDocumentInitial.setVisibility(View.GONE);
                        mMessagesViewHolder.cancelUploadDocument.setVisibility(View.GONE);
                        mMessagesViewHolder.retryUploadDocument.setVisibility(View.GONE);
                    } else {
                        mMessagesViewHolder.mProgressDownloadDocument.setVisibility(View.GONE);
                        mMessagesViewHolder.mProgressDownloadDocumentInitial.setVisibility(View.GONE);
                        mMessagesViewHolder.cancelDownloadDocument.setVisibility(View.GONE);
                        mMessagesViewHolder.retryDownloadDocument.setVisibility(View.GONE);
                    }
                }

            }

            try {

                String messageDate = UtilsTime.convertDateToString(mActivity, UtilsTime.convertStringToDate(messagesModel.getDate()));
                mMessagesViewHolder.setDate(messageDate);
                if (messagesModel.getSenderID() == PreferenceManager.getID(mActivity)) {
                    mMessagesViewHolder.showSent(messagesModel.getStatus());
                } else {
                    mMessagesViewHolder.hideSent();
                }
            } catch (Exception e) {
                AppHelper.LogCat("Exception time " + e.getMessage());
            }


        }

        holder.itemView.setActivated(selectedItems.get(position, false));
    }

    public void remove(int position) {
        this.mMessagesModel.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public int getItemCount() {
        return mMessagesModel != null ? mMessagesModel.size() : 0;
    }


    public void toggleSelection(int pos) {
        if (selectedItems.get(pos, false)) {

            selectedItems.delete(pos);
        } else {
            selectedItems.put(pos, true);

        }
        notifyItemChanged(pos);
    }

    public void clearSelections() {
        selectedItems.clear();
        notifyDataSetChanged();
    }

    public int getSelectedItemCount() {
        return selectedItems.size();
    }

    public List<Integer> getSelectedItems() {
        List<Integer> items = new ArrayList<>(selectedItems.size());
        for (int i = 0; i < selectedItems.size(); i++) {
            items.add(selectedItems.keyAt(i));
        }
        return items;
    }


    public MessagesModel getItem(int position) {
        return mMessagesModel.get(position);
    }

    public void UpdateMessageItem(MessagesModel messagesModel) {
        for (int i = 0; i < mMessagesModel.size(); i++) {
            MessagesModel model = mMessagesModel.get(i);
            if (messagesModel == model) {
                AppHelper.LogCat("Message  exist");
                notifyItemChanged(i);
            }
        }
    }

    public class MessagesViewHolder extends RecyclerView.ViewHolder implements SeekBar.OnSeekBarChangeListener, View.OnClickListener {
        @Bind(R.id.message_text)
        EmojiconTextView message;
        @Bind(R.id.date_message)
        TextView date;
        @Bind(R.id.sender_name)
        TextView senderName;
        @Bind(R.id.status_messages)
        ImageView statusMessages;
        @Bind(R.id.date_shadow)
        LinearLayout dateShadow;

        //var for  images
        @Bind(R.id.image_layout)
        FrameLayout imageLayout;
        @Bind(R.id.image_file)
        ImageView imageFile;
        @Bind(R.id.progress_bar_upload_image)
        ProgressBar mProgressUploadImage;
        @Bind(R.id.progress_bar_upload_image_init)
        ProgressBar mProgressUploadImageInitial;
        @Bind(R.id.cancel_upload_image)
        ImageButton cancelUploadImage;
        @Bind(R.id.retry_upload_image)
        LinearLayout retryUploadImage;
        @Bind(R.id.progress_bar_download_image)
        ProgressBar mProgressDownloadImage;
        @Bind(R.id.progress_bar_download_image_init)
        ProgressBar mProgressDownloadImageInitial;
        @Bind(R.id.cancel_download_image)
        ImageButton cancelDownloadImage;
        @Bind(R.id.download_image)
        LinearLayout downloadImage;
        @Bind(R.id.file_size_image)
        TextView fileSizeImage;

        //var for upload videos
        @Bind(R.id.video_layout)
        FrameLayout videoLayout;
        @Bind(R.id.video_thumbnail)
        ImageView videoThumbnailFile;
        @Bind(R.id.play_btn_video)
        ImageButton playBtnVideo;
        @Bind(R.id.progress_bar_upload_video)
        ProgressBar mProgressUploadVideo;
        @Bind(R.id.progress_bar_upload_video_init)
        ProgressBar mProgressUploadVideoInitial;
        @Bind(R.id.cancel_upload_video)
        ImageButton cancelUploadVideo;
        @Bind(R.id.retry_upload_video)
        LinearLayout retryUploadVideo;
        @Bind(R.id.progress_bar_download_video)
        ProgressBar mProgressDownloadVideo;
        @Bind(R.id.progress_bar_download_video_init)
        ProgressBar mProgressDownloadVideoInitial;
        @Bind(R.id.cancel_download_video)
        ImageButton cancelDownloadVideo;
        @Bind(R.id.download_video)
        LinearLayout downloadVideo;
        @Bind(R.id.file_size_video)
        TextView fileSizeVideo;

        @Bind(R.id.video_total_duration)
        TextView videoTotalDuration;

        //var for audio
        @Bind(R.id.audio_layout)
        LinearLayout audioLayout;
        @Bind(R.id.audio_user_image)
        ImageView userAudioImage;
        @Bind(R.id.progress_bar_upload_audio)
        ProgressBar mProgressUploadAudio;
        @Bind(R.id.progress_bar_upload_audio_init)
        ProgressBar mProgressUploadAudioInitial;
        @Bind(R.id.cancel_upload_audio)
        ImageButton cancelUploadAudio;
        @Bind(R.id.retry_upload_audio)
        LinearLayout retryUploadAudio;

        @Bind(R.id.retry_upload_audio_button)
        ImageButton retryUploadAudioButton;
        @Bind(R.id.progress_bar_download_audio)
        ProgressBar mProgressDownloadAudio;
        @Bind(R.id.progress_bar_download_audio_init)
        ProgressBar mProgressDownloadAudioInitial;
        @Bind(R.id.cancel_download_audio)
        ImageButton cancelDownloadAudio;
        @Bind(R.id.retry_download_audio)
        LinearLayout retryDownloadAudio;
        @Bind(R.id.retry_download_audio_button)
        ImageButton retryDownloadAudioButton;
        @Bind(R.id.play_btn_audio)
        ImageButton playBtnAudio;
        @Bind(R.id.pause_btn_audio)
        ImageButton pauseBtnAudio;
        @Bind(R.id.audio_progress_bar)
        SeekBar audioSeekBar;
        @Bind(R.id.audio_current_duration)
        TextView audioCurrentDurationAudio;
        @Bind(R.id.audio_total_duration)
        TextView audioTotalDurationAudio;

        //for documents
        @Bind(R.id.document_layout)
        LinearLayout documentLayout;
        @Bind(R.id.progress_bar_upload_document)
        ProgressBar mProgressUploadDocument;
        @Bind(R.id.progress_bar_upload_document_init)
        ProgressBar mProgressUploadDocumentInitial;
        @Bind(R.id.cancel_upload_document)
        ImageButton cancelUploadDocument;
        @Bind(R.id.retry_upload_document)
        LinearLayout retryUploadDocument;
        @Bind(R.id.progress_bar_download_document)
        ProgressBar mProgressDownloadDocument;
        @Bind(R.id.progress_bar_download_document_init)
        ProgressBar mProgressDownloadDocumentInitial;
        @Bind(R.id.cancel_download_document)
        ImageButton cancelDownloadDocument;
        @Bind(R.id.retry_download_document)
        LinearLayout retryDownloadDocument;
        @Bind(R.id.document_title)
        TextView documentTitle;
        @Bind(R.id.document_image)
        ImageButton documentImage;

        @Bind(R.id.document_size)
        TextView fileSizeDocument;

        UploadCallbacks mUploadCallbacks;
        DownloadCallbacks mDownloadCallbacks;
        AudioCallbacks mAudioCallbacks;
        UploadFilesHelper mUploadFilesHelper;

        MessagesViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            //for image upload
            setupProgressBarUploadImage();
            //for video upload
            setupProgressBarUploadVideo();
            //for audio upload
            setupProgressBarUploadAudio();
            //for document upload
            setupProgressBarUploadDocument();

            cancelDownloadImage.setOnClickListener(this);
            downloadImage.setOnClickListener(this);
            cancelUploadImage.setOnClickListener(this);
            retryUploadImage.setOnClickListener(this);
            imageLayout.setOnClickListener(this);

            cancelDownloadVideo.setOnClickListener(this);
            downloadVideo.setOnClickListener(this);
            cancelUploadVideo.setOnClickListener(this);
            retryUploadVideo.setOnClickListener(this);
            videoLayout.setOnClickListener(this);
            playBtnVideo.setOnClickListener(this);

            cancelDownloadAudio.setOnClickListener(this);
            retryDownloadAudioButton.setOnClickListener(this);
            cancelUploadAudio.setOnClickListener(this);
            retryUploadAudioButton.setOnClickListener(this);
            audioSeekBar.setOnSeekBarChangeListener(this);
            playBtnAudio.setOnClickListener(this);
            pauseBtnAudio.setOnClickListener(this);

            cancelDownloadDocument.setOnClickListener(this);
            retryDownloadDocument.setOnClickListener(this);
            cancelUploadDocument.setOnClickListener(this);
            retryUploadDocument.setOnClickListener(this);
            documentTitle.setOnClickListener(this);
            itemView.setOnClickListener(view -> {
                MessagesModel messagesModel = mMessagesModel.get(getAdapterPosition());
                if (messagesModel.isGroup()) {
                    AppHelper.LogCat("This is a group you cannot delete there message now");
                } else {
                    EventBus.getDefault().post(new Pusher("ItemIsActivatedMessages", view));
                }
            });

            mUploadCallbacks = new UploadCallbacks() {
                @Override
                public void onUpdate(int percentage, String type) {
                    AppHelper.LogCat("percentage " + percentage);
                    switch (type) {
                        case "image":
                            mProgressUploadImage.setVisibility(View.VISIBLE);
                            cancelUploadImage.setVisibility(View.VISIBLE);
                            mProgressUploadImageInitial.setVisibility(View.GONE);
                            mProgressUploadImage.setIndeterminate(false);
                            mProgressUploadImage.getIndeterminateDrawable().setColorFilter(AppHelper.getColor(mActivity, R.color.colorAccent), PorterDuff.Mode.SRC_IN);
                            mProgressUploadImage.setProgress(percentage);
                            break;
                        case "video":
                            mProgressUploadVideo.setVisibility(View.VISIBLE);
                            cancelUploadVideo.setVisibility(View.VISIBLE);
                            mProgressUploadVideoInitial.setVisibility(View.GONE);
                            mProgressUploadVideo.setIndeterminate(false);
                            mProgressUploadVideo.getIndeterminateDrawable().setColorFilter(AppHelper.getColor(mActivity, R.color.colorAccent), PorterDuff.Mode.SRC_IN);
                            mProgressUploadVideo.setProgress(percentage);
                            break;
                        case "audio":
                            mProgressUploadAudio.setVisibility(View.VISIBLE);
                            cancelUploadAudio.setVisibility(View.VISIBLE);
                            mProgressUploadAudioInitial.setVisibility(View.GONE);
                            mProgressUploadAudio.setIndeterminate(false);
                            mProgressUploadAudio.getIndeterminateDrawable().setColorFilter(AppHelper.getColor(mActivity, R.color.colorAccent), PorterDuff.Mode.SRC_IN);
                            mProgressUploadAudio.setProgress(percentage);
                            break;
                        case "document":
                            mProgressUploadDocument.setVisibility(View.VISIBLE);
                            cancelUploadDocument.setVisibility(View.VISIBLE);
                            mProgressUploadDocumentInitial.setVisibility(View.GONE);
                            mProgressUploadDocument.setIndeterminate(false);
                            mProgressUploadDocument.getIndeterminateDrawable().setColorFilter(AppHelper.getColor(mActivity, R.color.colorAccent), PorterDuff.Mode.SRC_IN);

                            break;
                    }
                }

                @Override
                public void onError(String type) {
                    AppHelper.LogCat("on error " + type);
                    switch (type) {
                        case "image":
                            mProgressUploadImage.setVisibility(View.GONE);
                            mProgressUploadImageInitial.setVisibility(View.GONE);
                            cancelUploadImage.setVisibility(View.GONE);
                            retryUploadImage.setVisibility(View.VISIBLE);
                            break;
                        case "video":
                            mProgressUploadVideo.setVisibility(View.GONE);
                            mProgressUploadVideoInitial.setVisibility(View.GONE);
                            cancelUploadVideo.setVisibility(View.GONE);
                            retryUploadVideo.setVisibility(View.VISIBLE);
                            break;
                        case "audio":
                            mProgressUploadAudio.setVisibility(View.GONE);
                            mProgressUploadAudioInitial.setVisibility(View.GONE);
                            cancelUploadAudio.setVisibility(View.GONE);
                            playBtnAudio.setVisibility(View.GONE);
                            pauseBtnAudio.setVisibility(View.GONE);
                            audioSeekBar.setEnabled(false);
                            retryUploadAudio.setVisibility(View.VISIBLE);
                            break;
                        case "document":
                            mProgressUploadDocument.setVisibility(View.GONE);
                            mProgressUploadDocumentInitial.setVisibility(View.GONE);
                            cancelUploadDocument.setVisibility(View.GONE);
                            documentImage.setVisibility(View.GONE);
                            retryUploadDocument.setVisibility(View.VISIBLE);
                            break;
                    }
                }

                @Override
                public void onFinish(String type, MessagesModel messagesModel) {
                    AppHelper.LogCat("on finish " + type + "fffo;e " + messagesModel.getImageFile() + "is filefff" + messagesModel.isFileUpload());
                    switch (type) {
                        case "image":
                            mProgressUploadImage.setVisibility(View.GONE);
                            mProgressUploadImageInitial.setVisibility(View.GONE);
                            cancelUploadImage.setVisibility(View.GONE);
                            retryUploadImage.setVisibility(View.GONE);
                            UpdateMessageItem(messagesModel);
                            EventBus.getDefault().post(new Pusher("uploadMessageFiles", messagesModel));
                            break;
                        case "video":
                            mProgressUploadVideo.setVisibility(View.GONE);
                            mProgressUploadVideoInitial.setVisibility(View.GONE);
                            cancelUploadVideo.setVisibility(View.GONE);
                            retryUploadVideo.setVisibility(View.GONE);
                            setVideoThumbnailFile(messagesModel);
                            UpdateMessageItem(messagesModel);
                            EventBus.getDefault().post(new Pusher("uploadMessageFiles", messagesModel));
                            break;
                        case "audio":
                            mProgressUploadAudio.setVisibility(View.GONE);
                            mProgressUploadAudioInitial.setVisibility(View.GONE);
                            cancelUploadAudio.setVisibility(View.GONE);
                            retryUploadAudio.setVisibility(View.GONE);
                            playBtnAudio.setVisibility(View.VISIBLE);
                            audioSeekBar.setEnabled(true);
                            setAudioTotalDurationAudio(messagesModel);
                            UpdateMessageItem(messagesModel);
                            EventBus.getDefault().post(new Pusher("uploadMessageFiles", messagesModel));
                            break;
                        case "document":
                            mProgressUploadDocument.setVisibility(View.GONE);
                            mProgressUploadDocumentInitial.setVisibility(View.GONE);
                            cancelUploadDocument.setVisibility(View.GONE);
                            retryUploadDocument.setVisibility(View.GONE);
                            documentImage.setVisibility(View.VISIBLE);
                            UpdateMessageItem(messagesModel);
                            EventBus.getDefault().post(new Pusher("uploadMessageFiles", messagesModel));
                            break;
                    }
                }

            };

            mDownloadCallbacks = new DownloadCallbacks() {
                @Override
                public void onUpdate(int percentage, String type) {
                    switch (type) {
                        case "image":
                            mProgressDownloadImage.setProgress(percentage);
                            break;
                        case "video":
                            mProgressDownloadVideo.setProgress(percentage);
                            break;
                        case "audio":
                            mProgressDownloadAudio.setProgress(percentage);
                            break;
                        case "document":
                            mProgressDownloadDocument.setProgress(percentage);
                            break;
                    }


                }

                @Override
                public void onError(String type) {
                    switch (type) {
                        case "image":
                            mProgressDownloadImage.setVisibility(View.GONE);
                            mProgressDownloadImageInitial.setVisibility(View.GONE);
                            cancelDownloadImage.setVisibility(View.GONE);
                            downloadImage.setVisibility(View.VISIBLE);
                            break;
                        case "video":
                            mProgressDownloadVideo.setVisibility(View.GONE);
                            mProgressDownloadVideoInitial.setVisibility(View.GONE);
                            cancelDownloadVideo.setVisibility(View.GONE);
                            downloadVideo.setVisibility(View.VISIBLE);
                            break;
                        case "audio":
                            mProgressDownloadAudio.setVisibility(View.GONE);
                            mProgressDownloadAudioInitial.setVisibility(View.GONE);
                            cancelDownloadAudio.setVisibility(View.GONE);
                            retryDownloadAudio.setVisibility(View.VISIBLE);
                            break;
                        case "document":
                            mProgressDownloadDocument.setVisibility(View.GONE);
                            mProgressDownloadDocumentInitial.setVisibility(View.GONE);
                            cancelDownloadDocument.setVisibility(View.GONE);
                            retryDownloadDocument.setVisibility(View.VISIBLE);
                            break;
                    }
                }

                @Override
                public void onFinish(String type) {
                    switch (type) {
                        case "image":
                            mProgressDownloadImage.setVisibility(View.GONE);
                            mProgressDownloadImageInitial.setVisibility(View.GONE);
                            cancelDownloadImage.setVisibility(View.GONE);
                            downloadImage.setVisibility(View.GONE);
                            notifyDataSetChanged();
                            break;
                        case "video":
                            mProgressDownloadVideo.setVisibility(View.GONE);
                            mProgressDownloadVideoInitial.setVisibility(View.GONE);
                            cancelDownloadVideo.setVisibility(View.GONE);
                            downloadVideo.setVisibility(View.GONE);
                            notifyDataSetChanged();
                            break;
                        case "audio":
                            mProgressDownloadAudio.setVisibility(View.GONE);
                            mProgressDownloadAudioInitial.setVisibility(View.GONE);
                            cancelDownloadAudio.setVisibility(View.GONE);
                            retryDownloadAudio.setVisibility(View.GONE);
                            notifyDataSetChanged();
                            break;
                        case "document":
                            mProgressDownloadDocument.setVisibility(View.GONE);
                            mProgressDownloadDocumentInitial.setVisibility(View.GONE);
                            cancelDownloadDocument.setVisibility(View.GONE);
                            retryDownloadDocument.setVisibility(View.GONE);
                            notifyDataSetChanged();
                            break;
                    }
                }
            };
            mAudioCallbacks = new AudioCallbacks() {
                @Override
                public void onUpdate(int percentage) {
                    audioSeekBar.setProgress(percentage);
                    if (percentage == 100)
                        mAudioCallbacks.onStop();
                }

                @Override
                public void onPause() {
                    AppHelper.LogCat("on pause audio");

                }

                @Override
                public void onStop() {
                    AppHelper.LogCat("on stop audio");
                    stopPlayingAudio();
                }

            };

        }


        void setAudioTotalDurationAudio(MessagesModel messagesModel) {
            if (messagesModel.getSenderID() == PreferenceManager.getID(mActivity)) {
                stopPlayingAudio();
                audioCurrentDurationAudio.setVisibility(View.GONE);
                audioTotalDurationAudio.setVisibility(View.VISIBLE);
                String messageId = String.valueOf(messagesModel.getId());
                String messageUsername = messagesModel.getUsername();
                String messageAudioFile = messagesModel.getAudioFile();
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... voids) {
                        String AudioDataSource;

                        if (FilesManager.isFileAudioExists(FilesManager.getSentAudio(messageId, messageUsername))) {
                            AudioDataSource = FilesManager.getFileAudioPath(messageId, messageUsername);
                        } else if (FilesManager.isFileRecordExists(messageAudioFile)) {
                            AudioDataSource = messageAudioFile;
                        } else {
                            AudioDataSource = EndPoints.BASE_URL + messageAudioFile;
                            FilesManager.downloadFilesToDevice(mActivity, messageAudioFile, messageId, messageUsername, "audio");
                        }
                        try {

                            if (AppHelper.isAndroid5()) {
                                FFmpegMediaMetadataRetriever mFFmpegMediaMetadataRetriever = new FFmpegMediaMetadataRetriever();
                                mFFmpegMediaMetadataRetriever.setDataSource(AudioDataSource);
                                String time = mFFmpegMediaMetadataRetriever.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_DURATION);
                                long timeInMilliSecond = Long.parseLong(time);
                                mActivity.runOnUiThread(() -> {
                                    setTotalTime(timeInMilliSecond);
                                });
                                mFFmpegMediaMetadataRetriever.release();
                            }

                        } catch (Exception e) {
                            AppHelper.LogCat("Exception total duration " + e.getMessage());
                        }


                        return null;
                    }
                }.execute();

            } else {
                stopPlayingAudio();
                audioCurrentDurationAudio.setVisibility(View.GONE);
                audioTotalDurationAudio.setVisibility(View.VISIBLE);
                String messageId = String.valueOf(messagesModel.getId());
                String messageUsername = messagesModel.getUsername();
                String messageAudioFile = messagesModel.getAudioFile();
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... voids) {
                        String AudioDataSource;

                        if (FilesManager.isFileAudioExists(FilesManager.getSentAudio(messageId, messageUsername))) {
                            AudioDataSource = FilesManager.getFileAudioPath(messageId, messageUsername);
                        } else {
                            AudioDataSource = EndPoints.BASE_URL + messageAudioFile;
                        }
                        try {
                            if (AppHelper.isAndroid5()) {
                                FFmpegMediaMetadataRetriever mFFmpegMediaMetadataRetriever = new FFmpegMediaMetadataRetriever();
                                mFFmpegMediaMetadataRetriever.setDataSource(AudioDataSource);
                                String time = mFFmpegMediaMetadataRetriever.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_DURATION);
                                long timeInMilliSecond = Long.parseLong(time);
                                mActivity.runOnUiThread(() -> setTotalTime(timeInMilliSecond));
                                mFFmpegMediaMetadataRetriever.release();
                            }
                        } catch (Exception e) {
                            AppHelper.LogCat("Exception total duration " + e.getMessage());
                        }


                        return null;
                    }
                }.execute();

            }

        }

        void setTotalTime(long totalTime) {
            audioTotalDurationAudio.setText(UtilsTime.getFileTime(totalTime));
        }

        void setVideoTotalDuration(MessagesModel messagesModel) {
            if (messagesModel.getSenderID() == PreferenceManager.getID(mActivity)) {
                videoTotalDuration.setVisibility(View.VISIBLE);
                String messageId = String.valueOf(messagesModel.getId());
                String messageUsername = messagesModel.getUsername();
                String messageVideoFile = messagesModel.getVideoFile();
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... voids) {
                        String VideoDataSource;

                        if (FilesManager.isFileVideosExists(FilesManager.getVideo(messageId, messageUsername))) {
                            VideoDataSource = FilesManager.getFileVideoPath(messageId, messageUsername);
                        } else {
                            VideoDataSource = EndPoints.BASE_URL + messageVideoFile;
                            FilesManager.downloadFilesToDevice(mActivity, messageVideoFile, messageId, messageUsername, "video");
                        }
                        try {
                            if (AppHelper.isAndroid5()) {
                                FFmpegMediaMetadataRetriever mFFmpegMediaMetadataRetriever = new FFmpegMediaMetadataRetriever();
                                mFFmpegMediaMetadataRetriever.setDataSource(VideoDataSource);
                                String time = mFFmpegMediaMetadataRetriever.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_DURATION);
                                long timeInMilliSecond = Long.parseLong(time);
                                mActivity.runOnUiThread(() -> setVideoTotalTime(timeInMilliSecond));
                                mFFmpegMediaMetadataRetriever.release();
                            }
                        } catch (Exception e) {
                            AppHelper.LogCat("Exception total duration " + e.getMessage());
                        }


                        return null;
                    }
                }.execute();

            } else {

                videoTotalDuration.setVisibility(View.VISIBLE);
                String messageId = String.valueOf(messagesModel.getId());
                String messageUsername = messagesModel.getUsername();
                String messageVideoFile = messagesModel.getVideoFile();
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... voids) {
                        String VideoDataSource;

                        if (FilesManager.isFileVideosExists(FilesManager.getVideo(messageId, messageUsername))) {
                            VideoDataSource = FilesManager.getFileVideoPath(messageId, messageUsername);
                        } else {
                            VideoDataSource = EndPoints.BASE_URL + messageVideoFile;
                        }
                        try {
                            if (AppHelper.isAndroid5()) {
                                FFmpegMediaMetadataRetriever mFFmpegMediaMetadataRetriever = new FFmpegMediaMetadataRetriever();
                                mFFmpegMediaMetadataRetriever.setDataSource(VideoDataSource);
                                String time = mFFmpegMediaMetadataRetriever.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_DURATION);
                                long timeInMilliSecond = Long.parseLong(time);
                                mActivity.runOnUiThread(() -> setVideoTotalTime(timeInMilliSecond));
                                mFFmpegMediaMetadataRetriever.release();
                            }
                        } catch (Exception e) {
                            AppHelper.LogCat("Exception total duration " + e.getMessage());
                        }


                        return null;
                    }
                }.execute();

            }

        }

        void setVideoTotalTime(long totalTime) {
            videoTotalDuration.setText(UtilsTime.getFileTime(totalTime));
        }

        void setImageFileOffline(String ImageUrl) {
            Glide.with(mActivity)
                    .load(ImageUrl)
                    .into(imageFile);
        }

        void setVideoThumbnailFileOffline(String ImageUrl) {
            Glide.with(mActivity)
                    .load(ImageUrl)
                    .into(videoThumbnailFile);
        }

        void setUnregistredUserAudioImage() {
            userAudioImage.setPadding(2, 2, 2, 2);
            userAudioImage.setImageResource(R.drawable.ic_user_holder_white_48dp);
        }

        void setUserAudioImage(String ImageUrl, String userId, String name) {

            if (FilesManager.isFileImagesProfileExists(FilesManager.getProfileImage(userId, name))) {

                Picasso.with(mActivity)
                        .load(FilesManager.getFileImageProfile(userId, name))
                        .transform(new CropSquareTransformation())
                        .resize(100, 100)
                        .centerCrop()
                        .into(userAudioImage);
            } else {

                Target target = new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        userAudioImage.setImageBitmap(bitmap);
                    }

                    @Override
                    public void onBitmapFailed(Drawable errorDrawable) {
                        userAudioImage.setImageDrawable(errorDrawable);
                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {
                        userAudioImage.setImageDrawable(placeHolderDrawable);
                    }
                };
                Picasso.with(mActivity)
                        .load(EndPoints.BASE_URL + ImageUrl)
                        .transform(new CropSquareTransformation())
                        .resize(100, 100)
                        .centerCrop()
                        .into(target);
            }

        }

        void setDocumentTitle(MessagesModel messagesModel) {

            String documentFile = messagesModel.getDocumentFile();
            String userId = String.valueOf(messagesModel.getId());
            String name = messagesModel.getUsername();
            int senderId = messagesModel.getSenderID();
            boolean isDownLoad = messagesModel.isFileDownLoad();
            String FileSize = messagesModel.getFileSize();
            if (senderId == PreferenceManager.getID(mActivity)) {
                File file;
                if (FilesManager.isFileDocumentsExists(FilesManager.getDocument(userId, name))) {
                    file = FilesManager.getFileDocument(userId, name);
                    String document_title = file.getName();
                    documentTitle.setText(document_title);
                    documentImage.setVisibility(View.VISIBLE);
                } else {
                    if (messagesModel.isFileUpload())
                        documentImage.setVisibility(View.VISIBLE);
                    FilesManager.downloadFilesToDevice(mActivity, documentFile, userId, name, "document");
                    documentTitle.setText(R.string.document);
                }

            } else {
                if (isDownLoad) {
                    documentImage.setVisibility(View.VISIBLE);
                    File file;
                    if (FilesManager.isFileDocumentsExists(FilesManager.getDocument(userId, name))) {
                        file = FilesManager.getFileDocument(userId, name);
                        String document_title = file.getName();
                        documentTitle.setText(document_title);
                    } else {
                        documentTitle.setText(R.string.document);
                    }
                } else {
                    retryDownloadDocument.setVisibility(View.VISIBLE);
                    documentTitle.setText(R.string.document);
                    if (FileSize == null) {
                        try {
                            getFileSize(documentFile, messagesModel);
                        } catch (Exception e) {
                            AppHelper.LogCat("Exception of file size");
                        }

                    } else {
                        fileSizeDocument.setVisibility(View.VISIBLE);
                        fileSizeDocument.setText(String.valueOf(FileSize));
                    }
                }
            }

        }

        void setImageFile(MessagesModel messagesModel) {
            String ImageUrl = messagesModel.getImageFile();
            String userId = String.valueOf(messagesModel.getId());
            String name = messagesModel.getUsername();
            int senderId = messagesModel.getSenderID();
            boolean isDownLoad = messagesModel.isFileDownLoad();
            String FileSize = messagesModel.getFileSize();
            if (senderId == PreferenceManager.getID(mActivity)) {
                if (FilesManager.isFileImagesOtherExists(FilesManager.getOthersSentImage(userId, name))) {
                    Picasso.with(mActivity)
                            .load(FilesManager.getFileImageOther(userId, name))
                            .resize(40, 40)
                            .centerCrop()
                            .placeholder(R.drawable.bg_rect_contact_image_holder)
                            .error(R.drawable.bg_rect_contact_image_holder)
                            .into(imageFile, new com.squareup.picasso.Callback() {
                                @Override
                                public void onSuccess() {
                                    Picasso.with(mActivity)
                                            .load(FilesManager.getFileImageOther(userId, name))
                                            .resize(500, 500)
                                            .centerCrop()
                                            .into(imageFile);
                                }

                                @Override
                                public void onError() {
                                    AppHelper.LogCat("Error to set image");
                                }
                            });
                } else {
                    Target target = new Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                            imageFile.setImageBitmap(bitmap);
                            FilesManager.downloadFilesToDevice(mActivity, ImageUrl, userId, name, "other");
                        }

                        @Override
                        public void onBitmapFailed(Drawable errorDrawable) {
                            imageFile.setImageDrawable(errorDrawable);
                        }

                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {
                            imageFile.setImageDrawable(placeHolderDrawable);
                        }
                    };
                    Picasso.with(mActivity)
                            .load(EndPoints.BASE_URL + ImageUrl)
                            .resize(40, 40)
                            .centerCrop()
                            .placeholder(R.drawable.bg_rect_contact_image_holder)
                            .error(R.drawable.bg_rect_contact_image_holder)
                            .into(imageFile, new com.squareup.picasso.Callback() {
                                @Override
                                public void onSuccess() {
                                    Picasso.with(mActivity)
                                            .load(EndPoints.BASE_URL + ImageUrl)
                                            .resize(500, 500)
                                            .centerCrop()
                                            .into(target);
                                }

                                @Override
                                public void onError() {
                                    AppHelper.LogCat("Error to set image");
                                }
                            });
                }


            } else {
                if (isDownLoad) {
                    if (FilesManager.isFileImagesOtherExists(FilesManager.getOthersSentImage(userId, name))) {
                        Picasso.with(mActivity)
                                .load(FilesManager.getFileImageOther(userId, name))
                                .resize(40, 40)
                                .centerCrop()
                                .placeholder(R.drawable.bg_rect_contact_image_holder)
                                .error(R.drawable.bg_rect_contact_image_holder)
                                .into(imageFile, new com.squareup.picasso.Callback() {
                                    @Override
                                    public void onSuccess() {
                                        Picasso.with(mActivity)
                                                .load(FilesManager.getFileImageOther(userId, name))
                                                .resize(500, 500)
                                                .centerCrop()
                                                .into(imageFile);
                                    }

                                    @Override
                                    public void onError() {
                                        AppHelper.LogCat("Error to set image");
                                    }
                                });
                    } else {
                        Picasso.with(mActivity)
                                .load(EndPoints.BASE_URL + ImageUrl)
                                .resize(40, 40)
                                .centerCrop()
                                .into(imageFile);

                    }

                } else {
                    downloadImage.setVisibility(View.VISIBLE);
                    Picasso.with(mActivity)
                            .load(EndPoints.BASE_URL + ImageUrl)
                            .resize(30, 30)
                            .centerCrop()
                            .placeholder(R.drawable.bg_rect_contact_image_holder)
                            .error(R.drawable.bg_rect_contact_image_holder)
                            .into(imageFile, new com.squareup.picasso.Callback() {
                                @Override
                                public void onSuccess() {
                                    if (FileSize == null) {
                                        try {
                                            getFileSize(ImageUrl, messagesModel);
                                        } catch (Exception e) {
                                            AppHelper.LogCat("Exception of file size");
                                        }

                                    } else {
                                        fileSizeImage.setVisibility(View.VISIBLE);
                                        fileSizeImage.setText(String.valueOf(FileSize));
                                    }
                                }

                                @Override
                                public void onError() {
                                    AppHelper.LogCat("Error to get file size");
                                }
                            });


                }
            }


        }

        void setVideoThumbnailFile(MessagesModel messagesModel) {
            String ImageUrl = messagesModel.getVideoThumbnailFile();
            String videoUrl = messagesModel.getVideoFile();
            String userId = String.valueOf(messagesModel.getId());
            String name = messagesModel.getUsername();
            int senderId = messagesModel.getSenderID();
            boolean isDownLoad = messagesModel.isFileDownLoad();
            String FileSize = messagesModel.getFileSize();
            if (senderId == PreferenceManager.getID(mActivity)) {
                if (FilesManager.isFileImagesOtherExists(FilesManager.getOthersSentImage(userId, name))) {
                    Picasso.with(mActivity)
                            .load(FilesManager.getFileImageOther(userId, name))
                            .resize(40, 40)
                            .centerCrop()
                            .placeholder(R.drawable.bg_rect_contact_image_holder)
                            .error(R.drawable.bg_rect_contact_image_holder)
                            .into(videoThumbnailFile, new com.squareup.picasso.Callback() {
                                @Override
                                public void onSuccess() {
                                    playBtnVideo.setVisibility(View.VISIBLE);
                                    Picasso.with(mActivity)
                                            .load(FilesManager.getFileImageOther(userId, name))
                                            .resize(500, 500)
                                            .centerCrop()
                                            .into(videoThumbnailFile);
                                }

                                @Override
                                public void onError() {
                                    AppHelper.LogCat("Error to set thumbnail");
                                }
                            });
                } else {
                    Target target = new Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                            videoThumbnailFile.setImageBitmap(bitmap);
                            FilesManager.downloadFilesToDevice(mActivity, ImageUrl, userId, name, "other");
                            FilesManager.downloadFilesToDevice(mActivity, videoUrl, userId, name, "video");
                        }

                        @Override
                        public void onBitmapFailed(Drawable errorDrawable) {
                            videoThumbnailFile.setImageDrawable(errorDrawable);
                        }

                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {
                            videoThumbnailFile.setImageDrawable(placeHolderDrawable);
                        }
                    };
                    Picasso.with(mActivity)
                            .load(EndPoints.BASE_URL + ImageUrl)
                            .resize(40, 40)
                            .centerCrop()
                            .placeholder(R.drawable.bg_rect_contact_image_holder)
                            .error(R.drawable.bg_rect_contact_image_holder)
                            .into(videoThumbnailFile, new com.squareup.picasso.Callback() {
                                @Override
                                public void onSuccess() {
                                    playBtnVideo.setVisibility(View.VISIBLE);
                                    Picasso.with(mActivity)
                                            .load(EndPoints.BASE_URL + ImageUrl)
                                            .resize(500, 500)
                                            .centerCrop()
                                            .into(target);
                                }

                                @Override
                                public void onError() {
                                    AppHelper.LogCat("Error to set thumbnail");
                                }
                            });
                }


            } else {
                if (isDownLoad) {
                    if (FilesManager.isFileImagesOtherExists(FilesManager.getOthersSentImage(userId, name))) {
                        playBtnVideo.setVisibility(View.VISIBLE);
                        Picasso.with(mActivity)
                                .load(FilesManager.getFileImageOther(userId, name))
                                .resize(40, 40)
                                .centerCrop()
                                .placeholder(R.drawable.bg_rect_contact_image_holder)
                                .error(R.drawable.bg_rect_contact_image_holder)
                                .into(videoThumbnailFile, new com.squareup.picasso.Callback() {
                                    @Override
                                    public void onSuccess() {
                                        Picasso.with(mActivity)
                                                .load(FilesManager.getFileImageOther(userId, name))
                                                .resize(500, 500)
                                                .centerCrop()
                                                .into(videoThumbnailFile);
                                    }

                                    @Override
                                    public void onError() {
                                        AppHelper.LogCat("Error to set thumbnail");
                                    }
                                });
                    } else {
                        playBtnVideo.setVisibility(View.VISIBLE);
                        Target target = new Target() {
                            @Override
                            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                videoThumbnailFile.setImageBitmap(bitmap);
                                FilesManager.downloadFilesToDevice(mActivity, ImageUrl, userId, name, "other");
                            }

                            @Override
                            public void onBitmapFailed(Drawable errorDrawable) {
                                videoThumbnailFile.setImageDrawable(errorDrawable);
                            }

                            @Override
                            public void onPrepareLoad(Drawable placeHolderDrawable) {
                                videoThumbnailFile.setImageDrawable(placeHolderDrawable);
                            }
                        };
                        Picasso.with(mActivity)
                                .load(EndPoints.BASE_URL + ImageUrl)
                                .resize(40, 40)
                                .centerCrop()
                                .placeholder(R.drawable.bg_rect_contact_image_holder)
                                .error(R.drawable.bg_rect_contact_image_holder)
                                .into(videoThumbnailFile, new com.squareup.picasso.Callback() {
                                    @Override
                                    public void onSuccess() {
                                        Picasso.with(mActivity)
                                                .load(EndPoints.BASE_URL + ImageUrl)
                                                .resize(500, 500)
                                                .centerCrop()
                                                .into(target);
                                    }

                                    @Override
                                    public void onError() {
                                        AppHelper.LogCat("Error to set image");
                                    }
                                });


                    }

                } else {
                    downloadVideo.setVisibility(View.VISIBLE);
                    playBtnVideo.setVisibility(View.GONE);
                    Picasso.with(mActivity)
                            .load(EndPoints.BASE_URL + ImageUrl)
                            .resize(30, 30)
                            .centerCrop()
                            .placeholder(R.drawable.bg_rect_contact_image_holder)
                            .error(R.drawable.bg_rect_contact_image_holder)
                            .into(videoThumbnailFile, new com.squareup.picasso.Callback() {
                                @Override
                                public void onSuccess() {
                                    if (FileSize == null) {
                                        try {
                                            getFileSize(videoUrl, messagesModel);
                                        } catch (Exception e) {
                                            AppHelper.LogCat("Exception of file size");
                                        }

                                    } else {
                                        fileSizeVideo.setVisibility(View.VISIBLE);
                                        fileSizeVideo.setText(String.valueOf(FileSize));
                                    }
                                }

                                @Override
                                public void onError() {
                                    AppHelper.LogCat("Error to get file size");
                                }
                            });


                }
            }


        }


        void setDateColorExistFile() {
            date.setTextColor(AppHelper.getColor(mActivity, R.color.colorWhite));
            dateShadow.setVisibility(View.VISIBLE);
        }

        void setDateColor() {
            date.setTextColor(AppHelper.getColor(mActivity, R.color.colorGrayStatus));
            dateShadow.setVisibility(View.GONE);
        }


        private void setupProgressBarUploadImage() {
            mProgressUploadImage.getIndeterminateDrawable().setColorFilter(AppHelper.getColor(mActivity, R.color.colorWhite), PorterDuff.Mode.SRC_IN);
            mProgressUploadImageInitial.getIndeterminateDrawable().setColorFilter(AppHelper.getColor(mActivity, R.color.colorWhite), PorterDuff.Mode.SRC_IN);
        }

        private void setupProgressBarUploadVideo() {
            mProgressUploadVideo.getIndeterminateDrawable().setColorFilter(AppHelper.getColor(mActivity, R.color.colorWhite), PorterDuff.Mode.SRC_IN);
            mProgressUploadVideoInitial.getIndeterminateDrawable().setColorFilter(AppHelper.getColor(mActivity, R.color.colorWhite), PorterDuff.Mode.SRC_IN);
        }

        private void setupProgressBarUploadAudio() {
            mProgressUploadAudio.getIndeterminateDrawable().setColorFilter(AppHelper.getColor(mActivity, R.color.colorAccent), PorterDuff.Mode.SRC_IN);
            mProgressUploadAudioInitial.getIndeterminateDrawable().setColorFilter(AppHelper.getColor(mActivity, R.color.colorAccent), PorterDuff.Mode.SRC_IN);
        }

        private void setupProgressBarUploadDocument() {
            mProgressUploadDocument.getIndeterminateDrawable().setColorFilter(AppHelper.getColor(mActivity, R.color.colorAccent), PorterDuff.Mode.SRC_IN);
            mProgressUploadDocumentInitial.getIndeterminateDrawable().setColorFilter(AppHelper.getColor(mActivity, R.color.colorAccent), PorterDuff.Mode.SRC_IN);
        }

        private void setupProgressBarDownloadImage() {
            mProgressDownloadImage.getIndeterminateDrawable().setColorFilter(AppHelper.getColor(mActivity, R.color.colorWhite), PorterDuff.Mode.SRC_IN);
            mProgressDownloadImageInitial.getIndeterminateDrawable().setColorFilter(AppHelper.getColor(mActivity, R.color.colorWhite), PorterDuff.Mode.SRC_IN);
        }

        private void setupProgressBarDownloadVideo() {
            mProgressDownloadVideo.getIndeterminateDrawable().setColorFilter(AppHelper.getColor(mActivity, R.color.colorWhite), PorterDuff.Mode.SRC_IN);
            mProgressDownloadVideoInitial.getIndeterminateDrawable().setColorFilter(AppHelper.getColor(mActivity, R.color.colorWhite), PorterDuff.Mode.SRC_IN);
        }

        private void setupProgressBarDownloadAudio() {
            mProgressDownloadAudioInitial.getIndeterminateDrawable().setColorFilter(AppHelper.getColor(mActivity, R.color.colorAccent), PorterDuff.Mode.SRC_IN);
            mProgressDownloadAudio.getIndeterminateDrawable().setColorFilter(AppHelper.getColor(mActivity, R.color.colorAccent), PorterDuff.Mode.SRC_IN);
        }

        private void setupProgressBarDownloadDocument() {
            mProgressDownloadDocumentInitial.getIndeterminateDrawable().setColorFilter(AppHelper.getColor(mActivity, R.color.colorAccent), PorterDuff.Mode.SRC_IN);
            mProgressDownloadDocument.getIndeterminateDrawable().setColorFilter(AppHelper.getColor(mActivity, R.color.colorAccent), PorterDuff.Mode.SRC_IN);
        }

        void hideImageFile() {
            imageFile.setVisibility(View.GONE);
            mProgressUploadImage.setVisibility(View.GONE);
            mProgressUploadImageInitial.setVisibility(View.GONE);
        }

        void showImageFile() {
            imageFile.setVisibility(View.VISIBLE);

        }

        void hideVideoThumbnailFile() {
            videoThumbnailFile.setVisibility(View.GONE);
            mProgressUploadVideo.setVisibility(View.GONE);
            mProgressUploadVideoInitial.setVisibility(View.GONE);
        }

        void showVideoThumbnailFile() {
            videoThumbnailFile.setVisibility(View.VISIBLE);

        }

        void setDate(String Date) {
            date.setText(Date);
        }

        void setSenderName(String SendName) {
            senderName.setText(SendName);
        }

        void setSenderColor(int Sendcolor) {
            senderName.setTextColor(Sendcolor);
        }

        void hideSenderName() {
            senderName.setVisibility(View.GONE);
        }

        void showSenderName() {
            senderName.setVisibility(View.VISIBLE);
        }

        void hideSent() {
            statusMessages.setVisibility(View.GONE);
        }

        void showSent(int status) {
            statusMessages.setVisibility(View.VISIBLE);
            switch (status) {
                case AppConstants.IS_WAITING:
                    statusMessages.setImageResource(R.drawable.ic_access_time_gray_24dp);
                    break;
                case AppConstants.IS_SENT:
                    statusMessages.setImageResource(R.drawable.ic_done_gray_24dp);
                    break;
                case AppConstants.IS_DELIVERED:
                    statusMessages.setImageResource(R.drawable.ic_done_all_gray_24dp);
                    break;
                case AppConstants.IS_SEEN:
                    statusMessages.setImageResource(R.drawable.ic_done_all_blue_24dp);
                    break;

            }

        }

        void getFileSize(String fileUrl, MessagesModel messagesModel) {
            final FilesDownloadService downloadService = mApiService.RootService(FilesDownloadService.class, PreferenceManager.getToken(mActivity), EndPoints.BASE_URL);
            Call<ResponseBody> downloadResponseCall = downloadService.downloadLargeFileSizeUrlSync(fileUrl);
            new AsyncTask<Void, Long, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    downloadResponseCall.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            if (response.isSuccessful()) {
                                if (response.body() != null) {
                                    long filesSize = response.body().contentLength();
                                    Realm realm = Realm.getDefaultInstance();
                                    realm.executeTransaction(realm1 -> {
                                        MessagesModel messagesModel1 = realm1.where(MessagesModel.class).equalTo("id", messagesModel.getId()).findFirst();
                                        messagesModel1.setFileSize(FilesManager.getFileSize(filesSize));
                                        realm1.copyToRealmOrUpdate(messagesModel1);
                                        UpdateMessageItem(messagesModel1);
                                    });
                                    realm.close();
                                }
                            } else {
                                AppHelper.LogCat("Failed to contact server");

                            }

                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            AppHelper.LogCat("downloadImage failed " + t.getMessage());

                        }


                    });
                    return null;
                }
            }.execute();
        }

        void downloadFile(MessagesModel messagesModel) {

            String fileUrl = null;
            String type = "null";

            if (messagesModel.getImageFile() != null && !messagesModel.getImageFile().equals("null")) {
                downloadImage.setVisibility(View.GONE);
                type = "image";
                fileUrl = messagesModel.getImageFile();
            } else if (messagesModel.getVideoFile() != null && !messagesModel.getVideoFile().equals("null")) {
                type = "video";
                fileUrl = messagesModel.getVideoFile();
            } else if (messagesModel.getAudioFile() != null && !messagesModel.getAudioFile().equals("null")) {
                retryDownloadAudio.setVisibility(View.GONE);
                type = "audio";
                fileUrl = messagesModel.getAudioFile();
            } else if (messagesModel.getDocumentFile() != null && !messagesModel.getDocumentFile().equals("null")) {
                retryDownloadDocument.setVisibility(View.GONE);
                type = "document";
                fileUrl = messagesModel.getDocumentFile();
            }

            final FilesDownloadService downloadService = mApiService.RootService(FilesDownloadService.class, PreferenceManager.getToken(mActivity), EndPoints.BASE_URL);
            String finalFileUrl = fileUrl;
            Call<ResponseBody> downloadResponseCall = downloadService.downloadLargeFileSizeUrlSync(finalFileUrl);
            String finalType = type;
            int messageId = messagesModel.getId();
            String username = messagesModel.getUsername();
            new AsyncTask<Void, Long, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    downloadResponseCall.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            if (response.isSuccessful()) {
                                AppHelper.LogCat("server contacted and has file");
                                DownloadFilesHelper downloadFilesHelper = new DownloadFilesHelper(response.body(), String.valueOf(messageId), username, finalType, mDownloadCallbacks);
                                boolean writtenToDisk = downloadFilesHelper.writeResponseBodyToDisk();
                                if (writtenToDisk) {
                                    new Handler().postDelayed(() -> {
                                        Realm realm = Realm.getDefaultInstance();
                                        realm.executeTransactionAsync(realm1 -> {
                                                    MessagesModel messagesModel1 = realm1.where(MessagesModel.class).equalTo("id", messageId).findFirst();
                                                    messagesModel1.setFileDownLoad(true);
                                                    realm1.copyToRealmOrUpdate(messagesModel1);
                                                }, () -> mDownloadCallbacks.onFinish(finalType)
                                                , error -> {
                                                    mDownloadCallbacks.onError(finalType);
                                                });
                                        realm.close();

                                    }, 2000);

                                } else {
                                    mDownloadCallbacks.onError(finalType);
                                }

                                switch (finalType) {
                                    case "image":
                                        mProgressDownloadImageInitial.setVisibility(View.GONE);
                                        mProgressDownloadImage.setVisibility(View.VISIBLE);
                                        cancelDownloadImage.setVisibility(View.VISIBLE);
                                        mProgressDownloadImage.setIndeterminate(false);
                                        mProgressDownloadImage.getIndeterminateDrawable().setColorFilter(AppHelper.getColor(mActivity, R.color.colorAccent), PorterDuff.Mode.SRC_IN);
                                        break;
                                    case "video":
                                        mProgressDownloadVideoInitial.setVisibility(View.GONE);
                                        mProgressDownloadVideo.setVisibility(View.VISIBLE);
                                        cancelDownloadVideo.setVisibility(View.VISIBLE);
                                        mProgressDownloadVideo.setIndeterminate(false);
                                        mProgressDownloadVideo.getIndeterminateDrawable().setColorFilter(AppHelper.getColor(mActivity, R.color.colorAccent), PorterDuff.Mode.SRC_IN);
                                        break;
                                    case "audio":
                                        mProgressDownloadAudioInitial.setVisibility(View.GONE);
                                        mProgressDownloadAudio.setVisibility(View.VISIBLE);
                                        cancelDownloadAudio.setVisibility(View.VISIBLE);
                                        mProgressDownloadAudio.setIndeterminate(false);
                                        mProgressDownloadAudio.getIndeterminateDrawable().setColorFilter(AppHelper.getColor(mActivity, R.color.colorAccent), PorterDuff.Mode.SRC_IN);
                                        break;
                                    case "document":
                                        mProgressDownloadDocumentInitial.setVisibility(View.GONE);
                                        mProgressDownloadDocument.setVisibility(View.VISIBLE);
                                        cancelDownloadDocument.setVisibility(View.VISIBLE);
                                        mProgressDownloadDocument.setIndeterminate(false);
                                        mProgressDownloadDocument.getIndeterminateDrawable().setColorFilter(AppHelper.getColor(mActivity, R.color.colorAccent), PorterDuff.Mode.SRC_IN);
                                        break;
                                }

                            } else {
                                AppHelper.LogCat("server contact failed");
                                mDownloadCallbacks.onError(finalType);
                            }

                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            AppHelper.LogCat("download is failed " + t.getMessage());
                            mDownloadCallbacks.onError(finalType);
                        }


                    });
                    return null;
                }
            }.execute();

            cancelDownloadImage.setOnClickListener(view -> {
                downloadResponseCall.cancel();
                cancelDownloadImage.setVisibility(View.GONE);
                downloadImage.setVisibility(View.VISIBLE);
                mProgressDownloadImage.setVisibility(View.GONE);
                mProgressDownloadImageInitial.setVisibility(View.GONE);
            });

            cancelDownloadAudio.setOnClickListener(view -> {
                downloadResponseCall.cancel();
                cancelDownloadAudio.setVisibility(View.GONE);
                downloadVideo.setVisibility(View.VISIBLE);
                mProgressDownloadVideo.setVisibility(View.GONE);
                mProgressDownloadVideoInitial.setVisibility(View.GONE);
            });

            cancelDownloadDocument.setOnClickListener(view -> {
                downloadResponseCall.cancel();
                cancelDownloadDocument.setVisibility(View.GONE);
                retryDownloadDocument.setVisibility(View.VISIBLE);
                mProgressDownloadDocument.setVisibility(View.GONE);
                mProgressDownloadDocumentInitial.setVisibility(View.GONE);
            });

            cancelDownloadVideo.setOnClickListener(view -> {
                downloadResponseCall.cancel();
                cancelDownloadVideo.setVisibility(View.GONE);
                downloadVideo.setVisibility(View.VISIBLE);
                mProgressDownloadVideo.setVisibility(View.GONE);
                mProgressDownloadVideoInitial.setVisibility(View.GONE);
            });

        }

        void uploadFile(MessagesModel messagesModel) {

            File file;
            try {
                String type = "null";
                if (messagesModel.getImageFile() != null && !messagesModel.getImageFile().equals("null")) {
                    type = "image";
                } else if (messagesModel.getVideoFile() != null && !messagesModel.getVideoFile().equals("null")) {
                    type = "video";
                } else if (messagesModel.getAudioFile() != null && !messagesModel.getAudioFile().equals("null")) {
                    type = "audio";
                } else if (messagesModel.getDocumentFile() != null && !messagesModel.getDocumentFile().equals("null")) {
                    type = "document";
                }
                switch (type) {
                    case "image":
                        file = new File(messagesModel.getImageFile());
                        mUploadFilesHelper = new UploadFilesHelper(file, mUploadCallbacks, "image/*", type);
                        uploadFileRequest(messagesModel, mUploadFilesHelper, "image");
                        break;
                    case "video":
                        file = new File(messagesModel.getVideoFile());
                        mUploadFilesHelper = new UploadFilesHelper(file, mUploadCallbacks, "video/*", type);
                        uploadFileRequest(messagesModel, mUploadFilesHelper, "video");
                        break;
                    case "audio":
                        file = new File(messagesModel.getAudioFile());
                        mUploadFilesHelper = new UploadFilesHelper(file, mUploadCallbacks, "audio/*", type);
                        uploadFileRequest(messagesModel, mUploadFilesHelper, "audio");
                        break;
                    case "document":
                        file = new File(messagesModel.getDocumentFile());
                        mUploadFilesHelper = new UploadFilesHelper(file, mUploadCallbacks, "application/pdf", type);
                        uploadFileRequest(messagesModel, mUploadFilesHelper, "document");
                        break;

                }
            } catch (Exception e) {
                AppHelper.LogCat("failed to select a type file " + e.getMessage());
            }


        }


        void uploadFileRequest(MessagesModel messagesModel, UploadFilesHelper uploadFilesHelper, String type) {
            FilesUploadService filesUploadService = mApiService.RootService(FilesUploadService.class, PreferenceManager.getToken(mActivity), EndPoints.BASE_URL);
            Call<FilesResponse> filesResponseCall;
            int messageId = messagesModel.getId();
            switch (type) {
                case "image":
                    filesResponseCall = filesUploadService.uploadMessageImage(uploadFilesHelper);
                    new AsyncTask<Void, Long, Void>() {
                        @Override
                        protected Void doInBackground(Void... voids) {
                            filesResponseCall.enqueue(new Callback<FilesResponse>() {
                                                          @Override
                                                          public void onResponse(Call<FilesResponse> call, Response<FilesResponse> response) {
                                                              if (response.isSuccessful()) {
                                                                  if (response.body().isSuccess()) {
                                                                      AppHelper.LogCat("url image " + response.body().getUrl());
                                                                      Realm realm = Realm.getDefaultInstance();
                                                                      realm.executeTransactionAsync(realm1 -> {
                                                                                  MessagesModel messagesModel1 = realm1.where(MessagesModel.class).equalTo("id", messageId).findFirst();
                                                                                  messagesModel1.setFileUpload(true);
                                                                                  messagesModel1.setImageFile(response.body().getUrl());

                                                                                  realm1.copyToRealmOrUpdate(messagesModel1);
                                                                              }, () -> {
                                                                                  MessagesModel messagesModel1 = realm.where(MessagesModel.class).equalTo("id", messageId).findFirst();
                                                                                  mUploadCallbacks.onFinish("image", messagesModel1);
                                                                                  AppHelper.LogCat("finish realm image");
                                                                              }
                                                                              , error -> {
                                                                                  AppHelper.LogCat("error realm image");
                                                                                  mUploadCallbacks.onError(type);
                                                                              });


                                                                      realm.close();
                                                                  } else {
                                                                      AppHelper.LogCat("failed to upload image " + response.body().getUrl());
                                                                      mUploadCallbacks.onError(type);
                                                                  }
                                                              } else

                                                              {
                                                                  AppHelper.LogCat("failed to upload image  ");
                                                                  mUploadCallbacks.onError(type);
                                                              }
                                                          }

                                                          @Override
                                                          public void onFailure
                                                                  (Call<FilesResponse> call, Throwable t) {
                                                              AppHelper.LogCat("failed to upload image  " + t.getMessage());
                                                              mUploadCallbacks.onError(type);

                                                          }
                                                      }

                            );


                            return null;
                        }
                    }.execute();
                    cancelUploadImage.setOnClickListener(view -> {
                        filesResponseCall.cancel();
                        cancelUploadImage.setVisibility(View.GONE);
                        retryUploadImage.setVisibility(View.VISIBLE);
                        mProgressUploadImage.setVisibility(View.GONE);
                        mProgressUploadImageInitial.setVisibility(View.GONE);
                    });
                    break;

                case "video":
                    File file = new File(messagesModel.getVideoThumbnailFile());
                    // create RequestBody instance from file
                    RequestBody thumbnail = RequestBody.create(MediaType.parse("image/*"), file);
                    filesResponseCall = filesUploadService.uploadMessageVideo(uploadFilesHelper, thumbnail);
                    new AsyncTask<Void, Long, Void>() {
                        @Override
                        protected Void doInBackground(Void... voids) {
                            filesResponseCall.enqueue(new Callback<FilesResponse>() {
                                                          @Override
                                                          public void onResponse(Call<FilesResponse> call, Response<FilesResponse> response) {
                                                              if (response.isSuccessful()) {
                                                                  if (response.body().isSuccess()) {
                                                                      Realm realm = Realm.getDefaultInstance();
                                                                      realm.executeTransactionAsync(realm1 -> {
                                                                                  MessagesModel messagesModel1 = realm1.where(MessagesModel.class).equalTo("id", messageId).findFirst();
                                                                                  messagesModel1.setFileUpload(true);
                                                                                  messagesModel1.setVideoFile(response.body().getUrl());
                                                                                  messagesModel1.setVideoThumbnailFile(response.body().getVideoThumbnail());
                                                                                  realm1.copyToRealmOrUpdate(messagesModel1);
                                                                              }, () -> {
                                                                                  MessagesModel messagesModel1 = realm.where(MessagesModel.class).equalTo("id", messageId).findFirst();
                                                                                  mUploadCallbacks.onFinish("video", messagesModel1);
                                                                                  AppHelper.LogCat("finish realm video");
                                                                              }
                                                                              , error -> {
                                                                                  AppHelper.LogCat("error realm video " + error.getMessage());
                                                                                  mUploadCallbacks.onError(type);
                                                                              });


                                                                      realm.close();
                                                                  } else {
                                                                      AppHelper.LogCat("failed to upload video " + response.body().getUrl());
                                                                      mUploadCallbacks.onError(type);
                                                                  }
                                                              } else

                                                              {
                                                                  AppHelper.LogCat("failed to upload video  ");
                                                                  mUploadCallbacks.onError(type);
                                                              }
                                                          }

                                                          @Override
                                                          public void onFailure
                                                                  (Call<FilesResponse> call, Throwable t) {
                                                              AppHelper.LogCat("failed to upload video  " + t.getMessage());
                                                              mUploadCallbacks.onError(type);

                                                          }
                                                      }

                            );


                            return null;
                        }
                    }.execute();
                    cancelUploadVideo.setOnClickListener(view -> {
                        filesResponseCall.cancel();
                        cancelUploadVideo.setVisibility(View.GONE);
                        retryUploadVideo.setVisibility(View.VISIBLE);
                        mProgressUploadVideo.setVisibility(View.GONE);
                        mProgressUploadVideoInitial.setVisibility(View.GONE);
                    });
                    break;
                case "audio":
                    filesResponseCall = filesUploadService.uploadMessageAudio(uploadFilesHelper);
                    new AsyncTask<Void, Long, Void>() {
                        @Override
                        protected Void doInBackground(Void... voids) {
                            filesResponseCall.enqueue(new Callback<FilesResponse>() {
                                @Override
                                public void onResponse(Call<FilesResponse> call, Response<FilesResponse> response) {
                                    if (response.isSuccessful()) {
                                        if (response.body().isSuccess()) {
                                            Realm realm = Realm.getDefaultInstance();
                                            realm.executeTransactionAsync(realm1 -> {
                                                        MessagesModel messagesModel1 = realm1.where(MessagesModel.class).equalTo("id", messageId).findFirst();
                                                        messagesModel1.setFileUpload(true);
                                                        messagesModel1.setAudioFile(response.body().getUrl());
                                                        realm1.copyToRealmOrUpdate(messagesModel1);
                                                    }, () -> {
                                                        MessagesModel messagesModel1 = realm.where(MessagesModel.class).equalTo("id", messageId).findFirst();
                                                        mUploadCallbacks.onFinish("audio", messagesModel1);
                                                        AppHelper.LogCat("finish realm audio");
                                                    }
                                                    , error -> {
                                                        mUploadCallbacks.onError(type);
                                                        AppHelper.LogCat("error realm audio");
                                                    });

                                            realm.close();
                                        } else {
                                            AppHelper.LogCat("failed to upload audio " + response.body().getUrl());
                                            AppHelper.CustomToast(mActivity, "Failed to upload audio");
                                            mUploadCallbacks.onError(type);
                                        }
                                    } else {
                                        AppHelper.LogCat("failed to upload audio  ");
                                        AppHelper.CustomToast(mActivity, "Failed to upload audio");
                                        mUploadCallbacks.onError(type);
                                    }
                                }

                                @Override
                                public void onFailure(Call<FilesResponse> call, Throwable t) {
                                    AppHelper.CustomToast(mActivity, "Failed to upload audio");
                                    AppHelper.LogCat("failed to upload audio  " + t.getMessage());
                                    mUploadCallbacks.onError(type);

                                }
                            });


                            return null;
                        }
                    }.execute();
                    cancelUploadAudio.setOnClickListener(view -> {
                        filesResponseCall.cancel();
                        cancelUploadAudio.setVisibility(View.GONE);
                        retryUploadAudio.setVisibility(View.VISIBLE);
                        mProgressUploadAudio.setVisibility(View.GONE);
                        mProgressUploadAudioInitial.setVisibility(View.GONE);
                    });
                    break;
                case "document":
                    filesResponseCall = filesUploadService.uploadMessageDocument(uploadFilesHelper);
                    new AsyncTask<Void, Long, Void>() {
                        @Override
                        protected Void doInBackground(Void... voids) {
                            filesResponseCall.enqueue(new Callback<FilesResponse>() {
                                @Override
                                public void onResponse(Call<FilesResponse> call, Response<FilesResponse> response) {
                                    if (response.isSuccessful()) {
                                        if (response.body().isSuccess()) {
                                            Realm realm = Realm.getDefaultInstance();
                                            realm.executeTransactionAsync(realm1 -> {
                                                        MessagesModel messagesModel1 = realm1.where(MessagesModel.class).equalTo("id", messageId).findFirst();
                                                        messagesModel1.setFileUpload(true);
                                                        messagesModel1.setDocumentFile(response.body().getUrl());
                                                        realm1.copyToRealmOrUpdate(messagesModel1);
                                                    }, () -> {
                                                        MessagesModel messagesModel1 = realm.where(MessagesModel.class).equalTo("id", messageId).findFirst();
                                                        mUploadCallbacks.onFinish("document", messagesModel1);
                                                        AppHelper.LogCat("finish realm document");
                                                    }
                                                    , error -> {
                                                        mUploadCallbacks.onError(type);
                                                        AppHelper.LogCat("error realm document");
                                                    });

                                            realm.close();
                                        } else {
                                            AppHelper.LogCat("failed to upload document isNotSuccess" + response.body().getUrl());
                                            mUploadCallbacks.onError(type);
                                            AppHelper.CustomToast(mActivity, "Failed to upload the document");
                                        }
                                    } else {
                                        AppHelper.LogCat("failed to upload document isNotSuccessful  ");
                                        AppHelper.CustomToast(mActivity, "Failed to upload the document");
                                        mUploadCallbacks.onError(type);
                                    }
                                }

                                @Override
                                public void onFailure
                                        (Call<FilesResponse> call, Throwable t) {
                                    AppHelper.CustomToast(mActivity, "Failed to upload the document");
                                    AppHelper.LogCat("failed to upload document Throwable " + t.getMessage());
                                    mUploadCallbacks.onError(type);

                                }
                            });


                            return null;
                        }
                    }.execute();
                    cancelUploadDocument.setOnClickListener(view -> {
                        filesResponseCall.cancel();
                        cancelUploadDocument.setVisibility(View.GONE);
                        retryUploadDocument.setVisibility(View.VISIBLE);
                        mProgressUploadDocument.setVisibility(View.GONE);
                        mProgressUploadDocumentInitial.setVisibility(View.GONE);
                    });
                    break;
            }


        }

        void stopPlayingAudio() {
            if (mMediaPlayer != null) {
                if (mMediaPlayer.isPlaying()) {
                    updateAudioProgressBar();
                    mMediaPlayer.stop();
                    mMediaPlayer.reset();
                    audioSeekBar.setProgress(0);
                    audioCurrentDurationAudio.setVisibility(View.GONE);
                    audioTotalDurationAudio.setVisibility(View.VISIBLE);
                    playBtnAudio.setVisibility(View.VISIBLE);
                    pauseBtnAudio.setVisibility(View.GONE);


                }

            }

        }

        void pausePlayingAudio() {
            if (mMediaPlayer != null) {
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.pause();
                    updateAudioProgressBar();
                    mAudioCallbacks.onPause();
                }
            }
        }

        void playingAudio(MessagesModel messagesModel) {
            if (messagesModel.getSenderID() == PreferenceManager.getID(mActivity)) {
                updateAudioProgressBar();
                String AudioDataSource;
                if (mMediaPlayer != null) {

                    try {
                        if (FilesManager.isFileAudioExists(FilesManager.getSentAudio(String.valueOf(messagesModel.getId()), messagesModel.getUsername()))) {
                            AudioDataSource = FilesManager.getFileAudioPath(String.valueOf(messagesModel.getId()), messagesModel.getUsername());
                            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                            mMediaPlayer.setDataSource(AudioDataSource);
                            mMediaPlayer.prepare();
                            mMediaPlayer.setOnPreparedListener(MediaPlayer::start);
                        } else {
                            AudioDataSource = EndPoints.BASE_URL + messagesModel.getAudioFile();
                            FilesManager.downloadFilesToDevice(mActivity, messagesModel.getAudioFile(), String.valueOf(messagesModel.getId()), messagesModel.getUsername(), "audio");
                            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                            mMediaPlayer.setDataSource(AudioDataSource);
                            mMediaPlayer.prepareAsync();
                            mMediaPlayer.setOnPreparedListener(MediaPlayer::start);
                        }

                    } catch (Exception e) {
                        AppHelper.LogCat("IOException audio sender" + e.getMessage());
                    }

                    mMediaPlayer.start();
                    audioTotalDurationAudio.setVisibility(View.GONE);
                    audioCurrentDurationAudio.setVisibility(View.VISIBLE);

                }
            } else {
                updateAudioProgressBar();
                String AudioDataSource;
                if (mMediaPlayer != null) {

                    try {
                        if (FilesManager.isFileAudioExists(FilesManager.getSentAudio(String.valueOf(messagesModel.getId()), messagesModel.getUsername()))) {
                            AudioDataSource = FilesManager.getFileAudioPath(String.valueOf(messagesModel.getId()), messagesModel.getUsername());
                            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                            mMediaPlayer.setDataSource(AudioDataSource);
                            mMediaPlayer.prepare();
                            mMediaPlayer.setOnPreparedListener(MediaPlayer::start);
                        } else {
                            AudioDataSource = EndPoints.BASE_URL + messagesModel.getAudioFile();
                            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                            mMediaPlayer.setDataSource(AudioDataSource);
                            mMediaPlayer.prepareAsync();
                            mMediaPlayer.setOnPreparedListener(MediaPlayer::start);
                        }

                    } catch (Exception e) {
                        AppHelper.LogCat("IOException audio recipient " + e.getMessage());
                    }


                    mMediaPlayer.start();
                    audioTotalDurationAudio.setVisibility(View.GONE);
                    audioCurrentDurationAudio.setVisibility(View.VISIBLE);

                }
            }


        }

        void updateAudioProgressBar() {
            mHandler.postDelayed(mUpdateTimeTask, 100);
        }


        /**
         * Background Runnable thread
         */
        private Runnable mUpdateTimeTask = new Runnable() {
            public void run() {
                try {
                    if (mMediaPlayer.isPlaying()) {
                        long totalDuration = mMediaPlayer.getDuration();
                        long currentDuration = mMediaPlayer.getCurrentPosition();
                        audioCurrentDurationAudio.setText(UtilsTime.getFileTime(currentDuration));
                        int progress = (int) UtilsTime.getProgressPercentage(currentDuration, totalDuration);
                        mAudioCallbacks.onUpdate(progress);
                        mHandler.postDelayed(this, 100);
                    }
                } catch (Exception e) {
                    AppHelper.LogCat("Exception mUpdateTimeTask " + e.getMessage());
                }

            }
        };

        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            mHandler.removeCallbacks(mUpdateTimeTask);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            int totalDuration = mMediaPlayer.getDuration();
            int currentPosition = (int) UtilsTime.progressToTimer(seekBar.getProgress(), totalDuration);
            mMediaPlayer.seekTo(currentPosition);
            updateAudioProgressBar();
        }

        @Override
        public void onClick(View view) {
            MessagesModel messagesModel = mMessagesModel.get(getAdapterPosition());
            switch (view.getId()) {
                case R.id.pause_btn_audio:
                    playBtnAudio.setVisibility(View.VISIBLE);
                    pauseBtnAudio.setVisibility(View.GONE);
                    pausePlayingAudio();
                    break;
                case R.id.play_btn_audio:
                    playBtnAudio.setVisibility(View.GONE);
                    pauseBtnAudio.setVisibility(View.VISIBLE);
                    stopPlayingAudio();
                    playingAudio(messagesModel);
                    break;
                case R.id.video_layout:
                    playingVideo(messagesModel);
                    break;
                case R.id.play_btn_video:
                    playingVideo(messagesModel);
                    break;
                case R.id.image_layout:
                    showImage(messagesModel);
                    break;
                case R.id.download_image:
                    setupProgressBarDownloadImage();
                    mProgressDownloadImageInitial.setVisibility(View.VISIBLE);
                    cancelDownloadImage.setVisibility(View.VISIBLE);
                    downloadImage.setVisibility(View.GONE);
                    new Handler().postDelayed(() -> downloadFile(messagesModel), 2000);
                    break;
                case R.id.cancel_download_image:
                    cancelDownloadImage.setVisibility(View.GONE);
                    downloadImage.setVisibility(View.VISIBLE);
                    mProgressDownloadImage.setVisibility(View.GONE);
                    mProgressDownloadImageInitial.setVisibility(View.GONE);

                    break;
                case R.id.cancel_upload_image:
                    cancelUploadImage.setVisibility(View.GONE);
                    retryUploadImage.setVisibility(View.VISIBLE);
                    mProgressUploadImage.setVisibility(View.GONE);
                    mProgressUploadImageInitial.setVisibility(View.GONE);
                    break;

                case R.id.retry_upload_image:
                    retryUploadImage.setVisibility(View.GONE);
                    setupProgressBarUploadImage();
                    mProgressUploadImageInitial.setVisibility(View.VISIBLE);
                    cancelUploadImage.setVisibility(View.VISIBLE);
                    mProgressUploadImageInitial.setIndeterminate(true);
                    new Handler().postDelayed(() -> uploadFile(messagesModel), 3000);
                    break;


                case R.id.download_video:
                    setupProgressBarDownloadVideo();
                    mProgressDownloadVideoInitial.setVisibility(View.VISIBLE);
                    cancelDownloadVideo.setVisibility(View.VISIBLE);
                    downloadVideo.setVisibility(View.GONE);
                    new Handler().postDelayed(() -> downloadFile(messagesModel), 2000);
                    break;
                case R.id.cancel_download_video:
                    cancelDownloadVideo.setVisibility(View.GONE);
                    downloadVideo.setVisibility(View.VISIBLE);
                    mProgressDownloadVideo.setVisibility(View.GONE);
                    mProgressDownloadVideoInitial.setVisibility(View.GONE);

                    break;
                case R.id.cancel_upload_video:
                    cancelUploadVideo.setVisibility(View.GONE);
                    retryUploadVideo.setVisibility(View.VISIBLE);
                    mProgressUploadVideo.setVisibility(View.GONE);
                    mProgressUploadVideoInitial.setVisibility(View.GONE);
                    break;
                case R.id.retry_upload_video:
                    retryUploadVideo.setVisibility(View.GONE);
                    setupProgressBarUploadVideo();
                    mProgressUploadVideoInitial.setVisibility(View.VISIBLE);
                    cancelUploadVideo.setVisibility(View.VISIBLE);
                    mProgressUploadVideoInitial.setIndeterminate(true);
                    new Handler().postDelayed(() -> uploadFile(messagesModel), 3000);
                    break;
                case R.id.cancel_download_audio:
                    cancelDownloadAudio.setVisibility(View.GONE);
                    retryDownloadAudio.setVisibility(View.VISIBLE);
                    mProgressDownloadAudio.setVisibility(View.GONE);
                    mProgressDownloadAudioInitial.setVisibility(View.GONE);
                    break;
                case R.id.retry_download_audio_button:
                    setupProgressBarDownloadAudio();
                    mProgressDownloadAudioInitial.setVisibility(View.VISIBLE);
                    cancelDownloadAudio.setVisibility(View.VISIBLE);
                    retryDownloadAudio.setVisibility(View.GONE);
                    new Handler().postDelayed(() -> downloadFile(messagesModel), 2000);
                    break;
                case R.id.cancel_download_document:
                    cancelDownloadDocument.setVisibility(View.GONE);
                    retryDownloadDocument.setVisibility(View.VISIBLE);
                    mProgressDownloadDocument.setVisibility(View.GONE);
                    mProgressDownloadDocumentInitial.setVisibility(View.GONE);
                    break;
                case R.id.retry_download_document:
                    setupProgressBarDownloadDocument();
                    mProgressDownloadDocumentInitial.setVisibility(View.VISIBLE);
                    cancelDownloadDocument.setVisibility(View.VISIBLE);
                    retryDownloadDocument.setVisibility(View.GONE);
                    new Handler().postDelayed(() -> downloadFile(messagesModel), 2000);
                    break;

                case R.id.cancel_upload_audio:
                    cancelUploadAudio.setVisibility(View.GONE);
                    retryUploadAudio.setVisibility(View.VISIBLE);
                    mProgressUploadAudio.setVisibility(View.GONE);
                    mProgressUploadAudioInitial.setVisibility(View.GONE);
                    break;
                case R.id.retry_upload_audio_button:
                    retryUploadAudio.setVisibility(View.GONE);
                    setupProgressBarUploadAudio();
                    mProgressUploadAudioInitial.setVisibility(View.VISIBLE);
                    cancelUploadAudio.setVisibility(View.VISIBLE);
                    mProgressUploadAudioInitial.setIndeterminate(true);
                    new Handler().postDelayed(() -> uploadFile(messagesModel), 3000);
                    break;
                case R.id.cancel_upload_document:
                    cancelUploadDocument.setVisibility(View.GONE);
                    retryUploadDocument.setVisibility(View.VISIBLE);
                    mProgressUploadDocument.setVisibility(View.GONE);
                    mProgressUploadDocumentInitial.setVisibility(View.GONE);
                    break;
                case R.id.retry_upload_document:
                    retryUploadDocument.setVisibility(View.GONE);
                    setupProgressBarUploadDocument();
                    mProgressUploadDocumentInitial.setVisibility(View.VISIBLE);
                    cancelUploadDocument.setVisibility(View.VISIBLE);
                    mProgressUploadDocumentInitial.setIndeterminate(true);
                    new Handler().postDelayed(() -> uploadFile(messagesModel), 3000);
                    break;
                case R.id.document_title:
                    if (FilesManager.isFileDocumentsExists(FilesManager.getDocument(String.valueOf(messagesModel.getId()), messagesModel.getUsername()))) {
                        openDocument(FilesManager.getFileDocument(String.valueOf(messagesModel.getId()), messagesModel.getUsername()));
                    } else {
                        File file = new File(EndPoints.BASE_URL + messagesModel.getDocumentFile());
                        openDocument(file);
                    }

                    break;
            }
        }

        private void showImage(MessagesModel messagesModel) {
            String imageUrl = messagesModel.getImageFile();
            String userId = String.valueOf(messagesModel.getId());
            String name = messagesModel.getUsername();
            if (messagesModel.getSenderID() == PreferenceManager.getID(mActivity)) {
                if (messagesModel.isFileUpload()) {
                    if (FilesManager.isFileImagesOtherExists(FilesManager.getOthersSentImage(String.valueOf(messagesModel.getId()), messagesModel.getUsername()))) {
                        File fileImageOther = FilesManager.getFileImageOther(String.valueOf(messagesModel.getId()), messagesModel.getUsername());
                        Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
                        Uri data = Uri.fromFile(fileImageOther);
                        intent.setDataAndType(data, "image/*");
                        try {
                            mActivity.startActivity(intent);
                        } catch (ActivityNotFoundException e) {
                            AppHelper.CustomToast(mActivity, mActivity.getString(R.string.no_application_to_show_image));
                        }

                    } else {
                        FilesManager.downloadFilesToDevice(mActivity, imageUrl, userId, name, "other");
                    }
                } else {
                    File fileVideo = new File(imageUrl);
                    Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
                    Uri data = Uri.fromFile(fileVideo);
                    intent.setDataAndType(data, "image/*");
                    try {
                        mActivity.startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        AppHelper.CustomToast(mActivity, mActivity.getString(R.string.no_application_to_show_image));
                    }

                }

            } else {
                if (messagesModel.isFileDownLoad()) {
                    if (FilesManager.isFileImagesOtherExists(FilesManager.getOthersSentImage(String.valueOf(messagesModel.getId()), messagesModel.getUsername()))) {
                        File fileImageOther = FilesManager.getFileImageOther(String.valueOf(messagesModel.getId()), messagesModel.getUsername());
                        Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
                        Uri data = Uri.fromFile(fileImageOther);
                        intent.setDataAndType(data, "image/*");
                        try {
                            mActivity.startActivity(intent);
                        } catch (ActivityNotFoundException e) {
                            AppHelper.CustomToast(mActivity, mActivity.getString(R.string.no_application_to_show_image));
                        }

                    } else {
                        FilesManager.downloadFilesToDevice(mActivity, imageUrl, userId, name, "other");
                    }
                }
            }

        }


        private void playingVideo(MessagesModel messagesModel) {
            String videoUrl = messagesModel.getVideoFile();
            String userId = String.valueOf(messagesModel.getId());
            String name = messagesModel.getUsername();
            if (FilesManager.isFileVideosExists(FilesManager.getVideo(String.valueOf(messagesModel.getId()), messagesModel.getUsername()))) {
                File fileVideo = FilesManager.getFileVideo(String.valueOf(messagesModel.getId()), messagesModel.getUsername());
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
                Uri data = Uri.fromFile(fileVideo);
                intent.setDataAndType(data, "video/*");
                mActivity.startActivity(intent);

            } else {
                FilesManager.downloadFilesToDevice(mActivity, videoUrl, userId, name, "video");
            }
        }

        void openDocument(File file) {
            if (file.exists()) {
                Uri path = Uri.fromFile(file);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(path, "application/pdf");
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                try {
                    mActivity.startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    AppHelper.CustomToast(mActivity, mActivity.getString(R.string.no_application_to_view_pdf));
                }
            }
        }

    }

    public void stopAudio() {
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
                mMediaPlayer.reset();
                mMediaPlayer.release();

            }
            mMediaPlayer = null;
        }

    }

}

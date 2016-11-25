package com.sourcecanyon.whatsClone.adapters.recyclerView.contacts;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.rockerhieu.emojicon.EmojiconTextView;
import com.sourcecanyon.whatsClone.R;
import com.sourcecanyon.whatsClone.activities.messages.MessagesActivity;
import com.sourcecanyon.whatsClone.activities.profile.ProfilePreviewActivity;
import com.sourcecanyon.whatsClone.app.AppConstants;
import com.sourcecanyon.whatsClone.app.EndPoints;
import com.sourcecanyon.whatsClone.helpers.AppHelper;
import com.sourcecanyon.whatsClone.helpers.Files.FilesManager;
import com.sourcecanyon.whatsClone.helpers.UtilsPhone;
import com.sourcecanyon.whatsClone.models.users.contacts.ContactsModel;
import com.sourcecanyon.whatsClone.ui.CropSquareTransformation;
import com.sourcecanyon.whatsClone.ui.RecyclerViewFastScroller;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

import static com.sourcecanyon.whatsClone.helpers.UtilsString.unescapeJava;

/**
 * Created by Abderrahim El imame on 20/02/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class ContactsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements RecyclerViewFastScroller.BubbleTextGetter {
    protected final Activity mActivity;
    private List<ContactsModel> mContactsModel;
    private String SearchQuery;


    public ContactsAdapter(@NonNull Activity mActivity, List<ContactsModel> mContactsModel) {
        this.mActivity = mActivity;
        this.mContactsModel = mContactsModel;
    }


    public void setContacts(List<ContactsModel> contactsModelList) {
        this.mContactsModel = contactsModelList;
        notifyDataSetChanged();
    }

    //Methods for search start
    public void setString(String SearchQuery) {
        this.SearchQuery = SearchQuery;
        notifyDataSetChanged();
    }

    public void animateTo(List<ContactsModel> models) {
        applyAndAnimateRemovals(models);
        applyAndAnimateAdditions(models);
        applyAndAnimateMovedItems(models);
    }

    private void applyAndAnimateRemovals(List<ContactsModel> newModels) {
        for (int i = mContactsModel.size() - 1; i >= 0; i--) {
            final ContactsModel model = mContactsModel.get(i);
            if (!newModels.contains(model)) {
                removeItem(i);
            }
        }
    }

    private void applyAndAnimateAdditions(List<ContactsModel> newModels) {
        for (int i = 0, count = newModels.size(); i < count; i++) {
            final ContactsModel model = newModels.get(i);
            if (!mContactsModel.contains(model)) {
                addItem(i, model);
            }
        }
    }

    private void applyAndAnimateMovedItems(List<ContactsModel> newModels) {
        for (int toPosition = newModels.size() - 1; toPosition >= 0; toPosition--) {
            final ContactsModel model = newModels.get(toPosition);
            final int fromPosition = mContactsModel.indexOf(model);
            if (fromPosition >= 0 && fromPosition != toPosition) {
                moveItem(fromPosition, toPosition);
            }
        }
    }

    private ContactsModel removeItem(int position) {
        final ContactsModel model = mContactsModel.remove(position);
        notifyItemRemoved(position);
        return model;
    }

    private void addItem(int position, ContactsModel model) {
        mContactsModel.add(position, model);
        notifyItemInserted(position);
    }

    private void moveItem(int fromPosition, int toPosition) {
        final ContactsModel model = mContactsModel.remove(fromPosition);
        mContactsModel.add(toPosition, model);
        notifyItemMoved(fromPosition, toPosition);
    }
    //Methods for search end

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mActivity).inflate(R.layout.row_contacts, parent, false);
        return new ContactsViewHolder(itemView);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final ContactsViewHolder contactsViewHolder = (ContactsViewHolder) holder;
        final ContactsModel contactsModel = this.mContactsModel.get(position);
        try {

            contactsViewHolder.setUsername(contactsModel.getUsername(), contactsModel.getPhone());


            String Username;
            String name = UtilsPhone.getContactName(mActivity, contactsModel.getPhone());
            if (name != null) {
                Username = name;
            } else {
                Username = contactsModel.getPhone();
            }

            SpannableString recipientUsername = SpannableString.valueOf(Username);
            if (SearchQuery == null) {
                contactsViewHolder.username.setText(recipientUsername, TextView.BufferType.NORMAL);
            } else {
                int index = TextUtils.indexOf(Username.toLowerCase(), SearchQuery.toLowerCase());
                if (index >= 0) {
                    recipientUsername.setSpan(new ForegroundColorSpan(AppHelper.getColor(mActivity, R.color.colorAccent)), index, index + SearchQuery.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                    recipientUsername.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), index, index + SearchQuery.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                }

                contactsViewHolder.username.setText(recipientUsername, TextView.BufferType.SPANNABLE);
            }
            if (contactsModel.getStatus() != null) {
                String status = unescapeJava(contactsModel.getStatus());
                if (status.length() > 18)
                    contactsViewHolder.setStatus(status.substring(0, 18) + "... " + "");

                else
                    contactsViewHolder.setStatus(status);

            } else {
                contactsViewHolder.setStatus(contactsModel.getPhone());
            }

            if (contactsModel.isLinked()) {
                contactsViewHolder.hideInviteButton();
            } else {
                contactsViewHolder.showInviteButton();
            }
            if (contactsModel.getImage() != null) {
                contactsViewHolder.setUserImage(contactsModel.getImage(), String.valueOf(contactsModel.getId()), contactsModel.getUsername());
            } else {
                contactsViewHolder.setNullUserImage(R.drawable.ic_user_holder_white_48dp);
            }
        } catch (Exception e) {
            AppHelper.LogCat("Contacts adapters Exception " + e.getMessage());
        }
        contactsViewHolder.setOnClickListener(view -> {
            if (view.getId() == R.id.user_image) {
                Intent mIntent = new Intent(mActivity, ProfilePreviewActivity.class);
                mIntent.putExtra("userID", contactsModel.getId());
                mIntent.putExtra("isGroup", false);
                mActivity.startActivity(mIntent);
            } else {
                if (contactsModel.isLinked()) {
                    Intent messagingIntent = new Intent(mActivity, MessagesActivity.class);
                    messagingIntent.putExtra("conversationID", 0);
                    messagingIntent.putExtra("recipientID", contactsModel.getId());
                    messagingIntent.putExtra("isGroup", false);
                    mActivity.startActivity(messagingIntent);
                } else {
                    String number = contactsModel.getPhone();
                    Intent smsIntent = new Intent(Intent.ACTION_VIEW);
                    smsIntent.setType("vnd.android-dir/mms-sms");
                    smsIntent.putExtra("address", number);
                    smsIntent.putExtra("sms_body", AppConstants.INVITE_MESSAGE_SMS);
                    mActivity.startActivity(smsIntent);
                }
            }

        });

    }


    @Override
    public int getItemCount() {
        if (mContactsModel != null) return mContactsModel.size();
        return 0;
    }

    @Override
    public String getTextToShowInBubble(int pos) {
        try {
            return Character.toString(mContactsModel.get(pos).getUsername().charAt(0));
        } catch (Exception e) {
            AppHelper.LogCat(e.getMessage());
            return e.getMessage();
        }

    }

    public class ContactsViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.user_image)
        ImageView userImage;
        @Bind(R.id.username)
        TextView username;
        @Bind(R.id.status)
        EmojiconTextView status;
        @Bind(R.id.invite)
        TextView invite;

        ContactsViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }


        void setUserImage(String ImageUrl, String userId, String name) {

            if (FilesManager.isFileImagesProfileExists(FilesManager.getProfileImage(userId, name))) {

                Picasso.with(mActivity)
                        .load(FilesManager.getFileImageProfile(userId, name))
                        .transform(new CropSquareTransformation())
                        .networkPolicy(NetworkPolicy.NO_CACHE)
                        .memoryPolicy(MemoryPolicy.NO_CACHE)
                        .resize(100, 100)
                        .centerCrop()
                        .into(userImage);
            } else {

                Target target = new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        userImage.setImageBitmap(bitmap);
                        FilesManager.downloadFilesToDevice(mActivity, ImageUrl, userId, name, "profile");
                    }

                    @Override
                    public void onBitmapFailed(Drawable errorDrawable) {
                        userImage.setImageDrawable(errorDrawable);
                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {
                        userImage.setImageDrawable(placeHolderDrawable);
                    }
                };
                Picasso.with(mActivity)
                        .load(EndPoints.BASE_URL + ImageUrl)
                        .transform(new CropSquareTransformation())
                        .networkPolicy(NetworkPolicy.NO_CACHE)
                        .memoryPolicy(MemoryPolicy.NO_CACHE)
                        .resize(100, 100)
                        .centerCrop()
                        .into(target);
            }

        }

        void setNullUserImage(int drawable) {
            userImage.setPadding(2, 2, 2, 2);
            userImage.setImageResource(drawable);
        }

        void hideInviteButton() {
            invite.setVisibility(View.GONE);
        }

        void showInviteButton() {
            invite.setVisibility(View.VISIBLE);
        }

        void setUsername(String Username, String phone) {
            if (Username != null) {
                username.setText(Username);
            } else {
                String name = UtilsPhone.getContactName(mActivity, phone);
                if (name != null) {
                    username.setText(name);
                } else {
                    username.setText(phone);
                }

            }

        }

        void setStatus(String Status) {
            status.setText(Status);
        }


        void setOnClickListener(View.OnClickListener listener) {
            itemView.setOnClickListener(listener);
            userImage.setOnClickListener(listener);
        }

    }


}

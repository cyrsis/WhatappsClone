package com.sourcecanyon.whatsClone.adapters.recyclerView;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.rockerhieu.emojicon.EmojiconTextView;
import com.sourcecanyon.whatsClone.R;
import com.sourcecanyon.whatsClone.activities.groups.AddMembersToGroupActivity;
import com.sourcecanyon.whatsClone.activities.messages.MessagesActivity;
import com.sourcecanyon.whatsClone.activities.profile.ProfilePreviewActivity;
import com.sourcecanyon.whatsClone.app.EndPoints;
import com.sourcecanyon.whatsClone.helpers.AppHelper;
import com.sourcecanyon.whatsClone.helpers.Files.FilesManager;
import com.sourcecanyon.whatsClone.helpers.UtilsPhone;
import com.sourcecanyon.whatsClone.models.users.contacts.ContactsModel;
import com.sourcecanyon.whatsClone.ui.CropSquareTransformation;
import com.sourcecanyon.whatsClone.ui.RecyclerViewFastScroller;
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
public class SelectContactsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements RecyclerViewFastScroller.BubbleTextGetter {
    private final Activity mActivity;
    private List<ContactsModel> mContactsModel;
    private static final int TYPE_ITEM = 1;
    private static final int TYPE_HEADER = 2;

    public void setContacts(List<ContactsModel> contactsModelList) {
        this.mContactsModel = contactsModelList;
        notifyDataSetChanged();
    }

    public SelectContactsAdapter(@NonNull Activity mActivity, List<ContactsModel> mContactsModel) {
        this.mActivity = mActivity;
        this.mContactsModel = mContactsModel;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_HEADER;
        } else {
            return TYPE_ITEM;
        }

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {

            View itemView = LayoutInflater.from(mActivity).inflate(R.layout.header_contacts, parent, false);
            return new ContactsHeaderViewHolder(itemView);
        } else {

            View itemView = LayoutInflater.from(mActivity).inflate(R.layout.row_contacts, parent, false);
            return new ContactsViewHolder(itemView);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ContactsViewHolder) {
            final ContactsViewHolder contactsViewHolder = (ContactsViewHolder) holder;
            final ContactsModel contactsModel = this.mContactsModel.get(position - 1);
            try {
                contactsViewHolder.setUsername(contactsModel.getUsername(), contactsModel.getPhone());


                if (contactsModel.getStatus() != null) {
                    contactsViewHolder.setStatus(contactsModel.getStatus());
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
                AppHelper.LogCat("" + e.getMessage());
            }
            contactsViewHolder.setOnClickListener(view -> {
                if (view.getId() == R.id.user_image) {
                    Intent mIntent = new Intent(mActivity, ProfilePreviewActivity.class);
                    mIntent.putExtra("userID", contactsModel.getId());
                    mIntent.putExtra("isGroup", false);
                    mActivity.startActivity(mIntent);
                } else {
                    Intent messagingIntent = new Intent(mActivity, MessagesActivity.class);
                    messagingIntent.putExtra("conversationID", 0);
                    messagingIntent.putExtra("recipientID", contactsModel.getId());
                    messagingIntent.putExtra("isGroup", false);
                    mActivity.startActivity(messagingIntent);
                    mActivity.finish();
                }

            });
        }

    }


    @Override
    public int getItemCount() {
        if (mContactsModel != null) return mContactsModel.size() + 1;
        return 1;
    }

    @Override
    public String getTextToShowInBubble(int pos) {
        return Character.toString(mContactsModel.get(pos).getUsername().charAt(0));
    }


    class ContactsHeaderViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.imageHeader)
        ImageView imageHeader;

        ContactsHeaderViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            imageHeader.setPadding(5, 5, 5, 5);
            imageHeader.setImageResource(R.drawable.ic_group_holder_white_opacity_48dp);
            itemView.setOnClickListener(v -> {
                mActivity.startActivity(new Intent(mActivity, AddMembersToGroupActivity.class));
                mActivity.finish();
            });
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
            String user = unescapeJava(Status);
            if (user.length() > 18)
                status.setText(user.substring(0, 18) + "... " + "");
            else
                status.setText(user);
        }


        void setOnClickListener(View.OnClickListener listener) {
            itemView.setOnClickListener(listener);
            userImage.setOnClickListener(listener);
        }

    }
}

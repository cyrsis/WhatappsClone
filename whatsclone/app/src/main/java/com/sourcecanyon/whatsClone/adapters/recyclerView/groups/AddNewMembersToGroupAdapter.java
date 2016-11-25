package com.sourcecanyon.whatsClone.adapters.recyclerView.groups;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.rockerhieu.emojicon.EmojiconTextView;
import com.sourcecanyon.whatsClone.R;
import com.sourcecanyon.whatsClone.api.APIGroups;
import com.sourcecanyon.whatsClone.api.APIService;
import com.sourcecanyon.whatsClone.app.AppConstants;
import com.sourcecanyon.whatsClone.app.EndPoints;
import com.sourcecanyon.whatsClone.helpers.AppHelper;
import com.sourcecanyon.whatsClone.helpers.Files.FilesManager;
import com.sourcecanyon.whatsClone.helpers.PreferenceManager;
import com.sourcecanyon.whatsClone.helpers.UtilsPhone;
import com.sourcecanyon.whatsClone.models.groups.GroupResponse;
import com.sourcecanyon.whatsClone.models.groups.MembersGroupModel;
import com.sourcecanyon.whatsClone.models.users.Pusher;
import com.sourcecanyon.whatsClone.models.users.contacts.ContactsModel;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import io.realm.Realm;
import io.realm.RealmQuery;
import jp.wasabeef.glide.transformations.CropCircleTransformation;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.sourcecanyon.whatsClone.helpers.UtilsString.unescapeJava;

/**
 * Created by Abderrahim El imame on 11/03/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class AddNewMembersToGroupAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Activity mActivity;
    private List<ContactsModel> mContactsModels;
    private LayoutInflater mInflater;
    private Realm realm;
    private int groupID;
    private APIService mApiService;

    public AddNewMembersToGroupAdapter(Activity mActivity, List<ContactsModel> mContactsModels, int groupID, APIService mApiService) {
        this.mActivity = mActivity;
        this.mContactsModels = mContactsModels;
        mInflater = LayoutInflater.from(mActivity);
        this.realm = Realm.getDefaultInstance();
        this.groupID = groupID;
        this.mApiService = mApiService;
    }

    public void setContacts(List<ContactsModel> mContactsModels) {
        this.mContactsModels = mContactsModels;
        notifyDataSetChanged();
    }


    public List<ContactsModel> getContacts() {
        return mContactsModels;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.row_add_members_group, parent, false);
        return new ContactsViewHolder(view);

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final ContactsViewHolder contactsViewHolder = (ContactsViewHolder) holder;
        final ContactsModel contactsModel = this.mContactsModels.get(position);
        try {
            if (contactsViewHolder.checkIfMemberExist(contactsModel.getId(), groupID)) {
                contactsViewHolder.itemView.setEnabled(false);
                contactsViewHolder.username.setTextColor(mActivity.getResources().getColor(R.color.colorGray2));
            } else {
                contactsViewHolder.itemView.setEnabled(true);
                contactsViewHolder.username.setTextColor(mActivity.getResources().getColor(R.color.colorBlack));
            }
            contactsViewHolder.setUsername(contactsModel.getPhone());


            if (contactsModel.getStatus() != null) {
                contactsViewHolder.setStatus(contactsModel.getStatus());
            }

            if (contactsModel.getImage() != null) {
                contactsViewHolder.setUserImage(contactsModel.getImage(), String.valueOf(contactsModel.getId()), contactsModel.getUsername());
            } else {
                contactsViewHolder.setNullUserImage(R.drawable.ic_user_holder_white_48dp);
            }
        } catch (Exception e) {
            AppHelper.LogCat("Exception" + e.getMessage());
        }

    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        if (mContactsModels != null) {
            return mContactsModels.size();
        } else {
            return 0;
        }
    }


    class ContactsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @Bind(R.id.user_image)
        ImageView userImage;

        @Bind(R.id.username)
        TextView username;

        @Bind(R.id.status)
        EmojiconTextView status;

        @Bind(R.id.select_icon)
        LinearLayout selectIcon;

        ContactsViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        boolean checkIfMemberExist(int memberID, int groupID) {
            RealmQuery<MembersGroupModel> query = realm.where(MembersGroupModel.class).equalTo("userId", memberID).equalTo("groupID", groupID);
            return query.count() == 0 ? false : true;
        }

        void setUserImage(String ImageUrl, String userId, String name) {
            if (FilesManager.isFileImagesProfileExists(FilesManager.getProfileImage(userId, name))) {
                Glide.with(mActivity)
                        .load(FilesManager.getFileImageProfile(userId, name))
                        .asBitmap()
                        .transform(new CropCircleTransformation(mActivity))
                        .into(userImage);
            } else {
                BitmapImageViewTarget target = new BitmapImageViewTarget(userImage) {
                    @Override
                    public void onLoadStarted(Drawable placeholder) {
                        super.onLoadStarted(placeholder);
                        userImage.setImageDrawable(placeholder);
                    }

                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        super.onResourceReady(resource, glideAnimation);
                        userImage.setImageBitmap(resource);
                        FilesManager.downloadFilesToDevice(mActivity, ImageUrl, userId, name, "profile");
                    }

                    @Override
                    public void onLoadFailed(Exception e, Drawable errorDrawable) {
                        super.onLoadFailed(e, errorDrawable);
                        userImage.setImageDrawable(errorDrawable);
                    }


                };

                Glide.with(mActivity)
                        .load(EndPoints.BASE_URL + ImageUrl)
                        .asBitmap()
                        .transform(new CropCircleTransformation(mActivity))
                        .into(target);
            }

        }

        void setNullUserImage(int drawable) {
            userImage.setPadding(2, 2, 2, 2);
            userImage.setImageResource(drawable);
        }

        void setUsername(String phone) {
            String name = UtilsPhone.getContactName(mActivity, phone);
            if (name != null) {
                username.setText(name);
            } else {
                username.setText(phone);
            }


        }

        void setStatus(String Status) {
            String statu = unescapeJava(Status);
            status.setText(statu);
        }

        @Override
        public void onClick(View view) {
            ContactsModel membersGroupModel = mContactsModels.get(getAdapterPosition());
            String theName;
            String name = UtilsPhone.getContactName(mActivity, membersGroupModel.getPhone());
            if (name != null) {
                theName = name;
            } else {
                theName = membersGroupModel.getPhone();
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
            builder.setMessage(mActivity.getString(R.string.add_to_group) + theName + mActivity.getString(R.string.member_to_group))
                    .setPositiveButton(mActivity.getString(R.string.add_new_member), (dialog, which) -> {
                        AddMembersToGroup(membersGroupModel.getId());
                    }).setNegativeButton(mActivity.getString(R.string.cancel), null).show();
        }


        private void AddMembersToGroup(int id) {
            APIGroups mApiGroups = mApiService.RootService(APIGroups.class, PreferenceManager.getToken(mActivity), EndPoints.BASE_URL);
            Call<GroupResponse> CreateGroupCall = mApiGroups.addMembers(groupID, id);
            AppHelper.showDialog(mActivity, mActivity.getString(R.string.adding_member));
            CreateGroupCall.enqueue(new Callback<GroupResponse>() {
                @Override
                public void onResponse(Call<GroupResponse> call, Response<GroupResponse> response) {
                    if (response.isSuccessful()) {
                        AppHelper.hideDialog();
                        if (response.body().isSuccess()) {
                            AppHelper.Snackbar(mActivity, mActivity.findViewById(R.id.ParentLayoutAddNewMembers), response.body().getMessage(), AppConstants.MESSAGE_COLOR_SUCCESS, AppConstants.TEXT_COLOR);
                            EventBus.getDefault().post(new Pusher("createGroup"));
                            EventBus.getDefault().post(new Pusher("addMember", String.valueOf(groupID)));
                            mActivity.finish();

                        } else {
                            AppHelper.Snackbar(mActivity, mActivity.findViewById(R.id.ParentLayoutAddNewMembers), response.body().getMessage(), AppConstants.MESSAGE_COLOR_WARNING, AppConstants.TEXT_COLOR);
                        }
                    } else {
                        AppHelper.hideDialog();
                        AppHelper.Snackbar(mActivity, mActivity.findViewById(R.id.ParentLayoutAddNewMembers), response.message(), AppConstants.MESSAGE_COLOR_WARNING, AppConstants.TEXT_COLOR);

                    }
                }

                @Override
                public void onFailure(Call<GroupResponse> call, Throwable t) {
                    AppHelper.hideDialog();
                    AppHelper.Snackbar(mActivity, mActivity.findViewById(R.id.ParentLayoutAddNewMembers), mActivity.getString(R.string.failed_to_add_member_to_group), AppConstants.MESSAGE_COLOR_WARNING, AppConstants.TEXT_COLOR);
                }
            });


        }
    }


}


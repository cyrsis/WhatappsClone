package com.sourcecanyon.whatsClone.adapters.recyclerView;

import android.app.Activity;
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

import com.sourcecanyon.whatsClone.R;
import com.sourcecanyon.whatsClone.helpers.AppHelper;
import com.sourcecanyon.whatsClone.models.CountriesModel;
import com.sourcecanyon.whatsClone.models.users.Pusher;
import com.sourcecanyon.whatsClone.ui.ColorGenerator;
import com.sourcecanyon.whatsClone.ui.TextDrawableBubbles;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;

/**
 * Created by Abderrahim El imame on 11/03/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class CountriesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Activity mActivity;
    private List<CountriesModel> mCountriesModel;

    private LayoutInflater mInflater;
    private String SearchQuery;


    public CountriesAdapter(Activity mActivity) {
        this.mActivity = mActivity;
        mInflater = LayoutInflater.from(mActivity);
    }

    public void setCountries(List<CountriesModel> mCountriesModel) {
        this.mCountriesModel = mCountriesModel;
        notifyDataSetChanged();
    }

    public List<CountriesModel> getCountries() {
        return mCountriesModel;
    }

    //Methods for search start
    public void setString(String SearchQuery) {
        this.SearchQuery = SearchQuery;
        notifyDataSetChanged();
    }

    public void animateTo(List<CountriesModel> models) {
        applyAndAnimateRemovals(models);
        applyAndAnimateAdditions(models);
        applyAndAnimateMovedItems(models);
    }

    private void applyAndAnimateRemovals(List<CountriesModel> newModels) {
        for (int i = mCountriesModel.size() - 1; i >= 0; i--) {
            final CountriesModel model = mCountriesModel.get(i);
            if (!newModels.contains(model)) {
                removeItem(i);
            }
        }
    }

    private void applyAndAnimateAdditions(List<CountriesModel> newModels) {
        for (int i = 0, count = newModels.size(); i < count; i++) {
            final CountriesModel model = newModels.get(i);
            if (!mCountriesModel.contains(model)) {
                addItem(i, model);
            }
        }
    }

    private void applyAndAnimateMovedItems(List<CountriesModel> newModels) {
        for (int toPosition = newModels.size() - 1; toPosition >= 0; toPosition--) {
            final CountriesModel model = newModels.get(toPosition);
            final int fromPosition = mCountriesModel.indexOf(model);
            if (fromPosition >= 0 && fromPosition != toPosition) {
                moveItem(fromPosition, toPosition);
            }
        }
    }

    private CountriesModel removeItem(int position) {
        final CountriesModel model = mCountriesModel.remove(position);
        notifyItemRemoved(position);
        return model;
    }

    private void addItem(int position, CountriesModel model) {
        mCountriesModel.add(position, model);
        notifyItemInserted(position);
    }

    private void moveItem(int fromPosition, int toPosition) {
        final CountriesModel model = mCountriesModel.remove(fromPosition);
        mCountriesModel.add(toPosition, model);
        notifyItemMoved(fromPosition, toPosition);
    }
    //Methods for search end

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.row_countries, parent, false);
        return new CountriesViewHolder(view);

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final CountriesViewHolder countriesViewHolder = (CountriesViewHolder) holder;
        final CountriesModel countriesModel = this.mCountriesModel.get(position);

        countriesViewHolder.setCountryName(countriesModel.getName());
        countriesViewHolder.setPicture(countriesModel.getName());
        SpannableString countryName = SpannableString.valueOf(countriesModel.getName());
        if (SearchQuery == null) {
            countriesViewHolder.countryName.setText(countryName, TextView.BufferType.NORMAL);
        } else {
            int index = TextUtils.indexOf(countriesModel.getName().toLowerCase(), SearchQuery.toLowerCase());
            if (index >= 0) {
                countryName.setSpan(new ForegroundColorSpan(AppHelper.getColor(mActivity, R.color.colorAccent)), index, index + SearchQuery.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                countryName.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), index, index + SearchQuery.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            }

            countriesViewHolder.countryName.setText(countryName, TextView.BufferType.SPANNABLE);
        }

    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public int getItemCount() {
        if (mCountriesModel != null) {
            return mCountriesModel.size();
        } else {
            return 0;
        }
    }

    public class CountriesViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.picture)
        ImageView picture;

        @Bind(R.id.country_name)
        TextView countryName;


        CountriesViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(view -> {
                CountriesModel countriesModel = mCountriesModel.get(getAdapterPosition());
                EventBus.getDefault().post(new Pusher("countryCode", countriesModel.getDial_code()));
                EventBus.getDefault().post(new Pusher("countryName", countriesModel.getName()));
            });
        }


        void setPicture(String countryN) {
            ColorGenerator generator = ColorGenerator.MATERIAL;
            int color = generator.getColor(countryN);
            String c = String.valueOf(countryName.getText().charAt(0));
            TextDrawableBubbles drawable = TextDrawableBubbles.builder()
                    .buildRound(c, color);
            picture.setImageDrawable(drawable);

        }


        void setCountryName(String countryN) {
            countryName.setText(countryN);
        }

    }


}


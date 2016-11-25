package com.sourcecanyon.whatsClone.activities.settings;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.sourcecanyon.whatsClone.R;
import com.sourcecanyon.whatsClone.helpers.AppHelper;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Abderrahim El imame on 8/17/16.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/bencherif_el
 */

public class AccountSettingsActivity extends AppCompatActivity {
    @Bind(R.id.app_bar)
    Toolbar toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_settings);
        ButterKnife.bind(this);
        setupToolbar();
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

/*

    @SuppressWarnings("unused")
    @OnClick(R.id.change_number)
    public void launchChangeNumber() {
        AppHelper.LaunchActivity(this, ChangeNumberActivity.class);
    }

*/

    @SuppressWarnings("unused")
    @OnClick(R.id.delete_account)
    public void launchDeleteAccount() {
        AppHelper.LaunchActivity(this, DeleteAccountActivity.class);
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            overridePendingTransition(R.anim.push_up_in, R.anim.push_up_out);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.push_up_in, R.anim.push_up_out);
        finish();
    }
}

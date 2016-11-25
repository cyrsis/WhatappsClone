package com.sourcecanyon.whatsClone.activities.profile;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.sourcecanyon.whatsClone.R;
import com.sourcecanyon.whatsClone.helpers.AppHelper;
import com.sourcecanyon.whatsClone.presenters.EditProfilePresenter;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by abderrahimelimame on 6/9/16.
 * Email : abderrahim.elimame@gmail.com
 */

public class EditUsernameActivity extends AppCompatActivity {
    @Bind(R.id.cancelStatus)
    TextView cancelStatusBtn;
    @Bind(R.id.OkStatus)
    TextView OkStatusBtn;
    @Bind(R.id.StatusWrapper)
    EditText StatusWrapper;
    @Bind(R.id.emoticonBtn)
    ImageView emoticonBtn;

    private String oldName;
    private EditProfilePresenter mEditProfilePresenter = new EditProfilePresenter(this, true);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_status);
        ButterKnife.bind(this);
        initializerView();
        mEditProfilePresenter.onCreate();
        if (getIntent().getExtras() != null) {
            oldName = getIntent().getStringExtra("currentUsername");
        }
        StatusWrapper.setText(oldName);

    }

    /**
     * method to initialize the view
     */
    private void initializerView() {
        emoticonBtn.setVisibility(View.GONE);
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.title_activity_edit_name);
        cancelStatusBtn.setOnClickListener(v -> finish());
        OkStatusBtn.setOnClickListener(v -> {
            String newUsername = StatusWrapper.getText().toString().trim();
            try {
                mEditProfilePresenter.EditCurrentName(newUsername);
            } catch (Exception e) {
                AppHelper.LogCat("Edit  name  Exception " + e.getMessage());
            }

        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }
}

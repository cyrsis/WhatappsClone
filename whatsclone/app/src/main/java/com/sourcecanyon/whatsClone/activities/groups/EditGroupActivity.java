package com.sourcecanyon.whatsClone.activities.groups;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rockerhieu.emojicon.EmojiconEditText;
import com.rockerhieu.emojicon.EmojiconGridFragment;
import com.rockerhieu.emojicon.EmojiconsFragment;
import com.rockerhieu.emojicon.emoji.Emojicon;
import com.sourcecanyon.whatsClone.R;
import com.sourcecanyon.whatsClone.helpers.AppHelper;
import com.sourcecanyon.whatsClone.helpers.UtilsString;
import com.sourcecanyon.whatsClone.presenters.EditGroupPresenter;

import butterknife.Bind;
import butterknife.ButterKnife;

import static com.sourcecanyon.whatsClone.helpers.UtilsString.unescapeJava;

/**
 * Created by abderrahimelimame on 6/9/16.
 * Email : abderrahim.elimame@gmail.com
 */

public class EditGroupActivity extends AppCompatActivity implements EmojiconGridFragment.OnEmojiconClickedListener, EmojiconsFragment.OnEmojiconBackspaceClickedListener {
    @Bind(R.id.cancelStatus)
    TextView cancelStatusBtn;
    @Bind(R.id.OkStatus)
    TextView OkStatusBtn;
    @Bind(R.id.StatusWrapper)
    EmojiconEditText StatusWrapper;
    @Bind(R.id.ParentLayoutStatusEdit)
    LinearLayout ParentLayoutStatusEdit;
    @Bind(R.id.emoticonBtn)
    ImageView emoticonBtn;
    @Bind(R.id.emojicons)
    FrameLayout emojiIconLayout;
    @Bind(R.id.app_bar)
    Toolbar toolbar;

    public boolean emoticonShown = false;
    private String oldName;
    private int groupID;
    private EditGroupPresenter mEditGroupPresenter = new EditGroupPresenter(this);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_status);
        ButterKnife.bind(this);
        initializerView();
        mEditGroupPresenter.onCreate();
        if (getIntent().getExtras() != null) {
            oldName = getIntent().getStringExtra("currentGroupName");
            groupID = getIntent().getExtras().getInt("groupID");
        }
        String oldNameUnescape = unescapeJava(oldName);
        StatusWrapper.setText(oldNameUnescape);
        StatusWrapper.setOnClickListener(v1 -> {
            if (emoticonShown) {
                emoticonShown = false;
                emojiIconLayout.setVisibility(View.GONE);
            }
        });
        emoticonBtn.setOnClickListener(v -> {
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
        setEmojIconFragment();


    }

    /**
     * method to setup the  EmojIcon Fragment
     */
    private void setEmojIconFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.emojicons, EmojiconsFragment.newInstance(false))
                .commit();
    }


    private void initializerView() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.title_activity_edit_name);
        cancelStatusBtn.setOnClickListener(v -> finish());
        OkStatusBtn.setOnClickListener(v -> {
            String insertedName = UtilsString.escapeJava(StatusWrapper.getText().toString());
            try {
                mEditGroupPresenter.EditCurrentName(insertedName, groupID);
            } catch (Exception e) {
                AppHelper.LogCat("Edit group name Exception  EditGroupActivity " + e.getMessage());
            }

        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        overridePendingTransition(R.anim.push_up_in, R.anim.push_up_out);
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onEmojiconClicked(Emojicon emojicon) {
        EmojiconsFragment.input(StatusWrapper, emojicon);
    }

    @Override
    public void onEmojiconBackspaceClicked(View v) {
        EmojiconsFragment.backspace(StatusWrapper);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.push_up_in, R.anim.push_up_out);
    }
}

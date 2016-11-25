package com.sourcecanyon.whatsClone.activities.popups;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.sourcecanyon.whatsClone.R;
import com.sourcecanyon.whatsClone.presenters.StatusPresenter;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Abderrahim El imame on 28/04/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class StatusDelete extends Activity {

    @Bind(R.id.deleteStatus)
    TextView deleteStatus;

    private StatusPresenter mStatusPresenter = new StatusPresenter(this);
    private int statusID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.popup_status_delete);
        ButterKnife.bind(this);
        if (getIntent().hasExtra("statusID") && getIntent().getExtras().getInt("statusID") != 0) {
            statusID = getIntent().getExtras().getInt("statusID");
        }
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.deleteStatus)
    public void DeleteStatus() {
        mStatusPresenter.DeleteStatus(statusID);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mStatusPresenter.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mStatusPresenter.onDestroy();
    }
}

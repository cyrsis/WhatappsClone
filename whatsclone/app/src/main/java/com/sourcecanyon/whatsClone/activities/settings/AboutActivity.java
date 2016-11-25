package com.sourcecanyon.whatsClone.activities.settings;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.sourcecanyon.whatsClone.R;
import com.sourcecanyon.whatsClone.helpers.AppHelper;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Abderrahim El imame on 8/19/16.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/bencherif_el
 */

public class AboutActivity extends AppCompatActivity {

    @Bind(R.id.version)
    TextView version;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ButterKnife.bind(this);
        PackageInfo packageinfo;
        try {
            packageinfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String appVersion = packageinfo.versionName;
            version.setText(getString(R.string.app_version) + appVersion);
        } catch (PackageManager.NameNotFoundException e) {
            AppHelper.LogCat(" About NameNotFoundException " + e.getMessage());
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.push_up_in, R.anim.push_up_out);
        finish();
    }

}

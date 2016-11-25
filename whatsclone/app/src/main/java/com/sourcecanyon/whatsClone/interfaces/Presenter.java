package com.sourcecanyon.whatsClone.interfaces;

/**
 * Created by abderrahimelimame on 6/11/16.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/bencherif_el
 */

public interface Presenter {
    void onStart();

    void onCreate();

    void onPause();

    void onResume();

    void onDestroy();

    void onLoadMore();

    void onRefresh();

    void onStop();
}

package com.sourcecanyon.whatsClone.interfaces;

/**
 * Created by Abderrahim El imame on 6/11/16.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/bencherif_el
 */

public interface LoadingData {

    void onShowLoading();

    void onHideLoading();

    void onErrorLoading(Throwable throwable);
}

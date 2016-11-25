package com.sourcecanyon.whatsClone.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

/**
 * Created by Abderrahim on 11/5/2015.
 * This class to create icon by font awesome
 */
public class FontsView extends TextView {
    private static final String TAG = FontsView.class.getSimpleName();
    //Cache the font load status to improve performance
    private static Typeface font;

    public FontsView(Context context) {
        super(context);
        setFont(context);
    }

    public FontsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setFont(context);
    }

    public FontsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setFont(context);
    }

    private void setFont(Context context) {
        // prevent exception in Android Studio / ADT interface builder
        if (this.isInEditMode()) {
            return;
        }

        //Check for font is already loaded
        if(font == null) {
            try {
                font = Typeface.createFromAsset(context.getAssets(), "fontawesome-webfont.ttf");
                Log.d(TAG, "Font awesome loaded");
            } catch (RuntimeException e) {
                Log.e(TAG, "Font awesome not loaded");
            }
        }

        //Finally set the font
        setTypeface(font);
    }
}

package com.sourcecanyon.whatsClone.animations;

import android.view.View;

/**
 * Created by Abderrahim El imame on 7/31/16.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/bencherif_el
 */

public class ViewAudioProxy {  public static float getAlpha(View view) {
    if (ViewAudioAnimation.NEED_PROXY) {
        return ViewAudioAnimation.wrap(view).getAlpha();
    } else {
        return view.getAlpha();
    }
}

    public static void setAlpha(View view, float alpha) {
        if (ViewAudioAnimation.NEED_PROXY) {
            ViewAudioAnimation.wrap(view).setAlpha(alpha);
        } else {
            view.setAlpha(alpha);
        }
    }

    public static float getPivotX(View view) {
        if (ViewAudioAnimation.NEED_PROXY) {
            return ViewAudioAnimation.wrap(view).getPivotX();
        } else {
            return view.getPivotX();
        }
    }

    public static void setPivotX(View view, float pivotX) {
        if (ViewAudioAnimation.NEED_PROXY) {
            ViewAudioAnimation.wrap(view).setPivotX(pivotX);
        } else {
            view.setPivotX(pivotX);
        }
    }

    public static float getPivotY(View view) {
        if (ViewAudioAnimation.NEED_PROXY) {
            return ViewAudioAnimation.wrap(view).getPivotY();
        } else {
            return view.getPivotY();
        }
    }

    public static void setPivotY(View view, float pivotY) {
        if (ViewAudioAnimation.NEED_PROXY) {
            ViewAudioAnimation.wrap(view).setPivotY(pivotY);
        } else {
            view.setPivotY(pivotY);
        }
    }

    public static float getRotation(View view) {
        if (ViewAudioAnimation.NEED_PROXY) {
            return ViewAudioAnimation.wrap(view).getRotation();
        } else {
            return view.getRotation();
        }
    }

    public static void setRotation(View view, float rotation) {
        if (ViewAudioAnimation.NEED_PROXY) {
            ViewAudioAnimation.wrap(view).setRotation(rotation);
        } else {
            view.setRotation(rotation);
        }
    }

    public static float getRotationX(View view) {
        if (ViewAudioAnimation.NEED_PROXY) {
            return ViewAudioAnimation.wrap(view).getRotationX();
        } else {
            return view.getRotationX();
        }
    }

    public void setRotationX(View view, float rotationX) {
        if (ViewAudioAnimation.NEED_PROXY) {
            ViewAudioAnimation.wrap(view).setRotationX(rotationX);
        } else {
            view.setRotationX(rotationX);
        }
    }

    public static float getRotationY(View view) {
        if (ViewAudioAnimation.NEED_PROXY) {
            return ViewAudioAnimation.wrap(view).getRotationY();
        } else {
            return view.getRotationY();
        }
    }

    public void setRotationY(View view, float rotationY) {
        if (ViewAudioAnimation.NEED_PROXY) {
            ViewAudioAnimation.wrap(view).setRotationY(rotationY);
        } else {
            view.setRotationY(rotationY);
        }
    }

    public static float getScaleX(View view) {
        if (ViewAudioAnimation.NEED_PROXY) {
            return ViewAudioAnimation.wrap(view).getScaleX();
        } else {
            return view.getScaleX();
        }
    }

    public static void setScaleX(View view, float scaleX) {
        if (ViewAudioAnimation.NEED_PROXY) {
            ViewAudioAnimation.wrap(view).setScaleX(scaleX);
        } else {
            view.setScaleX(scaleX);
        }
    }

    public static float getScaleY(View view) {
        if (ViewAudioAnimation.NEED_PROXY) {
            return ViewAudioAnimation.wrap(view).getScaleY();
        } else {
            return view.getScaleY();
        }
    }

    public static void setScaleY(View view, float scaleY) {
        if (ViewAudioAnimation.NEED_PROXY) {
            ViewAudioAnimation.wrap(view).setScaleY(scaleY);
        } else {
            view.setScaleY(scaleY);
        }
    }

    public static int getScrollX(View view) {
        if (ViewAudioAnimation.NEED_PROXY) {
            return ViewAudioAnimation.wrap(view).getScrollX();
        } else {
            return view.getScrollX();
        }
    }

    public static void setScrollX(View view, int value) {
        if (ViewAudioAnimation.NEED_PROXY) {
            ViewAudioAnimation.wrap(view).setScrollX(value);
        } else {
            view.setScrollX(value);
        }
    }

    public static int getScrollY(View view) {
        if (ViewAudioAnimation.NEED_PROXY) {
            return ViewAudioAnimation.wrap(view).getScrollY();
        } else {
            return view.getScrollY();
        }
    }

    public static void setScrollY(View view, int value) {
        if (ViewAudioAnimation.NEED_PROXY) {
            ViewAudioAnimation.wrap(view).setScrollY(value);
        } else {
            view.setScrollY(value);
        }
    }

    public static float getTranslationX(View view) {
        if (ViewAudioAnimation.NEED_PROXY) {
            return ViewAudioAnimation.wrap(view).getTranslationX();
        } else {
            return view.getTranslationX();
        }
    }

    public static void setTranslationX(View view, float translationX) {
        if (ViewAudioAnimation.NEED_PROXY) {
            ViewAudioAnimation.wrap(view).setTranslationX(translationX);
        } else {
            view.setTranslationX(translationX);
        }
    }

    public static float getTranslationY(View view) {
        if (ViewAudioAnimation.NEED_PROXY) {
            return ViewAudioAnimation.wrap(view).getTranslationY();
        } else {
            return view.getTranslationY();
        }
    }

    public static void setTranslationY(View view, float translationY) {
        if (ViewAudioAnimation.NEED_PROXY) {
            ViewAudioAnimation.wrap(view).setTranslationY(translationY);
        } else {
            view.setTranslationY(translationY);
        }
    }

    public static float getX(View view) {
        if (ViewAudioAnimation.NEED_PROXY) {
            return ViewAudioAnimation.wrap(view).getX();
        } else {
            return view.getX();
        }
    }

    public static void setX(View view, float x) {
        if (ViewAudioAnimation.NEED_PROXY) {
            ViewAudioAnimation.wrap(view).setX(x);
        } else {
            view.setX(x);
        }
    }

    public static float getY(View view) {
        if (ViewAudioAnimation.NEED_PROXY) {
            return ViewAudioAnimation.wrap(view).getY();
        } else {
            return view.getY();
        }
    }

    public static void setY(View view, float y) {
        if (ViewAudioAnimation.NEED_PROXY) {
            ViewAudioAnimation.wrap(view).setY(y);
        } else {
            view.setY(y);
        }
    }

    public static Object wrap(View view) {
        if (ViewAudioAnimation.NEED_PROXY) {
            return ViewAudioAnimation.wrap(view);
        } else {
            return view;
        }
    }
}

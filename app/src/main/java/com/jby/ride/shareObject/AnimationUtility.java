package com.jby.ride.shareObject;

import android.content.Context;
import android.view.View;

import com.jby.ride.R;

public class AnimationUtility {

    /*
    *
    *       Fading 1000 ms
    *
    * */
    public void fadeInVisible(Context context, View view){
        view.startAnimation(android.view.animation.AnimationUtils.loadAnimation(context, R.anim.fade_in));
        view.setVisibility(View.VISIBLE);
    }

    public void fadeOutInvisible(Context context, View view){
        view.startAnimation(android.view.animation.AnimationUtils.loadAnimation(context, R.anim.fade_out));
        view.setVisibility(View.INVISIBLE);
    }

    public void fadeOutGone(Context context, View view){
        view.startAnimation(android.view.animation.AnimationUtils.loadAnimation(context, R.anim.fade_out));
        view.setVisibility(View.GONE);
    }

    /*
    *
    *       Fading 300 ms
    *
    * */
    public void fastFadeInVisible(Context context, View view){
        view.startAnimation(android.view.animation.AnimationUtils.loadAnimation(context, R.anim.fade_in_fast));
        view.setVisibility(View.VISIBLE);
    }

    public void fastFadeOutInvisible(Context context, View view){
        view.startAnimation(android.view.animation.AnimationUtils.loadAnimation(context, R.anim.fade_out_fast));
        view.setVisibility(View.INVISIBLE);
    }

    public void fastFadeOutGone(Context context, View view){
        view.startAnimation(android.view.animation.AnimationUtils.loadAnimation(context, R.anim.fade_out_fast));
        view.setVisibility(View.GONE);
    }

    /*
    *
    *
    *
    * */

    public void minimize(Context context, View view){
        view.startAnimation(android.view.animation.AnimationUtils.loadAnimation(context, R.anim.minimize));
        view.setVisibility(View.VISIBLE);
    }

    public void slideOut(Context context, View view){
        view.startAnimation(android.view.animation.AnimationUtils.loadAnimation(context, R.anim.slide_out_right));
        view.setVisibility(View.GONE);
    }

}

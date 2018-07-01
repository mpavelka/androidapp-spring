package com.miloslavpavelka.spring;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.res.ResourcesCompat;
import android.util.AttributeSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;

import com.gelitenight.waveview.library.WaveView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mpavelka on 01/07/2018.
 */

public class WaterLevelView extends WaveView {

    ObjectAnimator waterLevelAnimator;

    // Constructors
    public WaterLevelView(Context context) {
        super(context);
        init();
    }
    public WaterLevelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    public WaterLevelView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        this.setWaveColor(
                ResourcesCompat.getColor(getResources(), R.color.colorWaveBack, null),
                ResourcesCompat.getColor(getResources(), R.color.colorWaveFront, null));
        this.setBorder(10, Color.parseColor("#44FFFFFF"));
        this.setShowWave(true);
        this.setShapeType(WaveView.ShapeType.SQUARE);

        // horizontal animation - wave waves infinitely.
        ObjectAnimator waveShiftAnim = ObjectAnimator.ofFloat(
                    this, "waveShiftRatio", 0f, 1f);
        waveShiftAnim.setRepeatCount(ValueAnimator.INFINITE);
        waveShiftAnim.setDuration(1000);
        waveShiftAnim.setInterpolator(new LinearInterpolator());

        // amplitude animation - wave grows big then grows small, repeatedly
        ObjectAnimator amplitudeAnim = ObjectAnimator.ofFloat(
                    this, "amplitudeRatio", 0f, 0.03f);
        amplitudeAnim.setRepeatCount(ValueAnimator.INFINITE);
        amplitudeAnim.setRepeatMode(ValueAnimator.REVERSE);
        amplitudeAnim.setDuration(5000);
        amplitudeAnim.setInterpolator(new LinearInterpolator());

        List<Animator> animators = new ArrayList<>();
        animators.add(waveShiftAnim);
        animators.add(amplitudeAnim);

        AnimatorSet mAnimatorSet = new AnimatorSet();
        mAnimatorSet.playTogether(animators);
        mAnimatorSet.start();
    }

    public void animateWaterLevelRatio(float targetLevel) {
        // top border correction
        targetLevel = (float) (targetLevel * 0.9);

        if (waterLevelAnimator != null) {
            waterLevelAnimator.cancel();
            waterLevelAnimator = null;
        }

        float currentWaterLevelRatio = this.getWaterLevelRatio();

        waterLevelAnimator = ObjectAnimator.ofFloat(
                this, "waterLevelRatio", currentWaterLevelRatio, targetLevel);
        waterLevelAnimator.setDuration(2000);
        waterLevelAnimator.setInterpolator(new DecelerateInterpolator());
        waterLevelAnimator.start();
    }

}

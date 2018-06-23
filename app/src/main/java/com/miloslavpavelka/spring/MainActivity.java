package com.miloslavpavelka.spring;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import com.gelitenight.waveview.library.WaveView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    public final String TAG = "MainActivity";

    private int consumedMl;
    private TextView textConsumedMlValue,
                     textDeficit;
    private EditText editTextPlan,
                     editTextFrom,
                     editTextTo;

    private WaveView mWaveView;
    private AnimatorSet mAnimatorSet;
    private ConstraintLayout mConstraintLayoutStats;

    private ObjectAnimator waveShiftAnim,
                           amplitudeAnim;
    private AnimatorSet waterAnimSet = null;

    // Plan
    private int fromHourOfDay,
                fromMinute,
                toHourOfDay,
                toMinute,
                planMl,
                planDeficitMl=0;

    // Positions
    private float currentWaterLevel = 0.0f,
                  currentStatsLevel = 3.0f;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Stats layout
        mConstraintLayoutStats = (ConstraintLayout) findViewById(R.id.linear_layout_stats);
        textConsumedMlValue = (TextView)findViewById(R.id.text_consumed_ml_value);
        textConsumedMlValue.setText("0ml");
        textDeficit = (TextView)findViewById(R.id.text_deficit_value);
        textDeficit.setText("0ml");

        // DRINK button
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDrinkDialog();
            }
        });

        // Wave
        mWaveView = (WaveView) findViewById(R.id.wave);
        Log.i(TAG, Integer.toString(R.color.colorWaveBack)+" vs. "+Integer.toString(Color.parseColor("#4480d6ff")));
        mWaveView.setWaveColor(
                ResourcesCompat.getColor(getResources(), R.color.colorWaveBack, null),
                ResourcesCompat.getColor(getResources(), R.color.colorWaveFront, null));
        mWaveView.setBorder(10, Color.parseColor("#44FFFFFF"));
        mWaveView.setShowWave(true);
        mWaveView.setShapeType(WaveView.ShapeType.SQUARE);

        // Edit Texts
        editTextPlan = (EditText) findViewById(R.id.edit_text_plan);
        editTextFrom = (EditText) findViewById(R.id.edit_text_from);
        editTextTo   = (EditText) findViewById(R.id.edit_text_to);
        editTextPlan.setClickable(true);
        editTextPlan.setFocusable(false);
        editTextPlan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlanMlPickerFragment planMlPickerFragment = new PlanMlPickerFragment();
                planMlPickerFragment.setOnPlanSetListener(new PlanMlPickerFragment.OnPlanSetListener() {
                    @Override
                    public void onPlanSet(int plan) {
                        setPlan(plan);
                        computeDeficit();
                        setConsumedMl(consumedMl);
                    }
                });
                planMlPickerFragment.setInitValue(planMl);
                planMlPickerFragment.show(getSupportFragmentManager(), "planMlDialog");
            }
        });
        editTextFrom.setClickable(true);
        editTextFrom.setFocusable(false);
        editTextFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Time Picker
                TimePickerFragment timePickerFragment = new TimePickerFragment();
                timePickerFragment.setOnTimeSetListener(new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        setFromTime(hourOfDay, minute);
                        //editTextFrom.setText(String.format("%02d", hourOfDay)+":"+String.format("%02d", minute));
                        computeDeficit();
                    }
                });
                timePickerFragment.show(getSupportFragmentManager(), "timePickerFrom");
            }
        });
        editTextTo.setClickable(true);
        editTextTo.setFocusable(false);
        editTextTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Time Picker
                TimePickerFragment timePickerFragment = new TimePickerFragment();
                timePickerFragment.setOnTimeSetListener(new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        setToTime(hourOfDay, minute);
                        //editTextTo.setText(String.format("%02d", hourOfDay)+":"+String.format("%02d", minute));
                        computeDeficit();
                    }
                });
                timePickerFragment.show(getSupportFragmentManager(), "timePickerTo");
            }
        });

        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        // Initial values
        setFromTime(
                sharedPref.getInt(getString(R.string.sp_from_hour_of_day), 8),
                sharedPref.getInt(getString(R.string.sp_from_minute), 0));
        setToTime(
                sharedPref.getInt(getString(R.string.sp_to_hour_of_day), 22),
                sharedPref.getInt(getString(R.string.sp_to_minute), 0));
        setPlan(sharedPref.getInt(getString(R.string.sp_plan), 3000));
        consumedMl = sharedPref.getInt(getString(R.string.sp_consumed_ml), 0);

        initWaterAnimation();

    }

    @Override
    protected void onResume() {
        super.onResume();
        computeDeficit();
        setConsumedMl(this.consumedMl);
    }

    private void computeDeficit() {
        // current time
        Calendar cal = Calendar.getInstance();
        int currentHourOfDay = cal.get(Calendar.HOUR_OF_DAY),
            currentMinute    = cal.get(Calendar.MINUTE),
            origPlanDeficitMl = this.planDeficitMl;


        int elapsedMinutes = 60*(currentHourOfDay-fromHourOfDay) - fromMinute + currentMinute;
        int planRangeMinutes = 60*(toHourOfDay-fromHourOfDay) - fromMinute + toMinute;
        // Linear interpolation
        int idealConsumedMl = (int)(this.planMl * ((float)elapsedMinutes/(float)planRangeMinutes));
        this.planDeficitMl = idealConsumedMl - this.consumedMl;
        this.planDeficitMl = this.planDeficitMl < 0 ? 0 : this.planDeficitMl;

        ValueAnimator animator = ValueAnimator.ofInt(origPlanDeficitMl, this.planDeficitMl);
        animator.setDuration(1000);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                // This happens on the UI thread apparently
                textDeficit.setText(animation.getAnimatedValue().toString()+"ml");
            }
        });
        animator.start();
    }

    protected void setPlan(int ml) {
        planMl = ml;
        editTextPlan.setText(Integer.toString(ml)+"ml");
        // Store to shared preferences
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(getString(R.string.sp_plan), planMl);
        editor.commit();
    }

    protected void setFromTime(int hourOfDay, int minute) {
        this.fromHourOfDay = hourOfDay;
        fromMinute = minute;
        // Store to shared preferences
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(getString(R.string.sp_from_hour_of_day), hourOfDay);
        editor.putInt(getString(R.string.sp_from_minute), minute);
        editor.commit();
        editTextFrom.setText(String.format("%02d", hourOfDay)+":"+String.format("%02d", minute));
    }

    protected void setToTime(int hourOfDay, int minute) {
        toHourOfDay = hourOfDay;
        toMinute = minute;
        // Store to shared preferences
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(getString(R.string.sp_to_hour_of_day), hourOfDay);
        editor.putInt(getString(R.string.sp_to_minute), minute);
        editor.commit();
        editTextTo.setText(String.format("%02d", hourOfDay)+":"+String.format("%02d", minute));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    /*
    * Displays the drink! dialog
    * Sets a dialog's button click listener
    *
    * */
    public void showDrinkDialog() {
        final DrinkDialogFragment drinkDialogFragment = new DrinkDialogFragment();
        drinkDialogFragment.setOnButtonDrinkClick(new Runnable() {
            @Override
            public void run() {
                int result = drinkDialogFragment.getResult();
                setConsumedMl(consumedMl + result);
                computeDeficit();
            }
        });
        drinkDialogFragment.show(getSupportFragmentManager(), "drinkDialog");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_reset:
                setConsumedMl(0);
                computeDeficit();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /*
    * Starts the value animation of consumed water value
    * */
    private void setConsumedMl(int consumedMl) {
        int origConsumedMl = this.consumedMl;
        this.consumedMl = consumedMl;

        // Store to shared preferences;
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(getString(R.string.sp_consumed_ml), consumedMl);
        editor.commit();

        // Animate
        ValueAnimator animator = ValueAnimator.ofInt(origConsumedMl, consumedMl);
        animator.setDuration(1000);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                // This happens on the UI thread apparently
                textConsumedMlValue.setText(animation.getAnimatedValue().toString()+"ml");
            }
        });
        animator.start();
        animateWaterLevelAndStats(1-((float)this.consumedMl/planMl));
    }


    private void initWaterAnimation() {
        List<Animator> animators = new ArrayList<>();

        // horizontal animation - wave waves infinitely.
        if (waveShiftAnim == null) {
            waveShiftAnim = ObjectAnimator.ofFloat(
                    mWaveView, "waveShiftRatio", 0f, 1f);
            waveShiftAnim.setRepeatCount(ValueAnimator.INFINITE);
            waveShiftAnim.setDuration(1000);
            waveShiftAnim.setInterpolator(new LinearInterpolator());
            animators.add(waveShiftAnim);
        }

        // amplitude animation - wave grows big then grows small, repeatedly
        if (amplitudeAnim == null) {
            amplitudeAnim = ObjectAnimator.ofFloat(
                    mWaveView, "amplitudeRatio", 0f, 0.03f);
            amplitudeAnim.setRepeatCount(ValueAnimator.INFINITE);
            amplitudeAnim.setRepeatMode(ValueAnimator.REVERSE);
            amplitudeAnim.setDuration(5000);
            amplitudeAnim.setInterpolator(new LinearInterpolator());
            animators.add(amplitudeAnim);
        }

        if (waterAnimSet == null) {
            mAnimatorSet = new AnimatorSet();
            mAnimatorSet.playTogether(animators);
            mAnimatorSet.start();
        }
    }

    private void animateWaterLevelAndStats(float percentage) {
        List<Animator> animators = new ArrayList<>();
        int animateLevelDuration = 2000;

        // Target level has a constraint of 60% of the screen;
        final float targetLevel = 0.9f * percentage;

        // vertical animation.
        // water level increases from 0 to center of WaveView
        ObjectAnimator waterLevelAnim = ObjectAnimator.ofFloat(
                mWaveView, "waterLevelRatio", currentWaterLevel, targetLevel);
        waterLevelAnim.setDuration(animateLevelDuration);
        waterLevelAnim.setInterpolator(new DecelerateInterpolator());
        animators.add(waterLevelAnim);

        // Stats position animation
        ValueAnimator statsPosAnim = ValueAnimator.ofFloat(0.0f, 1.0f);
        statsPosAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                View container = (View) mConstraintLayoutStats.getParent();
                float containerHeight = container.getHeight(),
                        statsHeight     = mConstraintLayoutStats.getHeight(),
                        animatedValue   = (float) animation.getAnimatedValue();
                if (containerHeight > 0) {
                    // Snap stats to water level
                    float currentStatsTranslation = (1.0f-currentStatsLevel)*containerHeight,
                          targetStatsTranslation = (1.0f-targetLevel)*containerHeight,
                          translationY = currentStatsTranslation + (targetStatsTranslation-currentStatsTranslation)*animatedValue;
                    // Add "margin" to the translation
                    float marginTop = 20,
                          marginBottom = 40;
                    translationY = translationY + Helpers.pxFromDp(getApplicationContext(), marginTop);
                    // Snap stats to bottom if overflow
                    if (translationY + statsHeight + marginBottom >= containerHeight)
                        translationY = containerHeight-statsHeight-marginBottom;
                    // Set translation
                    mConstraintLayoutStats.setTranslationY( translationY);
                } else {
                    // Height of the container not yet computed
                    mConstraintLayoutStats.setTranslationY(0.0f);
                }
            }
        });
        statsPosAnim.setDuration(animateLevelDuration);
        statsPosAnim.setInterpolator(new DecelerateInterpolator());
        animators.add(statsPosAnim);

        mAnimatorSet = new AnimatorSet();
        mAnimatorSet.playTogether(animators);
        mAnimatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                currentStatsLevel = targetLevel;
                currentWaterLevel = targetLevel;
            }
            @Override
            public void onAnimationStart(Animator animation) {}
            @Override
            public void onAnimationCancel(Animator animation) {}
            @Override
            public void onAnimationRepeat(Animator animation) {}
        });
        mAnimatorSet.start();
    }

    private void initNotifications() {
        
    }
}




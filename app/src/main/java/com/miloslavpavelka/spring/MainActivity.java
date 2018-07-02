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

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    public final String TAG = "MainActivity";

    private ConstraintLayout mConstraintLayoutStats;
    private WaterLevelView waterLevelView;
    private EditText
            editTextPlan,
            editTextPlanFrom,
            editTextPlanTo;
    private TextView
            textConsumedMl,
            textDeficitMl;
    private SpringManager springManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize Spring Manager and load data
        springManager = new SpringManager(getApplicationContext());
        springManager.load();


        editTextPlan = (EditText) findViewById(R.id.edit_text_plan);
        editTextPlanFrom = (EditText) findViewById(R.id.edit_text_from);
        editTextPlanTo = (EditText) findViewById(R.id.edit_text_to);
        textConsumedMl = (TextView) findViewById(R.id.text_consumed_ml_value);
        textDeficitMl = (TextView) findViewById(R.id.text_deficit_value);
        waterLevelView = (WaterLevelView) findViewById(R.id.wave);
    }


    @Override
    protected void onResume() {
        super.onResume();
        springManager.evaluate();
        updateUI();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_reset:
                springManager.setConsumedMl(0);
                springManager.store();
                springManager.evaluate();
                updateUI();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void setTextPlan(int plan) {
        editTextPlan.setText(Integer.toString(plan)+"ml");
    }
    private void setTextPlanFrom(int hourOfDay, int minute) {
        editTextPlanFrom.setText(String.format("%02d", springManager.getPlanFromHourOfDay())+":"+String.format("%02d", springManager.getPlanFromMinute()));
    }
    private void setTextPlanTo(int phourOfDay, int minute) {
        editTextPlanTo.setText(String.format("%02d", springManager.getPlanToHourOfDay())+":"+String.format("%02d", springManager.getPlanToMinute()));
    }
    private void setTextConsumedMl(int consumedMl) {
        textConsumedMl.setText(Integer.toString(consumedMl)+"ml");
    }
    private void setTextConsumedMl(String consumedMl) {
        textConsumedMl.setText(consumedMl+"ml");
    }
    private void setTextDeficitMl(int deficitMl) {
        textDeficitMl.setText(Integer.toString(deficitMl)+"ml");
    }
    private void setTextDeficitMl(String deficitMl) {
        textDeficitMl.setText(deficitMl+"ml");
    }

    private void updateUI() {
        updateEditTexts();
        Log.d(TAG, "consumedPlanRatio: "+Float.toString(springManager.getConsumedPlanRatio()));
        waterLevelView.animateWaterLevelRatio(
                (float)1-springManager.getConsumedPlanRatio()
        );
        animateConsumptionMl();
        animateDeficitMl();
    }

    private void updateEditTexts() {
        setTextPlan(
                springManager.getDailyPlanMl());
        setTextPlanFrom(
                springManager.getPlanFromHourOfDay(),
                springManager.getPlanFromMinute());
        setTextPlanTo(
                springManager.getPlanToHourOfDay(),
                springManager.getPlanToMinute());
    }

    private void animateConsumptionMl() {
        ValueAnimator animator = ValueAnimator.ofInt(
                springManager.getPrevConsumedMl(),
                springManager.getConsumedMl());
        animator.setDuration(1000);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                // This happens on the UI thread apparently
                setTextConsumedMl(animation.getAnimatedValue().toString());
            }
        });
        animator.start();

    }
    private void animateDeficitMl() {
        ValueAnimator animator = ValueAnimator.ofInt(
                springManager.getPrevDeficitMl(),
                springManager.getDeficitMl());
        animator.setDuration(1000);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                // This happens on the UI thread apparently
                setTextDeficitMl(animation.getAnimatedValue().toString());
            }
        });
        animator.start();

    }


    // Click listeners


    public void onClickEditTextPlan(View view) {
        PlanMlPickerFragment planMlPickerFragment = new PlanMlPickerFragment();
        planMlPickerFragment.setOnPlanSetListener(new PlanMlPickerFragment.OnPlanSetListener() {
            @Override
            public void onPlanSet(int plan) {
                springManager.setDailyPlanMl(plan);
                springManager.store();
                springManager.evaluate();
                updateUI();
            }
        });
        planMlPickerFragment.setInitValue(springManager.getDailyPlanMl());
        planMlPickerFragment.show(getSupportFragmentManager(), "planMlDialog");

    }


    public void onClickEditTextPlanFrom(View view) {
        TimePickerFragment timePickerFragment = new TimePickerFragment();
        timePickerFragment.setOnTimeSetListener(new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int fromHourOfDay, int fromMinute) {
                int toHourOfDay = springManager.getPlanToHourOfDay();
                int toMinute = springManager.getPlanToMinute();

                // Correcture of times
                if (fromHourOfDay ==23) {
                    springManager.setPlanFrom(fromHourOfDay, 0);
                    springManager.setPlanTo(23, 59);
                }
                else if (fromHourOfDay >= toHourOfDay) {
                    springManager.setPlanFrom(fromHourOfDay, fromMinute);
                    springManager.setPlanTo(fromHourOfDay+1, fromMinute);
                }
                else {
                    springManager.setPlanFrom(fromHourOfDay, fromMinute);
                }
                springManager.store();
                springManager.evaluate();
                updateUI();
            }
        });
        timePickerFragment.show(getSupportFragmentManager(), "timePickerFrom");

    }


    public void onClickEditTextPlanTo(View view) {
        TimePickerFragment timePickerFragment = new TimePickerFragment();
        timePickerFragment.setOnTimeSetListener(new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int toHourOfDay, int toMinute) {
                int fromHourOfDay = springManager.getPlanFromHourOfDay();
                int fromMinute = springManager.getPlanFromMinute();

                // Correcture of times
                if (toHourOfDay <= 1) {
                    springManager.setPlanFrom(0, toMinute);
                    springManager.setPlanTo(1, toMinute);
                }
                else if (toHourOfDay <= fromHourOfDay) {
                    springManager.setPlanFrom(toHourOfDay-1, toMinute);
                    springManager.setPlanTo(toHourOfDay, toMinute);
                }
                else {
                    springManager.setPlanTo(toHourOfDay, toMinute);
                }
                springManager.store();
                springManager.evaluate();
                updateUI();
            }
        });
        timePickerFragment.show(getSupportFragmentManager(), "timePickerTo");
    }

    public void onClickFAB(View view) {
        final DrinkDialogFragment drinkDialogFragment = new DrinkDialogFragment();
        drinkDialogFragment.setOnButtonDrinkClick(new Runnable() {
            @Override
            public void run() {
                springManager.drinkMl(drinkDialogFragment.getResult());
                springManager.store();
                springManager.evaluate();
                updateUI();

            }
        });
        drinkDialogFragment.show(getSupportFragmentManager(), "drinkDialog");
    }


}




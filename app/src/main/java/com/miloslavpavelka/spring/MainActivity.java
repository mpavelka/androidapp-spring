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

    private ConstraintLayout mConstraintLayoutStats;
    private WaterLevelView waterLevelView;
    private EditText
            editTextPlan,
            editTextPlanFrom,
            editTextPlanTo;
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
        waterLevelView = (WaterLevelView) findViewById(R.id.wave);
    }


    @Override
    protected void onResume() {
        super.onResume();
        updateEditTexts();
        waterLevelView.animateWaterLevelRatio(
            springManager.getConsumedPlanRatio()
        );
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


    // Click listeners


    public void onClickEditTextPlan(View view) {
        PlanMlPickerFragment planMlPickerFragment = new PlanMlPickerFragment();
        planMlPickerFragment.setOnPlanSetListener(new PlanMlPickerFragment.OnPlanSetListener() {
            @Override
            public void onPlanSet(int plan) {
                springManager.setDailyPlanMl(plan);
                updateEditTexts();
            }
        });
        planMlPickerFragment.setInitValue(springManager.getDailyPlanMl());
        planMlPickerFragment.show(getSupportFragmentManager(), "planMlDialog");

    }


    public void onClickEditTextPlanFrom(View view) {
        TimePickerFragment timePickerFragment = new TimePickerFragment();
        timePickerFragment.setOnTimeSetListener(new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                springManager.setPlanFromHourOfDay(hourOfDay);
                springManager.setPlanFromMinute(minute);
                updateEditTexts();
            }
        });
        timePickerFragment.show(getSupportFragmentManager(), "timePickerFrom");

    }


    public void onClickEditTextPlanTo(View view) {
        TimePickerFragment timePickerFragment = new TimePickerFragment();
        timePickerFragment.setOnTimeSetListener(new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                springManager.setPlanToHourOfDay(hourOfDay);
                springManager.setPlanToMinute(minute);
                updateEditTexts();
            }
        });
        timePickerFragment.show(getSupportFragmentManager(), "timePickerTo");
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

            }
        });
        drinkDialogFragment.show(getSupportFragmentManager(), "drinkDialog");
    }

}




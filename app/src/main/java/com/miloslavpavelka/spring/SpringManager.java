package com.miloslavpavelka.spring;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Calendar;

/**
 * Created by mpavelka on 01/07/2018.
 */

public class SpringManager {
    Context context;

    static final String
            SHARED_PREF_NAME = "springmgr",
            SP_FROM_HOUR_OF_DAY = "fromHourOfDay",
            SP_FROM_MINUTE = "fromMinute",
            SP_TO_HOUR_OF_DAY = "fromHourOfDay",
            SP_TO_MINUTE = "fromHourOfDay",
            SP_CONSUMED_ML = "consumedMl",
            SP_DAILY_PLAN_ML = "dailyPlanMl";

    private int
            dailyPlanMl,
            consumedMl,
            deficitMl,
            planFromHourOfDay,
            planFromMinute,
            planToHourOfDay,
            planToMinute;

    SpringManager(Context context) {
        this.context = context;
        this.reset();
    }

    private SharedPreferences getPreferences() {
        return this.context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
    }

    void reset() {
        this.dailyPlanMl = 0;
        this.consumedMl = 0;
        this.deficitMl = 0;
        this.planFromHourOfDay = 0;
        this.planFromMinute = 0;
        this.planToHourOfDay = 0;
        this.planToMinute = 0;
    }

    void load() {
        SharedPreferences prefs = this.getPreferences();
        this.dailyPlanMl = prefs.getInt(SP_DAILY_PLAN_ML, 0);
        this.consumedMl = prefs.getInt(SP_CONSUMED_ML, 0);
        this.planFromHourOfDay = prefs.getInt(SP_FROM_HOUR_OF_DAY, 0);
        this.planFromMinute = prefs.getInt(SP_FROM_MINUTE, 0);
        this.planToHourOfDay = prefs.getInt(SP_TO_HOUR_OF_DAY, 0);
        this.planToMinute = prefs.getInt(SP_TO_MINUTE, 0);

        // Compute deficit
        this.deficitMl = this.computeDeficitMl();
    }

    void store() {
        SharedPreferences prefs = this.getPreferences();
        SharedPreferences.Editor editor = prefs.edit();

        editor.putInt(SP_FROM_HOUR_OF_DAY, this.planFromHourOfDay);
        editor.putInt(SP_FROM_MINUTE, this.planFromMinute);
        editor.putInt(SP_TO_HOUR_OF_DAY, this.planToHourOfDay);
        editor.putInt(SP_TO_MINUTE, this.planToMinute);
        editor.putInt(SP_CONSUMED_ML, this.consumedMl);
        editor.putInt(SP_DAILY_PLAN_ML, this.dailyPlanMl);

        editor.commit();
    }

    int computeDeficitMl() {
        int resultMl,
            elapsedMinutes,
            planRangeMinutes,
            idealConsumedMl;

        // Current time
        Calendar cal = Calendar.getInstance();
        int currentHourOfDay = cal.get(Calendar.HOUR_OF_DAY),
            currentMinute    = cal.get(Calendar.MINUTE);

        // Compute
        elapsedMinutes = 60*(currentHourOfDay-this.planFromHourOfDay) - this.planFromMinute + currentMinute;
        planRangeMinutes = 60*(this.planToHourOfDay-this.planFromHourOfDay) - this.planFromMinute + this.planToMinute;
        idealConsumedMl = (int)(this.dailyPlanMl * ((float)elapsedMinutes/(float)planRangeMinutes)); // Linear interpolation
        resultMl = idealConsumedMl - this.consumedMl;

        // Return
        if (resultMl <= 0)
            return 0;
        return resultMl;

    }
}

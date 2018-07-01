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
            SP_TO_HOUR_OF_DAY = "toHourOfDay",
            SP_TO_MINUTE = "toMinute",
            SP_CONSUMED_ML = "consumedMl",
            SP_DAILY_PLAN_ML = "dailyPlanMl";

    private int
            dailyPlanMl,
            consumedMl,
            deficitMl,
            prevConsumedMl,
            prevDeficitMl,
            planFromHourOfDay,
            planFromMinute,
            planToHourOfDay,
            planToMinute;

    SpringManager(Context context) {
        this.context = context;
        this.deficitMl = 0;
        this.reset();
    }

    // Setters
    public void setDailyPlanMl(int ml) {
        dailyPlanMl = ml;
        setDeficitMl(computeDeficitMl());
    }
    public void setConsumedMl(int ml) {
        prevConsumedMl = consumedMl;
        consumedMl = ml;
        setDeficitMl(computeDeficitMl());
    }
    public void setPlanFrom(int hourOfDay, int minute) {
        planFromHourOfDay = hourOfDay;
        planFromMinute = minute;
        setDeficitMl(computeDeficitMl());
    }
    public void setPlanTo(int hourOfDay, int minute) {
        planToHourOfDay = hourOfDay;
        planToMinute = minute;
        setDeficitMl(computeDeficitMl());
    }
    public void drinkMl(int ml) {
        setConsumedMl(getConsumedMl()+ml);
    }
    protected void setDeficitMl(int ml) {
        prevDeficitMl = deficitMl;
        deficitMl = ml;
    }

    // Getters
    public int getDailyPlanMl() {
        return dailyPlanMl;
    }
    public int getPrevConsumedMl() {
        return prevConsumedMl;
    }
    public int getPrevDeficitMl() {
        return prevDeficitMl;
    }
    public int getConsumedMl() {
        return consumedMl;
    }
    public int getDeficitMl() {
        return deficitMl;
    }
    public int getPlanFromHourOfDay() {
        return planFromHourOfDay;
    }
    public int getPlanFromMinute() {
        return planFromMinute;
    }
    public int getPlanToHourOfDay() {
        return planToHourOfDay;
    }
    public int getPlanToMinute() {
        return planToMinute;
    }
    public float getConsumedPlanRatio() {
        if (dailyPlanMl == 0)
            return (float)1;
        return (float)consumedMl/(float)dailyPlanMl;
    }

    private SharedPreferences getPreferences() {
        return this.context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
    }

    void reset() {
        this.dailyPlanMl = 0;
        this.prevConsumedMl = this.consumedMl;
        this.prevDeficitMl = this.deficitMl;
        this.consumedMl = 0;
        this.deficitMl = 0;
        this.planFromHourOfDay = 0;
        this.planFromMinute = 0;
        this.planToHourOfDay = 0;
        this.planToMinute = 0;
    }

    void load() {
        SharedPreferences prefs = this.getPreferences();
        planFromHourOfDay = prefs.getInt(SP_FROM_HOUR_OF_DAY, 0);
        planFromMinute = prefs.getInt(SP_FROM_MINUTE, 0);
        planToHourOfDay = prefs.getInt(SP_TO_HOUR_OF_DAY, 0);
        planToMinute = prefs.getInt(SP_TO_MINUTE, 0);
        consumedMl = prefs.getInt(SP_CONSUMED_ML, 0);
        dailyPlanMl = prefs.getInt(SP_DAILY_PLAN_ML, 0);

        // Compute deficit
        this.deficitMl = this.computeDeficitMl();
    }

    void store() {
        SharedPreferences prefs = this.getPreferences();
        SharedPreferences.Editor editor = prefs.edit();

        editor.putInt(SP_FROM_HOUR_OF_DAY, planFromHourOfDay);
        editor.putInt(SP_FROM_MINUTE, planFromMinute);
        editor.putInt(SP_TO_HOUR_OF_DAY, planToHourOfDay);
        editor.putInt(SP_TO_MINUTE, planToMinute);
        editor.putInt(SP_CONSUMED_ML, consumedMl);
        editor.putInt(SP_DAILY_PLAN_ML, dailyPlanMl);

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

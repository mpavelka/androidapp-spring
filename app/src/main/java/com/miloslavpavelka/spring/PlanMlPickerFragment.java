package com.miloslavpavelka.spring;

import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.SeekBar;

import java.util.Calendar;
import java.util.HashMap;
/**
 * Created by mpavelka on 01/07/2017.
 */

public class PlanMlPickerFragment extends DialogFragment {
    final String TAG = "PlanMlPickerFragment";
    Button buttonDone;
    NumberPicker numberPicker;

//    HashMap<String, Integer> pickerValues;
    OnPlanSetListener onPlanSetListener = null;
    final int minValue=500;
    final int maxValue=5000;
    final int step=100;
    int initValue = 2500;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                               Bundle savedInstanceState) {
        // Use the current time as the default values for the picker
        View view = inflater.inflate(R.layout.dialog_plan, container, false);
        buttonDone = (Button)   view.findViewById(R.id.button_done);
        numberPicker = (NumberPicker)  view.findViewById(R.id.number_picker);

        // Set values
        int valuesLength = (maxValue-minValue)/step;
        String [] pickerValues = new String[valuesLength];
        for (int i=0; i<valuesLength; i++) {
            pickerValues[i] = Integer.toString(indexToValue(i))+"ml";
        }
        numberPicker.setDisplayedValues(pickerValues);
        numberPicker.setFocusable(false);
        numberPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        numberPicker.setMinValue(0);
        numberPicker.setMaxValue(valuesLength-1);
        numberPicker.setValue(valueToIndex(initValue));
        buttonDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {onButtonDoneClick(v);}
        });

        return view;
    }

    public void setOnPlanSetListener(OnPlanSetListener onPlanSetListener) {
        this.onPlanSetListener = onPlanSetListener;
    }

    public void setInitValue(int initValue) {
        this.initValue = initValue;
    }

    private int indexToValue(int i) {
        return i*step+minValue;
    }

    private int valueToIndex(int val) {
        return (val-minValue)/step;
    }

    private void onButtonDoneClick(View v) {
        if (onPlanSetListener != null) {
            onPlanSetListener.onPlanSet(indexToValue(numberPicker.getValue()));
        }
        dismiss();
    }

    public static class OnPlanSetListener {
        public void onPlanSet(int plan) {}
    }

}

package com.miloslavpavelka.spring;


import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;


/**
 * Created by mpavelka on 20/06/2017.
 */

public class DrinkDialogFragment extends DialogFragment {

    Button   buttonDrink;
    SeekBar  seekDrinkMl;
    TextView textDrinkMl;

    float minMl  = 0,
          maxMl  = 500;
    int result = 0;

    Runnable _onResultRunnable = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout to use as dialog or embedded fragment
        View view = inflater.inflate(R.layout.dialog_drink, container, false);
        buttonDrink = (Button)   view.findViewById(R.id.button_drink);
        seekDrinkMl = (SeekBar)  view.findViewById(R.id.seek_drink_ml);
        textDrinkMl = (TextView) view.findViewById(R.id.text_drink_ml);


        // SeekBar Change listener
        seekDrinkMl.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                onSeekDrinkMlChange(seekBar, progress, fromUser); }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        seekDrinkMl.incrementProgressBy(2);


        // Button "drink!" clicked
        buttonDrink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {onButtonDrinkClick(v);}
        });

        return view;
    }

    private void onButtonDrinkClick(View v) {
        if (_onResultRunnable != null) {
            _onResultRunnable.run();
        }
        dismiss();
    }

    public void setOnButtonDrinkClick(Runnable r) {
        _onResultRunnable = r;
    }

    /*
     * Listener for seek bar change
    */
    public void onSeekDrinkMlChange(SeekBar seekBar, int progress, boolean fromUser) {
        result = (int)(progress*(maxMl-minMl)/100);
        textDrinkMl.setText(Integer.toString(result) + "ml");
    }

    public int getResult() {
        return result;
    }
}

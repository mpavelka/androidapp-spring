package com.miloslavpavelka.spring;

import android.content.Context;

/**
 * Created by mpavelka on 22/06/2017.
 */

public class Helpers {
    public static float dpFromPx(final Context context, final float px) {
        return px / context.getResources().getDisplayMetrics().density;
    }

    public static float pxFromDp(final Context context, final float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }
}

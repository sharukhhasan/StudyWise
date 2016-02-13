package com.sharukhhasan.amazonaws.mobile.util;

import android.os.Handler;
import android.os.Looper;

/**
 * Created by Sharukh on 2/13/16.
 */
public class ThreadUtils
{
    private ThreadUtils() {
    }

    public static void runOnUiThread(final Runnable runnable) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            new Handler(Looper.getMainLooper()).post(runnable);
        } else {
            runnable.run();
        }
    }
}

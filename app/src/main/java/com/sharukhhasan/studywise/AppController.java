package com.sharukhhasan.studywise;

import android.app.Application;
import android.support.multidex.MultiDexApplication;
import android.util.Log;

import com.sharukhhasan.amazonaws.mobile.AWSMobileClient;

/**
 * Created by Sharukh on 2/12/16.
 */
public class AppController extends MultiDexApplication
{
    private final static String LOG_TAG = Application.class.getSimpleName();

    @Override
    public void onCreate() {
        Log.d(LOG_TAG, "Application.onCreate - Initializing application...");
        super.onCreate();
        initializeApplication();
        Log.d(LOG_TAG, "Application.onCreate - Application initialized OK");
    }

    private void initializeApplication() {
        AWSMobileClient.initializeMobileClientIfNecessary(getApplicationContext());

        // ...Put any application-specific initialization logic here...
    }
}

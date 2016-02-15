package com.sharukhhasan.studywise;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.facebook.FacebookSdk;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.sharukhhasan.amazonaws.mobile.AWSMobileClient;
import com.sharukhhasan.studywise.managers.SharedPreferenceManager;

/**
 * Created by Sharukh on 2/12/16.
 */
public class AppController extends Application implements Application.ActivityLifecycleCallbacks {

    @Override
    public void onCreate()
    {
        super.onCreate();
        instantiateManagers();
    }

    private void instantiateManagers()
    {
        FacebookSdk.sdkInitialize(this);
        Fresco.initialize(this);
        AWSMobileClient.initializeMobileClientIfNecessary(getApplicationContext());
        SharedPreferenceManager.getSharedInstance().initiateSharedPreferences(getApplicationContext());
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {}

    @Override
    public void onActivityStarted(Activity activity) {}

    @Override
    public void onActivityResumed(Activity activity) {}

    @Override
    public void onActivityPaused(Activity activity) {}

    @Override
    public void onActivityStopped(Activity activity) {}

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}

    @Override
    public void onActivityDestroyed(Activity activity) {}

}

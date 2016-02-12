package com.sharukhhasan.studywise;

import android.app.Application;

import com.facebook.FacebookSdk;
import com.firebase.client.Firebase;

/**
 * Created by Sharukh on 2/12/16.
 */
public class AppController extends Application
{
    @Override
    public void onCreate() {
        super.onCreate();
        FacebookSdk.sdkInitialize(this);
        Firebase.setAndroidContext(this);
    }
}

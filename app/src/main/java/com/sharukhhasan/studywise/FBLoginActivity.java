package com.sharukhhasan.studywise;

import android.support.v4.app.FragmentActivity;

import android.os.Bundle;

import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.facebook.FacebookCallback;
import com.facebook.login.widget.LoginButton;
import com.facebook.login.LoginResult;

/**
 * Created by Sharukh on 2/12/16.
 */
public class FBLoginActivity extends FragmentActivity
{
    CallbackManager callbackManager;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        LoginButton loginButton = (LoginButton) view.findViewById(R.id.usersettings_fragment_login_button);
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() { ... });
    }
}

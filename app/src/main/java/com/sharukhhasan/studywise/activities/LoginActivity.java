package com.sharukhhasan.studywise.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.sharukhhasan.studywise.R;

import butterknife.InjectView;

public class LoginActivity extends AppCompatActivity
{

    private static final String TAG = "LoginActivity";
    private static final String FIREBASE_URL = "https://studywise.firebaseio.com";
    private static final int REQUEST_SIGNUP = 0;

    private LoginButton loginButton;
    private CallbackManager callbackManager;

    @InjectView(R.id.input_username)
    EditText usernameInput;
    @InjectView(R.id.input_password) EditText passwordInput;
    @InjectView(R.id.btn_login)
    Button _loginButton;
    @InjectView(R.id.link_signup)
    TextView _signupLink;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();

        loginButton = (LoginButton) findViewById(R.id.facebook_login_button);

        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // App code
            }

            @Override
            public void onCancel() {
                // App code
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
            }
        });

    }

}

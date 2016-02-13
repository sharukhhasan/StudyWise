package com.sharukhhasan.studywise.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.cognito.CognitoSyncManager;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
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

        // Initialize the Amazon Cognito credentials provider
        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(getApplicationContext(), "us-east-1:0a3b862e-4534-4a7a-b37a-3bd0a46bc67d", // Identity Pool ID
                Regions.US_EAST_1 // Region
        );

        // Initialize the Cognito Sync client
        CognitoSyncManager syncClient = new CognitoSyncManager(getApplicationContext(), Regions.US_EAST_1, // Region
                credentialsProvider);

        AmazonS3 s3 = new AmazonS3Client(credentialsProvider);
        TransferUtility transferUtility = new TransferUtility(s3, getApplicationContext());

        CognitoSyncManager client = new CognitoSyncManager(getApplicationContext(), Regions.US_EAST_1, credentialsProvider);
    }

}

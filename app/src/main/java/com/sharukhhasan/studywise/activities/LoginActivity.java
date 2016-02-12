package com.sharukhhasan.studywise.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.sharukhhasan.studywise.R;

import java.util.Arrays;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class LoginActivity extends AppCompatActivity
{

    private static final String TAG = "LoginActivity";
    private static final int REQUEST_SIGNUP = 0;

    private Firebase ref;
    private Button loginButton;
    private CallbackManager callbackManager;

    @InjectView(R.id.input_username)
    EditText usernameInput;
    @InjectView(R.id.input_password) EditText passwordInput;
    @InjectView(R.id.btn_login)
    Button _loginButton;
    @InjectView(R.id.link_signup)
    TextView _signupLink;

    private com.facebook.GraphRequest.Callback friendsCallback = new GraphRequest.Callback()
    {
        @Override
        public void onCompleted (GraphResponse response)
        {
            Log.v("FACEBOOK", response.getRawResponse());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        callbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("user_friends", "user_photos"));
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult)
            {
                onFacebookAccessTokenChange(loginResult.getAccessToken());
            }

            @Override
            public void onCancel ()
            {
                //App code
            }

            @Override
            public void onError (FacebookException exception)
            {
                //App code
            }
        });

        ButterKnife.inject(this);

        View myView = this.findViewById(android.R.id.content);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
        {
            myView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        _loginButton.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                String username = usernameInput.getText().toString();
                String password = passwordInput.getText().toString();
                //loginUser(username.toLowerCase(Locale.getDefault()), password);
            }
        });

        _signupLink.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                startActivityForResult(intent, REQUEST_SIGNUP);
            }
        });

        Firebase.setAndroidContext(this);

        ref = new Firebase("https://studywise.firebaseIO.com");

    }

    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void onFacebookAccessTokenChange (AccessToken token)
    {
        if (token != null) {
            ref.authWithOAuthToken("facebook", token.getToken(), new Firebase.AuthResultHandler()
            {

                @Override
                public void onAuthenticated(AuthData authData)
                {
                    //The Facebook user is now authenticated with your Firebase app
                    getFacebookFriends();
                }

                @Override
                public void onAuthenticationError(FirebaseError firebaseError)
                {
                    //there was an error
                }
            });
        }
        else
        {
            /* Logged out of Facebook so do a logout from the Firebase app */
            ref.unauth();
        }
    }

    private void getFacebookFriends()
    {
        Bundle parameters = new Bundle();
        parameters.putString("redirect", "false");
        new GraphRequest(AccessToken.getCurrentAccessToken(), "/me/picture", parameters,
                HttpMethod.GET, friendsCallback).executeAsync();
    }


}

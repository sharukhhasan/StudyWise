package com.sharukhhasan.amazonaws.mobile.user.signIn;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.sharukhhasan.amazonaws.mobile.user.IdentityManager;
import com.sharukhhasan.amazonaws.mobile.util.ThreadUtils;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

/**
 * Created by Sharukh on 2/13/16.
 */
public class FacebookSignInProvider implements SignInProvider
{
    private static final String LOG_TAG = FacebookSignInProvider.class.getSimpleName();
    public static final String COGNITO_LOGIN_KEY_FACEBOOK = "graph.facebook.com";

    private CallbackManager facebookCallbackManager;

    private String userName;
    private String userImageUrl;

    final static int REFRESH_TOKEN_POLLING_INTERVAL_MS = 50;
    final static int REFRESH_TOKEN_POLLING_RETRIES = 300; // 15 seconds

    public FacebookSignInProvider(final Context context)
    {
        if(!FacebookSdk.isInitialized())
        {
            Log.d(LOG_TAG, "Initializing Facebook SDK...");
            FacebookSdk.sdkInitialize(context);
            Utils.logKeyHash(context);
        }
    }

    private AccessToken getSignedInToken()
    {
        final AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if(accessToken != null && !accessToken.isExpired())
        {
            Log.d(LOG_TAG, "Facebook Access Token is OK");
            return accessToken;
        }

        Log.d(LOG_TAG,"Facebook Access Token is null or expired.");
        return null;
    }

    @Override
    public boolean isRequestCodeOurs(final int requestCode)
    {
        return FacebookSdk.isFacebookRequestCode(requestCode);
    }

    @Override
    public void handleActivityResult(final int requestCode, final int resultCode, final Intent data)
    {
        facebookCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public View.OnClickListener initializeSignInButton(final Activity signInActivity, final View buttonView,
                                                       final IdentityManager.SignInResultsHandler resultsHandler) {

        FacebookSdk.sdkInitialize(signInActivity);

        if(buttonView == null)
        {
            throw new IllegalArgumentException("Facebook login button view not passed in.");
        }

        facebookCallbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().registerCallback(facebookCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult)
            {
                Log.d(LOG_TAG, "Facebook provider sign-in succeeded.");
                resultsHandler.onSuccess(FacebookSignInProvider.this);
            }

            @Override
            public void onCancel()
            {
                Log.d(LOG_TAG, "Facebook provider sign-in canceled.");
                resultsHandler.onCancel(FacebookSignInProvider.this);
            }

            @Override
            public void onError(FacebookException exception)
            {
                Log.e(LOG_TAG, "Facebook provider sign-in error: " + exception.getMessage());
                resultsHandler.onError(FacebookSignInProvider.this, exception);
            }
        });

        final View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                LoginManager.getInstance().logInWithReadPermissions(signInActivity, Arrays.asList("public_profile"));
            }
        };

        buttonView.setOnClickListener(listener);
        return listener;
    }

    @Override
    public String getDisplayName()
    {
        return "Facebook";
    }

    @Override
    public String getCognitoLoginKey()
    {
        return COGNITO_LOGIN_KEY_FACEBOOK;
    }

    @Override
    public boolean isUserSignedIn()
    {
        return getSignedInToken() != null;
    }

    @Override
    public String getToken()
    {
        AccessToken accessToken = getSignedInToken();
        if(accessToken != null)
        {
            return accessToken.getToken();
        }
        return null;
    }

    @Override
    public String refreshToken()
    {
        AccessToken accessToken = getSignedInToken();

        if(accessToken == null)
        {
            ThreadUtils.runOnUiThread(new Runnable() {
                @Override
                public void run()
                {
                    AccessToken.refreshCurrentAccessTokenAsync();
                }
            });

            int retries = 0;
            while(accessToken == null && (retries++ < REFRESH_TOKEN_POLLING_RETRIES))
            {
                try {
                    Thread.sleep(REFRESH_TOKEN_POLLING_INTERVAL_MS);
                } catch (final InterruptedException ex) {
                    Log.w(LOG_TAG, "Unexpected Interrupt of refreshToken()", ex);
                    throw new RuntimeException(ex);
                }
                accessToken = getSignedInToken();
            }
            if(accessToken == null)
            {
                return null;
            }
        }
        return accessToken.getToken();
    }

    @Override
    public void signOut()
    {
        clearUserInfo();
        LoginManager.getInstance().logOut();
    }

    private void clearUserInfo()
    {
        userName = null;
        userImageUrl = null;
    }

    @Override
    public String getUserName()
    {
        return userName;
    }

    @Override
    public String getUserImageUrl()
    {
        return userImageUrl;
    }

    public void reloadUserInfo()
    {
        clearUserInfo();
        if(!isUserSignedIn())
        {
            return;
        }

        final Bundle parameters = new Bundle();
        parameters.putString("fields", "name,picture.type(large)");
        final GraphRequest graphRequest = new GraphRequest(AccessToken.getCurrentAccessToken(), "me");
        graphRequest.setParameters(parameters);
        GraphResponse response = graphRequest.executeAndWait();

        JSONObject json = response.getJSONObject();
        try {
            userName = json.getString("name");
            userImageUrl = json.getJSONObject("picture")
                    .getJSONObject("data")
                    .getString("url");

        } catch (final JSONException jsonException) {
            Log.e(LOG_TAG, "Unable to get Facebook user info. " + jsonException.getMessage() + "\n" + response, jsonException);
        }
    }
}

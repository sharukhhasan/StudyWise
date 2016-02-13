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
    /** Log tag. */
    private static final String LOG_TAG = FacebookSignInProvider.class.getSimpleName();

    /** The Cognito login key for Facebook to be used in the Cognito login Map. */
    public static final String COGNITO_LOGIN_KEY_FACEBOOK = "graph.facebook.com";

    /** Facebook's callback manager. */
    private CallbackManager facebookCallbackManager;

    /** User's name. */
    private String userName;

    /** User's image Url. */
    private String userImageUrl;

    /** Polling interval for checking refreshed token status */
    final static int REFRESH_TOKEN_POLLING_INTERVAL_MS = 50;

    /** Number of retries to poll when refreshing the token. */
    final static int REFRESH_TOKEN_POLLING_RETRIES = 300; // 15 seconds

    /**
     * Constuctor. Intitializes the SDK and debug logs the app KeyHash that must be set up with
     * the facebook backend to allow login from the app.
     *
     * @param context the context.
     */
    public FacebookSignInProvider(final Context context) {

        if (!FacebookSdk.isInitialized()) {
            Log.d(LOG_TAG, "Initializing Facebook SDK...");
            FacebookSdk.sdkInitialize(context);
            Utils.logKeyHash(context);
        }
    }

    /**
     * @return the Facebook AccessToken when signed-in with a non-expired token.
     */
    private AccessToken getSignedInToken() {
        final AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if (accessToken != null && !accessToken.isExpired()) {
            Log.d(LOG_TAG, "Facebook Access Token is OK");
            return accessToken;
        }

        Log.d(LOG_TAG,"Facebook Access Token is null or expired.");
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isRequestCodeOurs(final int requestCode) {
        return FacebookSdk.isFacebookRequestCode(requestCode);
    }

    /** {@inheritDoc} */
    @Override
    public void handleActivityResult(final int requestCode, final int resultCode, final Intent data) {
        facebookCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    /** {@inheritDoc} */
    @Override
    public View.OnClickListener initializeSignInButton(final Activity signInActivity, final View buttonView,
                                                       final IdentityManager.SignInResultsHandler resultsHandler) {

        FacebookSdk.sdkInitialize(signInActivity);

        if (buttonView == null) {
            throw new IllegalArgumentException("Facebook login button view not passed in.");
        }

        facebookCallbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().registerCallback(facebookCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(LOG_TAG, "Facebook provider sign-in succeeded.");
                resultsHandler.onSuccess(FacebookSignInProvider.this);
            }

            @Override
            public void onCancel() {
                Log.d(LOG_TAG, "Facebook provider sign-in canceled.");
                resultsHandler.onCancel(FacebookSignInProvider.this);
            }

            @Override
            public void onError(FacebookException exception) {
                Log.e(LOG_TAG, "Facebook provider sign-in error: " + exception.getMessage());
                resultsHandler.onError(FacebookSignInProvider.this, exception);
            }
        });

        final View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginManager.getInstance().logInWithReadPermissions(signInActivity,
                        Arrays.asList("public_profile"));
            }
        };

        buttonView.setOnClickListener(listener);
        return listener;
    }

    /** {@inheritDoc} */
    @Override
    public String getDisplayName() {
        return "Facebook";
    }

    /** {@inheritDoc} */
    @Override
    public String getCognitoLoginKey() {
        return COGNITO_LOGIN_KEY_FACEBOOK;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isUserSignedIn() {
        return getSignedInToken() != null;
    }

    /** {@inheritDoc} */
    @Override
    public String getToken() {
        AccessToken accessToken = getSignedInToken();
        if (accessToken != null) {
            return accessToken.getToken();
        }
        return null;
    }

    @Override
    public String refreshToken() {
        AccessToken accessToken = getSignedInToken();
        // getSignedInToken() returns null if token is expired.
        if (accessToken == null) {
            ThreadUtils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // refreshes access token in the background.
                    AccessToken.refreshCurrentAccessTokenAsync();
                }
            });

            // There is no method to check the status of the refresh operation, so we have to resort to polling.
            int retries = 0;
            while (accessToken == null && (retries++ < REFRESH_TOKEN_POLLING_RETRIES)) {
                try {
                    Thread.sleep(REFRESH_TOKEN_POLLING_INTERVAL_MS);
                } catch (final InterruptedException ex) {
                    Log.w(LOG_TAG, "Unexpected Interrupt of refreshToken()", ex);
                    throw new RuntimeException(ex);
                }
                accessToken = getSignedInToken();
            }
            if (accessToken == null) {
                return null;
            }
        }
        return accessToken.getToken();
    }

    /** {@inheritDoc} */
    @Override
    public void signOut() {
        clearUserInfo();
        LoginManager.getInstance().logOut();
    }

    private void clearUserInfo() {
        userName = null;
        userImageUrl = null;
    }

    /** {@inheritDoc} */
    @Override
    public String getUserName() {
        return userName;
    }

    /** {@inheritDoc} */
    @Override
    public String getUserImageUrl() {
        return userImageUrl;
    }

    /** {@inheritDoc} */
    public void reloadUserInfo() {
        clearUserInfo();
        if (!isUserSignedIn()) {
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
            Log.e(LOG_TAG,
                    "Unable to get Facebook user info. " + jsonException.getMessage() + "\n" + response,
                    jsonException);
            // Nothing much we can do here.
        }
    }
}

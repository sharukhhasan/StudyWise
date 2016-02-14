package com.sharukhhasan.amazonaws.mobile.user.signIn;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

import com.sharukhhasan.amazonaws.mobile.user.IdentityManager;
import com.sharukhhasan.amazonaws.mobile.user.IdentityProvider;

/**
 * Created by Sharukh on 2/13/16.
 */
public interface SignInProvider extends IdentityProvider
{

    boolean isRequestCodeOurs(int requestCode);

    void handleActivityResult(int requestCode, int resultCode, Intent data);

    View.OnClickListener initializeSignInButton(Activity signInActivity, View buttonView, IdentityManager.SignInResultsHandler resultsHandler);
}

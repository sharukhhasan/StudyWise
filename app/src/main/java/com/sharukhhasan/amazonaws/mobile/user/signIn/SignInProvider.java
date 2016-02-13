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
    /**
     * Call isRequestCodeOurs to determine if this provider should handle an activity result.
     * @param requestCode the requestCode from a previous call to onActivityResult.
     * @return true if the request code is from this provider, otherwise false.
     */
    boolean isRequestCodeOurs(int requestCode);

    /**
     * Call handleActivityResult to handle the activity result.
     * @param requestCode the request code.
     * @param resultCode the result code.
     * @param data the result intent.
     */
    void handleActivityResult(int requestCode, int resultCode, Intent data);

    /**
     * Initialize the sign-in button for the sign-in activity.
     * @param signInActivity the activity for sign-in.
     * @param buttonView the view for the sign-in button to initialize.
     * @param resultsHandler the resultsHandler for provider sign-in.
     * @return the onClickListener for the button to be able to override the listener,
     *         and null if the button cannot be initialized.
     */
    View.OnClickListener initializeSignInButton(Activity signInActivity,
                                                View buttonView,
                                                IdentityManager.SignInResultsHandler resultsHandler);
}

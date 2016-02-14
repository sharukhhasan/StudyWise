package com.sharukhhasan.studywise.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;
import com.sharukhhasan.amazonaws.mobile.AWSMobileClient;
import com.sharukhhasan.amazonaws.mobile.user.signIn.SignInManager;
import com.sharukhhasan.amazonaws.mobile.user.signIn.SignInProvider;
import com.sharukhhasan.amazonaws.mobile.user.IdentityManager;
import com.sharukhhasan.amazonaws.mobile.user.IdentityProvider;
import com.sharukhhasan.studywise.R;

import java.util.concurrent.CountDownLatch;

public class SplashActivity extends Activity
{
    private final static String LOG_TAG = SplashActivity.class.getSimpleName();
    private final CountDownLatch timeoutLatch = new CountDownLatch(1);
    private SignInManager signInManager;

    /**
     * SignInResultsHandler handles the results from sign-in for a previously signed in user.
     */
    private class SignInResultsHandler implements IdentityManager.SignInResultsHandler
    {
        @Override
        public void onSuccess(final IdentityProvider provider)
        {
            Log.d(LOG_TAG, String.format("User sign-in with previous %s provider succeeded", provider.getDisplayName()));

            SignInManager.dispose();

            Toast.makeText(SplashActivity.this, String.format("Sign-in with %s succeeded.", provider.getDisplayName()), Toast.LENGTH_LONG).show();

            AWSMobileClient.defaultMobileClient().getIdentityManager().loadUserInfoAndImage(provider, new Runnable()
            {
                @Override
                public void run()
                {
                    goMain();
                }
            });
        }

        /**
         * For the case where the user previously was signed in, and an attempt is made to sign the
         * user back in again, there is not an option for the user to cancel, so this is overriden
         * as a stub.
         * @param provider the identity provider with which the user attempted sign-in.
         */
        @Override
        public void onCancel(final IdentityProvider provider)
        {
            Log.wtf(LOG_TAG, "Cancel can't happen when handling a previously sign-in user.");
        }

        /**
         * Receives the sign-in result that an error occurred signing in with the previously signed
         * in provider and re-directs the user to the sign-in activity to sign in again.
         * @param provider the identity provider with which the user attempted sign-in.
         * @param ex the exception that occurred.
         */
        @Override
        public void onError(final IdentityProvider provider, Exception ex)
        {
            Log.e(LOG_TAG, String.format("Cognito credentials refresh with %s provider failed. Error: %s", provider.getDisplayName(), ex.getMessage()), ex);
            Toast.makeText(SplashActivity.this, String.format("Sign-in with %s failed.", provider.getDisplayName()), Toast.LENGTH_LONG).show();
            goLogIn();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.d(LOG_TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        final Thread thread = new Thread(new Runnable() {
            public void run()
            {
                signInManager = SignInManager.getInstance(SplashActivity.this);
                final SignInProvider provider = signInManager.getPreviouslySignedInProvider();

                if(provider != null)
                {
                    signInManager.refreshCredentialsWithProvider(SplashActivity.this, provider, new SignInResultsHandler());
                }
                else
                {
                    goLogIn();
                }

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) { }

                timeoutLatch.countDown();
            }
        });
        thread.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        timeoutLatch.countDown();
        return true;
    }

    private void goAfterSplashTimeout(final Intent intent)
    {
        final Thread thread = new Thread(new Runnable() {
            public void run()
            {
                try {
                    timeoutLatch.await();
                } catch (InterruptedException e) {
                }

                SplashActivity.this.runOnUiThread(new Runnable() {
                    public void run()
                    {
                        startActivity(intent);
                        finish();
                    }
                });
            }
        });
        thread.start();
    }

    protected void goMain()
    {
        Log.d(LOG_TAG, "Launching Main Activity...");
        goAfterSplashTimeout(new Intent(this, HomeActivity.class));
    }

    protected void goLogIn()
    {
        Log.d(LOG_TAG, "Launching Sign-in Activity...");
        goAfterSplashTimeout(new Intent(this, LoginActivity.class));
    }
}

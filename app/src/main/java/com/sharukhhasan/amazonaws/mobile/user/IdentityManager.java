package com.sharukhhasan.amazonaws.mobile.user;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.SDKGlobalConfiguration;
import com.amazonaws.auth.AWSBasicCognitoIdentityProvider;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.sharukhhasan.amazonaws.mobile.AWSConfiguration;
import com.sharukhhasan.amazonaws.mobile.util.ThreadUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Sharukh on 2/13/16.
 */
public class IdentityManager
{
    public interface IdentityHandler
    {

        public void handleIdentityID(final String identityId);

        public void handleError(final Exception exception);
    }

    private static final String LOG_TAG = IdentityManager.class.getSimpleName();

    private CognitoCachingCredentialsProvider credentialsProvider;

    private final ExecutorService executorService = Executors.newFixedThreadPool(2);

    private IdentityProvider currentIdentityProvider = null;

    private SignInResultsAdapter resultsAdapter;

    private final HashSet<SignInStateChangeListener> signInStateChangeListeners = new HashSet<>();

    public class AWSRefreshingCognitoIdentityProvider extends AWSBasicCognitoIdentityProvider
    {
        public AWSRefreshingCognitoIdentityProvider(final String accountId, final String identityPoolId, final ClientConfiguration clientConfiguration)
        {
            super(accountId, identityPoolId, clientConfiguration);
        }

        @Override
        public String refresh()
        {
            if(currentIdentityProvider != null)
            {
                final String newToken = currentIdentityProvider.refreshToken();
                getLogins().put(currentIdentityProvider.getCognitoLoginKey(), newToken);
            }
            return super.refresh();
        }
    }

    public IdentityManager(final Context appContext, final ClientConfiguration clientConfiguration)
    {
        Log.d(LOG_TAG, "IdentityManager init");
        initializeCognito(appContext, clientConfiguration);
    }

    private void initializeCognito(final Context context, final ClientConfiguration clientConfiguration)
    {
        AWSRefreshingCognitoIdentityProvider cognitoIdentityProvider = new AWSRefreshingCognitoIdentityProvider(
                null, AWSConfiguration.AMAZON_COGNITO_IDENTITY_POOL_ID, clientConfiguration);

        credentialsProvider = new CognitoCachingCredentialsProvider(context, cognitoIdentityProvider, AWSConfiguration.AMAZON_COGNITO_REGION, clientConfiguration);

    }

    public boolean areCredentialsExpired()
    {
        final Date credentialsExpirationDate = credentialsProvider.getSessionCredentitalsExpiration();
        if(credentialsExpirationDate == null)
        {
            return true;
        }
        long currentTime = System.currentTimeMillis() - (long)(SDKGlobalConfiguration.getGlobalTimeOffset() * 1000);
        return (credentialsExpirationDate.getTime() - currentTime) < 0;
    }

    public CognitoCachingCredentialsProvider getCredentialsProvider()
    {
        return this.credentialsProvider;
    }

    public void getUserID(final IdentityHandler handler)
    {
        new Thread(new Runnable() {
            Exception exception = null;

            @Override
            public void run()
            {
                String identityId = null;

                try {
                    identityId = getCredentialsProvider().getIdentityId();
                } catch (final Exception exception) {
                    this.exception = exception;
                    Log.e(LOG_TAG, exception.getMessage(), exception);
                } finally {
                    final String result = identityId;

                    ThreadUtils.runOnUiThread(new Runnable() {
                        @Override
                        public void run()
                        {
                            if(exception != null)
                            {
                                handler.handleError(exception);
                                return;
                            }
                            handler.handleIdentityID(result);
                        }
                    });
                }
            }
        }).start();
    }

    public interface SignInResultsHandler
    {

        void onSuccess(IdentityProvider provider);

        void onCancel(IdentityProvider provider);

        void onError(IdentityProvider provider, Exception ex);
    }

    public interface SignInStateChangeListener
    {
        void onUserSignedIn();

        void onUserSignedOut();
    }

    private class SignInResultsAdapter implements SignInResultsHandler
    {
        final private SignInResultsHandler handler;

        public SignInResultsAdapter(final SignInResultsHandler handler)
        {
            this.handler = handler;
        }

        public void onSuccess(final IdentityProvider provider)
        {
            Log.d(LOG_TAG, String.format("SignInResultsAdapter.onSuccess(): %s provider sign-in succeeded.", provider.getDisplayName()));
            loginWithProvider(provider);
        }

        private void onCognitoSuccess()
        {
            Log.d(LOG_TAG, "SignInResultsAdapter.onCognitoSuccess()");
            handler.onSuccess(currentIdentityProvider);
        }

        private void onCognitoError(final Exception ex)
        {
            Log.d(LOG_TAG, "SignInResultsAdapter.onCognitoError()", ex);
            final IdentityProvider provider = currentIdentityProvider;
            IdentityManager.this.signOut();
            handler.onError(provider, ex);
        }

        public void onCancel(final IdentityProvider provider)
        {
            Log.d(LOG_TAG, String.format("SignInResultsAdapter.onCancel(): %s provider sign-in canceled.", provider.getDisplayName()));
            handler.onCancel(provider);
        }

        public void onError(final IdentityProvider provider, final Exception ex)
        {
            Log.e(LOG_TAG, String.format("SignInResultsAdapter.onError(): %s provider error. %s", provider.getDisplayName(), ex.getMessage()), ex);
            handler.onError(provider, ex);
        }
    }

    public void addSignInStateChangeListener(final SignInStateChangeListener listener)
    {
        synchronized(signInStateChangeListeners)
        {
            signInStateChangeListeners.add(listener);
        }
    }

    public void removeSignInStateChangeListener(final SignInStateChangeListener listener)
    {
        synchronized(signInStateChangeListeners)
        {
            signInStateChangeListeners.remove(listener);
        }
    }

    public void setResultsHandler(final SignInResultsHandler signInResultsHandler)
    {
        if(signInResultsHandler == null)
        {
            throw new IllegalArgumentException("signInResultsHandler cannot be null.");
        }
        this.resultsAdapter = new SignInResultsAdapter(signInResultsHandler);
    }

    public SignInResultsAdapter getResultsAdapter()
    {
        return resultsAdapter;
    }

    public boolean isUserSignedIn()
    {
        final Map<String, String> logins = credentialsProvider.getLogins();
        if(logins == null || logins.size() == 0)
        {
            return false;
        }
        return true;
    }

    public void signOut()
    {
        if(currentIdentityProvider != null)
        {
            currentIdentityProvider.signOut();
            credentialsProvider.clear();
            currentIdentityProvider = null;

            synchronized(signInStateChangeListeners)
            {
                for(final SignInStateChangeListener listener : signInStateChangeListeners)
                {
                    listener.onUserSignedOut();
                }
            }
        }
    }

    private void refreshCredentialWithLogins(final Map<String, String> loginMap)
    {
        credentialsProvider.clear();
        credentialsProvider.withLogins(loginMap);
        Log.d(getClass().getSimpleName(), "refresh credentials");
        credentialsProvider.refresh();
        Log.d(getClass().getSimpleName(), "Cognito ID: " + credentialsProvider.getIdentityId());
        Log.d(getClass().getSimpleName(), "Cognito Credentials: " + credentialsProvider.getCredentials());
    }

    public void loginWithProvider(final IdentityProvider provider)
    {
        Log.d(LOG_TAG, "loginWithProvider");
        final Map<String, String> loginMap = new HashMap<String, String>();
        loginMap.put(provider.getCognitoLoginKey(), provider.getToken());
        currentIdentityProvider = provider;

        executorService.submit(new Runnable() {
            @Override
            public void run()
            {
                try {
                    refreshCredentialWithLogins(loginMap);
                } catch (Exception ex) {
                    resultsAdapter.onCognitoError(ex);
                    return;
                }

                resultsAdapter.onCognitoSuccess();

                synchronized(signInStateChangeListeners)
                {
                    for(final SignInStateChangeListener listener : signInStateChangeListeners)
                    {
                        listener.onUserSignedIn();
                    }
                }
            }
        });
    }

    public IdentityProvider getCurrentIdentityProvider()
    {
        return currentIdentityProvider;
    }

    private Bitmap userImage = null;

    private void loadUserImage(final String userImageUrl)
    {
        if(userImageUrl == null)
        {
            userImage = null;
            return;
        }

        try {
            final InputStream is = new URL(userImageUrl).openStream();
            userImage = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            Log.w(LOG_TAG, "Failed to prefetch user image: " + userImageUrl, e);
            userImage = null;
        }
    }

    public void loadUserInfoAndImage(final IdentityProvider provider, final Runnable onReloadComplete)
    {
        executorService.submit(new Runnable() {
            @Override
            public void run()
            {
                provider.reloadUserInfo();
                loadUserImage(provider.getUserImageUrl());
                ThreadUtils.runOnUiThread(onReloadComplete);
            }
        });
    }

    public Bitmap getUserImage()
    {
        return userImage;
    }

    public String getUserName()
    {
        return currentIdentityProvider == null ? null : currentIdentityProvider.getUserName();
    }
}

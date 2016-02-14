package com.sharukhhasan.amazonaws.mobile.user.signIn;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Looper;
import android.view.View;

import com.amazonaws.AmazonServiceException;
import com.sharukhhasan.amazonaws.mobile.AWSMobileClient;
import com.sharukhhasan.amazonaws.mobile.user.IdentityManager;
import com.sharukhhasan.amazonaws.mobile.user.IdentityProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Sharukh on 2/13/16.
 */
public class SignInManager
{
    private final int HTTP_CODE_SERVICE_UNAVAILABLE = 503;
    private final static String LOG_TAG = SignInManager.class.getSimpleName();
    private final Map<Class<? extends SignInProvider>, SignInProvider> signInProviders = new HashMap<>();
    private static SignInManager singleton = null;
    private Context context = null;

    private SignInManager(final Context context)
    {
        assert (singleton == null);
        singleton = this;

        this.context = context.getApplicationContext();
        AWSMobileClient.initializeMobileClientIfNecessary(context);

        final FacebookSignInProvider facebookSignInProvider = new FacebookSignInProvider(context);
        addSignInProvider(facebookSignInProvider);
    }

    public synchronized static SignInManager getInstance(final Context context)
    {
        if(singleton == null)
        {
            singleton = new SignInManager(context);
        }
        return singleton;
    }

    public synchronized static void dispose()
    {
        singleton = null;
    }

    public void addSignInProvider(final SignInProvider signInProvider)
    {
        signInProviders.put(signInProvider.getClass(), signInProvider);
    }

    public SignInProvider getPreviouslySignedInProvider()
    {
        for(final SignInProvider provider : signInProviders.values())
        {
            if(provider.isUserSignedIn())
            {
                return provider;
            }
        }
        return null;
    }

    private class SignInResultsAdapter implements IdentityManager.SignInResultsHandler
    {
        final private IdentityManager.SignInResultsHandler handler;
        final private Activity activity;

        public SignInResultsAdapter(final Activity activity, final IdentityManager.SignInResultsHandler handler)
        {
            this.handler = handler;
            this.activity = activity;
        }

        private Activity getActivity()
        {
            return activity;
        }

        private boolean runningFromMainThread()
        {
            return Looper.myLooper() == Looper.getMainLooper();
        }

        @Override
        public void onSuccess(final IdentityProvider provider)
        {
            if(!runningFromMainThread())
            {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run()
                    {
                        handler.onSuccess(provider);
                    }
                });
            }
            else
            {
                handler.onSuccess(provider);
            }
        }

        @Override
        public void onCancel(final IdentityProvider provider)
        {
            if(!runningFromMainThread())
            {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run()
                    {
                        handler.onCancel(provider);
                    }
                });
            }
            else
            {
                handler.onCancel(provider);
            }
        }

        @Override
        public void onError(final IdentityProvider provider, final Exception ex)
        {
            final AmazonServiceException asEx;
            if(ex instanceof AmazonServiceException)
            {
                asEx = (AmazonServiceException) ex;
            }
            else
            {
                asEx = new AmazonServiceException(ex.getMessage(), ex);
                final String operationString = provider.getDisplayName().toLowerCase() + "-sign-in";
                asEx.setErrorType(AmazonServiceException.ErrorType.Unknown);
                asEx.setServiceName(operationString);
                asEx.setStatusCode(HTTP_CODE_SERVICE_UNAVAILABLE);
                asEx.setErrorCode(Integer.toString(HTTP_CODE_SERVICE_UNAVAILABLE));
                asEx.setRequestId(operationString);
            }

            if(!runningFromMainThread())
            {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run()
                    {
                        handler.onError(provider, asEx);
                    }
                });
            }
            else
            {
                handler.onError(provider, asEx);
            }
        }
    }

    private SignInResultsAdapter resultsAdapter;

    public void refreshCredentialsWithProvider(final Activity activity, final SignInProvider provider, final IdentityManager.SignInResultsHandler resultsHandler)
    {
        if(provider == null)
        {
            throw new IllegalArgumentException("The sign-in provider cannot be null.");
        }

        if(provider.getToken() == null)
        {
            resultsHandler.onError(provider, new IllegalArgumentException("Given provider not previously logged in."));
        }

        final IdentityManager identityManager = AWSMobileClient.defaultMobileClient().getIdentityManager();
        resultsAdapter = new SignInResultsAdapter(activity, resultsHandler);
        identityManager.setResultsHandler(resultsAdapter);

        AWSMobileClient.defaultMobileClient().getIdentityManager().loginWithProvider(provider);
    }

    public void setResultsHandler(final Activity activity, final IdentityManager.SignInResultsHandler resultsHandler)
    {
        final IdentityManager identityManager = AWSMobileClient.defaultMobileClient().getIdentityManager();
        resultsAdapter = new SignInResultsAdapter(activity, resultsHandler);
        identityManager.setResultsHandler(resultsAdapter);
    }

    public View.OnClickListener initializeSignInButton(final Class<? extends SignInProvider> providerClass, final View buttonView)
    {
        final IdentityManager identityManager = AWSMobileClient.defaultMobileClient().getIdentityManager();
        final SignInProvider provider = findProvider(providerClass);

        return provider.initializeSignInButton(resultsAdapter.getActivity(), buttonView, identityManager.getResultsAdapter());
    }

    private SignInProvider findProvider(final Class<? extends SignInProvider> clazz)
    {
        final SignInProvider provider = signInProviders.get(clazz);

        if(provider == null)
        {
            throw new IllegalArgumentException("No such provider : " + clazz.getCanonicalName());
        }

        return provider;
    }

    public boolean handleActivityResult(final int requestCode, final int resultCode, final Intent data)
    {
        for(final SignInProvider provider : signInProviders.values())
        {
            if(provider.isRequestCodeOurs(requestCode))
            {
                provider.handleActivityResult(requestCode, resultCode, data);
                return true;
            }
        }

        return false;
    }

    private void openBrowser(String url)
    {
        if(!url.startsWith("http://") || !url.startsWith("https://"))
        {
            url = "http://" + url;
        }

        final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
        context.startActivity(intent);
    }
}

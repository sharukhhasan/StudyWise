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

    /**
     * Constructor.
     * @param context context.
     */
    private SignInManager(final Context context) {
        assert (singleton == null);
        singleton = this;

        this.context = context.getApplicationContext();
        AWSMobileClient.initializeMobileClientIfNecessary(context);

        // Initialize Facebook SDK.
        final FacebookSignInProvider facebookSignInProvider = new FacebookSignInProvider(context);
        addSignInProvider(facebookSignInProvider);

    }

    /**
     * Gets the singleton instance of this class.
     * @return instance
     */
    public synchronized static SignInManager getInstance(final Context context) {
        if (singleton == null) {
            singleton = new SignInManager(context);
        }
        return singleton;
    }

    public synchronized static void dispose() {
        singleton = null;
    }

    /**
     * Adds a sign-in identity provider.
     * @param signInProvider sign-in provider
     */
    public void addSignInProvider(final SignInProvider signInProvider) {
        signInProviders.put(signInProvider.getClass(), signInProvider);
    }

    /**
     * Call getPreviouslySignedInProvider to determine if the user was left signed-in when the app
     * was last running.  This should be called on a background thread since it may perform file
     * i/o.  If the user is signed in with a provider, this will return the provider for which the
     * user is signed in.  Subsequently, refreshCredentialsWithProvider should be called with the
     * provider returned from this method.
     * @return false if not already signed in, true if the user was signed in with a provider.
     */
    public SignInProvider getPreviouslySignedInProvider() {

        for (final SignInProvider provider : signInProviders.values()) {
            // Note: This method may block. This loop could potentially be sped
            // up by running these calls in parallel using an executorService.
            if (provider.isUserSignedIn()) {
                return provider;
            }
        }
        return null;
    }

    private class SignInResultsAdapter implements IdentityManager.SignInResultsHandler {
        final private IdentityManager.SignInResultsHandler handler;
        final private Activity activity;

        public SignInResultsAdapter(final Activity activity,
                                    final IdentityManager.SignInResultsHandler handler) {
            this.handler = handler;
            this.activity = activity;
        }

        private Activity getActivity() {
            return activity;
        }

        private boolean runningFromMainThread() {
            return Looper.myLooper() == Looper.getMainLooper();
        }

        /** {@inheritDoc} */
        @Override
        public void onSuccess(final IdentityProvider provider) {
            if (!runningFromMainThread()) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        handler.onSuccess(provider);
                    }
                });
            } else {
                handler.onSuccess(provider);
            }
        }

        /** {@inheritDoc} */
        @Override
        public void onCancel(final IdentityProvider provider) {
            if (!runningFromMainThread()) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        handler.onCancel(provider);
                    }
                });
            } else {
                handler.onCancel(provider);
            }
        }

        /** {@inheritDoc} */
        @Override
        public void onError(final IdentityProvider provider, final Exception ex) {
            final AmazonServiceException asEx;
            if (ex instanceof AmazonServiceException) {
                asEx = (AmazonServiceException) ex;
            } else {
                asEx = new AmazonServiceException(ex.getMessage(), ex);
                final String operationString = provider.getDisplayName().toLowerCase() + "-sign-in";
                asEx.setErrorType(AmazonServiceException.ErrorType.Unknown);
                asEx.setServiceName(operationString);
                asEx.setStatusCode(HTTP_CODE_SERVICE_UNAVAILABLE);
                asEx.setErrorCode(Integer.toString(HTTP_CODE_SERVICE_UNAVAILABLE));
                asEx.setRequestId(operationString);
            }

            if (!runningFromMainThread()) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        handler.onError(provider, asEx);
                    }
                });
            } else {
                handler.onError(provider, asEx);
            }
        }
    }

    private SignInResultsAdapter resultsAdapter;

    /**
     * Refresh Cognito credentials with a provider.  Results handlers are always called on the main
     * thread.
     * @param activity the calling activity.
     * @param provider the sign-in provider that was previously signed in.
     * @param resultsHandler the handler to receive results for credential refresh.
     */
    public void refreshCredentialsWithProvider(final Activity activity,
                                               final SignInProvider provider,
                                               final IdentityManager.SignInResultsHandler resultsHandler) {

        if (provider == null) {
            throw new IllegalArgumentException("The sign-in provider cannot be null.");
        }

        if (provider.getToken() == null) {
            resultsHandler.onError(provider,
                    new IllegalArgumentException("Given provider not previously logged in."));
        }

        final IdentityManager identityManager = AWSMobileClient.defaultMobileClient()
                .getIdentityManager();
        resultsAdapter = new SignInResultsAdapter(activity, resultsHandler);
        identityManager.setResultsHandler(resultsAdapter);

        AWSMobileClient.defaultMobileClient()
                .getIdentityManager()
                .loginWithProvider(provider);
    }

    /**
     * Sets the results handler to handle final results from sign-in.  Results handlers are
     * always called on the UI thread.
     * @param activity the calling activity.
     * @param resultsHandler the handler for results from sign-in with a provider.
     */
    public void setResultsHandler(final Activity activity,
                                  final IdentityManager.SignInResultsHandler resultsHandler) {
        final IdentityManager identityManager = AWSMobileClient.defaultMobileClient()
                .getIdentityManager();

        resultsAdapter = new SignInResultsAdapter(activity, resultsHandler);
        // Set the final results handler with the identity manager.
        identityManager.setResultsHandler(resultsAdapter);
    }

    /**
     * Call initializeSignInButton to intialize the logic for sign-in for a specific provider.
     * @param providerClass the SignInProvider class.
     * @param buttonView the view for the button associated with this provider.
     * @return the onClickListener for the button to be able to override the listener.
     */
    public View.OnClickListener initializeSignInButton(final Class<? extends SignInProvider> providerClass,
                                                       final View buttonView) {
        final IdentityManager identityManager = AWSMobileClient.defaultMobileClient()
                .getIdentityManager();

        final SignInProvider provider = findProvider(providerClass);

        // Initialize the sign in button with the identity manager's results adapter.
        return provider.initializeSignInButton(resultsAdapter.getActivity(),
                buttonView,
                identityManager.getResultsAdapter());
    }

    private SignInProvider findProvider(final Class<? extends SignInProvider> clazz) {

        final SignInProvider provider = signInProviders.get(clazz);

        if (provider == null) {
            throw new IllegalArgumentException("No such provider : " + clazz.getCanonicalName());
        }

        return provider;
    }

    /**
     * Handle the Activity result for login providers.
     * @param requestCode the request code.
     * @param resultCode the result code.
     * @param data result intent.
     * @return true if the sign-in manager handle the result, otherwise false.
     */
    public boolean handleActivityResult(final int requestCode, final int resultCode, final Intent data) {

        for (final SignInProvider provider : signInProviders.values()) {
            if (provider.isRequestCodeOurs(requestCode)) {
                provider.handleActivityResult(requestCode, resultCode, data);
                return true;
            }
        }

        return false;
    }

    private void openBrowser(String url) {
        if (!url.startsWith("http://") || !url.startsWith("https://")) {
            url = "http://" + url;
        }

        final Intent intent =
                new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_MULTIPLE_TASK
                | Intent.FLAG_ACTIVITY_NO_HISTORY);
        context.startActivity(intent);
    }
}

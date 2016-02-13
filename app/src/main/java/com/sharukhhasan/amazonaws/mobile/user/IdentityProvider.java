package com.sharukhhasan.amazonaws.mobile.user;

/**
 * Created by Sharukh on 2/13/16.
 */
public interface IdentityProvider
{
    /**
     * @return the display name for this provider.
     */
    String getDisplayName();

    /**
     * @return the key used by Cognito in its login map when refreshing credentials.
     */
    String getCognitoLoginKey();

    /**
     * Returns whether signed in with a provider.
     * Note: This call may block.
     * @return true if signed in with this provider.
     */
    boolean isUserSignedIn();

    /**
     * Call getToken to retrieve the access token from successful sign-in with this provider.
     * Note: This call may block if the access token is not already cached.
     * @return the access token suitable for use with Cognito.
     */
    String getToken();

    /**
     * Refreshes the token if it has expired.
     * Note: this call may block due to network access, and must be called from a background thread.
     * @return the refreshed access token, or null if the token cannot be refreshed.
     */
    String refreshToken();

    /**
     * Call signOut to sign out of this provider.
     */
    void signOut();

    /**
     * Gets the user's name, assuming user is signed in.
     * @return user name or null if not signed-in.
     */
    String getUserName();

    /**
     * Gets the user's image url, assuming user is signed in.
     * @return image or null if not signed-in or has no image.
     */
    String getUserImageUrl();

    /**
     * Force the provider to reload user name and image.
     * Note: this is a blocking call.
     */
    void reloadUserInfo();
}

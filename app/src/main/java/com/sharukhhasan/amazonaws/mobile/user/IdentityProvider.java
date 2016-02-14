package com.sharukhhasan.amazonaws.mobile.user;

/**
 * Created by Sharukh on 2/13/16.
 */
public interface IdentityProvider
{
    String getDisplayName();

    String getCognitoLoginKey();

    boolean isUserSignedIn();

    String getToken();

    String refreshToken();

    void signOut();

    String getUserName();

    String getUserImageUrl();

    void reloadUserInfo();
}

package com.sharukhhasan.amazonaws.mobile.user.signIn;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.util.Base64;
import android.util.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Sharukh on 2/13/16.
 */
public class Utils
{
    private static final String LOG_TAG_KEY_HASH = "KeyHash";

    public static void logKeyHash(Context context) {

        try {
            Log.d(LOG_TAG_KEY_HASH, "PackageName: " + context.getPackageName());
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);

            for(Signature signature : info.signatures)
            {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d(LOG_TAG_KEY_HASH, "SHA1 B64: " + Base64.encodeToString(md.digest(), Base64.DEFAULT));
                StringBuilder sb = new StringBuilder();
                for(int b : md.digest())
                {
                    b &= 0xFF;
                    sb.append((b < 16) ? ("0" + Integer.toHexString(b)) : Integer.toHexString(b));
                    sb.append(":");
                }
                sb.setLength(sb.length()-1);
                Log.d(LOG_TAG_KEY_HASH, "SHA1 Hex: " + sb.toString().toUpperCase());
            }
        } catch (final PackageManager.NameNotFoundException e) {
            Log.wtf(LOG_TAG_KEY_HASH, "Can't find our own package name with package manager?!", e);
        } catch (final NoSuchAlgorithmException e) {
            Log.wtf(LOG_TAG_KEY_HASH, "System doesn't comprehend SHA message digest?!", e);
        }
    }
}

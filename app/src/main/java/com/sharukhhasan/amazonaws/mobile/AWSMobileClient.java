package com.sharukhhasan.amazonaws.mobile;

import android.content.Context;
import android.util.Log;

import com.amazonaws.ClientConfiguration;
import com.sharukhhasan.amazonaws.mobile.user.IdentityManager;

import com.amazonaws.regions.Regions;

/**
 * Created by Sharukh on 2/13/16.
 */
public class AWSMobileClient
{
    private final static String LOG_TAG = AWSMobileClient.class.getSimpleName();

    private static AWSMobileClient instance;

    private final Context context;

    private ClientConfiguration clientConfiguration;
    private IdentityManager identityManager;

    public static class Builder
    {
        private Context applicationContext;
        private String  cognitoIdentityPoolID;
        private Regions cognitoRegion;
        private ClientConfiguration clientConfiguration;
        private IdentityManager identityManager;

        public Builder(final Context context)
        {
            this.applicationContext = context.getApplicationContext();
        };

        public Builder withCognitoIdentityPoolID(final String cognitoIdentityPoolID)
        {
            this.cognitoIdentityPoolID = cognitoIdentityPoolID;
            return this;
        };

        public Builder withCognitoRegion(final Regions cognitoRegion)
        {
            this.cognitoRegion = cognitoRegion;
            return this;
        }

        public Builder withIdentityManager(final IdentityManager identityManager)
        {
            this.identityManager = identityManager;
            return this;
        }

        public Builder withClientConfiguration(final ClientConfiguration clientConfiguration)
        {
            this.clientConfiguration = clientConfiguration;
            return this;
        }

        public AWSMobileClient build()
        {
            return new AWSMobileClient(applicationContext, cognitoIdentityPoolID, cognitoRegion, identityManager, clientConfiguration);
        }
    }

    private AWSMobileClient(final Context context, final String  cognitoIdentityPoolID, final Regions cognitoRegion, final IdentityManager identityManager, final ClientConfiguration clientConfiguration)
    {
        this.context = context;
        this.identityManager = identityManager;
        this.clientConfiguration = clientConfiguration;

    }

    public static void setDefaultMobileClient(AWSMobileClient client)
    {
        instance = client;
    }

    public static AWSMobileClient defaultMobileClient()
    {
        return instance;
    }

    public IdentityManager getIdentityManager()
    {
        return this.identityManager;
    }

    public static void initializeMobileClientIfNecessary(final Context context)
    {
        if(AWSMobileClient.defaultMobileClient() == null)
        {
            Log.d(LOG_TAG, "Initializing AWS Mobile Client...");
            final ClientConfiguration clientConfiguration = new ClientConfiguration();
            clientConfiguration.setUserAgent(AWSConfiguration.AWS_MOBILEHUB_USER_AGENT);
            final IdentityManager identityManager = new IdentityManager(context, clientConfiguration);
            final AWSMobileClient awsClient = new AWSMobileClient.Builder(context)
                            .withCognitoRegion(AWSConfiguration.AMAZON_COGNITO_REGION)
                            .withCognitoIdentityPoolID(AWSConfiguration.AMAZON_COGNITO_IDENTITY_POOL_ID)
                            .withIdentityManager(identityManager)
                            .withClientConfiguration(clientConfiguration)
                            .build();

            AWSMobileClient.setDefaultMobileClient(awsClient);
        }
        Log.d(LOG_TAG, "AWS Mobile Client is OK");
    }
}

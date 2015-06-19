package com.tweetco.clients;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.microsoft.windowsazure.mobileservices.ApiJsonOperationCallback;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponse;
import com.tweetco.Exceptions.OrgNotSignedUpException;
import com.tweetco.TweetCo;
import com.tweetco.activities.ApiInfo;
import com.tweetco.dao.Tweet;
import com.tweetco.dao.TweetUser;

import java.net.MalformedURLException;

/**
 * Created by kirankumar on 19/06/15.
 */
public class AutoDiscoverClient {

    public class AutoDiscoverResult
    {
        public TweetUser tweetUser;
        public String serverUrl;
        public String authToken;
        public Exception exception;
    }

    public static final String TAG = "AutoDiscoverClient";
    public static final String DEFAULT_APP_KEY = "PImqNtOVaoZFzGrQDAcrXwQnpLuZCf69";
    public static final String DEFAULT_APP_URL = "https://tweetcotest.azure-mobile.net/";

    public AutoDiscoverResult discoverUser(String emailAddress) throws MalformedURLException, Exception
    {
        final AutoDiscoverResult result = discoverServer(emailAddress);

        if(result.exception == null && result.tweetUser == null) {
            MobileServiceClient client = new MobileServiceClient(result.serverUrl, result.authToken, TweetCo.mContext);
            JsonObject element = new JsonObject();
            //TODO Check if the input is email address
            element.addProperty(ApiInfo.kEmail, emailAddress);
            client.invokeApi(ApiInfo.USER_EXISTS, element,new ApiJsonOperationCallback()
            {

                @Override
                public void onCompleted(JsonElement user, Exception exception,
                                        ServiceFilterResponse arg2) {
                    if(exception != null)
                    {
                        Log.e(TAG, "Get identitiy failed");
                        exception.printStackTrace();
                        result.exception = exception;
                    }
                    else
                    {
                        Log.d(TAG, "Get identitiy success");

                        Gson gson = new Gson();

                        try
                        {
                            if(user.isJsonArray())
                            {
                                TweetUser[] tweetUsers = gson.fromJson(user, TweetUser[].class);
                                if(tweetUsers.length > 0)
                                {
                                    result.tweetUser = tweetUsers[0];
                                }
                            }
                            else
                            {
                                result.exception = new Exception("Failed to get userinfo");
                            }
                        }
                        catch(Exception e)
                        {
                            Log.e(TAG, "Failed to get user information: " + e.getMessage());
                            e.printStackTrace();
                            result.exception = new Exception("Failed to get userinfo");
                        }

                    }
                }
            }, true);

            return  result;
        }
        else {
            return result;
        }
    }

    private AutoDiscoverResult discoverServer(String emailAddress) throws MalformedURLException, Exception
    {
        final AutoDiscoverResult discoverResult = new AutoDiscoverResult();
        discoverResult.serverUrl = DEFAULT_APP_URL;
        discoverResult.authToken = DEFAULT_APP_KEY;

        MobileServiceClient client = new MobileServiceClient(discoverResult.serverUrl, discoverResult.authToken, TweetCo.mContext);
        JsonObject element = new JsonObject();
        //TODO Check if the input is email address
        element.addProperty(ApiInfo.kEmail, emailAddress);
        client.invokeApi(ApiInfo.USER_EXISTS, element,new ApiJsonOperationCallback()
        {

            @Override
            public void onCompleted(JsonElement user, Exception exception,
                                    ServiceFilterResponse arg2) {
                if(exception != null)
                {
                    Log.e(TAG, "Get identitiy failed");
                    exception.printStackTrace();
                    discoverResult.exception = exception;
                }
                else
                {
                    Log.d(TAG, "Get identitiy success");

                    Gson gson = new Gson();

                    try
                    {
                        if(user.isJsonArray())
                        {
                            TweetUser[] tweetUsers = gson.fromJson(user, TweetUser[].class);
                            if(tweetUsers.length > 0)
                            {
                                discoverResult.tweetUser = tweetUsers[0];
                            }
                        }
                        else
                        {
                            //This is auto-discovery for the user
                            JsonObject autoDiscoveryObj = user.getAsJsonObject();
                            JsonElement mobileServiceUrl = autoDiscoveryObj.get("serviceurl");
                            JsonElement mobileServiceKey = autoDiscoveryObj.get("appkey");

                            if(mobileServiceKey == null || mobileServiceUrl == null)
                            {
                                discoverResult.exception = new OrgNotSignedUpException();

                            }
                            else
                            {
                                discoverResult.serverUrl = mobileServiceUrl.getAsString();
                                discoverResult.authToken = mobileServiceKey.getAsString();
                            }
                        }
                    }
                    catch(Exception e)
                    {
                        Log.e(TAG, "Failed to get user information: "+e.getMessage());
                        e.printStackTrace();
                        discoverResult.exception = new Exception("Failed to get userinfo");
                    }

                }
            }
        }, true);

        return  discoverResult;
    }

}

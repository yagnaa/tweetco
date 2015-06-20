package com.tweetco.clients;

import android.util.Log;
import android.util.Pair;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.microsoft.windowsazure.mobileservices.ApiJsonOperationCallback;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponse;
import com.tweetco.activities.ApiInfo;
import com.tweetco.dao.LeaderboardUser;
import com.tweetco.datastore.AccountSingleton;

import org.apache.http.client.methods.HttpGet;

import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by kirankumar on 20/06/15.
 */
public class LeaderboardListClient {

    public List<LeaderboardUser> getLeaderboardList() throws MalformedURLException
    {
        final List<LeaderboardUser> list = new ArrayList<LeaderboardUser>();
        MobileServiceClient client = AccountSingleton.INSTANCE.getMobileServiceClient();

        client.invokeApi(ApiInfo.LEADERBOARD, "GET", new ArrayList<Pair<String, String>>(), new ApiJsonOperationCallback()
        {
            @Override
            public void onCompleted(JsonElement arg0, Exception arg1, ServiceFilterResponse arg2)
            {
                if(arg1 == null)
                {
                    try
                    {
                        Gson gson = new Gson();
                        Type collectionType = new TypeToken<List<LeaderboardUser>>(){}.getType();
                        List<LeaderboardUser> tempList = gson.fromJson(arg0, collectionType);
                        list.addAll(tempList);
                    }
                    catch(JsonSyntaxException exception)
                    {
                        exception.printStackTrace();
                        Log.e("TweetUserRunnable", "unable to parse tweetUser") ;
                    }

                }
                else
                {
                    Log.e("Item clicked","Exception fetching tweets received") ;
                }

            }
        }, true);

        return list;
    }

}

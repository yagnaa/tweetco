package com.tweetco.clients;

import android.util.Log;
import android.util.Pair;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.microsoft.windowsazure.mobileservices.ApiJsonOperationCallback;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponse;
import com.tweetco.TweetCo;
import com.tweetco.activities.ApiInfo;
import com.tweetco.dao.TweetUser;
import com.tweetco.datastore.AccountSingleton;
import com.tweetco.tweets.TweetCommonData;

import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by kirankumar on 19/06/15.
 */
public class UsersListClient {

    public List<TweetUser> getUsersListFromServer(final String serverAddress, final String username, final String authToken) throws MalformedURLException {
        final List<TweetUser> usersList = new ArrayList<TweetUser>();
        try {
            MobileServiceClient client = new MobileServiceClient(serverAddress, authToken, TweetCo.mContext);

            client.invokeApi(ApiInfo.GET_USERS, "GET",  new ArrayList<Pair<String, String>>(), new ApiJsonOperationCallback()
            {
                @Override
                public void onCompleted(JsonElement arg0, Exception arg1,
                                        ServiceFilterResponse arg2) {

                    if(arg1 == null)
                    {
                        Gson gson = new Gson();

                        Type collectionType = new TypeToken<List<TweetUser>>(){}.getType();
                        List<TweetUser> users = gson.fromJson(arg0, collectionType);
                        usersList.addAll(users);

                        Log.e("tag", "msg");
                        //TODO need to remove this
                        for (TweetUser tweetUser : users)
                        {
                            TweetCommonData.tweetUsers.put(tweetUser.username.toLowerCase(), tweetUser);

                        }
                    }
                    else
                    {
                        Log.e("Item clicked","Exception while loading users list") ;
                        arg1.printStackTrace();
                    }


                }
            }, true);

            return  usersList;

        }
        catch (MalformedURLException ex)
        {
            throw ex;
        }

    }

    public void followUser(String username, IFollowUnfollowStatus statusCallback) throws Exception {
        followOrUnfollowUser(username, true, statusCallback);
    }

    public void unfollowUser(String username, IFollowUnfollowStatus statusCallback) throws Exception {
        followOrUnfollowUser(username, false, statusCallback);
    }

    public interface IFollowUnfollowStatus {
        void success(String username);
        void failed(String username);
    }

    private void followOrUnfollowUser(final String username, boolean follow, final IFollowUnfollowStatus statusCallback) throws MalformedURLException, Exception
    {
        MobileServiceClient client = AccountSingleton.INSTANCE.getMobileServiceClient();
        JsonObject obj = new JsonObject();
        obj.addProperty(ApiInfo.kApiRequesterKey, AccountSingleton.INSTANCE.getUserName());
        if(follow)
        {
            obj.addProperty(ApiInfo.kUserToFollowKey, username);
        }
        else
        {
            obj.addProperty(ApiInfo.kUserToUnFollowKey, username);
        }

        client.invokeApi(follow ? ApiInfo.FOLLOW_USER: ApiInfo.UN_FOLLOW_USER, obj, new ApiJsonOperationCallback()
        {
            @Override
            public void onCompleted(JsonElement arg0, Exception arg1, ServiceFilterResponse arg2)
            {
                if(arg1 == null)
                {
                    Log.d("Item clicked", "Follow/Unfollow succeeded") ;
                    statusCallback.success(username);

                }
                else
                {
                    Log.d("Item clicked", "Follow/Unfollow failed") ;
                    arg1.printStackTrace();
                    statusCallback.failed(username);
                }

            }
        },true);
    }
}

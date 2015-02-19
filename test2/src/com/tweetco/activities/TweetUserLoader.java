package com.tweetco.activities;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.imagedisplay.util.AsyncTask;
import com.microsoft.windowsazure.mobileservices.ApiJsonOperationCallback;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponse;
import com.tweetco.dao.Tweet;
import com.tweetco.dao.TweetUser;
import com.tweetco.tweets.TweetCommonData;


public  class TweetUserLoader
{

	public TweetUserLoader()
	{
	}

	public void load(List<Tweet> tweetsList,Map<String, TweetUser> users)
	{
		List<String> usersToLoad = new ArrayList<String>();

		for(Tweet tweet:tweetsList)
		{
			if(!users.containsKey(tweet.sourceuser))
			{
				usersToLoad.add(tweet.sourceuser);
			}
		}

		for(String user:usersToLoad)
		{
			TweetUserAsyncTask task = new TweetUserAsyncTask(user);
			
			task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		}
	}


	public void load()
	{
		List<String> usersToLoad = new ArrayList<String>();

		Collection<Tweet> tweetsList = TweetCommonData.homeFeedTweets.values();

		Map<String,TweetUser> tweetUsersList = TweetCommonData.tweetUsers;

		for(Tweet tweet:tweetsList)
		{
			if(!tweetUsersList.containsKey(tweet.tweetowner) && !usersToLoad.contains(tweet.tweetowner))
			{
				usersToLoad.add(tweet.tweetowner);
			}
		}

		for(String user:usersToLoad)
		{
			if(!TextUtils.isEmpty(user))
			{
				TweetUserAsyncTask task = new TweetUserAsyncTask(user);
				
				task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			}
		}	
	}

	public static class TweetUserAsyncTask extends AsyncTask<Void, Void, TweetUser>
	{
		MobileServiceClient mClient = TweetCommonData.mClient;
		String mUserForWhomDataIsBeingLoaded = null;

		public TweetUserAsyncTask(String userName)
		{
			mUserForWhomDataIsBeingLoaded = userName;
		}

		@Override
		protected TweetUser doInBackground(Void... params) {

			JsonObject obj = new JsonObject();
			obj.addProperty(ApiInfo.kApiRequesterKey, mUserForWhomDataIsBeingLoaded);
			mClient.invokeApi(ApiInfo.GET_USER_INFO, obj, new ApiJsonOperationCallback() {

				@Override
				public void onCompleted(JsonElement arg0, Exception arg1,
						ServiceFilterResponse arg2) {
					if(arg1 == null)
					{
						Gson gson = new Gson();

						try
						{
							TweetUser[] tweetUser = gson.fromJson(arg0, TweetUser[].class);
							if(tweetUser.length > 0)
							{
								// Clear all the data points
								TweetCommonData.tweetUsers.put(mUserForWhomDataIsBeingLoaded.toLowerCase(Locale.US), tweetUser[0]);
							}
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
			},true);
			
			return null;	
		}

	}
}

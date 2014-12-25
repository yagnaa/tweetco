package com.yagnasri.displayingbitmaps.ui;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.microsoft.windowsazure.mobileservices.ApiJsonOperationCallback;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponse;
import com.tweetco.tweets.TweetCommonData;
import com.yagnasri.dao.TweetUser;
import com.yagnasri.displayingbitmaps.util.AsyncTask;


public  class TweetUserLoader
{
	TweetAdapter adapter = null;
	String mUsername = null;

	public TweetUserLoader(TweetAdapter adapter, String username)
	{
		this.adapter = adapter;
		mUsername = username;
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
			TweetUserAsyncTask task = new TweetUserAsyncTask(user,adapter);
			
			task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		}
	}


	public void load()
	{
		List<String> usersToLoad = new ArrayList<String>();

		List<Tweet> tweetsList = TweetCommonData.tweetsList;

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
				TweetUserAsyncTask task = new TweetUserAsyncTask(user,adapter);
				
				task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			}
		}
		
		if(usersToLoad!=null && !usersToLoad.isEmpty())
		{
			adapter.notifyDataSetChanged();
		}
				
	}

	public static class TweetUserAsyncTask extends AsyncTask<Void, Void, TweetUser>
	{
		MobileServiceClient mClient = AllInOneActivity.mClient;
		String mUserForWhomDataIsBeingLoaded = null;
		TweetAdapter adapter = null;

		public TweetUserAsyncTask(String userName,TweetAdapter adapter)
		{
			mUserForWhomDataIsBeingLoaded = userName;
			this.adapter = adapter;
		}

		@Override
		protected TweetUser doInBackground(Void... params) {

			JsonObject obj = new JsonObject();
			obj.addProperty("user", mUserForWhomDataIsBeingLoaded);
			mClient.invokeApi("getuserinfo", obj, new ApiJsonOperationCallback() {

				@Override
				public void onCompleted(JsonElement arg0, Exception arg1,
						ServiceFilterResponse arg2) {
					if(arg1 == null)
					{
						Gson gson = new Gson();

						try
						{
							TweetUser[] tweetUser = gson.fromJson(arg0, TweetUser[].class);
							adapter.addUser(mUserForWhomDataIsBeingLoaded,tweetUser[0]);
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
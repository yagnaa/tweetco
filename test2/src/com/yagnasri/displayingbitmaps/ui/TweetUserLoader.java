package com.yagnasri.displayingbitmaps.ui;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.microsoft.windowsazure.mobileservices.ApiJsonOperationCallback;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponse;
import com.yagnasri.dao.TweetUser;
import com.yagnasri.displayingbitmaps.util.AsyncTask;



public  class TweetUserLoader
{
	TweetAdapter adapter = null;

	public TweetUserLoader(TweetAdapter adapter)
	{
		this.adapter = adapter;
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

		List<Tweet> tweetsList = adapter.getTweetList();

		Map<String,TweetUser> tweetUsersList = adapter.getTweetUsersList();

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
	}

	public static class TweetUserAsyncTask extends AsyncTask<Void, Void, TweetUser>
	{
		MobileServiceClient mClient = AllInOneActivity.mClient;
		String mUserName = null;
		TweetAdapter adapter = null;

		public TweetUserAsyncTask(String userName,TweetAdapter adapter)
		{
			mUserName = userName;
			this.adapter = adapter;
		}

		@Override
		protected TweetUser doInBackground(Void... params) {

			JsonObject obj = new JsonObject();
			obj.addProperty("user", mUserName);
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
							adapter.addUser(mUserName,tweetUser[0]);
							
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
		
		
		@Override
	    protected void onPostExecute(TweetUser result) 
		{
			if(adapter!=null)
			{
				adapter.notifyDataSetChanged();
			}
	    }

	}
}
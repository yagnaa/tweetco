package com.tweetco.activities;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.microsoft.windowsazure.mobileservices.ApiJsonOperationCallback;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponse;
import com.tweetco.TweetCo;
import com.tweetco.activities.TweetAdapter.NewPageLoader;
import com.tweetco.dao.Tweet;
import com.tweetco.dao.TweetUser;
import com.tweetco.tweetlist.TweetListMode;
import com.tweetco.tweets.TweetCommonData;


public class PageLoader extends NewPageLoader
{
	public final class Constants {

	    // Defines a custom Intent action
	    public static final String BROADCAST_ACTION =
	        "com.tweetco.activities.BROADCAST";

	    // Defines the key for the status "extra" in an Intent
	    public static final String EXTENDED_DATA_STATUS =
	        "com.tweetco.activities.STATUS";

	}
	
	private static final String TAG = "PageLoader";
	public static final int TWEET_LOAD_BUFFER = 10; //When the user is slowly scrolling throught he tweets, if the backing adapter has
	//fewer than TWEET_LOAD_BUFFER tweets to show, we start loading the next batch
	private static final int SEVER_SIDE_BATCH_SIZE = 10;
	private MobileServiceClient mClient;
	private TweetUserLoader tweetUserLoader; //Loads user data

	private AtomicBoolean isRunning = new AtomicBoolean(false);

	private TweetListMode mTweetListMode = null;

	public static interface OnLoadCompletedCallback
	{
		public void onLoadCompleted(int numOfTweetsLoaded, boolean endOfList);
	}

	public PageLoader(TweetListMode tweetListMode)
	{
		mTweetListMode = tweetListMode;
		mClient = TweetCommonData.mClient;
		tweetUserLoader = new TweetUserLoader();
	}

	@Override
	public void loadNext(final OnLoadCompletedCallback callback ) 
	{
		Log.d(TAG, "Trying to load the next set of tweets");


		if(isRunning.compareAndSet(false, true))
		{

			final JsonObject tweetRequest = mTweetListMode.getNextTweetRequest();
			mClient.invokeApi(mTweetListMode.getApi() , tweetRequest, new ApiJsonOperationCallback() 
			{
				@Override
				public void onCompleted(JsonElement response, Exception arg1, ServiceFilterResponse arg2) 
				{
					isRunning.compareAndSet(true, false);
					if(arg1 == null)
					{
						//The received data contains an inner join of tweets and tweet users. 
						//Read them both.
						Gson gson = new Gson();

						Type collectionType = new TypeToken<List<Tweet>>(){}.getType();
						List<Tweet> list = gson.fromJson(response, collectionType);

						Type tweetusertype = new TypeToken<List<TweetUser>>(){}.getType();
						List<TweetUser> tweetUserlist = gson.fromJson(response, tweetusertype);

						mTweetListMode.processReceivedTweets(list,tweetUserlist,response,tweetRequest);

						tweetUserLoader.load();


						boolean endOflist = false;
						if (list.size() < SEVER_SIDE_BATCH_SIZE) 
						{
							endOflist = true;
						} else {
							endOflist = false;
						}

						if(callback!=null)
						{
							callback.onLoadCompleted(list.size(), endOflist);
						}

					}
					else
					{
						Log.e(TAG,"Exception fetching tweets received") ;
					}

				}
			},false);
		}


	}

	@Override
	public void loadTop(final OnLoadCompletedCallback callback ) 
	{
		Log.d(TAG, "Trying to load the top tweets");


		if(isRunning.compareAndSet(false, true))
		{
			final JsonObject tweetRequest = mTweetListMode.getPreviousTweetRequest();
			mClient.invokeApi(mTweetListMode.getApi() , tweetRequest, new ApiJsonOperationCallback() 
			{
				@Override
				public void onCompleted(JsonElement response, Exception arg1, ServiceFilterResponse arg2) 
				{
					isRunning.compareAndSet(true, false);
					if(arg1 == null)
					{

						//The received data contains an inner join of tweets and tweet users. 
						//Read them both.
						Gson gson = new Gson();

						Type collectionType = new TypeToken<List<Tweet>>(){}.getType();
						List<Tweet> list = gson.fromJson(response, collectionType);

						Type tweetusertype = new TypeToken<List<TweetUser>>(){}.getType();
						List<TweetUser> tweetUserlist = gson.fromJson(response, tweetusertype);


						mTweetListMode.processReceivedTweets(list,tweetUserlist,response,tweetRequest);

						tweetUserLoader.load();


						boolean endOflist = false;
						if (list.size() < SEVER_SIDE_BATCH_SIZE) 
						{
							endOflist = true;
						} else {
							endOflist = false;
						}

						if(callback!=null)
						{
							callback.onLoadCompleted(list.size(), endOflist);
						}
						else
						{
							Intent localIntent =
						            new Intent(Constants.BROADCAST_ACTION).putExtra(com.tweetco.activities.Constants.TWEETMODE_UPDATED, mTweetListMode);
						    // Broadcasts the Intent to receivers in this app.
						    LocalBroadcastManager.getInstance(TweetCo.mContext).sendBroadcast(localIntent);
						}

					}
					else
					{
						Log.e(TAG,"Exception fetching tweets received") ;
					}

				}
			},false);
		}

	}	
}

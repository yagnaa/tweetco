package com.tweetco.tweetlist;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.tweetco.tweets.TweetCommonData;
import com.yagnasri.dao.Tweet;
import com.yagnasri.dao.TweetUser;
import com.yagnasri.displayingbitmaps.ui.ApiInfo;

public class UserFeedMode extends TweetListMode implements Parcelable
{
	private String mUserName;

	public UserFeedMode(String username)
	{
		mUserName = username;
	}

	@Override
	public JsonObject getNextTweetRequest() 
	{
		JsonObject obj = new JsonObject();
		obj.addProperty(ApiInfo.kRequestingUserKey, mUserName);
		obj.addProperty(ApiInfo.kFeedTypeKey, ApiInfo.kUserFeedTypeValue);
		obj.addProperty(ApiInfo.kLastTweetIterator, getLastTweetIterator());
		return obj;
	}

	@Override
	public JsonObject getPreviousTweetRequest() 
	{
		// TODO Auto-generated method stub
		throw new IllegalArgumentException("GetPreviousTweetRequest not supported");
	}



	public int getLastTweetIterator()
	{
		int retValue =0;
		List<Tweet> userTweets = TweetCommonData.userTweetsList.get(mUserName);
		if(userTweets!=null && !userTweets.isEmpty())
		{
			Tweet tweet = userTweets.get(userTweets.size() - 1);
			retValue = tweet.iterator;
		}
		return retValue;
	}

	@Override
	public int processReceivedTweets(List<Tweet> list,List<TweetUser> tweetUserlist , JsonElement response,JsonObject tweetRequest,int index ) 
	{

		addEntriesToBottom(list);

		for(TweetUser user:tweetUserlist)
		{
			if(!TextUtils.isEmpty(user.username))
			{
				TweetCommonData.tweetUsers.put(user.username.toLowerCase(), user);
			}
		}
		return index;

	}

	/**
	 * In this mode the tweets will be added to the bottom
	 * @param entries
	 */
	public void addEntriesToBottom(List<Tweet> entries) 
	{
		// Add entries to the bottom of the list
		List<Tweet> userTweetList = TweetCommonData.userTweetsList.get(mUserName);
		if(userTweetList==null)
		{
			userTweetList = new ArrayList<Tweet>();
			TweetCommonData.userTweetsList.put(mUserName, userTweetList);
		}
		userTweetList.addAll(entries);
	}


	public void addEntriesToTop(List<Tweet> entries) 
	{
		// Add entries to the bottom of the list
		List<Tweet> userTweetList = TweetCommonData.userTweetsList.get(mUserName);
		if(userTweetList==null)
		{
			userTweetList = new ArrayList<Tweet>();
			TweetCommonData.userTweetsList.put(mUserName, userTweetList);
		}
		userTweetList.addAll(0,entries);
	}

	public void clearEntries() 
	{
		List<Tweet> userTweetList = TweetCommonData.userTweetsList.get(mUserName);
		if(userTweetList!=null)
		{
			userTweetList.clear();
		}
	}

	public void addUsers(Map<String,TweetUser> tweetUsers) 
	{
		// Clear all the data points
		TweetCommonData.tweetUsers.putAll(tweetUsers);
	}

	public void addUser(String user,TweetUser userInfo) 
	{
		// Clear all the data points
		TweetCommonData.tweetUsers.put(user.toLowerCase(), userInfo);
	}

	protected UserFeedMode(Parcel in) {
		mUserName = in.readString();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(mUserName);
	}

	@SuppressWarnings("unused")
	public static final Parcelable.Creator<HomeFeedMode> CREATOR = new Parcelable.Creator<HomeFeedMode>() {
		@Override
		public HomeFeedMode createFromParcel(Parcel in) {
			return new HomeFeedMode(in);
		}

		@Override
		public HomeFeedMode[] newArray(int size) {
			return new HomeFeedMode[size];
		}
	};

	@Override
	public int getCount() 
	{
		// TODO Auto-generated method stub
		int retValue = 0;

		// Add entries to the bottom of the list
		List<Tweet> userTweetList = TweetCommonData.userTweetsList.get(mUserName);
		if(userTweetList!=null)
		{
			retValue = userTweetList.size();
		}
		return retValue;
	}
	
	@Override
	public Object removeItem(int position)
	{
		// TODO Auto-generated method stub
		Tweet tweet = null;

		// Add entries to the bottom of the list
		List<Tweet> userTweetList = TweetCommonData.userTweetsList.get(mUserName);
		if(userTweetList!=null)
		{
			tweet = userTweetList.remove(position);
		}
		return tweet;
	}

	@Override
	public Object getItem(int position) 
	{

		// TODO Auto-generated method stub
		Tweet tweet = null;

		// Add entries to the bottom of the list
		List<Tweet> userTweetList = TweetCommonData.userTweetsList.get(mUserName);
		if(userTweetList!=null)
		{
			tweet = userTweetList.get(position);
		}
		return tweet;
	}

	@Override
	public long getItemId(int position) 
	{
		return position<0?0:position;
	}

	@Override
	public String getApi()
	{
		return ApiInfo.GET_TWEETS_FOR_USER;
	}
}
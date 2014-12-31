package com.tweetco.tweetlist;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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

public class TrendingFeedMode extends TweetListMode implements Parcelable
{
	//All the tweets that we are currently holding in memory
    private static List<Tweet> trendingTweetList = Collections.synchronizedList(new ArrayList<Tweet>());
	private String mTag;
	
	public TrendingFeedMode(String tag)
	{
		mTag = tag;
	}	

	@Override
	public JsonObject getNextTweetRequest() 
	{
		JsonObject obj = new JsonObject();
		obj.addProperty(ApiInfo.kTrendingTopicKey, mTag);
		obj.addProperty(ApiInfo.kLastTweetIterator, getLastTweetIterator());
		return obj;
	}
	
	@Override
	public JsonObject getPreviousTweetRequest() 
	{
		throw new IllegalAccessError("getPreviousTweetRequest is not implemented for TrendingFeedMode");
	}
	
	
	
	public int getLastTweetIterator()
	{
		int retValue =0;
		int size = trendingTweetList.size();
		if(size>0)
		{
			Tweet tweet = trendingTweetList.get(size - 1);
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
				TweetCommonData.tweetUsers.put(user.username, user);
			}
		}
		
		return index;
		
	}
	

	public void addEntriesToTop(List<Tweet> entries) 
	{
		// Add entries in reversed order to achieve a sequence used in most of messaging/chat apps
		if (entries != null) {
			Collections.reverse(entries);
		}

		// Add entries to the top of the list
		trendingTweetList.addAll(0, entries);
	}

	public void addEntriesToBottom(List<Tweet> entries) 
	{
		trendingTweetList.addAll(entries);
	}

	public void clearEntries() 
	{

		trendingTweetList.clear();
	}

	public void addUsers(Map<String,TweetUser> tweetUsers) 
	{
		TweetCommonData.tweetUsers.putAll(tweetUsers);
	}

	public void addUser(String user,TweetUser userInfo) 
	{
		TweetCommonData.tweetUsers.put(user, userInfo);
	}
	
	protected TrendingFeedMode(Parcel in) {
		mTag = in.readString();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(mTag);
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
		return trendingTweetList.size();
	}
	
	@Override
	public Object removeItem(int position)
	{
		return trendingTweetList.remove(position);
	}

	@Override
	public Object getItem(int position) 
	{
		return trendingTweetList.get(position);
	}

	@Override
	public long getItemId(int position) 
	{
		// TODO Auto-generated method stub
		return position<0?0:position;
	}
	
	
	@Override
	public String getApi()
	{
		return ApiInfo.GET_TWEETS_FOR_TREND;
	}
}
package com.tweetco.tweetlist;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.map.LinkedMap;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tweetco.activities.ApiInfo;
import com.tweetco.dao.Tweet;
import com.tweetco.dao.TweetUser;
import com.tweetco.tweets.TweetCommonData;

public class TrendingFeedMode extends TweetListMode implements Parcelable
{    
	private LinkedMap<Integer,Tweet> trendingTweetList = new LinkedMap<Integer, Tweet>();
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
		JsonObject obj = new JsonObject();
		obj.addProperty(ApiInfo.kTrendingTopicKey, mTag);
		obj.addProperty(ApiInfo.kLastTweetIterator, getFirstTweetIterator());
		obj.addProperty(ApiInfo.kTweetRequestTypeKey, ApiInfo.kNewTweetRequest);
		return obj;
	}

	public int getFirstTweetIterator()
	{
		int retValue =0;
		if(trendingTweetList.size()>0)
		{
			Integer firstKey = trendingTweetList.firstKey();
			if(firstKey!=null)
			{
				Tweet tweet = trendingTweetList.get(firstKey);
				retValue = tweet.iterator;
			}
		}
		return retValue;
	}

	public int getLastTweetIterator()
	{
		int retValue =0;
		if(trendingTweetList.size()>0)
		{
			Integer lastKey = trendingTweetList.lastKey();
			if(lastKey!=null)
			{
				Tweet tweet = trendingTweetList.get(lastKey);
				retValue = tweet.iterator;
			}
		}
		return retValue;
	}

	@Override
	public void processReceivedTweets(List<Tweet> list,List<TweetUser> tweetUserlist , JsonElement response,JsonObject tweetRequest) 
	{		
		//populate set
		boolean requestWasForOldTweets = true;
		
		JsonElement elem = tweetRequest.get(ApiInfo.kTweetRequestTypeKey);
		if(elem!=null)
		{
			if(elem.getAsString().equalsIgnoreCase(ApiInfo.kNewTweetRequest))
			{
				 requestWasForOldTweets = false;
			}
		}


        if(requestWasForOldTweets)
        {
    		addEntriesToBottom(list);
        }
        else
        {
        	addEntriesToTop(list);
        }


		for(TweetUser user:tweetUserlist)
		{
			if(!TextUtils.isEmpty(user.username))
			{
				TweetCommonData.tweetUsers.put(user.username.toLowerCase(), user);
			}
		}

	}


	public void addEntriesToTop(List<Tweet> entries) 
	{		
		if(!entries.isEmpty())
		{
			LinkedMap<Integer,Tweet> tweetList = trendingTweetList.clone();//(tweet.iterator, tweet);
			trendingTweetList.clear();
			// Add entries to the bottom of the list
			for(Tweet tweet:entries)
			{		
				trendingTweetList.put(tweet.iterator, tweet);
			}
			trendingTweetList.putAll(tweetList);
		}
	}

	public void addEntriesToBottom(List<Tweet> entries) 
	{		
		// Add entries to the bottom of the list
		for(Tweet tweet:entries)
		{
			trendingTweetList.put(tweet.iterator, tweet);
		}
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
		TweetCommonData.tweetUsers.put(user.toLowerCase(), userInfo);
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
		return trendingTweetList.get(trendingTweetList.get(position));
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

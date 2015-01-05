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

public class HomeFeedMode extends TweetListMode implements Parcelable
{
	private String mUserName;
	
	public HomeFeedMode(String username)
	{
		mUserName = username;
	}

	@Override
	public JsonObject getNextTweetRequest() 
	{
		// TODO Auto-generated method stub
		JsonObject obj = new JsonObject();
		obj.addProperty(ApiInfo.kRequestingUserKey, TweetCommonData.getUserName());
		obj.addProperty(ApiInfo.kFeedTypeKey, ApiInfo.kHomeFeedTypeValue);
		obj.addProperty(ApiInfo.kLastTweetIterator, getLastTweetIterator());
		obj.addProperty(ApiInfo.kTweetRequestTypeKey, ApiInfo.kOldTweetRequest);
		return obj;
	}

	@Override
	public JsonObject getPreviousTweetRequest() 
	{
		// TODO Auto-generated method stub
		JsonObject obj = new JsonObject();
		obj.addProperty(ApiInfo.kRequestingUserKey, TweetCommonData.getUserName());
		obj.addProperty(ApiInfo.kFeedTypeKey, ApiInfo.kHomeFeedTypeValue);
		obj.addProperty(ApiInfo.kLastTweetIterator, getFirstTweetIterator());
		obj.addProperty(ApiInfo.kTweetRequestTypeKey, ApiInfo.kNewTweetRequest);
		return obj;
	}



	public int getLastTweetIterator()
	{
		int retValue =0;
		if(TweetCommonData.homeFeedTweets.size()>0)
		{
			Integer lastKey = TweetCommonData.homeFeedTweets.lastKey();
			if(lastKey!=null)
			{
				Tweet tweet = TweetCommonData.homeFeedTweets.get(lastKey);
				retValue = tweet.iterator;
			}
		}
		return retValue;
	}
	
	public int getFirstTweetIterator()
	{
		int retValue =0;
		if(TweetCommonData.homeFeedTweets.size()>0)
		{
			Integer firstKey = TweetCommonData.homeFeedTweets.firstKey();
			if(firstKey!=null)
			{
				Tweet tweet = TweetCommonData.homeFeedTweets.get(firstKey);
				retValue = tweet.iterator;
			}
		}
		return retValue;
	}

	@Override
	public int processReceivedTweets(List<Tweet> list,List<TweetUser> tweetUserlist , JsonElement response,JsonObject tweetRequest,int index ) 
	{
		int returnIndex = index;
		//populate set
		boolean requestWasForOldTweets = false;
		
		JsonElement elem = tweetRequest.get(ApiInfo.kTweetRequestTypeKey);
		if(elem!=null)
		{
			requestWasForOldTweets = elem.getAsString().equalsIgnoreCase(ApiInfo.kOldTweetRequest);
		}


        if(requestWasForOldTweets)
        {
    		addEntriesToBottom(list);
        }
        else
        {
        	returnIndex += list.size();
        	addEntriesToTop(list);
        }


		for(TweetUser user:tweetUserlist)
		{
			if(!TextUtils.isEmpty(user.username))
			{
				TweetCommonData.tweetUsers.put(user.username.toLowerCase(), user);
			}
		}
		
		return returnIndex;
	}

	/**
	 * In this mode the tweets will be added to the bottom
	 * @param entries
	 */
	public void addEntriesToBottom(List<Tweet> entries) 
	{		
		// Add entries to the bottom of the list
		for(Tweet tweet:entries)
		{
			TweetCommonData.homeFeedTweets.put(tweet.iterator, tweet);
		}
	}
	

	public void addEntriesToTop(List<Tweet> entries) 
	{
		
		if(entries!=null && !entries.isEmpty())
		{
			LinkedMap<Integer,Tweet> tweetList = TweetCommonData.homeFeedTweets.clone();//(tweet.iterator, tweet);
			TweetCommonData.homeFeedTweets.clear();
			// Add entries to the bottom of the list
			for(Tweet tweet:entries)
			{		
				TweetCommonData.homeFeedTweets.put(tweet.iterator, tweet);
			}
			TweetCommonData.homeFeedTweets.putAll(tweetList);
		}
	}


	public void clearEntries() 
	{
		TweetCommonData.homeFeedTweets.clear();
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
	
	@Override
	public int getCount() 
	{	
		return TweetCommonData.homeFeedTweets.size();
	}
	
	@Override
	public Object removeItem(int position)
	{
		return TweetCommonData.homeFeedTweets.remove(TweetCommonData.homeFeedTweets.get(position));
	}
	
	@Override
	public Object getItem(int position) 
	{
		return TweetCommonData.homeFeedTweets.get(TweetCommonData.homeFeedTweets.get(position));
	}


	protected HomeFeedMode(Parcel in) {
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
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position<0?0:position;
	}
	
	@Override
	public String getApi()
	{
		return ApiInfo.GET_TWEETS_FOR_USER;
	}
	
}
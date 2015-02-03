package com.tweetco.tweetlist;

import java.util.List;

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

public class TweetRepliesFeedMode extends TweetListMode implements Parcelable
{
	private String mTweetSourceIterator;
	private LinkedMap<Integer,Tweet> replyTweetList = new LinkedMap<Integer, Tweet>();
	
	public TweetRepliesFeedMode(String tweetSourceIterator)
	{
		mTweetSourceIterator = tweetSourceIterator;
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) 
	{
		dest.writeString(mTweetSourceIterator);
	}

	@Override
	public JsonObject getPreviousTweetRequest() 
	{
		JsonObject obj = new JsonObject();
		obj.addProperty(ApiInfo.kSourceIteratorKey, mTweetSourceIterator);
		return obj;
	}

	@Override
	public JsonObject getNextTweetRequest() 
	{
		JsonObject obj = new JsonObject();
		obj.addProperty(ApiInfo.kSourceIteratorKey, mTweetSourceIterator);
		return obj;
	}

	@Override
	public void processReceivedTweets(List<Tweet> list,
			List<TweetUser> tweetUserlist, JsonElement response,
			JsonObject tweetRequest) {
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
			LinkedMap<Integer,Tweet> tweetList = replyTweetList.clone();//(tweet.iterator, tweet);
			replyTweetList.clear();
			// Add entries to the bottom of the list
			for(Tweet tweet:entries)
			{		
				replyTweetList.put(tweet.iterator, tweet);
			}
			replyTweetList.putAll(tweetList);
		}
	}

	public void addEntriesToBottom(List<Tweet> entries) 
	{		
		// Add entries to the bottom of the list
		for(Tweet tweet:entries)
		{
			replyTweetList.put(tweet.iterator, tweet);
		}
	}

	@Override
	public int getCount() 
	{
		return replyTweetList.size();
	}

	@Override
	public Object removeItem(int position)
	{
		return replyTweetList.remove(position);
	}

	@Override
	public Object getItem(int position) 
	{
		return replyTweetList.get(replyTweetList.get(position));
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
		return ApiInfo.GET_REPLY_TWEETS_FOR_TWEET;
	}
	
	protected TweetRepliesFeedMode(Parcel in) 
	{
		mTweetSourceIterator = in.readString();
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

}

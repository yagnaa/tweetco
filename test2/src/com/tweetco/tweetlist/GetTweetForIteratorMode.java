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

public class GetTweetForIteratorMode extends TweetListMode implements Parcelable
{

	private String mTweetSourceIterator;
	private LinkedMap<Integer,Tweet> sourceTweetList = new LinkedMap<Integer, Tweet>();
	
	public GetTweetForIteratorMode(String tweetSourceIterator)
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
		obj.addProperty(ApiInfo.kIteratorKey, mTweetSourceIterator);
		return obj;
	}

	@Override
	public JsonObject getNextTweetRequest() 
	{
		JsonObject obj = new JsonObject();
		obj.addProperty(ApiInfo.kIteratorKey, mTweetSourceIterator);
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
			LinkedMap<Integer,Tweet> tweetList = sourceTweetList.clone();//(tweet.iterator, tweet);
			sourceTweetList.clear();
			// Add entries to the bottom of the list
			for(Tweet tweet:entries)
			{		
				sourceTweetList.put(tweet.iterator, tweet);
			}
			sourceTweetList.putAll(tweetList);
		}
	}

	public void addEntriesToBottom(List<Tweet> entries) 
	{		
		// Add entries to the bottom of the list
		for(Tweet tweet:entries)
		{
			sourceTweetList.put(tweet.iterator, tweet);
		}
	}

	@Override
	public int getCount() 
	{
		return sourceTweetList.size();
	}

	@Override
	public Object removeItem(int position)
	{
		return sourceTweetList.remove(position);
	}

	@Override
	public Object getItem(int position) 
	{
		return sourceTweetList.get(sourceTweetList.get(position));
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
		return ApiInfo.GET_TWEET_FOR_ITERATOR;
	}
	
	protected GetTweetForIteratorMode(Parcel in) 
	{
		mTweetSourceIterator = in.readString();
	}
	

	@SuppressWarnings("unused")
	public static final Parcelable.Creator<GetTweetForIteratorMode> CREATOR = new Parcelable.Creator<GetTweetForIteratorMode>() {
		@Override
		public GetTweetForIteratorMode createFromParcel(Parcel in) {
			return new GetTweetForIteratorMode(in);
		}

		@Override
		public GetTweetForIteratorMode[] newArray(int size) {
			return new GetTweetForIteratorMode[size];
		}
	};

}

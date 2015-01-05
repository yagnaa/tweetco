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

public class BookmarksFeedMode extends TweetListMode implements Parcelable
{
	private String mUserName;
	
    private LinkedMap<Integer,Tweet> bookmarkedTweetList = new LinkedMap<Integer, Tweet>();
	
	public BookmarksFeedMode(String username)
	{
		mUserName = username;
	}

	@Override
	public JsonObject getNextTweetRequest() 
	{
		// TODO Auto-generated method stub
		JsonObject obj = new JsonObject();
		obj.addProperty(ApiInfo.kRequestingUserKey, TweetCommonData.getUserName());
		return obj;
	}

	@Override
	public JsonObject getPreviousTweetRequest() 
	{
		// TODO Auto-generated method stub
		JsonObject obj = new JsonObject();
		obj.addProperty(ApiInfo.kRequestingUserKey, TweetCommonData.getUserName());
		return obj;
	}



	public int getLastTweetIterator()
	{
		int retValue =0;
		if(bookmarkedTweetList.size()>0)
		{
			Integer lastKey = bookmarkedTweetList.lastKey();
			if(lastKey!=null)
			{
				Tweet tweet = bookmarkedTweetList.get(lastKey);
				retValue = tweet.iterator;
			}
		}
		return retValue;
	}

	@Override
	public int processReceivedTweets(List<Tweet> list,List<TweetUser> tweetUserlist ,JsonElement response,JsonObject tweetRequest,int index ) 
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
		for(Tweet tweet:entries)
		{
			bookmarkedTweetList.put(tweet.iterator, tweet);
		}
	}
	

	public void addEntriesToTop(List<Tweet> entries) 
	{
		if(!entries.isEmpty())
		{
			LinkedMap<Integer,Tweet> tweetList = bookmarkedTweetList.clone();//(tweet.iterator, tweet);
			bookmarkedTweetList.clear();
			// Add entries to the bottom of the list
			for(Tweet tweet:entries)
			{		
				bookmarkedTweetList.put(tweet.iterator, tweet);
			}
			bookmarkedTweetList.putAll(tweetList);
		}
	}


	public void clearEntries() 
	{
		bookmarkedTweetList.clear();
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
		return bookmarkedTweetList.size();
	}
	
	@Override
	public Object removeItem(int position)
	{
		return bookmarkedTweetList.remove(bookmarkedTweetList.get(position));
	}
	
	@Override
	public Object getItem(int position) 
	{
		return bookmarkedTweetList.get(bookmarkedTweetList.get(position));
	}


	protected BookmarksFeedMode(Parcel in) 
	{
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
	public static final Parcelable.Creator<BookmarksFeedMode> CREATOR = new Parcelable.Creator<BookmarksFeedMode>() {
		@Override
		public BookmarksFeedMode createFromParcel(Parcel in) {
			return new BookmarksFeedMode(in);
		}

		@Override
		public BookmarksFeedMode[] newArray(int size) {
			return new BookmarksFeedMode[size];
		}
	};

	@Override
	public long getItemId(int position) 
	{
		return position<0?0:position;
	}
	
	@Override
	public String getApi() {
		return ApiInfo.GET_BOOKMARKED_TWEETS;
	}
}
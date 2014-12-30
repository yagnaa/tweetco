package com.tweetco.tweetlist;

import java.lang.reflect.Type;
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

public class BookmarksFeedMode extends TweetListMode implements Parcelable
{
	private String mUserName;
	
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
		int size = TweetCommonData.tweetsList.size();
		if(size>0)
		{
			Tweet tweet = TweetCommonData.tweetsList.get(size - 1);
			retValue = tweet.iterator;
		}
		return retValue;
	}

	@Override
	public int processReceivedTweets(JsonElement response,JsonObject tweetRequest,int index ) 
	{


		//The teceived data contains an inner join of tweets and tweet users. 
		//Read them both.
		Gson gson = new Gson();

		Type collectionType = new TypeToken<List<Tweet>>(){}.getType();
		List<Tweet> list = gson.fromJson(response, collectionType);

		Type tweetusertype = new TypeToken<List<TweetUser>>(){}.getType();
		List<TweetUser> tweetUserlist = gson.fromJson(response, tweetusertype);

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

	/**
	 * In this mode the tweets will be added to the bottom
	 * @param entries
	 */
	public void addEntriesToBottom(List<Tweet> entries) 
	{
		// Add entries to the bottom of the list
		TweetCommonData.tweetsList.addAll(entries);
	}
	

	public void addEntriesToTop(List<Tweet> entries) 
	{
		// Add entries in reversed order to achieve a sequence used in most of messaging/chat apps
		if (entries != null) {
			Collections.reverse(entries);
		}

		// Add entries to the top of the list
		TweetCommonData.tweetsList.addAll(0, entries);
	}


	public void clearEntries() 
	{

		TweetCommonData.tweetsList.clear();
	}

	public void addUsers(Map<String,TweetUser> tweetUsers) 
	{
		// Clear all the data points
		TweetCommonData.tweetUsers.putAll(tweetUsers);
	}

	public void addUser(String user,TweetUser userInfo) 
	{
		// Clear all the data points
		TweetCommonData.tweetUsers.put(user, userInfo);
	}
	
	@Override
	public int getCount() 
	{	
		return TweetCommonData.tweetsList.size();
	}
	
	@Override
	public Object getItem(int position) 
	{
		return TweetCommonData.tweetsList.get(position);
	}


	protected BookmarksFeedMode(Parcel in) {
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
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public String getApi() {
		return ApiInfo.GET_BOOKMARKED_TWEETS;
	}
}
package com.tweetco.tweetlist;

import java.util.List;

import android.os.Parcelable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tweetco.dao.Tweet;
import com.tweetco.dao.TweetUser;

public abstract class TweetListMode implements Parcelable
{
	
	public abstract JsonObject getPreviousTweetRequest();
	
	public abstract JsonObject getNextTweetRequest();
	
	public abstract void processReceivedTweets(List<Tweet> list,List<TweetUser> tweetUserlist ,JsonElement response,JsonObject tweetRequest);
	
	public abstract int getCount();
	
	public abstract Object removeItem(int position);
	
	public abstract Object getItem(int position);
	
	public abstract long getItemId(int position);
	
	public abstract String getApi();
}

package com.tweetco.tweetlist;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public abstract class TweetListMode 
{
	
	public abstract JsonObject getPreviousTweetRequest();
	
	public abstract JsonObject getNextTweetRequest();
	
	public abstract int processReceivedTweets(JsonElement response,JsonObject tweetRequest,int index);
	
	public abstract int getCount();
	
	public abstract Object getItem(int position);
	
	public abstract long getItemId(int position);
	
	public abstract String getApi();
}
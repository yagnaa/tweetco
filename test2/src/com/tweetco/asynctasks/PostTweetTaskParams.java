package com.tweetco.asynctasks;

import com.microsoft.windowsazure.mobileservices.MobileServiceClient;

import android.graphics.drawable.BitmapDrawable;

public class PostTweetTaskParams 
{
	private String mUsername;
	private String mTweetContent;
	private BitmapDrawable mTweetImage;
	private String mContentTags;
	
	private MobileServiceClient mClient;
	
	public PostTweetTaskParams(MobileServiceClient client, String username) 
	{
		mClient = client;
		mUsername = username;
	}
	
	public MobileServiceClient getClient()
	{
		return mClient;
	}

	public String getUsername() {
		return mUsername;
	}


	public String getTweetContent() {
		return mTweetContent;
	}

	public void setTweetContent(String tweetContent) {
		this.mTweetContent = tweetContent;
	}

	public BitmapDrawable getTweetImage() {
		return mTweetImage;
	}

	public void setTweetImage(BitmapDrawable tweetImage) {
		this.mTweetImage = tweetImage;
	}

	public String getContentTags() {
		return mContentTags;
	}

	public void setContentTags(String mContentTags) {
		this.mContentTags = mContentTags;
	}
}

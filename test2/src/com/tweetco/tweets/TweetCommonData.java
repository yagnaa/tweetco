package com.tweetco.tweets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections4.map.LinkedMap;

import android.text.TextUtils;

import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.tweetco.database.dao.Account;
import com.yagnasri.dao.Tweet;
import com.yagnasri.dao.TweetUser;
import com.yagnasri.displayingbitmaps.util.ImageFetcher;

public class TweetCommonData 
{
	//Use only one of these through out
	public static MobileServiceClient mClient;
	
	//All the tweets that we are currently holding in memory    
    public static LinkedMap<Integer,Tweet> homeFeedTweets = new LinkedMap<Integer, Tweet>();

    public static Map<String,List<Tweet>> userTweetsList = new ConcurrentHashMap<String, List<Tweet>>();
    
    public static Map<String,TweetUser> tweetUsers = new ConcurrentHashMap<String, TweetUser>();
    
    public static ImageFetcher mImageFetcher;
    
	private static Account mAccount;

	public static Account getAccount() 
	{
		return mAccount;
	}
	
	public static String getUserName() 
	{
		if(mAccount == null || mAccount.getUsername() == null || TextUtils.isEmpty(mAccount.getUsername()))
		{
			throw new IllegalArgumentException("Account was not initialized");
		}
		return mAccount.getUsername();
	}

	public static void setAccount(Account account) 
	{
		mAccount = account;
	}
}

package com.tweetco.tweets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections4.map.LinkedMap;

import android.text.TextUtils;

import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.tweetco.activities.TrendingFragment;
import com.tweetco.activities.TweetUtils;
import com.tweetco.dao.Tweet;
import com.tweetco.dao.TweetUser;
import com.tweetco.database.dao.Account;

public class TweetCommonData 
{
	//Use only one of these through out
	public static MobileServiceClient mClient;
	
	//All the tweets that we are currently holding in memory    
    public static LinkedMap<Integer,Tweet> homeFeedTweets = new LinkedMap<Integer, Tweet>();

    public static Map<String,List<Tweet>> userTweetsList = new ConcurrentHashMap<String, List<Tweet>>();
    
    public static Map<String,TweetUser> tweetUsers = new ConcurrentHashMap<String, TweetUser>();
    
    public static List<TrendingFragment.TrendingTag> trendingTagLists = Collections.synchronizedList(new ArrayList<TrendingFragment.TrendingTag>());
    
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
	
	public static void bookmark(Tweet tweet, String userName)
	{
		if(tweet!=null && !TextUtils.isEmpty(userName))
		{
			if(!TweetUtils.isStringPresent(tweet.bookmarkers, userName))
			{
				tweet.bookmarkers = tweet.bookmarkers +  userName + ";";
			}
		}
	}
	
	public static void like(Tweet tweet, String userName)
	{
		if(tweet!=null && !TextUtils.isEmpty(userName))
		{
			if(!TweetUtils.isStringPresent(tweet.upvoters, userName))
			{
				tweet.upvoters = tweet.upvoters +  userName + ";" ;
			}
		}
	}
}

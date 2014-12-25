package com.tweetco.tweets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.text.TextUtils;

import com.tweetco.TweetCo;
import com.tweetco.database.dao.Account;
import com.yagnasri.dao.TweetUser;
import com.yagnasri.displayingbitmaps.ui.Tweet;
import com.yagnasri.displayingbitmaps.util.ImageFetcher;

public class TweetCommonData 
{
	//All the tweets that we are currently holding in memory
    public static List<Tweet> tweetsList = Collections.synchronizedList(new ArrayList<Tweet>());
    
  //  public static Map<String, List<Tweet>> tweetsMap = new ConcurrentHashMap<String, List<Tweet>>();
    
 
    
    //All the tweets that we are currently holding in memory
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

package com.tweetco.tweets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.yagnasri.dao.TweetUser;
import com.yagnasri.displayingbitmaps.ui.Tweet;
import com.yagnasri.displayingbitmaps.util.ImageFetcher;

public class TweetCommonData 
{
	//All the tweets that we are currently holding in memory
    public static List<Tweet> tweetsList = Collections.synchronizedList(new ArrayList<Tweet>());
    
    //All the tweets that we are currently holding in memory
    public static Map<String,TweetUser> tweetUsers = new ConcurrentHashMap<String, TweetUser>();
    
    public static ImageFetcher mImageFetcher;
}
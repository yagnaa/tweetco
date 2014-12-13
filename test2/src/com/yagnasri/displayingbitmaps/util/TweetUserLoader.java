
package com.yagnasri.displayingbitmaps.util;

import java.util.ArrayList;
import java.util.List;

import com.yagnasri.displayingbitmaps.ui.tweetusers;

/**
 * This class loads the user info (current and tweeters info). This is designed on the lines of ImageFetcher.
 * Currently it doesn't use any cache. In future a disk cache can be implemented.
 * @author yagnasri
 *
 */
public class TweetUserLoader
{
	public List<tweetusers> usersList = new ArrayList<tweetusers>();
}
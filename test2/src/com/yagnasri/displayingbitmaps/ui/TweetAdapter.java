package com.yagnasri.displayingbitmaps.ui;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.microsoft.windowsazure.mobileservices.ApiJsonOperationCallback;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponse;
import com.tweetco.R;
import com.tweetco.tweets.TweetCommonData;
import com.yagnasri.dao.Tweet;
import com.yagnasri.dao.TweetUser;
import com.yagnasri.displayingbitmaps.util.ImageFetcher;



/**
 * The main adapter that backs the GridView. This is fairly standard except the number of
 * columns in the GridView is used to create a fake top row of empty views as we use a
 * transparent ActionBar and don't want the real top row of images to start off covered by it.
 */
public class TweetAdapter extends BaseAdapter implements OnScrollListener 
{
	public static final String TAG = "TweetAdapter";
	
	public static final int TWEET_LOAD_BUFFER = 10; //When the user is slowly scrolling throught he tweets, if the backing adapter has
	                             //fewer than TWEET_LOAD_BUFFER tweets to show, we start loading the next batch

	// A demo listener to pass actions from view to adapter
	public static abstract class NewPageLoader {
		public abstract void onScrollNext();
	}

	public interface OnProfilePicClick
	{
		void onItemClick(int position);
	}

	private final Context mContext;
	private int mItemHeight = 0;
	private int mActionBarHeight = 0;
	private ImageFetcher mImageFetcher; //Fetches the images
	private String mUserName; // This is the user for which this adapter is associated
	private String mTrendTag = null;

	private NewPageLoader mNewPageLoader; //Fetches the tweets

	private TweetUserLoader tweetUserLoader; //Loads user data

	private OnProfilePicClick mOnProfilePicClickCallback;

	protected InfiniteScrollListPageListener mInfiniteListPageListener; 

	// A lock to prevent another scrolling event to be triggered if one is already in session
	protected boolean canScroll = false;
	// A flag to enable/disable row clicks
	protected boolean rowEnabled = true;


	private int oldCount = 0;

	public void lock() {
		canScroll = false;
	}	
	public void unlock() {
		canScroll = true;
	}
	
	public static enum TweetListMode
	{
		HOME_FEED,
		USER_FEED,
		TRENDING_FEED
	}
	
	private TweetListMode mTweetListMode = null; 

	public TweetAdapter(Context context, String username, ImageFetcher imageFetcher, TweetListMode mode, String trendTag, OnProfilePicClick onProfilePicClickCallback) 
	{
		super();
		mContext = context;
		mImageFetcher = imageFetcher;
		mUserName = username;
		mTrendTag = trendTag;
		
		mTweetListMode = mode;

		mOnProfilePicClickCallback = onProfilePicClickCallback;
		// Calculate ActionBar height
		TypedValue tv = new TypedValue();
		if (context.getTheme().resolveAttribute(
				android.R.attr.actionBarSize, tv, true)) {
			mActionBarHeight = TypedValue.complexToDimensionPixelSize(
					tv.data, context.getResources().getDisplayMetrics());
		}
		mNewPageLoader = new PageLoader(context, this);
		tweetUserLoader = new TweetUserLoader(this, mUserName);

		TweetCommonData.tweetsList.clear();
	}

	public void addEntriesToTop(List<Tweet> entries) {
		// Add entries in reversed order to achieve a sequence used in most of messaging/chat apps
		if (entries != null) {
			Collections.reverse(entries);
		}
		oldCount = TweetCommonData.tweetsList.size();
		// Add entries to the top of the list
		TweetCommonData.tweetsList.addAll(0, entries);
		notifyDataSetChanged();
	}

	public void addEntriesToBottom(List<Tweet> entries) {
		// Add entries to the bottom of the list

		oldCount = TweetCommonData.tweetsList.size();
		TweetCommonData.tweetsList.addAll(entries);
		notifyDataSetChanged();
	}

	public void clearEntries() {

		oldCount = TweetCommonData.tweetsList.size();
		TweetCommonData.tweetsList.clear();
		notifyDataSetChanged();
	}

	public void addUsers(Map<String,TweetUser> tweetUsers) 
	{
		// Clear all the data points
		TweetCommonData.tweetUsers.putAll(tweetUsers);
		notifyDataSetChanged();
	}

	public void addUser(String user,TweetUser userInfo) 
	{
		// Clear all the data points
		TweetCommonData.tweetUsers.put(user, userInfo);
	}

	@Override
	public int getCount() 
	{
		// If columns have yet to be determined, return no items
		//        if (getNumColumns() == 0) {
		//            return 0;
		//        }

		// Size + number of columns for top empty row
		return TweetCommonData.tweetsList.size();
	}

	@Override
	public Object getItem(int position) 
	{
		return TweetCommonData.tweetsList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position < 0 ? 0 : position;
	}

	@Override
	public int getViewTypeCount() 
	{
		return 1;
	}

	@Override
	public int getItemViewType(int position) {
		return 1;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup container) 
	{
		// Now handle the main ImageView thumbnails
		ImageView imageView;
		if (convertView == null) { // if it's not recycled, instantiate and initialize

			LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = layoutInflater.inflate(R.layout.tweet, container, false);


			imageView = (ImageView)convertView.findViewById(R.id.imageView1);
			imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

		} else { // Otherwise re-use the converted view
			imageView = (ImageView)convertView.findViewById(R.id.imageView1);
		}

		imageView.setTag(position);
		imageView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				int position = Integer.parseInt(v.getTag().toString());
				mOnProfilePicClickCallback.onItemClick(position);
			}
		});






		//Load TextFields here
		final Tweet tweet = (Tweet) getItem(position);
		TweetUser tweeter = null;
		if (tweet != null) {
			tweeter = (TweetUser) TweetCommonData.tweetUsers.get(tweet.tweetowner);
			TextView handle = (TextView) convertView.findViewById(R.id.handle);
			TextView userName = (TextView) convertView.findViewById(R.id.username);
			TextView tweetContent = (TextView) convertView.findViewById(R.id.tweetcontent);

			handle.setText(tweet.tweetowner);
			if(tweeter!=null && !TextUtils.isEmpty(tweeter.displayname))
			{
				userName.setText(tweeter.displayname);
			}
			else
			{
				userName.setText(tweet.tweetowner);
			}
			tweetContent.setText(tweet.tweetcontent);	
			Linkify.addLinks(tweetContent, Linkify.WEB_URLS);
			tweetContent.setMovementMethod(new LinkMovementMethod());


			//UpVote ImageView
			ImageView upvoteView = (ImageView) convertView.findViewById(R.id.upvote);
			upvoteView.setTag(position);
			setUpVoteFlag(upvoteView,TweetCommonData.getUserName());
			upvoteView.setOnClickListener(new OnClickListener() 
			{	
				@Override
				public void onClick(View v) 
				{
					Integer position = (Integer)v.getTag();
					//Show user profile view
					Tweet linkedTweet = (Tweet)getItem(position);
					if(linkedTweet != null)
					{
						String owner = linkedTweet.tweetowner;
						if(!TextUtils.isEmpty(owner))
						{
							upVote(v,TweetCommonData.getUserName(), linkedTweet.iterator, owner);
						}
					}
				}
			});

			ImageView bookmarkView = (ImageView) convertView.findViewById(R.id.bookmark);
			bookmarkView.setTag(position);
			setBookMarkFlag(bookmarkView,TweetCommonData.getUserName());
			bookmarkView.setOnClickListener(new OnClickListener() 
			{	
				@Override
				public void onClick(View v) 
				{
					Integer position = (Integer)v.getTag();
					//Show user profile view
					Tweet linkedTweet = (Tweet)getItem(position);
					if(linkedTweet != null)
					{
						String owner = linkedTweet.tweetowner;
						if(!TextUtils.isEmpty(owner))
						{
							upVote(v,TweetCommonData.getUserName(), linkedTweet.iterator, owner);
						}
					}
				}
			});

		}

		//        // Check the height matches our calculated column width
		//        if (imageView.getLayoutParams().height != mItemHeight) {
		//            imageView.setLayoutParams(mImageViewLayoutParams);
		//        }

		// Finally load the image asynchronously into the ImageView, this also takes care of
		// setting a placeholder image while the background thread runs
		if(tweeter!=null)
		{
			mImageFetcher.loadImage(tweeter.profileimageurl, imageView);
		}




		return convertView;
		//END_INCLUDE(load_gridview_item)
	}

	public void refreshAdapter()
	{
		this.notifyDataSetChanged();
	}

	private void setUpVoteFlag(ImageView imageView,String userName)
	{
		Integer position = (Integer)imageView.getTag();
		//Show user profile view
		Tweet linkedTweet = (Tweet)getItem(position);
		if(linkedTweet != null)
		{
			boolean isCurrentUserUpVoted  = TweetUtils.isStringPresent(linkedTweet.upvoters, userName);
			imageView.setPressed(isCurrentUserUpVoted);
		}
	}

	private void setBookMarkFlag(ImageView imageView,String userName)
	{
		Integer position = (Integer)imageView.getTag();
		//Show user profile view
		Tweet linkedTweet = (Tweet)getItem(position);
		if(linkedTweet != null)
		{
			boolean didCurrentUserBookmark = TweetUtils.isStringPresent(linkedTweet.bookmarkers, userName);
			imageView.setPressed(didCurrentUserBookmark);
		}

	}

	/**
	 * Sets the item height. Useful for when we know the column width so the height can be set
	 * to match.
	 *
	 * @param height
	 */
	public void setItemHeight(int height) {
		//        if (height == mItemHeight) {
		//            return;
		//        }
		//        mItemHeight = height;
		//        mImageViewLayoutParams =
		//                new GridView.LayoutParams(LayoutParams.MATCH_PARENT, mItemHeight);
		//        mImageFetcher.setImageSize(height);
		notifyDataSetChanged();
	}

	//This notifies that the list has ended
	public void notifyEndOfList() {
		// When there is no more to load use the lock to prevent loading from happening
		lock();
		// More actions when there is no more to load
		if (mInfiniteListPageListener != null) {
			mInfiniteListPageListener.endOfList();
		}
	}

	//This notifies that there are more tweets to be loaded
	public void notifyHasMore() {
		// Release the lock when there might be more to load
		unlock();
		// More actions when it might have more to load
		if (mInfiniteListPageListener != null) {
			mInfiniteListPageListener.hasMore();
		}
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
	
	public TweetListMode getTweetListMode()
	{
		return mTweetListMode;
	}


	@Override
	public void notifyDataSetChanged() {

		int count = getCount();
		// TODO Auto-generated method stub
		super.notifyDataSetChanged();
		if((oldCount==0 && count!=0) || (oldCount!=0 && count==0))
		{

		}
	}
	@Override
	public void notifyDataSetInvalidated() {
		// TODO Auto-generated method stub
		super.notifyDataSetInvalidated();
	}

	/**
	 * This function decides when to load the next set of tweets.
	 * @param view
	 * @param firstVisibleItem
	 * @param visibleItemCount
	 * @param totalItemCount
	 */
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {


		// In scroll-to-bottom-to-load mode, when the sum of first visible position and visible count equals the total number
		// of items in the adapter it reaches the bottom
		int bufferItemsToShow = getCount() -firstVisibleItem + visibleItemCount;
		Log.d(TAG, "There are "+bufferItemsToShow+" items to show in the adapter.");
		if (bufferItemsToShow < TWEET_LOAD_BUFFER  && canScroll) {
			onScrollNext();
		}

	}


	public void onScrollNext() {
		if (mNewPageLoader != null) {
			mNewPageLoader.onScrollNext();
		}
	}

	public void setInfiniteListPageListener(InfiniteScrollListPageListener infiniteListPageListener) {
		this.mInfiniteListPageListener = infiniteListPageListener;
	}
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// TODO Auto-generated method stub

	}



	public void upVote(final View upvoteView,final String requestingUser,int iterator,String tweetOwner)
	{
		MobileServiceClient mClient = TweetCommonData.mClient;
		JsonObject obj = new JsonObject();
		obj.addProperty(ApiInfo.kRequestingUserKey, requestingUser);
		obj.addProperty(ApiInfo.kIteratorKey, iterator);
		obj.addProperty(ApiInfo.kTweetOwner, tweetOwner);
		mClient.invokeApi(ApiInfo.UPVOTE, obj, new ApiJsonOperationCallback() {

			@Override
			public void onCompleted(JsonElement arg0, Exception arg1,
					ServiceFilterResponse arg2) {
				if(arg1 == null)
				{
					//TODO This has to be optimized on the server side.
					refreshAdapter();
				}
				else
				{
					Log.e(TAG,"Exception upVoting a tweet") ;
					arg1.printStackTrace();
				}

			}
		},false);
	}

	public void bookmark(final View bookmarkView,final String requestingUser,int iterator,String tweetOwner)
	{
		MobileServiceClient mClient = TweetCommonData.mClient;
		JsonObject obj = new JsonObject();
		obj.addProperty(ApiInfo.kRequestingUserKey, requestingUser);
		obj.addProperty(ApiInfo.kIteratorKey, iterator);
		obj.addProperty(ApiInfo.kTweetOwner, tweetOwner);
		mClient.invokeApi(ApiInfo.BOOKMARK, obj, new ApiJsonOperationCallback() 
		{

			@Override
			public void onCompleted(JsonElement arg0, Exception arg1,
					ServiceFilterResponse arg2) 
			{
				if(arg1 == null)
				{
					if(bookmarkView!=null && (bookmarkView instanceof ImageView))
					{
						setBookMarkFlag( (ImageView)bookmarkView, requestingUser);
					}
				}
				else
				{
					Log.e(TAG,"Exception bookmarking a tweet") ;
					arg1.printStackTrace();
				}

			}
		},false);
	}

	public void refresh()
	{
		mNewPageLoader.onScrollNext();
	}

	//TODO this has to be moved to a separate class
	private class PageLoader extends NewPageLoader
	{
		private static final int SEVER_SIDE_BATCH_SIZE = 10;
		Context mContext = null;
		private MobileServiceClient mClient;

		public PageLoader(Context context,TweetAdapter tweetAdapter)
		{
			mContext = context;
			mClient = TweetCommonData.mClient;
		}

		@Override
		public void onScrollNext() {

			// Loading lock to allow only one instance of loading
			lock();
			
			JsonObject obj = new JsonObject();
			TweetListMode mode = getTweetListMode();
			String api = ApiInfo.GET_TWEETS_FOR_USER;
			if(mode == TweetListMode.HOME_FEED)
			{
				obj.addProperty(ApiInfo.kRequestingUserKey, mUserName);
				obj.addProperty(ApiInfo.kFeedTypeKey, ApiInfo.kHomeFeedTypeValue);
				obj.addProperty(ApiInfo.kLastTweetIterator, getLastTweetIterator());
			}
			else if(mode == TweetListMode.USER_FEED)
			{
				obj.addProperty(ApiInfo.kRequestingUserKey, mUserName);
				obj.addProperty(ApiInfo.kFeedTypeKey, ApiInfo.kUserFeedTypeValue);
				obj.addProperty(ApiInfo.kLastTweetIterator, getLastTweetIterator());
			}
			else if(mode == TweetListMode.TRENDING_FEED)
			{
				obj.addProperty(ApiInfo.kTrendingTopicKey, mTrendTag);
				obj.addProperty(ApiInfo.kLastTweetIterator, getLastTweetIterator());
				api = ApiInfo.GET_TWEETS_FOR_TREND;
			}
			
			
			mClient.invokeApi(api , obj, new ApiJsonOperationCallback() {

				@Override
				public void onCompleted(JsonElement arg0, Exception arg1,
						ServiceFilterResponse arg2) {
					if(arg1 == null)
					{
						//The teceived data contains an inner join of tweets and tweet users. 
						//Read them both.
						Gson gson = new Gson();

						Type collectionType = new TypeToken<List<Tweet>>(){}.getType();
						List<Tweet> list = gson.fromJson(arg0, collectionType);
						
						Type tweetusertype = new TypeToken<List<TweetUser>>(){}.getType();
						List<TweetUser> tweetUserlist = gson.fromJson(arg0, tweetusertype);
						
						List<Tweet> removeTweetsList = new ArrayList<Tweet>();
//						if(mbGetTweetsByUser)
//						{
//							for (Tweet tweet : list) 
//							{
//								if(!tweet.tweetowner.equalsIgnoreCase(mUsername))
//								{
//									removeTweetsList.add(tweet);
//								}
//							}
//						}
//
//						if(!removeTweetsList.isEmpty())
//						{
//							list.removeAll(removeTweetsList);
//						}

						addEntriesToBottom(list);
						
						for(TweetUser user:tweetUserlist)
						{
							if(!TextUtils.isEmpty(user.username))
							{
								TweetCommonData.tweetUsers.put(user.username, user);
							}
						}


						// Add or remove the loading view depend on if there might be more to load
						if (list.size() < SEVER_SIDE_BATCH_SIZE) {
							notifyEndOfList();
						} else {
							notifyHasMore();
						}

						tweetUserLoader.load();

					}
					else
					{
						Log.e(TAG,"Exception fetching tweets received") ;
					}

				}
			},false);
		}	
	}
}

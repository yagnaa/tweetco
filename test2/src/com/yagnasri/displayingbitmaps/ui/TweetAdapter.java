package com.yagnasri.displayingbitmaps.ui;

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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.microsoft.windowsazure.mobileservices.ApiJsonOperationCallback;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponse;
import com.tweetco.R;
import com.tweetco.tweetlist.TweetListMode;
import com.tweetco.tweets.TweetCommonData;
import com.yagnasri.dao.Tweet;
import com.yagnasri.dao.TweetUser;
import com.yagnasri.displayingbitmaps.util.ImageFetcher;
import com.yagnasri.displayingbitmaps.util.Utils;



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
		public abstract void load(JsonObject tweetRequest);
	}

	public interface OnProfilePicClick
	{
		void onItemClick(int position);
	}

	private final Context mContext;
	private int mItemHeight = 0;
	private int mActionBarHeight = 0;
	private ImageFetcher mImageFetcher; //Fetches the images

	private NewPageLoader mNewPageLoader; //Fetches the tweets

	private TweetUserLoader tweetUserLoader; //Loads user data

	private OnProfilePicClick mOnProfilePicClickCallback;

	protected InfiniteScrollListPageListener mInfiniteListPageListener; 

	// A lock to prevent another scrolling event to be triggered if one is already in session
	protected boolean canScroll = false;
	// A flag to enable/disable row clicks
	protected boolean rowEnabled = true;


	public void lock() {
		canScroll = false;
	}	
	public void unlock() {
		canScroll = true;
	}
	
	private TweetListMode mTweetListMode = null; 

	public TweetAdapter(Context context, ImageFetcher imageFetcher, TweetListMode mode, OnProfilePicClick onProfilePicClickCallback) 
	{
		super();
		mContext = context;
		mImageFetcher = imageFetcher;
		
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
		tweetUserLoader = new TweetUserLoader(this);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup container) 
	{
		Log.d(TAG, "getView called for position ="+position +" convertView="+(convertView!=null));
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

		// Finally load the image asynchronously into the ImageView, this also takes care of
		// setting a placeholder image while the background thread runs
		if(tweeter!=null)
		{
			loadProfileImage(tweeter,imageView);
		}




		return convertView;
		//END_INCLUDE(load_gridview_item)
	}
	

	@Override
	public int getCount() 
	{
		return mTweetListMode.getCount();
	}
	
	@Override
	public Object getItem(int position) 
	{
		return mTweetListMode.getItem(position);
	}
	
	@Override
	public long getItemId(int position) 
	{
		return mTweetListMode.getItemId(position);
	}
	
	private void loadProfileImage(TweetUser tweeter,ImageView imageView)
	{
		Log.d(TAG,"tweeter.profileimageurl="+tweeter.profileimageurl+ "   imageView="+imageView.toString());
		if(TextUtils.isEmpty(tweeter.profileimageurl))
		{
			String initials = Utils.getInitials(tweeter.displayname);
			mImageFetcher.loadImage(initials, imageView);
		}
		else
		{
			mImageFetcher.loadImage(tweeter.profileimageurl, imageView);
		}
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
	public void setItemHeight(int height) 
	{
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
	
	public TweetListMode getTweetListMode()
	{
		return mTweetListMode;
	}



	/**
	 * This function decides when to load the next set of tweets.
	 * @param view
	 * @param firstVisibleItem
	 * @param visibleItemCount
	 * @param totalItemCount
	 */
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) 
	{
		// In scroll-to-bottom-to-load mode, when the sum of first visible position and visible count equals the total number
		// of items in the adapter it reaches the bottom
		int bufferItemsToShow = getCount() -(firstVisibleItem + visibleItemCount);
		Log.d(TAG, "There are getCount()="+getCount()+" firstVisibleItem="+firstVisibleItem+ " visibleItemCount="+visibleItemCount);
		if (bufferItemsToShow < TWEET_LOAD_BUFFER  && canScroll) 
		{
			onScrollNext();
		}
	}
	
	
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) 
	{
		// TODO Auto-generated method stub

	}


	private void onScrollNext() 
	{
		if (mNewPageLoader != null) 
		{
			JsonObject tweetRequest = mTweetListMode.getNextTweetRequest();
			//TODO Build the request for the load
			mNewPageLoader.load(tweetRequest);
		}
	}

	public void setInfiniteListPageListener(InfiniteScrollListPageListener infiniteListPageListener) 
	{
		this.mInfiniteListPageListener = infiniteListPageListener;
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

	public void refreshTop()
	{
		//TODO Build the tweetRequest and give it to loader
		JsonObject tweetRequest = mTweetListMode.getPreviousTweetRequest();
		//TODO Build the request for the load
		mNewPageLoader.load(tweetRequest);
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
		public void load(final JsonObject tweetRequest ) 
		{

			// Loading lock to allow only one instance of loading
			lock();
			
//			JsonObject obj = new JsonObject();
//			TweetListMode mode = getTweetListMode();
//			if(mode == TweetListMode.HOME_FEED)
//			{
//				obj.addProperty(ApiInfo.kRequestingUserKey, mUserName);
//				obj.addProperty(ApiInfo.kFeedTypeKey, ApiInfo.kHomeFeedTypeValue);
//				obj.addProperty(ApiInfo.kLastTweetIterator, getLastTweetIterator());
//				obj.addProperty(ApiInfo.kTweetRequestTypeKey, ApiInfo.kOldTweetRequest);
//			}
//			else if(mode == TweetListMode.USER_FEED)
//			{
//				obj.addProperty(ApiInfo.kRequestingUserKey, mUserName);
//				obj.addProperty(ApiInfo.kFeedTypeKey, ApiInfo.kUserFeedTypeValue);
//				obj.addProperty(ApiInfo.kLastTweetIterator, getLastTweetIterator());
//			}
//			else if(mode == TweetListMode.TRENDING_FEED)
//			{
//				obj.addProperty(ApiInfo.kTrendingTopicKey, mTrendTag);
//				obj.addProperty(ApiInfo.kLastTweetIterator, getLastTweetIterator());
//				api = ApiInfo.GET_TWEETS_FOR_TREND;
//			}
			
			Log.d(TAG, "Trying to load the next set of tweets");
			
			mClient.invokeApi(mTweetListMode.getApi() , tweetRequest, new ApiJsonOperationCallback() {

				@Override
				public void onCompleted(JsonElement arg0, Exception arg1,
						ServiceFilterResponse arg2) {
					if(arg1 == null)
					{
						//The teceived data contains an inner join of tweets and tweet users. 
						//Read them both.
//						Gson gson = new Gson();
//
//						Type collectionType = new TypeToken<List<Tweet>>(){}.getType();
//						List<Tweet> list = gson.fromJson(arg0, collectionType);
//						
//						Type tweetusertype = new TypeToken<List<TweetUser>>(){}.getType();
//						List<TweetUser> tweetUserlist = gson.fromJson(arg0, tweetusertype);
//
//						addEntriesToBottom(list);
//						
//						for(TweetUser user:tweetUserlist)
//						{
//							if(!TextUtils.isEmpty(user.username))
//							{
//								TweetCommonData.tweetUsers.put(user.username, user);
//							}
//						}


						mTweetListMode.processReceivedTweets(arg0,tweetRequest);
						
						refreshAdapter();
						// Add or remove the loading view depend on if there might be more to load
						//TODO spinner at the bottom
//						if (list.size() < SEVER_SIDE_BATCH_SIZE) {
//							notifyEndOfList();
//						} else {
//							notifyHasMore();
//						}

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

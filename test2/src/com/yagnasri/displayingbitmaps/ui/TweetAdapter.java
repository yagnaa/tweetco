package com.yagnasri.displayingbitmaps.ui;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.support.v4.view.GestureDetectorCompat;
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
import com.yagnasri.displayingbitmaps.ui.TweetListFragment.MyGestureListener;
import com.yagnasri.displayingbitmaps.util.ImageCache;
import com.yagnasri.displayingbitmaps.util.ImageFetcher;
import com.yagnasri.displayingbitmaps.util.Utils;



/**
 * The main adapter that backs the GridView. This is fairly standard except the number of
 * columns in the GridView is used to create a fake top row of empty views as we use a
 * transparent ActionBar and don't want the real top row of images to start off covered by it.
 */
public class TweetAdapter extends BaseAdapter
{
	public static final String TAG = "TweetAdapter";


	// A demo listener to pass actions from view to adapter
	public static abstract class NewPageLoader {
		public abstract void load(JsonObject tweetRequest,String api);
	}

	public interface OnProfilePicClick
	{
		void onItemClick(int position);
	}

	private final Context mContext;
	private int mItemHeight = 0;
	private int mActionBarHeight = 0;
	private ImageFetcher mImageFetcher; //Fetches the images
	
	private ImageFetcher mImageFetcher2; //Fetches the images



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

	public TweetAdapter(Context context, ImageFetcher imageFetcher, ImageFetcher imageFetcher2, TweetListMode mode, OnProfilePicClick onProfilePicClickCallback) 
	{
		super();
		mContext = context;
		mImageFetcher = imageFetcher;
		mImageFetcher2 = imageFetcher2;
		
		mTweetListMode = mode;

		mOnProfilePicClickCallback = onProfilePicClickCallback;
		// Calculate ActionBar height
		TypedValue tv = new TypedValue();
		if (context.getTheme().resolveAttribute(
				android.R.attr.actionBarSize, tv, true)) {
			mActionBarHeight = TypedValue.complexToDimensionPixelSize(
					tv.data, context.getResources().getDisplayMetrics());
		}


	}

	@Override
	public void notifyDataSetChanged() 
	{
		Log.v(TAG, "notifyDataSetChanged()");
		super.notifyDataSetChanged();
	}
	@Override
	public void notifyDataSetInvalidated() 
	{
		Log.v(TAG, "notifyDataSetInvalidated()");
		super.notifyDataSetInvalidated();
	}
	@Override
	public View getView(int position, View convertView, ViewGroup container) 
	{
		Log.v(TAG, "getView called for position ="+position +" convertView="+(convertView!=null));
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

			ImageView tweetContentImage = (ImageView) convertView.findViewById(R.id.tweet_content_image);
			loadTweetImage(tweet, tweetContentImage);

			
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
	
	public TweetListMode getTweetListMode()
	{
		return mTweetListMode;
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
	
	private void loadTweetImage(Tweet tweet,ImageView imageView)
	{
		Log.d(TAG,"tweeter.profileimageurl="+tweet.imageurl+ "   imageView="+imageView.toString());
		if(TextUtils.isEmpty(tweet.imageurl))
		{
			imageView.setVisibility(View.GONE);
		}
		else
		{
			imageView.setVisibility(View.VISIBLE);
			mImageFetcher2.loadImage(tweet.imageurl, imageView);
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

}

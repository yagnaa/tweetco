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
	private boolean canScroll = true;
	// A flag to enable/disable row clicks
	protected boolean rowEnabled = true;
	
	
	private boolean hasMoreItemsToLoad = false;
	private long lastDataSetChangedTime = System.currentTimeMillis();


	public void lock() 
	{
		canScroll = false;
	}
	
	public void unlock() 
	{
		canScroll = true;
	}
	
	public boolean canScroll()
	{
		if((lastDataSetChangedTime - System.currentTimeMillis()) > 60000)
		{
			canScroll = true;
		}
		return canScroll;
	}

	private TweetListMode mTweetListMode = null; 


	private static class ViewHolderForBookmarkUpVoteAndHide
	{
		int position;
		int iterator;
		String OwenerName;
	}

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
		lastDataSetChangedTime =  System.currentTimeMillis();
	}
	@Override
	public void notifyDataSetInvalidated() 
	{
		Log.v(TAG, "notifyDataSetInvalidated()");
		super.notifyDataSetInvalidated();
		lastDataSetChangedTime =  System.currentTimeMillis();
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
			tweeter = (TweetUser) TweetCommonData.tweetUsers.get(tweet.tweetowner.toLowerCase());
			TextView handle = (TextView) convertView.findViewById(R.id.handle);
			TextView userName = (TextView) convertView.findViewById(R.id.username);
			TextView tweetContent = (TextView) convertView.findViewById(R.id.tweetcontent);
			TextView tweetTime = (TextView) convertView.findViewById(R.id.time);
			
			if(tweeter!=null)
			{
				handle.setText(Utils.getTweetHandle(tweeter.username));
			}
			else
			{
				Log.e(TAG, "TweetUser Not found for tweet with content "+tweet.tweetcontent);
			}
			
			if(tweeter!=null && !TextUtils.isEmpty(tweeter.displayname))
			{
				userName.setText(tweeter.displayname);
			}
			else
			{
				userName.setText(tweeter.username);
			}
			tweetContent.setText(tweet.tweetcontent);	
			Linkify.addLinks(tweetContent, Linkify.WEB_URLS);
			tweetContent.setMovementMethod(new LinkMovementMethod());

			ImageView tweetContentImage = (ImageView) convertView.findViewById(R.id.tweet_content_image);
			loadTweetImage(tweet, tweetContentImage);

			tweetTime.setText(Utils.getTime(tweet.__createdAt));
			
			ViewHolderForBookmarkUpVoteAndHide holder = new ViewHolderForBookmarkUpVoteAndHide();
			holder.iterator = tweet.iterator;
			holder.OwenerName = tweet.tweetowner;
			holder.position = position;

			//UpVote ImageView
			ImageView upvoteView = (ImageView) convertView.findViewById(R.id.upvote);
			upvoteView.setTag(holder);
			setUpVoteFlag(upvoteView,tweet,TweetCommonData.getUserName());
			upvoteView.setOnClickListener(new OnClickListener() 
			{	
				@Override
				public void onClick(View upvoteView) 
				{
					upvoteView.setSelected(true);
					ViewHolderForBookmarkUpVoteAndHide holder = (ViewHolderForBookmarkUpVoteAndHide) upvoteView.getTag();
					upVote(upvoteView,TweetCommonData.getUserName(), holder.iterator, holder.OwenerName);
				}

			});

			ImageView bookmarkView = (ImageView) convertView.findViewById(R.id.bookmark);
			bookmarkView.setTag(holder);
			setBookMarkFlag(bookmarkView,tweet,TweetCommonData.getUserName());
			bookmarkView.setOnClickListener(new OnClickListener() 
			{	
				@Override
				public void onClick(View bookmarkView) 
				{
					bookmarkView.setSelected(true);
					ViewHolderForBookmarkUpVoteAndHide holder = (ViewHolderForBookmarkUpVoteAndHide) bookmarkView.getTag();
					bookmark(bookmarkView,TweetCommonData.getUserName(), holder.iterator, holder.OwenerName);
				}
			});


			ImageView hideTweet = (ImageView) convertView.findViewById(R.id.hide);
			hideTweet.setTag(holder);
			hideTweet.setOnClickListener(new OnClickListener() 
			{	
				@Override
				public void onClick(View v) 
				{
					ViewHolderForBookmarkUpVoteAndHide holder = (ViewHolderForBookmarkUpVoteAndHide) v.getTag();
					hide(v,TweetCommonData.getUserName(), holder.iterator);
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

	public Object removeItem(int position)
	{
		return mTweetListMode.removeItem(position);
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

	private void setUpVoteFlag(ImageView imageView,Tweet linkedTweet,String userName)
	{
		if(linkedTweet != null && imageView!=null)
		{
			boolean isCurrentUserUpVoted  = TweetUtils.isStringPresent(linkedTweet.upvoters, userName);
			imageView.setSelected(isCurrentUserUpVoted);
		}
	}
	
	private void setBookMarkFlag(ImageView imageView,Tweet linkedTweet,String userName)
	{
		if(linkedTweet != null && imageView!=null)
		{
			boolean didCurrentUserBookmark  = TweetUtils.isStringPresent(linkedTweet.bookmarkers, userName);
			imageView.setSelected(didCurrentUserBookmark);
		}
	}

	//This notifies that the list has ended
	public void notifyEndOfList() 
	{
		// When there is no more to load use the lock to prevent loading from happening
		lock();
		// More actions when there is no more to load
		if (mInfiniteListPageListener != null) {
			mInfiniteListPageListener.endOfList();
		}
	}

	//This notifies that there are more tweets to be loaded
	public void notifyHasMore() 
	{
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




	public void upVote(final View upvoteView,final String requestingUser,final int iterator,String tweetOwner)
	{
		MobileServiceClient mClient = TweetCommonData.mClient;
		JsonObject obj = new JsonObject();
		obj.addProperty(ApiInfo.kRequestingUserKey, requestingUser);
		obj.addProperty(ApiInfo.kIteratorKey, iterator);
		obj.addProperty(ApiInfo.kTweetOwner, tweetOwner);
		mClient.invokeApi(ApiInfo.UPVOTE, obj, new ApiJsonOperationCallback() {

			@Override
			public void onCompleted(JsonElement arg0, Exception arg1,
					ServiceFilterResponse arg2) 
			{
				ViewHolderForBookmarkUpVoteAndHide holder = (ViewHolderForBookmarkUpVoteAndHide) upvoteView.getTag();
				if(arg1 == null)
				{
					//This will ensure that we are dealing with the same view
					if(upvoteView!=null && (holder.iterator == iterator))
					{
						upvoteView.setSelected(true);
					}
					//TODO change the adapter underneath
				}
				else
				{
					//This will ensure that we are dealing with the same view
					if(upvoteView!=null && (holder.iterator == iterator))
					{
						upvoteView.setSelected(false);
					}
					Log.e(TAG,"Exception upVoting a tweet") ;
					arg1.printStackTrace();
				}

			}
		},false);
	}

	public void bookmark(final View bookmarkView,final String requestingUser,final int iterator,String tweetOwner)
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
				ViewHolderForBookmarkUpVoteAndHide holder = (ViewHolderForBookmarkUpVoteAndHide) bookmarkView.getTag();
				if(arg1 == null)
				{
					//This will ensure that we are dealing with the same view
					if(bookmarkView!=null && (holder.iterator == iterator))
					{
						bookmarkView.setSelected(true);
					}
					//TODO change the adapter underneath
				}
				else
				{
					//This will ensure that we are dealing with the same view
					if(bookmarkView!=null && (holder.iterator == iterator))
					{
						bookmarkView.setSelected(false);
					}
					Log.e(TAG,"Exception bookmarking a tweet") ;
					arg1.printStackTrace();
				}

			}
		},false);
	}

	public void hide(final View hideTweetView,final String requestingUser,int iterator)
	{
		MobileServiceClient mClient = TweetCommonData.mClient;
		JsonObject obj = new JsonObject();
		obj.addProperty(ApiInfo.kRequestingUserKey, requestingUser);
		obj.addProperty(ApiInfo.kIteratorKey, iterator);
		mClient.invokeApi(ApiInfo.HIDE_TWEET, obj, new ApiJsonOperationCallback() 
		{

			@Override
			public void onCompleted(JsonElement arg0, Exception arg1,
					ServiceFilterResponse arg2) 
			{
				if(arg1 == null)
				{
					ViewHolderForBookmarkUpVoteAndHide holder = (ViewHolderForBookmarkUpVoteAndHide) hideTweetView.getTag();
					//TODO change the adapter underneath
					TweetAdapter.this.removeItem(holder.position);
					refreshAdapter();
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

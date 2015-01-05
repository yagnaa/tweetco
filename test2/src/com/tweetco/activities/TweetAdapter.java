package com.tweetco.activities;

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
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.imagedisplay.util.ImageFetcher;
import com.imagedisplay.util.Utils;
import com.microsoft.windowsazure.mobileservices.ApiJsonOperationCallback;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponse;
import com.tweetco.R;
import com.tweetco.activities.Constants;
import com.tweetco.dao.Tweet;
import com.tweetco.dao.TweetUser;
import com.tweetco.tweetlist.TweetListMode;
import com.tweetco.tweets.TweetCommonData;



/**
 * The main adapter that backs the GridView. This is fairly standard except the number of
 * columns in the GridView is used to create a fake top row of empty views as we use a
 * transparent ActionBar and don't want the real top row of images to start off covered by it.
 */
public class TweetAdapter extends BaseAdapter
{
	public static final String TAG = "TweetAdapter";


	// A demo listener to pass actions from view to adapter
	public static abstract class NewPageLoader 
	{
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
	private long lastDataSetChangedTime = System.currentTimeMillis();

	private TweetListMode mTweetListMode = null; 


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
		long currentTime = System.currentTimeMillis();
		if((currentTime- lastDataSetChangedTime) > Constants.THROTTLE_TIME_BETWEEN_DOWN_SCROLL)
		{
			canScroll = true;
		}
		return canScroll;
	}



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
	
	
	
	private static class ViewHolder
	{
		ImageView profilePicImage;
		TextView handle;
		TextView userName;
		TextView tweetContent;
		TextView tweetTime;
		ImageView tweetContentImage;
		ImageView upvoteView;
		ImageView bookmarkView;
	//	ImageView hideTweet;
	}
	
	@Override
	public View getView(final int position, View convertView, ViewGroup container) 
	{
		Log.v(TAG, "getView called for position ="+position +" convertView="+(convertView!=null));

		if (convertView == null) 
		{ 
			LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = layoutInflater.inflate(R.layout.tweet, container, false);
			ViewHolder viewholder = new ViewHolder();
			viewholder.profilePicImage = (ImageView)convertView.findViewById(R.id.profile_pic);
			viewholder.profilePicImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
			viewholder.handle = (TextView) convertView.findViewById(R.id.handle);
			viewholder.userName = (TextView) convertView.findViewById(R.id.username);
			viewholder.tweetContent = (TextView) convertView.findViewById(R.id.tweetcontent);
			viewholder.tweetTime = (TextView) convertView.findViewById(R.id.time);
			
			viewholder.tweetContentImage = (ImageView) convertView.findViewById(R.id.tweet_content_image);
			viewholder.upvoteView = (ImageView) convertView.findViewById(R.id.upvote);
			viewholder.bookmarkView = (ImageView) convertView.findViewById(R.id.bookmark);
	//		viewholder.hideTweet = (ImageView) convertView.findViewById(R.id.hide);
			convertView.setTag(viewholder);

		} 
		
		ViewHolder holder = (ViewHolder) convertView.getTag();
		holder.profilePicImage.setOnClickListener(new OnClickListener() 
		{	
			@Override
			public void onClick(View v) 
			{
				mOnProfilePicClickCallback.onItemClick(position);
			}
		});



		//Load TextFields here
		final Tweet tweet = (Tweet) getItem(position);
		TweetUser tweeter = (TweetUser) TweetCommonData.tweetUsers.get(tweet.tweetowner.toLowerCase());
		if (tweet != null && tweeter!=null) 
		{
			holder.handle.setText(Utils.getTweetHandle(tweeter.username));			
			if(!TextUtils.isEmpty(tweeter.displayname))
			{
				holder.userName.setText(tweeter.displayname);
			}
			else
			{
				holder.userName.setText(tweeter.username);
			}
			holder.tweetContent.setText(tweet.tweetcontent);	
			Linkify.addLinks(holder.tweetContent, Linkify.WEB_URLS);
			holder.tweetContent.setMovementMethod(new LinkMovementMethod());

			loadTweetImage(tweet, holder.tweetContentImage);

			holder.tweetTime.setText(Utils.getTime(tweet.__createdAt));
			
			ViewHolderForBookmarkUpVoteAndHide viewHolderBookMarkUpvoteAndHide = new ViewHolderForBookmarkUpVoteAndHide();
			viewHolderBookMarkUpvoteAndHide.iterator = tweet.iterator;
			viewHolderBookMarkUpvoteAndHide.OwenerName = tweet.tweetowner;
			viewHolderBookMarkUpvoteAndHide.position = position;

			//UpVote ImageView

			holder.upvoteView.setTag(viewHolderBookMarkUpvoteAndHide);
			setUpVoteFlag(holder.upvoteView,tweet,TweetCommonData.getUserName());
			holder.upvoteView.setOnClickListener(new OnClickListener() 
			{	
				@Override
				public void onClick(View upvoteView) 
				{
					upvoteView.setSelected(true);
					ViewHolderForBookmarkUpVoteAndHide holder = (ViewHolderForBookmarkUpVoteAndHide) upvoteView.getTag();
					upVote(upvoteView,TweetCommonData.getUserName(), holder.iterator, holder.OwenerName);
				}

			});


			holder.bookmarkView.setTag(viewHolderBookMarkUpvoteAndHide);
			setBookMarkFlag(holder.bookmarkView,tweet,TweetCommonData.getUserName());
			holder.bookmarkView.setOnClickListener(new OnClickListener() 
			{	
				@Override
				public void onClick(View bookmarkView) 
				{
					bookmarkView.setSelected(true);
					ViewHolderForBookmarkUpVoteAndHide viewHolderBookMarkUpvoteAndHide = (ViewHolderForBookmarkUpVoteAndHide) bookmarkView.getTag();
					bookmark(bookmarkView,TweetCommonData.getUserName(), viewHolderBookMarkUpvoteAndHide.iterator, viewHolderBookMarkUpvoteAndHide.OwenerName);
				}
			});


//			holder.hideTweet.setTag(viewHolderBookMarkUpvoteAndHide);
//			holder.hideTweet.setOnClickListener(new OnClickListener() 
//			{	
//				@Override
//				public void onClick(View hideTweet) 
//				{
//					ViewHolderForBookmarkUpVoteAndHide viewHolderBookMarkUpvoteAndHide = (ViewHolderForBookmarkUpVoteAndHide) hideTweet.getTag();
//					hide(hideTweet,TweetCommonData.getUserName(), viewHolderBookMarkUpvoteAndHide.iterator);
//				}
//			});
			

			// Finally load the image asynchronously into the ImageView, this also takes care of
			// setting a placeholder image while the background thread runs
			if(tweeter!=null)
			{
				loadProfileImage(tweeter,holder.profilePicImage);
			}

		}
		else
		{
			Log.e(TAG, "TweetUser Not found for tweet with content "+tweet.tweetcontent);
		}


		return convertView;
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
				ViewHolderForBookmarkUpVoteAndHide viewHolderBookMarkUpvoteAndHide = (ViewHolderForBookmarkUpVoteAndHide) upvoteView.getTag();
				if(arg1 == null)
				{
					//This will ensure that we are dealing with the same view
					if(upvoteView!=null && (viewHolderBookMarkUpvoteAndHide.iterator == iterator))
					{
						upvoteView.setSelected(true);
						//TODO change the adapter underneath
						Tweet tweet = (Tweet)TweetAdapter.this.getItem(viewHolderBookMarkUpvoteAndHide.position);
						TweetCommonData.like(tweet,TweetCommonData.getUserName());
					}

				}
				else
				{
					//This will ensure that we are dealing with the same view
					if(upvoteView!=null && (viewHolderBookMarkUpvoteAndHide.iterator == iterator))
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
				ViewHolderForBookmarkUpVoteAndHide viewHolderBookMarkUpvoteAndHide = (ViewHolderForBookmarkUpVoteAndHide) bookmarkView.getTag();
				if(arg1 == null)
				{
					//This will ensure that we are dealing with the same view
					if(bookmarkView!=null && (viewHolderBookMarkUpvoteAndHide.iterator == iterator))
					{
						bookmarkView.setSelected(true);
						Tweet tweet = (Tweet)TweetAdapter.this.getItem(viewHolderBookMarkUpvoteAndHide.position);
						TweetCommonData.bookmark(tweet,TweetCommonData.getUserName());
					}			
				}
				else
				{
					//This will ensure that we are dealing with the same view
					if(bookmarkView!=null && (viewHolderBookMarkUpvoteAndHide.iterator == iterator))
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
					ViewHolderForBookmarkUpVoteAndHide viewHolderBookMarkUpvoteAndHide = (ViewHolderForBookmarkUpVoteAndHide) hideTweetView.getTag();
					//TODO change the adapter underneath
					TweetAdapter.this.removeItem(viewHolderBookMarkUpvoteAndHide.position);
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

package com.tweetco.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.imagedisplay.util.ImageFetcher;
import com.imagedisplay.util.Utils;
import com.onefortybytes.R;
import com.tweetco.TweetCo;
import com.tweetco.dao.Tweet;
import com.tweetco.dao.TweetUser;
import com.tweetco.tweetlist.TweetRepliesFeedMode;
import com.tweetco.tweetlist.UserFeedMode;
import com.tweetco.tweets.TweetCommonData;
import com.tweetco.utility.UiUtility;

public class TweetDetailActivity extends TweetCoBaseActivity 
{
	public static final String TAG = "TweetAdapter";
	
	//The first imageFetcher loads profileImages and the second one loads the tweetcontent images.
	ImageFetcher mImageFetcher;
	ImageFetcher mImageFetcher2;

	private static class ViewHolder
	{
		ImageView profilePicImage;
		TextView handle;
		TextView userName;
		TextView tweetContent;
		TextView tweetTime;
		ImageView tweetContentImage;
		TextView upvotesCount;
		TextView bookmarksCount;
		//		ImageView hideTweet;
	}
	
	@Override
	public void onCreate(Bundle bundle)
	{
		super.onCreate(bundle);
		
		setContentView(R.layout.tweetdetail);
		
		final Tweet tweet = (Tweet)getIntent().getParcelableExtra("Tweet");
		
		mImageFetcher = Utils.getImageFetcher(this, 60, 60);

		mImageFetcher2 = Utils.getImageFetcher(this, 60, 60);
		
		ViewHolder viewholder = new ViewHolder();
		viewholder.profilePicImage = UiUtility.getView(this, R.id.profile_pic);
		viewholder.profilePicImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
		viewholder.handle = UiUtility.getView(this, R.id.handle);
		viewholder.userName = UiUtility.getView(this, R.id.username);
		viewholder.tweetContent = UiUtility.getView(this, R.id.tweetcontent);
		viewholder.tweetTime = UiUtility.getView(this, R.id.time);

		viewholder.tweetContentImage = UiUtility.getView(this, R.id.tweet_content_image);
		viewholder.upvotesCount = UiUtility.getView(this, R.id.upvotesCount);
		viewholder.bookmarksCount = UiUtility.getView(this, R.id.bookmarksCount);
		
		viewholder.profilePicImage.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) 
			{
				String owner = tweet.tweetowner;
				if(!TextUtils.isEmpty(owner))
				{
					Intent intent = new Intent(TweetDetailActivity.this, UserProfileActivity.class);
					intent.putExtra(Constants.USERNAME_STR, owner);
					TweetDetailActivity.this.startActivity(intent);
				}
			}
		});
		
		TweetUser tweeter = (TweetUser) TweetCommonData.tweetUsers.get(tweet.tweetowner.toLowerCase());
		if (tweet != null && tweeter!=null) 
		{
			viewholder.handle.setText(Utils.getTweetHandle(tweeter.username));			
			if(!TextUtils.isEmpty(tweeter.displayname))
			{
				viewholder.userName.setText(tweeter.displayname);
			}
			else
			{
				viewholder.userName.setText(tweeter.username);
			}
			viewholder.tweetContent.setText(tweet.tweetcontent);	
			Linkify.addLinks(viewholder.tweetContent, Linkify.WEB_URLS | Linkify.HASH_TAGS | Linkify.USER_HANDLE);

			viewholder.tweetContent.setMovementMethod(new LinkMovementMethod());

			viewholder.bookmarksCount.setText(String.valueOf(getCount(tweet.bookmarkers, ";")));
			
			viewholder.upvotesCount.setText(String.valueOf(getCount(tweet.upvoters, ";")));
			
			loadTweetImage(tweet, viewholder.tweetContentImage);


			viewholder.tweetTime.setText(Utils.getTime(tweet.__createdAt));




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
				loadProfileImage(tweeter,viewholder.profilePicImage);
			}

		}
		else
		{
			Log.e(TAG, "TweetUser Not found for tweet with content "+tweet.tweetcontent);
		}
		
		if(!TextUtils.isEmpty(tweet.replies))
		{
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
	        TweetListFragment tweetListFragment = new TweetListFragment();
	        Bundle newBundle = new Bundle();
	        TweetRepliesFeedMode mode = new TweetRepliesFeedMode(String.valueOf(tweet.iterator));
	        newBundle.putParcelable(Constants.TWEET_LIST_MODE, mode);
	        newBundle.putBoolean("hideFooter", true);
	        tweetListFragment.setArguments(newBundle);
	        ft.replace(R.id.tweetsReplyListFragmentContainer, tweetListFragment);
	        ft.commit();
		}
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
	
	private void loadTweetImage(final Tweet tweet,ImageView imageView)
	{
		if(TextUtils.isEmpty(tweet.imageurl))
		{
			imageView.setVisibility(View.GONE);
		}
		else
		{
			imageView.setVisibility(View.VISIBLE);
			mImageFetcher2.loadImage(tweet.imageurl, imageView);
			imageView.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) 
				{
					Intent intent = new Intent(TweetCo.mContext,ImageViewActivity.class);
					intent.putExtra(Constants.IMAGE_TO_VIEW, tweet.imageurl);
					intent.setFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_NEW_TASK);
					TweetCo.mContext.startActivity(intent);				
				}
			});
		}
	}
	
	private static int getCount(String input, String delimeter)
	{
		int count = 0;
		
		if(!TextUtils.isEmpty(input))
		{
			count = input.split(delimeter).length;
		}
		
		return count;
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
	}
	
	@Override
	public void onResumeCallback() {
		// TODO Auto-generated method stub

	}

}

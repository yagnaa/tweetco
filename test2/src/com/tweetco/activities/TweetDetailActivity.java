package com.tweetco.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.imagedisplay.util.ImageFetcher;
import com.imagedisplay.util.Utils;
import com.microsoft.windowsazure.mobileservices.ApiJsonOperationCallback;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponse;
import com.onefortybytes.R;
import com.tweetco.TweetCo;
import com.tweetco.dao.Tweet;
import com.tweetco.dao.TweetUser;
import com.tweetco.tweetlist.TweetRepliesFeedMode;
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
		ImageView upvoteView;
		ImageView bookmarkView;
		TextView inReplyTo;
		ImageView replyToTweetButton;
		TextView tweet_upvotesCount;
		TextView tweet_bookmarksCount;
		//		ImageView hideTweet;
	}
	
	private static class ViewHolderForBookmarkUpVoteAndHide
	{
		Tweet tweet;
		int iterator;
		String OwenerName;
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
		viewholder.inReplyTo = UiUtility.getView(this, R.id.in_reply_to);
		viewholder.replyToTweetButton = UiUtility.getView(this, R.id.replyToTweet);
		viewholder.tweet_upvotesCount = UiUtility.getView(this, R.id.tweet_upvoteCount);
		viewholder.tweet_bookmarksCount = UiUtility.getView(this, R.id.tweet_bookmarksCount);

		viewholder.tweetContentImage = UiUtility.getView(this, R.id.tweet_content_image);
		viewholder.upvoteView = UiUtility.getView(this, R.id.upvote);
		viewholder.bookmarkView = UiUtility.getView(this, R.id.bookmark);
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
		if (tweet != null) 
		{
			String username = null;
			String displayName = null;
			if(tweeter == null)
			{
				username = " ";
				displayName = "Anonymous";
			}
			else
			{
				username = Utils.getTweetHandle(tweeter.username);
				displayName = tweeter.displayname;
			}
			
			viewholder.handle.setText(username);			
			viewholder.userName.setText(displayName);
			viewholder.tweetContent.setText(tweet.tweetcontent);	
			Linkify.addLinks(viewholder.tweetContent, Linkify.WEB_URLS | Linkify.HASH_TAGS | Linkify.USER_HANDLE);

			viewholder.tweetContent.setMovementMethod(new LinkMovementMethod());

			setCount(viewholder.tweet_upvotesCount, tweet.upvoters);
			setCount(viewholder.tweet_bookmarksCount, tweet.bookmarkers);
			viewholder.bookmarksCount.setText(String.valueOf(getCount(tweet.bookmarkers, ";") + " Bookmarks"));
			viewholder.bookmarksCount.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) 
				{
					if(!TextUtils.isEmpty(tweet.bookmarkers))
					{
						Intent intent = new Intent(getApplication(), UsersListActivity.class);
						intent.putExtra("title", "Bookmarked by");
						intent.putExtra("usersList", tweet.bookmarkers);
						startActivity(intent);
					}
				}
			});
			
			viewholder.upvotesCount.setText(String.valueOf(getCount(tweet.upvoters, ";") + " Upvotes"));
			viewholder.upvotesCount.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) 
				{
					if(!TextUtils.isEmpty(tweet.upvoters))
					{
						Intent intent = new Intent(getApplication(), UsersListActivity.class);
						intent.putExtra("title", "Upvoted by");
						intent.putExtra("usersList", tweet.upvoters);
						startActivity(intent);
					}
				}
			});
			
			viewholder.replyToTweetButton.setOnClickListener(new OnClickListener() 
			{
				@Override
				public void onClick(View v) 
				{
					launchPostTweetActivity("@"+tweet.tweetowner+" ", tweet.iterator, tweet.tweetowner);
				}
			});
			
			loadTweetImage(tweet, viewholder.tweetContentImage);


			viewholder.tweetTime.setText(Utils.getTime(tweet.__createdAt));

			ViewHolderForBookmarkUpVoteAndHide viewHolderBookMarkUpvoteAndHide = new ViewHolderForBookmarkUpVoteAndHide();
			viewHolderBookMarkUpvoteAndHide.iterator = tweet.iterator;
			viewHolderBookMarkUpvoteAndHide.OwenerName = tweet.tweetowner;
			viewHolderBookMarkUpvoteAndHide.tweet = tweet;
			
			if(!TextUtils.isEmpty(tweet.inreplyto))
			{
				viewholder.inReplyTo.setVisibility(View.VISIBLE);
				viewholder.inReplyTo.setText("In reply to " + tweet.sourceuser);
			}
			else
			{
				viewholder.inReplyTo.setVisibility(View.GONE);
			}

			//UpVote ImageView

			viewholder.upvoteView.setTag(viewHolderBookMarkUpvoteAndHide);
			setUpVoteFlag(viewholder.upvoteView,tweet,TweetCommonData.getUserName());
			viewholder.upvoteView.setOnClickListener(new OnClickListener() 
			{	
				@Override
				public void onClick(View upvoteView) 
				{
					upvoteView.setSelected(true);
					ViewHolderForBookmarkUpVoteAndHide holder = (ViewHolderForBookmarkUpVoteAndHide) upvoteView.getTag();
					upVote(upvoteView,TweetCommonData.getUserName(), holder.iterator, holder.OwenerName);
				}

			});


			viewholder.bookmarkView.setTag(viewHolderBookMarkUpvoteAndHide);
			setBookMarkFlag(viewholder.bookmarkView,tweet,TweetCommonData.getUserName());
			viewholder.bookmarkView.setOnClickListener(new OnClickListener() 
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
				loadProfileImage(tweeter,viewholder.profilePicImage);
			}
			else
			{
				mImageFetcher.loadImage("A", viewholder.profilePicImage);
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
		
		ActionBar actionbar = getSupportActionBar();
		if(actionbar!=null)
		{
			actionbar.setHomeButtonEnabled(true);
			actionbar.setDisplayHomeAsUpEnabled(true);
		}
	}
	
	private void setCount(TextView view, String input)
	{
		int count = 0;
		if(!TextUtils.isEmpty(input))
		{
			String[] counts = input.split(";");
			count = counts.length;
		}
		
		view.setText(String.valueOf(count));
		
	}
	
	public void launchPostTweetActivity(String existingString, int replySourceTweetIterator, String replySourceTweetUsername)
	{
		Intent intent = new Intent(this,PostTweetActivity.class);
		intent.putExtra(Constants.EXISTING_STRING, existingString);
		if(!TextUtils.isEmpty(replySourceTweetUsername))
		{
			intent.putExtra("replySourceTweetUsername", replySourceTweetUsername);
			intent.putExtra("replySourceTweetIterator", replySourceTweetIterator);
		}
		this.startActivityForResult(intent, Constants.POSTED_TWEET_REQUEST_CODE);
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
						Tweet tweet = viewHolderBookMarkUpvoteAndHide.tweet;
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
						Tweet tweet = viewHolderBookMarkUpvoteAndHide.tweet;
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
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    // Respond to the action bar's Up/Home button
	    case android.R.id.home:
	        NavUtils.navigateUpFromSameTask(this);
	        return true;
	    }
	    return super.onOptionsItemSelected(item);
	}

}

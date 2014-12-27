package com.tweetco.activities;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.MultiAutoCompleteTextView.Tokenizer;
import android.widget.TextView;

import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.tweetco.R;
import com.tweetco.activities.progress.AsyncTaskEventHandler;
import com.tweetco.asynctasks.PostTweetTask;
import com.tweetco.asynctasks.PostTweetTask.PostTweetTaskCompletionCallback;
import com.tweetco.asynctasks.PostTweetTaskParams;
import com.tweetco.tweets.TweetCommonData;
import com.tweetco.utility.ClientHelper;
import com.tweetco.utility.ImageUtility;
import com.tweetco.utility.UiUtility;
import com.yagnasri.dao.TweetUser;
import com.yagnasri.displayingbitmaps.ui.AllInOneActivity;
import com.yagnasri.displayingbitmaps.ui.Tweet;

public class PostTweetActivity extends TweetCoBaseActivity 
{
	private final static String TAG = "PostTweetActivity";
	private static final int TWEET_MAX_CHARS = 140;
	private static final int REQUEST_CODE_IMAGE_SELECT = 100;
	private static final int REQUEST_CODE_IMAGE_CAPTURE = 101;
	
	private MultiAutoCompleteTextView mTweetContent;
	private TextView mCharCount;
	private Button mSendButton;
	private Button mImageGalleryButton;
	private Button mImageCameraButton;
	private ImageView mTweetImage;
	private String[] mUsernames;
	
	private int mCharCountInt = TWEET_MAX_CHARS;
	AsyncTaskEventHandler asyncTaskEventHandler = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.posttweet);
		
		mTweetContent = UiUtility.getView(this, R.id.tweetContent);
		mCharCount = UiUtility.getView(this, R.id.charCount);
		mSendButton = UiUtility.getView(this, R.id.sendTweetButton);
		mImageGalleryButton = UiUtility.getView(this, R.id.imageGalleryButton);
		mImageCameraButton = UiUtility.getView(this, R.id.imageCameraButton);
		mTweetImage = UiUtility.getView(this, R.id.tweetImaage);
		asyncTaskEventHandler = new AsyncTaskEventHandler(this, "Posting...");
		mUsernames = getUsernames(TweetCommonData.tweetUsers.values().iterator());
		mTweetContent.setAdapter(new ArrayAdapter<String>(PostTweetActivity.this,
                android.R.layout.simple_dropdown_item_1line, mUsernames));
		mTweetContent.setThreshold(1);
		
		//From http://stackoverflow.com/questions/12691679/android-autocomplete-textview-similar-to-the-facebook-app
		//Create a new Tokenizer which will get text after '@' and terminate on ' '
		mTweetContent.setTokenizer(new Tokenizer() {

		  @Override
		  public CharSequence terminateToken(CharSequence text) {
		    int i = text.length();

		    while (i > 0 && text.charAt(i - 1) == ' ') {
		      i--;
		    }

		    if (i > 0 && text.charAt(i - 1) == ' ') {
		      return text;
		    } else {
		        SpannableString sp = new SpannableString(text + " ");
		        sp.setSpan(new ForegroundColorSpan(Color.BLUE), 0, sp.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		        return sp;
		      
		    }
		  }

		  @Override
		  public int findTokenStart(CharSequence text, int cursor) {
		    int i = cursor;

		    while (i > 0 && text.charAt(i - 1) != '@') {
		      i--;
		    }

		    //Check if token really started with @, else we don't have a valid token
		    if (i < 1 || text.charAt(i - 1) != '@') {
		      return cursor;
		    }

		    return i;
		  }

		  @Override
		  public int findTokenEnd(CharSequence text, int cursor) {
		    int i = cursor;
		    int len = text.length();

		    while (i < len) {
		      if (text.charAt(i) == ' ') {
		        return i;
		      } else {
		        i++;
		      }
		    }

		    return len;
		  }
		});
		
		mCharCount.setText(String.valueOf(mCharCountInt));
		
		mTweetContent.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) 
			{
				int decrementValue = (before > 0)? -before: count;
				mCharCountInt = mCharCountInt - decrementValue;
				mCharCount.setText(String.valueOf(mCharCountInt));
				if(mCharCountInt < 0)
				{
					mSendButton.setEnabled(false);
					mCharCount.setTextColor(Color.RED);
				}
				else
				{
					mSendButton.setEnabled(true);
					mCharCount.setTextColor(Color.BLACK);
				}
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				
				
			}
		});
		
		mSendButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) 
			{
				try 
				{
					MobileServiceClient client = ClientHelper.getMobileClient(PostTweetActivity.this);
					PostTweetTaskParams params = new PostTweetTaskParams(client, TweetCommonData.getUserName());
					params.setTweetContent(mTweetContent.getEditableText().toString());
					params.setTweetImage((BitmapDrawable) mTweetImage.getDrawable());
					
					new PostTweetTask(getApplicationContext(), params, asyncTaskEventHandler, new PostTweetTaskCompletionCallback() {
						
						@Override
						public void onPostTweetTaskSuccess(Tweet tweet) {
							asyncTaskEventHandler.dismiss();
							AllInOneActivity.tweetsListRefresh();
							finish();
						}
						
						@Override
						public void onPostTweetTaskFailure() {
							asyncTaskEventHandler.dismiss();
							Log.e(TAG, "Posting tweet failed");
						}
						
						@Override
						public void onPostTweetTaskCancelled() {
							asyncTaskEventHandler.dismiss();
							
						}
					}).execute();
				} 
				catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		
		mImageGalleryButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) 
			{
				Intent intent = ImageUtility.getImageChooserIntent(getApplicationContext());
				startActivityForResult(intent, REQUEST_CODE_IMAGE_SELECT);
			}
		});
		
		mImageCameraButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) 
			{
				Intent intent = ImageUtility.getImageCaptureIntent(getApplicationContext());
				startActivityForResult(intent, REQUEST_CODE_IMAGE_CAPTURE);
			}
		});
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (resultCode == Activity.RESULT_OK)
		{
			if (requestCode == REQUEST_CODE_IMAGE_SELECT || requestCode == REQUEST_CODE_IMAGE_CAPTURE)
			{
				Uri fileUri = null;
				try
				{
					fileUri = ImageUtility.onImageAttachmentReceived(getApplicationContext(), data);
				
					if(fileUri != null)
					{
						mTweetImage.setImageURI(fileUri);
					}
				}
				catch (FileNotFoundException e)
				{
					Log.e("PostTweet", "onActivityResult onImageAttachmentReceived FileNotFoundException");
				}
				catch (IOException e)
				{
					Log.e("PostTweet", "onActivityResult onImageAttachmentReceived IOException");
				}
				catch (IllegalArgumentException e)
				{
					Log.e("PostTweet", "onActivityResult onImageAttachmentReceived IllegalArgumentException, "+e.getMessage());					
				}

				if(fileUri != null)
				{
					
				}
				else
				{
					Log.e("PostTweet", "Attachment error");
				}

			}
			
		}
		else
		{
			if (requestCode == REQUEST_CODE_IMAGE_SELECT || requestCode == REQUEST_CODE_IMAGE_CAPTURE)
			{
				ImageUtility.onImageAttachmentCancelled();
			}
			Log.i("PostTweet","onActivityResult result code: " + resultCode + " for request code: " + requestCode);
		}

	}
	
	public static String[] getUsernames(Iterator<TweetUser> tweetUsers)
	{
		List<String> usernames = new ArrayList<String>();
		usernames.add("feedback");
		
		for (TweetUser user; tweetUsers.hasNext(); ) 
		{
			user = tweetUsers.next();
			usernames.add(user.username);
		}
		
		String[] usernamesList = new String[usernames.size()];
		
		return usernames.toArray(usernamesList);
	}
}

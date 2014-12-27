package com.tweetco.activities;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.yagnasri.dao.Tweet;

public class PostTweetActivity extends TweetCoBaseActivity 
{
	private final static String TAG = "PostTweetActivity";
	private static final int TWEET_MAX_CHARS = 140;

	private static final int REQUEST_CODE_IMAGE_SELECT = 100;
	private static final int REQUEST_CODE_IMAGE_CAPTURE = 101;
	
	private EditText mTweetContent;
	private TextView mCharCount;
	private Button mSendButton;
	private Button mImageGalleryButton;
	private Button mImageCameraButton;
	private ImageView mTweetImage;
	
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
		
		mCharCount.setText(String.valueOf(mCharCountInt));
		
		mTweetContent.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) 
			{
				mCharCountInt = TWEET_MAX_CHARS - count;
				mCharCount.setText(String.valueOf(mCharCountInt));
				if(mCharCountInt < 0)
				{
					mSendButton.setEnabled(false);
				}
				else
				{
					mSendButton.setEnabled(true);
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
						public void onPostTweetTaskSuccess(Tweet tweet) 
						{
							asyncTaskEventHandler.dismiss();
							Intent resultIntent = new Intent();
							resultIntent.putExtra(Constants.POSTED_TWEET, tweet);
							PostTweetActivity.this.setResult(RESULT_OK, resultIntent);
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
}

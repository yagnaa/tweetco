package com.tweetco.activities;

import java.net.MalformedURLException;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.microsoft.windowsazure.mobileservices.ApiJsonOperationCallback;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponse;
import com.tweetco.R;
import com.tweetco.TweetCo;
import com.tweetco.utility.UiUtility;

public class PostTweetActivity extends Activity 
{
	private static final int TWEET_MAX_CHARS = 140;
	private EditText mTweetContent;
	private TextView mCharCount;
	private Button mSendButton;
	
	private int mCharCountInt = TWEET_MAX_CHARS;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.posttweet);
		
		mTweetContent = UiUtility.getView(this, R.id.tweetContent);
		mCharCount = UiUtility.getView(this, R.id.charCount);
		mSendButton = UiUtility.getView(this, R.id.sendTweetButton);
		
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
					MobileServiceClient client = new MobileServiceClient(TweetCo.APP_URL, TweetCo.APP_KEY, PostTweetActivity.this);
					JsonObject element = new JsonObject();
					element.addProperty("tweetowner", TweetCo.getAccount().getUsername());
					element.addProperty("tweetcontent", mTweetContent.getEditableText().toString());
					client.invokeApi("PostTweet", element, new ApiJsonOperationCallback() {
						
						@Override
						public void onCompleted(JsonElement element, Exception exception,
								ServiceFilterResponse arg2) {
							if(exception != null)
							{
								Log.d("postTweet", "TweetPosted");
							}
							else
							{
								Log.e("postTweet", "TweetPost failed");
							}
							
						}
					}, false);
				} 
				catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}
}

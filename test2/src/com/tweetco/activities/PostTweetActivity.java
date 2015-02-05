package com.tweetco.activities;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.MultiAutoCompleteTextView.Tokenizer;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.services.urlshortener.Urlshortener;
import com.google.api.services.urlshortener.model.Url;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.onefortybytes.R;
import com.tweetco.activities.TrendingFragment.TrendingTag;
import com.tweetco.activities.progress.AsyncTaskEventHandler;
import com.tweetco.activities.progress.AsyncTaskEventSinks.AsyncTaskCancelCallback;
import com.tweetco.activities.progress.AsyncTaskEventSinks.UIEventSink;
import com.tweetco.asynctasks.PostTweetTask;
import com.tweetco.asynctasks.PostTweetTask.PostTweetTaskCompletionCallback;
import com.tweetco.asynctasks.PostTweetTaskParams;
import com.tweetco.dao.TweetUser;
import com.tweetco.tweets.TweetCommonData;
import com.tweetco.utility.AlertDialogUtility;
import com.tweetco.utility.ImageUtility;
import com.tweetco.utility.UiUtility;



public class PostTweetActivity extends TweetCoBaseActivity 
{
	private final static String TAG = "PostTweetActivity";
	private static final int TWEET_MAX_CHARS = 140;

	private static final int REQUEST_CODE_IMAGE_SELECT = 100;
	private static final int REQUEST_CODE_IMAGE_CAPTURE = 101;

	private MultiAutoCompleteTextView mTweetContent;
	private TextView mCharCount;
	private EditText mContentTags;
	private Button mSendButton;
	private Button mImageGalleryButton;
	private Button mImageCameraButton;
	private ImageView mTweetImage;
	private String[] mUsernames;
	
	private int replySourceTweetIterator = -1;
	private String replySourceTweetUsername = null;

	private int mCharCountInt = TWEET_MAX_CHARS;
	AsyncTaskEventHandler asyncTaskEventHandler = null;
	AsyncTaskEventHandler asyncTaskEventHandler2 = null;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.posttweet);

		mTweetContent = UiUtility.getView(this, R.id.tweetContent);
		mContentTags = UiUtility.getView(this, R.id.contentTags);
		mCharCount = UiUtility.getView(this, R.id.charCount);
		mSendButton = UiUtility.getView(this, R.id.sendTweetButton);
		mImageGalleryButton = UiUtility.getView(this, R.id.imageGalleryButton);
		mImageCameraButton = UiUtility.getView(this, R.id.imageCameraButton);
		mTweetImage = UiUtility.getView(this, R.id.tweetImaage);
		asyncTaskEventHandler = new AsyncTaskEventHandler(this, "Posting...");
		asyncTaskEventHandler2 = new AsyncTaskEventHandler(this, "Shortening Urls...");
		mUsernames = getUsernamesAndHashtags(TweetCommonData.tweetUsers.values().iterator(), TweetCommonData.trendingTagLists.iterator());
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

				while (i > 0 && text.charAt(i - 1) != '@' && text.charAt(i - 1) != '#') {
					i--;
				}

				//Check if token really started with @, else we don't have a valid token
				if (i < 1 || (text.charAt(i - 1) != '@' && text.charAt(i - 1) != '#') ) {
					return cursor;
				}

				return i - 1;
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

		mTweetContent.addTextChangedListener(new TextWatcher() 
		{

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
			public void beforeTextChanged(CharSequence s, int start, int count, int after) 
			{

			}

			@Override
			public void afterTextChanged(Editable tweetContent) 
			{
				mCharCountInt = TWEET_MAX_CHARS - tweetContent.length();
				mCharCount.setText(String.valueOf(mCharCountInt));
				Linkify.addLinks(tweetContent, Linkify.WEB_URLS);
				URLSpan[] spansList = tweetContent.getSpans(0, tweetContent.length()-1, URLSpan.class);
				for(URLSpan span:spansList)
				{
					Log.i(TAG,"Shortening URL");
					int start = tweetContent.getSpanStart(span);
					int end = tweetContent.getSpanEnd(span);
					if((end - start) > 21)
					{
						(new URLShortenerTask(tweetContent, span, asyncTaskEventHandler2)).execute();
					}
				}
			}
		});

		String existingString= getIntent().getStringExtra(Constants.EXISTING_STRING);
		if(!TextUtils.isEmpty(existingString))
		{
			mTweetContent.setText(existingString);
			mTweetContent.setSelection(existingString.length());
		}

		mSendButton.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{

				MobileServiceClient client = TweetCommonData.mClient;
				PostTweetTaskParams params = new PostTweetTaskParams(client, TweetCommonData.getUserName());
				params.setTweetContent(mTweetContent.getEditableText().toString());
				params.setTweetImage((BitmapDrawable) mTweetImage.getDrawable());
				params.setContentTags(mContentTags.getEditableText().toString());
				params.setReplySourceTweetIterator(replySourceTweetIterator);
				params.setReplySourceTweetUsername(replySourceTweetUsername);
				
				new PostTweetTask(getApplicationContext(), params, asyncTaskEventHandler, new PostTweetTaskCompletionCallback() 
				{

					@Override
					public void onPostTweetTaskSuccess() 
					{
						asyncTaskEventHandler.dismiss();
						Intent resultIntent = new Intent();
						PostTweetActivity.this.setResult(RESULT_OK, resultIntent);
						finish();
					}

					@Override
					public void onPostTweetTaskFailure() 
					{
						asyncTaskEventHandler.dismiss();
						Log.e(TAG, "Posting tweet failed");
						AlertDialogUtility.getAlertDialogOK(PostTweetActivity.this, "Failed to post your 140 bytes", new  DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								// TODO Auto-generated method stub
								
							}
						});
					}

					@Override
					public void onPostTweetTaskCancelled() 
					{
						asyncTaskEventHandler.dismiss();

					}
				}).execute();

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
				if(intent!=null)
				{
					startActivityForResult(intent, REQUEST_CODE_IMAGE_CAPTURE);
				}
				else
				{
					Toast.makeText(PostTweetActivity.this.getApplicationContext(), "Your device doesn't support this", Toast.LENGTH_SHORT).show();
				}
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

	public static String[] getUsernamesAndHashtags(Iterator<TweetUser> tweetUsers, Iterator<TrendingTag> hashTags)
	{
		List<String> usernamesAndHashtags = new ArrayList<String>();
		usernamesAndHashtags.add("@feedback");

		for (TweetUser user; tweetUsers.hasNext(); ) 
		{
			user = tweetUsers.next();
			usernamesAndHashtags.add("@"+user.username);
		}

		//usernamesAndHashtags.add("#feedback");
		for (TrendingTag tag; hashTags.hasNext(); ) 
		{
			tag = hashTags.next();
			usernamesAndHashtags.add("#"+tag.hashtag);
		}
		
		String[] usernamesList = new String[usernamesAndHashtags.size()];

		return usernamesAndHashtags.toArray(usernamesList);
	}

	public class URLShortenerTask extends AsyncTask<Void, Void, String> 
	{
		private final static String TAG = "URLShortenerTask";
		private UIEventSink m_uicallback;
		private Editable mEditable;
		private URLSpan mUrlSpan = null;

		public URLShortenerTask(Editable editable, URLSpan urlSpan,UIEventSink uicallback)
		{
			m_uicallback = uicallback; 
			mEditable = editable;
			mUrlSpan = urlSpan;
		}

		@Override
		protected void onPreExecute()
		{
			Log.d("tag","onPreExecute");
			if(m_uicallback!=null)
			{
				m_uicallback.onAysncTaskPreExecute(this, new AsyncTaskCancelCallback()
				{
					@Override
					public void onCancelled()
					{
						cancel(true);
					}
				}, true);
			}
		}

		@Override
		protected String doInBackground(Void... params) 
		{

			String shortUrl = null;
			Urlshortener.Builder builder = new Urlshortener.Builder (AndroidHttp.newCompatibleTransport(), AndroidJsonFactory.getDefaultInstance(), null);
			Urlshortener urlshortener = builder.build();

			com.google.api.services.urlshortener.model.Url url = new Url();

			int start = mEditable.getSpanStart(mUrlSpan);
			int end = mEditable.getSpanEnd(mUrlSpan);
			String str = mEditable.toString();
			String seq = str.substring(start, end);

			url.setLongUrl(seq);
			try {
				url = urlshortener.url().insert(url).execute();
				shortUrl = url.getId();
			} catch (IOException e) {
				return null;
			}


			return shortUrl;
		}

		@Override
		protected void onPostExecute(String  shortUrl)
		{
			asyncTaskEventHandler.dismiss();
			if(shortUrl != null)
			{

				int start = mEditable.getSpanStart(mUrlSpan);
				int end = mEditable.getSpanEnd(mUrlSpan);
				mEditable.replace(start, end, shortUrl);

				mTweetContent.setText(mEditable);
				mTweetContent.setSelection(mEditable.length());
			}
		}
	}

	@Override
	public void onResumeCallback() 
	{
		Intent intent = getIntent();
		replySourceTweetIterator = intent.getIntExtra("replySourceTweetIterator", -1);
		replySourceTweetUsername = intent.getStringExtra("replySourceTweetUsername");
	}
}

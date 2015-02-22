package com.tweetco.asynctasks;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Type;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.microsoft.windowsazure.mobileservices.ApiJsonOperationCallback;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponse;
import com.tweetco.activities.ApiInfo;
import com.tweetco.activities.progress.AsyncTaskEventSinks.AsyncTaskCancelCallback;
import com.tweetco.activities.progress.AsyncTaskEventSinks.UIEventSink;
import com.tweetco.dao.Tweet;

public class PostTweetTask extends AsyncTask<Void, Void, Exception> 
{
	public static interface PostTweetTaskCompletionCallback
	{
		public void onPostTweetTaskSuccess();
		public void onPostTweetTaskFailure ();
		public void onPostTweetTaskCancelled();
	}
	
	private final static String TAG = "PostTweetTask";
	private PostTweetTaskCompletionCallback m_completioncallback;
	private UIEventSink m_uicallback;
	private Context mContext;
	private PostTweetTaskParams mParams;
	private Exception mException = null;
	
	public PostTweetTask(Context context, PostTweetTaskParams params, UIEventSink uicallback, PostTweetTaskCompletionCallback completioncallback)
	{
		m_completioncallback = completioncallback;
		m_uicallback = uicallback; 
		mContext = context;
		mParams = params;
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
					m_completioncallback.onPostTweetTaskCancelled();
				}
			}, true);
		}
	}
	
	@Override
	protected Exception doInBackground(Void... params) 
	{
		MobileServiceClient client = mParams.getClient();
			JsonObject element = new JsonObject();
			String contentTags = mParams.getContentTags();
			String username = mParams.getUsername();
			if(!TextUtils.isEmpty(contentTags))
			{
				username = "tweetbot";
			}
			element.addProperty(ApiInfo.kTweetOwner, username);
			element.addProperty(ApiInfo.kTweetContentKey, mParams.getTweetContent());
			element.addProperty(ApiInfo.kTweetContentTags, mParams.getContentTags());
			if(!TextUtils.isEmpty(mParams.getReplySourceTweetUsername()))
			{
				element.addProperty(ApiInfo.kInReplyToValue, String.valueOf(mParams.getReplySourceTweetIterator()));
				element.addProperty(ApiInfo.kSourceUserKey, mParams.getReplySourceTweetUsername());
			}
			if(mParams.isAnonymous())
			{
				element.addProperty(ApiInfo.kAnonymous, "TRUE");
			}
			ByteArrayOutputStream bos = new ByteArrayOutputStream();  
			BitmapDrawable drawable = mParams.getTweetImage();
			
			if(drawable != null)
			{
				Bitmap bitmap = drawable.getBitmap();
				bitmap.compress(CompressFormat.JPEG,25,bos); 
				byte[] bb = bos.toByteArray();
				String image = Base64.encodeToString(bb, 0);
				element.addProperty("image", image);
				
			}
			client.invokeApi(ApiInfo.POST_TWEET, element, new ApiJsonOperationCallback() {
				
				@Override
				public void onCompleted(JsonElement element, Exception exception,
						ServiceFilterResponse arg2) {
					if(exception == null)
					{
						Log.d("postTweet", "TweetPosted");
					}
					else
					{
						Log.e("postTweet", "TweetPost failed");
						mException = exception;
					}
					
				}
			}, true);
			
			if(mParams.isPostToTwitter() && mParams.getTwitterApp().hasAccessToken())
			{
				try {
					mParams.getTwitterApp().updateStatus(mParams.getTweetContent());
				} catch (Exception e) {
					
					e.printStackTrace();
					mException = e;
				}
			}
		
		return mException;
	}

	@Override
	protected void onPostExecute(Exception exception)
	{
		if(exception == null)
		{
			m_completioncallback.onPostTweetTaskSuccess();
		}
		else
		{
			m_completioncallback.onPostTweetTaskFailure();
		}
	}
}

package com.tweetco.asynctasks;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Type;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.microsoft.windowsazure.mobileservices.ApiJsonOperationCallback;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponse;
import com.tweetco.activities.progress.AsyncTaskEventSinks.AsyncTaskCancelCallback;
import com.tweetco.activities.progress.AsyncTaskEventSinks.UIEventSink;
import com.yagnasri.displayingbitmaps.ui.ApiInfo;
import com.yagnasri.displayingbitmaps.ui.Tweet;

public class PostTweetTask extends AsyncTask<Void, Void, Tweet> 
{
	public static interface PostTweetTaskCompletionCallback
	{
		public void onPostTweetTaskSuccess(Tweet tweet);
		public void onPostTweetTaskFailure ();
		public void onPostTweetTaskCancelled();
	}
	
	private final static String TAG = "PostTweetTask";
	private PostTweetTaskCompletionCallback m_completioncallback;
	private UIEventSink m_uicallback;
	private Context mContext;
	private PostTweetTaskParams mParams;
	private Tweet mTweet = null;
	
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
	protected Tweet doInBackground(Void... params) 
	{
		MobileServiceClient client = mParams.getClient();
			JsonObject element = new JsonObject();
			element.addProperty(ApiInfo.kTweetOwner, mParams.getUsername());
			element.addProperty(ApiInfo.kTweetContentKey, mParams.getTweetContent());
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
						Gson gson = new Gson();
						Type tweetType = new TypeToken<Tweet>(){}.getType();
						mTweet = gson.fromJson(element, tweetType);
					}
					else
					{
						Log.e("postTweet", "TweetPost failed");
					}
					
				}
			}, true);
		
		return mTweet;
	}

	@Override
	protected void onPostExecute(Tweet tweet)
	{
		if(tweet != null)
		{
			m_completioncallback.onPostTweetTaskSuccess(tweet);
		}
		else
		{
			m_completioncallback.onPostTweetTaskFailure();
		}
	}
}

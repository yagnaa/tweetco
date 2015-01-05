package com.tweetco.asynctasks;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.methods.HttpGet;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.microsoft.windowsazure.mobileservices.ApiJsonOperationCallback;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponse;
import com.tweetco.activities.ApiInfo;
import com.tweetco.activities.progress.AsyncTaskEventSinks.AsyncTaskCancelCallback;
import com.tweetco.activities.progress.AsyncTaskEventSinks.UIEventSink;
import com.tweetco.dao.LeaderboardUser;
import com.tweetco.tweets.TweetCommonData;

public class GetLeaderboardTask extends AsyncTask <Void, Void, List<LeaderboardUser>>
{
	public static interface GetLeaderboardTaskCompletionCallback
	{
		public void onGetLeaderboardTaskSuccess(List<LeaderboardUser> users);
		public void onGetLeaderboardTaskFailure ();
		public void onGetLeaderboardTaskCancelled();
	}

	private static String TAG = "GetLeaderboardTask";

	private GetLeaderboardTaskCompletionCallback m_completioncallback;
	private UIEventSink m_uicallback;
	private Activity mActivity;
	private List<LeaderboardUser> result = null;


	public GetLeaderboardTask(Activity activity, UIEventSink uiCallback, GetLeaderboardTaskCompletionCallback callback)
	{
		mActivity = activity;
		m_uicallback = uiCallback;
		m_completioncallback = callback;
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
					m_completioncallback.onGetLeaderboardTaskCancelled();
				}
			}, true);
		}
	}

	@Override
	protected List<LeaderboardUser> doInBackground(Void... arg0)
	{

		MobileServiceClient mobileServiceClient = TweetCommonData.mClient;
		mobileServiceClient.invokeApi(ApiInfo.LEADERBOARD, HttpGet.METHOD_NAME, new ArrayList<Pair<String, String>>(), new ApiJsonOperationCallback() 
		{
			@Override
			public void onCompleted(JsonElement arg0, Exception arg1, ServiceFilterResponse arg2) 
			{					
				if(arg1 == null)
				{
					try
					{
						Gson gson = new Gson();
						Type collectionType = new TypeToken<List<LeaderboardUser>>(){}.getType();
						result = gson.fromJson(arg0, collectionType);
					}
					//TODO need to add this exception in all other places too.
					catch(JsonSyntaxException exception)
					{
						exception.printStackTrace();
						Log.e("TweetUserRunnable", "unable to parse tweetUser") ;
					}

				}
				else
				{
					Log.e("Item clicked","Exception fetching tweets received") ;
				}

			}
		}, true);


		return result;

	}

	@Override
	protected void onPostExecute(List<LeaderboardUser> result)
	{
		if(result != null)
		{
			m_completioncallback.onGetLeaderboardTaskSuccess(result);
		}
		else
		{
			m_completioncallback.onGetLeaderboardTaskFailure();
		}
	}

}

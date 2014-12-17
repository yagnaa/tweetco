package com.tweetco.asynctasks;

import java.net.MalformedURLException;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.microsoft.windowsazure.mobileservices.ApiJsonOperationCallback;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponse;
import com.tweetco.activities.progress.AsyncTaskEventSinks.AsyncTaskCancelCallback;
import com.tweetco.activities.progress.AsyncTaskEventSinks.UIEventSink;
import com.tweetco.asynctasks.AddAccountTask.AddAccountTaskCompletionCallback;
import com.tweetco.provider.TweetCoProviderConstants;
import com.tweetco.utility.ClientHelper;
import com.yagnasri.dao.LeaderboardUser;
import com.yagnasri.dao.TweetUser;

public class GetLeaderboardTask extends AsyncTask <Void, Void, LeaderboardUser[]>
{
	public static interface GetLeaderboardTaskCompletionCallback
	{
		public void onGetLeaderboardTaskSuccess(LeaderboardUser[] users);
		public void onGetLeaderboardTaskFailure ();
		public void onGetLeaderboardTaskCancelled();
	}

	private static String TAG = "GetLeaderboardTask";

	private GetLeaderboardTaskCompletionCallback m_completioncallback;
	private UIEventSink m_uicallback;
	private Activity mActivity;
	LeaderboardUser[] result;
	
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
	protected LeaderboardUser[] doInBackground(Void... arg0)
	{
		try 
		{
			MobileServiceClient mobileServiceClient = ClientHelper.getMobileClient(mActivity);
		
			mobileServiceClient.invokeApi("getleaderboard", "GET", new ArrayList<Pair<String, String>>(), new ApiJsonOperationCallback() 
			{
				@Override
				public void onCompleted(JsonElement arg0, Exception arg1,
						ServiceFilterResponse arg2) {
					if(arg1 == null)
					{
						Gson gson = new Gson();

						try
						{
							result = gson.fromJson(arg0, LeaderboardUser[].class);
							
							
							
						}
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
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
		
	}

	@Override
	protected void onPostExecute(LeaderboardUser[] result)
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

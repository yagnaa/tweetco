package com.tweetco.asynctasks;

import java.util.ArrayList;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.microsoft.windowsazure.mobileservices.ApiJsonOperationCallback;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponse;
import com.tweetco.activities.ApiInfo;
import com.tweetco.activities.progress.AsyncTaskEventSinks.AsyncTaskCancelCallback;
import com.tweetco.activities.progress.AsyncTaskEventSinks.UIEventSink;
import com.tweetco.dao.TweetUser;
import com.tweetco.database.dao.Account;
import com.tweetco.provider.TweetCoProviderConstants;

public class AddAccountTask extends AsyncTask<Void, Void, Uri> 
{
	public static interface AddAccountTaskCompletionCallback
	{
		public void onAddAccountTaskSuccess(Uri accountUri);
		public void onAddAccountTaskFailure ();
		public void onAccountCreationCancelled();
	}
	private static String TAG = "AddAccountTask";
	private static String CREATE_USER_API = "CreateUser";
	private static String FOLLOW_USER_API = "followuser";
	
	private AddAccountTaskCompletionCallback m_completioncallback;
	private UIEventSink m_uicallback;
	private Context mContext;
	private AddAccountTaskParams mParams;
	private Account mAccount = null;

	public AddAccountTask (Context context, AddAccountTaskParams params, UIEventSink uicallback, AddAccountTaskCompletionCallback completioncallback)
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
					m_completioncallback.onAccountCreationCancelled();
				}
			}, true);
		}
	}


	@Override
	protected Uri doInBackground(Void... arg0)
	{
		final MobileServiceClient client = mParams.getClient();
		client.invokeApi(CREATE_USER_API, "POST", new ArrayList<Pair<String, String>>(), new ApiJsonOperationCallback() 
		{

			@Override
			public void onCompleted(JsonElement user, Exception exception,
					ServiceFilterResponse arg2) {
				if(exception != null)
				{
					Log.e(TAG, "Get identitiy failed");
				}
				else
				{
					Log.d(TAG, "Get identitiy success");

					JsonArray userArray = user.getAsJsonArray();
					if(userArray != null && userArray.size() > 0)
					{
						JsonObject userObj = userArray.get(0).getAsJsonObject();
						String username = null;
						if(userObj.has("username"))
						{
							username = userObj.get("username").getAsString();
						}
						else
						{
							username = client.getCurrentUser().getUserId();
						}
						mAccount = new Account();
						mAccount.setUsername(username);
						mAccount.setAuthToken(client.getCurrentUser().getAuthenticationToken());
						
					}

				}
			}
		}, true);
		
		JsonObject obj = new JsonObject();
		obj.addProperty(ApiInfo.kApiRequesterKey, mAccount.getUsername());
		client.invokeApi(ApiInfo.GET_USER_INFO, obj, new ApiJsonOperationCallback() {

			@Override
			public void onCompleted(JsonElement arg0, Exception arg1,
					ServiceFilterResponse arg2) {
				if(arg1 == null)
				{
					Gson gson = new Gson();

					try
					{
						TweetUser[] tweetUser = gson.fromJson(arg0, TweetUser[].class);
						
						mAccount.followers = tweetUser[0].followers;
						mAccount.followees = tweetUser[0].followees;
						mAccount.profileimageurl = tweetUser[0].profileimageurl;
						mAccount.profilebgurl = tweetUser[0].profilebgurl;
						mAccount.bookmarkedtweets = tweetUser[0].bookmarkedtweets;
						mAccount.interesttags = tweetUser[0].interesttags;
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
		},true);
		
		return mContext.getContentResolver().insert(TweetCoProviderConstants.ACCOUNT_CONTENT_URI, mAccount.toContentValues());
	}

	@Override
	protected void onPostExecute(Uri result)
	{
		if(result != null)
		{
			m_completioncallback.onAddAccountTaskSuccess(result);
		}
		else
		{
			m_completioncallback.onAddAccountTaskFailure();
		}
	}

}

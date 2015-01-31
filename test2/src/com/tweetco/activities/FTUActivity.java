package com.tweetco.activities;


import java.net.MalformedURLException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.microsoft.windowsazure.mobileservices.ApiJsonOperationCallback;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponse;
import com.onefortybytes.R;
import com.tweetco.TweetCo;
import com.tweetco.activities.progress.AsyncTaskEventHandler;
import com.tweetco.activities.progress.AsyncTaskEventSinks.AsyncTaskCancelCallback;
import com.tweetco.activities.progress.AsyncTaskEventSinks.UIEventSink;
import com.tweetco.dao.TweetUser;
import com.tweetco.tweets.TweetCommonData;
import com.tweetco.utility.AlertDialogUtility;
import com.tweetco.utility.UiUtility;

public class FTUActivity extends ActionBarActivity
{
	
	private EditText mEmailAddress = null;
	private Button mContinue = null;
	AsyncTaskEventHandler asyncTaskEventHandler = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ftu);
		
		mEmailAddress = UiUtility.getView(this, R.id.FTUEmailAddress);
		mContinue = UiUtility.getView(this, R.id.FTULoginButton);
		asyncTaskEventHandler = new AsyncTaskEventHandler(this, "Fetching details");
		
		mContinue.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) 
			{
				final String emailAddress = mEmailAddress.getText().toString();
				if(!TextUtils.isEmpty(emailAddress))
				{
					try 
					{
						MobileServiceClient mobileServiceClient = new MobileServiceClient(TweetCo.APP_URL, TweetCo.APP_KEY, FTUActivity.this.getApplicationContext());
						new FetchUserInfoTask(getApplicationContext(), mobileServiceClient, asyncTaskEventHandler, new FetchUserInfoCompletionCallback() {
							
							@Override
							public void onFetchUserInfoTaskSuccess(TweetUser user, MobileServiceClient mobileClient) 
							{
								Log.d("FetchUserInfo", "User fetched");
								asyncTaskEventHandler.dismiss();
								TweetCommonData.mClient = mobileClient;
								Intent intent = new Intent(FTUActivity.this, FTUNewUserActivity.class);
								intent.putExtra("email", user.email);
								intent.putExtra("displayName", user.displayname);
								intent.putExtra("userName", user.username);
								intent.putExtra("password", user.password);
								startActivity(intent);
								finish();
								
							}
							
							@Override
							public void onFetchUserInfoTaskFailure(MobileServiceClient mobileClient, Exception exception) 
							{
								if(exception == null)
								{
									Log.d("FetchUserInfo", "User does not exist");
									asyncTaskEventHandler.dismiss();
									TweetCommonData.mClient = mobileClient;
									Intent intent = new Intent(FTUActivity.this, FTUNewUserActivity.class);
									intent.putExtra("email", emailAddress);
									startActivity(intent);
									finish();
								}
								else
								{
									getAddAccountFailedDialog("Failed to add account").show();
								}
							}
							
							@Override
							public void onFetchUserInfoCancelled() {
								asyncTaskEventHandler.dismiss();
								
							}
						}).execute();
					} catch (MalformedURLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
				else
				{
					getAddAccountFailedDialog("Please enter an email address").show();
				}
			}
		});
		
	}
	
	public static interface FetchUserInfoCompletionCallback
	{
		public void onFetchUserInfoTaskSuccess(TweetUser user, MobileServiceClient client);
		public void onFetchUserInfoTaskFailure (MobileServiceClient client, Exception mobileServiceException);
		public void onFetchUserInfoCancelled();
	}
	
	public class FetchUserInfoTask extends AsyncTask<Void, Void, TweetUser> 
	{
		
		private FetchUserInfoCompletionCallback m_completioncallback;
		private UIEventSink m_uicallback;
		private Context mContext;
		private MobileServiceClient mMobileClient;
		private TweetUser mTweetUser = null;
		private boolean mRetryFetchUserInfo = false;
		private Exception mMobileServiceException = null;

		public FetchUserInfoTask (Context context, MobileServiceClient mobileClient, UIEventSink uicallback, FetchUserInfoCompletionCallback completioncallback)
		{
			m_completioncallback = completioncallback;
			m_uicallback = uicallback; 
			mContext = context;
			mMobileClient = mobileClient;
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
						m_completioncallback.onFetchUserInfoCancelled();
					}
				}, true);
			}
		}


		@Override
		protected TweetUser doInBackground(Void... arg0)
		{
			final String TAG = "FetchUserInfoTask";
			final MobileServiceClient client = mMobileClient;
			JsonObject element = new JsonObject();
			//TODO Check if the input is email address
			element.addProperty(ApiInfo.kEmail, mEmailAddress.getText().toString());
			client.invokeApi("UserExists", element,new ApiJsonOperationCallback() 
			{

				@Override
				public void onCompleted(JsonElement user, Exception exception,
						ServiceFilterResponse arg2) {
					if(exception != null)
					{
						Log.e(TAG, "Get identitiy failed");
						mMobileServiceException = exception;
						mRetryFetchUserInfo = false;
					}
					else
					{
						Log.d(TAG, "Get identitiy success");

						Gson gson = new Gson();

						try
						{
							if(user.isJsonArray())
							{
								mRetryFetchUserInfo = false;
								TweetUser[] tweetUsers = gson.fromJson(user, TweetUser[].class);
								if(tweetUsers.length > 0)
								{
									mTweetUser = tweetUsers[0];
								}
							}
							else
							{
								//This is auto-discovery for the user
								JsonObject autoDiscoveryObj = user.getAsJsonObject();
								JsonElement mobileServiceUrl = autoDiscoveryObj.get("serviceurl");
								JsonElement mobileServiceKey = autoDiscoveryObj.get("appkey");
								
								if(mobileServiceKey == null || mobileServiceUrl == null)
								{
									getAddAccountFailedDialog("Your organisation hasn't signed up for this service.").show();
								}
								else
								{
									TweetCo.APP_URL = mobileServiceUrl.getAsString();
									TweetCo.APP_KEY = mobileServiceKey.getAsString();
									mMobileClient = new MobileServiceClient(TweetCo.APP_URL, TweetCo.APP_KEY, mContext);
									//Retry with the new AppUrl and AppKey to get user info
									if(!mRetryFetchUserInfo)
									{
										mRetryFetchUserInfo = true;
									}
									else
									{
										mRetryFetchUserInfo = false;
									}
									
								}
							}
						}
						catch(Exception e)
						{
							Log.e(TAG, "Failed to get user information: "+e.getMessage());
							e.printStackTrace();
						}

					}
				}
			}, true);
			
			if(mRetryFetchUserInfo)
			{
				mTweetUser = doInBackground();
			}
			
			return mTweetUser;
		}

		@Override
		protected void onPostExecute(TweetUser result)
		{
			if(result != null)
			{
				m_completioncallback.onFetchUserInfoTaskSuccess(result, mMobileClient);
			}
			else
			{
				m_completioncallback.onFetchUserInfoTaskFailure(mMobileClient, mMobileServiceException);
			}
		}

	}
	
	private AlertDialog getAddAccountFailedDialog(String errorMessage)
	{
		AlertDialog dialog = AlertDialogUtility.getAlertDialogOK(FTUActivity.this, errorMessage, null);
		return dialog;
	}
}

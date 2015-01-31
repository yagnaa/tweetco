package com.tweetco.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.microsoft.windowsazure.mobileservices.ApiJsonOperationCallback;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponse;
import com.onefortybytes.R;
import com.tweetco.activities.progress.AsyncTaskEventHandler;
import com.tweetco.activities.progress.AsyncTaskEventSinks.AsyncTaskCancelCallback;
import com.tweetco.activities.progress.AsyncTaskEventSinks.UIEventSink;
import com.tweetco.asynctasks.AddAccountTask;
import com.tweetco.asynctasks.AddAccountTask.AddAccountTaskCompletionCallback;
import com.tweetco.asynctasks.AddAccountTaskParams;
import com.tweetco.dao.TweetUser;
import com.tweetco.database.dao.Account;
import com.tweetco.provider.TweetCoProviderConstants;
import com.tweetco.tweets.TweetCommonData;
import com.tweetco.utility.AlertDialogUtility;
import com.tweetco.utility.UiUtility;

public class FTUNewUserActivity extends ActionBarActivity 
{
	private EditText mEmailAddress;
	private EditText mDisplayName;
	private EditText mUsername;
	private EditText mPassword;

	private String mPasswordFromServer;
	private boolean bActvityDestroyed = false;

	private Button mContinue = null;
	AsyncTaskEventHandler asyncTaskEventHandler = null;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.newuserftu);

		mEmailAddress = UiUtility.getView(this, R.id.FTUEmailAddress);
		mDisplayName = UiUtility.getView(this, R.id.FTUDisplayName);
		mUsername = UiUtility.getView(this, R.id.FTUUserName);
		mPassword = UiUtility.getView(this, R.id.FTUPassword);

		Intent intent = getIntent();
		String emailAddress = intent.getStringExtra("email");
		if(!TextUtils.isEmpty(emailAddress))
		{
			mEmailAddress.setText(emailAddress);
			mEmailAddress.setEnabled(false);
		}

		String displayName = intent.getStringExtra("displayName");
		if(!TextUtils.isEmpty(displayName))
		{
			mDisplayName.setText(displayName);
			mDisplayName.setEnabled(false);
		}

		String username = intent.getStringExtra("userName");
		if(!TextUtils.isEmpty(username))
		{
			mUsername.setText(username);
			mUsername.setEnabled(false);
		}

		mPasswordFromServer = intent.getStringExtra("password");
		asyncTaskEventHandler = new AsyncTaskEventHandler(this, "Adding account");
		mContinue = UiUtility.getView(this, R.id.FTULoginButton);
		
		mContinue.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
				String emailAddress = mEmailAddress.getText().toString();
				if(!TextUtils.isEmpty(emailAddress))
				{
					String displayName = mDisplayName.getText().toString();
					if(!TextUtils.isEmpty(displayName))
					{
						String username = mUsername.getText().toString();
						if(!TextUtils.isEmpty(username) && username.indexOf(" ") == -1 )
						{
							String password = mPassword.getText().toString();
							if(!TextUtils.isEmpty(password))
							{
								if(!TextUtils.isEmpty(mPasswordFromServer))
								{
									if(mPasswordFromServer.equals(password))
									{
										TweetUser tweetUser = new TweetUser();
										tweetUser.email = emailAddress;
										tweetUser.username = username;
										tweetUser.displayname = displayName;
										tweetUser.password = password;
										
										new AddNewUserTask(getApplicationContext(), TweetCommonData.mClient, tweetUser, asyncTaskEventHandler, new AddNewUserTaskCompletionCallback() {
											
											@Override
											public void onAddNewUserTaskSuccess(Uri uri) 
											{
												asyncTaskEventHandler.dismiss();

												new Thread(new Runnable() {

													@Override
													public void run() 
													{
														if(!FTUNewUserActivity.this.bActvityDestroyed)
														{
															Account account = null;
															Cursor c = getContentResolver().query(TweetCoProviderConstants.ACCOUNT_CONTENT_URI, null, null, null, null);
															if(c.moveToFirst())
															{
																account = new Account();
																account.restoreFromCursor(c);
															}

															if(account != null)
															{
																TweetCommonData.setAccount(account);
																
																startActivity(new Intent(getApplicationContext(), AllInOneActivity.class));
																
																finish();
															}
														}
													}
												}).start();
											}
											
											@Override
											public void onAddNewUserTaskFailure() 
											{
												asyncTaskEventHandler.dismiss();
												getAddAccountFailedDialog("Adding new user failed").show();
											}
											
											@Override
											public void onAddNewUserTaskCancelled() {
												asyncTaskEventHandler.dismiss();
												
											}
										}).execute();
									}
									else
									{
										getAddAccountFailedDialog("Given password does not match with the password on server").show();
									}
								}
								else
								{
									TweetUser tweetUser = new TweetUser();
									tweetUser.email = emailAddress;
									tweetUser.username = username;
									tweetUser.displayname = displayName;
									tweetUser.password = password;
									
									new AddNewUserTask(getApplicationContext(), TweetCommonData.mClient, tweetUser, asyncTaskEventHandler, new AddNewUserTaskCompletionCallback() {
										
										@Override
										public void onAddNewUserTaskSuccess(Uri uri) 
										{
											asyncTaskEventHandler.dismiss();

											new Thread(new Runnable() {

												@Override
												public void run() 
												{
													if(!FTUNewUserActivity.this.bActvityDestroyed)
													{
														Account account = null;
														Cursor c = getContentResolver().query(TweetCoProviderConstants.ACCOUNT_CONTENT_URI, null, null, null, null);
														if(c.moveToFirst())
														{
															account = new Account();
															account.restoreFromCursor(c);
														}

														if(account != null)
														{
															TweetCommonData.setAccount(account);
															
															startActivity(new Intent(getApplicationContext(), AllInOneActivity.class));
															
															finish();
														}
													}
												}
											}).start();
										}
										
										@Override
										public void onAddNewUserTaskFailure() 
										{
											asyncTaskEventHandler.dismiss();
											getAddAccountFailedDialog("Adding new user failed").show();
										}
										
										@Override
										public void onAddNewUserTaskCancelled() {
											asyncTaskEventHandler.dismiss();
											
										}
									}).execute();
									
								}
							}
							else
							{
								getAddAccountFailedDialog("Password cannot be empty").show();
							}
						}
						else
						{
							getAddAccountFailedDialog("Username cannot be empty and it cannot have more than one word.").show();
						}
					}
					else
					{
						getAddAccountFailedDialog("Display name cannot be empty").show();
					}
				}
				else
				{
					getAddAccountFailedDialog("Email address cannot be empty").show();
				}
				
				
			}
		});
	}
	
	@Override
	protected void onDestroy()
	{
		bActvityDestroyed = true;
		super.onDestroy();
	}
	
	private AlertDialog getAddAccountFailedDialog(String errorMessage)
	{
		AlertDialog dialog = AlertDialogUtility.getAlertDialogOK(FTUNewUserActivity.this, errorMessage, null);
		return dialog;
	}
	
	public static interface AddNewUserTaskCompletionCallback
	{
		public void onAddNewUserTaskSuccess(Uri uri);
		public void onAddNewUserTaskFailure ();
		public void onAddNewUserTaskCancelled();
	}
	
	public class AddNewUserTask extends AsyncTask<Void, Void, Uri> 
	{
		private AddNewUserTaskCompletionCallback m_completioncallback;
		private UIEventSink m_uicallback;
		private Context mContext;
		private MobileServiceClient mMobileClient;
		private TweetUser mTweetUser = null;
		private Account mAccount = null;

		public AddNewUserTask (Context context, MobileServiceClient mobileClient, TweetUser tweetUser, UIEventSink uicallback, AddNewUserTaskCompletionCallback completioncallback)
		{
			m_completioncallback = completioncallback;
			m_uicallback = uicallback; 
			mContext = context;
			mMobileClient = mobileClient;
			mTweetUser = tweetUser;
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
						m_completioncallback.onAddNewUserTaskCancelled();
					}
				}, true);
			}
		}


		@Override
		protected Uri doInBackground(Void... arg0)
		{
			final String TAG = "FetchUserInfoTask";
			final MobileServiceClient client = mMobileClient;
			JsonObject element = new JsonObject();
			//TODO Check if the input is email address
			element.addProperty(ApiInfo.kEmail, mTweetUser.email);
			element.addProperty(ApiInfo.kApiRequesterKey, mTweetUser.username);
			element.addProperty(ApiInfo.kPassword, mTweetUser.password);
			element.addProperty(ApiInfo.kDisplayName, mTweetUser.displayname);
			client.invokeApi("UserCreateNew", element,new ApiJsonOperationCallback() 
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

						JsonObject obj = new JsonObject();
						obj.addProperty(ApiInfo.kApiRequesterKey, mTweetUser.username);
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
										mAccount = new Account();
										mAccount.setUsername(tweetUser[0].username);
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
						

					}
				}
			}, true);
			
			return mContext.getContentResolver().insert(TweetCoProviderConstants.ACCOUNT_CONTENT_URI, mAccount.toContentValues());
		}

		@Override
		protected void onPostExecute(Uri uri)
		{
			if(uri != null)
			{
				m_completioncallback.onAddNewUserTaskSuccess(uri);
			}
			else
			{
				m_completioncallback.onAddNewUserTaskFailure();
			}
		}

	}
}

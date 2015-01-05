package com.tweetco.activities;

import java.net.MalformedURLException;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.microsoft.windowsazure.mobileservices.MobileServiceAuthenticationProvider;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.MobileServiceUser;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.UserAuthenticationCallback;
import com.tweetco.R;
import com.tweetco.TweetCo;
import com.tweetco.activities.progress.AsyncTaskEventHandler;
import com.tweetco.asynctasks.AddAccountTask;
import com.tweetco.asynctasks.AddAccountTask.AddAccountTaskCompletionCallback;
import com.tweetco.asynctasks.AddAccountTaskParams;
import com.tweetco.database.dao.Account;
import com.tweetco.provider.TweetCoProviderConstants;
import com.tweetco.tweets.TweetCommonData;
import com.tweetco.utility.AlertDialogUtility;
import com.tweetco.utility.UiUtility;
import com.yagnasri.displayingbitmaps.ui.AllInOneActivity;

public class ServiceDirectedLoginFTUActivity extends TweetCoBaseActivity 
{
	private static final String TAG = "ServiceDirectedLoginFTUActivity";
	private static String CREATE_USER_API = "CreateUser";

	AsyncTaskEventHandler asyncTaskEventHandler = null;
	private boolean bActvityDestroyed = false;
	private Button mRetryButton = null;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.servicebasedloginftulayout);
		mRetryButton = UiUtility.getView(this, R.id.retryButton);
		mRetryButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				authenticate();

			}
		});
		asyncTaskEventHandler = new AsyncTaskEventHandler(this, "Adding account");
		authenticate();
	}

	@Override
	protected void onDestroy()
	{
		bActvityDestroyed = true;
		super.onDestroy();
	}

	private void authenticate()
	{
		try {
			final MobileServiceClient mobileServiceClient = new MobileServiceClient(TweetCo.APP_URL, TweetCo.APP_KEY, this);
			mobileServiceClient.login(MobileServiceAuthenticationProvider.WindowsAzureActiveDirectory, new UserAuthenticationCallback() {

				@Override
				public void onCompleted(final MobileServiceUser userLogin, Exception exception, ServiceFilterResponse response) 
				{
					if(exception == null)
					{
						new AddAccountTask(getApplicationContext(), new AddAccountTaskParams(mobileServiceClient), asyncTaskEventHandler, new AddAccountTaskCompletionCallback() {

							@Override
							public void onAddAccountTaskSuccess(Uri accountUri) 
							{
								asyncTaskEventHandler.dismiss();
								
								TweetCommonData.mClient = mobileServiceClient;

								new Thread(new Runnable() {

									@Override
									public void run() 
									{
										if(!bActvityDestroyed)
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
											}
										}
									}
								}).start();
							}

							@Override
							public void onAddAccountTaskFailure() 
							{
								asyncTaskEventHandler.dismiss();
								Log.e(TAG, "Add account failed");
								getAddAccountFailedDialog("Add account failed.").show();
							}

							@Override
							public void onAccountCreationCancelled() 
							{
								asyncTaskEventHandler.dismiss();
								finish();
							}
						}).execute();
					}
					else
					{
						Log.e(TAG, "Login error: "+exception.getMessage());
				//		getAddAccountFailedDialog("Authentication failed during adding account.").show();
					}
				}
			});
		} 
		catch (MalformedURLException e) 
		{
			e.printStackTrace();
			Toast.makeText(this, "Login Failed", Toast.LENGTH_LONG).show();
			mRetryButton.setVisibility(View.VISIBLE);
		}
	}

	private AlertDialog getAddAccountFailedDialog(String errorMessage)
	{
		AlertDialog dialog = AlertDialogUtility.getAlertDialogOK(getApplicationContext(), errorMessage, null);

		return dialog;
	}
}

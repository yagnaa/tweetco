package com.tweetco.activities;

import java.net.MalformedURLException;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.microsoft.windowsazure.mobileservices.ApiJsonOperationCallback;
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
import com.tweetco.utility.AlertDialogUtility;
import com.tweetco.utility.UiUtility;

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
						mobileServiceClient.invokeApi(CREATE_USER_API, "POST", new ArrayList<Pair<String, String>>(), new ApiJsonOperationCallback() 
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
											String upn = userObj.get("username").getAsString();
											if(!TextUtils.isEmpty(upn) && upn.indexOf("@") != -1)
											{
												username = upn.substring(0, upn.indexOf("@"));
											}
											else
											{
												username = mobileServiceClient.getCurrentUser().getUserId();
											}
										}
										else
										{
											username = mobileServiceClient.getCurrentUser().getUserId();
										}
										Account account = new Account();
										account.setUsername(username);
										account.setAuthToken(mobileServiceClient.getCurrentUser().getAuthenticationToken());

										new AddAccountTask(getApplicationContext(), new AddAccountTaskParams(account), asyncTaskEventHandler, new AddAccountTaskCompletionCallback() {

											@Override
											public void onAddAccountTaskSuccess(Uri accountUri) 
											{
												asyncTaskEventHandler.dismiss();

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
																TweetCo.setAccount(account);
																
																startActivity(new Intent(getApplicationContext(), PostTweetActivity.class));
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

								}
							}
						}, false);
					}
					else
					{
						Log.e(TAG, "Login error: "+exception.getMessage());
						getAddAccountFailedDialog("Authentication failed during adding account.").show();
					}
				}
			});
		} 
		catch (MalformedURLException e) 
		{
			e.printStackTrace();
			Log.e(TAG, "Login error: "+e.getMessage());

			getAddAccountFailedDialog("Add account failed.").show();
		}
	}

	private AlertDialog getAddAccountFailedDialog(String errorMessage)
	{
		AlertDialog dialog = AlertDialogUtility.getAlertDialogOK(getApplicationContext(), errorMessage, null);

		return dialog;
	}
}

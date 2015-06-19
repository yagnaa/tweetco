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
import com.tweetco.Exceptions.OrgNotSignedUpException;
import com.tweetco.TweetCo;
import com.tweetco.activities.progress.AsyncTaskEventHandler;
import com.tweetco.activities.progress.AsyncTaskEventSinks.AsyncTaskCancelCallback;
import com.tweetco.activities.progress.AsyncTaskEventSinks.UIEventSink;
import com.tweetco.clients.AutoDiscoverClient;
import com.tweetco.dao.TweetUser;
import com.tweetco.tweets.TweetCommonData;
import com.tweetco.utility.AlertDialogUtility;
import com.tweetco.utility.UiUtility;

public class FTUActivity extends ActionBarActivity
{
	
	private EditText mEmailAddress = null;
	private Button mContinue = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ftu);
		
		mEmailAddress = UiUtility.getView(this, R.id.FTUEmailAddress);
		mContinue = UiUtility.getView(this, R.id.FTULoginButton);

		
		mContinue.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) 
			{
				final String emailAddress = mEmailAddress.getText().toString();
				if(!TextUtils.isEmpty(emailAddress)) {
					new AsyncTask<String, Void, AutoDiscoverClient.AutoDiscoverResult>() {

						AsyncTaskEventHandler asyncTaskEventHandler = new AsyncTaskEventHandler(FTUActivity.this, "Fetching details");

						@Override
						protected void onPreExecute() {
							asyncTaskEventHandler.onAysncTaskPreExecute(this, new AsyncTaskCancelCallback() {
								@Override
								public void onCancelled() {
									cancel(true);
								}
							}, true);
						}

						@Override
						protected AutoDiscoverClient.AutoDiscoverResult doInBackground(String... params) {
							String emailAddress = (String)params[0];
							try {
								AutoDiscoverClient.AutoDiscoverResult result = new AutoDiscoverClient().discoverUser(emailAddress);
								return result;
							}
							catch (Exception e)
							{

							}
							return null;
						}

						@Override
						protected void onPostExecute(AutoDiscoverClient.AutoDiscoverResult param) {
							asyncTaskEventHandler.dismiss();

							if(param != null && param.tweetUser != null) {
								Intent intent = new Intent(FTUActivity.this, FTUNewUserActivity.class);
								intent.putExtra("email", param.tweetUser.email);
								intent.putExtra("displayName", param.tweetUser.displayname);
								intent.putExtra("userName", param.tweetUser.username);
								intent.putExtra("password", param.tweetUser.password);
								intent.putExtra("serverAddress", param.serverUrl);
								intent.putExtra("authToken", param.authToken);
								startActivity(intent);
								finish();
							}
							else {
								if(param != null && param.exception != null && param.exception instanceof OrgNotSignedUpException) {
									getAddAccountFailedDialog("Your organisation hasn't signed up for this service.").show();
								}
								else {
									getAddAccountFailedDialog("Failed to add account").show();
								}
							}
						}
					}.execute(emailAddress);
				}
				else
				{
					getAddAccountFailedDialog("Please enter an email address").show();
				}
			}
		});
		
	}

	private AlertDialog getAddAccountFailedDialog(String errorMessage)
	{
		AlertDialog dialog = AlertDialogUtility.getAlertDialogOK(FTUActivity.this, errorMessage, null);
		return dialog;
	}
}

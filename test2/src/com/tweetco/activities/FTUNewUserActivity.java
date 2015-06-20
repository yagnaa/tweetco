package com.tweetco.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.onefortybytes.R;
import com.tweetco.datastore.AccountSingleton;
import com.tweetco.activities.progress.AsyncTaskEventHandler;
import com.tweetco.activities.progress.AsyncTaskEventSinks.AsyncTaskCancelCallback;
import com.tweetco.utility.AlertDialogUtility;
import com.tweetco.utility.UiUtility;

public class FTUNewUserActivity extends ActionBarActivity 
{
	private EditText mEmailAddress;
	private EditText mDisplayName;
	private EditText mUsername;
	private EditText mPassword;

	private String mPasswordFromServer;
	private String mServerAddress;
	private String mAuthToken;

	private Button mContinue = null;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.newuserftu);

		mEmailAddress = UiUtility.getView(this, R.id.FTUEmailAddress);
		mDisplayName = UiUtility.getView(this, R.id.FTUDisplayName);
		mUsername = UiUtility.getView(this, R.id.FTUUserName);
		mPassword = UiUtility.getView(this, R.id.FTUPassword);
		mContinue = UiUtility.getView(this, R.id.FTULoginButton);

		Intent intent = getIntent();
		if(intent != null)
		{
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
			mServerAddress = intent.getStringExtra("serverAddress");
			mAuthToken = intent.getStringExtra("authToken");
		}

		mContinue.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String emailAddress = mEmailAddress.getText().toString();
				if (!TextUtils.isEmpty(emailAddress)) {
					String displayName = mDisplayName.getText().toString();
					if (!TextUtils.isEmpty(displayName)) {
						String username = mUsername.getText().toString();
						if (!TextUtils.isEmpty(username) && username.indexOf(" ") == -1) {
							String password = mPassword.getText().toString();
							if (!TextUtils.isEmpty(password)) {
								if (!TextUtils.isEmpty(mPasswordFromServer)) {
									if (mPasswordFromServer.equals(password)) {
										addNewUser();
									} else {
										getAddAccountFailedDialog("Given password does not match with the password on server").show();
									}
								} else {
									addNewUser();

								}
							} else {
								getAddAccountFailedDialog("Password cannot be empty").show();
							}
						} else {
							getAddAccountFailedDialog("Username cannot be empty and it cannot have more than one word.").show();
						}
					} else {
						getAddAccountFailedDialog("Display name cannot be empty").show();
					}
				} else {
					getAddAccountFailedDialog("Email address cannot be empty").show();
				}


			}
		});
	}

	private void addNewUser()
	{
		new AsyncTask<String, Void, Void>()
		{
			AsyncTaskEventHandler asyncTaskEventHandler = new AsyncTaskEventHandler(FTUNewUserActivity.this, "Adding account");;
			@Override
			protected Void doInBackground(String... params) {
				AccountSingleton.INSTANCE.getAccountModel().insertAccountFromServer(params[0], params[1], params[2]);
				return null;
			}

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
			protected void onPostExecute(Void aVoid) {
				startActivity(new Intent(getApplicationContext(), AllInOneActivity.class));

				finish();
			}
		}.execute(new String[]{mServerAddress, mUsername.getText().toString(), mAuthToken});
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		// Always call the superclass so it can save the view hierarchy state
		super.onSaveInstanceState(savedInstanceState);

		// Save the user's current game state
		savedInstanceState.putString("serverAddress", mServerAddress);
		savedInstanceState.putString("authToken", mAuthToken);
		savedInstanceState.putString("passwordFromServer", mPasswordFromServer);

	}

	public void onRestoreInstanceState(Bundle savedInstanceState) {
		// Always call the superclass so it can restore the view hierarchy
		super.onRestoreInstanceState(savedInstanceState);

		// Restore state members from saved instance
		mServerAddress = savedInstanceState.getString("serverAddress");
		mAuthToken = savedInstanceState.getString("authToken");
		mPasswordFromServer = savedInstanceState.getString("passwordFromServer");
	}
	
	private AlertDialog getAddAccountFailedDialog(String errorMessage)
	{
		AlertDialog dialog = AlertDialogUtility.getAlertDialogOK(FTUNewUserActivity.this, errorMessage, null);
		return dialog;
	}
	
}

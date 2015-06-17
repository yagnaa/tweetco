package com.tweetco.activities;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Toast;

import com.imagedisplay.util.ImageFetcher;
import com.imagedisplay.util.Utils;
import com.onefortybytes.R;
import com.tweetco.account.AccountSingleton;
import com.tweetco.activities.progress.AsyncTaskEventHandler;
import com.tweetco.activities.progress.AsyncTaskEventSinks;
import com.tweetco.asynctasks.EditProfileTask;
import com.tweetco.asynctasks.EditProfileTask.EditProfileTaskCompletionCallback;
import com.tweetco.asynctasks.EditProfileTaskParams;
import com.tweetco.dao.TweetUser;
import com.tweetco.database.dao.Account;
import com.tweetco.interfaces.OnChangeListener;
import com.tweetco.models.AccountModel;
import com.tweetco.tweets.TweetCommonData;
import com.tweetco.utility.ImageUtility;
import com.tweetco.utility.UiUtility;

public class EditProfileActivity extends ActionBarActivity implements OnChangeListener<Account>
{
	private static final int REQUEST_CODE_PROFILE_PIC_IMAGE_SELECT = 100;
	private static final int REQUEST_CODE_PROFILE_PIC_IMAGE_CAPTURE = 101;
	private static final int REQUEST_CODE_HEADER_PIC_IMAGE_SELECT = 102;
	private static final int REQUEST_CODE_HEADER_PIC_IMAGE_CAPTURE = 103;
	
	private static final int PROFILE_PIC = 1;
	private static final int HEADER_PIC = 2;
	
	ImageFetcher mImageFetcher = null;
	AccountModel mAccountModel = null;

	private ImageView mProfilePic;
	private ImageView mHeaderPic;
	private EditText mWorkText;
	private MultiAutoCompleteTextView mSkillsText;
	private MultiAutoCompleteTextView mInterestsText;
	private SwipeRefreshLayout mSwipeRefreshLayout;
	
	private Uri mProfilePicUri;
	private Uri mHeaderPicUri;
	
	private boolean mProfilePicChanged = false;
	private boolean mHeaderPicChanged = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.editprofile);
		
		mProfilePic = UiUtility.getView(this, R.id.editProfileUserPic);
		mHeaderPic = UiUtility.getView(this, R.id.editProfileBackgroundPic);
		mWorkText = UiUtility.getView(this,  R.id.workText);
		mSkillsText = UiUtility.getView(this, R.id.skillsText);
		mInterestsText = UiUtility.getView(this, R.id.interestsText);
		mSwipeRefreshLayout = UiUtility.getView(this, R.id.edit_profile_swipe_refresh_layout);
		mImageFetcher = Utils.getImageFetcher(this, 60, 60);

		ActionBar actionbar = getSupportActionBar();
		if(actionbar!=null)
		{
			actionbar.setHomeButtonEnabled(true);
			actionbar.setDisplayHomeAsUpEnabled(true);
		}

		mProfilePic.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				getDialog(EditProfileActivity.this, PROFILE_PIC).show();
			}
		});


		mHeaderPic.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				getDialog(EditProfileActivity.this, HEADER_PIC).show();
			}
		});

		mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				if(mAccountModel != null)
				{
					new AsyncTask<Void, Void, Void>() {


						@Override
						protected void onPreExecute() {

						}

						@Override
						protected Void doInBackground(Void... params) {
							AccountSingleton.INSTANCE.getAccountModel().refreshAccountFromServer();
							return null;
						}

						@Override
						protected void onPostExecute(Void param) {
							mSwipeRefreshLayout.setRefreshing(false);
							refreshView(AccountSingleton.INSTANCE.getAccountModel().getAccountCopy());
						}

						@Override
						protected void onCancelled() {
							mSwipeRefreshLayout.setRefreshing(false);
						}
					}.execute();
				}
			}
		});
	}
	
	public static AlertDialog getDialog(final Activity activity, final int picCode)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setItems(new String[] {"Gallery", "Take Picture"}, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) 
			{
				int requestCode = REQUEST_CODE_PROFILE_PIC_IMAGE_SELECT;
				Intent intent = null;
				if(picCode == PROFILE_PIC)
				{
					if(which == 0)
					{
						requestCode = REQUEST_CODE_PROFILE_PIC_IMAGE_SELECT;
						intent = ImageUtility.getImageChooserIntent(activity);
					}
					else
					{
						requestCode = REQUEST_CODE_PROFILE_PIC_IMAGE_CAPTURE;
						 intent = ImageUtility.getImageCaptureIntent(activity);
					}
				}
				else
				{
					if(which == 0)
					{
						requestCode = REQUEST_CODE_HEADER_PIC_IMAGE_SELECT;
						intent = ImageUtility.getImageChooserIntent(activity);
					}
					else
					{
						requestCode = REQUEST_CODE_HEADER_PIC_IMAGE_CAPTURE;
						intent = ImageUtility.getImageCaptureIntent(activity);
					}
				}
				if(intent!=null)
				{
					activity.startActivityForResult(intent, requestCode);
				}
				else
				{
					Toast.makeText(activity.getApplicationContext(), "Your device doesn't support this", Toast.LENGTH_SHORT).show();
				}
			}
		});

		return builder.create();
	}
	
	private static Uri getFileUri(Context context, Intent data)
	{
		Uri fileUri = null;
		try
		{
			fileUri = ImageUtility.onImageAttachmentReceived(context, data);
		}
		catch (FileNotFoundException e)
		{
			Log.e("PostTweet", "onActivityResult onImageAttachmentReceived FileNotFoundException");
		}
		catch (IOException e)
		{
			Log.e("PostTweet", "onActivityResult onImageAttachmentReceived IOException");
		}
		catch (IllegalArgumentException e)
		{
			Log.e("PostTweet", "onActivityResult onImageAttachmentReceived IllegalArgumentException, "+e.getMessage());					
		}

		if(fileUri == null)
		{
			Log.e("PostTweet", "Attachment error");
		}
		
		return fileUri;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (resultCode == Activity.RESULT_OK)
		{
			if (requestCode == REQUEST_CODE_PROFILE_PIC_IMAGE_SELECT || requestCode == REQUEST_CODE_PROFILE_PIC_IMAGE_CAPTURE)
			{
				Uri fileUri = getFileUri(getApplicationContext(), data);
				mProfilePicUri = fileUri;
				mProfilePic.setImageURI(mProfilePicUri);			
				mProfilePicChanged = true;
			}
			else if (requestCode == REQUEST_CODE_HEADER_PIC_IMAGE_SELECT || requestCode == REQUEST_CODE_HEADER_PIC_IMAGE_CAPTURE)
			{
				Uri fileUri = getFileUri(getApplicationContext(), data);
				mHeaderPicUri = fileUri;
				mHeaderPic.setImageURI(mHeaderPicUri);
				mHeaderPicChanged = true;
			}
		}
		else
		{
			ImageUtility.onImageAttachmentCancelled();
			Log.i("PostTweet", "onActivityResult result code: " + resultCode + " for request code: " + requestCode);
		}
	}
	
	@Override
	public void onResume()
	{
		super.onResume();

		if(mAccountModel == null)
		{
			new AsyncTask<Void, Void, AccountModel>() {

				AsyncTaskEventHandler asyncTaskEventHandler;

				@Override
				protected void onPreExecute() {
					asyncTaskEventHandler = new AsyncTaskEventHandler(EditProfileActivity.this, "Loading...");
					asyncTaskEventHandler.onAysncTaskPreExecute(this, new AsyncTaskEventSinks.AsyncTaskCancelCallback()
					{
						@Override
						public void onCancelled()
						{
							cancel(true);
						}
					}, true);
				}

				@Override
				protected AccountModel doInBackground(Void... params) {
					return AccountSingleton.INSTANCE.getAccountModel();
				}



				@Override
				protected void onPostExecute(AccountModel accountModel) {
					EditProfileActivity.this.mAccountModel = accountModel;
					accountModel.addListener(EditProfileActivity.this);
					refreshView(accountModel.getAccountCopy());
					asyncTaskEventHandler.dismiss();
				}

				@Override
				protected void onCancelled() {
					asyncTaskEventHandler.dismiss();
					EditProfileActivity.this.finish();
				}
			}.execute();
		}
		else
		{
			mAccountModel.addListener(this);
			refreshView(mAccountModel.getAccountCopy());
		}
	}

	@Override
	public void onPause()
	{
		super.onPause();

		if(mAccountModel != null)
		{
			mAccountModel.removeListener(this);
		}
	}

	@Override
		public boolean onCreateOptionsMenu(Menu menu) {
			// Inflate the menu items for use in the action bar
					MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.editprofile_actions, menu);
			return super.onCreateOptionsMenu(menu);
		}

				@Override
		public boolean onOptionsItemSelected(MenuItem item) {
			// Handle presses on the action bar items
					switch (item.getItemId()) {
					case R.id.action_save_profile:
							saveProfile();
							return true;
					default:
							return super.onOptionsItemSelected(item);
				}
	}

	@Override
	public void onChange(final Account model) {
		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				refreshView(model);
			}
		});
	}

	private class AccountParams
	{
		public Account account;
		public BitmapDrawable profilePic;
		public BitmapDrawable bgPic;
	}

	private void saveProfile()
	{
		BitmapDrawable profilePic = null;
		BitmapDrawable bgPic = null;
		if (mProfilePicChanged) {
			profilePic = (BitmapDrawable) mProfilePic.getDrawable();
		}

		if (mHeaderPicChanged) {
			bgPic = (BitmapDrawable) mHeaderPic.getDrawable();
		}

		Account tempAccount = mAccountModel.getAccountCopy();
		tempAccount.workDetails = mWorkText.getText().toString();
		tempAccount.interesttags = GetTagsString(mInterestsText.getText().toString());
		tempAccount.skillstags = GetTagsString(mSkillsText.getText().toString());

		AccountParams params = new AccountParams();
		params.account = tempAccount;
		params.profilePic = profilePic;
		params.bgPic = bgPic;

		new AsyncTask<AccountParams, Void, Void>() {

			AsyncTaskEventHandler asyncTaskEventHandler;

			@Override
			protected void onPreExecute() {
				asyncTaskEventHandler = new AsyncTaskEventHandler(EditProfileActivity.this, "Saving...");
				asyncTaskEventHandler.onAysncTaskPreExecute(this, new AsyncTaskEventSinks.AsyncTaskCancelCallback()
				{
					@Override
					public void onCancelled()
					{
						cancel(true);
						EditProfileActivity.this.onChange(AccountSingleton.INSTANCE.getAccountModel().getAccountCopy());
					}
				}, true);
			}

			@Override
			protected Void doInBackground(AccountParams... params) {
				AccountParams par = (AccountParams)params[0];
				AccountSingleton.INSTANCE.getAccountModel().updateServer(par.account, par.profilePic, par.bgPic);
				return null;
			}



			@Override
			protected void onPostExecute(Void param) {
				asyncTaskEventHandler.dismiss();
				EditProfileActivity.this.finish();
			}

			@Override
			protected void onCancelled() {
				asyncTaskEventHandler.dismiss();
			}
		}.execute(params);

	}

	private  void refreshView(Account account)
	{
		mImageFetcher.loadImage(account.profileimageurl, mProfilePic);
		mImageFetcher.loadImage(account.profilebgurl, mHeaderPic);

		mWorkText.setText(account.workDetails);
		mSkillsText.setText(GetCommaSeparatedTags(account.skillstags));
		mInterestsText.setText(GetCommaSeparatedTags(account.interesttags));
	}

	private static String GetTagsString(String tags)
	{
		String tag = null;

		if(!TextUtils.isEmpty(tags)) {
			StringBuilder builder = new StringBuilder();
			String[] tagsArray = tags.split(",");
			for(String tempTag: tagsArray)
			{
				builder.append("#").append(tempTag);
			}
			builder.append("#");
			tag = builder.toString();
		}

		return tag;
	}

	private static String GetCommaSeparatedTags(String tags)
	{
		List<String> list = new ArrayList<String>();

		if(!TextUtils.isEmpty(tags))
		{
			String[] tagsArray = tags.split("#");
			for(String tag: tagsArray)
			{
				if(!TextUtils.isEmpty(tag))
				{
					list.add(tag);
				}
			}

			StringBuilder builder = new StringBuilder();
			for(String tag: list)
			{
				builder.append(tag).append(",");
			}

			return builder.substring(0, builder.length() -1);
		}
		else {
			return null;
		}


	}

}

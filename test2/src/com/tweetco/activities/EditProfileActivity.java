package com.tweetco.activities;

import java.io.FileNotFoundException;
import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

import com.tweetco.R;
import com.tweetco.activities.progress.AsyncTaskEventHandler;
import com.tweetco.asynctasks.EditProfileTask;
import com.tweetco.asynctasks.EditProfileTask.EditProfileTaskCompletionCallback;
import com.tweetco.asynctasks.EditProfileTaskParams;
import com.tweetco.tweets.TweetCommonData;
import com.tweetco.utility.ImageUtility;
import com.tweetco.utility.UiUtility;
import com.yagnasri.dao.Tweet;
import com.yagnasri.dao.TweetUser;

public class EditProfileActivity extends TweetCoBaseActivity 
{
	private static final int REQUEST_CODE_PROFILE_PIC_IMAGE_SELECT = 100;
	private static final int REQUEST_CODE_PROFILE_PIC_IMAGE_CAPTURE = 101;
	private static final int REQUEST_CODE_HEADER_PIC_IMAGE_SELECT = 102;
	private static final int REQUEST_CODE_HEADER_PIC_IMAGE_CAPTURE = 103;
	
	private static final int PROFILE_PIC = 1;
	private static final int HEADER_PIC = 2;
	
	
	ImageView mProfilePic;
	ImageView mHeaderPic;
	Button mSaveButton;
	AsyncTaskEventHandler asyncTaskEventHandler = null;
	boolean mProfilePicChanged = false;
	boolean mHeaderPicChanged = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.editprofile);
		
		mProfilePic = UiUtility.getView(this, R.id.editProfileUserPic);
		mHeaderPic = UiUtility.getView(this, R.id.editProfileBackgroundPic);
		mSaveButton = UiUtility.getView(this,  R.id.editProfileSaveButton);
		asyncTaskEventHandler = new AsyncTaskEventHandler(this, "Saving...");
		
		String username = TweetCommonData.getUserName();
		TweetUser user = TweetCommonData.tweetUsers.get(username);
		TweetCommonData.mImageFetcher.loadImage(user.profileimageurl, mProfilePic);
		TweetCommonData.mImageFetcher.loadImage(user.profilebgurl, mHeaderPic);
		
		
		mProfilePic.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				getDialog(EditProfileActivity.this, PROFILE_PIC).show();
			}
		});
		
		mHeaderPic.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				getDialog(EditProfileActivity.this, HEADER_PIC).show();
			}
		});
		
		mSaveButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) 
			{
				EditProfileTaskParams params = new EditProfileTaskParams(TweetCommonData.getUserName());
				if(mProfilePicChanged)
				{
					params.setProfileImage((BitmapDrawable)mProfilePic.getDrawable());
				}
				
				if(mHeaderPicChanged)
				{
					params.setProfileImage((BitmapDrawable)mHeaderPic.getDrawable());
				}
				
				new EditProfileTask(getApplicationContext(), params, asyncTaskEventHandler, new EditProfileTaskCompletionCallback() {
					
					@Override
					public void onEditProfileTaskSuccess() 
					{
						Log.d("EditProfile", "Success");
						finish();
					}
					
					@Override
					public void onEditProfileTaskFailure() {
						Log.d("EditProfile", "Failed");
						finish();
					}
					
					@Override
					public void onEditProfileTaskCancelled() {
						
					}
				}).execute();
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
				
				activity.startActivityForResult(intent, requestCode);
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
				mProfilePic.setImageURI(fileUri);
				mProfilePicChanged = true;
			}
			else if (requestCode == REQUEST_CODE_HEADER_PIC_IMAGE_SELECT || requestCode == REQUEST_CODE_HEADER_PIC_IMAGE_CAPTURE)
			{
				Uri fileUri = getFileUri(getApplicationContext(), data);
				mHeaderPic.setImageURI(fileUri);
				mHeaderPicChanged = true;
			}
		}
		else
		{
			ImageUtility.onImageAttachmentCancelled();
			Log.i("PostTweet","onActivityResult result code: " + resultCode + " for request code: " + requestCode);
		}

	}
}
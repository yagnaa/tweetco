package com.tweetco.asynctasks;

import android.graphics.drawable.BitmapDrawable;

public class EditProfileTaskParams 
{
	private String mUsername;
	private BitmapDrawable mProfileImage;
	private BitmapDrawable mHeaderImage;
	
	public EditProfileTaskParams(String username) 
	{
		mUsername = username;
	}
	

	public String getUsername() {
		return mUsername;
	}

	public BitmapDrawable getProfileImage() {
		return mProfileImage;
	}

	public void setProfileImage(BitmapDrawable profileImage) {
		this.mProfileImage = profileImage;
	}


	public BitmapDrawable getHeaderImage() {
		return mHeaderImage;
	}


	public void setHeaderImage(BitmapDrawable mHeaderImage) {
		this.mHeaderImage = mHeaderImage;
	}
}

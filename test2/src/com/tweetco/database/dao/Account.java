package com.tweetco.database.dao;

import android.content.ContentValues;
import android.database.Cursor;

public class Account extends DBContent
{
	public static final String TABLE_NAME = "Account";
	
	public static final String COLUMN_USERNAME           	= "username";
	public static final String COLUMN_PASSWORD           	= "password";
	public static final String COLUMN_SERVER_ADDRESS        = "serverAddress";
	public static final String COLUMN_AUTH_TOKEN	        = "authToken";
	public static final String COLUMN_USERID           		= "userid";
	public static final String COLUMN_DISPLAY_NAME          = "displayname";
	
	
	public static final int COLUMN_ID_INDEX                 	= 0;
	public static final int COLUMN_USERNAME_INDEX           	= 1;
	public static final int COLUMN_PASSWORD_INDEX           	= 2;
	public static final int COLUMN_SERVER_ADDRESS_INDEX         = 3;
	public static final int COLUMN_AUTH_TOKEN_INDEX           	= 4;
	public static final int COLUMN_USERID_INDEX           		= 5;
	public static final int COLUMN_DISPLAY_NAME_INDEX           = 6;
	
	
	private String	mUsername;
	private String mPassword;		//TODO Password is in plain bytes
	private String mServerAddress;
	private String mAuthToken;
	private String mUserId;
	private String mDisplayName;
	
	
	public String getUsername() {
		return mUsername;
	}
	public void setUsername(String m_username) {
		this.mUsername = m_username;
	}
	
	public String getPassword() {
		return mPassword;
	}
	public void setPassword(String mPassword) {
		this.mPassword = mPassword;
	}
	
	public void restoreFromCursor(Cursor c)
	{
		if(c != null)
		{
			setId(c.getLong(COLUMN_ID_INDEX));
			setUsername(c.getString(COLUMN_USERNAME_INDEX));
			setPassword(c.getString(COLUMN_PASSWORD_INDEX));
			setServerAddress(c.getString(COLUMN_SERVER_ADDRESS_INDEX));
			setAuthToken(c.getString(COLUMN_AUTH_TOKEN_INDEX));
			setUserId(c.getString(COLUMN_USERID_INDEX));
			setDisplayName(c.getString(COLUMN_DISPLAY_NAME_INDEX));
		}
	}
	
	public ContentValues toContentValues()
	{
		ContentValues cv = new ContentValues();
		
		cv.put(COLUMN_ID, getId());
		cv.put(COLUMN_USERNAME, getUsername());
		cv.put(COLUMN_PASSWORD, getPassword());
		cv.put(COLUMN_SERVER_ADDRESS, getServerAddress());
		cv.put(COLUMN_AUTH_TOKEN, getAuthToken());
		cv.put(COLUMN_USERID, getUserId());
		cv.put(COLUMN_DISPLAY_NAME, getDisplayName());
		
		return cv;
	}
	public String getServerAddress() {
		return mServerAddress;
	}
	public void setServerAddress(String mServerAddress) {
		this.mServerAddress = mServerAddress;
	}
	public String getAuthToken() {
		return mAuthToken;
	}
	public void setAuthToken(String mAuthToken) {
		this.mAuthToken = mAuthToken;
	}
	public String getUserId() {
		return mUserId;
	}
	public void setUserId(String mUserId) {
		this.mUserId = mUserId;
	}
	public String getDisplayName() {
		return mDisplayName;
	}
	public void setDisplayName(String mDisplayName) {
		this.mDisplayName = mDisplayName;
	}
}

package com.tweetco.database.dao;

import android.content.ContentValues;
import android.database.Cursor;

public class Account extends DBContent
{
	public static final String TABLE_NAME = "Account";
	
	public static final String COLUMN_USERNAME           		= "username";
	public static final String COLUMN_PASSWORD           		= "password";
	public static final String COLUMN_SERVER_ADDRESS        	= "serverAddress";
	public static final String COLUMN_AUTH_TOKEN	        	= "authToken";
	public static final String COLUMN_USERID           			= "userid";
	public static final String COLUMN_DISPLAY_NAME          	= "displayname";
	public static final String COLUMN_FOLLOWERS                 = "followers";
	public static final String COLUMN_FOLLOWEES                 = "followees";
	public static final String COLUMN_PROFILE_IMAGE_URL         = "profile_image_url";
	public static final String COLUMN_PROFILE_BG_URL            = "profile_bg_url";
	public static final String COLUMN_BOOKMARKED_TWEETS         = "bookmarked_tweets";
	public static final String COLUMN_INTEREST_TAGS             = "interest_tags";
	public static final String COLUMN_SKILLS_TAGS               = "skills_tags";
	public static final String COLUMN_PERSONAL_INTEREST_TAGS    = "personal_interest_tags";
	public static final String COLUMN_WORK_DETAILS              = "work_details";
	public static final String COLUMN_CONTACT_INFO				= "contact_info";



	public static final int COLUMN_ID_INDEX                 	= 0;
	public static final int COLUMN_USERNAME_INDEX           	= 1;
	public static final int COLUMN_PASSWORD_INDEX           	= 2;
	public static final int COLUMN_SERVER_ADDRESS_INDEX         = 3;
	public static final int COLUMN_AUTH_TOKEN_INDEX           	= 4;
	public static final int COLUMN_USERID_INDEX           		= 5;
	public static final int COLUMN_DISPLAY_NAME_INDEX           = 6;
	public static final int COLUMN_FOLLOWERS_INDEX              = 7;
	public static final int COLUMN_FOLLOWEES_INDEX              = 8;
	public static final int COLUMN_PROFILE_IMAGE_URL_INDEX      = 9;
	public static final int COLUMN_PROFILE_BG_URL_INDEX         = 10;
	public static final int COLUMN_BOOKMARKED_TWEETS_INDEX      = 11;
	public static final int COLUMN_INTEREST_TAGS_INDEX          = 12;
	public static final int COLUMN_SKILLS_TAGS_INDEX           	= 13;
	public static final int COLUMN_PERSONAL_INTEREST_TAGS_INDEX = 14;
	public static final int COLUMN_WORK_DETAILS_INDEX           = 15;
	public static final int COLUMN_CONTACT_INFO_INDEX           = 16;

	private String	mUsername;
	private String mPassword;		//TODO Password is in plain bytes
	private String mServerAddress;
	private String mAuthToken;
	private String mUserId;
	private String mDisplayName;
	
	public String followers;
	public String followees;
	public String profileimageurl;
	public String profilebgurl;
	public String bookmarkedtweets;
	public String interesttags;
    public String skillstags;
	public String personalInterestTags;
	public String workDetails;
	public String contactInfo;



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
			
			followers = c.getString(COLUMN_FOLLOWERS_INDEX);
			followees = c.getString(COLUMN_FOLLOWEES_INDEX);
			profileimageurl = c.getString(COLUMN_PROFILE_IMAGE_URL_INDEX);
			profilebgurl = c.getString(COLUMN_PROFILE_BG_URL_INDEX);
			bookmarkedtweets = c.getString(COLUMN_BOOKMARKED_TWEETS_INDEX);
			interesttags = c.getString(COLUMN_INTEREST_TAGS_INDEX);
            skillstags = c.getString(COLUMN_SKILLS_TAGS_INDEX);
			personalInterestTags = c.getString(COLUMN_PERSONAL_INTEREST_TAGS_INDEX);
			workDetails = c.getString(COLUMN_WORK_DETAILS_INDEX);
			contactInfo = c.getString(COLUMN_CONTACT_INFO_INDEX);


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
		
		cv.put(COLUMN_FOLLOWERS, followers);
		cv.put(COLUMN_FOLLOWEES, followees);
		cv.put(COLUMN_PROFILE_IMAGE_URL, profileimageurl);
		cv.put(COLUMN_PROFILE_BG_URL, profilebgurl);
		cv.put(COLUMN_BOOKMARKED_TWEETS, bookmarkedtweets);
		cv.put(COLUMN_INTEREST_TAGS, interesttags);
        cv.put(COLUMN_SKILLS_TAGS, skillstags);
		cv.put(COLUMN_PERSONAL_INTEREST_TAGS, personalInterestTags);
		cv.put(COLUMN_WORK_DETAILS, workDetails);
		cv.put(COLUMN_CONTACT_INFO, contactInfo);



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

    public Account getCopy()
    {
        Account account = new Account();

        account.setId(getId());
        account.setUsername(mUsername);
        account.setPassword(mPassword);
        account.setServerAddress(mServerAddress);
        account.setAuthToken(mAuthToken);
        account.setUserId(mUserId);
        account.setDisplayName(mDisplayName);

        account.followers = followers;
        account.followees = followees;
        account.profileimageurl = profileimageurl;
        account.profilebgurl = profilebgurl;
        account.bookmarkedtweets = bookmarkedtweets;
        account.interesttags = interesttags;
        account.skillstags = skillstags;
        account.personalInterestTags = personalInterestTags;
        account.workDetails = workDetails;
        account.contactInfo = contactInfo;

        return account;
    }
}

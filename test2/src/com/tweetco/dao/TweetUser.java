package com.tweetco.dao;


public class TweetUser
{
    public String Id;
	public String username;
	public String followers;
	public String followees;
	public String userid;
	public String profileimageurl;
	public String profilebgurl;
	public String bookmarkedtweets;
	public String displayname;
	public String email;
	public String password;

	public String interesttags;


	@Override
	public boolean equals(Object o) {
		boolean isEqual = false;
		if(o instanceof  TweetUser)
		{
			isEqual = username.equals(((TweetUser) o).username);
		}

		return isEqual;
	}
}

package com.tweetco.asynctasks;

import com.tweetco.database.dao.Account;

public class AddAccountTaskParams 
{
	private Account mAccount;
	
	public AddAccountTaskParams(Account account) 
	{
		mAccount = account;
	}
	
	public Account getAccount()
	{
		return mAccount;
	}
}

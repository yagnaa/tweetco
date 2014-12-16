package com.tweetco.asynctasks;

import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.tweetco.database.dao.Account;

public class AddAccountTaskParams 
{
	private MobileServiceClient mClient;
	
	public AddAccountTaskParams(MobileServiceClient client) 
	{
		mClient = client;
	}
	
	public MobileServiceClient getClient()
	{
		return mClient;
	}
}

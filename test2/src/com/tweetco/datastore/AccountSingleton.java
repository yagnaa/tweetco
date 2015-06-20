package com.tweetco.datastore;

import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.tweetco.TweetCo;
import com.tweetco.database.dao.Account;
import com.tweetco.models.AccountModel;

import java.net.MalformedURLException;

/**
 * Created by kirankum on 6/13/2015.
 */
public enum AccountSingleton {
    INSTANCE;

    private AccountModel accountModel;
    private boolean isInitialised = false;
    private Object lock = new Object();

    public AccountModel getAccountModel() {

        if(!isInitialised())
        {
            synchronized (lock)
            {
                if(!isInitialised())
                {
                    accountModel = new AccountModel();
                    setIsInitialised(true);
                }
            }
        }

        return accountModel;
    }

    public String getUserName()
    {
        return getAccountModel().getAccountCopy().getUsername();
    }

    public MobileServiceClient getMobileServiceClient() throws MalformedURLException
    {
        Account account = getAccountModel().getAccountCopy();
        return new MobileServiceClient(account.getServerAddress(), account.getAuthToken(), TweetCo.mContext);
    }

    private boolean isInitialised() {
        return isInitialised;
    }

    private void setIsInitialised(boolean isInitialised) {
        this.isInitialised = isInitialised;
    }


}

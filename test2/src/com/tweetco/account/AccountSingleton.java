package com.tweetco.account;

import android.database.Cursor;

import com.tweetco.TweetCo;
import com.tweetco.database.dao.Account;
import com.tweetco.models.AccountModel;
import com.tweetco.provider.TweetCoProviderConstants;

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

    private boolean isInitialised() {
        return isInitialised;
    }

    private void setIsInitialised(boolean isInitialised) {
        this.isInitialised = isInitialised;
    }


}

package com.tweetco.account;

import android.database.Cursor;

import com.tweetco.TweetCo;
import com.tweetco.database.dao.Account;
import com.tweetco.provider.TweetCoProviderConstants;

/**
 * Created by kirankum on 6/13/2015.
 */
public enum AccountSingleton {
    INSTANCE;

    private Account account;
    private boolean isInitialised = false;
    private Object lock = new Object();

    public Account getAccount() {

        if(!isInitialised())
        {
            synchronized (lock)
            {
                if(!isInitialised())
                {
                    account = getAccountInternal();
                    setIsInitialised(true);
                }
            }
        }

        return account;
    }

    private boolean isInitialised() {
        return isInitialised;
    }

    private void setIsInitialised(boolean isInitialised) {
        this.isInitialised = isInitialised;
    }

    private static Account getAccountInternal()  {
        Account account = null;

        Cursor c = TweetCo.mContext.getContentResolver().query(TweetCoProviderConstants.ACCOUNT_CONTENT_URI, null, null, null, null);
        if(c.moveToFirst())  {
            account = new Account();
            account.restoreFromCursor(c);
        }

        return account;
    }
}

package com.tweetco.models;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.microsoft.windowsazure.mobileservices.ApiJsonOperationCallback;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponse;
import com.tweetco.TweetCo;
import com.tweetco.activities.ApiInfo;
import com.tweetco.clients.AccountClient;
import com.tweetco.dao.Tweet;
import com.tweetco.dao.TweetUser;
import com.tweetco.database.dao.Account;
import com.tweetco.interfaces.SimpleObservable;
import com.tweetco.provider.TweetCoProviderConstants;

import java.io.ByteArrayOutputStream;
import java.net.MalformedURLException;

/**
 * Created by kirankum on 6/15/2015.
 */
public class AccountModel extends SimpleObservable<Account> {

    private Account account;
    private AccountClient accountClient;

    public  AccountModel()
    {
        account = getAccountInternal();
        accountClient = new AccountClient();
    }

    public Account getAccountCopy()
    {
        if(account != null) {
            return account.getCopy();
        }
        else {
            return null;
        }

    }

    private void  saveAccount(Account account)
    {
        this.account = account;
        String where = Account.COLUMN_ID + "= ? " ;
        TweetCo.mContext.getContentResolver().update(TweetCoProviderConstants.ACCOUNT_CONTENT_URI, account.toContentValues(), where, new String[]{String.valueOf(account.getId())});
        this.notifyObservers(this.account);
    }

    private void insertAccount(Account account)
    {
        this.account = account;
        TweetCo.mContext.getContentResolver().insert(TweetCoProviderConstants.ACCOUNT_CONTENT_URI, account.toContentValues());
        this.notifyObservers(this.account);
    }

    public void insertAccountFromServer(final String serverAddress, final String username, final String authToken)
    {
        Account tempAccount = accountClient.loadAccountFromServer(serverAddress, username, authToken);

        if(tempAccount != null) {
            insertAccount(tempAccount);
        }
    }

    public void refreshAccountFromServer()
    {
        Account tempAccount = accountClient.loadAccountFromServer(account.getServerAddress(), account.getUsername(), account.getAuthToken());

        if(tempAccount != null) {
            saveAccount(tempAccount);
        }
    }



    public void updateServer(final Account pAccount, BitmapDrawable profilePicDrawable, BitmapDrawable bgPicDrawable)
    {
        try {
            accountClient.updateServer(pAccount, profilePicDrawable, bgPicDrawable);

            saveAccount(pAccount);
        }
        catch (MalformedURLException e)
        {

        }
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

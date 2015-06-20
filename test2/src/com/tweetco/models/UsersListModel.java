package com.tweetco.models;

import com.tweetco.datastore.AccountSingleton;
import com.tweetco.clients.UsersListClient;
import com.tweetco.dao.TweetUser;
import com.tweetco.database.dao.Account;
import com.tweetco.datastore.UsersListSigleton;
import com.tweetco.interfaces.SimpleObservable;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by kirankumar on 19/06/15.
 */
public class UsersListModel extends SimpleObservable<UsersListModel> {

    private UsersListClient client = new UsersListClient();

    public void loadUsersList() throws MalformedURLException {
        if(UsersListSigleton.INSTANCE.getUsersList().isEmpty())
        {
            refreshUsersFromServer();
        }
        else
        {
            notifyObservers(this);
        }
    }

    public void refreshUsersFromServer() throws MalformedURLException
    {
        Account account = AccountSingleton.INSTANCE.getAccountModel().getAccountCopy();

        List<TweetUser> tempUsersList = client.getUsersListFromServer(account.getServerAddress(), account.getUsername(), account.getAuthToken());

        UsersListSigleton.INSTANCE.updateUsersListFromServer(tempUsersList);

        notifyObservers(this);
    }
}

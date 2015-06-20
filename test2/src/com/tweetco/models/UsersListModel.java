package com.tweetco.models;

import com.tweetco.datastore.AccountSingleton;
import com.tweetco.clients.UsersListClient;
import com.tweetco.dao.TweetUser;
import com.tweetco.database.dao.Account;
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

    private Map<String, TweetUser> userListMap = new ConcurrentHashMap<String, TweetUser>();
    private List<TweetUser> usersList = new ArrayList<TweetUser>();
    private UsersListClient client = new UsersListClient();


    public List<TweetUser> getUsersList()
    {
        return usersList;
    }

    public TweetUser getUser(String username)
    {
        return userListMap.get(username);
    }

    public void refreshUsersFromServer() throws MalformedURLException
    {
        Account account = AccountSingleton.INSTANCE.getAccountModel().getAccountCopy();

        List<TweetUser> tempUsersList = client.getUsersListFromServer(account.getServerAddress(), account.getUsername(), account.getAuthToken());

        usersList.removeAll(tempUsersList);

        usersList.addAll(tempUsersList);

        usersList.retainAll(tempUsersList);

        userListMap.clear();

        for(TweetUser user : usersList)
        {
            userListMap.put(user.username, user);
        }

        TweetUser currentUser = userListMap.get(AccountSingleton.INSTANCE.getAccountModel().getAccountCopy().getUsername());

        notifyObservers(this);
    }
}

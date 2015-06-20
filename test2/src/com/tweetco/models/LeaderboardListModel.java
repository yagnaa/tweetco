package com.tweetco.models;

import com.tweetco.clients.LeaderboardListClient;
import com.tweetco.dao.LeaderboardUser;
import com.tweetco.dao.TweetUser;
import com.tweetco.database.dao.Account;
import com.tweetco.datastore.AccountSingleton;
import com.tweetco.datastore.LeaderboardListSingleton;
import com.tweetco.datastore.UsersListSigleton;
import com.tweetco.interfaces.SimpleObservable;

import java.net.MalformedURLException;
import java.util.List;

/**
 * Created by kirankumar on 20/06/15.
 */
public class LeaderboardListModel extends SimpleObservable<LeaderboardListModel> {

    private LeaderboardListClient client = new LeaderboardListClient();

    public void loadLeaderboardUsersList() throws MalformedURLException {
        if(LeaderboardListSingleton.INSTANCE.getLeaderboardUserList().isEmpty())
        {
            refreshLeaderboardUsersFromServer();
        }
        else
        {
            notifyObservers(this);
        }
    }

    public void refreshLeaderboardUsersFromServer() throws MalformedURLException
    {
        Account account = AccountSingleton.INSTANCE.getAccountModel().getAccountCopy();

        List<LeaderboardUser> tempUsersList = client.getLeaderboardList();

        LeaderboardListSingleton.INSTANCE.updateLeaderboardUsersListFromServer(tempUsersList);

        notifyObservers(this);
    }

}

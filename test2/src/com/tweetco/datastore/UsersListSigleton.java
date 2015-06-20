package com.tweetco.datastore;

import com.tweetco.dao.TweetUser;
import com.tweetco.database.dao.Account;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by kirankumar on 20/06/15.
 */
public enum UsersListSigleton {
    INSTANCE;

    private Map<String, TweetUser> userListMap = new ConcurrentHashMap<String, TweetUser>();
    private List<TweetUser> usersList = new ArrayList<TweetUser>();

    public List<TweetUser> getUsersList()
    {
        return usersList;
    }

    public TweetUser getUser(String username)
    {
        return userListMap.get(username);
    }

    public TweetUser getCurrentUser()
    {
        return getUser(AccountSingleton.INSTANCE.getUserName());
    }

    public void updateUsersListFromServer(List<TweetUser> list)
    {
        usersList.removeAll(list);

        usersList.addAll(list);

        usersList.retainAll(list);

        userListMap.clear();

        for(TweetUser user : usersList)
        {
            userListMap.put(user.username, user);
        }
    }

    public void followUser(String username)
    {
        TweetUser user = getUser(username);
        if(user != null)
        {
            user.followers += AccountSingleton.INSTANCE.getUserName()+";";
        }

        TweetUser currentUser = getCurrentUser();
        currentUser.followees += username + ";";
    }

    public void unfollowUser(String username)
    {
        TweetUser user = getUser(username);
        if(user != null)
        {
            user.followers = removeUsername(user.followers, AccountSingleton.INSTANCE.getUserName());
        }

        TweetUser currentUser = getCurrentUser();
        currentUser.followees = removeUsername(currentUser.followees, username);
    }

    private static String removeUsername(String usernameList, String username)
    {
        String[] list = usernameList.split(username+";");
        StringBuilder builder = new StringBuilder();
        for(String name: list)
        {
            builder.append(name);
        }

        return builder.toString();
    }
}

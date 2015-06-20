package com.tweetco.datastore;

import com.tweetco.dao.TweetUser;

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
}

package com.tweetco.datastore;

import com.tweetco.dao.LeaderboardUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kirankumar on 20/06/15.
 */
public enum LeaderboardListSingleton {
    INSTANCE;

    private List<LeaderboardUser> leaderboardUserList = new ArrayList<LeaderboardUser>();

    public List<LeaderboardUser> getLeaderboardUserList()
    {
        return leaderboardUserList;
    }

    public void updateLeaderboardUsersListFromServer(List<LeaderboardUser> list)
    {
        leaderboardUserList.removeAll(list);

        leaderboardUserList.addAll(list);

        leaderboardUserList.retainAll(list);
    }

}

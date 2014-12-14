package com.tweetco.provider;

import android.net.Uri;

import com.tweetco.database.dao.Account;

public class TweetCoProviderConstants 
{
	public final static String AUTHORITY = "com.tweetco.provider";
	public final static Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);
	
	public final static Uri ACCOUNT_CONTENT_URI = Uri.parse(CONTENT_URI + "/" + Account.TABLE_NAME);
	public static final String ACCOUNT_CONTENT_TYPE 	= "vnd.android.cursor.dir/vnd.tweetco.account";
	
	public static final int BASE_SHIFT = 12;  // 12 bits to the base type: 0, 0x1000, 0x2000, etc.
	
	public static final int ACCOUNT_BASE = 0;
	public static final int ACCOUNT = ACCOUNT_BASE;
	public static final int ACCOUNT_ID = ACCOUNT_BASE + 1;
	
	// TABLE_NAMES MUST remain in the order of the BASE constants above (e.g. NOTE_BASE = 0x0000,
    // FOLDER_BASE = 0x1000, etc.)
    public static final String[] TABLE_NAMES = {
        Account.TABLE_NAME
    };
    
    public static String getTableName (int match)
	{
		String result = null;
		
		int tableIndex = match >> BASE_SHIFT;
		if (tableIndex>=0 && tableIndex<TABLE_NAMES.length)
		{
			result = TABLE_NAMES[tableIndex];
		}
		
		return result;
	}
    
    public static String getWhereClause(int match, Uri uri, String selection)
	{
		String whereClause = selection;
		
		switch (match) 
		{
		case ACCOUNT_ID:
			whereClause = whereWithId(uri.getPathSegments().get(1), selection);
			break;
		
		default:
			break;
		}
		
		return whereClause;
	}
	
	private static String whereWithId(String id, String selection) {
        StringBuilder sb = new StringBuilder(256);
        sb.append("_id=");
        sb.append(id);
        if (selection != null) {
            sb.append(" AND (");
            sb.append(selection);
            sb.append(')');
        }
        return sb.toString();
    }
}

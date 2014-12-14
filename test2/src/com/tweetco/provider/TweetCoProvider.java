package com.tweetco.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.tweetco.database.DatabaseHelper;
import com.tweetco.database.dao.Account;

public class TweetCoProvider extends ContentProvider 
{
	private final static String TAG = "TweetCoProvider";
	
	private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	private SQLiteDatabase mDatabase;
	
	static {
        // Email URI matching table
        UriMatcher matcher = sURIMatcher;
        
        matcher.addURI(TweetCoProviderConstants.AUTHORITY, Account.TABLE_NAME, TweetCoProviderConstants.ACCOUNT);
        matcher.addURI(TweetCoProviderConstants.AUTHORITY, Account.TABLE_NAME+"/#", TweetCoProviderConstants.ACCOUNT_ID);
	}
	
	/**
     * The idea here is that the two databases (EmailProvider.db and EmailProviderBody.db must
     * always be in sync (i.e. there are two database or NO databases).  This code will delete
     * any "orphan" database, so that both will be created together.  Note that an "orphan" database
     * will exist after either of the individual databases is deleted due to data corruption.
     */
    public void checkDatabases() {
    	synchronized (TweetCoProvider.class) {
    		// Uncache the databases
            if (mDatabase != null) {
                mDatabase = null;
            }
		}
    }
	
    SQLiteDatabase getDatabase(Context context) {
    	synchronized (TweetCoProvider.class) {
    		// Always return the cached database, if we've got one
            if (mDatabase != null) {
                return mDatabase;
            }

            // Whenever we create or re-cache the databases, make sure that we haven't lost one
            // to corruption
            checkDatabases();

            mDatabase = DatabaseHelper.getInstance(getContext());
            
            return mDatabase;
		}
    }
	
	private static int findMatch(Uri uri, String methodName) {
        int match = sURIMatcher.match(uri);
        if (match < 0) 
        {
            throw new IllegalArgumentException("Unknown uri: " + uri);
        } 
        else 
        {
            Log.d(TAG, methodName + ": uri=" + uri + ", match is " + match);
        }
        return match;
    }
	
	@Override
	public boolean onCreate() 
	{
		checkDatabases();
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) 
	{
		Cursor cursor = null;
		
		int match = findMatch(uri, "query");
		String tableName = TweetCoProviderConstants.getTableName(match);
		if(!TextUtils.isEmpty(tableName))
		{
			Context context = getContext();
			String whereClause = TweetCoProviderConstants.getWhereClause(match, uri, selection);
			SQLiteDatabase db = getDatabase(context);
			cursor = db.query(tableName, projection, whereClause, selectionArgs, null, null, sortOrder);
			
			cursor.setNotificationUri(context.getContentResolver(), uri);
		}
		return cursor;
	}

	@Override
	public String getType(Uri uri) 
	{
		String type = null;
		int match = findMatch(uri, "getType");
		switch (match) 
		{
		case TweetCoProviderConstants.ACCOUNT:
		case TweetCoProviderConstants.ACCOUNT_ID:
			type = TweetCoProviderConstants.ACCOUNT_CONTENT_TYPE;
			break;

		default:
			break;
		}
		
		return type;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) 
	{
		Uri result = null;

		int match = findMatch(uri, "insert");
		String tableName = TweetCoProviderConstants.getTableName(match);
		if(!TextUtils.isEmpty(tableName))
		{
			Context context = getContext();
			SQLiteDatabase db = getDatabase(context);
			long rowId = db.insert(tableName, null, values);
			if (rowId >= 0)
			{
				Uri noteUri = ContentUris.withAppendedId(uri, rowId);
				context.getContentResolver().notifyChange(noteUri, null);
				result = noteUri;
			}
			else
			{
				throw new SQLException("Failed to insert row into " + uri);
			}
		}
		
		return result;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) 
	{
		int count = 0;
		int match = findMatch(uri, "delete");
		String tableName = TweetCoProviderConstants.getTableName(match);
		boolean noteDeletion = false;
		if(!TextUtils.isEmpty(tableName))
		{
			Context context = getContext();
			SQLiteDatabase db = getDatabase(context);
			try 
			{
				String whereClause = TweetCoProviderConstants.getWhereClause(match, uri, selection);
				count = db.delete(tableName, whereClause, selectionArgs);
				
				if(noteDeletion)
				{
					db.setTransactionSuccessful();
				}
				
				context.getContentResolver().notifyChange(uri, null);
			} 
			catch (SQLException e) 
			{
				throw e;
			}
			finally
			{
				if(noteDeletion)
				{
					db.endTransaction();
				}
			}
		}
		return count;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) 
	{
		int count = 0;
		int match = findMatch(uri, "update");
		String tableName = TweetCoProviderConstants.getTableName(match);
		
		if(!TextUtils.isEmpty(tableName))
		{
			Context context = getContext();
			String whereClause = TweetCoProviderConstants.getWhereClause(match, uri, selection);
			SQLiteDatabase db = getDatabase(context);
			
			count = db.update(tableName, values, whereClause, selectionArgs);
			context.getContentResolver().notifyChange(uri, null);
		}
		
		return count;
	}

}

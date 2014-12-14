package com.tweetco.database;

import com.tweetco.database.dao.Account;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper 
{
	public static final String				DATABASE_NAME				= "tweetco.db";
	private static final int				DATABASE_CURRENT_VERSION	= 1;
		
	private static DatabaseHelper mInstance = null;
	
	private DatabaseHelper(Context context) 
	{
		super(context, DATABASE_NAME, null, DATABASE_CURRENT_VERSION);
	}
	
	public synchronized static SQLiteDatabase getInstance(Context context)
	{
		if(mInstance == null)
		{
			mInstance = new DatabaseHelper(context);
		}
		
		return mInstance.getWritableDatabase();
	}

	private void createAccountTable(SQLiteDatabase db)
	{
		String columns = 	Account.COLUMN_ID + " integer primary key autoincrement, " +
								Account.COLUMN_USERNAME + " text, " + 
								Account.COLUMN_PASSWORD + " text, " +
								Account.COLUMN_SERVER_ADDRESS + " text, " +
								Account.COLUMN_AUTH_TOKEN + " text, " + 
								Account.COLUMN_USERID + " text, " +
								Account.COLUMN_DISPLAY_NAME + " text";
		
		String createString = "create table if not exists " + Account.TABLE_NAME + "(" + columns + ");";
	
		db.execSQL(createString);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) 
	{
		createAccountTable(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
	{

	}
	
}

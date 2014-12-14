package com.tweetco.database.dao;

public abstract class DBContent 
{
	public static final String COLUMN_ID                 	= "_id";
	
	private long		mId;
	
	public long getId() {
		return mId;
	}
	public void setId(long m_id) {
		this.mId = m_id;
	}
}

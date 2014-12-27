package com.tweetco.utility;

import java.io.File;

import android.net.Uri;

import com.tweetco.TweetCo;

public class AttachmentHelper 
{
	private static final String ATTACHMENTS_DIR = "attachments";
	
	public static File getAttachmentsDir()
	{
		return  TweetCo.mContext.getFileStreamPath(ATTACHMENTS_DIR);
	}

	public static File getAttachmentFile(String fileName)
	{
		File newFile = new File(AttachmentHelper.getAttachmentsDir() + File.separator + fileName);
		newFile.getParentFile().mkdirs();
		return newFile;
	}
	
	public static void deleteAttachment(Uri uri)
	{
		TweetCo.mContext.getContentResolver().delete(uri, null, null);
	}
}

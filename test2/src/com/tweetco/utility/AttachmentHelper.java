package com.tweetco.utility;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

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
		try {
			File file = new File(new URI(uri.toString()));

			if(file.exists() && file.isFile())
			{
				file.delete();
			}
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

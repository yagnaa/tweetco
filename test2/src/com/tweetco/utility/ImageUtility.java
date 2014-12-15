package com.tweetco.utility;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;

import com.tweetco.R;

public class ImageUtility 
{
	private static final String IMG_EXTN = ".jpg";
	
	private static Uri msAttachmentUri = null;
	
	public static final String[] ACCEPTABLE_ATTACHMENT_SEND_UI_TYPES = new String[] {
    	"image/*"
    };
    

	public static Intent getImageChooserIntent(Context context)
	{
		return getImageChooserIntent(context, getImageAttachmentUri(context));
	}
	
	public static Intent getImageChooserIntent(Context context, Uri outputFileUri)
	{
		Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
		galleryIntent.addCategory(Intent.CATEGORY_OPENABLE);
		galleryIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
		galleryIntent.setType(ACCEPTABLE_ATTACHMENT_SEND_UI_TYPES[0]);

		Intent returnIntent = Intent.createChooser(galleryIntent, context.getString(R.string.choose_picture_dialog_title));

		return returnIntent;
	}
	
	public static Intent getImageCaptureIntent(Context context)
	{
		return getImageCaptureIntent(context, getImageAttachmentUri(context));
	}
	
	public static Intent getImageCaptureIntent(Context context, Uri outputFileUri)
	{
			Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			
			if(cameraIntent.resolveActivity(context.getPackageManager()) != null)
			{
				cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
				cameraIntent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
			}

		return cameraIntent;
	}
	
	private static Uri getImageAttachmentUri(Context context)
	{	
		String fileName = UUID.randomUUID().toString() + IMG_EXTN;
		File file = AttachmentHelper.getAttachmentFile(fileName);
		
		msAttachmentUri = FileProvider.getUriForFile(context, "com.tweetco.fileprovider", file);
		
		return msAttachmentUri;
	}
	
	public static void onImageAttachmentCancelled()
	{
		if(msAttachmentUri != null)
		{
			(new AttachmentDeleteThread(msAttachmentUri)).start();
			msAttachmentUri = null;
		}
	}
	
	private static class AttachmentDeleteThread extends Thread
	{
		Uri mAttachment = null;
		AttachmentDeleteThread(Uri attachment)
		{
			mAttachment = attachment;
		}

		@Override
		public void run() 
		{
			if(mAttachment != null)
			{
				AttachmentHelper.deleteAttachment(mAttachment);
			}
		}
	}
	
	public static Uri onImageAttachmentReceived(Context context, Intent data) throws FileNotFoundException, IOException
	{
		final boolean isCamera;
		if(data == null)
		{
			isCamera = true;
		}
		else
		{
			final String action = data.getAction();
			if(action == null)
			{
				isCamera = false;
			}
			else
			{
				isCamera = action.equals(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
			}
		}

		Uri selectedImageUri;
		if(isCamera)
		{
			selectedImageUri = msAttachmentUri;
		}
		else
		{
			selectedImageUri = data == null ? null : data.getData();
		}

		msAttachmentUri = null;
		return selectedImageUri;
	}
	
}

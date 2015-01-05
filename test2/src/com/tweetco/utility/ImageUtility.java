package com.tweetco.utility;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.widget.TextView;

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
		Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		ComponentName componentName = cameraIntent.resolveActivity(context.getPackageManager());
		if(componentName!= null)
		{
			String pack = componentName.getPackageName();
			Uri outputFileUri = getImageAttachmentUri(context);
			context.grantUriPermission(pack, outputFileUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
			cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
			cameraIntent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
			return cameraIntent;
		}

		return null;
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

	/**
     * rotate bitmap code reference:
     * http://stackoverflow.com/questions/20478765/how-to-get-the-correct-orientation-of-the-image-selected-from-the-default-image
     */
	private static Bitmap rotateBitmap(Bitmap bitmap, int orientation)
	{
		Matrix matrix = new Matrix();
		switch (orientation)
		{
		case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
			matrix.setScale(-1, 1);
			break;
		case ExifInterface.ORIENTATION_ROTATE_180:
			matrix.setRotate(180);
			break;
		case ExifInterface.ORIENTATION_FLIP_VERTICAL:
			matrix.setRotate(180);
			matrix.postScale(-1, 1);
			break;
		case ExifInterface.ORIENTATION_TRANSPOSE:
			matrix.setRotate(90);
			matrix.postScale(-1, 1);
			break;
		case ExifInterface.ORIENTATION_ROTATE_90:
			matrix.setRotate(90);
			break;
		case ExifInterface.ORIENTATION_TRANSVERSE:
			matrix.setRotate(-90);
			matrix.postScale(-1, 1);
			break;
		case ExifInterface.ORIENTATION_ROTATE_270:
			matrix.setRotate(-90);
			break;
		case ExifInterface.ORIENTATION_NORMAL:
		case ExifInterface.ORIENTATION_UNDEFINED:
		default:
			return bitmap;
		}
		try
		{
			Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
			bitmap.recycle();
			return bmRotated;
		}
		catch (OutOfMemoryError e)
		{
			Log.e("ImageUtility", "OutOfMemoryError occured while rotating the image");
			return bitmap;
		}
	}

	public static String getFileNameByUri(Context context, Uri uri)
	{
	    String fileName="unknown";//default fileName
	    Uri filePathUri = uri;
	    if (uri.getScheme().toString().compareTo("content")==0)
	    {      
	        String folderAbsolutePath = context.getFilesDir().getAbsolutePath() +"/attachments";
	        Cursor returnCursor =
	                context.getContentResolver().query(uri, null, null, null, null);
	        /*
	         * Get the column indexes of the data in the Cursor,
	         * move to the first row in the Cursor, get the data,
	         * and display it.
	         */
	        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
	        returnCursor.moveToFirst();
	        fileName = folderAbsolutePath + "/" + returnCursor.getString(nameIndex);
	    }
	    else if (uri.getScheme().compareTo("file")==0)
	    {
	        fileName = filePathUri.getLastPathSegment().toString();
	    }
	    else
	    {
	        fileName = fileName+"_"+filePathUri.getLastPathSegment();
	    }
	    
	    return fileName;
	}
	
	public static Bitmap correctImageRotation( Context context, Bitmap bitmap , Uri inputUri ) throws FileNotFoundException
	{
		int orientation = ExifInterface.ORIENTATION_UNDEFINED;
		
		try
		{
			ExifInterface exif = new ExifInterface(getFileNameByUri(context, inputUri));
			orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
		}
		catch (IOException e)
		{
		}
		
		return rotateBitmap(bitmap, orientation);
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

		selectedImageUri = getCorrectedImageUri(context, selectedImageUri, isCamera);
		
		msAttachmentUri = null;
		return selectedImageUri;
	}
	
	private static Uri getCorrectedImageUri(Context context, Uri fileUri, boolean isCamera)
	{
		Uri uri = fileUri;
		
		if(isCamera)
		{
			try 
			{
				InputStream is = context.getContentResolver().openInputStream(uri);
				// Decode bitmap with inSampleSize set
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inJustDecodeBounds = false;
				options.inPreferredConfig = Config.RGB_565;		
				Bitmap bmp = BitmapFactory.decodeStream(is, null, options);
				is.close();
				
				if(isCamera && bmp != null)
				{
					bmp = correctImageRotation(context, bmp, uri);
					
					OutputStream os = context.getContentResolver().openOutputStream(uri);
					try 
					{
						boolean result = bmp.compress(CompressFormat.JPEG, 25 , os);
					} 
					finally
					{
						if(os != null)
						{
							try 
							{
								os.close();
							} 
							catch (IOException e) 
							{
								Log.e("ImageUtility", "Failed closing file when copying image"+e);
							}
						}
					}
				}
				
				
				
				
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		return uri;
	}

}

package com.tweetco.asynctasks;

import java.io.ByteArrayOutputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.microsoft.windowsazure.mobileservices.ApiJsonOperationCallback;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponse;
import com.tweetco.activities.progress.AsyncTaskEventSinks.AsyncTaskCancelCallback;
import com.tweetco.activities.progress.AsyncTaskEventSinks.UIEventSink;
import com.tweetco.tweets.TweetCommonData;
import com.yagnasri.displayingbitmaps.ui.ApiInfo;

public class EditProfileTask extends AsyncTask<Void, Void, Void> 
{
	public static interface EditProfileTaskCompletionCallback
	{
		public void onEditProfileTaskSuccess();
		public void onEditProfileTaskFailure ();
		public void onEditProfileTaskCancelled();
	}
	
	private final static String TAG = "EditProfileTask";
	private EditProfileTaskCompletionCallback m_completioncallback;
	private UIEventSink m_uicallback;
	private Context mContext;
	private EditProfileTaskParams mParams;
	private boolean mbSuccess = false;
	
	public EditProfileTask(Context context, EditProfileTaskParams params, UIEventSink uicallback, EditProfileTaskCompletionCallback completioncallback)
	{
		m_completioncallback = completioncallback;
		m_uicallback = uicallback; 
		mContext = context;
		mParams = params;
	}
	
	@Override
	protected void onPreExecute()
	{
		Log.d("tag","onPreExecute");
		if(m_uicallback!=null)
		{
			m_uicallback.onAysncTaskPreExecute(this, new AsyncTaskCancelCallback()
			{
				@Override
				public void onCancelled()
				{
					cancel(true);
					m_completioncallback.onEditProfileTaskCancelled();
				}
			}, true);
		}
	}
	
	@Override
	protected Void doInBackground(Void... params) 
	{
		MobileServiceClient client = TweetCommonData.mClient;
			JsonObject element = new JsonObject();
			element.addProperty(ApiInfo.kApiRequesterKey, mParams.getUsername());
			ByteArrayOutputStream bos = new ByteArrayOutputStream();  
			BitmapDrawable profilePicDrawable = mParams.getProfileImage();
			
			if(profilePicDrawable != null)
			{
				Bitmap bitmap = profilePicDrawable.getBitmap();
				bitmap.compress(CompressFormat.JPEG,25,bos); 
				byte[] bb = bos.toByteArray();
				String image = Base64.encodeToString(bb, 0);
				element.addProperty(ApiInfo.kBase64ImageStringKey, image);
				
			}
			
			BitmapDrawable bgPicDrawable = mParams.getHeaderImage();
			
			if(bgPicDrawable != null)
			{
				Bitmap bitmap = bgPicDrawable.getBitmap();
				bitmap.compress(CompressFormat.JPEG,25,bos); 
				byte[] bb = bos.toByteArray();
				String image = Base64.encodeToString(bb, 0);
				element.addProperty(ApiInfo.kBase64BGImageStringKey, image);
				
			}
			
			client.invokeApi(ApiInfo.UPDATE_USER_IMAGE, element, new ApiJsonOperationCallback() {
				
				@Override
				public void onCompleted(JsonElement element, Exception exception, ServiceFilterResponse arg2) 
				{
					if(exception == null)
					{
						Log.d("EditProfile", "Profile edit saved");
						mbSuccess = true;
					}
					else
					{
						Log.e("EditProfile", "Profile edit save failed");
						mbSuccess = false;
					}
					
				}
			}, true);
		return null;
	}

	@Override
	protected void onPostExecute(Void value)
	{
		if(mbSuccess)
		{
			m_completioncallback.onEditProfileTaskSuccess();
		}
		else
		{
			m_completioncallback.onEditProfileTaskFailure();
		}
	}
}

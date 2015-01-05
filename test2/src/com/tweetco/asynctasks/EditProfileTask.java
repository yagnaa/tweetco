package com.tweetco.asynctasks;

import java.io.ByteArrayOutputStream;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.microsoft.windowsazure.mobileservices.ApiJsonOperationCallback;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponse;
import com.tweetco.TweetCo;
import com.tweetco.activities.ApiInfo;
import com.tweetco.activities.progress.AsyncTaskEventSinks.AsyncTaskCancelCallback;
import com.tweetco.activities.progress.AsyncTaskEventSinks.UIEventSink;
import com.tweetco.dao.TweetUser;
import com.tweetco.database.dao.Account;
import com.tweetco.provider.TweetCoProviderConstants;
import com.tweetco.tweets.TweetCommonData;

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



		if(mbSuccess)
		{

			JsonObject obj = new JsonObject();
			obj.addProperty(ApiInfo.kApiRequesterKey, mParams.getUsername());
			client.invokeApi(ApiInfo.GET_USER_INFO, obj, new ApiJsonOperationCallback() {

				@Override
				public void onCompleted(JsonElement arg0, Exception arg1,
						ServiceFilterResponse arg2) {
					if(arg1 == null)
					{
						Gson gson = new Gson();

						try
						{
							TweetUser[] tweetUser = gson.fromJson(arg0, TweetUser[].class);
							if(tweetUser.length > 0)
							{
								// Clear all the data points
								Log.d("EditProfileTask", "Fetched new UserData after posting new images") ;
								TweetCommonData.tweetUsers.put(mParams.getUsername().toLowerCase(), tweetUser[0]);
							}
							//Also Update the Account Table.
							Account account  = TweetCommonData.getAccount();
							ContentValues cv = new ContentValues();
							cv.put(Account.COLUMN_BOOKMARKED_TWEETS, tweetUser[0].bookmarkedtweets);
							cv.put(Account.COLUMN_FOLLOWEES, tweetUser[0].followees);
							cv.put(Account.COLUMN_FOLLOWERS, tweetUser[0].followers);
							cv.put(Account.COLUMN_INTEREST_TAGS, tweetUser[0].interesttags);
							cv.put(Account.COLUMN_PROFILE_BG_URL, tweetUser[0].profilebgurl);
							cv.put(Account.COLUMN_PROFILE_IMAGE_URL, tweetUser[0].profileimageurl);
							String where = Account.COLUMN_ID + "= ? " ;
							TweetCo.mContext.getContentResolver().update(TweetCoProviderConstants.ACCOUNT_CONTENT_URI, cv, where , new String[]{String.valueOf("0")});
							
						}
						catch(JsonSyntaxException exception)
						{
							exception.printStackTrace();
							Log.e("TweetUserRunnable", "unable to parse tweetUser") ;
						}

					}
					else
					{
						Log.e("Item clicked","Exception fetching tweets received") ;
					}

				}
			},true);
		}

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

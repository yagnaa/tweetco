package com.tweetco.asynctasks;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.tweetco.activities.progress.AsyncTaskEventSinks.AsyncTaskCancelCallback;
import com.tweetco.activities.progress.AsyncTaskEventSinks.UIEventSink;
import com.tweetco.provider.TweetCoProviderConstants;

public class AddAccountTask extends AsyncTask<Void, Void, Uri> 
{
	public static interface AddAccountTaskCompletionCallback
	{
		public void onAddAccountTaskSuccess(Uri accountUri);
		public void onAddAccountTaskFailure ();
		public void onAccountCreationCancelled();
	}
	private static String TAG = "AddAccountTask";
	
	private AddAccountTaskCompletionCallback m_completioncallback;
	private UIEventSink m_uicallback;
	private Context mContext;
	private AddAccountTaskParams mParams;

	public AddAccountTask (Context context, AddAccountTaskParams params, UIEventSink uicallback, AddAccountTaskCompletionCallback completioncallback)
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
					m_completioncallback.onAccountCreationCancelled();
				}
			}, true);
		}
	}


	@Override
	protected Uri doInBackground(Void... arg0)
	{
		return mContext.getContentResolver().insert(TweetCoProviderConstants.ACCOUNT_CONTENT_URI, mParams.getAccount().toContentValues());
	}

	@Override
	protected void onPostExecute(Uri result)
	{
		if(result != null)
		{
			m_completioncallback.onAddAccountTaskSuccess(result);
		}
		else
		{
			m_completioncallback.onAddAccountTaskFailure();
		}
	}

}

package com.tweetco.activities.progress;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.tweetco.R;
import com.tweetco.activities.progress.AsyncTaskEventSinks.AsyncTaskCancelCallback;

public class AsyncTaskEventHandler implements AsyncTaskEventSinks.UIEventSink 
{
	protected AsyncTaskCancelCallback	m_currentAsyncTaskCancelCallback;
	//protected ProgressFragment          m_progressDialog = null;   /**<Progress dialog that is displayed while AsyncTask executes*/
	private   Activity 					m_activity;
	private boolean 					m_bTaskRunning = false;
	private String 						message;
	
	public AsyncTaskEventHandler(Activity activity)
	{
		this(activity, activity.getResources().getString(R.string.strConnectionDialogMsg));
	}

	public AsyncTaskEventHandler(Activity activity, int msgid)
	{
		this(activity, activity.getResources().getString(msgid));
	}

	public AsyncTaskEventHandler(Activity activity, String progressText)
	{
		//m_progressDialog = createProgressDialog(activity, progressText);
		message = progressText;
		m_activity = activity;
	}
	
	@Override
	public void onAysncTaskPreExecute(Object asyncTask,AsyncTaskCancelCallback cancelCallback, boolean bShowProgressDialog)
	{
        m_currentAsyncTaskCancelCallback = cancelCallback;
        m_bTaskRunning = true;
        if(!m_activity.isFinishing() && bShowProgressDialog && !ProgressFragment.isShowing(m_activity))
        {
        	ProgressFragment.showProgress(m_activity, message);
        }	
	}

	@Override
	public void onUpdateProgressMessage(String message)
	{
		if(!m_activity.isFinishing() && message != null && ProgressFragment.isShowing(m_activity))
		{
			ProgressFragment.setMessage(m_activity,message);
		}
	}
	/**
	 * Called when the currently running async task needs to be cancelled. Called in relation to progress being cancelled
	 */
	private void onAsyncTaskCancelRequest()
	{
		m_bTaskRunning = false;
		if(m_currentAsyncTaskCancelCallback != null)
		{
			m_currentAsyncTaskCancelCallback.onCancelled();
		}
	}
	
	protected ProgressDialog createProgressDialog(Context context, String msg)
    {
        ProgressDialog pd = ProgressDialogUtil.createProgressDialog(context,msg,new DialogInterface.OnCancelListener()
        {
            @Override
            public void onCancel(DialogInterface dialog)
            {
                onAsyncTaskCancelRequest();
            }
        },true, false);
        return pd;
    }
	
	public void dismiss()
	{
		m_bTaskRunning = false;
	    if(!m_activity.isFinishing() && ProgressFragment.isShowing(m_activity))
	    {
	    	ProgressFragment.stopProgress(m_activity);
	    }
	}
	
	public void cancelAsyncTask()
	{
		if(m_bTaskRunning)
		{
			//Dismiss the dialog as the async task is being cancelled
			dismiss();
			onAsyncTaskCancelRequest();
		}
	}


	@Override
	public void onUpdateDataProgessValue(Integer updateProgress)
	{
	}


	@Override
	public void onDataAsyncTaskCancelRequest()
	{
	}
}

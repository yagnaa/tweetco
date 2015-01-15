package com.tweetco.activities;


import java.util.ArrayList;
import java.util.List;

import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.onefortybytes.R;
import com.tweetco.activities.FTUActivity.AddAccountTask.AddAccountTaskCompletionCallback;
import com.tweetco.activities.progress.AsyncTaskEventHandler;
import com.tweetco.activities.progress.AsyncTaskEventSinks.AsyncTaskCancelCallback;
import com.tweetco.activities.progress.AsyncTaskEventSinks.UIEventSink;
import com.tweetco.tweets.UserProfile;
import com.tweetco.utility.UiUtility;

public class FTUActivity extends TweetCoBaseActivity 
{
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ftu);
	}
	
	public static class FTUFragment extends Fragment
	{
		private EditText mServerAddress = null;
		private EditText mUsername = null;
		private EditText mPassword = null;
		private Button mContinue = null;
		
		AsyncTaskEventHandler asyncTaskEventHandler = null;
		
		@Override
	    public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                             Bundle savedInstanceState) {
	        // Inflate the layout for this fragment
	        View view = inflater.inflate(R.layout.ftufragmentlayout, container, false);
	        mServerAddress = UiUtility.getView(view, R.id.FTUAddServerAddress);
	        mUsername = UiUtility.getView(view, R.id.FTUAddUserName);
	        mPassword = UiUtility.getView(view, R.id.FTUAddPassword);
	        asyncTaskEventHandler = new AsyncTaskEventHandler(this.getActivity(), "Fetching info");
	        mContinue = UiUtility.getView(view, R.id.FTULoginButton);
	        mContinue.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) 
				{
					AddAccountTaskParams params = new AddAccountTaskParams();
					params.mServerAddress = mServerAddress.getText().toString();
					params.mUsername = mUsername.getText().toString();
					params.mPassword = mPassword.getText().toString();
					
					new AddAccountTask(getActivity(), params, asyncTaskEventHandler, new AddAccountTaskCompletionCallback() {
						
						@Override
						public void onAddAccountTaskSuccess(List<UserProfile> knowsUsersProfile,
								List<UserProfile> interactUserProfiles) {
							// TODO Auto-generated method stub
							
						}
						
						@Override
						public void onAddAccountTaskFailure() {
							// TODO Auto-generated method stub
							
						}
						
						@Override
						public void onAccountCreationCancelled() {
							// TODO Auto-generated method stub
							
						}
					});
				}
			});
	        return view;
	    }
	}
	
	public static class AddAccountTaskParams
	{
		public String mServerAddress;
		public String mUsername;
		public String mPassword;
	}
	
	public static class AddAccountTask extends AsyncTask<Void, Void, Void>
	{


		public static interface AddAccountTaskCompletionCallback
		{
			public void onAddAccountTaskSuccess (List<UserProfile> knowsUsersProfile, List<UserProfile> interactUserProfiles);
			public void onAddAccountTaskFailure ();
			public void onAccountCreationCancelled();
		}

		private AddAccountTaskCompletionCallback m_completioncallback;
		private UIEventSink m_uicallback;
		private Context mContext;
		private AddAccountTaskParams mParams;
		List<String[]> heirarchy = new ArrayList<String[]>();
		public List<UserProfile> knownUserProfiles = null;
		public List<UserProfile> interactUserProfiles = null;
		
		public AddAccountTask (Context context, AddAccountTaskParams params, UIEventSink uicallback, AddAccountTaskCompletionCallback completioncallback)
		{
			m_completioncallback = completioncallback;
			m_uicallback = uicallback; 
			mContext = context;
			mParams = params;
			//For Kirana
			heirarchy.add(new String[] {"yagnasri.alla@citrix.com", "rohan.kapoor@citrix.com", "udaya.kiran@citrix.com", "teja.singh@citrix.com" });
			//For Yagnasri
			heirarchy.add(new String[] {"kiran.kumar@citrix.com", "rohan.kapoor@citrix.com", "udaya.kiran@citrix.com", "teja.singh@citrix.com" });
			//For Uday
			heirarchy.add(new String[] {"yagnasri.alla@citrix.com", "rohan.kapoor@citrix.com", "kiran.kumar@citrix.com", "teja.singh@citrix.com" });
			
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
		protected Void doInBackground(Void... arg0)
		{
			
			return null;
		}

		public static boolean isTeamUser(String email)
		{
			return email.equalsIgnoreCase("kiran.kumar@citrix.com") || email.equalsIgnoreCase("udaya.kiran@citrix.com") || email.equalsIgnoreCase("yagnasri.alla@citrix.com") || email.equalsIgnoreCase("teja.singh@citrix.com") || email.equalsIgnoreCase("rohan.kapoor@citrix.com"); 
		}
		
		@Override
		protected void onPostExecute(Void result)
		{
			m_completioncallback.onAddAccountTaskSuccess(knownUserProfiles, interactUserProfiles);
		}
	}
}

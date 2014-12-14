package com.tweetco.activities.progress;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.tweetco.R;

public class ProgressFragment extends DialogFragment
{

	
	
		
		private static final String TAG = "ProgressFragment";
		
		private static final String ARG_TITLE = "title";
		
		private static final float DIM_AMOUNT = 0.3f;
		
		private CALLBACK mCallback;
		
		public interface CALLBACK {
			public void onBackPressed();
		}
		
		public static ProgressFragment showProgress(Activity activity, String title) {
			FragmentManager manager = activity.getFragmentManager();
			ProgressFragment frag = (ProgressFragment) manager.findFragmentByTag(TAG);
			if (frag != null) {
				frag.dismissAllowingStateLoss();
			}
			
			frag = new ProgressFragment();
			Bundle args = new Bundle(1);
			args.putString(ARG_TITLE, title);
			frag.setArguments(args);		
			frag.show(manager, TAG);
			
			return frag;
		}
		
		public static void stopProgress(Activity activity) {
			if(null != activity)
			{
				if(!activity.isFinishing())
				{
					FragmentManager manager = activity.getFragmentManager();
					ProgressFragment frag = (ProgressFragment) manager.findFragmentByTag(TAG);
					
					if (frag != null && frag.isVisible()) {
						frag.dismissAllowingStateLoss();
					}
				}
			}
		}
		
		public static boolean isShowing(Activity activity)
		{
			boolean isShowing = false;
			FragmentManager manager = activity.getFragmentManager();
			ProgressFragment frag = (ProgressFragment) manager.findFragmentByTag(TAG);
			
			if(null != frag && frag.isVisible()){
				isShowing = true;
			}
			
			return isShowing;

		}
		
		public final static void setMessage(Activity activity,String text)
		{
			FragmentManager manager = activity.getFragmentManager();
			ProgressFragment frag = (ProgressFragment) manager.findFragmentByTag(TAG);
			
			if( null != frag){
				frag.setMessageInTitle(text);
			}

		}
		
		private void setMessageInTitle(String title)
		{
			if( null != mTitle){
				mTitle.setText(title);
			}

		}
		
		protected TextView mTitle;
		
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
		}
			
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View view = inflater.inflate(R.layout.custom_progress_layout, null);
			mTitle = (TextView) view.findViewById(R.id.title);
			
			if (getArguments() != null && getArguments().containsKey(ARG_TITLE)) {
				mTitle.setText(getArguments().getString(ARG_TITLE));	
			}

			if (getDialog() != null) {
				getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
				getDialog().setCanceledOnTouchOutside(false);
				getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
				
				WindowManager.LayoutParams lp = getDialog().getWindow().getAttributes();  
				lp.dimAmount = DIM_AMOUNT;
				getDialog().getWindow().setAttributes(lp);
			}
			
			return view;
		}
		
		public void setOnBackPressed(CALLBACK callback) {
			mCallback = callback;
		}
		
		@Override
		public void onCancel(DialogInterface dialog) {
			if (mCallback != null) {
				mCallback.onBackPressed();
				super.onCancel(dialog);
			}
		}
}

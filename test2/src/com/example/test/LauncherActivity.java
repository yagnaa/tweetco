package com.example.test;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.microsoft.windowsazure.mobileservices.MobileServiceClient;



public class LauncherActivity extends Activity
{
	private MobileServiceClient mClient;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.launcher);
		
		Button b = (Button) (findViewById(R.id.button1));
		b.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(LauncherActivity.this, AllInOneActivity.class);
				LauncherActivity.this.startActivity(intent);
				LauncherActivity.this.finish();
			}
		});
        

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}
}

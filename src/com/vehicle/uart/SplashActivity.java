package com.vehicle.uart;

import android.app.Activity;  
import android.content.Intent;  
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.AlphaAnimation;  
import android.view.animation.Animation;  
import android.view.animation.Animation.AnimationListener;  
import android.widget.ImageView;
import android.widget.TextView;
	
public class SplashActivity extends Activity 
{

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);
		
		TextView version_name = (TextView) findViewById(R.id.version_name);
		version_name.setText(Feature.g_Version);

		new Handler().postDelayed(new Runnable()
		{
			@Override
			public void run() 
			{
				Intent intent = new Intent(SplashActivity.this, MainActivity.class);
				startActivity(intent);
				SplashActivity.this.finish();
			}
		}, 2500);
	} 
}

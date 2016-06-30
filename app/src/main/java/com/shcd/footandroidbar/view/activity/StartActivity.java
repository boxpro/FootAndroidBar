package com.shcd.footandroidbar.view.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class StartActivity extends Activity implements Runnable{
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		new Thread(this).start();
	}

	@Override
	public void run() {
		try {
			Thread.sleep(2000);
			Intent intent=new Intent();
			intent.setClass(StartActivity.this, LoginActivity.class);
			startActivity(intent);
			StartActivity.this.finish();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

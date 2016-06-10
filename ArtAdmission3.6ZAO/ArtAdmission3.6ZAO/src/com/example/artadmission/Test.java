package com.example.artadmission;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

public class Test extends Activity{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);  //设置无标题显示  
		setContentView(R.layout.activity_main);		
	}
}

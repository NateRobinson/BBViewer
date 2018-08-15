package com.arcblock.btcblockviewer.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.arcblock.btcblockviewer.R;
import com.arcblock.btcblockviewer.utils.StatusBarUtils;

public class AboutUsActivity extends AppCompatActivity {

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
		this.getWindow().setBackgroundDrawableResource(R.color.transparent);

		StatusBarUtils.MIUISetStatusBarLightMode(this.getWindow(), false);
		StatusBarUtils.FlymeSetStatusBarLightMode(this.getWindow(), false);

		setContentView(R.layout.activity_about_us);
	}

	@Override
	public void onBackPressed() {
		supportFinishAfterTransition();
	}
}

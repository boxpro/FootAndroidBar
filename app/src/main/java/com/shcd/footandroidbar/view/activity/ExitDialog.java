package com.shcd.footandroidbar.view.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;

/**退出Dialog*/
public class ExitDialog extends Dialog{
	public ExitDialog(Activity context) {
		super(context);
		final Activity activity = (Activity) context;
		new AlertDialog.Builder(context)
				.setTitle("退出")
				.setMessage("确定退出程序么？")
				.setPositiveButton("确定",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,int which) {
								dialog.dismiss();
								activity.finish();
							}
						})
				.setNegativeButton("取消", null)
				.show();
	}
}

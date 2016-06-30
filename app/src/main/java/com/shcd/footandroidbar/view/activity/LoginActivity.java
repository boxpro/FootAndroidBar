package com.shcd.footandroidbar.view.activity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.shcd.footandroidbar.R;
import com.shcd.footandroidbar.entity.CompanyInfo;
import com.shcd.footandroidbar.entity.TouchGUIVO;
import com.shcd.footandroidbar.http.HttpConnectUtil;
import com.shcd.footandroidbar.util.Const;
import com.shcd.footandroidbar.util.MyConverter;
import com.shcd.footandroidbar.util.Util;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends BaseActivity {
	private String UPDATE_SERVERAPK = "FootAndroidBar.apk";
	private ProgressDialog progressDialog;//下载更新时的进度条
	private Handler handler = new Handler();
	private SharedPreferences sp=null;
	private EditText accountEdit;
	private EditText passEdit;
	private CheckBox checkBox;
	private Button registerBtn;
	private ImageButton loginBtn;
	private ProgressBar progressBar;
	private String account,pass;
	//初始化版本号跟版本信息
	private String versionCode="1.0";
	private String updateInfos="";
	private TextView versionText;
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			new ExitDialog(LoginActivity.this);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bar_login_view);
		sp=getSharedPreferences("bar_infos", 0);
		findViewById();
		setListener();
	}
	
	/**
	 * 加载UI界面
	 * @author 曾杰 2014-6-3 下午2:45:08
	 */
	private void findViewById(){
		accountEdit=(EditText) findViewById(R.id.bar_login_account_edit);
		passEdit=(EditText) findViewById(R.id.bar_login_pass_edit);
		checkBox=(CheckBox) findViewById(R.id.bar_check_remeber);
		registerBtn=(Button) findViewById(R.id.bar_register_button);
		loginBtn=(ImageButton) findViewById(R.id.bar_login_button);
		progressBar=(ProgressBar) findViewById(R.id.bar_login_progress);
		versionText=(TextView) findViewById(R.id.bar_login_version);
	}
	
	/**
	 * 设置监听事件
	 * @author 曾杰 2014-6-3 下午2:51:46
	 */
	private void setListener(){
		initSpData();
		versionText.setText("v"+getVersion());
		//注册
		registerBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				RegisterDialog dialog=new RegisterDialog(LoginActivity.this);
				dialog.show();
			}
		});
		
		//登陆
		loginBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				account=accountEdit.getText().toString().trim();
				pass=passEdit.getText().toString();
				if ("".equalsIgnoreCase(account)) {
					Toast.makeText(LoginActivity.this, "对不起,公司编号不能为空!", Toast.LENGTH_SHORT).show();
					return;
				} else if ("".equalsIgnoreCase(pass)) {
					Toast.makeText(LoginActivity.this, "对不起,登录密码不能为空!", Toast.LENGTH_SHORT).show();
					return;
				}
				if (checkBox.isChecked()) {
					sp.edit().putString("account", account)
					.putString("pass", pass)
					.putBoolean("check", true).commit();
				}else{
					sp.edit().putString("account", "")
					.putString("pass", "")
					.putBoolean("check", false).commit();
				}
				progressBar.setVisibility(View.VISIBLE);
				GetVersionAsyncTask task=new GetVersionAsyncTask(LoginActivity.this);
				task.execute();
			}
		});
		
		
	}
	
	/**
	 * 从sp中获取保存的信息
	 * @author 曾杰 2014-6-3 下午4:17:59
	 */
	private void initSpData(){
		accountEdit.setText(sp.getString("account", ""));
		passEdit.setText(sp.getString("pass", ""));
		if (sp.getBoolean("check", false)) {
			checkBox.setChecked(true);
		}else{
			checkBox.setChecked(false);
		}
	}
	
	/**
	 * 异步线程进行水吧登录
	 * touchBarLogin.action
	 * @param code  公司code
	 * @param pwd 密码
	 * @param mobile_type 手机类型   0：android  1.ios
	 * @param sys_version 手机系统版本
	 * @param version 手机客户端的版本
	 * @return Results<TouchGUIVO>
	 * */
	class LoginAsyncTask extends AsyncTask<Void, Void, JSONObject>{

		@Override
		protected void onPostExecute(JSONObject result) {
			if (result==null) {
				Toast.makeText(LoginActivity.this, getString(R.string.exception), Toast.LENGTH_SHORT).show();
			}else{
				try {
					if (result.getInt("errorCode")==0) {
						String json=result.getString("result");
						if (json==null||"null".equalsIgnoreCase(json)) {
							
						}else{
							Gson gson=new Gson();
							TouchGUIVO guivo=gson.fromJson(json, TouchGUIVO.class);
							if (guivo!=null) {
								CompanyInfo companyInfo=guivo.getCompanyInfo();
								if (companyInfo!=null) {
									sp.edit().putString("companyName", companyInfo.getShortName()).putLong("companyID", companyInfo.getId()).putBoolean("isBluetooth",companyInfo.isBluetooth()).commit();
									Intent intent=new Intent();
									intent.setClass(LoginActivity.this, MainActivity.class);
									startActivity(intent);
								}else{
									Toast.makeText(LoginActivity.this, "对不起,登陆失败!", Toast.LENGTH_SHORT).show();
								}
							}else{
								Toast.makeText(LoginActivity.this, "对不起,登陆失败!", Toast.LENGTH_SHORT).show();
							}
						}
					}else{
						Toast.makeText(LoginActivity.this, result.getString("errorMsg"), Toast.LENGTH_SHORT).show();
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			progressBar.setVisibility(View.GONE);
		}

		@Override
		protected JSONObject doInBackground(Void... params) {
			Map<String, String> data=new HashMap<String, String>();
			data.clear();
			data.put("code", account);
			data.put("pwd", pass);
			data.put("mobile_type", "0");
			data.put("sys_version", getSysVersion());
			data.put("version", getVersion());
			String path=sp.getString("ip", "")+"/touchBarLogin.action?";
			JSONObject jsonObject=HttpConnectUtil.getJSONObjectData(path, data);
			return jsonObject;
		}
		
	}
	
	/**根据注册码获取IP**/
	class PortAsyncTask extends AsyncTask<String, Void, JSONObject> {
		@Override
		protected void onPostExecute(JSONObject result) {
			if (result==null) {
				Toast.makeText(LoginActivity.this, getString(R.string.exception), Toast.LENGTH_SHORT).show();
			}else{
				try {
					if (result.getInt("errorCode")==0) {
						String json=result.getString("result");//Util.decrypt(MyConverter.unescape(json))
						//Log.v("ip===", "==ip=="+Util.decrypt(MyConverter.unescape(json)));
						sp.edit().putString("ip", Util.decrypt(MyConverter.unescape(json)))
						.putBoolean("flag", true)
						.commit();
					}else{
						Toast.makeText(LoginActivity.this, result.getString("errorMsg"), Toast.LENGTH_SHORT).show();
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			progressBar.setVisibility(View.GONE);
		}

		@Override
		protected JSONObject doInBackground(String... params) {
			Map<String, String> data=new HashMap<String, String>();
			data.put("registerCode", params[0]);
			String path =Const.PUBLIC_IP+"/getIpByCode_Ex.action?";
			JSONObject jsonObject=HttpConnectUtil.getJSONObjectData(path, data);
			return jsonObject;
		}
	}
	
	/**获取终端版本号*/
	class GetVersionAsyncTask extends AsyncTask<Void, Void, JSONObject>{
		Context context;
		public GetVersionAsyncTask(Context context) {
			this.context=context;
		}
		@Override
		protected void onPostExecute(JSONObject result) {
			if (result==null) {
				progressBar.setVisibility(View.GONE);
				Toast.makeText(context, "对不起，获取系统中最新版本号失败!", Toast.LENGTH_SHORT).show();
			}else{
				try {
					if (result.getInt("errorCode")==0) {
						JSONObject jsonObject=result.getJSONObject("result");
						if (jsonObject==null) {
							progressBar.setVisibility(View.GONE);
						}else{
							versionCode=jsonObject.getString("sysVersion");
							updateInfos=jsonObject.getString("versionContent");
						}
					}else{
						progressBar.setVisibility(View.GONE);
						Toast.makeText(context, "对不起，获取系统中最新版本号失败!", Toast.LENGTH_SHORT).show();
					}
				} catch (JSONException e) {
					progressBar.setVisibility(View.GONE);
					e.printStackTrace();
				}
			}
			
			if (versionCode.compareToIgnoreCase(getVersion()) <= 0) {
				if (sp.getBoolean("flag", false)) {
					//启动登陆线程
					LoginAsyncTask loginAsyncTask=new LoginAsyncTask();
					loginAsyncTask.execute();
				}else{
					progressBar.setVisibility(View.GONE);
					RegisterDialog dialog=new RegisterDialog(LoginActivity.this);
					dialog.show();
				}
			} else {
				progressBar.setVisibility(View.GONE);
				doNewVersionUpdate();
			}
		}

		@Override
		protected JSONObject doInBackground(Void... params) {
			Map<String, String> data=new HashMap<String, String>();
			data.clear();
			data.put("appName", UPDATE_SERVERAPK);
			String path=Const.PUBLIC_IP+"/getTerminalVersion.action?";
			JSONObject list=HttpConnectUtil.getJSONObjectData(path, data);
			return list;
		}
	}
	
	/**弹出Dialog输入注册码*/
	class RegisterDialog extends Dialog{
		private EditText registerEdit;
		private String register;
		private Button sureButton,cancleButton;

		public RegisterDialog(Context context) {
			super(context);
		}
		
		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
			setContentView(R.layout.bar_dialog_view);
			registerEdit=(EditText) findViewById(R.id.bar_register_edit);
			sureButton=(Button) findViewById(R.id.bar_register_sure);
			cancleButton=(Button) findViewById(R.id.bar_register_cancle);
			registerEdit.setText(sp.getString("register", ""));
			//获取IP
			sureButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					register=registerEdit.getText().toString().trim();
					if ("".equalsIgnoreCase(register)) {
						Toast.makeText(LoginActivity.this, "对不起,注册码不能为空!", Toast.LENGTH_SHORT).show();
						return;
					}
					sp.edit().putString("register", register).commit();
					progressBar.setVisibility(View.VISIBLE);
					PortAsyncTask task=new PortAsyncTask();
					task.execute(register);
					RegisterDialog.this.dismiss();
				}
			});
			
			//取消
			cancleButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					RegisterDialog.this.dismiss();
				}
			});
		}
		
	}
	
	/** 版本更新信息 */
	private void doNewVersionUpdate() {
		LayoutInflater inflater = getLayoutInflater();
		final View layout = inflater.inflate(R.layout.bar_update_info_view,(ViewGroup) findViewById(R.id.newfeat));
		TextView titleView=(TextView) layout.findViewById(R.id.update_title);
		TextView contentView=(TextView) layout.findViewById(R.id.update_info);
		titleView.setText("您的版本可以升级为：" +versionCode);
		contentView.setText(updateInfos);

		Dialog dialog = new AlertDialog.Builder(LoginActivity.this)
				.setTitle("更新提示").setView(layout)
				// 设置内容
				.setPositiveButton("更新",// 设置确定按钮
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,int which) {
								progressDialog = new ProgressDialog(LoginActivity.this);
								progressDialog.setTitle("正在下载");
								progressDialog.setMessage("请稍候...");
								progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
								downFile(Const.PUBLIC_IP+"/apk/"+UPDATE_SERVERAPK);
							}
						}).create();// 创建
		dialog.show();
	}
	
	/** 获取版本号 */
	public String getVersion() {
		try {
			PackageManager manager = this.getPackageManager();
			PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
			String version = info.versionName;
			return version;
		} catch (Exception e) {
			e.printStackTrace();
			return "获取版本号失败!";
		}
	}
	
	/**
	 * 获取系统版本号
	 * @author 曾杰 2014-5-13 上午11:11:28
	 */
	private String getSysVersion(){
		return android.os.Build.VERSION.RELEASE;
	}
	
	/** 下载新的版本 */
	void downFile(final String url) {
		progressDialog.show();
		new Thread() {
			public void run() {
				HttpClient client = new DefaultHttpClient();
				HttpGet get = new HttpGet(url);
				HttpResponse response;
				try {
					response = client.execute(get);
					HttpEntity entity = response.getEntity();
					InputStream is = entity.getContent();
					FileOutputStream fileOutputStream = null;
					if (is != null) {
						File file = new File(Environment.getExternalStorageDirectory(),UPDATE_SERVERAPK);
						fileOutputStream = new FileOutputStream(file);
						byte[] buf = new byte[1024];
						int ch = -1;
						while ((ch = is.read(buf)) != -1) {
							fileOutputStream.write(buf, 0, ch);
						}
					}
					fileOutputStream.flush();
					if (fileOutputStream != null) {
						fileOutputStream.close();
					}
					down();
				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}.start();

	}

	void down() {
		handler.post(new Runnable() {
			public void run() {
				progressDialog.cancel();
				update();
			}
		});

	}

	void update() {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(new File(Environment.getExternalStorageDirectory(), UPDATE_SERVERAPK)),"application/vnd.android.package-archive");
		startActivity(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}

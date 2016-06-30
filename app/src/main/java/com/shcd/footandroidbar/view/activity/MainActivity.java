package com.shcd.footandroidbar.view.activity;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.shcd.footandroidbar.R;
import com.shcd.footandroidbar.entity.PadCashRoomMessageInfo;
import com.shcd.footandroidbar.entity.PadTeaRoomMessageInfo;
import com.shcd.footandroidbar.http.HttpConnectUtil;
import com.shcd.footandroidbar.util.BluetoothPrintFormatUtil;
import com.shcd.footandroidbar.util.PrintUtil;
import com.zj.btsdk.BluetoothService;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends BaseActivity{
	private ListView listView;
	private ProgressBar progressBar;
	private Button notReadBtn,alReadBtn,configPrintBtn;
	private SharedPreferences sp=null;
	private FootBarAdapter footAdapter=null;
	private static MediaPlayer mediaPlayer;
	private TextView versionText;
	private RadioGroup barRadioGroup;
	private RadioButton footRadioButton;
	private RadioButton teaRadioButton;
	private TeaBarAdapter teaAdapter=null;
	//下面的主要为蓝牙服务配置相关
	private boolean isRun = true;
	private boolean isPrintRun = false;
	private BluetoothService mService = null;
	private BluetoothDevice con_dev = null;
	private static final int REQUEST_CONNECT_DEVICE = 1;  //获取设备消息
	private static final int REQUEST_ENABLE_BT = 2;


	/**
	 * 用于判断是查询已读消息还是未读信息
	 * true代表已读消息
	 * false代表未读信息
	 * **/
	private Boolean flag=false;
	private Handler handler=new Handler();
	private Runnable runnable=new Runnable() {
		@Override
		public void run() {
			if (isRun){
				searchNotRead();
			}

		}
	};
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			closeTimer();
			MainActivity.this.finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bar_main_view);
		sp=getSharedPreferences("bar_infos", 0);
		mediaPlayer=MediaPlayer.create(this, R.raw.newmessage);
		findViewById();
		setListener();
		initPrintService();
		searchNotRead();
	}
	
	/**
	 * 加载UI界面
	 * @author 曾杰 2014-6-4 上午9:11:57
	 */
	private void findViewById(){
		listView=(ListView) findViewById(R.id.bar_main_list);
		progressBar=(ProgressBar) findViewById(R.id.bar_main_progress);
		notReadBtn=(Button) findViewById(R.id.bar_main_not_read);
		alReadBtn=(Button) findViewById(R.id.bar_main_al_read);
		versionText=(TextView) findViewById(R.id.bar_main_version);
		barRadioGroup=(RadioGroup) findViewById(R.id.bar_type_choose_radiogroup);
		footRadioButton=(RadioButton) findViewById(R.id.bar_type_choose_foot_radio);
		teaRadioButton=(RadioButton) findViewById(R.id.bar_type_choose_tea_radio);
		configPrintBtn =(Button) findViewById(R.id.config_blue_tooth);
	}
	
	/**
	 * 设置监听事件
	 * @author 曾杰 2014-6-4 上午9:13:53
	 */
	private void setListener(){
		versionText.setText("v"+getVersion());
		if (sp.getBoolean("choosebar", true)) {
			footRadioButton.setChecked(true);
			teaRadioButton.setChecked(false);
		} else {
			footRadioButton.setChecked(false);
			teaRadioButton.setChecked(true);
		}

		
		//已读消息
		alReadBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				searchAlRead();
			}
		});
		
		//未读信息
		notReadBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				searchNotRead();
			}
		});
		
		//选择类型
		barRadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if (footRadioButton.getId()==checkedId) {
					sp.edit().putBoolean("choosebar", true).commit();
				} else if (teaRadioButton.getId()==checkedId) {
					sp.edit().putBoolean("choosebar", false).commit();
				}
			}
		});

		//配置蓝牙服务
		configPrintBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				initBlueBoothService();
			}
		});
	}
	
	/**
	 * 播放音乐
	 * @author 曾杰 2014-6-4 下午1:58:43
	 */
	private void playMedia(){
		if (mediaPlayer!=null) {
			mediaPlayer.stop();

		}
		try {
			mediaPlayer.prepare();
			mediaPlayer.start();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * 查询已读消息
	 * @author 曾杰 2014-6-4 下午1:21:33
	 */
	private void searchAlRead(){
		clearChoose();
		flag=true;
		alReadBtn.setTextColor(Color.parseColor("#FFFFFF"));
		progressBar.setVisibility(View.VISIBLE);
		if (sp.getBoolean("choosebar", true)) {
			SearchFootBarTask task=new SearchFootBarTask();
			task.execute("true");
		} else {
			SearchTeaBarTask task=new SearchTeaBarTask();
			task.execute("true");
		}
		
	}
	
	/**
	 * 查询未读信息
	 * @author 曾杰 2014-6-4 下午1:22:00
	 */
	private void searchNotRead(){
		clearChoose();
		flag=false;
		notReadBtn.setTextColor(Color.parseColor("#FFFFFF"));
		progressBar.setVisibility(View.VISIBLE);
		if (sp.getBoolean("choosebar", true)) {
			SearchFootBarTask task=new SearchFootBarTask();
			task.execute("false");
		} else {
			SearchTeaBarTask task=new SearchTeaBarTask();
			task.execute("false");
		}
		
	}
	/**异步线程查询茶吧信息**/
	class SearchTeaBarTask extends AsyncTask<String, Void, JSONObject>{

		@Override
		protected void onPostExecute(JSONObject result) {
			if (result==null) {
				Toast.makeText(MainActivity.this, getString(R.string.exception), Toast.LENGTH_SHORT).show();
			}else{
				try {
					if (result.getInt("errorCode")==0) {
						List<PadTeaRoomMessageInfo> datas=new ArrayList<PadTeaRoomMessageInfo>();
						datas.clear();
						String json=result.getString("resultSet");
						if (json==null||"null".equalsIgnoreCase(json)) {
							
						}else{
							Gson gson=new Gson();
							Type listType = new TypeToken<LinkedList<PadTeaRoomMessageInfo>>(){}.getType();
							LinkedList<PadTeaRoomMessageInfo> lsMessageInfos = gson.fromJson(json, listType);
							for(Iterator<PadTeaRoomMessageInfo> iterator=lsMessageInfos.iterator();iterator.hasNext();){
								PadTeaRoomMessageInfo messageInfo=iterator.next();
								datas.add(messageInfo);
							}
							if (!flag&&datas.size()!=0) {
								playMedia();
							}
							if (datas.size()>0){
								if (sp.getBoolean("isBluetooth",true)){
									executePrint(BluetoothPrintFormatUtil.printTeaBaData(datas,sp.getString("companyName","")));
								}
							}
						  }
						teaAdapter=new TeaBarAdapter(datas);
						listView.setAdapter(teaAdapter);
					}else{
						Toast.makeText(MainActivity.this, result.getString("errorMsg"), Toast.LENGTH_SHORT).show();
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			if (handler!=null) {
				handler.postDelayed(runnable,10000);
				Log.d("time",System.currentTimeMillis()+"");
			}
			progressBar.setVisibility(View.GONE);
		}









		@Override
		protected JSONObject doInBackground(String... params) {
			Map<String, String> data=new HashMap<String, String>();
			data.clear();
			data.put("company_id", String.valueOf(sp.getLong("companyID", 0)));
			data.put("read_flag", params[0]);
			String path=sp.getString("ip", "")+"/loadUnreadPadTeaRoomMessageInfo.action?";
			JSONObject jsonObject=HttpConnectUtil.getJSONObjectData(path, data);
			return jsonObject;
		}
		
	}
	
	/**重写适配器显示茶吧的信息**/
	class TeaBarAdapter extends BaseAdapter{
		List<PadTeaRoomMessageInfo> datas;
		
		public TeaBarAdapter(List<PadTeaRoomMessageInfo> datas) {
			this.datas=datas;
		}

		@Override
		public int getCount() {
			return datas.size();
		}

		@Override
		public Object getItem(int position) {
			return datas.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View view, ViewGroup parent) {
			ViewHolder vh=null;
			if (view==null) {
				vh=new ViewHolder();
				view=getLayoutInflater().inflate(R.layout.bar_main_item, null);
				vh.roomCodeText=(TextView) view.findViewById(R.id.bar_main_code_item);
				vh.countText=(TextView) view.findViewById(R.id.bar_main_count_item);
				vh.messagerText=(TextView) view.findViewById(R.id.bar_main_messager_item);
				vh.handleButton=(Button) view.findViewById(R.id.bar_main_sure_item);
				vh.timeText=(TextView) view.findViewById(R.id.bar_main_time_item);
				if (flag) {
					vh.handleButton.setVisibility(View.GONE);
				}
				view.setTag(vh);
			}else{
				vh=(ViewHolder) view.getTag();
			}
			
			final PadTeaRoomMessageInfo messageInfo=datas.get(position);
			if (messageInfo!=null) {
				vh.roomCodeText.setText(messageInfo.getRoomCode());
				vh.countText.setText(messageInfo.getProductNum());
				vh.messagerText.setText(messageInfo.getMessageContent());
				vh.timeText.setText(messageInfo.getInputTm());
				//确认
				vh.handleButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						MakeSureTeaTask task=new MakeSureTeaTask();
						task.execute(messageInfo.getId().toString());
					}
				});
			}
			return view;
		}
		
		private class ViewHolder{
			TextView roomCodeText;
			TextView countText;
			TextView messagerText;
			TextView timeText;
			Button handleButton;
		}
		
		/**点击收到后删除信息*/
		public void remove(Long id){
			Iterator<PadTeaRoomMessageInfo> iterator=datas.iterator();
			while (iterator.hasNext()) {
				PadTeaRoomMessageInfo messageInfo=iterator.next();
				if (messageInfo.getId().intValue()==id.intValue()) {
					iterator.remove();
				}
			}
		}
	}
	
	/**茶吧点击确认***/
	class MakeSureTeaTask extends AsyncTask<String, Void, JSONObject>{
		@Override
		protected void onPostExecute(JSONObject result) {
			if (result==null) {
				Toast.makeText(MainActivity.this, getString(R.string.exception), Toast.LENGTH_SHORT).show();
			}else{
				try {
					if (result.getInt("errorCode")==0) {
						String json=result.getString("result");
						if (json==null||"null".equalsIgnoreCase(json)) {
							
						}else{
							Gson gson=new Gson();
							PadTeaRoomMessageInfo messageInfo=gson.fromJson(json, PadTeaRoomMessageInfo.class);
							if (messageInfo!=null) {
								teaAdapter.remove(messageInfo.getId());
								teaAdapter.notifyDataSetChanged();
							}
						}
					}else{
						Toast.makeText(MainActivity.this, result.getString("errorMsg"), Toast.LENGTH_SHORT).show();
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			progressBar.setVisibility(View.GONE);
		}

		@Override
		protected JSONObject doInBackground(String... params) {
			Map<String, String> data=new HashMap<String, String>();
			data.clear();
			data.put("target_id", params[0]);
			String path=sp.getString("ip", "")+"/updatePadTeaRoomMessafeInfoStatus.action?";
			JSONObject jsonObject=HttpConnectUtil.getJSONObjectData(path, data);
			return jsonObject;
		}
		
	}









	/**异步线程查询水吧平板信息*/
	class SearchFootBarTask extends AsyncTask<String, Void, JSONObject>{
		@Override
		protected void onPostExecute(JSONObject result) {
			if (result==null) {
				Toast.makeText(MainActivity.this, getString(R.string.exception), Toast.LENGTH_SHORT).show();
			}else{
				try {
					if (result.getInt("errorCode")==0) {
						List<PadCashRoomMessageInfo> datas=new ArrayList<PadCashRoomMessageInfo>();
						datas.clear();
						String json=result.getString("resultSet");
						if (json==null||"null".equalsIgnoreCase(json)) {
							
						}else{
							Gson gson=new Gson();
							Type listType = new TypeToken<LinkedList<PadCashRoomMessageInfo>>(){}.getType();
							LinkedList<PadCashRoomMessageInfo> lsMessageInfos = gson.fromJson(json, listType);
							for(Iterator<PadCashRoomMessageInfo> iterator=lsMessageInfos.iterator();iterator.hasNext();){
								PadCashRoomMessageInfo messageInfo=iterator.next();
								datas.add(messageInfo);
							}
							if (!flag&&datas.size()!=0) {
								playMedia();
							}
							if (datas.size()>0){
//								if (sp.getBoolean("isBluetooth",true)){
									executePrint(BluetoothPrintFormatUtil.printCashBaData(datas,sp.getString("companyName","")));
//								}
							}


						}
						footAdapter=new FootBarAdapter(datas);
						listView.setAdapter(footAdapter);
					}else{
						Toast.makeText(MainActivity.this, result.getString("errorMsg"), Toast.LENGTH_SHORT).show();
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			if (handler!=null) {
				handler.postDelayed(runnable, 10000);
				
			}
			progressBar.setVisibility(View.GONE);
		}

		@Override
		protected JSONObject doInBackground(String... params) {
			Map<String, String> data=new HashMap<String, String>();
			data.clear();
			data.put("company_id", String.valueOf(sp.getLong("companyID", 0)));
			data.put("read_flag", params[0]);
			String path=sp.getString("ip", "")+"/loadUnreadPadCashRoomMessageInfo.action?";
			JSONObject jsonObject=HttpConnectUtil.getJSONObjectData(path, data);
			return jsonObject;
		}
		
	}
	
	/**重写适配器*/
	class FootBarAdapter extends BaseAdapter{
		List<PadCashRoomMessageInfo> datas;
		
		public FootBarAdapter(List<PadCashRoomMessageInfo> datas) {
			this.datas=datas;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return datas.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return datas.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View view, ViewGroup parent) {
			ViewHolder vh=null;
			if (view==null) {
				vh=new ViewHolder();
				view=getLayoutInflater().inflate(R.layout.bar_main_item, null);
				vh.roomCodeText=(TextView) view.findViewById(R.id.bar_main_code_item);
				vh.countText=(TextView) view.findViewById(R.id.bar_main_count_item);
				vh.messagerText=(TextView) view.findViewById(R.id.bar_main_messager_item);
				vh.handleButton=(Button) view.findViewById(R.id.bar_main_sure_item);
				vh.timeText=(TextView) view.findViewById(R.id.bar_main_time_item);
				if (flag) {
					vh.handleButton.setVisibility(View.GONE);
				}
				view.setTag(vh);
			}else{
				vh=(ViewHolder) view.getTag();
			}
			
			final PadCashRoomMessageInfo messageInfo=datas.get(position);
			if (messageInfo!=null) {
				vh.roomCodeText.setText(messageInfo.getRoomCode());
				vh.countText.setText(messageInfo.getProductNum());
				vh.messagerText.setText(messageInfo.getMessageContent());
				vh.timeText.setText(messageInfo.getInputTm());
				//确认
				vh.handleButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						MakeSureFootTask task=new MakeSureFootTask();
						task.execute(messageInfo.getId().toString());
					}
				});
			}
			return view;
		}
		
		private class ViewHolder{
			TextView roomCodeText;
			TextView countText;
			TextView messagerText;
			TextView timeText;
			Button handleButton;
		}
		
		/**点击收到后删除信息*/
		public void remove(Long id){
			Iterator<PadCashRoomMessageInfo> iterator=datas.iterator();
			while (iterator.hasNext()) {
				PadCashRoomMessageInfo messageInfo=iterator.next();
				if (messageInfo.getId().intValue()==id.intValue()) {
					iterator.remove();
				}
			}
		}
		
	}
	
	/**异步线程水吧点击确认收到*/
	class MakeSureFootTask extends AsyncTask<String, Void, JSONObject>{
		@Override
		protected void onPostExecute(JSONObject result) {
			if (result==null) {
				Toast.makeText(MainActivity.this, getString(R.string.exception), Toast.LENGTH_SHORT).show();
			}else{
				try {
					if (result.getInt("errorCode")==0) {
						String json=result.getString("result");
						if (json==null||"null".equalsIgnoreCase(json)) {
							
						}else{
							Gson gson=new Gson();
							PadCashRoomMessageInfo messageInfo=gson.fromJson(json, PadCashRoomMessageInfo.class);
							if (messageInfo!=null) {
								footAdapter.remove(messageInfo.getId());
								footAdapter.notifyDataSetChanged();
							}
						}
					}else{
						Toast.makeText(MainActivity.this, result.getString("errorMsg"), Toast.LENGTH_SHORT).show();
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			progressBar.setVisibility(View.GONE);
		}

		@Override
		protected JSONObject doInBackground(String... params) {
			Map<String, String> data=new HashMap<String, String>();
			data.clear();
			data.put("target_id", params[0]);
			String path=sp.getString("ip", "")+"/updatePadCashRoomMessafeInfoStatus.action?";
			JSONObject jsonObject=HttpConnectUtil.getJSONObjectData(path, data);
			return jsonObject;
		}
		
	}
	
	@Override
	protected void onDestroy() {//退出MediaPlayer
		if (mediaPlayer!=null) {
			mediaPlayer.release();
	    	mediaPlayer = null;
		}
		super.onDestroy();
		if (mService != null)
			mService.stop();
		mService = null;
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
	 * 清除选中状态
	 * @author 曾杰 2014-6-4 下午1:16:47
	 */
	private void clearChoose(){
		alReadBtn.setTextColor(Color.parseColor("#666666"));
		notReadBtn.setTextColor(Color.parseColor("#666666"));
	}
	
	/**
	 * 关闭Timer
	 * @author 曾杰 2014-7-9 上午9:29:45
	 */
	private void closeTimer(){
		if (handler!=null) {
			handler.removeCallbacks(runnable);//停止
		}
		handler=null;
	}






	private synchronized void executePrint(String message){
		if(con_dev!=null){//如果当前设备为空则，不执行打印服务
			byte[] cmd = new byte[3];
			cmd[0] = 0x1b;
			cmd[1] = 0x21;
			cmd[2] |= 0x10;
			mService.write(cmd);
			mService.write(PrintUtil.byteFontToBig());
			mService.sendMessage(message, "GBK");
			//xmService.write(PrintUtil.byteFontToSmall());
			cmd[2] &= 0xEF;//取消加宽
			mService.write(cmd);
			mService.sendMessage("支持:创度软件科技上海有限公司\n", "GBK");

		}

	}

	/**
	 * 调用打印机服务
	 */
	private void initPrintService(){
		mService = new BluetoothService(this, mHandler);
		//蓝牙不可用退出程序
		if( mService.isAvailable() == false ){
			Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
			finish();
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case REQUEST_ENABLE_BT:      //请求打开蓝牙
				if (resultCode == Activity.RESULT_OK) {   //蓝牙已经打开
					Toast.makeText(this, "Bluetooth open successful", Toast.LENGTH_LONG).show();
				} else {                 //用户不允许打开蓝牙
					finish();
				}
				break;
			case  REQUEST_CONNECT_DEVICE:     //请求连接某一蓝牙设备
				if (resultCode == Activity.RESULT_OK) {   //已点击搜索列表中的某个设备项
					String address = data.getExtras()
							.getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);  //获取列表项中设备的mac地址
					con_dev = mService.getDevByMac(address);
					mService.connect(con_dev);
				}
				break;
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		if( mService.isBTopen() == false){
			Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
		}
	}

	private final  Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case BluetoothService.MESSAGE_STATE_CHANGE:
					switch (msg.arg1) {
						case BluetoothService.STATE_CONNECTED:   //已连接
							Toast.makeText(getApplicationContext(), "打印设备已经连接成功！",
									Toast.LENGTH_SHORT).show();
							break;
						case BluetoothService.STATE_CONNECTING:  //正在连接
							Log.d("蓝牙调试","正在连接.....");
							break;
						case BluetoothService.STATE_LISTEN:     //监听连接的到来
						case BluetoothService.STATE_NONE:
							Log.d("蓝牙调试","等待连接.....");
							break;
					}
					break;
				case BluetoothService.MESSAGE_CONNECTION_LOST:    //蓝牙已断开连接
					Toast.makeText(getApplicationContext(), "Device connection was lost",
							Toast.LENGTH_SHORT).show();
					break;
				case BluetoothService.MESSAGE_UNABLE_CONNECT:     //无法连接设备
					Toast.makeText(getApplicationContext(), "Unable to connect device",
							Toast.LENGTH_SHORT).show();
					break;
			}
		}

	};

	private void initBlueBoothService(){
		Intent serverIntent = new Intent(MainActivity.this,DeviceListActivity.class);      //运行另外一个类的活动
		startActivityForResult(serverIntent,REQUEST_CONNECT_DEVICE);
	}


}

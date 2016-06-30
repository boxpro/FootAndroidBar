package com.shcd.footandroidbar.http;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

public class HttpConnectUtil {

	/**
	 * @param path
	 * @param params
	 * 通过map集合传参获取JSONObject对象
	 * */
	public static JSONObject getJSONObjectData(String path,Map<String, String> params) {
		HttpClient httpclient = new DefaultHttpClient();
		httpclient.getParams().setIntParameter(HttpConnectionParams.SO_TIMEOUT, 60000);// 超时设置
		//设置网络连接超时
		httpclient.getParams().setIntParameter(HttpConnectionParams.CONNECTION_TIMEOUT, 60000);
		// 你的URL
		HttpPost httppost = new HttpPost(path);
		try {
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(params.size());

			for (Map.Entry<String, String> entry : params.entrySet()) {// 构建表单字段内容
				nameValuePairs.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
			}
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs,HTTP.UTF_8));
			HttpResponse response;
			response = httpclient.execute(httppost);
			InputStream inStream = response.getEntity().getContent();
			return parseJSONData(inStream);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 解析JSON数据
	 * 返回JSONObject
	 * @param inStream
	 * @return
	 */
	private static JSONObject parseJSONData(InputStream inStream)throws Exception {
		byte[] data = StreamTool.read(inStream);
		String json = new String(data);
		JSONObject jsonObject=new JSONObject(json);
		return jsonObject;
	}
}

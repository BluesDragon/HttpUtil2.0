package com.dragon.util;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends Activity {

	private HttpUtil httpUtil;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		test();
	}
	
	private void test(){
		String url = "http://127.0.0.1:18080/query?reqinfo=<req><data>{\"action\":\"playauth\",\"mid\":\"\",\"sid\":\"1\",\"fid\":\"ec733aee0b41344a5fc0dfdc3db8014f\",\"pid\":\"\",\"playtype\":\"\",\"ext\":\"vosp://cdn.voole.com:3528/play?fid=ec733aee0b41344a5fc0dfdc3db8014f&keyid=0&stamp=1411379162&auth=6caba6f89099539475e09a81e9696066&s=1&ext=oid:817,eid:100895,code:cate_hkzt_dsj_1408084722\"}</data></req>";
//		String url = "http://interfaceclientzhibosy.voole.com/b2b/filmlist.php?spid=20120629&epgid=100895&ctype=2&column=11972&ispay=1&oemid=817&hid=5CC6D0998A97&uid=1371181";
		
		httpUtil.get(url, new AjaxCallBack<Object>(){
			@Override
			public void onStart() {
				super.onStart();
				Log.d("test", "onStart");
			}
			@Override
			public void onSuccess(Object resutl) {
				super.onSuccess(resutl);
				Log.d("test", "onSuccess-->resutl:" + resutl);
//				textView.setText(resutl);
			}
			@Override
			public void onFailure(Throwable t, int errorNo, String strMsg) {
				super.onFailure(t, errorNo, strMsg);
				Log.d("test", "onFailure");
			}
			@Override
			public Object onParse(String str) {
				Log.d("test", "onParse:" + str);
				return new Object();
			}
			
		}, true);
	}


}

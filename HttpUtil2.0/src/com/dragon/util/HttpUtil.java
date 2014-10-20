package com.dragon.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import android.os.AsyncTask;
/**
 * 网络请求处理类
 * 
 * @author Dragon
 * @version 1.0
 */
public class HttpUtil {
	
	private static int connectTimeout = 15 * 1000;
	private static int readTimeout = 15 * 1000;
	
	private static int httpThreadCount = 3;//http线程池数量
	
	private static final ThreadFactory  sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);
        public Thread newThread(Runnable r) {
        	Thread tread = new Thread(r, "HttpUtil #" + mCount.getAndIncrement());
        	tread.setPriority(Thread.NORM_PRIORITY - 1);
            return tread;
        }
    };
	
	private static final Executor executor = Executors.newFixedThreadPool(httpThreadCount, sThreadFactory);
	
	/**
	 * 网络GET请求：
	 * 
	 * @param url 请求地址
	 * @param callBack 回调
	 */
	public <T> HttpHandler<T> get(String url, HttpCallBack<T> callBack){
		return get(url, callBack, false);
	}
	
	/**
	 * 网络GET请求：
	 * 
	 * @param url 请求地址
	 * @param callBack 回调
	 * @param isNeedParse 是否需要解析
	 */
	public <T> HttpHandler<T> get(String url, HttpCallBack<T> callBack, boolean isNeedParse){
		HttpHandler<T> httpHandler = new HttpHandler<T>(callBack, isNeedParse, connectTimeout, readTimeout);
		httpHandler.executeOnExecutor(executor, url);
		return httpHandler;
	}
	
	/**
	 * 同步请求网络，不区分线程，所以调用环境必须为子线程。
	 * 
	 * @param url 请求地址
	 * @return 返回结果
	 */
	public <T> String getSync(String url){
		HttpURLConnection conn = null;
		InputStream urlStream = null;
		try {
			conn = (HttpURLConnection) new URL(url).openConnection();
			conn.setConnectTimeout(connectTimeout);
			conn.setReadTimeout(readTimeout);
			conn.connect();
			if(conn.getResponseCode() == HttpURLConnection.HTTP_OK){
				urlStream = conn.getInputStream();
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlStream));
				String sCurrentLine = "";
				StringBuffer sb = new StringBuffer();
				while ((sCurrentLine = bufferedReader.readLine()) != null) {
					sb.append(sCurrentLine);
				}
				return sb.toString();
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(conn != null){
				conn.disconnect();
			}
			if(urlStream != null){
				try {
					urlStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return "";
	}
	
	/**
	 * 检查连接
	 * @param url 网络请求地址
	 * @return 是否连接正常
	 */
	public boolean checkConnect(String url){
		HttpURLConnection conn = null;
		InputStream urlStream = null;
		try {
			conn = (HttpURLConnection) new URL(url).openConnection();
			conn.setConnectTimeout(connectTimeout);
			conn.setReadTimeout(readTimeout);
			conn.connect();
			return conn.getResponseCode() == HttpURLConnection.HTTP_OK;
		} catch (Exception e) {
		} finally {
			if(conn != null){
				conn.disconnect();
			}
			if(urlStream != null){
				try {
					urlStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return false;
	}
	
	/**
	 * http请求处理者
	 * 
	 * @author Dragon
	 *
	 * @param <T> 泛型：Object
	 */
	public class HttpHandler <T> extends AsyncTask<Object, Object, Object>{
		
		private final HttpCallBack<T> callback;// 回调
		private boolean isNeedParse = false; //是否需要解析
		private int connectTimeout;// 连接超时时间
		private int readTimeout;// 数据读取超时时间
		private boolean isCancel = false;
		public void cancel(){
			isCancel = true;
			publishProgress(CANCEL); // 取消
		}
		
		/**
		 * HTTP请求处理者
		 * 
		 * @param callback 回调
		 * @param isNeedParse 是否需要解析
		 * @param connectTimeout 连接超时时间
		 * @param readTimeout 数据读取超时时间
		 */
		public HttpHandler(HttpCallBack<T> callback, boolean isNeedParse, int connectTimeout, int readTimeout){
			this.isNeedParse = isNeedParse;
			this.callback = callback;
			this.connectTimeout = connectTimeout;
			this.readTimeout = readTimeout;
		}

		@Override
		protected Object doInBackground(Object... params) {
			try {
				publishProgress(UPDATE_START); // 开始
				doRequest((String)params[0]);
			} catch (Exception e) {
				publishProgress(UPDATE_FAILURE,e,0,e.getMessage()); // 结束
			}
			return null;
		}
		
		/**
		 * 网络请求
		 * @param url 网络请求地址
		 * @throws Exception 异常
		 */
		private void doRequest(String url) throws Exception{
			HttpURLConnection conn = null;
			InputStream urlStream = null;
			try {
				conn = (HttpURLConnection) new URL(url).openConnection();
				conn.setConnectTimeout(connectTimeout);
				conn.setReadTimeout(readTimeout);
				conn.connect();
				if(conn.getResponseCode() == HttpURLConnection.HTTP_OK){
					urlStream = conn.getInputStream();
					BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlStream));
					String sCurrentLine = "";
					StringBuffer sb = new StringBuffer();
					while ((sCurrentLine = bufferedReader.readLine()) != null) {
						sb.append(sCurrentLine);
					}
					if(isNeedParse){
						Object obj = callback.onParse((String)sb.toString());
						publishProgress(UPDATE_SUCCESS, obj, isCancel);
					}else {
						publishProgress(UPDATE_SUCCESS, sb.toString(), isCancel);
					}
					conn.disconnect();
				}
			} catch (Exception e) {
				throw e;
			} finally{
				try {
					if(urlStream != null){
						urlStream.close();
						urlStream = null;
					}
				} catch (Exception e) {
				}
			}
		}
		
		private final static int UPDATE_START = 1;
		private final static int UPDATE_LOADING = 2;
		private final static int UPDATE_FAILURE = 3;
		private final static int UPDATE_SUCCESS = 4;
		private final static int CANCEL = 5;
		@SuppressWarnings("unchecked")
		@Override
		protected void onProgressUpdate(Object... values) {
			int update = Integer.valueOf(String.valueOf(values[0]));
			switch (update) {
			case UPDATE_START:
				if(callback!=null)
					callback.onStart();
				break;
			case UPDATE_LOADING:
				if(callback!=null)
					callback.onLoading(Long.valueOf(String.valueOf(values[1])),Long.valueOf(String.valueOf(values[2])));
				break;
			case UPDATE_FAILURE:
				if(callback!=null)
					callback.onFailure((Throwable)values[1],(Integer)values[2],(String)values[3]);
				break;
			case UPDATE_SUCCESS:
				if(callback!=null)
					callback.onSuccess((T)values[1], (Boolean)values[2]);
				break;
			case CANCEL:
				if(callback!=null)
					callback.onCancel();
				break;
			default:
				break;
			}
			super.onProgressUpdate(values);
		}

	}
}

package com.dragon.util;


/**
 * 
 * @author Dragon
 *
 * @param <T> 目前泛型支持 String,File,以后扩展：JSONObject,Bitmap,Byte[],XmlDom
 */
public abstract class AjaxCallBack<T> {
	
	private boolean progress = true;
	private int rate = 1000 * 1;//每秒
	
	public boolean isProgress() {
		return progress;
	}
	
	public int getRate() {
		return rate;
	}
	
	/**
	 * 设置进度,而且只有设置了这个了以后，onLoading才能有效。
	 * @param progress 是否启用进度显示
	 * @param rate 进度更新频率
	 */
	public AjaxCallBack<T> progress(boolean progress , int rate){
		this.progress = progress;
		this.rate = rate;
		return this;
	}
	
	/** 请求开始 */
	public void onStart(){};
	
	/** 请求成功 */
	public void onSuccess(T result){};
	
	/** 请求失败 */
	public void onFailure(Throwable t, int errorNo, String strMsg){};
	
	/**
	 * 请求中调用Onloading（每隔1秒回调）
	 * 
	 * @param count 下载文件的总大小
	 * @param current 下载文件的当前大小
	 */
	public void onLoading(long count, long current){};
	
	/**
	 * 网络请求后的解析
	 * 
	 * @param result 网络请求的字符串返回值
	 * @return 解析后的返回结果（自定义）
	 */
	public Object onParse(String result){
		return null;
	};
}

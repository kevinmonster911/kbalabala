package com.kbalabala.tools.http;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.concurrent.FutureCallback;

/**
 * Created by laziobird on 15-11-17.
 */
public class AsyncCallBack implements FutureCallback<HttpResponse> {
	HttpGet httpget;

	public void setHttpget(HttpGet httpget) {
		this.httpget = httpget;
	}

	public AsyncCallBack() {
	}

	/**
	 * 请求成功的监听触发函数，可以在里面添加业务逻辑
	 */
	public void completed(final HttpResponse response) {
		System.out.println(httpget + "->" + response.getStatusLine());
	}

	/**
	 * 请求失败的监听触发函数，可以在里面异常处理
	 */
	public void failed(final Exception ex) {
		System.out.println(httpget + "->" + ex);
	}

	public void cancelled() {
		System.out.println(httpget.getRequestLine() + " cancelled");
	}
}

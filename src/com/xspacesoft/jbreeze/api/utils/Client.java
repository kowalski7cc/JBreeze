package com.xspacesoft.jbreeze.api.utils;

import java.io.IOException;
import java.net.URL;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Client {

	public static final MediaType DATA =
			MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");
	
	private OkHttpClient client;
	private String url;

	public Client(String url) {
		client = new OkHttpClient();
		this.url = url;
	}

	public Client(URL url) {
		client = new OkHttpClient();
		this.url = url.toString();
	}

	public String get() throws IOException {
		Request request = new Request.Builder()
				.url(url)
				.build();
		try (Response response = client.newCall(request).execute()) {
			return response.body().string();
		}
	}
	
	public String post(String status) throws IOException {
		RequestBody body = RequestBody.create(DATA, status);
	    Request request = new Request.Builder()
	        .url(url)
	        .post(body)
	        .build();
	    try (Response response = client.newCall(request).execute()) {
	      return response.body().string();
	    }
	}

}

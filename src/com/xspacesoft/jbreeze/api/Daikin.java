package com.xspacesoft.jbreeze.api;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;

import com.xspacesoft.jbreeze.api.utils.Client;
import com.xspacesoft.jbreeze.api.utils.Decoder;
import com.xspacesoft.jbreeze.api.utils.Encoder;

public class Daikin implements Serializable {

	private static final long serialVersionUID = -3601821928697121588L;
	private InetAddress inetAddress;
	private String[] methods = new String[] {
			"/common/basic_info",
			"/aircon/get_sensor_info",
			"/aircon/get_control_info"
	};

	public Daikin(InetAddress inetAddress) {
		if(inetAddress == null)
			throw new IllegalArgumentException("Invalid URL");
		this.inetAddress = inetAddress;
	}

	public DaikinStatus getStatus() {
		StringBuilder stringBuilder = new StringBuilder();
		System.out.println("-START GET DATA-----------------------");
		try {
			for(String method : methods) {
				String result = new Client("http://" + inetAddress.getHostAddress() + method).get();
				while(result==null||!result.contains("ret=OK")) {
					result = new Client("http://" + inetAddress.getHostAddress() + method).get();
				}
				stringBuilder.append(result + ",");
				System.out.println(result);
//				sleep(100);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			System.out.println("-STOP GET DATA-----------------------");
		}
		return Decoder.decode(stringBuilder.toString());
	}

	public boolean setStatus(DaikinStatus status) {
		String statusString = Encoder.encode(status);
		try {
			System.out.println("-START POST DATA-----------------------");
			System.out.println(statusString);
			System.out.println("=RESPONSE DATA=======================");
			String result = new Client("http://" + inetAddress.getHostAddress() + "/aircon/set_control_info")
					.post(statusString);
			System.out.println(result);
			return result.contains("ret=OK");
		} catch (IOException e) {
			return false;
		} finally {
			System.out.println("-STOP POST DATA-----------------------");
		}

	}
	
//	private void sleep(int millis) {
//		try {
//			Thread.sleep(millis);
//		} catch (InterruptedException e) { }
//	}

	public InetAddress getInetAddress() {
		return inetAddress;
	}

	@Override
	public int hashCode() {
		return inetAddress.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this)
			return true;
		if(obj instanceof Daikin)
			return ((Daikin) obj).inetAddress.equals(inetAddress);
		return false;
	}

	@Override
	public String toString() {
		return inetAddress.getHostAddress().toString();
	}
	
	

}

package com.xspacesoft.jbreeze.api.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import com.xspacesoft.jbreeze.api.DaikinStatus;
import com.xspacesoft.jbreeze.api.options.FanDirection;
import com.xspacesoft.jbreeze.api.options.FanSpeed;
import com.xspacesoft.jbreeze.api.options.Mode;
import com.xspacesoft.jbreeze.api.options.Power;
import com.xspacesoft.jbreeze.api.options.SpecialMode;

public class Decoder {

	public static DaikinStatus decode(String data) {
		DaikinStatus daikinStatus = new DaikinStatus();
		String[] values = data.split(",");
		for(String value : values) {
			String[] pair = value.split("=");
//			System.out.println(value);
			switch(pair[0]) {
			case "pow":
				daikinStatus.setPower(Power.getPower(pair[1]));
				break;
			case "mode":
				daikinStatus.setMode(Mode.getMode(pair[1]));
				break;
			case "f_dir":
				daikinStatus.setFanDirection(FanDirection.getFanDirection(pair[1]));
				break;
			case "f_rate":
				daikinStatus.setFanSpeed(FanSpeed.getFanSpeed(pair[1]));
				break;
			case "name":
				try {
					daikinStatus.setName(URLDecoder.decode(pair[1], "UTF-8"));
				} catch (UnsupportedEncodingException e) { }
				break;
			case "stemp":
				daikinStatus.getTemperature()
					.setTarget((pair[1].equals("M")|pair[1].equals("--"))?null:parseFloat(pair[1]));
				break;
			case "shum":
				daikinStatus.getHumidity().setTarget((pair[1].equals("--"))?null:parseInt(pair[1]));
				break;
			case "mac":
				daikinStatus.setMac(pair[1]);
				break;
			case "htemp":
				daikinStatus.getTemperature().setActual(parseFloat(pair[1]));
				break;
			case "otemp":
				daikinStatus.getTemperature().setOutside(parseFloat(pair[1]));
				break;
			case "dt1":
				daikinStatus.getTemperature().setAutoValue(parseFloat(pair[1]));
				break;
			case "dt3":
				daikinStatus.getTemperature().setCooldValue(parseFloat(pair[1]));
				break;
			case "dt4":
				daikinStatus.getTemperature().setHeatValue(parseFloat(pair[1]));
				break;
			case "port":
				daikinStatus.setPort(parseInt(pair[1]));
				break;
			case "adv":
				daikinStatus.setSpecialMode(pair.length<2?SpecialMode.NONE:SpecialMode.getSpecialMode(pair[1]));
				daikinStatus.setSpecialModeActive(pair.length<2?false:!SpecialMode.getSpecialMode(pair[1]).equals(SpecialMode.NONE));
				break;
			}
		}
		return daikinStatus;
	}
	
	public static Integer parseInt(String string) {
		try {
			return Integer.parseInt(string);
		} catch (NumberFormatException e) {
			return null;
		}
	}
	
	public static Float parseFloat(String string) {
		try {
			return Float.parseFloat(string);
		} catch (NumberFormatException e) {
			return null;
		}
	}

}

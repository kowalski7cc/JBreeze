package com.xspacesoft.jbreeze.api.utils;

import com.xspacesoft.jbreeze.api.DaikinStatus;import com.xspacesoft.jbreeze.api.options.SpecialMode;

public class Encoder {

	public static String encode(DaikinStatus daikinStatus) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(daikinStatus.getPower().getPostOption());
		stringBuilder.append("&");
		stringBuilder.append(daikinStatus.getMode().getPostOption());
		stringBuilder.append("&");
		stringBuilder.append(daikinStatus.getTargetPostOption());
		stringBuilder.append("&");
		stringBuilder.append("shum=0");
		stringBuilder.append("&");
		stringBuilder.append(daikinStatus.getFanSpeed().getPostOption());
		stringBuilder.append("&");
		stringBuilder.append(daikinStatus.getFanDirection().getPostOption());
		
		// Optional values
		if(daikinStatus.getSpecialMode()!=SpecialMode.NONE)
			stringBuilder.append("&" + daikinStatus.getSpecialMode().getPostOption());
		
		return stringBuilder.toString();
	}
	
}

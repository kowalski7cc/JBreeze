package com.xspacesoft.jbreeze.api.options;

import java.util.Arrays;

import com.xspacesoft.jbreeze.api.PostOption;

public enum FanSpeed implements PostOption {
	AUTO("A"),
	SILENCE("B"),
	SPEED_1("3"),
	SPEED_2("4"),
	SPEED_3("5"),
	SPEED_4("6"),
	SPEED_5("7");
	
	private String id;

	private FanSpeed(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	@Override
	public String getPostOption() {
		return "f_rate=" + id;
	}
	
	public static FanSpeed getFanSpeed(String id) {
		return Arrays.asList(FanSpeed.values())
				.stream()
				.filter(p -> p.getId().equals(id))
				.findFirst()
				.orElse(AUTO);
	}

	@Override
	public String toString() {
		switch (this) {
		case AUTO:
			return "Auto";
		case SILENCE:
			return "Silence";
		case SPEED_1:
			return "1";
		case SPEED_2:
			return "2";
		case SPEED_3:
			return "3";
		case SPEED_4:
			return "4";
		case SPEED_5:
			return "5";
		default:
			return "Auto";
		}
	}
	
}

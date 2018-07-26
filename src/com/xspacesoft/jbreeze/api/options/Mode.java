package com.xspacesoft.jbreeze.api.options;

import java.util.Arrays;

import com.xspacesoft.jbreeze.api.PostOption;

public enum Mode implements PostOption {

	AUTO(1),
	DRY(2),
	COOL(3),
	HEAT(4),
	FAN(6);
	
	private int id;

	private Mode(int id) {
		this.id = id;
	}

	@Override
	public String getPostOption() {
		return "mode=" + id;
	}
	
	public static Mode getMode(int id) {
		return Arrays.asList(Mode.values())
				.stream()
				.filter(p -> p.getId() == id)
				.findFirst()
				.orElse(AUTO);
	}
	
	public static Mode getMode(String id) {
		try {
			return getMode(Integer.parseInt(id));
		} catch (NumberFormatException e) {
			return null;
		}
	}

	public int getId() {
		return id;
	}

	@Override
	public String toString() {
		switch (this) {
		case AUTO:
			return "Auto";
		case COOL:
			return "Cool";
		case DRY:
			return "Dry";
		case FAN:
			return "Fan";
		case HEAT:
			return "Heat";
		default:
			return "Auto";
		}
	}
	
}

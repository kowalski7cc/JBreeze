package com.xspacesoft.jbreeze.api.options;

import java.util.Arrays;

import com.xspacesoft.jbreeze.api.PostOption;

public enum Power implements PostOption {

	ON(1),
	OFF(0);
	
	private int id;

	private Power(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	@Override
	public String getPostOption() {
		return "pow=" + id;
	}
	
	public static Power getPower(int id) {
		return Arrays.asList(Power.values())
				.stream()
				.filter(p -> p.getId() == id)
				.findFirst()
				.orElse(OFF);
	}
	
	public static Power getPower(String id) {
		try {
			return getPower(Integer.parseInt(id));
		} catch (NumberFormatException e) {
			return null;
		}
	}

	@Override
	public String toString() {
		switch (this) {
		case OFF:
			return "Off";
		case ON:
			return "On";
		default:
			return "Off";
		}
	}
	
}

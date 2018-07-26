package com.xspacesoft.jbreeze.api.options;

import java.util.Arrays;

import com.xspacesoft.jbreeze.api.PostOption;

public enum FanDirection implements PostOption {

	NONE(0),
	HORIZONTAL(2),
	VERTICAL(1),
	VERTICAL_AND_HORIZONTAL(3);
	
	private int id;

	private FanDirection(int id) {
		this.id = id;
	}

	@Override
	public String getPostOption() {
		return "f_dir=" + id;
	}
	
	public static FanDirection getFanDirection(int id) {
		return Arrays.asList(FanDirection.values())
				.stream()
				.filter(p -> p.getId() == id)
				.findFirst()
				.orElse(NONE);
	}
	
	public static FanDirection getFanDirection(String id) {
		try {
			return getFanDirection(Integer.parseInt(id));
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
		case HORIZONTAL:
			return "Horizontal";
		case NONE:
			return "None";
		case VERTICAL:
			return "Vertical";
		case VERTICAL_AND_HORIZONTAL:
			return "Vertical and Horizontal";
		default:
			return "None";
		}
	}
	
	
}

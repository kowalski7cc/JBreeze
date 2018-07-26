package com.xspacesoft.jbreeze.api.options;

import java.util.Arrays;

import com.xspacesoft.jbreeze.api.PostOption;

public enum SpecialMode implements PostOption {

	NONE(""),
	POWERFUL("2"),
	ECONOMY("12");
	
	private String id;
	
	private SpecialMode(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public static SpecialMode getSpecialMode(String id) {
		return Arrays.asList(SpecialMode.values())
				.stream()
				.filter(p -> p.getId().equals(id))
				.findFirst()
				.orElse(NONE);
	}
	
	@Override
	public String getPostOption() {
		return ((id==null)||(id.equals("")))?"":"adv=" + id;
	}
}

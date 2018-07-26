package com.xspacesoft.jbreeze.api;

import com.xspacesoft.jbreeze.api.options.FanDirection;
import com.xspacesoft.jbreeze.api.options.FanSpeed;
import com.xspacesoft.jbreeze.api.options.Mode;
import com.xspacesoft.jbreeze.api.options.Power;
import com.xspacesoft.jbreeze.api.options.PowerTimer;
import com.xspacesoft.jbreeze.api.options.SpecialMode;

public class DaikinStatus {
	
	private Power power;
	private String name;
	private String mac;
	private Integer port;
	private String groupName;
	private FanDirection fanDirection;
	private FanSpeed fanSpeed;
	private Mode mode;
	private PowerTimer powerTimer;
	private Temperature temperature;
	private Humidity humidity;
	private SpecialMode specialMode;
	
	public DaikinStatus() {
		temperature = new Temperature();
		humidity = new Humidity();
	}
	
	public Power getPower() {
		return power;
	}
	public void setPower(Power power) {
		this.power = power;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getMac() {
		return mac;
	}
	public void setMac(String mac) {
		this.mac = mac;
	}
	public Integer getPort() {
		return port;
	}
	public void setPort(Integer port) {
		this.port = port;
	}
	public String getGroupName() {
		return groupName;
	}
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	public FanDirection getFanDirection() {
		return fanDirection;
	}
	public void setFanDirection(FanDirection fanDirection) {
		this.fanDirection = fanDirection;
	}
	public FanSpeed getFanSpeed() {
		return fanSpeed;
	}
	public void setFanSpeed(FanSpeed fanSpeed) {
		this.fanSpeed = fanSpeed;
	}
	public Mode getMode() {
		return mode;
	}
	public void setMode(Mode mode) {
		this.mode = mode;
	}
	public PowerTimer getPowerTimer() {
		return powerTimer;
	}
	public void setPowerTimer(PowerTimer powerTimer) {
		this.powerTimer = powerTimer;
	}
	public Temperature getTemperature() {
		return temperature;
	}
	public void setTemperature(Temperature temperature) {
		this.temperature = temperature;
	}
	public Humidity getHumidity() {
		return humidity;
	}
	public void setHumidity(Humidity humidity) {
		this.humidity = humidity;
	}
	public SpecialMode getSpecialMode() {
		return specialMode;
	}
	public void setSpecialMode(SpecialMode specialMode) {
		this.specialMode = specialMode;
	}
	public Float getTargetTemperature() {
		return temperature.getTargetTemp(mode);
	}
	public String getTargetPostOption() {
		return temperature.getTargetPostOption(mode);
	}
}

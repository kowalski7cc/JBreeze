package com.xspacesoft.jbreeze.api;

import com.xspacesoft.jbreeze.api.options.Mode;

public class Temperature {
	
	/** stemp value */
	private Float target;
	/** htemp value */
	private Float actual;
	/** otemp value */
	private Float outside;
	
	/** dt1 value */
	private Float autoValue;
	/** dt3 value */
	private Float cooldValue;
	/** dt4 value */
	private Float heatValue;
	
	/** stemp value */
	public Float getTarget() {
		return target;
	}
	
	/** stemp value */
	public void setTarget(Float target) {
		this.target = target;
	}
	
	/** htemp value */
	public Float getActual() {
		return actual;
	}
	
	/** htemp value */
	public void setActual(Float actual) {
		this.actual = actual;
	}
	
	/** otemp value */
	public Float getOutside() {
		return outside;
	}
	
	/** otemp value */
	public void setOutside(Float outside) {
		this.outside = outside;
	}

	/** dt1 value */
	public Float getAutoValue() {
		return autoValue;
	}

	/** dt1 value */
	public void setAutoValue(Float autoValue) {
		this.autoValue = autoValue;
	}

	/** dt3 value */
	public Float getCooldValue() {
		return cooldValue;
	}

	/** dt3 value */
	public void setCooldValue(Float cooldValue) {
		this.cooldValue = cooldValue;
	}

	/** dt4 value */
	public Float getHeatValue() {
		return heatValue;
	}

	/** dt4 value */
	public void setHeatValue(Float heatValue) {
		this.heatValue = heatValue;
	}
	
	public Float getTargetTemp(Mode mode) {
		if(target==null) {
			switch (mode) {
			case COOL:
				return cooldValue;
			case HEAT:
				return heatValue;
			case AUTO:
				return autoValue;
			default:
				// 'M' on DRY and '--' on FAN 
				return null;
			}
		} else {
			return target;
		}
	}
	
	public String getTargetPostOption(Mode mode) {
		if(target==null) {
			switch (mode) {
			case COOL:
				return "stemp=" + (int) cooldValue.floatValue();
			case HEAT:
				return "stemp=" + (int) heatValue.floatValue();
			case AUTO:
				return "stemp=" + (int) autoValue.floatValue();
			case DRY:
				return "stemp=M";
			case FAN:
				return "stemp=--";
			default:
				return "stemp=--";
			}
		} else {
			return "stemp=" + (int) target.floatValue();
		}
	}
	
}
